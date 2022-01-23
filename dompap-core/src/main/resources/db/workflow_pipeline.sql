--select WORKFLOW_CLASS, METHOD, 
--count(case when 10000 > (extract (DAY FROM (systimestamp-METHOD_STARTED))*24*60+EXTRACT (HOUR FROM (systimestamp-METHOD_STARTED))*60+EXTRACT (MINUTE FROM (systimestamp-METHOD_STARTED))) then 1 else 0 end) as x10000,
--count(case when 0   > (extract (DAY FROM (systimestamp-METHOD_STARTED))*24*60+EXTRACT (HOUR FROM (systimestamp-METHOD_STARTED))*60+EXTRACT (MINUTE FROM (systimestamp-METHOD_STARTED))) then 1 else 0 end) as x100
--from WFLW_WORKFLOW 
--group by WORKFLOW_CLASS, METHOD 


select WORKFLOW_CLASS, METHOD, 
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED>=5760 then 1 else 0 end) as "4+d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<5760 and MINUTES_SINCE_ENDED>=1440 then 1 else 0 end) as "<4d",
--sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<2880 and MINUTES_SINCE_ENDED>=1440 then 1 else 0 end) as "<2d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<1440 and MINUTES_SINCE_ENDED>=60 then 1 else 0 end) as "<1d",
--sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<720 and MINUTES_SINCE_ENDED>=360 then 1 else 0 end) as "<12h",
--sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<360 and MINUTES_SINCE_ENDED>=180 then 1 else 0 end) as "<6h",
--sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<180 and MINUTES_SINCE_ENDED>=60 then 1 else 0 end) as "<3h",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<60 and MINUTES_SINCE_ENDED>=10 then 1 else 0 end) as "<1h",
--sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_ENDED<30 and MINUTES_SINCE_ENDED>=10 then 1 else 0 end) as "<30m",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<10 then 1 else 0 end) as "<10m",
sum(case when METHOD_EXCEPTION is not null and START_WHEN is null then 1 else 0 end) as "FAILED", 
sum(case when METHOD_EXCEPTION is not null and START_WHEN is not null then 1 else 0 end) as "RETRY",
sum(case when MINUTES_SINCE_STARTED>=2 and METHOD_ENDED is null then 1 else 0 end) as "STALLED", 
sum(case when MINUTES_SINCE_STARTED<2 and METHOD_ENDED is null then 1 else 0 end) as "RUNNING", 
sum(case when METHOD_EXCEPTION is null and METHOD_STARTED is null and (START_WHEN is null or START_WHEN<=systimestamp) then 1 else 0 end) as "READY",
sum(case when METHOD_EXCEPTION is null and METHOD_STARTED is null and START_WHEN is not null then 1 else 0 end) as "LATER" 
from
(
select WORKFLOW_CLASS, METHOD, METHOD_STARTED, METHOD_ENDED, METHOD_EXCEPTION, START_WHEN, 
extract (DAY FROM (systimestamp-METHOD_STARTED))*24*60+EXTRACT (HOUR FROM (systimestamp-METHOD_STARTED))*60+EXTRACT (MINUTE FROM (systimestamp-METHOD_STARTED)) as MINUTES_SINCE_STARTED,
extract (DAY FROM (systimestamp-METHOD_ENDED))*24*60+EXTRACT (HOUR FROM (systimestamp-METHOD_ENDED))*60+EXTRACT (MINUTE FROM (systimestamp-METHOD_ENDED)) as MINUTES_SINCE_ENDED
from WFLW_WORKFLOW 
where WORKFLOW_CLASS like '%DocumentProcess%'
)
--where MINUTES_SINCE_STARTED>10000
--where WORKFLOW_CLASS like '%DocumentProcess%'
--and "FAILED">0 or "RETRY">0 or STALLED>0 or RUNNING>0 or READY>0 or LATER>0
group by WORKFLOW_CLASS, METHOD
order by WORKFLOW_CLASS;


---------------------------------------------------------

