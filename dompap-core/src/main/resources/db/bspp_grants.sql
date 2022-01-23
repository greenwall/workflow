-------------------
--
-- Script for granting permissions to technical users
--
-- Use SQL*plus for execution. For example:
--   sqlplus bspp/mypassword@(description=(address=(protocol=tcp)(HOST=ora-bspps)(port=1521))(Connect_data=(service_name=bspps)))
--

-- DBA handles roles and session connect permissions:
-- CREATE ROLE bspp_app_role;
-- GRANT create session, bspp_app_role TO bspp_app;

begin
  FOR x IN (SELECT * FROM user_tables) LOOP   
    EXECUTE IMMEDIATE 'GRANT ALL ON ' || x.table_name || ' TO bspp_app_role'; 
  END LOOP;
end;
/