create table if not exists ROD_ACTIVITIES (
	ACTIVITY_ID int(10) unsigned not null default 0,
	ACTIVITY_TITLE text not null default '',
	LEGINSTR_ID int(10) unsigned not null default 0,
	LEGINSTR_TITLE text not null default '',
	PRIMARY KEY  (ACTIVITY_ID)
) TYPE=MyISAM;

create table if not exists DST2ROD (
	DATASET_ID int(10) unsigned not null default 0,
	ACTIVITY_ID int(10) unsigned not null default 0,
	PRIMARY KEY  (DATASET_ID, ACTIVITY_ID)
) TYPE=MyISAM;

alter table DATAELEM add column IS_ROD_PARAM enum('true', 'false') not null default 'true';