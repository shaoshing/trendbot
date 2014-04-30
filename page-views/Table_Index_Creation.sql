CREATE TABLE `pagecount_feb_1` (
  `pageid` int(11) DEFAULT NULL,
  `pagetitle` varchar(1024) DEFAULT NULL,
  `pageviewcount` int(11) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  KEY `idx_pagecount_feb_1` (`pagetitle`(200),`datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pagecount_jan_1` (
  `pageid` int(11) DEFAULT NULL,
  `pagetitle` varchar(1024) DEFAULT NULL,
  `pageviewcount` int(11) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  KEY `idx_pagecount_jan_1` (`pagetitle`(200),`datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pagecount_feb_2` (
  `pageid` int(11) DEFAULT NULL,
  `pagetitle` varchar(1024) DEFAULT NULL,
  `pageviewcount` int(11) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  KEY `idx_pagecount_feb_2` (`pagetitle`(200),`datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pagecount_jan_2` (
  `pageid` int(11) DEFAULT NULL,
  `pagetitle` varchar(1024) DEFAULT NULL,
  `pageviewcount` int(11) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  KEY `idx_pagecount_jan_2` (`pagetitle`(200),`datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `trends` (
  `googletrendkey` varchar(100) DEFAULT NULL,
  `relatedkey` varchar(100) DEFAULT NULL,
  `pagetitle` varchar(100) DEFAULT NULL,
  `peaktime` datetime DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  `pageviewcount` int(11) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `trends1` (
  `googletrendkey` varchar(100) DEFAULT NULL,
  `relatedkey` varchar(100) DEFAULT NULL,
  `pagetitle` varchar(100) DEFAULT NULL,
  `peaktime` datetime DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  `pageviewcount` int(11) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




create index idx_pagecount_feb_1 on pagecount_feb_1(pagetitle(200),datetime);


create index idx_pagecount_feb_2 on pagecount_feb_2(pagetitle(200),datetime);



create index idx_pagecount_jan_1 on pagecount_jan_1(pagetitle(200),datetime);

create index idx_pagecount_jan_2 on pagecount_jan_2(pagetitle(200),datetime);



CREATE TABLE `pagecounts-20140228-230000` (
  `language` varchar(2) DEFAULT NULL,
  `pagetitle` varchar(1024) DEFAULT NULL,
  `pageviewcount` int(11) DEFAULT NULL,
  `datareturned` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


