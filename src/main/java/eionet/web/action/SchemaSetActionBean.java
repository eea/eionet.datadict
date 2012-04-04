package eionet.web.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.util.UrlBuilder;

import org.apache.commons.lang.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.meta.DElemAttribute;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.DAOFactory;
import eionet.meta.dao.SchemaSetDAO;
import eionet.meta.dao.domain.SchemaSet;
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
    private Collection<DropdownOperation> dropdownOperations;

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
     * 
     * @return
     * @throws DAOException
     */
    public Resolution add() throws DAOException {

        Resolution resolution = new ForwardResolution(ADD_SCHEMA_SET_JSP);
        if (!isGetOrHeadRequest()) {
            schemaSet.setWorkingUser(getUserName());
            schemaSet.setUser(getUserName());
            SchemaSetDAO dao = DAOFactory.getInstance().createDao(SchemaSetDAO.class);
            int schemaSetId = dao.add(schemaSet);
            resolution = new RedirectResolution(getClass()).addParameter("schemaSet.id", schemaSetId);
        }
        return resolution;
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

                            UrlBuilder urlBuilder =
                                new UrlBuilder(getContext().getLocale(), getContextPath() + getUrlBinding(), true);
                            urlBuilder.addParameter("schemaSet.id", schemaSet.getId());

                            dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("edit").toString(), "Edit metadata"));
                            dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("checkin").toString(), "Check in"));
                            dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("checkout").toString(),
                            "Undo checkout"));
                        }
                    } else {
                        UrlBuilder urlBuilder =
                            new UrlBuilder(getContext().getLocale(), getContextPath() + getUrlBinding(), true);
                        urlBuilder.addParameter("schemaSet.id", schemaSet.getId());

                        dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("edit").toString(), "New version"));
                        dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("checkin").toString(), "Check out"));
                        dropdownOperations.add(new DropdownOperation(urlBuilder.setEvent("checkout").toString(), "Delete"));
                    }
                }
            }
        }
        return dropdownOperations;
    }
}
