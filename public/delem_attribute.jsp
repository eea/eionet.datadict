<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static String oConnName="datadict";%>
<%!private String type=null;%>
<%!private String mode=null;%>
<%!private DElemAttribute attribute=null;%>
<%!private Vector namespaces=null;%>

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
			
			String attr_id = request.getParameter("attr_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (!mode.equals("add") && (attr_id == null || attr_id.length()==0)){ %>
				<b>Attribute ID is missing!</b> <%
				return;
			}
			
			String type = request.getParameter("type");
			if (type!=null && type.length()==0)
				type = null;
			/*if (type == null || type.length()==0) { %>
				<b>Type paramater is missing!</b>
				<%
				return;
			}*/
			
			if (request.getMethod().equals("POST")){
				
				AttributeHandler handler = new AttributeHandler(user.getConnection(), request, ctx);
				
				handler.execute();
				
				String redirUrl = request.getContextPath();
				
				if (mode.equals("add")){
					String id = handler.getLastInsertID();
					if (id != null && id.length()!=0)
						redirUrl = redirUrl + "/delem_attribute.jsp?mode=edit&attr_id=" + id + "&type=" + type;
				}
				else if (mode.equals("edit")){
					redirUrl = redirUrl + "/delem_attribute.jsp?mode=edit&attr_id=" + attr_id + "&type=" + type;
				}
				else if (mode.equals("delete")){
					//redirUrl = redirUrl + "/delem_attribute.jsp?mode=add&type=SIMPLE";
					%>
					<html><script>window.history.go(-1)</script></html>
					<%
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			String attr_name = null;
			String attr_shortname = null;
			
			Namespace namespace = null;

			if (!mode.equals("add")){
				Vector v = searchEngine.getDElemAttributes(attr_id,type);
				if (v!=null && v.size()!=0)
					attribute = (DElemAttribute)v.get(0);
				if (attribute!=null){
					type = attribute.getType();
					attr_name = attribute.getName();
					attr_shortname = attribute.getShortName();
					if (attr_name == null) attr_name = "unknown";
					if (attr_shortname == null) attr_shortname = "unknown";
					namespace = attribute.getNamespace();
				}
				else{ %>
					<b>Attribute was not found!</b> <%
					return;
				}
			}
			else{				
				namespaces = searchEngine.getNamespaces();
				if (namespaces == null) namespaces = new Vector();
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
				var b = confirm("This attribute will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory fields!");
					return;
				}
				
				<%
				if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
					%>
					if (!checkDisplayFor()){
						alert("You have to specify 'Display for' !");
						return;
					}
					<%
				}
				%>
				
				if (hasWhiteSpace("short_name")){
					alert("Short name cannot contain any white space!");
					return;
				}
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function checkObligations(){
			
			var oName = document.forms["form1"].name;
			var name = oName==null ? null : oName.value;

			<%
			if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
				%>
				var oOblig = document.forms["form1"].obligation;
				var i = oOblig.selectedIndex;
				var oblig = oOblig==null ? null : oOblig.options[i].value;
				
				if (oblig == null || oblig.length==0) return false;
				<%
			}
			%>
			
			var oShort = document.forms["form1"].short_name;
			var shortn = oShort==null ? null : oShort.value;
			
			var oTypeSelect = document.forms["form1"].typeSelect;
			if (oTypeSelect != null){
				var type = oTypeSelect.value;
				if (type.length==0)
					return false;
			}
			
			if (name == null || name.length==0) return false;
			if (shortn == null || shortn.length==0) return false;			
			
			return true;
		}
		
		function checkDisplayFor(){

			var i;
			var b = false;
			
			var checks = document.forms["form1"].elements["dispWhen"];			
			if (checks!=null && checks.length!=0){
				for (i=0; i<checks.length; i++){
					if (checks[i].checked) return true;
				}
			}
			
			return false;
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

		function fields(url){
			wAttrFields = window.open(url,"AttributeFields","height=600,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wAttrFields.focus()}
		}

		function onLoad(){
			<%
			String obligation = "";
			String dispType = "";
			if (!mode.equals("add")){
				
				obligation = attribute.getObligation();
				if (obligation != null && !mode.equals("view")){
    			%>
					var obligation = '<%=obligation%>';
					var o = document.forms["form1"].obligation;
					for (i=0; o!=null && i<o.options.length; i++){
						if (o.options[i].value == obligation){
							o.selectedIndex = i;
							break;
						}
					}
					
				<% }
				
				dispType = attribute.getDisplayType();
				if (dispType == null)
					dispType = "";

				if (!mode.equals("view")){
					%>
						var dispType = '<%=dispType%>';
						var o = document.forms["form1"].dispType;
						for (i=0; o!=null && i<o.options.length; i++){
							if (o.options[i].value == dispType){
								o.selectedIndex = i;
								break;
							}
						}
						
					<%
				}
			}
			
			%>
		}
		
		function goToEdit(){
			document.location.assign("delem_attribute.jsp?attr_id=<%=attr_id%>&type=<%=type%>&mode=edit");
		}
		
		function fixType(){
			var type = document.forms["form1"].typeSelect.value;
			if (type == null || type.length==0)
				return;
			document.location.assign("delem_attribute.jsp?mode=add&type=" + type);
		}
		
		function openFxValues(){
			//var url = "fixed_values.jsp?delem_id=<%=attr_id%>&#38;delem_name=<%=attr_shortname%>&#38;parent_type=attr";
			var url = "fixed_values.jsp?delem_id=<%=attr_id%>&delem_name=<%=attr_shortname%>&parent_type=attr";
			wCh1Values = window.open(url,"AllowableValues","height=600,width=800,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
			if (window.focus) {wCh1Values.focus()}
		}
    </script>
</head>
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
                <jsp:param name="name" value="Attribute"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<form id="form1" name="form1" method="POST" action="delem_attribute.jsp">
			
			<% if (!mode.equals("add")){ %>
				<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
				<%
				
				if (type!=null && type.equals(DElemAttribute.TYPE_SIMPLE)){
					%>
					<input type="hidden" name="simple_attr_id" value="<%=attr_id%>"/>
					<%
				}
				else{
					%>
					<input type="hidden" name="complex_attr_id" value="<%=attr_id%>"/>
					<%							
				}
				
			} %>
			
			<table width="500" cellspacing="0">
				<tr>
					<%
					if (mode.equals("add")){ %>
						<td colspan="2"><span class="head00">Add an attribute</span></td> <%
					}
					else if (mode.equals("edit")){ %>
						<td colspan="2"><span class="head00">Edit attribute</span></td> <%
					}
					else{ %>
						<td><span class="head00">View attribute</span></td>
						<td align="right">
							<%
							if (user!=null){ %>
								<input type="button" class="smallbutton" value="Edit" onclick="goToEdit()"/> <%
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
						<%
						if (type==null){ %>
							<br/><br/><b>NB! Please select the attribute type first. Otherwise your entries will be lost.
							Also, for simple attributes more inputs will be displayed.</b> <%
						}
						%>
						</span></td>
					</tr> <%
				}
				%>
				
				<tr height="5"><td colspan="2"></td></tr>
				
				<tr><td <td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
				
			</table>
			
			<%
			int displayed = 0;
			%>
			
			<table width="auto" cellspacing="0">
			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
				
					<a href="javascript:openAttrType()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Type</b>
						<%
						displayed++;
						if (!mode.equals("view")){
							%>
							&#160;(M)
							<%
						}
						%>
					</span>
				</td>
				<td>
					<%
					if (mode.equals("add") && type==null){ %>
						<select class="small" name="typeSelect" onchange="fixType()">
							<option value="">-- Select attribute type --</option>
							<option value="SIMPLE">Simple attribute</option>
							<option value="COMPLEX">Complex attribute</option>
						</select> <%
					}
					else{
						if(type.equals(DElemAttribute.TYPE_SIMPLE)){ %>
							<b>SIMPLE ATTRIBUTE</b>
						<% }else if (type.equals(DElemAttribute.TYPE_COMPLEX)){ %>
							<b>COMPLEX ATTRIBUTE</b>
						<% } else{ %>
							<b>SIMPLE ATTRIBUTE</b>
						<% }
					}
					%>
				</td>
			</tr>
			
			<tr height="10"><td colspan="2"></td></tr>
			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a href="javascript:openAttrShortName()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Short name</b>
						<%
						displayed++;
						if (!mode.equals("view")){
							%>
							&#160;(M)
							<%
						}
						%>
					</span>
				</td>
				<td>
					<% if(!mode.equals("add")){ %>
						<font class="title2" color="#006666"><%=attr_shortname%></font>
						<input type="hidden" name="short_name" value="<%=attr_shortname%>"/>
					<% } else{ %>
						<input type="text" class="smalltext" size="30" name="short_name"></input>
					<% } %>
				</td>
			</tr>
			

			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a href="javascript:openAttrName()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Name</b>
						<%
						displayed++;
						if (!mode.equals("view")){
							%>
							&#160;(M)
							<%
						}
						%>
					</span>
				</td>
				<td>
					<% if(mode.equals("edit")){ %>						
						<input <%=disabled%> type="text" class="smalltext" size="30" name="name" value="<%=attr_name%>"></input>
					<% } else if (mode.equals("add")){ %>
						<input <%=disabled%> type="text" class="smalltext" size="30" name="name"></input>
					<% } else { %>
						<span class="barfont" style="width:400"><%=attr_name%></span>
					<% } %>
				</td>
			</tr>
			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>	
				<td align="right" style="padding-right:10">
					<a href="javascript:openAttrDefinition()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Definition</b>
						<%
						displayed++;
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
						String definition = (attribute.getDefinition() == null) ? "" : attribute.getDefinition();
						if (mode.equals("edit")){
							%>
							<textarea <%=disabled%> class="small" rows="5" cols="52" name="definition"><%=definition%></textarea>
							<%
						}
						else{
							%>
							<span class="barfont" style="width:400"><%=definition%></span>
							<%
						}
					}
					else{
						%>
						<textarea <%=disabled%> class="small" rows="5" cols="52" name="definition"></textarea>
						<%
					}
					%>
				</td>
			</tr>
			
			<%
			if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
				%>
				
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>	
					<td align="right" style="padding-right:10">
						<a href="javascript:openAttrObligation()"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Obligation</b>
							<%
							displayed++;
							if (!mode.equals("view")){
								%>
								&#160;(M)
								<%
							}
							%>
						</span>
					</td>
					<td>
						<%
						if (mode.equals("view")){
							String dispOblig = "";
							if (obligation != null && obligation.equals("M"))
								dispOblig = "Mandatory";
							else if (obligation != null && obligation.equals("O"))
								dispOblig = "Optional";
							else if (obligation != null && obligation.equals("C"))
								dispOblig = "Conditional";
							%>
							<span class="barfont" style="width:400"><%=dispOblig%></span>
							<%
						}
						else{
							%>
							<select <%=disabled%> class="small" name="obligation">
								<option selected="true" value="M">Mandatory</option>
								<option value="O">Optional</option>
								<option value="C">Conditional</option>
							</select>
							<%
						}
						%>
					</td>
				</tr>
				
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Display type</b>
							<%
							displayed++;
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
						if (mode.equals("view")){
							String dispDispType = "Not specified";
							if (dispType.equals("text"))
								dispDispType = "Text box";
							else if (dispType.equals("textarea"))
								dispDispType = "Text area";
							else if (dispType.equals("select"))
								dispDispType = "Select box";
							%>
							<span class="barfont" style="width:400"><%=dispDispType%></span>
							<%
						}
						else{
							%>
							<select <%=disabled%> class="small" name="dispType">
								<option value="">- Do not display at all -</option>
								<option selected value="text">Text box</option>
								<option value="textarea">Text area</option>
								<option value="select">Select box</option>
							</select>
							<%
							if (mode.equals("edit") && dispType!=null && dispType.equals("select")){
								%>
								&#160;<span class="smallfont"><a href="fixed_values.jsp?delem_id=<%=attr_id%>&delem_name=<%=attr_shortname%>&parent_type=attr">
								<b>FIXED VALUES</b></a></span>
								<%
							}
						}
						%>
					</td>
				</tr>
				<%
			}
			%>
			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Display order</b>
						<%
						displayed++;
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
						int i = attribute.getDisplayOrder();
						String dispOrder = (i==999) ? "" : String.valueOf(i);
						if (mode.equals("edit")){
							%>
							<input <%=disabled%> type="text" class="smalltext" size="5" name="dispOrder" value="<%=dispOrder%>"></input>
							<%
						}
						else{
							%>
							<span class="barfont" style="width:400"><%=dispOrder%></span>
							<%
						}
					}
					else {
						%>
						<input <%=disabled%> type="text" class="smalltext" size="5" name="dispOrder"></input>
						<%
					}
					%>
				</td>
			</tr>
			
			<%
			if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){ %>
			
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" valign="top" style="padding-right:10">
						<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Display for</b>
							<%
							displayed++;
							if (!mode.equals("view")){
								%>
								&#160;(M)
								<%
							}
							%>
						</span>
					</td>
					<td>					
						<%
						String aggChecked = (!mode.equals("add") && attribute.displayFor("AGG")) ? "checked" : "";
						String ch1Checked = (!mode.equals("add") && attribute.displayFor("CH1")) ? "checked" : "";
						String ch2Checked = (!mode.equals("add") && attribute.displayFor("CH2")) ? "checked" : "";
						//String dclChecked = (!mode.equals("add") && attribute.displayFor("DCL")) ? "checked" : "";
						String dstChecked = (!mode.equals("add") && attribute.displayFor("DST")) ? "checked" : "";
						String tblChecked = (!mode.equals("add") && attribute.displayFor("TBL")) ? "checked" : "";
						String fxvChecked = (!mode.equals("add") && attribute.displayFor("FXV")) ? "checked" : "";
						
						if (mode.equals("view")){
							boolean hasOne = false;
							%>
							<span class="barfont" style="width:400">
							<%
							if (aggChecked.equals("checked")) { hasOne = true; %>
								Aggregate data elements <%
							}
							if (ch1Checked.equals("checked")) { hasOne = true; %>
								<br/>Data elements with fixed values <%
							}
							if (ch2Checked.equals("checked")) { hasOne = true; %>
								<br/>Data elements with quanitative values <%
							}
							if (dstChecked.equals("checked")) { hasOne = true; %>
								<br/>Datasets <%
							}
							if (tblChecked.equals("checked")) { hasOne = true; %>
								<br/>Dataset tables <%
							}
							if (fxvChecked.equals("checked")) { hasOne = true; %>
								<br/>Fixed values <%
							}
							if (!hasOne){ %>
								Not specified<%
							}
							%>
							</span>
							<%
						}
						else {
							%>							
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=aggChecked%> name="dispWhen" value="AGG"><span class="barfont">Aggregate data elements</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=ch1Checked%> name="dispWhen" value="CH1"><span class="barfont">Data elements with fixed values</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=ch2Checked%> name="dispWhen" value="CH2"><span class="barfont">Data elements with quanitative values</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=dstChecked%> name="dispWhen" value="DST"><span class="barfont">Datasets</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=tblChecked%> name="dispWhen" value="TBL"><span class="barfont">Dataset tables</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=fxvChecked%> name="dispWhen" value="FXV"><span class="barfont">Fixed values</span></input></br>
							<%
						}
						%>
						
					</td>
				</tr>
			<%
			} 
			
			if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
				%>
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Display width</b>
							<%
							displayed++;
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
							String dispWidth = attribute.getDisplayWidth();
							if (mode.equals("edit")){
								%>
								<input <%=disabled%> type="text" class="smalltext" size="5" name="dispWidth" value="<%=dispWidth%>"></input>
								<%
							}
							else{
								%>
								<span class="barfont" style="width:400"><%=dispWidth%></span>
								<%
							}
						}
						else {
							%>
							<input <%=disabled%> type="text" class="smalltext" size="5" name="dispWidth"></input>
							<%
						}
						%>
					</td>
				</tr>
				<%
			}
			
			if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
				%>
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Display height</b>
							<%
							displayed++;
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
							String dispHeight = attribute.getDisplayHeight();
							if (mode.equals("edit")){
								%>
								<input <%=disabled%> type="text" class="smalltext" size="5" name="dispHeight" value="<%=dispHeight%>"></input>
								<%
							}
							else{
								%>
								<span class="barfont" style="width:400"><%=dispHeight%></span>
								<%
							}
						}
						else {
							%>
							<input <%=disabled%> type="text" class="smalltext" size="5" name="dispHeight"></input>
							<%
						}
						%>
					</td>
				</tr>
				<%
			}
			%>
				
			
		<% if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX) && !mode.equals("add")){ // if COMPLEX and mode=add
		%>
			
		<tr valign="top">
			<td></td>
			<td>
				<b>*</b> <span class="smallfont"><a href="javascript:fields('m_attr_fields.jsp?attr_id=<%=attr_id%>&#38;attr_name=<%=attr_shortname%>&#38;attr_ns=<%=namespace.getShortName()%>')">
					<b>FIELDS</b></a></span>&#160;&#160;
				<span class="smallfont" style="font-weight: normal">
					&lt;&#160;click here to view/add/remove fields of this complex attribute
				</span>
			</td>
		</tr>
			
		<% } // end if COMPLEX and mode=add
	
		if (!mode.equals("view")){ %>
			<tr height="10"><td colspan="2"></td></tr>
			<tr valign="top">
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
		
		if (type!=null){ %>
			<input type="hidden" name="type" value="<%=type%>"/> <%
		}
		%>
		<input type="hidden" name="mode" value="<%=mode%>"/>
		
		<input type="hidden" name="ns" value="basens"/>
		
	</table>
	</form>
</div>
        </TD>
</TR>
</table>
	<script>
//			alert("vorm");
	</script>
</body>
</html>