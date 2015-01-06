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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;

/*Data for the table `cell` */

insert  into `cell`(`id`,`primary_key_value`,`data`,`column_id`) values (1,'1-1-1-1','1-1-1-1',1),(2,'1-1-1-1','James',2),(3,'1-1-1-1','Oduor',3),(4,'1-1-1-1','Otieno',4),(5,'1-1-1-1','Jalolwe',5),(6,'1-1-1-1','Jimmy Jammer',6),(7,'1-1-1-1','M',7),(8,'1-1-1-1','1983-06-15 00:00:00.0',8),(9,'1-1-1-1','2000-01-01 00:00:00.0',9),(10,'1-1-1-1','12345',10),(11,'1-1-1-1','67890',11),(12,'1-1-1-1','Jane',12),(13,'1-1-1-1','Auma',13),(14,'1-1-1-1','Ber',14),(15,'1-1-1-1','Kenneth Matsumoto',15),(16,'1-1-1-1','Osiemo',16),(17,'1-1-1-1',NULL,17),(18,'1-1-1-1',NULL,18),(19,'1-1-1-1',NULL,19),(20,'1-1-1-1',NULL,20),(21,'1-1-1-1',NULL,21),(22,'1-1-1-1',NULL,22),(23,'1-1-1-1',NULL,23),(24,'1-1-1-1',NULL,24),(25,'1-1-1-1','EXT',25),(26,'1-1-1-1','2013-02-11 00:00:00.0',26),(27,'1-1-1-1','Kondele',27),(28,'1-1-1-1','2013-06-08 00:00:00.0',28),(29,'1-1-1-1',NULL,29),(30,'1-1-1-1',NULL,30),(31,'1-1-1-1',NULL,31);

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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;

/*Data for the table `column` */

insert  into `column`(`id`,`name`,`ordinal_position`,`data_type`,`size`,`replicable`,`table_id`) values (1,'individid',1,'nvarchar',50,1,1),(2,'fname',2,'nvarchar',50,1,1),(3,'jname',3,'nvarchar',50,1,1),(4,'lname',4,'nvarchar',50,1,1),(5,'famcla',5,'nvarchar',50,1,1),(6,'akaname',6,'nvarchar',50,1,1),(7,'gender',7,'nvarchar',50,1,1),(8,'dob',8,'datetime',23,1,1),(9,'arrdate',9,'datetime',23,1,1),(10,'motherid',10,'nvarchar',50,1,1),(11,'fatherid',11,'nvarchar',50,1,1),(12,'mfname',12,'nvarchar',50,1,1),(13,'mjname',13,'nvarchar',50,1,1),(14,'mlname',14,'nvarchar',50,1,1),(15,'ffname',15,'nvarchar',50,1,1),(16,'fjname',16,'nvarchar',50,1,1),(17,'flname',17,'nvarchar',50,1,1),(18,'mtal',18,'nvarchar',50,1,1),(19,'mtyp',19,'nvarchar',50,1,1),(20,'chheadid',20,'nvarchar',50,1,1),(21,'cfname',21,'nvarchar',50,1,1),(22,'cjname',22,'nvarchar',50,1,1),(23,'clname',23,'nvarchar',50,1,1),(24,'cfcname',24,'nvarchar',50,1,1),(25,'lastevent',25,'nvarchar',50,1,1),(26,'lasteventdate',26,'datetime',23,1,1),(27,'villname',27,'nvarchar',50,1,1),(28,'expectedDeliveryDate',28,'datetime',23,1,1),(29,'ruralUrban',29,'nvarchar',50,1,1),(30,'pregnancyOutcome',30,'nvarchar',50,1,1),(31,'pregnancyEndDate',31,'datetime',23,1,1);

/*Table structure for table `database` */

DROP TABLE IF EXISTS `database`;

CREATE TABLE `database` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `NAME` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

/*Data for the table `database` */

insert  into `database`(`id`,`name`) values (1,'dummy_hdss');

/*Table structure for table `destination` */

DROP TABLE IF EXISTS `destination`;

CREATE TABLE `destination` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `last_received_transaction_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

/*Data for the table `destination` */

insert  into `destination`(`id`,`name`,`last_received_transaction_id`) values (1,'HDSS COMPANION',13);

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

/*Data for the table `table` */

insert  into `table`(`id`,`name`,`primary_keys`,`database_id`) values (1,'oec_view','individid',1);

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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;

/*Data for the table `transaction` */

insert  into `transaction`(`id`,`type`,`table_id`,`created_datetime`) values (1,'INSERT',1,'2013-02-11 11:24:41'),(2,'UPDATE',1,'2013-02-11 11:26:32'),(3,'UPDATE',1,'2013-02-11 12:28:07'),(4,'UPDATE',1,'2013-02-11 12:32:48'),(5,'UPDATE',1,'2013-02-11 12:34:55'),(6,'UPDATE',1,'2013-02-11 12:38:17'),(7,'UPDATE',1,'2013-02-11 12:39:59'),(8,'UPDATE',1,'2013-02-11 16:14:45'),(9,'UPDATE',1,'2013-02-12 10:41:50'),(10,'UPDATE',1,'2013-02-12 10:54:47'),(11,'UPDATE',1,'2013-02-12 10:57:43'),(12,'UPDATE',1,'2013-02-12 11:00:14'),(13,'UPDATE',1,'2013-02-12 11:00:39');

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
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=latin1;

/*Data for the table `transaction_data` */

insert  into `transaction_data`(`id`,`data`,`column_id`,`transaction_id`) values (1,'1-1-1-1',1,1),(2,'James',2,1),(3,'Oduor',3,1),(4,'Otieno',4,1),(5,'Kaleido',5,1),(6,'Jimmy Jammer',6,1),(7,'M',7,1),(8,'1983-06-15 00:00:00.0',8,1),(9,'2000-01-01 00:00:00.0',9,1),(10,'12345',10,1),(11,'67890',11,1),(12,'Alice',12,1),(13,'Auma',13,1),(14,'Ber',14,1),(15,'Kenneth Ber',15,1),(16,'Osiemo',16,1),(17,'',17,1),(18,'',18,1),(19,'',19,1),(20,'',20,1),(21,'',21,1),(22,'',22,1),(23,'',23,1),(24,'',24,1),(25,'',25,1),(26,'',26,1),(27,'',27,1),(28,'',28,1),(29,'',29,1),(30,'',30,1),(31,'',31,1),(32,'Some clan name',5,2),(33,'1-1-1-1',1,2),(34,'Kondele',27,3),(35,'1-1-1-1',1,3),(36,'Lolwe',27,4),(37,'1-1-1-1',1,4),(38,'Manyatta',27,5),(39,'1-1-1-1',1,5),(40,'2013-02-11 00:00:00.0',26,6),(41,'Kenya Re',27,6),(42,'1-1-1-1',1,6),(43,'EXT',25,7),(44,'2013-02-10 00:00:00.0',26,7),(45,'Obunga',27,7),(46,'1-1-1-1',1,7),(47,'2013-06-08 00:00:00.0',28,8),(48,'1-1-1-1',1,8),(49,'Jalego',5,9),(50,'1-1-1-1',1,9),(51,'Jakoya',5,10),(52,'1-1-1-1',1,10),(53,'Jalolwe',5,11),(54,'1-1-1-1',1,11),(55,'2013-02-11 00:00:00.0',26,12),(56,'Kondele',27,12),(57,'1-1-1-1',1,12),(58,'Jane',12,13),(59,'Kenneth Matsumoto',15,13),(60,'1-1-1-1',1,13);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
