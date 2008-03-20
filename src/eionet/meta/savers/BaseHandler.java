
package eionet.meta.savers;

import java.sql.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import eionet.meta.*;
import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;

import com.tee.util.Util;
import eionet.util.DbTransactionPolite;

/**
 * @author Jaanus Heinlaid
 */
public abstract class BaseHandler {

	/** */
	protected Connection conn = null;
	protected Parameters req = null;
	protected ServletContext ctx = null;
	protected HttpServletRequest httpServletRequest = null;
	
	/** */
	protected DDUser user = null;
	protected static LogServiceIF logger = new Log4jLoggerImpl();

	/**
	 * 
	 * @throws Exception
	 */
	public void execute() throws Exception {

		DbTransactionPolite tx = DbTransactionPolite.start(conn);
		try{
    		execute_();
    		tx.commit();
    	}
    	catch (Exception e){
    		tx.rollback();
    		throw e;
    	}
    	finally{    		
    		tx.end();
    	}
	}

	/**
	 * 
	 * @param pars
	 * @throws Exception
	 */
	public void execute(Parameters pars) throws Exception {
		this.req = pars;
		execute();
	}

	/**
	 * 
	 * @param pars
	 * @throws Exception
	 */
	public void execute(HttpServletRequest req) throws Exception {
		this.httpServletRequest = req;
		execute();
	}
	
	/**
	 *
	 */
	protected void cleanVisuals(){
		
		String vp = ctx==null ? null : ctx.getInitParameter("visuals-path");
		if (Util.nullString(vp))
			logger.error("cleanVisuals() failed to find visuals path!");
		
		MrProper mrProper = new MrProper(conn);
		mrProper.setUser(user);
				
		Parameters pars = new Parameters();
		pars.addParameterValue(MrProper.FUNCTIONS_PAR, MrProper.CLN_VISUALS);
		pars.addParameterValue(MrProper.VISUALS_PATH, vp);
		
		mrProper.execute(pars);
		logger.debug(mrProper.getResponse().toString());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public abstract void execute_() throws Exception;
}
