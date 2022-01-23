--select * from WFLW_WORKFLOW where METHOD like 'sendToNds' and CREATION_TIME>='2016-02-15 16:50' and CREATION_TIME<='2016-02-17 10:50' order by CREATION_TIME
--select count(*) from WFLW_WORKFLOW where servername is null and CREATION_TIME>='2016-02-15 16:50' and CREATION_TIME<='2016-02-17 10:50' order by CREATION_TIME

select * from WFLW_NDS_EVENT order by EVENT_TIMESTAMP desc;
select * from WFLW_NDS_EVENT where ID='eee238f4-b24c-4990-9a08-24e49e313621';
select * from WFLW_ARCHIVED_NDS_EVENT where SESSION_ID = 'f969621f-040a-4349-a345-c50d65318a0b' and ID='eee238f4-b24c-4990-9a08-24e49e313621';

--insert into WFLW_NDS_EVENT
--select e.ID as ID, e.EVENT_TIMESTAMP as EVENT_TIMESTAMP, e.CONTENT as CONTENT, 
--'G93283' as USER_ID, 'MANUAL' as APP_ID,
--null as TECHNICAL_USER_ID, null as REQUEST_ID, null as REQUEST_DOMAIN,
--e.SESSION_ID as SESSION_ID, 
--'2016-08-25 10:54:03' as EVENT_OCCURED,
--'2016-08-25 10:54:04' as EVENT_REPORTED,
--'http://archivesrest.prod.midas.root4.net:7501/archives/rest/1.0/read/find/content?genDocumentKey=NBAgSDDK18e29lfvtg9jq#amp;entity=CustomerDokDK' as DOCUMENT_URI,
--'order.signed' as EVENT,
--null as MESSAGE
--from WFLW_ARCHIVED_NDS_EVENT e where SESSION_ID = 'f969621f-040a-4349-a345-c50d65318a0b' and ID='eee238f4-b24c-4990-9a08-24e49e313621';

--select * from WFLW_NDS_EVENT where SESSION_ID='26c46e7e-382d-4651-9b41-def6d3c0020f';
select * from WFLW_WORKFLOW where ID='f969621f-040a-4349-a345-c50d65318a0b';


select w.*, m1.VALUE as customer from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0  
where w.WORKFLOW_CLASS like '%DocumentSigningProcessV2Impl%' and w.METHOD_EXCEPTION_MESSAGE like '%Regnr. er ikke et CMS-regnr.%'  
and w.CREATION_TIME>='2016-01-01' 
order by w.CREATION_TIME desc;

select ID, EXTERNAL_KEY, WORKFLOW_CLASS, CREATION_TIME, METHOD, START_WHEN 
from WFLW_WORKFLOW where METHOD is not null and METHOD_STARTED is null and WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailViewableLetterProcess' and (START_WHEN is null or START_WHEN<=sysdate) and ROWNUM <=1 order by CREATION_TIME;


--update WFLW_WORKFLOW set METHOD='generateDocument'
select * from WFLW_WORKFLOW w 
--update WFLW_WORKFLOW set METHOD='orderAcceptedArchiveStatus', METHOD_STARTED=METHOD_ENDED, START_WHEN=null
where WORKFLOW_CLASS like '%DKV8' and METHOD='orderAcceptedArchiveStatus'; --and START_WHEN>'2016-10-07 12:00'; -- and METHOD_EXCEPTION like '%ResourceException%';


--select * from WFLW_ARCHIVED_NDS_EVENT where SESSION_ID like '83bc009c%' order by EVENT_TIMESTAMP desc;

--delete from WFLW_ARCHIVED_NDS_EVENT where ID='a9e26250-18e7-462d-92b4-9dcaac6d8650'
--select ID, EVENT_TIMESTAMP, CONTENT, SESSION_ID from WFLW_ARCHIVED_NDS_EVENT where ID='a9e26250-18e7-462d-92b4-9dcaac6d8650'
--insert into WFLW_NDS_EVENT (ID, EVENT_TIMESTAMP, CONTENT, SESSION_ID) select ID, EVENT_TIMESTAMP, CONTENT, SESSION_ID from WFLW_ARCHIVED_NDS_EVENT where ID='a9e26250-18e7-462d-92b4-9dcaac6d8650'

