<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private Vector complexAttrs=null;%>

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
			
			String ds = request.getParameter("ds");
			
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
				
				String redirUrl = "complex_attrs.jsp?parent_id=" + parent_id +
															 "&parent_type=" + parent_type +
															 "&parent_name=" + parent_name +
															 "&parent_ns=" + parent_ns;
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
        	DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			Vector mComplexAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
			if (mComplexAttrs == null) mComplexAttrs = new Vector();
			
			complexAttrs = searchEngine.getComplexAttributes(parent_id, parent_type);
			
			if (complexAttrs == null) complexAttrs = new Vector();
			
			for (int i=0; mComplexAttrs.size()!=0 && i<complexAttrs.size(); i++){
				DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
				String attrID = attr.getID();
				for (int j=0; j<mComplexAttrs.size(); j++){
					DElemAttribute mAttr = (DElemAttribute)mComplexAttrs.get(j);
					String mAttrID = mAttr.getID();
					if (attrID.equals(mAttrID)){
						mComplexAttrs.remove(j);
						j--;
					}
				}
			}
			
			// JH170803
			// if the parent is not a working copy, its complex attributes cannot be edited.
			// so here we set the falg it is a working copy or not
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
					var b = confirm("This will delete all the attributes you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;	
				}
				
				document.forms["form1"].elements["mode"].value = mode;
				document.forms["form1"].submit();
			}
			
			<% String redirUrl = ""; %>
			
			function addNew(){
				var id = document.forms["form1"].elements["new_attr_id"].value;
				var url = "<%=redirUrl%>" + "complex_attr.jsp?mode=add&attr_id=" + id + 
							"&parent_id=<%=parent_id%>&parent_type=<%=parent_type%>&parent_name=<%=parent_name%>&parent_ns=<%=parent_ns%>";
				
				<%
				if (ds!=null && ds.equals("true")){
					%>
					url = url + "&ds=true";
					<%
				}
				%>
				
				window.location.replace(url);
			}
			
			function edit(id){
				var url = "<%=redirUrl%>" + "complex_attr.jsp?mode=edit&attr_id=" + id + 
							"&parent_id=<%=parent_id%>&parent_type=<%=parent_type%>&parent_name=<%=parent_name%>&parent_ns=<%=parent_ns%>";
				<%
				if (ds!=null && ds.equals("true")){
					%>
					url = url + "&ds=true";
					<%
				}
				%>
				
				window.location.replace(url);
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
						<td valign="bottom" align="middle"><span class="barfont">Complex attributes</span></td>
						<td valign="bottom" width="28"><img src="../images/bar_dot.jpg"/></td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

<form name="form1" method="POST" action="complex_attrs.jsp">

<table width="400">

	<%
	//String qualfName = (ds != null && ds.equals("true")) ? parent_name : parent_ns + ":" + parent_name;
	%>
	
	<tr valign="bottom">
		<td colspan="2">
			<span class="head00">Complex attributes of </span>
			<span class="title2" color="#006666"><%=Util.replaceTags(parent_name)%></span>
		</td>
	</tr>
	<tr height="10"><td colspan="2"></td></tr>
	
	<%
	if (complexAttrs.size() == 0){
		%>
		<tr height="10"><td colspan="2">None found!</td></tr>
		<%
	}
	else{
		%>
		<tr height="10">
			<td colspan="2">
			<%
			if (user!=null && isWorkingCopy){
				%>
				<input class="smallbutton" type="button" value="Remove selected" onclick="submitForm('delete')"/>
				<%
			}
			else{
				%>
				<input class="smallbutton" type="button" value="Remove selected" disabled/>
				<%
			}
			%>
			</td>
		</tr>
		<%
	}
	%>
	
	<tr height="5"><td colspan="2"></td></tr>
	<%
	if (mComplexAttrs.size() != 0){
		%>
		<tr height="10">
			<td colspan="2">
			<%
			if (user!=null){ %>
				<select class="small" name="new_attr_id"> <%
			} else{ %>
				<select class="small" name="new_attr_id" disabled> <%
			}
					for (int i=0; i<mComplexAttrs.size(); i++){
						DElemAttribute attr = (DElemAttribute)mComplexAttrs.get(i);
						String attrID = attr.getID();
						String attrName = attr.getShortName();
						%>
						<option value="<%=attrID%>"><%=attrName%></option>
						<%
					}
					%>
				</select>&#160;
				
				<%
				if (user != null && isWorkingCopy){
					%>
					<input class="smallbutton" type="button" value="Add new" onclick="addNew()"/>
					<%
				}
				else{
					%>
					<input class="smallbutton" type="button" value="Add new" disabled />
					<%
				}
				%>
				
			</td>
		</tr>
		
		<tr height="5"><td colspan="2"></td></tr>
		
		<%
	}
	%>
</table>

<%
	for (int i=0; i<complexAttrs.size(); i++){ // loop over attributes
		
		DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
		String attrID = attr.getID();
		String attrName = attr.getShortName();
		
		Vector attrFields = searchEngine.getAttrFields(attrID);
		
		%>
		
		<table cellspacing="0">
			<tr>
				<td align="right" style="border-bottom-width:1;border-bottom-style:groove;border-bottom-color:#808080;border-right-width:1;border-right-style:groove;border-right-color:#808080;">
					<input type="checkbox" style="height:13;width:13" name="del_attr" value="<%=attrID%>"/>
				</td>
				<td style="border-bottom-width:1;border-bottom-style:groove;border-bottom-color:#808080;">
					<b>&#160;<%=attrName%></b>
				</td>
			</tr>
			<tr>
				<td valign="top" style="padding-right:3;padding-top:3;border-right-width:1;border-right-style:groove;border-right-color:#808080;">
					<%
					if (user != null && isWorkingCopy){
						%>
						<input class="smallbutton" type="button" value="Edit" onClick="edit('<%=attrID%>')"/>
						<%
					}
					else{
						%>&#160;
<!--						<input class="smallbutton" type="button" value="Edit" disabled/ -->
						<%
					}
					%>
				</td>
				
				<td style="padding-left:3;padding-top:3">
					<table cellspacing="0">
						<tr>
						<%
						
						for (int t=0; t<attrFields.size(); t++){
							Hashtable hash = (Hashtable)attrFields.get(t);
							String name = (String)hash.get("name");
							%>
							<th align="left" style="padding-right:10"><%=name%></th>
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
								<td style="padding-right:10" <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=Util.replaceTags(fieldValue)%></td>
								<%
							}
							%>
							</tr>				
							<%
						}
						%>
					</table>
				</td>
			</tr>
			<tr height="5">
				<td colspan="2"></td>
			</tr>
		</table>
		<%
	}
%>

<input type="hidden" name="mode" value="delete"/>

<input type="hidden" name="parent_id" value="<%=parent_id%>"/>
<input type="hidden" name="parent_name" value="<%=parent_name%>"/>
<input type="hidden" name="parent_type" value="<%=parent_type%>"/>
<input type="hidden" name="parent_ns" value="<%=parent_ns%>"/>

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