<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%!private Vector fixedValues=null;%>
<%!private Vector mAttributes=null;%>
<%!private Vector fxvAttributes=null;%>
<%!private Vector fxvRelElems=null;%>
<%!private String mode=null;%>

<%@ include file="history.jsp" %>

<%!

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
			
			String delem_id = request.getParameter("delem_id");
			
			if (delem_id == null || delem_id.length()==0){ %>
				<b>Parent ID is missing!</b> <%
				return;
			}

			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) {
				mode = "view";
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
			
			
			
			String parentCSI = request.getParameter("parent_csi");
			String prevParent = request.getParameter("prv_parent_csi");
			
			String delem_name = request.getParameter("delem_name");
			if (delem_name == null) delem_name = "?";
			
			//String delem_ns = request.getParameter("ns");
			
			// handle the POST
			
			if (request.getMethod().equals("POST")){
				
				Connection userConn = null;
				try{
					userConn = user.getConnection();
					FixedValuesHandler handler =
						new FixedValuesHandler(userConn, request, ctx);
					
					try{
						handler.execute();
					}
					catch (Exception e){
						%>
						<html><body>
							<b><%=e.toString()%></b><br/>
							<%
							
							/*String redirUrl = "";
							redirUrl = redirUrl + "fixed_values.jsp?mode=edit&delem_id=" + delem_id +
																 "&delem_name=" + delem_name +
																 "&parent_type=" + parent_type + 
																 "&mode=edit";
																 
																 //"&ns=" + delem_ns;
							if (parentCSI!=null && parentCSI.length()!=0)
								redirUrl = redirUrl + "&parent_csi=" + parentCSI;
							if (prevParent!=null && prevParent.length()!=0)
								redirUrl = redirUrl + "&prv_parent_csi=" + prevParent;
								*/
							%>
							<a href="javascript:window.location.replace('<%=currentUrl%>')">&lt; back</a>
						</body></html>
						<%
						return;
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				/*
				StringBuffer redirUrl = new StringBuffer("");
				redirUrl.append("/fixed_values.jsp?mode=edit&delem_id=");
				redirUrl.append(delem_id);
				redirUrl.append("&delem_name=");
				redirUrl.append(delem_name);
				redirUrl.append("&parent_type=");
				redirUrl.append(parent_type);
				if (parentCSI!=null && parentCSI.length()!=0){
					redirUrl.append("&parent_csi=");
					redirUrl.append(parentCSI);
				}				
				if (prevParent!=null && prevParent.length()!=0){
					redirUrl.append("&prv_parent_csi=");
					redirUrl.append(prevParent);
				}
				*/
				//redirUrl.append("&ns=");
				//redirUrl.append(delem_ns);
							
				//response.sendRedirect(redirUrl.toString());
				response.sendRedirect(currentUrl);
				return;
			}
			
			// handle the GET
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

			if (mode.equals("view")){
				fixedValues = searchEngine.getAllFixedValues(delem_id, parent_type);
			}
			else{
				if (parentCSI!=null && parentCSI.length()!=0)
					fixedValues = searchEngine.getSubValues(parentCSI);
				else
					fixedValues = searchEngine.getFixedValues(delem_id, parent_type);
			}

			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			fxvAttributes = new Vector();
			DElemAttribute attribute = null;

			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType != null &&
						attribute.displayFor("FXV")){
					fxvAttributes.add(attribute);
				}
			}



			if (fixedValues == null) fixedValues = new Vector();

			fxvRelElems = searchEngine.getRelatedElements(delem_id, "elem", null, "CH1");
			if (fxvRelElems == null) fxvRelElems = new Vector();
			Vector relElemId = new Vector();
			
			String disabled = user == null ? "disabled" : "";
			
			boolean isWorkingCopy = parent_type.equals("elem") ? searchEngine.isWorkingCopy(delem_id, "elm") : true;
			
			
			//find parent url from history
			String parentUrl="";
			if (parent_type.equals("elem")){
				parentUrl="data_element.jsp?mode=view&delem_id="+delem_id;
				if (history!=null){
					String elemUrl = history.getLastMatching("data_element.jsp");
				
					if (elemUrl.indexOf("delem_id=" + delem_id)>-1)
						parentUrl = elemUrl;
				}
			}
			else{
				parentUrl="delem_attribute.jsp?attr_id=" + delem_id + "&type=SIMPLE&mode=" + mode;
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
	<script language="JavaScript" src='dynamic_table.js'></script>
	
	<script language="JavaScript">
			function submitForm(mode){
				document.forms["form1"].elements["mode"].value = mode;
				if (mode == "delete"){
					var b = confirm("This will delete all the  values you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;
				}
				document.forms["form1"].submit();
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
	    	if (modeStart == -1){  //could not find modeString
		    	modeStart = 0;
		    	modeString = "";
			}	    	
	    	if (modeStart != -1){
		    	StringBuffer buf = new StringBuffer(qryStr.substring(0, modeStart));
		    	buf.append("mode=edit&");
		    	buf.append(qryStr.substring(modeStart + modeString.length()));
		    	%>
				document.location.assign("fixed_values.jsp?<%=buf.toString()%>");
				<%
			}
			%>
		}
		function saveChanges(){
			tbl_obj.insertNumbers("pos_");
			submitForm("edit_positions");
		}
		function clickLink(sUrl){
			if (getChanged()==1){
				if(!confirm("This link leads you to the next page, but you have changed the order of elements.\n Are you sure you want to loose the changes?"))
					return;
			}
			window.location=sUrl;
		}
		function start() {
			<%
			if (!mode.equals("view")){ %>
				tbl_obj=new dynamic_table("tbl"); //create dynamic_table object
				<%
			}
			%>
		}

		//call to dynamic table methods. Originated from buttons or click on tr.
		function sel_row(o){
			tbl_obj.selectRow(o);
		}
		function moveRowUp(){
			tbl_obj.moveup();
			setChanged();
		}
		function moveRowDown(){
			tbl_obj.movedown();
			setChanged();
		}
		function moveFirst(){
			tbl_obj.movefirst();
			setChanged();
		}
		function moveLast(){
			tbl_obj.movelast();
			setChanged();
		}
		function setChanged(){
			document.forms["form1"].elements["changed"].value = 1;
		}
		function getChanged(){
			return document.forms["form1"].elements["changed"].value;
		}
		
		function importCodes(){
			var url = "import.jsp?mode=FXV&delem_id=<%=delem_id%>&short_name=<%=delem_name%>";
			document.location.assign(url);
		}
	</script>
	
<body onload="start()">
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
	                <jsp:param name="name" value="Allowable values"/>
	                <jsp:param name="back" value="true"/>
	            </jsp:include><%
            }
            else{ %>
            	<jsp:include page="location.jsp" flush='true'>
	                <jsp:param name="name" value="Suggested values"/>
	                <jsp:param name="back" value="true"/>
	            </jsp:include><%
        	}
        	%>
            
<div style="margin-left:30">
			
<form name="form1" method="POST" action="fixed_values.jsp">
<table width="400">
	
	<%
	
	if (parentCSI != null && parentCSI.length()!=0){
		StringBuffer buf = new StringBuffer();
		buf.append("fixed_values.jsp?mode=edit&delem_id=");
		buf.append(delem_id);
		buf.append("&delem_name=");
		buf.append(delem_name);
		buf.append("&parent_type=");
		buf.append(parent_type);
		if (prevParent != null && prevParent.length()!=0){
			buf.append("&parent_csi=");
			buf.append(prevParent);
		}
		%>
		<!--tr>
			<td colspan="2">
				<a href="javascript:window.location.replace('<%=buf.toString()%>')">&lt; back to upper level</a>
			</td>
		</tr>
		<tr height="20"><td colspan="2"></td></tr-->
		<%
	}
	%>
			
	<tr valign="bottom">
		<td colspan="2">
			<span class="head00">
				<%=initCaseTitle%> values of
				<span class="title2"><a href="<%=parentUrl%>"><%=Util.replaceTags(delem_name)%></a></span>
				<span class="head00"><%=dispParentType%></span>
			</span>
		</td>
	</tr>
	<tr height="10"><td colspan="2"></td></tr>
	<tr><td align="left">
		</td>
	</tr>
	<% if (mode.equals("view")){
		if (user!=null && isWorkingCopy){ %>
			<tr>
				<td colspan="2" align="right">
					<input type="button" class="smallbutton" value="Edit" onclick="edit()"/>
				</td>
			</tr>
		<%
		}
	
	 } else {%>
		<% if (user != null) { %>
			<tr height="10"><td colspan="2"><font class="mainfont">Enter a new value here:</font></td></tr>
			<tr>
				<td colspan="1" width="300">
					<input class="smalltext" type="text" size="20" name="new_value"></input>
					<input class="smallbutton" type="button" value="Add" onclick="submitForm('add')"/>&#160;
					<input class="smallbutton" type="button" value="Import..." onclick="importCodes()"/>
				</td>
			</tr>
		<% } %>
	<% } %>
			
	<tr height="10"><td colspan="2"></td></tr>
</table>
<% if (mode.equals("view")){
	%>
	<table width="auto" cellspacing="0">
		<tr>
			<th align="left" style="padding-left:5;padding-right:10" width="100">Value</th>
			<%
				for (int i=0; fxvAttributes!=null && i<fxvAttributes.size(); i++){
								
					attribute = (DElemAttribute)fxvAttributes.get(i);
					%>
					<th align="left" style="padding-left:5;padding-right:10" width="150"><%=attribute.getShortName()%></th>
					<%
				}
				for (int i=0; fxvRelElems!=null && i<fxvRelElems.size(); i++){
					CsiItem item = (CsiItem)fxvRelElems.get(i);
					String compID = item.getComponentID();
					if (compID == null) continue;
					relElemId.add(compID);
					%>
					<th align="left" style="padding-left:5;padding-right:10" width="150"><%=Util.replaceTags(item.getValue())%></th>
					<%
				}
			%>
		</tr>
		<%
		String mode= (user == null) ? "print" : "edit";
		for (int i=0; i<fixedValues.size(); i++){
			FixedValue fxv = (FixedValue)fixedValues.get(i);
			String value = fxv.getValue();
			String fxvID = fxv.getID();
			int level=fxv.getLevel();
			String fxvAttrValue = null;
			String fxvAttrValueShort = null;
			String spaces="";
			for (int j=1; j<level; j++){
				spaces +="&#160;&#160;&#160;";
			}
			
			%>
			<tr>
				<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<%=spaces%> <b><a href="fixed_value.jsp?fxv_id=<%=fxvID%>&#38;mode=<%=mode%>&delem_id=<%=delem_id%>&delem_name=<%=delem_name%>&parent_type=<%=typeParam%>">
						<%=Util.replaceTags(value)%>
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
							<span class="barfont"><%=Util.replaceTags(fxvAttrValueShort)%></span>
						</td>
					<%
					}
				}
				for (int k=0; relElemId!=null && k<relElemId.size(); k++){
					String component_id = (String)relElemId.get(k);
					String relElemValue = component_id!=null ? fxv.getItemValueByComponentId(component_id):"";
					%>
						<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
							<span class="barfont"><%=Util.replaceTags(relElemValue)%></span>
						</td>
					<%
				}
				%>
			</tr>
		<%
		}
		%>
	</table>
<%} else {%>
<table width="auto" cellspacing="0"  border="0"><tr><td rowspan="2">	
	<table width="auto" cellspacing="0" cellpadding="0" id="tbl">
  	<tr>
		<% if (user != null) { %>
			<td align="right" style="padding-right:10">
				<input class="smallbutton" type="button" value="Remove" onclick="submitForm('delete')">
			</td>
		<% } %>
		<th align="left" style="padding-left:5;padding-right:10">Value</th>
		<%
			for (int i=0; fxvAttributes!=null && i<fxvAttributes.size(); i++){
				
				attribute = (DElemAttribute)fxvAttributes.get(i);
				%>
				<th align="left" style="padding-right:10"><%=attribute.getShortName()%></th>
				<%
			}
			for (int i=0; fxvRelElems!=null && i<fxvRelElems.size(); i++){
	
				CsiItem item = (CsiItem)fxvRelElems.get(i);
				String compID = item.getComponentID();
				if (compID == null) continue;
				relElemId.add(compID);
				%>
				<th align="left" style="padding-right:10" width="150"><%=Util.replaceTags(item.getValue())%></th>
			<%
			}
			%>
	</tr>
	<tbody>
			
	<%
	for (int i=0; i<fixedValues.size(); i++){
		FixedValue fxv = (FixedValue)fixedValues.get(i);
		String value = fxv.getValue();
		String fxvID = fxv.getID();
		String fxvAttrValue = null;
		String fxvAttrValueShort = null;
		
		String alt = searchEngine.hasSubValues(fxvID) ? "go to sub-values" : "add sub-values";
		StringBuffer buf = new StringBuffer();
		buf.append("fixed_values.jsp?mode=edit&delem_id=");
		buf.append(delem_id);
		buf.append("&delem_name=");
		buf.append(delem_name);
		buf.append("&parent_type=");
		buf.append(typeParam);
		buf.append("&parent_csi=");
		buf.append(fxvID);
		
		if (parentCSI != null && parentCSI.length()!=0){
			buf.append("&prv_parent_csi=");
			buf.append(parentCSI);
		}
		
		%>
		<tr id="<%=fxvID%>" onclick="tbl_obj.selectRow(this);" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<% if (user != null) { %>
				<td align="right" style="padding-right:10">
					<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=fxvID%>" onclick="tbl_obj.clickOtherObject();"/>
				</td>
			<% } %>
			<td valign="bottom" align="left" style="padding-left:5;padding-right:10">
				<b><a href="javascript:clickLink('fixed_value.jsp?fxv_id=<%=fxvID%>&#38;mode=edit&delem_id=<%=delem_id%>&delem_name=<%=delem_name%>&parent_type=<%=typeParam%>')">
					<%=Util.replaceTags(value)%>
				</a></b>&#160;
				<map name="map<%=i%>">
					<area alt="<%=alt%>" shape="rect" coords="0,0,18,10" href="javascript:clickLink('<%=buf.toString()%>')"></area>
				</map>
				<img border="0" src="images/deeper.gif" height="10" width="18" usemap="#map<%=i%>"></img>
			</td>
			<%
			for (int c=0; fxvAttributes!=null && c<fxvAttributes.size(); c++){
	
				attribute = (DElemAttribute)fxvAttributes.get(c);
				
				fxvAttrValue = fxv.getAttributeValueByID(attribute.getID());
				if (fxvAttrValue==null || fxvAttrValue.length()==0){
				%>
					<td align="center" width="100" onmouseover=""></td>
				<%
			}
				else{
					if (fxvAttrValue.length()>60) 
						fxvAttrValueShort = fxvAttrValue.substring(0,60) + " ...";
					else
						fxvAttrValueShort = fxvAttrValue;

					%>
					<td align="left" style="padding-right:10" title="<%=fxvAttrValue%>">
						<span class="barfont"><%=Util.replaceTags(fxvAttrValueShort)%></span>
					</td>
					<%
				}
			}
			for (int k=0; relElemId!=null && k<relElemId.size(); k++){
				String component_id = (String)relElemId.get(k);
				String relElemValue = component_id!=null ? fxv.getItemValueByComponentId(component_id):"";
				%>
					<td align="left" style="padding-right:10">
						<span class="barfont"><%=Util.replaceTags(relElemValue)%></span>
					</td>
				<%
			}
			%>
			<td>
				<input type="hidden" name="pos_id" value="<%=fxvID%>" size="5">
				<input type="hidden" name="oldpos_<%=fxvID%>" value="<%=fxv.getPosition()%>" size="5">
				<input type="hidden" name="pos_<%=fxvID%>" value="0" size="5">
			</td>
		</tr>
		<%
	}
	%>
	
		</tbody>
	</table>
	</td>
	<%
		if (user!=null && isWorkingCopy && fixedValues.size()>1){ %>
			<td align="left" style="padding-right:10" valign="top" height="10">
				<input type="button" <%=disabled%> value="Save" class="smallbutton" onclick="saveChanges()" title="save the new order of elements"/>
			</td>
			</tr><tr><td>
					<table cellspacing="2" cellpadding="2" border="0">
						<tr>
						</tr>
						<td>
							<a href="javascript:moveFirst()"><img src="images/move_first.gif" border="0" title="move selected row to top"/></a>			
						</td></tr>
						<td>
							<a href="javascript:moveRowUp()"><img src="images/move_up.gif" border="0" title="move selected row up"/></a>			
						</td></tr>
						<tr><td>
							<img src="images/dot.gif"/>
						</td></tr>
						<tr><td>
							<a href="javascript:moveRowDown()"><img src="images/move_down.gif" border="0" title="move selected row down"/></a>			
						</td>
						<tr><td>
							<a href="javascript:moveLast()"><img src="images/move_last.gif" border="0" title="move selected row last"/></a>			
						</td>
					</tr><%
		}
		%>
		</table> 
	</td></tr></table>
<%
}
%>


<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
<input type="hidden" name="delem_name" value="<%=delem_name%>"/>
<input type="hidden" name="parent_type" value="<%=typeParam%>"></input>
<input type="hidden" name="mode" value="<%=mode%>"/>
<input type="hidden" name="changed" value="0"/>

<%
if (parentCSI!=null && parentCSI.length()!=0){ %>
	<input type="hidden" name="parent_csi" value="<%=parentCSI%>"/> <%
}
if (prevParent!=null && prevParent.length()!=0){ %>
	<input type="hidden" name="prv_parent_csi" value="<%=prevParent%>"/> <%
}
%>
</form>
</div>
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
