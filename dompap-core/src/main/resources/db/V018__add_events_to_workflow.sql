INSERT INTO WFLW_VERSION (SCRIPT) 
VALUES ('V018_add_events_to_workflow.sql');

ALTER TABLE WFLW_WORKFLOW add EVENTS_QUEUED int default 0; -- Incremented by NDS3 callback service - decremented when processing
ALTER TABLE WFLW_WORKFLOW add LATEST_EVENT timestamp; -- Latest event added by NDS 3 callback service
ALTER TABLE WFLW_WORKFLOW add PROCESS_EVENTS int; -- Old workflows or workflows busy executing steps and/or retrying may prevent event processing by setting to 0 or NULL
ALTER TABLE WFLW_WORKFLOW add CURRENT_EVENT VARCHAR(36); -- current event being processed by the workflow

ALTER TABLE WFLW_WORKFLOW add SUB_TYPE VARCHAR2(100); -- Workflows may be divided into sub types which still correspond to the same WORKFLOW_CLASS.

CREATE INDEX WFLW_WORKFLOW_IX5 ON WFLW_WORKFLOW(SUB_TYPE);
CREATE INDEX WFLW_WORKFLOW_IX6 ON WFLW_WORKFLOW(REQUEST_DOMAIN);


ALTER TABLE WFLW_ARCHIVED_WORKFLOW add EVENTS_QUEUED int default 0; 
ALTER TABLE WFLW_ARCHIVED_WORKFLOW add LATEST_EVENT timestamp; 
ALTER TABLE WFLW_ARCHIVED_WORKFLOW add PROCESS_EVENTS int; 

ALTER TABLE WFLW_ARCHIVED_WORKFLOW add SUB_TYPE VARCHAR2(100); -- Workflows may be divided into sub types which still correspond to the same WORKFLOW_CLASS.

-- TABLE FOR GENERAL EVENT USING NDS_EVENT AS SIDE TABLE FOR PARSED CONTENT
DROP TABLE WFLW_EVENT;

CREATE TABLE WFLW_EVENT (
	ID varchar(36), 	
	CREATION_TIME timestamp, -- time when event was received and stored.	 		
	CONTENT blob,	
	WORKFLOW_ID varchar(36), -- could be foreign key to workflow - as could WFLW_WORKFLOW.CURRENT_EVENT - but having both refer to the other would complicate archiving.	
	EVENT_TYPE varchar(50), -- eventType to identify the processing needed of the event.
	EVENT_NAME varchar(100), -- eventType to identify the processing needed of the event.
	PROCESSED_TIME timestamp,
	-- Optional information from general serviceContext
	USER_ID varchar(50),
	APPLICATION_ID varchar(50),
	TECHNICAL_USER_ID varchar(50),
	REQUEST_ID varchar(100),
	REQUEST_DOMAIN varchar(3),
	SESSION_ID varchar(200),
	primary key (ID)	 			
);

CREATE INDEX WFLW_EVENT_IX1 ON WFLW_EVENT(CREATION_TIME);
CREATE INDEX WFLW_EVENT_IX2 ON WFLW_EVENT(WORKFLOW_ID);
CREATE INDEX WFLW_EVENT_IX3 ON WFLW_EVENT(EVENT_TYPE);
CREATE INDEX WFLW_EVENT_IX4 ON WFLW_EVENT(EVENT_NAME);

DROP TABLE WFLW_ARCHIVED_EVENT;

CREATE TABLE WFLW_ARCHIVED_EVENT (
	ID varchar(36), 	
	CREATION_TIME timestamp, -- time when event was received and stored.	 		
	CONTENT blob,	
	WORKFLOW_ID varchar(36), 	
	EVENT_TYPE varchar(50), -- eventType to identify the processing needed of the event.
	EVENT_NAME varchar(100), -- eventType to identify the processing needed of the event.
	PROCESSED_TIME timestamp
);

