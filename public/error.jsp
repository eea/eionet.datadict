<%@page contentType="text/html;charset=UTF-8" import="com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	
	String bodyClass = request.getParameter("class");
	boolean isPopup = bodyClass!=null && bodyClass.equals("popup");
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Error - Data Dictionary</title>
	<meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
	<script language="javascript" src='script.js' type="text/javascript"></script>
</head>

<%
if (isPopup){
	%>
	<body class="popup">
	<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
	<%
}
else{
	%>
	<body>
	<jsp:include page="nlocation.jsp" flush='true'>
			<jsp:param name="name" value="Error"/>
			<jsp:param name="back" value="true"/>
		</jsp:include>
	<%@ include file="nmenu.jsp" %>
<div id="workarea">
		<%
}
%>

    	<form name="form1" action="index.jsp" method="get">
		
			<%
				String msg   = (String)request.getAttribute("DD_ERR_MSG");
				msg = msg==null ? "" : msg;
				
				String trc = (String)request.getAttribute("DD_ERR_TRC");
				trc = trc==null ? "" : trc;
				
//				String backLink = (String)request.getAttribute("DD_ERR_BACK_LINK");
//				backLink = backLink==null ? "javascript:history.back()" : backLink;
				String backLink = "javascript:history.back()";
			%>
			
			<%
			if (!isPopup){ %>
		<div id="operations">
				<ul>
					<li>
						<a href="<%=backLink%>">&lt; back</a>
					</li>
				</ul>
				</div>
			<% } %>
			
    <h1>Error:</h1>
			
		<p><b><%=msg%></b></p>
		<p><b><%=trc%></b></p>
		<input type="hidden" name="trc" value="<%=trc%>"/>
			
			
		</form>

            <%
if (!isPopup){ %>            
	</div><%
}
%>

</body>
</html>
