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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.ISchemaDAO;
import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.schemas.SchemaRepository;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;

/**
 * Schema service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
public class SchemaServiceImpl implements ISchemaService {

    /** The DAO for operations with attributes */
    @Autowired
    private IAttributeDAO attributeDAO;

    /** The DAO for operations with schemas */
    @Autowired
    private ISchemaDAO schemaDAO;

    /** SchemaSet DAO. */
    @Autowired
    private ISchemaSetDAO schemaSetDAO;

    /** Schema repository. */
    @Autowired
    private SchemaRepository schemaRepository;

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
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteSchemaSets(List<Integer> ids) throws ServiceException {
        try {
            attributeDAO.deleteAttributes(ids, DElemAttribute.ParentType.SCHEMA_SET.toString());
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
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteSchemas(List<Integer> ids) throws ServiceException {
        try {
            List<Schema> schemas = schemaDAO.getSchemas(ids);
            attributeDAO.deleteAttributes(ids, DElemAttribute.ParentType.SCHEMA.toString());
            schemaSetDAO.deleteSchemas(ids);
            // Delete files
            for (Schema schema : schemas) {
                String filePath = SchemaRepository.REPO_PATH + "/" + schema.getSchemaSetIdentifier() + "/" + schema.getFileName();
                schemaRepository.delete(new File(filePath));
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schemas", e);
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
    @Transactional(rollbackFor = ServiceException.class)
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
     * @see eionet.meta.service.ISchemaService#checkInSchemaSet(int, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkInSchemaSet(int schemaSetId, String username, String comment) throws ServiceException {

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("User name must not be blank.");
        }

        try {
            // Load schema set to check in.
            SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);

            // Ensure that the schema set is a working copy.
            if (!schemaSet.isWorkingCopy()) {
                throw new ServiceException("Schema set is not a working copy.");
            }

            // Ensure that the check-in user is the working user.
            if (!StringUtils.equals(username, schemaSet.getWorkingUser())) {
                throw new ServiceException("Check-in user is not the current working user.");
            }

            // Get checked-out copy id, see if we're overwriting it.
            int checkedOutCopyId = schemaSet.getCheckedOutCopyId();
            SchemaSet checkedOutCopy = schemaSetDAO.getSchemaSet(checkedOutCopyId);
            boolean isOverwrite = checkedOutCopy != null && checkedOutCopy.getIdentifier().equals(schemaSet.getIdentifier());
            if (isOverwrite) {
                // Remember id-mappings between the schemas of the two schema sets.
                Map<Integer, Integer> schemaMappings = schemaSetDAO.getSchemaMappings(checkedOutCopyId, schemaSetId);

                // Delete the checked-out copy.
                deleteSchemaSets(Collections.singletonList(checkedOutCopyId));

                // Schemas of the new schema set must get the ids of the schemas that were in the checked-out copy.
                for (Map.Entry<Integer, Integer> entry : schemaMappings.entrySet()) {
                    Integer substituteSchemaId = entry.getKey();
                    Integer replacedSchemaId = entry.getValue();
                    replaceSchemaId(replacedSchemaId, substituteSchemaId);
                }

                // Checked-in schema set must get the ID of the checked-out copy.
                replaceSchemaSetId(schemaSetId, checkedOutCopyId);
            } else {
                // Unlock checked-out copy.
                schemaSetDAO.unlock(checkedOutCopyId);
            }

            // Update the checked-in schema set.
            int finalId = isOverwrite ? checkedOutCopyId : schemaSetId;
            schemaSetDAO.checkIn(finalId, username, comment);
            return finalId;
        } catch (Exception e) {
            throw new ServiceException("Schema set check-in failed.", e);
        }
    }

    /**
     *
     * @param replacedId
     * @param substituteId
     */
    private void replaceSchemaSetId(int replacedId, int substituteId) {
        attributeDAO.replaceParentId(replacedId, substituteId, DElemAttribute.ParentType.SCHEMA_SET);
        schemaSetDAO.replaceId(replacedId, substituteId);
    }

    /**
     *
     * @param oldId
     * @param substituteId
     */
    private void replaceSchemaId(int replacedId, int substituteId) {
        attributeDAO.replaceParentId(replacedId, substituteId, DElemAttribute.ParentType.SCHEMA);
        schemaDAO.replaceId(replacedId, substituteId);
    }

    /**
     * @see eionet.meta.service.ISchemaService#addSchema(eionet.meta.dao.domain.Schema)
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
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

    /**
     * @see eionet.meta.service.ISchemaService#checkOutSchemaSet(int, java.lang.String, String)
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkOutSchemaSet(int schemaSetId, String username, String newIdentifier) throws ServiceException {

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        int newSchemaSetId;
        try {
            // Do schema set check-out, get the new schema set's ID.
            newSchemaSetId = schemaSetDAO.checkOutSchemaSet(schemaSetId, username, newIdentifier);

            // Copy the schema set's simple attributes.
            attributeDAO.copySimpleAttributes(schemaSetId, DElemAttribute.ParentType.SCHEMA_SET.toString(), newSchemaSetId);

            // Get the schema set's schemas and copy them and their simple attributes too.
            List<Schema> schemas = schemaDAO.listForSchemaSet(schemaSetId);
            for (Schema schema : schemas) {
                int newSchemaId = schemaDAO.copyToSchemaSet(schema.getId(), newSchemaSetId, schema.getFileName(), username);
                attributeDAO.copySimpleAttributes(schema.getId(), DElemAttribute.ParentType.SCHEMA.toString(), newSchemaId);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to check out schema set", e);
        }

        return newSchemaSetId;
    }

    // @Override
    // @Transactional(rollbackFor = ServiceException.class)
    // public void checkOutSchemaSet(int schemaSetId, String username, String comment) throws ServiceException {
    // if (StringUtils.isBlank(username)) {
    // throw new ServiceException("Chack in failed. User name must not be blank.");
    // }
    //
    // SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);
    //
    // if (!schemaSet.isWorkingCopy()) {
    // throw new ServiceException("Chack in failed. Schema set is not a working copy.");
    // }
    //
    // if (!StringUtils.equals(username, schemaSet.getWorkingUser())) {
    // throw new ServiceException("Chack in failed. Check-in user is not the current working user.");
    // }
    //
    // schemaSetDAO.checkInSchemaSet(schemaSet, username, comment);
    // }
}
