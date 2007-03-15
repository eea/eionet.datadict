-- MySQL dump 10.10
--
-- Host: localhost    Database: DataDict
-- ------------------------------------------------------
-- Server version	5.0.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ACLS`
--

DROP TABLE IF EXISTS `ACLS`;
CREATE TABLE `ACLS` (
  `ACL_ID` int(11) NOT NULL auto_increment,
  `ACL_NAME` varchar(100) NOT NULL default '',
  `PARENT_NAME` varchar(100) default NULL,
  `OWNER` varchar(255) NOT NULL default '',
  `DESCRIPTION` varchar(255) default '',
  PRIMARY KEY  (`ACL_ID`),
  UNIQUE KEY `ACL_NAME` (`ACL_NAME`,`PARENT_NAME`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `ACL_ROWS`
--

DROP TABLE IF EXISTS `ACL_ROWS`;
CREATE TABLE `ACL_ROWS` (
  `ACL_ID` int(11) NOT NULL default '0',
  `TYPE` enum('object','doc','dcc') NOT NULL default 'object',
  `ENTRY_TYPE` enum('owner','user','localgroup','other','foreign','unauthenticated','authenticated','mask') NOT NULL default 'user',
  `PRINCIPAL` char(16) NOT NULL default '',
  `PERMISSIONS` char(255) NOT NULL default '',
  `STATUS` int(1) default NULL,
  PRIMARY KEY  (`ACL_ID`,`TYPE`,`ENTRY_TYPE`,`PRINCIPAL`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `ATTRIBUTE`
--

DROP TABLE IF EXISTS `ATTRIBUTE`;
CREATE TABLE `ATTRIBUTE` (
  `M_ATTRIBUTE_ID` int(10) unsigned NOT NULL default '0',
  `DATAELEM_ID` int(10) unsigned NOT NULL default '0',
  `VALUE` text NOT NULL,
  `PARENT_TYPE` enum('E','C','DS','CSI','T') NOT NULL default 'E',
  PRIMARY KEY  (`M_ATTRIBUTE_ID`,`DATAELEM_ID`,`VALUE`(255),`PARENT_TYPE`),
  FULLTEXT KEY `VALUE` (`VALUE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `CACHE`
--

