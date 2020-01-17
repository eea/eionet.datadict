package eionet.datadict.services.auth.impl;

import eionet.datadict.services.auth.WebApiAuthInfo;
import eionet.datadict.services.auth.WebApiAuthService;
import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.meta.dao.domain.DDApiKey;
import eionet.meta.service.ConfigurationPropertyValueProvider;
import eionet.meta.service.IApiKeyService;
import eionet.meta.service.IJWTService;
import eionet.meta.service.ServiceException;
import eionet.util.PropsIF;
import java.util.Calendar;
import java.util.Date;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class WebApiAuthServiceImpl implements WebApiAuthService {

    public static final String JWT_ISSUER = "iss";
    public static final String API_KEY_IDENTIFIER_IN_JSON = "API_KEY";
    public static final String TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON = "iat";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApiAuthServiceImpl.class);

    private final IJWTService jwtService;
    private final IApiKeyService apiKeyService;
    private final ConfigurationPropertyValueProvider configurationPropertyValueProvider;

    @Autowired
    public WebApiAuthServiceImpl(IJWTService jwtService, IApiKeyService apiKeyService, ConfigurationPropertyValueProvider configurationPropertyValueProvider) {
        this.jwtService = jwtService;
        this.apiKeyService = apiKeyService;
        this.configurationPropertyValueProvider = configurationPropertyValueProvider;
    }
    
    @Override
    public DDUser authenticate(WebApiAuthInfo contextInfo) throws UserAuthenticationException {
        if (StringUtils.isBlank(contextInfo.getAuthenticationToken())) {
            throw new UserAuthenticationException("API Key missing");
        }
        
        String tokenUser;

        try {
            JSONObject jsonObject = jwtService.verify(this.getJwtKey(), this.getJwtAudience(), contextInfo.getAuthenticationToken());
            
            long createdTimeInSeconds = this.getDateCreatedInSeconds(jsonObject);
            long nowInSeconds = Calendar.getInstance().getTimeInMillis() / 1000l;

            // Check if Token is expired because it was created before a specific time limit
            if (nowInSeconds > (createdTimeInSeconds + (this.getJwtTimeoutMinutes() * 60))) {
                throw new UserAuthenticationException("Deprecated token");
            }

            String apiKey = this.getApiKey(jsonObject);
            DDApiKey ddApiKey = apiKeyService.getApiKey(apiKey);

            if (ddApiKey == null) {
                throw new UserAuthenticationException("Invalid key");
            }

            // Note: Scope can also be used
            if (ddApiKey.getExpires() != null) {
                Date now = Calendar.getInstance().getTime();

                if (now.after(ddApiKey.getExpires())) {
                    throw new UserAuthenticationException("Expired key");
                }
            }

            tokenUser = this.getTokenUser(jsonObject);

            if (StringUtils.isEmpty(tokenUser)) {
                throw new UserAuthenticationException("Empty jwt issuer");
            }

        } catch (ServiceException e) {
            LOGGER.error("Create Vocabulary API- Cannot verify key", e);
            throw new UserAuthenticationException(e.getMessage());
        }
        
        return new DDUser(tokenUser, true);
    }
    
    protected String getJwtKey() {
        return this.configurationPropertyValueProvider.getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_KEY);
    }
    
    protected String getJwtAudience() {
        return this.configurationPropertyValueProvider.getPropertyValue(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);
    }
    
    protected int getJwtTimeoutMinutes() {
        return this.configurationPropertyValueProvider.getPropertyIntValue(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES);
    }

    protected long getDateCreatedInSeconds(JSONObject jwtVerifyResult) {
        return jwtVerifyResult.getLong(TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON);
    }
    
    protected String getApiKey(JSONObject jwtVerifyResult) {
        if (!jwtVerifyResult.containsKey(API_KEY_IDENTIFIER_IN_JSON)) {
            return "";
        }
        
        return jwtVerifyResult.getString(API_KEY_IDENTIFIER_IN_JSON);
    }

    protected String getTokenUser(JSONObject jwtVerifyResult) {
        if (!jwtVerifyResult.containsKey(JWT_ISSUER)) {
            return null;
        }
        
        return jwtVerifyResult.getString(JWT_ISSUER);
    }
    
}
