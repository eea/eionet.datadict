
--
-- drop un-necessary tables
--

drop table AUTO_ID;
drop table CHOICE;
drop table CLASS2ELEM;
drop table CLSF_SCHEME;
drop table CSI_RELATION;
drop table CS_ITEM;
drop table CONTENT;
drop table DATA_CLASS;
drop table SEQUENCE;

--
-- update DATASET table
--

alter table DATASET add column DISP_CREATE_LINKS int(2) NOT NULL default '3';

--
-- update DATAELEM table
--

alter table DATAELEM drop column EXTENDS;
alter table DATAELEM drop column LOOKUP_ELEM;
update DATAELEM set IDENTIFIER=concat('Percentile', substring(IDENTIFIER,1,2)) where IDENTIFIER like '%_ILE';

--
-- clean DS_TABLE table
--

alter table DS_TABLE drop column DEFINITION;
alter table DS_TABLE drop column TYPE;
alter table DS_TABLE drop column REG_STATUS;