DROP TABLE IF EXISTS `CACHE`;
CREATE TABLE `CACHE` (
  `OBJ_ID` int(10) unsigned NOT NULL default '0',
  `OBJ_TYPE` enum('dst','tbl','elm') NOT NULL default 'dst',
  `ARTICLE` enum('pdf','xls','xform','xmlinst') NOT NULL default 'pdf',
  `FILENAME` varchar(255) default NULL,
  `CREATED` bigint(20) NOT NULL default '0',
  PRIMARY KEY  (`OBJ_ID`,`OBJ_TYPE`,`ARTICLE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `COMPLEX_ATTR_FIELD`
--

DROP TABLE IF EXISTS `COMPLEX_ATTR_FIELD`;
CREATE TABLE `COMPLEX_ATTR_FIELD` (
  `M_COMPLEX_ATTR_FIELD_ID` int(10) unsigned NOT NULL default '0',
  `VALUE` text NOT NULL,
  `ROW_ID` varchar(32) NOT NULL default '',
  PRIMARY KEY  (`ROW_ID`,`M_COMPLEX_ATTR_FIELD_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `COMPLEX_ATTR_ROW`
--

DROP TABLE IF EXISTS `COMPLEX_ATTR_ROW`;
CREATE TABLE `COMPLEX_ATTR_ROW` (
  `PARENT_ID` int(10) unsigned NOT NULL default '0',
  `PARENT_TYPE` enum('E','C','DS','FV','T') NOT NULL default 'E',
  `M_COMPLEX_ATTR_ID` int(10) unsigned NOT NULL default '0',
  `POSITION` int(3) NOT NULL default '0',
  `ROW_ID` varchar(32) NOT NULL default '',
  `HARV_ATTR_ID` varchar(32) default NULL,
  PRIMARY KEY  (`PARENT_ID`,`PARENT_TYPE`,`M_COMPLEX_ATTR_ID`,`POSITION`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `DATAELEM`
--

DROP TABLE IF EXISTS `DATAELEM`;
CREATE TABLE `DATAELEM` (
  `TYPE` enum('CH1','CH2') NOT NULL default 'CH2',
  `DATAELEM_ID` int(10) unsigned NOT NULL auto_increment,
  `NAMESPACE_ID` int(10) unsigned NOT NULL default '1',
  `SHORT_NAME` varchar(50) NOT NULL default '',
  `WORKING_USER` varchar(255) default NULL,
  `WORKING_COPY` enum('Y','N') NOT NULL default 'N',
  `REG_STATUS` enum('Recorded','Qualified','Released','Incomplete','Candidate') NOT NULL default 'Incomplete',
  `VERSION` int(4) unsigned NOT NULL default '1',
  `USER` varchar(255) default NULL,
  `DATE` bigint(20) NOT NULL default '0',
  `PARENT_NS` int(10) default NULL,
  `TOP_NS` int(10) default NULL,
  `IDENTIFIER` varchar(50) NOT NULL default '',
  `GIS` enum('','class','subclass','subtype') default NULL,
  `IS_ROD_PARAM` enum('true','false') NOT NULL default 'true',
  `CHECKEDOUT_COPY_ID` int(10) unsigned default NULL,
  PRIMARY KEY  (`DATAELEM_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `DATASET`
--

DROP TABLE IF EXISTS `DATASET`;
CREATE TABLE `DATASET` (
  `DATASET_ID` int(10) unsigned NOT NULL auto_increment,
  `SHORT_NAME` varchar(20) NOT NULL default '',
  `VERSION` int(4) unsigned NOT NULL default '1',
  `VISUAL` text,
  `DETAILED_VISUAL` text,
  `WORKING_USER` varchar(255) default NULL,
  `WORKING_COPY` enum('Y','N') NOT NULL default 'N',
  `REG_STATUS` enum('Recorded','Qualified','Released','Incomplete','Candidate') NOT NULL default 'Incomplete',
  `DATE` bigint(20) NOT NULL default '0',
  `USER` varchar(255) default NULL,
  `CORRESP_NS` int(10) unsigned NOT NULL default '0',
  `DELETED` varchar(255) default NULL,
  `IDENTIFIER` varchar(50) NOT NULL default '',
  `DISP_CREATE_LINKS` int(2) NOT NULL default '43',
  `CHECKEDOUT_COPY_ID` int(10) unsigned default NULL,
  PRIMARY KEY  (`DATASET_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `DOC`
--

DROP TABLE IF EXISTS `DOC`;
CREATE TABLE `DOC` (
  `OWNER_ID` int(10) unsigned NOT NULL default '0',
  `OWNER_TYPE` enum('dst','tbl','elm') NOT NULL default 'dst',
  `MD5_PATH` varchar(32) NOT NULL default '',
  `ABS_PATH` text NOT NULL,
  `TITLE` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`OWNER_ID`,`OWNER_TYPE`,`MD5_PATH`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `DST2ROD`
--

DROP TABLE IF EXISTS `DST2ROD`;
CREATE TABLE `DST2ROD` (
  `DATASET_ID` int(10) unsigned NOT NULL default '0',
  `ACTIVITY_ID` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`DATASET_ID`,`ACTIVITY_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `DST2TBL`
--

DROP TABLE IF EXISTS `DST2TBL`;
CREATE TABLE `DST2TBL` (
  `DATASET_ID` int(10) unsigned NOT NULL default '0',
  `TABLE_ID` int(10) unsigned NOT NULL default '0',
  `POSITION` int(3) unsigned NOT NULL default '0',
  PRIMARY KEY  (`DATASET_ID`,`TABLE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `DS_TABLE`
--

DROP TABLE IF EXISTS `DS_TABLE`;
CREATE TABLE `DS_TABLE` (
  `TABLE_ID` int(10) unsigned NOT NULL auto_increment,
  `SHORT_NAME` varchar(50) NOT NULL default '',
  `NAME` varchar(255) NOT NULL default '',
  `WORKING_USER` varchar(255) default NULL,
  `WORKING_COPY` enum('Y','N') NOT NULL default 'N',
  `VERSION` int(4) unsigned NOT NULL default '1',
  `DATE` bigint(20) NOT NULL default '0',
  `USER` varchar(255) default NULL,
  `CORRESP_NS` int(10) unsigned NOT NULL default '0',
  `PARENT_NS` int(10) unsigned NOT NULL default '0',
  `IDENTIFIER` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`TABLE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `FK_RELATION`
--

DROP TABLE IF EXISTS `FK_RELATION`;
CREATE TABLE `FK_RELATION` (
  `REL_ID` int(10) unsigned NOT NULL auto_increment,
  `A_ID` int(10) unsigned NOT NULL default '0',
  `B_ID` int(10) unsigned NOT NULL default '0',
  `A_CARDIN` enum('0','1','+','*') default '1',
  `B_CARDIN` enum('0','1','+','*') default '1',
  `DEFINITION` text NOT NULL,
  PRIMARY KEY  (`REL_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `FXV`
--

DROP TABLE IF EXISTS `FXV`;
CREATE TABLE `FXV` (
  `FXV_ID` int(10) unsigned NOT NULL auto_increment,
  `OWNER_ID` int(10) unsigned NOT NULL default '0',
  `OWNER_TYPE` enum('elem','attr') NOT NULL default 'elem',
  `VALUE` text NOT NULL,
  `IS_DEFAULT` enum('Y','N') NOT NULL default 'N',
  `DEFINITION` text NOT NULL,
  `SHORT_DESC` text NOT NULL,
  PRIMARY KEY  (`FXV_ID`),
  UNIQUE KEY `OWNER_ID` (`OWNER_ID`,`OWNER_TYPE`,`VALUE`(255))
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `HARV_ATTR`
--

DROP TABLE IF EXISTS `HARV_ATTR`;
CREATE TABLE `HARV_ATTR` (
  `HARV_ATTR_ID` varchar(150) NOT NULL default '',
  `HARVESTER_ID` varchar(150) NOT NULL default '',
  `HARVESTED` bigint(20) NOT NULL default '0',
  `MD5KEY` varchar(32) NOT NULL default '',
  `LOGICAL_ID` varchar(32) NOT NULL default '',
  PRIMARY KEY  (`HARV_ATTR_ID`,`HARVESTER_ID`,`HARVESTED`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `HARV_ATTR_FIELD`
--

DROP TABLE IF EXISTS `HARV_ATTR_FIELD`;
CREATE TABLE `HARV_ATTR_FIELD` (
  `HARV_ATTR_MD5` varchar(32) NOT NULL default '',
  `FLD_NAME` varchar(100) NOT NULL default '',
  `FLD_VALUE` text NOT NULL,
  PRIMARY KEY  (`HARV_ATTR_MD5`,`FLD_NAME`,`FLD_VALUE`(200))
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `HLP_AREA`
--

DROP TABLE IF EXISTS `HLP_AREA`;
CREATE TABLE `HLP_AREA` (
  `AREA_ID` varchar(100) NOT NULL default '',
  `SCREEN_ID` varchar(100) NOT NULL default '',
  `DESCRIPTION` text NOT NULL,
  `LANGUAGE` varchar(100) NOT NULL default '',
  `HTML` text NOT NULL,
  `MD5` varchar(32) NOT NULL default '',
  `POPUP_WIDTH` varchar(10) NOT NULL default '400',
  `POPUP_LENGTH` varchar(10) NOT NULL default '400',
  PRIMARY KEY  (`AREA_ID`,`SCREEN_ID`,`LANGUAGE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `HLP_SCREEN`
--

DROP TABLE IF EXISTS `HLP_SCREEN`;
CREATE TABLE `HLP_SCREEN` (
  `SCREEN_ID` varchar(100) NOT NULL default '',
  `DESCRIPTION` text NOT NULL,
  PRIMARY KEY  (`SCREEN_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `M_ATTRIBUTE`
--

DROP TABLE IF EXISTS `M_ATTRIBUTE`;
CREATE TABLE `M_ATTRIBUTE` (
  `M_ATTRIBUTE_ID` int(10) unsigned NOT NULL auto_increment,
  `NAME` varchar(255) NOT NULL default '',
  `OBLIGATION` enum('M','O','C') NOT NULL default 'M',
  `DEFINITION` text NOT NULL,
  `SHORT_NAME` varchar(50) NOT NULL default '',
  `NAMESPACE_ID` int(10) unsigned NOT NULL default '44',
  `DISP_TYPE` enum('text','textarea','select','image') default 'text',
  `DISP_ORDER` int(3) unsigned NOT NULL default '999',
  `DISP_WHEN` int(2) unsigned NOT NULL default '31',
  `DISP_WIDTH` int(3) unsigned NOT NULL default '20',
  `DISP_HEIGHT` int(3) unsigned NOT NULL default '1',
  `DISP_MULTIPLE` enum('0','1') NOT NULL default '0',
  `INHERIT` enum('0','1','2') NOT NULL default '0',
  PRIMARY KEY  (`M_ATTRIBUTE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `M_COMPLEX_ATTR`
--

DROP TABLE IF EXISTS `M_COMPLEX_ATTR`;
CREATE TABLE `M_COMPLEX_ATTR` (
  `M_COMPLEX_ATTR_ID` int(10) unsigned NOT NULL auto_increment,
  `NAME` varchar(255) NOT NULL default '',
  `OBLIGATION` enum('M','O','C') NOT NULL default 'M',
  `DEFINITION` text NOT NULL,
  `SHORT_NAME` varchar(50) NOT NULL default '',
  `NAMESPACE_ID` int(10) unsigned NOT NULL default '1',
  `DISP_ORDER` int(3) unsigned NOT NULL default '999',
  `DISP_WHEN` enum('1','2','3','4','5','6','7') NOT NULL default '7',
  `INHERIT` enum('0','1','2') NOT NULL default '0',
  `HARVESTER_ID` varchar(225) default NULL,
  PRIMARY KEY  (`M_COMPLEX_ATTR_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `M_COMPLEX_ATTR_FIELD`
--

DROP TABLE IF EXISTS `M_COMPLEX_ATTR_FIELD`;
CREATE TABLE `M_COMPLEX_ATTR_FIELD` (
  `M_COMPLEX_ATTR_ID` int(10) unsigned NOT NULL default '0',
  `NAME` varchar(255) NOT NULL default '',
  `M_COMPLEX_ATTR_FIELD_ID` int(10) unsigned NOT NULL auto_increment,
  `DEFINITION` text NOT NULL,
  `POSITION` int(2) NOT NULL default '0',
  `PRIORITY` enum('0','1') NOT NULL default '0',
  `HARV_ATTR_FLD_NAME` varchar(200) default NULL,
  PRIMARY KEY  (`M_COMPLEX_ATTR_FIELD_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `NAMESPACE`
--

DROP TABLE IF EXISTS `NAMESPACE`;
CREATE TABLE `NAMESPACE` (
  `NAMESPACE_ID` int(10) unsigned NOT NULL auto_increment,
  `SHORT_NAME` varchar(255) NOT NULL default '',
  `FULL_NAME` varchar(255) NOT NULL default '',
  `DEFINITION` varchar(255) NOT NULL default '',
  `PARENT_NS` int(10) unsigned default NULL,
  `WORKING_USER` varchar(255) default NULL,
  PRIMARY KEY  (`NAMESPACE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `ROD_ACTIVITIES`
--

DROP TABLE IF EXISTS `ROD_ACTIVITIES`;
CREATE TABLE `ROD_ACTIVITIES` (
  `ACTIVITY_ID` int(10) unsigned NOT NULL default '0',
  `ACTIVITY_TITLE` text NOT NULL,
  `LEGINSTR_ID` int(10) unsigned NOT NULL default '0',
  `LEGINSTR_TITLE` text NOT NULL,
  PRIMARY KEY  (`ACTIVITY_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `TBL2ELEM`
--

DROP TABLE IF EXISTS `TBL2ELEM`;
CREATE TABLE `TBL2ELEM` (
  `TABLE_ID` int(10) unsigned NOT NULL default '0',
  `DATAELEM_ID` int(10) unsigned NOT NULL default '0',
  `POSITION` int(3) unsigned NOT NULL default '0',
  PRIMARY KEY  (`TABLE_ID`,`DATAELEM_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

