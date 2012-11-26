alter table complex_attr_row change column PARENT_TYPE PARENT_TYPE enum('E','C','DS','FV','T','SCH','SCS') not null default 'E';
