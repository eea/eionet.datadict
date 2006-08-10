<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private String mode=null;%>
<%!private Hashtable attrField=null;%>

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
			
			request.setCharacterEncoding("UTF-8");
			
			ServletContext ctx = getServletContext();	
			
			XDBApplication.getInstance(ctx);
			AppUserIF user = SecurityUtil.getUser(request);
			
			String appName = ctx.getInitParameter("application-name");

			
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
			
			String field_id = request.getParameter("field_id");
			
			String attr_name = request.getParameter("attr_name");			
			String attr_id = request.getParameter("attr_id");			

			if (attr_id == null || attr_id.length()==0){ %>
				<b>Attribute ID is missing!</b> <%
				return;
			}
			if (field_id == null || field_id.length()==0){ %>
				<b>Attribute field ID is missing!</b> <%
				return;
			}
			
			if (attr_name == null) attr_name = "?";
			

			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			

			
			if (request.getMethod().equals("POST")){

				Connection userConn = null;
								
				try{
					userConn = user.getConnection();
					
					MAttrFieldsHandler handler = new MAttrFieldsHandler(userConn, request, ctx);
					
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
				String redirUrl=null;
				if (mode.equals("delete")){
					String	deleteUrl = history.gotoLastNotMatching("m_attr_field.jsp");
					redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
					//redirUrl = "" +
					//				"/m_attr_fields.jsp?mode=edit&attr_id=" + attr_id + "&attr_name=" + attr_name;
				}
				else {
					redirUrl=currentUrl;
					//redirUrl = "" +
					//				"/m_attr_field.jsp?mode=edit&attr_id=" + attr_id + "&attr_name=" + attr_name + "&field_id=" + field_id;
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
			
			attrField = searchEngine.getAttrField(field_id);		
			if (attrField == null) attrField = new Hashtable();
			String disabled = user == null ? "disabled='disabled'" : "";
			
			String name = (String)attrField.get("name");
			String definition = (String)attrField.get("definition");
			String priority = (String)attrField.get("priority");

			%>

<html>
	<head>
		<%@ include file="headerinfo.txt" %>
		<title>Meta</title>
		<script language="javascript" src='script.js' type="text/javascript"></script>
		<script language="javascript" type="text/javascript">
		// <![CDATA[
	
			function submitForm(mode){
				
				if (mode == "delete"){
					var b = confirm("This value will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;
				}
				
				
				document.forms["form1"].elements["mode"].value = mode;
				document.forms["form1"].submit();
			}
			function openPriority(){
				alert("Click the checkbox, if the field has high priority. Otherwise it has low priority.");
			}
			function onLoad(){
				<%
					if (priority != null){
	    			%>
						var pri = '<%=priority%>';
						var o = document.forms["form1"].priority;
						for (i=0; o!=null && i<o.options.length; i++){
							if (o.options[i].value == pri){
								o.selectedIndex = i;
								break;
							}
						}			
					<% 
					}
				%>
			}
			
		// ]]>
		</script>
	</head>
<body onload="onLoad()">
	<jsp:include page="nlocation.jsp" flush='true'>
		<jsp:param name="name" value="Allowable value"/>
		<jsp:param name="back" value="true"/>
	</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
	<%
	String backURL = "" + "/m_attr_fields.jsp?attr_id=" + attr_id + "&attr_name=" + attr_name;
	%>
	<form name="form1" method="post" action="m_attr_field.jsp">
  <div id="operations">
    <ul>
        <li class="help"><a target="_blank" href="help.jsp?screen=complex_attr_field&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
			<!-- li>
					<a href="javascript:window.location.replace('<%=backURL%>')">&lt; back to allowable values list</a>
			</li -->
    </ul>
  </div>

	<h1>Field of <em><%=Util.replaceTags(attr_name)%></em> attribute</h1>
			
			<table class="datatable">
			<tr>				
				<th scope="row" class="scope-row">
					Field name
				</th>
				<td>
					<%=Util.replaceTags(name)%>
				</td>
			</tr>
			<tr>				
				<th scope="row" class="scope-row">
					Definition
				</th>
				<td>
					<textarea <%=disabled%> class="small" rows="5" cols="60" name="definition"><%=Util.replaceTags(definition, true, true)%></textarea>
				</td>
			</tr>
			<tr>
				<th scope="row" class="scope-row">
					<!--a href="javascript:openPriority()"><span class="help">?</span></a>&#160;-->
					Priority
				</th>
				<td>
					<select <%=disabled%> name="priority" class="small">
						<option value="<%=DElemAttribute.FIELD_PRIORITY_HIGH%>">High</option>
						<option value="<%=DElemAttribute.FIELD_PRIORITY_LOW%>">Low</option>
					</select>
				</td>
			</tr>
			<%
			Vector harvFlds = searchEngine.getHarvesterFieldsByAttr(attr_id, false);
			if (harvFlds!=null && harvFlds.size()>0){
				%>
				<tr>
					<th scope="row" class="scope-row">
						Linked harvester field
					</th>
					<td>
						<%
						String harvFld = (String)attrField.get("harv_fld");
						String noLinkSelected = Util.voidStr(harvFld) ? "selected='selected'" : "";
						%>
						<select <%=disabled%> name="harv_fld" class="small">
							<option <%=noLinkSelected%> value="null">-- no link --</option>
							<%
							if (!Util.voidStr(harvFld)){ %>
								<option selected value="<%=Util.replaceTags(harvFld, true)%>"><%=Util.replaceTags(harvFld)%></option><%
							}
							
							for (int i=0; harvFlds!=null && i<harvFlds.size(); i++){
								String _harvFld = (String)harvFlds.get(i);
								%>
								<option value="<%=Util.replaceTags(_harvFld, true)%>"><%=Util.replaceTags(_harvFld)%></option> <%
							}
							%>
						</select>
					</td>
				</tr><%
			}
			%>
		
	</table>
		
				<% 
					if (user==null){ %>									
						<input class="mediumbuttonb" type="button" value="Save" disabled="disabled"/>&#160;&#160;
						<input class="mediumbuttonb" type="button" value="Delete" disabled="disabled"/>&#160;&#160;
					<%} else {%>
						<input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
						<input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;
					<% }
				%>
				
	<input type="hidden" name="mode" value="<%=mode%>"/>
	<input type="hidden" name="field_id" value="<%=field_id%>"/>
	<input type="hidden" name="del_field" value="<%=field_id%>"/>
	<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
	<input type="hidden" name="attr_name" value="<%=Util.replaceTags(attr_name, true)%>"/>
	
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
