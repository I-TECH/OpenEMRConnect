-- MySQL dump 10.13  Distrib 5.1.45, for Win32 (ia32)
--
-- Host: localhost    Database: lpi
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
/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`mpi` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `mpi`;
--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `address` (
  `address_id` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(50) DEFAULT NULL,
  `facility_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`address_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `facility`
--

DROP TABLE IF EXISTS `facility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `facility` (
  `facility_code` int(5) NOT NULL,
  `facility_name` varchar(100) DEFAULT NULL,
  `province` varchar(35) DEFAULT NULL,
  `district` varchar(35) DEFAULT NULL,
  `location` varchar(35) DEFAULT NULL,
  `sublocation` varchar(35) DEFAULT NULL,
  PRIMARY KEY (`facility_code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fingerprint`
--

DROP TABLE IF EXISTS `fingerprint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fingerprint` (
  `fingerprint_id` int(11) NOT NULL AUTO_INCREMENT,
  `fingerprint_uid` varchar(255) DEFAULT NULL,
  `person_id` varchar(25) NOT NULL COMMENT 'foreign key to person_id in person table',
  `fingerprint_type_id` int(11) DEFAULT NULL,
  `fingerprint_template` varbinary(5000) DEFAULT NULL,
  `fingerprint_technology_type_id` int(11) DEFAULT NULL,
  `date_created` datetime DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  PRIMARY KEY (`fingerprint_id`),
  KEY `FK_fingerprint` (`person_id`),
  KEY `FK_fingerprint_type` (`fingerprint_type_id`),
  KEY `FK_fingerprint_technology_type` (`fingerprint_technology_type_id`),
  CONSTRAINT `FK_fingerprint_fingerprint_type_id` FOREIGN KEY (`fingerprint_type_id`) REFERENCES `fingerprint_type` (`fingerprint_type_id`),
  CONSTRAINT `FK_fingerprint_technology_type_id` FOREIGN KEY (`fingerprint_technology_type_id`) REFERENCES `fingerprint_technology_type` (`fingerprint_technology_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fingerprint_technology_type`
--

DROP TABLE IF EXISTS `fingerprint_technology_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fingerprint_technology_type` (
  `fingerprint_technology_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `sdk_type` varchar(50) DEFAULT NULL,
  `sdk_version` varchar(50) DEFAULT NULL,
  `fingerprint_reader_brand_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`fingerprint_technology_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fingerprint_type`
--

DROP TABLE IF EXISTS `fingerprint_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fingerprint_type` (
  `fingerprint_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `hand_name` varchar(15) DEFAULT NULL,
  `finger_name` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`fingerprint_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `identifier_type`
--

DROP TABLE IF EXISTS `identifier_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `identifier_type` (
  `identifier_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `identifier_type_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`identifier_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `marital_status_type`
--

DROP TABLE IF EXISTS `marital_status_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `marital_status_type` (
  `marital_status_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `marital_status_name` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`marital_status_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person` (
  `person_id` int(11) NOT NULL AUTO_INCREMENT,
  `person_guid` varchar(38) DEFAULT NULL,
  `first_name` varchar(25) DEFAULT NULL,
  `middle_name` varchar(25) DEFAULT NULL,
  `last_name` varchar(25) DEFAULT NULL,
  `clan_name` varchar(25) DEFAULT NULL,
  `other_name` varchar(100) DEFAULT NULL,
  `sex` varchar(1) DEFAULT NULL,
  `birthdate` datetime DEFAULT NULL,
  `deathdate` datetime DEFAULT NULL,
  `mothers_first_name` varchar(15) DEFAULT NULL,
  `mothers_middle_name` varchar(15) DEFAULT NULL,
  `mothers_last_name` varchar(15) DEFAULT NULL,
  `fathers_first_name` varchar(15) DEFAULT NULL,
  `fathers_middle_name` varchar(15) DEFAULT NULL,
  `fathers_last_name` varchar(15) DEFAULT NULL,
  `compoundhead_first_name` varchar(15) DEFAULT NULL,
  `compoundhead_middle_name` varchar(15) DEFAULT NULL,
  `compoundhead_last_name` varchar(15) DEFAULT NULL,
  `village_id` int(11) DEFAULT NULL,
  `marital_status` int(11) DEFAULT NULL,
  `consent_signed` tinyint(1) DEFAULT NULL,
  `date_created` datetime DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  PRIMARY KEY (`person_id`),
  KEY `village_id` (`village_id`),
  KEY `marital_status` (`marital_status`),
  CONSTRAINT `FK_person` FOREIGN KEY (`village_id`) REFERENCES `village` (`village_id`),
  CONSTRAINT `FK_person_marital_type` FOREIGN KEY (`marital_status`) REFERENCES `marital_status_type` (`marital_status_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_identifier`
--

DROP TABLE IF EXISTS `person_identifier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person_identifier` (
  `person_identifier_id` int(11) NOT NULL AUTO_INCREMENT,
  `person_id` int(11) NOT NULL,
  `identifier_type_id` int(11) NOT NULL,
  `identifier` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`person_identifier_id`),
  KEY `FK_person_identifier` (`person_id`),
  KEY `FK_person_identifiertype_id` (`identifier_type_id`),
  KEY `identifier` (`identifier`),
  CONSTRAINT `FK_person_identifiertype_id` FOREIGN KEY (`identifier_type_id`) REFERENCES `identifier_type` (`identifier_type_id`),
  CONSTRAINT `FK_person_identifier_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `search_history`
--

DROP TABLE IF EXISTS `search_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `search_history` (
  `search_history_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `address_id` int(11) NOT NULL COMMENT 'FK to address_id in address table',
  `message_id` varchar(50) NOT NULL,
  `linked_search_id` int(11) DEFAULT NULL COMMENT 'FK to search_history_id in search history table',
  `search_datetime` datetime DEFAULT NULL,
  `s_first_name` varchar(15) DEFAULT NULL COMMENT 'searched first name',
  `s_middle_name` varchar(15) DEFAULT NULL COMMENT 'searched middle name',
  `s_last_name` varchar(15) DEFAULT NULL COMMENT 'searched last name',
  `s_birthdate` datetime DEFAULT NULL COMMENT 'search birthdate',
  `s_sex` char(1) DEFAULT NULL COMMENT 'searched sex',
  `s_clan_name` varchar(15) DEFAULT NULL COMMENT 'searched clan name',
  `s_village_name` varchar(50) DEFAULT NULL COMMENT 'searched village name',
  `s_site_name` varchar(50) DEFAULT NULL,
  `s_guid` varchar(38) DEFAULT NULL,
  `outcome` int(1) DEFAULT NULL COMMENT '0= No match found,1=match found,2=Linked search,null=no results returned',
  `m_datetime` datetime DEFAULT NULL,
  `rank` int(11) DEFAULT NULL,
  `m_person_id` int(11) DEFAULT NULL,
  `m_first_name` varchar(15) DEFAULT NULL COMMENT 'matched first_name',
  `m_middle_name` varchar(15) DEFAULT NULL COMMENT 'matched middle name',
  `m_last_name` varchar(15) DEFAULT NULL COMMENT 'matched last name',
  `m_birthdate` datetime DEFAULT NULL COMMENT 'matched birthdate',
  `m_sex` char(1) DEFAULT NULL COMMENT 'matched sex',
  `m_clan_name` varchar(15) DEFAULT NULL COMMENT 'matched clan name',
  `m_village_name` varchar(50) DEFAULT NULL COMMENT 'matched village name',
  `date_created` datetime DEFAULT NULL,
  PRIMARY KEY (`search_history_id`),
  KEY `FK_search_history_linked_search` (`linked_search_id`),
  KEY `FK_search_history_address` (`address_id`),
  KEY `FK_search_history_person` (`m_person_id`),
  CONSTRAINT `FK_search_history` FOREIGN KEY (`m_person_id`) REFERENCES `person` (`person_id`),
  CONSTRAINT `FK_search_history_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`address_id`),
  CONSTRAINT `FK_search_history_linked_search` FOREIGN KEY (`linked_search_id`) REFERENCES `search_history` (`search_history_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `search_history_fingerprint`
--

DROP TABLE IF EXISTS `search_history_fingerprint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `search_history_fingerprint` (
  `search_history_fingerprint_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'internal key ',
  `search_history_id` int(11) NOT NULL COMMENT 'foreign key to search history ',
  `fingerprint_type_id` int(11) DEFAULT NULL,
  `fingerprint_template` varbinary(5000) DEFAULT NULL,
  `fingerprint_technology_type_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`search_history_fingerprint_id`),
  KEY `FK_search_history_fingerprint_id` (`search_history_id`),
  CONSTRAINT `FK_search_history_fingerprint_id` FOREIGN KEY (`search_history_id`) REFERENCES `search_history` (`search_history_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `search_history_person_identifier`
--

DROP TABLE IF EXISTS `search_history_person_identifier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `search_history_person_identifier` (
  `search_history_person_identifier_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'internal key ',
  `search_history_id` int(11) NOT NULL COMMENT 'foreign key to search history ',
  `identifier_type_id` int(11) DEFAULT NULL,
  `identifier` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`search_history_person_identifier_id`),
  KEY `FK_search_history_person_identifier_id` (`search_history_id`),
  KEY `FK_search_history_identifier_type_id` (`identifier_type_id`),
  CONSTRAINT `FK_search_history_identifier_type_id` FOREIGN KEY (`identifier_type_id`) REFERENCES `identifier_type` (`identifier_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `village`
--

DROP TABLE IF EXISTS `village`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `village` (
  `village_id` int(11) NOT NULL AUTO_INCREMENT,
  `village_name` varchar(25) DEFAULT NULL,
  `editable` double DEFAULT NULL,
  PRIMARY KEY (`village_id`),
  KEY `village_id` (`village_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visit`
--

DROP TABLE IF EXISTS `visit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visit` (
  `visit_id` int(11) NOT NULL AUTO_INCREMENT,
  `visit_type_id` int(11) NOT NULL,
  `person_id` int(11) NOT NULL,
  `address_id` int(11) NOT NULL,
  `visit_date` datetime NOT NULL,
  `date_created` datetime NOT NULL,
  PRIMARY KEY (`visit_id`),
  KEY `FK_visit` (`person_id`),
  KEY `FK_visit_type` (`visit_type_id`),
  KEY `FK_visit_address_id` (`address_id`),
  CONSTRAINT `FK_visit_address_id` FOREIGN KEY (`address_id`) REFERENCES `address` (`address_id`),
  CONSTRAINT `FK_visit_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`),
  CONSTRAINT `FK_visit_type` FOREIGN KEY (`visit_type_id`) REFERENCES `visit_type` (`visit_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visit_type`
--

DROP TABLE IF EXISTS `visit_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visit_type` (
  `visit_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `visit_type_name` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`visit_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-01-18  8:12:23
