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

import net.sf.json.JSONObject;

import java.util.Map;

/**
 * Service for JWT.
 *
 * @author enver
 */
public interface IJWTService {

    /**
     * Verify a JWT using secret key.
     *
     * @param secretKey    a secret key to verify.
     * @param audience     audience.
     * @param jsonWebToken jwt value.
     * @return resolve json object.
     * @throws ServiceException if any error occurs.
     */
    JSONObject verify(String secretKey, String audience, String jsonWebToken) throws ServiceException;

    /**
     * Sign (encode) a json to create json web token.
     *
     * @param secretKey       a secret key to sign.
     * @param audience        audience.
     * @param jsonString      json as string.
     * @param expiryInMinutes expiration time in minutes (added to creation time).
     * @param algorithm       Algorithm as string value, supported algorithms are HS256, HS384 and HS512. Default is HS512.
     * @return encoded json web token.
     * @throws ServiceException if any error occurs.
     */
    String sign(String secretKey, String audience, String jsonString, int expiryInMinutes, String algorithm) throws ServiceException;

    /**
     * Sign (encode) a json to create json web token.
     *
     * @param secretKey       a secret key to sign.
     * @param audience        audience.
     * @param json            json as object.
     * @param expiryInMinutes expiration time in minutes (added to creation time).
     * @param algorithm       Algorithm as string value, supported algorithms are HS256, HS384 and HS512. Default is HS512.
     * @return encoded json web token.
     * @throws ServiceException if any error occurs.
     */
    String sign(String secretKey, String audience, JSONObject json, int expiryInMinutes, String algorithm) throws ServiceException;

    /**
     * Sign (encode) a json to create json web token.
     *
     * @param secretKey       a secret key to sign.
     * @param audience        audience.
     * @param jsonMap         json as map.
     * @param expiryInMinutes expiration time in minutes (added to creation time).
     * @param algorithm       Algorithm as string value, supported algorithms are HS256, HS384 and HS512. Default is HS512.
     * @return encoded json web token.
     * @throws ServiceException if any error occurs.
     */
    String sign(String secretKey, String audience, Map jsonMap, int expiryInMinutes, String algorithm) throws ServiceException;
}//end of class IJWTService
