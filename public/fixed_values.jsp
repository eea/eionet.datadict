<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%!private Vector fixedValues=null;%>
<%!private String mode=null;%>
<%!private static final int MAX_CELL_LEN=70;%>

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
	      					<h1>Error</h1><b>Not authorized to & post any data!</b>
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
			
			String delem_name = request.getParameter("delem_name");
			if (delem_name == null) delem_name = "?";
			
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
						e.printStackTrace(System.out);
						%>
						<html><body>
							<b><%=e.toString()%></b><br/>
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

			fixedValues = searchEngine.getFixedValues(delem_id, parent_type);
			if (fixedValues == null) fixedValues = new Vector();

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
			
			if (mode == "delete_all"){
				selectAll();
				mode = "delete";
			}
			
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
		
		function selectAll(){
			
			var checks = document.form1.del_id;
			for (var i=0; checks!=null && i<checks.length; i++){
				checks[i].checked=true;
			}
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
<table width="600">
	
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
			
	<tr height="5"><td colspan="2"></td></tr>
</table>
<% if (mode.equals("view")){
	%>
	<table width="auto" cellspacing="0">
		<tr>
			<th align="left" style="padding-left:5;padding-right:10">Value</th>
			<th align="left" style="padding-left:5;padding-right:10">ShortDescription</th>
			<th align="left" style="padding-left:5;padding-right:10">Definition</th>
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
			
			String definition = fxv.getDefinition();
			definition = definition==null ? "" : definition;
			definition = definition.length()>MAX_CELL_LEN ? definition.substring(0,MAX_CELL_LEN) + "..." : definition;
			
			String shortDesc = fxv.getShortDesc();
			shortDesc = shortDesc==null ? "" : shortDesc;
			shortDesc = shortDesc.length()>MAX_CELL_LEN ? shortDesc.substring(0,MAX_CELL_LEN) + "..." : shortDesc;
			
			%>
			
			<tr <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td valign="bottom" align="left" style="padding-left:5;padding-right:10">
					<%=spaces%>
					<b>
					<a href="fixed_value.jsp?fxv_id=<%=fxvID%>&amp;mode=<%=mode%>&amp;delem_id=<%=delem_id%>&amp;delem_name=<%=delem_name%>&amp;parent_type=<%=typeParam%>">
						<%=Util.replaceTags(value)%>
					</a>
					</b>
				</td>
				<td valign="bottom" align="left" title="Definition" style="padding-left:5;padding-right:10">
					<span class="barfont"><%=definition%></span>
				</td>
				<td valign="bottom" align="left" title="Definition" style="padding-left:5;padding-right:10">
					<span class="barfont"><%=shortDesc%></span>
				</td>
			</tr>
		<%
		}
		%>
	</table>
<%} else {%>
<table width="600" cellspacing="0"  border="0"><tr><td rowspan="2">	
	<table width="auto" cellspacing="0" cellpadding="0" id="tbl">
	<% if (user != null) { %>
		<tr>
			<td align="left" colspan="3">
				<input class="smallbutton" type="button" value="Remove selected" onclick="submitForm('delete')">&#160;
				<input class="smallbutton" type="button" value="Remove all" onclick="submitForm('delete_all')">
			</td>
		</tr>
		<tr height="3"><td colspan="3"></td></tr>
	<% } %>	
  	<tr>
  		<th>&#160;</th>
		<th align="left" style="padding-left:5;padding-right:10" width="100">Value</th>
		<th align="left" style="padding-right:10" width="500">Definition</th>
	</tr>
	<tbody>
			
	<%
	for (int i=0; i<fixedValues.size(); i++){
		
		FixedValue fxv = (FixedValue)fixedValues.get(i);
		String value = fxv.getValue();
		String fxvID = fxv.getID();
		String fxvAttrValue = null;
		String fxvAttrValueShort = null;
		
		String definition = fxv.getDefinition();
		definition = definition==null ? "" : definition;
		definition = definition.length()>MAX_CELL_LEN ? definition.substring(0,MAX_CELL_LEN) + "..." : definition;
		
		%>
		<tr id="<%=fxvID%>" onclick="tbl_obj.selectRow(this);" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<% if (user != null) { %>
				<td align="right" valign="top" style="padding-right:10">
					<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=fxvID%>" onclick="tbl_obj.clickOtherObject();"/>
				</td>
			<% } %>
			<td valign="bottom" align="left" style="padding-left:5;padding-right:10">
				<b><a href="javascript:clickLink('fixed_value.jsp?fxv_id=<%=fxvID%>&amp;mode=edit&amp;delem_id=<%=delem_id%>&amp;delem_name=<%=delem_name%>&amp;parent_type=<%=typeParam%>')">
					<%=Util.replaceTags(value)%>
				</a></b>&#160;
			</td>
			<td align="left" style="padding-right:10" title="Definition">
				<span class="barfont"><%=Util.replaceTags(definition)%></span>
			</td>
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
	<%/*
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
		}*/
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

</form>
<br/><br/>
</div>
</body>
</html>

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {
	}
}
%>