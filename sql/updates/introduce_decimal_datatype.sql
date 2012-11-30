
-- allow 'decimal' as one possible value for "Datatype" attribute
insert into FXV (OWNER_ID,OWNER_TYPE,VALUE,DEFINITION,SHORT_DESC) values
((select M_ATTRIBUTE_ID from M_ATTRIBUTE where SHORT_NAME='Datatype' limit 1), 'attr', 'decimal',
'Represents any numbers, and allows to precisely define the total number of digits in these numbers, and the number of digits in their fraction parts', '');

-- create temporary table for IDs of floats and doubles
create temporary table floats_and_doubles select distinct DATAELEM_ID as ELM_ID from ATTRIBUTE where M_ATTRIBUTE_ID=25 and PARENT_TYPE='E' and VALUE in ('float','double');

-- create temporary table for IDs of floats and doubles that have 'DecimalPrecision' or 'MaxSize' specififed
create temporary table decimals select distinct DATAELEM_ID from ATTRIBUTE,floats_and_doubles where M_ATTRIBUTE_ID in (24,31) and PARENT_TYPE='E' and DATAELEM_ID=ELM_ID;

-- set the datatype of all above-found floats and doubles into 'decimal'
update ATTRIBUTE set VALUE='decimal' where M_ATTRIBUTE_ID=25 and PARENT_TYPE='E' and DATAELEM_ID in (select DATAELEM_ID from decimals);
drop table floats_and_doubles;
drop table decimals;

-- create temporary table for IDs	 of decimals whose DecimalPrecision=0
create temporary table actually_integers select distinct DATAELEM_ID from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and VALUE='decimal' and DATAELEM_ID in (select distinct DATAELEM_ID from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=31 and VALUE='0');

-- turn all decimals whose DecimalPrecision=0 into integers
update ATTRIBUTE set VALUE='integer' where PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and DATAELEM_ID in (select DATAELEM_ID from actually_integers);
delete from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=31 and DATAELEM_ID in (select DATAELEM_ID from actually_integers);
drop table actually_integers;

-- remove all MaxSize attributes where datatype is not in ('string','integer','decimal')
create temporary table no_max_size_allowed select distinct DATAELEM_ID from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and VALUE in ('boolean','date','float','double');
delete from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=24 and DATAELEM_ID in (select DATAELEM_ID from no_max_size_allowed);
drop table no_max_size_allowed;

-- remove all MaxSize=0 attributes where datatype is integer or decimal
create temporary table integers_and_decimals select distinct DATAELEM_ID from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and VALUE in ('integer','decimal');
delete from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=24 and VALUE='0' and DATAELEM_ID in (select DATAELEM_ID from integers_and_decimals);
drop table integers_and_decimals;

-- remove all MaxExclusiveValue,MaxInclusiveValue,MinExclusiveValue,MinInclusiveValue,DecimalPrecision attributes where datatype is non-numeric
create temporary table non_numerics select distinct DATAELEM_ID from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and VALUE in ('string','boolean','date');
delete from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID in (31,33,34,41,42) and DATAELEM_ID in (select DATAELEM_ID from non_numerics);
drop table non_numerics;

-- remove all MinSize attributes where datatype is not string
create temporary table non_strings select distinct DATAELEM_ID from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and VALUE in ('boolean','integer','date','decimal','float','double');
delete from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=8 and DATAELEM_ID in (select DATAELEM_ID from non_strings);
drop table non_strings;

-- remove DecimalPrecision attributes where datatype is not decimal
create temporary table non_decimals select distinct DATAELEM_ID from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and VALUE in ('string','boolean','integer','date','float','double');
delete from ATTRIBUTE where PARENT_TYPE='E' and M_ATTRIBUTE_ID=31 and DATAELEM_ID in (select DATAELEM_ID from non_decimals);
drop table non_decimals;

