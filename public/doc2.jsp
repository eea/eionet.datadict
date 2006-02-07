<%@page contentType="text/html;charset=UTF-8" import="java.util.*,com.tee.xmlserver.*,com.tee.uit.help.Helps"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="history.jsp" %>

<%
	request.setCharacterEncoding("UTF-8");
	XDBApplication.getInstance(getServletContext());
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <title>Documentation</title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <link rel="stylesheet" type="text/css" href="layout-print.css" media="print" />
    <link rel="stylesheet" type="text/css" href="layout-screen.css" media="screen" />
    <link rel="stylesheet" type="text/css" href="layout-handheld.css" media="handheld" />
    <link type="text/css" rel="stylesheet" href="boxes.css"/>
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
    <script type="text/javascript" src='script.js'></script>
</head>
<body>
				<jsp:include page="nlocation.jsp" flush='true'>
        			<jsp:param name="name" value="Documentation"/>
	            </jsp:include>
    <%@ include file="nmenu.jsp" %>
<div id="workarea">
	<div id="outerframe">
		<div id="innerframe">
				<%=Helps.get("doc2", "text")%>
		</div>				
  </div>				
				<jsp:include page="footer.jsp" flush="true">
				</jsp:include>
								
</div>
</body>
</html>