select * from
(
-- WITH METHODS
--select WORKFLOW_CLASS, METHOD,
-- WITHOUT METHODS
select WORKFLOW_CLASS, 

  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_STARTED<=systimestamp-1/1440*14400 then 1 else 0 end) as "10+d",
  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_STARTED>systimestamp-1/1440*14400 and METHOD_STARTED<=systimestamp-1/1440*1440 then 1 else 0 end) as "<10d",
  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_STARTED>systimestamp-1/1440*1440 and METHOD_STARTED<=systimestamp-1/1440*60 then 1 else 0 end) as "<1d",
  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_STARTED>systimestamp-1/1440*60 and METHOD_STARTED<=systimestamp-1/1440*10 then 1 else 0 end) as "<1h",
  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is not null and METHOD_STARTED>systimestamp-1/1440*10 then 1 else 0 end) as "<10m",

  --sum(case when METHOD_EXCEPTION is null and METHOD_STARTED>systimestamp-1/1440*60 then 1 else 0 end) as "RECENT",

  sum(case when METHOD_EXCEPTION is not null and START_WHEN is null then 1 else 0 end) as "FAILED", 
  sum(case when METHOD_EXCEPTION is not null and START_WHEN is not null then 1 else 0 end) as "RETRY",
  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is null and METHOD_STARTED is not null and METHOD_STARTED<=systimestamp-1/24/30 then 1 else 0 end) as "STALLED", 
  sum(case when METHOD_EXCEPTION is null and METHOD_ENDED is null and METHOD_STARTED is not null and METHOD_STARTED>systimestamp-1/24/30 then 1 else 0 end) as "RUNNING", 
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED is null and (START_WHEN is null or START_WHEN<=systimestamp) then 1 else 0 end) as "READY",
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED is null and (START_WHEN is not null and START_WHEN>systimestamp) then 1 else 0 end) as "LATER", 
  max(LAST_UPDATE_TIME) as LAST_UPDATE_TIME
from WFLW_WORKFLOW 
--WITHOUT ARCHIVE METHODS
where 1=1
and METHOD not like '!%'
and WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.HouseholdDocumentProcessDKV2'
--group by WORKFLOW_CLASS, METHOD
group by WORKFLOW_CLASS
order by WORKFLOW_CLASS
)
where 1=1
--and (FAILED>0 or RETRY>0 or READY>0 or LATER>0 or RUNNING>0 or STALLED>0 )
and ("<1h">0 or "<10m">0 or FAILED>0 or RETRY>0 or READY>0 or LATER>0 or STALLED>0)
and LAST_UPDATE_TIME>sysdate-30;

----------------STATUS WITHOUT METHOD - (REWRITTEN TO AVOID DATE CALCULATIONS) ----------------------------

select * from
(
-- WITH METHODS
select WORKFLOW_CLASS, METHOD,
-- WITHOUT METHODS
--select WORKFLOW_CLASS, 

  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED<=systimestamp-1/1440*14400 then 1 else 0 end) as "10+d",
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED>systimestamp-1/1440*14400 and METHOD_STARTED<=systimestamp-1/1440*1440 then 1 else 0 end) as "<10d",
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED>systimestamp-1/1440*1440 and METHOD_STARTED<=systimestamp-1/1440*60 then 1 else 0 end) as "<1d",
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED>systimestamp-1/1440*60 and METHOD_STARTED<=systimestamp-1/1440*10 then 1 else 0 end) as "<1h",
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED>systimestamp-1/1440*10 then 1 else 0 end) as "<10m",
  sum(case when METHOD_EXCEPTION is not null and START_WHEN is null then 1 else 0 end) as "FAILED", 
  sum(case when METHOD_EXCEPTION is not null and START_WHEN is not null then 1 else 0 end) as "RETRY",
  sum(case when METHOD_STARTED<=systimestamp-1/24/30 and METHOD_ENDED is null then 1 else 0 end) as "STALLED", 
  sum(case when METHOD_STARTED>systimestamp-1/24/30 and METHOD_ENDED is null then 1 else 0 end) as "RUNNING", 
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED is null and (START_WHEN is null or START_WHEN<=systimestamp) then 1 else 0 end) as "READY",
  sum(case when METHOD_EXCEPTION is null and METHOD_STARTED is null and START_WHEN is not null then 1 else 0 end) as "LATER" 
