<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

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
			
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.addHeader("Cache-Control", "no-store");
			response.setDateHeader("Expires", 0);
			
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
	      				<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	      				<body>
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
						<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"><body><h1>Error</h1><p><%=e.toString()%></p></body></html>
						<%
						return;
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				
				if (mode.equals("delete"))
					redirUrl = redirUrl + "&wasdel=true";
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
			
			String backURL = "complex_attrs.jsp?parent_id=" + parent_id +
															 "&parent_type=" + parent_type +
															 "&parent_name=" + parent_name +
															 "&parent_ns=" + parent_ns +
															 "&table_id=" + table_id+
															 "&dataset_id=" + dataset_id;
			String backURLEscaped = "complex_attrs.jsp?parent_id=" + parent_id +
														 "&amp;parent_type=" + parent_type +
														 "&amp;parent_name=" + parent_name +
														 "&amp;parent_ns=" + parent_ns +
														 "&amp;table_id=" + table_id+
														 "&amp;dataset_id=" + dataset_id;
			if (ds != null){
				backURL = backURL + "&ds=" + ds;
				backURLEscaped = backURLEscaped + "&amp;ds=" + ds;
			}
			
			String wasDelete = request.getParameter("wasdel");
			if (wasDelete!=null && attribute==null){
				response.sendRedirect(backURL);
				return;
			}

			%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<%@ include file="headerinfo.jsp" %>
		<title>Complex attribute</title>
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
				document.forms["form1"].action = "pick_attrvalue.jsp";
				document.forms["form1"].submit();
			}
			
			function openHarvested(id){
				document.forms["form1"].action = "pick_harvattr.jsp";
				document.forms["form1"].submit();
			}
			
			function doLoad(){
				
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
			
	// ]]>
	</script>
	</head>

<%
String hlpScreen = mode.equals("view") ? "complex_attr_view" : "complex_attr_edit";
%>

<body onload="doLoad()">
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Complex attribute"/>
		<jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
	</jsp:include>
<%@ include file="nmenu.jsp" %>

<div id="workarea">

	<%
	if (mode.equals("add")){
		if (attrFields == null || attrFields.size()==0){
			%>
			<span class="error">No fields found for this attribute!</span></body></html>
			<%
			return;
		}
	}
	else if (attribute == null){
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
		
<form id="form1" method="post" action="complex_attr.jsp">
		
	<%
	if (!mode.equals("view") || (user != null && isWorkingCopy && mode.equals("view"))){
		%>
		<div id="operations">
			<ul>
				<%
				if (!mode.equals("view")){ %>
					<li><a href="javascript:window.location.replace('<%=backURLEscaped%>')">&lt; back to attributes</a></li><%
				}
				if (user != null && isWorkingCopy && mode.equals("view")){ %>
					<li><a href="javascript:goTo('edit')">Edit</a></li> <%
				}
				%>
			</ul>
		</div><%
	}

StringBuffer parentLink = new StringBuffer();
String dispParentType = request.getParameter("parent_type");
if (dispParentType==null)
	dispParentType = "";
else if (dispParentType.equals("DS")){
	dispParentType = "dataset";
	parentLink.append("dataset.jsp?ds_id=");
}
else if (dispParentType.equals("T")){
	dispParentType = "table";
	parentLink.append("dstable.jsp?table_id=");
}
else if (dispParentType.equals("E")){			
	dispParentType = "element";
	parentLink.append("data_element.jsp?delem_id=");
}

String dispParentName = request.getParameter("parent_name");
if (dispParentName==null)
	dispParentName = "";

if (parentLink.length()>0)
	parentLink.append(request.getParameter("parent_id")).append("&amp;mode=edit");	
%>

<h1 style="margin-bottom:20px">Complex attribute <a href=""><%=attrName%></a> of <%=dispParentType%> <a href="<%=parentLink%>"><%=dispParentName%></a></h1>

	<%
	if (!mode.equals("view")){
		%>
		<div>
			<%
			if (user!=null){
				%>
				<input class="smallbutton" type="button" name="addbutton" value="Add" onclick="submitForm('add')" />
				<input class="smallbutton" type="button" value="Copy" onclick="openValues('<%=attr_id%>')" />
				<%				
				if (harvesterID!=null && harvesterID.length()>0){ %>
					<input class="smallbutton" type="button" value="Get harvested"  onclick="openHarvested('<%=attr_id%>')" /><%
				}
			}
			else{
				%>
				<input class="smallbutton" type="button" value="Add" disabled="disabled"/><%
			}
			%>
		</div>
	
	<table class="datatable" cellspacing="0" cellpadding="0">
	
		<%
		for (int t=0; attrFields!=null && t<attrFields.size(); t++){			
			Hashtable hash = (Hashtable)attrFields.get(t);
			String id = (String)hash.get("id");
			String name = (String)hash.get("name");
			String givenValue = request.getParameter(AttrFieldsHandler.FLD_PREFIX + id);
			if (givenValue==null)
				givenValue = "";
			%>
			<tr>
				<th style="padding:0;text-align:right"><%=Util.replaceTags(name)%>:&nbsp;</th>
				<td style="padding:0">
					<input class="smalltext" type="text" name="<%=AttrFieldsHandler.FLD_PREFIX%><%=id%>" value="<%=givenValue%>"/>
				</td>
			</tr>
			<%
		}
		%>
	</table><%
} // if (!mode.equals("view"))
%>
	<div style="overflow:auto">
	<table cellpadding="0" cellspacing="0" class="datatable">
	
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
						<input class="smallbutton" type="button" value="Remove" disabled="disabled"/>
						<%
					}
					%>
				</td>
				<td style="width:10px">&nbsp;</td>
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
						<td style="width:10px">&nbsp;</td>
					<%
			
					for (int t=0; t<attrFields.size(); t++){
						Hashtable hash = (Hashtable)attrFields.get(t);
						String fieldID = (String)hash.get("id");
						String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);						
						if (fieldValue == null) fieldValue = " ";
							String tdStyle = "padding-left:5;padding-right:10";
							if (displayed%2 != 0)
								tdStyle = tdStyle + ";background-color:#D3D3D3;";
							%>
							<td style="<%=tdStyle%>"><%=Util.replaceTags(fieldValue)%></td>
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
				<td style="width:10px">&nbsp;</td>
			<%
			}
			%>
			<%
			
			for (int t=0; t<attrFields.size(); t++){
				Hashtable hash = (Hashtable)attrFields.get(t);
				String fieldID = (String)hash.get("id");
				String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
				if (fieldValue == null) fieldValue = " ";
					String tdStyle = "padding-left:5;padding-right:10";
					if (displayed % 2 != 0)
						tdStyle = tdStyle + ";background-color:#D3D3D3;";
					%>
					<td style="<%=tdStyle%>"><%=Util.replaceTags(fieldValue)%></td>
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

<div style="display:none">
	<input type="hidden" name="allowToAdd" value="<%=nonInheritedCount==0%>"/>
	<input type="hidden" name="attrName" value="<%=attrName%>"/>
	
	<input type="hidden" name="mode" value="<%=mode%>"/>
	<input type="hidden" name="type" value="COMPLEX"/>
	
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
	
	String qryStr = request.getQueryString();
	if (qryStr!=null && qryStr.length()>0){
		int i = qryStr.indexOf(AttrFieldsHandler.FLD_PREFIX);
		if (i>0)
			qryStr = qryStr.substring(0,i);
	}
	%>
	
	<input type="hidden" name="requester_qrystr" value="<%=Util.replaceTags(qryStr, true, true)%>" />
	<input type="hidden" name="requester_redir_url" value="<%=Util.replaceTags(redirUrl, true, true)%>" />
	
</div>
</form>
</div>
</div> <!-- container -->
<jsp:include page="footer.jsp" flush="true"/>
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
