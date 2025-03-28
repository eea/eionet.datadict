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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.IFolderDAO;
import eionet.meta.dao.domain.Folder;

/**
 * Folder DAO implementation.
 *
 * @author Juhan Voolaid
 */
@Repository
public class FolderDAOImpl extends GeneralDAOImpl implements IFolderDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Folder> getFolders() {
        Map<String, Object> params = new HashMap<String, Object>();

        StringBuilder sql = new StringBuilder();
        sql.append("select ID, IDENTIFIER, LABEL ");
        sql.append("from VOCABULARY_SET ");
        sql.append("order by IDENTIFIER");

        List<Folder> items = getNamedParameterJdbcTemplate().query(sql.toString(), params, new FolderRowMapper());

        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createFolder(Folder folder) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_SET (IDENTIFIER, LABEL) ");
        sql.append("values (:identifier, :label)");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", folder.getIdentifier());
        parameters.put("label", folder.getLabel());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
        return getLastInsertId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFolderUnique(String identifier, int excludedId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", identifier);
        parameters.put("excludedId", excludedId);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(ID) from VOCABULARY_SET ");
        sql.append("where IDENTIFIER = :identifier and ID != :excludedId");

        int result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), parameters,Integer.class);

        return result == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Folder getFolder(int folderId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", folderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select ID, IDENTIFIER, LABEL ");
        sql.append("from VOCABULARY_SET ");
        sql.append("where ID=:id");

        Folder result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new FolderRowMapper());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFolderEmpty(int folderId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("folderId", folderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_ID) from VOCABULARY ");
        sql.append("where FOLDER_ID = :folderId ");

        int result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), parameters,Integer.class);

        return result == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFolder(int folderId) {
        String sql = "delete from VOCABULARY_SET where ID=:folderId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("folderId", folderId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFolder(Folder folder) {
        String sql = "update VOCABULARY_SET set IDENTIFIER = :identifier, LABEL = :label where ID = :folderId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("folderId", folder.getId());
        parameters.put("identifier", folder.getIdentifier());
        parameters.put("label", folder.getLabel());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Folder getFolderByIdentifier(String folderIdentifier) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("identifier", folderIdentifier);

        StringBuilder sql = new StringBuilder();
        sql.append("select ID, IDENTIFIER, LABEL ");
        sql.append("from VOCABULARY_SET ");
        sql.append("where IDENTIFIER=:identifier");

        Folder result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new FolderRowMapper());

        return result;
    }

    /**
     *
     * Map VOCABULARY_SET table fields to Folder object properties.
     *
     * @author Enriko Käsper
     */
    class FolderRowMapper implements RowMapper<Folder> {
        @Override
        public Folder mapRow(ResultSet rs, int rowNum) throws SQLException {
            Folder f = new Folder();
            f.setId(rs.getInt("ID"));
            f.setIdentifier(rs.getString("IDENTIFIER"));
            f.setLabel(rs.getString("LABEL"));
            return f;
        }
    }

}
