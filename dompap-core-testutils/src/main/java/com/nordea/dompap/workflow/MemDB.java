package com.nordea.dompap.workflow;

import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

public class MemDB {
	private static final String CREATE_WFLW_WORKFLOW = "CREATE TABLE WFLW_WORKFLOW (ID VARCHAR(36) NOT NULL, EXTERNAL_KEY VARCHAR(200), WORKFLOW_CLASS VARCHAR(200) NOT NULL, SUB_TYPE VARCHAR(100), USER_ID VARCHAR(10) DEFAULT '', BRANCH_ID VARCHAR(10) DEFAULT '', CREATION_TIME TIMESTAMP(6) NOT NULL, LAST_UPDATE_TIME TIMESTAMP(6) NOT NULL, METHOD VARCHAR(200) NOT NULL, METHOD_STARTED TIMESTAMP(6), METHOD_ENDED TIMESTAMP(6), METHOD_EXCEPTION VARCHAR(200), METHOD_EXCEPTION_MESSAGE VARCHAR(2000), STACKTRACE CLOB, START_WHEN TIMESTAMP(6), SERVERNAME VARCHAR(30), VERSION VARCHAR(30), REQUEST_DOMAIN VARCHAR(255), EXEC_SEQNO INT, LABEL VARCHAR2(36), "
			+ "IS_PICKED int DEFAULT 0, "
			+ "EVENTS_QUEUED int DEFAULT 0, LATEST_EVENT timestamp, PROCESS_EVENTS int, CURRENT_EVENT VARCHAR(36), "
			+ "PRIMARY KEY (ID))";
	
	private static final String DROP_WFLW_WORKFLOW = "DROP TABLE WFLW_WORKFLOW";
	private static final String CREATE_WFLW_WORKFLOW_CONTROLLER = "CREATE TABLE WFLW_WORKFLOW_CONTROLLER (ID VARCHAR(36) NOT NULL, CONTROLLER BLOB, PRIMARY KEY (ID), FOREIGN KEY (ID) REFERENCES WFLW_WORKFLOW (ID))";
	private static final String DROP_WFLW_WORKFLOW_CONTROLLER = "DROP TABLE WFLW_WORKFLOW_CONTROLLER";
//	private static final String CREATE_WFLW_PROPERTYTYPE = "CREATE TABLE WFLW_PROPERTYTYPE (ID INTEGER NOT NULL, NAME VARCHAR(100) NOT NULL, DESCRIPTION VARCHAR(300), PRIMARY KEY (ID), CONSTRAINT SYS_C005480 UNIQUE (NAME))";
	private static final String CREATE_WFLW_PROPERTYTYPE = "CREATE TABLE WFLW_PROPERTYTYPE (ID INT PRIMARY KEY, NAME VARCHAR(100) UNIQUE NOT NULL, DESCRIPTION VARCHAR(300));";
	private static final String DROP_WLFW_PROPERTYTYPE = "DROP TABLE WFLW_PROPERTYTYPE";
	private static final String CREATE_WFLW_METADATA = "CREATE TABLE WFLW_METADATA (WORKFLOW_ID VARCHAR(36) NOT NULL, PROPERTY_ID INTEGER NOT NULL, VALUE VARCHAR(200) NOT NULL, FOREIGN KEY (WORKFLOW_ID) REFERENCES WFLW_WORKFLOW (ID), FOREIGN KEY (PROPERTY_ID) REFERENCES WFLW_PROPERTYTYPE (ID))";
	private static final String DROP_WFLW_METADATA = "DROP TABLE WFLW_METADATA";
	private static final String CREATE_WFLW_WORKFLOW_CONTENT = "CREATE TABLE WFLW_WORKFLOW_CONTENT (ID VARCHAR(36) NOT NULL, CONTENT BLOB, PRIMARY KEY (ID), FOREIGN KEY (ID) REFERENCES WFLW_WORKFLOW (ID))";
	private static final String DROP_WFLW_WORKFLOW_CONTENT = "DROP TABLE WFLW_WORKFLOW_CONTENT";
	private static final String CREATE_WFLW_WORKFLOW_HISTORY = "CREATE TABLE WFLW_WORKFLOW_HISTORY (ID VARCHAR(36) NOT NULL, METHOD VARCHAR(200) NOT NULL, METHOD_STARTED TIMESTAMP(6) NOT NULL, METHOD_ENDED TIMESTAMP(6) NOT NULL, METHOD_EXCEPTION VARCHAR(200), METHOD_EXCEPTION_MESSAGE VARCHAR(2000), STACKTRACE CLOB, SERVERNAME VARCHAR(30), VERSION VARCHAR(255), FOREIGN KEY (ID) REFERENCES WFLW_WORKFLOW (ID))";
	private static final String DROP_WFLW_WORKFLOW_HISTORY = "DROP TABLE WFLW_WORKFLOW_HISTORY";
	private static final String CREATE_WFLW_LABEL = "CREATE TABLE WFLW_LABEL (ID VARCHAR(36) PRIMARY KEY, CREATION_TIME TIMESTAMP NOT NULL, EXPIRE_TIME TIMESTAMP, CREATED_BY VARCHAR(10) NOT NULL, NAME VARCHAR(100) UNIQUE NOT NULL, IGNORE_WORKFLOWS INT DEFAULT 0, DESCRIPTION VARCHAR(300))";
	private static final String DROP_WFLW_LABEL = "DROP TABLE WFLW_LABEL";

