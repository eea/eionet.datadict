<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!private Vector elems=null;%>
<%!private ServletContext ctx=null;%>
<%!private Vector mAttributes=null;%>

<%!

private String getAttributeIdByName(String name){
	

		for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}
private String getAttributeValue(DataElement elem, String name){
	
	String id = getAttributeIdByName(name);
	if (elem == null) return null;
	DElemAttribute attr = elem.getAttributeById(id);
	if (attr == null) return null;
	return attr.getValue();
}

private boolean isIn(Vector elems, String id){
	
	for (int i=0; id!=null && i<elems.size(); i++){
		
		Object o = elems.get(i);
		Class oClass = o.getClass();
		if (oClass.getName().endsWith("Hashtable")) continue;
		
		DataElement elem = (DataElement)o;
        if (elem.getID().equalsIgnoreCase(id))
        	return true;
	}
        
    return false;
}

private DDuser getUser(HttpServletRequest req) {
	
	DDuser user = null;
    
    HttpSession httpSession = req.getSession(false);
    if (httpSession != null) {
    	user = (DDuser)httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
	}
      
    if (user != null)
    	return user.isAuthentic() ? user : null;
	else 
    	return null;
}

%>

<%

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

// check if the user is authorized
DDuser user = getUser(request);
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

//check if table id is specified
String tableID = request.getParameter("table_id");
if (tableID == null || tableID.length()==0){ %>
	<b>Table ID is missing!</b> <%
	return;
}

String contextParam = request.getParameter("ctx");
if (contextParam == null) contextParam = "";

String dsID = request.getParameter("ds_id");
if (dsID == null || dsID.length()==0){ %>
	<b>Dataset ID is missing!</b> <%
	return;
}

String dsName = request.getParameter("ds_name");
if (dsName == null || dsName.length()==0) dsName = "unknown";

ctx = getServletContext();

//handle the POST
if (request.getMethod().equals("POST")){
	
	DataElementHandler handler = new DataElementHandler(user.getConnection(), request, ctx);
	
	try{
		handler.execute();
	}
	catch (Exception e){ %>
		<html><body><b><%=e.toString()%></b></body></html> <%
		return;
	}
	
	// build reload URL

	StringBuffer redirUrl = new StringBuffer(request.getContextPath() + "/tblelems.jsp?table_id=");
	redirUrl.append(tableID);
	redirUrl.append("&ds_id=");
	redirUrl.append(dsID);
	redirUrl.append("&ds_name=");
	redirUrl.append(dsName);
	redirUrl.append("&ctx=");
	redirUrl.append(contextParam);
	
	response.sendRedirect(redirUrl.toString());
	return;
}


//handle the GET

String appName = ctx.getInitParameter("application-name");

Connection conn = DBPool.getPool(appName).getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

DsTable dsTable = searchEngine.getDatasetTable(tableID);
if (dsTable == null){ %>
	<b>Table was not found!</b> <%
	return;
}

String tableNs = dsTable.getNamespace();
if (tableNs == null){ %>
	<b>Table namespace was not found!</b> <%
	return;
}

