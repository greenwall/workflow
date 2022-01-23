INSERT INTO WFLW_VERSION (SCRIPT)
VALUES ('V028__search-indexes.sql');

-- workflow searches always order by CREATION_TIME desc
drop index WFLW_WORKFLOW_IX100;
CREATE INDEX WFLW_WORKFLOW_IX100 ON WFLW_WORKFLOW(CREATION_TIME desc) INITRANS 10;