package com.nordea.dompap.workflow;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.ProfitCenter;
import com.nordea.next.dompap.domain.UserId;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@SuppressWarnings("rawtypes")
public class WorkflowMapper implements JdbcUtil.RowMapper<Workflow> {

	@Override
    public Workflow<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
    	Workflow<?> wf = mapBasicRow(rs);
    	
        String serverName = rs.getString("SERVERNAME");
        String version = rs.getString("VERSION");
        wf.setServerName(serverName);
        wf.setVersion(version);

        return wf; 
    }
    
    private static Workflow<?> mapBasicRow(ResultSet rs) throws SQLException {
    	UUID id = UUID.fromString(rs.getString("ID"));
        String externalKey = rs.getString("EXTERNAL_KEY");
        String workflowClassName = rs.getString("WORKFLOW_CLASS");
        String subType = rs.getString("SUB_TYPE");
        UserId userId = createUserId(rs.getString("USER_ID"));
        String branch = rs.getString("BRANCH_ID");
        BranchId branchId = BranchUtil.toBranchId(branch);
        ProfitCenter profitCenter = BranchUtil.toProfitCenter(branch);
        Date creationTime = rs.getTimestamp("CREATION_TIME");
        Date lastUpdateTime = rs.getTimestamp("LAST_UPDATE_TIME");
        String methodName = rs.getString("METHOD");
        Date startWhen = rs.getTimestamp("START_WHEN");
        Date methodStarted = rs.getTimestamp("METHOD_STARTED");
        Date methodEnded = rs.getTimestamp("METHOD_ENDED");
        String exceptionName = rs.getString("METHOD_EXCEPTION");
        String exceptionMessage = rs.getString("METHOD_EXCEPTION_MESSAGE");

        String requestDomain = rs.getString("REQUEST_DOMAIN");
    	UUID labelId = uuidFromString(rs.getString("LABEL"));           
        int execSeqno = rs.getInt("EXEC_SEQNO");
        int eventsQueued = rs.getInt("EVENTS_QUEUED");
        Date latestEvent = rs.getTimestamp("LATEST_EVENT");
        int processEvents = rs.getInt("PROCESS_EVENTS");
        UUID currentEventId = uuidFromString(rs.getString("CURRENT_EVENT"));

        Workflow<?> wf;
        if (branchId!=null) {
            wf = new Workflow<>(id, externalKey, workflowClassName, subType, userId, branchId, creationTime, lastUpdateTime);
        } else {
            wf = new Workflow<>(id, externalKey, workflowClassName, subType, userId, profitCenter, creationTime, lastUpdateTime);
        }

        wf.setMethodName(methodName);
        wf.setStartWhen(startWhen);
        wf.setMethodStarted(methodStarted);
        wf.setMethodEnded(methodEnded);
        wf.setExceptionName(exceptionName);
        wf.setExceptionMessage(exceptionMessage);
        wf.setRequestDomain(requestDomain);
        wf.setLabelId(labelId);
		wf.setExecSeqno(execSeqno);
		wf.setEventsQueued(eventsQueued);
		wf.setLatestEvent(latestEvent);
		wf.setProcessEvents(processEvents!=0);
		wf.setCurrentEventId(currentEventId);
        return wf;
    }
    
    private static UserId createUserId(String userId) {
        return isNotEmpty(userId) ? new UserId(userId) : null;
    }

    private static boolean isNotEmpty(String value) {
        return isNotBlank(value) && isNotBlank(value.trim());
    }
    
    private static UUID uuidFromString(String uuid) {
    	return uuid!=null ? UUID.fromString(uuid) : null;
    }

}

