/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.dao.mysql;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import eionet.meta.dao.IGeneralDao;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;
import eionet.meta.dao.domain.ComplexAttributeField;
import eionet.meta.service.data.IObjectWithDynamicAttrs;

/**
 * General dao.
 *
 * @author Juhan Voolaid
 */
public abstract class GeneralDAOImpl extends NamedParameterJdbcDaoSupport implements IGeneralDao {

    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(GeneralDAOImpl.class);

    /**
     * Data source.
     */
    @Autowired
    private DataSource dataSource;

    /** Initializes the needed objects after bean creation */
    @PostConstruct
    private void init() {
        super.setDataSource(dataSource);
    }

    /**
     * @return
     */
    protected int getLastInsertId() {
        return getJdbcTemplate().queryForInt("select last_insert_id()");
    }

    /**
     * Get the ID of 'Name' attribute
     *
     * @return
     */
    protected int getNameAttributeId() {
        return getJdbcTemplate().queryForInt("select M_ATTRIBUTE_ID from M_ATTRIBUTE where SHORT_NAME='Name'");
    }

    /**
     * Build SQL for searching objects by dynamic attribute values. The method also fills SQL query parameters map.
     *
     * @param filter
     *            IObjectWithDynamicAttrsFilter object where attribute values have been defined
     * @param params
     *            SQL query parameters map.
     * @param keyField
     *            Foreign key field in SQL to be used when joining ATTRIBUTE table.
     * @return SQL constraint with attributes values.
     */
    protected StringBuilder getAttributesSqlConstraintAndAppendParams(IObjectWithDynamicAttrs filter, Map<String, Object> params,
            String keyField) {

        StringBuilder sql = new StringBuilder();
        boolean attributesExist = false;
        if (filter.getAttributes() != null) {
            for (Attribute attr : filter.getAttributes()) {
                if (StringUtils.isNotEmpty(attr.getValue())) {
                    attributesExist = true;
                    break;
                }
            }
        }
        if (attributesExist) {
            for (int i = 0; i < filter.getAttributes().size(); i++) {
                Attribute a = filter.getAttributes().get(i);
                String idKey = "attrId" + i;
                String valueKey = "attrValue" + i;
                if (StringUtils.isNotEmpty(a.getValue())) {
                    sql.append("and " + keyField + " in ( ");
                    sql.append("select a.DATAELEM_ID from ATTRIBUTE a where a.PARENT_TYPE = :parentType ");
                    sql.append(" and a.M_ATTRIBUTE_ID = :" + idKey + " and a.VALUE like :" + valueKey);
                    sql.append(") ");
                    params.put(idKey, a.getId());
                    params.put(valueKey, "%" + a.getValue() + "%");
                }

            }
        }
        return sql;
    }

    /**
     * Build SQL for searching objects by dynamic complex attribute field values. The method also fills SQL query parameters map.
     *
     * @param filter
     *            IObjectWithDynamicAttrsFilter object where attribute values have been defined
     * @param params
     *            SQL query parameters map.
     * @param keyField
     *            Foreign key field in SQL to be used when joining ATTRIBUTE table.
     * @return SQL constraint with attributes values.
     */
    protected StringBuilder getComplexAttrsSqlConstraintAndAppendParams(IObjectWithDynamicAttrs filter,
            Map<String, Object> params, String keyField) {

        StringBuilder sql = new StringBuilder();
        boolean attributesExist = false;
        if (filter.getComplexAttributes() != null) {
            for (ComplexAttribute attr : filter.getComplexAttributes()) {
                if (attr.getFields() != null) {
                    for (ComplexAttributeField field : attr.getFields()) {
                        if (StringUtils.isNotEmpty(field.getValue())) {
                            attributesExist = true;
                            break;
                        }
                    }
                }
            }
        }
        if (attributesExist) {
            for (int i = 0; i < filter.getComplexAttributes().size(); i++) {
                ComplexAttribute a = filter.getComplexAttributes().get(i);
                String idKey = "complexAttrId" + i;
                if (a.getFields() != null) {
                    for (int j = 0; j < a.getFields().size(); j++) {
                        ComplexAttributeField field = a.getFields().get(j);
                        if (StringUtils.isNotEmpty(field.getValue())) {
                            sql.append("and " + keyField + " in ( ");
                            sql.append("select a.PARENT_ID from COMPLEX_ATTR_ROW a INNER JOIN COMPLEX_ATTR_FIELD f "
                                    + " ON a.ROW_ID=f.ROW_ID where a.PARENT_TYPE = :parentType and a.M_COMPLEX_ATTR_ID = :"
                                    + idKey);

                            String idFieldKey = "attrFieldId" + j;
                            String valueKey = "attrFieldValue" + j;
                            sql.append("  and f.M_COMPLEX_ATTR_FIELD_ID = :" + idFieldKey);
                            params.put(idFieldKey, field.getId());
                            if (field.isExactMatchInSearch()) {
                                sql.append("  and f.VALUE = :" + valueKey);
                                params.put(valueKey, field.getValue());
                            } else {
                                sql.append("  and f.VALUE like :" + valueKey);
                                params.put(valueKey, "%" + field.getValue() + "%");
                            }
                            sql.append(") ");
                        }

                    }
                    params.put(idKey, a.getId());
                }
            }
        }
        return sql;
    }
}
