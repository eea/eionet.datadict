<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private String mode=null;%>
<%!private Hashtable attrField=null;%>

<%@ include file="history.jsp" %>

<%!

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

			ServletContext ctx = getServletContext();	
			
			XDBApplication.getInstance(ctx);
			AppUserIF user = SecurityUtil.getUser(request);
			
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
			
			String field_id = request.getParameter("field_id");
			
			String attr_name = request.getParameter("attr_name");			
			String attr_id = request.getParameter("attr_id");			

			if (attr_id == null || attr_id.length()==0){ %>
				<b>Attribute ID is missing!</b> <%
				return;
			}
			if (field_id == null || field_id.length()==0){ %>
				<b>Attribute field ID is missing!</b> <%
				return;
			}
			
			if (attr_name == null) attr_name = "?";
			

			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			

			
			if (request.getMethod().equals("POST")){

				Connection userConn = null;
								
				try{
					userConn = user.getConnection();
					
					MAttrFieldsHandler handler = new MAttrFieldsHandler(userConn, request, ctx);
					
					try{
						handler.execute();
					}
					catch (Exception e){
						%>
						<html><body><b><%=e.toString()%></b></body></html>
						<%
						return;
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				String redirUrl=null;
				if (mode.equals("delete")){
					String	deleteUrl = history.gotoLastNotMatching("m_attr_field.jsp");
					redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
					//redirUrl = "" +
					//				"/m_attr_fields.jsp?mode=edit&attr_id=" + attr_id + "&attr_name=" + attr_name;
				}
				else {
					redirUrl=currentUrl;
					//redirUrl = "" +
					//				"/m_attr_field.jsp?mode=edit&attr_id=" + attr_id + "&attr_name=" + attr_name + "&field_id=" + field_id;
				}
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			attrField = searchEngine.getAttrField(field_id);		
			if (attrField == null) attrField = new Hashtable();
			String disabled = user == null ? "disabled" : "";
			
			String name = (String)attrField.get("name");
			String definition = (String)attrField.get("definition");
			String priority = (String)attrField.get("priority");

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
			
			if (mode == "delete"){
				var b = confirm("This value will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		function openPriority(){
			alert("Click the checkbox, if the field has high priority. Otherwise it has low priority.");
		}
		function onLoad(){
			<%
				if (priority != null){
    			%>
					var pri = '<%=priority%>';
					var o = document.forms["form1"].priority;
					for (i=0; o!=null && i<o.options.length; i++){
						if (o.options[i].value == pri){
							o.selectedIndex = i;
							break;
						}
					}			
				<% 
				}
			%>
		}
		
	</script>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0" onload="onLoad()">
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
                <jsp:param name="name" value="Allowable value"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">

	<%
	String backURL = "" + "/m_attr_fields.jsp?attr_id=" + attr_id + "&attr_name=" + attr_name;
	
	%>
		<form name="form1" method="POST" action="m_attr_field.jsp">
			
			<table width="auto" cellspacing="0" cellpadding="0">
			
			<!-- tr>
				<td colspan="2">
					<a href="javascript:window.location.replace('<%=backURL%>')">< back to allowable values list</a>
				</td>
			</tr -->
					
			<tr height="20"><td colspan="2"></td></tr>
			<tr valign="bottom">
				<td colspan="2">
					<span class="head00">Field of</span>
					<span class="title2"><%=Util.replaceTags(attr_name)%></span>
					<span class="head00"> attribute</span>
				</td>
			</tr>
			
			<tr height="20"><td colspan="2"></td></tr>
			
			<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
			
			<tr>				
				<td align="right" style="padding-right:10" valign="top">
					<b><font color="black">Field name</font></b>
				</td>
				<td colspan="1" valign="top">
					<font class="title2" color="#006666"><%=Util.replaceTags(name)%></font>
				</td>
			</tr>
			<tr>				
				<td align="right" style="padding-right:10" valign="top">
					<b><font color="black">Definition</font></b>
				</td>
				<td colspan="1" valign="top">
					<textarea <%=disabled%> class="small" rows="5" cols="60" name="definition"><%=Util.replaceTags(definition, true)%></textarea>
				</td>
			</tr>
			<tr>				
				<td align="right" style="padding-right:10" valign="top">
					<!--a href="javascript:openPriority()"><span class="help">?</span></a>&#160;-->
					<span class="mainfont"><b>Priority</b></span>
				</td>
				<td colspan="1" valign="top">
					<select <%=disabled%> name="priority" class="small">
						<option value="<%=DElemAttribute.FIELD_PRIORITY_HIGH%>">High</option>
						<option value="<%=DElemAttribute.FIELD_PRIORITY_LOW%>">Low</option>
					</select>
				</td>
			</tr>
		
		<tr height="10"><td colspan="2"></td></tr>
		
		<tr>
			<td>&#160;</td>
			<td colspan="2">
			
				<% 
					if (user==null){ %>									
						<input class="mediumbuttonb" type="button" value="Save" disabled="true"/>&#160;&#160;
						<input class="mediumbuttonb" type="button" value="Delete" disabled="true"/>&#160;&#160;
					<%} else {%>
						<input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
						<input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;
					<% }
				%>
				
			</td>
		</tr>
		
		
	</table>

	<input type="hidden" name="mode" value="<%=mode%>"/>
	<input type="hidden" name="field_id" value="<%=field_id%>"/>
	<input type="hidden" name="del_field" value="<%=field_id%>"/>
	<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
	<input type="hidden" name="attr_name" value="<%=attr_name%>"/>
	
	</form>
</div>
        </TD>
</TR>
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