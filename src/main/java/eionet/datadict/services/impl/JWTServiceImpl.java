package eionet.datadict.services.impl;

import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;



@Service
public class JWTServiceImpl implements JWTService {

    /**
     * JWT Audience.
     */
    private static final String JWT_AUDIENCE = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);

    /**
     * JWT signature algorithm.
     */
    private static final String JWT_SIGNATURE_ALGORITHM = Props.getProperty(PropsIF.DD_VOCABULARY_ADI_JWT_ALGORITHM);

    /**
     * JWT subject.
     */
    private static final String JWT_SUBJECT = "eea";

    /**
     * JWT issuer.
     */
    private static final String JWT_ISSUER = "eea";

    /**
     * JWT api key.
     */
    private static final String JWT_API_KEY= Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_KEY);

    /**
     * Creates a valid JWT token for vocabulary rdf upload via tha API
     *
     * @param dbApiKeyValue    the API_KEY value from the database
     * @return the token
     * @throws ServiceException if any error occurs.
     */
    @Override
    public String generateJWTToken(String dbApiKeyValue) throws ServiceException{

        //The JWT will be signed with secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(JWT_API_KEY);

        //TODO use JWT_SIGNATURE_ALGORITHM
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());

        Map<String, Object> claims= new HashMap<>();
        claims.put("API_KEY", dbApiKeyValue);

        //TODO change id
        //The JWT parameters are set
        JwtBuilder builder = Jwts.builder().setId("1")
                .setIssuedAt(new Date())
                .setSubject(JWT_SUBJECT)
                .setIssuer(JWT_ISSUER)
                .setAudience(JWT_AUDIENCE)
                .setClaims(claims)
                .signWith(signingKey);

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }
}
