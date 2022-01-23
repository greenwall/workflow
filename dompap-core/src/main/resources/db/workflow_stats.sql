--select to_date(cast(CREATION_TIME as date)), WORKFLOW_CLASS, count(*) from WFLW_WORKFLOW 
--group by to_date(cast(CREATION_TIME as date)), WORKFLOW_CLASS
--order by to_date(cast(CREATION_TIME as date)) desc;

--select WORKFLOW_CLASS, USER_ID, count(*) from WFLW_WORKFLOW 
--where to_date(cast(CREATION_TIME as date))=to_date(cast(sysdate as date))
--group by WORKFLOW_CLASS, USER_ID 


select count(*) as cnt, dt, started, sent, event, accepted, cms, sdo from
(
--select id, started, sent, event, accepted, cms, sdo from 
(select distinct(ID) as id, to_date(cast(CREATION_TIME as date)) as dt from WFLW_WORKFLOW )
--(select distinct(ID) as id from WFLW_WORKFLOW where to_date(cast(CREATION_TIME as date))=to_date(cast(sysdate-4 as date)))
--(select distinct(ID) as id from WFLW_WORKFLOW where CREATION_TIME<'2015-10-27')
left outer join   
(select distinct(ID) as id_event, METHOD as event from WFLW_WORKFLOW_HISTORY where METHOD='processNdsEvent' ) on id_event=id
left outer join   
(select distinct(ID) as id_started, METHOD as started from WFLW_WORKFLOW_HISTORY where METHOD='sendDocumentsToArchive') on id_started=id
left outer join   
(select distinct(ID) as id_sent, METHOD as sent from WFLW_WORKFLOW_HISTORY where METHOD='sendToNds') on id_sent=id
left outer join   
(select distinct(ID) as id_accepted, METHOD as accepted from WFLW_WORKFLOW_HISTORY where METHOD='updateStatusInArchiveOrderAccepted') on id_accepted=id
left outer join   
(select distinct(ID) as id_cms, METHOD as cms from WFLW_WORKFLOW_HISTORY where METHOD='createCMSActivity') on id_cms=id
left outer join   
(select distinct(ID) as id_sdo, METHOD as sdo from WFLW_WORKFLOW_HISTORY where METHOD='storeSdoInArchive') on id_sdo=id
--order by started, sent, event, accepted, sdo
)
group by dt, started, sent, event, accepted, cms, sdo
order by dt, started, sent, event, accepted, cms, sdo
;



select count(*) as cnt, started, sent, event, accepted, cms, sdo from
(
--select id, started, sent, event, accepted, cms, sdo from 
--(select distinct(ID) as id, to_date(cast(CREATION_TIME as date)) as dt from WFLW_WORKFLOW )
--(select distinct(ID) as id from WFLW_WORKFLOW where to_date(cast(CREATION_TIME as date))=to_date(cast(sysdate-4 as date)))
(select distinct(ID) as id from WFLW_WORKFLOW where CREATION_TIME<'2015-10-28')
left outer join   
(select distinct(ID) as id_event, METHOD as event from WFLW_WORKFLOW_HISTORY where METHOD='processNdsEvent' ) on id_event=id
left outer join   
(select distinct(ID) as id_started, METHOD as started from WFLW_WORKFLOW_HISTORY where METHOD='sendDocumentsToArchive') on id_started=id
left outer join   
(select distinct(ID) as id_sent, METHOD as sent from WFLW_WORKFLOW_HISTORY where METHOD='sendToNds') on id_sent=id
left outer join   
(select distinct(ID) as id_accepted, METHOD as accepted from WFLW_WORKFLOW_HISTORY where METHOD='updateStatusInArchiveOrderAccepted') on id_accepted=id
left outer join   
(select distinct(ID) as id_cms, METHOD as cms from WFLW_WORKFLOW_HISTORY where METHOD='createCMSActivity') on id_cms=id
left outer join   
(select distinct(ID) as id_sdo, METHOD as sdo from WFLW_WORKFLOW_HISTORY where METHOD='storeSdoInArchive') on id_sdo=id
--order by started, sent, event, accepted, sdo
)
group by started, sent, event, accepted, cms, sdo
order by started, sent, event, accepted, cms, sdo
