<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private Vector elems=null;%>
<%!private ServletContext ctx=null;%>

<%@ include file="history.jsp" %>

<%

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

XDBApplication.getInstance(getServletContext());
AppUserIF user = SecurityUtil.getUser(request);
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

//check if element id is specified
String delemID = request.getParameter("delem_id");
if (delemID == null || delemID.length()==0){ %>
	<b>Data element ID is missing!</b> <%
	return;
}

String disabled = user == null ? "disabled" : "";

String delemName = request.getParameter("delem_name");
if (delemName == null || delemName.length()==0) delemName = "unknown";

ctx = getServletContext();

//handle the POST

if (request.getMethod().equals("POST")){
	
	Connection userConn = null;
	
	try{
		userConn = user.getConnection();
		CsiRelationHandler handler = new CsiRelationHandler(userConn, request, ctx);
		
		try{
			handler.execute();
		}
		catch (Exception e){ %>
			<html><body><b><%=e.toString()%></b></body></html> <%
			return;
		}
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}
	
	// build reload URL

	/*StringBuffer redirUrl = new StringBuffer("" + "/rel_elements.jsp?delem_id=");
	redirUrl.append(delemID);
	redirUrl.append("&delem_name=");
	redirUrl.append(delemName);
	*/
	response.sendRedirect(currentUrl);
	return;

}

//handle the GET

String appName = ctx.getInitParameter("application-name");

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);


elems = searchEngine.getRelatedElements(delemID, "elem");

StringBuffer collect_elems=new StringBuffer();

if (disabled.equals("")){
	boolean isWorkingCopy = searchEngine.isWorkingCopy(delemID, "elm");
	if (!isWorkingCopy) disabled = "disabled";
}
	
%>

<html>
<head>
	<title>Meta</title>
	<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
	<link href="eionet.css" rel="stylesheet" type="text/css"/>
</head>

<script language="JavaScript" src='script.js'></script>

<script language="JavaScript">
		function submitForm(mode){
			
			if (mode=="delete"){
				var b = confirm("This will delete all the relations between elements you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode=="add" && hasWhiteSpace("delem_name")){
				alert("Short name cannot contain any white space!");
				return;
			}
				
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function hasWhiteSpace(input_name){
			
			var elems = document.forms["form1"].elements;
			if (elems == null) return false;
			for (var i=0; i<elems.length; i++){
				var elem = elems[i];
				if (elem.name == input_name){
					var val = elem.value;
					if (val.indexOf(" ") != -1) return true;
				}
			}
			
			return false;
		}
		function openAdd(url){
			
			var selected = document.forms["form1"].collect_elems.value;
			if (url != null) url = url + "&selected=" + selected;
			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
			if (window.focus) {wAdd.focus()}
		}
		function pickElem(id, name){
			document.forms["form1"].childcomp_id.value=id;
			document.forms["form1"].mode.value="add";
			submitForm('add');
			
			return false;
		}
		
</script>
	
<body>
<%@ include file="header.htm" %>
<table border="0">
    <tr valign="top">
        <td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Related elements"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">
			
<form name="form1" method="POST" action="rel_elements.jsp">

	<table width="500" cellspacing="0" cellpadding="0">

		
		<tr valign="bottom">
			<td>
				<font class="head00">Related data elements of <span class="title2"><%=Util.replaceTags(delemName)%></span>.
			</td>
		</tr>
		
		<tr height="5"><td></td></tr>
		
		<tr><td style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
		
		<tr>
			<td>
				This is a list of data elements that are somehow related to <font color="#006666"><%=Util.replaceTags(delemName)%></font>.
				The nature of  relations can be seen and edited when clicking on a related element. Use 'Add' to add new
				relations, checkboxes and 'Remove' will help you to remove existing ones.
			</td>
		</tr>
		
		<tr height="5"><td></td></tr>
		
	</table>
	
	<table width="600" cellspacing="0">
		<tr>
			<td width="60"></td>
			<td style="padding-left:5">
				<input type="button" <%=disabled%> class="smallbutton" value="Add" onclick="openAdd('search.jsp?ctx=popup')"/>
			</td>
		</tr>

		<tr>
			<td align="right" style="padding-right:10">
				<input type="button" <%=disabled%> value="Remove" class="smallbutton" onclick="submitForm('delete')"/>
			</td>				
			<th align="left" style="padding-left:5;padding-right:10">Short name</th>
			<th align="left" style="padding-right:10">Description</th>
		</tr>
			
		<%
		
		collect_elems.append(delemID + "|");
		for (int i=0; elems!=null && i<elems.size(); i++){
			
			CsiItem item = (CsiItem)elems.get(i);
			
			String compID = item.getComponentID();
			if (compID == null) continue;
			collect_elems.append(compID + "|");
			
			%>
			<tr>
				<td align="right" style="padding-right:10"><input type="checkbox" name="del_id" value="<%=item.getID()%>"/>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a href="rel_element.jsp?delem_id=<%=delemID%>&#38;delem_name=<%=delemName%>&#38;child_id=<%=item.getID()%>">
						<%=Util.replaceTags(item.getValue())%>
					</a>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<%=item.getRelDescription()%>
				</td>
			</tr>
			<%
		}
		
		%>

	</table>
	
	<input type="hidden" name="mode" value="delete"/>
	<input type="hidden" name="delem_id" value="<%=delemID%>"/>
	<input type="hidden" name="parentcomp_id" value="<%=delemID%>"/>
	<input type="hidden" name="parent_id" value=""/>
	<input type="hidden" name="delem_name" value="<%=delemName%>"/>
	<input type="hidden" name="component_type" value="elem"/>
	<input type="hidden" name="csi_type" value="elem"/>
	<input type="hidden" name="rel_type" value="abstract"/>
	<input type="hidden" name="childcomp_id"/>
	
	<input type="hidden" name="collect_elems" value="<%=collect_elems.toString()%>"></input>
		
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
