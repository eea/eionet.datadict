<%@page contentType="text/html" import="java.util.*,com.tee.xmlserver.*,com.tee.uit.help.Helps"%>

<%@ include file="history.jsp" %>

<%
	XDBApplication.getInstance(getServletContext());
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
    <script language="JavaScript" src='script.js'></script>
</head>
<body>
    <%@ include file="header.htm" %>
    <table border="0" cellspacing="0" cellpadding="0">
        <tr valign="top">
            <td nowrap="true" width="130">
                <p><center>
                    <%@ include file="menu.jsp" %>
                </center></P>
            </TD>
            <TD>
               	<jsp:include page="location.jsp" flush='true'>
        			<jsp:param name="name" value="Documentation"/>
	            </jsp:include>

				<div style="margin-left:20">
				
				<table border="0" cellspacing="0" cellpadding="4">
					<tr height="10"><td></td></tr>					
					<tr>
						<td width="620" style="border: 1 dotted #C0C0C0">												
							<table border="0" width="100%" cellspacing="0" cellpadding="3" bordercolorlight="#C0C0C0" bordercolordark="#C0C0C0" style="border: 1px solid #FF9900">
								<tr>
									<td>
										<%=Helps.get("doc2", "text")%>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
				
				<jsp:include page="footer.jsp" flush="true">
				</jsp:include>
								
				</div>
            </TD>
        </TR>
    </table>
</body>
</html>
