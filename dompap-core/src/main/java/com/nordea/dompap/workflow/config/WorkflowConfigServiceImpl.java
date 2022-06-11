package com.nordea.dompap.workflow.config;

import com.nordea.dompap.jdbc.JdbcUtil;
import lombok.AllArgsConstructor;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class WorkflowConfigServiceImpl implements WorkflowConfigService {

	final DataSource dataSource;

	@Override
	public List<WorkflowClassServer> getWorkFlowClassServers(String className, String serverName, Boolean enabled) throws ResourceException {
		String sql = "select WORKFLOW_CLASS, SERVERNAME, ENABLED from WFLW_WORKFLOW_CLASS_SERVER where 1=1 ";
		if (className!=null) {
			sql += "and WORKFLOW_CLASS=? ";
		}
		if (serverName!=null) {
			sql += "and SERVERNAME=? ";
		}
		if (enabled!=null) {
			sql += "and ENABLED=? ";				
		}
		try (Connection con = getDataSource().getConnection();			
			PreparedStatement ps = con.prepareStatement(sql)) {
			int paramNo = 1;
			if (className!=null) {
				ps.setString(paramNo++, className);
			}
			if (serverName!=null) {
				ps.setString(paramNo++, serverName);
			}
			if (enabled!=null) {
				ps.setInt(paramNo++, enabled ? 1 : 0);
			}
			return JdbcUtil.listQuery(ps, (rows, rowNum) -> {
				String className1 = rows.getString("WORKFLOW_CLASS");
				String serverName1 = rows.getString("SERVERNAME");
				boolean enabled1 = rows.getInt("ENABLED")==1;
				return new WorkflowClassServer(className1, serverName1, enabled1);
			});
		} catch (SQLException e) {
			throw new ResourceException(e.toString() + ":" + sql, e);
		}							
	}

	@Override
	public List<String> getWorkFlowClassesFor(String serverName, Boolean enabled) throws ResourceException {
		String sql = "select WORKFLOW_CLASS from WFLW_WORKFLOW_CLASS_SERVER where 1=1 ";
		if (serverName!=null) {
			sql += "and SERVERNAME=? ";
		}
		if (enabled!=null) {
			sql += "and ENABLED=? ";				
		}
		try (Connection con = getDataSource().getConnection();			
			PreparedStatement ps = con.prepareStatement(sql)) {
			int paramNo = 1;
			if (serverName!=null) {
				ps.setString(paramNo++, serverName);
			}
			if (enabled!=null) {
				ps.setInt(paramNo++, enabled ? 1 : 0);
			}
			return JdbcUtil.listQuery(ps, stringMapper);
		} catch (SQLException e) {
			throw new ResourceException(e.toString() + ":" + sql, e);
		}							
	}

	@Override
	public void saveWorkFlowClassFor(String serverName, String workFlowClass, boolean enabled) throws ResourceException {
		int rowsUpdated;
		{
			String sql = "update WFLW_WORKFLOW_CLASS_SERVER set ENABLED = ? where WORKFLOW_CLASS = ? and SERVERNAME = ?";
			try (Connection con = getDataSource().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
	
				ps.setInt(1, enabled ? 1 : 0);
				ps.setString(2, workFlowClass);
				ps.setString(3, serverName);
				rowsUpdated = ps.executeUpdate();
				
			} catch (SQLException e) {
				throw new ResourceException(e.toString() + ":" + sql, e);
			}					
		}
		if (rowsUpdated==0) {
			String sql = "insert into WFLW_WORKFLOW_CLASS_SERVER (WORKFLOW_CLASS, SERVERNAME, ENABLED) values (?,?,?)";
			try (Connection con = getDataSource().getConnection();
					PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, workFlowClass);
				ps.setString(2, serverName);
				ps.setInt(3, enabled ? 1 : 0);
				ps.execute();
			} catch (SQLException e) {
				throw new ResourceException(e.toString() + ":" + sql, e);
			}					
		}
	
	}

	private static final JdbcUtil.RowMapper<String> stringMapper = (rs, rowNum) -> rs.getString("WORKFLOW_CLASS");

	private DataSource getDataSource() {
		return dataSource;
	}

}
