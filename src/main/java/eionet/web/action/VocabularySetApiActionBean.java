package eionet.web.action;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.model.VocabularySet;
import eionet.datadict.services.auth.WebApiAuthInfoService;
import eionet.datadict.services.auth.WebApiAuthService;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@UrlBinding("/api/vocabularysets/{identifier}/{$event}")
public class VocabularySetApiActionBean extends AbstractActionBean {

    public static final String JSON_FORMAT = "application/json";

    @SpringBean
    private WebApiAuthInfoService webApiAuthInfoService;
    @SpringBean
    private WebApiAuthService webApiAuthService;
    @SpringBean
    private VocabularyDataService vocabularyDataService;

    private String identifier;
    private String label;

    public Resolution createVocabularySet() {
        Thread.currentThread().setName("ADD-VOCABULARYSET");
        MDC.put("sessionId", getContext().getRequest().getSession().getId().substring(0,16));
        try {
            this.webApiAuthService.authenticate(this.webApiAuthInfoService.getAuthenticationInfo(getContext().getRequest()));
        } catch (UserAuthenticationException ex) {
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, ex.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        }
        VocabularySet vocabularySet = new VocabularySet();
        vocabularySet.setIdentifier(this.identifier);
        vocabularySet.setLabel(this.label);
        final VocabularySet created;

        try {
            created = this.vocabularyDataService.createVocabularySet(vocabularySet);
        } catch (EmptyParameterException ex) {
            LOGGER.info(ex.getMessage(), ex);
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, ex.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        } catch (DuplicateResourceException ex) {
            LOGGER.info(ex.getMessage(), ex);
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.CONFLICT, ex.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        }

        StreamingResolution result = new StreamingResolution(JSON_FORMAT) {

            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                List<String> messages = new ArrayList<String>();
                messages.add(String.format("Successfully created vocabulary set %s", created.getIdentifier()));
                VocabularyJSONOutputHelper.writeJSON(response.getOutputStream(), messages);
            }

        };

        return result;
    }

    public WebApiAuthInfoService getWebApiAuthInfoService() {
        return webApiAuthInfoService;
    }

    public void setWebApiAuthInfoService(WebApiAuthInfoService webApiAuthInfoService) {
        this.webApiAuthInfoService = webApiAuthInfoService;
    }

    public WebApiAuthService getWebApiAuthService() {
        return webApiAuthService;
    }

    public void setWebApiAuthService(WebApiAuthService webApiAuthService) {
        this.webApiAuthService = webApiAuthService;
    }

    public VocabularyDataService getVocabularyDataService() {
        return vocabularyDataService;
    }

    public void setVocabularyDataService(VocabularyDataService vocabularyDataService) {
        this.vocabularyDataService = vocabularyDataService;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
