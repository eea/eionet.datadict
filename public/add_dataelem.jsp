<%@page contentType="text/html" import="java.util.*"%>
<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
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
            <TD nowrap="true">
                
            	<jsp:include page="location.jsp" flush='true'>
                	<jsp:param name="name" value="Add a data element"/>
            	</jsp:include>
            	
				<div style="margin-left:10">
                <table width="600">
                	<tr height="5"><td>&#160;</td></tr>
					<tr>
						<td>
							<span class="head00">Add a data element</span>
						</td>
					<tr height="5"><td></td></tr>
					<tr>
						<td>
						<span class="mainfont">
							This is a function that enables you to define new data elements manually
							as opposed to importing them as XMLSchema files. So it's kind of a form
							interface for creating XML Schemas within a limited scope of XML Schema
							specification.<br></br>
							By letting you to specify ISO 11179 attributes for a data element, this
							interface automatically generates a simple XMLSchema for encompassing
							those attributes. Naturally it also adds those attributes into the database,
							because Data Dictionary keeps the data element definitions in formats of both 
							XMLSchema and operational data.<br></br>
							So this is an interface for those wanting to save the trouble of getting to
							know the wide and complex world of XML Schemas.
							There's a separate interface for each of three data element types as seen in
							Data Dictionary. Please choose one and have a go:
						</span>
						</td>
					</tr>
					<tr height="5"><td>&#160;</td></tr>
					<tr>
						<td>
							<ul>
								<li><span class="mainfont"><a href="data_element.jsp?mode=add&type=AGG"><b>AGGREGATE DATA ELEMENT</b></a></span></li><br/><br/>
								<li><span class="mainfont"><a href="data_element.jsp?mode=add&type=CH1"><b>DATA ELEMENT WITH FIXED VALUES</b></a></span></li><br/><br/>
								<li><span class="mainfont"><a href="data_element.jsp?mode=add&type=CH2"><b>DATA ELEMENT WITH QUANTITATIVE VALUES</b></a></span></li>
							</ul>
						</td>
					</tr>
				</table>
				</div>
            </TD>
        </TR>
    </table>
</body>
</html>
