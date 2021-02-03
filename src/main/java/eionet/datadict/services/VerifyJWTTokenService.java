package eionet.datadict.services;

public interface VerifyJWTTokenService {
    Boolean verifyToken(String jsonWebToken);
}

