package eionet.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;

/**
 * Bean for binding a vocabulary to a data element of this type (CH3).
 *
 * @author Kaido Laine
 */
@UrlBinding("/bindvocabulary")
public class BindVocabularyActionBean extends AbstractActionBean {

    /** search results jsp name. */
    private static final String SEARCH_RESULTS_JSP = "bind_vocabularies.jsp";

    /** data element jsp. */
    private static final String VIEW_DATAELEMENT_JSP = "data_element.jsp";


    /** vocabulary search filter. */
    private VocabularyFilter vocabularyFilter;

    /** vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** vocabularies search result. */
    private VocabularyResult vocabularies;

    /**
     * Data element ID that needs bound vocabulary.
     */
    private String elementId;

    /**
     * search and list vocabularies.
     * @return Stripes resolution
     * @throws ServiceException if search fails
     */
    @DefaultHandler
    public Resolution search() throws ServiceException {
        if (vocabularyFilter == null) {
            vocabularyFilter  = new VocabularyFilter();
        }
        //search only released vocabularies
        vocabularyFilter.setStatus(RegStatus.RELEASED);

        vocabularies = vocabularyService.searchVocabularies(vocabularyFilter);

        return new ForwardResolution(SEARCH_RESULTS_JSP);

    }

    /**
     * Binds a vocabulary to the data element.
     * @return data element page
     * @throws ServiceException if binding does not succeed
     */
    public Resolution bind() throws ServiceException {

        String vocabularyId = getContext().getRequestParameter("vocabularyId");

        vocabularyService.bindVocabulary(Integer.valueOf(elementId), Integer.valueOf(vocabularyId));

        return new ForwardResolution(VIEW_DATAELEMENT_JSP).addParameter("delem_id", elementId);

    }


    public VocabularyFilter getVocabularyFilter() {
        return vocabularyFilter;
    }


    public void setVocabularyFilter(VocabularyFilter vocabularyFilter) {
        this.vocabularyFilter = vocabularyFilter;
    }


    public VocabularyResult getVocabularies() {
        return vocabularies;
    }


    public void setVocabularies(VocabularyResult vocabularies) {
        this.vocabularies = vocabularies;
    }


    public String getElementId() {
        return elementId;
    }


    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
