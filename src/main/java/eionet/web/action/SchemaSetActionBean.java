package eionet.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.meta.SchemaSet;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.DAOFactory;
import eionet.meta.dao.SchemaSetDAO;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
@UrlBinding("/schemaSet.action")
public class SchemaSetActionBean extends AbstractActionBean{

    /** */
    private static final String ADD_SCHEMA_SET_JSP = "/pages/addSchemaSet.jsp";
    private static final String SCHEMA_SET_JSP = "/pages/schemaSet.jsp";

    /** */
    private SchemaSet schemaSet;

    /**
     * 
     * @return
     */
    @DefaultHandler
    public Resolution view(){
        return new ForwardResolution(SCHEMA_SET_JSP);
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution add() throws DAOException{

        Resolution resolution = new ForwardResolution(ADD_SCHEMA_SET_JSP);
        if (!isGetOrHeadRequest()){
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
     * @param schemaSet the schemaSet to set
     */
    public void setSchemaSet(SchemaSet schemaSet) {
        this.schemaSet = schemaSet;
    }
}