--select BRANCH_ID,count(*) from WFLW_WORKFLOW where METHOD='OrderSigned_cmsActivity'
--group by BRANCH_ID order by count(*) desc

--update WFLW_WORKFLOW set METHOD='archiveDocuments', METHOD_STARTED=null, METHOD_ENDED=null where ID = '012c7780-2cdd-413f-91e7-a8f2df37e97a'
--select * from WFLW_WORKFLOW w where WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.fi.DocumentProcessFI' order by CREATION_TIME desc
--select * from WFLW_WORKFLOW w order by CREATION_TIME desc
--select * from WFLW_NDS_EVENT where SESSION_ID like 'a4ef%' order by EVENT_TIMESTAMP desc

--select * from WFLW_WORKFLOW w left outer join WFLW_METADATA meta on w.ID=meta.WORKFLOW_ID order by CREATION_TIME desc

--select * from WFLW_WORKFLOW where METHOD_EXCEPTION_MESSAGE not like '%order.about_to_expire%' and METHOD_STARTED>'2016-01-01' order by CREATION_TIME

--upd WFLW_WORKFLOW set METHOD='OrderSigned_cmsActivity', METHOD_STARTED=null, METHOD_ENDED=null, START_WHEN=null

select * from WFLW_WORKFLOW_CONTENT where ID='c9286e08-dc4b-45f3-b8a8-9f84a8b4982b';

select w.ID, h.* from WFLW_WORKFLOW w 
left outer join WFLW_WORKFLOW_HISTORY h on w.ID=h.ID
left outer join WFLW_WORKFLOW_CONTENT c on w.ID=c.ID
where w.CREATION_TIME>'2016-09-27' 
and h.METHOD_STARTED>'2016-09-30 06:20'
and h.METHOD_STARTED<'2016-09-30 06:21'
and w.WORKFLOW_CLASS like '%MassMailViewableLetterProcess'
--and h.METHOD='archiveDocument'
order by h.METHOD_STARTED;

--where w.CREATION_TIME>'2016-09-27' 
--and h.METHOD_STARTED>'2016-09-28 03:57'
--and h.METHOD_STARTED<'2016-09-28 03:59';

--and h.METHOD='archiveDocument'
--and w.WORKFLOW_CLASS like '%MassMailViewableLetterProcess';
--and METHOD ='OrderSigned_cmsActivity' 
--and START_WHEN>sysdate;
--and METHOD_ENDED is null;
--and METHOD_EXCEPTION is not null 
--and METHOD_EXCEPTION_MESSAGE like '%Regnr. er ikke et CMS-regnr%';
--and METHOD_ENDED>'2016-09-28 13:40';
--order by CREATION_TIME desc;

--set METHOD='archiveDocuments', METHOD_STARTED=null, METHOD_ENDED=null 


select * from WFLW_WORKFLOW where CREATION_TIME>'2016-01-01' and METHOD='sendToNds' order by CREATION_TIME desc;

select * from WFLW_WORKFLOW where CREATION_TIME>'2016-01-01' and METHOD='processNdsEvent' and METHOD_EXCEPTION='java.lang.IllegalArgumentException' and METHOD_EXCEPTION_MESSAGE like '%error.timeout%' order by CREATION_TIME desc;

select * from WFLW_WORKFLOW where CREATION_TIME>'2016-01-01' and METHOD='processNdsEvent' and METHOD_EXCEPTION='java.lang.IllegalArgumentException' and METHOD_EXCEPTION_MESSAGE like '%about_to_expire%' order by CREATION_TIME desc;

select * from WFLW_WORKFLOW where WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3' and CREATION_TIME>'2016-01-01' and METHOD='processNdsEvent' order by CREATION_TIME desc;

select USER_ID, BRANCH_ID, max(CREATION_TIME), min(CREATION_TIME) from WFLW_WORKFLOW where USER_ID like 'G5%' group by USER_ID, BRANCH_ID order by USER_ID, BRANCH_ID;

------------------------ PENDING EVENTS ------------------------------------------------

select w.ID, e.EVENT_OCCURED, w.CREATION_TIME, e.EVENT, w.EXTERNAL_KEY, e.SESSION_ID, w.METHOD, w.METHOD_STARTED, e.MESSAGE from WFLW_WORKFLOW w 
join WFLW_NDS_EVENT e on e.SESSION_ID=w.ID
--and MESSAGE not like '%Dompap%'
--and EVENT='order.signed'
--and EXTERNAL_KEY='005338V3a1cf37edda13267b0a1a27'
and WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailViewableLetterProcess'
--and METHOD='sendToNds'
and  CREATION_TIME>'2016-07-08'
order by CREATION_TIME desc;

