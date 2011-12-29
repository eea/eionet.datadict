<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

    <%
    // implementation of the servlet's service method
    //////////////////////////////////////////////////

    request.setCharacterEncoding("UTF-8");

    // ensure the page is not cached
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    ServletContext ctx = getServletContext();
    DDUser user = SecurityUtil.getUser(request);

    // POST request not allowed for anybody who hasn't logged in
    if (request.getMethod().equals("POST") && user==null){
        request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    // get values of several request parameters:
    // - dataset id number
    String dsID = request.getParameter("ds_id");
    if (dsID == null || dsID.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
    String dsName = request.getParameter("ds_name");

    //// handle the POST request //////////////////////
    //////////////////////////////////////////////////
    if (request.getMethod().equals("POST")){

        Connection userConn = null;
        DsTableHandler handler = null;
        try{
            userConn = user.getConnection();
            handler = new DsTableHandler(userConn, request, ctx);
            handler.setUser(user);

            try{
                handler.execute();
            }
            catch (Exception e){
                String msg = e.getMessage();

                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(bytesOut));
                String trace = bytesOut.toString(response.getCharacterEncoding());

                String backLink = history.getBackUrl();

                request.setAttribute("DD_ERR_MSG", msg);
                request.setAttribute("DD_ERR_TRC", trace);
                request.setAttribute("DD_ERR_BACK_LINK", backLink);

                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        }
        finally{
            try { if (userConn!=null) userConn.close();
            } catch (SQLException e) {}
        }

        // disptach the POST request
        String redirUrl = "dstables.jsp?ds_id=" + dsID;
        if (dsName!=null && dsName.length()>0)
            redirUrl = redirUrl + "&ds_name=" + dsName;
        response.sendRedirect(redirUrl);
        return;
    }
    //// end of handle the POST request //////////////////////
    // any code below must not be reached when POST request!!!

    Connection conn = null;

    Vector tables = null;
    Vector attributes=null;
    String workingUser = null;

    // the whole page's try block
    try {

        // get db connection, init search engine object
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
        searchEngine.setUser(user);

        // get the dataset object
        Dataset dataset = searchEngine.getDataset(dsID);
        if (dataset==null){
            request.setAttribute("DD_ERR_MSG", "No dataset found with this id number: " + dsID);
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        // get values for some parameters based on dataset object
        workingUser = dataset.getWorkingUser();
        dsName = dataset.getShortName();
        tables = searchEngine.getDatasetTables(dsID, true);
        boolean editPrm = user!=null && dataset.isWorkingCopy() && workingUser!=null && workingUser.equals(user.getUserName());
    %>

<%
// start HTML //////////////////////////////////////////////////////////////
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Meta</title>

    <script src="dynamic_table.js" type="text/javascript"></script>
    <script src="modal_dialog.js" type="text/javascript"></script>

    <script type="text/javascript">
    // <![CDATA[
            function submitForm(mode){

                if (mode=="delete"){
                    var b = confirm("This will delete all the tables you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
                    if (b==false) return;
                }

                document.forms["form1"].elements["mode"].value = mode;
                document.forms["form1"].submit();
            }

            function saveOrder(){
                tbl_obj.insertNumbers("pos_");
                submitForm("edit_order");
            }
            function start() {
                tbl_obj=new dynamic_table("tbl"); //create dynamic_table object
            }
            function sel_row(o){
                tbl_obj.selectRow(o);
            }
            function moveRowUp(){
                tbl_obj.moveup();
                setChanged();
            }
            function moveRowDown(){
                tbl_obj.movedown();
                setChanged();
            }
            function moveFirst(){
                tbl_obj.movefirst();
                setChanged();
            }
            function moveLast(){
                tbl_obj.movelast();
                setChanged();
            }
    // ]]>
    </script>
</head>

<body onload="start()">
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
    <jsp:param name="name" value="Dataset tables"/>
    <jsp:param name="helpscreen" value="dataset_tables"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
<form id="form1" method="post" action="dstables.jsp">

    <h1>
        Tables in <em><a href="<%=request.getContextPath()%>/datasets/<%=dsID%>"><%=Util.processForDisplay(dataset.getShortName())%></a></em> dataset
    </h1>

        <table width="100%" cellspacing="0"  style="border:0">
        <tr>

            <!-- table of elements -->

            <td style="width:90%">
                <table width="100%" cellspacing="0" id="tbl" class="datatable">

        <thead>
            <tr>
                <%
                if (editPrm){
                    %>
                    <td colspan="4">
                        <input type="button" value="Add new" class="smallbutton"
                               onclick="window.location.replace('<%=request.getContextPath()%>/tables/add/?ds_id=<%=dsID%>&amp;ds_name=<%=Util.processForDisplay(dsName)%>&amp;ctx=ds')"/>
                        <%
                        if (tables!=null && tables.size()>0){%>
                            <input type="button" value="Remove selected" class="smallbutton" onclick="submitForm('delete')"/>
                            <input type="button" value="Save order" class="smallbutton" onclick="saveOrder()" title="Save the current order of tables"/><%
                        }
                        %>
                    </td><%
                }
                %>
            </tr>
            <tr style="height:5px;"><td colspan="4"></td></tr>
            <tr>
                <th align="right" style="padding-right:10px">&nbsp;</th>
                <th align="left" style="padding-right:10px; border-left:0">Name</th>
                <th align="left" style="padding-left:5px;padding-right:10px">Short name</th>
                <th align="left" style="padding-right:10px;">Definition</th>
            </tr>
        </thead>
        <tbody id="tbl_body">
        <%
        DElemAttribute attr = null;
        for (int i=0; tables!=null && i<tables.size(); i++){

            String tblName = "";
            String tblDef = "";
            DsTable table = (DsTable)tables.get(i);
            String tableLink = request.getContextPath() + "/tables/" + table.getID();
            attributes = searchEngine.getAttributes(table.getID(), "T", DElemAttribute.TYPE_SIMPLE);

            for (int c=0; c<attributes.size(); c++){
                attr = (DElemAttribute)attributes.get(c);
                   if (attr.getName().equalsIgnoreCase("Name"))
                       tblName = attr.getValue();
                   if (attr.getName().equalsIgnoreCase("Definition"))
                       tblDef = attr.getValue();
            }

            String tblFullName = tblName;
            tblName = tblName.length()>40 && tblName != null ? tblName.substring(0,40) + " ..." : tblName;
            String tblFullDef = tblDef;
            tblDef = tblDef.length()>40 && tblDef != null ? tblDef.substring(0,40) + " ..." : tblDef;
            String trStyle = (i % 2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
            %>
            <tr id="tr<%=table.getID()%>" onclick="tbl_obj.selectRow(this);" <%=trStyle%>>
                <td align="right" style="padding-right:10px">
                    <%
                    if (editPrm){
                        %>
                        <input type="checkbox" style="height:13px;width:13px" name="del_id" value="<%=table.getID()%>"/><%
                    }
                    %>
                </td>
                <td align="left" style="padding-left:5px;padding-right:10px">
                    <a href="<%=tableLink%>"><%=Util.processForDisplay(tblName)%></a>
                </td>
                <td align="left" style="padding-right:10px" title="<%=Util.processForDisplay(tblFullName, true)%>">
                    <%=Util.processForDisplay(table.getShortName())%>
                </td>
                <td align="left" style="padding-right:10px" title="<%=Util.processForDisplay(tblFullDef, true)%>">
                    <%=Util.processForDisplay(tblDef)%>
                    <input type="hidden" name="oldpos_<%=table.getID()%>" value="<%=table.getPositionInDataset()%>"/>
                    <input type="hidden" name="pos_<%=table.getID()%>" value="<%=table.getPositionInDataset()%>"/>
                </td>
            </tr>
            <%
        }
        %>
        </tbody>
    </table>

    </td>

            <!-- ordering buttons -->

            <%
            if (tables.size()>1 && editPrm){ %>
                <td style="text-align:left;padding-right:10px;vertical-align:middle;height:10px;width:10%">
                    <table cellspacing="2" cellpadding="2" style="border:0">
                        <tr>
                            <td>
                                <a href="javascript:moveFirst()"><img src="images/move_first.gif" style="border:0" alt="" title="move selected row to top"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="javascript:moveRowUp()"><img src="images/move_up.gif" style="border:0" alt="" title="move selected row up"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <img src="images/dot.gif" alt=""/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="javascript:moveRowDown()"><img alt="" src="images/move_down.gif" style="border:0" title="move selected row down"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="javascript:moveLast()"><img alt="" src="images/move_last.gif" style="border:0" title="move selected row last"/></a>
                            </td>
                        </tr>
                    </table>
                </td><%
            }
            %>
        </tr>

    </table>

    <div style="display:none">
        <input type="hidden" name="mode" value="delete"/>
        <input type="hidden" name="ds_id" value="<%=dsID%>"/>
        <input type="hidden" name="ds_name" value="<%=Util.processForDisplay(dataset.getShortName(), true)%>"/>
        <input type="hidden" name="ds_idf" value="<%=dataset.getIdentifier()%>"/>
    </div>

</form>
</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.jsp" %>
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
