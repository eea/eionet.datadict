<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private String mode=null;%>
<%!private Namespace namespace=null;%>

<%@ include file="history.jsp" %>


			<%
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");

			String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
			
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
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
			if (mode == null || mode.length()==0) {
				mode = "view";
			}
			
			if (request.getMethod().equals("POST")){
				
				Connection userConn = null;
				String id = null;
				
				try{
					userConn = user.getConnection();
					NamespaceHandler handler = new NamespaceHandler(userConn, request, ctx);
					handler.execute();
					id = handler.getLastInsertID();
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				String redirUrl = "";
				if (mode.equals("add")){					
					if (id != null && id.length()!=0)
						redirUrl = redirUrl + "namespace.jsp?mode=edit&ns_id=" + id;
				}
				else if (mode.equals("edit")){
					redirUrl = currentUrl;
					//redirUrl = redirUrl + "namespace.jsp?mode=edit&ns_id=" + id;
				}
				else if (mode.equals("delete")){
					String	deleteUrl = history.gotoLastMatching("namespaces.jsp");
					redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
					//redirUrl = redirUrl + "index.jsp";
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			if (!mode.equals("add") && (ns_id == null || ns_id.length()==0)){ %>
				<b>Namespace ID is missing!</b> <%
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			boolean nsEditable = false;
			
			if (!mode.equals("add")){
				Vector v = searchEngine.getNamespaces(ns_id);
				if (v!=null && v.size()!=0){
					namespace = (Namespace)v.get(0);
					if (!namespace.getID().equals("1") && namespace.getTable()==null && namespace.getDataset()==null)
						nsEditable = true;
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
<body>
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
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<form id="form1" method="POST" action="namespace.jsp">
			
			<table width="600" cellspacing="0">
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
							if (user!=null && nsEditable){ %>
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
			
			<table width="600" cellspacing="0"  cellpadding="0" border="0">
			
			<tr>
				<td align="right" style="padding-right:10">
					<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
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
						String shortName = namespace.getShortName();
						if (shortName==null) shortName = "";
						%>
						<font class="title2" color="#006666"><%=Util.replaceTags(shortName)%></font>
						<input type="hidden" name="ns_id" value="<%=ns_id%>"/>
						
					<% } else{ %>
						<input <%=disabled%> type="text" class="smalltext" size="10" name="ns_id"></input>
					<% } %>
				</td>
			</tr>
			
			<tr <% if (mode.equals("view")) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
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
						String fullName = Util.replaceTags(namespace.getFullName());
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
			
			<tr>	
				<td align="right" style="padding-right:10">
					<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
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
						String description = Util.replaceTags(namespace.getDescription());
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
			if (namespace.getTable()!=null){
				Dataset ds = searchEngine.getDataset(namespace.getDataset());
				String dsName = ds==null ? "" : ds.getShortName();
				%>
				<tr height="5"><td colspan="2">&#160;</td></tr>
				<tr>
					<td class="barfont" colspan="2">
						<b>NB!</b><br/>This namespace was automatically created in the process of creating the
						<a href="dstable.jsp?table_id=<%=namespace.getTable()%>&ds_id=<%=namespace.getDataset()%>&ds_name=<%=dsName%>&mode=view">corresponding table</a>
							and will also be automatically deleted when the latter will be deleted.
					</td>
				</tr><%
			}
			else if (namespace.getDataset()!=null){ %>
				<tr height="5"><td colspan="2">&#160;</td></tr>
				<tr>
					<td class="barfont" colspan="2">
						<b>NB!</b><br/>This namespace was automatically created in the process of creating the
						<a href="dataset.jsp?ds_id=<%=namespace.getDataset()%>&mode=view">corresponding dataset</a>
							and will also be automatically deleted when the latter will be deleted.
					</td>
				</tr><%
			}
			%>
		
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
						if (user==null || !nsEditable){ %>									
							<input type="button" class="mediumbuttonb" value="Save" disabled="true"/>&#160;&#160;
							<!--input type="button" class="mediumbuttonb" value="Delete" disabled="true"/>&#160;&#160;-->
						<%} else {%>
							<input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
							<!--input type="button" class="mediumbuttonb" value="Delete" onclick="submitForm('delete')"/>&#160;&#160; -->
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

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {}
}
%>
