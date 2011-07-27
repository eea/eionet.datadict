package eionet.meta.savers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;

import eionet.util.Util;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class RodLinksHandler extends BaseHandler{

    /**
     * @param conn
     * @param ctx
     */
    public RodLinksHandler(Connection conn, ServletContext ctx) {
        this.conn = conn;
        this.ctx = ctx;
    }

    /* (non-Javadoc)
     * @see eionet.meta.savers.BaseHandler#execute_()
     */
    public void execute_() throws Exception {

        String dstID = httpServletRequest.getParameter("dst_id");
        if (dstID==null)
            throw new Exception("RodLinksHandler: dstID is missing!");

        String mode = httpServletRequest.getParameter("mode");
        if (mode==null)
            throw new Exception("RodLinksHandler: mode is missing!");
        else if (mode.equals("add"))
            addRodLinks(dstID);
        else if (mode.equals("rmv"))
            rmvRodLinks(dstID);
        else
            throw new Exception("RodLinksHandler: unknown mode " + mode);
    }

    /**
     * @param dstID
     * @throws Exception
     */
    private void addRodLinks(String dstID) throws Exception {

        String raID = httpServletRequest.getParameter("ra_id");
        if (Util.voidStr(raID))
            throw new Exception("ra_id is missing!");

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("select count(*) from DST2ROD where DATASET_ID=").
        append(inParams.add(dstID, Types.INTEGER)).append(" and ACTIVITY_ID=").append(inParams.add(raID, Types.INTEGER));

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new Exception("This dataset is already linked with this obligation!");
            else
                rs.close();

            String raTitle = httpServletRequest.getParameter("ra_title");
            String liID = httpServletRequest.getParameter("li_id");
            String liTitle = httpServletRequest.getParameter("li_title");

            inParams = new INParameters();
            buf = new StringBuffer("select count(*) from ROD_ACTIVITIES where ");
            buf.append("ACTIVITY_ID=").append(inParams.add(raID, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {

                inParams = new INParameters();
                LinkedHashMap map = new LinkedHashMap();

                map.put("ACTIVITY_ID", inParams.add(raID, Types.INTEGER));
                if (raTitle!=null)
                    map.put("ACTIVITY_TITLE", inParams.add(raTitle));
                if (liID!=null)
                    map.put("LEGINSTR_ID", inParams.add(liID, Types.INTEGER));
                if (liTitle!=null)
                    map.put("LEGINSTR_TITLE", inParams.add(liTitle));

                SQL.executeUpdate(SQL.insertStatement("ROD_ACTIVITIES", map), inParams, conn);
            }

            inParams = new INParameters();
            LinkedHashMap map = new LinkedHashMap();
            map.put("DATASET_ID", inParams.add(dstID, Types.INTEGER));
            map.put("ACTIVITY_ID", inParams.add(raID, Types.INTEGER));
            SQL.executeUpdate(SQL.insertStatement("DST2ROD", map), inParams, conn);
        }
        catch (Exception e) {
            try {
                if (rs != null)   rs.close();
                if (stmt != null) stmt.close();
            }
            catch (SQLException sqlee) {}
        }
    }

    /**
     * @param dstID
     * @throws Exception
     */
    private void rmvRodLinks(String dstID) throws Exception {

        String[] raIDs = httpServletRequest.getParameterValues("del_id");
        if (raIDs==null || raIDs.length==0)
            throw new Exception("ra_id is missing!");

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("delete from DST2ROD where DATASET_ID=");
        buf.append(dstID).append(" and (");
        for (int i=0; i<raIDs.length; i++) {
            if (i>0)
                buf.append(" or ");
            buf.append("ACTIVITY_ID=").append(inParams.add(raIDs[i], Types.INTEGER));
        }
        buf.append(")");

        SQL.executeUpdate(buf.toString(), inParams, conn);
    }
}
