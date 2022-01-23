-- Count workflows
select sysdate, 'WORFKLOW', count(*) from WFLW_WORKFLOW
union
select sysdate, 'ARCHIVED_WORKFLOW', count(*) from WFLW_ARCHIVED_WORKFLOW;


-- Count sizes of relevant tables
select 'WFLW_WORKFLOW_CONTENT', count(*) as "rows", sum(length(c.CONTENT)) as "size" from WFLW_WORKFLOW_CONTENT c
union
select 'WFLW_ARCHIVED_WORKFLOW_CONTENT', count(*) as "rows", sum(length(c.CONTENT)) as "size" from WFLW_ARCHIVED_WORKFLOW_CONTENT c
union
select 'WFLW_NDS_EVENT', count(*) as "rows", sum(length(c.CONTENT)) as "size" from WFLW_NDS_EVENT c
union
select 'WFLW_ARCHIVED_NDS_EVENT', count(*) as "rows", sum(length(c.CONTENT)) as "size" from WFLW_ARCHIVED_NDS_EVENT c
union
select 'WFLW_WORKFLOW_CONTROLLER', count(*) as "rows", sum(length(c.CONTROLLER)) as "size" from WFLW_WORKFLOW_CONTROLLER c
union
select 'WFLW_ARCHIVED_WORKFLOW', count(*) as "rows", sum(length(c.METHOD)) as "size" from WFLW_ARCHIVED_WORKFLOW c
union
select 'WFLW_ARCHIVED_WORKFLOW_HISTORY', count(*) as "rows", sum(length(c.STACKTRACE)) as "size" from WFLW_ARCHIVED_WORKFLOW_HISTORY c
union
--select 'WFLW_EVENT', count(*) as "rows", sum(length(c.CONTENT)) as "size" from WFLW_EVENT c
--union
select 'WFLW_ARCHIVED_EVENT', count(*) as "rows", sum(length(c.CONTENT)) as "size" from WFLW_ARCHIVED_EVENT c;
--order by sum(length(c.CONTROLLER)) desc;


select w.WORKFLOW_CLASS, count(*), sum(length(c.CONTENT)) from WFLW_WORKFLOW_CONTENT c left outer join WFLW_WORKFLOW w on w.ID=c.ID
--where w.CREATION_TIME >= '2016-10-01' and w.CREATION_TIME < '2017-04-01'
--where w.METHOD='OrderSigned_cmsActivity'
--where w.WORKFLOW_CLASS = 'com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV2'
group by w.WORKFLOW_CLASS
order by sum(length(c.CONTENT)) desc;

select count(*) from WFLW_ARCHIVED_WORKFLOW w on w.ID=c.ID
--where w.CREATION_TIME >= '2016-10-01' and w.CREATION_TIME < '2017-04-01'


select w.WORKFLOW_CLASS, count(*), sum(length(c.CONTENT)) from WFLW_ARCHIVED_WORKFLOW_CONTENT c left outer join WFLW_ARCHIVED_WORKFLOW w on w.ID=c.ID
--where w.CREATION_TIME >= '2016-10-01' and w.CREATION_TIME < '2017-04-01'
--where w.METHOD='OrderSigned_cmsActivity'
--where w.WORKFLOW_CLASS = 'com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV2'
group by w.WORKFLOW_CLASS
order by sum(length(c.CONTENT)) desc;


-- truncate archive tables
ALTER SESSION SET ddl_lock_timeout=10;

truncate table WFLW_ARCHIVED_WORKFLOW_CONTENT;
truncate table WFLW_ARCHIVED_WORKFLOW;
truncate table WFLW_ARCHIVED_WORKFLOW_HISTORY;
truncate table WFLW_ARCHIVED_NDS_EVENT;
truncate table WFLW_ARCHIVED_EVENT;

--delete from WFLW_ARCHIVED_WORKFLOW where CREATION_TIME<'2017-09-01';
--select count(*) from WFLW_ARCHIVED_WORKFLOW;
--truncate table WFLW_NDS_EVENT;

-- delete from archived_workflows since truncate is disallowed? TODO find constraint
select count(*) from WFLW_ARCHIVED_WORKFLOW where CREATION_TIME<'2017-08-01';
delete from WFLW_ARCHIVED_WORKFLOW where CREATION_TIME<'2017-08-01';

---------------------------------
-- fast forward archiving
select count(*) from WFLW_WORKFLOW 
where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
and METHOD='!orderForViewAccepted'
and START_WHEN<'2017-11-25';

update WFLW_WORKFLOW set START_WHEN='2017-11-24'
where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
and METHOD='!orderForViewAccepted'
and START_WHEN>'2017-11-25'
and CREATION_TIME<'2017-11-23 14:00:00';

select count(*)
from WFLW_WORKFLOW 
where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
and METHOD='!orderForViewAccepted'
and START_WHEN>'2017-11-25'
and CREATION_TIME<'2017-11-23 15:00:00';
---------------------------------

