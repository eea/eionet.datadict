<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util"%>
<%
	request.setCharacterEncoding("UTF-8");
	String resptext = (String)request.getAttribute("TEXT");
%>
<html>
<head>
	<title>Data Dictionary</title>
	<meta content="text/html; charset=UTF-8" http-equiv="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="javascript" src='script.js'></script>
    <script language="javascript">
		function openPDF(){
			
			document.forms["form1"].submit();

		}
	</script>
</head>
<body>
<%@ include file="header.htm"%>
<table border="0">
    <tr valign="top">
		<td nowrap="nowrap" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></p>
        </td>
        <td>
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
		</td>
	</tr>
</table>
<form acceptcharset="UTF-8" name="form1" action="GetImportResults" method="POST">
<%
	String text=Util.Replace(resptext, "<br/>", "\n");

%>
<input type="hidden" name="text" value="<%=text%>"></input>
</form>
</center>
</body>
</html>
