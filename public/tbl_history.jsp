<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

			<%
			
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
			String tblID = request.getParameter("table_id");
			if (tblID == null || tblID.length()==0){ %>
				<b>Table ID is missing!</b> <%
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

			DsTable dsTable = searchEngine.getDatasetTable(tblID);
			if (dsTable==null){
				%>
				<b>Table not found!</b>
				<%
				return;
			}
						
			Vector v = searchEngine.getTblHistory(dsTable.getIdentifier(),
												  dsTable.getDatasetName(),
												  dsTable.getVersion());
			
			if (v==null || v.size()==0){
				%>
				<b>No history found for this table!</b>
				<%
				return;
			}
		
			%>

<html>
    <head>
        <title>Element history</title>
        <meta http-equiv="Content-Type" content="text/html"/>
        <link href="eionet.css" rel="stylesheet" type="text/css"/>
        <script language="JavaScript" src='script.js'></script>
        <script language="JavaScript">
            function view(id){
                    window.opener.location="dstable.jsp?mode=view&table_id=" + id;
                    window.close();
            }
	</script>
    </head>
<body class="popup">
    <div class="popuphead">
        <h1>Data Dictionary</h1>
        <hr />
    </div>
    <form name="form1" method="post" action="complex_attr.jsp">

        <h2>History of <em><%=dsTable.getShortName()%></em>
            below version <em><%=dsTable.getVersion()%></em>
        </h2>
        <p>
            Click on version to go to corresponding table view.<br/>
            User indicates the creator of the version.
        </p>
	<table width="auto" cellspacing="0" id="tbl">
            <tr>
                <th align="left" style="padding-left:5;padding-right:10">Version</th>
                <th align="left" style="padding-right:10">User</th>
                <th align="left" style="padding-right:10">Date</th>
            </tr>
		
		<%
		for (int i=0; i<v.size(); i++){
			
			Hashtable hash = (Hashtable)v.get(i);
			String id = (String)hash.get("id");
			String version = (String)hash.get("version");
			String usr = (String)hash.get("user");
			String date = (String)hash.get("date");
			
			%>
			<tr>
                            <td align="left" style="padding-left:5;padding-right:10">
                                <a href="javascript:view('<%=id%>')">&#160;<%=version%>&#160;</a>
                            </td>
                            <td align="left" style="padding-right:10"><%=usr%></td>
                            <td align="left" style="padding-right:10"><%=date%></td>
			</tr>
			<%
		}
		%>

	</table>
    </form>
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
