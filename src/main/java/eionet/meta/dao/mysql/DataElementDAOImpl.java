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

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.InferenceRule;
import eionet.meta.dao.domain.InferenceRule.RuleType;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.mysql.valueconverters.BooleanToYesNoConverter;
import eionet.meta.service.data.DataElementsFilter;
import eionet.util.Props;
import eionet.util.PropsIF;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import eionet.meta.service.data.VocabularyConceptBoundElementFilter;
import eionet.util.Pair;
import eionet.util.Triple;
import java.sql.PreparedStatement;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

/**
 * Data element DAO.
 *
 * @author Juhan Voolaid
 */
@Repository
public class DataElementDAOImpl extends GeneralDAOImpl implements IDataElementDAO {

    /**
     * Logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DataElementDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataElement> searchDataElements(DataElementsFilter filter) {
        boolean commonElements = filter.getElementType().equals(DataElementsFilter.COMMON_ELEMENT_TYPE);
        return commonElements ? executeCommonElementQuery(filter) : executeNonCommonElementQuery(filter);
    }

    /**
     * finds Common elements.
     *
     * @param filter search filter
     * @return list of data elements
     */
    private List<DataElement> executeCommonElementQuery(final DataElementsFilter filter) {
        Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();

        sql.append("select de.DATAELEM_ID, de.IDENTIFIER, de.SHORT_NAME, de.REG_STATUS, de.DATE, de.TYPE, de.WORKING_USER, ");
        sql.append("a.VALUE as NAME ");
        sql.append("from DATAELEM de ");
        sql.append("LEFT JOIN (ATTRIBUTE a, M_ATTRIBUTE ma) ");
        sql.append("ON (de.DATAELEM_ID=a.DATAELEM_ID AND a.PARENT_TYPE='E' AND ma.M_ATTRIBUTE_ID=a.M_ATTRIBUTE_ID ");
        sql.append("and ma.SHORT_NAME='Name') ");
        sql.append("where ");
        sql.append("de.PARENT_NS is null ");
        sql.append("and de.WORKING_COPY = 'N' ");

        // Filter parameters
        if (StringUtils.isNotEmpty(filter.getRegStatus())) {
            sql.append("and de.REG_STATUS = :regStatus ");
            params.put("regStatus", filter.getRegStatus());
        }
        if (StringUtils.isNotEmpty(filter.getType())) {
            sql.append("and de.TYPE = :type ");
            params.put("type", filter.getType());
        }
        if (StringUtils.isNotEmpty(filter.getShortName())) {
            sql.append("and de.SHORT_NAME like :shortName ");
            params.put("shortName", "%" + filter.getShortName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            sql.append("and de.IDENTIFIER like :identifier ");
            String like = "%";
            if (filter.isExactIdentifierMatch()) {
                like = "";
            }
            params.put("identifier", like + filter.getIdentifier() + like);
        }

        if (filter.isIncludeOnlyInternal()) {
            sql.append("and de.IDENTIFIER NOT like '%:%'");
        }
        // attributes
        for (int i = 0; i < filter.getAttributes().size(); i++) {
            Attribute a = filter.getAttributes().get(i);
            String idKey = "attrId" + i;
            String valueKey = "attrValue" + i;
            if (StringUtils.isNotEmpty(a.getValue())) {
                sql.append("and ");
                sql.append("de.DATAELEM_ID in ( ");
                sql.append("select a.DATAELEM_ID from ATTRIBUTE a WHERE ");
                sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " AND a.VALUE like :" + valueKey + " AND a.PARENT_TYPE = :parentType ");
                sql.append(") ");
            }
            params.put(idKey, a.getId());
            String value = "%" + a.getValue() + "%";
            params.put(valueKey, value);
            params.put("parentType", DElemAttribute.ParentType.ELEMENT.toString());
        }

        sql.append("order by de.IDENTIFIER asc, de.DATAELEM_ID desc");

        // LOGGER.debug("SQL: " + sql.toString());
        final List<DataElement> dataElements = new ArrayList<DataElement>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
            DataElement de;
            String curElmIdf;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // int elmID = rs.getInt("de.DATAELEM_ID");
                String elmIdf = rs.getString("de.IDENTIFIER");
                // skip non-existing elements, ie trash from some erroneous situation
                if (elmIdf == null) {
                    return;
                }

                // the following if block skips non-latest
                if (curElmIdf != null && elmIdf.equals(curElmIdf)) {
                    if (!filter.isIncludeHistoricVersions()) {
                        return;
                    }
                } else {
                    curElmIdf = elmIdf;
                }

                de = new DataElement();
                de.setId(rs.getInt("de.DATAELEM_ID"));
                de.setShortName(rs.getString("de.SHORT_NAME"));
                de.setStatus(rs.getString("de.REG_STATUS"));
                de.setType(rs.getString("de.TYPE"));
                de.setModified(new Date(rs.getLong("de.DATE")));
                de.setWorkingUser(rs.getString("de.WORKING_USER"));
                de.setIdentifier(rs.getString("de.IDENTIFIER"));

                de.setName(rs.getString("NAME"));
                dataElements.add(de);
            }
        });

        return dataElements;
    }

    /**
     * finds non-Common elements.
     *
     * @param filter search filter
     * @return list of data elements
     */
    private List<DataElement> executeNonCommonElementQuery(final DataElementsFilter filter) {
        Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();

        sql.append("select de.DATAELEM_ID, de.IDENTIFIER, de.SHORT_NAME, ds.REG_STATUS, de.DATE, de.TYPE, ");
        sql.append("t.SHORT_NAME as tableName, ds.IDENTIFIER as datasetName, ds.IDENTIFIER, ds.DATASET_ID, t.IDENTIFIER, ");
        sql.append("t.TABLE_ID, de.WORKING_USER, de.WORKING_COPY, a.VALUE AS NAME ");
        sql.append("from DATAELEM de ");
        sql.append("left join TBL2ELEM t2e on (de.DATAELEM_ID = t2e.DATAELEM_ID) ");
        sql.append("LEFT JOIN (ATTRIBUTE a, M_ATTRIBUTE ma) ");
        sql.append("ON (de.DATAELEM_ID=a.DATAELEM_ID AND a.PARENT_TYPE='E' AND ma.M_ATTRIBUTE_ID=a.M_ATTRIBUTE_ID ");
        sql.append("and ma.SHORT_NAME='Name') ");
        sql.append("left join DS_TABLE t on (t2e.TABLE_ID = t.TABLE_ID) ");
        sql.append("left join DST2TBL d2t on (t.TABLE_ID = d2t.TABLE_ID) ");
        sql.append("left join DATASET ds on (d2t.DATASET_ID = ds.DATASET_ID) ");
        sql.append("where ");
        sql.append("de.PARENT_NS is not null ");
        sql.append("and ds.DELETED is null ");
        sql.append("and ds.WORKING_COPY = 'N' ");
        // Filter parameters
        if (StringUtils.isNotEmpty(filter.getDataSet())) {
            sql.append("and ds.IDENTIFIER = :dataSet ");
            params.put("dataSet", filter.getDataSet());
        }
        if (StringUtils.isNotEmpty(filter.getType())) {
            sql.append("and de.TYPE = :type ");
            params.put("type", filter.getType());
        }
        if (StringUtils.isNotEmpty(filter.getShortName())) {
            sql.append("and de.SHORT_NAME like :shortName ");
            params.put("shortName", "%" + filter.getShortName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            sql.append("and de.IDENTIFIER like :identifier ");
            params.put("identifier", "%" + filter.getIdentifier() + "%");
        }
        // attributes
        for (int i = 0; i < filter.getAttributes().size(); i++) {
            Attribute a = filter.getAttributes().get(i);
            String idKey = "attrId" + i;
            String valueKey = "attrValue" + i;
            if (StringUtils.isNotEmpty(a.getValue())) {
                sql.append("and ");
                sql.append("de.DATAELEM_ID in ( ");
                sql.append("select a.DATAELEM_ID from ATTRIBUTE a WHERE ");
                sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " AND a.VALUE like :" + valueKey + " AND a.PARENT_TYPE = :parentType ");
                sql.append(") ");
            }
            params.put(idKey, a.getId());
            String value = "%" + a.getValue() + "%";
            params.put(valueKey, value);
            params.put("parentType", DElemAttribute.ParentType.ELEMENT.toString());
        }

        sql.append("order by ds.IDENTIFIER asc, ds.DATASET_ID desc, t.IDENTIFIER asc, t.TABLE_ID desc, de.IDENTIFIER asc, ")
                .append("de.DATAELEM_ID desc");

        // LOGGER.debug("SQL: " + sql.toString());
        final List<DataElement> dataElements = new ArrayList<DataElement>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
            DataElement de;
            String curDstIdf;
            String curDstID;

            @Override
            public void processRow(ResultSet rs) throws SQLException {

                String dstID = rs.getString("ds.DATASET_ID");
                String dstIdf = rs.getString("ds.IDENTIFIER");
                if (dstID == null || dstIdf == null) {
                    return;
                }

                String tblID = rs.getString("t.TABLE_ID");
                String tblIdf = rs.getString("t.IDENTIFIER");
                // skip non-existing tables, ie trash from some erroneous situation
                if (tblID == null || tblIdf == null) {
                    return;
                }

                // int elmID = rs.getInt("de.DATAELEM_ID");
                String elmIdf = rs.getString("de.IDENTIFIER");
                // skip non-existing elements, ie trash from some erroneous situation
                if (elmIdf == null) {
                    return;
                }

                // the following if block skips elements from non-latest DATASETS
                if (curDstIdf == null || !curDstIdf.equals(dstIdf)) {
                    curDstID = dstID;
                    curDstIdf = dstIdf;
                } else if (!filter.isIncludeHistoricVersions()) {
                    if (!curDstID.equals(dstID)) {
                        return;
                    }
                }

                de = new DataElement();
                de.setId(rs.getInt("de.DATAELEM_ID"));
                de.setShortName(rs.getString("de.SHORT_NAME"));
                de.setStatus(rs.getString("ds.REG_STATUS"));
                de.setType(rs.getString("de.TYPE"));
                de.setModified(new Date(rs.getLong("de.DATE")));
                de.setTableName(rs.getString("tableName"));
                de.setDataSetName(rs.getString("datasetName"));
                de.setWorkingUser(rs.getString("de.WORKING_USER"));

                de.setIdentifier(rs.getString("de.IDENTIFIER"));
                de.setName(rs.getString("NAME"));
                dataElements.add(de);
            }
        });

        return dataElements;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SQLException
     */
    @Override
    public List<Attribute> getDataElementAttributes() throws SQLException {
        List<Attribute> result = new ArrayList<Attribute>();
        DDSearchEngine searchEngine = new DDSearchEngine(getConnection());

        @SuppressWarnings("rawtypes")
        Vector attrs = searchEngine.getDElemAttributes();

        for (int i = 0; i < attrs.size(); i++) {
            DElemAttribute attribute = (DElemAttribute) attrs.get(i);

            if (attribute.displayFor("CH1") || attribute.displayFor("CH2")) {
                Attribute a = new Attribute();
                a.setId(Integer.parseInt(attribute.getID()));
                a.setName(attribute.getName());
                a.setShortName(attribute.getShortName());

                result.add(a);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FixedValue> getFixedValues(int dataElementId, Boolean countryCode) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", dataElementId);
        params.put("ownerType", "elem");

        String sql = "select * from FXV where OWNER_ID=:ownerId and OWNER_TYPE=:ownerType ";
        if(countryCode == true){
            String excludedCountryCodes = Props.getProperty(PropsIF.EXCLUDED_COUNTY_CODES);
            String[] excludedCountryCodesList = excludedCountryCodes.split("," );
            sql+=" and VALUE not in (:excludedCountryCodes) ";
            params.put("excludedCountryCodes", Arrays.asList(excludedCountryCodesList));
        }

        sql+= " order by FXV_ID ";



        List<FixedValue> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<FixedValue>() {
            @Override
            public FixedValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                FixedValue fv = new FixedValue();
                fv.setId(rs.getInt("FXV_ID"));
                fv.setOwnerId(rs.getInt("OWNER_ID"));
                fv.setOwnerType(rs.getString("OWNER_TYPE"));
                fv.setValue(rs.getString("VALUE"));
                fv.setDefinition(rs.getString("DEFINITION"));
                fv.setShortDescription(rs.getString("SHORT_DESC"));
                return fv;
            }
        });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataElement getDataElement(int id) {
        String sql = "select * from DATAELEM de where de.DATAELEM_ID = :id";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id);

        DataElement result = getNamedParameterJdbcTemplate().queryForObject(sql, parameters, new RowMapper<DataElement>() {
            @Override
            public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement de = new DataElement();
                de.setId(rs.getInt("de.DATAELEM_ID"));
                de.setIdentifier(rs.getString("de.IDENTIFIER"));
                de.setShortName(rs.getString("de.SHORT_NAME"));
                de.setStatus(rs.getString("de.REG_STATUS"));
                de.setType(rs.getString("de.TYPE"));
                de.setModified(new Date(rs.getLong("de.DATE")));
                de.setWorkingCopy(new BooleanToYesNoConverter().convertBack(rs.getString("de.WORKING_COPY")));
                de.setWorkingUser(rs.getString("de.WORKING_USER"));
                de.setDate(rs.getString("de.DATE"));
                setParentNamespace(de, rs, "de.PARENT_NS");
                de.setAllConceptsValid(rs.getBoolean("de.ALL_CONCEPTS_LEGAL"));
                de.setVocabularyId(rs.getInt("de.VOCABULARY_ID"));

                return de;
            }
        });
        return result;
    }

    @Override
    public boolean dataElementExists(int id) {
        String sql = "select count(*) from DATAELEM where DATAELEM_ID = :id";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id);

        int count = getNamedParameterJdbcTemplate().queryForObject(sql, parameters, Integer.class);
        return (count > 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataElement getDataElement(String identifier) {
        return getDataElement(getCommonDataElementId(identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCommonDataElementId(String identifier) {
        String sql
                = "select max(de.DATAELEM_ID) from DATAELEM de where de.IDENTIFIER = :identifier and de.REG_STATUS = :regStatus "
                + "and PARENT_NS IS NULL ";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", identifier);
        parameters.put("regStatus", RegStatus.RELEASED.toString());

        return getNamedParameterJdbcTemplate().queryForObject(sql, parameters, Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSet getParentDataSet(int dataElementId) {
        String sql
                = "select ds.*\n"
                + "from (\n"
                + "	select DATAELEM_ID from DATAELEM where DATAELEM_ID = :dataElementId and PARENT_NS is not null\n"
                + ") de \n"
                + "	inner join TBL2ELEM dt2de on dt2de.DATAELEM_ID = de.DATAELEM_ID\n"
                + "	inner join DST2TBL ds2dt on ds2dt.TABLE_ID = dt2de.TABLE_ID\n"
                + "	inner join DATASET ds on ds.DATASET_ID = ds2dt.DATASET_ID;";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataElementId", dataElementId);

        List<DataSet> result = this.getNamedParameterJdbcTemplate().query(sql, parameters, new RowMapper<DataSet>() {

            @Override
            public DataSet mapRow(ResultSet rs, int i) throws SQLException {
                DataSet ds = new DataSet();
                ds.setId(rs.getInt("ds.DATASET_ID"));
                ds.setIdentifier(rs.getString("ds.IDENTIFIER"));
                ds.setDate(rs.getLong("ds.DATE"));
                ds.setShortName(rs.getString("ds.SHORT_NAME"));
                ds.setWorkingCopy(new BooleanToYesNoConverter().convertBack(rs.getString("ds.WORKING_COPY")));
                ds.setWorkingUser(rs.getString("ds.WORKING_USER"));

                return ds;
            }
        });

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataElementDataType(int dataElementId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select at.VALUE from DATAELEM de ");
        sql.append("left join ATTRIBUTE at on at.DATAELEM_ID = de.DATAELEM_ID ");
        sql.append("left join M_ATTRIBUTE ma on ma.M_ATTRIBUTE_ID = at.M_ATTRIBUTE_ID ");
        sql.append("where de.dataelem_id = :dataElementId and ma.NAME like :dataType ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dataElementId", dataElementId);
        params.put("dataType", "datatype");

        String result = null;
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDataElement(int vocabularyFolderId, int dataElementId) {
        String sql = "insert into VOCABULARY2ELEM (VOCABULARY_ID, DATAELEM_ID) values (:vocabularyFolderId, :dataElementId)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);
        params.put("dataElementId", dataElementId);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDataElement(int vocabularyFolderId, int dataElementId) {
        String sql = "delete from VOCABULARY2ELEM where VOCABULARY_ID = :vocabularyFolderId and DATAELEM_ID = :dataElementId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);
        params.put("dataElementId", dataElementId);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataElement> getVocabularyDataElements(int vocabularyFolderId) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ve.*, de.* from VOCABULARY2ELEM ve left join DATAELEM de on ve.DATAELEM_ID = de.DATAELEM_ID ");
        sb.append("where ve.VOCABULARY_ID = :vocabularyFolderId");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);

        List<DataElement> result = getNamedParameterJdbcTemplate().query(sb.toString(), params, new RowMapper<DataElement>() {

            @Override
            public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement de = new DataElement();
                de.setId(rs.getInt("de.DATAELEM_ID"));
                de.setShortName(rs.getString("de.SHORT_NAME"));
                de.setStatus(rs.getString("de.REG_STATUS"));
                de.setType(rs.getString("de.TYPE"));
                de.setModified(new Date(rs.getLong("de.DATE")));
                de.setWorkingUser(rs.getString("de.WORKING_USER"));
                de.setIdentifier(rs.getString("de.IDENTIFIER"));

                return de;
            }

        });

        for (DataElement elem : result) {
            List<FixedValue> fxvs = getFixedValues(elem.getId(), false);
            elem.setFixedValues(fxvs);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyDataElements(int vocabularyFolderId) {
        String sql = "delete from VOCABULARY2ELEM where VOCABULARY_ID = :vocabularyFolderId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);

        getNamedParameterJdbcTemplate().update(sql, params);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyConceptDataElementValues(int vocabularyConceptId) {
        String sql = "delete from VOCABULARY_CONCEPT_ELEMENT where VOCABULARY_CONCEPT_ID = :vocabularyConceptId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyConceptId", vocabularyConceptId);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void deleteVocabularyConceptDataElementValues(int vocabularyConceptId, int dataElementId) {
        String sql = "delete from VOCABULARY_CONCEPT_ELEMENT where VOCABULARY_CONCEPT_ID = :vocabularyConceptId and DATAELEM_ID = :dataElementId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyConceptId", vocabularyConceptId);
        params.put("dataElementId", dataElementId);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId) {
        String sql
                = "update VOCABULARY2ELEM set VOCABULARY_ID = :targetVocabularyFolderId "
                + "where VOCABULARY_ID = :sourceVocabularyFolderId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceVocabularyFolderId", sourceVocabularyFolderId);
        params.put("targetVocabularyFolderId", targetVocabularyFolderId);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into VOCABULARY2ELEM (VOCABULARY_ID, DATAELEM_ID) ");
        sb.append("select :targetVocabularyFolderId, DATAELEM_ID ");
        sb.append("from VOCABULARY2ELEM ");
        sb.append("where VOCABULARY_ID = :sourceVocabularyFolderId");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceVocabularyFolderId", sourceVocabularyFolderId);
        params.put("targetVocabularyFolderId", targetVocabularyFolderId);

        getNamedParameterJdbcTemplate().update(sb.toString(), params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<List<DataElement>>> getVocabularyConceptsDataElementValues(int vocabularyFolderId,
            int[] vocabularyConceptIds, boolean emptyAttributes) {
        // this does not work for IN type, although it is recommended!!!!
        // final MapSqlParameterSource params = new MapSqlParameterSource();
        //
        // params.addValue("vocabularyFolderId", vocabularyFolderId);
        // params.addValue("vocabularyConceptIds", vocabularyConceptIds);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);
        // to work in "IN" clause, it should be list of Integer objects.
        params.put("vocabularyConceptIds", Arrays.asList(ArrayUtils.toObject(vocabularyConceptIds)));

        StringBuilder sql = new StringBuilder();
        sql.append("select * from VOCABULARY_CONCEPT_ELEMENT v ");
        if (emptyAttributes) {
            sql.append("RIGHT OUTER JOIN DATAELEM d ");
        } else {
            sql.append("LEFT JOIN DATAELEM d ");
        }
        sql.append("ON (v.DATAELEM_ID = d.DATAELEM_ID and v.VOCABULARY_CONCEPT_ID in (:vocabularyConceptIds)) ");
        sql.append("LEFT JOIN VOCABULARY2ELEM ve on ve.DATAELEM_ID = d.DATAELEM_ID ");
        sql.append("LEFT JOIN VOCABULARY_CONCEPT rc on v.RELATED_CONCEPT_ID = rc.VOCABULARY_CONCEPT_ID ");
        sql.append("LEFT JOIN VOCABULARY rcv ON rc.VOCABULARY_ID = rcv.VOCABULARY_ID ");
        sql.append("LEFT JOIN VOCABULARY_SET rcvs ON rcv.FOLDER_ID = rcvs.ID ");
        sql.append("where ve.VOCABULARY_ID = :vocabularyFolderId ");
        sql.append("order by v.VOCABULARY_CONCEPT_ID, ve.DATAELEM_ID, v.ELEMENT_VALUE, rc.IDENTIFIER");

        final Map<Integer, List<List<DataElement>>> result = new HashMap<Integer, List<List<DataElement>>>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {

            List<List<DataElement>> listOfValues = null;
            List<DataElement> values = null;
            int previousDataElemId = -1;
            int previousConceptId = -1;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int conceptId = rs.getInt("v.VOCABULARY_CONCEPT_ID");
                int dataElemId = rs.getInt("d.DATAELEM_ID");

                if (previousConceptId != conceptId) {
                    listOfValues = new ArrayList<List<DataElement>>();
                    result.put(conceptId, listOfValues);
                    values = new ArrayList<DataElement>();
                    listOfValues.add(values);
                } else if (previousDataElemId != dataElemId) {
                    values = new ArrayList<DataElement>();
                    listOfValues.add(values);
                }

                previousConceptId = conceptId;
                previousDataElemId = dataElemId;

                DataElement de = new DataElement();
                de.setId(dataElemId);
                de.setShortName(rs.getString("d.SHORT_NAME"));
                de.setStatus(rs.getString("d.REG_STATUS"));
                de.setType(rs.getString("d.TYPE"));
                de.setModified(new Date(rs.getLong("d.DATE")));
                de.setWorkingUser(rs.getString("d.WORKING_USER"));

                de.setIdentifier(rs.getString("d.identifier"));

                de.setAttributeValue(rs.getString("v.ELEMENT_VALUE"));
                de.setAttributeLanguage(rs.getString("v.LANGUAGE"));
                de.setRelatedConceptId(rs.getInt("v.RELATED_CONCEPT_ID"));

                de.setRelatedConceptIdentifier(rs.getString("rc.IDENTIFIER"));
                de.setRelatedConceptLabel(rs.getString("rc.LABEL"));
                de.setRelatedConceptVocabulary(rs.getString("rcv.IDENTIFIER"));
                de.setRelatedConceptBaseURI(rs.getString("rcv.BASE_URI"));
                de.setRelatedConceptVocSet(rs.getString("rcvs.IDENTIFIER"));
                de.setVocabularyId(rs.getInt("d.VOCABULARY_ID"));

                de.setRelatedConceptOriginalId(rs.getInt("rc.ORIGINAL_CONCEPT_ID"));
                de.setRelatedVocabularyStatus(rs.getString("rcv.REG_STATUS"));
                de.setRelatedVocabularyWorkingCopy(rs.getInt("rcv.WORKING_COPY") == 1);
                de.setValueId(rs.getInt("v.ID"));
                List<FixedValue> fxvs = getFixedValues(de.getId(), false);
                de.setFixedValues(fxvs);
                de.setElemAttributeValues(getDataElementAttributeValues(dataElemId));

                values.add(de);
            }
        });

        if (emptyAttributes && result.get(0) != null) {
            if (result.containsKey(vocabularyConceptIds[0])) {
                result.get(vocabularyConceptIds[0]).addAll(result.get(0));
            } else {
                result.put(vocabularyConceptIds[0], result.get(0));
            }
        }

        // fill empty lists for not found concepts
        for (int conceptId : vocabularyConceptIds) {
            if (!result.containsKey(conceptId)) {
                result.put(conceptId, new ArrayList<List<DataElement>>());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertVocabularyConceptDataElementValues(int vocabularyConceptId, List<DataElement> dataElementValues) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT ");
        sql.append("(VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE, LANGUAGE, RELATED_CONCEPT_ID) ");
        sql.append("values (:vocabularyConceptId, :dataElementId, :elementValue, :language, :relatedConceptId)");

        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[dataElementValues.size()];

        for (int i = 0; i < batchValues.length; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("vocabularyConceptId", vocabularyConceptId);
            params.put("dataElementId", dataElementValues.get(i).getId());
            params.put("elementValue", dataElementValues.get(i).getAttributeValue());
            params.put("language", dataElementValues.get(i).getAttributeLanguage());
            params.put("relatedConceptId", dataElementValues.get(i).getRelatedConceptId());
            batchValues[i] = params;
        }

        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkoutVocabularyConceptDataElementValues(int newVocabularyFolderId) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT ");
        sql.append("(VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE, LANGUAGE, RELATED_CONCEPT_ID) ");
        sql.append("select con.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, v.ELEMENT_VALUE, v.LANGUAGE, v.RELATED_CONCEPT_ID ");
        sql.append("from VOCABULARY_CONCEPT_ELEMENT v ");
        sql.append("left join VOCABULARY_CONCEPT con on v.VOCABULARY_CONCEPT_ID = con.ORIGINAL_CONCEPT_ID  ");
        sql.append("where con.VOCABULARY_ID = :newVocabularyFolderId");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("newVocabularyFolderId", newVocabularyFolderId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);

        updateCheckedoutRelatedConceptIds(newVocabularyFolderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyVocabularyConceptDataElementValues(int oldVocabularyId, int newVocabularyId) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT ");
        sql.append("(VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE, LANGUAGE, RELATED_CONCEPT_ID) ");
        sql.append("select newc.VOCABULARY_CONCEPT_ID, vce.DATAELEM_ID, vce.ELEMENT_VALUE, vce.LANGUAGE, vce.RELATED_CONCEPT_ID ");
        sql.append("from VOCABULARY_CONCEPT_ELEMENT vce, ");
        sql.append("VOCABULARY_CONCEPT oldc, VOCABULARY_CONCEPT newc ");
        sql.append("where oldc.VOCABULARY_CONCEPT_ID=vce.VOCABULARY_CONCEPT_ID AND oldc.VOCABULARY_ID = :oldVocabularyFolderId ");
        sql.append("AND newc.VOCABULARY_ID = :newVocabularyFolderId AND newc.IDENTIFIER = oldc.IDENTIFIER ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("newVocabularyFolderId", newVocabularyId);
        parameters.put("oldVocabularyFolderId", oldVocabularyId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);

        updateCopiedRelatedConceptIds(oldVocabularyId, newVocabularyId);
    }

    /**
     * After copying localref type element IDs have to be changed.
     *
     * @param oldVocabularyId old vocabulary ID
     * @param newVocabularyId new vocabulary ID
     */
    private void updateCopiedRelatedConceptIds(int oldVocabularyId, int newVocabularyId) {
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE VOCABULARY_CONCEPT_ELEMENT ocev, VOCABULARY_CONCEPT oldc, VOCABULARY_CONCEPT newc, ");
        sql.append("VOCABULARY_CONCEPT relc ");
        sql.append("SET ocev.RELATED_CONCEPT_ID = newc.VOCABULARY_CONCEPT_ID ");
        sql.append("where ");
        sql.append("ocev.RELATED_CONCEPT_ID = oldc.VOCABULARY_CONCEPT_ID ");
        sql.append("and ocev.VOCABULARY_CONCEPT_ID = relc.VOCABULARY_CONCEPT_ID ");
        sql.append("and relc.VOCABULARY_ID = :newVocabularyId ");
        sql.append("and oldc.VOCABULARY_ID = :oldVocabularyId ");
        sql.append("and newc.VOCABULARY_ID = :newVocabularyId ");
        sql.append("and oldc.IDENTIFIER = newc.IDENTIFIER ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("newVocabularyId", newVocabularyId);
        parameters.put("oldVocabularyId", oldVocabularyId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean vocabularyHasElemendBinding(int vocabularyFolderId, int elementId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyId", vocabularyFolderId);
        parameters.put("elementId", elementId);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_ID) from VOCABULARY2ELEM ");
        sql.append("where DATAELEM_ID = :elementId and VOCABULARY_ID = :vocabularyId ");

        int result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), parameters, Integer.class);

        return result > 0;
    }

    /**
     * updates localref IDs to the new concept ones.
     *
     * @param newVocabularyId new vocabulary ID
     */
    private void updateCheckedoutRelatedConceptIds(int newVocabularyId) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE VOCABULARY_CONCEPT_ELEMENT cev, VOCABULARY_CONCEPT con1, VOCABULARY_CONCEPT con2, DATAELEM e ");
        sql.append("set cev.RELATED_CONCEPT_ID = con2.VOCABULARY_CONCEPT_ID ");
        sql.append("where cev.VOCABULARY_CONCEPT_ID = con1.VOCABULARY_CONCEPT_ID ");
        sql.append("and cev.RELATED_CONCEPT_ID = con2.ORIGINAL_CONCEPT_ID ");
        sql.append("and cev.DATAELEM_ID = e.DATAELEM_ID ");
        sql.append("and con1.VOCABULARY_ID = :newVocabularyFolderId ");
        sql.append("and con2.VOCABULARY_ID = :newVocabularyFolderId ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("newVocabularyFolderId", newVocabularyId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRelatedElements(int vocabularyConceptId) {
        String sql = "delete from VOCABULARY_CONCEPT_ELEMENT where RELATED_CONCEPT_ID = :relatedConceptId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("relatedConceptId", vocabularyConceptId);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public Map<String, List<String>> getDataElementAttributeValues(int elementId) {
        String sql
                = "select SHORT_NAME, VALUE from ATTRIBUTE, M_ATTRIBUTE"
                + " where DATAELEM_ID=:parentId and PARENT_TYPE='E' and ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID"
                + " order by SHORT_NAME, VALUE";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentId", elementId);

        final HashMap<String, List<String>> resultMap = new HashMap<String, List<String>>();
        getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String shortName = rs.getString("SHORT_NAME");
                String value = rs.getString("VALUE");
                List<String> values = resultMap.get(shortName);
                if (values == null) {
                    values = new ArrayList<String>();
                    resultMap.put(shortName, values);
                }
                values.add(value);
            }
        });

        return resultMap;
    }

    @Override
    public List<DataElement> getDataSetElements(int datasetId) {
        StringBuilder sb = new StringBuilder();
        sb.append("select distinct de.* from DST2TBL dt, TBL2ELEM te, DATAELEM de WHERE dt.TABLE_ID = te.TABLE_ID ");
        sb.append("and te.DATAELEM_ID = de.DATAELEM_ID AND dt.DATASET_ID = :datasetId");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("datasetId", datasetId);

        List<DataElement> result = getNamedParameterJdbcTemplate().query(sb.toString(), params, new RowMapper<DataElement>() {

            @Override
            public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement de = new DataElement();
                de.setId(rs.getInt("de.DATAELEM_ID"));
                de.setShortName(rs.getString("de.SHORT_NAME"));
                de.setStatus(rs.getString("de.REG_STATUS"));
                de.setType(rs.getString("de.TYPE"));
                de.setModified(new Date(rs.getLong("de.DATE")));
                de.setWorkingUser(rs.getString("de.WORKING_USER"));
                de.setIdentifier(rs.getString("de.IDENTIFIER"));

                int parentNs = rs.getInt("de.PARENT_NS");
                if (parentNs > 0) {
                    de.setParentNamespace(parentNs);
                }
                de.setWorkingCopy(rs.getString("de.WORKING_COPY").equalsIgnoreCase("Y"));

                return de;
            }

        });

        for (DataElement elem : result) {
            List<FixedValue> fxvs = getFixedValues(elem.getId(), false);
            elem.setFixedValues(fxvs);
        }
        return result;
    }

    @Override
    public void bindVocabulary(int dataElementId, int vocabularyId) {
        String sql = "update DATAELEM set VOCABULARY_ID=:vocabularyId WHERE DATAELEM_ID=:dataElementId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyId", vocabularyId);
        params.put("dataElementId", dataElementId);

        getNamedParameterJdbcTemplate().update(sql, params);

    }

    @Override
    public List<DataElement> getVocabularySourceElements(List<Integer> vocabularyIds) {
        StringBuilder sql = new StringBuilder();
        sql.append("select  de.* from DATAELEM de WHERE de.VOCABULARY_ID IN(:vocabularyIds)");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyIds", vocabularyIds);

        List<DataElement> result = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<DataElement>() {

            @Override
            public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement de = new DataElement();
                de.setId(rs.getInt("de.DATAELEM_ID"));
                de.setShortName(rs.getString("de.SHORT_NAME"));
                de.setStatus(rs.getString("de.REG_STATUS"));
                de.setType(rs.getString("de.TYPE"));
                de.setModified(new Date(rs.getLong("de.DATE")));
                de.setWorkingUser(rs.getString("de.WORKING_USER"));
                de.setIdentifier(rs.getString("de.IDENTIFIER"));

                int parentNs = rs.getInt("de.PARENT_NS");
                if (parentNs > 0) {
                    de.setParentNamespace(parentNs);
                }

                return de;
            }

        });

        return result;

    }

    @Override
    public void moveVocabularySources(int originalVocabularyId, int vocabularyId) {

        String sql = "update DATAELEM set VOCABULARY_ID = :originalVocabularyId WHERE VOCABULARY_ID = :vocabularyId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyId", vocabularyId);
        params.put("originalVocabularyId", originalVocabularyId);

        getNamedParameterJdbcTemplate().update(sql, params);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IDataElementDAO#changeDataElemType(int, java.lang.String)
     */
    @Override
    public void changeDataElemType(int elemId, String newType) {

        String sql = "update DATAELEM set TYPE = :newType WHERE DATAELEM_ID = :elemId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("elemId", elemId);
        params.put("newType", newType);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IDataElementDAO#removeSimpleAttrsByShortName(int, java.lang.String[])
     */
    @Override
    public void removeSimpleAttrsByShortName(int elemId, String... attrShortNames) {

        if (attrShortNames == null || attrShortNames.length == 0) {
            return;
        }

        String sql
                = "delete from ATTRIBUTE where DATAELEM_ID = :elemId and PARENT_TYPE='E' and M_ATTRIBUTE_ID in ("
                + "select distinct M_ATTRIBUTE_ID from M_ATTRIBUTE where SHORT_NAME in (:names))";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("elemId", elemId);
        params.put("names", Arrays.asList(attrShortNames));

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void createInverseElements(int dataElementId, int conceptId, Integer newRelationalConceptId) {
        getJdbcTemplate().update("call CreateReverseLink(?, ?, ?)", dataElementId, conceptId, newRelationalConceptId);
    }

    @Override
    public void deleteReferringInverseElems(int conceptId, List<DataElement> dataElements) {
        for (DataElement elem : dataElements) {
            if (elem.getRelatedConceptId() != null) {
                deleteInverseElemsOfConcept(conceptId, elem);
            }
        }

    }

    @Override
    public void deleteInverseElemsOfConcept(int conceptId, DataElement dataElement) {
        getJdbcTemplate().update("call DeleteReverseLink(?, ?)", dataElement.getId(), conceptId);
    }

    @Override
    public List<DataElement> getPotentialReferringVocabularyConceptsElements() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.*, bu.base_uri, bu.vocabulary_id, bu.identifier FROM VOCABULARY_CONCEPT_ELEMENT AS v, ");
        sql.append("(SELECT base_uri, vocabulary_id, identifier FROM VOCABULARY ");
        sql.append("WHERE base_uri IS NOT NULL AND base_uri > '') AS bu, ");
        sql.append("DATAELEM AS d, ATTRIBUTE AS a, M_ATTRIBUTE AS ma ");
        sql.append("WHERE v.dataelem_id = d.dataelem_id AND d.dataelem_id = a.dataelem_id ");
        sql.append("AND ma.m_attribute_id = a.m_attribute_id AND v.related_concept_id IS NULL ");
        sql.append("AND v.element_value IS NOT NULL AND v.element_value LIKE CONCAT(bu.base_uri,'%') ");
        sql.append("AND d.PARENT_NS IS NULL AND a.value = 'reference' ");
        sql.append("GROUP BY v.id ");
        sql.append("HAVING COUNT(v.id) = 1 ");
        sql.append("ORDER BY v.id, v.dataelem_id ");

        List<DataElement> result
                = getNamedParameterJdbcTemplate().query(sql.toString(), new HashMap<String, Object>(), new RowMapper<DataElement>() {
                    @Override
                    public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                        DataElement de = new DataElement();
                        // id field of DataElement class is used to store Vocabulary_Concept_Element.ID column.
                        de.setId(rs.getInt("v.id"));
                        de.setVocabularyConceptId(rs.getInt("v.vocabulary_concept_id"));
                        de.setAttributeValue(rs.getString("v.element_value"));
                        de.setRelatedConceptBaseURI(rs.getString("bu.base_uri"));
                        de.setRelatedConceptVocabulary(rs.getString("bu.identifier"));
                        // !!! ATTENTION: related concept id field is used to store vocabulary id temporarily.
                        de.setRelatedConceptId(rs.getInt("bu.vocabulary_id"));
                        return de;
                    }
                });

        return result;
    } // end of method getPotentialReferringVocabularyConceptsElements

    @Override
    public void deleteReferringReferenceElems(int vocabularyId) {

        String sql
                = "delete vce.* FROM datadict.vocabulary_concept_element vce, VOCABULARY_CONCEPT vsource, VOCABULARY_CONCEPT, "
                + "VOCABULARY v " + "vtarget where vce.RELATED_CONCEPT_ID = :conceptId "
                + "AND vce.VOCABULARY_CONCEPT_ID = vsource.VOCABULARY_CONCEPT_ID  "
                + "AND vtarget.VOCABULARY_CONCEPT_ID = vce.RELATED_CONCEPT_ID  "
                + "AND vsource.VOCABULARY_ID <> vtarget.VOCABULARY_ID";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyId", vocabularyId);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public Collection<InferenceRule> getInferenceRules(DataElement parentElem) {
        StringBuilder sql = new StringBuilder("select * from INFERENCE_RULE where DATAELEM_ID = :dataelem_id");
        final DataElement source = getDataElement(parentElem.getId());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dataelem_id", parentElem.getId());

        List<InferenceRule> result = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<InferenceRule>() {

            @Override
            public InferenceRule mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement target = getDataElement(Integer.parseInt(rs.getString("TARGET_ELEM_ID")));
                InferenceRule rule = new InferenceRule(source, RuleType.fromName(rs.getString("RULE")), target);
                return rule;
            }

        });

        // add inverted rules for owl:inverseOf rule type as owl:inverseOf rule is bi-directional but is stored as a single row in the database
        sql = new StringBuilder("select * from INFERENCE_RULE where TARGET_ELEM_ID = :dataelem_id and RULE='owl:inverseOf'");
        List<InferenceRule> invertedRules = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<InferenceRule>() {
            @Override
            public InferenceRule mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement target = getDataElement(Integer.parseInt(rs.getString("DATAELEM_ID")));
                InferenceRule rule = new InferenceRule(source, RuleType.fromName(rs.getString("RULE")), target);
                return rule;
            }
        });

        for (InferenceRule invertedRule : invertedRules) {
            if (!result.contains(invertedRule)) {
                result.add(invertedRule);
            }
        }

        return result;
    }

    @Override
    public void createInferenceRule(InferenceRule rule) {
        StringBuilder sql = new StringBuilder("insert into INFERENCE_RULE (DATAELEM_ID, RULE, TARGET_ELEM_ID) values (:sourceElementId, :ruleName, :targetElementId)");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceElementId", rule.getSourceDElement().getId());
        params.put("ruleName", rule.getTypeName());
        params.put("targetElementId", rule.getTargetDElement().getId());

        getNamedParameterJdbcTemplate().update(sql.toString(), params);
    }

    @Override
    public void deleteInferenceRule(InferenceRule rule) {
        String sql;
        if (rule.getType() == RuleType.INVERSE) {
            sql = "delete from INFERENCE_RULE  where (DATAELEM_ID=:sourceElementId or DATAELEM_ID=:targetElementId) and RULE=:ruleName and (TARGET_ELEM_ID=:targetElementId or TARGET_ELEM_ID=:sourceElementId)";
        } else {
            sql = "delete from INFERENCE_RULE where DATAELEM_ID=:sourceElementId AND RULE=:ruleName and TARGET_ELEM_ID=:targetElementId";
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceElementId", rule.getSourceDElement().getId());
        params.put("ruleName", rule.getTypeName());
        params.put("targetElementId", rule.getTargetDElement().getId());

        getNamedParameterJdbcTemplate().update(sql.toString(), params);
    }

    @Override
    public boolean inferenceRuleExists(InferenceRule rule) {
        String sql;
        if (rule.getType() == RuleType.INVERSE) {
            sql = "select count(*) from INFERENCE_RULE where (DATAELEM_ID=:sourceElementId or DATAELEM_ID=:targetElementId) and RULE=:ruleName and (TARGET_ELEM_ID=:targetElementId or TARGET_ELEM_ID=:sourceElementId)";
        } else {
            sql = "select count(*) from INFERENCE_RULE where DATAELEM_ID=:sourceElementId and RULE=:ruleName and TARGET_ELEM_ID=:targetElementId";
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceElementId", rule.getSourceDElement().getId());
        params.put("ruleName", rule.getTypeName());
        params.put("targetElementId", rule.getTargetDElement().getId());

        int count = getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class);
        return (count > 0);
    }

    @Override
    public void updateInferenceRule(InferenceRule rule, InferenceRule newRule) {
        StringBuilder sql = new StringBuilder("update INFERENCE_RULE set RULE = :newRuleName, TARGET_ELEM_ID = :newTargetElementId where DATAELEM_ID = :sourceElementId AND RULE = :ruleName AND TARGET_ELEM_ID = :targetElementId");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceElementId", rule.getSourceDElement().getId());
        params.put("ruleName", rule.getTypeName());
        params.put("targetElementId", rule.getTargetDElement().getId());
        params.put("newRuleName", newRule.getTypeName());
        params.put("newTargetElementId", newRule.getTargetDElement().getId());

        getNamedParameterJdbcTemplate().update(sql.toString(), params);
    }

    @Override
    public Collection<DataElement> grepDataElement(String pattern) {
        String sql = "select DATAELEM_ID, SHORT_NAME from DATAELEM where SHORT_NAME like :pattern";

        MapSqlParameterSource params = new MapSqlParameterSource();
        String fullPattern = "%" + pattern + "%";
        params.addValue("pattern", fullPattern);

        List<DataElement> elements = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<DataElement>() {
            @Override
            public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement element = new DataElement();
                element.setId(Integer.parseInt(rs.getString("DATAELEM_ID")));
                element.setShortName(rs.getString("SHORT_NAME"));
                return element;
            }
        });
        return elements;
    }

    private void setParentNamespace(DataElement de, ResultSet rs, String columnName) throws SQLException {
        int parentNamespace = rs.getInt(columnName);

        if (rs.wasNull()) {
            de.setParentNamespace(null);
        } else {
            de.setParentNamespace(parentNamespace);
        }
    }

    @Override
    public void updateVocabularyConceptDataElementValue(int id, String value, String language, Integer relatedConceptId) {
        String sql = "update VOCABULARY_CONCEPT_ELEMENT SET ELEMENT_VALUE = :attributeValue, LANGUAGE= :language, "
                + "RELATED_CONCEPT_ID = :relatedConceptId where ID = :id";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeValue", value);
        params.put("relatedConceptId", relatedConceptId);
        params.put("language", language);

        params.put("id", id);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public VocabularyConceptBoundElementFilter getVocabularyConceptBoundElementFilter(int dataElementId, List<Integer> vocabularyConceptIds) {
        DataElement dataElement = getDataElement(dataElementId);
        if (dataElement == null) {
            return null;
        }

        final VocabularyConceptBoundElementFilter filter = new VocabularyConceptBoundElementFilter(dataElement);

        if (vocabularyConceptIds != null && !vocabularyConceptIds.isEmpty()) {
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("dataElementId", dataElementId);
            params.put("vocabularyConceptIds", vocabularyConceptIds);

            StringBuilder sql = new StringBuilder();
            sql.append("select * from (select distinct ELEMENT_VALUE as `key`, ELEMENT_VALUE as `value` from VOCABULARY_CONCEPT_ELEMENT where ELEMENT_VALUE is not null and DATAELEM_ID=:dataElementId and VOCABULARY_CONCEPT_ID in (:vocabularyConceptIds) ");
            sql.append("union all select distinct vce.RELATED_CONCEPT_ID, vc.LABEL from VOCABULARY_CONCEPT_ELEMENT vce, VOCABULARY_CONCEPT vc where vce.RELATED_CONCEPT_ID is not null and vce.RELATED_CONCEPT_ID = vc.VOCABULARY_CONCEPT_ID ");
            sql.append("and vce.DATAELEM_ID=:dataElementId and vce.VOCABULARY_CONCEPT_ID in (:vocabularyConceptIds)) as filters order by `value`");

            getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    filter.getOptions().put(rs.getString("key"), rs.getString("value"));
                }
            });
        }

        return filter;
    }

    @Override
    public void deleteVocabularyConceptDataElementValues(List<Integer> vocabularyConceptIds) {
        if (vocabularyConceptIds == null || vocabularyConceptIds.isEmpty()) {
            return;
        }

        String sql = "delete from VOCABULARY_CONCEPT_ELEMENT where VOCABULARY_CONCEPT_ID in (:vocabularyConceptIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyConceptIds", vocabularyConceptIds);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int[][] batchInsertVocabularyConceptDataElementValues(List<VocabularyConcept> vocabularyConcepts, int batchSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT ");
        sql.append("(VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE, LANGUAGE, RELATED_CONCEPT_ID) ");
        sql.append("values (?, ?, ?, ?, ?)");

        List<DataElement> allDataElements = new ArrayList<DataElement>();
        for (VocabularyConcept vocabularyConcept : vocabularyConcepts) {
            List<List<DataElement>> vocabularyConceptElements = vocabularyConcept.getElementAttributes();
            for (List<DataElement> elementMeta : vocabularyConceptElements) {
                for (DataElement element : elementMeta) {
                    element.setVocabularyConceptId(vocabularyConcept.getId());
                    allDataElements.add(element);
                }
            }
        }

        int[][] result = getJdbcTemplate().batchUpdate(sql.toString(), allDataElements, batchSize,
                new ParameterizedPreparedStatementSetter<DataElement>() {
            @Override
            public void setValues(PreparedStatement ps, DataElement element) throws SQLException {
                ps.setInt(1, element.getVocabularyConceptId());
                ps.setInt(2, element.getId());
                ps.setString(3, element.getAttributeValue());
                ps.setString(4, element.getAttributeLanguage());
                ps.setObject(5, element.getRelatedConceptId());
            }
        });

        return result;
    }

    @Override
    public int[][] batchCreateInverseElements(List<Triple<Integer, Integer, Integer>> relatedReferenceElements, int batchSize) {
        String sql = "call CreateReverseLink(?, ?, ?)";

        int[][] result = getJdbcTemplate().batchUpdate(sql, relatedReferenceElements, batchSize,
                new ParameterizedPreparedStatementSetter<Triple<Integer, Integer, Integer>>() {
            @Override
            public void setValues(PreparedStatement ps, Triple<Integer, Integer, Integer> triple) throws SQLException {
                ps.setInt(1, triple.getLeft());
                ps.setInt(2, triple.getCentral());
                ps.setInt(3, triple.getRight());
            }
        });

        return result;
    }

    @Override
    public Map<Integer, String> getDataElementDataTypes(Collection<Integer> dataElementIds) {
        StringBuilder sql = new StringBuilder();
        sql.append("select de.DATAELEM_ID, at.VALUE from DATAELEM de ");
        sql.append("left join ATTRIBUTE at on at.DATAELEM_ID = de.DATAELEM_ID ");
        sql.append("left join M_ATTRIBUTE ma on ma.M_ATTRIBUTE_ID = at.M_ATTRIBUTE_ID ");
        sql.append("where de.DATAELEM_ID in (:dataElementIds) and ma.NAME like :dataType ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dataElementIds", dataElementIds);
        params.put("dataType", "datatype");

        final Map<Integer, String> result = new HashMap<Integer, String>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                result.put(rs.getInt("de.DATAELEM_ID"), rs.getString("at.VALUE"));
            }
        });
        return result;
    }

    @Override
    public List<DataElement> getCommonDataElementsWorkingCopiesOf(String userName) {
        String sql = "select * from DATAELEM de where de.PARENT_NS is null and de.WORKING_COPY = 'Y' and de.WORKING_USER = :userName "
                + "order by de.IDENTIFIER asc, de.DATAELEM_ID desc";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userName", userName);

        List<DataElement> result = getNamedParameterJdbcTemplate().query(sql, parameters, new RowMapper<DataElement>() {
            @Override
            public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataElement de = new DataElement();
                de.setId(rs.getInt("de.DATAELEM_ID"));
                de.setIdentifier(rs.getString("de.IDENTIFIER"));
                de.setShortName(rs.getString("de.SHORT_NAME"));
                de.setStatus(rs.getString("de.REG_STATUS"));
                de.setType(rs.getString("de.TYPE"));
                de.setModified(new Date(rs.getLong("de.DATE")));
                de.setWorkingCopy(new BooleanToYesNoConverter().convertBack(rs.getString("de.WORKING_COPY")));
                de.setWorkingUser(rs.getString("de.WORKING_USER"));
                de.setDate(rs.getString("de.DATE"));
                setParentNamespace(de, rs, "de.PARENT_NS");
                de.setAllConceptsValid(rs.getBoolean("de.ALL_CONCEPTS_LEGAL"));
                de.setVocabularyId(rs.getInt("de.VOCABULARY_ID"));

                return de;
            }
        });
        return result;
    }

    @Override
    public List<Integer> getOrphanNonCommonDataElementIds() {
        String sql
                = "select distinct DATAELEM.DATAELEM_ID from DATAELEM left join TBL2ELEM on DATAELEM.DATAELEM_ID = TBL2ELEM.DATAELEM_ID "
                + "where DATAELEM.PARENT_NS is not null and TBL2ELEM.DATAELEM_ID is null";

        return getJdbcTemplate().queryForList(sql, Integer.class);
    }

    @Override
    public int delete(List<Integer> ids) {
        String sql = "delete from DATAELEM where DATAELEM_ID in (:ids)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void deleteRelatedConcepts(int dataElementId, Collection<Integer> relatedConceptIds) {
        if (relatedConceptIds == null || relatedConceptIds.isEmpty()) {
            return;
        }

        String sql = "delete from VOCABULARY_CONCEPT_ELEMENT where DATAELEM_ID = :dataElementId AND RELATED_CONCEPT_ID in (:relatedConceptIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dataElementId", dataElementId);
        params.put("relatedConceptIds", relatedConceptIds);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int[][] batchCreateVocabularyBoundElements(List<Pair<Integer, Integer>> vocabularyIdToDataElementId, int batchSize) {
        String sql = "insert ignore into VOCABULARY2ELEM (VOCABULARY_ID, DATAELEM_ID) values (?, ?)";

        int[][] result = getJdbcTemplate().batchUpdate(sql.toString(), vocabularyIdToDataElementId, batchSize,
                new ParameterizedPreparedStatementSetter<Pair<Integer, Integer>>() {
            @Override
            public void setValues(PreparedStatement ps, Pair<Integer, Integer> pair) throws SQLException {
                ps.setInt(1, pair.getLeft());
                ps.setInt(2, pair.getRight());
            }
        });
        return result;
    }

    @Override
    public int[][] batchCreateInverseRelations(List<Triple<Integer, Integer, Integer>> relatedReferenceElements, int batchSize) {
        String sql = "insert ignore into VOCABULARY_CONCEPT_ELEMENT (VOCABULARY_CONCEPT_ID, DATAELEM_ID, RELATED_CONCEPT_ID) VALUES (?, ?, ?)";

        int[][] result = getJdbcTemplate().batchUpdate(sql, relatedReferenceElements, batchSize,
                new ParameterizedPreparedStatementSetter<Triple<Integer, Integer, Integer>>() {
            @Override
            public void setValues(PreparedStatement ps, Triple<Integer, Integer, Integer> triple) throws SQLException {
                ps.setInt(1, triple.getLeft());
                ps.setInt(2, triple.getCentral());
                ps.setInt(3, triple.getRight());
            }
        });
        return result;
    }

    @Override
    public int getInverseElementID(int dataElementId) {
        String sql = "select GetInverseElemId(:elemId)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("elemId", dataElementId);
        Integer inverseElementId = getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class);
        return inverseElementId != null ? inverseElementId : 0;
    }

    @Override
    public Map<String, Integer> getMultipleCommonDataElementIds(List<String> identifiers) {
        Map<String, Integer> elementMap = new HashMap<String, Integer>();
        for (String identifier: identifiers){
            Integer elementID = this.getCommonDataElementId(identifier);
            if(elementID != null) {
                elementMap.put(identifier, elementID);
            }
        }
        return elementMap;
    }

    @Override
    public void removeVocabularyId(List<Integer> dataElementIds) {
        String sql = "update DATAELEM set VOCABULARY_ID=null WHERE DATAELEM_ID in (:dataElementIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dataElementIds", dataElementIds);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void changeMultipleDataElemType(List<Integer> dataElementIds, String newType) {

        String sql = "update DATAELEM set TYPE = :newType WHERE DATAELEM_ID in (:dataElementIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newType", newType);
        params.put("dataElementIds", dataElementIds);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public Boolean checkIfEntryExistsInVocabularyConceptElementById(Integer vocabularyConceptElementId) {
        String sql = "select count(*) from VOCABULARY_CONCEPT_ELEMENT vce where vce.ID = :vocabularyConceptElementId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyConceptElementId", vocabularyConceptElementId);

        Integer numberOfEntries = this.getNamedParameterJdbcTemplate().queryForObject(sql, parameters, Integer.class);

        return numberOfEntries > 0;
    }


}
