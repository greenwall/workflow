INSERT INTO WFLW_VERSION (SCRIPT) 
VALUES ('V013__watch_dogs_feature.sql');

create or replace procedure check_create_objects(
p_object_type IN varchar2,
p_object_name IN varchar2,
p_object_query IN varchar2

) AS
obj_count int:=0;
invalid_param_value exception;
BEGIN
	
	-- check of parameters are having not null values
	if p_object_type is null or  p_object_name is null or p_object_query is null then
	 raise invalid_param_value;
	end if;
	
	-- check if object exits of not
  select count(*) 
  into obj_count 
  from user_objects 
  where 
  object_type=p_object_type 
  and 
  object_name=p_object_name;
  
    if obj_count=1 THEN
	  	DBMS_OUTPUT.PUT_LINE(p_object_type ||':' ||p_object_name || ' already exist');
	   ELSE
	     execute immediate p_object_query;
	    DBMS_OUTPUT.PUT_LINE(p_object_type ||':' ||p_object_name || ' is created');
	  END IF;
	
	  EXCEPTION 
	  WHEN invalid_param_value then
 	   DBMS_OUTPUT.PUT_LINE(' Parameters value can not be null;');
 	  
END check_create_objects;
/

set serveroutput on;
declare
sql_query varchar2(500);
obj_count int;
current_user varchar2(100);
not_valid_user exception;
begin
	-- take current login user
	select user into current_user from dual;
	-- verify if it is not desired one	
	 	 if  user not in ('BSI','BSPP') then
	 	  	raise not_valid_user;
	 end if;
    -----------------------------
    check_create_objects(
	 					 'SEQUENCE',
	 					 'WRKFLW_WTHDOG_SEQ',
	 					 'CREATE SEQUENCE WRKFLW_WTHDOG_SEQ 
                          START WITH 100 
                          INCREMENT BY  1 
                          CACHE 100 
                          NOCYCLE'
	                    );
   
   -----------------------------
    check_create_objects(
	 					 'SEQUENCE',
	 					 'WRKFLW_ALERT_SEQ',
	 					 'CREATE SEQUENCE WRKFLW_ALERT_SEQ 
                          START WITH 100 
                          INCREMENT BY  1 
                          CACHE 100 
                          NOCYCLE'
	                    );
   -----------------------------
   	check_create_objects(
							'TABLE',
							'WFLW_SERVER',
							'CREATE TABLE WFLW_SERVER(WFS_ID INT,
                                       WFS_SERVER_NAME VARCHAR2(255 CHAR),
                                       WFS_CREATION_TIME TIMESTAMP,
                                       WFS_LAST_UPDT_TIME TIMESTAMP,
                                       WFS_DISABLE CHAR, 
                                       PRIMARY KEY(WFS_ID),
                                       CONSTRAINT WFS_SRV_NME UNIQUE(WFS_SERVER_NAME))
                                       '
							);
  -------------------------------
    check_create_objects(
    					'INDEX',
    					'INDEX_LST_UPDT_TIME',
    					'CREATE INDEX INDEX_LST_UPDT_TIME ON WFLW_SERVER(WFS_LAST_UPDT_TIME)'
    					);            
                      
	------------------------------
     
			check_create_objects(
							'TABLE',
							'WFLW_ALERT',
							'CREATE TABLE WFLW_ALERT(WFA_ID INT,
                                       WFA_SERVER_NAME VARCHAR2(255 CHAR),
                                       WFA_WORKFLOW_NAME VARCHAR2(200 CHAR),
                                       WFA_MESSAGE VARCHAR2(2000 CHAR),
                                       WFA_ALERT_TYPE VARCHAR2(255 CHAR),
                                       WFA_CREATION_TIME TIMESTAMP,
									   WFA_LAST_UPDT_TIME TIMESTAMP,
									   WFA_NEXT_NOTIFY_TIME TIMESTAMP, 
                                       PRIMARY KEY(WFA_ID),
                                       CONSTRAINT WFS_ALRT_NME UNIQUE(WFA_SERVER_NAME,WFA_WORKFLOW_NAME))'
                                       
							);
              
	
   -- Handle the invalid user exception 
	EXCEPTION
     WHEN not_valid_user THEN
	DBMS_OUTPUT.PUT_LINE('Unexpected User:'||current_user ||', Please login with BSI or BSPP user and then run script');
	
end ;
/

declare
sql_query varchar2(200);
obj_count int:=0;
begin
	 select count(*)  into obj_count from user_objects where object_type='PROCEDURE' and object_name='CHECK_CREATE_OBJECTS';
	--- dropped temporary stored procedure------
	if obj_count=1 then
	 	sql_query:='drop procedure CHECK_CREATE_OBJECTS';
	 	execute immediate sql_query;
	 	DBMS_OUTPUT.PUT_LINE('CHECK_CREATE_OBJECTS procedure dropped');
	end if;
end ;

/