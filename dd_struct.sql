-- MySQL dump 8.23
--
-- Host: localhost    Database: DataDict
---------------------------------------------------------
-- Server version	3.23.58

--
-- Table structure for table `ACLS`
--

CREATE TABLE ACLS (
  ACL_ID int(11) NOT NULL auto_increment,
  ACL_NAME varchar(100) NOT NULL default '',
  PARENT_NAME varchar(100) default NULL,
  OWNER varchar(255) NOT NULL default '',
  DESCRIPTION varchar(255) default '',
  PRIMARY KEY  (ACL_ID)
) TYPE=MyISAM;

--
-- Table structure for table `ACL_ROWS`
--

CREATE TABLE ACL_ROWS (
  ACL_ID int(11) NOT NULL default '0',
  TYPE enum('object','doc','dcc') NOT NULL default 'object',
  ENTRY_TYPE enum('owner','user','localgroup','other','foreign','unauthenticated','authenticated','mask') NOT NULL default 'user',
  PRINCIPAL char(16) NOT NULL default '',
  PERMISSIONS char(255) NOT NULL default '',
  STATUS int(1) default NULL
) TYPE=MyISAM;

--
-- Table structure for table `ATTRIBUTE`
--

CREATE TABLE ATTRIBUTE (
  M_ATTRIBUTE_ID int(10) unsigned NOT NULL default '0',
  DATAELEM_ID int(10) unsigned NOT NULL default '0',
  VALUE text NOT NULL,
  PARENT_TYPE enum('E','C','DS','CSI','T') NOT NULL default 'E',
  PRIMARY KEY  (M_ATTRIBUTE_ID,DATAELEM_ID,VALUE(255),PARENT_TYPE),
  FULLTEXT KEY value (VALUE)
) TYPE=MyISAM;

--
-- Table structure for table `AUTO_ID`
--

CREATE TABLE AUTO_ID (
  ID int(10) unsigned NOT NULL auto_increment,
  TYPE enum('seq','chc') NOT NULL default 'seq',
  PRIMARY KEY  (ID,TYPE)
) TYPE=MyISAM;

--
-- Table structure for table `CHOICE`
--

CREATE TABLE CHOICE (
  CHOICE_ID int(10) unsigned NOT NULL default '0',
  CHILD_ID int(10) unsigned NOT NULL default '0',
  CHILD_TYPE enum('elm','seq') NOT NULL default 'elm',
  PRIMARY KEY  (CHOICE_ID,CHILD_ID,CHILD_TYPE)
) TYPE=MyISAM;

--
-- Table structure for table `CLASS2ELEM`
--

CREATE TABLE CLASS2ELEM (
  DATAELEM_ID int(10) unsigned NOT NULL default '0',
  DATACLASS_ID int(10) unsigned NOT NULL default '0'
) TYPE=MyISAM;

--
-- Table structure for table `CLSF_SCHEME`
--

CREATE TABLE CLSF_SCHEME (
  CS_ID int(10) unsigned NOT NULL auto_increment,
  CS_NAME varchar(255) NOT NULL default '',
  CS_TYPE varchar(100) NOT NULL default '',
  CS_VERSION varchar(20) NOT NULL default '0.1',
  CS_DESCRIPTION text NOT NULL,
  PRIMARY KEY  (CS_ID)
) TYPE=MyISAM;

--
-- Table structure for table `COMPLEX_ATTR_FIELD`
--

CREATE TABLE COMPLEX_ATTR_FIELD (
  M_COMPLEX_ATTR_FIELD_ID int(10) unsigned NOT NULL default '0',
  VALUE text NOT NULL,
  ROW_ID varchar(32) NOT NULL default '',
  PRIMARY KEY  (ROW_ID,M_COMPLEX_ATTR_FIELD_ID)
) TYPE=MyISAM;

--
-- Table structure for table `COMPLEX_ATTR_ROW`
--

