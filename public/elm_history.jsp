<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

			<%
			
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
			String elmID = request.getParameter("delem_id");
			if (elmID == null || elmID.length()==0){ %>
				<b>Element ID is missing!</b> <%
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			Vector v = searchEngine.getElmHistory(elmID);
			
			if (v==null || v.size()==0){
				%>
				<b>No history found for this element!</b>
				<%
				return;
			}
			
			DataElement dataElement = searchEngine.getDataElement(elmID);
		
			%>

<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
	    <script language="JavaScript" src='script.js'></script>
	</head>
	<script language="JavaScript">
		function view(id){
			//window.location="data_element.jsp?mode=view&delem_id=" + id;
			window.opener.location="data_element.jsp?mode=view&delem_id=" + id;
			window.close();
		}
	</script>
<body style="background-color:#f0f0f0;background-image:url('../images/eionet_background2.jpg');background-repeat:repeat-y;"
		topmargin="0" leftmargin="0" marginwidth="0" marginheight="0">
<div style="margin-left:30">
	<br></br>
	<font color="#006666" size="5" face="Arial"><strong><span class="head2">Data Dictionary</span></strong></font>
	<br></br>
	<font color="#006666" face="Arial" size="2">
		<strong><span class="head0"><script language="JavaScript">document.write(getDDVersionName())</script></span></strong>
	</font>
	<br></br>
	<table cellspacing="0" cellpadding="0" width="400" border="0">
			<tr>
	         	<td align="bottom" width="20" background="../images/bar_filled.jpg" height="25">&#160;</td>
	          	<td width="600" background="../images/bar_filled.jpg" height="25">
		            <table height="8" cellSpacing="0" cellPadding="0" border="0">
		            	<tr>
				         	<td valign="bottom" align="middle"><span class="barfont">EIONET</span></td>
				            <td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
				         	<td valign="bottom" align="middle"><span class="barfont">Data Dictionary</span></td>
							<td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
							<td valign="bottom" align="middle"><span class="barfont">Element history</span></td>
							<td valign="bottom" width="28"><img src="../images/bar_dot.jpg"/></td>
						</tr>				
					</table>
				</td>
			</tr>			
	</table>

<form name="form1" method="POST" action="complex_attr.jsp">

	<table width="auto" cellspacing="0" id="tbl">
	
		<tr>
			<td colspan="3">
				<span class="head00">
					History of <font class="title2" color="#006666"><%=dataElement.getShortName()%></font>
					below version <font class="title2" color="#006666"><%=dataElement.getVersion()%></font>
				</span>
			</td>
		</tr>
		<tr height="15"><td colspan="3"></td></tr>
		<tr><td colspan="3">Click on version to go to corresponding element view.</td></tr>
		<tr height="5"><td colspan="3"></td></tr>

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
