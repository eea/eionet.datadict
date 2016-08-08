<%@page import="eionet.meta.notif.Subscriber"%>
<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private String currentUrl=null;%>
<%@ include file="history.jsp" %>

    <%!
    // servlet-scope helper functions
    //////////////////////////////////
    /**
    *
    */
    private String getValue(String id, String mode, Vector attributes) {
        if (id == null) return null;
        if (mode.equals("add")) return null;

        for (int i=0; attributes!=null && i<attributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (id.equals(attr.getID()))
                return attr.getValue();
        }

        return null;
    }
    /**
    *
    */

    private Vector getValues(String id, String mode, Vector attributes) {
        if (id == null) return null;
        if (mode.equals("add")) return null;

        for (int i=0; attributes!=null && i<attributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (id.equals(attr.getID()))
                return attr.getValues();
        }

        return null;
    }
    %>

    <%

    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    // implementation of the servlet's service method
    //////////////////////////////////////////////////

    request.setCharacterEncoding("UTF-8");

    String mode=null;
    Vector mAttributes=null;
    Vector attributes=null;
    Dataset dataset=null;
    Vector complexAttrs=null;
    Vector tables=null;
    Vector otherVersions = null;
    String feedbackValue = null;

    ServletContext ctx = getServletContext();
    DDUser user = SecurityUtil.getUser(request);

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
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("subscribe")) {
        feedbackValue = "Subscription successful!";
    }

    // POST request not allowed for anybody who hasn't logged in
    if (request.getMethod().equals("POST") && user == null) {
        request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    // get values of most important request parameters:
    // - id number
    // - alphanumeric identifier
    // - mode
    String dstIdf = request.getParameter("dataset_idf");
    String ds_id = request.getParameter("ds_id");
    mode = request.getParameter("mode");
    if (mode == null || mode.trim().length() == 0) {
        mode = "view";
    }

    if (mode.equals("add")) {
        if (user == null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")) {
            request.setAttribute("DD_ERR_MSG", "You have no permission to add a dataset!");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }

    if (mode.equals("view")) {
        if (Util.isEmpty(dstIdf) && Util.isEmpty(ds_id)) {
            request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id or dataset_idf");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }
    else if (mode.equals("edit")) {
        if (Util.isEmpty(ds_id)) {
            request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }

    // as of Sept 2006,  parameter "action" is a helper to add some extra context to parameter "mode"
    String action = request.getParameter("action");
    if (action!=null && action.trim().length() == 0) action = null;

    // if requested by alphanumeric identifier and not by auto-generated id,
    // then it means the dataset's latest version is requested
    boolean isLatestRequested = mode.equals("view") && !Util.isEmpty(dstIdf) && Util.isEmpty(ds_id);


    //// handle the POST request//////////////////////
    //////////////////////////////////////////////////
    if (request.getMethod().equals("POST")) {

        Connection userConn = null;
        DatasetHandler handler = null;
        try {
            userConn = user.getConnection();
            handler = new DatasetHandler(userConn, request, ctx);
            handler.setUser(user);
            try {
                handler.execute();
            }
            catch (Exception e) {
                handler.cleanup();
                String msg = e.getMessage();
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(bytesOut));
                String trace = bytesOut.toString(response.getCharacterEncoding());
                request.setAttribute("DD_ERR_MSG", msg);
                request.setAttribute("DD_ERR_TRC", trace);
                if (e instanceof DDException) {
                    HashMap<String, Object> errorParams = ((DDException)e).getErrorParameters();
                    if (errorParams != null && errorParams.containsKey(DDException.ERR_ELEMS_KEY)) {
                        request.setAttribute("DD_ERR_ELEMS", errorParams.get(DDException.ERR_ELEMS_KEY));
                    }
                }

                String backLink = request.getParameter("submitter_url");
                if (backLink == null || backLink.length() == 0)
                    backLink = history.getBackUrl();
                request.setAttribute("DD_ERR_BACK_LINK", backLink);
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        }
        finally{
            try { if (userConn!=null) userConn.close();
            } catch (SQLException e) {}
        }

        // dispatch the POST request
        ////////////////////////////
        String redirUrl = null;
        if (mode.equals("add")) {
            String id = handler.getLastInsertID();
            if (id!=null && id.length() > 0) {
                redirUrl = request.getContextPath() + "/datasets/" + id;
            }
        }
        else if (mode.equals("edit")) {

            if (request.getParameter("check_in")!=null && request.getParameter("check_in").equalsIgnoreCase("true")) {
                // if this was a check-in, redirect to the view of the checked-in copy
                redirUrl = request.getContextPath() + "/datasets/" + handler.getCheckedInCopyID() + "/?feedback=checkin";
            }
            else{
                redirUrl = request.getContextPath() + "/datasets/" + ds_id;
                if (request.getParameter("saveclose") == null || request.getParameter("saveclose").equals("false")) {
                    redirUrl = redirUrl + "/edit";
                }
            }
        }
        else if (mode.equals("delete")) {
            String checkedoutCopyID = request.getParameter("checkedout_copy_id");
            String wasWorkingCopy = request.getParameter("is_working_copy");
            if (checkedoutCopyID != null && !checkedoutCopyID.isEmpty()) {
                redirUrl = request.getContextPath() + "/datasets/" + checkedoutCopyID + "/?feedback=undo_checkout";
            }
            else if (wasWorkingCopy != null && wasWorkingCopy.equals("true")) {
                redirUrl = request.getContextPath() + "/datasets.jsp?feedback=undo_checkout";
            }
            else {
                redirUrl = request.getContextPath() + "/datasets.jsp?feedback=delete";
            }

        }

        response.sendRedirect(redirUrl);
        return;
    }
    //// end of handle the POST request //////////////////////
    // any code below must not be reached when POST request!!!

    Connection conn = null;

    // the whole page's try block
    try {

        // get db connection, init search engine object
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);

        // initialize the metadata of attributes
        mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);

        String idfier = "";
        String ds_name = "";
        String version = "";
        String dsVisual = null;
        String workingUser = null;
        String regStatus = null;
        String latestID = null;
        boolean isLatestDst = false;
        boolean imgVisual = false;
        boolean editPrm = false;
        boolean editReleasedPrm = false;
        boolean advancedAccess = false;
        boolean canCheckout = false;
        boolean canNewVersion = false;

        // if not in add mode, get the dataset object and some parameters based on it
        if (!mode.equals("add")) {

            // get the dataset object
            if (isLatestRequested) {

                Vector v = new Vector();
                if (user == null) {
                    v.add("Released");
                    v.add("Recorded");
                }
                dataset = searchEngine.getLatestDst(dstIdf, v);

                if (dataset!=null) {
                    // double-making-sure that ds_id value is correct
                    ds_id = dataset.getID();
                }
            }
            else{
                dataset = searchEngine.getDataset(ds_id);
            }

            // if dataset object found, populate some parameters based on it
            if (dataset!=null) {

                idfier = dataset.getIdentifier();
                ds_name = dataset.getShortName();
                version = dataset.getVersion();

                regStatus = dataset.getStatus();
                workingUser = dataset.getWorkingUser();
                editPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
                editReleasedPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "er");
                advancedAccess = SecurityUtil.hasPerm(user != null ? user.getUserName() : null, "/datasets/" + dataset.getIdentifier(), DDUser.MSACCESS_ADVANCED_PRM);

                Vector v = null;
                if (user == null) {
                    v = new Vector();
                    v.add("Released");
                    v.add("Recorded");
                }
                latestID = searchEngine.getLatestDstID(idfier, v);
                isLatestDst = latestID!=null && ds_id.equals(latestID);

                canNewVersion = !dataset.isWorkingCopy() && workingUser == null && regStatus!=null && user!=null && isLatestDst;
                if (canNewVersion) {
                    canNewVersion = regStatus.equals("Released") || regStatus.equals("Recorded");
                    if (canNewVersion)
                        canNewVersion = editPrm || editReleasedPrm;
                }

                canCheckout = !dataset.isWorkingCopy() 
                        && workingUser == null 
                        && regStatus!=null && !regStatus.equals("Retired") && !regStatus.equals("Superseded")
                        && user!=null && isLatestDst;
                if (canCheckout) {
                    if (regStatus.equals("Released"))
                            //|| regStatus.equals("Recorded"))
                        canCheckout = editReleasedPrm;
                    else
                        canCheckout = editPrm || editReleasedPrm;
                }

                // get the visual structure, so it will be displayed already in the dataset view
                dsVisual = dataset.getVisual();
                if (dsVisual!=null && dsVisual.length()!=0) {
                    int i = dsVisual.lastIndexOf(".");
                    if (i != -1) {
                        String visualType = dsVisual.substring(i+1, dsVisual.length()).toUpperCase();
                        if (visualType.equals("GIF") || visualType.equals("JPG") || visualType.equals("JPEG") || visualType.equals("PNG"))
                            imgVisual = true;
                    }
                }

                // get the dataset's other versions (does not include working copies)
                if (mode.equals("view"))
                    otherVersions = searchEngine.getDstOtherVersions(dataset.getIdentifier(), dataset.getID());
            }
            else{
                if (user!=null) {
                    request.setAttribute("DD_ERR_MSG", "Could not find a dataset of this id or identifier in any status!");
                }
                else{
                    request.setAttribute("DD_ERR_MSG", "Could not find a dataset of this id or identifier in 'Recorded' or 'Released' status! " +
                            "As an anonymous user, you are not allowed to see definitions in any other status.");
                }
                session.setAttribute(AfterCASLoginServlet.AFTER_LOGIN_ATTR_NAME, SecurityUtil.buildAfterLoginURL(request));
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        }

        // populate attribute values of the dataset
        DElemAttribute attribute = null;
        String attrID = null;
        String attrValue = null;
        attributes = searchEngine.getAttributes(ds_id, "DS", DElemAttribute.TYPE_SIMPLE);
        complexAttrs = searchEngine.getComplexAttributes(ds_id, "DS");
        if (complexAttrs == null) complexAttrs = new Vector();

        // get the dataset's tables and links to ROD
        tables = searchEngine.getDatasetTables(ds_id, true);
        Vector rodLinks = dataset == null ? null : searchEngine.getRodLinks(dataset.getID());

        // init version manager object
        VersionManager verMan = new VersionManager(conn, searchEngine, user);

        // security checks, checkin/checkout operations, dispatching of the GET request
        if (mode.equals("edit")) {
            if (!dataset.isWorkingCopy() || user == null || (workingUser!=null && !workingUser.equals(user.getUserName()))) {
                request.setAttribute("DD_ERR_MSG", "You have no permission to edit this dataset!");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        }
        else if (mode.equals("view") && dataset!=null && action!=null && (action.equals("subscribe"))) {
            Subscriber.subscribeToDataset(Collections.singleton(user.getUserName()), dataset.getIdentifier());
            response.sendRedirect(request.getContextPath() + "/datasets/" + dataset.getID() + "/?feedback=subscribe");
        }
        else if (mode.equals("view") && action!=null && (action.equals("checkout") || action.equals("newversion"))) {

            if (action.equals("checkout") && !canCheckout) {
                request.setAttribute("DD_ERR_MSG", "You have no permission to check out this dataset!");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
            if (action.equals("newversion") && !canNewVersion) {
                request.setAttribute("DD_ERR_MSG", "You have no permission to create new version of this dataset!");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }

            // if creating new version, let VersionManager know about this
            if (action.equals("newversion")) {
                eionet.meta.savers.Parameters pars = new eionet.meta.savers.Parameters();
                pars.addParameterValue("resetVersionAndStatus", "resetVersionAndStatus");
                verMan.setServlRequestParams(pars);
            }

            // check out the dataset
            String copyID = verMan.checkOut(ds_id, "dst");
            if (!ds_id.equals(copyID)) {
                // send to copy if created successfully, remove previous url (edit original) from history
                history.remove(history.getCurrentIndex());
                response.sendRedirect(request.getContextPath() + "/datasets/" + copyID + "/?feedback=checkout");
            }
        }
        else if (mode.equals("view") && dataset!=null) {
            // anonymous users should not be allowed to see anybody's working copy
            if (dataset.isWorkingCopy() && user == null) {
                request.setAttribute("DD_ERR_MSG", "Anonymous users are not allowed to view a working copy!");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
            // anonymous users should not be allowed to see definitions that are NOT in Recorded or Released status
            if (user == null && regStatus!=null && !regStatus.equals("Recorded") && !regStatus.equals("Released")) {
                request.setAttribute("DD_ERR_MSG", "Definitions NOT in Recorded or Released status are inaccessible for anonymous users.");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
            // redircet user to his working copy of this dataset (if such exists)
            String workingCopyID = verMan.getWorkingCopyID(dataset);
            if (workingCopyID!=null && workingCopyID.length()>0) {
                response.sendRedirect(request.getContextPath() + "/datasets/" + workingCopyID);
            }
        }

        // prepare the page's HTML title, shown in browser title bar
        StringBuffer pageTitle = new StringBuffer();
        if (mode.equals("edit"))
            pageTitle.append("Edit dataset");
        else
            pageTitle.append("Dataset");
        if (dataset!=null && dataset.getShortName()!=null)
            pageTitle.append(" - ").append(dataset.getShortName());
    %>

<%
// start HTML //////////////////////////////////////////////////////////////
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title><%=pageTitle.toString()%></title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/modal_dialog.js"></script>
    <script type="text/javascript">
    // <![CDATA[

        function warnDatasetType(datasetType, action) {
            if (datasetType.toLowerCase() == 'retired' || datasetType.toLowerCase() == 'superseded') {
                if (['a', 'e', 'i', 'o', 'u'].indexOf(datasetType.toLowerCase().charAt(0))!=-1) {
                    return confirm('The '+action+' you are about to perform is about an '+datasetType+' dataset. Are you sure?');
                }
                else {
                    return confirm('The '+action+' you are about to perform is about a '+datasetType+' dataset. Are you sure?');
                }
            } else {
                return true;
            }
        }
        
        function deleteDatasetReady() {
            document.forms["form1"].elements["mode"].value = "delete";
            document.forms["form1"].submit();
        }
        
        function submitForm(mode) {

            if (mode == "delete") {
                <%
                if (regStatus!=null && dataset!=null && !dataset.isWorkingCopy() && regStatus.equals("Released")) {
                    if (!canCheckout) {
                        %>
                        alert("You have no permission to delete this dataset!");
                        return;
                        <%
                    }
                }

                String confirmationText = "Are you sure you want to delete this dataset? Click OK, if yes. Otherwise click Cancel.";
                if (dataset!=null && dataset.isWorkingCopy())
                    confirmationText = "This working copy will be deleted! Click OK, if you want to continue. Otherwise click Cancel.";
                else if (regStatus!=null && dataset!=null && !dataset.isWorkingCopy() && regStatus.equals("Released"))
                    confirmationText = "You are about to delete a Released dataset! Are you sure you want to do this? Click OK, if yes. Otherwise click Cancel.";
                %>

                var b = confirm("<%=confirmationText%>");
                if (b==false) return;

                <%
                if (dataset!=null && dataset.isWorkingCopy()) { %>
                    document.forms["form1"].elements["complete"].value = "true";
                    deleteDatasetReady();
                    return;<%
                }
                else{ %>
                    // now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
                    openNoYes("../yesno_dialog.html", "Do you want the dataset to be deleted permanently (answering No will enable to restore it later)?", delDialogReturn,100, 400);
                    return;<%
                }
                %>
            }

            if (mode != "delete") {
                if (!checkObligations()) {
                    alert("You have not specified one of the mandatory atttributes!");
                    return;
                }

                if (hasWhiteSpace("idfier")) {
                    alert("Identifier cannot contain any white space!");
                    return;
                }

                if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)) {
                    alert("Identifier not valid for usage as an XML tag! " +
                          "In the first character only underscore or latin characters are allowed! " +
                          "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
                    return;
                }

                //slctAllValues();
            }

            if (mode=="editclose") {
                mode = "edit";
                document.forms["form1"].elements["saveclose"].value = "true";
            }

            document.forms["form1"].elements["mode"].value = mode;
            document.forms["form1"].submit();
        }

        function delDialogReturn() {
            var v = dialogWin.returnValue;
            if (v == null || v=="" || v=="cancel") return;

            document.forms["form1"].elements["complete"].value = v;
            deleteDatasetReady();
        }

        function checkObligations() {

            var o = document.forms["form1"].ds_name;
            if (o!=null && o.value.length == 0) return false;

            var elems = document.forms["form1"].elements;
            for (var i=0; elems!=null && i<elems.length; i++) {
                var elem = elems[i];
                var elemName = elem.name;
                var elemValue = elem.value;
                if (startsWith(elemName, "attr_")) {
                    var o = document.forms["form1"].elements[i+1];
                    if (o == null) return false;
                    if (!startsWith(o.name, "oblig_"))
                        continue;
                    if (o.value == "M" && (elemValue == null || elemValue.length == 0)) {
                        return false;
                    }
                }
            }

            return true;
        }

        function hasWhiteSpace(input_name) {

            var elems = document.forms["form1"].elements;
            if (elems == null) return false;
            for (var i=0; i<elems.length; i++) {
                var elem = elems[i];
                if (elem.name == input_name) {
                    var val = elem.value;
                    if (val.indexOf(" ") != -1) return true;
                }
            }

            return false;
        }

        function startsWith(str, pattern) {
            var i = str.indexOf(pattern,0);
            if (i!=-1 && i == 0)
                return true;
            else
                return false;
        }

        function endsWith(str, pattern) {
            var i = str.indexOf(pattern, str.length-pattern.length);
            if (i!=-1)
                return true;
            else
                return false;
        }

        function checkIn() {
            submitCheckIn();
        }

        function submitCheckIn() {
            <%
            if (regStatus!=null && regStatus.equals("Released")) {
                %>
                var b = confirm("You are checking in with Released status! This will automatically release your changes into public view. " +
                                "If you want to continue, click OK. Otherwise click Cancel.");
                if (b==false) return;
                <%
            }
            %>

            document.forms["form1"].elements["check_in"].value = "true";
            document.forms["form1"].elements["mode"].value = "edit";
            document.forms["form1"].submit();
        }

        function goTo(mode, id) {
            if (mode == "edit") {
                document.location.assign("<%=request.getContextPath()%>/datasets/" + id + "/edit");
            }
            else if (mode=="checkout") {
                document.location.assign("<%=request.getContextPath()%>/datasets/" + id + "/checkout");
            }
            else if (mode=="newversion") {
                document.location.assign("<%=request.getContextPath()%>/datasets/" + id + "/newversion");
            }
            else if (mode=="view") {
                document.location.assign("<%=request.getContextPath()%>/datasets/" + id);
            }
        }
        function slctAllValues() {

            var elems = document.forms["form1"].elements;
            if (elems == null) return true;

            for (var j=0; j<elems.length; j++) {
                var elem = elems[j];
                var elemName = elem.name;
                if (startsWith(elemName, "attr_mult_")) {
                    var slct = document.forms["form1"].elements[elemName];
                    if (slct.options && slct.length) {
                        if (slct.length==1 && slct.options[0].value=="" && slct.options[0].text=="") {
                            slct.remove(0);
                            slct.length = 0;
                        }
                        for (var i=0; i<slct.length; i++) {
                            slct.options[i].selected = "true";
                        }
                    }
                }
            }
        }

        function validForXMLTag(str) {

            // if empty string not allowed for XML tag
            if (str == null || str.length == 0) {
                return false;
            }

            // check the first character (only underscore or A-Z or a-z allowed)
            var ch = str.charCodeAt(0);
            if (!(ch==95 || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))) {
                return false;
            }

            // check the rets of characters ((only underscore or hyphen or dot or 0-9 or A-Z or a-z allowed))
            if (str.length==1) return true;
            for (var i=1; i<str.length; i++) {
                ch = str.charCodeAt(i);
                if (!(ch==95 || ch==45 || ch==46 || (ch>=48 && ch<=57) || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))) {
                    return false;
                }
            }

            return true;
        }

   // ]]>
    </script>
</head>

<%
String hlpScreen = dataset!=null && dataset.isWorkingCopy() ? "dataset_working_copy" : "dataset";
if (mode.equals("edit"))
    hlpScreen = "dataset_edit";
else if (mode.equals("add"))
    hlpScreen = "dataset_add";
%>

<body>
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
    <jsp:param name="name" value="Dataset"/>
    <jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
</jsp:include>
<c:set var="currentSection" value="datasets" />
<%@ include file="/pages/common/navigation.jsp" %>
<div id="workarea">
    <h1>${param.mode eq 'add' ? 'Add' : (param.mode eq 'edit' ? 'Edit' : 'View')} dataset definition</h1>
        <!-- quick links -->
        <%
            if (mode.equals("view")) {
                Vector quicklinks = new Vector();

                if (dataset!=null && dataset.getVisual() != null) {
                    quicklinks.add("Data model | model");
                }
                if (tables != null && tables.size() > 0) {
                    quicklinks.add("Tables | tables");
                }
                if (complexAttrs != null && complexAttrs.size() > 0) {
                    quicklinks.add("Complex attributes | cattrs");
                }
                if (rodLinks != null && rodLinks.size( )> 0) {
                    quicklinks.add("Obligations in ROD | rodlinks");
                }
                request.setAttribute("quicklinks", quicklinks);
        %>
            <jsp:include page="quicklinks.jsp" flush="true" />
        <%
            }

            boolean goToNewest = false;
            if (mode.equals("view") && !dataset.isWorkingCopy()) {
                if (user!=null || (user == null && !isLatestRequested)) {
                    if (latestID!=null && !latestID.equals(dataset.getID())) {
                        goToNewest = true;
                    }
                }
            }

            boolean isDisplayOperations = mode.equals("view") && user!=null && dataset!=null && dataset.getIdentifier()!=null;
            if (isDisplayOperations==false) {
                isDisplayOperations = (mode.equals("view") && !dataset.isWorkingCopy()) && user!=null && (latestID!=null && !latestID.equals(dataset.getID()));
            }

            if (isDisplayOperations || goToNewest) {
                %>
                <div id="drop-operations">
                    <ul>
                        <%
                        if (goToNewest) {
                            %>
                                <li class="newest"><a href="<%=request.getContextPath()%>/datasets/<%=latestID%>">Go to newest</a></li>
                            <%
                        }
                        if (isDisplayOperations) {
                            // the link
                            if (mode.equals("view") && dataset!=null && dataset.isWorkingCopy()) {
                                if (workingUser!=null && user!=null && workingUser.equals(user.getUserName())) {
                            %>
                                <%
                                String dstrodLink = request.getContextPath() + "/dstrod_links.jsp?dst_idf=" + dataset.getIdentifier() + "&amp;dst_id=" + dataset.getID() + "&amp;dst_name=" + dataset.getShortName();
                                %>
                                <li class="edit"><a href="<%=request.getContextPath()%>/datasets/<%=ds_id%>/edit">Edit metadata</a></li>
                                <li class="edit"><a href="<%=request.getContextPath()%>/complex_attrs.jsp?parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=Util.processForDisplay(ds_name)%>&amp;ds=true">Edit complex attributes</a></li>
                                <li class="manage"><a href="<%=request.getContextPath()%>/dstables.jsp?ds_id=<%=ds_id%>&amp;ds_name=<%=Util.processForDisplay(ds_name)%>">Manage tables</a></li>
                                <li class="manage"><a href="<%=request.getContextPath()%>/dsvisual.jsp?ds_id=<%=ds_id%>">Manage model</a></li>
                                <li class="manage"><a href="<%=dstrodLink%>">Manage links to ROD</a></li>
                                <li class="checkin"><a href="javascript:checkIn()">Check in</a></li>
                                <li class="undo"><a href="javascript:submitForm('delete')">Undo checkout</a></li>
                            <%
                                }
                            }
                            if (mode.equals("view")) {
                                if (canNewVersion) {
                            %>
                                <li class="newVersion"><a href="<%=request.getContextPath()%>/datasets/<%=ds_id%>/newversion">New version</a></li>
                            <%
                                }
                                if (canCheckout) {
                            %>
                                <li class="checkout"><a href="<%=request.getContextPath()%>/datasets/<%=ds_id%>/checkout">Check out</a></li>
                                <li class="delete"><a href="javascript:submitForm('delete')">Delete</a></li>
                            <%
                                }
                            }
                            if (mode.equals("view") && user!=null && dataset!=null && dataset.getIdentifier()!=null && !dataset.isWorkingCopy()) {%>
                                <li class="subscribe"><a href="<%=request.getContextPath()%>/datasets/<%=ds_id%>/subscribe">Subscribe</a></li><%
                            }
                            // display the "Upload document" and "Manage cache" links
                            if (mode.equals("view") && (editPrm || editReleasedPrm)) {%>
                                <li class="doc">
                                    <a rel="nofollow" href="<%=request.getContextPath()%>/doc_upload.jsp?ds_id=<%=ds_id%>&amp;idf=<%=Util.processForDisplay(dataset.getIdentifier())%>" onclick="return warnDatasetType(<%=regStatus%>, 'upload')">
                                        Upload a document
                                    </a>
                                </li>
                                <li class="doc">
                                    <a rel="nofollow" href="<%=request.getContextPath()%>/GetCache?obj_id=<%=ds_id%>&amp;obj_type=dst&amp;idf=<%=Util.processForDisplay(dataset.getIdentifier())%>">Open cache</a>
                                </li>
                            <%}
                        }%>
                        </ul>
                  </div>
                  <%
              }

            if (feedbackValue != null) {
            %>
                <div class="system-msg">
                    <strong><%= feedbackValue %></strong>
                </div>
            <%
            }

            %>

            <form id="form1" method="post" action="<%=request.getContextPath()%>/datasets" style="clear:both">
                <div style="display:none">
                <%
                if (!mode.equals("add")) { %>
                    <input type="hidden" name="ds_id" value="<%=ds_id%>"/><%
                }
                else { %>
                    <input type="hidden" name="dummy"/><%
                }

                %>
                </div>

                <!--=======================-->
                <!-- main table inside div -->
                <!--=======================-->

                                <!-- pdfs & schema & docs -->

                                <%
                                if (mode.equals("view")) {

                                    Vector docs = searchEngine.getDocs(ds_id);
                                    boolean dispAll = editPrm || editReleasedPrm;
                                    boolean dispPDF = dataset!=null && dataset.displayCreateLink("PDF");
                                    boolean dispXLS = dataset!=null && dataset.displayCreateLink("XLS");
                                    boolean dispODS = dataset!=null && dataset.displayCreateLink("ODS");
                                    boolean dispMDB = dataset!=null && dataset.displayCreateLink("MDB");
                                    boolean dispXmlSchema = dataset!=null && dataset.displayCreateLink("XMLSCHEMA");
                                    boolean dispXmlInstance = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xmli");
                                    boolean dispDocs = docs!=null && docs.size()>0;

                                    if (dispAll || dispPDF || dispXLS || dispXmlSchema || dispXmlInstance || dispDocs || dispMDB || dispODS) {
                                        %>
                                            <script type="text/javascript">
                                                $(function() {
                                                    applyExportOptionsToggle();
                                                });
                                            </script>
                                            <div id="createbox">
                                                <ul>
                                                    <%
                                                    // PDF link
                                                    if (dispAll || dispPDF) { %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetPrintout?format=PDF&amp;obj_type=DST&amp;obj_id=<%=ds_id%>&amp;out_type=GDLN" class="pdf"  onclick="return warnDatasetType('<%=regStatus%>', 'download')">
                                                                Create technical specification for this dataset
                                                            </a>
                                                        </li><%
                                                    }

                                                    // XML Schema link
                                                    if (dispAll || dispXmlSchema) { %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetSchema?id=DST<%=ds_id%>" class="xsd">
                                                                Create an XML Schema for this dataset
                                                            </a>
                                                        </li><%
                                                    }

                                                    // XML Instance link
                                                    if (dispAll || dispXmlInstance) { %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetXmlInstance?id=<%=dataset.getID()%>&amp;type=dst" class="xml">
                                                                Create an instance XML for this dataset
                                                            </a>
                                                        </li><%
                                                    }

                                                    // MS Excel link
                                                    if (dispAll || dispXLS) { %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetXls?obj_type=dst&amp;obj_id=<%=ds_id%>" class="excel" onclick="return warnDatasetType(<%=regStatus%>, 'download')">
                                                                Create an MS Excel template for this dataset
                                                            </a>
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=excel"></a>
                                                        </li>
                                                    <% }
                                                    if ((dispAll || dispXLS) && user != null) { %>
                                                         <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetXls?obj_type=dst&amp;obj_act=dd&amp;obj_id=<%=ds_id%>" class="excel" onclick="return warnDatasetType(<%=regStatus%>, 'download')">
                                                                Create an MS Excel template for this dataset with drop-down boxes (BETA)
                                                            </a>
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=excel_dropdown"></a>
                                                        </li>
                                                        <%
                                                    }

                                                    // OpenDocument spreadsheet link
                                                    if (dispAll || dispODS) { %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetOds?type=dst&amp;id=<%=ds_id%>" class="open-doc" onclick="return warnDatasetType(<%=regStatus%>, 'download')">
                                                                Create an OpenDocument spreadsheet template for this dataset
                                                            </a>
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=ods"></a>
                                                        </li><%
                                                    }

                                                    // MS Access link
                                                    if (dispAll || dispMDB) { %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetMdb?dstID=<%=ds_id%>&amp;vmdonly=true" class="access" onclick="return warnDatasetType(<%=regStatus%>, 'download')">
                                                                Create validation metadata for MS Access template
                                                            </a>
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=access"></a>
                                                        </li><%
                                                    }

                                                    // Advanced MS Access template generation link
                                                    if (dispAll || advancedAccess) {
                                                        %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/GetMSAccess?dstID=<%=ds_id%>" class="access" onclick="return warnDatasetType(<%=regStatus%>, 'download')">
                                                                Create advanced MS Access template
                                                            </a>
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=advancedMSAccess"></a>
                                                        </li><%
                                                    }

                                                    // codelists
                                                    if (dispAll || dispXmlSchema) { %>
                                                        <li>
                                                            <stripes:link rel="nofollow" beanclass="eionet.web.action.CodelistDownloadActionBean" class="csv">
                                                                <stripes:param name="ownerType" value="datasets"/>
                                                                <stripes:param name="ownerId" value="<%=dataset.getID()%>"/>
                                                                <stripes:param name="format" value="csv"/>
                                                                Get the comma-separated codelists of this dataset
                                                            </stripes:link>
                                                        </li>
                                                        <li>
                                                            <stripes:link rel="nofollow" beanclass="eionet.web.action.CodelistDownloadActionBean" class="xml">
                                                                <stripes:param name="ownerType" value="datasets"/>
                                                                <stripes:param name="ownerId" value="<%=dataset.getID()%>"/>
                                                                <stripes:param name="format" value="xml"/>
                                                                Get the codelists of this dataset in XML format
                                                            </stripes:link>
                                                        </li><%
                                                    }

                                                    // display links to uploaded documents
                                                    for (int i=0; docs!=null && i<docs.size(); i++) {
                                                        Hashtable hash = (Hashtable)docs.get(i);
                                                        String md5   = (String)hash.get("md5");
                                                        String file  = (String)hash.get("file");
                                                        String icon  = (String)hash.get("icon");
                                                        String title = (String)hash.get("title");
                                                        %>
                                                        <li>
                                                            <a rel="nofollow" href="<%=request.getContextPath()%>/DocDownload?file=<%=Util.processForDisplay(md5)%>"><img style="border:0" src="<%=request.getContextPath()%>/images/<%=Util.processForDisplay(icon)%>" width="16" height="16" alt="icon"/><%=Util.processForDisplay(title)%></a>
                                                                <%
                                                                if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u")) {
                                                                    %><a  href="<%=request.getContextPath()%>/DocUpload?ds_id=<%=ds_id%>&amp;delete=<%=Util.processForDisplay(md5)%>&amp;idf=<%=Util.processForDisplay(dataset.getIdentifier())%>"><img style="border:0" src="<%=request.getContextPath()%>/images/delete.gif" width="14" height="14"/></a><%
                                                                }
                                                                %>
                                                            </td>
                                                        </li>
                                                        <%
                                                    }
                                                    %>
                                                </ul>
                                    </div>
                                        <%
                                    }
                                }
                                %>

                                <!-- start dotted -->

                                    <div id="outerframe">
                                        <!-- attributes -->
                                        <%
                                        int displayed = 1;
                                        String isOdd = Util.isOdd(displayed);
                                        %>

                                        <table class="datatable results">
                                            <!-- static attributes -->

                                            <!-- Identifier -->
                                            <tr class="<%=isOdd%>">
                                                <th scope="row" class="scope-row simple_attr_title">
                                                    Identifier
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=identifier"></a>
                                                </th>
                                                <%
                                                if (!mode.equals("view")) {%>
                                                    <td class="simple_attr_help">
                                                        <img src="<%=request.getContextPath()%>/images/mandatory.gif" alt="Mandatory" title="Mandatory"/>
                                                    </td><%
                                                }
                                                %>
                                                <td class="simple_attr_value">
                                                    <%
                                                    if(!mode.equals("add")) { %>
                                                        <b><%=Util.processForDisplay(idfier)%></b>
                                                        <input type="hidden" name="idfier" value="<%=Util.processForDisplay(idfier,true)%>"/><%
                                                    }
                                                    else{ %>
                                                        <input class="smalltext" type="text" size="30" name="idfier"/><%
                                                    }
                                                    %>
                                                </td>
                                                <%isOdd = Util.isOdd(++displayed);%>
                                            </tr>

                                            <!-- short name -->
                                            <tr id="short_name_row" class="<%=isOdd%>">
                                                <th class="scope-row short_name">
                                                    Short name
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=short_name"></a>
                                                </th>
                                                <%
                                                if (!mode.equals("view")) {
                                                    %>
                                                    <td class="short_name simple_attr_help">
                                                        <img src="<%=request.getContextPath()%>/images/mandatory.gif" alt="Mandatory" title="Mandatory"/>
                                                    </td><%
                                                }
                                                %>
                                                <td class="short_name_value">
                                                    <%
                                                    if (mode.equals("view")) { %>
                                                        <%=Util.processForDisplay(dataset.getShortName())%>
                                                        <input type="hidden" name="ds_name" value="<%=Util.processForDisplay(dataset.getShortName(),true)%>"/><%
                                                    }
                                                    else if (mode.equals("add")) {%>
                                                        <input class="smalltext" type="text" size="40" name="ds_name"/><%
                                                    }
                                                    else{ %>
                                                        <input class="smalltext" type="text" size="40" name="ds_name" value="<%=Util.processForDisplay(dataset.getShortName())%>"/><%
                                                    }
                                                    %>
                                                </td>

                                                <%isOdd = Util.isOdd(++displayed);%>
                                            </tr>

                                            <!-- RegistrationStatus -->

                                            <tr class="<%=isOdd%>">
                                                <th scope="row" class="scope-row simple_attr_title">
                                                    Registration status
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=regstatus"></a>
                                                </th>
                                                <%
                                                if (!mode.equals("view")) {%>
                                                    <td class="simple_attr_help">
                                                        <img src="<%=request.getContextPath()%>/images/mandatory.gif" alt="Mandatory"  title="Mandatory"/>
                                                    </td><%
                                                }
                                                %>
                                                <td class="simple_attr_value">
                                                    <%
                                                    if (mode.equals("view")) { %>
                                                        <%=Util.processForDisplay(regStatus)%>
                                                        <%
                                                        long timestamp = dataset.getDate() == null ? 0 : Long.parseLong(dataset.getDate());
                                                        String dateString = timestamp == 0 ? "" : eionet.util.Util.releasedDate(timestamp);
                                                        String dateTimeString = timestamp == 0 ? "" : dateString + " " + eionet.util.Util.hoursMinutesSeconds(timestamp);

                                                        if (workingUser!=null) {
                                                            if (dataset.isWorkingCopy() && user!=null && workingUser.equals(user.getUserName())) {
                                                                %>
                                                                <span class="caution" title="Checked out on <%=dateTimeString%>">(Working copy)</span><%
                                                            }
                                                            else if (user!=null) {
                                                                %>
                                                                <span class="caution">(checked out by <em><%=workingUser%></em>)</span><%
                                                            }
                                                        }
                                                        else if (dateString.length()>0 && (regStatus.equalsIgnoreCase("RELEASED") || user!=null)) {
                                                            if (user == null) {
                                                                %><span><%=dateString%></span><%
                                                            }
                                                            else{
                                                                %><span style="color:#A8A8A8;font-size:0.8em">(checked in <%=dateTimeString%> by <%=dataset.getUser()%>)</span><%
                                                            }
                                                        }
                                                    }
                                                    else{ %>
                                                        <select name="reg_status" onchange="form_changed('form1')"> <%
                                                            Vector regStatuses = "add".equals(mode) ? verMan.getSettableRegStatuses() : verMan.getRegStatuses();
                                                            for (int i = 0; i < regStatuses.size(); i++) {
                                                                String status = (String)regStatuses.get(i);
                                                                String selected = status.equals(regStatus) ? "selected=\"selected\"" : "";
                                                                String disabled = verMan.getSettableRegStatuses().contains(status) ? "" : "disabled=\"disabled\"";
                                                                String title = disabled.length() > 0 ? "table=\"This status not allowed any more when adding/saving.\"" : "";
                                                                String style = disabled.length() > 0 ? "style=\"background-color: #F2F2F2;\"" : "";
                                                                %>
                                                                <option <%=style%> <%=selected%> <%=disabled%> <%=title%> value="<%=Util.processForDisplay(status)%>"><%=Util.processForDisplay(status)%></option><%
                                                            } %>
                                                        </select><%
                                                    }
                                                    %>
                                                </td>

                                                <%isOdd = Util.isOdd(++displayed);%>
                                            </tr>

                                            <!-- Reference URL -->
                                            <%
                                            if (mode.equals("view")) {
                                                String refUrl = dataset.getReferenceURL();
                                                %>
                                              <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        Reference URL
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=refurl"></a>
                                                    </th>
                                                    <td class="simple_attr_value">
                                                        <small><a  href="<%=refUrl%>"><%=refUrl%></a></small>
                                                    </td>

                                                    <%isOdd = Util.isOdd(++displayed);%>
                                                </tr><%
                                            }
                                            %>

                                            <!-- dynamic attributes -->
                                            <%
                                            for (int i=0; mAttributes!=null && i<mAttributes.size(); i++) {

                                                attribute = (DElemAttribute)mAttributes.get(i);
                                                String dispType = attribute.getDisplayType();
                                                if (dispType == null) continue;

                                                String attrOblig = attribute.getObligation();
                                                String obligImg  = "optional.gif";
                                                String obligTxt  = "Optional";
                                                if (attrOblig.equalsIgnoreCase("M")) {
                                                    obligImg = "mandatory.gif";
                                                    obligTxt  = "Mandatory";
                                                }
                                                else if (attrOblig.equalsIgnoreCase("C")) {
                                                    obligImg = "conditional.gif";
                                                    obligTxt  = "Conditional";
                                                }

                                                if (!attribute.displayFor("DST")) continue;

                                                attrID = attribute.getID();
                                                attrValue = getValue(attrID, mode, attributes);

                                                if (mode.equals("view") && (attrValue == null || attrValue.length() == 0))
                                                    continue;

                                                //displayed++; - done below

                                                String width  = attribute.getDisplayWidth();
                                                String height = attribute.getDisplayHeight();

                                                String disabled = user == null ? "disabled='disabled'" : "";

                                                boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
                                                Vector multiValues=null;
                                                if (dispMultiple) {
                                                    multiValues = getValues(attrID, mode, attributes);
                                                }

                                                %>

                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        <%=Util.processForDisplay(attribute.getName())%>
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                                                    </th>
                                                    <%
                                                    if (!mode.equals("view")) {%>
                                                        <td class="simple_attr_help">
                                                            <img src="<%=request.getContextPath()%>/images/<%=Util.processForDisplay(obligImg)%>" alt="<%=Util.processForDisplay(obligTxt)%>" title="<%=Util.processForDisplay(obligTxt)%>"/>
                                                        </td><%
                                                    }
                                                    %>

                                                    <!-- dynamic attribute value display -->

                                                    <td class="simple_attr_value">
                                                        <%

                                                        // if mode is 'view', display simple a text, otherwise an input
                                                        if (mode.equals("view")) {
                                                            if (dispMultiple) {
                                                                for (int k=0; multiValues!=null && k<multiValues.size(); k++) {
                                                                    attrValue = (String)multiValues.get(k);
                                                                    %><%if (k>0)%>, <%;%><%=Util.processForDisplay(attrValue)%><%
                                                                }
                                                            }
                                                            else{ %>
                                                                <%=Util.processForDisplay(attrValue)%> <%
                                                            }
                                                        }
                                                        else{ // start display input

                                                            if (dispMultiple) { // mutliple display

                                                                Vector allPossibleValues = null;
                                                                if (dispType.equals("select"))
                                                                    allPossibleValues = searchEngine.getFixedValues(attrID, "attr");
                                                                else if (dispType.equals("text"))
                                                                    allPossibleValues = searchEngine.getSimpleAttributeValues(attrID);

                                                                String divHeight = "7.5em";
                                                                String textName = "other_value_attr_" + attrID;
                                                                String divID = "multiselect_div_attr_" + attrID;
                                                                String checkboxName = "attr_mult_" + attrID;
                                                                Vector displayValues = new Vector();
                                                                if (multiValues!=null && multiValues.size()>0)
                                                                    displayValues.addAll(multiValues);
                                                                if (allPossibleValues!=null && allPossibleValues.size()>0)
                                                                    displayValues.addAll(allPossibleValues);
                                                                %>
                                                                <input type="text" name="<%=textName%>" value="insert other value" style="font-size:0.9em" onfocus="this.value=''"/>
                                                                <input type="button" value="-&gt;" style="font-size:0.8em;" onclick="addMultiSelectRow(document.forms['form1'].elements['<%=textName%>'].value, '<%=checkboxName%>','<%=divID%>')"/>
                                                                <div id="<%=divID%>" class="multiselect" style="height:<%=divHeight%>;width:25em;">
                                                                    <%
                                                                    HashSet displayedSet = new HashSet();
                                                                    for (int k=0; displayValues!=null && k<displayValues.size(); k++) {

                                                                        Object valueObject = displayValues.get(k);
                                                                        attrValue = (valueObject instanceof FixedValue) ? ((FixedValue)valueObject).getValue() : valueObject.toString();
                                                                        if (displayedSet.contains(attrValue))
                                                                            continue;

                                                                        String strChecked = "";
                                                                        if (multiValues!=null && multiValues.contains(attrValue))
                                                                            strChecked = "checked=\"checked\"";
                                                                        %>
                                                                        <label style="display:block">
                                                                            <input type="checkbox" name="<%=checkboxName%>" value="<%=attrValue%>" <%=strChecked%> style="margin-right:5px"/><%=attrValue%>
                                                                        </label>
                                                                        <%
                                                                        displayedSet.add(attrValue);
                                                                    }
                                                                    %>
                                                                </div>
                                                                <%
                                                            }
                                                            else{ // no multiple display

                                                                if (dispType.equals("text")) {
                                                                    if (attrValue!=null) {
                                                                        %>
                                                                        <input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" value="<%=Util.processForDisplay(attrValue)%>" onchange="form_changed('form1')"/>
                                                                        <%
                                                                    }
                                                                    else{
                                                                        %>
                                                                        <input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"/>
                                                                        <%
                                                                    }
                                                                }
                                                                else if (dispType.equals("textarea")) {
                                                                    if (attrValue!=null) {
                                                                        %>
                                                                        <textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"><%=Util.processForDisplay(attrValue, true, true)%></textarea>
                                                                        <%
                                                                    }
                                                                    else{
                                                                        %>
                                                                        <textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"></textarea>
                                                                        <%
                                                                    }
                                                                }
                                                                else if (dispType.equals("select")) { %>
                                                                    <select <%=disabled%> class="small" name="attr_<%=attrID%>" onchange="form_changed('form1')">
                                                                        <%
                                                                        Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
                                                                        if (fxValues == null || fxValues.size() == 0) { %>
                                                                            <option selected="selected" value=""></option> <%
                                                                        }
                                                                        else{
                                                                            boolean selectedByValue = false;
                                                                            for (int g=0; g<fxValues.size(); g++) {
                                                                                FixedValue fxValue = (FixedValue)fxValues.get(g);

                                                                                String isSelected = (fxValue.getDefault() && !selectedByValue) ? "selected='selected'" : "";

                                                                                if (attrValue!=null && attrValue.equals(fxValue.getValue())) {
                                                                                    isSelected = "selected='selected'";
                                                                                    selectedByValue = true;
                                                                                }

                                                                                %>
                                                                                <option <%=isSelected%> value="<%=Util.processForDisplay(fxValue.getValue())%>"><%=Util.processForDisplay(fxValue.getValue())%></option> <%
                                                                            }
                                                                        }
                                                                        %>
                                                                    </select>
                                                                    <a class="helpButton" href="<%=request.getContextPath()%>/fixedvalues/attr/<%=attrID%>">
                                                                        
                                                                    </a>
                                                                    <%
                                                                }
                                                                else{ %>
                                                                    Unknown display type!<%
                                                                }

                                                            } // end of no multiple display

                                                        } // end display input
                                                        %>
                                                        <input type="hidden" name="oblig_<%=attrID%>" value="<%=Util.processForDisplay(attribute.getObligation(),true)%>"/>
                                                    </td>

                                                    <!-- end of dynamic attribute value display -->

                                                    <%isOdd = Util.isOdd(++displayed);%>
                                                </tr>
                                                <!-- end of dynamic attribute row -->
                                                <%
                                            }
                                            %>

                                            <!-- public outputs -->

                                            <%
                                            if (!mode.equals("add") && editPrm) {
                                                String checkedPDF = dataset.displayCreateLink("PDF") ? "checked='checked'" : "";
                                                String checkedXLS = dataset.displayCreateLink("XLS") ? "checked='checked'" : "";
                                                String checkedODS = dataset.displayCreateLink("ODS") ? "checked='checked'" : "";
                                                String checkedMDB = dataset.displayCreateLink("MDB") ? "checked='checked'" : "";
                                                String checkedXmlSchema = dataset.displayCreateLink("XMLSCHEMA") ? "checked='checked'" : "";
                                                %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        Public outputs
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=public_outputs"></a>
                                                    </th>
                                                    <%
                                                    if (!mode.equals("view")) {%>
                                                        <td class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/optional.gif" width="16" height="16" alt="optional"/>
                                                        </td><%
                                                    }
                                                    %>
                                                    <td class="simple_attr_value">
                                                        <%
                                                        if(mode.equals("view")) { %>
                                                            <input type="checkbox" disabled="disabled" <%=checkedPDF%>/>
                                                                <small>Technical specification in PDF format</small>
                                                            <br/>
                                                            <input type="checkbox" disabled="disabled" <%=checkedXLS%>/>
                                                                <small>MS Excel template</small>
                                                            <br/>
                                                            <input type="checkbox" disabled="disabled" <%=checkedXmlSchema%>/>
                                                                <small>The definition on XML Schema format</small>
                                                            <br/>
                                                            <input type="checkbox" disabled="disabled" <%=checkedODS%>/>
                                                                <small>OpenDocument spreadsheet</small>
                                                            <%
                                                        }
                                                        else{ %>
                                                            <input type="checkbox" name="disp_create_links" value="PDF" <%=checkedPDF%>/>
                                                                <small>Technical specification in PDF format</small>
                                                            <br/>
                                                            <input type="checkbox" name="disp_create_links" value="XLS" <%=checkedXLS%>/>
                                                                <small>MS Excel template</small>
                                                            <br/>
                                                            <input type="checkbox" name="disp_create_links" value="XMLSCHEMA" <%=checkedXmlSchema%>/>
                                                                <small>The definition on XML Schema format</small>
                                                            <br/>
                                                            <input type="checkbox" name="disp_create_links" value="ODS" <%=checkedODS%>/>
                                                                <small>OpenDocument spreadsheet</small>
                                                            <%
                                                        }
                                                        %>
                                                    </td>

                                                    <%isOdd = Util.isOdd(++displayed);%>
                                                </tr><%
                                            }%>

                                            <!-- dataset number -->
                                            <%
                                            // display only in non-add mode and for users with edit prm
                                            if (!mode.equals("add") && editPrm) {
                                                %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        Dataset number
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=dataset_number"></a>
                                                    </th>
                                                    <%
                                                    if (!mode.equals("view")) {%>
                                                        <td class="simple_attr_help">
                                                            <img src="<%=request.getContextPath()%>/images/mandatory.gif" alt="Mandatory" title="Mandatory"/>
                                                        </td><%
                                                    }
                                                    %>
                                                    <td class="simple_attr_value">
                                                        <%=dataset.getID()%>
                                                    </td>

                                                    <%isOdd = Util.isOdd(++displayed);%>
                                                </tr><%
                                            }
                                            %>
                                            <!-- add, save, check-in, undo check-out buttons -->
                                            <%
                                            // add case
                                            if (mode.equals("add")) { %>
                                                <tr>
                                                    <th></th>
                                                    <td colspan="3"><input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/></td>
                                                </tr>
                                            <%
                                            }
                                            // edit case
                                            else if (mode.equals("edit") && dataset!=null && dataset.isWorkingCopy()) {
                                                if (workingUser!=null && user!=null && workingUser.equals(user.getUserName())) {
                                                    %>
                                                    <tr>
                                                        <th></th>
                                                        <td colspan="3">
                                                            <input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&nbsp;
                                                            <input type="button" class="mediumbuttonb" value="Save &amp; close" onclick="submitForm('editclose')"/>&nbsp;
                                                            <input type="button" class="mediumbuttonb" value="Cancel" onclick="goTo('view', '<%=ds_id%>')"/>
                                                        </td>
                                                    </tr>
                                                    <%
                                                }
                                            }
                                            %>
                                        </table>
                                        <!-- end of attributes -->

                                        <!-- data model -->
                                        <%
                                        if ((mode.equals("edit") && user!=null) || (mode.equals("view") && dataset.getVisual()!=null)) {
                                            // thumbnail
                                            if (mode.equals("view") && dataset.getVisual()!=null) {
                                            %>
                                            <h2 id="model">
                                                Data model
                                            </h2>
                                            <%
                                                if (imgVisual) { %>
<div class="figure-plus-container">
  <div class="figure-plus">
    <div class="figure-image">
      <a href="<%=request.getContextPath()%>/visuals/<%=Util.processForDisplay(dsVisual)%>"><img src="<%=request.getContextPath()%>/visuals/<%=Util.processForDisplay(dsVisual)%>"
         alt="thumbnail" class="scaled poponmouseclick"/></a>
    </div>
    <div class="figure-note">
      Click thumbnail to view large version of the data model
    </div>
  </div>
</div>
                                                    <%
                                                }
                                                else{ %>
                                                    <div style="text-align:right">
                                                        The file representing the dataset stucture cannot be displayed on this web-page.
                                                        But you can see it by pressing the following link:<br/>
                                                        <a href="javascript:openStructure('visuals/<%=Util.processForDisplay(dsVisual)%>')"><%=Util.processForDisplay(dsVisual)%></a>
                                                    </div><%
                                                }
                                            }
                                        }
                                        %>

                                        <!-- tables list -->

                                        <%
                                        if ((mode.equals("view") && tables!=null && tables.size()>0) || mode.equals("edit")) {
                                            // tables table
                                            if (mode.equals("view")) {
                                                %>
                                            <h2 id="tables">
                                                Dataset tables
                                            </h2>
                                                <table class="datatable results">
                                                    <col style="width:50%"/>
                                                    <col style="width:50%"/>
                                                    <thead>
                                                    <tr>
                                                        <th>Full name</th>
                                                        <th>Short name</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    <%
                                                    boolean hasMarkedTables = false;
                                                    for (int i=0; i<tables.size(); i++) {

                                                        DsTable table = (DsTable)tables.get(i);
                                                        String tableLink = "";
                                                        if (isLatestRequested) {
                                                            tableLink = request.getContextPath() + "/datasets/latest/" + dataset.getIdentifier() + "/tables/" + table.getIdentifier();
                                                        }
                                                        else{
                                                            tableLink = request.getContextPath() + "/tables/" + table.getID();
                                                        }

                                                        String tblFullName = "";
                                                        attributes = searchEngine.getAttributes(table.getID(), "T", DElemAttribute.TYPE_SIMPLE);
                                                        for (int c=0; c<attributes.size(); c++) {
                                                            DElemAttribute attr = (DElemAttribute)attributes.get(c);
                                                               if (attr.getName().equalsIgnoreCase("Name"))
                                                                   tblFullName = attr.getValue();
                                                        }
                                                        if (tblFullName!=null && tblFullName.length()>40)
                                                            tblFullName = tblFullName.substring(0,40) + " ...";
                                                        String escapedFullName = Util.processForDisplay(tblFullName,true,true);
                                                        String zebraClass  = (i + 1) % 2 != 0 ? "odd" : "even";
                                                        %>
                                                        <tr class="<%=zebraClass%>">
                                                            <td>
                                                                <a href="<%=tableLink%>" title="<%=escapedFullName%>">
                                                                    <%=escapedFullName%>
                                                                </a>
                                                            </td>
                                                            <td>
                                                                <%=Util.processForDisplay(table.getShortName())%>
                                                            </td>
                                                        </tr>
                                                        <%
                                                    }
                                                    %>
                                                    </tbody>
                                                </table><%
                                            }
                                        }
                                        %>

                                        <!-- rod links -->

                                        <%
                                        if (mode.equals("edit") || (mode.equals("view") && rodLinks!=null && rodLinks.size()>0)) {

                                            %>

                                                <!-- title & link part -->

                                                <!-- table part -->
                                                <%
                                                if (mode.equals("view") && rodLinks!=null && rodLinks.size()>0) {%>
                                                <h2 id="rodlinks">
                                                        Obligations in ROD
                                                </h2>
                                                            <table class="datatable results">
                                                                <col style="width:20%"/>
                                                                <col style="width:40%"/>
                                                                <col style="width:40%"/>
                                                                <thead>
                                                                    <tr>
                                                                        <th>Obligation</th>
                                                                        <th>Legal instrument</th>
                                                                        <th>Details</th>
                                                                    </tr>
                                                                </thead>
                                                                <%
                                                                // rows
                                                                for (int i=0; i<rodLinks.size(); i++) {

                                                                    Hashtable rodLink = (Hashtable)rodLinks.get(i);
                                                                    String raTitle = (String)rodLink.get("ra-title");
                                                                    String liTitle = (String)rodLink.get("li-title");
                                                                    String raDetails = (String)rodLink.get("ra-url");

                                                                    %>
                                                                    <tr>
                                                                        <td>
                                                                            <%=Util.processForDisplay(raTitle)%>
                                                                        </td>
                                                                        <td>
                                                                            <%=Util.processForDisplay(liTitle)%>
                                                                        </td>
                                                                        <td>
                                                                            <a  href="<%=Util.processForDisplay(raDetails, true)%>"><%=Util.processForDisplay(raDetails, true)%></a>
                                                                        </td>
                                                                    </tr><%
                                                                }
                                                                %>
                                                            </table>
                                                    <%
                                                }
                                        }
                                        %>


                                        <!-- complex attributes -->

                                        <%
                                        if ((mode.equals("edit") && user!=null) || (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0)) {
                                                // the table
                                                if (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0) {
                                                    %>
                                                <h2 id="cattrs">
                                                    Complex attributes
                                                </h2>
                                                            <table class="datatable results" id="dataset-attributes">
                                                                <col style="width:30%"/>
                                                                <col style="width:70%"/>
                                                                <%
                                                                displayed = 1;
                                                                isOdd = Util.isOdd(displayed);
                                                                for (int i=0; i<complexAttrs.size(); i++) {

                                                                    DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
                                                                    attrID = attr.getID();
                                                                    String attrName = attr.getName();
                                                                    Vector attrFields = searchEngine.getAttrFields(attrID, DElemAttribute.FIELD_PRIORITY_HIGH);
                                                                    %>

                                                                    <tr class="<%=isOdd%>">
                                                                        <td>
                                                                            <a href="<%=request.getContextPath()%>/complex_attr.jsp?attr_id=<%=attrID%>&amp;parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=Util.processForDisplay(ds_name)%>&amp;ds=true" title="Click here to view all the fields">
                                                                                <%=Util.processForDisplay(attrName)%>
                                                                            </a>
                                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX"></a>
                                                                        </td>
                                                                        <td>
                                                                            <%
                                                                            StringBuffer rowValue=null;
                                                                            Vector rows = attr.getRows();
                                                                            for (int j=0; rows!=null && j<rows.size(); j++) {

                                                                                if (j>0) {%>---<br/><%}

                                                                                Hashtable rowHash = (Hashtable)rows.get(j);
                                                                                rowValue = new StringBuffer();

                                                                                for (int t=0; t<attrFields.size(); t++) {
                                                                                    Hashtable hash = (Hashtable)attrFields.get(t);
                                                                                    String fieldID = (String)hash.get("id");
                                                                                    String fieldValue = fieldID == null ? null : (String)rowHash.get(fieldID);
                                                                                    if (fieldValue == null) fieldValue = "";
                                                                                    if (fieldValue.trim().equals("")) continue;

                                                                                    if (t>0 && fieldValue.length()>0  && rowValue.toString().length()>0)
                                                                                        rowValue.append(", ");

                                                                                    rowValue.append(Util.processForDisplay(fieldValue));
                                                                                    %>
                                                                                    <%=Util.processForDisplay(fieldValue)%><br/><%
                                                                                }
                                                                            }
                                                                            %>
                                                                        </td>

                                                                        <% isOdd = Util.isOdd(++displayed); %>
                                                                    </tr><%
                                                                }
                                                                %>
                                                            </table>
                                                    <%
                                                }
                                                %>

                                            <!-- end complex attributes -->

                                            <%
                                            // other versions
                                            if (mode.equals("view") && otherVersions!=null && otherVersions.size()>0) {%>
                                                <h2 id="versions">
                                                    Other versions
                                                </h2>
                                                <table class="datatable results">
                                                    <col style="width:25%"/>
                                                    <col style="width:25%"/>
                                                    <col style="width:25%"/>
                                                    <col style="width:25%"/>
                                                    <thead>
                                                        <tr>
                                                            <th>Dataset number</th>
                                                            <th>Status</th>
                                                            <th>Release date</th>
                                                            <th></th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                    <%
                                                    Dataset otherVer;
                                                    for (int i=0; i<otherVersions.size(); i++) {
                                                        otherVer = (Dataset)otherVersions.get(i);
                                                        String status = otherVer.getStatus();
                                                        String releaseDate = null;
                                                        String releaseDateHint = null;
                                                        if (status.equals("Released")) {
                                                            releaseDate = otherVer.getDate();
                                                        }
                                                        if (releaseDate!=null) {
                                                            long timestamp = Long.parseLong(releaseDate);
                                                            releaseDate = eionet.util.Util.releasedDate(timestamp);
                                                            releaseDateHint = releaseDate + " " + eionet.util.Util.hoursMinutesSeconds(timestamp);
                                                        }
                                                        else{
                                                            releaseDate = "";
                                                            releaseDateHint = "";
                                                        }
                                                        String zebraClass  = (i + 1) % 2 != 0 ? "odd" : "even";
                                                        %>
                                                        <tr class="<%=zebraClass%>">
                                                            <td><%=otherVer.getID()%></td>
                                                            <td><%=status%></td>
                                                            <td title="<%=releaseDateHint%>"><%=releaseDate%></td>
                                                            <td>
                                                                <%
                                                                if (searchEngine.skipByRegStatus(otherVer.getStatus())) { %>
                                                                    &nbsp;<%
                                                                }
                                                                else{ %>
                                                                    [<a href="<%=request.getContextPath()%>/datasets/<%=otherVer.getID()%>">view</a>]<%
                                                                }
                                                                %>
                                                            </td>
                                                        </tr>
                                                        <%
                                                    }
                                                    %>
                                                    </tbody>
                                                </table>
                                                <%
                                            }
                                        }
                                        %>

                                    </div>
                                <!-- end dotted -->

                <!-- various hidden inputs -->
                <div style="display:none">
                    <input type="hidden" name="mode" value="<%=mode%>"/>
                    <input type="hidden" name="check_in" value="false"/>
                    <input type="hidden" name="changed" value="0"/>
                    <input type="hidden" name="complete" value="false"/>
                    <input type="hidden" name="saveclose" value="false"/>
                    <%
                    String checkedoutCopyID = dataset == null ? null : dataset.getCheckedoutCopyID();
                    if (checkedoutCopyID!=null) {%>
                        <input type="hidden" name="checkedout_copy_id" value="<%=checkedoutCopyID%>"/><%
                    }
                    if (dataset!=null) {

                        if (dataset.isWorkingCopy()) {
                            %>
                            <input type="hidden" name="is_working_copy" value="true"/><%
                        }
                        String checkInNo = dataset.getVersion();
                        if (checkInNo.equals("1")) {
                            %>
                            <input type="hidden" name="upd_version" value="true"/><%
                        }
                    }
                    // submitter url, might be used by POST handler who might want to send back to POST submitter
                    String submitterUrl = Util.getServletPathWithQueryString(request);
                    if (submitterUrl!=null) {
                        submitterUrl = Util.processForDisplay(submitterUrl);
                        %>
                        <input type="hidden" name="submitter_url" value="<%=submitterUrl%>"/><%
                    }
                    %>
                </div>
            </form>

            </div> <!-- workarea -->
            </div> <!-- container -->
            <%@ include file="footer.jsp" %>

<script type="text/javascript">
    var contextPath = "<%=request.getContextPath()%>";
</script>
<script type="text/javascript" src="<%=request.getContextPath()%>/popbox.js"></script>
<script type="text/javascript">
// <![CDATA[
        var popclickpop = {
         'onclick' : function() { Pop(this,-50,'PopBoxImageLarge'); },
         'pbShowPopBar' : false,
         'pbShowPopImage' : false,
         'pbShowPopText' : false,
         'pbShowRevertImage': true,
         'pbShowPopCaption' : true
        };
        PopRegisterClass('poponmouseclick', popclickpop);
    popBoxShowPopBar = false;
// ]]>
</script>
</body>
</html>

<%
// end the whole page try block
}
catch (Exception e) {
    if (response.isCommitted())
        e.printStackTrace(System.out);
    else{
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
}
finally {
    try { if (conn!=null) conn.close();
    } catch (SQLException e) {}
}
%>
