alter table COMPLEX_ATTR_ROW change column PARENT_TYPE PARENT_TYPE enum('E','C','DS','FV','T','SCH','SCS') not null default 'E';
