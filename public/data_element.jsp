<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static String oConnName="datadict";%>
<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private DataElement dataElement=null;%>
<%!private Vector namespaces=null;%>
<%!private Vector complexAttrs=null;%>
<%!private Vector fixedValues=null;%>
<%!private Vector fxvAttributes=null;%>

<%!

private DElemAttribute getAttributeByName(String name){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr;
	}
        
    return null;
}

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
	DElemAttribute attr = dataElement.getAttributeById(id);
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
			
			String delem_id = request.getParameter("delem_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (!mode.equals("add")&& (delem_id == null || delem_id.length()==0)){ %>
				<b>Data element ID is missing!</b> <%
				return;
			}
			
			String type = request.getParameter("type");
			if (type!=null && type.length()==0)
				type = null;
			
			String contextParam = request.getParameter("ctx");
			if (contextParam == null) contextParam = "";
			
			String dsID = request.getParameter("ds_id");
			String tableID = request.getParameter("table_id");
			
			String s = request.getParameter("pick");
			boolean wasPick = (s==null || !s.equals("true")) ? false : true;
			
			if (wasPick)
				tableID = null;
			
			if (request.getMethod().equals("POST")){
				
				DataElementHandler handler = null;
				
				if (!wasPick){
				
					handler = new DataElementHandler(user.getConnection(), request, ctx);
					
					try {
						handler.execute();
					}
					catch (Exception e){
						%>
						<html><body><b><%=e.toString()%></b></body></html>
						<%
						return;
					}
				}
				
				String redirUrl = request.getContextPath();
				
				if (mode.equals("add") && !wasPick){
					String id = handler.getLastInsertID();
					if (id != null && id.length()!=0)
						redirUrl = redirUrl + "/data_element.jsp?mode=edit&delem_id=" + id;
				}
				else if (mode.equals("edit") && !wasPick){
					redirUrl = redirUrl + "/data_element.jsp?mode=edit&delem_id=" + delem_id;
		/*+EK I think it should be here */
					if (delem_id!=null) redirUrl = redirUrl + "&delem_id=" + delem_id;
					if (type!=null) redirUrl = redirUrl + "&type=" + type;
					
					if (dsID != null) redirUrl = redirUrl + "&ds_id=" + dsID;
					if (tableID != null) redirUrl = redirUrl + "&table_id=" + tableID;
					if (contextParam != null) redirUrl = redirUrl + "&ctx=" + contextParam;
		/*- EK*/
				}
				else if (mode.equals("delete") && !wasPick){
					%>
					<html><script>window.history.go(-1)</script></html>
					<%
				}
				else if (wasPick){
					redirUrl = redirUrl + "/data_element.jsp?&mode=" + mode;
					
					if (delem_id!=null) redirUrl = redirUrl + "&delem_id=" + delem_id;
					if (type!=null) redirUrl = redirUrl + "&type=" + type;
					
					if (dsID != null) redirUrl = redirUrl + "&ds_id=" + dsID;
					if (tableID != null) redirUrl = redirUrl + "&table_id=" + tableID;
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			String delem_name = "";
			
			Namespace namespace = null;
			
			if (mode.equals("edit") || mode.equals("view")){
				dataElement = searchEngine.getDataElement(delem_id);
				if (dataElement!=null){
					type = dataElement.getType();
					//delem_name = dataElement.getAttributeValueByName("Name");
					delem_name = dataElement.getShortName();
					if (delem_name == null) delem_name = "unknown";
					if (delem_name.length() == 0) delem_name = "empty";
					namespace = dataElement.getNamespace();
				}
				else{ %>
					<b>Data element was not found!</b> <%
					return;
				}
			}
			else{
				if (mAttributes.size()==0){ %>
					<b>No metadata on attributes found! Nothing to add.</b> <%
					return;
				}
				
				/*if (type == null || type.length()==0) { %>
					<b>Type paramater is missing!</b>
					<%
					return;
				}
				
				if (type.equals("AGG") && contextParam.equals("ds")) { %>
					<b>Aggregate data elements are not allowed in datasets and their tables!</b>
					<%
					return;
				}*/
			}
			
			namespaces = searchEngine.getNamespaces();
			if (namespaces == null) namespaces = new Vector();
			
			DElemAttribute attribute = null;
			
			if (!contextParam.startsWith("ds") && dsID==null && tableID==null && dataElement!=null)
				tableID = dataElement.getTableID();
			
			Dataset dataset = null;
			DsTable dsTable = null;
			
			if (tableID != null && tableID.length()!=0){
				dsTable = searchEngine.getDatasetTable(tableID);
				if (dsTable != null)
					dsID = dsTable.getDatasetID();
			}
			
			if (dsID != null && dsID.length()!=0){
				dataset = searchEngine.getDataset(dsID);
			}
			
			if (contextParam.startsWith("ds") && (dataset==null || dsTable==null)){ %>
				<b>Dataset and table were not found!</b> <%
				return;
			}
			complexAttrs = searchEngine.getComplexAttributes(delem_id, "E");
			
			if (complexAttrs == null) complexAttrs = new Vector();
			
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

		function submitPick(){
			document.forms["form1"].elements["pick"].value = "true";
			document.forms["form1"].submit();
		}
		
		function submitForm(mode){
			
			if (mode == "delete"){
				var b = confirm("This data element will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return;
				}
				
				if (hasWhiteSpace("delem_name")){
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
		
		function subelems(uri){
			uri = uri + "&open=true";
			wSubElems = window.open(uri,"SubElements","height=700,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wSubElems.focus()}
		}

		function ch1values(url){
					wCh1Values = window.open(url,"AllowableValues","height=600,width=800,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
					if (window.focus) {wCh1Values.focus()}
		}
		
		function complexAttrs(url){
					wComplexAttrs = window.open(url,"ComplexAttributes","height=500,width=500,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
					if (window.focus) {wComplexAttrs.focus()}
		}
		
		function printable(url){
					wPrintablePage = window.open(url + "&open=true","PrintablePage","height=600,width=700,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=no");
					if (window.focus) {wPrintablePage.focus()}
		}
		
		function openDatasetPick(){
	    	wPick = window.open("dspick.jsp","PickDataset",'height=400,width=400,status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes');
    	}
    	
    	function setPickedDataset(dsName, dsID){
	    	document.forms["form1"].ds_name.value=dsName;
	    	document.forms["form1"].ds_id.value=dsID;
    	}
    	
    	function edit(){
	    	<%
	    	String modeString = new String("mode=view&");
	    	String qryStr = request.getQueryString();
	    	int modeStart = qryStr.indexOf(modeString);
	    	if (modeStart == -1){
		    	modeString = new String("mode=view");
		    	modeStart = qryStr.indexOf(modeString);
	    	}
	    	
	    	if (modeStart != -1){
		    	StringBuffer buf = new StringBuffer(qryStr.substring(0, modeStart));
		    	buf.append("mode=edit&");
		    	buf.append(qryStr.substring(modeStart + modeString.length()));
		    	%>
				document.location.assign("data_element.jsp?<%=buf.toString()%>");
				<%
			}
			%>
		}
		
		function fixType(){
			var type = document.forms["form1"].typeSelect.value;
			if (type == null || type.length==0)
				return;
			document.location.assign("data_element.jsp?mode=add&type=" + type);
		}
		
		function onLoad(){
		}
		
	</script>
</head>

<%
			
String attrID = null;
String attrValue = null;
%>
			
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
		            <jsp:param name="name" value="Data Element"/>
		        </jsp:include>
		        
		        <div style="margin-left:30">
		        
			<form name="form1" id="form1" method="POST" action="data_element.jsp">
			
			<% if (!mode.equals("add")){ %>
				<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
			<% } else { %>
				<input type="hidden" name="dummy"/>
			<% } %>
			
			<table width="500" cellspacing="0">
				<tr>
					<%
					if (mode.equals("add")){ %>
						<td colspan="2"><span class="head00">Add a data element</span></td> <%
					}
					else if (mode.equals("edit")){ %>
						<td colspan="2"><span class="head00">Edit data element</span></td> <%
					}
					else{ %>
						<td><span class="head00">View data element</span></td>
						<td align="right">
							<%
							if (user!=null){ %>
								<input type="button" class="smallbutton" value="Edit" onclick="edit()"/> <%
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
						(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.<br/>
						Note that for aggregate data elements you cannot specify a dataset and table.
						<%
						if (type==null){ %>
							<br/><br/><b>NB! Please select the element type first. Otherwise your entries will be lost!</b> <%
						}
						%>
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
					<a href="javascript:openType()"><span class="help">?</span></a>&#160;
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
				<td colspan="2">
					<%
					if (mode.equals("add") && (type==null || type.length()==0)){ %>
						<select class="small" name="typeSelect" onchange="fixType()">
							<option value="">-- Select element type --</option>
							<option value="AGG">Aggregate data element</option>
							<option value="CH1">Data element with fixed values</option>
							<option value="CH2">Data element with quantitative values</option>
						</select> <%
					}
					else{
						if(type.equals("AGG")){ %>
							<b>AGGREGATE DATA ELEMENT</b>
						<% }else if (type.equals("CH1")){ %>
							<b>DATA ELEMENT WITH FIXED VALUES</b>
						<% }else if (type.equals("CH2")){ %>					
							<b>DATA ELEMENT WITH QUANTITATIVE VALUES</b>
						<% } else{ %>
							<b>AGGREGATE DATA ELEMENT</b> <%
						}
					}
					%>
				</td>
			</tr>
			
			<tr height="10"><td colspan="3"></td></tr>
			
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
						<font class="title2" color="#006666"><%=delem_name%></font>
						<input type="hidden" name="delem_name" value="<%=delem_name%>"/>
					<% } else{ %>
						<input class="smalltext" class="smalltext" type="text" size="30" name="delem_name"></input>
					<% } %>
				</td>
			</tr>
			
			
			<%
			if (type!=null && !type.equals("AGG") && (mode.equals("add") || dataset!=null)){ %>
			
				<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Dataset</b>
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
					<td colspan="2">
						<%
						if (contextParam.startsWith("ds") || !mode.equals("add")){
								%>
								<font class="title2" color="#006666"><%=dataset.getShortName()%></font>
								<input type="hidden" name="ds_id" value="<%=dataset.getID()%>"/>
								<%
						}
						else{ %>
							<select class="small" name="ds_id" onchange="submitPick('<%%>')">
								<%
								if (dsID==null || dataset==null){ %>
									<option selected value="">-- select a dataset --</option>
									<%
								}
								else{ %>
									<option value="">-- no particular dataset --</option>
									<%
								}
								
								Vector datasets = searchEngine.getDatasets();
								for (int i=0; datasets!=null && i<datasets.size(); i++){
									Dataset ds = (Dataset)datasets.get(i);
									String selected = (dsID!=null && dataset!=null && dsID.equals(ds.getID())) ? "selected" : "";
									%>
									<option <%=selected%> value="<%=ds.getID()%>"><%=ds.getShortName()%></option>
									<%
								}
								
								%>
							</select>
							<%
						}
						%>
					</td>
				</tr>
				<%
			}
			%>
			
			<%
			if (type!=null && (!type.equals("AGG") && (mode.equals("add") || dataset!=null))){
				
				%>
				<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a href="javascript:alert('Under construction!')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Table</b>
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
					<td colspan="2">
						<%
						if (contextParam.startsWith("ds")  || !mode.equals("add")){
								%>
								<font class="title2" color="#006666"><%=dsTable.getShortName()%></font>
								<input type="hidden" name="table_id" value="<%=dsTable.getID()%>"/>
								<%
						}
						else{ %>
							<select class="small" name="table_id">
								<%
								if (dataset!=null){
									if (tableID==null || dsTable==null){ %>
										<option selected value="">-- select a table --</option>
										<%
									}
									
									Vector tables = searchEngine.getDatasetTables(dataset.getID());
									for (int i=0; tables!=null && i<tables.size(); i++){
										DsTable tbl = (DsTable)tables.get(i);
										String selected = (tableID!=null && dsTable!=null && tableID.equals(tbl.getID())) ? "selected" : "";
										%>
										<option <%=selected%> value="<%=tbl.getID()%>"><%=tbl.getShortName()%></option>
										<%
									}
								}
								else{ %>
									<option value="">-- pick a dataset first! --</option>
									<%
								}
								%>
							</select>
							<%
						}
						%>
					</td>
				</tr>
				<%
			}
			%>
			
			<%
			
			if (type!=null && type.equals("AGG")){
				
				if (mode.equals("add")){
					
					String disabled = user == null ? "disabled" : "";
				
					%>
					<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<td align="right" style="padding-right:10">
							<a href="javascript:openExtends()"><span class="help">?</span></a>&#160;
							<span class="mainfont"><b>Extends</b>
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
						<td colspan="2">					
							<select class="small" name="extends" <%=disabled%> >
								<option value="">-- none --</option>
								<%
								
								if (user != null){
									
									Vector dataElements = searchEngine.getDataElements();
									String thisExtension = dataElement == null ? null : dataElement.getExtension();
								
									for (int i=0; dataElements!=null && i<dataElements.size(); i++){
										
										DataElement elem = (DataElement)dataElements.get(i);
										
										if (!elem.getType().equals("AGG"))
											continue;
										
										Namespace elemNs = elem.getNamespace();
										String elemID = elem.getID();										
										
										if (thisExtension != null && thisExtension.equals(elemID)){
											%>
											<option selected value="<%=elemID%>"><%=elemNs.getShortName()%>:<%=elem.getShortName()%></option>
											<%
										}
										else{
											%>								
											<option value="<%=elemID%>"><%=elemNs.getShortName()%>:<%=elem.getShortName()%></option>
											<%
										}
									}
								}
								
								%>
							</select>
						</td>
					</tr>
					<%
				}
				else{
					String extensionID = dataElement.getExtension();
					if (extensionID != null){
						
						DataElement extElem = searchEngine.getDataElement(extensionID);
						String dispExtension = extElem.getNamespace().getShortName() + ":" + extElem.getShortName();
						
						%>
						<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
							<td align="right" style="padding-right:10">
								<a href="javascript:openExtends()"><span class="help">?</span></a>&#160;
								<span class="mainfont"><b>Extends</b>
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
							<td colspan="2">
								<font class="head0" color="#006666"><%=dispExtension%></font>
							</td>
						</tr>
						<%
					}
				}
			}
			
			Vector classes = searchEngine.getDataClasses();
			if (classes != null && classes.size()!=0){
				
				String dataClassID = null;
				if (!mode.equals("add")) dataClassID = dataElement.getDataClass();
				
				if (dataClassID==null)
					ctx.log("dataClassID=null");
				else
					ctx.log("dataClassID=" + dataClassID);
				
				String disabled = user == null ? "disabled" : "";
			}
			
			// dynamical display of attributes, really cool... I hope...
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null)
					continue;
				
				boolean dispFor = type==null ? attribute.displayFor("AGG") : attribute.displayFor(type);
				
				if (!dispFor)
					continue;
				
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
						<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=view">
						<span class="help">?</span></a>&#160;
						<span class="mainfont"><b><%=attribute.getShortName()%></b>
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
						else{
						
							if (dispType.equals("text")){
								if (attrValue!=null){
									%>
									<input <%=disabled%> class="smalltext" class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>"/>
									<%
								}
								else{
									%>
									<input <%=disabled%> class="smalltext" class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>"/>
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
					<b>*</b> <span class="smallfont"><a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=delem_id%>&#38;parent_type=E&#38;parent_name=<%=delem_name%>&#38;parent_ns=<%=namespace.getShortName()%>')">
						<b>COMPLEX ATTRIBUTES</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						&lt;&#160;click here to view/edit complex attributes specified for this data element
					</span>
				</td>
			</tr>
			<%
			}
			%>
		
		<!-- ALLOWABLE VALUES table -->
		<% if (type!=null && type.equals("CH1") && !mode.equals("add")){ // if CH1 and mode=add
			boolean bShowLink=false;
			if (mode.equals("view")){
				fxvAttributes = new Vector();

				for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
					attribute = (DElemAttribute)mAttributes.get(i);
					String dispType = attribute.getDisplayType();
					if (dispType != null &&
						attribute.displayFor("FXV")){
					fxvAttributes.add(attribute);
					}
				}

				fixedValues = searchEngine.getFixedValues(delem_id, "elem", false);
				if (fixedValues == null) fixedValues = new Vector();

				if (fixedValues.size()>0){
				%>
					<tr height="5"><td colspan="2"></td></tr>
					<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<span class="mainfont"><b>Allowable values</b></span>
						</td>
						<td>
							<table width="auto" cellspacing="0">
								<tr>
									<th width="100">Value</th>
									<%
										for (int i=0; fxvAttributes!=null && i<fxvAttributes.size(); i++){
											
											attribute = (DElemAttribute)fxvAttributes.get(i);
											%>
											<th width="150"><%=attribute.getShortName()%></th>
											<%
										}
									%>
								</tr>
	
								<%
								String mode= (user == null) ? "print" : "edit";

								for (int i=0; i<fixedValues.size(); i++){
									if (i==20){	// it's possible to see only the first 20 values on element page
										%>
										<tr><td colspan="<%=fxvAttributes.size() + 1 %>">
											<span class="barfont">... &#160; to view the whole list of allowable values, click the link below</span>
										</td></tr>
										<%
										if (user == null) bShowLink=true;
										break;
									}
									else{
										FixedValue fxv = (FixedValue)fixedValues.get(i);
										String value = fxv.getValue();
										String fxvID = fxv.getID();
										String fxvAttrValue = null;
										String fxvAttrValueShort = null;
										
										%>
										<tr>
											<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
												<b><a href="fixed_value.jsp?fxv_id=<%=fxvID%>&#38;mode=<%=mode%>&delem_id=<%=delem_id%>&delem_name=<%=delem_name%>&parent_type=elem">
													<%=value%>
												</a></b>
											</td>
											<%
											for (int c=0; fxvAttributes!=null && c<fxvAttributes.size(); c++){
						
												attribute = (DElemAttribute)fxvAttributes.get(c);
						
												fxvAttrValue = fxv.getAttributeValueByID(attribute.getID());
												if (fxvAttrValue==null || fxvAttrValue.length()==0){
												%>
													<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>></td>
												<%
												}
												else{
													if (fxvAttrValue.length()>35){
														fxvAttrValueShort = fxvAttrValue.substring(0,35) + " ...";
												}
													else{
														fxvAttrValueShort = fxvAttrValue;
													}
													%>
													<td valign="bottom" align="left" title="<%=fxvAttrValue%>" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
														<span class="barfont"><%=fxvAttrValueShort%></span>
													</td>
													<%
												}
											}
											%>
										</tr>
									<%
									}
									%>
								<%
								}
								%>
							</table>
						</td>
					</tr>
					<%
				}
		 	}  	// end if (mode.equals("view"))
		
			if (!mode.equals("add") && !mode.equals("view")
					|| (mode.equals("view") && user!=null)
					|| bShowLink){ // if mode is not 'add'
			%>
			<tr height="5"><td colspan="2"></td></tr>
			<tr>
				<td>&#160;</td><td>
					<b>*</b> <span class="smallfont"><a href="fixed_values.jsp?delem_id=<%=delem_id%>&#38;delem_name=<%=delem_name%>&#38;ns=<%=namespace.getShortName()%>">
						<b>ALLOWABLE VALUES</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						<% if (user != null) %>
							&lt;&#160;click here to view/add/remove fixed values of this data element
					</span>
				</td>
			</tr>
		
			<% 
			}
		} // end if CH1 and mode=add
		%>
		<%
		if (type!=null && type.equals("AGG") && !mode.equals("add")){ // if AGG and mode is not 'add'
		
			String seqID = dataElement.getSequence();
			String chcID = dataElement.getChoice();
			
			String extID = dataElement.getExtension();
			
			StringBuffer url = new StringBuffer();
			
			if (seqID != null && chcID != null)
				url.append("javascript:alert('A data element is expected to have only a sequence or only a choice of sub-elements, not both!");
			else{
				url.append("javascript:subelems('content.jsp?parent_type=elm");
				url.append("&parent_id=" + delem_id);
				url.append("&parent_name=" + delem_name);
				url.append("&parent_ns=" + namespace.getShortName());
				
				if (extID != null)
					url.append("&ext_id=" + extID);
			}
			
			if (seqID != null){				
				url.append("&content_id=" + seqID);
				url.append("&content_type=seq");
			}
			else if (chcID != null){
				url.append("&content_id=" + chcID);
				url.append("&content_type=chc");
			}
			
			url.append("')");
				
		%>
			
		<tr>
			<td>&#160;</td>
			<td colspan="2">
				<b>*</b> <span class="smallfont"><a href="<%=url.toString()%>">
					<b>SUBELEMENTS</b></a></span>&#160;&#160;
				<span class="smallfont" style="font-weight: normal">
					&lt;&#160;click here to view/add/remove subelements of this aggregate
				</span>
			</td>
		</tr>
			
		<% 
		} // if AGG and mode is not 'add'
		/*
		if (!mode.equals("add")){ //if mode!=add
		%>
		<tr>
			<td>&#160;</td>
			<td colspan="2">
				<b>*</b> <span class="smallfont"><a href="rel_elements.jsp?delem_id=<%=delem_id%>&#38;delem_name=<%=delem_name%>&#38;ns=<%=namespace.getShortName()%>">
					<b>RELATED ELEMENTS</b></a></span>&#160;&#160;
				<span class="smallfont" style="font-weight: normal">
					&lt;&#160;click here to view/add/remove related data elements of this data element
				</span>
			</td>
		</tr>
		<%
		}*/
		if (!mode.equals("view")){
			%>	
			<tr height="10"><td colspan="3"></td></tr>
			<tr>
				<td>&#160;</td>
				<td colspan="2">
				
					<% 
					
					if (mode.equals("add")){ // if mode is "add"
						if (user==null){ %>									
							<input class="mediumbuttonb" type="button" value="Add" disabled="true"/>&#160;&#160;
						<%} else {%>
							<input class="mediumbuttonb" type="button" value="Add" onclick="submitForm('add')"/>&#160;&#160;
						<% }
					} // end if mode is "add"
					
					if (!mode.equals("add")){ // if mode is not "add"
						if (user==null){ %>									
							<input class="mediumbuttonb" type="button" value="Save" disabled="true"/>&#160;&#160;
							<input class="mediumbuttonb" type="button" value="Delete" disabled="true"/>&#160;&#160;
						<%} else {%>
							<input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
							<input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;
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
					<!--a href="javascript:printable('data_element_print.jsp?mode=print&delem_id=<%=delem_id%>&type=<%=type%>')">
						Printable page
					  </a-->
				</td>
			</tr>
			<%
		}
		
		if (type!=null){
			%>
			<input type="hidden" name="type" value="<%=type%>"/>
			<%
		}
		%>
		<input type="hidden" name="mode" value="<%=mode%>"/>
		<input type="hidden" name="ctx" value="<%=contextParam%>"/>
		
		<input type="hidden" name="pick" value="false"/>
	
		<%
		if (!mode.equals("add")){
			%>	
			<input type="hidden" name="ns" value="<%=namespace.getID()%>"/>
			<%
		}
		else{
			%>
			<input type="hidden" name="ns" value="basens"/>
			<%
		}
		%>
		
	</table>
	</form>
</div>
        </TD>
</TR>
</table>
</body>
</html>