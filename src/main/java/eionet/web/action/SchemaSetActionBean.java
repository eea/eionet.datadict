package eionet.web.action;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
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
import net.sourceforge.stripes.util.UrlBuilder;

import org.apache.commons.lang.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.meta.DElemAttribute;
import eionet.meta.FixedValue;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.schemas.SchemaRepository;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ServiceException;
import eionet.util.UrlBuilderExt;
import eionet.web.util.DropdownOperation;
import eionet.web.util.Tab;

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
    private static final String METADATA_TAB = "Metadata";
    private static final String SCHEMAS_TAB = "Schemas";

    /** */
    private SchemaSet schemaSet;
    private Collection<DElemAttribute> attributes;

    /** */
    private List<Schema> schemas;

    /** */
    private FileBean uploadedFile;

    /** */
    private Collection<DropdownOperation> dropdownOperations;

    /** */
    private Map<String, Set<String>> multiValuedAttributeValues;
    private Map<String, Set<String>> fixedValuedAttributeValues;
    private Map<Integer, Set<String>> saveAttributeValues;

    /** Check-in comment. */
    private String comment;

    /** */
    private String tab = METADATA_TAB;
    private List<Tab> tabs;

    /** */
    private List<Integer> schemaIds;

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

        Resolution resolution = new ForwardResolution(VIEW_SCHEMA_SET_JSP);
        if (tab != null && tab.equals(SCHEMAS_TAB)) {
            resolution = new ForwardResolution(SCHEMA_SET_SCHEMAS_JSP);
        }
        return resolution;
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

        Resolution resolution = new ForwardResolution(EDIT_SCHEMA_SET_JSP);
        if (tab != null && tab.equals(SCHEMAS_TAB)) {
            resolution = new ForwardResolution(SCHEMA_SET_SCHEMAS_JSP);
        }
        return resolution;
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
        }
        return resolution;
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
        schemaService.checkInSchemaSet(schemaSet.getId(), getUserName(), comment);
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSet.getId());
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
        throw new UnsupportedOperationException("Action not impemented yet!");
    }

    /**
     *
     * @return
     */
    public Resolution delete() {
        throw new UnsupportedOperationException("Action not impemented yet!");
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public Resolution deleteSchemas() throws ServiceException {
        schemaService.deleteSchemas(schemaIds);

        RedirectResolution resolution = new RedirectResolution(getClass());
        resolution.addParameter("schemaSet.id", schemaSet.getId());
        resolution.addParameter("tab", SCHEMAS_TAB);

        return resolution;
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

        RedirectResolution resolution = new RedirectResolution(getClass());
        resolution.addParameter("schemaSet.id", schemaSet.getId());
        resolution.addParameter("tab", SCHEMAS_TAB);

        return resolution;
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
    public Collection<DElemAttribute> getAttributes() throws DAOException {

        if (attributes == null) {
            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                attributes =
                        searchEngine.getObjectAttributes(schemaSet.getId(), DElemAttribute.ParentType.SCHEMA_SET,
                                DElemAttribute.TYPE_SIMPLE);
            } finally {
                searchEngine.close();
            }
        }
        return attributes;
    }

    /**
     * @return the dropdownOperations
     */
    public Collection<DropdownOperation> getDropdownOperations() {

        if (dropdownOperations == null) {
            dropdownOperations = new ArrayList<DropdownOperation>();
            if (schemaSet != null && getContext().getEventName().equals("view")) {
                DDUser user = getUser();
                if (user != null) {
                    if (schemaSet.isWorkingCopy()) {
                        // Is my working copy
                        if (StringUtils.equals(user.getUserName(), schemaSet.getWorkingUser())) {

                            UrlBuilderExt urlBuilder = new UrlBuilderExt(getContext(), getContextPath() + getUrlBinding(), true);
                            urlBuilder.addParameter("schemaSet.id", schemaSet.getId());

                            dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("edit").toString(), "Edit metadata"));
                            dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("checkIn").toString(), "Check in"));
                            dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("checkOut").toString(),
                                    "Undo checkout"));
                        }
                    } else {
                        UrlBuilderExt urlBuilder = new UrlBuilderExt(getContext(), getContextPath() + getUrlBinding(), true);
                        urlBuilder.addParameter("schemaSet.id", schemaSet.getId());

                        dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("newVersion").toString(), "New version"));
                        dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("checkOut").toString(), "Check out"));
                        dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("delete").toString(), "Delete"));
                    }
                }
            }
        }
        return dropdownOperations;
    }

    /**
     * @return the possibleAttributeValues
     * @throws DAOException
     */
    public Map<String, Set<String>> getMultiValuedAttributeValues() throws DAOException {

        if (multiValuedAttributeValues == null) {
            multiValuedAttributeValues = new HashMap<String, Set<String>>();
            Collection<DElemAttribute> attributes = getAttributes();
            DDSearchEngine searchEngine = null;
            try {
                searchEngine = DDSearchEngine.create();
                for (DElemAttribute attribute : attributes) {

                    if (attribute.isMultipleValuesAllowed()) {

                        Vector existingValues = attribute.getValues();
                        Collection possibleValues = null;
                        if (StringUtils.equals(attribute.getDisplayType(), "select")) {
                            possibleValues = getFixedValuedAttributeValues().get(attribute.getID());
                        } else {
                            possibleValues = searchEngine.getSimpleAttributeValues(attribute.getID());
                        }

                        LinkedHashSet<String> values = new LinkedHashSet<String>();
                        if (existingValues != null) {
                            values.addAll(existingValues);
                        }
                        if (possibleValues != null) {
                            values.addAll(possibleValues);
                        }
                        multiValuedAttributeValues.put(attribute.getID(), values);
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
                for (DElemAttribute attribute : attributes) {

                    if (StringUtils.equals(attribute.getDisplayType(), "select")) {

                        LinkedHashSet<String> values = new LinkedHashSet<String>();
                        Collection<FixedValue> fixedValues = searchEngine.getFixedValues(attribute.getID(), "attr");
                        for (FixedValue fxv : fixedValues) {
                            String value = fxv.getValue();
                            if (value != null) {
                                values.add(value);
                            }
                        }
                        fixedValuedAttributeValues.put(attribute.getID(), values);
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
    public Map<Integer, Set<String>> getSaveAttributeValues() {

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
     *
     * @return
     */
    public List<Tab> getTabs() {

        if (tabs == null) {
            tabs = new ArrayList<Tab>();

            UrlBuilder urlBuilder = new UrlBuilder(getContext().getLocale(), getClass(), false);
            urlBuilder.addParameter("schemaSet.id", schemaSet.getId());
            boolean selected = tab == null || tab.equals(METADATA_TAB);

            tabs.add(new Tab(METADATA_TAB, urlBuilder.toString(), "Metadata attributes of this schema set", selected));

            urlBuilder = new UrlBuilder(getContext().getLocale(), getClass(), false);
            urlBuilder.addParameter("schemaSet.id", schemaSet.getId());
            urlBuilder.addParameter("tab", SCHEMAS_TAB);
            selected = tab != null && tab.equals(SCHEMAS_TAB);

            tabs.add(new Tab(SCHEMAS_TAB, urlBuilder.toString(), "Schemas in this schema set", selected));
        }

        return tabs;
    }

    /**
     * @param tab
     *            the tab to set
     */
    public void setTab(String tab) {
        this.tab = tab;
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

}
