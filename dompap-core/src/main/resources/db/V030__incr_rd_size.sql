INSERT INTO WFLW_VERSION (SCRIPT) 
VALUES ('V030__incr_rd_size.sql');
commit;

ALTER TABLE WFLW_EVENT MODIFY REQUEST_DOMAIN VARCHAR2(255 CHAR);
ALTER TABLE WFLW_ARCHIVED_WORKFLOW MODIFY REQUEST_DOMAIN VARCHAR2(255 CHAR);
ALTER TABLE WFLW_WORKFLOW MODIFY REQUEST_DOMAIN VARCHAR2(255 CHAR);
ALTER TABLE WFLW_NDS_EVENT MODIFY REQUEST_DOMAIN VARCHAR2(255 CHAR);
