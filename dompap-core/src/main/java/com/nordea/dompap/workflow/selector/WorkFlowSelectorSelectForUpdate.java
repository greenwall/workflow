package com.nordea.dompap.workflow.selector;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.jdbc.SqlUtils;
import com.nordea.dompap.jdbc.StringMapper;
import com.nordea.dompap.workflow.WorkFlow;
import com.nordea.dompap.workflow.WorkFlowUtil;
import com.nordea.dompap.workflow.config.WorkFlowConfig;
import com.nordea.dompap.workflow.event.WorkFlowEventListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Selects a ready workflow using select for update to lock that workflow row upon selection.
 */
@Slf4j
public class WorkFlowSelectorSelectForUpdate extends AbstractWorkFlowSelector implements WorkFlowSelector {
	private static final Method onEvent = WorkFlowUtil.getMethod(WorkFlowEventListener.class, "onEvent");
	private final boolean skipLocked;

	public WorkFlowSelectorSelectForUpdate(WorkFlowConfig config, DataSource dataSource) {
		super(config, dataSource);
		skipLocked = config.isSelectorSkipLocked();
	}

	private int getSampling() {
		// Only samples between 0-100 is allowed - everything else returns 0 = no sampling.
		int i = config.getWorkflowSample();
		if (i>0 && i<100) {
			return i;
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> List<WorkFlow<T>> pickReadyMethodWorkFlows(Connection con, String workflowClassName, String subType, ExecutorInfo executorInfo, int maxCount) throws ResourceException, SQLException {
		String sql;
		List<WorkFlow<?>> workflows;
		Date methodStarted;
		List<WorkFlow<T>> result;
		
		String subTypeCriteria = buildSubTypeCriteria(subType);

		con.setAutoCommit(false);

		int sample = getSampling();
		if (sample==0) {
			sql = "select /*+ index(WFLW_WORKFLOW WFLW_WORKFLOW_IX14) */ * from WFLW_WORKFLOW "
					+ "where WORKFLOW_CLASS=? "
					+ subTypeCriteria
					+ " and IS_PICKED = 0 "
					//            		+ "and (START_WHEN is null or START_WHEN<=?) "
					+ "and (START_WHEN<=?) "
					+ "and ROWNUM <=" + maxCount +" "
					+ "for update";
		} else {
			// Select the first ready workflow (with METHOD)
			sql = "select /*+ index(WFLW_WORKFLOW WFLW_WORKFLOW_IX14) */ * from WFLW_WORKFLOW SAMPLE("+sample+") "
					+ "where WORKFLOW_CLASS=? "
					+ subTypeCriteria
					+ " and IS_PICKED = 0 "
					+ "and (START_WHEN<=?) "
					+ "and ROWNUM <=" + maxCount +" "
					+ "for update";
		}
		
		if (skipLocked) {
			sql += " skip locked";
		}

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setQueryTimeout(10);
			
			int i=1;
			ps.setString(i++, workflowClassName);
			if (StringUtils.isNotBlank(subTypeCriteria)) {
				ps.setString(i++, subType);
			}
			ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			workflows = JdbcUtil.listQuery(ps, workFlowMapper);
		}

		if (workflows.isEmpty()) {
			// No workflows ready.
			con.rollback();
			con.setAutoCommit(true);
			return Collections.emptyList();
		} else {
			// Try picking it - i.e. marking it ready assuming that no other job did this simultaneously.
			// Note to prevent that workflow was picked up, executed and released meanwhile we check the execution sequence number (a version) is still the same.
			methodStarted = new Date();

			try {
				result = new ArrayList<>(workflows.size());
				for(WorkFlow<?> workflow : workflows) {
					sql = "update WFLW_WORKFLOW set IS_PICKED=1, METHOD_STARTED=?, METHOD_ENDED=null, METHOD_EXCEPTION=null, METHOD_EXCEPTION_MESSAGE=null, SERVERNAME=?, VERSION=?, EXEC_SEQNO = ? where ID = ? and (EXEC_SEQNO = ? or EXEC_SEQNO is null) and METHOD is not null and IS_PICKED = 0";
					try (PreparedStatement ps = con.prepareStatement(sql)) {
						ps.setTimestamp(1, JdbcUtil.toTimestamp(methodStarted));
						ps.setString(2, executorInfo.getServerName());
						ps.setString(3, executorInfo.getVersion());
						ps.setInt(4, workflow.getExecSeqno()+1);
						ps.setString(5, workflow.getId().toString());
						ps.setInt(6, workflow.getExecSeqno());
	
						int updatedRows = ps.executeUpdate();
						
						if (updatedRows == 1) {
							// Workflow picked (i.e. marked as started).
							workflow.setMethodStarted(methodStarted);
							workflow.setServerName(executorInfo.getServerName());
							workflow.setVersion(executorInfo.getVersion());
							result.add((WorkFlow<T>)workflow);
						}
					}
				}
				con.commit();
			} catch (Exception e) {
				// Rollback if any exception.
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
			return result;
		}
		
	}   

	@SuppressWarnings("unchecked")
	@Override
	protected <T> List<WorkFlow<T>> pickReadyEventWorkFlows(Connection con, String workflowClassName, String subType, ExecutorInfo executorInfo, int maxCount) throws ResourceException, SQLException {

		List<WorkFlow<T>> result;
		String sql;
		List<WorkFlow<?>> workflows;
		Date methodStarted;

		String methodName = onEvent.getName();
		String eventId;

		String subTypeCriteria = buildSubTypeCriteria(subType);

		con.setAutoCommit(false);

		// Select the first ready workflow (with EVENTS_QUEUED and PROCESS_EVENTS)
		// Even if a METHOD is waiting interrupt this if PROCESS_EVENTS>0
		sql = "select /*+ index(WFLW_WORKFLOW WFLW_WORKFLOW_IX15) */ * from WFLW_WORKFLOW "
				+ "where WORKFLOW_CLASS=? "
				+ subTypeCriteria
				+ "and EVENTS_QUEUED>0 and PROCESS_EVENTS=1 "
				+ "and ROWNUM <="+maxCount+" for update";

		if (skipLocked) {
			sql += " skip locked";
		}
		
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setQueryTimeout(10);
			
			ps.setString(1, workflowClassName);
			if (StringUtils.isNotBlank(subTypeCriteria)) {
				ps.setString(2, subType);
			}
			workflows = JdbcUtil.listQuery(ps, workFlowMapper);
		}

		if (workflows.isEmpty()) {
			con.rollback();
			con.setAutoCommit(true);
			// No workflows ready.
			return Collections.emptyList();
		}
		
		result = new ArrayList<>(workflows.size());
		try {
			for(WorkFlow<?> workflow : workflows) {
				// Try picking it - i.e. marking it ready assuming that no other job did this simultaneously.
				// Note to prevent that workflow was picked up, executed and released meanwhile we check the execution sequence number (a version) is still the same.
				methodStarted = new Date();

				// Select the first event for this workflow
				sql = "select ID from WFLW_EVENT where PROCESSED_TIME is null and WORKFLOW_ID=? order by CREATION_TIME ";
				try (PreparedStatement ps = con.prepareStatement(sql)) {
					ps.setString(1, workflow.getId().toString());
					eventId = SqlUtils.first(ps, new StringMapper("ID"));
				}

				if (eventId==null) {     
					// If no event then another thread got it first - try again
					log.info("Workflow "+workflow.getId()+" had "+workflow.getEventsQueued()+" but selecting first found none - assume workflow started by another thread");
				} else {
					// In same transaction update the event to be processed, 
					// so if workflow is locked the event is marked as processed and the currentEvent of the workflow points to it.
					Date processedTime = new Date();
					sql = "update WFLW_EVENT set PROCESSED_TIME=? where ID=?";
					try (PreparedStatement ps = con.prepareStatement(sql)) {
						ps.setTimestamp(1, JdbcUtil.toTimestamp(processedTime));
						ps.setString(2, eventId);
						ps.executeUpdate();
					}

					// Try picking it - i.e. marking it ready assuming that no other job did this simultaneously.
					// Note to prevent that workflow was picked up, executed and released meanwhile we check the execution sequence number (a version) is still the same.
					// Decrement EVENTS_QUEUED and disable PROCESS_EVENTS.
					sql = "update WFLW_WORKFLOW set METHOD_STARTED=?, IS_PICKED=1, METHOD_ENDED=null, METHOD_EXCEPTION=null, METHOD_EXCEPTION_MESSAGE=null, SERVERNAME=?, VERSION=?, EXEC_SEQNO = ?, "
							+"METHOD=?, EVENTS_QUEUED=EVENTS_QUEUED-1, PROCESS_EVENTS=0, CURRENT_EVENT=? "
							+"where ID = ? and (EXEC_SEQNO = ? or EXEC_SEQNO is null)";
					try (PreparedStatement ps = con.prepareStatement(sql)) {
						ps.setTimestamp(1, JdbcUtil.toTimestamp(methodStarted));
						ps.setString(2, executorInfo.getServerName());
						ps.setString(3, executorInfo.getVersion());
						ps.setInt(4, workflow.getExecSeqno()+1);
						ps.setString(5, methodName);
						ps.setString(6, eventId);
						ps.setString(7, workflow.getId().toString());
						ps.setInt(8, workflow.getExecSeqno());

						int updatedRows = ps.executeUpdate();
						
						if (updatedRows == 1) {
							// Workflow picked (i.e. marked as started).
							workflow.setMethodName(methodName);
							workflow.setMethodStarted(methodStarted);
							workflow.setServerName(executorInfo.getServerName());
							workflow.setVersion(executorInfo.getVersion());
							workflow.setExecSeqno(workflow.getExecSeqno()+1);
							workflow.setCurrentEventId(UUID.fromString(eventId));

							result.add((WorkFlow<T>)workflow);
						}
					}
				}
			}
			con.commit();
			
			return result;
		} catch(Exception e) {
			con.rollback();
			throw e;
		} finally {
			con.setAutoCommit(true);
		}
	}

}
