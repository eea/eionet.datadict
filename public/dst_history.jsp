<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

			<%

			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);

			ServletContext ctx = getServletContext();
			String appName = ctx.getInitParameter("application-name");

			String dstID = request.getParameter("ds_id");
			if (dstID == null || dstID.length()==0){ %>
				<b>Dataset id is missing!</b> <%
				return;
			}

			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();

			try { // start the whole page try block

			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

			Dataset dataset = searchEngine.getDataset(dstID);
			if (dataset==null){
				%>
				<b>Dataset not found!</b>
				<%
				return;
			}

			searchEngine.setUser(user);

			Vector v = searchEngine.getDstHistory(dataset.getShortName(), dataset.getVersion());

			if (v==null || v.size()==0){
				%>
				<b>No history found for this dataset!</b>
				<%
				return;
			}

			%>

<html>
	<head>
		<title>Dataset history</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
	    <script language="JavaScript" src='script.js'></script>
	</head>
	<script language="JavaScript">
		function view(id){
			window.opener.location="dataset.jsp?mode=view&ds_id=" + id;
			window.close();
		}
	</script>
<body class="popup">
<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
</div>
<form name="form1" method="POST" action="complex_attr.jsp">
    <h2>
        History of <em><%=dataset.getShortName()%></em>
        below version <em><%=dataset.getVersion()%></em>
    </h2>
    <p>
        Click on version to go to corresponding dataset view.<br/>
        User indicates the creator of the version.
        <%
        if (user!=null && user.isAuthentic()){ %>
                <br/>If you see a <font color="red">'D'</font> sign behind the version,
                        it means it has been deleted by you.<br/>
                        You can go to 'Restore datasets' page by clicking the sign.<%
        }
        %>
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
			String deleted = (String)hash.get("deleted");

			%>
			<tr>
				<td align="left" style="padding-left:5;padding-right:10">
					<a href="javascript:view('<%=id%>')">&#160;<%=version%>&#160;</a>
					<%
					if (deleted!=null){%>
						&#160;&#160;<a href="restore_datasets.jsp?SearchType=SEARCH"><font color="red">D</font></a><%
					}
					%>
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
