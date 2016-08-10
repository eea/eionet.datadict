<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil,org.apache.commons.lang.StringUtils"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ include file="/pages/common/taglibs.jsp"%>
<%!private static final String ATTR_PREFIX = "attr_";%>

<%@ include file="history.jsp" %>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    // get user object from session
    DDUser user = SecurityUtil.getUser(request);

    // see if popup
    boolean popup = request.getParameter("ctx")!=null && request.getParameter("ctx").equals("popup");
    String newerThan = request.getParameter("newerThan");

    // declare some global stuff
    Connection conn = null;
    Vector dataElements = null;
    DDSearchEngine searchEngine = null;
    String idfier = request.getParameter("idfier");

    // start the whole page try block
    try {
        // get the DB connection and set up search engine
        conn = ConnectionUtil.getConnection();
        searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);
        if (newerThan != null && newerThan.length() > 0) {
            dataElements = searchEngine.getNewerReleases(idfier, newerThan);
        } else {
            // get statical search parameters
            String type = request.getParameter("type");
            String short_name = request.getParameter("short_name");
            String searchPrecision = request.getParameter("search_precision");
            boolean isSearchForWorkingCopies = request.getParameter("wrk_copies") != null && request.getParameter("wrk_copies").equals("true");
            boolean isIncludeHistoricVersions = request.getParameter("incl_histver") != null && request.getParameter("incl_histver").equals("true");

            String oper="=";
            if (searchPrecision != null && searchPrecision.equals("free")) {
                oper=" match ";
            }
            if (searchPrecision != null && searchPrecision.equals("substr")) {
                oper=" like ";
            }

            // get dynamical search parameters
            Vector params = new Vector();
            Enumeration parNames = request.getParameterNames();
            while (parNames.hasMoreElements()) {
                String parName = (String)parNames.nextElement();
                if (!parName.startsWith(ATTR_PREFIX)) {
                    continue;
                }

                String parValue = request.getParameter(parName);
                if (parValue.length()==0) {
                    continue;
                }

                DDSearchParameter param = new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");

                if (oper!= null && oper.trim().equalsIgnoreCase("like")) {
                    param.addValue("'%" + parValue + "%'");
                } else {
                    param.addValue("'" + parValue + "'");
                }
                params.add(param);
            }
            dataElements = searchEngine.getCommonElements(params, type, short_name, idfier, request.getParameter("reg_status"), isSearchForWorkingCopies, isIncludeHistoricVersions, oper);
        }
        String sortName = (String) request.getParameter("sort_name");
        DataElementSort sort = DataElementSort.fromString(sortName);
        if (sort == null) {
            sort = DataElementSort.SHORT_NAME; // fall-back
        }
        String sortOrder = (String) request.getParameter("sort_order");
        boolean descending = StringUtils.isNotBlank(sortOrder) && sortOrder.equals("desc");
        Collections.sort(dataElements, sort.getComparator(descending));
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Search results - Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[
        function warnStatus(status){
            if (status.toLowerCase() == 'retired' || status.toLowerCase() == 'superseded') {
                if (['a', 'e', 'i', 'o', 'u'].indexOf(status.toLowerCase().charAt(0))!=-1) {
                    return confirm('You are about to select an '+status+' data element. If you want to continue click OK. Otherwise click Cancel.');
                }
                else {
                    return confirm('You are about to select a '+status+' data element. If you want to continue click OK. Otherwise click Cancel.');
                }
            } else {
                return true;
            }
        }
        <%
        if (popup){ %>
            function pickElem(elmID, rowIndex){
                // make sure the opener exists and is not closed
                if (opener && !opener.closed) {
                    // if the opener has pickElem(elmID) function at it returns true, close this popup
                    // else don't close it (multiple selection might be wanted)
                    if (window.opener.pickElem(elmID)==true)
                        closeme();
                    else
                        hideRow(rowIndex);
                }
                else
                    alert("You have closed the main window!\n\nNo action will be taken.")
            }
            function hideRow(i){
                var t = document.getElementById("tbl");
                var row = t.getElementsByTagName("TR")[i+1];
                row.style.display = "none";
            }
            function closeme(){
                window.close()
            }
            <%
        }
        %>
        // ]]>
    </script>
</head>

<%
if (popup){
    %>
    <body class="popup">
        <div id="pagehead">
            <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
            <div id="networktitle">Eionet</div>
            <div id="sitetitle"><%=application.getInitParameter("appDispName")%></div>
            <div id="sitetagline">This service is part of Reportnet</div>
        </div> <!-- pagehead -->
        <div id="workarea">
        <%
}
else{ %>
        <body>
        <div id="container">
        <jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Search results"/>
            <jsp:param name="helpscreen" value="common_element_search_results"/>
        </jsp:include>
        <c:set var="currentSection" value="dataElements" />
        <%@ include file="/pages/common/navigation.jsp" %>
        <div id="workarea">
        <%
}
%>

            <h1>Search results</h1>
            <%
            if (popup){
                %>
                <div id="drop-operations">
                    <ul>
                        <li class="close"><a href="javascript:window.close();">Close</a></li>
                    </ul>
                </div><%
            }
            if (user==null){ %>
                <p class="advise-msg">Note: Common elements NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.</p><%
            }

            if (dataElements==null || dataElements.size()==0){
                %>
                <p class="not-found"><strong>No results matching the search criteria were found.</p>
                </div></body></html>
                <%
                return;
            }
            %>

            <!-- result table -->
            <h2 class="results">Total results: <%=dataElements.size()%></h2>
            <table class="datatable results">
            <thead>
                <tr>
                    <th<c:if test="${param.sort_name eq 'SHORT_NAME'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
                        <c:url var="shortNameSortingUrl" value="/common_elms.jsp">
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
                        <a title="Sort on Element" href="${shortNameSortingUrl}">Element</a>
                    </th>
                    <th<c:if test="${param.sort_name eq 'TYPE'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
                        <c:url var="typeSortingUrl" value="/common_elms.jsp">
                            <c:forEach items="${param}" var="entry">
                                <c:if test="${entry.key != 'sort_name' and entry.key != 'sort_order'}">
                                    <c:param name="${entry.key}" value="${entry.value}" />
                                </c:if>
                            </c:forEach>
                            <c:param name="sort_name" value="TYPE" />
                            <c:if test="${param.sort_name eq 'TYPE' and param.sort_order ne 'desc'}">
                                <c:param name="sort_order" value="desc" />
                            </c:if>
                        </c:url>
                        <a title="Sort on Type" href="${typeSortingUrl}">Type</a>
                    </th>
                    <c:if test="${param.incl_histver eq 'true'}">
                        <th<c:if test="${param.sort_name eq 'ID'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
                            <c:url var="idSortingUrl" value="/common_elms.jsp">
                                <c:forEach items="${param}" var="entry">
                                    <c:if test="${entry.key != 'sort_name' and entry.key != 'sort_order'}">
                                        <c:param name="${entry.key}" value="${entry.value}" />
                                    </c:if>
                                </c:forEach>
                                <c:param name="sort_name" value="ID" />
                                <c:if test="${param.sort_name eq 'ID' and param.sort_order ne 'desc'}">
                                    <c:param name="sort_order" value="desc" />
                                </c:if>
                            </c:url>
                            <a title="Sort on Version" href="${idSortingUrl}">Version</a>
                        </th>
                    </c:if>
                    <th<c:if test="${param.sort_name eq 'STATUS'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
                        <c:url var="regStatusSortingUrl" value="/common_elms.jsp">
                            <c:forEach items="${param}" var="entry">
                                <c:if test="${entry.key != 'sort_name' and entry.key != 'sort_order'}">
                                    <c:param name="${entry.key}" value="${entry.value}" />
                                </c:if>
                            </c:forEach>
                            <c:param name="sort_name" value="STATUS" />
                            <c:if test="${param.sort_name eq 'STATUS' and param.sort_order ne 'desc'}">
                                <c:param name="sort_order" value="desc" />
                            </c:if>
                        </c:url>
                        <a title="Sort on Status" href="${regStatusSortingUrl}">Status</a>
                    </th>
                    <%
                    if (popup){
                        %>
                        <th>&nbsp;</th><%
                    }
                    %>
                </tr>
            </thead>
            <tbody>

            <%
                int displayed = 0;

                // get IDs of elements to exclude
                HashSet excludeIDs = new HashSet();
                String strExcludeIDs = request.getParameter("exclude");
                if (strExcludeIDs!=null && strExcludeIDs.length()>0){
                    StringTokenizer st = new StringTokenizer(strExcludeIDs, ",");
                     while (st.hasMoreTokens()) {
                         excludeIDs.add(st.nextToken());
                     }
                }

                // search results processing loop
                for (int i=0; i<dataElements.size(); i++){
                    DataElement dataElement = (DataElement)dataElements.get(i);
                    String delem_id = dataElement.getID();
                    String delem_name = dataElement.getShortName();
                    String delem_type = dataElement.getType();

                    String displayType = delem_type;
                    if (delem_type.equals("CH1")) {
                        displayType = "Fixed values - code list";
                    } else if (delem_type.equals("CH2")) {
                        displayType = "Quantitative";
                    } else if (delem_type.equals("CH3")) {
                        displayType = "Fixed values - vocabulary";
                    }

                    String workingUser = dataElement.getWorkingUser();
                    String status = dataElement.getStatus();

                    StringBuffer viewLink = new StringBuffer();
                    viewLink.append(request.getContextPath()).append("/dataelements/").append(delem_id);
                    if (popup){
                        viewLink.append("/?popup=");
                    }

                    StringBuffer selectLink = new StringBuffer("javascript:pickElem(");
                    selectLink.append(delem_id).append(",").append(displayed+1).append(")");

                    boolean clickable = status!=null ? !searchEngine.skipByRegStatus(status) : true;
                    if (clickable) {
                        if (excludeIDs.contains(delem_id)) {
                            clickable = false;
                        }
                    }
                    String strDisabled = clickable ? "" : " disabled=\"disabled\"";
                    String statusImg   = "images/" + Util.getStatusImage(status);
                    String statusTxt   = Util.getStatusRadics(status);
                    String zebraClass = (i + 1) % 2 != 0 ? "odd" : "even";
                %>

                    <tr class="<%=zebraClass%>">
                        <td<%=strDisabled%>>
                            <%
                            if (clickable){%>
                                <a href="<%=viewLink%>">
                                    <%=Util.processForDisplay(delem_name)%>
                                </a><%
                            } else { %>
                                <%=Util.processForDisplay(delem_name)%><%
                            }
                            // mark checked-out elements
                            if (user!=null && workingUser!=null){ %>
                                <span title="<%=workingUser%>" class="checkedout">*</span><%
                            }
                            %>
                        </td>
                        <td<%=strDisabled%>>
                            <%=Util.processForDisplay(displayType)%>
                        </td>
                        <c:if test="${param.incl_histver eq 'true'}">
                            <td<%=strDisabled%>>
                                <%=delem_id%>
                            </td>
                        </c:if>
                        <td<%=strDisabled%>>
                            <dd:datasetRegStatus value="<%=status%>" />
                            <%
                            if (status.equals("Released")){
                                String releaseDate = dataElement.getDate();
                                releaseDate = releaseDate==null ? "" : eionet.util.Util.releasedDateShort(Long.parseLong(releaseDate));
                                %>
                                <sup class="commonelm"><%=releaseDate%></sup><%
                            }
                            %>
                        </td>
                        <%
                        if (popup){
                            %>
                            <td>
                                <%if (clickable){
                                    %>
                                    [<a href="<%=selectLink%>" onclick="return warnStatus('<%=status%>')">select</a>]
                                <%}%>
                            </td>
                            <%
                        }
                        %>
                    </tr><%
                    displayed++;
                }
                %>
            </tbody>
        </table>
            <div style="display:none">
                <%
                if (popup){ %>
                    <input type="hidden" name="ctx" value="popup"/><%
                }
                %>
                <c:if test="${param.incl_histver eq 'true'}">
                    <input name="incl_histver" type="hidden" value="true"/>
                </c:if>
            </div>
        </form>

    </div> <!-- workarea -->
    <%
    if (!popup){
        %>
        </div> <!-- container -->
        <%@ include file="footer.jsp" %><%
    }
    %>
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
