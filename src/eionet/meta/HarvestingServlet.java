package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

import eionet.meta.harvest.*;
import eionet.util.*;

import com.tee.xmlserver.AppUserIF;

public class HarvestingServlet extends HttpServlet {
	
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
									
		ServletOutputStream out = res.getOutputStream();
		res.setContentType("text/plain");
		
		AppUserIF user = SecurityUtil.getUser(req);
		if (user==null || !user.isAuthentic())
			throw new ServletException("User not authorized!");
		
		out.println("Harvesting. Please wait ...");
		out.flush();
		                                	
		HarvesterIF harvester = new OrgHarvester();

		try{
			harvester.harvest();
			out.println("Successfully done!!!");
		}
		catch (Exception e){
			out.println("Encountered the following exception:");
			e.printStackTrace(new PrintStream(out));
			
			LogServiceIF log = harvester.getLog();
			log.fatal("", e);
			e.printStackTrace(System.out);
			harvester.cleanup();
		}
		
		out.flush();             	
    }
}
