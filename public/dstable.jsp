<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<!--%!private final static String USER_SESSION_ATTRIBUTE="DataDictionaryUser";%-->
<%!ServletContext ctx=null;%>
<%!private Vector mAttributes=null;%>
<%!private Vector attributes=null;%>
<%!private Vector elems=null;%>
<%!private String mode=null;%>

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
private String getAttributeIdByName(String name){
	

		for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}
private String getAttributeValue(DataElement elem, String name){
	
	String id = getAttributeIdByName(name);
	if (elem == null) return null;
	DElemAttribute attr = elem.getAttributeById(id);
	if (attr == null) return null;
	return attr.getValue();
}

%>

<%

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

// check if the user is authorized
DDuser user = getUser(request);
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

String disabled = user == null ? "disabled" : "";

mode = request.getParameter("mode");
if (mode == null || mode.length()==0) { %>
	<b>Mode paramater is missing!</b>
	<%
	return;
}

String tableID = request.getParameter("table_id");
if (!mode.equals("add") && (tableID == null || tableID.length()==0)){ %>
	<b>Table ID is missing!</b> <%
	return;
}

String contextParam = request.getParameter("ctx");
if (contextParam == null) contextParam = "";

String dsID = request.getParameter("ds_id");
if (!mode.equals("add") && (dsID == null || dsID.length()==0)){
	%>
	<b>Dataset ID is missing!</b>
	<%
	return;
}

String dsName = request.getParameter("ds_name");
if (!mode.equals("add") && (dsName == null || dsName.length()==0)){
	%>
	<b>Dataset short name is missing!</b>
	<%
	return;
}

ctx = getServletContext();

//handle the POST
if (request.getMethod().equals("POST")){
	
	DsTableHandler handler = new DsTableHandler(user.getConnection(), request, ctx);
	
	try{
		handler.execute();
	}
	catch (Exception e){ %>
		<html><body><b><%=e.toString()%></b></body></html> <%
		return;
	}
	
	String redirUrl = request.getContextPath();
				
	if (mode.equals("add")){
		String id = handler.getLastInsertID();
		if (id != null && id.length()!=0)
			redirUrl = redirUrl +
							"/dstable.jsp?mode=edit&table_id=" + id +
							 "&ctx=" + contextParam;
			if (dsName!=null) redirUrl = redirUrl + "&ds_name=" + dsName;
			if (dsID!=null) redirUrl = redirUrl + "&ds_id=" + dsID;
	}
	else if (mode.equals("edit")){
		redirUrl = redirUrl + "/dstable.jsp?mode=edit&table_id=" + tableID + "&ds_name=" + dsName + "&ds_id=" + dsID + "&ctx=" + contextParam;
	}
	else if (mode.equals("delete")){
		%>
		<html><script>window.history.go(-1)</script></html>
		<%
	}
	
	response.sendRedirect(redirUrl);
	return;
}


//handle the GET

String appName = ctx.getInitParameter("application-name");

Connection conn = DBPool.getPool(appName).getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

DsTable dsTable = searchEngine.getDatasetTable(tableID);
if (!mode.equals("add") && dsTable == null){ %>
	<b>Table was not found!</b> <%
	return;
}

mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);

attributes = searchEngine.getAttributes(tableID, "T", DElemAttribute.TYPE_SIMPLE);

elems = searchEngine.getDataElements(null, null, null, null, tableID);
			
