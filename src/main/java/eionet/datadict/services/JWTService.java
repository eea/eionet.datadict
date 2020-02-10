package eionet.datadict.services;

import eionet.meta.service.ServiceException;

public interface JWTService {

    /**
     * Creates a valid JWT token for vocabulary rdf upload via tha API
     *
     * @return the token
     * @throws ServiceException if any error occurs.
     */
    String generateJWTToken() throws ServiceException;

    String getJwtAudience();

    String getJwtSignatureAlgorithm();

    String getJwtSubject();

    String getJwtIssuer();

    String getJwtApiKey();
}
