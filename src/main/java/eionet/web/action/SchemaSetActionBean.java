package eionet.web.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.meta.DElemAttribute;
import eionet.meta.FixedValue;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.DAOFactory;
import eionet.meta.dao.SchemaSetDAO;
import eionet.meta.dao.domain.SchemaSet;
import eionet.util.UrlBuilderExt;
import eionet.web.util.DropdownOperation;

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

    /** */
    private SchemaSet schemaSet;
    private Collection<DElemAttribute> attributes;

    /** */
    private Collection<DropdownOperation> dropdownOperations;

    /** */
    private Map<String, Set<String>> multiValuedAttributeValues;
    private Map<String, Set<String>> fixedValuedAttributeValues;
    private Map<Integer, Set<String>> saveAttributeValues;

    /** Check-in comment. */
    private String comment;

    /**
     * 
     * @return
     * @throws DAOException
     * @throws SQLException
     */
    @DefaultHandler
    @HandlesEvent(value = "view")
    public Resolution view() throws DAOException, SQLException {

        loadSchemaSet();
        return new ForwardResolution(VIEW_SCHEMA_SET_JSP);
    }

    /**
     * 
     * @return
     * @throws DAOException
     * @throws SQLException
     */
    @HandlesEvent(value = "edit")
    public Resolution edit() throws DAOException, SQLException {

        loadSchemaSet();
        return new ForwardResolution(EDIT_SCHEMA_SET_JSP);
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution add() throws DAOException {

        Resolution resolution = new ForwardResolution(ADD_SCHEMA_SET_JSP);
        if (!isGetOrHeadRequest()) {
            schemaSet.setWorkingUser(getUserName());
            schemaSet.setUserModified(getUserName());
            SchemaSetDAO dao = DAOFactory.getInstance().createDao(SchemaSetDAO.class);
            int schemaSetId = dao.add(schemaSet);
            resolution = new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSetId);
        }
        return resolution;
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution save() throws DAOException {

        doSave();
        return new ForwardResolution(EDIT_SCHEMA_SET_JSP);
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution saveAndClose() throws DAOException {
        doSave();
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSet.getId());
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution cancel() throws DAOException {
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSet.getId());
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution checkIn() throws DAOException {

        SchemaSetDAO dao = DAOFactory.getInstance().createDao(SchemaSetDAO.class);
        dao.checkIn(schemaSet.getId(), getUserName(), comment);
        return new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSet.getId());
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution checkOut() throws DAOException {
        throw new UnsupportedOperationException("Action not impemented yet!");
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {
        throw new UnsupportedOperationException("Action not impemented yet!");
    }

    /**
     * 
     * @throws DAOException
     */
    private void loadSchemaSet() throws DAOException {
        SchemaSetDAO dao = DAOFactory.getInstance().createDao(SchemaSetDAO.class);
        this.schemaSet = dao.getById(schemaSet.getId());
        if (schemaSet == null) {
            addSystemMessage("No schema set found with the given id: " + schemaSet.getId());
        }
    }

    /**
     * @throws DAOException
     * 
     */
    private void doSave() throws DAOException {

        dumpRequestParameters();

        schemaSet.setUserModified(getUserName());
        SchemaSetDAO dao = DAOFactory.getInstance().createDao(SchemaSetDAO.class);
        dao.save(schemaSet, getSaveAttributeValues());
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

                        dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("edit").toString(), "New version"));
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
}
