/*
SQLyog Community v8.4 Beta2
MySQL - 5.1.45-community : Database - mcds
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`mcds` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `mcds`;

/*Table structure for table `cda` */

DROP TABLE IF EXISTS `cda`;

CREATE TABLE `cda` (
  `cda_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'internal key',
  `patient_clinical_id` varchar(100) NOT NULL COMMENT 'Clinic ID assigned to a patient in a given facility',
  `hdss_id` varchar(100) DEFAULT NULL COMMENT 'Hdss',
  `facility_code` varchar(11) DEFAULT NULL COMMENT 'facility code from mfl',
  `facility_name` varchar(100) DEFAULT NULL COMMENT 'name of the facility',
  `cda` longtext COMMENT 'summarized patient information',
  `first_name` varchar(64) DEFAULT NULL COMMENT 'patient''s first name',
  `last_name` varchar(64) DEFAULT NULL COMMENT 'patient''s last name',
  `cda_dob` varchar(8) COMMENT 'date of birth as defined in the cda',
  `moh_dob` date COMMENT 'date of birth following MOH guidance',
  `gender` ENUM ('M', 'F', 'U', 'N') DEFAULT 'N' COMMENT '''M'' (male), ''F'' (female), ''U'' (undifferentiated) and ''N'' (nullFlavor/unknown)',
  `source_type` varchar(25) DEFAULT NULL COMMENT 'source of the document: EMR,LIS, Pharm etc',
  `source_system` varchar(64) DEFAULT NULL,
  `date_stored` datetime DEFAULT NULL COMMENT 'datetime when cda is stored in cds',
  `date_generated` datetime DEFAULT NULL COMMENT 'datetime when cda was generated from source',
  PRIMARY KEY (`cda_id`),
  KEY `patient_clinical_id` (`patient_clinical_id`),
  KEY `hdss_id` (`hdss_id`),
  UNIQUE `unique_cda` (`patient_clinical_id`, `hdss_id`, `facility_code`, `source_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `cda` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
