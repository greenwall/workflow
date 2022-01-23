
create or replace view WFLW_WORKFLOW_DSPV2 as
select w.*,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderAccepted_archiveStatus') as accepted,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderFailed_archiveStatus') as failed,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderRejected_archiveStatus') as rejected,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderSigned_archiveStatus') as signed,
(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='OrderExpired_archiveStatus') as expired
from WFLW_WORKFLOW w 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.integration.DocumentSigningProcessV2Impl'
