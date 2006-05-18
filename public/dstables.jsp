<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private Vector tables=null;%>
<%!private Vector attributes=null;%>
<%!ServletContext ctx=null;%>

<%@ include file="history.jsp" %>

<%

request.setCharacterEncoding("UTF-8");

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
	
	String dsIdf = request.getParameter("ds_idf");
	boolean delPrm = user!=null &&
					 dsIdf!=null &&
					 SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u");
	
	if (!delPrm){ %>
		<b>Not allowed!</b> <%
		return;
	}
	
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

tables = searchEngine.getDatasetTables(dsID, true);

DElemAttribute attr = null;

VersionManager verMan = new VersionManager(conn, searchEngine, user);
String topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
boolean topFree = topWorkingUser==null ? true : false;

if (disabled.equals("")){
	if (!topFree) disabled = "disabled";
}

boolean dsLatest = dataset==null ? false : verMan.isLatestDst(dataset.getID(), dataset.getIdentifier());
if (disabled.equals("")){
	if (!dsLatest) disabled = "disabled";
}
	
%>

<html>
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Meta</title>

<script language="javascript" src='script.js' type="text/javascript"></script>

<script language="javascript" type="text/javascript">
// <![CDATA[
		function submitForm(mode){
			
			if (mode=="delete"){
				var b = confirm("This will delete all the tables you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
// ]]>
</script>

</head>
	
<body>

<jsp:include page="nlocation.jsp" flush='true'>
		<jsp:param name="name" value="Dataset tables"/>
		<jsp:param name="back" value="true"/>
	</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
<table border="0">
    <tr valign="top">
        <td>
            
<div style="margin-left:30">
	
<form name="form1" method="post" action="dstables.jsp">

	<table width="440">

		<!--==================== dataset title  =========================================-->

		<%
		String dsName = dataset.getShortName();
		if (dsName == null)
			dsName = "unknown";
		%>

		<tr valign="bottom">
			<td><font class="head00">Tables in 
				<span class="title2"><a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><%=Util.replaceTags(dsName)%></a></span>
			 dataset</font></td>
		</tr>
		
	</table>

	<table width="auto" cellspacing="0">
	
		<tr><td colspan="4">&nbsp;</td></tr>
	
		<tr>
			<%
			boolean dstPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
			if (dstPrm){ %>		
				<td colspan="3">
					<input type="button" <%=disabled%> value="Add new" class="smallbutton"
						   onclick="window.location.replace('dstable.jsp?mode=add&amp;ds_id=<%=dsID%>&#38;ds_name=<%=Util.replaceTags(dsName)%>&amp;ctx=ds')"/>
					<input type="button" <%=disabled%> value="Remove selected" class="smallbutton" onclick="submitForm('delete')"/>
				</td><%
			}
			%>
			<td align="right">
				<a target="_blank" href="help.jsp?screen=dataset_tables&amp;area=pagehelp" onclick="pop(this.href)">
					<img src="images/pagehelp.jpg" border="0" alt="Get some help on this page" />
				</a>
			</td>
		</tr>
		
		<tr style="height:5px;"><td colspan="4"></td></tr>

		<tr>
			<th align="right" style="padding-right:10">&nbsp;</th>
			<th align="left" style="padding-right:10; border-left:0">
				<table width="100%">
					<tr>
						<td align="right" width="50%">
							<b>Name</b>
						</td>
						<td align="left" width="50%">
							<a target="_blank" href="help.jsp?attrshn=Name&amp;attrtype=SIMPLE" onclick="pop(this.href)">
								<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt=""/>
							</a>
						</td>
					</tr>
				</table>
			</th>
			<th align="left" style="padding-left:5;padding-right:10">
				<table width="100%">
					<tr>
						<td align="right" width="50%">
							<b>Short name</b>
						</td>
						<td align="left" width="50%">
							<a target="_blank" href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href)">
								<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt=""/>
							</a>
						</td>
					</tr>
				</table>
			</th>
			<th align="left" style="padding-right:10; border-right:1px solid #FF9900">
				<table width="100%">
					<tr>
						<td align="right" width="50%">
							<b>Definition</b>
						</td>
						<td align="left" width="50%">
							<a target="_blank" href="help.jsp?attrshn=Definition&amp;attrtype=SIMPLE" onclick="pop(this.href)">
								<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt=""/>
							</a>
						</td>
					</tr>
				</table>
			</th>
			
		</tr>
			
		<%
		for (int i=0; tables!=null && i<tables.size(); i++){
			
			DsTable table = (DsTable)tables.get(i);
			
			String tableLink = "dstable.jsp?mode=view&amp;table_id=" + table.getID() + "&amp;ds_id=" + dsID + "&amp;ds_name=" + dsName + "&amp;ctx=ds";
			
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
														  table.getIdentifier(), "tbl");

			String tblElmWorkingUser = searchEngine.getTblElmWorkingUser(table.getID());
			
			%>
			<tr>
				<td align="right" style="padding-right:10">
					<%
					if (user!=null && dstPrm){
						
						if (tblWorkingUser!=null){ // mark checked-out tables
							%> <font title="<%=Util.replaceTags(tblWorkingUser, true)%>" color="red">* </font> <%
						}
						else if (tblElmWorkingUser!=null){ // mark tables having checked-out elements
							%> <font title="<%=Util.replaceTags(tblElmWorkingUser, true)%>" color="red">* </font> <%
						}
					
						if (tblWorkingUser==null && topFree){ %>
							<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=table.getID()%>"/><%
						}
					}
					%>					
				</td>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a href="<%=tableLink%>"><%=Util.replaceTags(tblName)%></a>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=Util.replaceTags(tblFullName, true)%>">
					<%=Util.replaceTags(table.getShortName())%>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=Util.replaceTags(tblFullDef, true)%>">
					<%=Util.replaceTags(tblDef)%>
					<input type="hidden" name="mode" value="delete"/>
					<input type="hidden" name="ds_id" value="<%=dsID%>"/>
					<input type="hidden" name="ds_name" value="<%=Util.replaceTags(dataset.getShortName(), true)%>"/>
					<input type="hidden" name="ds_idf" value="<%=dataset.getIdentifier()%>"/>
				</td>
			</tr>
			<%
		}
		%>

	</table>
	
	
	
</form>
</div>
</td>
</tr>
</table>
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
