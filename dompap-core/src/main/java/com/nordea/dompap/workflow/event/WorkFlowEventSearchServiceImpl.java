package com.nordea.dompap.workflow.event;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.workflow.config.WorkFlowContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class WorkFlowEventSearchServiceImpl implements WorkFlowEventSearchService {

    private final WorkFlowContext context;

	@Override
	public WorkFlowEventSearchResult searchEvents(WorkFlowEventSearch search, Integer startRow, Integer maxRows) throws ResourceException {

        int total = countEvents(search);

        int minRow = startRow != null ? startRow : 0;
        int maxRow = maxRows != null ? minRow + maxRows : minRow + 100;

        List<Object> params = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        // Paging selects to wrap real select defining rownum twice!
        // select * from (select tmp.*, rownum rn from (
        builder.append("select * from (select tmp.*, rownum rn from (");

        // Subset of workflows
        builder.append("select * from WFLW_EVENT ");

        addSearchQuery(builder, params, search);

        // Ensure ordering for paging to work
        builder.append("order by CREATION_TIME desc");

        // Paging wrapper
        // ) tmp where rownum<=200) where rn > 190
        builder.append(") tmp where rownum<=?) where rn > ?");        
        
        params.add(maxRow);
        params.add(minRow);
		
        String sql = builder.toString();
        
        log.info(sql);
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int n = 1;
            for (Object param : params) {
                ps.setObject(n++, param);
            }
            
            List<WorkFlowEvent> workflows = JdbcUtil.listQuery(ps, WorkFlowEventServiceImpl.eventMapper);
            return new WorkFlowEventSearchResult(total, workflows);
        } catch (SQLException e) {
            throw wrapSqlException(e, sql);
        }
	}	

    private int countEvents(WorkFlowEventSearch search) throws ResourceException {
        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();

        select.append("select count(*) from WFLW_EVENT ");
        addSearchQuery(select, params, search);

        String sql = select.toString();
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int n = 1;
            for (Object param : params) {
        		ps.setObject(n++, param);
            }

            return JdbcUtil.countQuery(ps);
        } catch (SQLException e) {
            throw wrapSqlException(e, sql);
        }
    }
	
    private void addSearchQuery(StringBuilder select, List<Object> params, WorkFlowEventSearch search) {
    	// Hack to avoid where/and test
    	select.append(" where 1=1 ");
    	
        if (search.getCreationTime() != null && search.getCreationTime().getStart() != null) {
            select.append(" and CREATION_TIME>=? ");
            params.add(JdbcUtil.toTimestamp(search.getCreationTime().getStart().toDate()));
        }
        if (search.getCreationTime() != null && search.getCreationTime().getEnd() != null) {
            select.append(" and CREATION_TIME<=? ");
            params.add(JdbcUtil.toTimestamp(search.getCreationTime().getEnd().toDate()));
        }
        
        addSearchCriteria(select, params, search.getEventId(), "ID");

        addSearchCriteria(select, params, search.getWorkFlowId(), "WORKFLOW_ID");
        
        addSearchCriteria(select, params, search.getEventType(), "EVENT_TYPE");

        addSearchCriteria(select, params, search.getEventName(), "EVENT_NAME");
    }

    private void addSearchCriteria(StringBuilder select, List<Object> params, String criteriaValue, String columnName) {
        // External key
        if (criteriaValue != null) {
            if (criteriaValue.contains("%")) {
                select.append(" and ").append(columnName).append(" like ? ");
            } else {
                select.append(" and ").append(columnName).append(" = ? ");
            }
            params.add(criteriaValue);
        }    	
    }

    private ResourceException wrapSqlException(SQLException e, String sql) {
        return new ResourceException(e + ":" + sql, e);
    }

	private DataSource getDataSource() {
	    return context.getDataSource();
	}
	
}
