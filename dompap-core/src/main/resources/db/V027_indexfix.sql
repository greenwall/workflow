INSERT INTO WFLW_VERSION (SCRIPT)
VALUES ('V027_indexfix.sql');

alter index wflw_workflow_IX14 initrans 10;
alter index wflw_workflow_IX14 rebuild online;

alter index wflw_workflow_IX15 initrans 10;
alter index wflw_workflow_IX15 rebuild online;

alter table wflw_workflow_content initrans 32;
alter table wflw_workflow initrans 8;

/* Ask oracle politely to stop using all the storage in the known universe instead of reusing it when updating the table */
alter table wflw_workflow_content enable row movement;
alter table wflw_workflow_content shrink space cascade;
alter table wflw_workflow_content move lob (content) store as securefile (retention none compress DEDUPLICATE);

alter table wflw_workflow move;

-- Apparent these fails, but the select then alter index works:
alter index (select INDEX_NAME from user_indexes where TABLE_NAME='WFLW_WORKFLOW_CONTENT' and INDEX_TYPE='NORMAL') rebuild online;
alter index (select INDEX_NAME from user_indexes where TABLE_NAME='WFLW_WORKFLOW' and INDEX_TYPE='NORMAL') rebuild online;

select INDEX_NAME from user_indexes where TABLE_NAME='WFLW_WORKFLOW_CONTENT' and INDEX_TYPE='NORMAL';
select INDEX_NAME from user_indexes where TABLE_NAME='WFLW_WORKFLOW' and INDEX_TYPE='NORMAL';

--alter index SYS_C005441 rebuild online;
--alter index SYS_C005440 rebuild online;

