<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%!private Vector tables=null;%>
<%!private Vector attributes=null;%>
<%!ServletContext ctx=null;%>

<%@ include file="history.jsp" %>

<%

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);


XDBApplication.getInstance(getServletContext());

// check if the user is authorized
AppUserIF user = SecurityUtil.getUser(request);
if (request.getMethod().equals("POST")){
	if (user == null){
		%>
			<html>
			<body>
				<h1>Error</h1><b>Not authorized to post any data!</b>
			</body>
			</html>
		<%
		return;
	}
}

String disabled = user == null ? "disabled" : "";

//check if dataset id is specified
String dsID = request.getParameter("ds_id");
if (dsID == null || dsID.length()==0){ %>
	<b>Dataset ID is missing!</b> <%
	return;
}

ctx = getServletContext();

//handle the POST
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
			handler.cleanup();
			%>
			<html><body><b><%=e.toString()%></b></body></html> <%
			return;
		}
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}

	String redirUrl = currentUrl;
	String newDstID = handler.getNewDstID();
	if (newDstID!=null)
		redirUrl = "dataset.jsp?mode=view&ds_id=" + newDstID;
	response.sendRedirect(redirUrl);
	return;
}


//handle the GET

String appName = ctx.getInitParameter("application-name");

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

Dataset dataset = searchEngine.getDataset(dsID);
if (dataset == null){ %>
	<b>Dataset was not found!</b> <%
	return;
}

tables = searchEngine.getDatasetTables(dsID);

DElemAttribute attr = null;

VersionManager verMan = new VersionManager(conn, searchEngine, user);
String topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
boolean topFree = topWorkingUser==null ? true : false;

if (disabled.equals("")){
	if (!topFree) disabled = "disabled";
}

String latestDstID = dataset==null ? null : verMan.getLatestDstID(dataset);
boolean dsLatest = Util.voidStr(latestDstID) ? true : latestDstID.equals(dataset.getID());

if (disabled.equals("")){
	if (!dsLatest) disabled = "disabled";
}
	
%>

<html>
<head>
	<title>Meta</title>
	<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
	<link href="eionet.css" rel="stylesheet" type="text/css"/>
</head>

<script language="JavaScript" src='script.js'></script>

<script language="JavaScript">
		function submitForm(mode){
			
			if (mode=="delete"){
				var b = confirm("This will delete all the tables you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
</script>
	
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0">
<%@ include file="header.htm" %>
<table border="0">
    <tr valign="top">
        <td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Dataset tables"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">
	
<form name="form1" method="POST" action="dstables.jsp">

	<table width="440">

		<!---------------------- dataset title  ------------------------------------------->

		<%
		String dsName = dataset.getShortName();
		if (dsName == null)
			dsName = "unknown";
		%>

		<tr valign="bottom">
			<td><font class="head00">Tables in 
				<a href="dataset.jsp?ds_id=<%=dsID%>&mode=view"><span class="title2"><%=Util.replaceTags(dsName)%></span></a>
			 dataset</td>
		</tr>
		
		<%
		if (user!=null){ %>
			<tr>
				<td>
					A red wildcard (<font color="red">*</font>) means that the table is under work
					and cannot be deleted. Otherwise checkboxes enable to remove selected tables.
				</td>
			</tr><%
		}
		%>
		
	</table>

	<table width="auto" cellspacing="0">
	
		<tr>
			<td colspan="5" align="right">
				<input type="button"
					   <%=disabled%>
					   value="Add new"
					   class="smallbutton"
					   onclick="window.location.replace('dstable.jsp?mode=add&ds_id=<%=dsID%>&#38;ds_name=<%=dsName%>&ctx=ds')"/>
			</td>
		</tr>
		
		<tr><td colspan="5"></td></tr>

		<tr>
			<td align="right" style="padding-right:10">
				<input type="button" <%=disabled%> value="Remove" class="smallbutton" onclick="submitForm('delete')"/>
			</td>				
			<th align="left" style="padding-left:5;padding-right:10">Short name</th>
			<th align="left" style="padding-right:10">Full name</th>
			<th align="left" style="padding-right:10">Definition</th>
			<th align="left" style="padding-right:10">Type</th>
		</tr>
			
		<%
		for (int i=0; tables!=null && i<tables.size(); i++){
			
			DsTable table = (DsTable)tables.get(i);
			String tableLink = "dstable.jsp?mode=view&table_id=" + table.getID() + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=ds";
			
			String tblName = "";
			String tblDef = "";
		
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
			
			String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(),
														  table.getShortName(), "tbl");

			%>
			<tr>
				<td align="right" style="padding-right:10">
					<%
					if (user!=null){
						
						if (tblWorkingUser!=null){ // mark checked-out tables
							%> <font color="red">* </font> <%
						}
					
						if (tblWorkingUser==null && topFree){ %>
							<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=table.getID()%>"/><%
						}
					}
					%>					
				</td>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a href="<%=tableLink%>"><%=Util.replaceTags(table.getShortName())%></a>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=tblFullName%>">
					<%=tblName%>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=tblFullDef%>">
					<%=tblDef%>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<%=table.getType()%>
				</td>
			</tr>
			<%
		}
		%>

	</table>
	
	<input type="hidden" name="mode" value="delete"/>
	<input type="hidden" name="ds_id" value="<%=dsID%>"/>
	
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