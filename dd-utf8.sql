
-- shorten the primary key fields that would become too long with utf8
----------------------------------------------------------------------

alter table HARV_ATTR change column HARVESTER_ID HARVESTER_ID varchar(150) not null default '';
alter table HARV_ATTR change column HARV_ATTR_ID HARV_ATTR_ID varchar(150) not null default '';

alter table HARV_ATTR_FIELD drop primary key;
alter table HARV_ATTR_FIELD change column FLD_NAME FLD_NAME varchar(100) not null default '';
alter table HARV_ATTR_FIELD add primary key (HARV_ATTR_MD5, FLD_NAME, FLD_VALUE(200));

-- drop all current FULLTEXT keys
--------------------------------------------

alter table ATTRIBUTE drop key VALUE;

-- convert all CHAR, VARCHAR & TEXT columns in all tables
-- into UTF-8
----------------------------------------------------------

alter table ACLS convert to character set utf8;
alter table ACL_ROWS convert to character set utf8;
alter table ATTRIBUTE convert to character set utf8;
alter table CACHE convert to character set utf8;
alter table COMPLEX_ATTR_FIELD convert to character set utf8;
alter table COMPLEX_ATTR_ROW convert to character set utf8;
alter table DATAELEM convert to character set utf8;
alter table DATASET convert to character set utf8;
alter table DOC convert to character set utf8;
alter table DST2ROD convert to character set utf8;
alter table DST2TBL convert to character set utf8;
alter table DS_TABLE convert to character set utf8;
alter table FK_RELATION convert to character set utf8;
alter table FXV convert to character set utf8;
alter table HARV_ATTR convert to character set utf8;
alter table HARV_ATTR_FIELD convert to character set utf8;
alter table HLP_AREA convert to character set utf8;
alter table HLP_SCREEN convert to character set utf8;
alter table M_ATTRIBUTE convert to character set utf8;
alter table M_COMPLEX_ATTR convert to character set utf8;
alter table M_COMPLEX_ATTR_FIELD convert to character set utf8;
alter table NAMESPACE convert to character set utf8;
alter table ROD_ACTIVITIES convert to character set utf8;
alter table TBL2ELEM convert to character set utf8;

-- re-create all FULLTEXT keys

alter table ATTRIBUTE add fulltext (VALUE);

-- set the default character set for new columns --------------

alter database DataDict default character set utf8;