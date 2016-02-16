<%@ page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.dao.domain.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil,org.apache.commons.lang.StringUtils"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String oSearchCacheAttrName="datasets_search_cache";%>
<%!final static String oSearchUrlAttrName="datasets_search_url";%>
<%!private boolean restore = false;%>
<%!
    private Vector attrs = null;
    private Vector def_attrs = null;
    private Vector attr_ids = null;
    private Hashtable inputAttributes = null;

    private String getAttributeIdByName(String name) {
        for (int i=0; i<attrs.size(); i++) {
            DElemAttribute attr = (DElemAttribute)attrs.get(i);
            if (attr.getName().equalsIgnoreCase(name)) {
                return attr.getID();
            }
        }
        return null;
    }

    private String getAttributeNameById(String id) {
        for (int i=0; i<attrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)attrs.get(i);
            if (attr.getID().equals(id)) {
                return attr.getName();
            }
        }
        return null;
    }

    private String setDefaultAttrs(String name) {
        String id = getAttributeIdByName(name);
        if (id != null) {
            def_attrs.add(id);
        }  
        return null;
    }
%>

<%@ include file="history.jsp" %>
<%@ include file="sorting.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oShortName;
    public String oFullName;
    public String oFName;  //truncated full name
    public String oVersion;
    public Vector oTables;
    public String oIdentifier;

    private String oCompStr = null;
    private int iO = 0;

    private String regStatus = "";
    private String sortableStatus = "";
    public boolean clickable = false;
    public String workingUser = null;
    public boolean canDelete = false;

    public c_SearchResultEntry(String _oID, String _oShortName, String _oVersion, String _oFName, Vector _oTables) {
        oID = _oID == null ? "" : _oID;
        oShortName = _oShortName == null ? "" : _oShortName;
        oFName = _oFName == null ? "" : _oFName;
        oVersion = _oVersion == null ? "" : _oVersion;
        oTables = _oTables;
        oFullName = oFName;

        if (oFName.length() > 64) {
            oFName = oFName.substring(0,60) + " ...";
        }
    }

    public void setComp(int i, int o) {
        switch(i) {
            case 1: oCompStr=oFName; break;
            case 2: oCompStr=sortableStatus; break;
            case 3: oCompStr=oID; break;
            default: oCompStr=oFName; break;
        }
        iO=o;
    }

    public String toString() {
        return oCompStr;
    }

    public int compareTo(Object oC1) {
        return iO*oCompStr.compareToIgnoreCase(oC1.toString());
    }

    public void setRegStatus(String stat) {
        regStatus = stat;
    }

    public String getRegStatus() {
        return regStatus;
    }

    public void setSortableStatus(String sortableStatus) {
        this.sortableStatus = sortableStatus;
    }
}%>

