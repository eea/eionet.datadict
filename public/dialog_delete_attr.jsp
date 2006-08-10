<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%!private Vector objects=null;%>
<%!
ServletContext ctx = null;
%>

<%@ include file="history.jsp" %>

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	
	String attr_id = request.getParameter("attr_id");
	String type = request.getParameter("type");
    String short_name = request.getParameter("short_name");

	if (request.getMethod().equals("POST")){
    	if (user == null){
	    	%><html><body><h1>Error</h1><b>Not authorized to post any data!</b></body></html><%
	      	return;
      	}
		Connection userConn = null;
		String redirUrl = "";
				
		try{
			userConn = user.getConnection();

			AttributeHandler handler = new AttributeHandler(userConn, request, ctx);
			handler.setUser(user);
			handler.execute();

			String	deleteUrl = history.gotoLastMatching("attributes.jsp");
			redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}
				
		response.sendRedirect(redirUrl);
		return;
	}

	if (attr_id==null || type==null){
    	%><html><body><h1>Error</h1><b>Attribute type or attribute id is not specified!</b></body></html><%
		return;
	}
	
	ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");

	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	try { // start the whole page try block
	
		conn = pool.getConnection();

		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		
		objects = searchEngine.getAttributeObjects(attr_id, type);
		
		
%>

<html>
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Data Dictionary</title>
	<script language="javascript" src='script.js' type="text/javascript"></script>
	<script language="javascript" type="text/javascript">
	// <![CDATA[
		function deleteAttr(){
				
			document.forms["form1"].submit();
		}
		
		function cancel(){
			back = "<%=history.getBackUrl()%>";
			document.location.assign(back);
		}
	// ]]>
	</script>

</head>
<body>
		<jsp:include page="nlocation.jsp" flush='true'>
			<jsp:param name="name" value="Attribute"/>
			<jsp:param name="back" value="true"/>
		</jsp:include>
	<%@ include file="nmenu.jsp" %>

<div id="workarea">
	<form name="form1" action="dialog_delete_attr.jsp" method="post">
	<h1>Deleting attribute: <%=Util.replaceTags(short_name)%></h1>
	<p>Are you sure you want to delete the attribute, because it is part of the following objects' definitions: </p>
				<table width="500">
					<% 
					// DATASETS
					int d=0;
					if (objects!=null){
						%>
						<%
						for (int i=0; i<objects.size(); i++){
				
							Hashtable object = (Hashtable)objects.get(i);
					
							String parent_type =(String)object.get("parent_type");
							String parent_id =(String)object.get("parent_id");
							String parent_name =(String)object.get("parent_name");
							
							String type_name="";
							String link="";
							String version="";
							if (parent_type.equals("DS")){
								type_name="Dataset";
								link="dataset.jsp?mode=view&ds_id=" + parent_id;
							}
							else if (parent_type.equals("T")){
								type_name="Table";
								link="dstable.jsp?mode=view&table_id=" + parent_id;
							}
							else if (parent_type.equals("CSI")){
								type_name="Allowable value";
								String comp_id = (String)object.get("component_id");
								String comp_type = (String)object.get("component_type");
								link="fixed_value.jsp?mode=view&fxv_id=" + parent_id+ "&delem_id=" + comp_id + "&parent_type=" + comp_type;
							}
							else if (parent_type.equals("E")){
								type_name="Data element";
								link="data_element.jsp?mode=view&delem_id=" + parent_id;
							}
							if (!parent_type.equals("CSI")){
								version = "version: " + (String)object.get("version");
							}
							d++;
							%>
				
							<tr valign="top">					
								<td align="left" style="padding-left:5;padding-right:10" <% if (d % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
									<%=type_name%>:&#160;
									<a href="<%=link%>">
									<%=Util.replaceTags(parent_name)%></a>&#160;<%=version%>
								</td>
							</tr>
						<%
						}
					}
					%>
					
					<tr><td align="left">&#160;</td></tr>
					<tr style="height:30px;"><td><b>!Be aware that if you delete this attribute, it affects all these objects' definitions.</b>
					</td></tr>
					<tr style="height:30px;"><td align="left">
						<input type="button" onclick="cancel();" value="Cancel" class="mediumbuttonb"/>
						<input type="button" onclick="deleteAttr();" value="Delete" class="mediumbuttonb"/>
					</td></tr>
				</table>

				<input type="hidden" name="mode" value="delete"/>
				<input type="hidden" name="type" value="<%=type%>"/>
				<%			
				if (type!=null && type.equals(DElemAttribute.TYPE_SIMPLE)){
					%>
					<input type="hidden" name="simple_attr_id" value="<%=attr_id%>"/>
					<%
				}
				else{
					%>
					<input type="hidden" name="complex_attr_id" value="<%=attr_id%>"/>
					<%							
				}
				%>				
				<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
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
