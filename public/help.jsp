<%@page contentType="text/html" import="com.tee.uit.help.Helps, java.sql.*, eionet.meta.*, com.tee.xmlserver.*"%>

<%

String helpText = "";
String width  = "400";
String height = "400";

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
		
	String _width = Helps.getPopupWidth(screen, area);
	if (_width!=null && _width.length()!=0)
		width = _width;
	
	String _height = Helps.getPopupLength(screen, area);
	if (_height!=null && _height.length()!=0)
		height = _height;
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
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>
<%=helpText%>
</body>
</html>