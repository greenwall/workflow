select count(*) from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01';


select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01';

select count(*) from WFLW_WORKFLOW_PARTIAL_CONTENT where PARENT_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');
delete from WFLW_WORKFLOW_PARTIAL_CONTENT where PARENT_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');

select count(*) from WFLW_WORKFLOW_HISTORY where ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');
delete from WFLW_WORKFLOW_HISTORY where ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');

select count(*) from WFLW_WORKFLOW_CONTENT where ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');
delete from WFLW_WORKFLOW_CONTENT where ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');

select count(*) from WFLW_WORKFLOW_CONTROLLER where ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');
delete from WFLW_WORKFLOW_CONTROLLER where ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');

select count(*) from WFLW_EVENT where WORKFLOW_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');
delete from WFLW_EVENT where WORKFLOW_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');

select count(*) from WFLW_METADATA where WORKFLOW_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');
delete from WFLW_METADATA where WORKFLOW_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');

select count(*) from WFLW_NDS_EVENT where SESSION_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');
delete from WFLW_NDS_EVENT where SESSION_ID in (select ID from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01');

select count(*) from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01';
delete from WFLW_WORKFLOW where CREATION_TIME<'2020-01-01';

