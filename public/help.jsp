<%@page contentType="text/html" import="com.tee.uit.help.Helps, java.sql.*, eionet.meta.*, com.tee.xmlserver.*"%>

<%

String helpText = "";
String width  = "400";
String height = "400";

String screen = request.getParameter("screen");
String area   = request.getParameter("area");
String attrid = request.getParameter("attrid");

if (attrid==null){
	if (screen==null || area==null){ %>
		<b>Missing screen or area!</b> <%
		return;
	}
	
	helpText = Helps.get(screen, area);
	width  = Helps.getPopupWidth(screen, area);
	height = Helps.getPopupLength(screen, area);
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
		helpText = searchEngine.getAttrHelp(attrid, attrtype);
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

<html>
<head>
    <title>Data Dictionary</title>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1" />
    <link type="text/css" rel="stylesheet" href="eionet_new.css" />
    <script language="JavaScript">
    	function load(){
	    	resize();
    	}
    	
    	function resize(){
	    	window.resizeTo(<%=width%>, <%=height%>);
    	}
    </script>
</head>
<body class="popup" onload="load()">
<div class="popuphead">
	<h1>Data Dictionary Help</h1>
	<hr/>
</div>
<%=helpText%>
</body>
</html>