package eionet.meta.inservices;

import javax.servlet.http.*;

public interface InServiceClientIF {
	
	public abstract void execute(HttpServletRequest req) throws Exception;
}
