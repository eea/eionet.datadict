<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*,eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	
	ServletContext ctx = getServletContext();			
	String appName = ctx.getInitParameter("application-name");
	
	String attr_id = request.getParameter("attr_id");
	if (attr_id == null || attr_id.length()==0) { %>
		<b>Attribute id paramater is missing!</b> <%
		return;
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
	
	String position = request.getParameter("position");
	if (position == null || position.length()==0)
		position = "0";
	
	if (request.getMethod().equals("POST")){
		
		if (user==null || !user.isAuthentic()){
			%>
  				<html>
  				<body>
  					<h1>Error</h1><b>Not authorized to post any data!</b>
  				</body>
  				</html>
  			<%
  			return;
		}
		
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
		
		%>
		<html>
			<head>
				<script language="javascript">
				// <![CDATA[
				var s = opener.document.forms["form1"].elements["reloadUrl"].value;
				if (s!=null)
					opener.location.assign(s);
				else
					opener.location.reload(true);
				window.close();
				// ]]>
				</script>
			</head>
		</html>
		<%
	}

	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();

	try { // start the whole page try block
		
	conn = pool.getConnection();
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	Vector harvAttrs = searchEngine.getHarvestedAttrs(attr_id);
	Vector harvFields = (harvAttrs!=null && harvAttrs.size()>0) ?
						searchEngine.getHarvesterFieldsByAttr(attr_id) :
						null;
	String colsp = (harvFields==null || harvFields.size()==0) ? "1" : String.valueOf(harvFields.size());
	
	HashSet added = searchEngine.getHarvestedAttrIDs(attr_id, parent_id, parent_type);
%>

<html>
	<head>
		<title>Meta</title>
		<meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
		<script language="javascript" type="text/javascript">
		// <![CDATA[

			function closeme(){
				window.close();
			}
			
			function selected(id){
				
				if (opener && !opener.closed) {
					var elems = document.forms["form1"].elements;
					var i;
					for (i=0; i<elems.length; i++){
						var name = elems[i].name;
						if (name == "chk"){
							if (elems[i].value!=id){
								elems[i].checked=false;
							}
							else if (elems[i].checked==true){
								document.forms["form1"].elements["harv_attr_id"].value = id;
							}
							else{
								document.forms["form1"].elements["harv_attr_id"].value = "";
							}
						}
					}
					
					//alert(document.forms["form1"].elements["harv_attr_id"].value);
					
					document.forms["form1"].submit();
				}
				else {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.")
				}
			}
		// ]]>
		</script>
	</head>

<body class="popup">

<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>

<div style="margin-left:30">
	<form aname="form1" action="pick_harvattr.jsp" method="post">
	<table>
		<%
		
		int displayed = 0;
		
		if (harvAttrs==null || harvAttrs.size()==0){
			displayed++;
			%>
			<tr><td colspan="<%=colsp%>"><b>Nothing harvested for this attribute!</b></td></tr><%
		}
		else{ %>			
			<tr><td colspan="<%=colsp%>"><b>Select one of the harvested attribute values below:</b></td></tr>
			<tr><td colspan="<%=colsp%>">&#160;</td></tr><%
			
			if (harvFields!=null && harvFields.size()>0){ %>
				<tr><td>&#160;</td>
				<%
				for (int i=0; i<harvFields.size(); i++){ %>
					<th align="left" style="padding-right:10">&#160;<%=(String)harvFields.get(i)%></th><%
				}
				%>
				</tr><%
			}
		}
		
		for (int i=0; harvAttrs!=null && i<harvAttrs.size(); i++){
			Hashtable attrHash = (Hashtable)harvAttrs.get(i);
			String harvAttrID = (String)attrHash.get("harv_attr_id");
			if (added.contains(harvAttrID))
				continue;
			%>
			<tr>
				<td valign="top" style="padding-right:10">
						<input type="checkbox"
							   name="chk"
							   value="<%=harvAttrID%>"
							   onclick="selected('<%=harvAttrID%>')">
						</input>
				</td>
				<%
				for (int j=0; harvFields!=null && j<harvFields.size(); j++){
					String field = (String)harvFields.get(j); %>
					<td <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-right:10">
						&#160;<%=(String)attrHash.get(field)%>
					</td><%
				}
				%>
			</tr><%
			
			displayed++;
		}
		
		if (displayed==0){ %>
			<tr><td colspan="<%=colsp%>">&#160;&#160;(all have been already selected)</td></tr><%
		}
		%>		
	</table>
	
	<br/>
	
	<input type="hidden" name="parent_id" value="<%=parent_id%>"></input>
	<input type="hidden" name="parent_type" value="<%=parent_type%>"></input>
	<input type="hidden" name="position" value="<%=position%>"></input>
	
	<input type="hidden" name="attr_id" value="<%=attr_id%>"></input>
	<input type="hidden" name="harv_attr_id" value=""></input>
	
	<input type="hidden" name="mode" value="add"></input>
	
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
