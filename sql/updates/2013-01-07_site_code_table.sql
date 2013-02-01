
create table T_SITE_CODE (
    SITE_CODE_ID int(10) unsigned not null auto_increment,
    VOCABULARY_CONCEPT_ID int(10) unsigned not null,
    SITE_CODE varchar(100) default null,
    INITIAL_SITE_NAME varchar(200) default null,
	SITE_CODE_NAT varchar(30) default null,
    STATUS enum('AVAILABLE','ALLOCATED','ASSIGNED','DELETED','DISAPPEARED') not null default 'AVAILABLE',
    CC_ISO2 char(2) default null,
    PARENT_ISO char(3) default null,
    DATE_CREATED datetime null default null,
    USER_CREATED varchar(50) default null,
    DATE_ALLOCATED datetime null default null,
    USER_ALLOCATED varchar(50),
    DATE_DELETED year null default null,
	YEARS_DELETED varchar(100) default null,
	YEARS_DISAPPEARED varchar(100) default null,
    primary key (SITE_CODE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE T_SITE_CODE ADD CONSTRAINT FK_VOCABULARY_CONCEPT_ID FOREIGN KEY (VOCABULARY_CONCEPT_ID) REFERENCES T_VOCABULARY_CONCEPT(VOCABULARY_CONCEPT_ID) ON DELETE CASCADE;


alter table T_SITE_CODE ADD COLUMN YEARS_DELETED varchar(100) default null;
alter table T_SITE_CODE ADD COLUMN YEARS_DISAPPEARED varchar(100) default null;
