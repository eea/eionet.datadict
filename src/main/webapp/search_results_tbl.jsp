<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String oSearchCacheAttrName="tbl_search_cache";%>
<%!final static String oSearchUrlAttrName="tbl_search_url";%>

<%@ include file="history.jsp" %>
<%@ include file="sorting.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oDsID;
    public String oShortName;
    public String oName;
    public String oFullName;
    public String oDsName;
    public String workingUser = null;
    public String regStatus = null;
    public boolean clickable = true;

    private String oCompStr=null;
    private int iO=0;

    public c_SearchResultEntry(String _oID,String _oDsID,String _oShortName,String _oName,String _oDsName) {

            oID        = _oID==null ? "" : _oID;
            oDsID  = _oDsID==null ? "" : _oDsID;
            oShortName    = _oShortName==null ? "" : _oShortName;
            oName= _oName==null ? "" : _oName;
            oDsName    = _oDsName==null ? "" : _oDsName;

            oFullName = oName;

            if (oName.length() > 60)
                oName = oName.substring(0,60) + " ...";
    };

    public void setComp(int i,int o) {
        switch(i) {
            case 2: oCompStr=oDsName; break;
            case 3: oCompStr=oName; break;
            case 4: oCompStr=regStatus; break;
            default: oCompStr=oShortName; break;
            }
        iO=o;
        }

    public String toString() {
        return oCompStr;
    }

    public int compareTo(Object oC1) {
        return iO*oCompStr.compareToIgnoreCase(oC1.toString());
    }

}%>