-- if method==sendToNds then :
ndsEventWaiting

select * from WFLW_NDS_EVENT e 
where EVENT_OCCURED>'2016-07-25'
order by EVENT_OCCURED desc;

select to_char(w.METHOD_ENDED, 'yyyy-MM-dd') as dt, w.WORKFLOW_CLASS, w.METHOD, count(*)
from WFLW_WORKFLOW w
where w.WORKFLOW_CLASS in (
'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4', 
'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3', 
'com.nordea.next.documentbox.integration.DocumentSigningProcessV2Impl')
and w.METHOD like '%Failed%'
group by w.WORKFLOW_CLASS, w.METHOD, to_char(w.METHOD_ENDED, 'yyyy-MM-dd')
order by dt desc;

select * 
from WFLW_WORKFLOW w 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4' 
and w.METHOD='sendToNds'
and w.CREATION_TIME>=sysdate-45 
order by w.CREATION_TIME desc;


----------------------- GROUP BY branch_id, weekno -----------------------------------
--select BRANCH_ID, to_char(CREATION_TIME, 'iw') as weekno, count(*) from WFLW_WORKFLOW w 
--where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessV2Impl' 
--and CREATION_TIME>='2016-01-01' and CREATION_TIME<'2017-01-01'
--group by w.BRANCH_ID, to_char(CREATION_TIME, 'iw')
--order by weekno desc

--select BRANCH_ID, m2.VALUE as category, to_char(CREATION_TIME, 'iw') as weekno, count(*) from WFLW_WORKFLOW w 
--left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
--where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessV2Impl' 
--and CREATION_TIME>='2016-01-01' and CREATION_TIME<'2017-01-01'
--group by w.BRANCH_ID, m2.VALUE, to_char(CREATION_TIME, 'iw')
--order by weekno desc

-------------- Report for business on Upload Client documents ----------------------

------------ LATEST DK

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where (
--    w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4'
    w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV5'
    or w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV6'
    or w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV7'
    or w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV8'
  )
) v on w.ID=v.ID 
  where (
--    w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4'
    w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV5'
    or w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV6'
    or w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV7'
    or w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV8'
  )
and w.CREATION_TIME>=sysdate-135 
order by w.CREATION_TIME desc;

------------ Overdraft

select w.ID, w.WORKFLOW_CLASS, w.EXTERNAL_KEY, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
--  where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4'
  where w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
) v on w.ID=v.ID 
--where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4' 
  where w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
and w.CREATION_TIME>=sysdate-135 
order by w.CREATION_TIME desc;

------------ Massmail

select w.ID, w.WORKFLOW_CLASS, w.EXTERNAL_KEY, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailViewableLetterProcess'
) v on w.ID=v.ID 
  where w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailViewableLetterProcess'
and w.CREATION_TIME>=sysdate-135 
order by w.CREATION_TIME desc;

------------ LATEST NO

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.DocumentProcessNO%'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.DocumentProcessNO%'
  )
and w.CREATION_TIME>=sysdate-135 
order by w.CREATION_TIME desc;

------------ LATEST SE

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.DocumentProcessSE%'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.DocumentProcessSE%'
  )
and w.CREATION_TIME>=sysdate-135 
order by w.CREATION_TIME desc;

------------ LATEST FI

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.DocumentProcessFI%'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.DocumentProcessFI%'
  )
and w.CREATION_TIME>=sysdate-135 
order by w.CREATION_TIME desc;


------------ old
select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='updateStatusInArchiveOrderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='storeSdoInArchive') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessImpl'
) v on w.ID=v.ID 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessImpl' 
--and w.CREATION_TIME>=sysdate-37 
order by w.CREATION_TIME desc;


------------ Time METHOD generateDocument

select w.ID, w.WORKFLOW_CLASS, w.EXTERNAL_KEY, w.BRANCH_ID, w.USER_ID, w.CREATION_TIME,
  (select min(h.METHOD_STARTED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='generateDocument') as GENERATE_START,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='generateDocument') as GENERATE_END,
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_STARTED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='generateDocument') as GENERATE_START,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='generateDocument') as GENERATE_END
  from WFLW_WORKFLOW w 
