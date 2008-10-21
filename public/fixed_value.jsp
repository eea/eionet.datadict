<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

<%
	String mode=null;
	FixedValue fxv=null;

	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
	response.setHeader("Expires", Util.getExpiresDateString());

	request.setCharacterEncoding("UTF-8");
		
	ServletContext ctx = getServletContext();
	DDUser user = SecurityUtil.getUser(request);	
	
	// POST request not allowed for anybody who hasn't logged in			
	if (request.getMethod().equals("POST") && user==null){
		request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	
	// get vital request parameters
	String fxv_id = request.getParameter("fxv_id");
	String delem_id = request.getParameter("delem_id");
	if (delem_id == null || delem_id.length()==0){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: delem_id");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	String parent_type = request.getParameter("parent_type");
	if (parent_type == null)
		parent_type = "CH1";
	else if (!parent_type.equals("CH1") && !parent_type.equals("CH2") && !parent_type.equals("attr")){
		request.setAttribute("DD_ERR_MSG", "Unknown parent type: " + parent_type);
		request.getRequestDispatcher("error.jsp").forward(request, response);
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
				
	mode = request.getParameter("mode");
	if (mode == null || mode.length()==0) {
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: mode");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	if (!mode.equals("add") && (fxv_id == null || fxv_id.length()==0)){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: fxv_id");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	if (!mode.equals("view") && user==null){
		request.setAttribute("DD_ERR_MSG", "Mode not allowed for anonymous users: " + mode);
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}

	//// handle the POST request//////////////////////
	//////////////////////////////////////////////////
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
				String msg = e.getMessage();					
				ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(bytesOut));
				String trace = bytesOut.toString(response.getCharacterEncoding());					
				request.setAttribute("DD_ERR_MSG", msg);
				request.setAttribute("DD_ERR_TRC", trace);
				String backLink = request.getParameter("submitter_url");
				if (backLink==null || backLink.length()==0)
					backLink = history.getBackUrl();
				request.setAttribute("DD_ERR_BACK_LINK", backLink);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}
		// dispatch the POST request		
		if (mode.equals("edit"))
			redirUrl=currentUrl;
		else if (mode.equals("delete")){
			String deleteUrl = history.gotoLastNotMatching("fixed_value.jsp");
			if (deleteUrl!=null && deleteUrl.length()>0) 
				redirUrl=deleteUrl;
			else 
				redirUrl = redirUrl + "fixed_values.jsp?mode=edit&delem_id=" + delem_id +
													 "&delem_name=" + delem_name +
													 "&parent_type=" + parent_type;
		}
		response.sendRedirect(redirUrl);
		return;
	}
	//// end of handle the POST request //////////////////////

	Connection conn = null;
	boolean isBooleanDatatype = false;
	
	// the whole page's try block
	try {
		conn = ConnectionUtil.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		
		// make sure this a working copy of the current user
		if (parent_type.equals("elem") && !mode.equals("view")){
			DataElement dataElement = searchEngine.getDataElement(delem_id);
			if (dataElement==null){
				request.setAttribute("DD_ERR_MSG", "Data element not found with this ID: " + delem_id);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			else{
				boolean isWorkingCopy = false;
				String workingUser = null;
				if (dataElement.isCommon() && dataElement.isWorkingCopy()){
					isWorkingCopy = true;
					workingUser = dataElement.getWorkingUser();
				}
				else if (!dataElement.isCommon()){
					String dstID = dataElement.getDatasetID();
					if (dstID==null){
						request.setAttribute("DD_ERR_MSG", "Missing dataset ID in this element: " + delem_id);
						request.getRequestDispatcher("error.jsp").forward(request, response);
						return;
					}
					else{
						Dataset dst = searchEngine.getDataset(dstID);
						if (dst==null){
							request.setAttribute("DD_ERR_MSG", "Dataset not found with this ID: " + dstID);
							request.getRequestDispatcher("error.jsp").forward(request, response);
							return;
						}
						else if (dst.isWorkingCopy()){
							isWorkingCopy = true;
							workingUser = dst.getWorkingUser();
						}
					}
				}
				if (!isWorkingCopy){
					request.setAttribute("DD_ERR_MSG", "Mode not allowed for non-working copy: " + mode);
					request.getRequestDispatcher("error.jsp").forward(request, response);
					return;
				}
				else if (workingUser==null){
					request.setAttribute("DD_ERR_MSG", "Working copy with no working user");
					request.getRequestDispatcher("error.jsp").forward(request, response);
					return;
				}
				else if (!workingUser.equals(user.getUserName())){
					request.setAttribute("DD_ERR_MSG", "Not your working copy");
					request.getRequestDispatcher("error.jsp").forward(request, response);
					return;
				}
				
				String dataType = dataElement.getAttributeValueByShortName("Datatype");
				isBooleanDatatype = dataType!=null && dataType.equals("boolean");
			}
		}
		// end of make sure this a working copy of the current user
		
		String value = "";
		if (!mode.equals("add")){
			fxv = searchEngine.getFixedValue(fxv_id);
			if (fxv!=null)
				value = fxv.getValue();
			else{
				request.setAttribute("DD_ERR_MSG", "No value found with this ID: " + fxv_id);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
		}
			
		//find parent url from history
		String parentUrl="";
		if (parent_type.equals("elem")){
			parentUrl="data_element.jsp?mode=view&amp;delem_id="+delem_id;
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
			parentUrl="delem_attribute.jsp?attr_id=" + delem_id + "&amp;type=SIMPLE&amp;mode=" + mode;
			if (history!=null){
				String attrUrl = history.getLastMatching("delem_attribute.jsp");
			
				if (attrUrl.indexOf("delem_id=" + delem_id)>-1)
					parentUrl = attrUrl;
			}
		}
		
		String strDeleteDisabled = isBooleanDatatype ? "disabled=\"disabled\"" : "";
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Data Dictionary - Fixed value</title>
	<script type="text/javascript">
	// <![CDATA[

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

			
	// ]]>
	</script>
</head>
<body>
<div id="container">
<%
if (valsType.equals("CH1")){ %>
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Allowable value"/>
        <jsp:param name="helpscreen" value="fixed_value"/>
    </jsp:include><%
}
else{ %>
	<jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Suggested value"/>
        <jsp:param name="helpscreen" value="suggested_value"/>
    </jsp:include><%
}
%>            
<%@ include file="nmenu.jsp" %>
<div id="workarea">

    	<h1>
    		<%=Util.replaceTags(initCaseTitle)%> value of <a href="<%=Util.replaceTags(parentUrl, true)%>"><%=Util.replaceTags(delem_name, true)%></a> <%=dispParentType%>
    	</h1>
			
		<form id="form1" method="post" action="fixed_value.jsp">
		
		<%
		String strMandatory = mode.equals("view") ? "" : "<img src=\"images/mandatory.gif\" alt=\"Mandatory\" title=\"Mandatory\"/>";
		String strOptional  = mode.equals("view") ? "" : "<img src=\"images/optional.gif\" alt=\"Optional\" title=\"Optional\"/>";
		String strTableClass = " class=\"datatable\"";
		%>
	
		<table cellspacing="0" class="datatable" style="width:auto">
			<tr>				
				<th scope="row">Value:</th>
				<td><%=strMandatory%></td>
				<td>
					<%
					if(!mode.equals("add")){ %>
						<em><%=Util.replaceTags(value)%></em>
						<input type="hidden" name="fxv_value" value="<%=Util.replaceTags(value, true)%>"/><%
					}
					else{ %>
						<input class="smalltext" type="text" size="30" name="fxv_value"/><%
					}
					%>
				</td>
			</tr>
			
			<%
			if (parent_type.equals("attr")){ %>
				<tr>				
					<th scope="row">Default:</th>
					<td><%=strOptional%></td>
					<td>
						<%
						String strDefault = null;
						if (fxv!=null)
							strDefault = fxv.getDefault() ? "Yes" : "No";
						if (mode.equals("view")){ %>
							<%=strDefault%><%
						}
						else{ %>
							<select class="small" name="is_default">
								<%
								if (fxv==null || fxv.getDefault()==false){ %>
									<option selected="selected" value="false">No</option>
									<option value="true">Yes</option><%
								}
								else if (fxv!=null && fxv.getDefault()==true){ %>
									<option value="false">No</option>
									<option selected="selected" value="true">Yes</option><%
								}
								%>
							</select><%
						}
						%>
					</td>
				</tr>
				<%
			}
			%>
			
			<tr>
				<th scope="row">Definition:</th>
				<td><%=strOptional%></td>
				<td>
					<%
					if (mode.equals("view")){ %>
						<%=Util.replaceTags(fxv.getDefinition(), true, true)%><%
					}
					else{ %>
						<textarea class="small" rows="3" cols="60" name="definition"><%=Util.replaceTags(fxv.getDefinition(), true, true)%></textarea><%
					}
					%>
				</td>
			</tr>			
			<tr>
				<th scope="row">Short description:</th>
				<td><%=strOptional%></td>
				<td>
					<%
					if (mode.equals("view")){ %>
						<%=Util.replaceTags(fxv.getShortDesc(), true, true)%><%
					}
					else{ %>
						<textarea class="small" rows="3" cols="60" name="short_desc"><%=Util.replaceTags(fxv.getShortDesc(), true, true)%></textarea><%
					}
					%>
				</td>
			</tr>		
			<%
			if (mode.equals("edit")){ %>
				<tr>
					<td colspan="2">&nbsp;</td>
					<td>
						<input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&nbsp;&nbsp;
						<input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')" <%=strDeleteDisabled%>/>&nbsp;&nbsp;
					</td>
				</tr><%
			}
			%>
	</table>
	
	<div style="display:none">
		<input type="hidden" name="mode" value="<%=mode%>"/>
		<input type="hidden" name="fxv_id" value="<%=fxv_id%>"/>
		<input type="hidden" name="del_id" value="<%=fxv_id%>"/>
		<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
		<input type="hidden" name="delem_name" value="<%=Util.replaceTags(delem_name, true)%>"/>
		<input type="hidden" name="parent_type" value="<%=Util.replaceTags(valsType, true)%>"/>
	</div>	
	</form>
</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.txt" %>
</body>
</html>

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e){}
}
%>
