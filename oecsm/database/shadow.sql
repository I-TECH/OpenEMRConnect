/*
SQLyog Community v8.4 Beta2
MySQL - 5.5.12 : Database - shadow
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`shadow` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `shadow`;

/*Table structure for table `cell` */

DROP TABLE IF EXISTS `cell`;

CREATE TABLE `cell` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `primary_key_value` varchar(500) NOT NULL DEFAULT '',
  `data` varchar(60000) DEFAULT '',
  `column_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cell` (`column_id`),
  CONSTRAINT `FK_cell` FOREIGN KEY (`column_id`) REFERENCES `column` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `cell` */

/*Table structure for table `column` */

DROP TABLE IF EXISTS `column`;

CREATE TABLE `column` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `ordinal_position` int(11) NOT NULL,
  `data_type` varchar(100) NOT NULL,
  `size` int(11) NOT NULL,
  `replicable` tinyint(4) NOT NULL,
  `table_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_column` (`table_id`),
  CONSTRAINT `FK_column` FOREIGN KEY (`table_id`) REFERENCES `table` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `column` */

/*Table structure for table `database` */

DROP TABLE IF EXISTS `database`;

CREATE TABLE `database` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `NAME` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `database` */

/*Table structure for table `destination` */

DROP TABLE IF EXISTS `destination`;

CREATE TABLE `destination` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `last_received_transaction_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `destination` */

/*Table structure for table `table` */

DROP TABLE IF EXISTS `table`;

CREATE TABLE `table` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL DEFAULT '',
  `primary_keys` varchar(100) NOT NULL DEFAULT '',
  `database_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_table` (`database_id`),
  CONSTRAINT `FK_table` FOREIGN KEY (`database_id`) REFERENCES `database` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `table` */

/*Table structure for table `transaction` */

DROP TABLE IF EXISTS `transaction`;

CREATE TABLE `transaction` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` enum('INSERT','UPDATE','DELETE') NOT NULL,
  `table_id` int(11) NOT NULL,
  `created_datetime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_transaction` (`table_id`),
  CONSTRAINT `FK_transaction` FOREIGN KEY (`table_id`) REFERENCES `table` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `transaction` */

/*Table structure for table `transaction_data` */

DROP TABLE IF EXISTS `transaction_data`;

CREATE TABLE `transaction_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` varchar(60000) NOT NULL DEFAULT '',
  `column_id` int(11) NOT NULL,
  `transaction_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_transaction_data` (`transaction_id`),
  KEY `FK_transaction_data_1` (`column_id`),
  CONSTRAINT `FK_transaction_data` FOREIGN KEY (`transaction_id`) REFERENCES `transaction` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_transaction_data_1` FOREIGN KEY (`column_id`) REFERENCES `column` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `transaction_data` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