--  where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4'
  where w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
) v on w.ID=v.ID 
--where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4' 
  where w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
and w.CREATION_TIME>=sysdate-2 
order by w.CREATION_TIME desc;


------ VIEW
select w.*,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as accepted,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as failed,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as rejected,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as signed,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as expired,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as aboutToExpire,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as canceled
from WFLW_WORKFLOW w 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3';


-- Workflows pending event - should be empty when selecting workflows created more than 30 days ago
select w.ID, USER_ID, CREATION_TIME, EXTERNAL_KEY, ACCEPTED, FAILED, SIGNED, REJECTED, EXPIRED, METHOD, m2.VALUE as CATEGORY from 
(
select w.*,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as accepted,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as failed,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as rejected,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as signed,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as expired,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as aboutToExpire,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as canceled
from WFLW_WORKFLOW w 
where w.WORKFLOW_CLASS in (
  'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3',
  'com.nordea.next.documentbox.workflow.no.DocumentProcessNOV1',
  'com.nordea.next.documentbox.workflow.se.DocumentProcessSEV1')
) w 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
where ACCEPTED < '2016-04-01' and FAILED is null and REJECTED is null and SIGNED is null and EXPIRED is null
and m2.VALUE is not null
--and CREATION_TIME>'2016-01-22' and CREATION_TIME<='2016-01-26'
order by CREATION_TIME desc;


-------------------------- COUNT order.about_to_expire --------------------------------------
--select count(*) from WFLW_WORKFLOW where METHOD='processNdsEvent' and METHOD_EXCEPTION_MESSAGE like '%order.about_to_expire%' and CREATION_TIME>'2016-01-28' order by CREATION_TIME

--select * from WFLW_WORKFLOW_HISTORY where METHOD='processNdsEvent' and METHOD_EXCEPTION_MESSAGE like '%timeout%' and METHOD_STARTED>'2016-03-17' order by METHOD_STARTED
--select * from WFLW_WORKFLOW where METHOD='processNdsEvent' and METHOD_EXCEPTION like '%ResourceException' and CREATION_TIME>'2016-02-25' order by CREATION_TIME
--select * from WFLW_WORKFLOW where METHOD='processNdsEvent' and METHOD_EXCEPTION_MESSAGE like '%timeout%' and CREATION_TIME>'2016-02-25' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='sendToNds', METHOD_STARTED=null, METHOD_ENDED=null where METHOD='processNdsEvent' and METHOD_EXCEPTION_MESSAGE like '%timeout%' and CREATION_TIME>'2016-02-25' 

--select * from WFLW_WORKFLOW where METHOD='ndsEventWaiting' and METHOD_STARTED is not null and CREATION_TIME>'2016-01-27' order by CREATION_TIME

--select * from WFLW_WORKFLOW where METHOD='processNdsEvent' and METHOD_EXCEPTION is not null and CREATION_TIME>'2016-04-20' order by CREATION_TIME;
--update WFLW_WORKFLOW set METHOD='sendToNds', METHOD_STARTED=null, METHOD_ENDED=null where METHOD='processNdsEvent' and METHOD_EXCEPTION is not null and CREATION_TIME>'2016-01-27' 

--select * from WFLW_WORKFLOW where METHOD like 'sendToNds%' and METHOD_EXCEPTION is not null and CREATION_TIME>'2016-01-28' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='sendToNds', METHOD_STARTED=null, METHOD_ENDED=null where METHOD='sendToNds' and METHOD_EXCEPTION is not null and CREATION_TIME>'2016-01-29' 

--select * from WFLW_WORKFLOW where ID='0db492b5-3ba2-4a31-a68e-9d8d17f714a8'
--update WFLW_WORKFLOW set METHOD='OrderAccepted_archiveStatus', METHOD_STARTED=null, METHOD_ENDED=null  where ID='0db492b5-3ba2-4a31-a68e-9d8d17f714a8'

--select * from WFLW_WORKFLOW where METHOD like 'OrderFailed%' and METHOD_ENDED>='2016-01-25' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='sendToNds', METHOD_STARTED=null, METHOD_ENDED=null where METHOD like 'OrderFailed%' and METHOD_ENDED>='2016-01-25' 

