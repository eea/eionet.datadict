<%@page contentType="text/html" import="com.tee.xmlserver.*"%>

<%
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
%>

<html>
<head>
	<title>Data Dictionary</title>
	<META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="JavaScript" src='script.js'></script>
	<SCRIPT LANGUAGE="JavaScript">
	</SCRIPT>
</head>
<body>

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
                <jsp:param name="name" value="Error"/>
            </jsp:include>            
           
            <div style="margin-left:30">
            	<form name="form1" action="index.jsp" method="GET">
				<table width="500">
				
					<%
						String msg   = (String)request.getAttribute("DD_ERR_MSG");
						msg = msg==null ? "empty message" : msg;
						
						String trc = (String)request.getAttribute("DD_ERR_TRC");
						trc = trc==null ? "empty trace" : trc;
						
						String backLink = (String)request.getAttribute("DD_ERR_BACK_LINK");
						backLink = backLink==null ? "javascript:history.back()" : backLink;
					%>
					
					<tr height="30"><td>&#160;</td></tr>
					<tr><td><font class="head00"><u>Error:</u></font></td></tr>
					<tr><td><b><%=msg%></b></td></tr>
					<tr height="10"><td>&#160;</td></tr>
					<tr><td><a href="<%=backLink%>">&lt; back</a></td></tr>
					
					<input type="hidden" name="trc" value="<%=trc%>"/>
					
				</table>
				</form>
            </div>
		</TD>
	</tr>
</table>
</body>
</html>
