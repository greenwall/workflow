package com.nordea.dompap.workflow;

/**
 * Export all workflows with eBoks time
 * TODO - only first draft of query is written.
 * @deprecated
 */
@Deprecated
public class WorkflowExport {
	
/*
 
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
  where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4'
) v on w.ID=v.ID 
where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4' 
and w.CREATION_TIME>=sysdate-90 
order by w.CREATION_TIME desc;
 
 */
	private void build() {
        StringBuilder sql = new StringBuilder();

        sql.append("select w.ID, w.WORKFLOW_CLASS, w.EXTERNAL_KEY, w.BRANCH_ID, w.USER_ID, ");

        sql.append(toChar("CREATION_TIME", "CREATED"));
        sql.append(toChar("ACCEPTED", "ACCEPTED"));
        sql.append(toChar("FAILED", "FAILED")); 
        sql.append(toChar("REJECTED", "REJECTED")); 
        sql.append(toChar("SIGNED", "SIGNED")); 
        sql.append(toChar("EXPIRED", "EXPIRED"));
        sql.append(toChar("ABOUTTOEXPIRE", "ABOUTTOEXPIRE")); 
        sql.append(toChar("CANCELED", "CANCELED")); 

        sql.append("m2.VALUE as category,"); 
        sql.append("m1.VALUE as customer ");
        sql.append("from WFLW_WORKFLOW w "); 
        sql.append("left outer join WFLW_METADATA m1 on w.ID=m1.WORKFLOW_ID and m1.PROPERTY_ID=0 "); 
        sql.append("left outer join WFLW_METADATA m2 on w.ID=m2.WORKFLOW_ID and m2.PROPERTY_ID=2 "); 
        sql.append("left outer join ( "); 
        sql.append(subselect());
    	sql.append(") v on w.ID=v.ID "); 
    	sql.append("where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4' "); 
    	sql.append("and w.CREATION_TIME>=sysdate-90 "); 
    	sql.append("order by w.CREATION_TIME desc");
		
	}
	
	private static String toChar(String workflowColumnName, String resultColumnName) {
		return "to_char(w."+workflowColumnName+", 'yyyy-MM-dd hh24:mi:ss') as "+resultColumnName+",";		
	}
	
	private static String subselect() {

		return "select w.*, " +
				selectMax("orderAccepted", "ACCEPTED") + ", " +
				selectMax("orderFailed", "FAILED") + ", " +
				selectMax("orderRejected", "REJECTED") + ", " +
				selectMax("orderSigned", "SIGNED") + ", " +
				selectMax("orderExpired", "EXPIRED") + ", " +
				selectMax("orderAboutToExpire", "ABOUTTOEXPIRE") + ", " +
				selectMax("orderCanceled", "CANCELED") + " " +
				"from WFLW_WORKFLOW w " +
				"where w.WORKFLOW_CLASS='com.nordea.next.documentbox.workflow.dk.DocumentProcessDKV4'";
	}

	private static String selectMax(String methodName, String resultColumnName) {
		return "(select max(h.METHOD_ENDED) from WFLW_WORKFLOW_HISTORY h where h.ID=w.ID and h.METHOD='"+methodName+"') as "+resultColumnName;
	}
}
