package eionet.datadict.services.auth;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class WebApiAuthInfo {

    private final String authenticationToken;
    private String remoteAddress;
    private String remoteHost;
    
    public WebApiAuthInfo(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public WebApiAuthInfo(String authenticationToken, String remoteHost, String remoteAddress) {
        this(authenticationToken);
        this.remoteHost = remoteHost;
        this.remoteAddress = remoteAddress;
    }
    
    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    
}
