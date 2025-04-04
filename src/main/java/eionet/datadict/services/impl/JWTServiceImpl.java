package eionet.datadict.services.impl;

import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class JWTServiceImpl implements JWTService {

    /**
     * JWT Audience.
     */
    private String JWT_AUDIENCE = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);

    /**
     * JWT signature algorithm.
     */
    private String JWT_SIGNATURE_ALGORITHM = Props.getProperty(PropsIF.DD_VOCABULARY_ADI_JWT_ALGORITHM);

    /**
     * JWT subject.
     */
    private String JWT_SUBJECT = "eea";

    /**
     * JWT issuer.
     */
    private String JWT_ISSUER = "eea";

    /**
     * JWT api key.
     */
    private String JWT_API_KEY= Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_KEY);

    /**
     * JWT api key.
     */
    private String DD_URL= Props.getProperty(PropsIF.DD_URL);

    /**
     * Creates a valid JWT token for vocabulary rdf upload via tha API
     *
     * @return the token
     * @throws ServiceException if any error occurs.
     */
    @Override
    public String generateJWTToken() {

        //The JWT will be signed with secret
        byte[] apiKeySecretBytes = this.getJwtApiKey().getBytes();

        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());

        Date now = new Date();

        Map<String, Object> claims= new HashMap<>();
        claims.put("iat", now.getTime());
        claims.put("iss", this.getJwtIssuer());
        claims.put("sub", this.getJwtSubject());
        claims.put("aud", this.getJwtAudience());
        claims.put("domain", this.getDD_URL());

        //The JWT parameters are set
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .signWith(signingKey);

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    @Override
    public String getJwtAudience() {
        return JWT_AUDIENCE;
    }

    @Override
    public String getJwtSignatureAlgorithm() {
        return JWT_SIGNATURE_ALGORITHM;
    }

    @Override
    public String getJwtSubject() {
        return JWT_SUBJECT;
    }

    @Override
    public String getJwtIssuer() {
        return JWT_ISSUER;
    }

    @Override
    public String getJwtApiKey() {
        return JWT_API_KEY;
    }

    @Override
    public String getDD_URL() {
        return DD_URL;
    }
}
