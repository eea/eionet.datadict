/**
 *
 */
package eionet.web.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.FixedValue;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.SchemaSet.RegStatus;
import eionet.meta.schemas.SchemaRepository;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * @author Jaanus Heinlaid
 *
 */
@UrlBinding("/schema.action")
public class SchemaActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(SchemaActionBean.class);

    /** */
    private static final String ADD_ROOT_LEVEL_SCHEMA_JSP = "/pages/schemaSets/addRootLevelSchema.jsp";

    /** */
    private static final String VIEW_SCHEMA_JSP = "/pages/schemaSets/viewSchema.jsp";

    /** */
    private static final String EDIT_SCHEMA_JSP = "/pages/schemaSets/editSchema.jsp";

    /** */
    private Schema schema;
    private SchemaSet schemaSet;
    private FileBean uploadedFile;

    /** */
    private LinkedHashMap<Integer, DElemAttribute> attributes;

    /** This schema's attribute values as submitted from the save/add form. */
    private Map<Integer, Set<String>> saveAttributeValues;

    /** Values of this schema set's attributes that can have multiple values. */
    private Map<String, Set<String>> multiValuedAttributeValues;

    /** Values of this schema set's attributes that can have only fixed values. */
    private Map<String, Set<String>> fixedValuedAttributeValues;

    /** Schema service. */
    @SpringBean
    private ISchemaService schemaService;

    /** Schema repository. */
    @SpringBean
    private SchemaRepository schemaRepository;

    /**
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {

        loadSchema();
        return new ForwardResolution(VIEW_SCHEMA_JSP);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution edit() throws ServiceException {

        loadSchema();
        return new ForwardResolution(EDIT_SCHEMA_JSP);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution save() throws ServiceException {

        schemaService.updateSchema(schema, getSaveAttributeValues(), getUserName());
        addSystemMessage("Schema successfully updated!");
        return new ForwardResolution(EDIT_SCHEMA_JSP);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution saveAndClose() throws ServiceException {

        schemaService.updateSchema(schema, getSaveAttributeValues(), getUserName());
        addSystemMessage("Schema successfully updated!");
        return new RedirectResolution(getClass()).addParameter("schema.id", schema.getId());
    }

    /**
     *
     * @return
     * @throws ServiceException
     * @throws IOException
     */
    public Resolution add() throws ServiceException, IOException {

        Resolution resolution = new ForwardResolution(ADD_ROOT_LEVEL_SCHEMA_JSP);
        if (!isGetOrHeadRequest()) {

            File savedSchemaFile = null;
            int schemaId;
            try {
                savedSchemaFile = schemaRepository.addSchema(uploadedFile, null, false);

                schema.setFileName(uploadedFile.getFileName());
                schema.setUserModified(getUserName());
                schema.setWorkingUser(getUserName());
                schema.setWorkingCopy(true);

                schemaId = schemaService.addSchema(schema, getSaveAttributeValues());
            } catch (ServiceException e) {
                SchemaRepository.deleteQuietly(savedSchemaFile);
                throw e;
            }
            resolution = new RedirectResolution(getClass()).addParameter("schema.id", schemaId);
            addSystemMessage("Working copy successfully created!");
        }
        return resolution;
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution cancel() throws ServiceException {
        return new RedirectResolution(getClass()).addParameter("schema.id", schema.getId());
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution checkIn() throws ServiceException {

        loadSchema();
        addCautionMessage("Operation not supported yet!");
        return new ForwardResolution(VIEW_SCHEMA_JSP);
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution undoCheckout() throws ServiceException {

        loadSchema();
        addCautionMessage("Operation not supported yet!");
        return new ForwardResolution(VIEW_SCHEMA_JSP);
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    public Resolution reupload() throws IOException, ServiceException{

        loadSchema();
        String schemaSetIdentifier = getSchemaSet() == null ? null : getSchemaSet().getIdentifier();
        File schemaFile = schemaRepository.reuploadSchema(schema.getFileName(), schemaSetIdentifier, uploadedFile);

        addSystemMessage("Schema file successfully uploaded!");
        return new ForwardResolution(VIEW_SCHEMA_JSP);
    }

    /**
     *
     * @throws IOException
     * @throws DAOException
     */
    @ValidationMethod(on = {"add"})
    public void validateAdd() throws ServiceException, IOException, DAOException {

        if (!isUserLoggedIn() || !getUser().hasPermission("/schemasets", "i")){
            throw new ServiceException("No permission to create root-level schemas!");
        }

        if (isGetOrHeadRequest()){
            return;
        }

        if (uploadedFile == null) {
            addGlobalValidationError("No file uploaded!");
        }
        else if (uploadedFile.getSize() <= 0){
            addGlobalValidationError("Uploaded file must not be empty!");
        }

        if (!isValidationErrors()) {
            validateXmlSchema();
        }

        if (!isValidationErrors()){
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

        if (isValidationErrors()) {
            loadSchema();
            getContext().setSourcePageResolution(new ForwardResolution(ADD_ROOT_LEVEL_SCHEMA_JSP));
        }
    }

    /**
     * @throws ServiceException
     *
     */
    @ValidationMethod(on = {"view", "edit", "save", "saveAndClose", "reupload"}, priority = 1)
    public void validateViewEditSave() throws ServiceException{

        LOGGER.trace("Method entered");

        if (schema==null || schema.getId()<=0){
            throw new ServiceException("Schema id missing!");
        }

        loadSchema();

        if (!isUserLoggedIn()){
            RegStatus regStatus = getSchemaSet() == null ? schema.getRegStatus() : getSchemaSet().getRegStatus();
            if (!regStatus.equals(SchemaSet.RegStatus.RELEASED)){
                throw new ServiceException("Un-authenticated users can only see definitions in Released status!");
            }
        }

        boolean isAllowed = true;
        if (schema.isWorkingCopy()){
            isAllowed = isUserLoggedIn() && (schema.isWorkingCopyOf(getUserName()));
        }

        if (isAllowed){
            SchemaSet schemaSet = getSchemaSet();
            if (schemaSet!=null && schemaSet.isWorkingCopy()){
                isAllowed = isUserLoggedIn() && (schemaSet.isWorkingCopyOf(getUserName()));
            }
        }

        if (!isAllowed) {
            throw new ServiceException("A working copy can only be seen and edited by its owner or system administrator!");
        }
    }

    /**
     *
     * @throws DAOException
     */
    @ValidationMethod(on = {"save", "saveAndClose"}, priority = 2)
    public void validateSave() throws DAOException {

        LOGGER.trace("Method entered");

        if (isGetOrHeadRequest()) {
            return;
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
     *
     * @throws IOException
     * @throws ServiceException
     * @throws DAOException
     */
    @ValidationMethod(on = {"reupload"}, priority = 2)
    public void validateReupload() throws IOException, ServiceException, DAOException {

        LOGGER.trace("Method entered");

        if (uploadedFile == null) {
            addGlobalValidationError("No file uploaded!");
        }
        else if (uploadedFile.getSize() <= 0){
            addGlobalValidationError("Uploaded file must not be empty!");
        }

        if (!isValidationErrors()) {
            if (schema == null || schema.getId() <= 0) {
                addGlobalValidationError("Schema id missing!");
            }
        }

        if (!isValidationErrors()) {
            validateXmlSchema();
        }

        if (isValidationErrors()) {
            loadSchema();
            getContext().setSourcePageResolution(new ForwardResolution(VIEW_SCHEMA_JSP));
        }
    }

    /**
     *
     * @throws ServiceException
     */
    private void loadSchema() throws ServiceException {
        schema = schemaService.getSchema(schema.getId());
    }

    /**
     * @throws IOException
     */
    private void validateXmlSchema() throws IOException {
        InputStream inputStream = null;
        try {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            inputStream = uploadedFile.getInputStream();
            factory.newSchema(new StreamSource(inputStream));
        } catch (SAXException saxe) {
            addGlobalValidationError("Not a valid XML Schema file!");
            try {
                uploadedFile.delete();
            } catch (Exception e) {
                LOGGER.error("Failed to delete uploaded file " + uploadedFile.getFileName(), e);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     *
     * @return
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
     *
     * @return
     * @throws DAOException
     */
    public LinkedHashMap<Integer, DElemAttribute> getAttributes() throws DAOException {

        if (attributes == null) {

            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                int schemaId = schema == null ? 0 : schema.getId();

                attributes =
                    searchEngine.getObjectAttributes(schemaId, DElemAttribute.ParentType.SCHEMA,
                            DElemAttribute.TYPE_SIMPLE);

                // If this is a POST request where new attribute values are submitted (e.g. "save", "add", etc)
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
     * @return the schema
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * @param schema
     *            the schema to set
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     *
     * @return
     */
    public boolean isRootLevelSchema() {
        return schema != null && schema.getSchemaSetId() <= 0;
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public boolean isMySchemaSetWorkingCopy() throws ServiceException {

        boolean result = false;
        SchemaSet schemaSet = getSchemaSet();
        if (schemaSet != null && schemaSet.isWorkingCopy() && StringUtils.equals(schemaSet.getWorkingUser(), getUserName())){
            result = true;
        }

        return result;
    }

    /**
     *
     * @return
     */
    public boolean isUserWorkingCopy() {

        boolean result = false;
        String sessionUser = getUserName();
        if (isRootLevelSchema() && !StringUtils.isBlank(sessionUser)) {
            String workingUser = schema.getWorkingUser();
            return schema.isWorkingCopy() && StringUtils.equals(workingUser, sessionUser);
        }

        return result;
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
     * @return the schemaSet
     * @throws ServiceException
     */
    public SchemaSet getSchemaSet() throws ServiceException {

        if (schema!=null && !isRootLevelSchema()){
            if (schemaSet==null) {
                schemaSet = schemaService.getSchemaSet(schema.getSchemaSetId());
            }
        }

        return schemaSet;
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
                DDSearchEngine.close(searchEngine);
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
                DDSearchEngine.close(searchEngine);
            }
        }
        return fixedValuedAttributeValues;
    }

    /**
     * Returns the download link for the given schema file.
     * If the schema is part of a schema set, the link's format is contextPath/schemas/schemaSetIdentifier/schemaFileName.
     * If the schema is a root-level schema, the link's format is contextPath/schemas/schemaFileName.
     * In both cases, schemaSetIdentifier and schemaFileName are escaped for XML, i.e. the link is meant
     * as "href" value in an HTML link.
     *
     * @return
     * @throws ServiceException
     */
    public String getSchemaDownloadLink() throws ServiceException{

        if (schema == null){
            throw new IllegalStateException("Schema object must be initialized!");
        }

        StringBuilder sb = new StringBuilder(getContextPath()).append("/schemas/");
        SchemaSet schemaSet = getSchemaSet();
        if (schemaSet!=null){
            sb.append(schemaSet.getIdentifier()).append("/");
        }
        sb.append(schema.getFileName());

        return sb.toString();
    }

    /**
     * @param uploadedFile the uploadedFile to set
     */
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
}