CREATE TABLE COMPLEX_ATTR_ROW (
  PARENT_ID int(10) unsigned NOT NULL default '0',
  PARENT_TYPE enum('E','C','DS','FV','T') NOT NULL default 'E',
  M_COMPLEX_ATTR_ID int(10) unsigned NOT NULL default '0',
  POSITION int(3) NOT NULL default '0',
  ROW_ID varchar(32) NOT NULL default '',
  HARV_ATTR_ID varchar(32) default NULL,
  PRIMARY KEY  (PARENT_ID,PARENT_TYPE,M_COMPLEX_ATTR_ID,POSITION)
) TYPE=MyISAM;

--
-- Table structure for table `CONTENT`
--

CREATE TABLE CONTENT (
  PARENT_ID int(10) unsigned NOT NULL default '0',
  CHILD_ID int(10) unsigned NOT NULL default '0',
  PARENT_TYPE enum('elm','seq','chc') NOT NULL default 'elm',
  CHILD_TYPE enum('elm','seq','chc') NOT NULL default 'elm',
  PRIMARY KEY  (PARENT_ID,CHILD_ID,PARENT_TYPE,CHILD_TYPE)
) TYPE=MyISAM;

--
-- Table structure for table `CSI_RELATION`
--

CREATE TABLE CSI_RELATION (
  PARENT_CSI int(10) unsigned NOT NULL default '0',
  CHILD_CSI int(10) unsigned NOT NULL default '0',
  REL_TYPE enum('taxonomy','abstract') NOT NULL default 'taxonomy',
  REL_DESCRIPTION text NOT NULL,
  PRIMARY KEY  (PARENT_CSI,CHILD_CSI)
) TYPE=MyISAM;

--
-- Table structure for table `CS_ITEM`
--

CREATE TABLE CS_ITEM (
  CSI_ID int(10) unsigned NOT NULL auto_increment,
  CS_ID int(10) unsigned NOT NULL default '0',
  CSI_TYPE varchar(100) NOT NULL default '',
  CSI_VALUE text NOT NULL,
  COMPONENT_ID int(10) unsigned NOT NULL default '0',
  COMPONENT_TYPE enum('elem','attr','elem_name') NOT NULL default 'elem',
  IS_DEFAULT enum('Y','N') NOT NULL default 'N',
  POSITION int(3) NOT NULL default '0',
  PRIMARY KEY  (CSI_ID)
) TYPE=MyISAM;

--
-- Table structure for table `DATAELEM`
--

CREATE TABLE DATAELEM (
  TYPE enum('AGG','CH1','CH2') NOT NULL default 'AGG',
  DATAELEM_ID int(10) unsigned NOT NULL auto_increment,
  NAMESPACE_ID int(10) unsigned NOT NULL default '1',
  SHORT_NAME varchar(50) NOT NULL default '',
  EXTENDS int(10) unsigned default NULL,
  LOOKUP_ELEM int(10) unsigned NOT NULL default '0',
  WORKING_USER varchar(255) default NULL,
  WORKING_COPY enum('Y','N') NOT NULL default 'N',
  REG_STATUS enum('Recorded','Qualified','Released','Incomplete','Candidate') NOT NULL default 'Incomplete',
  VERSION int(4) unsigned NOT NULL default '1',
  USER varchar(255) default NULL,
  DATE bigint(20) NOT NULL default '0',
  PARENT_NS int(10) default NULL,
  TOP_NS int(10) default NULL,
  PRIMARY KEY  (DATAELEM_ID)
) TYPE=MyISAM;

--
-- Table structure for table `DATASET`
--

CREATE TABLE DATASET (
  DATASET_ID int(10) unsigned NOT NULL auto_increment,
  SHORT_NAME varchar(20) NOT NULL default '',
  VERSION int(4) unsigned NOT NULL default '1',
  VISUAL text,
  DETAILED_VISUAL text,
  WORKING_USER varchar(255) default NULL,
  WORKING_COPY enum('Y','N') NOT NULL default 'N',
  REG_STATUS enum('Recorded','Qualified','Released','Incomplete','Candidate') NOT NULL default 'Incomplete',
  DATE bigint(20) NOT NULL default '0',
  USER varchar(255) default NULL,
  CORRESP_NS int(10) unsigned NOT NULL default '0',
  DELETED varchar(255) default NULL,
  PRIMARY KEY  (DATASET_ID)
) TYPE=MyISAM;

