<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%@ include file="history.jsp" %>

<%

ServletContext ctx = getServletContext();			
String appName = ctx.getInitParameter("application-name");

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

String relID = request.getParameter("rel_id");
if (relID == null || relID.length()==0) {%>
	<b>FK relation ID not specified!</b><%
	return;
}

String mode = request.getParameter("mode");

if (request.getMethod().equals("POST")){
	
	Connection userConn = null;
	String id = null;
	
	try{
		userConn = user.getConnection();
		FKHandler handler = new FKHandler(userConn, request, ctx);
		handler.execute();
	}
	catch (Exception e){
		
		String msg = e.getMessage();
		
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
		e.printStackTrace(new PrintStream(bytesOut));
		String trace = bytesOut.toString(response.getCharacterEncoding());
		
		String backLink = history.getBackUrl();
		
		request.setAttribute("DD_ERR_MSG", msg);
		request.setAttribute("DD_ERR_TRC", trace);
		request.setAttribute("DD_ERR_BACK_LINK", backLink);
		
		request.getRequestDispatcher("error.jsp").forward(request, response);
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}
	
	if (mode.equals("delete"))
		response.sendRedirect(history.getBackUrl());
	else
		response.sendRedirect(currentUrl);
	
	return;
}

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

Hashtable fkRel = searchEngine.getFKRelation(relID);

String disabled = user == null ? "disabled" : "";

%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
    
		function submitForm(mode){
			
			if (mode == "delete"){
				var b = confirm("This relation will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function cardinalityHelp(){
			alert("Cardinaltiy of '+' stands for one or more, cardinaltiy of '*' stands for zero or more");
		}
		
    </script>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0">
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
                <jsp:param name="name" value="Foreign key relation"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<form id="form1" method="POST" action="fk_relation.jsp">
			
			<table>
				<tr>
					<td width="500">
						This is the foreign key relation between elements
						<font color="#006666"><%=(String)fkRel.get("a_name")%></font> and
						<font color="#006666"><%=(String)fkRel.get("b_name")%></font>.<br/>
						The relation is direction-less, so it doesn't matter which
						exactly is A or B.
					</td>
					<td></td>
				</tr>
				
				<tr>
					<td width="500"
						style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">
						&#160;
					</td>
					<td></td>
				</tr>
			</table>
			
			<table width="600" cellspacing="0"  cellpadding="0" border="0">
			
			<tr height="10"><td colspan="2">&#160;</td></tr>
			
			<tr>
				<td align="right" style="padding-right:10"><span class="mainfont"><b>Element A</b></span></td>
				<td><font class="title2" color="#006666"><%=(String)fkRel.get("a_name")%></font></td>
			</tr>
			
			<tr>
				<td align="right" style="padding-right:10"><span class="mainfont"><b>Element B</b></span></td>
				<td> <font class="title2" color="#006666"><%=(String)fkRel.get("b_name")%></font></td>
			</tr>
			
			<tr>
				<td align="right" style="padding-right:10">
					<span class="mainfont"><b>Cardinality (A to B)</b></span>
				</td>
				<td>
					<%
					Vector cardins = new Vector();
					cardins.add("0");
					cardins.add("1");
					cardins.add("+");
					cardins.add("*");
					%>
					<select name="a_cardin">
						<%
						String aCardin = (String)fkRel.get("a_cardin");
						for (int i=0; i<cardins.size(); i++){
							String cardin = (String)cardins.get(i);
							String selected = aCardin.equals(cardin) ? "selected" : ""; %>
							<option <%=selected%> value="<%=cardin%>"><%=cardin%></option><%
						}
						%>
					</select>&#160;to&#160;
					<select name="b_cardin">
						<%
						String bCardin = (String)fkRel.get("b_cardin");
						for (int i=0; i<cardins.size(); i++){
							String cardin = (String)cardins.get(i);
							String selected = bCardin.equals(cardin) ? "selected" : ""; %>
							<option <%=selected%> value="<%=cardin%>"><%=cardin%></option><%
						}
						%>
					</select>&#160;<a href="javascript:cardinalityHelp()">help</a>
				</td>
			</tr>
			
			<tr>	
				<td align="right" style="padding-right:10">
					<a href="javascript:alert('Description of this relation')"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Description</b></span>
				</td>
				<td>
					<textarea <%=disabled%>
							  class="small"
							  rows="3" cols="52"
							  name="definition"><%=(String)fkRel.get("definition")%></textarea>
				</td>
			</tr>
			
			<tr height="20"><td colspan="2"></td></tr>
			
			<tr>
				<td></td>
				<td>
					<input type="button" <%=disabled%> class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
					<input type="button" <%=disabled%> class="mediumbuttonb" value="Delete" onclick="submitForm('delete')"/>
				</td>
			</tr>
	</table>
	
	<input type="hidden" name="rel_id" value="<%=relID%>"/>
	<input type="hidden" name="mode" value="<%=mode%>"/>
	
	</form>
</div>
</TD>
</TR>
</table>
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