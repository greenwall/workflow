
select doc.ID, doc.EXTERNAL_KEY, doc.WORKFLOW_CLASS, doc.USER_ID, doc.BRANCH_ID, doc.CREATION_TIME, doc.LAST_UPDATE_TIME, doc.METHOD, doc.START_WHEN, doc.METHOD_STARTED, doc.METHOD_ENDED, doc.METHOD_EXCEPTION, doc.METHOD_EXCEPTION_MESSAGE 
,x.PROPERTY_ID, x.VALUE 
from WFLW_WORKFLOW doc, WFLW_METADATA x, 
(
        select * from (select tmp.*, rownum rn from (
                select doc.ID
                from WFLW_WORKFLOW doc, WFLW_METADATA n1
                where 1=1 
                and n1.WORKFLOW_ID = doc.ID
                and n1.PROPERTY_ID = 0 and n1.VALUE like 'A1%'
                                
                order by doc.CREATION_TIME desc, doc.WORKFLOW_CLASS, doc.USER_ID 
        ) tmp where rownum<=10) where rn > 0
) id
where id.ID = doc.ID and doc.ID = x.WORKFLOW_ID
order by doc.CREATION_TIME desc, doc.WORKFLOW_CLASS, doc.USER_ID 