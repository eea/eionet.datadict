<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*"%>


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
response.setDateHeader("Expires", 0);

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
	<b>Parent element ID is missing!</b> <%
	return;
}

String parent_type = request.getParameter("parent_type");
if (parent_type == null || parent_type.length()==0){ %>
	<b>Parent type is missing!</b> <%
	return;
}

String parent_name = request.getParameter("parent_name");
if (parent_name == null) parent_name = "anonymous";

String parent_ns = request.getParameter("parent_ns");
if (parent_ns == null) parent_ns = "anonymous";

StringBuffer redirUrl = new StringBuffer("");
redirUrl.append("/contents.jsp?parent_id=");
redirUrl.append(parent_id);
redirUrl.append("&parent_name=");
redirUrl.append(parent_name);
redirUrl.append("&parent_ns=");
redirUrl.append(parent_ns);
redirUrl.append("&parent_type=");
redirUrl.append(parent_type);

String contentID   = request.getParameter("content_id");
String contentType = request.getParameter("content_type");

if (contentID != null){
	
	if (contentType == null){
		%>
		<b>The type of content is missing! (choice or relation?)</b>
		<%
		return;
	}
	
	if (!(contentType.equals("seq") || contentType.equals("chc"))){
		%>
		<b>Unknown content type! (must be one of choice or sequence)</b>
		<%
		return;
	}
	
	redirUrl.append("&content_id=");
	redirUrl.append(contentID);
	redirUrl.append("&content_type=");
	redirUrl.append(contentType);
}
else if (contentType != null){
	redirUrl.append("&content_type=");
	redirUrl.append(contentType);
}

String extChoice = null;
String extSequence = null;

String extendsID = request.getParameter("ext_id");
if (extendsID != null){
	
	redirUrl.append("&ext_id=");
	redirUrl.append(extendsID);
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	try{
		conn = pool.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		
		DataElement extElem = searchEngine.getDataElement(extendsID);
		if (extElem != null){
			extChoice = extElem.getChoice();
			extSequence = extElem.getSequence();
		}
	}
	finally{
		try { if (conn!=null) conn.close();
		} catch (SQLException e) {}
	}
}

if (extChoice != null && extSequence != null){
	%>
	<b>A data element (the base element in this case) is expected to have only a sequence or only a choice of sub-elements, not both!</b>
	<%
	return;
}

if (extChoice != null){
	redirUrl.append("&ext_chc_id=");
	redirUrl.append(extChoice);
}

if (extSequence != null){
	redirUrl.append("&ext_seq_id=");
	redirUrl.append(extSequence);
}

String opening = request.getParameter("open");
if (opening != null && !opening.equals("false"))
	redirUrl.append("&open=true");

if (contentID != null || extChoice != null || extSequence != null || contentType != null){
	response.sendRedirect(redirUrl.toString());
	return;
}
		
	
%>
<%@ include file="history.jsp" %>
<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
    	<script language="JavaScript" src='script.js'></script>
	</head>
	
	<script language="JavaScript">
	
			function openSequenceHelp(){
				alert("Sequence is an ordered list of data elements. Each of its elements in XML document must appear in the specified order. " +
					"For each element it is possible to specify how many times it can occur at its specified location. This can vary from " +
					"zero to unlimited number of occurabces.");
			}

			function openChoiceHelp(){
				alert("Choice is a group of data elements from which  o n l y  o n e  can appear in the XML document! A choice is naturally not ordered " +
						"and it can contain as many elements as you like.");
			}
			
	</script>
	
<body onload="load()">
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
                <jsp:param name="name" value="Allowable value"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
<div style="margin-left:30">
	
<form name="form1" method="POST" action="content.jsp">

<table width="560">

	<tr valign="bottom">
		<td colspan="4"><font class="head00">Subelements of <font class="title2" color="#006666"><%=parent_ns%>:<%=parent_name%></font></font></td>
	</tr>
	<tr height="10"><td colspan="4">&#160;</td></tr>
	
	<tr valign="bottom">
		<td colspan="4">
			There are no sub-elements currently specified for this data element nor does it extend the content of
			any other data element. If you want to start adding sub-elements to this data element,
			you have to first select if they will form a <a href="javascript:openSequenceHelp()">
			<b><font color="black">sequence</b></font></a> or a <a href="javascript:openChoiceHelp()">
			<font color="black"><b>choice</b></font></a>. You can do that by using the select-box below.
		</td>
	</tr>
	
	<tr height="10"><td colspan="4">&#160;</td></tr>
	<tr valign="bottom">
		<td colspan="4">
			<font class="smallFont">Select the type of sub-elements formation from here:</font><br></br>
			<select class="small" name="contentType">
				<option selected value="seq">Sequence</option>
				<option value="chc">Choice</option>
			</select>&#160;&#160;
			<%
				if (user != null){
					%>				
					<input
						type="button"
						value="Go"
						class="smallbutton"
						onclick="window.location.replace('<%=redirUrl.toString()%>' + '&content_type=' + document.forms['form1'].elements['contentType'].value)"/>
					<%
				}
				else{
					%>
					<input
						type="button"
						value="Go"
						class="smallbutton"
						disabled="true"/>
					<%
				}
			%>
		</td>
	</tr>
</table>

</form>
</div>
        </TD>
</TR>
</table>
</body>
</html>
