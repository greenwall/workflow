CREATE TABLE COLLECT_LOCKED_ACCOUNT
(
  ID                       VARCHAR2(36) NOT NULL PRIMARY KEY,
  USER_ID                  VARCHAR2(10 CHAR) NOT NULL,
  STATUS                   VARCHAR2(10 CHAR) NOT NULL,
  METHOD_EXCEPTION_MESSAGE VARCHAR2(2000 CHAR),
  CREATION_TIME            TIMESTAMP(6)        NOT NULL
);