--select * from WFLW_WORKFLOW where METHOD like 'OrderSigned_cmsActivity' and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-02-01' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='OrderSigned_cmsActivity', METHOD_STARTED=null, METHOD_ENDED=null where METHOD like 'OrderSigned_cmsActivity' and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-02-01' 

--select * from WFLW_WORKFLOW where METHOD like 'OrderRejected%' and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-01-01' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='OrderRejected_cmsActivity', METHOD_STARTED=null, METHOD_ENDED=null where METHOD like 'OrderRejected%' and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-01-01' 

--select * from WFLW_WORKFLOW where METHOD like 'OrderAccepted_archiveStatus' and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-02-08' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='OrderAccepted_archiveStatus', METHOD_STARTED=null, METHOD_ENDED=null where METHOD like 'OrderAccepted%' and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-02-08' 

--select * from WFLW_WORKFLOW where METHOD like 'archiveDocuments%' and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-02-14' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='archiveDocuments', METHOD_STARTED=null, METHOD_ENDED=null where METHOD like 'archiveDocuments%'  and METHOD_EXCEPTION is not null and CREATION_TIME>='2016-02-14' 

-- Workflows failing in orderSigned
select * from WFLW_WORKFLOW where WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3' and METHOD='orderSigned' and START_WHEN is not null order by CREATION_TIME desc;
--update WFLW_WORKFLOW set METHOD='orderSigned', METHOD_STARTED=null, METHOD_ENDED=null, START_WHEN=null where WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3' and METHOD='orderSigned' and START_WHEN is not null; 


-------------- PENDING workflows ---------------

--select * from WFLW_WORKFLOW where METHOD like 'archiveDocuments%' and METHOD_STARTED>='2016-02-01' and METHOD_ENDED is null and METHOD_STARTED<'2016-02-03' order by CREATION_TIME
--update WFLW_WORKFLOW set METHOD='archiveDocuments', METHOD_STARTED=null, METHOD_ENDED=null where METHOD like 'archiveDocuments%' and METHOD_STARTED>='2016-02-01' and METHOD_ENDED is null and METHOD_STARTED<'2016-02-03'  

--select * from WFLW_WORKFLOW where METHOD like 'Order%' and METHOD_STARTED is not null and METHOD_ENDED is null and CREATION_TIME<='2016-02-09' order by CREATION_TIME
--select * from WFLW_WORKFLOW where METHOD is not null and METHOD_STARTED is not null and METHOD_ENDED is null and CREATION_TIME<='2016-02-10' order by CREATION_TIME

--select * from WFLW_WORKFLOW where METHOD_EXCEPTION is not null and CREATION_TIME>='2016-01-26' order by CREATION_TIME

--select * from WFLW_WORKFLOW where METHOD like 'sendToNds%' and CREATION_TIME>'2016-01-28' order by CREATION_TIME

--select w.ID, SESSION_ID, CREATION_TIME, w.USER_ID, w.METHOD, w.METHOD_EXCEPTION, EVENT, n.* from WFLW_NDS_EVENT n left outer join WFLW_WORKFLOW w on w.ID=n.SESSION_ID where CREATION_TIME<'2016-02-09' and METHOD='sendToNds' order by CREATION_TIME

--update WFLW_WORKFLOW set METHOD='ndsEventWaiting', METHOD_STARTED=null, METHOD_ENDED=null where ID='b91de779-f98e-481d-b71d-d57f37b889af'



--select * from WFLW_WORKFLOW where ID='d3cca65d-5cf4-4ce2-a175-b0793ed0b2da'

--select * from WFLW_ARCHIVED_NDS_EVENT where SESSION_ID='862b6cde-a383-47ce-a16c-21f46e1f62c9'

--select * from WFLW_WORKFLOW_HISTORY where ID = '012c7780-2cdd-413f-91e7-a8f2df37e97a'

--select w.ID, CREATION_TIME, w.USER_ID, w.METHOD_ENDED-w.CREATION_TIME as diff from WFLW_WORKFLOW w left outer join WFLW_WORKFLOW_HISTORY h on w.ID=h.ID where w.METHOD like 'OrderRejected%' and extract(DAY FROM w.METHOD_ENDED-w.CREATION_TIME)>'29' order by diff desc, w.creation_time desc

