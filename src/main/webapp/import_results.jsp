<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
    String resptext = (String)request.getAttribute("TEXT");
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
        function openPDF(){
            document.forms["form1"].submit();
        }
    </script>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Import results"/>
    </jsp:include>
    <%@ include file="nmenu.jsp" %>
    <div id="workarea">
        <h1>Import results</h1>
        <p><a href="javascript:openPDF()">Save import results into PDF file</a></p>
        <p>
            <b><%=resptext%></b>
        </p>
        <form id="form1" action="GetImportResults" method="post">
            <%
            String text=resptext.replace("<br/>", "\n");
            %>
            <div style="display:none">
                <input type="hidden" name="text" value="<%=Util.processForDisplay(text, true)%>"/>
            </div>
        </form>
    </div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.jsp" %>
</body>
</html>
