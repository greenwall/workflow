package com.nordea.dompap.workflow;

import com.nordea.dompap.jdbc.JdbcUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WorkFlowStatusServiceImpl implements WorkFlowStatusService {
    private static final long MAX_AGE_MILLIS = 30000;
    private static final int MAX_CACHE = 10;

    private final DataSource dataSource;

	private final Map<WorkFlowStatusQuery, WorkFlowStatusQueryResult> cachedQueries = new HashMap<>();

    @Override
    public List<WorkFlowMethodCount> getWorkflowStatus(int[] periods) throws ResourceException {
    	return getWorkflowStatus(periods, null);
    }
    
    @Override
    public List<WorkFlowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException {
    	WorkFlowStatusQuery query = new WorkFlowStatusQuery();
    	query.setPeriods(periods);
    	query.setWorkflowClass(workflowClass);
    	query.setShowMethods(false);
    	query.setIncludeForArchive(false);
        query.setIncludeLastDays(30); // unlimited - if positive only include workflows updated within last x days.
        query.setIncludeRecentMinutes(60);
        query.setExcludeLabelled(false); 
                
        return getWorkflowStatus(query);
    }

    @Override
    public List<WorkFlowMethodCount> getWorkflowStatus(WorkFlowStatusQuery query) throws ResourceException {
        WorkFlowStatusQueryResult cachedResult = cachedQueries.get(query);
        if (cachedResult!=null) {
            if (isExpired(cachedResult)) {
                cachedQueries.remove(query);
            } else {
                return cachedResult.getQueryResult();
            }
        }

        List<WorkFlowMethodCount> wfmc = loadWorkflowStatus(query);
        WorkFlowStatusQueryResult result = new WorkFlowStatusQueryResult(wfmc);

        if (cachedQueries.size()>MAX_CACHE) {
            evictResults();
        }
        cachedQueries.put(query, result);
        return result.getQueryResult();
    }

    private void evictResults() {
        WorkFlowStatusQueryResult oldest = null;
        for (WorkFlowStatusQueryResult result : cachedQueries.values()) {
            if (oldest==null || result.getTimeOfQueryMillis()<oldest.getTimeOfQueryMillis()) {
                oldest = result;
            }
        }
        if (oldest!=null) {
            cachedQueries.remove(oldest);
        }
    }

    private boolean isExpired(WorkFlowStatusQueryResult cachedResult) {
        long ageMillis = System.currentTimeMillis() - cachedResult.getTimeOfQueryMillis();
        return ageMillis > MAX_AGE_MILLIS;
    }


    public List<WorkFlowMethodCount> loadWorkflowStatus(WorkFlowStatusQuery query) throws ResourceException {

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        // int[] periods = {5760, 2880, 1440, 720, 360, 180, 60, 30, 10};
        
        sql.append("select * from ( ");
        sql.append("select /*+ORDERED INDEX(w WFLW_WORKFLOW_IX25 )*/ WORKFLOW_CLASS, w.SUB_TYPE, ");
        
        if (query.isShowMethods()) {
            sql.append("METHOD, ");
        }

        final String STARTED_BEFORE = " METHOD_STARTED<=sysdate-1/1440* ";
        final String STARTED_AFTER = " METHOD_STARTED>sysdate-1/1440* ";
        final String ENDED_BEFORE = " METHOD_ENDED<=sysdate-1/1440* ";
        final String ENDED_AFTER = " METHOD_ENDED>sysdate-1/1440* ";
        
        sql.append("sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and " + ENDED_BEFORE).append(query.getPeriods()[0])
                .append(" then 1 else 0 end) as \"").append(">=").append(query.getPeriods()[0]).append("\",");

        for (int n = 0; n < query.getPeriods().length - 1; n++) {
            sql.append("sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and " + ENDED_AFTER).append(query.getPeriods()[n]);
            sql.append(" and " + ENDED_BEFORE).append(query.getPeriods()[n + 1]);
            sql.append(" then 1 else 0 end) as \"<").append(query.getPeriods()[n]).append("\",");
        }
        int last = query.getPeriods().length - 1;
        sql.append("sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and " + ENDED_AFTER).append(query.getPeriods()[last])
                .append(" then 1 else 0 end) as \"").append("<").append(query.getPeriods()[last]).append("\",");

        // Add recent column (last hour)
        if (query.getIncludeRecentMinutes()>0) {
        	sql.append("sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and " + ENDED_AFTER).append(query.getIncludeRecentMinutes()).append(" then 1 else 0 end) as \"RECENT\", ");
        }
        
        sql.append("sum(case when METHOD_EXCEPTION is not null and IS_PICKED=1 then 1 else 0 end) as \"FAILED\",");
        sql.append("sum(case when METHOD_EXCEPTION is not null and IS_PICKED=0 then 1 else 0 end) as \"RETRY\",");
        sql.append("sum(case when METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_ENDED is null and "+STARTED_BEFORE+"2 then 1 else 0 end) as \"STALLED\",");
        sql.append("sum(case when METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_ENDED is null and "+STARTED_AFTER+"2 then 1 else 0 end) as \"RUNNING\",");
        sql.append("sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN<=sysdate then 1 else 0 end) as \"READY\",");
        sql.append("sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN>sysdate and METHOD not like '!%' then 1 else 0 end) as \"LATER\",");
        sql.append("sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN>sysdate and METHOD like '!%' then 1 else 0 end) as \"FINALIZED\",");
        sql.append("sum(case when IS_PICKED=0 and START_WHEN<=trunc(sysdate) + 1 and METHOD like '!%' then 1 else 0 end) as \"ARCHIVABLE\" ");
        sql.append("from WFLW_WORKFLOW w ");
        if (query.isExcludeLabelled()) {
        	sql.append("left outer join WFLW_LABEL l on l.ID=LABEL ");
        }
        
        sql.append("where 1=1 "); // subsequent criterias are just and prefixed.
        if (query.isExcludeLabelled()) {
        	sql.append("and (l.EXPIRE_TIME>sysdate or l.EXPIRE_TIME is null) and (l.IGNORE_WORKFLOWS=0 or l.IGNORE_WORKFLOWS is null) ");
        } else {
	        if (query.getLabelId()!=null) {
	            sql.append(" and LABEL = ? ");
	        	params.add(query.getLabelId().toString());	            
	        }
        }
        if (!query.isIncludeForArchive()) {
        	sql.append("and METHOD not like '!%' ");
        }
        if (query.getWorkflowClass()!=null) {
        	sql.append("and WORKFLOW_CLASS = ? ");
    		params.add(query.getWorkflowClass());
        }
        if (query.getIncludeLastDays()>0) {
            // Filter on CREATION_TIME not LAST_UPDATE_TIME since index exists on CREATION_TIME
//        	sql.append("and LAST_UPDATE_TIME>sysdate-"+query.getIncludeLastDays()+" ");
            sql.append("and w.CREATION_TIME>sysdate-").append(query.getIncludeLastDays()).append(" ");
        }

        sql.append("group by WORKFLOW_CLASS, w.SUB_TYPE ");        	
        if (query.isShowMethods()) {
            sql.append(", METHOD ");
        }
        sql.append("order by WORKFLOW_CLASS, w.SUB_TYPE");

        // Surrounding select
        sql.append(") ");
        sql.append("where 1=1 "); // subsequent criterias are just and prefixed.
        
        if (query.getIncludeRecentMinutes()>0) {
//        	sql.append("and (RECENT>0) ");
        	sql.append("and (RECENT>0 or RETRY>0 or READY>0 or RUNNING>0 or STALLED>0) ");
//        	sql.append("and (RECENT>0  or FAILED>0 or RETRY>0 or RUNNING>0 or READY>0 or LATER>0 or STALLED>0) ");
        }
        		
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int n = 1;
            for (Object param : params) {
                ps.setObject(n++, param);
            }
        	
            return JdbcUtil.listQuery(ps, new WorkFlowMethodCountMapper(query.getPeriods(), query.isShowMethods()));
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }

    private static final class WorkFlowStatusQueryResult {
        private final List<WorkFlowMethodCount> queryResult;
        private final long timeOfQueryMillis;

        private WorkFlowStatusQueryResult(List<WorkFlowMethodCount> queryResult) {
            this.queryResult = queryResult;
            timeOfQueryMillis = System.currentTimeMillis();
        }

        public List<WorkFlowMethodCount> getQueryResult() {
            return queryResult;
        }

        public long getTimeOfQueryMillis() {
            return timeOfQueryMillis;
        }
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
