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
                	<jsp:param name="name" value="Add an attribute"/>
            	</jsp:include>
            	
				<div style="margin-left:10">
				
                <table width="600">
                	<tr height="5"><td>&#160;</td></tr>
					<tr>
						<td>
							<span class="head00">Add an attribute</span>
						</td>
					</tr>
					<tr height="5"><td></td></tr>
					<tr>
						<td>
						<span class="mainfont">
							This is a function that enables you to define new data element attributes.
							These will then automatically become specifiable for data elements and will
							appear in the data element views if you specify so.
							So this is a place where you can introduce data element attributes specific
							to your own needs or attributes originating from a commonly practiced standard.
							But from wherever the attributes originate, you must also specify the namespace
							into which they belong. The concept of namespace is the same for attributes as
							it is for data elements. If you haven't specified a namespace for your needs,
							you must do so or put your attribute(s) into the Data Dictionary's default
							namespace (dd).
							There are two types of data element attributes and for both there's a slightly
							different adding/editing interface. Please choose one from below and have a go:
						</span>
						</td>
					</tr>
					<tr height="5"><td>&#160;</td></tr>
					<tr>
						<td>
							<ul>
								<li>
									<span class="mainfont"><a href="delem_attribute.jsp?mode=add&type=SIMPLE"><b>SIMPLE ATTRIBUTE</b></a></span></br>
									<span class="mainfont">
										This is the type of attribute which is a simple name/value pair. It does
										not have any complex structure of its own and it can appear <b>only once</b>
										per data element. Simple attributes are for example Name, Identifier, etc.
									</span><br/><br/>
								</li>
								<li>
									<span class="mainfont"><a href="delem_attribute.jsp?mode=add&type=COMPLEX"><b>COMPLEX ATTRIBUTE</b></a></span></br>
									<span class="mainfont">
										This is the type of attribute which concists of a set of fields, each represnting
										a simple name/value pair. For example a 'Relation' could be a complex attribute
										with two fields: 'Link' telling the related link, 'Type' telling the relation
										type (IsPartOf, HasPartOf, etc). Values of all fields form a row and there can
										be several rows for a complex attribute. Note that this way you can specify
										<b>multiple values</b> for an attribute. For example to define synonymous names for a
										data element, you define a complex attribute named 'Synonymous names', having
										a field called 'Name' and specify several rows of 'Name' for the data element.
									</span>
								</li>
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
