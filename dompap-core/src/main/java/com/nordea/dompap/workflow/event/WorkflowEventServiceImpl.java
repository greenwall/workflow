package com.nordea.dompap.workflow.event;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.jdbc.SqlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class WorkflowEventServiceImpl implements WorkflowEventService {

	private final DataSource dataSource;

	@Override
	public WorkflowEvent createEvent(UUID id, byte[] content, String eventType, UUID workflowId, String eventName) throws ResourceException {
		if (StringUtils.isBlank(eventType)) {
			throw new IllegalArgumentException("eventType can not be blank.");
		}
		if (id==null) {
			id = UUID.randomUUID();
		}
		Date creationTime = new Date();
		
		String sql = "insert into WFLW_EVENT "
			+" (ID, CREATION_TIME, CONTENT, WORKFLOW_ID, EVENT_TYPE, EVENT_NAME)"
			+" values (?, ?, ?, ?, ?, ?)";	
		
		try (Connection con = getDataSource().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maxLength(id.toString(), 36));
			ps.setTimestamp(2, JdbcUtil.toTimestamp(creationTime));
			if (content!=null) {
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				ps.setBinaryStream(3, bais, content.length);
			} else {
				ps.setBinaryStream(3, null, 0);
			}			
			ps.setString(4, workflowId!=null ? maxLength(workflowId.toString(), 36) : null);
			ps.setString(5, maxLength(eventType, 50));
			ps.setString(6, maxLength(eventName, 100));

			ps.execute();
				
			return new WorkflowEvent(id, creationTime, content, workflowId, eventType, eventName, null);
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}					
	}	

	private ResourceException wrapSqlException(SQLException e, String sql) {
		return new ResourceException(e + ":" + sql, e);
	}
	
	@Override
	public WorkflowEvent updateEventInfo(WorkflowEvent event, UUID workflowId, String eventName, String userId, String applicationId, String technicalUserId, String requestId, String requestDomain, String sessionId) throws ResourceException {
		// TODO Missing test coverage
		String sql = "update WFLW_EVENT set "
			+" WORKFLOW_ID=?, EVENT_NAME=?, USER_ID=?, APPLICATION_ID=?, TECHNICAL_USER_ID=?, REQUEST_ID=?, REQUEST_DOMAIN=?, SESSION_ID=? where ID=?";
		
		try (Connection con = getDataSource().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, workflowId!=null ? maxLength(workflowId.toString(), 36) : null);
			ps.setString(2, maxLength(eventName, 100));			
			ps.setString(3, maxLength(userId, 50));
			ps.setString(4, maxLength(applicationId, 50));
			ps.setString(5, maxLength(technicalUserId, 50));
			ps.setString(6, maxLength(requestId, 100));
			ps.setString(7, maxLength(requestDomain, 9));
			ps.setString(8, maxLength(sessionId, 200));

			ps.setString(9, maxLength(event.id.toString(), 36));
						
			ps.execute();
			
			event.userId = userId;
			event.applicationId = applicationId;
			event.technicalUserId = technicalUserId;
			event.requestId = requestId;
			event.requestDomain = requestDomain;
			event.sessionId = sessionId;
			
			return event;
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}					
	}	
		
	@Override
	public WorkflowEvent getEvent(UUID uuid) throws ResourceException {
		String sql = " select ID, CREATION_TIME, CONTENT, WORKFLOW_ID, EVENT_TYPE, EVENT_NAME, PROCESSED_TIME, USER_ID, APPLICATION_ID, TECHNICAL_USER_ID, REQUEST_ID, REQUEST_DOMAIN, SESSION_ID "
				+" from WFLW_EVENT where ID=?";
		try (Connection con = getDataSource().getConnection();			
			PreparedStatement ps = con.prepareStatement(sql)) {							
			
			ps.setString(1, uuid.toString());
			return JdbcUtil.exactQuery(ps, eventMapper);
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}
	}

	/**
	 * Returns the earliest received non-processed Event matching the given sessionId.
	 * So after processing this Event it must be marked as processed (or archived) before the next is returned.
	 * Events are processed in order of reception.
	 */
	@Override
	public WorkflowEvent getFirstEventFor(UUID workflowId) throws ResourceException {
	String sql = " select ID, CREATION_TIME, CONTENT, WORKFLOW_ID, EVENT_TYPE, EVENT_NAME, PROCESSED_TIME, USER_ID, APPLICATION_ID, TECHNICAL_USER_ID, REQUEST_ID, REQUEST_DOMAIN, SESSION_ID "
				+" from WFLW_EVENT where PROCESSED_TIME is null and WORKFLOW_ID=? order by CREATION_TIME ";

		try (Connection con = getDataSource().getConnection();			
			PreparedStatement ps = con.prepareStatement(sql)) {							

			ps.setString(1, maxLength(workflowId.toString(), 36));
			return SqlUtils.first(ps, eventMapper);
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}
	}

	/**
	 * Returns the last processed Event matching the given sessionId.
	 */
	@Override
	public WorkflowEvent getLastEventFor(UUID workflowId) throws ResourceException {
	String sql = " select ID, CREATION_TIME, CONTENT, WORKFLOW_ID, EVENT_TYPE, EVENT_NAME, PROCESSED_TIME, USER_ID, APPLICATION_ID, TECHNICAL_USER_ID, REQUEST_ID, REQUEST_DOMAIN, SESSION_ID "
				+" from WFLW_EVENT where PROCESSED_TIME is not null and WORKFLOW_ID=? order by PROCESSED_TIME desc ";

		try (Connection con = getDataSource().getConnection();
			PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maxLength(workflowId.toString(), 36));
			return SqlUtils.first(ps, eventMapper);
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}
	}

	/**
	 * Marks an event as processed so it will not be picked up again.
	 */
	@Override
	public void processedEvent(WorkflowEvent event) throws ResourceException {
	
		Date processedTime = new Date();
		String sql = "update WFLW_EVENT set PROCESSED_TIME=? where ID=?";
		
		try (Connection con = getDataSource().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setTimestamp(1, JdbcUtil.toTimestamp(processedTime));
			ps.setString(2, maxLength(event.id.toString(), 36));						
			ps.execute();			
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}					
	}

	/**
	 * Archive an event after processing, meaning that it is moved to table WFLW_ARCHIVED_NDS_EVENT and deleted from table WFLW_NDS_EVENT
	 */
	@Override
	public void archiveEvent(WorkflowEvent event) throws ResourceException {
		if (event==null) {
			throw new NullPointerException("No event instance.");
		}

		// If no id was given, event was never stored.
		if (event.id==null) {			
			throw new NullPointerException("Event has no id!");
		}
			
		String sql = ""; 
		try (Connection con = getDataSource().getConnection()) {
			con.setAutoCommit(false);
			
			sql = "insert into WFLW_ARCHIVED_EVENT (ID,	CREATION_TIME, CONTENT, WORKFLOW_ID, EVENT_TYPE, EVENT_NAME, PROCESSED_TIME) values (?,?,?,?,?,?,?) ";				
			try (PreparedStatement ps = con.prepareStatement(sql)) {			
				ps.setString(1, maxLength(event.id));
				ps.setTimestamp(2, JdbcUtil.toTimestamp(event.creationTime));
				if (event.content!=null) {
					ByteArrayInputStream bais = new ByteArrayInputStream(event.content);
					ps.setBinaryStream(3, bais, event.content.length);
				} else {
					ps.setBinaryStream(3, null, 0);
				}
				ps.setString(4, maxLength(event.workflowId));
				ps.setString(5, maxLength(event.eventType, 50));
				ps.setString(6, maxLength(event.eventName, 100));
				ps.setTimestamp(7, JdbcUtil.toTimestamp(event.processedTime));
				ps.execute();
			}
			
			sql = "delete from WFLW_EVENT where ID=?";
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, event.id.toString());
				ps.execute();
			}
			con.commit();
						
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}						
	}
		
	public static final JdbcUtil.RowMapper<WorkflowEvent> eventMapper = (rs, rowNum) -> {
		UUID id = UUID.fromString(rs.getString("ID"));
		Date creationTime = rs.getTimestamp("CREATION_TIME");
		byte[] content = rs.getBytes("CONTENT");
		UUID workflowId = getUUID(rs, "WORKFLOW_ID");
		String eventType = rs.getString("EVENT_TYPE");
		String eventName = rs.getString("EVENT_NAME");
		Date processedTime = rs.getTimestamp("PROCESSED_TIME");

		WorkflowEvent event = new WorkflowEvent(id, creationTime, content, workflowId, eventType, eventName, processedTime);
		event.userId = rs.getString("USER_ID");
		event.applicationId = rs.getString("APPLICATION_ID");
		event.technicalUserId = rs.getString("TECHNICAL_USER_ID");
		event.requestId = rs.getString("REQUEST_ID");
		event.requestDomain = rs.getString("REQUEST_DOMAIN");
		event.sessionId = rs.getString("SESSION_ID");
		event.content = content;
		return event;
	};
	
	private static UUID getUUID(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return value!=null ? UUID.fromString(value) : null;
	}

	private static String maxLength(UUID id) {
		return id!=null ? maxLength(id.toString(), 36) : null;
	}
	
	private static String maxLength(String str, int maxLen) {
		if (str!=null && str.length()>maxLen) {
			throw new IllegalArgumentException("["+str+"] can only contain "+maxLen+" characters, but had "+str.length()+" chars.");
		} else {
			return str;
		}
	}

	private DataSource getDataSource() {
		return dataSource;
	}

   //USER_ID, APPLICATION_ID, TECHNICAL_USER_ID, REQUEST_ID, REQUEST_DOMAIN, SESSION_ID=
	@Override
	public WorkflowEvent createEvent(WorkflowEventBuilder workFlowEventBuilder) throws ResourceException {
		if (StringUtils.isBlank(workFlowEventBuilder.eventType)) {
			throw new IllegalArgumentException("eventType can not be blank.");
		}
		if (workFlowEventBuilder.id==null) {
			workFlowEventBuilder.id = UUID.randomUUID();
		}
		Date creationTime = new Date();
		
		String sql = "insert into WFLW_EVENT "
			+" (ID, CREATION_TIME, CONTENT, WORKFLOW_ID, EVENT_TYPE, EVENT_NAME,USER_ID, APPLICATION_ID, "
			+" TECHNICAL_USER_ID, REQUEST_ID, REQUEST_DOMAIN, SESSION_ID)"
			+" values (?, ?, ?, ?, ?, ?,?,?,?,?,?,?)";	
		
		try (Connection con = getDataSource().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maxLength(workFlowEventBuilder.id.toString(), 36));
			ps.setTimestamp(2, JdbcUtil.toTimestamp(creationTime));
			if (workFlowEventBuilder.content!=null) {
				ByteArrayInputStream bais = new ByteArrayInputStream(workFlowEventBuilder.content);
				ps.setBinaryStream(3, bais, workFlowEventBuilder.content.length);
			} else {
				ps.setBinaryStream(3, null, 0);
			}			
			ps.setString(4, workFlowEventBuilder.workflowId!=null ? maxLength(workFlowEventBuilder.workflowId.toString(), 36) : null);
			ps.setString(5, maxLength(workFlowEventBuilder.eventType, 50));
			ps.setString(6, maxLength(workFlowEventBuilder.eventName, 100));
			ps.setString(7, maxLength(workFlowEventBuilder.userId, 50));
			ps.setString(8, maxLength(workFlowEventBuilder.applicationId, 50));
			ps.setString(9, maxLength(workFlowEventBuilder.technicalUserId, 50));
			ps.setString(10, maxLength(workFlowEventBuilder.requestId, 100));
			ps.setString(11, maxLength(workFlowEventBuilder.requestDomain, 9));
			ps.setString(12, maxLength(workFlowEventBuilder.sessionId, 200));

			ps.execute();
				
			return new WorkflowEvent(workFlowEventBuilder.id, workFlowEventBuilder.creationTime, workFlowEventBuilder.content, workFlowEventBuilder.workflowId, workFlowEventBuilder.eventType, workFlowEventBuilder.eventName, null);
		} catch (SQLException e) {
			throw wrapSqlException(e, sql);
		}	
	}
}
