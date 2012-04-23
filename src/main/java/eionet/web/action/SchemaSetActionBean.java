package eionet.web.action;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.FixedValue;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.schemas.SchemaRepository;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
@UrlBinding("/schemaSet.action")
public class SchemaSetActionBean extends AbstractActionBean {

    /** */
    private static final String ADD_SCHEMA_SET_JSP = "/pages/schemaSets/addSchemaSet.jsp";
    private static final String VIEW_SCHEMA_SET_JSP = "/pages/schemaSets/viewSchemaSet.jsp";
    private static final String EDIT_SCHEMA_SET_JSP = "/pages/schemaSets/editSchemaSet.jsp";
    private static final String SCHEMA_SET_SCHEMAS_JSP = "/pages/schemaSets/schemaSetSchemas.jsp";

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** Schema repository. */
    @SpringBean
    private SchemaRepository schemaRepository;

    /** */
    private SchemaSet schemaSet;
    private LinkedHashMap<Integer, DElemAttribute> attributes;

    /** */
    private List<Schema> schemas;

    /** */
    private FileBean uploadedFile;

    /** Values of this schema set's attributes that can have multiple values. */
    private Map<String, Set<String>> multiValuedAttributeValues;

    /** Values of this schema set's attributes that can have only fixed values. */
    private Map<String, Set<String>> fixedValuedAttributeValues;

    /** This schema set's attribute values as submitted from the save form. */
    private Map<Integer, Set<String>> saveAttributeValues;

    /** Check-in comment. */
    private String comment;

    /** */
    private List<Integer> schemaIds;

    /** */
    private String newIdentifier;

    /**
     * View action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    @HandlesEvent(value = "view")
    public Resolution view() throws ServiceException {

        loadSchemaSet();

        // If checked out by me, redirect to my working copy
        if (isUserLoggedIn() && schemaSet.isCheckedOutBy(getUserName())) {
            SchemaSet workingCopy = schemaService.getWorkingCopyOfSchemaSet(schemaSet.getId());
            if (workingCopy == null) {
                throw new ServiceException("Failed to find working copy of schema set " + schemaSet.getId());
            } else {
                return new RedirectResolution(getClass()).addParameter("schemaSet.id", workingCopy.getId());
            }
        }

        return new ForwardResolution(VIEW_SCHEMA_SET_JSP);
    }

    /**
     * Edit action.
     *
     * @return
     * @throws ServiceException
     */
    @HandlesEvent(value = "edit")
    public Resolution edit() throws ServiceException {

        loadSchemaSet();
        return new ForwardResolution(EDIT_SCHEMA_SET_JSP);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution editSchemas() throws ServiceException {

        loadSchemaSet();
        return new ForwardResolution(SCHEMA_SET_SCHEMAS_JSP);
    }

    /**
     * Add action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution add() throws ServiceException {

        Resolution resolution = new ForwardResolution(ADD_SCHEMA_SET_JSP);
        if (!isGetOrHeadRequest()) {
            int schemaSetId = schemaService.addSchemaSet(schemaSet, getUserName());
            resolution = new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSetId);
            addSystemMessage("Working copy successfully created!");
        }
        return resolution;
    }

    /**
     *
     * @throws DAOException
     */
    @ValidationMethod(on = {"add", "save", "saveAndClose"})
    public void validateAdd() throws DAOException {

        if (isGetOrHeadRequest()) {
            return;
        }

        if (StringUtils.equals(getContext().getEventName(), "add")) {
            if (schemaSet == null || StringUtils.isBlank(schemaSet.getIdentifier())) {
                addGlobalValidationError("Identifier is missing!");
            }
        }

        LinkedHashMap<Integer, DElemAttribute> attributesMap = getAttributes();
        for (DElemAttribute attribute : attributesMap.values()) {

            if (attribute.isMandatory()) {
                Integer attrId = Integer.valueOf(attribute.getID());
                Set<String> attrValues = getSaveAttributeValues().get(attrId);
                if (attrValues == null || attrValues.isEmpty() || StringUtils.isBlank(attrValues.iterator().next())) {
                    addGlobalValidationError(attribute.getShortName() + " is missing!");
                }
            }
        }
    }

    /**
     * Save action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution save() throws ServiceException {
        schemaService.updateSchemaSet(schemaSet, getSaveAttributeValues(), getUserName());
        return new ForwardResolution(EDIT_SCHEMA_SET_JSP);
    }

    /**
     * Save and close action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveAndClose() throws ServiceException {
        schemaService.updateSchemaSet(schemaSet, getSaveAttributeValues(), getUserName());
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSet.getId());
    }

    /**
     * Cancel action.
     *
     * @return
     * @throws DAOException
     */
    public Resolution cancel() throws DAOException {
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSet.getId());
    }

