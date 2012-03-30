package eionet.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.DAOFactory;
import eionet.meta.dao.SchemaSetDAO;
import eionet.meta.dao.domain.SchemaSet;

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

    /** */
    private SchemaSet schemaSet;

    /**
     * 
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        SchemaSetDAO dao = DAOFactory.getInstance().createDao(SchemaSetDAO.class);
        this.schemaSet = dao.getById(schemaSet.getId());
        if (schemaSet == null) {
            addSystemMessage("No schema set found with the given id: " + schemaSet.getId());
        }
        return new ForwardResolution(VIEW_SCHEMA_SET_JSP);
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
}
