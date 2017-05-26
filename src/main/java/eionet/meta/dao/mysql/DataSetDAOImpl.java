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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.DatasetRegStatus;
import eionet.meta.service.data.DatasetFilter;
import org.springframework.jdbc.core.RowMapper;

/**
 * Data set DAO implementation.
 *
 * @author Juhan Voolaid
 */
@Repository
public class DataSetDAOImpl extends GeneralDAOImpl implements IDataSetDAO {

    /**
     * Dataset full name column label when the value is queried.
     */
    private static final String DATASET_NAME_COLUMN_LABEL = "DATASET_NAME";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSet> getDataSets() {
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct DATASET.* ");
        sql.append("from DATASET ");
        sql.append("where CORRESP_NS is not null and DATASET.DELETED is null ");
        sql.append("and DATASET.WORKING_COPY='N' ");
        sql.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");

        Map<String, Object> params = new HashMap<String, Object>();

        DataSetRowCallbackHandler dataSetRowCallbackHandler = new DataSetRowCallbackHandler();
        getNamedParameterJdbcTemplate().query(sql.toString(), params, dataSetRowCallbackHandler);
        return dataSetRowCallbackHandler.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSet> searchDatasets(DatasetFilter datasetFilter) {

        StringBuilder sql = new StringBuilder();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentType", DElemAttribute.ParentType.DATASET.toString());

        sql.append("select distinct * ");
        sql.append("from DATASET ");
        sql.append("where CORRESP_NS is not null and DELETED is null ");
        sql.append("and WORKING_COPY='N' ");

        if (StringUtils.isNotEmpty(datasetFilter.getIdentifier())) {
            sql.append("and IDENTIFIER like :identifier ");
            params.put("identifier", "%" + datasetFilter.getIdentifier() + "%");
        }

        if (StringUtils.isNotEmpty(datasetFilter.getShortName())) {
            sql.append("and SHORT_NAME like :shortName ");
            params.put("shortName", "%" + datasetFilter.getShortName() + "%");
        }
        // registration statuses into constraints
        if (datasetFilter.getRegStatuses() != null && datasetFilter.getRegStatuses().size() > 0) {
            sql.append("and REG_STATUS IN (:regStatuses) ");
            params.put("regStatuses", datasetFilter.getRegStatuses());
        }

        sql.append(getAttributesSqlConstraintAndAppendParams(datasetFilter, params, "DATASET_ID"));
        sql.append(getComplexAttrsSqlConstraintAndAppendParams(datasetFilter, params, "DATASET_ID"));

        sql.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");

        DataSetRowCallbackHandler dataSetRowCallbackHandler = new DataSetRowCallbackHandler();
        getNamedParameterJdbcTemplate().query(sql.toString(), params, dataSetRowCallbackHandler);
        return dataSetRowCallbackHandler.getResult();
    }

    @Override
    public List<DataSet> getRecentlyReleasedDatasets(int limit) {
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct DATASET.*, ATTRIBUTE.VALUE as ");
        sql.append(DATASET_NAME_COLUMN_LABEL);
        sql.append(" from DATASET ");
        sql.append(" inner join ATTRIBUTE on (DATASET.DATASET_ID = ATTRIBUTE.DATAELEM_ID AND ATTRIBUTE.PARENT_TYPE = :parent)");
        sql.append(" inner join M_ATTRIBUTE on (ATTRIBUTE.M_ATTRIBUTE_ID = M_ATTRIBUTE.M_ATTRIBUTE_ID");
        sql.append(" and M_ATTRIBUTE.SHORT_NAME = :name)");
        sql.append(" where DATASET.DELETED is null");
        sql.append(" and DATASET.WORKING_COPY='N' and REG_STATUS = :releasedRegStatus");
        sql.append(" order by DATE desc, DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("releasedRegStatus", DatasetRegStatus.RELEASED.toString());
        params.put("name", "Name");
        params.put("parent", DElemAttribute.ParentType.DATASET.toString());

        DataSetRowCallbackHandler dataSetRowCallbackHandler =
                new DataSetRowCallbackHandler(Arrays.asList(DATASET_NAME_COLUMN_LABEL), limit);
        getNamedParameterJdbcTemplate().query(sql.toString(), params, dataSetRowCallbackHandler);
        return dataSetRowCallbackHandler.getResult();
    }

    /**
     * Inner class to process rows from dataset queries.
     */
    private static final class DataSetRowCallbackHandler implements RowCallbackHandler {
        /**
         * Result list.
         */
        private List<DataSet> result;
        /**
         * Flag for is full name queried.
         */
        private boolean fullNameQueried;
        /**
         * Row instance.
         */
        private DataSet dataSet = null;
        /**
         * Collections of seen identifier.
         */
        private Set<String> seenIdentifiers;
        /**
         * Limit for the list.
         */
        private int limit;

        /**
         * Private constructor (since class is private inner class).
         *
         */
        private DataSetRowCallbackHandler() {
            this(new ArrayList<String>(), -1);
        }

        /**
         * Private constructor (since class is private inner class).
         *
         * @param additionalFieldsToSet
         *            additional fields to set.
         */
        private DataSetRowCallbackHandler(List<String> additionalFieldsToSet) {
            this(additionalFieldsToSet, -1);
        }

        /**
         * Private constructor (since class is private inner class).
         *
         * @param additionalFieldsToSet
         *            additional fields to set.
         * @param limit
         *            limit of list
         */
        private DataSetRowCallbackHandler(List<String> additionalFieldsToSet, int limit) {
            this.fullNameQueried = additionalFieldsToSet.contains(DATASET_NAME_COLUMN_LABEL);
            this.limit = limit;
            this.result = new ArrayList<DataSet>();
            // HashSet has better performance for contains.
            this.seenIdentifiers = new HashSet<String>();
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            // check for limit. negative values will never be reached so applying a negative value for limit means positive
            // infinite.
            if (this.result.size() == limit) {
                return;
            }

            // make sure we get the latest version of the dataset
            String identifier = rs.getString("IDENTIFIER");

            // checking for previous dataset id make it faster for ordered by identifier queries
            if (this.dataSet != null
                    && (this.dataSet.getIdentifier().equals(identifier) || this.seenIdentifiers.contains(identifier))) {
                return;
            }

            this.dataSet = new DataSet();
            this.dataSet.setId(rs.getInt("DATASET_ID"));
            this.dataSet.setIdentifier(identifier);
            this.dataSet.setShortName(rs.getString("SHORT_NAME"));
            this.dataSet.setDate(rs.getLong("DATE"));
            if (this.fullNameQueried) {
                this.dataSet.setName(rs.getString(DATASET_NAME_COLUMN_LABEL));
            }

            this.result.add(this.dataSet);
            this.seenIdentifiers.add(identifier);
        }

        public List<DataSet> getResult() {
            return this.result;
        }
    } // end of inner class DataSetRowCallbackHandler

    @Override
    public String getIdentifierById(int id) {
        String sql = "select IDENTIFIER from DATASET where DATASET_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        List<String> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("IDENTIFIER");
            }
        });
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<DataSet> getWorkingCopiesOf(String userName) {
        String sql = "select * from DATASET where DELETED is null " +
                "and WORKING_COPY = 'Y' and WORKING_USER = :userName order by IDENTIFIER asc, DATASET_ID desc";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userName", userName);

        DataSetRowCallbackHandler dataSetRowCallbackHandler = new DataSetRowCallbackHandler();
        getNamedParameterJdbcTemplate().query(sql.toString(), params, dataSetRowCallbackHandler);
        return dataSetRowCallbackHandler.getResult();
    }

}