select count(*), min(START_WHEN), max(START_WHEN)
--extract( day from START_WHEN-CREATION_TIME), CREATION_TIME, START_WHEN, (to_timestamp(CREATION_TIME+2)) as archive
from WFLW_WORKFLOW 
where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
and METHOD='!orderForViewAccepted'
and START_WHEN>'2017-12-01';
--and extract( day from START_WHEN-CREATION_TIME)>=30
--and rownum<=1;

update WFLW_WORKFLOW set START_WHEN='2017-11-25 16:00:00'
where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
and METHOD='!orderForViewAccepted'
and START_WHEN>'2017-12-01';


---------------------------------

select count(*) from WFLW_WORKFLOW 
where WORKFLOW_CLASS='com.nordea.scrat.workflow.NordeaPay'
and METHOD='!orderForViewAccepted'
and START_WHEN<'2017-12-10';

update WFLW_WORKFLOW set START_WHEN='2017-11-24'
where WORKFLOW_CLASS='com.nordea.scrat.workflow.NordeaPay' 
and METHOD='!orderForViewAccepted'
and START_WHEN<'2017-12-10';

---------------------------------

select * from WFLW_WORKFLOW 
where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
and METHOD='!orderForViewAccepted'
order by METHOD_ENDED desc;


select count(*), METHOD from WFLW_WORKFLOW
where METHOD_STARTED<'2017-01-01'
group by METHOD
order by count(*) desc;

select ID, count(*) from (
select w.ID as ID from WFLW_WORKFLOW w left outer join WFLW_WORKFLOW_HISTORY h on h.ID=w.ID
where h.METHOD_STARTED>'2017-03-14 15:00' and h.METHOD_STARTED<'2017-03-14 18:00'
and h.METHOD='startIndividualWorkflows'
)
group by ID
order by count(*) desc;

select count(*), sum(length(c.CONTENT)) from WFLW_WORKFLOW_CONTENT c
order by sum(length(c.CONTENT)) desc;

select w.WORKFLOW_CLASS, count(*), sum(length(c.CONTENT)) from WFLW_WORKFLOW_CONTENT c left outer join WFLW_WORKFLOW w on w.ID=c.ID
where w.CREATION_TIME >= '2016-10-01' and w.CREATION_TIME < '2017-04-01'
--where w.METHOD='OrderSigned_cmsActivity'
--where w.WORKFLOW_CLASS = 'com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV2'
group by w.WORKFLOW_CLASS
order by sum(length(c.CONTENT)) desc;

select w.METHOD, count(*), sum(length(c.CONTENT)) from WFLW_WORKFLOW_CONTENT c left outer join WFLW_WORKFLOW w on w.ID=c.ID
--where w.CREATION_TIME >= '2016-10-01' and w.CREATION_TIME < '2016-11-01'
--where w.METHOD='OrderSigned_cmsActivity'
where w.WORKFLOW_CLASS = 'com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV2'
group by w.METHOD
order by sum(length(c.CONTENT)) desc;


select count(*), sum(length(c.CONTENT)) from WFLW_WORKFLOW_CONTENT c left outer join WFLW_WORKFLOW w on w.ID=c.ID
--where w.CREATION_TIME >= '2016-10-01' and w.CREATION_TIME < '2016-11-01'
where w.WORKFLOW_CLASS = 'com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV3'
and w.METHOD like '%OrderSigned%' --_cmsActivity'
and w.METHOD_ENDED<'2017-04-16'
and w.METHOD_EXCEPTION is null
order by sum(length(c.CONTENT)) desc;

--162798	586198157399

--update WFLW_WORKFLOW w  set w.METHOD='!OrderSigned_cmsActivity' , w.METHOD_STARTED=null, w.METHOD_ENDED=null, w.START_WHEN=null
--where w.CREATION_TIME >= '2016-10-01' and w.CREATION_TIME < '2016-11-01'
select count(*) from WFLW_ARCHIVED_WORKFLOW w
where w.WORKFLOW_CLASS = 'com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV2'
and w.METHOD='OrderSigned_cmsActivity'
--and w.METHOD_ENDED<'2017-03-16'
and w.METHOD_EXCEPTION is null;

--upd WFLW_WORKFLOW set METHOD='OrderSigned_cmsActivity', METHOD_STARTED=null, METHOD_ENDED=null, START_WHEN=null

select count(*), sum(length(c.CONTENT)) from WFLW_ARCHIVED_WORKFLOW_CONTENT c
order by sum(length(c.CONTENT)) desc;

delete from WFLW_ARCHIVED_WORKFLOW_CONTENT;

select count(*), sum(length(e.CONTENT)) from WFLW_ARCHIVED_NDS_EVENT e
order by sum(length(e.CONTENT)) desc;

delete from WFLW_ARCHIVED_NDS_EVENT;



MassMailViewableLetterProcess

select w.ID as ID from WFLW_WORKFLOW w left outer join WFLW_WORKFLOW_CONTENT c on h.ID=c.ID
where h.METHOD_STARTED>'2017-03-14 15:00' and h.METHOD_STARTED<'2017-03-14 18:00'
and h.METHOD='startIndividualWorkflows'
