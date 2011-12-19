CREATE DATABASE  IF NOT EXISTS `cds` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `cds`;
-- MySQL dump 10.13  Distrib 5.1.40, for Win32 (ia32)
--
-- Host: localhost    Database: cds
-- ------------------------------------------------------
-- Server version	5.1.45-community

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
-- Table structure for table `cds_store`
--

DROP TABLE IF EXISTS `cds_store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cds_store` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `destination` varchar(45) NOT NULL,
  `message` blob NOT NULL,
  `voided` tinyint(1) NOT NULL,
  `received_datetime` datetime NOT NULL,
  `voided_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cds_store`
--

LOCK TABLES `cds_store` WRITE;
/*!40000 ALTER TABLE `cds_store` DISABLE KEYS */;
INSERT INTO `cds_store` VALUES (1,'ke.go.moh.facility.14080.tb.reception','<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\r\n<PRPA_IN201302UV02 xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ITSVersion=\"XML_1.0\" xsi:schemaLocation=\"urn:hl7-org:v3 ../../schema/HL7V3/NE2008/multicacheschemas/PRPA_IN201311UV.xsd\">\r\n    <id extension=\"13160650464701\" root=\"1.3.6.1.4.1.150.2474.11.1.1\"/>\r\n    <creationTime value=\"201101010930\"/>\r\n    <interactionId extension=\"PRPA_IN201302UV02\" root=\"2.16.840.1.113883.1.6\"/>\r\n    <processingCode code=\"P\"/>\r\n    <processingModeCode code=\"T\"/>\r\n    <acceptAckCode code=\"NE\"/>\r\n    <receiver typeCode=\"RCV\">\r\n        <device classCode=\"DEV\" determinerCode=\"INSTANCE\">\r\n            <id extension=\"ke.go.moh.facility.14080.cds\" root=\"1.3.6.1.4.1.150.2474.11.1.2\"/>\r\n            <name>Clinical Document Store</name>\r\n        </device>\r\n    </receiver>\r\n    <sender>\r\n        <device classCode=\"DEV\" determinerCode=\"INSTANCE\">\r\n            <id extension=\"ke.go.moh.facility.14080.tb.reception\" root=\"1.3.6.1.4.1.150.2474.11.1.2\"/>\r\n            <name>Cds Test application </name>\r\n        </device>\r\n    </sender>\r\n    <controlActProcess classCode=\"CACT\" moodCode=\"EVN\">\r\n        <code code=\"PRPA_TE201302UV02\" codeSystem=\"2.16.840.1.113883.1.6\"/>\r\n        <subject typeCode=\"SUBJ\">\r\n            <registrationEvent classCode=\"REG\" moodCode=\"EVN\">\r\n                <id nullFlavor=\"NA\"/>\r\n                <statusCode code=\"active\"/>\r\n                <subject1 typeCode=\"SBJ\">\r\n                    <patient classCode=\"PAT\">\r\n                        <statusCode code=\"active\"/>\r\n                        <patientPerson>\r\n                            <name>\r\n                                <given>Harry</given>\r\n                                <given>Kimani</given>\r\n                                <family>Thuku</family>\r\n                            </name>\r\n                            <administrativeGenderCode code=\"M\"/>\r\n                            <deceasedTime value=\"20110601\"/>\r\n                            <id extension=\"no\" root=\"1.3.6.1.4.1.150.2474.11.1.4.3\"/>\r\n                            <id extension=\"12945-12345/2010\" root=\"1.3.6.1.4.1.150.2474.11.1.5.4\"/>\r\n                            <id extension=\"ke.go.moh.facility.14080.tb.reception\" root=\"1.3.6.1.4.1.150.2474.11.1.6.1\"/>\r\n                            <id extension=\"20110915\" root=\"1.3.6.1.4.1.150.2474.11.1.6.2\"/>\r\n                        </patientPerson>\r\n                    </patient>\r\n                </subject1>\r\n            </registrationEvent>\r\n        </subject>\r\n    </controlActProcess>\r\n</PRPA_IN201302UV02>\r\n',0,'2011-09-15 08:37:26',NULL);
/*!40000 ALTER TABLE `cds_store` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-09-17 15:48:04
