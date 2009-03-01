-- CREATE DATABASE `e132863_stats` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

use `e132863_stats`;

/*==============================================================*/
/* Table: DOWNLOAD_LOG                                          */
/*==============================================================*/
create table DOWNLOAD_LOG
(
   DOWNLOAD_LOG_ID      int not null auto_increment,
   DATE_INSERT          char(17) not null,
   TYPE                 char(1) not null,
   FILE                 varchar(255) not null,
   REMOTE_ADDRESS       varchar(80),
   USER_AGENT           varchar(255),
   primary key (DOWNLOAD_LOG_ID)
);
