CREATE TABLE `ANNOTATION` (
  `ID` int(11) NOT NULL,
  `OUTPOINT` int(11) default NULL,
  `INPOINT` int(11) default NULL,
  `MEDIA_PACKAGE_ID` varchar(255) collate utf8_unicode_ci default NULL,
  `SESSION_ID` varchar(255) collate utf8_unicode_ci default NULL,
  `CREATED` datetime default NULL,
  `LENGTH` int(11) default NULL,
  `ANNOTATION_VAL` varchar(255) collate utf8_unicode_ci default NULL,
  `ANNOTATION_KEY` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `CAPTURE_AGENT` (
  `NAME` varchar(255) collate utf8_unicode_ci NOT NULL,
  `state` varchar(255) collate utf8_unicode_ci NOT NULL,
  `capabilities` blob,
  `lastHeardFrom` bigint(20) NOT NULL,
  `url` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `JOB` (
  `ID` varchar(255) collate utf8_unicode_ci NOT NULL,
  `HOST` varchar(255) collate utf8_unicode_ci default NULL,
  `STATUS` int(11) default NULL,
  `ELEMENT_XML` mediumtext collate utf8_unicode_ci,
  `TYPE` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SCHED_EVENT` (
  `ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SCHED_EVENT_METADATA` (
  `EVENT_ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  `METADATA_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`EVENT_ID`,`METADATA_ID`),
  KEY `FK_SCHED_EVENT_METADATA_METADATA_ID` (`METADATA_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SCHED_METADATA` (
  `ID` bigint(20) NOT NULL,
  `MD_VAL` varchar(255) collate utf8_unicode_ci default NULL,
  `MD_KEY` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SCHED_R_EVENT` (
  `ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  `RECURRENCE` varchar(255) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SCHED_R_EVENT_ITEM` (
  `REC_EVENT_ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  `EVENT_ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`REC_EVENT_ID`,`EVENT_ID`),
  KEY `FK_SCHED_R_EVENT_ITEM_EVENT_ID` (`EVENT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SCHED_R_EVENT_METADATA` (
  `REC_EVENT_ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  `MD_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`REC_EVENT_ID`,`MD_ID`),
  KEY `FK_SCHED_R_EVENT_METADATA_MD_ID` (`MD_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SEQUENCE` (
  `SEQ_NAME` varchar(50) collate utf8_unicode_ci NOT NULL,
  `SEQ_COUNT` decimal(38,0) default NULL,
  PRIMARY KEY  (`SEQ_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO SEQUENCE VALUES('SEQ_GEN', 50);

CREATE TABLE `SERIES` (
  `ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SERIES_METADATA` (
  `METADATA_KEY` varchar(128) collate utf8_unicode_ci NOT NULL,
  `METADATA_VAL` varchar(256) collate utf8_unicode_ci default NULL,
  `SERIES_ID` varchar(128) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`METADATA_KEY`,`SERIES_ID`),
  KEY `FK_SERIES_METADATA_SERIES_ID` (`SERIES_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `SERVICE_REGISTRATION` (
  `HOST` varchar(255) collate utf8_unicode_ci NOT NULL,
  `JOB_TYPE` varchar(255) collate utf8_unicode_ci NOT NULL,
  `MAINTENANCE` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`HOST`,`JOB_TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `UPLOAD` (
  `ID` varchar(255) collate utf8_unicode_ci NOT NULL,
  `total` bigint(20) NOT NULL,
  `received` bigint(20) NOT NULL,
  `filename` varchar(255) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