<%!class c_SearchResultSet {
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

    Integer oSortCol=null;
    Integer oSortOrder=null;
    
    try {
        oSortCol=new Integer(request.getParameter("sort_column"));
        oSortOrder=new Integer(request.getParameter("sort_order"));
    } catch(Exception e) {
        oSortCol=null;
        oSortOrder=null;
    }

    // if this is no sorting request, then remember the query string in session in order to come back if needed
    if (oSortCol==null) {
        String query = request.getQueryString() == null ? "" : request.getQueryString();
        String searchUrl =  request.getRequestURI() + "?" + query;
        session.setAttribute(oSearchUrlAttrName, searchUrl);
    }

    Vector datasets = null;
    DDSearchEngine searchEngine = null;
    Connection conn = null;
    DDUser user = SecurityUtil.getUser(request);
    String _isSearchForWorkingCopies = request.getParameter("wrk_copies");
    boolean isSearchForWorkingCopies = (_isSearchForWorkingCopies!=null && _isSearchForWorkingCopies.equals("true")) ? true : false;
    boolean isIncludeHistoricVersions = request.getParameter("incl_histver")!=null && request.getParameter("incl_histver").equals("true");
    String feedbackValue = null;

     // Feedback messages
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("checkout")) {
        feedbackValue = "Working copy successfully created!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("checkin")) {
        feedbackValue = "Check-in successful!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("undo_checkout")) {
        feedbackValue = "Working copy successfully discarded!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("delete")) {
        feedbackValue = "Deletion successful!";
    }

    try { // start the whole page try block
        
        String search_precision = request.getParameter("search_precision");
        String attrID = null;
        String attrValue = null;
        String attrName = null;
        StringBuffer collect_attrs = new StringBuffer();
        HashSet displayedCriteria = new HashSet();
        String sel_attr = request.getParameter("sel_attr");
        String sel_type = request.getParameter("sel_type");
        String short_name = request.getParameter("short_name");
        String idfier = request.getParameter("idfier");
        
        conn = ConnectionUtil.getConnection();

        if (request.getMethod().equals("POST")) {
            if (user==null) { %>
                <b>Not allowed!</b> <%
                return;
            } else {
                String[] ds_ids = request.getParameterValues("ds_id");
                for (int i=0; ds_ids!=null && i<ds_ids.length; i++) {
                    String dsIdf = request.getParameter("ds_idf_" + ds_ids[i]);
                    if (dsIdf==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "d")){ %>
                        <b>Not allowed!</b><%
                    }
                }
            }

            Connection userConn = null;
            DatasetHandler handler = null;

            try {
                userConn = user.getConnection();
                handler = new DatasetHandler(userConn, request, ctx);
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
        }

        session.removeAttribute(oSearchCacheAttrName);

        searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);

        // Begin search_dataset.jsp
        attrs = searchEngine.getDElemAttributes();
        if (attrs == null) {
            attrs = new Vector();
        }

        attr_ids = new Vector();
        def_attrs = new Vector();

        setDefaultAttrs("Name");
        setDefaultAttrs("Definition");
        setDefaultAttrs("Keywords");
        setDefaultAttrs("EEAissue");

        if (sel_attr == null) {
            sel_attr = "";
        }
        if (sel_type == null) {
            sel_type = "";
        }
        if (short_name == null) {
            short_name = "";
        }
        if (idfier == null) {
            idfier = "";
        }
        if (search_precision == null) {
            search_precision = "substr";
        }

        // get inserted attributes
        String input_attr;
        inputAttributes = new Hashtable();
        for (int i=0; i<attrs.size(); i++) {
            DElemAttribute attribute = (DElemAttribute)attrs.get(i);
            String attr_id = attribute.getID();

            input_attr = request.getParameter("attr_" + attr_id);
            if (input_attr!=null) {
                inputAttributes.put(attr_id, input_attr);
                attr_ids.add(attr_id);
            }
        }

        String srchType = request.getParameter("search_precision");
        String oper = "=";
        if (srchType != null && srchType.equals("free")) {
            oper = " match ";
        }
        if (srchType != null && srchType.equals("substr")) {
            oper = " like ";
        }

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

            DDSearchParameter param =
                new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");

            if (oper!= null && oper.trim().equalsIgnoreCase("like")) {
                param.addValue("%" + parValue + "%");
            } else {
                param.addValue(parValue);
            }
            params.add(param);
        }
       // String short_name = request.getParameter("short_name");
       //  String idfier = request.getParameter("idfier");
        String version = request.getParameter("version");

        // see if looking for deleted datasets
        String _restore = request.getParameter("restore");
        if (_restore!=null && _restore.equals("true")) {
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
            restore = true;
            datasets = searchEngine.getDeletedDatasets();
        } else {
            HashSet statuses = null;
            String requestedStatus = request.getParameter("regStatus");
            if (requestedStatus!=null && requestedStatus.length()>0) {
                statuses = new HashSet();
                statuses.add(requestedStatus);
            }
            datasets = searchEngine.getDatasets(params, short_name, idfier, version, oper, isSearchForWorkingCopies, isIncludeHistoricVersions, statuses);
            request.setAttribute("registrationStatuses", DatasetRegStatus.values());
            request.setAttribute("viewName", "datasets");

            DataSetSort sort = null;
            String sortName = (String) request.getParameter("sort_name");
            if (StringUtils.isNotBlank(sortName) && DataSetSort.valueOf(sortName) != null) {
                sort = DataSetSort.valueOf(sortName);
            } else {
                sort = DataSetSort.NAME; // fall-back
            }
            String sortOrder = (String) request.getParameter("sort_order");
            boolean descending = StringUtils.isNotBlank(sortOrder) && sortOrder.equals("desc");
            Collections.sort(datasets, sort.getComparator(descending));
            request.setAttribute("user", user);
        }
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Datasets - Data Dictionary</title>
    <script type="text/javascript" src="modal_dialog.js"></script>
    <script type="text/javascript">
    // <![CDATA[
        function setLocation() {
            if (document.forms["form1"].searchUrl) {
                document.forms["form1"].searchUrl.value = document.location.href;
            }
        }

        function goTo(mode) {
            if (mode == "add") {
                document.location.assign("<%=request.getContextPath()%>/datasets/add");
            }
        }

        function showSortedList(clmn, ordr) {
            if ((document.forms["sort_form"].elements["sort_column"].value != clmn)
                   || (document.forms["sort_form"].elements["sort_order"].value != ordr)) {
                document.forms["sort_form"].elements["sort_column"].value=clmn;
                document.forms["sort_form"].elements["sort_order"].value=ordr;
                document.forms["sort_form"].submit();
            }
        }

        function deleteDataset() {
            // first confirm if the deletetion is about to take place at all
            var b = confirm("Selected datasets will be deleted! You will be given a chance to delete them permanently or save them for restoring later. Click OK, if you want to continue. Otherwise click Cancel.");
            if (b == false) {
                return;
            }
            
            // get all checkboxes and control if there is any cannot delete or released dataset
            var cannotDeletedDatasetIds = new Array();
            var releasedDatasetIds = new Array();
            var checkboxes = document.getElementsByName("ds_id");  
            var found = false;
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].checked) {
                    var datasetId = checkboxes[i].value;
                    var canDeleteThisDataset = document.getElementById("can_delete_ds_idf_" + datasetId);
                    //if there is an attempt to delete unauthorized datasets, uncheck them
                    if (canDeleteThisDataset.value == 'false') {
                        checkboxes[i].checked = false;
                        cannotDeletedDatasetIds.push(datasetId);
                        continue;
                    }

                    if(document.getElementById("released_ds_idf_" + datasetId).value == 'true') {
                        releasedDatasetIds.push(datasetId);
                    }
                    found = true;
                }
            }

            // if there is an attempt to delete unauthorized datasets, notify user
            if (cannotDeletedDatasetIds.length > 0) {
                var promptMessage = "You don't have permission to delete following datasets: ";
                for (var i = 0; i < cannotDeletedDatasetIds.length; i++ ) {
                    promptMessage += "\n  -  " + document.getElementById("ds_idf_" + cannotDeletedDatasetIds[i]).value;
                }
                alert(promptMessage);
            }

            // if nothing selected no need to continue
            if (!found) {
                alert("Select at least one Dataset to continue");
                return;
            }

            // if there is an attempt to delete released datasets, ask user for confirmation
            if (releasedDatasetIds.length > 0) {
                var promptMessage = "Following datasets are RELEASED: ";
                for (var i = 0; i < releasedDatasetIds.length; i++ ) {
                    promptMessage += "\n  -  " + document.getElementById("ds_idf_" + releasedDatasetIds[i]).value;
                }
                promptMessage += "\nAre you sure you want to delete?\nClick OK, if you want to continue. Otherwise click Cancel.";
                var b2 = confirm(promptMessage);
                if (b2 == false) {
                    return;
                }
            }

            // now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
            openNoYes("yesno_dialog.html", "Do you want the selected datasets to be deleted permanently?\n(Note that working copies will always be permanently deleted)", delDialogReturn,100, 400);
        }

        function generateCombinedPdf() {
            var checkboxes = document.getElementsByName("ds_id");
            var checkedDatasets = new Array(); 
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].checked) {
                    checkedDatasets.push(checkboxes[i].value);
                }
            }

            // if nothing selected no need to continue
            if (checkedDatasets == 0) {
               alert("Select at least one Dataset to continue");
               return;
            }

            var objectIds = "obj_id=";
            for (var i = 0; i < checkedDatasets.length; i++) {
                objectIds += checkedDatasets[i] + ":";
            }

            var urlSuffix = "/GetPrintout?format=PDF&obj_type=DST&" + objectIds + "&out_type=GDLN";
            var ctx = "${pageContext.request.contextPath}";
            window.location = (ctx + urlSuffix);
        }

        function delDialogReturn() {
            var v = dialogWin.returnValue;
            if (v==null || v=="" || v=="cancel") {
                return;
            }
            document.forms["form1"].elements["complete"].value = v;
            deleteDatasetReady();
        }

        function deleteDatasetReady() {
            document.forms["form1"].elements["mode"].value = "delete";
            document.forms["form1"].submit();
        }

        function restoreDataset() {
            document.forms["form1"].elements["mode"].value = "restore";
            document.forms["form1"].submit();
        }

        function alertReleased(chkbox) {
            if (chkbox.checked==true) {
                alert("Please note that you selected a dataset in Released status!");
            }
        }

        function doLoad() {
            <%
                if (search_precision != null) {
                %>
                    var sPrecision = '<%=search_precision%>';
                    var o = document.forms["searchDatasetsForm"].search_precision;
                    for (i=0; o!=null && i<o.length; i++){
                        if (o[i].value == sPrecision) {
                            o[i].checked = true;
                            break;
                        }
                    }
                <%
                }
            %>

            if (document.forms["form1"] && document.forms["form1"].elements["count_checkboxes"] && document.forms["form1"].elements["del_button"]){
                if (document.forms["form1"].elements["count_checkboxes"].value <= 0){
                    document.forms["form1"].elements["del_button"].disabled = true;
                }
            }
        }

        function submitForm(action){
            document.forms["searchDatasetsForm"].action=action;
            document.forms["searchDatasetsForm"].submit();
        }

        function selAttr(id, type){
            document.forms["searchDatasetsForm"].sel_attr.value=id;
            document.forms["searchDatasetsForm"].sel_type.value=type;
            submitForm('datasets.jsp');
        }

        $(function() {
            $(".selectable").click(function () {
                if ($(this).is(":checked")) {
                    $(this).parents("tr").addClass("selected");
                } else {
                    $(this).parents("tr").removeClass("selected");
                }
            });
        });
    // ]]>
    </script>