DElemAttribute attribute = null;
String attrID = null;
String attrValue = null;

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
				var b = confirm("This table will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations(mode)){
					alert("You have not specified one of the mandatory fields!");
					return;
				}
				
				if (hasWhiteSpace("short_name")){
					alert("Short name cannot contain any white space!");
					return;
				}
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			
			var oDsId = document.forms["form1"].ds_id;
			if (mode=="add" && oDsId!=null){
				document.forms["form1"].ds_name.value = oDsId.options[oDsId.selectedIndex].text;
			}
			
			document.forms["form1"].submit();
		}
		
		function checkObligations(mode){
			
			var oShortName = document.forms["form1"].short_name;
			var shortName = oShortName==null ? null : oShortName.value;

			if (shortName == null || shortName.length==0) return false;
			
			var oDsId = document.forms["form1"].ds_id;
			if (mode=="add" && (oDsId == null || oDsId.value==null || oDsId.value.length==0))
				return false;
			
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
				document.location.assign("dstable.jsp?mode=edit&table_id=" + id + "&ds_id=<%=dsID%>&ds_name=<%=dsName%>");
			}
		}
		
		function openElements(uri){
			//uri = uri + "&open=true";
			wElems = window.open(uri,"TableElements","height=500,width=750,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wElems.focus()}
		}

</script>

<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0">
<%@ include file="header.htm" %>

<script language="JAVASCRIPT" for="window" event="onload">    
			<%
			String type = "";
			if (!mode.equals("add")){
				
				type = dsTable.getType();
				if (type != null && !mode.equals("view")){
    				%>
					var type = '<%=type%>';
					var o = document.forms["form1"].type;
					for (i=0; o!=null && i<o.options.length; i++){
						if (o.options[i].value == type){
							o.selectedIndex = i;
							break;
						}
					}
					<%
				}
			}
			%>
	</script>
<table border="0">
    <tr valign="top">
		    <td nowrap="true" width="125">
		        <p><center>
		            <%@ include file="menu.jsp" %>
		        </center></P>
		    </td>
        <TD>      
	        <jsp:include page="location.jsp" flush='true'>
	            <jsp:param name="name" value="Dataset table"/>
	        </jsp:include>
	        
	        <div style="margin-left:30">
	
<form name="form1" method="POST" action="dstable.jsp">
	
	<table width="500" cellspacing="0">
	
		<%
		//if (contextParam.equals("ds")){
		if (false){ %>
			<tr>
				<td><a href="dstables.jsp?ds_id=<%=dsID%>&ds_name=<%=dsName%>">< back to dataset tables</a></td>
			</tr>
		<%
		}
		%>
		
		<tr height="10"><td></td></tr>
		
		<tr>
			<%
			if (mode.equals("add")){ %>
				<td colspan="2">
					<span class="head00">Add a new table</span>
					<%
					if (dsID != null && dsID.length()!=0){ %>
						<span class="head00">to</span>
						<a href="dataset.jsp?ds_id=<%=dsID%>&mode=view"><span class="title2"><%=dsName%></span></a>
						<span class="head00">dataset</span> <%
					}
					%>
				</td> <%
			}
			else if (mode.equals("edit")){ %>
				<td colspan="2">
					<span class="head00">Edit</span>
					<span class="title2"><%=dsTable.getShortName()%></span>
					<span class="head00">table in</span>
					<a href="dataset.jsp?ds_id=<%=dsID%>&mode=view"><span class="title2"><%=dsName%></span></a>
					<span class="head00">dataset</span>
				</td> <%
			}
			else{ %>
				<td>
					<span class="head00">View</span>
					<span class="title2"><%=dsTable.getShortName()%></span>
					<span class="head00">table in</span>
					<a href="dataset.jsp?ds_id=<%=dsID%>&mode=view"><span class="title2"><%=dsName%></span></a>
					<span class="head00">dataset</span>
				</td>
				<td align="right">
					<%
					if (user!=null){ %>
						<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=tableID%>')"/> <%
					}
					else{
						%>&#160;
<!--						<input type="button" class="smallbutton" value="Edit" disabled/ -->
					<%
					}
					%>
				</td> <%
			}
			%>
		</tr>
		
		<tr height="5"><td colspan="2"></td></tr>
		
		<tr><td <td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
		
	</table>

	<%
	int displayed = 0;
	%>
	
	<table width="auto" cellspacing="0">
		
		<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<td align="right" style="padding-right:10">
				<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
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
					<font class="title2" color="#006666"><%=dsTable.getShortName()%></font>
					<input type="hidden" name="short_name" value="<%=dsTable.getShortName()%>"/>
				<% } else{ %>
					<input <%=disabled%> type="text" class="smalltext" size="30" name="short_name"></input>
				<% } %>
			</td>
		</tr>
		
		<%
		if (mode.equals("add")){ %>
			<tr valign="top">
				<td align="right" style="padding-right:10">
					<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Dataset</b>&#160;(M)</span>
				</td>
				<td>
					<select name="ds_id" class="small" <%=disabled%>>
						<option value="">-- select a dataset --</option>
						<%
						Vector datasets = searchEngine.getDatasets();
						for (int i=0; datasets!=null && i<datasets.size(); i++){
							Dataset ds = (Dataset)datasets.get(i); %>
							<option value="<%=ds.getID()%>"><%=ds.getShortName()%></option> <%
						}
						%>
					</select>
				</td>
			</tr> <%
		}
		%>
		
		<!--tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<td align="right" style="padding-right:10">
				<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
				<span class="mainfont"><b>Full name</b>
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
				if(!mode.equals("add")){
					String name = (dsTable.getName() == null) ? "" : dsTable.getName();
					if(mode.equals("edit")){ %>
						<input <%=disabled%> type="text" class="smalltext" size="30" name="full_name" value="<%=name%>"></input> <%
					}else{ %>
						<span class="barfont" style="width:400"><%=name%></span> <%
					}
				} else{ %>
					<input <%=disabled%> type="text" class="smalltext" size="30" name="full_name"></input> <%
				}
				%>
			</td>
		</tr>
		
		<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<td align="right" style="padding-right:10">
				<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
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
				<% if (!mode.equals("add")){
					String definition = (dsTable.getDefinition() == null) ? "" : dsTable.getDefinition();
					if(mode.equals("edit")){ %>
						<textarea <%=disabled%> class="small" rows="3" cols="42" name="definition"><%=definition%></textarea> <%
					}else{ %>
						<span class="barfont" style="width:400"><%=definition%></span> <%
					}
				}else{ %>
						<textarea <%=disabled%> class="small" rows="3" cols="42" name="definition"></textarea>
				<% } %>
			</td>
		</tr-->
		
		<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<td align="right" style="padding-right:10">
				<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
				<span class="mainfont"><b>Type</b>
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
				if (mode.equals("view")){
					String dispType = "Not specified";
					if (type!=null && type.equalsIgnoreCase("normal"))
						dispType = "Normal";
					else if (type!=null && type.equalsIgnoreCase("lookup"))
						dispType = "Lookup";
					%>
					<span class="barfont" style="width:400"><%=dispType%></span> <%
				}
				else { %>
					<select name="type" class="small" <%=disabled%>>
						<option selected="true" value="normal">Normal</option>
						<option value="lookup">Lookup</option>
					</select> <%
				}
				%>
			</td>
		</tr>
			<%

			// dynamical display of attributes
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				
				String attrOblig = attribute.getObligation();
				
				if (!attribute.displayFor("TBL")) continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				
				if (mode.equals("view") && (attrValue==null || attrValue.length()==0))
					continue;
				
				displayed++;
				
				String attrNs = attribute.getNamespace().getShortName();
				
				String width  = attribute.getDisplayWidth();
				String height = attribute.getDisplayHeight();
				
				%>
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=edit"><span class="help">?</span></a>&#160;
						<span class="mainfont"><!--%=attrNs%>:--><b><%=attribute.getShortName()%></b>
							<%
							//displayed++;
							if (!mode.equals("view")){
								%>
								&#160;(<%=attribute.getObligation()%>)
								<%
							}
							%>
						</span>
					</td>
					<td>
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
									<input <%=disabled%> type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>"/>
									<%
								}
								else{
									%>
									<input <%=disabled%> type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>"/>
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
						}
						%>
					</td>
				</tr>
				<input type="hidden" name="oblig_<%=attrID%>" value="<%=attribute.getObligation()%>"/>
				<%
			}
			%>
		
		<!-- elements list goes here, if user is in view mode  -->
		<%
		if (mode.equals("view")){ %>
			<tr><td>&#160;</td><td>&#160;</td></tr>
			<tr valign="top">
				<td align="right" style="padding-right:10">
					<span class="mainfont"><b>Elements</b></span>
				</td>
				<td>
					<%
					if (elems != null && elems.size()>0){
						%>
						
						<table width="auto" cellspacing="0">
							<tr>
								<th align="left" style="padding-left:5;padding-right:10">Short name</th>
								<th align="left" style="padding-right:10">Datatype</th>
								<th align="left" style="padding-right:10">MaxSize</th>
								<th align="left" style="padding-right:10">Type</th>
							</tr>
			
							<%
			
							Hashtable types = new Hashtable();
							types.put("AGG", "Aggregate");
							types.put("CH1", "Codes");
							types.put("CH2", "Quantitative");
					
							for (int i=0; elems!=null && i<elems.size(); i++){
					
								DataElement elem = (DataElement)elems.get(i);
								String elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID() + "&ds_id=" + dsID + "&table_id=" + tableID + "&ctx=" + contextParam;
					
								String elemType = (String)types.get(elem.getType());
				
								String datatype = getAttributeValue(elem, "Datatype");		
								if (datatype == null) datatype="";
			
								String max_size = getAttributeValue(elem, "MaxSize");		
								if (max_size == null) max_size="";
	
								%>
								<tr>
									<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
										<a href="<%=elemLink%>"><%=elem.getShortName()%></a>
									</td>
									<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
										<span class="barfont"><%=datatype%></span>
									</td>
									<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
										<span class="barfont"><%=max_size%></span>
									</td>
									<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
										<% if (elem.getType().equals("CH1")){ %>
											<span class="barfont"><a href="fixed_values.jsp?delem_id=<%=elem.getID()%>&#38;delem_name=<%=elem.getShortName()%>"><%=elemType%></a></span>
										<%} else{ %>
											<span class="barfont"><%=elemType%></span>
										<% } %>
									</td>
								</tr>
							<%
							}
							%>
							</table>
						<%
					}
					else {
					%>
						<span class="barfont" style="width:400">There is no elements in the table</span>
					<% } %>
					</td>
				</tr>
			<%
			}
			%>
			
			<%		
			if ((!mode.equals("add") && !mode.equals("view")) 
					|| (mode.equals("view") && user!=null)){ // if mode is not 'add'
				%>
				<tr height="5"><td colspan="2"></td></tr>
				<tr>
					<td>&#160;</td>
					<td colspan="2">
						<b>*</b> <span class="smallfont">
							<% 
							String elemLink; 
							if (contextParam.equals("ds"))
								elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=" + contextParam;
							else
								elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=dstbl";
							%>
							<a href="<%=elemLink%>">
							<b>ELEMENTS</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to specify/remove elements of this table
						</span>
					</td>
				</tr>
				<%
			} // if mode is not 'add'
		
			if (!mode.equals("view")){
			%>
			<tr height="10"><td colspan="2"></td></tr>
			
			<tr>
				<td>&#160;</td>
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
			</tr>
			<%
		}
		%>

	</table>

<input type="hidden" name="mode" value="<%=mode%>"/>
	
<%
if (!mode.equals("add")){ %>
	<input type="hidden" name="table_id" value="<%=tableID%>"/>
	<input type="hidden" name="del_id" value="<%=tableID%>"/>
	<input type="hidden" name="ds_id" value="<%=dsID%>"/>
	<input type="hidden" name="ds_name" value="<%=dsName%>"/> <%
}
else{ %>
	<input type="hidden" name="ds_name"/> <%
}
%>

<input type="hidden" name="ctx" value="<%=contextParam%>"/>
	
</form>
</div>
</TD></TR></table>
</body>
</html>