<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private FixedValue fxv=null;%>
<%!private Vector relElems=null;%>
<%!private Vector elemValues=null;%>

<%@ include file="history.jsp" %>

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
	DElemAttribute attr = fxv.getAttributeById(id);
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
			
			String fxv_id = request.getParameter("fxv_id");
			
			String delem_id = request.getParameter("delem_id");			
			if (delem_id == null || delem_id.length()==0){ %>
				<b>Data element ID is missing!</b> <%
				return;
			}
			
			String parent_type = request.getParameter("parent_type");
			if (parent_type == null)
				parent_type = "CH1";
			else if (!parent_type.equals("CH1") && !parent_type.equals("CH2") && !parent_type.equals("attr")){ %>
				<b>Unknown parent type!</b> <%
				return;
			}
			
			String valsType = "CH1";
			if (!parent_type.equals("attr")){
				valsType = parent_type;
				parent_type = "elem";
			}
			String typeParam = parent_type.equals("attr") ? "attr" : valsType;
			
			String initCaseTitle = valsType.equals("CH1") ? "Allowable" : "Suggested";
			String lowerCaseTitle = valsType.equals("CH1") ? "allowable" : "suggested";
			String upperCaseTitle = valsType.equals("CH1") ? "ALLOWABLE" : "SUGGESTED";
			
			
			
			String dispParentType = parent_type.equals("elem") ? "element" : "attribute";
			
			String delem_name = request.getParameter("delem_name");
			if (delem_name == null) delem_name = "?";
			
			String ns = request.getParameter("ns");

			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (!mode.equals("add") && (fxv_id == null || fxv_id.length()==0)){ %>
				<b>Value ID is missing!</b> <%
				return;
			}

			
			if (request.getMethod().equals("POST")){
				
				Connection userConn = null;
				String redirUrl = "";
				
				try{
					userConn = user.getConnection();
					FixedValuesHandler handler = new FixedValuesHandler(userConn, request, ctx);
					
					try {
						handler.execute();
					}
					catch (Exception e){
						%>
						<html><body>
							<b><%=e.toString()%></b><br>
							<%
							/*if (fxv_id != null && fxv_id.length()!=0)
								redirUrl = redirUrl + "fixed_value.jsp?mode=edit&fxv_id=" + fxv_id +
																 "&delem_id=" + delem_id +
																 "&delem_name=" + delem_name +
																 "&parent_type=" + parent_type;
																 //"&ns=" + ns;
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
			
				/* mode add is not needed currently
				if (mode.equals("add")){
					String id = handler.getLastInsertID();
					if (id != null && id.length()!=0)
						redirUrl = redirUrl + "fixed_value.jsp?mode=edit&fxv_id=" + id +
															 "&delem_id=" + delem_id +
															 "&delem_name=" + delem_name +
															 "&ns=" + ns;
				}
				*/
				if (mode.equals("edit")){
					redirUrl=currentUrl;
					/*redirUrl = redirUrl + "fixed_value.jsp?mode=edit&fxv_id=" + fxv_id + 
															 "&delem_id=" + delem_id +
															 "&delem_name=" + delem_name +
															 "&parent_type=" + parent_type;
					*/										 //"&ns=" + ns;
				}
				else if (mode.equals("delete")){
					String deleteUrl = history.gotoLastNotMatching("fixed_value.jsp");

					if (deleteUrl!=null&&deleteUrl.length()>0) 
						redirUrl=deleteUrl;
					else 
						redirUrl = redirUrl + "fixed_values.jsp?mode=edit&delem_id=" + delem_id +
															 "&delem_name=" + delem_name +
															 "&parent_type=" + parent_type;
					
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
			
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			String value = "";
					
			if (!mode.equals("add")){
				fxv = searchEngine.getFixedValue(fxv_id);
				if (fxv!=null){
					value = fxv.getValue();
					if (value == null) value = "unknown";
					if (value.length() == 0) value = "empty";
				}
				else{ %>
					<b>Value was not found!</b> <%
					return;
				}
			}
			else{
				if (mAttributes.size()==0){ %>
					<b>No metadata on attributes found! Nothing to add.</b> <%
					return;
				}
				
			}
				
			String disabled = user == null ? "disabled" : "";
			
			relElems = searchEngine.getRelatedElements(delem_id, "elem", null, "CH1");
			if (relElems == null) relElems = new Vector();

			
			boolean isWorkingCopy = parent_type.equals("elem") ? searchEngine.isWorkingCopy(delem_id, "elm") : true;

			//find parent url from history
			String parentUrl="";
			if (parent_type.equals("elem")){
				parentUrl="data_element.jsp?mode=view&delem_id="+delem_id;
				if (history!=null){
					String elemUrl = history.getLastMatching("data_element.jsp");
				
					if (elemUrl.indexOf("delem_id=" + delem_id)>-1)
						parentUrl = elemUrl;
					if (delem_name.equals("?")){
						DataElement elem = searchEngine.getDataElement(delem_id);
						if (elem!=null)	delem_name=elem.getShortName();
						if (delem_name == null) delem_name = "?";
					}
				}
			}
			else{
				String mm = mode.equals("print") ? "view" : mode;
				parentUrl="delem_attribute.jsp?attr_id=" + delem_id + "&type=SIMPLE&mode=" + mm;
				if (history!=null){
					String attrUrl = history.getLastMatching("delem_attribute.jsp");
				
					if (attrUrl.indexOf("delem_id=" + delem_id)>-1)
						parentUrl = attrUrl;
				}
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
				var b = confirm("This value will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
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

		function onLoad(){    
			<%
			String isDefault = "";
			if (!mode.equals("add")){
				isDefault = fxv.getDefault() ? "true" : "false";
    			%>
    			var isDefault = '<%=isDefault%>';
				var o = document.forms["form1"].is_default;
				for (i=0; o!=null && i<o.options.length; i++){
					if (o.options[i].value == isDefault){
						o.selectedIndex = i;
						break;
					}
				}
				<%
			}
			
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
			
			DElemAttribute attribute = null;
			%>
		}
			
	</script>
<body onLoad="onLoad()">
<%@ include file="header.htm" %>
<table border="0">
    <tr valign="top">
        <td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
        	<%
        	if (valsType.equals("CH1")){ %>
	            <jsp:include page="location.jsp" flush='true'>
	                <jsp:param name="name" value="Allowable value"/>
	                <jsp:param name="back" value="true"/>
	            </jsp:include><%
            }
            else{ %>
            	<jsp:include page="location.jsp" flush='true'>
	                <jsp:param name="name" value="Suggested value"/>
	                <jsp:param name="back" value="true"/>
	            </jsp:include><%
        	}
        	%>
            
<div style="margin-left:30">

	<%
	String backURL = "" + "/fixed_values.jsp?delem_id=" + delem_id +
															 "&delem_name=" + delem_name +
															 "&parent_type=" + parent_type;
															 //"&ns=" + ns;
	%>
		<form name="form1" method="POST" action="fixed_value.jsp">
			
			<table width="auto" cellspacing="0" cellpadding="0">
			
			<tr height="20"><td colspan="2"></td></tr>
			<tr valign="bottom">
				<td colspan="2">
					<span class="head00"><%=initCaseTitle%> value of</span>
					<span class="title2"><a href="<%=parentUrl%>"><%=Util.replaceTags(delem_name)%></a></span>
					<span class="head00"><%=dispParentType%></span>
				</td>
			</tr>
			
			<tr height="20"><td colspan="2"></td></tr>
			
			<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
			
			<tr>				
				<td align="right" style="padding-right:10" valign="top">
					<b><font color="black">Value</font></b>(M)
				</td>
				<td colspan="1" valign="top">
					<% if(!mode.equals("add")){ %>
						<font class="title2" color="#006666"><%=Util.replaceTags(value)%></font>
						<input type="hidden" name="fxv_value" value="<%=value%>"/>
					<% } else{ %>
						<input class="smalltext" type="text" size="30" name="fxv_value"></input>
					<% } %>
				</td>
			</tr>
			
			<%			
			if (parent_type.equals("attr")){
				%>
				<tr>				
					<td align="right" style="padding-right:10" valign="top">
						<b><font color="black">Default</font></b>(O)
					</td>
					<td colspan="1" valign="top">
						<select <%=disabled%> class="small" name="is_default">
							<option selected value="false">No</option>
							<option value="true">Yes</option>
						</select>
					</td>
				</tr>
				<%
			}
			
			// dynamical display of attributes
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				

				if (!attribute.displayFor("FXV"))
					continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				
				String width  = attribute.getDisplayWidth();
				String height = attribute.getDisplayHeight();
				
				disabled = user == null ? "disabled" : "";
				
				%>
				<tr>
					<td valign="top" style="padding-right:10" align="right"><b><%=attribute.getShortName()%></b>(<%=attribute.getObligation()%>)
						<!--b><a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=edit">
							<font color="black"><%=attribute.getShortName()%></font>
						</a></b-->
					</td>
					<td colspan="1" valign="top">
				<%
  				 if (mode.equals("print")){
					if (attrValue==null)
						%>&#160;<%
					else
						%><%=Util.replaceTags(attrValue)%><%
					continue;
				}

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
						<textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>"><%=Util.replaceTags(attrValue, true)%></textarea>
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
				
				%>
				</td></tr>
				<input type="hidden" name="oblig_<%=attrID%>" value="<%=attribute.getObligation()%>"/>
				<%
			}
			for (int i=0; relElems!=null && i<relElems.size(); i++){
			
				CsiItem item = (CsiItem)relElems.get(i);
			
				String compID = item.getComponentID();
				if (compID == null) continue;
				
				elemValues = searchEngine.getAllFixedValues(compID, "elem");
				if (elemValues == null) elemValues = new Vector();

				String selected_id = fxv.getItemIdByComponentId(compID);
				%>
				<tr>
					<td align="right" valign="top" style="padding-right:10">
						<a href="rel_element.jsp?delem_id=<%=delem_id%>&#38;delem_name=<%=delem_name%>&#38;child_id=<%=item.getID()%>">
						<span class="help">?</span></a>&#160;
						<span class="mainfont"><b>
							<%=Util.replaceTags(item.getValue())%>
						</b></span>
					</td>
					<td colspan="1" valign="top">
						<%
  				 		if (mode.equals("print")){
							for (int n=0; n<elemValues.size(); n++){
								FixedValue fxValue = (FixedValue)elemValues.get(n);
								String id = fxValue.getID();
								if(id.equals(selected_id)){
									if (fxValue.getValue()==null)
										%>&#160;<%
									else
										%><%=Util.replaceTags(fxValue.getValue())%><%
									break;
								}
							}
						}
						else{
						%>
							<select <%=disabled%> class="small" name="item_<%=item.getID()%>">
								<option value="">-- Not selected --</option>
								<%
								for (int n=0; n<elemValues.size(); n++){
									FixedValue fxValue = (FixedValue)elemValues.get(n);
										String id = fxValue.getID();
									String isSelected = id.equals(selected_id) ? "selected" : "";
									%>
									<option <%=isSelected%> value="<%=id%>"><%=Util.replaceTags(fxValue.getValue())%></option> <%
								}
								%>
							</select>
						<% } %>
					</td>
				</tr>
				<%
			}
			%>
		
		<tr height="10"><td colspan="2"></td></tr>
		
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
				
				if (!mode.equals("add") && !mode.equals("print")){ // if mode is not "add" and not "print"
					if (user==null || !isWorkingCopy){ %>									
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
		
		<input type="hidden" name="mode" value="<%=mode%>"/>
		
	</table>
	<input type="hidden" name="fxv_id" value="<%=fxv_id%>"/>
	<input type="hidden" name="del_id" value="<%=fxv_id%>"/>
	<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
	<input type="hidden" name="delem_name" value="<%=delem_name%>"/>
	<input type="hidden" name="ns" value="<%=ns%>"></input>
	
	<input type="hidden" name="parent_type" value="<%=valsType%>"/>
	
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