INSERT INTO WFLW_VERSION (SCRIPT) 
VALUES ('V011__add_index_metadata.sql');

CREATE INDEX WFLW_METADATA_IX2 ON WFLW_METADATA(WORKFLOW_ID);