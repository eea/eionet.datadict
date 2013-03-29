-- MySQL dump 10.13  Distrib 5.5.30, for Linux (x86_64)
--
-- Host: localhost    Database: DataDictTest
-- ------------------------------------------------------
-- Server version	5.5.9

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
-- Table structure for table `DATABASECHANGELOGLOCK`
--

DROP TABLE IF EXISTS `DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int(11) NOT NULL,
  `LOCKED` tinyint(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOGLOCK`
--

LOCK TABLES `DATABASECHANGELOGLOCK` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOGLOCK` VALUES (1,0,NULL,NULL);
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATABASECHANGELOG`
--

DROP TABLE IF EXISTS `DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(63) NOT NULL,
  `AUTHOR` varchar(63) NOT NULL,
  `FILENAME` varchar(200) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`,`AUTHOR`,`FILENAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOG`
--

LOCK TABLES `DATABASECHANGELOG` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOG` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOG` VALUES ('rev-1','roug','sql/dd-struct.xml','2013-03-29 09:50:50',1,'EXECUTED','3:03e27830639c62e74d817de8137929ce','Create Table','',NULL,'2.0.5'),('rev-10','roug','sql/dd-struct.xml','2013-03-29 09:50:51',10,'EXECUTED','3:0c6c134821e7e819da35c69702581777','Create Table','',NULL,'2.0.5'),('rev-100','roug','sql/dd-struct.xml','2013-03-29 09:50:53',80,'EXECUTED','3:e5e967e7c7c393ca3c98c77ef3830565','Set Column as Auto-Increment (x18)','Auto increments can\'t be added in the create table change set.',NULL,'2.0.5'),('rev-11','roug','sql/dd-struct.xml','2013-03-29 09:50:51',11,'EXECUTED','3:b4d67f002865dcaae2c13a541dc01648','Create Table','',NULL,'2.0.5'),('rev-12','roug','sql/dd-struct.xml','2013-03-29 09:50:51',12,'EXECUTED','3:016b65ac15ae9f182118858f40e015a6','Create Table','',NULL,'2.0.5'),('rev-13','roug','sql/dd-struct.xml','2013-03-29 09:50:51',13,'EXECUTED','3:95017ace3be70d030bd9de6ed7e63034','Create Table','',NULL,'2.0.5'),('rev-14','roug','sql/dd-struct.xml','2013-03-29 09:50:51',14,'EXECUTED','3:85e2c1566c5fb3e93c3aa79aff567a26','Create Table','',NULL,'2.0.5'),('rev-15','roug','sql/dd-struct.xml','2013-03-29 09:50:51',15,'EXECUTED','3:0d428027ba99a7e283cbf05210c4286d','Create Table','',NULL,'2.0.5'),('rev-16','roug','sql/dd-struct.xml','2013-03-29 09:50:51',16,'EXECUTED','3:79d6095cdba7fec1b988094c654c926a','Create Table','',NULL,'2.0.5'),('rev-17','roug','sql/dd-struct.xml','2013-03-29 09:50:51',17,'EXECUTED','3:72e7ccd9d24142809b967832c6b9cd6b','Create Table','',NULL,'2.0.5'),('rev-18','roug','sql/dd-struct.xml','2013-03-29 09:50:51',18,'EXECUTED','3:c05b3851ee97c90b2eaac413e6eef672','Create Table','',NULL,'2.0.5'),('rev-19','roug','sql/dd-struct.xml','2013-03-29 09:50:51',19,'EXECUTED','3:5289ec0eb7d98740950bee8d90025b08','Create Table','',NULL,'2.0.5'),('rev-2','roug','sql/dd-struct.xml','2013-03-29 09:50:50',2,'EXECUTED','3:31f776bd1eefbcfac14c43a743a38714','Create Table','',NULL,'2.0.5'),('rev-20','roug','sql/dd-struct.xml','2013-03-29 09:50:51',20,'EXECUTED','3:755c579b422d4d64dc35a846bb0cda3b','Create Table','',NULL,'2.0.5'),('rev-21','roug','sql/dd-struct.xml','2013-03-29 09:50:51',21,'EXECUTED','3:b018a11c07975c335b58a9a73020be79','Create Table','',NULL,'2.0.5'),('rev-22','roug','sql/dd-struct.xml','2013-03-29 09:50:51',22,'EXECUTED','3:c5f0591d1bfa66257955ad1e32561d9e','Create Table','',NULL,'2.0.5'),('rev-23','roug','sql/dd-struct.xml','2013-03-29 09:50:51',23,'EXECUTED','3:fbba73747437fad173c1e902227d151b','Create Table','',NULL,'2.0.5'),('rev-24','roug','sql/dd-struct.xml','2013-03-29 09:50:51',24,'EXECUTED','3:e6e1217a6efc1d4d3e2fb3d4d4bdc074','Create Table','',NULL,'2.0.5'),('rev-25','roug','sql/dd-struct.xml','2013-03-29 09:50:51',25,'EXECUTED','3:48ae1cdce3799189eb8279949bc3b9d5','Create Table','',NULL,'2.0.5'),('rev-26','roug','sql/dd-struct.xml','2013-03-29 09:50:51',26,'EXECUTED','3:ba24eee7032dc4bdd4589fa459aea55d','Create Table','',NULL,'2.0.5'),('rev-27','roug','sql/dd-struct.xml','2013-03-29 09:50:51',27,'EXECUTED','3:233d13430a2418865f67be1e47f451bd','Create Table','',NULL,'2.0.5'),('rev-28','roug','sql/dd-struct.xml','2013-03-29 09:50:51',28,'EXECUTED','3:1febff328dfa3dbacf0756dd361edda7','Create Table','',NULL,'2.0.5'),('rev-29','roug','sql/dd-struct.xml','2013-03-29 09:50:51',29,'EXECUTED','3:dcd9c2a845f36deef4d6436a22a6d17c','Create Table','',NULL,'2.0.5'),('rev-3','roug','sql/dd-struct.xml','2013-03-29 09:50:50',3,'EXECUTED','3:07e90374b33f0bce1f580fdca2139b7a','Create Table','',NULL,'2.0.5'),('rev-30','roug','sql/dd-struct.xml','2013-03-29 09:50:51',30,'EXECUTED','3:cb178244f0d3e6638cfef342a05e4b8e','Create Table','',NULL,'2.0.5'),('rev-31','roug','sql/dd-struct.xml','2013-03-29 09:50:51',31,'EXECUTED','3:feacec27b3ee9b31b529e37274fc635b','Create Table','',NULL,'2.0.5'),('rev-32','roug','sql/dd-struct.xml','2013-03-29 09:50:51',32,'EXECUTED','3:7b371b8f54e41341ccab47ef826b4000','Create Table','',NULL,'2.0.5'),('rev-33','roug','sql/dd-struct.xml','2013-03-29 09:50:51',33,'EXECUTED','3:15568afadb64be51e6c3f95c9764f684','Create Table','',NULL,'2.0.5'),('rev-35','roug','sql/dd-struct.xml','2013-03-29 09:50:51',34,'EXECUTED','3:eb4536fcc08a5310d6e977f64179f0ec','Add Primary Key','',NULL,'2.0.5'),('rev-36','roug','sql/dd-struct.xml','2013-03-29 09:50:51',35,'EXECUTED','3:645a07db88f6a29ff14f068536cbecc3','Add Primary Key','',NULL,'2.0.5'),('rev-37','roug','sql/dd-struct.xml','2013-03-29 09:50:51',36,'EXECUTED','3:14508fb59410620546d3fa48b30fa6e4','Add Primary Key','',NULL,'2.0.5'),('rev-38','roug','sql/dd-struct.xml','2013-03-29 09:50:51',37,'EXECUTED','3:8342f6286b9befc3489998ca5b8deca2','Custom SQL','',NULL,'2.0.5'),('rev-39','roug','sql/dd-struct.xml','2013-03-29 09:50:51',38,'EXECUTED','3:a84ceca67b6d69ba78a704da5ec1f8ad','Add Primary Key','',NULL,'2.0.5'),('rev-4','roug','sql/dd-struct.xml','2013-03-29 09:50:51',4,'EXECUTED','3:291b961918eb51a36079a6eafa081292','Create Table','',NULL,'2.0.5'),('rev-41','roug','sql/dd-struct.xml','2013-03-29 09:50:51',39,'EXECUTED','3:ac8cc30be1f594c093638877b7bf1854','Add Foreign Key Constraint','',NULL,'2.0.5'),('rev-42','roug','sql/dd-struct.xml','2013-03-29 09:50:51',40,'EXECUTED','3:18fcf63ef62265bc876f9ee184af90d4','Add Foreign Key Constraint','',NULL,'2.0.5'),('rev-43','roug','sql/dd-struct.xml','2013-03-29 09:50:51',41,'EXECUTED','3:6d531d4260e241531c9a3816883de300','Add Foreign Key Constraint','',NULL,'2.0.5'),('rev-44','roug','sql/dd-struct.xml','2013-03-29 09:50:51',42,'EXECUTED','3:d66c15aaec9e11f17dcb5dbf5448e4ad','Add Foreign Key Constraint','',NULL,'2.0.5'),('rev-45','roug','sql/dd-struct.xml','2013-03-29 09:50:51',43,'EXECUTED','3:e5f30f21e22a0c1736741029be922806','Add Foreign Key Constraint','',NULL,'2.0.5'),('rev-46','roug','sql/dd-struct.xml','2013-03-29 09:50:51',44,'EXECUTED','3:69c33c3f9b44f6893ba3a31a2c23e8ef','Create Index','',NULL,'2.0.5'),('rev-47','roug','sql/dd-struct.xml','2013-03-29 09:50:51',45,'EXECUTED','3:6a99911583be1117ad0cd8fad8f9daff','Custom SQL','',NULL,'2.0.5'),('rev-48','roug','sql/dd-struct.xml','2013-03-29 09:50:51',46,'EXECUTED','3:670d4c934ab8336fdb5403b01b6b78db','Create Index','',NULL,'2.0.5'),('rev-49','roug','sql/dd-struct.xml','2013-03-29 09:50:51',47,'EXECUTED','3:2f2bf0dea66cbc489ae1b65d14f32dfe','Create Index','',NULL,'2.0.5'),('rev-5','roug','sql/dd-struct.xml','2013-03-29 09:50:51',5,'EXECUTED','3:704081a0fb893f25f2c75547a0ba1f62','Create Table','',NULL,'2.0.5'),('rev-50','roug','sql/dd-struct.xml','2013-03-29 09:50:51',48,'EXECUTED','3:0f15584489746e1bcca6385c92cf7e2c','Create Index','',NULL,'2.0.5'),('rev-51','roug','sql/dd-struct.xml','2013-03-29 09:50:51',49,'EXECUTED','3:4b9700005dd8c8e8ee57e472ad7e2ce3','Create Index','',NULL,'2.0.5'),('rev-52','roug','sql/dd-struct.xml','2013-03-29 09:50:52',50,'EXECUTED','3:c512ad56fb34037f3e837de375673dcf','Create Index','',NULL,'2.0.5'),('rev-53','roug','sql/dd-struct.xml','2013-03-29 09:50:52',51,'EXECUTED','3:589ce4234779c03111e983d36f086c82','Create Index','',NULL,'2.0.5'),('rev-54','roug','sql/dd-struct.xml','2013-03-29 09:50:52',52,'EXECUTED','3:a178d112ee540a031c906f308a2f5633','Create Index (x4)','',NULL,'2.0.5'),('rev-58','roug','sql/dd-struct.xml','2013-03-29 09:50:52',53,'EXECUTED','3:581363b5b818ffcd38a67d1e5b58cbda','Create Index (x3)','',NULL,'2.0.5'),('rev-6','roug','sql/dd-struct.xml','2013-03-29 09:50:51',6,'EXECUTED','3:5a596db2341c17ed850ee622528a49be','Create Table','',NULL,'2.0.5'),('rev-61','roug','sql/dd-struct.xml','2013-03-29 09:50:52',54,'EXECUTED','3:e6c4890c5ecc29c9371b010767b22035','Create Index','',NULL,'2.0.5'),('rev-62','roug','sql/dd-struct.xml','2013-03-29 09:50:52',55,'EXECUTED','3:7aa7a16fbf85945eff32b9ed3ac37793','Create Index','',NULL,'2.0.5'),('rev-63','roug','sql/dd-struct.xml','2013-03-29 09:50:52',56,'EXECUTED','3:5fe0904648b7e76cebad106652a320eb','Create Index','',NULL,'2.0.5'),('rev-64','roug','sql/dd-struct.xml','2013-03-29 09:50:52',57,'EXECUTED','3:3b0a9adca7a60a428172d2dab2163599','Create Index','',NULL,'2.0.5'),('rev-65','roug','sql/dd-struct.xml','2013-03-29 09:50:52',58,'EXECUTED','3:1423c83bd03b9919fd946b67ac317d39','Create Index','',NULL,'2.0.5'),('rev-66','roug','sql/dd-struct.xml','2013-03-29 09:50:52',59,'EXECUTED','3:a0e07e274c97e7f44faa05f39751064a','Create Index','',NULL,'2.0.5'),('rev-67','roug','sql/dd-struct.xml','2013-03-29 09:50:52',60,'EXECUTED','3:dfe4fbedebee6739cdd55d450bb6c4aa','Create Index','',NULL,'2.0.5'),('rev-68','roug','sql/dd-struct.xml','2013-03-29 09:50:52',61,'EXECUTED','3:3d75ef46b9218fc4da7d1cccb9d9797b','Create Index','',NULL,'2.0.5'),('rev-69','roug','sql/dd-struct.xml','2013-03-29 09:50:52',62,'EXECUTED','3:35a9be6553533b2393f84c6aeb7ab201','Create Index','',NULL,'2.0.5'),('rev-7','roug','sql/dd-struct.xml','2013-03-29 09:50:51',7,'EXECUTED','3:b75b2a145b3fae24adae7bbe88a9c0ec','Create Table','',NULL,'2.0.5'),('rev-70','roug','sql/dd-struct.xml','2013-03-29 09:50:52',63,'EXECUTED','3:f7412662ee6ace1e24e5ab0f1b6fcc61','Create Index','',NULL,'2.0.5'),('rev-71','roug','sql/dd-struct.xml','2013-03-29 09:50:52',64,'EXECUTED','3:e9ad86dc5b92bf9c69a80bdc1402244f','Create Index','',NULL,'2.0.5'),('rev-72','roug','sql/dd-struct.xml','2013-03-29 09:50:52',65,'EXECUTED','3:f8eaad2b8873199e051a38b90026c1d1','Create Index','',NULL,'2.0.5'),('rev-73','roug','sql/dd-struct.xml','2013-03-29 09:50:52',66,'EXECUTED','3:95e9ee17ed3640ac71bd16da807d1b3f','Create Index (x4)','',NULL,'2.0.5'),('rev-77','roug','sql/dd-struct.xml','2013-03-29 09:50:52',67,'EXECUTED','3:7372f7838cebb7e2d5b0acae679d5dd9','Create Index (x2)','',NULL,'2.0.5'),('rev-79','roug','sql/dd-struct.xml','2013-03-29 09:50:52',68,'EXECUTED','3:32753207975cbb91626bd23186e9498a','Custom SQL','',NULL,'2.0.5'),('rev-8','roug','sql/dd-struct.xml','2013-03-29 09:50:51',8,'EXECUTED','3:cc11be673159e640ba055d3b4118bd32','Create Table','',NULL,'2.0.5'),('rev-80','roug','sql/dd-struct.xml','2013-03-29 09:50:52',69,'EXECUTED','3:0d14256d1f85f9887099c8841a0a3de6','Custom SQL','',NULL,'2.0.5'),('rev-81','roug','sql/dd-struct.xml','2013-03-29 09:50:52',70,'EXECUTED','3:43c8192371bf736373bcede35541820c','Create Index','',NULL,'2.0.5'),('rev-82','roug','sql/dd-struct.xml','2013-03-29 09:50:52',71,'EXECUTED','3:e42515deafcbafe3bb507ff5256da933','Create Index (x3)','',NULL,'2.0.5'),('rev-85','roug','sql/dd-struct.xml','2013-03-29 09:50:52',72,'EXECUTED','3:237e12ef0bd299334e8b485de06a7cff','Create Index (x2)','',NULL,'2.0.5'),('rev-87','roug','sql/dd-struct.xml','2013-03-29 09:50:52',73,'EXECUTED','3:f4302a1f1393f8626b3861a9778d6c91','Create Index (x3)','',NULL,'2.0.5'),('rev-9','roug','sql/dd-struct.xml','2013-03-29 09:50:51',9,'EXECUTED','3:a1e34f0abd7e5d44989538503091cfd7','Create Table','',NULL,'2.0.5'),('rev-91','roug','sql/dd-struct.xml','2013-03-29 09:50:52',74,'EXECUTED','3:2e2f5377f6d0e081cedc12818bdaa8b2','Create Index (x2)','',NULL,'2.0.5'),('rev-92','roug','sql/dd-struct.xml','2013-03-29 09:50:52',75,'EXECUTED','3:3a6ee23be06b32a3b5cae15f9255d629','Add Foreign Key Constraint','',NULL,'2.0.5'),('rev-93','roug','sql/dd-struct.xml','2013-03-29 09:50:52',76,'EXECUTED','3:801154f7037f0a02fb2b697c72fac578','Create Index (x2)','',NULL,'2.0.5'),('rev-95','roug','sql/dd-struct.xml','2013-03-29 09:50:52',77,'EXECUTED','3:86d98dedf634fc30ba0348da36461a35','Create Index','',NULL,'2.0.5'),('rev-96','roug','sql/dd-struct.xml','2013-03-29 09:50:52',78,'EXECUTED','3:7ddfa20e6e6b46b4cdb24218aa835cf4','Create Index (x3)','',NULL,'2.0.5'),('rev-99','roug','sql/dd-struct.xml','2013-03-29 09:50:52',79,'EXECUTED','3:c98883f63deeeef0ecd45fb2d1e15697','Custom SQL','',NULL,'2.0.5');
/*!40000 ALTER TABLE `DATABASECHANGELOG` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-03-29  9:52:41
