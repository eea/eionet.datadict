<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static String oConnName="datadict";%>
<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private Vector attributes=null;%>
<%!private Dataset dataset=null;%>
<%!private Vector complexAttrs=null;%>
<%!private Vector tables=null;%>

<%!

private String getValue(String id){
	if (id==null) return null;
	if (mode.equals("add")) return null;
	
	for (int i=0; attributes!=null && i<attributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)attributes.get(i);
		if (id.equals(attr.getID()))
			return attr.getValue();
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

%>

			<%
			
			DDuser user = getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
		    String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
			
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
			
			String ds_id = request.getParameter("ds_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (!mode.equals("add") && (ds_id == null || ds_id.length()==0)){ %>
				<b>Dataset ID is missing!</b> <%
				return;
			}
			
			if (request.getMethod().equals("POST")){
				
				DatasetHandler handler = new DatasetHandler(user.getConnection(), request, ctx);
				
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
						redirUrl = redirUrl + "/dataset.jsp?mode=edit&ds_id=" + id;
				}
				else if (mode.equals("edit")){
					redirUrl = redirUrl + "/dataset.jsp?mode=edit&ds_id=" + ds_id;
				}
				else if (mode.equals("delete")){
					%>
					<html><script>window.history.go(-1)</script></html>
					<%
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			String ds_name = "";
			String version = "";
			
			if (!mode.equals("add")){
				
				dataset = searchEngine.getDataset(ds_id);
				if (dataset!=null){
					ds_name = dataset.getShortName();
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";
					
					version = dataset.getVersion();
					if (version == null) version = "unknown";
					if (version.length() == 0) version = "empty";
				}
				else{ %>
					<b>Dataset was not found!</b> <%
					return;
				}
			}
			else{
				if (mAttributes.size()==0){ %>
					<b>No metadata on attributes found! Nothing to add.</b> <%
					return;
				}
			}
			
			attributes = searchEngine.getAttributes(ds_id, "DS", DElemAttribute.TYPE_SIMPLE);
			
			DElemAttribute attribute = null;
			String attrID = null;
			String attrValue = null;
			
			complexAttrs = searchEngine.getComplexAttributes(ds_id, "DS");		
			if (complexAttrs == null) complexAttrs = new Vector();
			tables = searchEngine.getDatasetTables(ds_id);
			
			%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
    
    	function openSchema(){
			window.open("station.xsd",null, "height=400,width=600,status=no,toolbar=no,menubar=no,location=no,scrollbars=yes,top=100,left=100");
		}

		function submitForm(mode){
			
			if (mode == "delete"){
				var b = confirm("This dataset will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return;
				}
				
				if (hasWhiteSpace("ds_name")){
					alert("Short name cannot contain any white space!");
					return;
				}
				
				var identifierInputName = document.forms["form1"].IdentifierInputName.value;
						
				if (identifierInputName!=null && hasWhiteSpace(identifierInputName)){
					alert("Identifier cannot contain any white space!");
					return;
				}
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function checkObligations(){
			
			var o = document.forms["form1"].delem_name;
			if (o!=null){
				if (o.value.length == 0) return false;
			}
			
			var o = document.forms["form1"].version;
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

		function openTables(uri){
			uri = uri + "&open=true";
			wElems = window.open(uri,"DatasetTables","height=500,width=670,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wElems.focus()}
		}

		function complexAttrs(url){
					wComplexAttrs = window.open(url,"ComplexAttributes","height=600,width=500,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
					if (window.focus) {wComplexAttrs.focus()}
		}
		
		function goTo(mode, id){
			if (mode == "edit"){
				document.location.assign("dataset.jsp?mode=edit&ds_id=" + id);
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
                <jsp:param name="name" value="Dataset"/>
            </jsp:include>
            
			<div style="margin-left:30">
			<form name="form1" id="form1" method="POST" action="dataset.jsp">
			
			<% if (!mode.equals("add")){ %>
				<input type="hidden" name="ds_id" value="<%=ds_id%>"/>
			<% } else { %>
				<input type="hidden" name="dummy"/>
			<% } %>
			
			<table width="500" cellspacing="0">
				<tr>
						<%
						if (mode.equals("add")){ %>
							<td colspan="2"><span class="head00">Add a dataset</span></td> <%
						}
						else if (mode.equals("edit")){ %>
							<td colspan="2"><span class="head00">Edit dataset</span></td> <%
						}
						else{ %>
							<td><span class="head00">View dataset</span></td>
							<td align="right">
								<%
								if (user!=null){ %>
									<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=ds_id%>')"/> <%
								}
								else{ %>&#160;
<!--									<input type="button" class="smallbutton" value="Edit" disabled/ --> 
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
			
			<table width="auto" cellspacing="0" cellpadding="0" border="0">
			
			<%
			int displayed = 0;
			%>
			
			<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a href="javascript:openShortName()"><span class="help">?</span></a>&#160;
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
				<td colspan="2">
					<% if(!mode.equals("add")){ %>
						<font class="title2" color="#006666"><%=ds_name%></font>
						<input type="hidden" name="ds_name" value="<%=ds_name%>"/>
					<% } else{ %>
						<input class="smalltext" type="text" size="30" name="ds_name"></input>
					<% } %>
				</td>
			</tr>
			
			<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a href="javascript:openVersion()"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Version</b>
						<%
						if (!mode.equals("view")){
							%>
							&#160;(M)
							<%
						}
						%>
					</span>
				</td>
				<td colspan="2">
					<% if(!mode.equals("add")){ %>
						<font class="title2" color="#006666"><%=version%></font>
						<input type="hidden" name="version" value="<%=version%>"/>
					<% } else{ %>
						<input class="smalltext" type="text" size="30" name="version"></input>
					<% } %>
				</td>
			</tr>
			
			
			<%

			// dynamical display of attributes, really cool... I hope...
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				
				String attrOblig = attribute.getObligation();
				
				if (!attribute.displayFor("DST")) continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				
				if (mode.equals("view") && (attrValue==null || attrValue.length()==0))
					continue;
				
				displayed++;
				
				String attrNs = attribute.getNamespace().getShortName();
				
				String width  = attribute.getDisplayWidth();
				String height = attribute.getDisplayHeight();
				
				String disabled = user == null ? "disabled" : "";
				
				%>
				<tr <% if (mode.equals("view") && displayed % 2 == 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" valign="top" style="padding-right:10">
						<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=edit"><span class="help">?</span></a>&#160;
						<span class="mainfont"><!--%=attrNs%>:--><b><%=attribute.getShortName()%></b>
							<%
							if (!mode.equals("view")){
								%>
								&#160;(<%=attribute.getObligation()%>)
								<%
							}
							%>
						</span>
					</td>
					
					<td colspan="2">
						<%				
						if (attribute.getShortName().equalsIgnoreCase("Identifier")){					
							%>
							<input type="hidden" name="IdentifierInputName" value="attr_<%=attrID%>"/>
							<%
						}
						
						// if mode is 'view', display a span, otherwise an input
						
						if (mode.equals("view")){
							%>
							<span class="barfont" style="width:400"><%=attrValue%></span>
							<%
						}
						else{ // start display input
						
							if (dispType.equals("text")){
								if (attrValue!=null){
									%>
									<input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>"/>
									<%
								}
								else{
									%>
									<input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>"/>
									<%
								}
							}
							else if (dispType.equals("textarea")){
								if (attrValue!=null){
									%>
									<textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>"><%=attrValue%></textarea>
									<%
								}
								else{
									%>
									<textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>"></textarea>
									<%
								}
							}
							else if (dispType.equals("select")){ %>							
								<select <%=disabled%> class="small" name="attr_<%=attrID%>">
									<%
									Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
									if (fxValues==null || fxValues.size()==0){ %>
										<option selected value=""></option> <%
									}
									else{
										for (int g=0; g<fxValues.size(); g++){
											FixedValue fxValue = (FixedValue)fxValues.get(g);
											String isSelected = fxValue.getDefault() ? "selected" : "";
											if (attrValue!=null && attrValue.equals(fxValue.getValue()))
												isSelected = "selected";
											%>
											<option <%=isSelected%> value="<%=fxValue.getValue()%>"><%=fxValue.getValue()%></option> <%
										}
									}
									%>
								</select> <%
							}
							else{ %>
								<span class="barfont" style="width:400">Unknown display type!</span> <%
							}
						} // end display input			
						%>
					</td>
				</tr>
				<input type="hidden" name="oblig_<%=attrID%>" value="<%=attribute.getObligation()%>"/>
				<%
			}
			%>
			
			<!-- COMPLEX ATTRIBUTES table -->
			<%
			if (mode.equals("view") && complexAttrs.size()> 0){
				%>
				<tr><td>&#160;</td><td>&#160;</td></tr>
				<%
				for (int i=0; i<complexAttrs.size(); i++){
	
					DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
					attrID = attr.getID();
					String attrName = attr.getShortName();   
					Vector attrFields = searchEngine.getAttrFields(attrID);
		
					%>		
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=COMPLEX&mode=view">
							<span class="help">?</span></a>&#160;
							<span class="mainfont"><b><%=attrName%></b></span>
						</td>
						<td>
							<table width="auto" cellspacing="0">
								<tr>
								<%
	
								for (int t=0; t<attrFields.size(); t++){
									Hashtable hash = (Hashtable)attrFields.get(t);
									String name = (String)hash.get("name");
									%>
									<th><%=name%></th>
									<%
								}
								%>
								</tr>
								<%

								Vector rows = attr.getRows();
								for (int j=0; rows!=null && j<rows.size(); j++){
									Hashtable rowHash = (Hashtable)rows.get(j);
									%>
									<tr>
									<%
							
									for (int t=0; t<attrFields.size(); t++){
										Hashtable hash = (Hashtable)attrFields.get(t);
										String fieldID = (String)hash.get("id");
										String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
										if (fieldValue == null) fieldValue = " ";
										%>
										<td style="padding-right:10" <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<span class="barfont"><%=fieldValue%>
										</td>
										<%
									}	
									%>
									</tr>				
								<%
								}%>
							</table>
						</td>
					</tr>
				<%
				}
			}
			%>

			<%
		
			if (!mode.equals("add") && !mode.equals("view")
					|| (mode.equals("view") && user!=null)){ // if mode is not 'add'
			%>
			
			<tr height="5"><td colspan="2"></td></tr>
			<tr>
				<td>&#160;</td>
				<td>
					<b>*</b> <span class="smallfont"><a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=ds_id%>&#38;parent_type=DS&#38;parent_name=<%=ds_name%>&#38;ds=true')">
						<b>COMPLEX ATTRIBUTES</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						&lt;&#160;click here to view/edit complex attributes specified for this data element
					</span>
				</td>
			</tr>
			<%
			}
			%>
		<!-- DATASET TABLES table -->
		<% if (!mode.equals("add")){ // if CH1 and mode=add
			boolean bShowLink=false;
			if (mode.equals("view")){

			
				if (tables.size()>0){
					DElemAttribute attr = null;
	
					%>
					<tr height="5"><td colspan="2"></td></tr>
					<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<span class="mainfont"><b>Dataset tables</b></span>
						</td>
						<td>
							<table width="auto" cellspacing="0">
								<tr>
									<th align="left" style="padding-left:5;padding-right:10">Short name</th>
									<th align="left" style="padding-right:10">Full name</th>
									<th align="left" style="padding-right:10">Type</th>
								</tr>
								<%
								for (int i=0; tables!=null && i<tables.size(); i++){
			
									DsTable table = (DsTable)tables.get(i);
									String tableLink = "dstable.jsp?mode=view&table_id=" + table.getID() + "&ds_id=" + ds_id + "&ds_name=" + ds_name + "&ctx=ds";
			
									String tblName = "";
		
									attributes = searchEngine.getAttributes(table.getID(), "T", DElemAttribute.TYPE_SIMPLE);
		
									for (int c=0; c<attributes.size(); c++){
										attr = (DElemAttribute)attributes.get(c);
       									if (attr.getName().equalsIgnoreCase("Name"))
       										tblName = attr.getValue();
									}

									String tblFullName = tblName;
									tblName = tblName.length()>40 && tblName != null ? tblName.substring(0,40) + " ..." : tblName;

									%>
									<tr>
										<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<a href="<%=tableLink%>"><%=table.getShortName()%></a>
										</td>
										<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=tblFullName%>">
											<span class="barfont"><%=tblName%></span>
										</td>
										<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<span class="barfont"><%=table.getType()%></span>
										</td>
									</tr>
								<%
								}
								%>
							</table>
						</td>
					</tr>
				<% } %>
			<% } %>
			<tr height="5"><td colspan="3"></td></tr>
			<%			
			if (!mode.equals("view")
					|| (mode.equals("view") && user!=null)){ // if mode is not 'add'
			%>
				<tr>
					<td>&#160;</td>
					<td colspan="2">
						<b>*</b> <span class="smallfont"><a href="dstables.jsp?ds_id=<%=ds_id%>&#38;ds_name=<%=ds_name%>">
							<b>TABLES</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to specify/remove tables of this dataset
						</span>
					</td>
				</tr>
			<%
		}
		} // if mode is not 'add'
		
		if (!mode.equals("view")){
			%>
			
			<tr height="15"><td colspan="3"></td></tr>
			
			<tr>
				<td>&#160;</td>
				<td colspan="2">
				
					<% 
					
					if (mode.equals("add")){ // if mode is "add"
						if (user==null){ %>									
							<input class="mediumbuttonb" type="button" value="Add" disabled="true"/>&#160;
						<%} else {%>
							<input class="mediumbuttonb" type="button" value="Add" onclick="submitForm('add')"/>&#160;
						<% }
					} // end if mode is "add"
					
					if (!mode.equals("add")){ // if mode is not "add"
						if (user==null){ %>									
							<input class="mediumbuttonb" type="button" value="Save" disabled="true"/>&#160;
							<input class="mediumbuttonb" type="button" value="Delete" disabled="true"/>&#160;
						<%} else {%>
							<input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&#160;
							<input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&#160;
						<% }
					} // end if mode is not "add"
					
					%>
					
				</td>
			</tr>
			<%
		}
		
		//if (!mode.equals("add")){ // if mode is not "add"
		if (false){ // if mode is not "add"
			%>
			<tr height="20"><td colspan="3"></td></tr>
			<tr>
				<td colspan="3">
					<a href="javascript:alert('Under repairement!')">Printable page</a>
					<!--a href="javascript:printable('dataset_print.jsp?ds_id=<%=ds_id%>&open=true')">
						Printable page
					  </a-->
				</td>
			</tr>
			<%
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