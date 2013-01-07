
create table T_SITE_CODE (
    SITE_CODE int(10) unsigned not null,
    SITE_NAME varchar(120) default null,
	SITE_CODE_NAT varchar(30) default null,
    STATUS enum('NEW','ALLOCATED','ASSIGNED') not null default 'NEW',
    CC_ISO2 char(2) default null,
    CC_ISO3 char(3) default null,
    DATE_CREATED timestamp not null default now(),
    USER_CREATED varchar(50) default null,
    DATE_ALLOCATED timestamp null default null,
    USER_ALLOCATED varchar(50),
    primary key (SITE_CODE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

