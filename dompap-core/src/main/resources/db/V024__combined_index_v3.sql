INSERT INTO WFLW_VERSION (SCRIPT)
VALUES ('V024__combined_index_v3.sql');

-- Combined index on WORKFLOW_CLASS, METHOD_STARTED and START_WHEN to support workflow select query:
-- where METHOD_STARTED is null and WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterV4'
-- and (START_WHEN is null or START_WHEN<=sysdate)

-- This is the index used by workflow selector to pick a ready workflow.
-- Updated on every checkpoint (after each method) and polled by available threads.
DROP INDEX WFLW_WORKFLOW_IX13;
CREATE INDEX WFLW_WORKFLOW_IX13 ON WFLW_WORKFLOW(WORKFLOW_CLASS, METHOD_STARTED, START_WHEN) INITRANS 10;

