
-------------- Report for business on Upload Client documents ----------------------

------------ LATEST DK => export-dk.xlsx - for historic reasons dk was left out, i.e. export.xslx

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  '' PERSON,
  '' COMPANY
from WFLW_WORKFLOW w
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV10'
    or w.WORKFLOW_CLASS='com.nordea.documentbox.workflow.DocumentBoxDK'
  )
) v on w.ID=v.ID 
  where (
        w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV10'
    or w.WORKFLOW_CLASS='com.nordea.documentbox.workflow.DocumentBoxDK'
  )
and w.CREATION_TIME>=sysdate-60
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;

------------ Overdraft => export-overdraft.xlsx

select w.ID, w.WORKFLOW_CLASS, w.EXTERNAL_KEY, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  wf_category.VALUE as category,
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  '' PERSON,
  '' COMPANY
from WFLW_WORKFLOW w
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
  where w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
  or w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterV4'
) v on w.ID=v.ID
  where 
(
  w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
  or w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterV4'
)
and w.CREATION_TIME>=sysdate-60
order by w.CREATION_TIME desc;

------------ Massmail => export-massmail.xlsx

select w.ID, w.WORKFLOW_CLASS, w.EXTERNAL_KEY, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  wf_category.VALUE as category,
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  '' PERSON,
  '' COMPANY
from WFLW_WORKFLOW w
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
  where w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
  or w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailDK'
) v on w.ID=v.ID 
  where w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
  or w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailDK'
and w.CREATION_TIME>=sysdate-60
order by w.CREATION_TIME desc;

------------ LATEST NO => export-no.xlsx

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  wf_category.VALUE as category,
  wf_customer.VALUE as customer,
    case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  '' PERSON,
  '' COMPANY
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.HouseholdDocumentProcessNOV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxNO'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.DocumentProcessNO%'
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.HouseholdDocumentProcessNOV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxNO'
  )
and w.CREATION_TIME>=sysdate-60
order by w.CREATION_TIME desc;

------------ LATEST SE => export-se.xlsx

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  wf_category.VALUE as category,
  wf_customer.VALUE as customer,
    case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  '' PERSON,
  '' COMPANY
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.HouseholdDocumentProcessSEV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxSE'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.DocumentProcessSE%'
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.HouseholdDocumentProcessSEV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxSE'
  )
and w.CREATION_TIME>=sysdate-60
order by w.CREATION_TIME desc;

------------ LATEST FI => export-fi.xlsx

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
  wf_category.VALUE as category,
--  w.METHOD,
  wf_customer.VALUE as customer,
    case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  '' PERSON,
  '' COMPANY
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.HouseholdDocumentProcessFIV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxFI'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.DocumentProcessFI%'
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.HouseholdDocumentProcessFIV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxFI'    
  )
and w.CREATION_TIME>=sysdate-60
order by w.CREATION_TIME desc;

------------ LATEST ONLINE => export-online.xlsx
select w.ID, w.BRANCH_ID, w.USER_ID, 
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED,  
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED,  
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED,  
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED,  
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED,  
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED,  
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE,  
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED,  
  wf_category.VALUE as category,
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  '' PERSON,
  '' COMPANY
from WFLW_WORKFLOW w  
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    w.WORKFLOW_CLASS = 'com.nordea.agreement.workflow.SafeDepositBoxV2' 
    or w.WORKFLOW_CLASS = 'com.nordea.scrat.workflow.agreement.OneClickDigital'
  ) 
) v on w.ID=v.ID  
  where ( 
    w.WORKFLOW_CLASS = 'com.nordea.agreement.workflow.SafeDepositBoxV2' 
    or w.WORKFLOW_CLASS = 'com.nordea.scrat.workflow.agreement.OneClickDigital'
  ) 
and w.CREATION_TIME>=sysdate-60
order by w.CREATION_TIME desc;
-----------------------------


select * from WFLW_PROPERTYTYPE order by NAME;

----------------- CSV including ISFORSIGNING, COMPANY and PERSON

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
--  m2.VALUE as category, 
--  w.METHOD,
--  w.EXTERNAL_KEY,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  case when w.WORKFLOW_CLASS like '%Household%' or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV%' then 1 else 0 end as PERSON,
  case when w.WORKFLOW_CLASS like '%Corporate%' then 1 else 0 end as COMPANY
from WFLW_WORKFLOW w
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV10'
    or w.WORKFLOW_CLASS='com.nordea.documentbox.workflow.DocumentBoxDK'
  )
) v on w.ID=v.ID
  where (
    w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV10'
    or w.WORKFLOW_CLASS='com.nordea.documentbox.workflow.DocumentBoxDK'
  )
and w.CREATION_TIME>=sysdate-60
--and w.METHOD <> 'orderAcceptedArchiveStatus'
--and m2.VALUE is null
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;


----------------- OVERDRAFT

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
--  m2.VALUE as category, 
--  w.METHOD,
--  w.EXTERNAL_KEY,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,   
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  case when w.WORKFLOW_CLASS like '%Household%' or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV%' then 1 else 0 end as PERSON,   
  case when w.WORKFLOW_CLASS like '%Corporate%' then 1 else 0 end as COMPANY   
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
  w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
  or w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterV4'
  )
) v on w.ID=v.ID 
  where (
  w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterProcessV2'
  or w.WORKFLOW_CLASS='com.nordea.overdraft.workflow.OverdraftLetterV4'
  )
and w.CREATION_TIME>=sysdate-60
--and w.METHOD <> 'orderAcceptedArchiveStatus'
--and m2.VALUE is null
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;

