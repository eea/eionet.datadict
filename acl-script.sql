insert into ACLS (ACL_NAME, PARENT_NAME, OWNER, DESCRIPTION) select distinct
DATASET_ID, '/datasets', 'palitmet', concat('Short_name=', SHORT_NAME) from DATASET;

insert into ACLS (ACL_NAME, PARENT_NAME, OWNER, DESCRIPTION) select distinct
TABLE_ID, '/tables', 'palitmet', concat('Short_name=', SHORT_NAME, ', Parent_ns=', PARENT_NS)
from DS_TABLE;

insert into ACLS (ACL_NAME, PARENT_NAME, OWNER, DESCRIPTION) select distinct
DATAELEM_ID, '/elements', 'palitmet', concat('Short_name=', SHORT_NAME, ', Parent_ns=', PARENT_NS)
from DATAELEM;

insert into ACLS (ACL_NAME, PARENT_NAME, OWNER, DESCRIPTION) select distinct
concat('s', M_ATTRIBUTE_ID), '/attributes', 'palitmet', concat('Short_name=', SHORT_NAME)
from M_ATTRIBUTE;

insert into ACLS (ACL_NAME, PARENT_NAME, OWNER, DESCRIPTION) select distinct
concat('c', M_COMPLEX_ATTR_ID), '/attributes', 'palitmet', concat('Short_name=', SHORT_NAME)
from M_COMPLEX_ATTR;

insert into ACL_ROWS (ACL_ID, TYPE, ENTRY_TYPE, PRINCIPAL, PERMISSIONS, STATUS)
select distinct ACL_ID, 'object', 'user', 'palitmet', 'u,d,w,c', 1 from ACLS;