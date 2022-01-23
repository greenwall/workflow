-- QUERY FOR STATUS ON WORKFLOWS

select * from
  (
    -------------- WITH METHODS
    --select WORKFLOW_CLASS, METHOD,
    -------------- WITHOUT METHODS
    select WORKFLOW_CLASS,

      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and METHOD_ENDED is not null and METHOD_ENDED<=sysdate-1/1440*14400 then 1 else 0 end) as "10+d",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*14400 and METHOD_ENDED<=sysdate-1/1440*1440 then 1 else 0 end) as "<10d",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*1440 and METHOD_ENDED<=sysdate-1/1440*60 then 1 else 0 end) as "<1d",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*60 and METHOD_ENDED<=sysdate-1/1440*10 then 1 else 0 end) as "<1h",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*10 then 1 else 0 end) as "<10m",

      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and METHOD_ENDED>sysdate-1/1440*60 then 1 else 0 end) as "RECENT",

      sum(case when METHOD_EXCEPTION is not null and IS_PICKED=1 then 1 else 0 end) as "FAILED",
      sum(case when METHOD_EXCEPTION is not null and IS_PICKED=0 then 1 else 0 end) as "RETRY",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_ENDED is null and METHOD_STARTED<=sysdate-1/1440*2 then 1 else 0 end) as "STALLED",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_ENDED is null and METHOD_STARTED>sysdate-1/1440*2 then 1 else 0 end) as "RUNNING",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN<=sysdate then 1 else 0 end) as "READY",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN>sysdate then 1 else 0 end) as "LATER",
      max(LAST_UPDATE_TIME) as LAST_UPDATE_TIME
    from WFLW_WORKFLOW
    -------------- WITHOUT ARCHIVE METHODS
    where 1=1
          --and LAST_UPDATE_TIME>sysdate-2
          and CREATION_TIME>sysdate-20
          and CREATION_TIME<sysdate-0
    --------------- EXCLUDE ARCHIVE
    --and METHOD not like '!%'
    --------------- SPECIFIC CLASS
    --and WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV2'

    -------------- WITH METHODS
    --group by WORKFLOW_CLASS, METHOD
    -------------- WITHOUT METHODS
    group by WORKFLOW_CLASS

    order by WORKFLOW_CLASS
  )
where 1=1
--and RECENT>0 or (RETRY>0 or READY>0 or RUNNING>0 or STALLED>0 )
--and RECENT>0
--and ("<1h">0 or "<10m">0 or FAILED>0 or RETRY>0 or READY>0 or LATER>0 or STALLED>0)
--and LAST_UPDATE_TIME>sysdate-2
;



------------------------------------
-- TESTING
------------------------------------
select count(*) from WFLW_WORKFLOW;
select count(*) from WFLW_ARCHIVED_WORKFLOW;

select count(*) from WFLW_WORKFLOW where START_WHEN is null;
select * from WFLW_WORKFLOW where METHOD_EXCEPTION is not null ;

select * from WFLW_WORKFLOW where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail' and  METHOD_EXCEPTION is null and METHOD_ENDED is null and METHOD_STARTED<=sysdate-1/1440*2;

select * from WFLW_WORKFLOW where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail' and IS_PICKED=0 and METHOD_EXCEPTION is null and METHOD_ENDED>sysdate-1/1440*14400 and METHOD_ENDED<=sysdate-1/1440*1440 ;

select * from WFLW_WORKFLOW where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail' and  METHOD_EXCEPTION is not null and IS_PICKED=0;
select * from WFLW_WORKFLOW where WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail' and  METHOD_EXCEPTION is not null and IS_PICKED=1 order by CREATION_TIME desc;


select count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is not null and IS_PICKED=1;
-- FAILED
select count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is not null and IS_PICKED=0;
-- RETRY
select count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_ENDED is null and METHOD_STARTED<=sysdate-1/1440*2;
-- STALLED
select count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_ENDED is null and METHOD_STARTED>sysdate-1/1440*2;
-- RUNNING
select count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN<=sysdate;
-- READY
select count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN>sysdate;
-- LATER

select count(*) from WFLW_WORKFLOW where IS_PICKED=1 and WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail' ;

