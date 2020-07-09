<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil,org.apache.commons.lang.StringUtils,eionet.meta.dao.domain.*"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

<%
    request.setCharacterEncoding("UTF-8");

    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    DDUser user = SecurityUtil.getUser(request);
    if (user==null || !user.isAuthentic()) {
        Exception e = new Exception("User not authorized!");
        String msg = e.getMessage();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bytesOut));
        String trace = bytesOut.toString(response.getCharacterEncoding());

        String backLink = history.getBackUrl();

        request.setAttribute("DD_ERR_MSG", msg);
        request.setAttribute("DD_ERR_TRC", trace);
        request.setAttribute("DD_ERR_BACK_LINK", backLink);

        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    if (request.getMethod().equals("POST")) {
        String[] ds_ids = request.getParameterValues("ds_id");
        for (int i=0; ds_ids!=null && i < ds_ids.length; i++){
            String dsIdf = request.getParameter("ds_idf_" + ds_ids[i]);
            if (dsIdf==null || !SecurityUtil.hasPerm(user, "/datasets/" + dsIdf, "d")){ %>
                <b>Not allowed!</b><%
            }
        }

        Connection userConn = null;
        DatasetHandler handler = null;

        try {
            userConn = user.getConnection();
            handler = new DatasetHandler(userConn, request, getServletContext());
            handler.setUser(user);
            handler.execute();
        } finally {
            handler.cleanup();
            try { 
                if (userConn!=null) {
                    userConn.close();
                }
            } catch (SQLException e) {}
        }
        response.sendRedirect("restore_datasets.jsp?SearchType=SEARCH&restore=true");
        return;
    }

    Connection conn = null;
    try { // start the whole page try block
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);
        Vector<Dataset> datasets = searchEngine.getDeletedDatasets();
        VersionManager verMan = new VersionManager(conn, searchEngine, user);
        
        String sortName = (String) request.getParameter("sort_name");
        eionet.meta.DataSetSort sort = eionet.meta.DataSetSort.fromString(sortName);
        if (sort == null) {
            sort = eionet.meta.DataSetSort.SHORT_NAME; // fall-back
        }
        String sortOrder = (String) request.getParameter("sort_order");
        boolean descending = StringUtils.isNotBlank(sortOrder) && sortOrder.equals("desc");
        Collections.sort(datasets, sort.getComparator(descending));
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Restore datasets - Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[
        function deleteDataset() {
            if ($(".selectable:checked").length === 0) {
                alert("Select at least one dataset to continue");
                return;
            }
            var msg = "This will permanently delete the selected datasets! Click OK, if you want to continue. Otherwise click Cancel.";
            var b = confirm(msg);
            if (b==false) {
                return;
            }
            document.forms["form1"].elements["mode"].value = "delete";
            document.forms["form1"].submit();
        }

        function restoreDataset() {
            if ($(".selectable:checked").length === 0) {
                alert("Select at least one dataset to continue");
                return;
            }
            document.forms["form1"].elements["mode"].value = "restore";
            document.forms["form1"].submit();
        }
        // ]]>
    </script>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Restore datasets"/>
        <jsp:param name="helpscreen" value="restore_datasets"/>
    </jsp:include>
    <c:set var="currentSection" value="datasets" />
    <%@ include file="/pages/common/navigation.jsp" %>
    <div id="workarea">
        <h1>Restore datasets</h1>
        <div id="drop-operations">
            <ul>
                <li class="back"><a href="${pageContext.request.contextPath}/datasets.jsp">Back to datasets</a></li></li>
                <% if (user != null && !datasets.isEmpty()) { %>
                    <li class="restore"><a href="javascript:restoreDataset()">Restore selected</a></li>
                    <li class="delete"><a href="javascript:deleteDataset()">Delete selected</a></li>
                <%}%>
            </ul>
        </div>
        <%
            if (datasets == null || datasets.size()==0){
                %>
                <p class="not-found">No datasets found.</p></div></div><%@ include file="footer.jsp" %></body></html>
                <%
                return;
            }
        %>
        <h2 class="results">Total results: <%=datasets.size()%></h2>
        <form id="form1" method="post" action="restore_datasets.jsp">
        <!--  result table -->
        <table class="datatable results">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th<c:if test="${param.sort_name eq 'SHORT_NAME'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
                        <c:url var="shortNameSortingUrl" value="/restore_datasets.jsp">
                            <c:forEach items="${param}" var="entry">
                                <c:if test="${entry.key != 'sort_name' and entry.key != 'sort_order'}">
                                    <c:param name="${entry.key}" value="${entry.value}" />
                                </c:if>
                            </c:forEach>
                            <c:param name="sort_name" value="SHORT_NAME" />
                            <c:if test="${param.sort_name eq 'SHORT_NAME' and param.sort_order ne 'desc'}">
                                <c:param name="sort_order" value="desc" />
                            </c:if>
                        </c:url>
                        <a title="Sort on Dataset" href="${shortNameSortingUrl}">Dataset</a>
                    </th>
                    <th>CheckInNo</th>
                    <th>Tables</th>
                </tr>
            </thead>
            <tbody>
            <%
                boolean wasDelPrm = false;
                for (int i=0; i<datasets.size(); i++){
                    Dataset dataset = (Dataset)datasets.get(i);
                    String ds_id = dataset.getID();
                    String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
                    String ds_name = Util.processForDisplay(dataset.getShortName());
                    if (ds_name == null) {
                        ds_name = "unknown";
                    }
                    if (ds_name.length() == 0) {
                        ds_name = "empty";
                    }

                    Vector tables = searchEngine.getDatasetTables(ds_id, true);

                    String dsFullName=dataset.getName();
                    if (dsFullName == null) {
                        dsFullName = ds_name;
                    }
                    if (dsFullName.length() == 0) {
                        dsFullName = ds_name;
                    }
                    if (dsFullName.length()>60) {
                        dsFullName = dsFullName.substring(0,60) + " ...";
                    }

                    boolean delPrm = user!=null && SecurityUtil.hasPerm(user, "/datasets/" + dataset.getIdentifier(), "u");
                    if (delPrm) {
                        wasDelPrm = true;
                    }

                    String workingUser = verMan.getDstWorkingUser(dataset.getIdentifier());
                    String topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());

                    boolean canDelete = topWorkingUser==null ||
                                        (dataset.isWorkingCopy() &&
                                        workingUser!=null && user!=null &&
                                        workingUser.equals(user.getUserName()));
                    String zebraClass = (i + 1) % 2 != 0 ? "odd" : "even";
                    %>

                    <tr class="<%=zebraClass%>">
                        <td>
                            <%
                            if (delPrm){
                                if (topWorkingUser!=null){ // mark checked-out datasets
                                    %> <span title="<%=Util.processForDisplay(topWorkingUser, true)%>" class="checkedout">*</span> <%
                                }

                                if (canDelete){ %>
                                    <input type="checkbox" class="selectable" style="height:13;width:13" name="ds_id" value="<%=ds_id%>"/>
                                    <input type="hidden" name="ds_idf_<%=dataset.getID()%>" value="<%=dataset.getIdentifier()%>"/>
                                    <%
                                }
                            }
                            %>
                        </td>
                        <td title="<%=Util.processForDisplay(dsFullName, true)%>">
                            <a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;out_type=GDLN&amp;obj_id=<%=dataset.getID()%>">
                                <%=Util.processForDisplay(dsFullName)%>
                            </a>
                        </td>
                        <td>
                            <%=dsVersion%>
                        </td>
                        <td>
                            <%
                            for (int c=0; tables!=null && c<tables.size(); c++){
                                DsTable table = (DsTable)tables.get(c);
                                String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(), table.getIdentifier(), "tbl");
                                %>
                                    <%=Util.processForDisplay(table.getShortName())%>
                                <%
                                if (user!=null && tblWorkingUser!=null){ // mark checked-out elements
                                    %>&nbsp;<span class="checkedout">*</span> <%
                                }
                                %>
                                <br/><%
                            }
                            %>
                        </td>
                    </tr>
                    <%
                }
                %>
            </tbody>
        </table>
        <div style="display:none">
            <input type="hidden" name="mode" value="view"/>
            <!-- Special input for 'delete' mode only. Inidcates if dataset(s) should be deleted completely. -->
            <input type="hidden" name="complete" value="true"/>
        </div>
        </form>
        </div> <!-- workarea -->
        </div> <!-- container -->
      <%@ include file="footer.jsp" %>
    </body>
</html>
<%
// end the whole page try block
} finally {
    try { 
        if (conn!=null) {
            conn.close();
        }
    } catch (SQLException e) {}
}
%>
