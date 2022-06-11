package com.nordea.dompap.workflow.selector;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.workflow.Workflow;
import com.nordea.dompap.workflow.WorkflowMapper;
import com.nordea.dompap.workflow.WorkflowUtil;
import com.nordea.dompap.workflow.config.WorkflowConfig;
import com.nordea.dompap.workflow.event.WorkflowEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Selects a workflow by caching a number of ready workflows, and upon selection trying to lock any workflow from this bucket.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWorkflowSelector implements WorkflowSelector {
	protected static final Method onEvent = WorkflowUtil.getMethod(WorkflowEventListener.class, "onEvent");

	protected final WorkflowConfig config;
	protected final DataSource dataSource;

	public <T> Workflow<T> pickReadyWorkFlow(String workflowClassName, String subType, ExecutorInfo executorInfo) throws ResourceException {
    	List<Workflow<T>> result = pickReadyWorkFlows(workflowClassName, subType, executorInfo, 1);
    	
    	if (result == null || result.isEmpty())
    		return null;
    	
    	return result.get(0);
    }
	
	
    /**
     * Selects a workflow ready for execution.
     * Ensures that only one worker picks a ready workflow, by selecting and
     * updating the selection using IS_PICKED as a lock.
     */
	@Override
	public <T> List<Workflow<T>> pickReadyWorkFlows(String workflowClassName, String subType, ExecutorInfo executorInfo, int maxCount)
			throws ResourceException {

		try (Connection con = getDataSource().getConnection()) {

        	// Try selecting and locking a workflow ready for method.
        	long pickReadyMethodWorkFlowStart = System.currentTimeMillis();
        	List<Workflow<T>> workflows = pickReadyMethodWorkFlows(con, workflowClassName, subType, executorInfo, maxCount);
        	for(Workflow<T> workflow : workflows) {
        		log.info("{}:{}, pickReadyMethodWorkFlow:{}, time={}", workflowClassName, subType, workflow.getId(), System.currentTimeMillis()-pickReadyMethodWorkFlowStart);
        	}
        	if (workflows.isEmpty()) {
        		// Try selecting and locking a workflow with a queued event.
            	long pickReadyEventWorkFlowStart = System.currentTimeMillis();
        		workflows = pickReadyEventWorkFlows(con, workflowClassName, subType, executorInfo, maxCount);
            	for(Workflow<T> workflow : workflows) {
            		log.info("{}:{}, pickReadyEventWorkFlow:{}, time={}", workflowClassName, subType, workflow.getId(), System.currentTimeMillis()-pickReadyEventWorkFlowStart);
            	}
        	}
        	
//        	Monitor.timeUsed("pickReadyEventWorkFlow workflows", workflows.size());
            return workflows;
        } catch (SQLException e) {
            throw new ResourceException(e.toString(), e);
        }
    }

    protected abstract <T> List<Workflow<T>> pickReadyMethodWorkFlows(Connection con, String workflowClassName, String subType, ExecutorInfo executorInfo, int maxCount) throws ResourceException, SQLException;
	

	protected abstract <T> List<Workflow<T>> pickReadyEventWorkFlows(Connection con, String workflowClassName, String subType, ExecutorInfo executorInfo, int maxCount) throws ResourceException, SQLException;
    
    /**
     * subType==null => all subtypes
     * subType empty => only null or empty subtypes
     * subType contains % => like '%' i.e. empty or anything, but not null
     * subType contains no wildcard => = ? 
     */
    protected String buildSubTypeCriteria(String subType) {
    	if (subType==null) {
    		return "";
    	} else {
    		if (StringUtils.isEmpty(subType)) {
    			return "and (SUB_TYPE=? or SUB_TYPE is null) ";  
    		} else {
    			// build LIKE query only if subType contains wildcard
    			if (subType.contains("%")) {
    				return "and SUB_TYPE like ? ";
    			} else {
    				return "and SUB_TYPE=? ";
    			}
    		}
    	}
    }
    
	protected static final JdbcUtil.RowMapper<Workflow<?>> workFlowMapper = new JdbcUtil.RowMapper<Workflow<?>>() {
    	private final WorkflowMapper mapper = new WorkflowMapper();
        @Override
        public Workflow<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
        	Workflow<?> wf = mapper.mapRow(rs, rowNum);
        	
            String serverName = rs.getString("SERVERNAME");
            String version = rs.getString("VERSION");
            wf.setServerName(serverName);
            wf.setVersion(version);

            return wf; 
        }
    };
    
    protected DataSource getDataSource() throws ResourceException {
    	return dataSource;
    }
}
