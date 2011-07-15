package eionet.meta.inservices;

import javax.servlet.http.HttpServletRequest;

public interface InServiceClientIF {
    
    public abstract void execute(HttpServletRequest req) throws Exception;
}
