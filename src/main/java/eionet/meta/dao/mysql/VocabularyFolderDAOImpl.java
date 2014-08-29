/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either exprevf or
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

import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.Triple;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vocabualary folder DAO.
 *
 * @author Juhan Voolaid
 */
@Repository
public class VocabularyFolderDAOImpl extends GeneralDAOImpl implements IVocabularyFolderDAO {

    @Override
    public List<VocabularyFolder> getReleasedVocabularyFolders(int folderId) {
        List<String> statuses = new ArrayList<String>();
        statuses.add(RegStatus.RELEASED.toString());
        statuses.add(RegStatus.PUBLIC_DRAFT.toString());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("folderId", folderId);
        params.put("statuses", statuses);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where v.WORKING_COPY=FALSE and v.FOLDER_ID=:folderId and v.REG_STATUS in (:statuses) ");
        sql.append("order by f.IDENTIFIER, v.IDENTIFIER ");

        List<VocabularyFolder> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getVocabularyFolders(String userName) {
        Map<String, Object> params = new HashMap<String, Object>();

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where ");
        if (StringUtils.isBlank(userName)) {
            sql.append("v.WORKING_COPY=FALSE ");
        } else {
            sql.append("(v.WORKING_COPY=FALSE or v.WORKING_USER=:workingUser) ");
            params.put("workingUser", userName);
        }
        sql.append("order by f.IDENTIFIER, v.IDENTIFIER ");

        List<VocabularyFolder> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getVocabularyFolders(int folderId, String userName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("folderId", folderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where ");
        if (StringUtils.isBlank(userName)) {
            sql.append("v.WORKING_COPY=FALSE ");
        } else {
            sql.append("(v.WORKING_COPY=FALSE or v.WORKING_USER=:workingUser) ");
            params.put("workingUser", userName);
        }
        sql.append("and v.FOLDER_ID=:folderId ");
        sql.append("order by f.IDENTIFIER, v.IDENTIFIER ");

        List<VocabularyFolder> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("continuityId", continuityId);
        params.put("vocabularyFolderId", vocabularyFolderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where v.CONTINUITY_ID = :continuityId and v.VOCABULARY_ID != :vocabularyFolderId ");
        if (StringUtils.isBlank(userName)) {
            sql.append("and v.WORKING_COPY=FALSE ");
        } else {
            sql.append("and (v.WORKING_COPY=FALSE or v.WORKING_USER=:workingUser) ");
            params.put("workingUser", userName);
        }

        List<VocabularyFolder> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyFolder> getWorkingCopies(String userName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("workingUser", userName);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where v.WORKING_USER=:workingUser and v.WORKING_COPY = 1");

        List<VocabularyFolder> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderId(rs.getInt("f.ID"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return items;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createVocabularyFolder(VocabularyFolder vocabularyFolder) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY (IDENTIFIER, LABEL, REG_STATUS, CONTINUITY_ID, VOCABULARY_TYPE,");
        sql.append(" WORKING_COPY, WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID,");
        sql.append(" CONCEPT_IDENTIFIER_NUMERIC, BASE_URI, FOLDER_ID, NOTATIONS_EQUAL_IDENTIFIERS)");
        sql.append(" values (:identifier, :label, :regStatus, :continuityId, :vocabularyType, :workingCopy, :workingUser, now(),");
        sql.append(" :userModified, :checkedOutCopyId, :numericConceptIdentifiers, :baseUri, :folderId, :enforceNotationToId)");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", vocabularyFolder.getIdentifier());
        parameters.put("label", vocabularyFolder.getLabel());
        parameters.put("regStatus", vocabularyFolder.getRegStatus().toString());
        parameters.put("continuityId", vocabularyFolder.getContinuityId());
        parameters.put("vocabularyType", vocabularyFolder.getType().name());
        parameters.put("workingCopy", vocabularyFolder.isWorkingCopy());
        parameters.put("workingUser", vocabularyFolder.getWorkingUser());
        parameters.put("userModified", vocabularyFolder.getUserModified());
        parameters.put("checkedOutCopyId",
                vocabularyFolder.getCheckedOutCopyId() <= 0 ? null : vocabularyFolder.getCheckedOutCopyId());
        parameters.put("numericConceptIdentifiers", vocabularyFolder.isNumericConceptIdentifiers());
        parameters.put("baseUri", vocabularyFolder.getBaseUri());
        parameters.put("folderId", vocabularyFolder.getFolderId());
        parameters.put("enforceNotationToId", vocabularyFolder.isNotationsEqualIdentifiers());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
        return getLastInsertId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("identifier", identifier);
        params.put("workingCopy", workingCopy);
        params.put("folderIdentifier", folderName);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where v.IDENTIFIER=:identifier and v.WORKING_COPY=:workingCopy and f.IDENTIFIER=:folderIdentifier");

        VocabularyFolder result =
                getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyFolder(VocabularyFolder vocabularyFolder) {
        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY set");
        sql.append(" IDENTIFIER = :identifier, LABEL = :label, REG_STATUS = :regStatus, CHECKEDOUT_COPY_ID = :checkedOutCopyId,");
        sql.append(" WORKING_COPY = :workingCopy, WORKING_USER = :workingUser, DATE_MODIFIED = :dateModified,");
        sql.append(" USER_MODIFIED = :userModified, CONCEPT_IDENTIFIER_NUMERIC = :numericConceptIdentifiers, BASE_URI = :baseUri,");
        sql.append(" FOLDER_ID = :folderId, NOTATIONS_EQUAL_IDENTIFIERS=:enforceNotationId");
        sql.append(" where VOCABULARY_ID = :vocabularyFolderId");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyFolderId", vocabularyFolder.getId());
        parameters.put("identifier", vocabularyFolder.getIdentifier());
        parameters.put("label", vocabularyFolder.getLabel());
        parameters.put("regStatus", vocabularyFolder.getRegStatus().toString());
        parameters.put("workingCopy", vocabularyFolder.isWorkingCopy());
        parameters.put("workingUser", vocabularyFolder.getWorkingUser());
        parameters.put("userModified", vocabularyFolder.getUserModified());
        parameters.put("checkedOutCopyId",
                vocabularyFolder.getCheckedOutCopyId() <= 0 ? null : vocabularyFolder.getCheckedOutCopyId());
        parameters.put("numericConceptIdentifiers", vocabularyFolder.isNumericConceptIdentifiers());
        parameters.put("baseUri", vocabularyFolder.getBaseUri());
        parameters.put("folderId", vocabularyFolder.getFolderId());
        parameters.put("dateModified", vocabularyFolder.getDateModified());
        parameters.put("enforceNotationId", vocabularyFolder.isNotationsEqualIdentifiers());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyFolders(List<Integer> ids, boolean keepRelatedValues) {
        String sql = "delete from VOCABULARY where VOCABULARY_ID in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyFolder getVocabularyFolder(int vocabularyFolderId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", vocabularyFolderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where VOCABULARY_ID=:id");

        VocabularyFolder result =
                getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("checkedOutCopyId", checkedOutCopyId);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where v.CHECKEDOUT_COPY_ID=:checkedOutCopyId");

        VocabularyFolder result =
                getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", identifier);
        parameters.put("folderId", folderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_ID) from VOCABULARY ");
        sql.append("where IDENTIFIER = :identifier and FOLDER_ID = :folderId ");
        if (excludedVocabularyFolderIds != null && excludedVocabularyFolderIds.length > 0) {
            sql.append("and VOCABULARY_ID not in (:excludedVocabularyFolderIds)");

            List<Integer> excluded = new ArrayList<Integer>();
            for (int i : excludedVocabularyFolderIds) {
                excluded.add(i);
            }
            parameters.put("excludedVocabularyFolderIds", excluded);
        }

        int result = getNamedParameterJdbcTemplate().queryForInt(sql.toString(), parameters);

        return result == 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IVocabularyFolderDAO#forceNotationsToIdentifiers(int)
     */
    @Override
    public int forceNotationsToIdentifiers(int vocabularyId) {
        String sql = "update VOCABULARY_CONCEPT set NOTATION=IDENTIFIER where VOCABULARY_ID=:vocId";
        return getNamedParameterJdbcTemplate().update(sql, Collections.singletonMap("vocId", vocabularyId));
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IVocabularyFolderDAO#getVocabularyFolderOfConcept(int)
     */
    @Override
    public VocabularyFolder getVocabularyFolderOfConcept(int conceptId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("conceptId", conceptId);

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID ");
        sql.append("where VOCABULARY_ID=");
        sql.append("(select VOCABULARY_ID from VOCABULARY_CONCEPT where VOCABULARY_CONCEPT_ID=:conceptId)");

        VocabularyFolder result =
                getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setFolderId(rs.getShort("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        return result;
    }

    @Override
    public List<Triple<String, String, Integer>> getVocabularyFolderBoundElementsMeta(int vocabularyFolderId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT max(c) as ELEM_COUNT, IDENTIFIER, ELEM_LANG FROM (");
        sql.append("SELECT DISTINCT count(DATAELEM_ID) AS C, VOCABULARY_CONCEPT_ID, DATAELEM.IDENTIFIER, ");
        sql.append("vce.LANGUAGE AS ELEM_LANG ");
        sql.append("from VOCABULARY_CONCEPT JOIN VOCABULARY_CONCEPT_ELEMENT vce USING(VOCABULARY_CONCEPT_ID) ");
        sql.append("JOIN DATAELEM USING(DATAELEM_ID) ").append("where VOCABULARY_CONCEPT.VOCABULARY_ID=:folderId ");
        sql.append("GROUP BY VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEM_LANG ");
        sql.append("ORDER BY VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEM_LANG");
        sql.append(") q GROUP BY IDENTIFIER, ELEM_LANG ORDER BY IDENTIFIER, ELEM_LANG ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("folderId", vocabularyFolderId);

        List<Triple<String, String, Integer>> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<Triple<String, String, Integer>>() {
                    @Override
                    public Triple<String, String, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        String elementName = rs.getString("IDENTIFIER");
                        String elementLanguage = StringUtils.trimToNull(rs.getString("ELEM_LANG"));
                        Integer elementMaxCount = rs.getInt("ELEM_COUNT");
                        Triple<String, String, Integer> pair =
                                new Triple<String, String, Integer>(elementName, elementLanguage, elementMaxCount);
                        return pair;
                    }
                });

        return items;
    }

    @Override
    public VocabularyResult searchVocabularies(VocabularyFilter filter) {
        Map<String, Object> params = new HashMap<String, Object>();

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, f.ID, f.IDENTIFIER, f.LABEL ");
        sql.append("from VOCABULARY v ");

        sql.append("left join VOCABULARY_SET f on f.ID=v.FOLDER_ID where 1=1 ");

        if (StringUtils.isNotEmpty(filter.getText())) {
            if (filter.isWordMatch()) {
                params.put("text", "[[:<:]]" + filter.getText() + "[[:>:]]");
                sql.append("AND (v.LABEL REGEXP :text ");
                sql.append("or v.IDENTIFIER REGEXP :text) ");

            } else if (filter.isExactMatch()) {
                params.put("text", filter.getText());
                sql.append("AND (v.LABEL = :text ");
                sql.append("or v.IDENTIFIER = :text) ");

            } else {
                params.put("text", "%" + filter.getText() + "%");
                sql.append("AND (v.LABEL like :text ");
                sql.append("or v.IDENTIFIER like :text) ");
            }
        } else if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            params.put("identifier", filter.getIdentifier());
            sql.append("AND v.IDENTIFIER like :identifier ");
        }

        if (filter.isWorkingCopy() != null) {
            params.put("workingCopy", filter.isWorkingCopy() ? 1 : 0);
            sql.append("AND WORKING_COPY = :workingCopy");
        }

        if (filter.getStatus() != null) {
            params.put("regStatus", filter.getStatus().getLabel());
            sql.append("AND REG_STATUS = :regStatus");
        }

        // related concepts text:
        if (StringUtils.isNotEmpty(filter.getConceptText())) {
            if (filter.isWordMatch()) {
                params.put("text", "[[:<:]]" + filter.getConceptText() + "[[:>:]]");
                sql.append(" AND EXISTS (SELECT 1 FROM VOCABULARY_CONCEPT vc WHERE vc.VOCABULARY_ID = v.VOCABULARY_ID ");
                sql.append(" AND (vc.LABEL REGEXP :conceptText OR vc.IDENTIFIER REGEXP :conceptText ");
                sql.append(" OR vc.DEFINITION REGEXP :conceptText)) ");
            } else if (filter.isExactMatch()) {
                params.put("conceptText", filter.getConceptText());
                sql.append(" AND EXISTS (SELECT 1 FROM VOCABULARY_CONCEPT vc WHERE vc.VOCABULARY_ID = v.VOCABULARY_ID ");
                sql.append(" AND (vc.LABEL = :conceptText OR vc.IDENTIFIER = :conceptText OR vc.DEFINITION = :conceptText)) ");
            } else {
                params.put("conceptText", "%" + filter.getConceptText() + "%");
                sql.append(" AND EXISTS (SELECT 1 FROM VOCABULARY_CONCEPT vc WHERE vc.VOCABULARY_ID = v.VOCABULARY_ID ");
                sql.append(" AND (vc.LABEL like :conceptText ");
                sql.append(" OR vc.IDENTIFIER like :conceptText OR vc.DEFINITION like :conceptText) ) ");
            }
        }

        if (StringUtils.isNotBlank(filter.getBaseUri())) {
            params.put("baseUri", filter.getBaseUri());
            sql.append(" AND v.BASE_URI like :baseUri ");
        }
        sql.append(" ORDER BY v.IDENTIFIER");

        List<VocabularyFolder> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setFolderId(rs.getInt("f.ID"));
                        vf.setFolderName(rs.getString("f.IDENTIFIER"));
                        vf.setFolderLabel(rs.getString("f.LABEL"));
                        return vf;
                    }
                });

        String totalSql = "SELECT FOUND_ROWS()";
        int totalItems = getJdbcTemplate().queryForInt(totalSql);

        VocabularyResult result = new VocabularyResult(items, totalItems, filter);

        return result;
    }

    @Override
    public void updateRelatedConceptValueToUri(List<Integer> vocabularyIds) {
        String sql =
                "UPDATE VOCABULARY_CONCEPT_ELEMENT ce, VOCABULARY v, VOCABULARY_CONCEPT c "
                        + "SET ce.ELEMENT_VALUE = concat(v.BASE_URI, c.IDENTIFIER), ce.RELATED_CONCEPT_ID = null "
                        + "WHERE c.VOCABULARY_ID = v.VOCABULARY_ID AND v.VOCABULARY_ID IN (:vocabularyIds) "
                        + "and ce.RELATED_CONCEPT_ID = c.VOCABULARY_CONCEPT_ID";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyIds", vocabularyIds);

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    @Override
    public void updateRelatedConceptValueToId(DataElement element) {
        String sql =
                "UPDATE VOCABULARY_CONCEPT_ELEMENT ce SET ce.ELEMENT_VALUE = null, ce.RELATED_CONCEPT_ID = :relatedConceptId "
                        + "WHERE ce.ID = :vocabularyConceptElementId ";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("relatedConceptId", element.getRelatedConceptId());
        parameters.put("vocabularyConceptElementId", element.getId());

        getNamedParameterJdbcTemplate().update(sql, parameters);
    } // end of method updateRelatedConceptValueToId

    @Override
    public boolean vocabularyHasBaseUri(List<Integer> ids) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyIds", ids);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(1) from VOCABULARY ");
        sql.append("where BASE_URI IS NOT NULL and VOCABULARY_ID IN (:vocabularyIds) ");

        int result = getNamedParameterJdbcTemplate().queryForInt(sql.toString(), parameters);

        return result > 0;
    }

    @Override
    public int populateEmptyBaseUris(String prefix) {
        String sql =
                "UPDATE VOCABULARY AS v INNER JOIN VOCABULARY_SET AS vs ON v.FOLDER_ID = vs.ID "
                        + "SET v.BASE_URI = CONCAT(:sitePrefix, vs.IDENTIFIER, :separator, v.IDENTIFIER, :separator) "
                        + "WHERE v.BASE_URI IS NULL OR v.BASE_URI = ''";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("sitePrefix", prefix);
        parameters.put("separator", "/");

        return getNamedParameterJdbcTemplate().update(sql, parameters);
    } // end of method populateEmptyBaseUris

    @Override
    public int changeSitePrefix(String oldSitePrefix, String newSitePrefix) {
        String sql =
                "UPDATE VOCABULARY AS v SET v.BASE_URI = REPLACE(v.BASE_URI, :oldSitePrefix, :newSitePrefix) "
                        + "WHERE v.BASE_URI IS NOT NULL AND v.BASE_URI LIKE :oldSitePrefixLike";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("oldSitePrefix", oldSitePrefix);
        parameters.put("newSitePrefix", newSitePrefix);
        parameters.put("oldSitePrefixLike", oldSitePrefix + "%");

        return getNamedParameterJdbcTemplate().update(sql, parameters);
    } // end of method changeSitePrefix

    @Override
    public List<VocabularyFolder> getRecentlyReleasedVocabularyFolders(int limit) {

        StringBuilder sql = new StringBuilder();
        sql.append("select v.VOCABULARY_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
        sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, ");
        sql.append("v.CONCEPT_IDENTIFIER_NUMERIC, v.NOTATIONS_EQUAL_IDENTIFIERS ");
        sql.append("from VOCABULARY v ");
        sql.append("where v.WORKING_COPY=FALSE and v.REG_STATUS like :releasedStatus ");
        sql.append("order by v.DATE_MODIFIED desc, v.IDENTIFIER asc ");
        sql.append("limit :limit");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("releasedStatus", RegStatus.RELEASED.toString());
        params.put("limit", limit);

        List<VocabularyFolder> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                    @Override
                    public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyFolder vf = new VocabularyFolder();
                        vf.setId(rs.getInt("v.VOCABULARY_ID"));
                        vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                        vf.setLabel(rs.getString("v.LABEL"));
                        vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                        vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                        vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                        vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                        vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                        vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                        vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                        vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                        vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                        vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                        vf.setBaseUri(rs.getString("v.BASE_URI"));
                        return vf;
                    }
                });

        return items;

    } //end of method getRecentlyReleasedVocabularyFolders
}
