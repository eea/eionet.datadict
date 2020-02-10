package eionet.datadict.services.impl;

import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Calendar;
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
    //TODO check if this is taken from db
    private String JWT_SUBJECT = "eea";

    /**
     * JWT issuer.
     */
    //TODO check if this is taken from db
    private String JWT_ISSUER = "eea";

    /**
     * JWT api key.
     */
    private String JWT_API_KEY= Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_KEY);

    /**
     * Creates a valid JWT token for vocabulary rdf upload via tha API
     *
     * @return the token
     * @throws ServiceException if any error occurs.
     */
    @Override
    public String generateJWTToken() throws ServiceException{

        //The JWT will be signed with secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(this.getJwtApiKey());

        //TODO use JWT_SIGNATURE_ALGORITHM
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());

        Date now = new Date();
        Calendar cal=Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.YEAR,1);

        Map<String, Object> headerParams= new HashMap<>();
        headerParams.put("iat", now.getTime());
        headerParams.put("exp", cal.getTime().getTime());
        headerParams.put("iss", this.getJwtIssuer());
        headerParams.put("sub", this.getJwtSubject());
        headerParams.put("aud", this.getJwtAudience());

        //The JWT parameters are set
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setSubject(this.getJwtSubject())
                .setIssuer(this.getJwtIssuer())
                .setAudience(this.getJwtAudience())
                .setHeaderParams(headerParams)
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
}
