
-- By default, elements in table shall be non-mandatory
alter table TBL2ELEM change column MANDATORY MANDATORY tinyint(1) not null default 0;

-- collect IDs of tables from datasets whose short name starts with "WISE-Soe:"
create temporary table TABLE_ID_TEMP select distinct TABLE_ID from DST2TBL where DATASET_ID in (select DATASET_ID from DATASET where SHORT_NAME like 'WISE-Soe:%');

-- make elements in all above-collected tables non-mandatory
update TBL2ELEM set MANDATORY=0 where TABLE_ID in (select TABLE_ID from TABLE_ID_TEMP);

-- drop above-created temporary table
drop table TABLE_ID_TEMP;


