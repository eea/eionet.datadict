<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util"%>
<%

	String resptext = (String)request.getAttribute("TEXT");
%>
<html>
<head>
	<title>Data Dictionary</title>
	<META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="JavaScript" src='script.js'></script>
    <script language="JAVASCRIPT">
		function openPDF(){
			
			document.forms["form1"].submit();

		}
	</script>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0"">
<%@ include file="header.htm"%>
<table border="0">
    <tr valign="top">
		<td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Import results"/>
            </jsp:include>
            
            	<div style="margin-left:30">
					<table>
						<tr>
							<td colspan="3"><font class="head00">Import results</font></td>
						</tr>
						<tr height="10"><td colspan="3"></td></tr>
						<tr height="10"><td colspan="3"><a href="javascript:openPDF()">Save import results into PDF file</a></td></tr>
						<tr height="10"><td colspan="3"></td></tr>
						<tr>
							<td colspan="3"><b><%=resptext%></b></td>
						</tr>
					</table>
				</div>
		</TD>
	</tr>
</table>
<form name="form1" action="GetImportResults" method="POST">
<%
	String text=Util.Replace(resptext, "<br>", "\n");

%>
<input type="hidden" name="text" value="<%=text%>"></input>
</form>
</center>
</body>
</html>