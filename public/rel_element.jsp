<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private String mode="edit";%>
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
			
	
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
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
			
			String delem_id = request.getParameter("delem_id");
			
			if (delem_id == null || delem_id.length()==0){ %>
				<b>Data element ID is missing!</b> <%
				return;
			}

						String child_csi = request.getParameter("child_id");
			
			if (child_csi == null || child_csi.length()==0){ %>
				<b>Child item ID is missing!</b> <%
				return;
			}
			
			
			String delem_name = request.getParameter("delem_name");
			if (delem_name == null) delem_name = "?";


			if (request.getMethod().equals("POST")){
				
				mode = request.getParameter("mode");
				if (mode == null || mode.length()==0) { %>
					<b>Mode paramater is missing!</b>
					<%
					return;
				}
				
				String redirUrl = "";
				
				Connection userConn = null;
				try{
					userConn = user.getConnection();
					CsiRelationHandler handler = new CsiRelationHandler(userConn, request, ctx);

					try {
						handler.execute();
					}
					catch (Exception e){
						%>
						<html><body>
							<b><%=e.toString()%></b><br>
							<%
							/*
							if (delem_id != null && child_csi!=null)
								redirUrl = redirUrl + "rel_element.jsp?mode=edit&delem_id=" + delem_id +
																"&delem_name=" + delem_name +
																 "&child_id=" + child_csi;
							*/									 
							%>
							<a href="javascript:window.location.replace('<%=currentUrl%>')">< back</a>
							
						</body></html>
						<%
						return;
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
			
				if (mode.equals("edit")){
					redirUrl=currentUrl;
					//redirUrl = redirUrl + "rel_element.jsp?mode=edit&delem_id=" + delem_id +
					//										"&delem_name=" + delem_name +
					//										 "&child_id=" + child_csi;
				}
				else if (mode.equals("delete")){
					String deleteUrl = history.gotoLastNotMatching("rel_element.jsp");
					if (deleteUrl!=null&&deleteUrl.length()>0) 
						redirUrl=deleteUrl;
					else 
					redirUrl = redirUrl + "rel_elements.jsp?mode=edit&delem_id=" + delem_id +
															 "&delem_name=" + delem_name;
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
			
			Vector elems = searchEngine.getRelatedElements(delem_id, "elem", child_csi); //there should be only one item in the vector

			CsiItem item = null;
			if (elems.size()>0)
				item = (CsiItem)elems.get(0);
			
			String disabled = user == null ? "disabled" : "";
						
			if (disabled.equals("")){
				boolean isWorkingCopy = searchEngine.isWorkingCopy(delem_id, "elm");
				if (!isWorkingCopy) disabled = "disabled";
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
			
			if (mode == "delete"){
				var b = confirm("This relation will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
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
                <jsp:param name="name" value="Relation"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">

		<form name="form1" method="POST" action="rel_element.jsp">
			
			<table width="600" cellspacing="0" cellpadding="0">
			
					
			<tr height="20"><td colspan="2"></td></tr>
			
			<tr valign="bottom">
				<td colspan="2">
					<span class="head00">Related data element of</span>
					<span class="title2"><%=Util.replaceTags(delem_name)%></span>
				</td>
			</tr>
			
			<tr height="5"><td colspan="2"></td></tr>
			
			<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
			
			<tr>
				<td colspan="2">
					Element specified in 'Related element' field is related to <font color="#006666"><%=Util.replaceTags(delem_name)%></font>
					element in a context specified in 'Relation descrition' field. You can get to related element's details by cliking on it.
				</td>
			</tr>
			
			<tr height="10"><td colspan="2"></td></tr>
			
			<tr>				
				<td align="right" style="padding-right:10" valign="top">
					<b><font color="black">Related element</font></b>
				</td>
				<td colspan="1" valign="top">
					<font class="title2" color="#006666">
						<a href="data_element.jsp?delem_id=<%=item.getComponentID()%>&#38;mode=view">
							<%=Util.replaceTags(item.getValue())%>
						</A>
					</font>
				</td>
			</tr>
			<tr>				
				<td align="right" style="padding-right:10" valign="top">
					<b><font color="black">Relation description</font></b>
				</td>
				<td colspan="1" valign="top">
					<% if (user!=null) {%>
						<textarea class="small" rows="5" cols="50" name="rel_description"><%=Util.replaceTags(item.getRelDescription(),true)%></textarea>
					<%} else {%>
						<%=Util.replaceTags(item.getRelDescription())%>
					<%}%>
				</td>
			</tr>
		
			<tr height="10"><td colspan="2"></td></tr>
		
		<tr>
			<td>&#160;</td>
			<td colspan="2">
			
				<% 
				
			
					if (user!=null){ %>									
						<input <%=disabled%> class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
						<input <%=disabled%> class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;
					<% }
				
				%>
				
			</td>
		</tr>
		
		<input type="hidden" name="mode" value="<%=mode%>"/>
		
	</table>
	<input type="hidden" name="del_id" value="<%=child_csi%>"/>
	<input type="hidden" name="child_id" value="<%=child_csi%>"/>
	<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
	<input type="hidden" name="delem_name" value="<%=delem_name%>"/>
	<input type="hidden" name="component_type" value="elem"/>
	<input type="hidden" name="csi_type" value="elem"/>
	<input type="hidden" name="rel_type" value="abstract"/>
<input type="hidden" name="parentcomp_id" value="<%=delem_id%>"/>
	
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