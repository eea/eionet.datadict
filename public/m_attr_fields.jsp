<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!private final static String USER_SESSION_ATTRIBUTE="DataDictionaryUser";%>
<%!private Vector attrFields=null;%>

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
			
			String attr_id = request.getParameter("attr_id");
			
			if (attr_id == null || attr_id.length()==0){ %>
				<b>Attribute ID is missing!</b> <%
				return;
			}
			
			String attr_name = request.getParameter("attr_name");
			if (attr_name == null) attr_name = "?";
			
			String attr_ns = request.getParameter("attr_ns");
			if (attr_ns == null) attr_ns = "?";
			
			if (request.getMethod().equals("POST")){
				
				MAttrFieldsHandler handler = new MAttrFieldsHandler(user.getConnection(), request, ctx);
				
				try{
					handler.execute();
				}
				catch (Exception e){
					%>
					<html><body><b><%=e.toString()%></b></body></html>
					<%
					return;
				}
				
				String redirUrl = request.getContextPath() +
									"/m_attr_fields.jsp?attr_id=" + attr_id + "&attr_name=" + attr_name + "&attr_ns=" + attr_ns;
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			attrFields = searchEngine.getAttrFields(attr_id);
			
			if (attrFields == null) attrFields = new Vector();
			
			%>

<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
	</head>
	<script language="JavaScript">
			function submitForm(mode){
				
				if (mode == "delete"){
					var b = confirm("This will delete all the fields you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;
				}
			
				document.forms["form1"].elements["mode"].value = mode;
				document.forms["form1"].submit();
			}
	</script>
<body style="background-color:#f0f0f0;background-image:url('../images/eionet_background2.jpg');background-repeat:repeat-y;"
		topmargin="0" leftmargin="0" marginwidth="0" marginheight="0">
<div style="margin-left:30">
	<br></br>
	<font color="#006666" size="5" face="Arial"><strong><span class="head2">Data Dictionary</span></strong></font>
	<br></br>
	<font color="#006666" face="Arial" size="2"><strong><span class="head0">Prototype v1.0</span></strong></font>
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
					<td valign="bottom" align="middle"><span class="barfont">Complex attribute</span></td>
					<td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
					<td valign="bottom" align="middle"><span class="barfont">Fields</span></td>
					<td valign="bottom" width="28"><img src="../images/bar_dot.jpg"/></td>
				</tr>
			</table>
			</td></tr>
			<tr><td>&#160;</td></tr>
	</table>

<form name="form1" method="POST" action="m_attr_fields.jsp">
<table width="400">
	<tr valign="bottom">
		<td colspan="2"><font class="head00">Fields of <font class="title2" color="#006666"><%=attr_ns%>:<%=attr_name%></font></font></td>
	</tr>
	<tr height="10"><td colspan="2"></td></tr>
	<tr height="10"><td colspan="2"><font class="smallFont">Enter a new field here:</font><br></br></td></tr>
	<tr>
		<td width="100">Name:</td>
		<td width="300">
			<input type="text" size="20" name="new_field"></input>&#160;
			<%
			if (user!=null){
				%>
				<input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')">
				<%
			}
			else{
				%>
				<input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')" disabled="true"/>
				<%
			}
			%>
		</td>
	</tr>
	<tr>
		<td width="100">Definition:</td>
		<td width="300">
			<textarea rows="2" cols="30" name="definition"></textarea>
		</td>
	</tr>
	<tr height="10"><td colspan="2"></td></tr>
</table>
<table width="440">
	<tr>
		<td width="40">
			<%
			if (user!=null){
				%>
				<input type="button" value="Remove" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('delete')">
				<%
			}
			else{
				%>
				<input type="button" value="Remove" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('delete')" disabled="true"/>
				<%
			}
			%>
		</td>
		<th width="100">Name</th>
		<th width="300">Definition</th>
	</tr>
	
	<%
	
	//String position = String.valueOf(attrFields.size() + 1);
	int position = 0;
	
	for (int i=0; i<attrFields.size(); i++){
		Hashtable hash = (Hashtable)attrFields.get(i);
		String id = (String)hash.get("id");
		String name = (String)hash.get("name");
		String definition = (String)hash.get("definition");
		if (definition.length()>20) definition = definition.substring(0,20) + " ...";
		
		int pos = Integer.parseInt((String)hash.get("position"));
		if (pos >= position) position = pos +1;
			
		%>
		<tr>
			<td align="center" width="40"><input type="checkbox" style="height:13;width:13" name="del_field" value="<%=id%>"/></td>
			<td align="center" width="100"><%=name%></td>
			<td align="center" width="300" onmouseover=""><%=definition%></td>
		</tr>
		<%
	}
	%>
	
</table>

<input type="hidden" name="mode" value="add"></input>
<input type="hidden" name="position" value="<%=String.valueOf(position)%>"></input>

<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
<input type="hidden" name="attr_name" value="<%=attr_name%>"/>
<input type="hidden" name="attr_ns" value="<%=attr_ns%>"></input>
</form>
</div>
</body>
</html>
