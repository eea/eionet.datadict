package eionet.datadict.services;


public interface JWTService {

    /**
     * Creates a valid JWT token for vocabulary rdf upload via tha API
     *
     * @return the token
     */
    String generateJWTToken();

    String getJwtAudience();

    String getJwtSignatureAlgorithm();

    String getJwtSubject();

    String getJwtIssuer();

    String getJwtApiKey();

    String getDD_URL();
}
