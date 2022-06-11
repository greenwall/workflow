package com.nordea.dompap.workflow;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.next.dompap.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class WorkflowLabelServiceImpl implements WorkflowLabelService {

	private final DataSource dataSource;

    @Override
	public WorkflowLabel create(UserId createdBy, Date expireTime, String name, int ignoreWorkflows, String description) throws ResourceException {
		if (createdBy==null) {
			throw new IllegalArgumentException("WorkFlowLabel must be created by a user. Null not allowed.");
		}
		UUID id = UUID.randomUUID();
		Date creationTime = new Date();
		WorkflowLabel label = new WorkflowLabel(id, createdBy, creationTime, expireTime, name, ignoreWorkflows, description);
			
		String sql = "insert into WFLW_LABEL (ID, CREATION_TIME, EXPIRE_TIME, CREATED_BY, NAME,  DESCRIPTION, IGNORE_WORKFLOWS) values (?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, id.toString());
	        ps.setTimestamp(2, JdbcUtil.toTimestamp(creationTime));
	        ps.setTimestamp(3, JdbcUtil.toTimestamp(expireTime));
			ps.setString(4, createdBy.getId());
	        ps.setString(5,  name);
	        ps.setString(6, description);
	        ps.setInt(7, ignoreWorkflows);
			ps.execute();

			log.info("Label "+name+" created.");
			return label;
		} catch (SQLException e) {
	        throw new ResourceException(e + ":" + sql, e);
	    }		
	}
	
	@Override
	public void update(WorkflowLabel label) throws ResourceException {
		String sql = "update WFLW_LABEL set EXPIRE_TIME=?, NAME=?, DESCRIPTION=?, IGNORE_WORKFLOWS=? where ID = ? ";
		
		try (Connection con = getDataSource().getConnection();
	                PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setTimestamp(1, JdbcUtil.toTimestamp(label.getExpireTime()));
			ps.setString(2, label.getName());
			ps.setString(3, label.getDescription());
			ps.setInt(4,  label.getIgnoreWorkflows());
			ps.setString(5, label.getId().toString());
			ps.execute();
		} catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }		
	}
	
	@Override
	public List<WorkflowLabel> searchLabels(UserId createdBy, Interval creationTime, Interval expireTime, String name) throws ResourceException {
        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();
		
		select.append("SELECT * FROM WFLW_LABEL where 1=1 ");
		if (createdBy!=null) {
			params.add(createdBy.getId());
			select.append("and CREATED_BY=? ");
		}
		if (creationTime!=null) {
			if (creationTime.getStart()!=null) {
				params.add(JdbcUtil.toTimestamp(creationTime.getStart().toDate()));
				select.append("and CREATION_TIME>=? ");
			}
			if (creationTime.getEnd()!=null) {
				params.add(JdbcUtil.toTimestamp(creationTime.getEnd().toDate()));
				select.append("and CREATION_TIME<? ");
			}
		}
		if (expireTime!=null) {
			if (expireTime.getStart()!=null) {
				params.add(JdbcUtil.toTimestamp(expireTime.getStart().toDate()));
				select.append("and (EXPIRE_TIME>=? or EXPIRE_TIME is null) ");
			}
			if (expireTime.getEnd()!=null) {
				params.add(JdbcUtil.toTimestamp(expireTime.getEnd().toDate()));
				select.append("and (EXPIRE_TIME<? or EXPIRE_TIME is null) ");
			}
		}
		if (StringUtils.isNotBlank(name)) {
			if (StringUtils.contains(name, "%")) {
				params.add(name);
				select.append("and NAME like ? ");
			} else {
				params.add(name);
				select.append("and NAME = ? ");				
			}
		}
		
        String sql = select.toString();
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int n = 1;
            for (Object param : params) {
                ps.setObject(n++, param);
            }

			return JdbcUtil.listQuery(ps, workFlowLabelMapper);
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }		
	}
	
	@Override
	public WorkflowLabel getLabel(UUID id) throws ResourceException {
		String sql = "SELECT * from WFLW_LABEL WHERE ID = ?";
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
        	ps.setString(1, id.toString());
            return JdbcUtil.exactQuery(ps, workFlowLabelMapper);
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }				
	}

	@Override
	public int addLabelToWorkFlows(WorkflowLabel label, List<UUID> workflowIds) throws ResourceException {
		// TODO Make update with in array work, to avoid doing sequential updates!!!
		String sql = "UPDATE WFLW_WORKFLOW set LABEL=? WHERE ID = ?";

		try (Connection con = getDataSource().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			con.setAutoCommit(false);
			int rows = 0;
			for (UUID id: workflowIds) {
				// Convert UUID's to String and get an array	    		
	        	ps.setString(1, label.getId().toString());
	        	ps.setString(2, id.toString());
	    		rows += ps.executeUpdate();
			}
			con.commit();
        	return rows;
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }				
    }

	@Override
	public int removeLabelFromWorkFlows(WorkflowLabel label, List<UUID> workflowIds) throws ResourceException {
		// TODO Make update with in array work, to avoid doing sequential updates!!!
		String sql = "UPDATE WFLW_WORKFLOW set LABEL=null WHERE ID = ?";

		try (Connection con = getDataSource().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			con.setAutoCommit(false);
			int rows = 0;
			for (UUID id: workflowIds) {
	        	ps.setString(1, id.toString());
	    		rows += ps.executeUpdate();
			}
			con.commit();
        	return rows;
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }				
	}
	
	@Override
	public void deleteLabel(WorkflowLabel label) throws ResourceException {
		// TODO Remove label from any workflow then delete label
		final UUID id = label.getId();
		String sql = "";
		int rows;
        try (Connection con = getDataSource().getConnection()) {
        	con.setAutoCommit(false);
    		String updateSql = "UPDATE WFLW_WORKFLOW set LABEL = null WHERE LABEL = ?";
    		sql = updateSql;
        	try (PreparedStatement ps = con.prepareStatement(updateSql)) {
        		ps.setString(1, id.toString());
        		rows = ps.executeUpdate();
        	}
        	
    		String deleteSql = "DELETE FROM WFLW_LABEL WHERE ID = ?";
    		sql = deleteSql;
        	try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
        		ps.setString(1, id.toString());
                ps.execute();
        	}
        	con.commit();
        	label.setId(null);

			log.info("Label "+label.getName()+" deleted and removed from "+rows+" workflows.");
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }		
	}

    private static final JdbcUtil.RowMapper<WorkflowLabel> workFlowLabelMapper = (rs, rowNum) -> {
		UUID id = UUID.fromString(rs.getString("ID"));
		UserId userId =  new UserId(rs.getString("CREATED_BY"));
		Date creationTime = rs.getTimestamp("CREATION_TIME");
		Date expireTime = rs.getTimestamp("EXPIRE_TIME");
		String name = rs.getString("NAME");
		int ignoreWorkflows = rs.getInt("IGNORE_WORKFLOWS");
		String description = rs.getString("DESCRIPTION");

		return new WorkflowLabel(id, userId, creationTime, expireTime, name, ignoreWorkflows, description);
	};
	
    private DataSource getDataSource() {
    	return dataSource;
    }
}
