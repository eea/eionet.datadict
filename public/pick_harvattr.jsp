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
  				<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
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
				<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"><body><b><%=e.toString()%></b></body></html>
				<%
				return;
			}
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}
		
		%>
		<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
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

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
	<head>
		<%@ include file="headerinfo.jsp" %>
		<title>Meta</title>
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

<div id="pagehead">
	    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
	    <div id="networktitle">Eionet</div>
	    <div id="sitetitle">Data Dictionary (DD)</div>
	    <div id="sitetagline">This service is part of Reportnet</div>    
	</div> <!-- pagehead -->
	<div id="operations" style="margin-top:10px">
		<ul>
			<li><a href="javascript:window.close();">Close</a></li>
		</ul>
	</div>
	<div id="workarea" style="clear:right">
	<%
	if (harvAttrs==null || harvAttrs.size()==0){ %>
		<h5>Nothing harvested for this attribute!</h5><%
	}
	else{ %>
		<h5>Select one of the harvested attribute values below:</h5><%
	}
	%>	
	<form name="form1" action="pick_harvattr.jsp" method="post">
	<table class="datatable">
		<%				
		if (harvFields!=null && harvFields.size()>0){
			%>
			<tr>
				<th>&nbsp;</th>
				<%
				for (int i=0; i<harvFields.size(); i++){ %>
					<th align="left" style="padding-right:10">&#160;<%=(String)harvFields.get(i)%></th><%
				}
				%>
			</tr><%
		}
		
		int displayed = 0;
		for (int i=0; harvAttrs!=null && i<harvAttrs.size(); i++){
			Hashtable attrHash = (Hashtable)harvAttrs.get(i);
			String harvAttrID = "11";//(String)attrHash.get("harv_attr_id");
			if (added.contains(harvAttrID))
				continue;
			%>
			<tr>
				<td valign="top" style="padding-right:10">
						<input type="checkbox"
							   name="chk"
							   value="<%=harvAttrID%>"
							   onclick="selected('<%=harvAttrID%>')"/>
				</td>
				<%
				for (int j=0; harvFields!=null && j<harvFields.size(); j++){
					String field = (String)harvFields.get(j);
					%>
					<td <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-right:10">
						&nbsp;<%=(String)attrHash.get(field)%>
					</td><%
				}
				%>
			</tr><%
			
			displayed++;
		}
		%>		
	</table>
	<%
	if (displayed==0 && !(harvAttrs==null || harvAttrs.size()==0)){ %>
		<p>(all have been already selected)</p><%
	}
	%>
	<input type="hidden" name="parent_id" value="<%=parent_id%>"/>
	<input type="hidden" name="parent_type" value="<%=parent_type%>"/>
	<input type="hidden" name="position" value="<%=position%>"/>
	
	<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
	<input type="hidden" name="harv_attr_id" value=""/>
	
	<input type="hidden" name="mode" value="add"/>
	
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
