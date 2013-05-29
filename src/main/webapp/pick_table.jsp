<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!private Vector selected=null;%>


<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    ServletContext ctx = getServletContext();

    Connection conn = null;
    DDUser user = SecurityUtil.getUser(request);

    try { // start the whole page try block

    conn = ConnectionUtil.getConnection();

    String short_name = request.getParameter("short_name");
    String idfier = request.getParameter("idfier");
    String full_name = request.getParameter("full_name");
    String definition = request.getParameter("definition");

    Integer oSortCol=null;
    Integer oSortOrder=null;
    try {
        oSortCol=new Integer(request.getParameter("sort_column"));
        oSortOrder=new Integer(request.getParameter("sort_order"));
    }
    catch(Exception e){
        oSortCol=null;
        oSortOrder=null;
    }

    String searchType=request.getParameter("SearchType");

    String tableLink="";

    String sel = request.getParameter("selected");

    String backUrl = "search_table.jsp?ctx=popup&amp;selected=" + sel;
    String id=null;
    selected= new Vector();
    if (sel!=null && sel.length()>0){
        int i=sel.indexOf("|");
        while (i>0){
            id = sel.substring(0, i);
            sel = sel.substring(i+1);
            selected.add(id);
            i=sel.indexOf("|");
        }
    }

    if (sel==null) sel="";

    DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
    searchEngine.setUser(user);

    String srchType = request.getParameter("search_precision");
    String oper="=";
    if (srchType != null && srchType.equals("free"))
        oper=" match ";
    if (srchType != null && srchType.equals("substr"))
        oper=" like ";

    Vector params = new Vector();
    Enumeration parNames = request.getParameterNames();
    while (parNames.hasMoreElements()){
        String parName = (String)parNames.nextElement();
        if (!parName.startsWith(ATTR_PREFIX))
            continue;

        String parValue = request.getParameter(parName);
        if (parValue.length()==0)
            continue;

        DDSearchParameter param =
            new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");
        if (oper!= null && oper.trim().equalsIgnoreCase("like"))
            param.addValue("'%" + parValue + "%'");
        else
            param.addValue("'" + parValue + "'");
        params.add(param);
    }

    Vector dsTables = searchEngine.getDatasetTables(params, short_name, idfier, full_name, definition, oper);

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <%@ include file="headerinfo.jsp" %>
        <title>Meta</title>
        <script type="text/javascript">
        // <![CDATA[
            function pickTable(id, i, name) {
                if (opener && !opener.closed) {
                    if (window.opener.pickTable(id, name)==true)  //window opener should have function pickTABLE with 2 params - tbl id & tbl name
                                                                // and if it returns true, then the popup window is closed,
                                                                // otherwise multiple selection is allowed
                        closeme();
                    hideRow(i);
                } else {
                    alert("You have closed the main window.\n\nNo action will be taken.")
                }
            }
            function hideRow(i){
                var t = document.getElementById("tbl");
                var row = t.getElementsByTagName("TR")[i+1];
                row.style.display = "none";
            }
            function closeme(){
                window.close()
            }
        // ]]>
        </script>
    </head>
    <!--script language="javascript" for="window" event="onload">
    </script-->

<body class="popup">

    <div id="pagehead">
        <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
        <div id="networktitle">Eionet</div>
        <div id="sitetitle">Data Dictionary (DD)</div>
        <div id="sitetagline">This service is part of Reportnet</div>
    </div> <!-- pagehead -->
    <div id="operations" style="margin-top:10px">
        <ul>
            <li><a href="javascript:window.close();">Close</a></li>
            <li><a href="<%=backUrl%>">back to search</a></li>
        </ul>
    </div>

<div id="workarea">
    <form id="form1" action="">
        <h5>Select dataset table:</h5>

        <table id="tbl" class="datatable">
            <thead>
                <tr>
                    <th align="left" style="padding-left:5;padding-right:10">Short name</th>
                    <th align="left" style="padding-right:10">Dataset</th>
                    <th align="left" style="padding-right:10">Full name</th>
                </tr>
            </thead>
            <tbody>
             <%
                    if (dsTables == null || dsTables.size()==0){
                    %>
                        <tr><td colspan="4"><b>No results found!</b></td></tr></tbody></table></form></div></td></tr></table></body></html>
                    <%
                        return;
                    }

                    int c=0;
                    DElemAttribute attr = null;

                    for (int i=0; i<dsTables.size(); i++){
                        DsTable table = (DsTable)dsTables.get(i);
                        String table_id = table.getID();
                        String table_name = table.getShortName();
                        String ds_id = table.getDatasetID();
                        String ds_name = null;
                        if (ds_id!=null){
                            Dataset ds = (Dataset)searchEngine.getDataset(ds_id);
                            ds_name = ds.getShortName();
                        }

                        if (table_name == null) table_name = "unknown";
                        if (table_name.length() == 0) table_name = "empty";

                        if (ds_name == null || ds_name.length() == 0) ds_name = "unknown";

                        //String fullName = table.getName();
                        String tblName = "";

                        Vector attributes = searchEngine.getAttributes(table_id, "T", DElemAttribute.TYPE_SIMPLE);

                        for (int j=0; j<attributes.size(); j++){
                            attr = (DElemAttribute)attributes.get(j);
                            if (attr.getName().equalsIgnoreCase("Name"))
                                tblName = attr.getValue();
                        }

                        String tblFullName = tblName;
                        tblName = tblName.length()>60 && tblName != null ? tblName.substring(0,60) + " ..." : tblName;

                        String trStyle = (i%2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
                        c++;
                    %>

                    <tr <%=trStyle%>>
                        <td align="left" style="padding-left:5;padding-right:10">
                            <a href="#" onclick="pickTable(<%=table_id%>, <%=c%>, '<%=table_name%>')">
                            <%=Util.processForDisplay(table_name)%></a>
                        </td>
                        <td align="left" style="padding-right:10"><%=Util.processForDisplay(ds_name)%></td>
                        <td align="left" style="padding-right:10"><%=Util.processForDisplay(tblName)%></td>
                    </tr>

                <%
            }
            %>
            </tbody>
        </table>

     </form>
</div>
</body>
</html>

<%
// end the whole page try block
}
finally {
    try { if (conn!=null) conn.close();
    } catch (SQLException e) {}
}
%>
