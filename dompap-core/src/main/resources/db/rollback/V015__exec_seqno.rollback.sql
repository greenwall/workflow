DELETE FROM WFLW_VERSION where SCRIPT='V015__exec_seqno.sql';

ALTER TABLE WFLW_WORKFLOW DROP COLUMN EXEC_SEQNO;
ALTER TABLE WFLW_ARCHIVED_WORKFLOW DROP COLUMN EXEC_SEQNO;