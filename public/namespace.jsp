<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!private String mode=null;%>
<%!private Namespace namespace=null;%>

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
			
			//DDuser user = getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");

			String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
			
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
			}						
			
			String ns_id = request.getParameter("ns_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (request.getMethod().equals("POST")){
				
				NamespaceHandler handler = new NamespaceHandler(user.getConnection(), request, ctx);
				handler.execute();
				String id = handler.getLastInsertID();
				
				String redirUrl = request.getContextPath();
				if (mode.equals("add")){					
					if (id != null && id.length()!=0)
						redirUrl = redirUrl + "/namespace.jsp?mode=edit&ns_id=" + id;
				}
				else if (mode.equals("edit")){
					redirUrl = redirUrl + "/namespace.jsp?mode=edit&ns_id=" + id;
				}
				else if (mode.equals("delete")){
					%>
					<html><script>window.history.go(-1)</script></html>
					<%
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			if (!mode.equals("add") && (ns_id == null || ns_id.length()==0)){ %>
				<b>Namespace ID is missing!</b> <%
				return;
			}
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			if (!mode.equals("add")){
				Vector v = searchEngine.getNamespaces(ns_id);
				if (v!=null && v.size()!=0){
					namespace = (Namespace)v.get(0);
				}
				else{ %>
					<b>Namespace was not found!</b> <%
					return;
				}
			}
			
			String disabled = user == null ? "disabled" : "";
			
			%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
    
		function submitForm(mode){
			
			if (mode == "delete"){
				var b = confirm("This namespace will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory fields!");
					return;
				}
				
				if (hasWhiteSpace("shortName")){
					alert("Short name cannot contain any white space!");
					return;
				}
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function checkObligations(mode){
			
			if (mode == "delete") return true;
			//var oName = document.forms["form1"].shortName;
			var oName = document.forms["form1"].ns_id;
			var name = oName==null ? null : oName.value;
			//var oUrl = document.forms["form1"].url;
			//var url = oUrl==null ? null : oUrl.value;
			
			if (name == null || name.length==0) return false;
			//if (url == null || url.length==0) return false;
			
			return true;
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
		
		function goTo(mode, id){
			if (mode == "edit"){
				document.location.assign("namespace.jsp?ns_id=" + id + "&mode=edit");
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
                <jsp:param name="name" value="Namespace"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<form id="form1" method="POST" action="namespace.jsp">
			
			<table width="500" cellspacing="0">
				<tr>
					<%
					if (mode.equals("add")){ %>
						<td colspan="2"><span class="head00">Add a namespace</span></td> <%
					}
					else if (mode.equals("edit")){ %>
						<td colspan="2"><span class="head00">Edit namespace</span></td> <%
					}
					else{ %>
						<td><span class="head00">View namespace</span></td>
						<td align="right">
							<%
							if (user!=null){ %>
								<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=ns_id%>')"/> <%
							}
							else{
								%>&#160;
<!--								<input type="button" class="smallbutton" value="Edit" disabled/ -->
							<%
							}
							%>
						</td> <%
					}
					%>
				</tr>
				
				<%
				if (!mode.equals("view")){ %>
				
					<tr height="5"><td colspan="2"></td></tr>
				
					<tr>
						<td colspan="2"><span class="Mainfont">
						(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.
						</span></td>
					</tr> <%
				}
				%>
				
				<tr height="5"><td colspan="2"></td></tr>
				
				<tr><td <td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
				
			</table>
			
			<table width="auto" cellspacing="0">
			
			<tr>
				<td align="right" style="padding-right:10">
					<a href="javascript:openNsShortName()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Short name</b>
						<%
						if (!mode.equals("view")){
							%>
							&#160;(M)
							<%
						}
						%>
					</span>
				</td>
				<td>
					<% if(!mode.equals("add")){
						String shortName = namespace.getID(); //namespace.getShortName();
						if (shortName==null) shortName = "";
						%>
						<font class="title2" color="#006666"><%=shortName%></font>
						<input type="hidden" name="ns_id" value="<%=shortName%>"/>
					<% } else{ %>
						<input <%=disabled%> type="text" class="smalltext" size="10" name="ns_id"></input>
					<% } %>
				</td>
			</tr>
			
			<tr <% if (mode.equals("view")) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a href="javascript:openNsName()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Name</b>
						<%
						if (!mode.equals("view")){
							%>
							&#160;(O)
							<%
						}
						%>
					</span>
				</td>
				<td>
					<%
					if(!mode.equals("add")){
						String fullName = namespace.getFullName();
						if (fullName==null) fullName = "";
						if (mode.equals("edit")){ %>
							<input <%=disabled%> type="text" class="smalltext" size="30" name="fullName" value="<%=fullName%>"></input> <%
						} else { %>
							<span class="barfont" style="width:400"><%=fullName%></span> <%
						}
					} else{ %>
						<input <%=disabled%> type="text" class="smalltext" size="30" name="fullName"></input> <%
					}
					%>
				</td>
			</tr>
			
			<!--tr>
				<td align="right" style="padding-right:10">
					<b><a href="javascript:openNsURL()"><font color="black">URL</font></a></b>:&#160;(M)
				</td>
				<td colspan="2">
					<% if(!mode.equals("add")){
						String url = namespace.getUrl();
						if (url==null) url = "";
						if (url.startsWith("/")) url = urlPath + url;
						%>		
						<input <%=disabled%> type="text" size="60" name="url" value="<%=url%>"></input>
					<% } else{ %>
						<input <%=disabled%> type="text" size="60" name="url"></input>
					<% } %>
				</td>
			</tr-->
			
			<tr>	
				<td align="right" style="padding-right:10">
					<a href="javascript:openNsDescr()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Description</b>
						<%
						if (!mode.equals("view")){
							%>
							&#160;(O)
							<%
						}
						%>
					</span>
				</td>
				<td>
					<%
					if (!mode.equals("add")){
						String description = namespace.getDescription();
						if (description==null) description = "";
						if (mode.equals("edit")){ %>
							<textarea <%=disabled%> class="small" rows="3" cols="52" name="description"><%=description%></textarea> <%
						} else { %>
							<span class="barfont" style="width:400"><%=description%></span> <%
						}
					}
					else{ %>
						<textarea <%=disabled%> class="small" rows="3" cols="52" name="description"></textarea> <%
					}
					%>
				</td>
			</tr>
		
		<%
		if (!mode.equals("view")){ %>
			<tr height="20"><td colspan="2"></td></tr>
			
			<tr>
				<td></td>
				<td>
					<%
					if (mode.equals("add")){ // if mode is "add"
						if (user==null){ %>									
							<input type="button" class="mediumbuttonb" value="Add" disabled="true"/>&#160;&#160;
						<%} else {%>
							<input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>&#160;&#160;
						<% }
					} // end if mode is "add"
					
					if (!mode.equals("add")){ // if mode is not "add"
						if (user==null){ %>									
							<input type="button" class="mediumbuttonb" value="Save" disabled="true"/>&#160;&#160;
							<input type="button" class="mediumbuttonb" value="Delete" disabled="true"/>&#160;&#160;
						<%} else {%>
							<input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
							<input type="button" class="mediumbuttonb" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;
						<% }
					} // end if mode is not "add"
					
					%>
					
				</td>
			</tr> <%
		}
		%>
		
		<input type="hidden" name="mode" value="<%=mode%>"/>
		
	</table>
	</form>
</div>
        </TD>
</TR>
</table>
</body>
</html>