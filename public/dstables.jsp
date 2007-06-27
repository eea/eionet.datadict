<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

	<%
	// implementation of the servlet's service method
	//////////////////////////////////////////////////
	
	request.setCharacterEncoding("UTF-8");
	
	// ensure the page is not cached
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setDateHeader("Expires", 0);

	ServletContext ctx = getServletContext();
	XDBApplication.getInstance(ctx);
	AppUserIF user = SecurityUtil.getUser(request);
	
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
	DBPoolIF pool = XDBApplication.getDBPool();
	
	Vector tables = null;
	Vector attributes=null;
	String workingUser = null;
	
	// the whole page's try block
	try {
		
		// get db connection, init search engine object
		conn = pool.getConnection();
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
	// ]]>
	</script>
</head>
	
<body>
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
	<jsp:param name="name" value="Dataset tables"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
<form id="form1" method="post" action="dstables.jsp">
	<div id="operations">
		<ul>
			<li class="help"><a href="help.jsp?screen=dataset_tables&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
		</ul>
	</div>

	<h1>
		Tables in <em><a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><%=Util.replaceTags(dataset.getShortName())%></a></em> dataset
	</h1>
		
	<table class="datatable" cellspacing="0" style="margin-top:20px;width:auto;border:0">
	
		<tr>
			<%
			if (editPrm){
				%>
				<td colspan="4">
					<input type="button" value="Add new" class="smallbutton"
						   onclick="window.location.replace('dstable.jsp?mode=add&amp;ds_id=<%=dsID%>&#38;ds_name=<%=Util.replaceTags(dsName)%>&amp;ctx=ds')"/>
					<%
					if (tables!=null && tables.size()>0){%>
						<input type="button" value="Remove selected" class="smallbutton" onclick="submitForm('delete')"/><%
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
			
		<%
		DElemAttribute attr = null;
		for (int i=0; tables!=null && i<tables.size(); i++){

			String tblName = "";
			String tblDef = "";
			DsTable table = (DsTable)tables.get(i);
			String tableLink = "dstable.jsp?mode=view&amp;table_id=" + table.getID() + "&amp;ds_id=" + dsID + "&amp;ds_name=" + dsName + "&amp;ctx=ds";
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
			<tr <%=trStyle%>>
				<td align="right" style="padding-right:10px">
					<%
					if (editPrm){
						%>
						<input type="checkbox" style="height:13px;width:13px" name="del_id" value="<%=table.getID()%>"/><%
					}
					%>				
				</td>
				<td align="left" style="padding-left:5px;padding-right:10px">
					<a href="<%=tableLink%>"><%=Util.replaceTags(tblName)%></a>
				</td>
				<td align="left" style="padding-right:10px" title="<%=Util.replaceTags(tblFullName, true)%>">
					<%=Util.replaceTags(table.getShortName())%>
				</td>
				<td align="left" style="padding-right:10px" title="<%=Util.replaceTags(tblFullDef, true)%>">
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
</div> <!-- workarea -->
</div> <!-- container -->
<jsp:include page="footer.jsp" flush="true" />
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