    /**
     * Check in action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution checkIn() throws ServiceException {
        int finalId = schemaService.checkInSchemaSet(schemaSet.getId(), getUserName(), comment);
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", finalId);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution checkOut() throws ServiceException {
        int newSchemaSetId = schemaService.checkOutSchemaSet(schemaSet.getId(), getUserName(), null);
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", newSchemaSetId);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution newVersion() throws ServiceException {

        int newSchemaSetId = schemaService.checkOutSchemaSet(schemaSet.getId(), getUserName(), newIdentifier);
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", newSchemaSetId);
    }

    /**
     *
     * @throws DAOException
     */
    @ValidationMethod(on = {"newVersion"})
    public void validateNewVersion() throws DAOException {

        if (StringUtils.isBlank(newIdentifier)) {
            addGlobalValidationError("New identifier is missing!");
            return;
        }

        getContext().setSourcePageResolution(new ForwardResolution(VIEW_SCHEMA_SET_JSP));
    }

    /**
     * Action for deleting the schema set.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution delete() throws ServiceException {
        schemaService.deleteSchemaSets(Collections.singletonList(schemaSet.getId()), getUserName());
        addSystemMessage("Schema set succesfully deleted.");
        return new RedirectResolution(BrowseSchemaSetsActionBean.class);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution undoCheckout() throws ServiceException {
        int checkedOutCopyId = schemaService.undoCheckOutSchemaSet(schemaSet.getId(), getUserName());
        if (checkedOutCopyId > 0) {
            return new RedirectResolution(getClass()).addParameter("schemaSet.id", checkedOutCopyId);
        } else {
            return new RedirectResolution(BrowseSchemaSetsActionBean.class);
        }
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution deleteSchemas() throws ServiceException {
        schemaService.deleteSchemas(schemaIds);

        if (schemaIds.size() == 1) {
            addSystemMessage("Schema succesfully deleted.");
        } else if (schemaIds.size() > 1) {
            addSystemMessage("Schemas succesfully deleted.");
        }

        return editSchemas();
    }

    /**
     * Loads schema set.
     *
     * @throws ServiceException
     * @throws IOException
     */
    public Resolution uploadSchema() throws ServiceException, IOException {

        // TODO overwrite flag should not be always true, it should come from user
        File schemaFile = schemaRepository.add(uploadedFile, schemaSet.getIdentifier(), true);

        try {

            Schema schema = new Schema();
            schema.setFileName(uploadedFile.getFileName());
            schema.setUserModified(getUserName());
            schema.setSchemaSetId(schemaSet.getId());
            schemaService.addSchema(schema);
        } catch (ServiceException e) {
            SchemaRepository.deleteQuietly(schemaFile);
            throw e;
        } catch (RuntimeException e) {
            SchemaRepository.deleteQuietly(schemaFile);
            throw e;
        }

        return editSchemas();
    }

    /**
     *
     * @throws ServiceException
     */
    private void loadSchemaSet() throws ServiceException {
        schemaSet = schemaService.getSchemaSet(schemaSet.getId());
    }

    /**
     * @param schemaService
     *            the schemaService to set
     */
    public void setSchemaService(ISchemaService schemaService) {
        this.schemaService = schemaService;
    }

    /**
     * @return the schemaSet
     */
    public SchemaSet getSchemaSet() {
        return schemaSet;
    }

    /**
     * @param schemaSet
     *            the schemaSet to set
     */
    public void setSchemaSet(SchemaSet schemaSet) {
        this.schemaSet = schemaSet;
    }

    /**
     *
     * @return
     */
    public boolean isUserWorkingCopy() {

        boolean result = false;
        String sessionUser = getUserName();
        if (!StringUtils.isBlank(sessionUser)) {
            if (schemaSet != null) {
                String workingUser = schemaSet.getWorkingUser();
                return schemaSet.isWorkingCopy() && StringUtils.equals(workingUser, sessionUser);
            }
        }

        return result;
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public List<Schema> getSchemas() throws ServiceException {

        if (schemas == null) {
            schemas = schemaService.listSchemaSetSchemas(schemaSet.getId());
        }
        return schemas;
    }

    /**
     * @return the attributes
     * @throws DAOException
     */
    public LinkedHashMap<Integer, DElemAttribute> getAttributes() throws DAOException {

        if (attributes == null) {
            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                int schemaSetId = schemaSet == null ? 0 : schemaSet.getId();
                attributes =
                    searchEngine.getObjectAttributes(schemaSetId, DElemAttribute.ParentType.SCHEMA_SET,
                            DElemAttribute.TYPE_SIMPLE);
            } finally {
                searchEngine.close();
            }
        }
        return attributes;
    }

