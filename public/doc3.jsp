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
											<font class="head00">Data Dictionary - administrative tools - login mode</font><br></br>
											<b>Adding or revising the content</b><br>
										</p>
										<p>
											Revision and definitions of new datasets is handled by persons who have obtained
											administration rights and have logged in by pressing the <a href="javascript:login()">Login</a> button.
											<ul>
												<li>
													<b>Revision</b> of data definitions: Once you log in, you will be provided
													with additional functions on the left-hand pane, enabling you to add new data
													definitions or revise content. This you can do manually, by writing into
													the forms on the web pages.
												</li>
												<li>
													You can <b>import data</b> definitions directly in XML format. EEA has
													developed an MSAccess database for preparation of the data definitions,
													which you can then generate into XML format which in turn can then be
													imported into Data Dictionary database.
												</li>
												<li>
													Each data definition can be <b>represented in XML Schema</b> format
													and you can see (and save) them when pressing the relevant link at the bottom
													of definition views.
												</li>
											</ul>
										</p>
										<p>
											<b>Adding or revising the definition structure </b>
										</p>
										<p>
											Each dataset, table and data element is defined by a set of attributes.
											A lot of them correspond to <a target="_blank"
											href="http://isotc.iso.ch/livelink/livelink/fetch/2000/2489/Ittf_Home/PubliclyAvailableStandards.htm">
											ISO 11179 standard</a> for describing data elements. 
											Data Dictionary attribute set will be relatively stable. 
											However, the system is flexible and an administrator can dynamically
											<b>add/remove attributes</b> from/to the system. 
											To search for attributes, administrator can use the <b>Attributes</b>
											button on the left-hand pane.
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
