--select count(*) from EDMS_METADATA
--select count(*) from EDMS_DOCUMENT

--select * from EDMS_PROPERTYTYPE

--select * from (
--select * from EDMS_METADATA where PROPERTY_ID=9 and VALUE = 'i000i' 
--union 
--select * from EDMS_METADATA where PROPERTY_ID=7 and VALUE LIKE 'g0%' 
--) order by PROPERTY_ID

--select  from (
--select DOCUMENT_ID, 'i' from EDMS_METADATA where PROPERTY_ID=9 and VALUE = 'i000i' 
--union all
--select DOCUMENT_ID, 'g' from EDMS_METADATA where PROPERTY_ID=7 and VALUE LIKE 'g0%' 
--) 


--select distinct a.DOCUMENT_ID, doc.NAME, a.VALUE as a, b.VALUE as b
--from EDMS_METADATA a, EDMS_METADATA b
--join EDMS_DOCUMENT doc on b.DOCUMENT_ID=doc.ID
--where a.DOCUMENT_ID = b.DOCUMENT_ID 
--and a.PROPERTY_ID=8 and a.VALUE LIKE 'h93%' 
--and b.PROPERTY_ID=9 and b.VALUE LIKE 'i22%' 
--order by DOCUMENT_ID



--3 criterias
--select distinct doc.ID, doc.NAME, a.VALUE as a, b.VALUE as b, c.VALUE as c, d.VALUE as d
--from EDMS_DOCUMENT doc, EDMS_METADATA a, EDMS_METADATA b , EDMS_METADATA c, EDMS_METADATA d
--where 1=1 
--and doc.ID  = a.DOCUMENT_ID
--and a.DOCUMENT_ID = b.DOCUMENT_ID 
--and b.DOCUMENT_ID = c.DOCUMENT_ID
--and c.DOCUMENT_ID = d.DOCUMENT_ID
--and a.PROPERTY_ID=8 and a.VALUE LIKE 'h11%' 
--and b.PROPERTY_ID=8 and b.VALUE LIKE 'h2%' 
--and c.PROPERTY_ID=8 and c.VALUE LIKE 'h4%' 
--and d.PROPERTY_ID=5 and d.VALUE LIKE 'e1%' 
--order by doc.ID


--select * from EDMS_METADATA m, EDMS_DOCUMENT d 
--where m.DOCUMENT_ID = d.ID
--and d.ID in ('aaadaa08-d1e5-4be4-8e9a-751ee5ddff75', 'ae6acebc-ee96-4bd9-9637-8205e31722f1', 'ce12ef1e-c058-42fc-b867-8a357658e60e')
--order by d.ID, property_id

--select * from ( 
--select tmp.*, rownum rn from (
--select distinct doc.ID, doc.NAME , n1.VALUE as n1 from EDMS_DOCUMENT doc , EDMS_METADATA n1 where 1=1  and doc.ID=n1.DOCUMENT_ID and n1.PROPERTY_ID=8  and n1.VALUE='h1111h' 
--order by doc.ID ) tmp where rownum<=200) where rn > 0 

--select distinct doc.ID, doc.NAME , n1.VALUE as n1, n2.VALUE as n2 from EDMS_DOCUMENT doc , EDMS_METADATA n1, EDMS_METADATA n2 where 1=1  and doc.ID=n1.DOCUMENT_ID and n1.DOCUMENT_ID=n2.DOCUMENT_ID and n1.PROPERTY_ID=8  and n1.VALUE='h1111h'  and n2.PROPERTY_ID=9  and n2.VALUE like 'i11%'

--update EDMS_DOCUMENT set ARCHIVE_ID=1 where ARCHIVE_ID is null

select doc.ID, doc.KEY, doc.NAME, doc.ARCHIVE_ID, doc.USER_ID, doc.GENERATION_TIME, doc.ARCHIVAL_TIME, t.NAME PROP_NAME, n.VALUE, a.NAME ARCHIVE_NAME 
from EDMS_DOCUMENT doc, EDMS_ARCHIVE a, EDMS_METADATA n, EDMS_PROPERTYTYPE t 
where doc.ID='aa7ab857-8fc9-448d-a4b9-a5363ee80ab6' and n.PROPERTY_ID=t.ID and doc.ID=n.DOCUMENT_ID and a.ID=doc.ARCHIVE_ID