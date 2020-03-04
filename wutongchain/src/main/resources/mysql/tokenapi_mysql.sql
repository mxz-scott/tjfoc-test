-- MySQL dump 10.13  Distrib 5.7.29, for Linux (x86_64)
--
-- Host: localhost    Database: tokendb
-- ------------------------------------------------------
-- Server version	5.7.29-0ubuntu0.16.04.1

DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `address` varchar(64) NOT NULL,
  `addrtype` varchar(10) NOT NULL,
  `privatekey` varchar(256) NOT NULL,
  `publickey` varchar(256) NOT NULL,
  `cert` varchar(512) NOT NULL,
  `group` varchar(100) NOT NULL,
  `tags` varchar(256) NOT NULL,
  `comments` varchar(256) NOT NULL,
  `createdtime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号表';


LOCK TABLES `account` WRITE;
UNLOCK TABLES;


DROP TABLE IF EXISTS `accountgroup`;
CREATE TABLE `accountgroup` (
  `id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `tags` varchar(256) NOT NULL,
  `comments` varchar(256) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组表';


LOCK TABLES `accountgroup` WRITE;
UNLOCK TABLES;


DROP TABLE IF EXISTS `duplicate`;
CREATE TABLE `duplicate` (
  `hash` varchar(100) NOT NULL,
  `txid` varchar(100) NOT NULL,
  `createdtime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重复交易检测';


LOCK TABLES `duplicate` WRITE;
UNLOCK TABLES;


DROP TABLE IF EXISTS `localheight`;
CREATE TABLE `localheight` (
  `name` varchar(100) NOT NULL,
  `height` bigint(20) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地高度';


LOCK TABLES `localheight` WRITE;
UNLOCK TABLES;


DROP TABLE IF EXISTS `muladdr`;
CREATE TABLE `muladdr` (
  `address` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `subaddress` varchar(1024) NOT NULL,
  `createdtime` bigint(20) NOT NULL DEFAULT '0',
  `comments` varchar(256) NOT NULL,
  PRIMARY KEY (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多签地址表';


LOCK TABLES `muladdr` WRITE;
UNLOCK TABLES;


DROP TABLE IF EXISTS `sm4key`;
CREATE TABLE `sm4key` (
  `name` varchar(100) NOT NULL,
  `key` varchar(256) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对称密钥';


LOCK TABLES `sm4key` WRITE;
UNLOCK TABLES;


DROP TABLE IF EXISTS `tokentype`;
CREATE TABLE `tokentype` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tokentype` varchar(100) NOT NULL,
  `freeze` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='token列表';


LOCK TABLES `tokentype` WRITE;
UNLOCK TABLES;


DROP TABLE IF EXISTS `unspenttoken`;
CREATE TABLE `unspenttoken` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `txid` varchar(100) NOT NULL,
  `index` bigint(20) NOT NULL DEFAULT '0',
  `tokentype` varchar(100) NOT NULL,
  `address` varchar(64) NOT NULL,
  `value` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_utxo` (`txid`,`index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未花费列表';

LOCK TABLES `unspenttoken` WRITE;
UNLOCK TABLES;