    /**
     * @return the possibleAttributeValues
     * @throws DAOException
     */
    public Map<String, Set<String>> getMultiValuedAttributeValues() throws DAOException {

        if (multiValuedAttributeValues == null) {
            multiValuedAttributeValues = new HashMap<String, Set<String>>();
            LinkedHashMap<Integer, DElemAttribute> attributeMap = getAttributes();
            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                for (Map.Entry<Integer, DElemAttribute> entry : attributeMap.entrySet()) {

                    int attributeId = entry.getKey();
                    DElemAttribute attribute = entry.getValue();

                    if (attribute.isMultipleValuesAllowed()) {

                        Vector existingValues = attribute.getValues();
                        Collection possibleValues = null;
                        if (StringUtils.equals(attribute.getDisplayType(), "select")) {
                            possibleValues = getFixedValuedAttributeValues().get(attributeId);
                        } else {
                            possibleValues = searchEngine.getSimpleAttributeValues(String.valueOf(attributeId));
                        }

                        LinkedHashSet<String> values = new LinkedHashSet<String>();
                        if (existingValues != null) {
                            values.addAll(existingValues);
                        }
                        if (possibleValues != null) {
                            values.addAll(possibleValues);
                        }
                        multiValuedAttributeValues.put(String.valueOf(attributeId), values);
                    }
                }
            } catch (SQLException e) {
                throw new DAOException(e.getMessage(), e);
            } finally {
                searchEngine.close();
            }
        }
        return multiValuedAttributeValues;
    }

    /**
     * @return the fixedValuedAttributeValues
     * @throws DAOException
     */
    public Map<String, Set<String>> getFixedValuedAttributeValues() throws DAOException {

        if (fixedValuedAttributeValues == null) {
            fixedValuedAttributeValues = new HashMap<String, Set<String>>();
            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                for (Map.Entry<Integer, DElemAttribute> entry : getAttributes().entrySet()) {

                    int attributeId = entry.getKey();
                    DElemAttribute attribute = entry.getValue();

                    if (StringUtils.equals(attribute.getDisplayType(), "select")) {

                        LinkedHashSet<String> values = new LinkedHashSet<String>();
                        Collection<FixedValue> fixedValues = searchEngine.getFixedValues(attribute.getID(), "attr");
                        for (FixedValue fxv : fixedValues) {
                            String value = fxv.getValue();
                            if (value != null) {
                                values.add(value);
                            }
                        }
                        fixedValuedAttributeValues.put(String.valueOf(attributeId), values);
                    }
                }
            } catch (SQLException e) {
                throw new DAOException(e.getMessage(), e);
            } finally {
                searchEngine.close();
            }
        }
        return fixedValuedAttributeValues;
    }

    /**
     * @return the saveAttributeValues
     */
    private Map<Integer, Set<String>> getSaveAttributeValues() {

        if (saveAttributeValues == null) {

            saveAttributeValues = new HashMap<Integer, Set<String>>();

            HttpServletRequest request = getContext().getRequest();
            Enumeration paramNames = request.getParameterNames();
            if (paramNames != null && paramNames.hasMoreElements()) {
                do {
                    String paramName = paramNames.nextElement().toString();
                    Integer attributeId = null;
                    if (paramName.startsWith(DElemAttribute.REQUEST_PARAM_MULTI_PREFIX)) {
                        attributeId =
                            Integer.valueOf(StringUtils.substringAfter(paramName, DElemAttribute.REQUEST_PARAM_MULTI_PREFIX));
                    } else if (paramName.startsWith(DElemAttribute.REQUEST_PARAM_PREFIX)) {
                        attributeId = Integer.valueOf(StringUtils.substringAfter(paramName, DElemAttribute.REQUEST_PARAM_PREFIX));
                    }

                    if (attributeId != null) {
                        String[] paramValues = request.getParameterValues(paramName);
                        LinkedHashSet<String> valueSet = new LinkedHashSet<String>();
                        for (int i = 0; i < paramValues.length; i++) {
                            valueSet.add(paramValues[i]);
                        }
                        if (!valueSet.isEmpty()) {
                            saveAttributeValues.put(attributeId, valueSet);
                        }
                    }
                } while (paramNames.hasMoreElements());
            }
        }
        return saveAttributeValues;
    }

    /**
     * @return the uploadedFile
     */
    public FileBean getUploadedFile() {
        return uploadedFile;
    }

    /**
     * @param uploadedFile
     *            the uploadedFile to set
     */
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * @param schemaIds
     *            the schemaIds to set
     */
    public void setSchemaIds(List<Integer> schemaIds) {
        this.schemaIds = schemaIds;
    }

    /**
     * @param schemaRepository
     *            the schemaRepository to set
     */
    public void setSchemaRepository(SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    /**
     * @param newIdentifier
     *            the newIdentifier to set
     */
    public void setNewIdentifier(String newIdentifier) {
        this.newIdentifier = newIdentifier;
    }
}