	private static final String CREATE_WFLW_EVENT = "CREATE TABLE WFLW_EVENT (ID varchar(36), CREATION_TIME timestamp, CONTENT blob, WORKFLOW_ID varchar(36), EVENT_TYPE varchar(50), EVENT_NAME varchar(100), PROCESSED_TIME timestamp, USER_ID varchar(50),	APPLICATION_ID varchar(50),	TECHNICAL_USER_ID varchar(50), REQUEST_ID varchar(100), REQUEST_DOMAIN varchar(3), SESSION_ID varchar(200),	primary key (ID))";
	private static final String DROP_WFLW_EVENT = "DROP TABLE WFLW_EVENT";

//	private static final String CREATE_WFLW_NDS_EVENT = "CREATE TABLE WFLW_NDS_EVENT";
//	private static final String DROP_WFLW_NDS_EVENT = "DROP TABLE WFLW_NDS_EVENT";

//	private static String dburl = "jdbc:hsqldb:mem:testcase;shotdown=true;sql.syntax_ora=true";

//	@Autowired
	private DataSource dataSource;

	public MemDB(DataSource dataSource) {
		this.dataSource = dataSource;
	}
/*
	public MemDB(String dburl) {
		if (StringUtils.isNotEmpty(dburl)) {
			MemDB.dburl = dburl;
		}
	}
/*	
	public void createDB() throws ClassNotFoundException, SQLException, IOException {
		String[] scriptNames = {
			"V001__create_version.sql",
			"V002__create_tables.sql",
			"V003__workflow_history.sql",
			"V004__create_nds_tables.sql",
			"V005__add_archive_and_delay.sql",
			"V006__workflow_metadata.sql",
			"V007__server_version.sql",
			"V008__workflow_controller.sql",
			"V009__workflow_config.sql",
			"V010__add_server_version_to_archive.sql",
			"V011_add_index_metadata.sql",
//			"V012__incr_field_sizes.sql",
//			"V013__watch_dogs_feature.sql",
			"V014__exec_seqno.sql"
		};
		
		Class.forName("org.hsqldb.jdbcDriver");
		try (Connection dbcon = getConnection()) {
			for (String scriptName: scriptNames) {
				scriptName = "/db/"+scriptName;
				executeSQL(dbcon, IOUtils.toString(MemDB.class.getResourceAsStream(scriptName)));
			}
		}
	}
*/
	
