/**
 *
 */
package eionet.web.action;

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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

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
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * @author Jaanus Heinlaid
 *
 */
@UrlBinding("/schema.action")
public class SchemaActionBean extends AbstractActionBean {

    /** */
    private static final String ADD_SCHEMA_JSP = "/pages/schemaSets/addSchema.jsp";

    /** */
    private static final String VIEW_SCHEMA_JSP = "/pages/schemaSets/viewSchema.jsp";

    /** */
    private static final String EDIT_SCHEMA_JSP = "/pages/schemaSets/editSchema.jsp";

    /** */
    private Schema schema;
    private SchemaSet schemaSet;

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
     * @throws ServiceException
     */
    private void loadSchema() throws ServiceException {
        schema = schemaService.getSchema(schema.getId());
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
}
