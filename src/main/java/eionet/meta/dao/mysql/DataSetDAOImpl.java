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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.domain.DataSet;

/**
 * Data set DAO implementation.
 *
 * @author Juhan Voolaid
 */
@Repository
public class DataSetDAOImpl extends GeneralDAOImpl implements IDataSetDAO {

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
        final List<DataSet> result = new ArrayList<DataSet>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
            DataSet dataSet;
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // make sure we get the latest version of the dataset
                String identifier = rs.getString("IDENTIFIER");

                if (dataSet != null && dataSet.getIdentifier().equals(identifier)) {
                    return;
                }

                dataSet = new DataSet();
                dataSet.setId(rs.getInt("DATASET_ID"));
                dataSet.setIdentifier(rs.getString("IDENTIFIER"));
                dataSet.setShortName(rs.getString("SHORT_NAME"));

                result.add(dataSet);
            }
        });

        return result;
    }

}
