<%@page contentType="text/html" import="java.util.*,com.tee.xmlserver.*"%>

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
							<table border="0" width="100%" cellspacing="0" cellpadding="3" bordercolorlight="#C0C0C0" bordercolordark="#C0C0C0" style="border: 1 solid #FF9900">
								<tr>
									<td>
										<p>
											<br/>
											<font class="head00">Data Dictionary - functions, services and users</font><br></br>
											The Data Dictionary serves some main functions<br>
										</p>
										<p>
											<ul>
												<li>provide countries with <b>detailed</b> specifications of what to produce and report, to be looked up at web services of by provision of fact sheet documents for download.</li>
												<li>provide <b>parameters</b> necessary as input in <b>technical quality control</b> and validation of the reported data.</li>
												<li>provides a reference for <b>users</b> of the EEA and others using data following the specifications.</li>
												<li>provide a reference for <b>harmonisation processes</b> at the European level, the existing specifications to be reused when reporting obligations get revised or new ones are to be defined.</li>
												<li>Data Dictionary does not only contain spec ifications of data to be reported from the countries, but also other data needed flowing from other sources to be used in <b>indicator</b> development.</li>
												<li>If agreed with the EEA, countries can also store own definitions about national or internal reporting obligations.</li>
											</ul>
											<br>
											Reporting obligations are usually long-lasting. The definitions found in the Data Dictionary, therefore, are relatively stable. Revision and definitions of new dataset is handled by persons who have obtained administrating rights and have logged in by pressing the <a href="javascript:login()">Login</a> button. 
										</p>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
				
				<%@ include file="footer.htm" %>
								
				</div>
            </TD>
        </TR>
    </table>
</body>
</html>
