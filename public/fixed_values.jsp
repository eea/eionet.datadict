<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final int MAX_CELL_LEN=70;%>

<%@ include file="history.jsp" %>

<%
	Vector fixedValues = null;
	String mode = null;
	
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
	String delem_id = request.getParameter("delem_id");
	if (delem_id == null || delem_id.length()==0){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: delem_id");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	mode = request.getParameter("mode");
	if (mode == null || mode.length()==0)
		mode = "view";
	if (!mode.equals("view") && user==null){
		request.setAttribute("DD_ERR_MSG", "Mode not allowed for anonymous users: " + mode);
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	
	String parent_type = request.getParameter("parent_type");
	if (parent_type == null)
		parent_type = "CH1";
	else if (!parent_type.equals("CH1") && !parent_type.equals("CH2") && !parent_type.equals("attr")){
		request.setAttribute("DD_ERR_MSG", "Unknown parent_type: " + parent_type);
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
	
	//// handle the POST request//////////////////////
	//////////////////////////////////////////////////
	if (request.getMethod().equals("POST")){
		
		Connection userConn = null;
		try{
			userConn = user.getConnection();
			FixedValuesHandler handler = new FixedValuesHandler(userConn, request, ctx);
			try{
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
		
		response.sendRedirect(currentUrl);
		return;
	}
	//// end of handle the POST request//////////////////////
	
	Connection conn = null;
	boolean canEdit = false;
	boolean isBooleanDatatype = false;
	
	// the whole page's try block
	try {	
		conn = ConnectionUtil.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		
		// if fixed values of element, see if working copy and who is working user
		if (parent_type.equals("elem")){
			String workingUser = null;
			boolean isWorkingCopy = false;
			DataElement dataElement = searchEngine.getDataElement(delem_id);
			if (dataElement==null){
				request.setAttribute("DD_ERR_MSG", "Data element not found with this ID: " + delem_id);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			else{
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
			}
			
			if (isWorkingCopy && workingUser==null){
				request.setAttribute("DD_ERR_MSG", "Working copy with no working user");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			
			if (mode.equals("view"))
				canEdit = user!=null && isWorkingCopy && workingUser.equals(user.getUserName());
			else{
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
		// end if fixed values of element, see if working copy and who is working user
		
		fixedValues = searchEngine.getFixedValues(delem_id, parent_type);
		if (fixedValues == null)
			fixedValues = new Vector();
				
		//find parent url from history
		String parentUrl="";
		if (parent_type.equals("elem")){
			parentUrl="data_element.jsp?mode=view&amp;delem_id="+delem_id;
			if (history!=null){
				String elemUrl = history.getLastMatching("data_element.jsp");
			
				if (elemUrl.indexOf("delem_id=" + delem_id)>-1)
					parentUrl = elemUrl;
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
		
		String strFormDisabled = isBooleanDatatype ? "disabled=\"disabled\"" : "";
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Data Dictionary - Fixed values</title>
	<script type="text/javascript">
	// <![CDATA[
	
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
		
		function importCodes(){
			var url = "import.jsp?mode=FXV&delem_id=<%=delem_id%>&short_name=<%=delem_name%>";
			document.location.assign(url);
		}
		
		function selectAll(){
			
			var checks = document.forms["form1"].elements["del_id"];
			for (var i=0; checks!=null && i<checks.length; i++){
				checks[i].checked=true;
			}
		}
	// ]]>
	</script>
</head>

<%
String hlpScreen = valsType.equals("CH1") ? "fixed_values" : "suggested_values";
hlpScreen = mode.equals("view") ? hlpScreen + "_view" : hlpScreen + "_edit";
%>
<body>
<div id="container">
	<%
	if (valsType.equals("CH1")){ %>
        <jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Allowable values"/>
            <jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
        </jsp:include><%
    }
    else{ %>
    	<jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Suggested values"/>
            <jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
        </jsp:include><%
	}
	%>            
    <%@ include file="nmenu.jsp" %>
	<div id="workarea">
		
		<h1>
			<%=Util.replaceTags(initCaseTitle)%> values of <a href="<%=Util.replaceTags(parentUrl, true)%>"><%=Util.replaceTags(delem_name, true)%></a> <%=dispParentType%>
		</h1>
		
		<form id="form1" method="post" action="fixed_values.jsp">
			<%
			if (mode.equals("view") && canEdit){ %>
				<table width="600">
					<tr>
						<td colspan="2" align="right">
							<input type="button" class="smallbutton" value="Edit" onclick="edit()"/>
						</td>
					</tr>
				</table><%
			}
			else if (!mode.equals("view")){
				String text = "Enter a new value here:";
				if (isBooleanDatatype){
					StringBuffer buf = new StringBuffer("<strong>NB!</strong> Adding/removing values of boolean data element is disabled, since they are limited to 'true' and 'false' by definition. ");
					buf.append("However, you can change the values' definitions by clicking on the value.<br/><br/>");
					text = buf.toString();
				}
				%>
				<table width="600">
					<tr style="height:10px"><td colspan="2"><%=text%></td></tr>
					<tr>
						<td colspan="1" style="width:300px">
							<input class="smalltext" type="text" size="20" name="new_value" <%=strFormDisabled%>/>
							<input class="smallbutton" type="button" value="Add" onclick="submitForm('add')" <%=strFormDisabled%>/>&nbsp;
							<%
							if (!valsType.equals("AGG")){ %>
								<input class="smallbutton" type="button" value="Import..." onclick="importCodes()" <%=strFormDisabled%>/><%
							}
							%>
						</td>
					</tr>
				</table><%
			}
			
			if (mode.equals("view")){%>
				<table class="datatable">
					<thead>
					<tr>
						<th scope="col" class="scope-col">Value</th>
						<th scope="col" class="scope-col">Definition</th>
						<th scope="col" class="scope-col">ShortDescription</th>
					</tr>
					</thead>
					<tbody>
					<%
					for (int i=0; i<fixedValues.size(); i++){
						FixedValue fxv = (FixedValue)fixedValues.get(i);
						String value = fxv.getValue();
						String fxvID = fxv.getID();
						int level=fxv.getLevel();
						String fxvAttrValue = null;
						String fxvAttrValueShort = null;
						String spaces="";
						for (int j=1; j<level; j++){
							spaces +="&nbsp;&nbsp;&nbsp;";
						}
						
						String definition = fxv.getDefinition();
						definition = definition==null ? "" : definition;
						definition = definition.length()>MAX_CELL_LEN ? definition.substring(0,MAX_CELL_LEN) + "..." : definition;
						
						String shortDesc = fxv.getShortDesc();
						shortDesc = shortDesc==null ? "" : shortDesc;
						shortDesc = shortDesc.length()>MAX_CELL_LEN ? shortDesc.substring(0,MAX_CELL_LEN) + "..." : shortDesc;
						%>
						<tr <% if (i % 2 != 0) %> class="zebraeven" <% else %> class="zebraodd" <%; %>>
							<td>
								<%=spaces%>
								<a href="fixed_value.jsp?fxv_id=<%=fxvID%>&amp;mode=<%=mode%>&amp;delem_id=<%=delem_id%>&amp;delem_name=<%=Util.replaceTags(delem_name)%>&amp;parent_type=<%=typeParam%>">
									<%=Util.replaceTags(value)%>
								</a>
							</td>
							<td>
								<%=Util.replaceTags(definition)%>
							</td>
							<td>
								<%=Util.replaceTags(shortDesc)%>
							</td>				
						</tr><%
					}
					%>
					</tbody>
				</table><%
			}
			else {%>
				<table cellspacing="1" cellpadding="1">
					<thead>
						<tr>
							<td align="left" colspan="3">
								<input class="smallbutton" type="button" value="Remove selected" onclick="submitForm('delete')" <%=strFormDisabled%>/>
								<input class="smallbutton" type="button" value="Remove all" onclick="submitForm('delete_all')" <%=strFormDisabled%>/>
							</td>
						</tr>
						<tr>
							<td colspan="3">&nbsp;</td>
						</tr>
						<tr>
							<th style="font-weight:bold;color:#FFFFFF;background-color:#666666;">&nbsp;</th>
							<th style="font-weight:bold;color: #FFFFFF;background-color:#666666;width:100px">Value</th>
							<th style="font-weight:bold;color: #FFFFFF;background-color:#666666;width:500px">Definition</th>
						</tr>
					</thead>
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
						String trStyle = (i % 2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
						%>
						<tr <%=trStyle%>>
							<td align="right" valign="top" style="padding-right:10">
								<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=fxvID%>" <%=strFormDisabled%>/>
							</td>
							<td valign="bottom" align="left" style="padding-left:5;padding-right:10">
								<b><a href="fixed_value.jsp?fxv_id=<%=fxvID%>&amp;mode=edit&amp;delem_id=<%=delem_id%>&amp;delem_name=<%=Util.replaceTags(delem_name)%>&amp;parent_type=<%=typeParam%>">
									<%=Util.replaceTags(value)%>
								</a></b>&nbsp;
							</td>
							<td align="left" style="padding-left:5;padding-right:10" title="Definition">
								<%=Util.replaceTags(definition)%>
							</td>
							<td>
								<input type="hidden" name="pos_id" value="<%=fxvID%>" size="5" />
								<input type="hidden" name="oldpos_<%=fxvID%>" value="<%=fxv.getPosition()%>" size="5" />
								<input type="hidden" name="pos_<%=fxvID%>" value="0" size="5" />
							</td>
						</tr><%
					}
					%>			
					</tbody>
				</table><%
			}
			%>
			<div style="display:none">
				<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
				<input type="hidden" name="delem_name" value="<%=Util.replaceTags(delem_name)%>"/>
				<input type="hidden" name="parent_type" value="<%=typeParam%>"/>
				<input type="hidden" name="mode" value="<%=mode%>"/>
				<input type="hidden" name="changed" value="0"/>
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
	} catch (SQLException e) {
	}
}
%>
