
--
-- This script fixes the incorrect values of ROW_ID in COMPLEX_ATTR_ROW and COMPLEX_ATTR_FIELD tables.
-- The ROW_ID must be a MD5 hash of COMPLEX_ATTR_ROW's PARENT_ID, PARENT_TYPE, M_COMPLEX_ATTR_ID and POSITION concatenated.
--

alter table COMPLEX_ATTR_ROW add column CORRECT_ROW_ID varchar(32) default null;
alter table COMPLEX_ATTR_FIELD add column CORRECT_ROW_ID varchar(32) default null;

update COMPLEX_ATTR_ROW set CORRECT_ROW_ID=md5(concat(PARENT_ID,PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION));
update COMPLEX_ATTR_ROW set CORRECT_ROW_ID=NULL where ROW_ID=CORRECT_ROW_ID;
update COMPLEX_ATTR_FIELD set CORRECT_ROW_ID = (select CORRECT_ROW_ID from COMPLEX_ATTR_ROW where COMPLEX_ATTR_ROW.ROW_ID=COMPLEX_ATTR_FIELD.ROW_ID and CORRECT_ROW_ID is not null);

update COMPLEX_ATTR_ROW set ROW_ID=CORRECT_ROW_ID where CORRECT_ROW_ID is not null;
update COMPLEX_ATTR_FIELD set ROW_ID=CORRECT_ROW_ID where CORRECT_ROW_ID is not null;

alter table COMPLEX_ATTR_ROW drop column CORRECT_ROW_ID;
alter table COMPLEX_ATTR_FIELD drop column CORRECT_ROW_ID;
