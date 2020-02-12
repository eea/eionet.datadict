package eionet.web.action;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.datadict.services.JWTService;
import eionet.meta.DDUser;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import eionet.meta.service.ServiceException;
import net.sf.json.JSONObject;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

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
     * Json output format.
     */
    public static final String JSON_FORMAT = "application/json";


    /**
     * Generates a valid JWT token for DD api for vocabulary update via rdf upload
     *
     * @return
     */
    @DefaultHandler
    public Resolution generateJWTToken() throws ServiceException, JsonProcessingException {
        StopWatch timer = new StopWatch();
        timer.start();
        LOGGER.info("generateJWTToken API - Began process for jwt token generation.");

        /* The request method should be POST*/
        if (!isPostRequest()) {
            throw new ServiceException("generateJWTToken API - The request method was not POST.");
        }

        HttpServletRequest request = getContext().getRequest();
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if(username == null || password == null){
            throw new ServiceException("generateJWTToken API - Credentials were missing.");
        }

        LOGGER.info(String.format("generateJWTToken API - User %s has requested generation of a JWT token.",username));

        /* TODO authenticate user
            If user doesn't exist, throw exception and return message to caller (JSON format ?)
         */
        /*DDUser user = new DDUser();
        if (!user.authenticate(username, password)) {
            throw new ServiceException("generateJWTToken API - Wrong credentials were retrieved.");
        }
        if (!user.isUserInRole("dd_admin")) {
            throw new ServiceException("generateJWTToken API - User is not admin.");
        }*/

        String generatedToken = this.getJwtService().generateJWTToken();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", generatedToken);
        String outputMsg = new ObjectMapper().writeValueAsString(jsonObject);
        StreamingResolution resolution = new StreamingResolution(JSON_FORMAT, outputMsg);

        timer.stop();
        LOGGER.info(String.format("generateJWTToken API - Generation of token for user %s was completed, total time of execution: ", username));
        return resolution;
    }

    public JWTService getJwtService() {
        return jwtService;
    }
}
