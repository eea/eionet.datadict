<%@page import="eionet.doc.dto.DocumentationDTO"%>
<%@page import="eionet.doc.dto.DocPageDTO"%>
<%@page import="eionet.doc.DocumentationService"%>
<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,java.io.*,eionet.meta.*,eionet.util.sql.ConnectionUtil,eionet.help.Helps,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!final static int MAX_DOCUMENTATION_ITEMS=5;%>

<%@ include file="history.jsp" %>

<%
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
response.setHeader("Expires", Util.getExpiresDateString());

request.setCharacterEncoding("UTF-8");

Connection conn = null;
ServletContext ctx = getServletContext();
String appName = ctx.getInitParameter("application-name");
try{
    conn = ConnectionUtil.getConnection();

    DDUser user = SecurityUtil.getUser(request);
    DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
    searchEngine.setUser(user);

    HashSet filterStatuses = new HashSet();
    filterStatuses.add("Released");
    Vector releasedDatasets = searchEngine.getDatasets(null, null, null, null, null, false, filterStatuses);
    request.setAttribute("rlsd_datasets", releasedDatasets);
}
catch (Exception e){

    request.setAttribute("DD_ERR_MSG", e.toString());

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(bytesOut));
    String trace = bytesOut.toString(response.getCharacterEncoding());
    if (trace!=null)
        request.setAttribute("DD_ERR_TRC", trace);
}
finally{
    try{
        if (conn!=null) conn.close();
    }
    catch (SQLException e){}
}


%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title><%=appName %></title>
</head>
<body class="threecolumns">
<div id="container">
    <jsp:include page="nlocation.jsp" flush="false" />
    <%@ include file="nmenu.jsp" %>
    <div id="rightcolumn" class="quickjumps">
        <%=Helps.get("front_page", "news")%>
    </div>
    <div id="workarea">


                    <%

                    // exceptionous part
                    String errMsg = (String)request.getAttribute("DD_ERR_MSG");
                    if (errMsg!=null){
                        String errTrc = (String)request.getAttribute("DD_ERR_TRC");
                        %>
                        <div class="warning-msg">
                        <strong>DD encountered the following error:</strong>
                        <p>
                        <%=errMsg%>
                        </p>
                        </div>
                        <%
                        if (errTrc!=null){
                            %>
                            <form id="errtrc" action="http://">
                                <div style="display:none">
                                    <input type="hidden" name="errtrc" value="<%=errTrc%>"/>
                                </div>
                            </form>
                            <%
                        }
                    }
                    // no exceptions
                    else{
                        %>
                        <div id="outerframe">
                            <jsp:include page="/releasedItems.action" flush="true" />
                            <div>
                                <h2>Documentation</h2>
                                <%
                                DocPageDTO docs = DocumentationService.getInstance().view(null, null);
                                if (docs != null && docs.getDocs() != null && docs.getDocs().size() > 0) {
                                    %>
                                    <ul>
                                    <%
                                    for (DocumentationDTO doc : docs.getDocs()) {
                                        %><li><a href="documentation/<%=doc.getPageId()%>"><%=Util.processForDisplay(doc.getTitle(), true, true)%></a></li><%
                                    }
                                    %>
                                    </ul>
                                    <%
                                } else{
                                    %><p>No documentation currently available in the database.</p><%
                                }
                                %>
                            </div>
                            <%=Helps.get("front_page", "support")%>
                        </div> <!-- outerframe -->
                        <%
                    } // end of excpetions if/else
                    %>

</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.jsp" %>
</body>
</html>