--select w.ID, CREATION_TIME, w.USER_ID, systimestamp-w.CREATION_TIME as diff, w.EXTERNAL_KEY, w.METHOD from WFLW_WORKFLOW w where w.METHOD like 'OrderRejected%' and extract(DAY FROM systimestamp-w.METHOD_ENDED)>'29' order by w.creation_time desc
--select count(*) from WFLW_WORKFLOW w where w.METHOD like 'OrderAccepted%' and extract(DAY FROM systimestamp-w.METHOD_ENDED)>'29' order by w.creation_time 
--and extract(DAY FROM systimestamp-h_accept.METHOD_ENDED)>'35' order by w.creation_time desc 

--select distinct w.ID, CREATION_TIME, w.USER_ID, w.METHOD, h_accept.METHOD_ENDED as accepted, h_sign.METHOD_ENDED as signed, h_reject.METHOD_ENDED as rejected, h_fail.METHOD_ENDED as failed
--from WFLW_WORKFLOW w 
--left outer join WFLW_WORKFLOW_HISTORY h_accept on w.ID=h_accept.ID 
--left outer join WFLW_WORKFLOW_HISTORY h_sign on w.ID=h_sign.ID 
--left outer join WFLW_WORKFLOW_HISTORY h_reject on w.ID=h_reject.ID 
--left outer join WFLW_WORKFLOW_HISTORY h_fail on w.ID=h_fail.ID 
--where h_accept.METHOD='OrderAccepted_archiveStatus' 
--and h_sign.METHOD='OrderSigned_archiveStatus' 
--and h_fail.METHOD='OrderFailed_archiveStatus' 
--and h_reject.METHOD='OrderRejected_archiveStatus' 
--and w.METHOD like 'OrderAccepted%' 

