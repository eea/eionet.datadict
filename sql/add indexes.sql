
-- ------------------------------------------------------------------------------------------------------
-- Re-arrange indexes of ATTRIBUTE
-- ------------------------------------------------------------------------------------------------------

alter table ATTRIBUTE drop primary key;
alter table ATTRIBUTE add index (DATAELEM_ID);
alter table ATTRIBUTE add index (PARENT_TYPE);
alter table ATTRIBUTE add index (M_ATTRIBUTE_ID);
alter table ATTRIBUTE add unique index ATTRIBUTE_UNIQUE (DATAELEM_ID,PARENT_TYPE,M_ATTRIBUTE_ID,VALUE(255));

-- ------------------------------------------------------------------------------------------------------
--  Fix COMPLEX_ATTR_ROW where ROW_ID is not md5(concat(PARENT_ID,PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION)
-- ------------------------------------------------------------------------------------------------------

insert into
  COMPLEX_ATTR_FIELD (M_COMPLEX_ATTR_FIELD_ID,VALUE,ROW_ID)
select
  M_COMPLEX_ATTR_FIELD_ID,VALUE,md5(concat(PARENT_ID,PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION))
from
  COMPLEX_ATTR_FIELD,COMPLEX_ATTR_ROW
where
  COMPLEX_ATTR_FIELD.ROW_ID=COMPLEX_ATTR_ROW.ROW_ID
  and md5(concat(PARENT_ID,PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION)) <> COMPLEX_ATTR_ROW.ROW_ID;

update COMPLEX_ATTR_ROW set ROW_ID=md5(concat(PARENT_ID,PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION))
where ROW_ID <> md5(concat(PARENT_ID,PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION));

-- ------------------------------------------------------------------------------------------------------
-- Re-arrange indexes of COMPLEX_ATTR_ROW table
-- ------------------------------------------------------------------------------------------------------

alter table COMPLEX_ATTR_ROW drop primary key;
alter table COMPLEX_ATTR_ROW add index (PARENT_ID);
alter table COMPLEX_ATTR_ROW add index (PARENT_TYPE);
alter table COMPLEX_ATTR_ROW add index (M_COMPLEX_ATTR_ID);
alter table COMPLEX_ATTR_ROW add unique index ROW_ID (ROW_ID);

-- ------------------------------------------------------------------------------------------------------
-- Re-arrange indexes of COMPLEX_ATTR_ROW table
-- ------------------------------------------------------------------------------------------------------

alter table COMPLEX_ATTR_FIELD drop primary key;
alter table COMPLEX_ATTR_FIELD add index (ROW_ID);
alter table COMPLEX_ATTR_FIELD add index (M_COMPLEX_ATTR_FIELD_ID);
alter table COMPLEX_ATTR_FIELD add unique index COMPLEX_ATTR_FIELD_UNIQUE (ROW_ID,M_COMPLEX_ATTR_FIELD_ID);

-- ------------------------------------------------------------------------------------------------------
-- Add new indexes to DATAELEM table
-- ------------------------------------------------------------------------------------------------------

alter table DATAELEM add index (IDENTIFIER);
alter table DATAELEM add index (SHORT_NAME);
alter table DATAELEM add index (PARENT_NS);

-- ------------------------------------------------------------------------------------------------------
-- Add new indexes to DATASET table
-- ------------------------------------------------------------------------------------------------------

alter table DATASET add index (IDENTIFIER);
alter table DATASET add index (SHORT_NAME);
alter table DATASET add index (CORRESP_NS);

-- ------------------------------------------------------------------------------------------------------
-- Re-arrange indexes of DOC table
-- ------------------------------------------------------------------------------------------------------

alter table DOC drop primary key;
alter table DOC add index (OWNER_ID);
alter table DOC add index (OWNER_TYPE);
alter table DOC add unique index DOC_UNIQUE (OWNER_ID,OWNER_TYPE,MD5_PATH);

-- ------------------------------------------------------------------------------------------------------
-- Re-arrange indexes of DST2ROD table
-- ------------------------------------------------------------------------------------------------------

alter table DST2ROD drop primary key;
alter table DST2ROD add index (DATASET_ID);
alter table DST2ROD add index (ACTIVITY_ID);
alter table DST2ROD add unique index DST2ROD_UNIQUE (DATASET_ID,ACTIVITY_ID);

-- ------------------------------------------------------------------------------------------------------
-- Re-arrange indexes of DST2TBL table
-- ------------------------------------------------------------------------------------------------------

alter table DST2TBL drop primary key;
alter table DST2TBL add index (DATASET_ID);
alter table DST2TBL add index (TABLE_ID);
alter table DST2TBL add unique index DST2TBL_UNIQUE (DATASET_ID,TABLE_ID);

-- ------------------------------------------------------------------------------------------------------
-- Add new indexes to DS_TABLE table
-- ------------------------------------------------------------------------------------------------------

alter table DS_TABLE add index (IDENTIFIER);
alter table DS_TABLE add index (SHORT_NAME);
alter table DS_TABLE add index (CORRESP_NS);
alter table DS_TABLE add index (PARENT_NS);

-- ------------------------------------------------------------------------------------------------------
-- Add new indexes to FK_RELATION table
-- ------------------------------------------------------------------------------------------------------

alter table FK_RELATION add index (A_ID);
alter table FK_RELATION add index (B_ID);

-- ------------------------------------------------------------------------------------------------------
-- Add new indexes to FXV table
-- ------------------------------------------------------------------------------------------------------

alter table FXV add index (OWNER_TYPE);
alter table FXV add unique index FXV_UNIQUE (OWNER_ID,OWNER_TYPE,VALUE(255));

-- ------------------------------------------------------------------------------------------------------
-- Re-arrange indexes of TBL2ELEM table
-- ------------------------------------------------------------------------------------------------------

alter table TBL2ELEM drop primary key;
alter table TBL2ELEM add index (TABLE_ID);
alter table TBL2ELEM add index (DATAELEM_ID);
alter table TBL2ELEM add unique index TBL2ELEM_UNIQUE (TABLE_ID,DATAELEM_ID);

