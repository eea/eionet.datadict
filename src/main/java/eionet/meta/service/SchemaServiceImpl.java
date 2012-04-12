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

package eionet.meta.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.ISchemaDAO;
import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;

/**
 * Schema service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
public class SchemaServiceImpl implements ISchemaService {

    /** The DAO for operations with schemas */
    @Autowired
    private ISchemaDAO schemaDAO;

    /** SchemaSet DAO. */
    @Autowired
    private ISchemaSetDAO schemaSetDAO;

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    public SchemaSetsResult searchSchemaSets(SchemaSetFilter searchFilter) throws ServiceException {
        try {
            return schemaSetDAO.searchSchemaSets(searchFilter);
        } catch (Exception e) {
            throw new ServiceException("Failed to search schema sets", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    public List<SchemaSet> getSchemaSets(boolean limited) throws ServiceException {
        try {
            return schemaSetDAO.getSchemaSets(limited);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema sets", e);
        }
    }

    /**
     *
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteSchemaSets(List<Integer> ids) throws ServiceException {
        try {
            schemaSetDAO.deleteAttributes(ids);
            schemaSetDAO.deleteSchemaSets(ids);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schema sets", e);
        }

    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    public SchemaSet getSchemaSet(int id) throws ServiceException {
        try {
            return schemaSetDAO.getSchemaSet(id);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema set", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    @Transactional
    public int addSchemaSet(SchemaSet schemaSet, String userName) throws ServiceException {
        schemaSet.setWorkingUser(userName);
        schemaSet.setUserModified(userName);
        try {
            return schemaSetDAO.createSchemaSet(schemaSet);
        } catch (Exception e) {
            throw new ServiceException("Failed to add schema set", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void updateSchemaSet(SchemaSet schemaSet, Map<Integer, Set<String>> attributes, String username)
    throws ServiceException {
        try {
            schemaSetDAO.updateSchemaSet(schemaSet);
            schemaSetDAO.updateSchemaSetAttributes(schemaSet.getId(), attributes);
        } catch (Exception e) {
            throw new ServiceException("Failed to update schema set", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void checkInSchemaSet(int schemaSetId, String username, String comment) throws ServiceException {
        if (StringUtils.isBlank(username)) {
            throw new ServiceException("Chack in failed. User name must not be blank.");
        }

        SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);

        if (!schemaSet.isWorkingCopy()) {
            throw new ServiceException("Chack in failed. Schema set is not a working copy.");
        }

        if (!StringUtils.equals(username, schemaSet.getWorkingUser())) {
            throw new ServiceException("Chack in failed. Check-in user is not the current working user.");
        }

        schemaSetDAO.checkInSchemaSet(schemaSet, username, comment);
    }

    /**
     * @param schemaSetDAO
     *            the schemaSetDAO to set
     */
    public void setSchemaSetDAO(ISchemaSetDAO schemaSetDAO) {
        this.schemaSetDAO = schemaSetDAO;
    }

    /**
     * @see eionet.meta.service.ISchemaService#addSchema(eionet.meta.dao.domain.Schema)
     */
    @Override
    @Transactional
    public int addSchema(Schema schema) throws ServiceException {

        try {
            return schemaDAO.createSchema(schema);
        } catch (Exception e) {
            throw new ServiceException("Failed to add schema", e);
        }
    }

    /**
     * @see eionet.meta.service.ISchemaService#listSchemaSetSchemas(int)
     */
    @Override
    public List<Schema> listSchemaSetSchemas(int schemaSetId) throws ServiceException {

        try {
            return schemaDAO.listForSchemaSet(schemaSetId);
        } catch (Exception e) {
            throw new ServiceException("Failed to list schemas of the given schema set", e);
        }
    }
}
