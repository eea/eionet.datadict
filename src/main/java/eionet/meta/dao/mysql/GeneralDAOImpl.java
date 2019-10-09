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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import eionet.meta.dao.IGeneralDao;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.service.data.IObjectWithDynamicAttrs;

/**
 * General dao.
 *
 * @author Juhan Voolaid
 */
public abstract class GeneralDAOImpl extends NamedParameterJdbcDaoSupport implements IGeneralDao {

    /** Logger. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(GeneralDAOImpl.class);

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
        return getJdbcTemplate().queryForObject("select last_insert_id()",Integer.class);
    }

    /**
     * Get the ID of 'Name' attribute
     *
     * @return
     */
    protected int getNameAttributeId() {
        return getJdbcTemplate().queryForObject("select M_ATTRIBUTE_ID from M_ATTRIBUTE where SHORT_NAME='Name'", Integer.class);
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

}