--WITH accepted AS (select h.METHOD_ENDED from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderAccepted_archiveStatus')
--(select h.METHOD_ENDED from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='sendToNds' and h.METHOD_EXCEPTION is null) as sent,

--select count(*)
--m.VALUE, dsp2.* 
--from WFLW_WORKFLOW_DSPV2 dsp2 left outer join WFLW_METADATA m on dsp2.ID=m.WORKFLOW_ID where m.PROPERTY_ID=1
--where accepted is not null and signed is null and rejected is not null and extract(DAY FROM rejected-accepted)>='21'
--and METHOD_EXCEPTION is null 
--where m.PROPERTY_ID=1 order by accepted desc



-------------- REORDERED signed and accepted ------------------------------
--select ID, m.VALUE, USER_ID, CREATION_TIME, ACCEPTED, SIGNED, REJECTED, FAILED
--from WFLW_WORKFLOW_DSPV2 w
--left outer join WFLW_METADATA m on w.ID=m.WORKFLOW_ID and m.PROPERTY_ID=0
--where accepted is not null and signed is not null and accepted>signed
--and CREATION_TIME>='2016-01-01' 
--order by CREATION_TIME desc

-------------- NON_PROCESSED callbacks ------------------------------
--select w.ID, w.EXTERNAL_KEY, w.USER_ID, w.CREATION_TIME, w.ACCEPTED, w.SIGNED, w.REJECTED, w.FAILED, w.METHOD
--select count(*)
--select SESSION_ID, w.USER_ID, w.CREATION_TIME, EVENT, METHOD, CONTENT
--from WFLW_NDS_EVENT e 
--join WFLW_WORKFLOW_DSPV2 w on e.SESSION_ID=w.ID
--where SESSION_ID is not null and METHOD not like 'OrderSigned%'
--order by EVENT desc, METHOD desc


-------------- sendToNds without events ------------------------------
--select *
--from WFLW_WORKFLOW_DSPV2 w 
--left outer join WFLW_NDS_EVENT n on w.ID=n.SESSION_ID
--where METHOD='sendToNds' and w.CREATION_TIME>='2015-01-01' 

-------------- Missing expired ------------------------------
select w.ID, USER_ID, CREATION_TIME, EXTERNAL_KEY, ACCEPTED, FAILED, SIGNED, REJECTED, EXPIRED, METHOD, m2.VALUE as CATEGORY from WFLW_WORKFLOW_DSPV2 w 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
where ACCEPTED < '2016-04-01' and FAILED is null and REJECTED is null and SIGNED is null and EXPIRED is null
and m2.VALUE is not null
--and CREATION_TIME>'2016-01-22' and CREATION_TIME<='2016-01-26'
order by CREATION_TIME desc
;

--select count(*) from WFLW_WORKFLOW_DSPV2 w 
--where ACCEPTED < '2016-01-20' and FAILED is null and REJECTED is null and SIGNED is null


--where ACCEPTED < '2016-01-21' and FAILED is null and REJECTED is null and SIGNED is null
--and CREATION_TIME>'2015-01-19' and CREATION_TIME<='2016-01-20'

--select * from WFLW_ARCHIVED_NDS_EVENT where EVENT_TIMESTAMP>'2016-02-24' and SESSION_ID like 'a86e7200%' order by EVENT_TIMESTAMP desc


-----------------------------------------------------------------------

--order by w.CREATION_TIME desc

--select * from WFLW_WORKFLOW where METHOD='sendToNds' and CREATION_TIME>='2015-01-01' 

--select count(*) from WFLW_WORKFLOW w where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessV2Impl'
--select count(*) from WFLW_METADATA where PROPERTY_ID=1

--select w.id, w.external_key, creation_time, h.method_started, w.method, w.method_exception from WFLW_WORKFLOW_HISTORY h left outer join WFLW_WORKFLOW w on w.ID=h.ID where h.METHOD='sendToNds' and h.METHOD_STARTED>'2016-01-03 19:53:30' order by h.METHOD_STARTED, w.id

--select count(*), to_char(CREATION_TIME,'yyyy-mm'), METHOD from WFLW_WORKFLOW w left outer join WFLW_METADATA m on w.ID=m.WORKFLOW_ID and m.PROPERTY_ID=1 
--where WORKFLOW_CLASS like '%DocumentSigningProcessV2Impl%' and METHOD like 'Order%' group by to_char(CREATION_TIME,'yyyy-mm'), METHOD order by to_char(CREATION_TIME,'yyyy-mm'), METHOD

--select length(CONTENT) from WFLW_WORKFLOW_CONTENT order by length(CONTENT) desc

--select count(*) from WFLW_WORKFLOW w where CREATION_TIME>'2016-01-18' 
--select * from WFLW_WORKFLOW w left outer join WFLW_METADATA m on w.ID=m.WORKFLOW_ID where CREATION_TIME>'2016-01-18' order by w.creation_time desc
--select * from WFLW_WORKFLOW w, WFLW_METADATA m where w.ID=m.WORKFLOW_ID and m.PROPERTY_ID=0 and m.VALUE='4307415521'
--select count(*) from WFLW_METADATA

--select w.ID, w.EXTERNAL_KEY, w.CREATION_TIME, w.METHOD, w.WORKFLOW_CLASS, c.CONTENT from WFLW_WORKFLOW w left outer join WFLW_WORKFLOW_CONTENT c on w.ID=c.ID where CREATION_TIME<'2016-02-09' order by CREATION_TIME desc

--select * from WFLW_PROPERTYTYPE
--select * from WFLW_METADATA where PROPERTY_ID=2 and VALUE like 'Grundkonto%'

--------------------------------------------------
-- old
---
select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
select w.*,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderAccepted_archiveStatus') as ACCEPTED,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderFailed_archiveStatus') as FAILED,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderRejected_archiveStatus') as REJECTED,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderSigned_archiveStatus') as SIGNED,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderExpired_archiveStatus') as EXPIRED,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderAboutToExpire_cmsActivity') as ABOUTTOEXPIRE,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
from WFLW_WORKFLOW w 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessV2Impl'
) v on w.ID=v.ID 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessV2Impl' 
and w.CREATION_TIME>=sysdate-90 
order by w.CREATION_TIME desc;


------------ v3

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3'
) v on w.ID=v.ID 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV3' 
and w.CREATION_TIME>=sysdate-90 
order by w.CREATION_TIME desc;

------------ v4

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  m2.VALUE as category, 
  m1.VALUE as customer
from WFLW_WORKFLOW w 
left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 
left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 
left outer join 
(
  select w.*,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAccepted') as ACCEPTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderFailed') as FAILED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderRejected') as REJECTED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderSigned') as SIGNED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderExpired') as EXPIRED,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderAboutToExpire') as ABOUTTOEXPIRE,
  (select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='orderCanceled') as CANCELED
  from WFLW_WORKFLOW w 
  where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4'
) v on w.ID=v.ID 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4' 
and w.CREATION_TIME>=sysdate-90 
order by w.CREATION_TIME desc;
