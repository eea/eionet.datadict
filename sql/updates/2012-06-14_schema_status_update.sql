ALTER TABLE T_SCHEMA MODIFY REG_STATUS enum('Draft','Public draft','Released') DEFAULT NULL;
ALTER TABLE T_SCHEMA_SET MODIFY REG_STATUS enum('Draft','Public draft','Released') DEFAULT NULL;
