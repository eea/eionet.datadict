package eionet.web.action;

import eionet.datadict.services.data.CheckoutsService;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.List;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

@UrlBinding("/checkouts")
public class CheckoutsActionBean extends AbstractActionBean {
    
    public static final String CHECKOUTS_PAGE = "/pages/checkouts.jsp";

    @SpringBean
    private CheckoutsService checkoutsService;

    private List<DataSet> dataSets;
    private List<DataElement> dataElements;
    private List<SchemaSet> schemaSets;
    private List<Schema> schemas;
    private List<VocabularyFolder> vocabularies;

    public CheckoutsService getCheckoutsService() {
        return checkoutsService;
    }

    public void setCheckoutsService(CheckoutsService checkoutsService) {
        this.checkoutsService = checkoutsService;
    }

    public List<DataSet> getDataSets() {
        return dataSets;
    }

    public List<DataElement> getDataElements() {
        return dataElements;
    }

    public List<SchemaSet> getSchemaSets() {
        return schemaSets;
    }

    public List<Schema> getSchemas() {
        return schemas;
    }

    public List<VocabularyFolder> getVocabularies() {
        return vocabularies;
    }

    @DefaultHandler
    public Resolution view() {
         if (!isUserLoggedIn()) {
            return createErrorResolution(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "You have to login to access the checkouts page");
        }
        dataSets = this.checkoutsService.getDataSetsWorkingCopies(getUserName());
        dataElements = this.checkoutsService.getCommonDataElementsWorkingCopies(getUserName());
        schemaSets = this.checkoutsService.getSchemaSetsWorkingCopies(getUserName());
        schemas = this.checkoutsService.getSchemasWorkingCopies(getUserName());
        vocabularies = this.checkoutsService.getVocabulariesWorkingCopies(getUserName());
        return new ForwardResolution(CHECKOUTS_PAGE);
    }

}