<%!class c_SearchResultSet {
    private boolean isSorted=false;
    private int iSortColumn=0;
    private int iSortOrder=0;
    public boolean isAuth = false;

    public Vector oElements;
    public boolean SortByColumn(Integer oCol,Integer oOrder) {
        if ((iSortColumn!=oCol.intValue()) || (iSortOrder!=oOrder.intValue())) {
            for(int i=0; i<oElements.size(); i++) {
                c_SearchResultEntry oEntry=(c_SearchResultEntry)oElements.elementAt(i);
                oEntry.setComp(oCol.intValue(),oOrder.intValue());
            }
            Collections.sort(oElements);
            return true;
        }
        return false;
    }
}%>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    ServletContext ctx = getServletContext();

    DDUser user = SecurityUtil.getUser(request);

    String short_name = request.getParameter("short_name");
    String idfier = request.getParameter("idfier");
    String full_name = request.getParameter("full_name");
    String definition = request.getParameter("definition");

    Integer oSortCol=null;
    Integer oSortOrder=null;
    try {
        oSortCol=new Integer(request.getParameter("sort_column"));
        oSortOrder=new Integer(request.getParameter("sort_order"));
    }
    catch(Exception e){
        oSortCol=null;
        oSortOrder=null;
    }

    // if this is no sorting request, then remember the query string in session in order to come back if needed
    if (oSortCol==null){
        String query = request.getQueryString() == null ? "" : request.getQueryString();
        String searchUrl =  request.getRequestURI() + "?" + query;

           session.setAttribute(oSearchUrlAttrName, searchUrl);
       }

       // The following if block tries to identify if a login has happened in which
    // case it will redirect the response to the query string in session. This
    // happens regardless of weather it's a sorting request or search request.
    c_SearchResultSet rs = (c_SearchResultSet)session.getAttribute(oSearchCacheAttrName);
    if (rs!=null){
        if (rs.isAuth && user==null || !rs.isAuth && user!=null){
            session.removeAttribute(oSearchCacheAttrName);

            response.sendRedirect((String)session.getAttribute(oSearchUrlAttrName));
        }
    }

    String pageMode = request.getParameter("sort_column")!=null ? "sort" : "search";
    String tableLink="";
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Tables - Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[

        function showSortedList(clmn,ordr) {
            if ((document.forms["sort_form"].elements["sort_column"].value != clmn)
                   || (document.forms["sort_form"].elements["sort_order"].value != ordr)) {
                document.forms["sort_form"].elements["sort_column"].value=clmn;
                document.forms["sort_form"].elements["sort_order"].value=ordr;
                document.forms["sort_form"].submit();
            }
        }
    // ]]>
    </script>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Tables"/>
        <jsp:param name="helpscreen" value="tables"/>
    </jsp:include>
    <c:set var="currentSection" value="tables" />
    <%@ include file="/pages/common/navigation.jsp" %>

<div id="workarea">
    <h1>Tables from latest versions of datasets in any status</h1>
    <%
    if (user==null){%>
        <p class="advise-msg">
            Note: Tables from datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.
        </p><%
    }
    %>

    <!-- search buttons -->
    <div id="drop-operations">
        <ul>
            <li class="search"><a href="search_table.jsp">Search</a></li>
        </ul>
    </div>


        <!-- the result table -->
        <table width="100%" class="sortable results" style="clear:both">
         <col style="width:34%"/>
         <col style="width:22%"/>
         <col style="width:22%"/>
         <col style="width:22%"/>
            <thead>
            <tr>
                <th>
                    <%
                    String sortedImg  = getSortedImg(3, oSortCol, oSortOrder);
                    String sortedLink = getSortedLink(3, oSortCol, oSortOrder);
                    %>
                    <a title="Sort on Table" href="<%=Util.processForDisplay(sortedLink, true)%>">
                          Full name&nbsp;<img src="<%=Util.processForDisplay(sortedImg, true)%>" width="12" height="12" alt=""/>
                    </a>
                </th>
                <th>
                    <%
                    sortedImg  = getSortedImg(1, oSortCol, oSortOrder);
                    sortedLink = getSortedLink(1, oSortCol, oSortOrder);
                    %>
                    <a title="Sort on Short name" href="<%=Util.processForDisplay(sortedLink, true)%>">
                          Short name&nbsp;<img src="<%=Util.processForDisplay(sortedImg, true)%>" width="12" height="12" alt=""/>
                    </a>
                </th>
                <th>
                    <%
                    sortedImg  = getSortedImg(2, oSortCol, oSortOrder);
                    sortedLink = getSortedLink(2, oSortCol, oSortOrder);
                    %>
                    <a title="Sort on Dataset" href="<%=Util.processForDisplay(sortedLink, true)%>">
                          Dataset&nbsp;<img src="<%=Util.processForDisplay(sortedImg, true)%>" width="12" height="12" alt=""/>
                    </a>
                </th>
                <th>
                    <%
                    sortedImg  = getSortedImg(4, oSortCol, oSortOrder);
                    sortedLink = getSortedLink(4, oSortCol, oSortOrder);
                    %>
                    <a title="Sort on Dataset status" href="<%=Util.processForDisplay(sortedLink,true)%>">
                          Dataset status&nbsp;<img src="<%=Util.processForDisplay(sortedImg,true)%>" width="12" height="12" alt=""/>
                    </a>
                </th>
            </tr>
            </thead>
            <tbody>

            <%

            Connection conn = null;

            try { // start the whole page try block

            if (pageMode.equals("search")){

                session.removeAttribute(oSearchCacheAttrName);

                try {

                    // we establish a database connection and create a search engine
                    conn = ConnectionUtil.getConnection();
                    DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
                    searchEngine.setUser(user);

                    String srchType = request.getParameter("search_precision");
                    String oper="=";
                    if (srchType != null && srchType.equals("free"))
                        oper=" match ";
                    if (srchType != null && srchType.equals("substr"))
                        oper=" like ";

                    Vector params = new Vector();
                    Enumeration parNames = request.getParameterNames();
                    while (parNames.hasMoreElements()){
                        String parName = (String)parNames.nextElement();
                        if (!parName.startsWith(ATTR_PREFIX))
                            continue;

                        String parValue = request.getParameter(parName);
                        if (parValue.length()==0)
                            continue;

                        DDSearchParameter param =
                            new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");

                        if (oper!= null && oper.trim().equalsIgnoreCase("like"))
                            param.addValue("'%" + parValue + "%'");
                        else
                            param.addValue("'" + parValue + "'");
                        params.add(param);
                    }

                    Vector dsTables = searchEngine.getDatasetTables(params, short_name, idfier, full_name, definition, oper);

                    // see if any result were found
                    if (dsTables == null || dsTables.size()==0){ %>
                        <tr>
                            <td colspan="3"><br/>
                                <%
                                // prepare message trailer for un-authenticated users
                                String msgTrailer = user==null ? " for un-authenticated users" : "";

                                // see if this is a search or just listing all the tables
                                if (Util.isEmpty(request.getParameter("search_precision"))){ // listing all the tables
                                    %>
                                    <b>No table definitions were found<%=Util.processForDisplay(msgTrailer)%>!</b><%
                                }
                                else{ // a search
                                    %>
                                    <b>No table definitions matching the search criteria were found<%=Util.processForDisplay(msgTrailer)%>!</b><%
                                }
                                %>
                            </td>
                        </tr>
                        </table></form></div></body></html> <%
                        return;
                    }

                    DElemAttribute attr = null;

                    c_SearchResultSet oResultSet=new c_SearchResultSet();
                    oResultSet.isAuth = user!=null;
                    oResultSet.oElements=new Vector();
                    session.setAttribute(oSearchCacheAttrName,oResultSet);

                    for (int i=0; i<dsTables.size(); i++){
                        DsTable table = (DsTable)dsTables.get(i);
                        String table_id = table.getID();
                        String table_name = table.getShortName();
                        String ds_id = table.getDatasetID();
                        String ds_name = table.getDatasetName();
                        String dsNs = table.getParentNs();
                        String tblName = table.getName()==null ? "" : table.getName();
                        tblName = tblName.length()>60 && tblName != null ? tblName.substring(0,60) + " ..." : tblName;
                        String tblFullName = tblName;
                        String workingUser = table.getDstWorkingUser();

                        tableLink = request.getContextPath() + "/tables/" + table_id;

                        String zebraClass = (i + 1) % 2 != 0 ? "odd" : "even";
                        String regStatus = table.getDstStatus();
                        boolean clickable = regStatus!=null ? !searchEngine.skipByRegStatus(regStatus) : true;
                        String strDisabled = clickable ? "" : " class=\"disabled\"";
                        String statusImg   = "images/" + Util.getStatusImage(regStatus);
                        String statusTxt   = Util.getStatusRadics(regStatus);

                        c_SearchResultEntry oEntry = new c_SearchResultEntry(table_id,
                                                                             ds_id,
                                                                             table_name,
                                                                             tblFullName,
                                                                             ds_name);
                        oEntry.workingUser = workingUser;
                        oEntry.clickable = clickable;
                        oEntry.regStatus = regStatus;
                        oResultSet.oElements.add(oEntry);
                        %>
                        <tr class="<%=zebraClass%>">
                            <%
                            // 1st column, table full name
                            %>
                            <td <%=strDisabled%>>
                                <%
                                if (clickable){%>
                                    <a href="<%=tableLink%>">
                                        <%=Util.processForDisplay(tblFullName)%>
                                    </a><%
                                }
                                else{%>
                                    <%=Util.processForDisplay(tblFullName)%><%
                                }
                                %>
                            </td>
                            <%
                            // 2nd column, table short name
                            %>
                            <td>
                                <%=Util.processForDisplay(table_name)%>
                            </td>
                            <%
                            // 3rd column, dataset short name
                            %>
                            <td>
                                <%=Util.processForDisplay(ds_name)%>
                                <%
                                // mark checked-out datasets
                                if (user!=null && workingUser!=null){ %>
                                    <div title="<%=workingUser%>" class="checkedout">*</div><%
                                }
                                %>
                            </td>
                            <%
                            // 4th column, dataset status
                            %>
                            <td>
                                <%
                                if (clickable){ %>
                                    <img style="border:0" src="<%=Util.processForDisplay(statusImg)%>" width="56" height="12" title="<%=regStatus%>" alt="<%=regStatus%>"/><%
                                }
                                else{ %>
                                    <span style="color:gray;text-decoration:none;font-size:8pt" title="<%=regStatus%>">
                                        <strong><%=statusTxt%></strong>
                                    </span><%
                                }
                                %>
                            </td>
                        </tr>
                        <%
                    }
                    %>
            </tbody>
        </table>
        <p>Total results: <%=dsTables.size()%></p><%
                }
                catch(Exception e){
                    %><b>ERROR: <%=e%></b><%
                }
            }
            else{
                // No search - return from another result set or a total stranger...
                c_SearchResultSet oResultSet=(c_SearchResultSet)session.getAttribute(oSearchCacheAttrName);
                if (oResultSet==null) {
                    %><p>This page has experienced a time-out. Try searching again.</p><%
                }
                else {
                    if ((oSortCol!=null) && (oSortOrder!=null))
                        oResultSet.SortByColumn(oSortCol,oSortOrder);

                    c_SearchResultEntry oEntry;

                    // loop over search result set
                    for (int i=0;i<oResultSet.oElements.size();i++){

                        oEntry=(c_SearchResultEntry)oResultSet.oElements.elementAt(i);
                        String strDisabled = oEntry.clickable ? "" : " class=\"disabled\"";
                        String zebraClass = (i + 1) % 2 != 0 ? "odd" : "even";
                        tableLink = request.getContextPath() + "/tables/" + oEntry.oID;
                        String statusImg   = "images/" + Util.getStatusImage(oEntry.regStatus);
                        String statusTxt   = Util.getStatusRadics(oEntry.regStatus);

                        %>
                        <tr class="<%=zebraClass%>">
                            <%
                            // 1st column, table full name
                            %>
                            <td<%=strDisabled%>>
                                <%
                                if (oEntry.clickable){%>
                                    <a href="<%=tableLink%>">
                                        <%=Util.processForDisplay(oEntry.oName)%>
                                    </a><%
                                }
                                else{%>
                                    <%=Util.processForDisplay(oEntry.oName)%><%
                                }
                                %>
                            </td>
                            <%
                            // 2nd column, table short name
                            %>
                            <td>
                                <%=Util.processForDisplay(oEntry.oShortName)%>
                            </td>
                            <%
                            // 3rd column, dataset short name
                            %>
                            <td>
                                <%=Util.processForDisplay(oEntry.oDsName)%>
                                <%
                                // mark checked-out datasets
                                if (user!=null && oEntry.workingUser!=null){ %>
                                    <div title="<%=oEntry.workingUser%>" class="checkedout">*</div><%
                                }
                                %>
                            </td>
                            <%
                            // 4th column, dataset status
                            %>
                            <td>
                                <%
                                if (oEntry.clickable){ %>
                                    <img style="border:0" src="<%=Util.processForDisplay(statusImg)%>" width="56" height="12" title="<%=oEntry.regStatus%>" alt="<%=oEntry.regStatus%>"/><%
                                }
                                else{ %>
                                    <span style="color:gray;text-decoration:none;font-size:8pt" title="<%=oEntry.regStatus%>">
                                        <strong><%=statusTxt%></strong>
                                    </span><%
                                }
                                %>
                            </td>
                        </tr>
                        <%
                    }
                    %>
            </tbody>
        </table>
        <p>Total results: <%=oResultSet.oElements.size()%></p><%
               }
        }
        %>

        <form id="sort_form" action="search_results_tbl.jsp" method="get">
            <div style="display:none">
                <input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
                <input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
            </div>
        </form>

            </div> <!-- workarea -->
            </div> <!-- container -->
      <%@ include file="footer.jsp" %>
    </body>
</html>

<%
// end the whole page try block
}
finally {
    try { if (conn!=null) conn.close();
    } catch (SQLException e) {}
}
%>
