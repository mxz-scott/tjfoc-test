
CREATE DATABASE IF NOT EXISTS tokendb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tokendb;

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



DROP TABLE IF EXISTS `muladdr`;
CREATE TABLE `muladdr` (
  `address` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `subaddress` varchar(1024) NOT NULL,
  `createdtime` bigint(20) NOT NULL DEFAULT '0',
  `comments` varchar(256) NOT NULL,
  PRIMARY KEY (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多签地址表';

DROP TABLE IF EXISTS `duplicate`;
CREATE TABLE `duplicate` (
  `hash` varchar(100) NOT NULL,
  `txid` varchar(100) NOT NULL,
  `createdtime` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重复交易检测';


DROP TABLE IF EXISTS `accountgroup`;
CREATE TABLE `accountgroup` (
  `id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `tags` varchar(256) NOT NULL,
  `comments` varchar(256) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组表';


DROP TABLE IF EXISTS `unpsenttoken`;
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


DROP TABLE IF EXISTS `tokentype`;
CREATE TABLE `tokentype` (
  `id` bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `tokentype` varchar(100) NOT NULL,
  `freeze` bool NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='token列表';

DROP TABLE IF EXISTS `localheight`;
CREATE TABLE `localheight` (
  `name` varchar(100) NOT NULL,
  `height` bigint(20) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地高度';

DROP TABLE IF EXISTS `sm4key`;
CREATE TABLE `sm4key` (
  `name` varchar(100) NOT NULL,
  `key` varchar(256) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对称密钥';

