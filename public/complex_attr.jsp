<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

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
			
			request.setCharacterEncoding("UTF-8");
			
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
				<span class="error">Mode is missing!</span> <%
				return;
			}
			
			if (request.getMethod().equals("POST")){
      			if (user == null){
	      			%>
	      				<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
	      				<body class="popup">
                                        <div class="error">
	      					<h1>Error</h1>
                                                <p>Not authorized to post any data!</p>
                                        </div>
	      				</body>
	      				</html>
	      			<%
	      			return;
      			}
			}						
			
			String parent_id = request.getParameter("parent_id");
			
			if (parent_id == null || parent_id.length()==0){ %>
				<span class="error">Parent ID is missing!</span> <%
				return;
			}
			
			String parent_type = request.getParameter("parent_type");
			
			if (parent_type == null || parent_type.length()==0){ %>
				<span class="error">Parent type is missing!</span> <%
				return;
			}
			
			String parent_name = request.getParameter("parent_name");
			if (parent_name == null) parent_name = "?";
			
			String parent_ns = request.getParameter("parent_ns");
			if (parent_ns == null) parent_ns = "?";
			
			String attr_id = request.getParameter("attr_id");
			
			if (attr_id == null || attr_id.length()==0){ %>
				<span class="error">Attribute ID is missing!</span> <%
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
						<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"><body class="popup"><h1>Error</h1><p><%=e.toString()%></p></body></html>
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

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
	<head>
		<%@ include file="headerinfo.txt" %>
		<title>Complex attribute</title>
	    <script type="text/javascript" src="script.js"></script>
	<script type="text/javascript">
	// <![CDATA[
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
			
			function openHarvested(id){
				<%
				StringBuffer buf = new StringBuffer("pick_harvattr.jsp?attr_id=");
				buf.append(attr_id);
				buf.append("&parent_id=");
				buf.append(parent_id);
				buf.append("&parent_type=");
				buf.append(parent_type);
				%>
				
				var link = "<%=buf.toString()%>";
				link = link + "&position=" + document.forms["form1"].elements["position"].value;
				
				attrWindow=window.open(link,
										"HarvestedAttributes",
										"height=400,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
										
				if (window.focus) {attrWindow.focus()}
			}
			
			function doLoad(){
				
				resize();
				
				var attrName = document.forms["form1"].elements["attrName"].value;
				var allowToAdd = document.forms["form1"].elements["allowToAdd"].value;
				if (attrName!=null && (attrName=="SubmitOrganisation" || attrName=="RespOrganisation")){
					if (allowToAdd=="false"){
						if (document.forms["form1"].elements["addbutton"] != null){
							document.forms["form1"].elements["addbutton"].disabled = true;
						}
					}
				}
			}
			
			function resize(){
		    	window.resizeTo(700, 600);
			}
			
	// ]]>
	</script>
	</head>
<body class="popup" onload="doLoad()">
		
<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>

	<%
	
	String backURL = "complex_attrs.jsp?parent_id=" + parent_id +
															 "&amp;parent_type=" + parent_type +
															 "&amp;parent_name=" + parent_name +
															 "&amp;parent_ns=" + parent_ns +
															 "&amp;table_id=" + table_id+
															 "&amp;dataset_id=" + dataset_id;
	if (ds != null)
		backURL = backURL + "&amp;ds=" + ds;
		
	if (mode.equals("add")){
		if (attrFields == null || attrFields.size()==0){
			%>
			<span class="error">No fields found for this attribute!</span></body></html>
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
		<span class="error">Attribute not found!</span><br/>
			<a href="javascript:history.back(-1)">
				<b>&lt; back</b>
			</a></div>
		</body></html>
		<%
		return;
	}
	
	boolean inherit = attribute.getInheritable().equals("0") ? false:true;
	String harvesterID = attribute.getHarvesterID();
	
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
		
<form name="form1" method="post" action="complex_attr.jsp">
		<%
		String hlpScreen = mode.equals("view") ? "complex_attr_view" : "complex_attr_edit";
		%>
		
	<div id="operations">
		<ul>
				<li class="help"><a target="_blank" href="help.jsp?screen=<%=hlpScreen%>&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
<%
if (!mode.equals("view")){
%>
				<li><a href="javascript:window.location.replace('<%=backURL%>')">
				&lt; back to attributes</a></li>
<%
}
%>
			<%
			if (user != null && isWorkingCopy && mode.equals("view")){ %>
				<li><a href="javascript:goTo('edit')">Edit</a></li> <%
			}
			%>
		</ul>
	</div>


<table width="400" class="datatable">
	<tr>
		<th scope="row" class="scope-row">
				<%
				String nsPrefix = "";
				if (parent_type.equals("DS")){
					%>Dataset<%
				}
				else if (parent_type.equals("T")){
					nsPrefix = parent_ns + ":";
					%>Table<%
				}
				else if (parent_type.equals("C")){
					nsPrefix = parent_ns + ":";
					%>Class<%
				}
				else {
					if (ds != null && ds.equals("true")){
						%>Dataset<%
					} else {
						nsPrefix = parent_ns + ":";
						%>Element<%
					}
				}
				%>
			</th>
			<td><%=Util.replaceTags(parent_name)%></td>
	</tr>
	
	<tr>
		<th scope="row" class="scope-row">Attribute</th>
		<td><%=Util.replaceTags(attrName)%></td>
	</tr>
	
</table>

<%
if (!mode.equals("view")){
%>
<div style="margin-left:5px">
<%
	if (user!=null){
		%>
		<input class="smallbutton" type="button" name="addbutton" value="Add"  onclick="submitForm('add')" />&nbsp;
		<input class="smallbutton" type="button" value="Copy" onclick="openValues('<%=attr_id%>')" />&nbsp;
		<%
		if (harvesterID!=null && harvesterID.length()>0){ %>
			<input class="smallbutton" type="button" value="Get"  onclick="openHarvested('<%=attr_id%>')" /><%
		}
	}
	else{
		%>
		<input class="smallbutton" type="button" value="Add" disabled="true"/>
		<%
	}
%>
<table style="border: 1px solid #808080">

	<%
		for (int t=0; attrFields!=null && t<attrFields.size(); t++){			
			Hashtable hash = (Hashtable)attrFields.get(t);
			String id = (String)hash.get("id");
			String name = (String)hash.get("name");
			%>
			<tr>
				<td class="small" width="100" align="right"><b><%=Util.replaceTags(name)%></b>:</td>
				<td>
					<input class="smalltext" type="text" name="<%=AttrFieldsHandler.FLD_PREFIX%><%=id%>"/>
				</td>
			</tr>
			<%
		}
	%>
</table>
</div>
<br/>
<%
}
%>
<div style="margin-left:5px">

	<table cellpadding="0" cellspacing="0">
	
		<tr>
			<%
			if (!mode.equals("view")){
			%>
				<td>
					<%
					if (user!=null && (rows!=null && rows.size()!=0)){
						%>
						<input class="smallbutton" type="button" value="Remove" onclick="submitForm('delete')" />
						<%
					}
					else{
						%>
						<input class="smallbutton" type="button" value="Remove" disabled/>
						<%
					}
					%>
				</td>
				<td width="10">&nbsp;</td>
				<%
			}
			%>
			
			
			<%
			for (int t=0; attrFields!=null && t<attrFields.size(); t++){
				Hashtable hash = (Hashtable)attrFields.get(t);
				String name = (String)hash.get("name");
					String style = "padding-left:5px;padding-right:10px";
					%>
					<th class="small" align="left" style="<%=style%>"><%=Util.replaceTags(name)%></th>
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
						<td class="small" align="right"><%=sInhText%></td>
						<td class="small" width="10">&nbsp;</td>
					<%
			
					for (int t=0; t<attrFields.size(); t++){
						Hashtable hash = (Hashtable)attrFields.get(t);
						String fieldID = (String)hash.get("id");
						String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
						if (fieldValue == null) fieldValue = " ";
							%>
							<td class="small" <% if (displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-left:5;padding-right:10"><%=Util.replaceTags(fieldValue)%></td>
							<%
					}		
					displayed++;
					
					%>
					</tr>				
					<%
				}
			}
		}
		
		int nonInheritedCount = 0;
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
				<td class="small" align="right"><input type="checkbox" style="height:13;width:13" name="del_row" value="<%=row_id%>"/></td>
				<td class="small" width="10">&nbsp;</td>
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
					<td class="small" <% if (displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-left:5;padding-right:10"><%=Util.replaceTags(fieldValue)%></td>
					<%
			}
			displayed++;
			nonInheritedCount++;
			
			%>
			</tr>				
			<%
		}
		%>	

	</table>
</div>

<input type="hidden" name="allowToAdd" value="<%=nonInheritedCount==0%>"/>
<input type="hidden" name="attrName" value="<%=Util.replaceTags(attrName, true)%>"/>

<input type="hidden" name="mode" value="<%=mode%>"/>

<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
<input type="hidden" name="parent_id" value="<%=parent_id%>"/>
<input type="hidden" name="parent_name" value="<%=Util.replaceTags(parent_name, true)%>"/>
<input type="hidden" name="parent_type" value="<%=parent_type%>"/>
<input type="hidden" name="parent_ns" value="<%=parent_ns%>"/>
<input type="hidden" name="table_id" value="<%=table_id%>"/>
<input type="hidden" name="dataset_id" value="<%=dataset_id%>"/>

<input type="hidden" name="position" value="<%=String.valueOf(position)%>"/>

<%
if (ds != null){
	%>
	<input type="hidden" name="ds" value="<%=ds%>"/>
	<%
}
%>
															 
</form>

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
