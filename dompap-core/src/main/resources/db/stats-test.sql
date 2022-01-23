select * from
  (
    -------------- WITH METHODS
    --select WORKFLOW_CLASS, METHOD,
    -------------- WITHOUT METHODS
    select WORKFLOW_CLASS,

      --  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_ENDED<=sysdate-1/1440*14400 then 1 else 0 end) as "10+d",
      --  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*14400 and METHOD_ENDED<=sysdate-1/1440*1440 then 1 else 0 end) as "<10d",
      --  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*1440 and METHOD_ENDED<=sysdate-1/1440*60 then 1 else 0 end) as "<1d",
      --  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*60 and METHOD_ENDED<=sysdate-1/1440*10 then 1 else 0 end) as "<1h",
      --  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_ENDED>sysdate-1/1440*10 then 1 else 0 end) as "<10m",
      --  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED>sysdate-1/1440*60 then 1 else 0 end) as "RECENT"


      sum(case when METHOD_EXCEPTION is null  and METHOD_ENDED<='2018-01-22' then 1 else 0 end) as "10+d",
      sum(case when METHOD_EXCEPTION is null  and METHOD_ENDED>'2018-01-22' and METHOD_ENDED<='2018-01-31' then 1 else 0 end) as "<10d",
      sum(case when METHOD_EXCEPTION is null  and METHOD_ENDED>'2018-01-31' and METHOD_ENDED<='2018-02-01 09:00' then 1 else 0 end) as "<1d",
      sum(case when METHOD_EXCEPTION is null  and METHOD_ENDED>'2018-02-01 09:00' and METHOD_ENDED<='2018-02-01 09:50' then 1 else 0 end) as "<1h",
      sum(case when METHOD_EXCEPTION is null  and METHOD_ENDED>'2018-02-01 09:50' then 1 else 0 end) as "<10m",
      sum(case when METHOD_EXCEPTION is null  and METHOD_ENDED>'2018-02-01 09:00' then 1 else 0 end) as "RECENT",

      sum(case when METHOD_EXCEPTION is not null and IS_PICKED=1 then 1 else 0 end) as "FAILED",
      sum(case when METHOD_EXCEPTION is not null and IS_PICKED=0 then 1 else 0 end) as "RETRY",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_STARTED<='2018-02-01 09:58' then 1 else 0 end) as "STALLED",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_STARTED>'2018-02-01 09:58' then 1 else 0 end) as "RUNNING",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN<='2018-02-01 10:00' then 1 else 0 end) as "READY",
      sum(case when METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN>'2018-02-01 10:00' then 1 else 0 end) as "LATER",
      max(LAST_UPDATE_TIME) as LAST_UPDATE_TIME

    from WFLW_WORKFLOW
    -------------- WITHOUT ARCHIVE METHODS
    where 1=1
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
      and RECENT>0 or (RETRY>0 or READY>0 or RUNNING>0 or STALLED>0 )
      --and RECENT>0
      --and ("<1h">0 or "<10m">0 or FAILED>0 or RETRY>0 or READY>0 or LATER>0 or STALLED>0)
      and LAST_UPDATE_TIME>'2018-01-01'
;




select WORKFLOW_CLASS, '10+d' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and METHOD_ENDED<='2018-01-22' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, '<10d' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and METHOD_ENDED>'2018-01-22' and METHOD_ENDED<='2018-01-31' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, '<1d' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and METHOD_ENDED>'2018-01-31' and METHOD_ENDED<='2018-01-31 09:00' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, '<1h' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and METHOD_ENDED>'2018-01-31 09:00' and METHOD_ENDED<='2018-01-31 09:50' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, '<10m' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and METHOD_ENDED>'2018-01-31 09:50' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, 'RECENT' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and METHOD_ENDED>'2018-01-31 09:00' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, 'FAILED' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is not null and IS_PICKED=1 group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, 'RETRY' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is not null and IS_PICKED=0 group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, 'STALLED' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_STARTED<='2018-01-31 09:58' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, 'RUNNING' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=1 and METHOD_STARTED>'2018-01-31 09:58' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, 'READY' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN<='2018-01-31 10:00' group by WORKFLOW_CLASS
union
select WORKFLOW_CLASS, 'LATER' "period", count(*) from WFLW_WORKFLOW where METHOD_EXCEPTION is null and IS_PICKED=0 and START_WHEN>'2018-01-31 10:00' group by WORKFLOW_CLASS
;