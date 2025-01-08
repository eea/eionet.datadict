<%@page import="org.apache.commons.lang.ArrayUtils"%>
<%@ page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.dao.domain.DatasetRegStatus,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil,org.apache.commons.lang.StringUtils"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
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

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    ServletContext ctx = getServletContext();

    Vector<Dataset> datasets = null;
    DDSearchEngine searchEngine = null;
    Connection conn = null;
    DDUser user = SecurityUtil.getUser(request);
    boolean isSearchForWorkingCopies = request.getParameter("wrk_copies") != null && request.getParameter("wrk_copies").equals("true");
    boolean isIncludeHistoricVersions = request.getParameter("incl_histver") != null && request.getParameter("incl_histver").equals("true");

    try { // start the whole page try block
        String attrID = null;
        String attrValue = null;
        String attrName = null;
        StringBuffer collect_attrs = new StringBuffer();
        HashSet displayedCriteria = new HashSet();
        String sel_attr = request.getParameter("sel_attr");
        String sel_type = request.getParameter("sel_type");
        String short_name = request.getParameter("short_name");
        String idfier = request.getParameter("idfier");
        String contextParam = request.getParameter("ctx");
        String exclude = request.getParameter("exclude");
        String[] excludedList = {};
        List<Integer> excludedIndexes = new ArrayList();
        if (exclude!=null){ 
            excludedList = exclude.split(",");
        }
        
        conn = ConnectionUtil.getConnection();

        if (request.getMethod().equals("POST")) {
            if (user==null) { %>
                <b>Not allowed!</b> <%
                return;
            } else {
                String[] ds_ids = request.getParameterValues("ds_id");
                for (int i=0; ds_ids!=null && i<ds_ids.length; i++) {
                    String dsIdf = request.getParameter("ds_idf_" + ds_ids[i]);
                    if (dsIdf==null || !SecurityUtil.hasPerm(user, "/datasets/" + dsIdf, "d")){ %>
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

        searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);

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

        String version = request.getParameter("version");
        
        HashSet statuses = null;
        String requestedStatus = request.getParameter("regStatus");
        if (requestedStatus!=null && requestedStatus.length()>0) {
            String[] statusesArray = requestedStatus.split(",");
            statuses = new HashSet();
            for (String status : statusesArray) {
                statuses.add(status);
            }
        }
        datasets = searchEngine.getDatasets(params, short_name, idfier, version, oper, isSearchForWorkingCopies, isIncludeHistoricVersions, statuses);
        request.setAttribute("registrationStatuses", DatasetRegStatus.values());
        
        String regStatusFilter = request.getParameter("regStatusFilter");
        if (regStatusFilter != null && regStatusFilter.equals("false")) {
            request.setAttribute("regStatusFilter", false);
        }
        else {
            request.setAttribute("regStatusFilter", true);
        }
        String sortName = (String) request.getParameter("sort_name");
        DataSetSort sort = DataSetSort.fromString(sortName);
        if (sort == null) {
            sort = DataSetSort.NAME; // fall-back
        }
        String sortOrder = (String) request.getParameter("sort_order");
        boolean descending = StringUtils.isNotBlank(sortOrder) && sortOrder.equals("desc");
        Collections.sort(datasets, sort.getComparator(descending));
        Map<Dataset, Vector<DsTable>> datasetsToTables = new LinkedHashMap();
        Map<String, Boolean> deletableDatasets = new LinkedHashMap();
        for (Dataset dataset : datasets) {
            if (ArrayUtils.contains(excludedList, dataset.getID())){
                excludedIndexes.add(datasets.indexOf(dataset));
                continue;
            }
            datasetsToTables.put(dataset, searchEngine.getDatasetTables(dataset.getID(), true));
            
            boolean canDelete = !dataset.isWorkingCopy() && dataset.getWorkingUser()==null && dataset.getStatus()!=null && user!=null;
            if (canDelete) {
                boolean editPrm = SecurityUtil.hasPerm(user, "/datasets/" + dataset.getIdentifier(), "u");
                boolean editReleasedPrm = SecurityUtil.hasPerm(user, "/datasets/" + dataset.getIdentifier(), "er");
                if (dataset.getStatus().equals("Released") || dataset.getStatus().equals("Recorded")) {
                    canDelete = editReleasedPrm;
                } else {
                    canDelete = editPrm || editReleasedPrm;
                }
            }
            deletableDatasets.put(dataset.getID(), canDelete);
        }
        
        for (Integer indexToRemove : excludedIndexes) {
            int toRemove = indexToRemove;
            datasets.remove(toRemove);
        }
        
        
        request.setAttribute("datasets", datasetsToTables);
        request.setAttribute("deletableDatasets", deletableDatasets);
        request.setAttribute("user", user);
        if (user!=null && SecurityUtil.hasPerm(user, "/datasets", "i")) {
            request.setAttribute("canAddDataset", "true");
        }
        request.setAttribute("ctx", contextParam);
        boolean isPopup = contextParam!=null && contextParam.equals("popup");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Datasets - Data Dictionary</title>
    <script type="text/javascript" src="${pageContext.request.contextPath}/modal_dialog.js"></script>
    <script type="text/javascript">
    // <![CDATA[
        function pickDataset(id, shortName){
            if (window.opener.pickDataset(id,shortName)){
               window.close();
            } else {
                window.alert("An error occured! Popup window is closing");
                window.close();
            }
            return;
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
                alert("Select at least one dataset to continue");
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
            openNoYes("${pageContext.request.contextPath}/yesno_dialog.html", "Do you want the selected datasets to be deleted permanently?\n(Note that working copies will always be permanently deleted)", delDialogReturn,100, 400);
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
            applySearchToggle("searchDatasetsForm");
        });
    // ]]>
    </script>
</head>
<body>
    <%
    if (!isPopup){
        %>
        <div id="container">
        <jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Datasets"/>
            <jsp:param name="helpscreen" value="datasets"/>
        </jsp:include>
        <c:set var="currentSection" value="datasets" />
        <%@ include file="/pages/common/navigation.jsp" %>
        <div id="workarea">
        <c:choose>
            <c:when test="${param.wrk_copies eq 'true'}">
                <h1>Working copies of dataset definitions</h1>
            </c:when>
            <c:otherwise>
                <h1>${param.incl_histver eq "true" ? "All" : "Latest"} versions of datasets in any status</h1>
            </c:otherwise>
        </c:choose>
        <c:if test="${empty user}">
            <p class="advise-msg">Note: Datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.</p>
        </c:if>
            <div id="drop-operations">
            <ul>
                <li class="search open"><a class="searchSection" href="#" title="Search datasets">Search</a></li>
                <c:if test="${not empty user}">
                    <c:if test="${user.authentic}">
                        <li class="restore"><a href="${pageContext.request.contextPath}/restore_datasets.jsp?SearchType=SEARCH&amp;restore=true" title="Restore datasets">Restore</a></li>
                    </c:if>
                    <c:if test="${param.wrk_copies ne 'true' and not empty canAddDataset}">
                        <li class="add"><a href="${pageContext.request.contextPath}/datasets/add">Add</a></li>
                    </c:if>
                    <c:if test="${param.wrk_copies ne 'true'}">
                        <li class="delete"><a href="javascript:deleteDataset()">Delete selected</a></li>
                    </c:if>
                    <c:if test="${user.authentic}">
                        <li class="pdf"><a href="javascript:generateCombinedPdf()">Generate PDF of selected</a></li>
                    </c:if>
                </c:if>
            </ul>
        </div>
    <%
    }
    else{ %>
        <div id="pagehead">
            <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
            <div id="networktitle">Eionet</div>
            <div id="sitetitle"><%=application.getInitParameter("appDispName")%></div>
            <div id="sitetagline">This service is part of Reportnet</div>
        </div> <!-- pagehead -->
        <div id="workarea">
            <h1>Search datasets</h1>
        <%
    }%> 
        <form id="searchDatasetsForm" action="${pageContext.request.contextPath}/datasets.jsp" method="get">
            <div id="filters">
                <table class="filter">
                    <c:choose>  
                        <c:when test="${regStatusFilter}">
                            <tr>
                                <td class="label">
                                    <label for="regStatus">Registration Status</label>
                                    <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=regstatus"></a>
                                </td>
                                <td class="input">
                                    <select name="regStatus" id="regStatus" class="small">
                                        <option value="">All</option>
                                        <c:forEach items="${registrationStatuses}" var="status">
                                            <option value="${fn:escapeXml(status.name)}" ${param.regStatus eq status.name ? 'selected="selected"' : ''}>${fn:escapeXml(status.name)}</option>
                                        </c:forEach>
                                    </select>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="regStatusFilter" value="false" />
                            <input type="hidden" name="regStatus" value="${fn:escapeXml(requestedStatus)}" />
                        </c:otherwise>
                    </c:choose>
                    <tr>
                        <td class="label">
                            <label for="short_name">Short name</label>
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=short_name"></a>
                        </td>
                        <td class="input">
                            <input type="text" class="smalltext" size="59" name="short_name" id="short_name" value="${fn:escapeXml(param.short_name)}"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label for="idfier">Identifier</label>
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=identifier"></a>
                        </td>
                        <td class="input">
                            <input type="text" class="smalltext" size="59" name="idfier" id="idfier" value="${fn:escapeXml(param.idfier)}"/>
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
                    <tr>
                        <td class="label">
                            <label for="attr_<%=attrID%>"><%=Util.processForDisplay(attrName)%></label>
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                        </td>
                        <td class="input">
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" id="attr_<%=attrID%>" size="59"  value="<%=Util.processForDisplay(attrValue, true)%>"/>
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
                    <tr>
                        <td class="label">
                            <label for="attr_<%=attrID%>"><%=Util.processForDisplay(attrName)%></label>
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                        </td>
                        <td class="input">
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" id="attr_<%=attrID%>" size="59" value="<%=Util.processForDisplay(attrValue, true)%>"/>
                            <a class="deleteButton" title="Remove attribute from search criteria" href="javascript:selAttr(<%=attrID%>, 'remove');"></a>
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
                    <tr>
                        <td class="label">
                            <label for="attr_<%=attrID%>"><%=Util.processForDisplay(attrName)%></label>
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                        </td>
                        <td class="input">
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" id="attr_<%=attrID%>" size="59" value=""/>
                            <a class="deleteButton" title="Remove attribute from search criteria" href="javascript:selAttr(<%=attrID%>, 'remove');"></a>
                        </td>
                    </tr>
                    <%
                            }
                        }
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
                        <td></td>
                        <td class="input">
                            <select name="add_criteria" id="add_criteria" onchange="selAttr(this.options[this.selectedIndex].value, 'add')">
                                <option value="">Add criteria</option>
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
                    <tr>
                        <td class="label">Search method</td>
                        <td class="input bordered">
                            <input type="radio" name="search_precision" id="ssubstr" value="substr" ${param.search_precision ne 'exact' ? 'checked="checked"' : ''} /><label for="ssubstr">Substring search</label>
                            <input type="radio" name="search_precision" id="sexact" value="exact" ${param.search_precision eq 'exact' ? 'checked="checked"' : ''} /><label for="sexact">Exact search</label>
                        </td>
                    </tr>
                    <c:if test="${not empty user and user.authentic}">
                        <tr>
                            <td class="label"><label for="wrk_copies">Working copies only</label></td>
                            <td class="input bordered">
                                <input type="checkbox" name="wrk_copies" id="wrk_copies" value="true" ${param.wrk_copies eq 'true' ? 'checked="checked"' : ''} />
                                <label for="wrk_copies" class="smallfont">Yes</label>
                            </td>
                        </tr>
                    </c:if>
                    <tr>
                        <td class="label"><label for="incl_histver">Include historic versions</label></td>
                        <td class="input bordered">
                            <input type="checkbox" name="incl_histver" id="incl_histver" value="true" ${param.incl_histver eq 'true' ? 'checked="checked"' : ''} />
                            <label for="incl_histver" class="smallfont">Yes</label>
                        </td>
                    </tr>
                </table>
                <p class="actions">
                    <input class="mediumbuttonb searchButton" type="submit" value="Search" />
                    <input class="mediumbuttonb" type="reset" value="Reset" />
                </p>
                    <!-- table for 'Add' -->
                    <div style="display:none">
                        <input type="hidden" name="sel_attr" value=""/>
                        <input type="hidden" name="sel_type" value=""/>
                        <input type="hidden" name="type" value="DST"/>
                        <!-- collect all the attributes already used in criterias -->
                        <input type="hidden" name="collect_attrs" value="<%=Util.processForDisplay(collect_attrs.toString(), true)%>"/>
                        <input type="hidden" name="ctx" value="${ctx}"/>
                    </div>
                </div>
            </form>
            <c:choose>
                <c:when test="${empty datasets}">
                    <p class="not-found">No dataset definitions were found.</p>
                </c:when>    
                <c:otherwise>
                    <h2 class="results">Total results: ${fn:length(datasets)}</h2>
                    <form id="form1" method="post" action="datasets.jsp">
                        <table class="datatable results">

                        <%
                        // temporarly we do not display version aka CheckInNo, because for the time being it doesn't function properly anyway
                        boolean isDisplayVersionColumn = isIncludeHistoricVersions;//false;//user!=null;
                        boolean isDisplayHelperColumn = user!=null;

                        if (isDisplayHelperColumn) { %>
                            <col />
                            <col /><%
                        }
                        else { %>
                            <col /><%
                        }

                        if (isDisplayVersionColumn) { %>
                            <col />
                            <col />
                            <col /><%
                        } else { %>
                            <col />
                            <col /><%
                        }
                        %>

                        <!-- the table itself -->
                   <thead>
                        <tr>
                            <c:if test="${not empty user}">
                                <th></th>
                            </c:if>
                            <th<c:if test="${param.sort_name eq 'NAME'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
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
                                <a title="Sort on Dataset" href="${nameSortingUrl}">Dataset</a>
                            </th>
                            <c:if test="${param.incl_histver eq 'true'}">
                                <th<c:if test="${param.sort_name eq 'ID'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
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
                                    <a title="Sort on Version" href="${idSortingUrl}">Version</a>
                                </th>
                            </c:if>
                            <th<c:if test="${param.sort_name eq 'STATUS'}"> class="selected ${param.sort_order eq 'desc' ? 'desc': 'asc'}"</c:if>>
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
                                <a title="Sort on Status" href="${regStatusSortingUrl}">Status</a>
                            </th>
                        <%if (!isPopup) {%>    
                            <th>
                                Tables
                            </th>
                        <%} else {%>
                            <th></th>
                        <%}%>
                        </tr>
                  </thead>
                  <tbody>
                    <c:forEach items="${datasets}" var="entry" varStatus="row">
                        <c:set var="dataset" value="${entry.key}" />   
                        <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                            <c:if test="${not empty user}">
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty dataset.workingUser}">
                                            <div title="${fn:escapeXml(dataset.workingUser)}" class="checkedout">*</div>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="checkbox" class="selectable" style="height:13;width:13" name="ds_id" value="${dataset.ID}" />
                                            <input type="hidden" name="ds_idf_${dataset.ID}" id="ds_idf_${dataset.ID}" value="${fn:escapeXml(dataset.identifier)}"/>
                                            <input type="hidden" id="can_delete_ds_idf_${dataset.ID}" value="${deletableDatasets['${dataset.ID}']}"/>
                                            <input type="hidden" id="released_ds_idf_${dataset.ID}" value="${dataset.status eq 'Released'? true : false}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </c:if>
                            <c:set var="clickable" value="${not empty dataset.status and (((empty user or not user.authentic) and (dataset.status eq 'Incomplete' or dataset.status eq 'Candidate' or dataset.status eq 'Qualified' or dataset.status eq 'Retired' or dataset.status eq 'Superseded')))  ? false : true}" />
                            <td title="${fn:escapeXml(dataset.name)}">
                                <c:if test="${clickable}">
                                    <a href="${pageContext.request.contextPath}/datasets/${dataset.ID}">
                                </c:if>
                                    ${fn:escapeXml(dataset.name)}
                                <c:if test="${clickable}"></a></c:if>
                            </td>
                            <c:if test="${param.incl_histver eq 'true'}">
                                <td>${dataset.ID}</td>
                            </c:if>
                            <td>
                                <dd:datasetRegStatus value="${dataset.status}" />
                            </td>
                        <%if (!isPopup) {%>    
                            <td>
                                <c:forEach items="${entry.value}" var="table">
                                    <c:choose>
                                        <c:when test="${param.wrk_copies eq 'true'}">
                                            ${fn:escapeXml(table.shortName)}
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${clickable}">
                                                <a href="${pageContext.request.contextPath}/tables/${table.ID}">
                                            </c:if>
                                                ${fn:escapeXml(table.shortName)}
                                            <c:if test="${clickable}"></a></c:if>
                                        </c:otherwise>
                                    </c:choose>
                                    <br/>
                                </c:forEach>
                            </td>
                        <%} else {%>
                            <td>
                                <a href="javascript: pickDataset('${dataset.ID}', '${dataset.shortName}')">[Select]</a>
                            </td>
                        <%}%>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

                        <div style="display:none">
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
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
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
