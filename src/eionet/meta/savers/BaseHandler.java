
package eionet.meta.savers;

import java.sql.*;
import javax.servlet.ServletContext;

import eionet.meta.*;

import com.tee.util.Util;
import com.tee.xmlserver.AppUserIF;

public abstract class BaseHandler {
	
	protected Connection conn = null;
	protected Parameters req = null;
	protected ServletContext ctx = null;
	
	protected AppUserIF user = null;
    
	protected void cleanVisuals(){
		
		String vp = ctx==null ? null : ctx.getInitParameter("visuals-path");
		if (Util.nullString(vp))
			log("cleanVisuals() failed to find visuals path!");
		
		MrProper mrProper = new MrProper(conn);
		mrProper.setUser(user);
				
		Parameters pars = new Parameters();
		pars.addParameterValue(MrProper.FUNCTIONS_PAR, MrProper.CLN_VISUALS);
		pars.addParameterValue(MrProper.VISUALS_PATH, vp);
		
		mrProper.execute(pars);
		log(mrProper.getResponse().toString());
	}
    
    protected void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}