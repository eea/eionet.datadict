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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.ISchemaDAO;
import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.schemas.SchemaRepository;
import eionet.meta.service.data.SchemaFilter;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;
import eionet.meta.service.data.SchemasResult;
import eionet.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;

/**
 * Schema service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class SchemaServiceImpl implements ISchemaService {

    /** The DAO for operations with attributes. */
    @Autowired
    private IAttributeDAO attributeDAO;

    /** The DAO for operations with schemas. */
    @Autowired
    private ISchemaDAO schemaDAO;

    /** SchemaSet DAO. */
    @Autowired
    private ISchemaSetDAO schemaSetDAO;

    /** Schema repository. */
    @Autowired
    private SchemaRepository schemaRepository;
    
    /**
     * Logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(SchemaServiceImpl.class);

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
            throw new ServiceException("Failed to search schema sets: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    public SchemasResult searchSchemas(SchemaFilter searchFilter) throws ServiceException {
        try {
            return schemaDAO.searchSchemas(searchFilter);
        } catch (Exception e) {
            throw new ServiceException("Failed to search schemas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SchemaSet> getSchemaSets(String userName) throws ServiceException {
        try {
            return schemaSetDAO.getSchemaSets(userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema sets: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteSchemaSets(List<Integer> ids, String userName, boolean includingContents) throws ServiceException {
        doDeleteSchemaSets(ids, userName, includingContents);
    }

    /**
     * @param schemaSetIds
     * @param username
     * @param includingContents
     * @throws ServiceException
     */
    private void doDeleteSchemaSets(List<Integer> schemaSetIds, String username, boolean includingContents)
            throws ServiceException {
        try {
            // Validate permissions
            boolean deletePerm = username != null && SecurityUtil.hasPerm(username, "/schemasets", "d");
            if (!deletePerm) {
                throw new ValidationException("No delete permission!");
            }
            boolean deleteReleasedPerm = username != null && SecurityUtil.hasPerm(username, "/schemasets", "er");
            List<SchemaSet> schemaSets = schemaSetDAO.getSchemaSets(schemaSetIds);
            ensureDeleteAllowed(username, deleteReleasedPerm, schemaSets);

            // Delete schemas
            List<Integer> schemaIds = schemaDAO.getSchemaIds(schemaSetIds);
            doDeleteSchemas(schemaIds, null, includingContents);

            // Delete schema set folders, if requested so.
            if (includingContents) {
                for (SchemaSet schemaSet : schemaSets) {
                    schemaRepository.deleteSchemaSet(schemaSet.getIdentifier());
                }
            }

            // Delete schema sets
            attributeDAO.deleteAttributes(schemaSetIds, DElemAttribute.ParentType.SCHEMA_SET.toString());
            schemaSetDAO.deleteSchemaSets(schemaSetIds);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schema sets: " + e.getMessage(), e);
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
            } else if (schemaSet.getRegStatus().equals(RegStatus.RELEASED)) {
                if (!deleteReleasedPerm) {
                    throw new ValidationException("No permission to delete released schema set: " + schemaSet.getIdentifier());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteSchemas(List<Integer> ids, String userName, boolean includingContents) throws ServiceException {
        doDeleteSchemas(ids, userName, includingContents);
    }

    /**
     * Deletes schemas with given ids.
     *
     * @param ids
     * @param userName
     * @param includingContents
     * @throws ServiceException
     */
    private void doDeleteSchemas(List<Integer> ids, String userName, boolean includingContents) throws ServiceException {

        if (ids == null || ids.size() == 0) {
            return;
        }
        try {
            List<Schema> schemas = schemaDAO.getSchemas(ids);
            attributeDAO.deleteAttributes(ids, DElemAttribute.ParentType.SCHEMA.toString());
            schemaDAO.deleteSchemas(ids);

            // Delete files if requested so.
            if (includingContents) {
                for (Schema schema : schemas) {
                    schemaRepository.deleteSchema(schema.getFileName(), schema.getSchemaSetIdentifier());
                }
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schemas: " + e.getMessage(), e);
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
            throw new ServiceException("Failed to get schema set: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    public SchemaSet getSchemaSet(String identifier, boolean workingCopy) throws ServiceException {
        try {
            return schemaSetDAO.getSchemaSet(identifier, workingCopy);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema set: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int addSchemaSet(SchemaSet schemaSet, Map<Integer, Set<String>> attributes, String userName) throws ServiceException {
        schemaSet.setWorkingUser(userName);
        schemaSet.setUserModified(userName);
        try {
            int schemaSetId = schemaSetDAO.createSchemaSet(schemaSet);
            if (attributes != null && !attributes.isEmpty()) {
                schemaSetDAO.updateSchemaSetAttributes(schemaSetId, attributes);
            }
            return schemaSetId;
        } catch (Exception e) {
            throw new ServiceException("Failed to add schema set: " + e.getMessage(), e);
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
            if (attributes != null && !attributes.isEmpty()) {
                schemaSetDAO.updateSchemaSetAttributes(schemaSet.getId(), attributes);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to update schema set: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkInSchemaSet(int schemaSetId, String username, String comment) throws ServiceException {

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("User name must not be blank.");
        }

        try {
            LOGGER.info(String.format("Checking in schema set #%d for user %s with comment: %s.", schemaSetId, username, comment));
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

                    // Delete the checked-out copy, unlock it first.
                    schemaSetDAO.setWorkingUser(checkedOutCopyId, null);
                    deleteSchemaSets(Collections.singletonList(checkedOutCopyId), username, false);

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
                    schemaSetDAO.setWorkingUser(checkedOutCopyId, null);
                }
            }

            // Update the checked-in schema set.
            int finalId = isOverwrite ? checkedOutCopyId : schemaSetId;
            schemaSetDAO.checkIn(finalId, username, comment);

            // Finally, do necessary check-in actions in the repository too.
            List<String> schemasInDatabase = schemaSetDAO.getSchemaFileNames(schemaSet.getIdentifier());
            schemaRepository.checkInSchemaSet(schemaSet.getIdentifier(), schemasInDatabase);
            LOGGER.info(String.format("Schema set #%d has been checked in by user %s.", finalId, username));
            return finalId;
        } catch (Exception e) {
            throw new ServiceException("Schema set check-in failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkInSchema(int schemaId, String userName, String comment) throws ServiceException {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank.");
        }

        try {
            LOGGER.info(String.format("Checking in schema #%d for user %s with comment: %s.", schemaId, userName, comment));
            // Load the schema that is being checked in.
            Schema schema = schemaDAO.getSchema(schemaId);

            // Ensure that the schema is a working copy.
            if (!schema.isWorkingCopy()) {
                throw new ServiceException("This schema is not a working copy.");
            }

            // Ensure that the check-in user is the working user.
            if (!StringUtils.equals(userName, schema.getWorkingUser())) {
                throw new ServiceException("Check-in user is not the current working user.");
            }

            // Get checked-out copy id, see if we're overwriting it.
            int checkedOutCopyId = schema.getCheckedOutCopyId();
            boolean isOverwrite = false;
            if (checkedOutCopyId > 0) {
                Schema checkedOutCopy = schemaDAO.getSchema(checkedOutCopyId);
                isOverwrite = checkedOutCopy != null && checkedOutCopy.getFileName().equals(schema.getFileName());
                if (isOverwrite) {

                    // Delete the checked-out copy.
                    schemaDAO.unlock(checkedOutCopyId);
                    deleteSchemas(Collections.singletonList(checkedOutCopyId), userName, false);

                    // Checked-in schema must get the ID of the checked-out copy.
                    attributeDAO.replaceParentId(schemaId, checkedOutCopyId, DElemAttribute.ParentType.SCHEMA);
                    schemaDAO.replaceId(schemaId, checkedOutCopyId);
                } else {
                    // Unlock checked-out copy.
                    schemaDAO.unlock(checkedOutCopyId);
                }
            }

            // Update the checked-in schema.
            int finalId = isOverwrite ? checkedOutCopyId : schemaId;
            schemaDAO.checkIn(finalId, userName, comment);

            // Finally, do necessary check-in actions in the repository too.
            schemaRepository.checkInSchema(schema.getFileName());
            LOGGER.info(String.format("Schema #%d has been checked in by user %s.", finalId, userName));
            return finalId;
        } catch (Exception e) {
            throw new ServiceException("Schema check-in failed: " + e.getMessage(), e);
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
     * Replace schema identifier.
     *
     * @param replacedId
     *            previous identifier
     * @param substituteId
     *            new identifier
     */
    private void replaceSchemaId(int replacedId, int substituteId) {
        attributeDAO.replaceParentId(replacedId, substituteId, DElemAttribute.ParentType.SCHEMA);
        schemaDAO.replaceId(replacedId, substituteId);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = ServiceException.class)
    public int addSchema(Schema schema, Map<Integer, Set<String>> attributes) throws ServiceException {
        if (schema.getSchemaSetId() == 0) {
            schema.setRegStatus(RegStatus.DRAFT);
        }

        try {
            int schemaId = schemaDAO.createSchema(schema);
            if (attributes != null && !attributes.isEmpty()) {
                schemaDAO.updateSchemaAttributes(schemaId, attributes);
            }
            return schemaId;
        } catch (Exception e) {
            throw new ServiceException("Failed to add schema: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Schema> listSchemaSetSchemas(int schemaSetId) throws ServiceException {

        try {
            List<Schema> schemas = schemaDAO.listForSchemaSet(schemaSetId);
            for (Schema schema : schemas) {
                schema.setAttributeValues(attributeDAO.getAttributeValues(schema.getId(),
                        DElemAttribute.ParentType.SCHEMA.toString()));
            }
            return schemas;
        } catch (Exception e) {
            throw new ServiceException("Failed to list schemas of the given schema set: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkOutSchemaSet(int schemaSetId, String userName) throws ServiceException {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        try {
            LOGGER.info(String.format("Checking out schema set #%d for user %s.", schemaSetId, userName));
            SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);
            if (schemaSet.isWorkingCopy()) {
                throw new ServiceException("Cannot check out a working copy!");
            }

            if (StringUtils.isNotBlank(schemaSet.getWorkingUser())) {
                throw new ServiceException("Cannot check out an already checked-out schema set!");
            }

            // Do schema set check-out, get the new schema set's ID.
            schemaSetDAO.setWorkingUser(schemaSetId, userName);
            int newSchemaSetId = schemaSetDAO.copySchemaSetRow(schemaSetId, userName, null);

            // Copy the schema set's simple attributes.
            attributeDAO.copySimpleAttributes(schemaSetId, DElemAttribute.ParentType.SCHEMA_SET.toString(), newSchemaSetId);

            // Get the schema set's schemas and copy them and their simple attributes too.
            List<Schema> schemas = schemaDAO.listForSchemaSet(schemaSetId);
            LOGGER.info(String.format("Schemas for schema set #%d have been retrieved.",schemaSetId));
            for (Schema schema : schemas) {
                int newSchemaId = schemaDAO.copyToSchemaSet(schema.getId(), newSchemaSetId, schema.getFileName(), userName);
                attributeDAO.copySimpleAttributes(schema.getId(), DElemAttribute.ParentType.SCHEMA.toString(), newSchemaId);
            }

            // Make a working copy in repository too.
            schemaRepository.checkOutSchemaSet(schemaSet.getIdentifier());
            LOGGER.info(String.format("Schema set #%d has been checked out by user %s successfully.", schemaSet.getIdentifier(), userName));

            return newSchemaSetId;
        } catch (Exception e) {
            throw new ServiceException("Failed to check out schema set: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int undoCheckOutSchemaSet(int schemaSetId, String username) throws ServiceException {

        try {
            LOGGER.info(String.format("Undoing check out schema set #%d for user %s.", schemaSetId, username));
            int result = 0;
            SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);
            if (schemaSet != null) {

                if (!schemaSet.isWorkingCopyOf(username)) {
                    throw new ServiceException("Undo checkout can only be performed on your working copy!");
                }
                doDeleteSchemaSets(Collections.singletonList(schemaSetId), username, false);
                int checkedOutCopyId = schemaSet.getCheckedOutCopyId();
                if (checkedOutCopyId > 0) {
                    schemaSetDAO.setWorkingUser(checkedOutCopyId, null);
                    result = checkedOutCopyId;
                }
            }

            // Finally, do the necessary undo-checkout actions in repository too.
            List<String> schemasInDatabase = schemaSetDAO.getSchemaFileNames(schemaSet.getIdentifier());
            schemaRepository.undoCheckOutSchemaSet(schemaSet.getIdentifier(), schemasInDatabase);
            LOGGER.info(String.format("Undoing checking out for schema set #%d by user %s has been completed successfully.", schemaSetId, username));
            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to undo check-out of schema set: " + e.getMessage(), e);
        }
    }

    @Override
    public SchemaSet getWorkingCopyOfSchemaSet(int checkedOutCopyId) throws ServiceException {

        try {
            return schemaSetDAO.getWorkingCopyOfSchemaSet(checkedOutCopyId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get working copy of schema set: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SchemaSet> getSchemaSetWorkingCopiesOf(String userName) throws ServiceException {

        if (StringUtils.isBlank(userName)) {
            throw new ValidationException("User name must not be blank!");
        }

        try {
            return schemaSetDAO.getWorkingCopiesOf(userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema set working copies of user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Schema> getSchemaWorkingCopiesOf(String userName) throws ServiceException {

        if (StringUtils.isBlank(userName)) {
            throw new ValidationException("User name must not be blank!");
        }

        try {
            return schemaDAO.getWorkingCopiesOf(userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema working copies of user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Attribute> getSchemaSetAttributes() throws ServiceException {
        try {
            return attributeDAO.getAttributes(DElemAttribute.ParentType.SCHEMA_SET);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema set attributes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Attribute> getSchemaAttributes() throws ServiceException {
        try {
            return attributeDAO.getAttributes(DElemAttribute.ParentType.SCHEMA);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema attributes: " + e.getMessage(), e);
        }
    }

    @Override
    public Schema getSchema(int id) throws ServiceException {
        try {
            List<Schema> schemas = schemaDAO.getSchemas(Collections.singletonList(id));
            return schemas != null && !schemas.isEmpty() ? schemas.get(0) : null;
        } catch (Exception e) {
            throw new ServiceException("Failed to get a schema by this id: " + e.getMessage(), e);
        }
    }

    @Override
    public Schema getSchema(String schemaSetIdentifier, String schemaFileName, boolean workingCopy) throws ServiceException {
        try {
            return schemaDAO.getSchema(schemaSetIdentifier, schemaFileName, workingCopy);
        } catch (Exception e) {
            throw new ServiceException("Failed to get a schema: " + e.getMessage(), e);
        }
    }

    @Override
    public Schema getRootLevelSchema(String schemaFileName, boolean workingCopy) throws ServiceException {
        try {
            return schemaDAO.getRootLevelSchema(schemaFileName, workingCopy);
        } catch (Exception e) {
            throw new ServiceException("Failed to get a root level schema: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateSchema(Schema schema, Map<Integer, Set<String>> attributes, String username) throws ServiceException {

        try {
            schemaDAO.updateSchema(schema);
            if (attributes != null && !attributes.isEmpty()) {
                schemaDAO.updateSchemaAttributes(schema.getId(), attributes);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to update schema: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean schemaSetExists(String schemaSetIdentifier) throws ServiceException {

        try {
            return schemaSetDAO.exists(schemaSetIdentifier);
        } catch (Exception e) {
            throw new ServiceException("Failed to check if a schema set by this identifier already exists: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int checkOutSchema(int schemaId, String userName) throws ServiceException {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        try {
            LOGGER.info(String.format("Checking out schema #%d for user %s.", schemaId, userName));
            Schema schema = schemaDAO.getSchema(schemaId);
            if (schema.isWorkingCopy()) {
                throw new ServiceException("Cannot check out a working copy!");
            }

            if (StringUtils.isNotBlank(schema.getWorkingUser())) {
                throw new ServiceException("Cannot check out an already checked-out schema set!");
            }

            // Do schema check-out, get the new schema's ID.
            schemaDAO.setWorkingUser(schemaId, userName);
            int newSchemaId = schemaDAO.copySchemaRow(schemaId, userName, null, schema.getRegStatus());
            LOGGER.info(String.format("Schema #%d has new id: #%d.", schemaId, newSchemaId));

            // Copy the schema's simple attributes.
            attributeDAO.copySimpleAttributes(schemaId, DElemAttribute.ParentType.SCHEMA.toString(), newSchemaId);

            // Make a working copy in the repository too.
            schemaRepository.checkOutSchema(schema.getFileName());
            LOGGER.info(String.format("Schema #%d has been checked out successfully by user %s.", newSchemaId, userName));

            return newSchemaId;
        } catch (Exception e) {
            throw new ServiceException("Failed to perform check-out on this schema: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Schema> getRootLevelSchemas(String userName) throws ServiceException {

        try {
            return schemaDAO.getRootLevelSchemas(userName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schemas: " + e.getMessage(), e);
        }
    }

    @Override
    public Schema getWorkingCopyOfSchema(int schemaId) throws ServiceException {

        try {
            return schemaDAO.getWorkingCopyOfSchema(schemaId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get working copy of schema: " + e.getMessage(), e);
        }
    }

    @Override
    public int undoCheckOutSchema(int schemaId, String userName) throws ServiceException {

        try {
            LOGGER.info(String.format("Undoing check out schema #%d for user %s.", schemaId, userName));
            int result = 0;
            Schema schema = schemaDAO.getSchema(schemaId);
            if (schema != null) {

                if (!schema.isWorkingCopyOf(userName)) {
                    throw new ServiceException("Undo checkout can only be performed on your working copy!");
                }

                // TODO should supply username too? should delete contents too?
                doDeleteSchemas(Collections.singletonList(schemaId), null, false);

                int checkedOutCopyId = schema.getCheckedOutCopyId();
                if (checkedOutCopyId > 0) {
                    schemaDAO.unlock(checkedOutCopyId);
                    result = checkedOutCopyId;
                }
            }

            // Finally, do the necessary undo-checkout actions in repository too.
            List<String> schemasInDatabase = schemaSetDAO.getSchemaFileNames(null);
            schemaRepository.undoCheckOutSchema(schema.getFileName(), schemasInDatabase);
            LOGGER.info(String.format("Undoing check out schema #%d for user %s.", schemaId, userName));
            
            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to undo check-out of schema: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int copySchemaSet(int schemaSetId, String userName, String identifier) throws ServiceException {

        if (schemaSetId <= 0) {
            throw new IllegalArgumentException("Invalid schema set id: " + schemaSetId);
        }

        if (StringUtils.isBlank(userName) || StringUtils.isBlank(identifier)) {
            throw new IllegalArgumentException("User name and identifier must not be blank!");
        }

        try {
            SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);

            // Copy schema set row, get the new row's ID.
            int newSchemaSetId = schemaSetDAO.copySchemaSetRow(schemaSetId, userName, identifier);

            // reset the status of new schema set to DRAFT
            SchemaSet newSchemaSet = schemaSetDAO.getSchemaSet(newSchemaSetId);
            newSchemaSet.setRegStatus(RegStatus.DRAFT);
            newSchemaSet.setStatusModified(null);
            schemaSetDAO.updateSchemaSet(newSchemaSet);

            // Copy the schema set's simple attributes.
            attributeDAO.copySimpleAttributes(schemaSetId, DElemAttribute.ParentType.SCHEMA_SET.toString(), newSchemaSetId);

            // Get the schema set's schemas and copy them and their simple attributes too.
            List<Schema> schemas = schemaDAO.listForSchemaSet(schemaSetId);
            for (Schema schema : schemas) {
                int newSchemaId = schemaDAO.copyToSchemaSet(schema.getId(), newSchemaSetId, schema.getFileName(), userName);
                attributeDAO.copySimpleAttributes(schema.getId(), DElemAttribute.ParentType.SCHEMA.toString(), newSchemaId);
            }

            // Copy the schema set in repository too.
            schemaRepository.copySchemaSet(schemaSet.getIdentifier(), identifier);

            return newSchemaSetId;
        } catch (Exception e) {
            throw new ServiceException("Failed to copy schema set: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SchemaSet> getSchemaSetVersions(String userName, String continuityId, int... excludeIds) throws ServiceException {

        try {
            return schemaSetDAO.getSchemaSetVersions(userName, continuityId, excludeIds);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema set versions for continuity id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Schema> getSchemaVersions(String userName, String continuityId, int... excludeIds) throws ServiceException {
        try {
            return schemaDAO.getSchemaVersions(userName, continuityId, excludeIds);
        } catch (Exception e) {
            throw new ServiceException("Failed to get root-level schema versions for continuity id: " + e.getMessage(), e);
        }
    }

    @Override
    public int copySchema(int schemaId, String userName, FileBean newFile) throws ServiceException {

        if (schemaId <= 0) {
            throw new IllegalArgumentException("Invalid schema id: " + schemaId);
        }

        if (StringUtils.isBlank(userName) || newFile == null) {
            throw new IllegalArgumentException("User name and the new file must not be null or empty!");
        }

        try {
            Schema schema = schemaDAO.getSchema(schemaId);

            // Copy schema row, get the new row's ID.
            int newSchemaId = schemaDAO.copySchemaRow(schemaId, userName, newFile.getFileName(), schema.getRegStatus());

            // Copy the schema's simple attributes.
            attributeDAO.copySimpleAttributes(schemaId, DElemAttribute.ParentType.SCHEMA.toString(), newSchemaId);

            // Save the new file in schema repository.
            schemaRepository.addSchema(newFile, null, false);

            return newSchemaId;
        } catch (Exception e) {
            throw new ServiceException("Failed to copy schema row: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void copySchema(int schemaId, int schemaSetId, String userName, String newFileName) throws ServiceException {
        try {
            Schema schema = schemaDAO.getSchema(schemaId);
            SchemaSet schemaSet = schemaSetDAO.getSchemaSet(schemaSetId);

            // Copy schema to new schema set
            int newSchemaId = schemaDAO.copyToSchemaSet(schemaId, schemaSetId, newFileName, userName);

            // Copy the schema's simple attributes.
            attributeDAO.copySimpleAttributes(schemaId, DElemAttribute.ParentType.SCHEMA.toString(), newSchemaId);

            // Copy the file in schema repository.
            String srcSchemaSetIdentifier = schema.getSchemaSetIdentifier();
            boolean srcWorkingCopy = false;
            if (StringUtils.isEmpty(srcSchemaSetIdentifier)) {
                // root level schema
                srcWorkingCopy = schema.isWorkingCopy();
                srcSchemaSetIdentifier = null;
            } else {
                // schema from schema set
                SchemaSet srcSchemaSet = schemaSetDAO.getSchemaSet(schema.getSchemaSetId());
                srcWorkingCopy = srcSchemaSet.isWorkingCopy();
            }
            schemaRepository.copySchema(schema.getFileName(), srcSchemaSetIdentifier, srcWorkingCopy, newFileName,
                    schemaSet.getIdentifier(), schemaSet.isWorkingCopy());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to copy schema: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean schemaExists(String fileName, int schemaSetId) throws ServiceException {
        try {
            return schemaDAO.schemaExists(fileName, schemaSetId);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Schema> getSchemasForObligation(String obligationId, boolean releasedOnly) throws ServiceException {
        try {
            SchemaSetFilter schemasetFilter = new SchemaSetFilter();
            if (releasedOnly) {
                schemasetFilter.setRegStatuses(Arrays.asList(RegStatus.RELEASED.toString()));
            } else {
                schemasetFilter.setRegStatuses(RegStatus.getPublicStatusesForObligations());
            }
            
            Attribute rodAttribute = attributeDAO.getAttributeByName("obligation");
            if (rodAttribute != null) {
                rodAttribute.setValue(obligationId);
                schemasetFilter.setAttributes(Arrays.asList(new Attribute[] {rodAttribute}));
            }
            
            schemasetFilter.setUsePaging(false);

            // search schemasets
            SchemaSetsResult schemasetsResult = schemaSetDAO.searchSchemaSets(schemasetFilter);

            if (schemasetsResult != null && schemasetsResult.getList().size() > 0) {
                return schemaDAO.listForSchemaSets(schemasetsResult.getList());
            } else {
                return new ArrayList<Schema>();
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to search schemas for obligation: " + e.getMessage(), e);
        }
    }

}
