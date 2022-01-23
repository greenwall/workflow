-- Temp storage of documents
--

-- DBA TODO:
--- Create table space for blob of an typical size of 1MB. Smallest blob are 50KB. Largest blob probably around 20MB.

CREATE TABLE DOC_CONTENT (
	ID VARCHAR(36) NOT NULL,	
	CREATION_TIME TIMESTAMP NOT NULL,
	CONTENT blob,
	primary key (ID, CREATION_TIME)
);

-- DBA TODO: Need to partition table to be able to truncate partitions. Suggest either weekly or monthly partitions.
--           Truncate older data than 1 month or 5 weeks.
-- 
--   partition by range ( create_time )
--   INTERVAL (NUMTOYMINTERVAL(1, 'MONTH'))
--   (  
--     PARTITION pos_data_p2 VALUES LESS THAN (TO_DATE('1-4-2014', 'DD-MM-YYYY')),
--     PARTITION pos_data_p3 VALUES LESS THAN (TO_DATE('1-5-2014', 'DD-MM-YYYY'))
--     ... 
--   )

CREATE INDEX DOC_CONTENT_IDX1 ON DOC_CONTENT(
    ID
);

-- Index used for partition of table -- Maybe a function index is better?
CREATE INDEX DOC_CONTENT_IDX2 ON DOC_CONTENT(
    CREATION_TIME
);


CREATE TABLE DOC_WFLW_DOCUMENT_REF (
    WFLW_ID VARCHAR(36) NOT NULL,
    DOC_CONTENT_ID VARCHAR(36) NOT NULL,
    PRIMARY KEY (WFLW_ID, DOC_CONTENT_ID)
);


