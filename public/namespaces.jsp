<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

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
%>

<%
	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	
	String urlPath = ctx.getInitParameter("basens-path");
	if (urlPath == null) urlPath = "";
							
	Connection conn = DBPool.getPool(appName).getConnection();
	
	DDuser user = getUser(request);
	
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
		
		
			
		NamespaceHandler handler =
					new NamespaceHandler(user.getConnection(), request, ctx, "delete");
				
		handler.execute();
		
		String redirUrl = request.getParameter("searchUrl");
		if (redirUrl != null && redirUrl.length()!=0){
			ctx.log("redir= " + redirUrl);
			response.sendRedirect(redirUrl);
		}
	}	
	
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	Vector namespaces = searchEngine.getNamespaces();	
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
		function submitForm(){
			
			var b = confirm("This will delete all the namespaces you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
			if (b==false) return;
					
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
			
			document.forms["form1"].submit();
		}
		
		function goTo(mode){
			if (mode == "add"){
				document.location.assign('namespace.jsp?mode=add');
			}
		}
    </script>
</head>
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
                <jsp:param name="name" value="Namespaces"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<%
            
            if (namespaces == null || namespaces.size()==0){
	            %>
	            <b>No namespaces were found!</b></div></TD></TR></table></body></html>
	            <%
	            return;
            }
            %>
            
			<form id="form1" method="POST" action="namespaces.jsp">
			
		<table width="500">
			<tr><td><font class="head00">Namespaces</font></td></tr>
			<tr height="10"><td></td></tr>
			<tr>
				<td>
					To view or modify a namespace, click its Full name in the list below.
					To add a new namespace, click the 'Add' button on top of the list.
					The left-most column enables you to delete selected namespaces.
				</td>
			</tr>
			<tr height="10"><td></td></tr>
		</table>
		
		<table width="auto" cellspacing="0">
		
			<tr>
				<td></td>
				<td align="left" colspan="2" style="padding-bottom:5">
					<% if (user != null){
						%>
						<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>
						<%
					}
					else{
						%>
						<input type="button" class="smallbutton" value="Add" disabled/>
						<%
					}
					%>
				</td>
			</tr>
		
			<tr>
				<td  align="right" style="padding-right:10">
					<% if (user != null){
						%>
						<input type="button" value="Delete" class="smallbutton" onclick="submitForm()"/>
						<%
					}
					else{
						%>
						<input type="button" value="Delete" class="smallbutton" disabled/>
						<%
					}
					%>
				</td>
				<th align="left" style="padding-left:5;padding-right:10">Full name</th>
				<th align="left" style="padding-right:10">Description</th>
			</tr>
			
			<%
			
			for (int i=0; i<namespaces.size(); i++){
				
				Namespace namespace = (Namespace)namespaces.get(i);
				
				String ns_id = namespace.getID();
				String ns_name = namespace.getFullName();
				//String ns_short_name = namespace.getShortName();
				if (ns_name == null) ns_name = "unknown";
				if (ns_name.length() == 0) ns_name = "empty";
				
				String descr = namespace.getDescription();
				if (descr == null) descr = "";
				
				/*String ns_url = namespace.getUrl();
				if (ns_url.startsWith("/")) ns_url = urlPath + ns_url;
				
				String displayUrl = ns_url;
				if (ns_url.length()>100){
					displayUrl = ns_url.substring(0,100) + " ...";
				}*/
				
				String displayDescr = descr;
				if (descr.length()>100){
					displayDescr = descr.substring(0,100) + " ...";
				}
				
				String displayName = ns_name;
				if (ns_name.length()>20){
					displayName = ns_name.substring(0,20) + " ...";
				}
				
				%>
				
				<tr>
					<td align="right" style="padding-right:10">
						<input type="checkbox" style="height:13;width:13" name="ns_id" value="<%=ns_id%>"/>
					</td>
					<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<a href="namespace.jsp?ns_id=<%=ns_id%>&#38;mode=view">
						<%=displayName%></a>
					</td>
					<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<%=displayDescr%>
					</td>
				</tr>
				
				<%
			}
			%>
			
		</table>
		
		<input type="hidden" name="searchUrl" value=""/>
		
		</form>
			</div>
			
		</TD>
</TR>
</table>
</body>
</html>