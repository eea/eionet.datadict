<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*,eionet.util.Util"%>

<%!private String type=null;%>
<%!private String mode=null;%>
<%!private DElemAttribute attribute=null;%>
<%!private Vector attrFields=null;%>
<%!private DDSearchEngine  searchEngine=null;%>
<%@ include file="history.jsp" %>

			<%
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
		    String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";

			XDBApplication.getInstance(ctx);
			AppUserIF user = SecurityUtil.getUser(request);
			
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
			
			if (mode.equals("add")){
				boolean addPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/attributes", "i");
				if (!addPrm){ %>
					<b>Not allowed!</b> <%
					return;
				}
			}
			
			if (!mode.equals("add") && (attr_id == null || attr_id.length()==0)){ %>
				<b>Attribute ID is missing!</b> <%
				return;
			}
			
			String type = request.getParameter("type");
			if (type!=null && type.length()==0)
				type = null;
			
			String idPrefix = "";
			if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX))
				idPrefix = "c";
			else if (type!=null && type.equals(DElemAttribute.TYPE_SIMPLE))
				idPrefix = "s";
			
			// check permissions
			boolean editPrm = false;
			boolean deletePrm = false;
			if (!mode.equals("add")){
				editPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/attributes/" + idPrefix + attr_id, "u");
				deletePrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/attributes/" + idPrefix + attr_id, "d");
			}
			if (mode.equals("edit") && !editPrm){ %>
				<b>Not allowed!</b> <%
				return;
			}
			if (mode.equals("delete") && !deletePrm){ %>
				<b>Not allowed!</b> <%
				return;
			}
			
			if (request.getMethod().equals("POST")){
				
				Connection userConn = null;
				String redirUrl = "";
				
				try{
					userConn = user.getConnection();

					// if mode==delete, check whether the attribute is used somewhere. if Yes, then prompt user
					if (mode.equals("delete")){
						searchEngine = new DDSearchEngine(userConn, "", ctx);
						boolean hasObjects = searchEngine.hasAttributeObjects(attr_id, type);
						if (hasObjects){
					        String sName = request.getParameter("short_name");
					        
							response.sendRedirect("dialog_delete_attr.jsp?mode=delete&attr_id=" + attr_id + "&type=" + type + "&short_name=" + sName);
							return;
						}
					}

					AttributeHandler handler = new AttributeHandler(userConn, request, ctx);
					handler.setUser(user);
					handler.execute();
					
					if (mode.equals("add")){
						String id = handler.getLastInsertID();
						if (id != null && id.length()!=0)
							redirUrl = redirUrl + "delem_attribute.jsp?mode=edit&attr_id=" + id + "&type=" + type;

						if (history!=null){
							int idx = history.getCurrentIndex();
							if (backUrl.indexOf("mode=add")>0){ 
								history.remove(idx-1);
								idx--;
							}
							if (idx>0)
								history.remove(idx);
						}
					}
					else if (mode.equals("edit")){
						redirUrl = currentUrl;
						//redirUrl = redirUrl + "delem_attribute.jsp?mode=edit&attr_id=" + attr_id + "&type=" + type;
					}
					else if (mode.equals("delete")){
						String	deleteUrl = history.gotoLastMatching("attributes.jsp");
						redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
						//redirUrl = redirUrl + "delem_attribute.jsp?mode=add&type=SIMPLE";
						//redirUrl = redirUrl + "index.jsp";
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			searchEngine = new DDSearchEngine(conn, "", ctx);
			
			String attr_name = null;
			String attr_shortname = null;
			Namespace attrNamespace = null;
			
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
					
					attrNamespace = attribute.getNamespace();
						
					if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX)){
						attrFields = searchEngine.getAttrFields(attr_id);
						if (attrFields == null) attrFields = new Vector();
					}
				}
				else{ %>
					<b>Attribute was not found!</b> <%
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
			//var url = "fixed_values.jsp?delem_id=<%=attr_id%>&amp;delem_name=<%=attr_shortname%>&amp;parent_type=attr";
			var url = "fixed_values.jsp?mode=edit&delem_id=<%=attr_id%>&delem_name=<%=attr_shortname%>&parent_type=attr";
			wCh1Values = window.open(url,"AllowableValues","height=600,width=800,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
			if (window.focus) {wCh1Values.focus()}
		}
		
		function helpNamespace(){
			alert('Context is required to produce the XMLSchema exports of data defintions. ' +
					'Basically it defines the context in which you define this attribute. Attributes can be roughly ' +
					'divided into two contexts: those originating from ISO11179 and those specific to Data Dictionary.');
		}
		
		function harvest(){
			var msg = "This might take a couple of minutes, depending on the harvestign connection speed and " +	
						"the amount of objects to harvest!";
			wHarvest = window.open("HarvestingServlet","Harvest","height=200,width=300,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
			if (window.focus) {wHarvest.focus()}
		}
		
    </script>
</head>
<body onload="onLoad()">
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
                <jsp:param name="back" value="true"/>
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
						<td colspan="2"><span class="head00">Add an attribute definition</span></td> <%
					}
					else if (mode.equals("edit")){ %>
						<td colspan="2"><span class="head00">Edit attribute definition</span></td> <%
					}
					else{ %>
						<td><span class="head00">View attribute definition</span></td>
						<td align="right">
							<%
							if (user!=null && editPrm){ %>
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
				
				<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
				
			</table>
			
			<%
			int displayed = 0;
			%>
			
			<table width="auto" cellspacing="0">
			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
				
					<a target="_blank" href="attr_fields.html#type"><span class="help">?</span></a>&#160;
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
					<a target="_blank" href="attr_fields.html#short_name"><span class="help">?</span></a>&#160;
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
					<a target="_blank" href="attr_fields.html#name"><span class="help">?</span></a>&#160;
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
					<a target="_blank" href="attr_fields.html#context"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Context</b>
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
						
						String nsName = attrNamespace==null ? null : attrNamespace.getFullName();
						if (nsName==null){
							if (attrNamespace==null)
								nsName = "";
							else
								nsName = attrNamespace.getShortName();
						}
						if (nsName == null) nsName = "";
						
						%>
						<span class="barfont" style="width:400"><%=nsName%></span> <%
					}
					else{
						%>
						<select <%=disabled%> class="small" name="ns">
							<%
							Vector namespaces = searchEngine.getNamespaces();
							for (int k=0; namespaces!=null && k<namespaces.size(); k++){
								Namespace ns = (Namespace)namespaces.get(k);
								
								if (ns.getTable()!=null || ns.getDataset()!=null || (ns.getID()!=null && ns.getID().equals("1")))
									continue;
									
								String nsName = null;
								if (ns!=null) nsName = ns.getFullName();
								if (nsName == null) nsName = ns.getShortName();
								if (nsName == null) nsName = "";
								
								if (nsName.indexOf("attributes") < 0)
									continue;
								
								String ifSelected = "";
								if (attrNamespace!=null){
									if (attrNamespace.getID().equals(ns.getID())){
										ifSelected = "selected";
									}
								}
								else if (nsName.indexOf("Data Dictionary") != -1)
									ifSelected = "selected";
									
								%>
								<option <%=ifSelected%> value="<%=ns.getID()%>"><%=nsName%></option>
								<%
							}
							%>
						</select>
						<%
					}
					%>
				</td>
			</tr>
			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>	
				<td align="right" style="padding-right:10">
					<a target="_blank" href="attr_fields.html#definition"><span class="help">?</span></a>&#160;
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
						<a target="_blank" href="attr_fields.html#obligation"><span class="help">?</span></a>&#160;
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
						<a target="_blank" href="attr_fields.html#disp_type"><span class="help">?</span></a>&#160;
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
							else if (dispType.equals("image"))
								dispDispType = "Image";
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
								<option value="image">Image</option>
							</select>
							<%
							if (mode.equals("edit") && dispType!=null && dispType.equals("select")){
								%>
								&#160;<span class="smallfont"><a href="fixed_values.jsp?mode=edit&amp;delem_id=<%=attr_id%>&amp;delem_name=<%=attr_shortname%>&amp;parent_type=attr">
								<b>FIXED VALUES</b></a></span>
								<%
							}
						}
						%>
					</td>
				</tr>
				<%
				if (mode.equals("view") && dispType!=null && dispType.equals("select")){
				%>
					<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<td align="right" style="padding-right:10">
							<a target="_blank" href="attr_fields.html#fxv"><span class="help">?</span></a>&#160;
							<span class="mainfont">
								<a href="fixed_values.jsp?mode=view&amp;delem_id=<%=attr_id%>&amp;delem_name=<%=attr_shortname%>&amp;parent_type=attr">
									<b>Fixed values</b>
								</a>
							</span>
						</td>
						<td>
						<%
							displayed++;
							Vector fxValues = searchEngine.getFixedValues(attr_id, "attr");
							if (fxValues!=null && fxValues.size()>0){ 
								for (int g=0; g<fxValues.size(); g++){
									FixedValue fxValue = (FixedValue)fxValues.get(g);
									%>
									<span class="barfont" style="width:400"><%=Util.replaceTags(fxValue.getValue())%></span><br>
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
						<a target="_blank" href="attr_fields.html#disp_mult"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Display multiple</b>
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
							String multi = attribute.getDisplayMultiple();
							String checked = (multi.equals("1")) ? "checked":"";
							String checked_text = (multi.equals("1")) ? "True":"False";
							if (mode.equals("edit")){
								%>
								<input <%=disabled%> <%=checked%> type="checkbox" class="smalltext" name="dispMultiple" value="1"></input>
								<%
							}
							else{
								%>
								<span class="barfont" style="width:400"><%=checked_text%></span>
								<%
							}
						}
					else {
						%>
						<input <%=disabled%> type="checkbox" class="smalltext" name="dispMultiple" value="1"></input>
						<%
					}
					%>
				</td>
			</tr>
				<%
			}
			%>
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" valign="top" style="padding-right:10">
					<a target="_blank" href="attr_fields.html#inherit"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Inheritance</b>
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
					String inh_text[]=new String[3];
					inh_text[0] = "No inheritance";
					inh_text[1] = "Inherit attribute values from parent level wtih possibilty to add new values";
					inh_text[2] = "Inherit attribute values from parent level wtih possibilty to overwrite them";
					int chk = 0;

					if (!mode.equals("add")){
						String inherit = attribute.getInheritable();
						if (inherit==null) inherit="0";
						chk =  Integer.parseInt(inherit);
					}						
					if (mode.equals("view")){
						%>
						<span class="barfont" style="width:400"><%=inh_text[chk]%></span>
						<%
					}
					else{
						for (int i=0;i<3;i++){
						%>
							<input value="<%=i%>" <%=disabled%> <% if (i==chk) %>checked<%;%> type="radio" class="smalltext" name="inheritable"><%=inh_text[i]%></input><br>
						<%
						}
					}
				%>
				</td>
			</tr>
			
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a target="_blank" href="attr_fields.html#disp_order"><span class="help">?</span></a>&#160;
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
						<a target="_blank" href="attr_fields.html#disp_for"><span class="help">?</span></a>&#160;
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
							if (ch1Checked.equals("checked")) { hasOne = true; %>
								Data elements with fixed values <%
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
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=ch1Checked%> name="dispWhen" value="CH1"><span class="barfont">Data elements with fixed values</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=ch2Checked%> name="dispWhen" value="CH2"><span class="barfont">Data elements with quanitative values</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=dstChecked%> name="dispWhen" value="DST"><span class="barfont">Datasets</span></input></br>
							<input <%=disabled%> type="checkbox" style="height:13;width:13" <%=tblChecked%> name="dispWhen" value="TBL"><span class="barfont">Dataset tables</span></input></br>
							<%
						}
						%>
						
					</td>
				</tr>
			<%
			} 
			%>
			<%
			if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
				%>
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a target="_blank" href="attr_fields.html#dispw"><span class="help">?</span></a>&#160;
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
						<a target="_blank" href="attr_fields.html#disph"><span class="help">?</span></a>&#160;
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
			
			
			// start HARVESTER LINK
			
			boolean dispHarvesterID = false;
			String harvesterID = null;
			if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX)){
				
				if (!mode.equals("add"))
					harvesterID = attribute.getHarvesterID();
					
				if (mode.equals("view")){
					if (!Util.voidStr(harvesterID))
						dispHarvesterID = true;
				}
				else
					dispHarvesterID = true;
			}
			
			if (dispHarvesterID){
				
				Vector harvesters = null;
				if (!mode.equals("view"))
					harvesters = searchEngine.getHarvesters();
				
				%>
			
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a target="_blank" href="attr_fields.html"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Linked harvester</b>
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
						if (!mode.equals("view")){							
							String noLinkSelected = Util.voidStr(harvesterID) ? "selected" : "";
							%>
							<select class="small" name="harv_id">
								<option <%=noLinkSelected%> value="null">-- no link --</option>
								<%
								for (int i=0; harvesters!=null && i<harvesters.size(); i++){
									String harvID = (String)harvesters.get(i);
									String selected = "";
									if (!Util.voidStr(harvesterID) && harvesterID.equals(harvID))
										selected = "selected";
									%>
									<option <%=selected%> value="<%=harvID%>"><%=harvID%></option><%
								}
								%>
							</select><%
							
							if (user!=null && user.isAuthentic()){ %>
								<input type="button" class="smallbutton" value="Harvest" onclick="harvest()"/><%
							}
						}
						else { %>
							<span class="barfont" style="width:400"><%=harvesterID%></span> <%
						}
						%>
					</td>
				</tr>
				<%
			}
			
			// end HARVESTER LINK
			%>
			
		<% if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX) && !mode.equals("add")){ // if COMPLEX and mode=add
		%>
		<tr valign="top">
			<td align="right" style="padding-right:10">
				<span class="mainfont"><b>Fields</b></span>
			</td>
			<td>
				<table>
					<tr>
						<th width="100">Name</th>
						<th width="300">Definition</th>
					</tr>
					<%
	
					//String position = String.valueOf(attrFields.size() + 1);
					int position = 0;
					if (attrFields!=null){
						for (int i=0; i<attrFields.size(); i++){
							Hashtable hash = (Hashtable)attrFields.get(i);
							String id = (String)hash.get("id");
							String name = (String)hash.get("name");
							String definition = (String)hash.get("definition");
							if (definition.length()>50) definition = definition.substring(0,50) + " ...";
							String fieldLink = "m_attr_field.jsp?mode=view&attr_id=" + attr_id + "&attr_name=" + attr_name + "&attr_ns=basens&field_id=" + id;
			
							int pos = Integer.parseInt((String)hash.get("position"));
							if (pos >= position) position = pos +1;
			
							%>
							<tr <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
								<td align="center" width="100"><a href="<%=fieldLink%>"><%=name%></a></td>
								<td align="center" width="300" onmouseover=""><%=definition%></td>
							</tr>
							<%
						}
					}
					%>
				</table>
			</td>
		</tr>
		<%
		if (user!=null){
			%>
			<tr valign="top">
				<td></td>
				<td>
					<b>*</b> <span class="smallfont"><a href="m_attr_fields.jsp?attr_id=<%=attr_id%>&amp;attr_name=<%=attr_shortname%>">
						<b>FIELDS</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						&lt;&#160;click here to add/remove fields of this complex attribute
					</span>
				</td>
			</tr>
			<%
		}
		%>
			
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

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {}
}
%>
