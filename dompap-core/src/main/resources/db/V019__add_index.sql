INSERT INTO WFLW_VERSION (SCRIPT) 
VALUES ('V019__add_index.sql');

DROP INDEX WFLW_WORKFLOW_IX5;
CREATE INDEX WFLW_WORKFLOW_IX5 ON WFLW_WORKFLOW(SUB_TYPE, 1);

DROP INDEX WFLW_WORKFLOW_IX6;
CREATE INDEX WFLW_WORKFLOW_IX6 ON WFLW_WORKFLOW(REQUEST_DOMAIN, 1);

DROP INDEX WFLW_WORKFLOW_IX7;
CREATE INDEX WFLW_WORKFLOW_IX7 ON WFLW_WORKFLOW(START_WHEN,1);

DROP INDEX WFLW_WORKFLOW_IX8;
CREATE INDEX WFLW_WORKFLOW_IX8 ON WFLW_WORKFLOW(EVENTS_QUEUED);

DROP INDEX WFLW_WORKFLOW_IX9;
CREATE INDEX WFLW_WORKFLOW_IX9 ON WFLW_WORKFLOW(PROCESS_EVENTS, 1);

DROP INDEX WFLW_WORKFLOW_IX10;
CREATE INDEX WFLW_WORKFLOW_IX10 ON WFLW_WORKFLOW(METHOD_STARTED, 1);

DROP INDEX WFLW_WORKFLOW_IX11;
CREATE INDEX WFLW_WORKFLOW_IX11 ON WFLW_WORKFLOW(METHOD_ENDED, 1);

DROP INDEX WFLW_WORKFLOW_IX12;
CREATE INDEX WFLW_WORKFLOW_IX12 ON WFLW_WORKFLOW(METHOD, 1);
