/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.meta.service;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for JWT with auth0 library.
 *
 * @author enver
 * @see <a href="https://github.com/auth0/java-jwt">Auth0 Java JWT Implementation</a>.
 */
@Service
public class Auth0JWTServiceImpl implements IJWTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0JWTServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject verify(String secretKey, String audience, String jsonWebToken) throws ServiceException {
        try {
            JWTVerifier jwtVerifier = new JWTVerifier(secretKey, audience);
            Map<String, Object> decodedPayload = jwtVerifier.verify(jsonWebToken);
            return JSONObject.fromObject(decodedPayload);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException | IllegalStateException | SignatureException | JWTVerifyException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }//end of method verify

    /**
     * {@inheritDoc}
     */
    @Override
    public String sign(String secretKey, String audience, String jsonString, int expiryInMinutes, String algorithm) throws ServiceException {
        try {
            Map result = new ObjectMapper().readValue(jsonString, HashMap.class);
            return sign(secretKey, audience, result, expiryInMinutes, algorithm);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }//end of method sign

    /**
     * {@inheritDoc}
     */
    @Override
    public String sign(String secretKey, String audience, JSONObject json, int expiryInMinutes, String algorithm) throws ServiceException {
        if (json != null) {
            return sign(secretKey, audience, json.toString(), expiryInMinutes, algorithm);
        } else {
            return sign(secretKey, audience, (String) null, expiryInMinutes, algorithm);
        }
    }//end of method sign

    /**
     * {@inheritDoc}
     */
    @Override
    public String sign(String secretKey, String audience, Map jsonMap, int expiryInMinutes, String algorithm) throws ServiceException {
        JWTSigner jwtSigner = new JWTSigner(secretKey);
        JWTSigner.Options options = new JWTSigner.Options();
        options.setAlgorithm(getAlgorithm(algorithm));
        options.setExpirySeconds(expiryInMinutes * 60);
        options.setIssuedAt(true);
        options.setJwtId(true);
        return jwtSigner.sign(jsonMap, options);
    }//end of method sign

    /**
     * Gets or returns default algorithm for given string.
     * Note: Not using map for memory and since it is handled only in this method.
     *
     * @param algorithmStr algorithm identifier as string.
     * @return Algorithm object.
     */
    private Algorithm getAlgorithm(String algorithmStr) {
        if (StringUtils.equals("HS384", algorithmStr)) {
            return Algorithm.HS384;
        } else if (StringUtils.equals("HS256", algorithmStr)) {
            return Algorithm.HS256;
        }

        return Algorithm.HS512;
    }//end of method getAlgorithm

}//end of class IJWTService
