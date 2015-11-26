<%@page contentType="text/html;charset=UTF-8" import="eionet.help.Helps, java.sql.*, eionet.meta.*,eionet.util.sql.ConnectionUtil,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
response.setHeader("Expires", Util.getExpiresDateString());

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
    try {
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
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
        <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
</head>
<body class="popup">
    <div id="pagehead">
        <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
        <div id="networktitle">Eionet</div>
        <div id="sitetitle"><%=application.getInitParameter("appDispName")%></div>
        <div id="sitetagline">This service is part of Reportnet</div>
    </div> <!-- pagehead -->

    <div id="workarea" style="clear:right">
        <%=helpText%>
    </div>
</body>
</html>