</head>
<body onload="doLoad()">
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Datasets"/>
        <jsp:param name="helpscreen" value="datasets"/>
    </jsp:include>
    <%@ include file="nmenu.jsp" %>
    <div id="workarea">
        <%
                    if (feedbackValue != null) {
                    %>
                        <div class="system-msg">
                        <%= feedbackValue %>
                        </div>
                    <%
                    }

                    if (!restore && isSearchForWorkingCopies){ %>
                        <h1>Working copies of dataset definitions</h1><%
                    }
                    else if (!restore){
                        String strAllOrLatest = isIncludeHistoricVersions ? "All " : "Latest";
                        %>
                        <h1><%=strAllOrLatest%> versions of datasets in any status</h1><%
                    }
                    else{%>
                        <h1>Restore datasets</h1><%
                    }
            if (user == null) { %>
                <p class="advise-msg">Note: Datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.</p><%
            }
            %>
        
        <!-- search, restore -->
        <div id="drop-operations">
        <h2>Operations</h2>
        <ul>
            <li><a href="search_dataset.jsp" title="Search datasets">Search</a></li>
            <%
            if (user != null){
            %>
                    <%
                    if (user.isAuthentic() && !restore){%>
                        <li><a href="restore_datasets.jsp?SearchType=SEARCH&amp;restore=true" title="Restore datasets">Restore</a></li><%
                    }
                    // update buttons
                    if (!isSearchForWorkingCopies && SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")) {
                    %>
                    <li><a href="<%=request.getContextPath()%>/datasets/add">Add</a></li>
                    <%
                    }
                    if (!isSearchForWorkingCopies) {
                    %>
                    <li><a href="javascript:deleteDataset()">Delete selected</a></li>
                    <%
                    }
                    
                    if (user.isAuthentic()){%>
                    <li><a href="javascript:generateCombinedPdf()">Generate PDF of selected</a></li><%
                }
                    %>


            <%
            }
            %>
                </ul>
                </div>
            <form id="searchDatasetsForm" action="datasets.jsp" method="get">
                <h1>Search datasets</h1>
                <table width="600" cellspacing="0" style="padding-top:10px">
                    <col style="width: 14em"/>
                    <col style="width: 16px"/>
                    <col span="2"/>
                    <tr>
                        <td align="right">
                            <label for="regStatus" class="question">Registration Status</label>
                        </td>
                        <td>
                            <a class="helpButton" href="help.jsp?screen=dataset&amp;area=regstatus">
                                <img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
                            </a>
                        </td>
                        <td colspan="2">
                            <select name="regStatus" id="regStatus" class="small">
                                <option value="">All</option>
                                <c:forEach items="${registrationStatuses}" var="status">
                                    <option value="${fn:escapeXml(status.name)}" ${param.regStatus eq status.name ? 'selected="selected"' : ''}>${fn:escapeXml(status.name)}</option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>

                    <tr style="vertical-align:top">
                        <td align="right">
                            <label for="short_name" class="question">Short name</label>
                        </td>
                        <td>
                            <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name">
                                <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                            </a>
                        </td>
                        <td colspan="2">
                            <input type="text" class="smalltext" size="59" name="short_name" id="short_name" value="${fn:escapeXml(param.short_name)}"/>
                        </td>
                    </tr>

                    <tr style="vertical-align:top">
                        <td align="right">
                            <label class="question">Identifier</label>
                        </td>
                        <td>
                            <a class="helpButton" href="help.jsp?screen=dataset&amp;area=identifier">
                                <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                            </a>
                        </td>
                        <td colspan="2">
                            <input type="text" class="smalltext" size="59" name="idfier" value="${fn:escapeXml(param.idfier)}"/>
                        </td>
                    </tr>
                    <%
                        // get default attributes, which are always on the page (defined above)
                    if (def_attrs!=null) {
                            for (int i=0; i < def_attrs.size(); i++) {
                                attrID = (String) def_attrs.get(i);
                                attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
                                attrName = getAttributeNameById(attrID);

                                if (inputAttributes.containsKey(attrID)) {
                                    inputAttributes.remove(attrID);
                                }

                                if (attrID!=null) {
                                    collect_attrs.append(attrID + "|");
                                    displayedCriteria.add(attrID);
                    %>
                    <tr style="vertical-align:top">
                        <td align="right">
                            <label class="question"><%=Util.processForDisplay(attrName)%></label>
                        </td>
                        <td>
                            <a class="helpButton" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE">
                                <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                            </a>
                        </td>
                        <td colspan="2">
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=Util.processForDisplay(attrValue, true)%>"/>
                        </td>
                    </tr>
                    <%
                                }
                            }
                        }
                        // get attributes selected from picked list (get the ids from url)
                        if (attr_ids!=null) {
                            for (int i=0; i < attr_ids.size(); i++) {
                                attrID = (String)attr_ids.get(i);

                                if (!inputAttributes.containsKey(attrID)) {
                                    continue;
                                }
                                if (sel_type.equals("remove") && attrID.equals(sel_attr)) {
                                    continue;
                                }

                                attrName = getAttributeNameById(attrID);

                                attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
                                if (attrValue == null) {
                                    attrValue = "";
                                }
                                collect_attrs.append(attrID + "|");
                                displayedCriteria.add(attrID);
                    %>
                    <tr style="vertical-align:top">
                        <td align="right">
                            <label class="question"><%=Util.processForDisplay(attrName)%></label>
                        </td>
                        <td>
                            <a class="helpButton" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE">
                                <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                            </a>
                        </td>
                        <td>
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59" value="<%=Util.processForDisplay(attrValue, true)%>"/>
                        </td>
                        <td>
                            <a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" style="border:0" alt="Remove attribute from search criterias"/></a>
                        </td>
                    </tr>
                    <%
                            }
                        }
                        // add the last selection
                        if (sel_type!=null && sel_attr!=null) {
                            if (sel_type.equals("add")) {
                                attrID = sel_attr;
                                collect_attrs.append(attrID + "|");
                                displayedCriteria.add(attrID);
                                attrName = getAttributeNameById(attrID);
                    %>
                    <tr style="vertical-align:top">
                        <td align="right">
                            <label class="question"><%=Util.processForDisplay(attrName)%></label>
                        </td>
                        <td>
                            <a class="helpButton" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE">
                                <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                            </a>
                        </td>
                        <td>
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59" value=""/>
                        </td>
                        <td>
                            <a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" style="border:0" alt="Remove attribute from search criterias"/></a>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td colspan="2">
                            <input type="radio" name="search_precision" id="ssubstr" value="substr" ${param.search_precision ne 'exact' ? 'checked="checked"' : ''} /><label for="ssubstr">Substring search</label>
                            <input type="radio" name="search_precision" id="sexact" value="exact" ${param.search_precision eq 'exact' ? 'checked="checked"' : ''} /><label for="sexact">Exact search</label>
                        </td>
                    </tr>
                    <%
                        // if authenticated user, enable to get working copies only
                        if (user!=null && user.isAuthentic()) {
                    %>
                    <tr style="vertical-align:top">
                        <td colspan="2"></td>
                        <td colspan="2">
                            <input type="checkbox" name="wrk_copies" id="wrk_copies" value="true" ${param.wrk_copies eq 'true' ? 'checked="checked"' : ''} />
                            <label for="wrk_copies" class="smallfont">Working copies only</label>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    <tr style="vertical-align:top">
                        <td colspan="2"></td>
                        <td colspan="2">
                            <input type="checkbox" name="incl_histver" id="incl_histver" value="true" ${param.incl_histver eq 'true' ? 'checked="checked"' : ''} />
                            <label for="incl_histver" class="smallfont">Include historic versions</label>
                        </td>
                    </tr>
                    <tr style="vertical-align:top">
                        <td colspan="2"></td>
                        <td>
                            <input class="mediumbuttonb" type="button" value="Search" onclick="submitForm('datasets.jsp')"/>
                            <input class="mediumbuttonb" type="reset" value="Reset"/>
                        </td>
                    </tr>
                    <%
                        Vector addCriteria = new Vector();
                        for (int i=0; attrs!=null && i<attrs.size(); i++) {
                            DElemAttribute attribute = (DElemAttribute) attrs.get(i);
                            if (!attribute.displayFor("DST")) {
                                continue;
                            }

                            if (!displayedCriteria.contains(attribute.getID())) {
                                Hashtable hash = new Hashtable();
                                hash.put("id", attribute.getID());
                                hash.put("name", attribute.getName());
                                addCriteria.add(hash);
                            }
                        }

                        if (addCriteria.size()>0) {
                    %>
                    <tr>
                        <td colspan="4" style="text-align:right">
                            <label for="add_criteria">Add criteria</label>
                            <select name="add_criteria" id="add_criteria" onchange="selAttr(this.options[this.selectedIndex].value, 'add')">
                                <option value=""></option>
                                <%
                                    for (int i=0; i<addCriteria.size(); i++) {
                                        Hashtable hash = (Hashtable)addCriteria.get(i);
                                %>
                                    <option value="<%=hash.get("id")%>"><%=hash.get("name")%></option>
                                <%}%>
                            </select>
                        </td>
                    </tr>
                    <%}%>
                </table>
                <!-- table for 'Add' -->
                <div style="display:none">
                    <input type="hidden" name="sel_attr" value=""/>
                    <input type="hidden" name="sel_type" value=""/>
                    <input type="hidden" name="type" value="DST"/>
                    <!-- collect all the attributes already used in criterias -->
                    <input type="hidden" name="collect_attrs" value="<%=Util.processForDisplay(collect_attrs.toString(), true)%>"/>
                </div>
            </form>
            <form id="form1" method="post" action="datasets.jsp" onsubmit="setLocation()">
            <!-- the buttons part -->
                <%
                    // check if any results found
                    if (datasets == null || datasets.size()==0) {

                        // see if this is a search or just listing all the datasets
                        if (Util.isEmpty(request.getParameter("search_precision"))) { // listing all the datasets
                            %>
                            <b>No dataset definitions were found!</b><%
                        }
                        else{ // a search
                            %>
                            <b>No dataset definitions matching the search criteria were found!</b><%
                        }
                        %>

                        </div></div><%@ include file="footer.jsp" %></body></html>
                        <%
                        return;
                    }
                %>

        <table class="sortable" width="100%" style="clear:both">

            <%
            // temporarly we do not display version aka CheckInNo, because for the time being it doesn't function properly anyway
            boolean isDisplayVersionColumn = isIncludeHistoricVersions;//false;//user!=null;
            boolean isDisplayHelperColumn = user!=null;

            int colSpan = 3;
            if (isDisplayHelperColumn)
                colSpan++;
            if (isDisplayVersionColumn)
                colSpan++;

            if (isDisplayHelperColumn) { %>
                <col style="width: 3%"/>
                <col style="width: 32%"/><%
            }
            else { %>
                <col style="width: 35%"/><%
            }

            if (isDisplayVersionColumn) { %>
                <col style="width: 10%"/>
                <col style="width: 15%"/>
                <col style="width: 40%"/><%
            } else { %>
                <col style="width: 20%"/>
                <col style="width: 45%"/><%
            }
            %>

            <!-- the table itself -->
       <thead>
            <tr>
                <%
                if (isDisplayHelperColumn) { %>
                    <th></th><%
                }
                String sortedImg  = getSortedImg(1, oSortCol, oSortOrder);
                String sortedLink = getSortedLink(1, oSortCol, oSortOrder);
                String sortedAlt  = getSortedAlt(sortedImg);
                %>
                <c:url var="nameSortingUrl" value="/datasets.jsp">
                    <c:forEach items="${param}" var="entry">
                        <c:if test="${entry.key != 'sort_name' and entry.key != 'sort_order'}">
                            <c:param name="${entry.key}" value="${entry.value}" />
                        </c:if>
                    </c:forEach>
                    <c:param name="sort_name" value="NAME" />
                    <c:if test="${param.sort_name eq 'NAME' and param.sort_order ne 'desc'}">
                        <c:param name="sort_order" value="desc" />
                    </c:if>
                </c:url>
                <th>
                    <a title="Sort on Dataset" href="${nameSortingUrl}">
                        Dataset&nbsp;<img src="<%=Util.processForDisplay(sortedImg,true)%>" width="12" height="12" alt="<%=Util.processForDisplay(sortedAlt,true)%>"/>
                    </a>
                </th>
                <%
                if (isDisplayVersionColumn) {
                    sortedImg  = getSortedImg(3, oSortCol, oSortOrder);
                    sortedLink = getSortedLink(3, oSortCol, oSortOrder);
                    sortedAlt  = getSortedAlt(sortedImg);
                    %>
                    <c:url var="idSortingUrl" value="/datasets.jsp">
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
                    <th>
                        <a title="Sort on Version" href="${idSortingUrl}">
                          Version&nbsp;<img src="<%=Util.processForDisplay(sortedImg,true)%>" width="12" height="12" alt="<%=Util.processForDisplay(sortedAlt,true)%>"/>
                        </a>
                    </th><%
                }
                %>
                <th>
                    <%
                    sortedImg = getSortedImg(2, oSortCol, oSortOrder);
                    sortedLink = getSortedLink(2, oSortCol, oSortOrder);
                    sortedAlt = getSortedAlt(sortedImg);
                    %>
                    
                    <c:url var="regStatusSortingUrl" value="/datasets.jsp">
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
                    <a title="Sort on Status" href="${regStatusSortingUrl}">
                        Status&nbsp;<img src="<%=Util.processForDisplay(sortedImg,true)%>" width="12" height="12" alt="<%=Util.processForDisplay(sortedAlt,true)%>"/>
                    </a>
                    
                </th>
                <th>
                    Tables
                </th>
            </tr>
      </thead>
      <tbody>

            <%
            DElemAttribute attr = null;
            int countCheckboxes = 0;
            c_SearchResultSet oResultSet=new c_SearchResultSet();
            oResultSet.isAuth = user!=null;
            oResultSet.oElements=new Vector();
            session.setAttribute(oSearchCacheAttrName,oResultSet);

            for (int i=0; i<datasets.size(); i++) {
                Dataset dataset = (Dataset)datasets.get(i);
                String ds_id = dataset.getID();
                Vector tables = searchEngine.getDatasetTables(ds_id, true);
                String regStatus = dataset.getStatus();
                boolean clickable = searchEngine.skipByRegStatus(regStatus) ? false : true;
                String linkDisabled = clickable ? "" : "class=\"disabled\"";
                String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
                String ds_name = Util.processForDisplay(dataset.getShortName());
                String dsLink = clickable ? request.getContextPath() + "/datasets/" + ds_id : "#";
                String dsFullName=dataset.getName();
                if (dsFullName!=null && dsFullName.length()>64)
                    dsFullName = dsFullName.substring(0,60) + " ...";
                String workingUser = dataset.getWorkingUser();

                String statusImg   = "images/" + Util.getStatusImage(regStatus);
                String statusTxt   = Util.getStatusRadics(regStatus);
                String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
                String alertReleased = regStatus.equals("Released") ? "onclick=\"alertReleased(this)\"" : "";
                boolean released = regStatus.equals("Released");

                boolean canDelete = !dataset.isWorkingCopy() && workingUser==null && regStatus!=null && user!=null;
                if (canDelete) {
                    boolean editPrm = SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
                    boolean editReleasedPrm = SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "er");
                    if (regStatus.equals("Released") || regStatus.equals("Recorded")) {
                        canDelete = editReleasedPrm;
                    } else {
                        canDelete = editPrm || editReleasedPrm;
                    }
                }

                c_SearchResultEntry oEntry = new c_SearchResultEntry(ds_id, ds_name, dsVersion, dsFullName, tables);
                oEntry.setRegStatus(regStatus);
                oEntry.workingUser = workingUser;
                oEntry.setSortableStatus(Util.getStatusSortString(regStatus));
                oEntry.clickable = clickable;
                oEntry.oIdentifier = dataset.getIdentifier();
                oEntry.canDelete = canDelete;

                oResultSet.oElements.add(oEntry);
                %>

                    <tr valign="top" class="<%=zebraClass%>">
                        <%
                        // the 1st column: checkbox, red asterisk or nbsp
                        if (isDisplayHelperColumn) { %>
                            <td align="right">
                                <%
                                if (workingUser!=null) { %>
                                    <div title="<%=Util.processForDisplay(workingUser,true)%>" class="checkedout">*</div><%
                                } else { %>
                                <input type="checkbox" class="selectable" style="height:13;width:13" name="ds_id" value="<%=ds_id%>" />
                                <input type="hidden" name="ds_idf_<%=ds_id%>" id="ds_idf_<%=ds_id%>" value="<%=dataset.getIdentifier()%>"/>
                                <input type="hidden" id="can_delete_ds_idf_<%=ds_id%>" value="<%=canDelete%>"/>
                                <input type="hidden" id="released_ds_idf_<%=ds_id%>" value="<%=released%>"/>
                                <%
                                countCheckboxes++;
                                }
                                %>
                            </td><%
                        }

                        // the 2nd column: full name link
                        if (clickable==false) { %>
                            <td title="<%=Util.processForDisplay(dsFullName,true)%>" class="disabled">
                                <%=Util.processForDisplay(dsFullName, true)%>
                            </td><%
                        } else { %>
                            <td title="<%=Util.processForDisplay(dsFullName,true)%>">
                                <a href="<%=Util.processForDisplay(dsLink,true)%>">
                                    <%=Util.processForDisplay(dsFullName, true)%>
                                </a>
                            </td><%
                        }
                        %>

                        <%
                        // 3rd column: version aka CheckInNo
                        if (isDisplayVersionColumn) { %>
                            <td>
                                <%=dataset.getID()%>
                            </td><%
                        }

                        // 4th column: Registration status
                        %>
                        <td>
                            <%
                            if (clickable) { %>
                                <img style="border:0" src="<%=Util.processForDisplay(statusImg)%>" width="56" height="12" title="<%=regStatus%>" alt="<%=regStatus%>"/><%
                            } else { %>
                                <span style="color:gray;text-decoration:none;font-size:8pt" title="<%=regStatus%>">
                                    <strong><%=statusTxt%></strong>
                                </span><%
                            }
                            %>
                        </td>
                        <%
                        // 5th column: tables in this dataset
                        %>
                        <td>
                            <%
                            for (int c=0; tables!=null && c<tables.size(); c++) {
                                DsTable table = (DsTable)tables.get(c);
                                StringBuffer tableLink = new StringBuffer(request.getContextPath());
                                tableLink.append("/tables/").append(table.getID());

                                // it is probably less confusing if there are no links for tables of working copies
                                if (isSearchForWorkingCopies) { %>
                                    <%=Util.processForDisplay(table.getShortName())%><%
                                } else {
                                    if (clickable) { %>
                                        <a href="<%=tableLink%>">
                                            <%=Util.processForDisplay(table.getShortName())%>
                                        </a><%
                                    } else { %>
                                        <span class="disabled"><%=Util.processForDisplay(table.getShortName())%></span><%
                                    }
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
        <p>Total results: <%=datasets.size()%></p>

            <div style="display:none">
                <input type="hidden" name="searchUrl" value=""/>
                <input type="hidden" name="mode" value="view"/>
                <input type="hidden" name="complete" value="false"/>
                <%
                if (isSearchForWorkingCopies) { %>
                    <input name="wrk_copies" type="hidden" value="true"/><%
                }
                if (isIncludeHistoricVersions) { %>
                    <input name="incl_histver" type="hidden" value="true"/><%
                }
                // helper hidden input so that we can disable delete button if no checkboxes were displayed
                %>
                <input name="count_checkboxes" type="hidden" value="<%=countCheckboxes%>"/>
            </div>
        </form>

        <form id="sort_form" action="datasets.jsp" method="get">
            <div style="display:none">
                <input name="sort_column" type="hidden" value="<%=(oSortCol==null) ? "" : oSortCol.toString()%>"/>
                <input name="sort_order" type="hidden" value="<%=(oSortOrder==null) ? "" : oSortOrder.toString()%>"/>
                <%
                if (isSearchForWorkingCopies) { %>
                    <input name="wrk_copies" type="hidden" value="true"/><%
                }
                if (isIncludeHistoricVersions) { %>
                    <input name="incl_histver" type="hidden" value="true"/><%
                }
                %>
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
