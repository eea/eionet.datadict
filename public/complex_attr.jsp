<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

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
			
			String mode = request.getParameter("mode");
			
			if (mode == null || mode.length()==0){ %>
				<b>Mode is missing!</b> <%
				return;
			}
			
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
			
			String parent_id = request.getParameter("parent_id");
			
			if (parent_id == null || parent_id.length()==0){ %>
				<b>Parent ID is missing!</b> <%
				return;
			}
			
			String parent_type = request.getParameter("parent_type");
			
			if (parent_type == null || parent_type.length()==0){ %>
				<b>Parent type is missing!</b> <%
				return;
			}
			
			String parent_name = request.getParameter("parent_name");
			if (parent_name == null) parent_name = "?";
			
			String parent_ns = request.getParameter("parent_ns");
			if (parent_ns == null) parent_ns = "?";
			
			String attr_id = request.getParameter("attr_id");
			
			if (attr_id == null || attr_id.length()==0){ %>
				<b>Attribute ID is missing!</b> <%
				return;
			}
			
			String attr_name = request.getParameter("attr_name");
			if (attr_name == null) attr_name = "?";
			
			String attr_ns = request.getParameter("attr_ns");
			if (attr_ns == null) attr_ns = "?";
			
			String ds = request.getParameter("ds");
			
			// For getting inherited attributes
			String dataset_id = request.getParameter("dataset_id");
			if (dataset_id == null) dataset_id = "";
			String table_id = request.getParameter("table_id");
			if (table_id == null) table_id = "";

			String redirUrl = "complex_attr.jsp?mode=edit&parent_id=" + parent_id +
															 "&parent_type=" + parent_type +
															 "&parent_name=" + parent_name +
															 "&parent_ns=" + parent_ns +
															 "&attr_id=" + attr_id+
															 "&table_id=" + table_id+
															 "&dataset_id=" + dataset_id;
			if (ds != null)
				redirUrl = redirUrl + "&ds=" + ds;

			if (request.getMethod().equals("POST")){
				
				Connection userConn = null;
				
				try{
					userConn = user.getConnection();
					AttrFieldsHandler handler = new AttrFieldsHandler(userConn, request, ctx);
					
					try{
						handler.execute();
					}
					catch (Exception e){
						%>
						<html><body><b><%=e.toString()%></b></body></html>
						<%
						return;
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				
				if (mode.equals("delete")) redirUrl = redirUrl + "&wasdel=true";
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			Vector v = null;
			
			if (mode.equals("add"))
				v = searchEngine.getDElemAttributes(attr_id,DElemAttribute.TYPE_COMPLEX);
			else
				v = searchEngine.getComplexAttributes(parent_id, parent_type, attr_id, table_id, dataset_id);
			
			DElemAttribute attribute = (v==null || v.size()==0) ? null : (DElemAttribute)v.get(0);
			boolean inherit = attribute.getInheritable().equals("0") ? false:true;
			
			
			Vector attrFields = searchEngine.getAttrFields(attr_id);
			
			String _type = null;
			if (parent_type.equals("E"))
				_type="elm";
			else if (parent_type.equals("DS"))
				_type="dst";
			else if (parent_type.equals("T"))
				_type="tbl";
			boolean isWorkingCopy = _type==null ? true : searchEngine.isWorkingCopy(parent_id, _type);
			%>

<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
	    <script language="JavaScript" src='script.js'></script>
	</head>
	<script language="JavaScript">
			function submitForm(mode){
				
				if (mode == "delete"){
					var b = confirm("This will delete all the rows you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;	
				}
				
				document.forms["form1"].elements["mode"].value = mode;
				document.forms["form1"].submit();
			}
			function goTo(mode){
				if (mode == "edit"){
					document.location.assign("<%=redirUrl%>");
				}
			}
			
			function openValues(id){
				attrWindow=window.open("pick_attrvalue.jsp?attr_id=" + id + "&type=COMPLEX","Attribute_values","height=400,width=700,status=no,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
				if (window.focus) {attrWindow.focus()}
			}
	</script>
<body style="background-color:#f0f0f0;background-image:url('../images/eionet_background2.jpg');background-repeat:repeat-y;"
		topmargin="0" leftmargin="0" marginwidth="0" marginheight="0">
<div style="margin-left:30">
	<br></br>
	<font color="#006666" size="5" face="Arial"><strong><span class="head2">Data Dictionary</span></strong></font>
	<br></br>
	<font color="#006666" face="Arial" size="2">
		<strong><span class="head0"><script language="JavaScript">document.write(getDDVersionName())</script></span></strong>
	</font>
	<br></br>
	<table cellspacing="0" cellpadding="0" width="400" border="0">
			<tr>
	         	<td align="bottom" width="20" background="../images/bar_filled.jpg" height="25">&#160;</td>
	          	<td width="600" background="../images/bar_filled.jpg" height="25">
		            <table height="8" cellSpacing="0" cellPadding="0" border="0">
		            	<tr>
				         	<td valign="bottom" align="middle"><span class="barfont">EIONET</span></td>
				            <td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
				         	<td valign="bottom" align="middle"><span class="barfont">Data Dictionary</span></td>
							<td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
							<td valign="bottom" align="middle"><span class="barfont">Complex attribute</span></td>
							<td valign="bottom" width="28"><img src="../images/bar_dot.jpg"/></td>
						</tr>				
					</table>
				</td>
			</tr>			
	</table>

	<%
	
	String backURL = "complex_attrs.jsp?parent_id=" + parent_id +
															 "&parent_type=" + parent_type +
															 "&parent_name=" + parent_name +
															 "&parent_ns=" + parent_ns +
															 "&table_id=" + table_id+
															 "&dataset_id=" + dataset_id;
	if (ds != null)
		backURL = backURL + "&ds=" + ds;
		
	if (mode.equals("add")){
		if (attrFields == null || attrFields.size()==0){
			%>
			<b>No fields found for this attribute!</b></div></body></html>
			<%
			return;
		}
	}
	else if (attribute == null){
		String wasDelete = request.getParameter("wasdel");
		if (wasDelete != null){
			response.sendRedirect(backURL);
			return;
		}
		%>
		<b>Attribute not found!</b><br>
			<a href="javascript:history.back(-1)">
				<b>< back</b>
			</a></div>
		</body></html>
		<%
		return;
	}
	
	String attrName = attribute.getShortName();
	int position = 0;
	
	
	Vector rows = null;
	Vector inheritRows=null;
	Vector originalRows=null;


	if (inherit){
		if (mode.equals("view")){
			rows = attribute.getRows();
		}
		else{
			inheritRows = attribute.getInheritedValues();
			rows = attribute.getOriginalValues();
		}
	}
	else
		rows = attribute.getRows();
	
	
	%>
		
<form name="form1" method="POST" action="complex_attr.jsp">

<table width="400">
<%
if (!mode.equals("view")){
%>
<tr>
	<td colspan="2">
		<span class="smallfont">
			<a href="javascript:window.location.replace('<%=backURL%>')">
				<b>< back to attributes</b>
			</a>
		</span>
	</td>
</tr>
<%
}
%>
<tr height="10"><td colspan="2"></td></tr>

<tr valign="bottom">
		<td colspan="2">
			<span class="head00">
				<%
				String nsPrefix = "";
				if (parent_type.equals("DS")){
					%>Dataset: <%
				}
				else if (parent_type.equals("T")){
					nsPrefix = parent_ns + ":";
					%>Table: <%
				}
				else if (parent_type.equals("C")){
					nsPrefix = parent_ns + ":";
					%>Class: <%
				}
				else {
					if (ds != null && ds.equals("true")){
						%>Dataset: <%
					} else {
						nsPrefix = parent_ns + ":";
						%>Element: <%
					}
				}
				%>
			</span>
			<span class="title2" color="#006666"><%=Util.replaceTags(parent_name)%></span>
		</td>
	</tr>
	
	<tr valign="bottom">
		<td>
			<span class="head00">Attribute: </span><span class="title2" color="#006666"><%=attrName%></span>
		</td>
		<td align="right">
			<%
				if (user != null && isWorkingCopy && mode.equals("view")){
			%>
					<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit')"/> 
			<%
				}
			%>
		</td>
	</tr>
	
	<tr height="10"><td colspan="2"></td></tr>
	
</table>

<%
if (!mode.equals("view")){
%>
<div style="margin-left:5">
<%
	if (user!=null){
		%>
		<input class="smallbutton" type="button" value="Add" onclick="submitForm('add')">
		<input class="smallbutton" type="button" value="Copy" onclick="openValues('<%=attr_id%>')">
		<%
	}
	else{
		%>
		<input class="smallbutton" type="button" value="Add" disabled="true"/>
		<%
	}
%>
<table style="border: 1 solid #808080">

	<%
		for (int t=0; t<attrFields.size(); t++){			
			Hashtable hash = (Hashtable)attrFields.get(t);
			String id = (String)hash.get("id");
			String name = (String)hash.get("name");
			%>
			<tr>
				<td width="100" align="right"><span class="mainfont"><b><%=name%></b>:</span></td>
				<td><input class="smalltext" type="text" name="<%=AttrFieldsHandler.FLD_PREFIX%><%=id%>"/></td>
			</tr>
			<%
		}
	%>
</table>
</div>
</br>
<%
}
%>
<div style="margin-left:5">

	<table cellpadding="0" cellspacing="0">
	
		<tr>
			<%
			if (!mode.equals("view")){
			%>
				<td>
					<%
					if (user!=null && (rows!=null && rows.size()!=0)){
						%>
						<input class="smallbutton" type="button" value="Remove" onclick="submitForm('delete')">
						<%
					}
					else{
						%>
						<input class="smallbutton" type="button" value="Remove" disabled/>
						<%
					}
					%>
				</td>
				<td width="10">&#160;</td>
				<%
			}
			%>
			
			
			<%
			for (int t=0; t<attrFields.size(); t++){
				Hashtable hash = (Hashtable)attrFields.get(t);
				String name = (String)hash.get("name");
					%>
					<th align="left" style="padding-right:10">&#160;<%=name%></th>
					<%
			}
			%>
		</tr>
		<%
		int displayed=0;
		// show inherited rows
		if (inherit && inheritRows!=null && !mode.equals("view")){
			String sInhText = (rows!=null && rows.size()>0 && attribute.getInheritable().equals("2")) ? "Overridden":"Inherited";
			if (sInhText.equals("Inherited")){
				for (int j=0; inheritRows!=null && j<inheritRows.size(); j++){
					Hashtable rowHash = (Hashtable)inheritRows.get(j);
					String row_id = (String)rowHash.get("rowid");
					int pos = Integer.parseInt((String)rowHash.get("position"));
					if (pos >= position) position = pos +1;
					%>
					<tr>
						<td align="right"><span><%=sInhText%></td>
						<td width="10">&#160;</td>
					<%
			
					for (int t=0; t<attrFields.size(); t++){
						Hashtable hash = (Hashtable)attrFields.get(t);
						String fieldID = (String)hash.get("id");
						String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
						if (fieldValue == null) fieldValue = " ";
							%>
							<td <% if (displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-right:10">&#160;<%=Util.replaceTags(fieldValue)%></td>
							<%
					}		
					displayed++;
					
					%>
					</tr>				
					<%
				}
			}
		}
		
		for (int j=0; rows!=null && j<rows.size(); j++){
			Hashtable rowHash = (Hashtable)rows.get(j);
			String row_id = (String)rowHash.get("rowid");
			int pos = Integer.parseInt((String)rowHash.get("position"));
			if (pos >= position) position = pos +1;
			%>
			<tr>
			<%
			if (!mode.equals("view")){
			%>
				<td align="right"><input type="checkbox" style="height:13;width:13" name="del_row" value="<%=row_id%>"/></td>
				<td width="10">&#160;</td>
			<%
			}
			%>
			<%
			
			for (int t=0; t<attrFields.size(); t++){
				Hashtable hash = (Hashtable)attrFields.get(t);
				String fieldID = (String)hash.get("id");
				String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
				if (fieldValue == null) fieldValue = " ";
					%>
					<td <% if (displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-right:10">&#160;<%=Util.replaceTags(fieldValue)%></td>
					<%
			}
			displayed++;
			
			%>
			</tr>				
			<%
		}
		%>	

	</table>
</div>

<input type="hidden" name="mode" value="<%=mode%>"/>

<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
<input type="hidden" name="parent_id" value="<%=parent_id%>"/>
<input type="hidden" name="parent_name" value="<%=parent_name%>"/>
<input type="hidden" name="parent_type" value="<%=parent_type%>"/>
<input type="hidden" name="parent_ns" value="<%=parent_ns%>"/>
<input type="hidden" name="table_id" value="<%=table_id%>"/>
<input type="hidden" name="dataset_id" value="<%=dataset_id%>"/>

<input type="hidden" name="position" value="<%=String.valueOf(position)%>"></input>

<%
if (ds != null){
	%>
	<input type="hidden" name="ds" value="<%=ds%>"/>
	<%
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
