package eionet.web.action;


import eionet.datadict.services.JWTService;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.DDApiKey;
import eionet.meta.service.IApiKeyService;
import eionet.meta.service.ServiceException;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * JWT Api Action Bean
 *
 * @author enver
 */
@UrlBinding("/api/jwt")
public class JWTApiActionBean extends AbstractActionBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTApiActionBean.class);

    /**
     * jwt service.
     */
    @SpringBean
    private JWTService jwtService;


    /**
     * Generates a valid JWT token for DD api for vocabulary update via rdf upload
     *
     * @return
     */
    @DefaultHandler
    public String generateJWTToken() throws ServiceException {
        StopWatch timer = new StopWatch();
        timer.start();
        LOGGER.info("generateJWTToken API - Began process for jwt token generation.");

        /* TODO get username & password and validate that user is admin
            If not, throw exception and return message to caller (JSON format ?)
         */

        /* The request method should be POST*/
        if (!isPostRequest()) {
            throw new ServiceException("generateJWTToken API - The request method was not POST.");
        }

        HttpServletRequest request = getContext().getRequest();
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        LOGGER.info("username: " + username);
        LOGGER.info("password: " + password);

        DDUser user = new DDUser();
        if (!user.authenticate(username, password)) {
            throw new ServiceException("generateJWTToken API - Wrong credentials were retrieved.");
        }
        if (!user.isUserInRole("dd_admin")) {
            throw new ServiceException("generateJWTToken API - User is not admin.");
        }


        String generatedToken = this.getJwtService().generateJWTToken();


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
