<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static String oConnName="datadict";%>
<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private DataClass dataClass=null;%>
<%!private Vector namespaces=null;%>

<%!
private String getAttributeIdByName(String name){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}

private String getValue(String id){
	if (id==null) return null;
	if (mode.equals("add")) return null;
	DElemAttribute attr = dataClass.getAttributeById(id);
	if (attr == null) return null;
	return attr.getValue();
}

private String getAttributeObligationById(String id){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getID().equalsIgnoreCase(id))
        	return attr.getObligation();
	}
        
    return null;
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
			
		    String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
			
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
			
			String class_id = request.getParameter("class_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (!mode.equals("add") && (class_id == null || class_id.length()==0)){ %>
				<b>Data class ID is missing!</b> <%
				return;
			}

			
			if (request.getMethod().equals("POST")){
				
				DataClassHandler handler = new DataClassHandler(user.getConnection(), request, ctx);
				
				
				try {
					handler.execute();
				}
				catch (Exception e){
					%>
					<html><body><b><%=e.toString()%></b></body></html>
					<%
					return;
				}				
			
				String redirUrl = request.getContextPath();
				
				if (mode.equals("add")){
					String id = handler.getLastInsertID();
					if (id != null && id.length()!=0)
						redirUrl = redirUrl + "/data_class.jsp?mode=edit&class_id=" + id;
				}
				else if (mode.equals("edit")){
					redirUrl = redirUrl + "/data_class.jsp?mode=edit&class_id=" + class_id;
				}
				else if (mode.equals("delete")){
					//redirUrl = redirUrl + "/data_class.jsp?mode=add";
					%>
					<html><script>window.history.go(-1)</script></html>
					<%
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			//mAttributes = searchEngine.getDElemAttributes();
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			String class_name = "";
			
			Namespace namespace = null;
			
			if (!mode.equals("add")){
				dataClass = searchEngine.getDataClass(class_id);
				if (dataClass!=null){
					class_name = dataClass.getShortName();
					if (class_name == null) class_name = "unknown";
					if (class_name.length() == 0) class_name = "empty";
					namespace = dataClass.getNamespace();
				}
				else{ %>
					<b>Data Class was not found!</b> <%
					return;
				}
			}
			else{
				if (mAttributes.size()==0){ %>
					<b>No metadata on attributes found! Nothing to add.</b> <%
					return;
				}
				
			}
			
			namespaces = searchEngine.getNamespaces();
			if (namespaces == null) namespaces = new Vector();
			
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
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return;
				}
				
				if (hasWhiteSpace("class_name")){
					alert("Short name cannot contain any white space!");
					return;
				}
						
				if (hasWhiteSpace(document.forms["form1"].identifierInputName.value)){
					alert("Identifier cannot contain any white space!");
					return;
				}
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function checkObligations(){
			
			var o = document.forms["form1"].class_name;
			if (o!=null){
				if (o.value.length == 0) return false;
			}
			
			var elems = document.forms["form1"].elements;
			if (elems == null) return true;
			
			for (var i=0; i<elems.length; i++){
				var elem = elems[i];
				var elemName = elem.name;
				var elemValue = elem.value;
				if (startsWith(elemName, "attr_")){
					var o = document.forms["form1"].elements[i+1];
					if (o == null) return false;
					if (!startsWith(o.name, "oblig_"))
						continue;
					if (o.value == "M" && (elemValue==null || elemValue.length==0)){
						return false;
					}
				}
			}
			
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
		
		function startsWith(str, pattern){
			var i = str.indexOf(pattern,0);
			if (i!=-1 && i==0)
				return true;
			else
				return false;
		}
		
		function endsWith(str, pattern){
			var i = str.indexOf(pattern, str.length-pattern.length);
			if (i!=-1)
				return true;
			else
				return false;
		}

		function elems(url){
					wClassElems = window.open(url,"Classelements","height=600,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
					if (window.focus) {wClassElems.focus()}
		}
		function printable(url){
					wPrintablePage = window.open(url,"PrintablePage","height=600,width=700,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=no");
					if (window.focus) {wPrintablePage.focus()}
		}
		function complexAttrs(url){
					wComplexAttrs = window.open(url,"ComplexAttributes","height=600,width=500,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
		}
	
    </script>
    <script language="JAVASCRIPT" for="window" event="onload">    
    
			<%
			
			String attrID = getAttributeIdByName("Datatype");
			String attrValue = getValue(attrID);
			if (attrValue != null){
    		%>
				var o = document.forms["form1"].attr_<%=attrID%>;
				var datatype = '<%=attrValue%>';
				for (i=0; datatype!=null && o!=null && i<o.options.length; i++){
					if (o.options[i].value == datatype){
						o.selectedIndex = i;
						break;
					}
				}
				
			<%
			}
			
			if (dataClass != null){
				namespace = dataClass.getNamespace();
				if (namespace != null && mode.equalsIgnoreCase("add")){
					String namespace_id = namespace.getID();
					if (namespace_id != null){
		    		%>
		    			var ns_id = '<%=namespace_id%>';
						var o = document.forms["form1"].ns;
						for (i=0; o!=null && i<o.options.length; i++){
							if (o.options[i].value == ns_id){
								o.selectedIndex = i;
								break;
							}
						}
						
					<%
					}
				}
			}
			DElemAttribute attribute = null;
			%>
			
	</script>
</head>
<% if (!mode.equals("print")){ %>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0"">
<%@ include file="header.htm"%>
<% }
else{ %>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0" style="background-image: url('')">
<% } %>
<table border="0">
    <tr valign="top">
      <% if (!mode.equals("print")){ %>
		<td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
	  <% } %>
        <TD>
          <% if (!mode.equals("print")){ %>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Data Class"/>
            </jsp:include>
 	      <% } %>
            
			<div style="margin-left:30">
			<form name="form1" method="POST" action="data_class.jsp">
			
			<% if (!mode.equals("add")){ %>
				<input type="hidden" name="class_id" value="<%=class_id%>"/>
			<% } else { %>
				<input type="hidden" name="dummy"/>
			<% } %>
			
			<table width="auto">
  			  <% if (!mode.equals("print")){ %>
				<tr>
					<td colspan="3">
						<% if (mode.equals("add")){ %>
								<font class="head00">Add a data class</font>
							<% }
							   else{ %>
								<font class="head00">View/modify a data class</font>
							<% } %>
					</td>
				</tr>
				
				<tr height="10"><td colspan="3"></td></tr>

								
				<tr>
					<td colspan="3">
					
					To find out more about the definition fields below, please click on their titles.<br/>
					(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.
					</td>
				</tr>
				
			  <%
			  }
			  else{
			  %>
				<tr>
					<td colspan="3"><font class="head00">Data class</font></td>
				</tr>
				<tr height="10"><td colspan="3"></td></tr>
				<tr>
					<td colspan="3">				
						(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.
					</td>
				</tr>
			  <% } %>
				<tr height="10"><td colspan="3"></td></tr>
			</table>
			
			<table width="auto">
					
			<tr height="10"><td colspan="3"></td></tr>
			
			<tr>				
				<td width="150">
		  				<% if (!mode.equals("print")){ %>
							<b><a href="javascript:openShortName()"><font color="black">Short name</font></a></b>:&#160;(M)
						<%} else { %>
							<b><font color="black">Short name</font></b>:&#160;(M)
						<% } %>
				</td>
				<td colspan="2">
					<% if(!mode.equals("add")){ %>
						<font class="title2" color="#006666"><%=class_name%></font>
						<input type="hidden" name="class_name" value="<%=class_name%>"/>
					<% } else{ %>
						<input type="text" size="30" name="class_name"></input>
					<% } %>
				</td>
			</tr>
			
			<tr>				
				<td width="150">
	  				<% if (!mode.equals("print")){ %>
						<b><a href="javascript:openNamespace()"><font color="black">Namespace</font></a></b>:&#160;(M)
					<%
					} 
					else{ 
					%>
						<b><font color="black">Namespace</font></b>:&#160;(M)
					<%
					}
					%>
				</td>
				<td colspan="2">
				
					<% if(!mode.equals("add")){
						
						String nsDisp = "unknown";
						if (namespace != null){
							String nsUrl = namespace.getUrl();
							if (nsUrl.startsWith("/")) nsUrl = urlPath + nsUrl;
							nsDisp = namespace.getShortName() + " - " + nsUrl;
						}
						
						%>
						<font class="head0" color="#006666"><%=nsDisp%></font>
						<input type="hidden" name="ns" value="<%=namespace.getID()%>"/>
					<% } else{ %>
						<select name="ns">						
							<%
							for (int i=0; i<namespaces.size(); i++){
								Namespace ns = (Namespace)namespaces.get(i);
								String ns_id = ns.getID();
								String nsUrl = ns.getUrl();
								if (nsUrl.startsWith("/")) nsUrl = urlPath + nsUrl;
								String nsDisp = ns.getShortName() + " - " + nsUrl;
								if (ns_id.equalsIgnoreCase("basens")){
									%>
									<option selected value="<%=ns_id%>"><%=nsDisp%></option>
									<%
								}
								else {
									%>
									<option value="<%=ns_id%>"><%=nsDisp%></option>
									<%
								}						
							}
							%>
						</select>
					<% } %>
					
				</td>
			</tr>
			<%
			// dynamical display of attributes, really cool... I hope...
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				

				if (!attribute.displayFor("AGG") || !attribute.displayFor("DCL"))
					continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				
				String attrNs = attribute.getNamespace().getShortName();
				
				String width  = attribute.getDisplayWidth();
				String height = attribute.getDisplayHeight();
				
				disabled = user == null ? "disabled" : "";
				
				%>
				<tr>
					<td width="150">
		  				<% if (!mode.equals("print")){ %>
							<b><a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=edit">
								<font color="black"><%=attrNs%>:<%=attribute.getShortName()%></font>
							</a></b>&#160;(<%=attribute.getObligation()%>)
						<% }
						else {
						%>
							<b><font color="black"><%=attrNs%>:<%=attribute.getShortName()%></font></b>:&#160;(<%=attribute.getObligation()%>)
						<% } %>

					</td>
					<td colspan="2">
				<%
  				 if (mode.equals("print")){
					if (attrValue==null)
						%>&#160;<%
					else
						%><%=attrValue%><%
					continue;
				}

				if (attribute.getShortName().equalsIgnoreCase("Identifier")){					
					%>
					<input type="hidden" name="identifierInputName" value="attr_<%=attrID%>"/>
					<%
				}
				
				if (dispType.equals("text")){
					if (attrValue!=null){
						%>
						<input <%=disabled%> type="text" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>"/>
						<%
					}
					else{
						%>
						<input <%=disabled%> type="text" size="<%=width%>" name="attr_<%=attrID%>"/>
						<%
					}
				}
				else if (dispType.equals("textarea")){
					if (attrValue!=null){
						%>
						<textarea <%=disabled%> rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>"><%=attrValue%></textarea>
						<%
					}
					else{
						%>
						<textarea <%=disabled%> rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>"></textarea>
						<%
					}
				}
				else{
					if (attrValue!=null){
						%>
						<select <%=disabled%> name="attr_<%=attrID%>"><option selected value="<%=attrValue%>"><%=attrValue%></option></select>
						<%
					}
					else{
						%>
						<select <%=disabled%> name="attr_<%=attrID%>"><option value="">&#160;</option></select>
						<%
					}
				}
				
				%>
				</td></tr>
				<input type="hidden" name="oblig_<%=attrID%>" value="<%=attribute.getObligation()%>"/>
				<%
			}
			%>
			</table>
			
		
		<table width="auto">
		
		
		<% if (!mode.equals("add") && !mode.equals("print")){ 
		%>
			<tr>
				<td width="150pts"></td>
				<td colspan="2">
					* <a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=class_id%>&#38;parent_type=C&#38;parent_name=<%=class_name%>&#38;parent_ns=<%=namespace.getShortName()%>')">
						COMPLEX ATTRIBUTES
					</a>&#160;&#160;&lt;&#160;<font class="smallFont">click here to view/edit complex attributes specified for this data class</font>
				</td>
			</tr>
			<tr>
				<td width="150pts"></td>
				<td colspan="2">
					* <a href="javascript:elems('class2elems.jsp?class_id=<%=class_id%>&#38;class_name=<%=class_name%>&#38;ns=<%=namespace.getShortName()%>')">
						ELEMENTS
					  </a>&#160;&#160;&lt;&#160;<font class="smallFont">click here to view/add/remove elements of this class</font>
				</td>
			</tr>
		<% }
		%>
		<tr height="20"><td colspan="3"></td></tr>
		<tr>
			<td width="150pts"></td>
			<td colspan="2">
			
				<% 
				
				if (mode.equals("add")){ // if mode is "add"
					if (user==null){ %>									
						<input type="button" value="Add" disabled="true"/>&#160;&#160;
					<%} else {%>
						<input type="button" value="Add" onclick="submitForm('add')"/>&#160;&#160;
					<% }
				} // end if mode is "add"
				
				if (!mode.equals("add") && !mode.equals("print")){ // if mode is not "add" and not "print"
					if (user==null){ %>									
						<input type="button" value="Save" disabled="true"/>&#160;&#160;
						<input type="button" value="Delete" disabled="true"/>&#160;&#160;
					<%} else {%>
						<input type="button" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
						<input type="button" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;
					<% }
				} // end if mode is not "add"
				
				%>
				
			</td>
		</tr>
		<% if (!mode.equals("add") && !mode.equals("print")){ // if mode is not "add" and not "print" %>
		<tr height="20"><td colspan="3"></td></tr>
		<tr>
			<td colspan="3">
				<a href="javascript:printable('data_class_print.jsp?class_id=<%=class_id%>')">
					Printable page
				  </a>
			</td>
		</tr>
		<% } %>
		
		<input type="hidden" name="mode" value="<%=mode%>"/>
		
	</table>
	</form>
</div>
        </TD>
</TR>
</table>
</body>
</html>