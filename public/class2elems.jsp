<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!private final static String USER_SESSION_ATTRIBUTE="DataDictionaryUser";%>
<%!public Vector classElems=null;%>

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
			
			String class_id = request.getParameter("class_id");
			
			if (class_id == null || class_id.length()==0){ %>
				<b>Class ID is missing!</b> <%
				return;
			}
			
			String class_name = request.getParameter("class_name");
			if (class_name == null) class_name = "?";
			
			String class_ns = request.getParameter("ns");
			
			if (request.getMethod().equals("POST")){
				Class2ElemHandler handler =
					new Class2ElemHandler(user.getConnection(), request, ctx);
						
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
									"/class2elems.jsp?class_id=" + class_id + "&class_name=" + class_name + "&ns=" + class_ns;

				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			Vector classElems = searchEngine.getClass2Elems(class_id);
			
			if (classElems == null) classElems = new Vector(); 
			
			Vector classElemsID = new Vector();  //Vector for class element's ids
			for (int i=0; i<classElems.size(); i++){
				DataElement elem = (DataElement)classElems.get(i);
				classElemsID.addElement(elem.getID());
			}

			Vector dataElements = searchEngine.getDataElements(); // ask for all data elements
			if (dataElements == null) dataElements = new Vector();
				
			%>

<html>
	<head>
		<title>Elements</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
	</head>
	
	<script language="JavaScript">
			function submitForm(mode){
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
	<table cellspacing="0" cellpadding="0" width="621" border="0">
			<tr>
         	<td align="bottom" width="20" background="../images/bar_filled.jpg" height="25">&#160;</td>
          	<td width="600" background="../images/bar_filled.jpg" height="25">
            <table height="8" cellSpacing="0" cellPadding="0" border="0">
            	<tr>
		         	<td valign="bottom" align="middle"><span class="barfont">EIONET</span></td>
		            <td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
		         	<td valign="bottom" align="middle"><span class="barfont">Data Dictionary</span></td>
					<td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
					<td valign="bottom" align="middle"><span class="barfont">Data class</span></td>
					<td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
					<td valign="bottom" align="middle"><span class="barfont">Data Elements</span></td>
					<td valign="bottom" width="28"><img src="../images/bar_dot.jpg"/></td>
				</tr>
			</table>
			</td></tr>
			<tr><td>&#160;</td></tr>
	</table>
<form name="form1" method="POST" action="class2elems.jsp">
<table width="560">
	<tr valign="bottom">
		<td colspan="2"><font class="head00">Elements belonging to class <font class="title2" color="#006666"><%=class_ns%>:<%=class_name%></font></font></td>
	</tr>
	<tr height="10"><td colspan="2">&#160;</td></tr>
	<tr>
		<td width="520">
		
			<font class="smallFont">Select new elements from here:</font><br></br>
			<select name="element" style="width:520">
			<%
			for (int i=0; i<dataElements.size(); i++){
				
				DataElement elem = (DataElement)dataElements.get(i);
				
				String elemID = elem.getID();
				String elemName = elem.getShortName();
				if (elemName == null) elemName = "unknown";
				if (elemName.length()==0) elemName = "empty";
				
				Namespace ns = elem.getNamespace();
				String nsName = ns == null ? "unknown" : ns.getShortName();
				if (nsName == null) nsName = "unknown";
				if (nsName.length()==0) nsName = "empty";
				
				elemName = nsName + ":" + elemName;
				
				if (!classElemsID.contains(elemID)){
					%>
					<option value="<%=elemID%>"><%=elemName%></option>
					<%
				}
			}
			%>		
			</select>		
		</td>
		<td width="40">
		
				<font class="smallFont">&#160;</font><br></br>
				<%
				if (user != null){
					%>				
					<input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')"/>
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
</table>
<table width="560">
	<tr>
		<td width="40">
		
		<%
		if (user!=null){
			%>
			<input type="button" value="Remove" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('delete')"/>
			<%
		}
		else{
			%>
			<input type="button" value="Remove" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('delete')" disabled="true"/>
			<%
		}
		%>
		
		</td>
		<th width="320">Namespace:ShortName</th>
	</tr>
	
	<%
	
	for (int i=0; i<classElems.size(); i++){
		DataElement elem = (DataElement)classElems.get(i);
		String elemName = elem.getShortName();
		if (elemName == null) elemName = "unknown";
		if (elemName.length()==0) elemName = "empty";
		
		Namespace ns = elem.getNamespace();
		String nsName = ns == null ? "unknown" : ns.getShortName();
		if (nsName == null) nsName = "unknown";
		if (nsName.length()==0) nsName = "empty";
		
		elemName = nsName + ":" + elemName;
		
		String elemLink = "data_element_print.jsp?mode=print&delem_id=" + elem.getID() + "&type=" + elem.getType();
		
		%>
		<tr>
			<td align="center" width="40"><input type="checkbox" style="height:13;width:13" name="del_id" value="<%=elem.getID()%>"/>
			<input type="hidden" name="elem_name_<%=elem.getID()%>" value="<%=elemName%>"/>
			<td align="center" width="320"><a href="<%=elemLink%>"><%=elemName%></a></td>
		</tr>
		<%
	}
	%>

</table>
<input type="hidden" name="mode" value="add"></input>

<input type="hidden" name="class_id" value="<%=class_id%>"></input>
<input type="hidden" name="class_name" value="<%=class_name%>"></input>

<input type="hidden" name="ns" value="<%=class_ns%>"></input>

</form>
</div>
</body>
</html>