<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!private Vector fixedValues=null;%>
<%!private Vector mAttributes=null;%>
<%!private Vector fxvAttributes=null;%>

<%!

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

private String legalizeAlert(String in){
        
    in = (in != null ? in : "");
    StringBuffer ret = new StringBuffer(); 
  
    for (int i = 0; i < in.length(); i++) {
        char c = in.charAt(i);
        if (c == '\'')
            ret.append("\\'");
        else if (c == '\\')
        	ret.append("\\\\");
        else
            ret.append(c);
    }

    return ret.toString();
}


%>

			<%
			
			DDuser user = getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
			/*DDuser user = new DDuser(DBPool.getPool(appName));
	
			String username = "root";
			String password = "ABr00t";
			boolean f = user.authenticate(username, password);*/
			
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
			
			String delem_id = request.getParameter("delem_id");
			
			if (delem_id == null || delem_id.length()==0){ %>
				<b>Parent ID is missing!</b> <%
				return;
			}
			
			String parent_type = request.getParameter("parent_type");
			if (parent_type == null)
				parent_type = "elem";
			else if (!parent_type.equals("elem") && !parent_type.equals("attr")){ %>
				<b>Unknown parent type!</b> <%
				return;
			}
			
			String dispParentType = parent_type.equals("elem") ? "element" : "attribute";
			
			String parentCSI = request.getParameter("parent_csi");
			String prevParent = request.getParameter("prv_parent_csi");
			
			String delem_name = request.getParameter("delem_name");
			if (delem_name == null) delem_name = "?";
			
			//String delem_ns = request.getParameter("ns");
			
			// handle the POST
			
			if (request.getMethod().equals("POST")){
				FixedValuesHandler handler =
					new FixedValuesHandler(user.getConnection(), request, ctx);
				
				//boolean schemaResult = true;
				//String errorMessage = null;
				
				try{
					handler.execute();
				}
				catch (Exception e){
					%>
					<html><body>
						<b><%=e.toString()%></b><br/>
						<%
						String backUrl = request.getContextPath();
						backUrl = backUrl + "/fixed_values.jsp?delem_id=" + delem_id +
															 "&delem_name=" + delem_name +
															 "&parent_type=" + parent_type;
															 //"&ns=" + delem_ns;
						if (parentCSI!=null && parentCSI.length()!=0)
							backUrl = backUrl + "&parent_csi=" + parentCSI;
						if (prevParent!=null && prevParent.length()!=0)
							backUrl = backUrl + "&prv_parent_csi=" + prevParent;
						%>
						<a href="javascript:window.location.replace('<%=backUrl%>')">< back</a>
					</body></html>
					<%
					return;
					
					/*schemaResult = handler.getSchemaResult();
					if (schemaResult == true)
						errorMessage = "Fixed values handler encountered the following error: " + e.toString();
					else
						errorMessage = e.toString();*/
				}
				
				
				StringBuffer redirUrl = new StringBuffer(request.getContextPath());
				redirUrl.append("/fixed_values.jsp?delem_id=");
				redirUrl.append(delem_id);
				redirUrl.append("&delem_name=");
				redirUrl.append(delem_name);
				redirUrl.append("&parent_type=");
				redirUrl.append(parent_type);
				if (parentCSI!=null && parentCSI.length()!=0){
					redirUrl.append("&parent_csi=");
					redirUrl.append(parentCSI);
				}				
				if (prevParent!=null && prevParent.length()!=0){
					redirUrl.append("&prv_parent_csi=");
					redirUrl.append(prevParent);
				}
				
				//redirUrl.append("&ns=");
				//redirUrl.append(delem_ns);
							
				response.sendRedirect(redirUrl.toString());
				return;
			}
			
			// handle the GET
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			if (parentCSI!=null && parentCSI.length()!=0)
				fixedValues = searchEngine.getSubValues(parentCSI);
			else
				fixedValues = searchEngine.getFixedValues(delem_id, parent_type);

			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			fxvAttributes = new Vector();
			DElemAttribute attribute = null;

			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType != null &&
						attribute.displayFor("FXV")){
					fxvAttributes.add(attribute);
				}
			}



			if (fixedValues == null) fixedValues = new Vector();
			
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
				document.forms["form1"].elements["mode"].value = mode;
				if (mode == "delete"){
					var b = confirm("This will delete all the  values you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;
				}
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
                <jsp:param name="name" value="Allowable values"/>
            </jsp:include>
            
<div style="margin-left:30">
			
<form name="form1" method="POST" action="fixed_values.jsp">
<table width="400">
	
	<%
	
	if (parentCSI != null && parentCSI.length()!=0){
		StringBuffer buf = new StringBuffer();
		buf.append("fixed_values.jsp?delem_id=");
		buf.append(delem_id);
		buf.append("&delem_name=");
		buf.append(delem_name);
		buf.append("&parent_type=");
		buf.append(parent_type);
		if (prevParent != null && prevParent.length()!=0){
			buf.append("&parent_csi=");
			buf.append(prevParent);
		}
		%>
		<tr>
			<td colspan="2">
				<a href="javascript:window.location.replace('<%=buf.toString()%>')">< back to upper level</a>
			</td>
		</tr>
		<tr height="20"><td colspan="2"></td></tr>
		<%
	}
	%>
			
	<tr valign="bottom">
		<td colspan="2">
			<span class="head00">
				Allowable values of
				<span class="title2"><%=delem_name%></span>
				<span class="head00"><%=dispParentType%></span>
			</span>
		</td>
	</tr>
	<tr height="10"><td colspan="2"></td></tr>
	<tr><td align="left">
		</td>
	</tr>
	<% if (user != null) { %>
		<tr height="10"><td colspan="2"><font class="mainfont">Enter a new value here:</font></td></tr>
		<tr>
			<td colspan="2" width="300">
				<input class="smalltext" type="text" size="20" name="new_value"></input>
				<input class="smallbutton" type="button" value="Add" onclick="submitForm('add')">
			</td>
		</tr>
	<% } %>
	<!--tr>
		<td width="100">Definition:</td>
		<td width="300">
			<textarea rows="2" cols="30" name="definition"></textarea>
		</td>
	</tr>
	<tr>
		<td width="250">Matching element:</td>
		<td width="150">
			<select name="repr_elem">
				<option value="">-- None --</option>
				<%
				Vector dataElements = null;
				if (user != null) dataElements = searchEngine.getDataElements();
				if (dataElements != null && dataElements.size()!=0){
					for (int i=0; i<dataElements.size(); i++){
						DataElement dataElement = (DataElement)dataElements.get(i);
						Namespace ns = dataElement.getNamespace();
						%>
						<option value="<%=dataElement.getID()%>"><%=ns.getShortName()%>:<%=dataElement.getShortName()%></option>
						<%
					}
				}
				%>
			</select>
		</td>
	</tr-->
			
	<tr height="10"><td colspan="2"></td></tr>
</table>
<table width="auto" cellspacing="0" cellpadding="0">
	<tr>
		<% if (user != null) { %>
			<td align="right" style="padding-right:10">
				<input class="smallbutton" type="button" value="Remove" onclick="submitForm('delete')">
			</td>
		<% } %>
		<th align="left" style="padding-left:5;padding-right:10">Value</th>
		<%
			for (int i=0; fxvAttributes!=null && i<fxvAttributes.size(); i++){
				
				attribute = (DElemAttribute)fxvAttributes.get(i);
				%>
				<th align="left" style="padding-right:10"><%=attribute.getShortName()%></th>
				<%
			}
			%>
	</tr>
	
	<%
	for (int i=0; i<fixedValues.size(); i++){
		FixedValue fxv = (FixedValue)fixedValues.get(i);
		String value = fxv.getValue();
		String fxvID = fxv.getID();
		String fxvAttrValue = null;
		String fxvAttrValueShort = null;
		
		String alt = searchEngine.hasSubValues(fxvID) ? "go to sub-values" : "add sub-values";
		StringBuffer buf = new StringBuffer();
		buf.append("fixed_values.jsp?delem_id=");
		buf.append(delem_id);
		buf.append("&delem_name=");
		buf.append(delem_name);
		buf.append("&parent_type=");
		buf.append(parent_type);
		buf.append("&parent_csi=");
		buf.append(fxvID);
		
		if (parentCSI != null && parentCSI.length()!=0){
			buf.append("&prv_parent_csi=");
			buf.append(parentCSI);
		}
		
		%>
		<tr>
			<% if (user != null) { %>
				<td align="right" style="padding-right:10">
					<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=fxvID%>"/>
				</td>
			<% } %>
			<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<b><a href="fixed_value.jsp?fxv_id=<%=fxvID%>&#38;mode=edit&delem_id=<%=delem_id%>&delem_name=<%=delem_name%>&parent_type=<%=parent_type%>">
					<%=value%>
				</a></b>&#160;
				<map name="map<%=i%>">
					<area alt="<%=alt%>" shape="rect" coords="0,0,18,10" href="<%=buf.toString()%>"></area>
				</map>
				<img border="0" src="../images/deeper.gif" height="10" width="18" usemap="#map<%=i%>"></img>
			</td>
			<%
			for (int c=0; fxvAttributes!=null && c<fxvAttributes.size(); c++){
	
				attribute = (DElemAttribute)fxvAttributes.get(c);
				
				fxvAttrValue = fxv.getAttributeValueByID(attribute.getID());
				if (fxvAttrValue==null || fxvAttrValue.length()==0){
				%>
					<td align="center" width="100" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> onmouseover=""></td>
				<%
			}
				else{
					if (fxvAttrValue.length()>60) 
						fxvAttrValueShort = fxvAttrValue.substring(0,60) + " ...";
					else
						fxvAttrValueShort = fxvAttrValue;

					%>
					<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=fxvAttrValue%>">
						<%=fxvAttrValueShort%>
					</td>
					<%
				}
			}
			%>
		</tr>
		<%
	}
	%>
	
</table>

<input type="hidden" name="mode" value="add"></input>

<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
<input type="hidden" name="delem_name" value="<%=delem_name%>"/>
<input type="hidden" name="parent_type" value="<%=parent_type%>"></input>

<%
if (parentCSI!=null && parentCSI.length()!=0){ %>
	<input type="hidden" name="parent_csi" value="<%=parentCSI%>"/> <%
}
if (prevParent!=null && prevParent.length()!=0){ %>
	<input type="hidden" name="prv_parent_csi" value="<%=prevParent%>"/> <%
}
%>
</form>
</div>
</body>
</html>