	public void createDB() throws ClassNotFoundException, SQLException, IOException {
		try (Connection dbcon = getConnection()) {
			executeSQL(dbcon, CREATE_WFLW_WORKFLOW);
			executeSQL(dbcon, CREATE_WFLW_PROPERTYTYPE);
			executeSQL(dbcon, CREATE_WFLW_METADATA);
//			executeSQL(dbcon, CREATE_);
			executeSQL(dbcon, CREATE_WFLW_WORKFLOW_CONTENT);
			executeSQL(dbcon, CREATE_WFLW_WORKFLOW_CONTROLLER);
			executeSQL(dbcon, CREATE_WFLW_WORKFLOW_HISTORY);
			executeSQL(dbcon, CREATE_WFLW_LABEL);
			executeSQL(dbcon, CREATE_WFLW_EVENT);
		}
		try (Connection dbcon = getConnection()) {
			executeSQL(dbcon, "SELECT * from WFLW_WORKFLOW");
		}
	}
	
/*	
	public void deleteDB() throws ClassNotFoundException, SQLException {		
		Class.forName("org.hsqldb.jdbcDriver");
		try (Connection dbcon = getConnection()) {
			executeSQL(dbcon, "DROP TABLE WFLW_ARCHIVED_WORKFLOW_HISTORY IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WFLW_ARCHIVED_WORKFLOW_CONTENT IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WFLW_ARCHIVED_WORKFLOW IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WFLW_WORKFLOW_HISTORY IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WFLW_WORKFLOW_CONTENT IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WFLW_METADATA IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WLFW_PROPERTYTYPE IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WFLW_WORKFLOW_CONTROLLER IF EXISTS CASCADE");
			executeSQL(dbcon, "DROP TABLE WFLW_NDS_EVENT IF EXISTS CASCADE");			
			executeSQL(dbcon, "DROP TABLE WFLW_WORKFLOW IF EXISTS CASCADE");
		}
	}
*/
	public void deleteDB() throws ClassNotFoundException, SQLException {		
		Class.forName("org.hsqldb.jdbcDriver");
		try (Connection dbcon = getConnection()) {
			executeSQL(dbcon, DROP_WFLW_METADATA);
			executeSQL(dbcon, DROP_WLFW_PROPERTYTYPE);
			executeSQL(dbcon, DROP_WFLW_WORKFLOW_CONTROLLER);
			executeSQL(dbcon, DROP_WFLW_WORKFLOW_CONTENT);
			executeSQL(dbcon, DROP_WFLW_WORKFLOW_HISTORY);
			executeSQL(dbcon, DROP_WFLW_LABEL);
			executeSQL(dbcon, DROP_WFLW_EVENT);
			executeSQL(dbcon, DROP_WFLW_WORKFLOW);
		}
	}
	
	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public String getContentRaw(String uuid) throws SQLException {
		try (Connection dbcon = getConnection()) {
			try (PreparedStatement ps = dbcon.prepareStatement("SELECT CONTENT FROM WFLW_WORKFLOW_CONTENT WHERE ID = ?")) {
				ps.setString(1, uuid);
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					byte[] bytes = rs.getBytes(1);
					if (bytes != null && bytes.length > 0) {
						return new String(bytes);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Executes a sql stament with no result set.
	 * @param dbcon
	 * @param sql
	 * @throws SQLException
	 */
	private static void executeSQL(Connection dbcon, String sql) throws SQLException {
		String[] stats = StringUtils.split(sql, ";");
		for (String stat : stats) {
			stat = stat.trim();
			if (StringUtils.isNotBlank(stat)) {
//			if (!StringUtils.startsWith(stat, "DROP") && StringUtils.isNotBlank(stat)) {
//				try {
					System.out.println(stat);					
					dbcon.prepareStatement(stat).execute();
					dbcon.commit();
//				} catch (SQLSyntaxErrorException e) {
//					if (!e.getMessage().contains("object not found")) {
//						throw e;
//					}
//				}
			}
		}
	}
}