--
-- Table structure for table `DATA_CLASS`
--

CREATE TABLE DATA_CLASS (
  DATACLASS_ID int(10) unsigned NOT NULL auto_increment,
  NAMESPACE_ID varchar(32) NOT NULL default 'basens',
  SHORT_NAME varchar(50) NOT NULL default '',
  PRIMARY KEY  (DATACLASS_ID)
) TYPE=MyISAM;

--
-- Table structure for table `DST2TBL`
--

CREATE TABLE DST2TBL (
  DATASET_ID int(10) unsigned NOT NULL default '0',
  TABLE_ID int(10) unsigned NOT NULL default '0',
  POSITION int(3) unsigned NOT NULL default '0',
  PRIMARY KEY  (DATASET_ID,TABLE_ID)
) TYPE=MyISAM;

--
-- Table structure for table `DS_TABLE`
--

CREATE TABLE DS_TABLE (
  TABLE_ID int(10) unsigned NOT NULL auto_increment,
  SHORT_NAME varchar(50) NOT NULL default '',
  NAME varchar(255) NOT NULL default '',
  DEFINITION text NOT NULL,
  TYPE enum('normal','lookup') default 'normal',
  WORKING_USER varchar(255) default NULL,
  WORKING_COPY enum('Y','N') NOT NULL default 'N',
  REG_STATUS enum('Recorded','Qualified','Released','Incomplete','Candidate') NOT NULL default 'Incomplete',
  VERSION int(4) unsigned NOT NULL default '1',
  DATE bigint(20) NOT NULL default '0',
  USER varchar(255) default NULL,
  CORRESP_NS int(10) unsigned NOT NULL default '0',
  PARENT_NS int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (TABLE_ID)
) TYPE=MyISAM;

--
-- Table structure for table `FIXED_VALUE`
--

CREATE TABLE FIXED_VALUE (
  FIXED_VALUE_ID int(10) unsigned NOT NULL auto_increment,
  DATAELEM_ID int(10) unsigned NOT NULL default '0',
  VALUE varchar(255) NOT NULL default '',
  REPR_ELEM int(10) unsigned default NULL,
  PARENT_TYPE enum('elem','attr') NOT NULL default 'elem',
  IS_DEFAULT enum('Y','N') NOT NULL default 'N',
  PRIMARY KEY  (FIXED_VALUE_ID)
) TYPE=MyISAM;

--
-- Table structure for table `FK_RELATION`
--

CREATE TABLE FK_RELATION (
  REL_ID int(10) unsigned NOT NULL auto_increment,
  A_ID int(10) unsigned NOT NULL default '0',
  B_ID int(10) unsigned NOT NULL default '0',
  A_CARDIN enum('0','1','+','*') default '1',
  B_CARDIN enum('0','1','+','*') default '1',
  DEFINITION text NOT NULL,
  PRIMARY KEY  (REL_ID)
) TYPE=MyISAM;

--
-- Table structure for table `HARV_ATTR`
--

CREATE TABLE HARV_ATTR (
  HARV_ATTR_ID varchar(255) NOT NULL default '',
  HARVESTER_ID varchar(225) NOT NULL default '',
  HARVESTED bigint(20) NOT NULL default '0',
  MD5KEY varchar(32) NOT NULL default '',
  LOGICAL_ID varchar(32) NOT NULL default '',
  PRIMARY KEY  (HARV_ATTR_ID,HARVESTER_ID,HARVESTED)
) TYPE=MyISAM;

--
-- Table structure for table `HARV_ATTR_FIELD`
--

CREATE TABLE HARV_ATTR_FIELD (
  HARV_ATTR_MD5 varchar(32) NOT NULL default '',
  FLD_NAME varchar(200) NOT NULL default '',
  FLD_VALUE text NOT NULL,
  PRIMARY KEY  (HARV_ATTR_MD5,FLD_NAME,FLD_VALUE(250))
) TYPE=MyISAM;

--
-- Table structure for table `M_ATTRIBUTE`
--

