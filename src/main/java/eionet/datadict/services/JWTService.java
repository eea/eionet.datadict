package eionet.datadict.services;

import eionet.meta.service.ServiceException;

public interface JWTService {

    /**
     * Creates a valid JWT token for vocabulary rdf upload via tha API
     *
     * @param apiKey    the API_KEY value from the database
     * @return the token
     * @throws ServiceException if any error occurs.
     */
    String generateJWTToken(String apiKey) throws ServiceException;

    String getJwtAudience();

    String getJwtSignatureAlgorithm();

    String getJwtSubject();

    String getJwtIssuer();

    String getJwtApiKey();
}
