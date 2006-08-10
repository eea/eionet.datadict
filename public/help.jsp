<%@page contentType="text/html;charset=UTF-8" import="com.tee.uit.help.Helps, java.sql.*, eionet.meta.*, com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%

request.setCharacterEncoding("UTF-8");

String helpText = "";

String screen = request.getParameter("screen");
String area   = request.getParameter("area");
String attrid = request.getParameter("attrid");
String attrshn = request.getParameter("attrshn");

if (attrid==null && attrshn==null){
	if (screen==null || area==null){ %>
		<b>Missing screen or area!</b> <%
		return;
	}
	
	String _helpText = Helps.get(screen, area);
	if (_helpText!=null)
		helpText = _helpText;		
}
else{
	
	String attrtype = request.getParameter("attrtype");
	if (attrtype==null){ %>
		<b>Missing attribute type!</b><%
		return;
	}
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	try {
		conn = pool.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", getServletContext());
		if (attrid==null || attrid.length()==0)
			helpText = searchEngine.getAttrHelpByShortName(attrshn, attrtype);
		else
			helpText = searchEngine.getAttrHelp(attrid, attrtype);
		
		helpText = helpText==null ? "" : helpText;
	}
	catch (Exception e){ %>
		<b><%=e.toString()%></b><%
		return;
	}
	finally{
		try { if (conn!=null) conn.close(); } catch (SQLException e) {}
	}
}

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
		<%@ include file="headerinfo.txt" %>
    <title>Data Dictionary</title>
</head>
<body class="popup">
<div class="popuphead">
	<h1>Data Dictionary Help</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>
<%=helpText%>
</body>
</html>
