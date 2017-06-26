package eionet.web.action;

import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.data.AttributeDataService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import eionet.meta.DDException;
import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.FixedValue;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.schemas.SchemaRepository;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import eionet.util.XmlValidator;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
@UrlBinding("/schemaset/{schemaSet.identifier}/{$event}")
public class SchemaSetActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(SchemaSetActionBean.class);

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

    @SpringBean
    private AttributeService attributeService;

    @SpringBean
    private AttributeDataService attributeDataService;

    @SpringBean
    private IVocabularyService vocabularyService;

    /** Simple attributes of this schema set. */
    private SchemaSet schemaSet;

    /** */
    private LinkedHashMap<Integer, DElemAttribute> attributes;

    /** Complex attributes of this schema set. */
    private Vector complexAttributes;

    /** For every complex attribute in {@link #complexAttributes} maps the Vector of its fields. */
    private HashMap<String, Vector> complexAttributeFields;

    /** */
    private List<Schema> schemas;

    /** */
    private List<SchemaSet> otherVersions;

    /** */
    private FileBean uploadedFile;

    /** Values of this schema set's attributes that can have multiple values. */
    private Map<String, Set<String>> multiValuedAttributeValues;

    /** Values of this schema set's attributes that can have only fixed values. */
    private Map<String, Set<String>> fixedValuedAttributeValues;

    /** This schema set's attribute values as submitted from the save form. */
    private Map<Integer, Set<String>> saveAttributeValues;

    /** */
    private List<Integer> schemaIds;

    /** */
    private String newIdentifier;

    /**
     * Attributes that are mandatory for schemas (!), not schema sets. Used when validating upload of a new schema.
     */
    private Collection<DElemAttribute> mandatorySchemaAttributes;

    private boolean workingCopy;

    /**
     * View action.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    @DefaultHandler
    @HandlesEvent(value = "view")
    public Resolution view() throws ServiceException {

        loadSchemaSetByIdentifier();

        if (!isUserLoggedIn()) {
            if (!Util.enumEquals(schemaSet.getRegStatus(), RegStatus.RELEASED, RegStatus.PUBLIC_DRAFT, RegStatus.DEPRECATED)) {
                throw new ServiceException("Un-authenticated users can only see definitions in Released status!");
            }
        }

        return new ForwardResolution(VIEW_SCHEMA_SET_JSP);
    }

    /**
     * Edit action.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    @HandlesEvent(value = "edit")
    public Resolution edit() throws ServiceException {
        workingCopy = true;
        loadSchemaSetByIdentifier();

        if (!isUserWorkingCopy()) {
            throw new ServiceException("Operation allowed on your working copy only!");
        }

        if (isUserLoggedIn() && schemaSet.isCheckedOutBy(getUserName())) {
            throw new ServiceException("A checked-out schema set can only be edited by its owner!");
        }

        return new ForwardResolution(EDIT_SCHEMA_SET_JSP);
    }

    /**
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution editSchemas() throws ServiceException {
        workingCopy = true;
        loadSchemaSetByIdentifier();

        if (!isUserWorkingCopy()) {
            throw new ServiceException("Operation allowed on your working copy only!");
        }

        if (isUserLoggedIn() && schemaSet.isCheckedOutBy(getUserName())) {
            throw new ServiceException("Only owner of the cheked out schema set can edit the schema set.");
        }

        return new ForwardResolution(SCHEMA_SET_SCHEMAS_JSP);
    }

    /**
     * Add action.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution add() throws ServiceException {

        if (!isCreateAllowed()) {
            throw new ServiceException("You are not authorised for this operation!");
        }

        Resolution resolution = new ForwardResolution(ADD_SCHEMA_SET_JSP);
        if (!isGetOrHeadRequest()) {
            schemaService.addSchemaSet(schemaSet, getSaveAttributeValues(), getUserName());
            resolution =
                    new RedirectResolution(getClass()).addParameter("schemaSet.identifier", schemaSet.getIdentifier())
                            .addParameter("workingCopy", true);
            addSystemMessage("Working copy successfully created!");
        }
        return resolution;
    }

    /**
     * Save action.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution save() throws ServiceException {
        schemaService.updateSchemaSet(schemaSet, getSaveAttributeValues(), getUserName());
        addSystemMessage("Schema set successfully updated!");

        return new ForwardResolution(EDIT_SCHEMA_SET_JSP);
    }

    /**
     * Save and close action.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution saveAndClose() throws ServiceException {

        schemaService.updateSchemaSet(schemaSet, getSaveAttributeValues(), getUserName());
        addSystemMessage("Schema set successfully updated!");
        return new RedirectResolution(getClass()).addParameter("schemaSet.identifier", schemaSet.getIdentifier()).addParameter(
                "workingCopy", true);
    }

    /**
     * Cancel action.
     *
     * @return resolution
     * @throws DAOException
     *             if operation fails
     */
    public Resolution cancelEdit() throws DAOException {
        return new RedirectResolution(getClass()).addParameter("schemaSet.identifier", schemaSet.getIdentifier()).addParameter(
                "workingCopy", true);
    }

    /**
     *
     * @return resolution
     * @throws DAOException
     *             if operation fails
     */
    public Resolution cancelAdd() throws DAOException {
        return new RedirectResolution(BrowseSchemaSetsActionBean.class);
    }

    /**
     * Check in action.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution checkIn() throws ServiceException {

        loadSchemaSetById();
        if (!isUserWorkingCopy()) {
            throw new ServiceException("Operation allowed on your working copy only!");
        }

        schemaService.checkInSchemaSet(schemaSet.getId(), getUserName(), schemaSet.getComment());
        addSystemMessage("Schema set successfully checked in!");
        return new RedirectResolution(getClass()).addParameter("schemaSet.identifier", schemaSet.getIdentifier()).addParameter(
                "workingCopy", false);
    }

    /**
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution checkOut() throws ServiceException {

        loadSchemaSetById();
        if (!isCheckoutAllowed()) {
            throw new ServiceException("You are not authorised for this operation!");
        }

        schemaService.checkOutSchemaSet(schemaSet.getId(), getUserName());
        addSystemMessage("Schema set successfully checked out!");
        return new RedirectResolution(getClass()).addParameter("schemaSet.identifier", schemaSet.getIdentifier()).addParameter(
                "workingCopy", true);
    }

    /**
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution newVersion() throws ServiceException {

        if (!isCreateAllowed()) {
            throw new ServiceException("You are not authorised for this operation!");
        }

        schemaService.copySchemaSet(schemaSet.getId(), getUserName(), newIdentifier);
        addSystemMessage("The new version's working copy successfully created!");
        return new RedirectResolution(getClass()).addParameter("schemaSet.identifier", newIdentifier).addParameter("workingCopy",
                true);
    }

    /**
     * Action for deleting the schema set.
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution delete() throws ServiceException {

        loadSchemaSetById();
        if (!isDeleteAllowed()) {
            throw new ServiceException("You are not authorized for this operation!");
        }

        schemaService.deleteSchemaSets(Collections.singletonList(schemaSet.getId()), getUserName(), true);
        addSystemMessage("Schema set succesfully deleted.");
        return new RedirectResolution(BrowseSchemaSetsActionBean.class);
    }

    /**
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution undoCheckout() throws ServiceException {

        loadSchemaSetById();
        if (!isUserWorkingCopy()) {
            throw new ServiceException("Operation allowed on your working copy only!");
        }

        int checkedOutCopyId = schemaService.undoCheckOutSchemaSet(schemaSet.getId(), getUserName());
        addSystemMessage("Working copy successfully deleted!");
        if (checkedOutCopyId > 0) {
            return new RedirectResolution(getClass()).addParameter("schemaSet.identifier", schemaSet.getIdentifier())
                    .addParameter("workingCopy", false);
        } else {
            return new RedirectResolution(BrowseSchemaSetsActionBean.class);
        }
    }

    /**
     *
     * @return resolution
     * @throws ServiceException
     *             if operation fails
     */
    public Resolution deleteSchemas() throws ServiceException {

        loadSchemaSetById();
        if (!isUserWorkingCopy()) {
            throw new ServiceException("Operation allowed on your working copy only!");
        }

        schemaService.deleteSchemas(schemaIds, getUserName(), false);

        if (schemaIds.size() == 1) {
            addSystemMessage("Schema succesfully deleted.");
        } else if (schemaIds.size() > 1) {
            addSystemMessage("Schemas succesfully deleted.");
        }

        return new RedirectResolution(getClass(), "editSchemas").addParameter("schemaSet.identifier", schemaSet.getIdentifier())
                .addParameter("workingCopy", true);
    }

    /**
     * Uploads schema.
     *
     * @return resolution
     *
     * @throws ServiceException
     *             if operation fails
     * @throws IOException
     *             if operation fails
     */
    public Resolution uploadSchema() throws ServiceException, IOException {

        File schemaFile = null;
        try {
            schemaFile = schemaRepository.addSchema(uploadedFile, schemaSet.getIdentifier(), true);

            Schema schema = new Schema();
            schema.setFileName(uploadedFile.getFileName());
            schema.setUserModified(getUserName());
            schema.setSchemaSetId(schemaSet.getId());
            schemaService.addSchema(schema, getSaveAttributeValues());
        } catch (ServiceException e) {
            SchemaRepository.deleteQuietly(schemaFile);
            throw e;
        } catch (RuntimeException e) {
            SchemaRepository.deleteQuietly(schemaFile);
            throw e;
        }

        addSystemMessage("Schema successfully uploaded!");
        return new RedirectResolution(getClass(), "editSchemas").addParameter("schemaSet.identifier", schemaSet.getIdentifier())
                .addParameter("workingCopy", true);
    }

    /**
     * Uploads other document.
     *
     * @return resolution
     *
     * @throws ServiceException
     *             if operation fails
     * @throws IOException
     *             if operation fails
     */
    public Resolution uploadOtherDocument() throws ServiceException, IOException {

        File schemaFile = null;
        try {
            schemaFile = schemaRepository.addSchema(uploadedFile, schemaSet.getIdentifier(), true);

            Schema schema = new Schema();
            schema.setFileName(uploadedFile.getFileName());
            schema.setUserModified(getUserName());
            schema.setSchemaSetId(schemaSet.getId());
            schema.setOtherDocument(true);
            schemaService.addSchema(schema, getSaveAttributeValues());
        } catch (ServiceException e) {
            SchemaRepository.deleteQuietly(schemaFile);
            throw e;
        } catch (RuntimeException e) {
            SchemaRepository.deleteQuietly(schemaFile);
            throw e;
        }

        addSystemMessage("Schema successfully uploaded!");
        return new RedirectResolution(getClass(), "editSchemas").addParameter("schemaSet.identifier", schemaSet.getIdentifier())
                .addParameter("workingCopy", true);
    }

    @ValidationMethod(on = {"add", "save", "saveAndClose"})
    public void validateAddSave() throws DAOException, ServiceException, ResourceNotFoundException, EmptyParameterException {
        if (isGetOrHeadRequest()) {
            return;
        }

        if (StringUtils.equals(getContext().getEventName(), "add")) {
            if (schemaSet == null || StringUtils.isBlank(schemaSet.getIdentifier())) {
                addGlobalValidationError("Identifier is missing!");
            }

            if (schemaService.schemaSetExists(schemaSet.getIdentifier())) {
                addGlobalValidationError("A schema set or a schema set working copy by this identifier already exists!");
            }

            if (SchemaSet.ROOT_IDENTIFIER.equals(schemaSet.getIdentifier())) {
                addGlobalValidationError("Invalid identifier. \"" + SchemaSet.ROOT_IDENTIFIER + "\" is reserved identifier.");
            }
        }

        LinkedHashMap<Integer, DElemAttribute> attributesMap = getAttributes();
        for (DElemAttribute attribute : attributesMap.values()) {

            if (attribute.isMandatory()) {
                if (attribute.getDisplayType().equals("vocabulary") && !StringUtils.equals(getContext().getEventName(), "add")) {
                    if (getVocabularyConcepts(attribute).isEmpty()) {
                        addGlobalValidationError(attribute.getShortName() + " is missing!");
                    }
                } else {
                    Integer attrId = Integer.valueOf(attribute.getID());
                    Set<String> attrValues = getSaveAttributeValues().get(attrId);
                    if (attrValues == null || attrValues.isEmpty() || StringUtils.isBlank(attrValues.iterator().next())) {
                        addGlobalValidationError(attribute.getShortName() + " is missing!");
                    }
                }
            }
        }
    }

    /**
     * @throws ServiceException
     *             if operation fails
     *
     */
    @ValidationMethod(on = {"checkIn"})
    public void validateCheckIn() throws ServiceException {

        if (isCheckInCommentsRequired()) {
            if (schemaSet == null || StringUtils.isBlank(schemaSet.getComment())) {
                addGlobalValidationError("Check-in comment is mandatory!");
                loadSchemaSetById();
                getContext().setSourcePageResolution(new ForwardResolution(VIEW_SCHEMA_SET_JSP));
            }
        }
    }

    /**
     *
     * @throws DAOException
     *             if operation fails
     * @throws ServiceException
     *             if operation fails
     */
    @ValidationMethod(on = {"newVersion"})
    public void validateNewVersion() throws DAOException, ServiceException {

        if (StringUtils.isBlank(newIdentifier)) {
            addGlobalValidationError("New identifier is missing!");
            return;
        }

        if (!isValidationErrors()) {
            if (schemaService.schemaSetExists(newIdentifier)) {
                addGlobalValidationError("A schema set or a schema set working copy by this identifier already exists!");
            }
        }

        getContext().setSourcePageResolution(new ForwardResolution(VIEW_SCHEMA_SET_JSP));
    }

    /**
     * @throws IOException
     * @throws ServiceException
     * @throws DAOException
     * @throws SAXException
     * @throws ParserConfigurationException
     *
     */
    @ValidationMethod(on = {"uploadSchema"})
    public void validateFileUpload() throws IOException, ServiceException, DAOException, ParserConfigurationException,
            SAXException {

        if (uploadedFile == null) {
            addGlobalValidationError("No file uploaded!");
        } else if (uploadedFile.getSize() <= 0) {
            addGlobalValidationError("Uploaded file must not be empty!");
        }

        if (!isValidationErrors()) {
            if (schemaSet == null || StringUtils.isBlank(schemaSet.getIdentifier())) {
                addGlobalValidationError("Schema set identifier missing!");
            }
        }

        if (!isValidationErrors()) {
            if (schemaService.schemaExists(uploadedFile.getFileName(), schemaSet.getId())) {
                addGlobalValidationError("A schema with such a file name already exists!");
            }
        }

        if (!isValidationErrors()) {
            validateUploadedFile();
        }

        if (!isValidationErrors()) {
            for (DElemAttribute mandatoryAttr : getMandatorySchemaAttributes()) {
                Integer attrId = Integer.valueOf(mandatoryAttr.getID());
                Set<String> values = getSaveAttributeValues().get(attrId);
                if (CollectionUtils.isEmpty(values) || StringUtils.isBlank(values.iterator().next())) {
                    addGlobalValidationError(mandatoryAttr.getShortName() + " is missing!");
                    break;
                }
            }
        }

        if (isValidationErrors()) {
            loadSchemaSetById();
        }
    }

    /**
     * @throws IOException
     * @throws ServiceException
     * @throws DAOException
     * @throws SAXException
     * @throws ParserConfigurationException
     *
     */
    @ValidationMethod(on = {"uploadOtherDocument"})
    public void validateOtherDocumentUpload() throws IOException, ServiceException, DAOException, ParserConfigurationException,
            SAXException {

        if (uploadedFile == null) {
            addGlobalValidationError("No file uploaded!");
        } else if (uploadedFile.getSize() <= 0) {
            addGlobalValidationError("Uploaded file must not be empty!");
        }

        if (!isValidationErrors()) {
            if (schemaSet == null || StringUtils.isBlank(schemaSet.getIdentifier())) {
                addGlobalValidationError("Schema set identifier missing!");
            }
        }

        if (!isValidationErrors()) {
            if (schemaService.schemaExists(uploadedFile.getFileName(), schemaSet.getId())) {
                addGlobalValidationError("A schema with such a file name already exists!");
            }
        }

        if (!isValidationErrors()) {
            for (DElemAttribute mandatoryAttr : getMandatorySchemaAttributes()) {
                Integer attrId = Integer.valueOf(mandatoryAttr.getID());
                Set<String> values = getSaveAttributeValues().get(attrId);
                if (CollectionUtils.isEmpty(values) || StringUtils.isBlank(values.iterator().next())) {
                    addGlobalValidationError(mandatoryAttr.getShortName() + " is missing!");
                    break;
                }
            }
        }

        if (isValidationErrors()) {
            loadSchemaSetById();
        }
    }

    /**
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     *
     */
    private void validateUploadedFile() throws IOException, ParserConfigurationException, SAXException {

        InputStream inputStream = null;
        try {
            inputStream = uploadedFile.getInputStream();
            XmlValidator xmlValidator = new XmlValidator();
            if (!xmlValidator.isWellFormedXml(inputStream)) {
                addGlobalValidationError("Not a well-formed XML: " + xmlValidator.getValidationError().getMessage());
                LOGGER.error("Not a well-formed XML!", xmlValidator.getValidationError());
                // Exit right away, because an ill-formed XML is not worth further parsing.
                return;
            }

            IOUtils.closeQuietly(inputStream);
            inputStream = uploadedFile.getInputStream();
            if (!xmlValidator.isValidXmlSchema(inputStream)) {
                addCautionMessage("The uploaded file was not found to be a valid XML Schema! Reason: "
                        + xmlValidator.getValidationError().getMessage());
                LOGGER.error("Not a valid XML Schema file!", xmlValidator.getValidationError());
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     *
     * @throws ServiceException
     */
    private void loadSchemaSetByIdentifier() throws ServiceException {
        schemaSet = schemaService.getSchemaSet(schemaSet.getIdentifier(), workingCopy);
    }

    /**
     *
     * @throws ServiceException
     */
    private void loadSchemaSetById() throws ServiceException {
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
    public boolean isCheckInCommentsRequired() {
        String checkInCommentsRequired = Props.getProperty(PropsIF.CHECK_IN_COMMENTS_REQUIRED);
        return checkInCommentsRequired != null && checkInCommentsRequired.trim().equalsIgnoreCase("true");
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
     * Returns true if the current user is allowed to do check-out on this schema set. Otherwise returns false.
     *
     * @return
     */
    public boolean isCheckoutAllowed() {
        try {
            if (SecurityUtil.hasPerm(getUserName(), "/schemasets", "er")) {
                return true;
            } else {
                return !schemaSet.isReleased() && SecurityUtil.hasPerm(getUserName(), "/schemasets", "u");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Returns true if the current user is allowed to delete this schema set. Otherwise returns false.
     *
     * @return
     */
    public boolean isDeleteAllowed() {

        try {
            if (SecurityUtil.hasPerm(getUserName(), "/schemasets", "er")) {
                return true;
            } else {
                return !schemaSet.isReleased() && SecurityUtil.hasPerm(getUserName(), "/schemasets", "d");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Returns true if the current user is allowed to create new schema sets. Otherwise returns false.
     *
     * @return
     */
    public boolean isCreateAllowed() {

        if (getUser() != null) {
            try {
                return SecurityUtil.hasPerm(getUserName(), "/schemasets", "i");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return false;
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

                // If this is a POST request of "add", "save" or "saveAndClose",
                // then substitute the values we got from database with the values
                // we got from the request.
                if (!isGetOrHeadRequest()) {
                    String eventName = getContext().getEventName();
                    if (eventName.equals("add") || eventName.equals("save") || eventName.equals("saveAndClose")) {

                        for (Map.Entry<Integer, Set<String>> savedAttrEntry : getSaveAttributeValues().entrySet()) {
                            int attrId = savedAttrEntry.getKey();
                            DElemAttribute attributeObj = attributes.get(attrId);
                            if (attributeObj != null) {
                                attributeObj.nullifyValues();
                                Set<String> savedValues = savedAttrEntry.getValue();
                                for (String savedValue : savedValues) {
                                    attributeObj.setValue(savedValue);
                                }
                            }
                        }
                    }
                }
            } finally {
                DDSearchEngine.close(searchEngine);
            }
        }
        return attributes;
    }

    /**
     * @return the possibleAttributeValues
     * @throws DAOException
     */
    public Map<String, Set<String>> getMultiValuedAttributeValues() throws DDException {

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
                DDSearchEngine.close(searchEngine);
            }
        }
        return multiValuedAttributeValues;
    }

    /**
     * @return the fixedValuedAttributeValues
     * @throws DDException
     *             if database query fails
     */
    public Map<String, Set<String>> getFixedValuedAttributeValues() throws DDException {

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
                DDSearchEngine.close(searchEngine);
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
                        saveAttributeValues.put(attributeId, valueSet);
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

    /**
     *
     * @return
     * @throws DAOException
     */
    public Collection<DElemAttribute> getMandatorySchemaAttributes() throws DAOException {

        if (mandatorySchemaAttributes == null) {
            mandatorySchemaAttributes = new ArrayList<DElemAttribute>();
            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                LinkedHashMap<Integer, DElemAttribute> attrsMap =
                        searchEngine.getObjectAttributes(0, DElemAttribute.ParentType.SCHEMA, DElemAttribute.TYPE_SIMPLE);
                if (attrsMap != null) {
                    for (DElemAttribute attribute : attrsMap.values()) {
                        if (attribute.isMandatory() && !attribute.getDisplayType().equals("vocabulary")) {
                            mandatorySchemaAttributes.add(attribute);
                        }
                    }
                }
            } finally {
                DDSearchEngine.close(searchEngine);
            }
        }
        return mandatorySchemaAttributes;
    }

    /**
     * @return the otherVersions
     * @throws ServiceException
     */
    public List<SchemaSet> getOtherVersions() throws ServiceException {

        if (otherVersions == null) {

            if (schemaSet == null) {
                throw new IllegalStateException("Schema set must be loaded for this operation!");
            }

            otherVersions = schemaService.getSchemaSetVersions(getUserName(), schemaSet.getContinuityId(), schemaSet.getId());
        }
        return otherVersions;
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public SchemaSet getSchemaSetWorkingCopy() throws ServiceException {

        if (schemaSet == null) {
            throw new IllegalStateException("The schema set object must be initialized for this operation!");
        } else if (!isUserLoggedIn() || !schemaSet.isCheckedOutBy(getUserName())) {
            return null;
        } else {
            return schemaService.getWorkingCopyOfSchemaSet(schemaSet.getId());
        }
    }

    /**
     * @return the workingCopy
     */
    public boolean isWorkingCopy() {
        return workingCopy;
    }

    /**
     * @param workingCopy
     *            the workingCopy to set
     */
    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    /**
     *
     * @return
     */
    public boolean isCheckedOut() {

        if (schemaSet == null) {
            return false;
        } else {
            return StringUtils.isNotBlank(schemaSet.getWorkingUser()) && !schemaSet.isWorkingCopy();
        }
    }

    /**
     *
     * @return
     */
    public boolean isCheckedOutByUser() {

        if (schemaSet == null || getUser() == null) {
            return false;
        } else {
            return StringUtils.isNotBlank(schemaSet.getWorkingUser()) && !schemaSet.isWorkingCopy()
                    && schemaSet.getWorkingUser().equals(getUserName());
        }
    }

    /**
     * @return the complexAttributes
     * @throws DAOException
     */
    public Vector getComplexAttributes() throws DAOException {

        if (complexAttributes == null) {

            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                complexAttributes =
                        schemaSet == null ? new Vector() : searchEngine.getComplexAttributes(String.valueOf(schemaSet.getId()),
                                DElemAttribute.ParentType.SCHEMA_SET.toString());

                complexAttributeFields = new HashMap<String, Vector>();
                for (Iterator iter = complexAttributes.iterator(); iter.hasNext();) {
                    DElemAttribute attr = (DElemAttribute) iter.next();
                    String attrId = attr.getID();
                    complexAttributeFields.put(attrId, searchEngine.getAttrFields(attrId, DElemAttribute.FIELD_PRIORITY_HIGH));
                }
            } catch (SQLException e) {
                throw new DAOException(e.getMessage(), e);
            } finally {
                DDSearchEngine.close(searchEngine);
            }
        }
        return complexAttributes;
    }

    /**
     * @return the complexAttributeFields
     */
    public HashMap<String, Vector> getComplexAttributeFields() {
        return complexAttributeFields;
    }

    public Integer getVocabularyBinding(int attributeId) {
       return attributeDataService.getVocabularyBinding(attributeId);
    }

    public List<VocabularyConcept> getVocabularyConcepts(DElemAttribute attribute) throws ResourceNotFoundException, EmptyParameterException {
        DataDictEntity ddEntity = new DataDictEntity(schemaSet.getId(), DataDictEntity.Entity.SCS);
        Attribute.ValueInheritanceMode valueInheritanceMode = Attribute.ValueInheritanceMode.getInstance(attribute.getInheritable());
        return attributeService.getAttributeVocabularyConcepts(Integer.parseInt(attribute.getID()), ddEntity, valueInheritanceMode);
    }

    public VocabularyFolder getVocabularyBindingFolder(int attributeId) throws ServiceException {
        Integer boundVocabularyId = getVocabularyBinding(attributeId);
        if (boundVocabularyId != null) {
            return vocabularyService.getVocabularyFolder(boundVocabularyId);
        }
        return null;
    }

}
