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
												<font class="head00">Concepts &amp; terms - datasets, tables, data elements </font><br></br>
												<b>&nbsp;&nbsp;Datasets</b><br>
											</p>
											<p>
												A collection of data exchanged between applications or humans. In Reportnet and Data Dictionary's context a dataset is a collection of tables containing the reported data. Often the "tables" will actually recede to a single table only. Usually datasets come as MSAccess databases or MSExcel files. They are subject to certain data flows and obliged to be reported by Reportnet players according to legislation.
											</p>
										<p><b>&nbsp;&nbsp;Tables</b></p>
											<p>
												A table in Data Dictionary's context is a table in dataset. It can be either a data table or a lookup table for how to interpret the data. A lookup table can be for example made for holding country codes or whichever other code lists.
												Columns in a table stand for data elements, rows for their values.
											</p>
										<p><b>&nbsp;&nbsp;Data elements</b></p>
											<p>
												Data elements are the different attributes or kinds of information linked up to entity. In a tabular structure the data elements are
											</p>
										<p style="margin-left:30"><img src="images/delem_description.gif"></p>
											<p>
												There can be different kinds of data elements
												<ul>
													<li><b>Data element with quantitative values</b>: The most commonly used kind, where any answer can be loaded if within the value domain. Examples: data elements "Longitude" or pH,  which allows any measured number to be used, or data element "Sitename" allowing any text describing a site.</li>
													<li><b>Data element with fixed values</b>: A data element where a predefined code list or other fixed values are the only accepted values. Examples: Station size with fixed values like Small, Large, etc.)</li>
												</ul>
											</p>
											<p>
												Important items when defining a data element is
												<ul>
													<li>Data type: if the content of the data element should be text, integer, Boolean or other types. Also specifies if decimals are to be used, and how many decimals are accepted.</li>
													<li>Sizemax: How large the field could be as a maximum.</li>
													<li>Value domain. In many cases you can define the value domain, e.g. only values from 0-100 is accepted for percentage values.</li>
													<li>Allowable values: If a pre-coded list is to be used, the allowable values will correspond to this list.</li>
													<li>Multiplicity: How many answers you allow for each case/object.</li>
												</ul>
											</p>
											<p>
												There is a long series of other attributes also being used to define the data element, among others the keywords being used to describe it, institution responsible for the data definition etc. For a full list of the attributes used, see <a href="attributes.jsp">attributes list</a>.
											</p>
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
