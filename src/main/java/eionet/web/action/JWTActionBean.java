package eionet.web.action;

import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UrlBinding("/jwt")
public class JWTActionBean extends AbstractActionBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTActionBean.class);

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
        LOGGER.info("generateJWTToken - Began process for jwt token generation.");
        String generatedToken = this.getJwtService().generateJWTToken();
        timer.stop();
        LOGGER.info("generateJWTToken - Generation of token was completed, total time of execution: " + timer.toString());
        return generatedToken;
    }

    public JWTService getJwtService() {
        return jwtService;
    }
}
