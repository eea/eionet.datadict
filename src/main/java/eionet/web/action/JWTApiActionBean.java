package eionet.web.action;


import eionet.datadict.services.JWTService;
import eionet.meta.dao.domain.DDApiKey;
import eionet.meta.service.IApiKeyService;
import eionet.meta.service.ServiceException;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT Api Action Bean
 *
 * @author enver
 */
@UrlBinding("/api/jwt")
public class JWTApiActionBean extends AbstractActionBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTApiActionBean.class);

    //Instance members
    /**
     * jwt service.
     */
    @SpringBean
    private JWTService jwtService;

    /**
     * API-Key service.
     */
    @SpringBean
    private IApiKeyService apiKeyService;


    /**
     * Generates a valid JWT token for DD api for vocabulary update via rdf upload
     *
     * @return
     */
    public String generateJWTToken() throws ServiceException {
        StopWatch timer = new StopWatch();
        timer.start();
        LOGGER.info("generateJWTToken API - Began process for jwt token generation");

        /* TODO get username & password and validate that user is admin
            If not, throw exception and return message to caller (JSON format ?)
         */

        /* TODO get keyvalue from request
         */
        String requestedApiKeyValue = "1";

        /* TODO if key value is retrieved from request and scope is not used for subject & issuer, then the following is not needed
         */
        DDApiKey ddApiKey = apiKeyService.getApiKey(requestedApiKeyValue);

        /* TODO call jwtService.generateJWTToken with db values retrieved as parameters
         */
        String generatedToken = this.getJwtService().generateJWTToken(requestedApiKeyValue);




        timer.stop();
        LOGGER.info("generateJWTToken API - Generation of token was completed, total time of execution: " + timer.toString());

        /* TODO Send user the retrieved token (JSON format ?)
         */
        return generatedToken;
    }

    public JWTService getJwtService() {
        return jwtService;
    }
}
