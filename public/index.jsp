<%@page contentType="text/html" import="java.util.*,com.tee.xmlserver.*,com.tee.uit.help.Helps,eionet.util.Util"%>

<%@ include file="history.jsp" %>

<%

	XDBApplication.getInstance(getServletContext());
	
	String page_id = request.getParameter("page");

	if (page_id==null || page_id.length()==0)
		page_id = "0";

	String page_name=null;

	if (page_id.equals("1")){
		page_name = "Functions";
	}
	else if (page_id.equals("2")){
		page_name = "Concepts";
	}
	else if (page_id.equals("3")){
		page_name = "Login mode";
	}
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
    <link type="text/css" rel="stylesheet" href="boxes.css">
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
               	<% if (page_name == null){%>
	                <jsp:include page="location.jsp" flush='true'/>
           		<%} else{ %>
	                <jsp:include page="location.jsp" flush='true'>
            			<jsp:param name="name" value="<%=page_name%>"/>
            			<jsp:param name="back" value="true"/>
		            </jsp:include>
	            <% } %>

				<div style="margin-left:20">
				
				<table border="0" cellspacing="0" cellpadding="0">
					<tr height="10"><td></td></tr>					
					<tr>
						<td width="620" style="border: 1 dotted #C0C0C0">												
							<table border="0" width="100%" cellspacing="4" cellpadding="0">
							
								<!-- released data definitions part -->
								
			                	<tr>
			                  		<td width="100%" colspan="2">
			                    		<jsp:include page="released_datasets.jsp" flush="true">
		                    			</jsp:include>
			                  		</td>
			                	</tr>
			                	<tr>
			                  		<td width="101%" colspan="2" height="10"></td>
			                	</tr>
			                	
			                	<tr>
			                	
			                		<!-- the login part -->
			                		
			                  		<jsp:include page="protarea.jsp" flush="true"></jsp:include>
			                  		
			                  		<!-- the support part -->
			                  		
			                  		<td width="50%" style="border: 1 solid #FF9900" valign="top">
			                  			<%=Util.getUrlContent("http://www.eionet.eu.int/boxes/DD/box3/view_teaser_box?vis=standard&width=302")%>
			                  		</td>
			                	</tr>
			                	<tr>
			                	
			                		<!-- the documentation part -->
			                		
			                  		<td width="50%" style="border: 1 solid #FF9900" valign="top">
										<%=Util.getUrlContent("http://www.eionet.eu.int/boxes/DD/box2/view_teaser_box?vis=standard&width=302")%>
			                  		</td>
			                  		
			                  		<!-- the news part -->
			                  		
			                  		<td width="50%" style="border: 1 solid #FF9900" valign="top">
										<%=Util.getUrlContent("http://www.eionet.eu.int/boxes/DD/box1/view_teaser_box?vis=standard&width=302")%>
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
