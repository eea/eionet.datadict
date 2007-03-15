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
-- Dumping data for table `M_COMPLEX_ATTR`
--


/*!40000 ALTER TABLE `M_COMPLEX_ATTR` DISABLE KEYS */;
LOCK TABLES `M_COMPLEX_ATTR` WRITE;
INSERT INTO `M_COMPLEX_ATTR` (`M_COMPLEX_ATTR_ID`, `NAME`, `OBLIGATION`, `DEFINITION`, `SHORT_NAME`, `NAMESPACE_ID`, `DISP_ORDER`, `DISP_WHEN`, `INHERIT`, `HARVESTER_ID`) VALUES (7,'Guideline text','O','Link to additional guideline text documents.','Guidelines',3,21,'7','0',NULL),(8,'Registration Authority','O','Institution authorised to register data element specifications. Source ISO 11179.','RegistrationAuthority',2,54,'7','2',NULL),(11,'Submit Organisation','M','The organisation that has submitted the data element specification for addition, change or cancellation. ','SubmitOrganisation',2,53,'7','2',NULL),(12,'Responsible Organisation','O','The organisation responsible for the content descriptions of the mandatory attributes by which the data element is specified. Source ISO 11179.','RespOrganisation',2,52,'7','2',NULL),(14,'ROD','O','Link to the relevent record for this dataflow in the reporting obligations database.','ROD',3,999,'7','0',NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `M_COMPLEX_ATTR` ENABLE KEYS */;

--
-- Dumping data for table `M_ATTRIBUTE`
--


/*!40000 ALTER TABLE `M_ATTRIBUTE` DISABLE KEYS */;
LOCK TABLES `M_ATTRIBUTE` WRITE;
INSERT INTO `M_ATTRIBUTE` (`M_ATTRIBUTE_ID`, `NAME`, `OBLIGATION`, `DEFINITION`, `SHORT_NAME`, `NAMESPACE_ID`, `DISP_TYPE`, `DISP_ORDER`, `DISP_WHEN`, `DISP_WIDTH`, `DISP_HEIGHT`, `DISP_MULTIPLE`, `INHERIT`) VALUES (1,'Name','M','Single or multi word designation assigned to a data element. Source: ISO 11179.','Name',2,'text',1,75,40,1,'0','0'),(39,'Version','O','This attribute refers to the version number used by the submit organisation in their guidelines/documents.','Version',3,'text',57,8,20,1,'0','0'),(4,'Definition','O','Statement that expresses the essential nature of a data element and permits its differentiation from all other data elements. Source: ISO 11179.','Definition',2,'textarea',8,75,60,4,'0','0'),(5,'Keywords','O','One or more significant words used for retrieval of data elements. Source: ISO 11179.','Keyword',2,'text',5,75,40,1,'1','1'),(24,'Maximum size','M','The maximum number of storage units to represent the data element value.\r\nSource: ISO 11179.','MaxSize',2,'text',32,1,5,1,'0','0'),(25,'Datatype','M','A set of distinct values for representing the data element value. Source: ISO 11179.','Datatype',2,'select',30,3,15,1,'0','0'),(8,'Minimum size','M','The minimum number of storage units to represent the data element value.\r\nSource: ISO 11179.','MinSize',2,'text',31,1,5,1,'0','0'),(15,'Short Description','O','Brief text about the content of the dataset.','ShortDescription',3,'textarea',7,72,60,1,'0','0'),(17,'Methodology for obtaining data','O','Brief descritpion of methodology for obtaining data in dataset.','Methodology',3,'textarea',20,75,60,6,'0','0'),(18,'Planned updating frequency','O','Planned update frequency for the definitions in the dataset.','PlannedUpdFreq',3,'text',51,8,10,1,'0','0'),(31,'Decimal precision','O','This attribute is the number of decimal places.','DecimalPrecision',3,'text',33,1,20,1,'0','0'),(28,'Public or Internal','O','The information is either public or for database administration.','PublicOrInternal',3,'select',38,3,15,1,'0','0'),(32,'Unit','O','The unit of measurment for data elements (where appropriate).','Unit',3,'text',34,1,20,1,'0','0'),(33,'Minimum value','O','','MinValue',3,'text',35,1,20,1,'0','0'),(34,'Maximum value','O','','MaxValue',3,'text',36,1,20,1,'0','0'),(37,'EEA Issue','O','An issue form the EEA environmental issues list.','EEAissue',3,'select',6,75,20,1,'1','1'),(40,'Descriptive image','O','','Descriptive_image',3,'image',999,67,20,1,'1','0');
UNLOCK TABLES;
/*!40000 ALTER TABLE `M_ATTRIBUTE` ENABLE KEYS */;

--
-- Dumping data for table `M_COMPLEX_ATTR_FIELD`
--


/*!40000 ALTER TABLE `M_COMPLEX_ATTR_FIELD` DISABLE KEYS */;
LOCK TABLES `M_COMPLEX_ATTR_FIELD` WRITE;
INSERT INTO `M_COMPLEX_ATTR_FIELD` (`M_COMPLEX_ATTR_ID`, `NAME`, `M_COMPLEX_ATTR_FIELD_ID`, `DEFINITION`, `POSITION`, `PRIORITY`, `HARV_ATTR_FLD_NAME`) VALUES (1,'field6',4,'field6 definition',2,'0',NULL),(1,'field3',3,'field3 definitioon',1,'0',NULL),(1,'field7',6,'field7 definition',3,'0',NULL),(2,'Name',7,'',1,'0',NULL),(2,'Definition',8,'',2,'0',NULL),(2,'Type',11,'',3,'0',NULL),(3,'URL',12,'',1,'0',NULL),(3,'Comments',13,'',2,'0',NULL),(3,'Type',14,'',3,'0',NULL),(3,'Source',15,'',4,'0',NULL),(4,'Definition',21,'',1,'0',NULL),(4,'Name',20,'',0,'0',NULL),(5,'RelatedDataReference',23,'A reference between the data element and any related data.',0,'0',NULL),(5,'Type',24,'An expression that characterises the relationship between the data element and related data.',1,'0',NULL),(4,'RelatedDocum',25,'zxczxc',2,'1',NULL),(6,'Name',26,'name of the issue (eg. Air, Nature, ..)',0,'0',NULL),(7,'url',27,'Link to document',2,'0',NULL),(7,'Description',28,'description of the guideline',1,'0',NULL),(8,'contactPerson',55,'',3,'1',NULL),(8,'address',54,'',2,'1',NULL),(8,'name',53,'',1,'0',NULL),(8,'name-abbrevation',52,'',0,'0',NULL),(9,'author',34,'',0,'0',NULL),(9,'institution',35,'',1,'0',NULL),(9,'title',36,'',2,'0',NULL),(9,'publisher',37,'',3,'0',NULL),(9,'year',38,'',4,'0',NULL),(9,'url',39,'',5,'0',NULL),(11,'contactPerson',66,'',3,'0',NULL),(11,'address',65,'',6,'1',NULL),(11,'name',64,'',1,'0',NULL),(11,'name-abbrevation',63,'',2,'0',NULL),(12,'contactPerson',61,'',3,'1',NULL),(12,'address',60,'',2,'1',NULL),(12,'name',59,'',1,'0',NULL),(12,'name-abbrevation',58,'',0,'0',NULL),(14,'url',50,'',0,'0',NULL),(14,'name',51,'',1,'0',NULL),(8,'url',56,'',4,'0',NULL),(12,'url',62,'',4,'0',NULL),(11,'url',68,'',5,'0',NULL),(11,'PhoneNr',69,'',7,'1',NULL),(11,'e-mail',70,'',4,'0',NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `M_COMPLEX_ATTR_FIELD` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

