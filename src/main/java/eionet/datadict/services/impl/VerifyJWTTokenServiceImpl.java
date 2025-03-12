package eionet.datadict.services.impl;

import eionet.datadict.services.VerifyJWTTokenService;
import eionet.meta.service.Auth0JWTServiceImpl;
import eionet.meta.service.IJWTService;
import eionet.util.Props;
import eionet.util.PropsIF;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class VerifyJWTTokenServiceImpl implements VerifyJWTTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyJWTTokenServiceImpl.class);

    /**
     * Created time identifier in json.
     */
    public static final String TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON = "iat";

    /**
     * DOMAIN identifier in json.
     */
    public static final String DOMAIN = "domain";

    /**
     * JWT Key.
     */
    private static final String JWT_KEY = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_KEY);

    /**
     * JWT Audience.
     */
    private static final String JWT_AUDIENCE = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);

    /**
     * JWT Timeout in minutes for verification (used to validate if sent token is still active or deprecated).
     */
    private static final int JWT_TIMEOUT_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES);

    /**
     * JWT service.
     */
    private IJWTService jwtService = new Auth0JWTServiceImpl();

    @Override
    public Boolean verifyToken(String jsonWebToken){
        try {
            JSONObject jsonObject = jwtService.verify(JWT_KEY, JWT_AUDIENCE, jsonWebToken);

            long createdTimeInSeconds = jsonObject.getLong(TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON);

            long nowInSeconds = Calendar.getInstance().getTimeInMillis() / 1000l;
            if (nowInSeconds > (createdTimeInSeconds + (JWT_TIMEOUT_IN_MINUTES * 60))) {
                LOGGER.error("uploadRdf API - Deprecated token");
                return false;
            }

            /* Check if the domain that the token was generated in, is the same as this one. */
            String domain = null;
            try{
                domain = jsonObject.getString(DOMAIN);
            }
            catch(Exception e){
                LOGGER.error("uploadRdf API - The token does not include domain information");
                return false;
            }

            if (!Props.getProperty(PropsIF.DD_URL).equals(domain)) {
                LOGGER.error("uploadRdf API - The token was not generated from this domain");
                return false;
            }

        } catch (Exception e) {
            LOGGER.error("uploadRdf API - Cannot verify key", e);
            return false;
        }
        return true;
    }
}














