package com.nordea.dompap.workflow;

import com.nordea.dompap.jdbc.JdbcUtil;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.sql.ResultSet;
import java.sql.SQLException;

@Value
@AllArgsConstructor
class WorkFlowMethodCountMapper implements JdbcUtil.RowMapper<WorkFlowMethodCount> {
    int[] periods;
    boolean includeMethod;

    @Override
    public WorkFlowMethodCount mapRow(ResultSet rs, int rowNum) throws SQLException {
        String workflowClassName = rs.getString("WORKFLOW_CLASS");
        String subType = rs.getString("SUB_TYPE");
        String methodName = null;
        if (includeMethod) {
        	methodName = rs.getString("METHOD");
        }

        int failed = rs.getInt("FAILED");
        int retry = rs.getInt("RETRY");
        int stalled = rs.getInt("STALLED");
        int ready = rs.getInt("READY");
        int running = rs.getInt("RUNNING");
        int later = rs.getInt("LATER");
        int finalized = rs.getInt("FINALIZED");
        int archivable = rs.getInt("ARCHIVABLE");
        
        int countGreaterThanFirst = rs.getInt(">=" + periods[0]);

        int[] counts = new int[periods.length];
        for (int n = 0; n < periods.length; n++) {
            counts[n] = rs.getInt("<" + periods[n]);
        }

        WorkFlowMethodCount wfmc = new WorkFlowMethodCount(workflowClassName, subType, methodName);
        wfmc.failed = failed;
        wfmc.retry = retry;
        wfmc.stalled = stalled;
        wfmc.ready = ready;
        wfmc.later = later;
        wfmc.running = running;
        wfmc.finalized = finalized;
        wfmc.archivable = archivable;
        wfmc.countGreaterThanFirstPeriod = countGreaterThanFirst;
        wfmc.periodsInMinutes = periods;
        wfmc.countLessThanPeriod = counts;

        return wfmc;
    }
}