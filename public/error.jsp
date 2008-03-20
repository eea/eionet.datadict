<%@page contentType="text/html;charset=UTF-8" import="eionet.meta.DDUser"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	DDUser user = SecurityUtil.getUser(request);
	
	String bodyClass = request.getParameter("class");
	boolean isPopup = bodyClass!=null && bodyClass.equals("popup");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Error - Data Dictionary</title>
	<meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
</head>

<%
if (isPopup){
	%>
	<body class="popup">
	<div id="pagehead">
    	<a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
	    <div id="networktitle">Eionet</div>
	    <div id="sitetitle">Data Dictionary (DD)</div>
	    <div id="sitetagline">This service is part of Reportnet</div>    
	</div> <!-- pagehead -->
	<div id="workarea">
	<%
}
else{
	%>
	<body>
		<div id="container">
		<jsp:include page="nlocation.jsp" flush="true">
			<jsp:param name="name" value="Error"/>			
		</jsp:include>
		<%@ include file="nmenu.jsp" %>
		<div id="workarea"><%
}
%>

    	<form id="form1" action="index.jsp" method="get">
		
			<%
				String msg   = (String)request.getAttribute("DD_ERR_MSG");
				msg = msg==null ? "no error message found in request" : msg;
				
				String trc = (String)request.getAttribute("DD_ERR_TRC");
				trc = trc==null ? "no error trace found in request" : trc;
				
//				String backLink = (String)request.getAttribute("DD_ERR_BACK_LINK");
//				backLink = backLink==null ? "javascript:history.back()" : backLink;
				String backLink = "javascript:history.back()";
			%>
			
			<%
			if (!isPopup){
				%>
				<div id="operations">
					<ul>
						<li>
							<a href="<%=backLink%>">&lt; back</a>
						</li>
					</ul>
				</div><%
			}
			%>
			
    		<h1>Error:</h1>
			<p><strong><%=msg%></strong></p>
			<p><strong><%=trc%></strong></p>
		</form>
</div> <!-- workarea -->
<%
if (!isPopup){
	%>
	</div> <!-- container -->
	<%@ include file="footer.txt" %><%
}
%>
</body>
</html>
