-- drop fulltext on ATTRIBUTE, because InnoDB does not support full text indexes
alter table ATTRIBUTE drop key VALUE;

-- change all tables to InnoDB
alter table acl_rows engine=InnoDB;
alter table acls engine=InnoDB;
alter table attribute engine=InnoDB;
alter table cache engine=InnoDB;
alter table complex_attr_field engine=InnoDB;
alter table complex_attr_row engine=InnoDB;
alter table dataelem engine=InnoDB;
alter table dataset engine=InnoDB;
alter table doc engine=InnoDB;
alter table ds_table engine=InnoDB;
alter table dst2rod engine=InnoDB;
alter table dst2tbl engine=InnoDB;
alter table fk_relation engine=InnoDB;
alter table fxv engine=InnoDB;
alter table harv_attr engine=InnoDB;
alter table harv_attr_field engine=InnoDB;
alter table hlp_area engine=InnoDB;
alter table hlp_screen engine=InnoDB;
alter table m_attribute engine=InnoDB;
alter table m_complex_attr engine=InnoDB;
alter table m_complex_attr_field engine=InnoDB;
alter table namespace engine=InnoDB;
alter table rod_activities engine=InnoDB;
alter table tbl2elem engine=InnoDB;
