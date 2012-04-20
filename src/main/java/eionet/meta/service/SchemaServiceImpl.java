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
import eionet.util.SecurityUtil;

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
     * @see eionet.meta.service.ISchemaService#getSchemaSets(boolean)
     */
    @Override
    public List<SchemaSet> getSchemaSets(boolean releasedOnly) throws ServiceException {
        try {
            return schemaSetDAO.getSchemaSets(releasedOnly);
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
    public void deleteSchemaSets(List<Integer> ids, String username) throws ServiceException {
        doDeleteSchemaSets(ids, username);
    }

    /**
     * @param ids
     * @param username
     * @throws ServiceException
     */
    private void doDeleteSchemaSets(List<Integer> ids, String username) throws ServiceException {
        try {
            // Validate permissions
            boolean deletePerm = username != null && SecurityUtil.hasPerm(username, "/schemasets", "d");
            boolean deleteReleasedPerm = username != null && SecurityUtil.hasPerm(username, "/schemasets", "er");
            if (!deletePerm && !deleteReleasedPerm) {
                throw new ValidationException("No delete permission");
            }
            List<SchemaSet> schemaSets = schemaSetDAO.getSchemaSets(ids);
            ensureDeleteAllowed(username, deleteReleasedPerm, schemaSets);

            // Delete schemas
            List<Integer> schemaIds = schemaDAO.getSchemaIds(ids);
            doDeleteSchemas(schemaIds);

            // Delete schema set folders
            for (SchemaSet ss : schemaSets) {
                if (StringUtils.isNotEmpty(ss.getIdentifier())) {
                    String folder = SchemaRepository.REPO_PATH + "/" + ss.getIdentifier();
                    schemaRepository.delete(new File(folder));
                }
            }

            // Delete scmema sets
            attributeDAO.deleteAttributes(ids, DElemAttribute.ParentType.SCHEMA_SET.toString());
            schemaSetDAO.deleteSchemaSets(ids);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schema sets", e);
        }
    }

    /**
     * @param username
     * @param deleteReleasedPerm
     * @param schemaSets
     * @throws ValidationException
     */
    private void ensureDeleteAllowed(String username, boolean deleteReleasedPerm, List<SchemaSet> schemaSets)
    throws ValidationException {
        for (SchemaSet schemaSet : schemaSets) {
            if (schemaSet.isCheckedOut()) {
                throw new ValidationException("Cannot delete a checked-out schema set: " + schemaSet.getIdentifier());
            } else if (schemaSet.isWorkingCopy() && !StringUtils.equals(username, schemaSet.getWorkingUser())) {
                throw new ValidationException("Cannot delete another user's working copy: " + schemaSet.getIdentifier());
            } else if (schemaSet.getRegStatus().equals(SchemaSet.RegStatus.RELEASED)) {
                if (!deleteReleasedPerm) {
                    throw new ValidationException("No permission to delete released schema set: " + schemaSet.getIdentifier());
                }
            }
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
        doDeleteSchemas(ids);
    }

    /**
     * Deletes schemas with given ids.
     *
     * @param ids
     * @throws ServiceException
     */
    private void doDeleteSchemas(List<Integer> ids) throws ServiceException {
        if (ids == null || ids.size() == 0) {
            return;
        }
        try {
            List<Schema> schemas = schemaDAO.getSchemas(ids);
            attributeDAO.deleteAttributes(ids, DElemAttribute.ParentType.SCHEMA.toString());
            schemaDAO.deleteSchemas(ids);
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
            boolean isOverwrite = false;
            if (checkedOutCopyId > 0) {
                SchemaSet checkedOutCopy = schemaSetDAO.getSchemaSet(checkedOutCopyId);
                isOverwrite = checkedOutCopy != null && checkedOutCopy.getIdentifier().equals(schemaSet.getIdentifier());
                if (isOverwrite) {
                    // Remember id-mappings between the schemas of the two schema sets.
                    Map<Integer, Integer> schemaMappings = schemaSetDAO.getSchemaMappings(checkedOutCopyId, schemaSetId);

                    // Delete the checked-out copy.
                    schemaSetDAO.unlock(checkedOutCopyId);
                    deleteSchemaSets(Collections.singletonList(checkedOutCopyId), username);

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

    /**
     * @see eionet.meta.service.ISchemaService#undoCheckOutSchemaSet(int, java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int undoCheckOutSchemaSet(int schemaSetId, String username) throws ServiceException {

        try {
            int result = 0;
            SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);
            if (schemaSet != null) {
                doDeleteSchemaSets(Collections.singletonList(schemaSetId), username);
                int checkedOutCopyId = schemaSet.getCheckedOutCopyId();
                if (checkedOutCopyId > 0) {
                    schemaSetDAO.unlock(checkedOutCopyId);
                    result = checkedOutCopyId;
                }
            }

            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to undo check-out of schema set", e);
        }
    }

    /**
     * @see eionet.meta.service.ISchemaService#getWorkingCopyOfSchemaSet(int)
     */
    @Override
    public SchemaSet getWorkingCopyOfSchemaSet(int checkedOutCopyId) throws ServiceException {

        try {
            return schemaSetDAO.getWorkingCopyOfSchemaSet(checkedOutCopyId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get working copy of schema set " + checkedOutCopyId, e);
        }
    }

    /**
     * @throws ServiceException
     * @see eionet.meta.service.ISchemaService#getSchemaSetWorkingCopiesOf(java.lang.String)
     */
    @Override
    public List<SchemaSet> getSchemaSetWorkingCopiesOf(String userName) throws ServiceException {

        if (StringUtils.isBlank(userName)){
            throw new ValidationException("User name must not be blank!");
        }

        try {
            return schemaSetDAO.getWorkingCopiesOf(userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get working copies of user " + userName, e);
        }
    }
}
