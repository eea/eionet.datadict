<%@page contentType="text/html;charset=UTF-8" import="com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	
	String bodyClass = request.getParameter("class");
	boolean isPopup = bodyClass!=null && bodyClass.equals("popup");
%>

<html>
<head>
	<title>Error - Data Dictionary</title>
	<meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
	<link type="text/css" rel="stylesheet" href="eionet.css"/>
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
	<table border="0">
    	<tr valign="top">
        	<td>
}
%>

    <div style="margin-left:30">
    	<form name="form1" action="index.jsp" method="get">
		<table width="500">
		
			<%
				String msg   = (String)request.getAttribute("DD_ERR_MSG");
				msg = msg==null ? "empty message" : msg;
				
				String trc = (String)request.getAttribute("DD_ERR_TRC");
				trc = trc==null ? "empty trace" : trc;
				
				String backLink = (String)request.getAttribute("DD_ERR_BACK_LINK");
				backLink = backLink==null ? "javascript:history.back()" : backLink;
			%>
			
			<tr style="height:30px;"><td>&#160;</td></tr>
			<tr><td><font class="head00"><u>Error:</u></font></td></tr>
			<tr><td><b><%=msg%></b></td></tr>
			<tr style="height:10px;">
				<td>&#160;
					<input type="hidden" name="trc" value="<%=trc%>"/>
				</td>
			</tr>
			
			<%
			if (!isPopup){ %>
				<tr>
					<td>
						<a href="<%=backLink%>">&lt; back</a>
					</td>
				</tr><%
			}
			%>
			
			
			
		</table>
		</form>
    </div>

            <%
if (!isPopup){ %>            
	</td>
	</tr>
	</table>
	</div><%
}
%>

</body>
</html>
