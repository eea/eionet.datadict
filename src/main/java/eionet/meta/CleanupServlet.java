package eionet.meta;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import eionet.util.DataManipulations;
import eionet.util.LogServiceIF;
import eionet.util.SecurityUtil;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.Transaction;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class CleanupServlet extends HttpServlet{

    /** */
    private static final Logger LOGGER = Logger.getLogger(CleanupServlet.class);

    /** */
    public static final String PAR_ACTION = "action";
    public static final String PAR_OBJ_TYPE = "obj_type";
    public static final String PAR_OBJ_IDS = "obj_ids";
    public static final String ATTR_DELETE_SUCCESS = "obj_ids";
    /** */
    public static final String ACTION_CLEANUP = "Cleanup";
    public static final String ACTION_DELETE_ELM = "Delete elements";
    public static final String ACTION_DELETE_TBL = "Delete tables";
    public static final String ACTION_DELETE_DST = "Delete datasets";

    /** */
    protected LogServiceIF logger = null;

    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req,res);
    }

    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        res.setCharacterEncoding("UTF-8");
        req.removeAttribute(ATTR_DELETE_SUCCESS);

        String action = null;
        Connection conn = null;
        PrintWriter writer = null;
        DataManipulations dataManipulations = null;
        Transaction tx = null;
        try {
            writer = res.getWriter();
            guard(req);
            res.setContentType("text/plain");

            action = req.getParameter(PAR_ACTION);
            if (action==null)
                throw new Exception("Missing request parameter: " + PAR_ACTION);
            String objIDs = req.getParameter(PAR_OBJ_IDS);

            conn = ConnectionUtil.getConnection();
            if (action.equals(ACTION_CLEANUP)) {
                res.setContentType("text/plain");
                dataManipulations = new DataManipulations(conn, writer);

                tx = Transaction.begin(conn);

                dataManipulations.cleanup();

                tx.commit();

                dataManipulations.outputWriteln("");
                dataManipulations.outputWriteln("ALL DONE!");
            }
            else if (action.equals(ACTION_DELETE_ELM) || action.equals(ACTION_DELETE_TBL) || action.equals(ACTION_DELETE_DST)) {
                if (objIDs!=null && objIDs.trim().length()>0) {

                    tx = Transaction.begin(conn);
                    StringTokenizer st = new StringTokenizer(objIDs);
                    while (st.hasMoreTokens()) {

                        if (dataManipulations==null)
                            dataManipulations = new DataManipulations(conn, null);

                        String objID = st.nextToken();
                        if (action.equals(ACTION_DELETE_ELM))
                            dataManipulations.deleteElm(objID);
                        else if (action.equals(ACTION_DELETE_TBL))
                            dataManipulations.deleteTblWithElements(objID);
                        else if (action.equals(ACTION_DELETE_DST))
                            dataManipulations.deleteDstWithTablesAndElements(objID);;
                    }

                    tx.commit();

                    req.setAttribute(ATTR_DELETE_SUCCESS, "");
                    req.getRequestDispatcher("clean.jsp").forward(req,res);
                }
            }
            else
                throw new Exception("Unkown parameter value: " + action);
        }
        catch (Exception e) {

            if (tx!=null){
                tx.rollback();
            }

            LOGGER.error(e);

            if (writer!=null) {
                String trace = eionet.util.Util.getStack(e);
                writer.println(trace==null ? e.toString() : trace);
                writer.flush();
            }
        }
        finally {

            if (tx!=null)
                tx.end();

            if (writer!=null)
                writer.close();
            try {
                if (conn!=null)
                    conn.close();
            }
            catch (SQLException e) {}
        }
    }

    /**
     *
     * @param req
     * @throws Exception
     */
    private void guard(HttpServletRequest req) throws Exception {
        DDUser user = SecurityUtil.getUser(req);
        if (user == null)
            throw new Exception("Not authenticated!");
    }
}
