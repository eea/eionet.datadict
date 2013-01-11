
create table T_SITE_CODE (
    SITE_CODE_ID int(10) unsigned not null auto_increment,
    VOCABULARY_CONCEPT_ID int(10) unsigned not null,
    SITE_CODE varchar(100) default null,
    SITE_NAME_TMP varchar(120) default null,
	SITE_CODE_NAT varchar(30) default null,
    STATUS enum('NEW','ALLOCATED','ASSIGNED') not null default 'NEW',
    CC_ISO2 char(2) default null,
    DATE_CREATED timestamp not null default now(),
    USER_CREATED varchar(50) default null,
    DATE_ALLOCATED timestamp null default null,
    USER_ALLOCATED varchar(50),
    primary key (SITE_CODE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE T_SITE_CODE ADD CONSTRAINT FK_VOCABULARY_CONCEPT_ID FOREIGN KEY (VOCABULARY_CONCEPT_ID) REFERENCES T_VOCABULARY_CONCEPT(VOCABULARY_CONCEPT_ID) ON DELETE CASCADE;