elems = searchEngine.getDataElements(null, null, null, null, tableID);
mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
	
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
				var b = confirm("This will delete all the elements you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			//var mode;
			//var elm = document.forms["form1"].elements["elm"].value;
			//if (elm == "new")
				//mode = "add";
			//else
				//mode = "edit";
			
			if (mode=="add" && hasWhiteSpace("delem_name")){
				alert("Short name cannot contain any white space!");
				return;
			}
				
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function hasWhiteSpace(input_name){
			
			var elems = document.forms["form1"].elements;
			if (elems == null) return false;
			for (var i=0; i<elems.length; i++){
				var elem = elems[i];
				if (elem.name == input_name){
					var val = elem.value;
					if (val.indexOf(" ") != -1) return true;
				}
			}
			
			return false;
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
                <jsp:param name="name" value="Table elements"/>
            </jsp:include>
            
<div style="margin-left:30">
			
<form name="form1" method="POST" action="tblelems.jsp">

	<table width="500">

		<!---------------------- title  ------------------------------------------->

		<%
		String tableName = dsTable.getShortName();
		if (tableName == null)
			tableName = "unknown";

    	//if (contextParam.equals("dstbl")){
    	if (false){ %>
			<tr>
				<td colspan="4">
					<a href="dstable.jsp?mode=view&table_id=<%=tableID%>&ds_id=<%=dsID%>&ds_name=<%=dsName%>&ctx=<%=contextParam%>">
						< back to table view
					</a>
				</td>
			</tr> <%
		}
		%>
		
		<tr valign="bottom">
			<td colspan="4">
				<font class="head00">Elements in <a href="dstable.jsp?mode=view&table_id=<%=tableID%>&ds_id=<%=dsID%>&ds_name=<%=dsName%>"><span class="title2"><%=tableName%></span></a> table,
				<a href="dataset.jsp?ds_id=<%=dsID%>&mode=view"><span class="title2"><%=dsName%></span></a> dataset.
			</td>
		</tr>
		
		<tr height="5"><td colspan="4"></td></tr>
	</table>
	<table width="auto" cellspacing="0" cellpadding="0"><tr><td>To view a data element, click on its Short name in the list below.</td></tr></table>
	
	<table width="auto" cellspacing="0" cellpadding="0">
		<% if (user != null) { %>
			<tr><td colspan="2">You can add a new element from here:</td></tr>
		
			<tr height="5"><td colspan="2"></td></tr>
		
			<tr>
 				<td align="right"><span class="barfont">Short name:</span></td>
				<td style="padding-left:5"><input type="text" class="smalltext" width="10" name="delem_name"/></td>
			</tr>
		
			<tr>
				<td align="right"><span class="barfont">Type:</span></td>
				<td style="padding-left:5">
					<select name="type" class="small">
						<option selected value="CH2">Quantitative</option>
						<option value="CH1">Fixed values</option>
					</select>
				</td>
			</tr>
		
			<input type="hidden" name="ns" value="<%=tableNs%>"/>
		
			<tr height="5"><td colspan="2"></td></tr>
		
			<tr>
				<td></td>
				<td style="padding-left:5">
					<input type="button" class="smallbutton" value="Add" onclick="submitForm('add')"/>
				</td>
			</tr>
		<% } %>

				<tr height="10"><td colspan="2"></td></tr>
	</table>
	
	<table width="auto" cellspacing="0">

		<tr>
			<% if (user != null) { %>
				<td align="right" style="padding-right:10">
					<input type="button" <%=disabled%> value="Remove" class="smallbutton" onclick="submitForm('delete')"/>
				</td>
			<% } %>
			<th align="left" style="padding-left:5;padding-right:10">Short name</th>
			<th align="left" style="padding-right:10">Datatype</th>
			<th align="left" style="padding-right:10">MaxSize</th>
			<th align="left" style="padding-right:10">Type</th>
		</tr>
			
		<%
		
		Hashtable types = new Hashtable();
		types.put("AGG", "Aggregate");
		types.put("CH1", "Codes");
		types.put("CH2", "Quantitative");
		
		for (int i=0; elems!=null && i<elems.size(); i++){
			
			DataElement elem = (DataElement)elems.get(i);
			String elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID() + "&ds_id=" + dsID + "&table_id=" + tableID + "&ctx=" + contextParam;
			
			String elemType = (String)types.get(elem.getType());
			
			String datatype = getAttributeValue(elem, "Datatype");		
			if (datatype == null) datatype="";
			
			String max_size = getAttributeValue(elem, "MaxSize");		
			if (max_size == null) max_size="";

						%>
			<tr>
				<% if (user != null) { %>
					<td align="right" style="padding-right:10"><input type="checkbox" style="height:13;width:13" name="delem_id" value="<%=elem.getID()%>"/>
				<% } %>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a href="<%=elemLink%>"><%=elem.getShortName()%></a>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<%=datatype%>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<%=max_size%>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<% if (elem.getType().equals("CH1")){ %>
						<a href="fixed_values.jsp?delem_id=<%=elem.getID()%>&#38;delem_name=<%=elem.getShortName()%>"><%=elemType%></a>
					<%} else{ %>
						<%=elemType%>
					<% } %>
				</td>
			</tr>
			<%
		}
		%>

	</table>
	
	<input type="hidden" name="mode" value="delete"/>
	<input type="hidden" name="ds_id" value="<%=dsID%>"/>
	<input type="hidden" name="ds_name" value="<%=dsName%>"/>
	<input type="hidden" name="table_id" value="<%=tableID%>"/>
	<input type="hidden" name="ctx" value="<%=contextParam%>"/>
	
</form>
</div>
</body>
</html>