from WFLW_WORKFLOW 
--WITHOUT ARCHIVE METHODS
where METHOD not like '!%'
and LAST_UPDATE_TIME>systimestamp-30
group by WORKFLOW_CLASS, METHOD
--group by WORKFLOW_CLASS
order by WORKFLOW_CLASS
)
where 1=1
--and (FAILED>0 or RETRY>0 or READY>0 or LATER>0 or RUNNING>0 or STALLED>0 )
and ("<1h">0 or "<10m">0 or FAILED>0 or RETRY>0 or READY>0 or LATER>0 or STALLED>0);

----------------SUMMARY ACTIVITY----------------------------

SELECT WORKFLOW_CLASS, count(*) as RECENT_PROGRESS 
FROM WFLW_WORKFLOW 
WHERE CREATION_TIME>systimestamp-numtodsinterval(60,'minute')
group by WORKFLOW_CLASS
order by WORKFLOW_CLASS;

----------------STATUS LATEST HOUR LAST 7 DAYS ----------------------------

select * from
(
select WORKFLOW_CLASS, 
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<1440*7+60 and MINUTES_SINCE_STARTED>=1440*7 then 1 else 0 end) as "7d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<1440*6+60 and MINUTES_SINCE_STARTED>=1440*6 then 1 else 0 end) as "6d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<1440*5+60 and MINUTES_SINCE_STARTED>=1440*5 then 1 else 0 end) as "5d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<1440*4+60 and MINUTES_SINCE_STARTED>=1440*4 then 1 else 0 end) as "4d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<1440*3+60 and MINUTES_SINCE_STARTED>=1440*3 then 1 else 0 end) as "3d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<1440*2+60 and MINUTES_SINCE_STARTED>=1440*2 then 1 else 0 end) as "2d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<1440*1+60 and MINUTES_SINCE_STARTED>=1440*1 then 1 else 0 end) as "1d",
sum(case when METHOD_EXCEPTION is null and MINUTES_SINCE_STARTED<60 then 1 else 0 end) as "<1h",
sum(case when MINUTES_SINCE_LAST_UPDATE<60 and METHOD_EXCEPTION is not null and START_WHEN is null then 1 else 0 end) as "FAILED<1h", 
sum(case when MINUTES_SINCE_LAST_UPDATE<60 and METHOD_EXCEPTION is not null and START_WHEN is not null then 1 else 0 end) as "RETRY<1h",
max(LAST_UPDATE_TIME) as LAST_UPDATE_TIME
from
(
  select WORKFLOW_CLASS, METHOD_STARTED, METHOD_ENDED, METHOD_EXCEPTION, START_WHEN, LAST_UPDATE_TIME,
  extract (DAY FROM (systimestamp-CREATION_TIME))*24*60+EXTRACT (HOUR FROM (systimestamp-CREATION_TIME))*60+EXTRACT (MINUTE FROM (systimestamp-CREATION_TIME)) as MINUTES_SINCE_STARTED,
  extract (DAY FROM (systimestamp-LAST_UPDATE_TIME))*24*60+EXTRACT (HOUR FROM (systimestamp-LAST_UPDATE_TIME))*60+EXTRACT (MINUTE FROM (systimestamp-LAST_UPDATE_TIME)) as MINUTES_SINCE_LAST_UPDATE
  from WFLW_WORKFLOW 
)
group by WORKFLOW_CLASS
order by WORKFLOW_CLASS
)
where "7d">0 or "6d">0 or "5d">0 or "4d">0 or "3d">0 or "2d">0 or "1d">0 or "<1h">0 or "FAILED<1h">0 or "RETRY<1h">0;



