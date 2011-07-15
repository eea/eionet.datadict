package eionet.meta;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.harvest.HarvesterIF;
import eionet.meta.harvest.OrgHarvester;
import eionet.util.LogServiceIF;
import eionet.util.SecurityUtil;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class HarvestingServlet extends HttpServlet {

    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        ServletOutputStream out = res.getOutputStream();
        res.setContentType("text/plain");

        DDUser user = SecurityUtil.getUser(req);
        if (user==null || !user.isAuthentic())
            throw new ServletException("User not authorized!");

        out.println("Harvesting. Please wait ...");
        out.flush();

        HarvesterIF harvester = new OrgHarvester();

        try {
            harvester.harvest();
            out.println("Successfully done!!!");
        }
        catch (Exception e) {
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