CREATE TABLE M_ATTRIBUTE (
  M_ATTRIBUTE_ID int(10) unsigned NOT NULL auto_increment,
  NAME varchar(255) NOT NULL default '',
  OBLIGATION enum('M','O','C') NOT NULL default 'M',
  DEFINITION text NOT NULL,
  SHORT_NAME varchar(50) NOT NULL default '',
  NAMESPACE_ID int(10) unsigned NOT NULL default '44',
  DISP_TYPE enum('text','textarea','select','image') default 'text',
  DISP_ORDER int(3) unsigned NOT NULL default '999',
  DISP_WHEN int(2) unsigned NOT NULL default '31',
  DISP_WIDTH int(3) unsigned NOT NULL default '20',
  DISP_HEIGHT int(3) unsigned NOT NULL default '1',
  DISP_MULTIPLE enum('0','1') NOT NULL default '0',
  INHERIT enum('0','1','2') NOT NULL default '0',
  PRIMARY KEY  (M_ATTRIBUTE_ID)
) TYPE=MyISAM;

--
-- Table structure for table `M_COMPLEX_ATTR`
--

CREATE TABLE M_COMPLEX_ATTR (
  M_COMPLEX_ATTR_ID int(10) unsigned NOT NULL auto_increment,
  NAME varchar(255) NOT NULL default '',
  OBLIGATION enum('M','O','C') NOT NULL default 'M',
  DEFINITION text NOT NULL,
  SHORT_NAME varchar(50) NOT NULL default '',
  NAMESPACE_ID int(10) unsigned NOT NULL default '1',
  DISP_ORDER int(3) unsigned NOT NULL default '999',
  DISP_WHEN enum('1','2','3','4','5','6','7') NOT NULL default '7',
  INHERIT enum('0','1','2') NOT NULL default '0',
  HARVESTER_ID varchar(225) default NULL,
  PRIMARY KEY  (M_COMPLEX_ATTR_ID)
) TYPE=MyISAM;

--
-- Table structure for table `M_COMPLEX_ATTR_FIELD`
--

CREATE TABLE M_COMPLEX_ATTR_FIELD (
  M_COMPLEX_ATTR_ID int(10) unsigned NOT NULL default '0',
  NAME varchar(255) NOT NULL default '',
  M_COMPLEX_ATTR_FIELD_ID int(10) unsigned NOT NULL auto_increment,
  DEFINITION text NOT NULL,
  POSITION int(2) NOT NULL default '0',
  PRIORITY enum('0','1') NOT NULL default '0',
  HARV_ATTR_FLD_NAME varchar(200) default NULL,
  PRIMARY KEY  (M_COMPLEX_ATTR_FIELD_ID)
) TYPE=MyISAM;

--
-- Table structure for table `NAMESPACE`
--

CREATE TABLE NAMESPACE (
  NAMESPACE_ID int(10) unsigned NOT NULL auto_increment,
  SHORT_NAME varchar(255) NOT NULL default '',
  FULL_NAME varchar(255) NOT NULL default '',
  DEFINITION varchar(255) NOT NULL default '',
  PARENT_NS int(10) unsigned default NULL,
  WORKING_USER varchar(255) default NULL,
  PRIMARY KEY  (NAMESPACE_ID)
) TYPE=MyISAM;

--
-- Table structure for table `SEQUENCE`
--

CREATE TABLE SEQUENCE (
  SEQUENCE_ID int(10) unsigned NOT NULL default '0',
  CHILD_ID int(10) unsigned NOT NULL default '0',
  CHILD_TYPE enum('elm','chc') NOT NULL default 'elm',
  POSITION int(4) unsigned NOT NULL default '0',
  MIN_OCCURS enum('0','1') NOT NULL default '0',
  MAX_OCCURS enum('1','unbounded') NOT NULL default '1',
  PRIMARY KEY  (SEQUENCE_ID,CHILD_ID,CHILD_TYPE)
) TYPE=MyISAM;

--
-- Table structure for table `TBL2ELEM`
--

CREATE TABLE TBL2ELEM (
  TABLE_ID int(10) unsigned NOT NULL default '0',
  DATAELEM_ID int(10) unsigned NOT NULL default '0',
  POSITION int(3) unsigned NOT NULL default '0',
  PRIMARY KEY  (TABLE_ID,DATAELEM_ID)
) TYPE=MyISAM;

