INSERT INTO WFLW_VERSION (SCRIPT) 
VALUES ('V021__combined_index.sql');

-- Combined index on WORKFLOW_CLASS, METHOD_STARTED and START_WHEN to support workflow select query:
-- where METHOD is not null and METHOD_STARTED is null and WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterV4'
-- and (START_WHEN is null or START_WHEN<=sysdate) 

DROP INDEX WFLW_WORKFLOW_IX13;
--CREATE INDEX WFLW_WORKFLOW_IX13 ON WFLW_WORKFLOW(WORKFLOW_CLASS, nvl(METHOD_STARTED, CREATION_TIME));
CREATE INDEX WFLW_WORKFLOW_IX13 ON WFLW_WORKFLOW(WORKFLOW_CLASS, nvl(METHOD_STARTED, CREATION_TIME), nvl(START_WHEN, CREATION_TIME));