----------------- MassMailViewableLetterProcess

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
--  m2.VALUE as category, 
--  w.METHOD,
--  w.EXTERNAL_KEY,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,   
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  case when w.WORKFLOW_CLASS like '%Household%' or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV%' then 1 else 0 end as PERSON,   
  case when w.WORKFLOW_CLASS like '%Corporate%' then 1 else 0 end as COMPANY   
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
  w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
  or w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailDK'
  )
) v on w.ID=v.ID 
  where (
  w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMail'
  or w.WORKFLOW_CLASS='com.nordea.massmail.workflow.MassMailDK'
  )
and w.CREATION_TIME>=sysdate-60
--and w.METHOD <> 'orderAcceptedArchiveStatus'
--and m2.VALUE is null
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;

----------------- NO

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
--  m2.VALUE as category, 
--  w.METHOD,
--  w.EXTERNAL_KEY,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,   
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  case when w.WORKFLOW_CLASS like '%Household%' or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV%' then 1 else 0 end as PERSON,   
  case when w.WORKFLOW_CLASS like '%Corporate%' then 1 else 0 end as COMPANY   
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.HouseholdDocumentProcessNOV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxNO'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.DocumentProcessNO%'
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.no.HouseholdDocumentProcessNOV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxNO'
  )
and w.CREATION_TIME>=sysdate-60
--and w.METHOD <> 'orderAcceptedArchiveStatus'
--and m2.VALUE is null
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;

----------------- SE

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
--  m2.VALUE as category, 
--  w.METHOD,
--  w.EXTERNAL_KEY,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,   
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  case when w.WORKFLOW_CLASS like '%Household%' or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV%' then 1 else 0 end as PERSON,   
  case when w.WORKFLOW_CLASS like '%Corporate%' then 1 else 0 end as COMPANY   
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.HouseholdDocumentProcessSEV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxSE'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.DocumentProcessSE%'
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.se.HouseholdDocumentProcessSEV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxSE'
  )
and w.CREATION_TIME>=sysdate-60
--and w.METHOD <> 'orderAcceptedArchiveStatus'
--and m2.VALUE is null
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;

----------------- FI

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
--  m2.VALUE as category, 
--  w.METHOD,
--  w.EXTERNAL_KEY,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,   
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  case when w.WORKFLOW_CLASS like '%Household%' or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV%' then 1 else 0 end as PERSON,   
  case when w.WORKFLOW_CLASS like '%Corporate%' then 1 else 0 end as COMPANY   
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.HouseholdDocumentProcessFIV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxFI'    
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.DocumentProcessFI%'
    or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.fi.HouseholdDocumentProcessFIV%'
    or w.WORKFLOW_CLASS = 'com.nordea.documentbox.workflow.DocumentBoxFI'    
  )
and w.CREATION_TIME>=sysdate-60
--and w.METHOD <> 'orderAcceptedArchiveStatus'
--and m2.VALUE is null
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;

----------------- ONLINE

select w.ID, w.BRANCH_ID, w.USER_ID,
  to_char(w.CREATION_TIME, 'yyyy-MM-dd hh24:mi:ss') as CREATED, 
  to_char(v.ACCEPTED, 'yyyy-MM-dd hh24:mi:ss') as ACCEPTED, 
  to_char(v.FAILED, 'yyyy-MM-dd hh24:mi:ss') as FAILED, 
  to_char(v.REJECTED, 'yyyy-MM-dd hh24:mi:ss') as REJECTED, 
  to_char(v.SIGNED, 'yyyy-MM-dd hh24:mi:ss') as SIGNED, 
  to_char(v.EXPIRED, 'yyyy-MM-dd hh24:mi:ss') as EXPIRED, 
  to_char(v.ABOUTTOEXPIRE, 'yyyy-MM-dd hh24:mi:ss') as ABOUTTOEXPIRE, 
  to_char(v.CANCELED, 'yyyy-MM-dd hh24:mi:ss') as CANCELED, 
--  m2.VALUE as category, 
--  w.METHOD,
--  w.EXTERNAL_KEY,
  case when w.METHOD = 'orderAcceptedArchiveStatus' then null
       else wf_category.VALUE
  end as category,   
  wf_customer.VALUE as customer,
  case when wf_sign.VALUE = 'Yes' or wf_sign.VALUE='true' then 1 else 0 end as isForSigning,
  case when w.WORKFLOW_CLASS like '%Household%' or w.WORKFLOW_CLASS like 'com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV%' then 1 else 0 end as PERSON,   
  case when w.WORKFLOW_CLASS like '%Corporate%' then 1 else 0 end as COMPANY   
from WFLW_WORKFLOW w 
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'customerNumber'
   ) wf_customer on w.ID = wf_customer.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'category'
   ) wf_category on w.ID = wf_category.WORKFLOW_ID
left outer join
  (select wm.WORKFLOW_ID, wm.VALUE
   from WFLW_METADATA wm
   left outer join WFLW_PROPERTYTYPE wp ON wm.PROPERTY_ID = wp.ID
   where wp.NAME = 'isSignable'
   ) wf_sign on w.ID = wf_sign.WORKFLOW_ID
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
    w.WORKFLOW_CLASS = 'com.nordea.agreement.workflow.SafeDepositBoxV2' 
    or w.WORKFLOW_CLASS = 'com.nordea.scrat.workflow.agreement.OneClickDigital'
  )
) v on w.ID=v.ID 
  where (
    w.WORKFLOW_CLASS = 'com.nordea.agreement.workflow.SafeDepositBoxV2' 
    or w.WORKFLOW_CLASS = 'com.nordea.scrat.workflow.agreement.OneClickDigital'
  )
and w.CREATION_TIME>=sysdate-60
--and w.METHOD <> 'orderAcceptedArchiveStatus'
--and m2.VALUE is null
and (v.ACCEPTED is not null or v.FAILED is not null)
order by w.CREATION_TIME desc;


