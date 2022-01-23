
-- DBA TODO: Reuse role already defined in BSPP. Modify script to match correct grants of "create session" 

CREATE ROLE bspp_app_role;
GRANT create session, bspp_app_role TO bspp_app;

GRANT ALL ON DOC_WFLW_DOCUMENT_REF TO bspp_app_role;
GRANT SELECT, INSERT ON DOC_CONTENT TO bspp_app_role;



