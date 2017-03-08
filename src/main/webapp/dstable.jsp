<%@page import="eionet.meta.dao.domain.VocabularyConcept"%>
<%@page import="eionet.datadict.model.DataDictEntity"%>
<%@page import="eionet.meta.dao.domain.VocabularyFolder"%>
<%@page import="eionet.meta.notif.Subscriber"%>
<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.meta.service.data.*,eionet.util.sql.ConnectionUtil"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%> 


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

    <%!
    // servlet-scope helper functions
    //////////////////////////////////

    /**
     *
     */
    private String getValue(String id, String mode, Vector attributes){
        return getValue(id, 0, mode, attributes);
    }

    /**
     *    int val indicates which type of value is requested. the default is 0
     *    0 - display value (if original value is null, then show inherited value)
     *    1 - original value
     *    2 - inherited value
     */
    private String getValue(String id, int val, String mode, Vector attributes){
        if (id==null) return null;
        if (mode.equals("add") && val<2) return null;
        for (int i=0; attributes!=null && i<attributes.size(); i++){
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (id.equals(attr.getID())){
                if (val==1)
                    return attr.getOriginalValue();
                else if (val==2)
                    return attr.getInheritedValue();
                else
                    return attr.getValue();

            }
        }
        return null;
    }

    /**
     *
     */
    private Vector getValues(String id, String mode, Vector attributes){
        return getValues(id, 0, mode, attributes);
    }

    /**
     *  int val indicates which group of values is requested. the default is 0
     *  0 - all
     *  1 - original
     *  2 - inherited
     */
    private Vector getValues(String id, int val, String mode, Vector attributes){
        if (id==null) return null;
        if (mode.equals("add") && val<2) return null;
        for (int i=0; attributes!=null && i<attributes.size(); i++){
            DElemAttribute attr = (DElemAttribute)attributes.get(i);
            if (id.equals(attr.getID())){
                if (val==1)
                    return attr.getOriginalValues();
                else if (val==2)
                    return attr.getInheritedValues();
                else
                    return attr.getValues();
            }
        }
        return null;
    }

    /**
     *
     */
    private String getAttributeIdByName(String name, Vector mAttributes){
        for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
            DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr.getID();
        }
        return null;
    }

    /**
     *
     */
    private String getAttributeValue(DataElement elem, String name, Vector mAttributes){
        String id = getAttributeIdByName(name, mAttributes);
        if (elem == null) return null;
        DElemAttribute attr = elem.getAttributeById(id);
        if (attr == null) return null;
        return attr.getValue();
    }
    %>

    <%
    // implementation of the servlet's service method
    //////////////////////////////////////////////////

    request.setCharacterEncoding("UTF-8");
    ServletContext ctx = getServletContext();
    Vector mAttributes = null;
    Vector attributes = null;
    Vector complexAttrs = null;
    Vector elems = null;
    String mode = null;
    String dsIdf = null;
    String feedbackValue = null;

    // make sure page is not cached
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    DDUser user = SecurityUtil.getUser(request);

    // POST request not allowed for anybody who hasn't logged in
    if (request.getMethod().equals("POST") && user==null){
        request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    // get values of several request parameters:
    // - mode
    // - table's id number
    // - table's alphanumeric identifier
    // - id number of table to copy
    // - dataset's id number
    String tableIdf = request.getParameter("table_idf");
    String tableID = request.getParameter("table_id");
    String copy_tbl_id = request.getParameter("copy_tbl_id");
    String dsID = request.getParameter("ds_id");
    String dsName = request.getParameter("ds_name");
    String datasetIdf = request.getParameter("dataset_idf");
    String action = request.getParameter("action");

    mode = request.getParameter("mode");

    if (mode == null || mode.trim().length()==0){
        mode = "view";
    }

    if (mode.equals("add")){
        if (Util.isEmpty(dsID)){
            request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }
    else if (mode.equals("view")){
        if (Util.isEmpty(tableID) && (Util.isEmpty(tableIdf) || Util.isEmpty(datasetIdf))){
            request.setAttribute("DD_ERR_MSG", "Missing request parameter: table_id or (table_idf and dataset_idf)");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }
    else if (mode.equals("edit")){
        if (Util.isEmpty(tableID)){
            request.setAttribute("DD_ERR_MSG", "Missing request parameter: table_id");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }
    else if (mode.equals("copy")){
        if (Util.isEmpty(copy_tbl_id)){
            request.setAttribute("DD_ERR_MSG", "Missing request parameter: copy_tbl_id");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }

    // if requested by alphanumeric identifier and not by auto-generated id,
    // then it means the table's latest version is requested
    boolean isLatestRequested = mode.equals("view") && !Util.isEmpty(tableIdf) && !Util.isEmpty(datasetIdf) && Util.isEmpty(tableID);

    //// handle the POST request//////////////////////
    //////////////////////////////////////////////////
    if (request.getMethod().equals("POST")){

        Connection userConn = null;
        DsTableHandler handler = null;
        try{
            userConn = user.getConnection();
            handler = new DsTableHandler(userConn, request, ctx);
            handler.setUser(user);
            handler.setVersioning(false);

            try{
                handler.execute();
            }
            catch (Exception e){
                String msg = e.getMessage();
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(bytesOut));
                String trace = bytesOut.toString(response.getCharacterEncoding());
                request.setAttribute("DD_ERR_MSG", msg);
                request.setAttribute("DD_ERR_TRC", trace);
                String backLink = request.getParameter("submitter_url");
                if (backLink==null || backLink.length()==0)
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

        // disptach the POST request
        ////////////////////////////
        String redirUrl = null;
        if (mode.equals("add")){
            // if this was add, send to the added copy
            String id = handler.getLastInsertID();
            if (id!=null && id.length()>0){
                redirUrl = request.getContextPath() + "/tables/" + id;
            }

            if (dsName!=null || dsID!=null){
                String queryStr = "/?";
                if (dsName!=null){
                    queryStr = queryStr + "ds_name=" + dsName;
                }
                if (dsID!=null){
                    if (!queryStr.endsWith("?")){
                        queryStr = queryStr + "&";
                    }
                    queryStr = queryStr + "ds_id=" + dsID;
                }
            }

            if (history!=null){
                history.remove(history.getCurrentIndex());
            }
        }
        else if (mode.equals("edit")){
            // if this was a "saveclose", send to view mode, otherwise stay in edit mode
            redirUrl = request.getContextPath() + "/tables/" + tableID;
            String strSaveclose = request.getParameter("saveclose");
            if (strSaveclose==null || strSaveclose.equals("false")){
                redirUrl = redirUrl + "/edit";
            }
        }
        else if (mode.equals("delete")){
            // if dataset id number given, send to view mode of the dataset working copy, otherwise to home page
            if (dsID!=null && !dsID.isEmpty()){
                redirUrl = request.getContextPath() + "/datasets/" + dsID;
            }
            else{
                redirUrl = request.getContextPath();
            }
        }
        else if (mode.equals("copy")){
            String id = handler.getLastInsertID();
            if (id!=null && id.length()>0){
                redirUrl = request.getContextPath() + "/tables/" + id; //+ "/edit";
            }
            if (history!=null){
                history.remove(history.getCurrentIndex());
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
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);

        // if not in add mode, get the table object
        DsTable dsTable = null;
        if (!mode.equals("add")){

            if (isLatestRequested){

                Vector v = new Vector();
                if (user==null){
                    v.add("Recorded");
                    v.add("Released");
                }
                dsTable = searchEngine.getLatestTbl(tableIdf, datasetIdf, v);
                if (dsTable!=null){
                    tableID = dsTable.getID();
                }
            }
            else{
                dsTable = searchEngine.getDatasetTable(tableID);
            }

            if (dsTable == null){
                if (user!=null){
                    request.setAttribute("DD_ERR_MSG", "Could not find a table of this id or identifier in any status");
                }
                else{
                    request.setAttribute("DD_ERR_MSG", "Could not find a table of this id or identifier in 'Recorded' or 'Released' status! " +
                    "As an anonymous user, you are not allowed to see definitions in any other status.");
                }
                session.setAttribute(AfterCASLoginServlet.AFTER_LOGIN_ATTR_NAME, SecurityUtil.buildAfterLoginURL(request));
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }

            // overwrite dataset id parameter with the one from table object
            dsID = dsTable.getDatasetID();
            if (dsID==null || dsID.length()==0){
                request.setAttribute("DD_ERR_MSG", "Missing dataset id number in the table object");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        }

        // get the dataset object (having reached this point, dataset id number is not null)
        String workingUser = null;
        Dataset dataset = searchEngine.getDataset(dsID);
        if (dataset==null){
            request.setAttribute("DD_ERR_MSG", "No dataset found with this id number: " + dsID);
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        // anonymous users should not be allowed to see tables of a dataset working copy
        if (mode.equals("view") && user==null && dataset.isWorkingCopy()){
            request.setAttribute("DD_ERR_MSG", "Anonymous users are not allowed to view tables from a dataset working copy!");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
        // anonymous users should not be allowed to see tables from datasets that are NOT in Recorded or Released status
        if (mode.equals("view") && user==null && dataset.getStatus()!=null && !dataset.getStatus().equals("Recorded") && !dataset.getStatus().equals("Released")){
            request.setAttribute("DD_ERR_MSG", "Tables from datasets NOT in Recorded or Released status are inaccessible for anonymous users.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        if (mode.equals("view") && action!=null && action.equals("subscribe") && dsTable!=null && dataset!=null){
            Subscriber.subscribeToTable(Collections.singleton(user.getUserName()), dataset.getIdentifier(), dsTable.getIdentifier());
            feedbackValue = "Subscription successful!";
        }

        // set some helper variables
        dsName = dataset.getShortName();
        dsIdf = dataset.getIdentifier();
        workingUser = dataset.getWorkingUser();
        boolean editDstPrm = user!=null && dataset.isWorkingCopy() && workingUser!=null && workingUser.equals(user.getUserName());

        // security checks for identified users
        if (!mode.equals("view") && editDstPrm==false){
            request.setAttribute("DD_ERR_MSG", "You have no permission to do modifications in this dataset!");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        // get metadata of attributes
        mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
        // get values of attributes
        attributes = searchEngine.getAttributes(tableID, "T", DElemAttribute.TYPE_SIMPLE, null, dsID);
        complexAttrs = searchEngine.getComplexAttributes(tableID, "T", null, null, dsID);
        if (complexAttrs==null)
            complexAttrs = new Vector();

        // get the table's elements
        if (mode.equals("view") && dsTable!=null)
            elems = searchEngine.getDataElements(null, null, null, null, tableID);

        // prepare the page's HTML title, shown in browser title bar
        StringBuffer pageTitle = new StringBuffer();
        if (mode.equals("edit"))
            pageTitle.append("Edit table");
        else
            pageTitle.append("Table");
        if (dsTable!=null && dsTable.getShortName()!=null)
            pageTitle.append(" - ").append(dsTable.getShortName());
        if (dataset!=null && dataset.getShortName()!=null)
            pageTitle.append("/").append(dataset.getShortName());
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

        function submitForm(mode){

            if (mode == "delete"){
                <%
                if (dataset!=null && dataset.getStatus().equals("Released")){
                    %>
                    var a = confirm("Please be aware that you are about to remove a table from a dataset definition " +
                              "in Released status. Unless you change the dataset definition's status back to something lower, " +
                              "this removal will become instantly visible for the public visitors! " +
                              "Click OK, if you want to continue. Otherwise click Cancel.");
                    if (a==false) return;
                    <%
                }
                %>
            }

            if (mode != "delete"){
                if (!checkObligations()){
                    alert("You have not specified one of the mandatory fields!");
                    return;
                }

                if (hasWhiteSpace("idfier")){
                    alert("Identifier cannot contain any white space!");
                    return;
                }

                if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)){
                    alert("Identifier not valid for usage as an XML tag! " +
                          "In the first character only underscore or latin characters are allowed! " +
                          "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
                    return;
                }
            }

            //slctAllValues();

            if (mode=="editclose"){
                mode = "edit";
                document.forms["form1"].elements["saveclose"].value = "true";
            }

            document.forms["form1"].elements["mode"].value = mode;
            document.forms["form1"].submit();
        }

        function delDialogReturn(){
            var v = dialogWin.returnValue;
            if (v==null || v=="" || v=="cancel") return;

            document.forms["form1"].elements["upd_version"].value = v;
            deleteReady();
        }

        function deleteReady(){
            document.forms["form1"].elements["mode"].value = "delete";
            document.forms["form1"].submit();
        }
        
        function isIdentifierFilled(){
            var idf = document.forms["form1"].idfier;
            if (idf!=null && idf.value.length == 0) return false;
            return true;
        }

        function checkObligations(){

            var o = document.forms["form1"].short_name;
            if (o!=null && o.value.length == 0) return false;

            o = document.forms["form1"].idfier;
            if (o!=null && o.value.length == 0) return false;

            var elems = document.forms["form1"].elements;
            for (var i=0; elems!=null && i<elems.length; i++){
                var elem = elems[i];
                var elemName = elem.name;
                var elemValue = elem.value;
                if (startsWith(elemName, "attr_")){
                    var o = document.forms["form1"].elements[i+1];
                    if (o == null) return false;
                    if (!startsWith(o.name, "oblig_"))
                        continue;
                    if (o.value == "M" && (elemValue==null || elemValue.length==0)){
                        return false;
                    }
                }
            }

            return true;
        }

        function hasWhiteSpace(input_name){

            var elems = document.forms["form1"].elements;
            if (elems == null) return false;
            for (var i=0; i<elems.length; i++){
                var elem = elems[i];
                if (elem.name == input_name){
                    var val = elem.value;
                    if (val.indexOf(" ") != -1) return true;
                }
            }

            return false;
        }

        function goTo(mode, id){
            document.location.assign("<%=request.getContextPath()%>/tables/" + id + "/" + mode + "/?&ds_id=<%=dsID%>&ds_name=<%=dsName%>");
        }

        function openElements(uri){
            //uri = uri + "&open=true";
            wElems = window.open(uri,"TableElements","height=500,width=750,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
            if (window.focus) {wElems.focus()}
        }

        function slctAllValues(){

            var elems = document.forms["form1"].elements;
            if (elems == null) return true;

            for (var j=0; j<elems.length; j++){
                var elem = elems[j];
                var elemName = elem.name;
                if (startsWith(elemName, "attr_mult_")){
                    var slct = document.forms["form1"].elements[elemName];
                    if (slct.options && slct.length){
                        if (slct.length==1 && slct.options[0].value=="" && slct.options[0].text==""){
                            slct.remove(0);
                            slct.length = 0;
                        }
                        for (var i=0; i<slct.length; i++){
                            slct.options[i].selected = "true";
                        }
                    }
                }
            }
        }

        function startsWith(str, pattern){
            var i = str.indexOf(pattern,0);
            if (i!=-1 && i==0)
                return true;
            else
                return false;
        }

        function copyTbl(context){

            if (!isIdentifierFilled()){
                    alert("You have not specified the table's identifier.");
                    return;
            }

            if (hasWhiteSpace("idfier")){
                    alert("Identifier cannot contain any white space!");
                    return;
            }

            if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)){
                    alert("Identifier not valid for usage as an XML tag! " +
                          "In the first character only underscore or latin characters are allowed! " +
                          "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
                    return;
            }
            

            var url='/search_table.jsp?ctx=popup';
            wAdd = window.open(context+url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
            if (window.focus){wAdd.focus();}
        }

        function pickTable(id, name){

            document.forms["form1"].elements["copy_tbl_id"].value=id;
            document.forms["form1"].elements["mode"].value="copy";
            document.forms["form1"].submit();
            return true;
        }

        function validForXMLTag(str){

            // if empty string not allowed for XML tag
            if (str==null || str.length==0){
                return false;
            }

            // check the first character (only underscore or A-Z or a-z allowed)
            var ch = str.charCodeAt(0);
            if (!(ch==95 || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))){
                return false;
            }

            // check the rets of characters ((only underscore or hyphen or dot or 0-9 or A-Z or a-z allowed))
            if (str.length==1) return true;
            for (var i=1; i<str.length; i++){
                ch = str.charCodeAt(i);
                if (!(ch==95 || ch==45 || ch==46 || (ch>=48 && ch<=57) || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))){
                    return false;
                }
            }

            return true;
        }
// ]]>
</script>
</head>

<%
String hlpScreen = "table";
if (mode.equals("edit"))
    hlpScreen = "table_edit";
else if (mode.equals("add"))
    hlpScreen = "table_add";

%>

<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Dataset table"/>
        <jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
    </jsp:include>
    <c:set var="currentSection" value="tables" />
    <%@ include file="/pages/common/navigation.jsp" %>

    <div id="workarea">

        <%
        boolean subscribe = false;
        if (mode.equals("view") && user!=null && dsTable!=null &&
                dsTable.getIdentifier()!=null && dataset!=null && dataset.getIdentifier()!=null && !dataset.isWorkingCopy()){
            subscribe = true;
        }

        // main table head

        String pageHeadingVerb = "View";
        if (mode.equals("add"))
            pageHeadingVerb = "Add";
        else if (mode.equals("edit"))
            pageHeadingVerb = "Edit";
        %>
        <h1><%=pageHeadingVerb%> table <%if (mode.equals("add")){ %>to <a href="<%=request.getContextPath()%>/datasets/<%=dsID%>"><%=Util.processForDisplay(dsName)%></a> dataset<%}%></h1>

        <!-- quick links -->
        <%
            if (mode.equals("view")) {
                Vector quicklinks = new Vector();

                if (elems != null && elems.size() > 0) {
                    quicklinks.add("Elements | elements");
                }
                if (complexAttrs != null && complexAttrs.size() > 0) {
                    quicklinks.add("Complex attributes | cattrs");
                }
                request.setAttribute("quicklinks", quicklinks);
        %>
                <jsp:include page="quicklinks.jsp" flush="true" />
        <%
            }
            if ((mode.equals("view") && editDstPrm==true) || subscribe) {
            %>
            <div id="drop-operations">
                <ul>
                    <%
                    if (mode.equals("view") && editDstPrm==true) {
                    %>
                    <li class="edit"><a href="<%=request.getContextPath()%>/tables/<%=tableID%>/edit/?ds_id=<%=dsID%>&amp;ds_name=<%=dsName%>">Edit metadata</a></li>
                    <%
                    // elements link
                    String elemLink = request.getContextPath() + "/tblelems.jsp?table_id=" + tableID + "&amp;ds_id=" + dsID + "&amp;ds_name=" + dsName + "&amp;ds_idf=" + dsIdf;
                    %>
                    <li class="edit"><a href="<%=request.getContextPath()%>/complex_attrs.jsp?parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=Util.processForDisplay(dsTable.getShortName())%>&amp;dataset_id=<%=dsID%>">Edit complex attributes</a></li>
                    <li class="manage"><a href="<%=elemLink%>">Manage elements</a></li>
                    <li class="delete"><a href="javascript:submitForm('delete')">Delete</a></li>
                    <%
                    }
                    if (subscribe) {%>
                           <li class="subscribe"><a href="<%=request.getContextPath()%>/tables/<%=tableID%>/subscribe">Subscribe</a></li><%
                    }
                    // display the link about cache
                    boolean dispCache = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u");
                    if (editDstPrm || dispCache) {%>
                        <li class="doc"><a rel="nofollow" href="<%=request.getContextPath()%>/cache?objectId=<%=tableID%>&amp;objectTypeKey=tbl">Open cache</a></li>
                    <%}%>
                </ul>
            </div>
            <%
            }
            %>

            <%
            if (feedbackValue != null) {%>
                <div class="system-msg"><strong><%= feedbackValue %></strong></div><%
            }
            %>

        <form id="form1" method="post" action="<%=request.getContextPath()%>/tables" style="clear:both">

            <!--=======================-->
            <!-- main table inside div -->
            <!--=======================-->

                            <!-- schema, MS Excel template, XForm, XmlInst, etc -->
                            <%
                            if (mode.equals("view")) {

                                boolean dispAll = editDstPrm;
                                boolean dispXLS = dataset!=null && dataset.displayCreateLink("XLS");
                                boolean dispODS = dataset!=null && dataset.displayCreateLink("ODS");
                                boolean dispXmlSchema = dataset!=null && dataset.displayCreateLink("XMLSCHEMA");
                                boolean dispXmlInstance = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xmli");

                                if (dispAll || dispXLS || dispXmlSchema || dispXmlInstance || dispODS){
                                    %>
                                    <script type="text/javascript">
                                        $(function() {
                                            applyExportOptionsToggle();
                                        });
                                    </script>
                                    <div id="createbox">
                                        <ul>
                                            <%
                                            // XML Schema link
                                            if (dispAll || dispXmlSchema){ %>
                                                <li>
                                                    <a rel="nofollow" href="<%=request.getContextPath()%>/GetSchema?id=TBL<%=tableID%>" class="xsd">
                                                        Create an XML Schema for this table
                                                    </a>
                                                </li><%
                                            }

                                            // XML Instance link
                                            if (dispAll || dispXmlInstance){ %>
                                                <li>
                                                    <a rel="nofollow" href="<%=request.getContextPath()%>/GetXmlInstance?id=<%=tableID%>&amp;type=tbl" class="xml">
                                                        Create an instance XML for this table
                                                    </a>
                                                </li><%
                                            }

                                            // MS Excel link
                                            if (dispAll || dispXLS){
                                            %>
                                                <li>
                                                    <a rel="nofollow" href="<%=request.getContextPath()%>/GetXls?obj_type=tbl&amp;obj_id=<%=tableID%>" class="excel">
                                                        Create an MS Excel template for this table
                                                    </a>
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=table&amp;area=excel"></a>
                                                </li>
                                            <% }
                                            if ((dispAll || dispXLS) && user != null) { %>
                                                <li>
                                                    <a rel="nofollow" href="<%=request.getContextPath()%>/GetXls?obj_type=tbl&amp;obj_act=dd&amp;obj_id=<%=tableID%>" class="excel">
                                                        Create an MS Excel template for this table with drop-down boxes (BETA)
                                                    </a>
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=table&amp;area=excel_dropdown"></a>
                                                </li>
                                                <%
                                            }

                                            // OpenDocument spreadsheet template link
                                            if (dispAll || dispODS){ %>
                                                <li>
                                                    <a rel="nofollow" href="<%=request.getContextPath()%>/GetOds?type=tbl&amp;id=<%=tableID%>" class="open-doc">
                                                        Create an OpenDocument spreadsheet template for this table
                                                    </a>
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=table&amp;area=ods"></a>
                                                </li><%
                                            }

                                            // codelist
                                            if (dispAll || dispXmlSchema){ %>
                                                <li>
                                                    <stripes:link rel="nofollow" beanclass="eionet.web.action.CodelistDownloadActionBean" class="csv">
                                                        <stripes:param name="ownerType" value="tables"/>
                                                        <stripes:param name="ownerId" value="<%=dsTable.getID()%>"/>
                                                        <stripes:param name="format" value="csv"/>
                                                        Get the comma-separated codelists of this table
                                                    </stripes:link>
                                                </li>
                                                <li>
                                                    <stripes:link rel="nofollow" beanclass="eionet.web.action.CodelistDownloadActionBean" class="csv">
                                                        <stripes:param name="ownerType" value="tables"/>
                                                        <stripes:param name="ownerId" value="<%=dsTable.getID()%>"/>
                                                        <stripes:param name="format" value="xml"/>
                                                        Get the codelists of this table in XML format
                                                    </stripes:link>
                                                </li><%
                                            }

                                            // TESTING the link for creating dBase II format
                                            if (user!=null){
                                                String userName = user.getUserName();
                                                if (userName.equals("roug") || userName.equals("heinlja") || userName.equals("cryan")) {
                                                    %>
                                                    <li>
                                                        <a rel="nofollow" href="<%=request.getContextPath()%>/GetDbf/<%=dsTable.getID()%>" class="dbf">
                                                            Create dBaseII
                                                        </a>
                                                    </li>
                                                    <%
                                                }
                                            }
                                        %>
                                        </ul>
                                    </div>
                                    <%
                                }
                            }
                            %>

                            <!-- XML Conv data and link -->
                            <%
                            String schemaUrl = Props.getRequiredProperty(PropsIF.DD_URL) + "/GetSchema?id=TBL" + tableID;
                            SchemaConversionsData xmlConvData = searchEngine.getXmlConvData(schemaUrl);
                            if (xmlConvData != null){ %>
                                <div class="system-msg">
                                    <strong>
                                    There are <%=xmlConvData.getNumberOfQAScripts()%> QA scripts and <%=xmlConvData.getNumberOfConversions()%> conversion scripts registered for this table.
                                    <% if (xmlConvData.getNumberOfQAScripts() > 0 || xmlConvData.getNumberOfConversions() > 0) {%>
                                    <br />
                                    <a href="<%=xmlConvData.getXmlConvUrl()%>?schema=<%=schemaUrl%>">Link to the schema page on XMLCONV</a>
                                    <%}%>
                                    </strong>
                                </div><%
                            }
                            %>

                            <!-- start dotted -->
                            <div id="outerframe">

                                    <!-- attributes -->

                                    <%
                                    int displayed = 1;
                                    String isOdd = Util.isOdd(displayed);
                                    %>

                                    <table class="datatable results" width="100%">
                                        <!-- static attributes -->

                                        <!-- Identifier -->
                                        <tr class="<%=isOdd%>">
                                            <th scope="row" class="scope-row simple_attr_title">
                                                Identifier
                                                <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=identifier"></a>
                                            </th>
                                            <%
                                            if (!mode.equals("view")){%>
                                                <td class="simple_attr_help">
                                                    <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                </td><%
                                            }
                                            %>
                                            <td class="simple_attr_value">
                                                <%
                                                if(!mode.equals("add")){ %>
                                                    <b><%=Util.processForDisplay(dsTable.getIdentifier())%></b>
                                                    <input type="hidden" name="idfier" value="<%=dsTable.getIdentifier()%>"/><%
                                                }
                                                else{ %>
                                                    <input type="text" class="smalltext" size="30" name="idfier"/><%
                                                }
                                                %>
                                            </td>
                                            <%isOdd = Util.isOdd(++displayed);%>
                                        </tr>

                                        <!-- short name -->
                                        <tr id="short_name_row" class="<%=isOdd%>">
                                            <th scope="row" class="scope-row short_name">
                                                Short name
                                                <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=short_name"></a>
                                            </th>
                                            <%
                                            if (!mode.equals("view")){
                                                %>
                                                <td class="short_name simple_attr_help">
                                                    <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                </td><%
                                            }
                                            %>
                                            <td class="short_name_value">
                                                <%
                                                if (mode.equals("view")){ %>
                                                    <%=Util.processForDisplay(dsTable.getShortName())%>
                                                    <input type="hidden" name="short_name" value="<%=Util.processForDisplay(dsTable.getShortName(),true)%>"/><%
                                                }
                                                else if (mode.equals("add")){%>
                                                    <input class="smalltext" type="text" size="30" name="short_name"/><%
                                                }
                                                else{ %>
                                                    <input class="smalltext" type="text" size="30" name="short_name" value="<%=Util.processForDisplay(dsTable.getShortName())%>"/><%
                                                }
                                                %>
                                            </td>
                                            <%isOdd = Util.isOdd(++displayed);%>
                                        </tr>

                                        <!-- dataset -->
                                        <tr class="<%=isOdd%>">
                                            <th scope="row" class="scope-row simple_attr_title">
                                                Dataset
                                                <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=table&amp;area=dataset"></a>
                                            </th>
                                            <%
                                            if (!mode.equals("view")){%>
                                                <td class="simple_attr_help">
                                                    <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                </td><%
                                            }
                                            %>
                                            <td class="simple_attr_value">
                                                <a href="<%=request.getContextPath()%>/datasets/<%=dsID%>">
                                                    <b><%=Util.processForDisplay(dsName)%></b>
                                                </a>
                                                <%
                                                if (mode.equals("view") && dataset.isWorkingCopy()){ %>
                                                    <span class="caution">(Working copy)</span><%
                                                }
                                                %>
                                            </td>
                                            <%isOdd = Util.isOdd(++displayed);%>
                                        </tr>

                                        <!-- Reference URL -->
                                        <%
                                        String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
                                        if (mode.equals("view") && jspUrlPrefix!=null) {
                                            String refUrl = dsTable.getReferenceURL();
                                            %>
                                            <tr class="<%=isOdd%>">
                                                <th scope="row" class="scope-row simple_attr_title">
                                                    Reference URL
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=refurl"></a>
                                                </th>
                                                <td class="simple_attr_value">
                                                    <small><a href="<%=refUrl%>"><%=refUrl%></a></small>
                                                </td>
                                                <%isOdd = Util.isOdd(++displayed);%>
                                            </tr><%
                                        }
                                        %>

                                        <!-- dynamic attributes -->
                                        <%
                                        String attrID = null;
                                        String attrValue = null;
                                        DElemAttribute attribute = null;
                                        boolean imagesQuicklinkSet = false;

                                        for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
                                            attribute = (DElemAttribute)mAttributes.get(i);
                                            String dispType = attribute.getDisplayType();
                                            if (dispType == null) continue;

                                            String attrOblig = attribute.getObligation();
                                            String obligImg  = "optional.gif";
                                            if (attrOblig.equalsIgnoreCase("M"))
                                                obligImg = "mandatory.gif";
                                            else if (attrOblig.equalsIgnoreCase("C"))
                                                obligImg = "conditional.gif";

                                            if (!attribute.displayFor("TBL")) continue;

                                            attrID = attribute.getID();
                                            attrValue = getValue(attrID, mode, attributes);

                                            String width  = attribute.getDisplayWidth();
                                            String height = attribute.getDisplayHeight();

                                            boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
                                            boolean inherit = attribute.getInheritable().equals("0") ? false:true;

                                            if (mode.equals("view") && (attrValue==null || attrValue.length()==0))
                                                continue;

                                            // if image attribute and no reason to display then skip
                                            if (dispType.equals("image")){
                                                if (mode.equals("add") || (mode.equals("edit") && user==null) || (mode.equals("view") && Util.isEmpty(attrValue)))
                                                    continue;
                                            }
                                            
                                            if (dispType.equals("vocabulary") && mode.equals("add")) {
                                                continue;
                                            }

                                            Vector multiValues=null;
                                            String inheritedValue=null;

                                            if (!mode.equals("view")){

                                                if (inherit) inheritedValue = getValue(attrID, 2, mode, attributes);

                                                if (mode.equals("edit")){
                                                    if (dispMultiple){
                                                        if (inherit){
                                                            multiValues = getValues(attrID, 1, mode, attributes); //original values only
                                                        } else {
                                                            multiValues = getValues(attrID, 0, mode, attributes);  //all values
                                                        }
                                                    } else {
                                                        if (inherit) attrValue = getValue(attrID, 1, mode, attributes);  //get original value
                                                    }
                                                }
                                            }

                                            %>

                                            <tr class="<%=isOdd%>">
                                                <th scope="row" class="scope-row simple_attr_title">
                                                    <%=Util.processForDisplay(attribute.getName())%>
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                                                </th>
                                                <%
                                                if (!mode.equals("view")){%>
                                                    <td class="simple_attr_help">
                                                        <img style="border:0" src="<%=request.getContextPath()%>/images/<%=Util.processForDisplay(obligImg)%>" width="16" height="16" alt=""/>
                                                    </td><%
                                                }
                                                %>

                                                <!-- dynamic attribute value display -->
                                                <td class="simple_attr_value"><%
                                                    // handle image attribute first
                                                    if (dispType.equals("image")){

                                                        if (!imagesQuicklinkSet){ %>
                                                            <a id="images"></a><%
                                                            imagesQuicklinkSet = true;
                                                        }

                                                        // thumbnail
                                                        if (mode.equals("view") && !Util.isEmpty(attrValue)){
                                                            %>
                                                            <div class="figure-plus-container">
                                                                <div class="figure-plus">
                                                                    <div class="figure-image">
                                                                        <a href="<%=request.getContextPath()%>/visuals/<%=Util.processForDisplay(attrValue)%>">
                                                                            <img src="<%=request.getContextPath()%>/visuals/<%=Util.processForDisplay(attrValue)%>" alt="Image file could not be found on the server" class="scaled poponmouseclick"/>
                                                                        </a>
                                                                    </div>
                                                                </div>
                                                            </div><%
                                                        }

                                                        // link to image edit page
                                                        if (mode.equals("edit") && user!=null){
                                                            String actionText = Util.isEmpty(attrValue) ? "add image" : "manage this image";
                                                            %>
                                                            <span class="barfont">
                                                                <a href="<%=request.getContextPath()%>/imgattr.jsp?obj_id=<%=tableID%>&amp;obj_type=T&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=Util.processForDisplay(dsTable.getShortName())%>&amp;attr_name=<%=Util.processForDisplay(attribute.getShortName())%>">Click to <%=Util.processForDisplay(actionText)%></a>
                                                            </span><%
                                                        }
                                                    }
                                                    // if view mode, display simple text
                                                    else if (mode.equals("view") && dispType.equals("vocabulary")) {
                                                        DataDictEntity ddEntity = new DataDictEntity(Integer.parseInt(tableID), DataDictEntity.Entity.T);
                                                        List<VocabularyConcept> vocabularyConcepts = searchEngine.getAttributeVocabularyConcepts(Integer.parseInt(attrID), ddEntity, attribute.getInheritable());
                                                        if (vocabularyConcepts != null) { %>
                                                            <ul class="stripedmenu">
                                                            <%
                                                                VocabularyFolder vf = null;
                                                                for (VocabularyConcept vocabularyConcept : vocabularyConcepts) {
                                                                    if (vf == null) {
                                                                        vf = searchEngine.getVocabulary(vocabularyConcept.getVocabularyId());
                                                                    }
                                                            %>
                                                                <li>
                                                                    <a href="<%=request.getContextPath()%>/vocabularyconcept/<%=Util.processForDisplay(vf.getFolderName())%>/<%=Util.processForDisplay(vf.getIdentifier())%>/<%=Util.processForDisplay(vocabularyConcept.getIdentifier())%>">
                                                                        <%=vocabularyConcept.getLabel()%>
                                                                    </a>
                                                                </li>
                                                            <%}%>
                                                            </ul>
                                                    <%
                                                        }
                                                    } else if (mode.equals("view")){ %>
                                                        <%=Util.processForDisplay(attrValue)%><%
                                                    }
                                                    // if non-view mode, display input
                                                    else{

                                                        // inherited value(s)
                                                        if (inherit && dispType.equals("vocabulary")){
                                                            DataDictEntity ddEntity = new DataDictEntity(Integer.parseInt(tableID), DataDictEntity.Entity.T);                                                            
                                                            List<VocabularyConcept> inheritedValues = searchEngine.getInheritedAttributeVocabularyConcepts(Integer.parseInt(attrID), ddEntity);
                                                            if(inheritedValues!=null && !inheritedValues.isEmpty()){
                                                            %>
                                                                <c:set var="inheritable" value="<%=attribute.getInheritable()%>" />
                                                                <c:choose>
                                                                    <c:when test="${inheritable eq '2'}">
                                                                        <c:out value="Overriding parent level value: "/>
                                                                    </c:when>
                                                                    <c:when test="${inheritable eq '1'}">
                                                                        <c:out value="Inherited from parent level: "/>
                                                                    </c:when>
                                                                </c:choose>
                                                                <ul class="stripedmenu">
                                                                    <c:forEach var="value" items="<%=inheritedValues%>" varStatus="count">
                                                                        <li><c:out value="${value.label}"/></li>
                                                                    </c:forEach>
                                                                </ul>
                                                                </br>
                                                            <%}
                                                        }
                                                        else if (inherit && inheritedValue!=null){
                                                            String sInhText = (((dispMultiple && multiValues!=null) ||
                                                                                (!dispMultiple && attrValue!=null)) &&
                                                                                attribute.getInheritable().equals("2")) ?
                                                                                "Overriding parent level value: " :
                                                                                "Inherited from parent level: ";

                                                            if (sInhText.startsWith("Inherited")){ %>
                                                                <%=sInhText%><%=Util.processForDisplay(inheritedValue)%><br/><%
                                                            }
                                                        }

                                                        // mutliple display
                                                        if (dispMultiple && !dispType.equals("image") && !dispType.equals("vocabulary")){

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
                                                            <input type="text" name="<%=textName%>" value="insert other value" style="font-size:0.9em;color:#76797C" onfocus="this.value=''"/>
                                                            <input type="button" value="-&gt;" style="font-size:0.8em;" onclick="addMultiSelectRow(document.forms['form1'].elements['<%=textName%>'].value, '<%=checkboxName%>','<%=divID%>')"/>
                                                            <div id="<%=divID%>" class="multiselect" style="height:<%=divHeight%>;width:25em;">
                                                                <%
                                                                HashSet displayedSet = new HashSet();
                                                                for (int k=0; displayValues!=null && k<displayValues.size(); k++){

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
                                                            </div><%
                                                        }
                                                        else if (dispMultiple && dispType.equals("vocabulary")) {
                                                            if (searchEngine.existsVocabularyBinding(Integer.parseInt(attrID))) { %>
                                                              <%DataDictEntity ddEntity = new DataDictEntity(Integer.parseInt(tableID), DataDictEntity.Entity.T);
                                                                List<VocabularyConcept> vocabularyConcepts = searchEngine.getAttributeVocabularyConcepts(Integer.parseInt(attrID), ddEntity, "0");
                                                                %>
                                                                <ul class="stripedmenu">
                                                                    <c:forEach var="vocabularyConcept" items="<%=vocabularyConcepts%>" varStatus="count">
                                                                        <li><c:out value="${vocabularyConcept.label}"/></li>
                                                                    </c:forEach>
                                                                </ul>
                                                                <a href="<%=request.getContextPath()%>/vocabularyvalues/attribute/<%=attrID%>/table/<%=dsTable.getID()%>">[Manage links to the vocabulary]</a>
                                                            <%} else { %>
                                                                [Manage links to the vocabulary]
                                                            <%}
                                                      }
                                                        // no multiple display
                                                        else{

                                                            if (dispType.equals("text")){
                                                                if (attrValue!=null){
                                                                    %>
                                                                    <input type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>" onchange="form_changed('form1')"/>
                                                                    <%
                                                                }
                                                                else{
                                                                    %>
                                                                    <input type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"/>
                                                                    <%
                                                                }
                                                            }
                                                            else if (dispType.equals("textarea")){
                                                                if (attrValue!=null){
                                                                    %>
                                                                    <textarea class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"><%=Util.processForDisplay(attrValue, true, true)%></textarea>
                                                                    <%
                                                                }
                                                                else{
                                                                    %>
                                                                    <textarea class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"></textarea>
                                                                    <%
                                                                }
                                                            }
                                                            else if (dispType.equals("select")){ %>
                                                                <select class="small" name="attr_<%=attrID%>" onchange="form_changed('form1')">
                                                                    <%
                                                                    Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
                                                                    if (fxValues==null || fxValues.size()==0){ %>
                                                                        <option selected value=""></option> <%
                                                                    }
                                                                    else{
                                                                        boolean selectedByValue = false;
                                                                        for (int g=0; g<fxValues.size(); g++){
                                                                            FixedValue fxValue = (FixedValue)fxValues.get(g);
                                                                            String isSelected = (fxValue.getDefault() && !selectedByValue) ? "selected" : "";
                                                                            if (attrValue!=null && attrValue.equals(fxValue.getValue())){
                                                                                isSelected = "selected";
                                                                                selectedByValue = true;
                                                                            }
                                                                            %>
                                                                            <option <%=isSelected%> value="<%=Util.processForDisplay(fxValue.getValue())%>"><%=Util.processForDisplay(fxValue.getValue())%></option> <%
                                                                        }
                                                                    }
                                                                    %>
                                                                </select>
                                                                    <a class="helpButton" href="<%=request.getContextPath()%>/fixedvalues/attr/<%=attrID + "/" + ("view".equals(mode) ? "view" : "edit")%>"></a>
                                                                <%
                                                            }else if (dispType.equals("vocabulary")){ 
                                                                        if (searchEngine.existsVocabularyBinding(Integer.parseInt(attrID))){%>
                                                                          <%DataDictEntity ddEntity = new DataDictEntity(Integer.parseInt(tableID), DataDictEntity.Entity.T);
                                                                            List<VocabularyConcept> vocabularyConcepts = searchEngine.getAttributeVocabularyConcepts(Integer.parseInt(attrID), ddEntity, "0");
                                                                          %>
                                                                            <ul class="stripedmenu">
                                                                                <c:forEach var="vocabularyConcept" items="<%=vocabularyConcepts%>" varStatus="count">
                                                                                    <li><c:out value="${vocabularyConcept.label}"/></li>
                                                                                </c:forEach>
                                                                            </ul>
                                                                            <a href="<%=request.getContextPath()%>/vocabularyvalues/attribute/<%=attrID%>/table/<%=dsTable.getID()%>">[Manage links to the vocabulary]</a>
                                                                      <%} else { %>
                                                                            [Manage links to the vocabulary]
                                                                        <%}
                                                            }else {%>
                                                                Unknown display type!<%
                                                            }
                                                        }

                                                    } // end display input
                                                    %>
                                                    <input type="hidden" name="oblig_<%=attrID%>" value="<%=Util.processForDisplay(attribute.getObligation(),true)%>"/>
                                                </td>
                                                <!-- end dynamic attribute value display -->

                                                <%isOdd = Util.isOdd(++displayed);%>
                                            </tr>
                                            <%
                                        }
                                        %>
                                        <%
                                        pageContext.setAttribute("dsTable", dsTable);
                                        pageContext.setAttribute("mode", mode);
                                        /* if (mode.equals("edit") || mode.equals("add")) {
                                            pageContext.setAttribute("rdfNamespaces", searchEngine.getRdfNamespaces());
                                        } */
                                        %>
                                        
                                        <!-- add, save, check-in, undo check-out buttons -->
                                            <%
                                            if (mode.equals("add") || mode.equals("edit")) {
                                                // add case
                                                if (mode.equals("add")) {
                                                    %>
                                                    <tr>
                                                        <th></th>
                                                        <td colspan="3">
                                                            <input type="submit" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>
                                                            <input type="submit" class="mediumbuttonb" value="Copy"
                                                                onclick="copyTbl('<%=request.getContextPath()%>')"
                                                                title="Copies table structure and attributes from existing dataset table"/>
                                                        </td>
                                                    </tr><%
                                                } else if (mode.equals("edit")) { // edit case
                                                %>
                                                    <tr>
                                                        <th></th>
                                                        <td colspan="3">
                                                            <input type="submit" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>
                                                            <input type="submit" class="mediumbuttonb" value="Save &amp; close" onclick="submitForm('editclose')"/>
                                                            <input type="submit" class="mediumbuttonb" value="Cancel" onclick="goTo('view', '<%=tableID%>')"/>
                                                        </td>
                                                    </tr>
                                            <%
                                                }
                                            }
                                            %>
                                    </table>
                                    <!-- end of attributes -->

                                    <!-- table elements -->
                                    <%
                                    if ((mode.equals("view") && elems!=null && elems.size()>0)) {


                                            String title = "Elements";

                                            boolean hasMarkedElems = false;
                                            boolean hasForeignKeys = false;
                                            boolean hasMultivalElms = false;
                                            boolean hasCommonElms = false;
                                            boolean hasMandatoryElms = false;
                                            boolean hasPrimaryKeys = false;
                                            %>

                                                <h2 id="elements"><%=Util.processForDisplay(title)%></h2>

                                                <%
                                                // elements table
                                                if (mode.equals("view") && elems!=null && elems.size()>0) {

                                                    // set colwidths
                                                    String widthShortName = "50%";
                                                    String widthDatatype  = "19%";
                                                    String widthElemtype  = "31%";

                                                    Hashtable types = new Hashtable();
                                                    types.put("CH1", "Fixed values");
                                                    types.put("CH2", "Quantitative");
                                                    types.put("CH3", "Vocabulary");
                                                    %>
                                                              <table class="datatable results">
                                                                    <col style="width:<%=widthShortName%>"/>
                                                                    <col style="width:<%=widthDatatype%>"/>
                                                                    <col style="width:<%=widthElemtype%>"/>
                                                                <thead>
                                                                    <tr>
                                                                        <th>Element name</th>
                                                                        <th>Datatype</th>
                                                                        <th>Element type</th>
                                                                    </tr>
                                                                </thead>
                                                                <%
                                                                // rows loop
                                                                for (int i=0; i<elems.size(); i++){

                                                                    DataElement elem = (DataElement)elems.get(i);

                                                                    boolean elmCommon = elem.getNamespace()==null || elem.getNamespace().getID()==null;
                                                                    String elemLink = request.getContextPath() + "/dataelements/" + elem.getID();
                                                                    String elemDefinition = elem.getAttributeValueByShortName("Definition");
                                                                    String linkTitle = elemDefinition==null ? "" : elemDefinition;
                                                                    String elemType = (String)types.get(elem.getType());
                                                                    String datatype = getAttributeValue(elem, "Datatype", mAttributes);
                                                                    if (datatype == null) datatype="";
                                                                    String max_size = getAttributeValue(elem, "MaxSize", mAttributes);
                                                                    if (max_size == null) max_size="";

                                                                    // see if the element is part of any foreign key relations
                                                                    Vector _fks = searchEngine.getFKRelationsElm(elem.getID(), dataset.getID());
                                                                    boolean fks = (_fks!=null && _fks.size()>0) ? true : false;

                                                                    // flag indicating if element can have multiple values
                                                                    boolean isMulitvalElem = elem.getValueDelimiter()!=null;
                                                                    %>
                                                                    <tr>
                                                                        <!-- short name -->
                                                                        <td>
                                                                            <%
                                                                            if (elem.isMandatoryFlag()){
                                                                                %>
                                                                                <span style="font:bold;font-size:1.2em">*</span>
                                                                                <%
                                                                                hasMandatoryElms = true;
                                                                            }
                                                                            %>
                                                                            <a href="<%=elemLink%>" title="<%=Util.processForDisplay(linkTitle, true, true)%>">
                                                                                <%=Util.processForDisplay(elem.getAttributeValueByName("Name") + " (" + elem.getIdentifier() + ")")%>
                                                                            </a>

                                                                            <%
                                                                            if (elmCommon){ %>
                                                                                <sup style="color:#858585;font-weight:bold;">C</sup><%
                                                                                hasCommonElms = true;
                                                                            }

                                                                            // FK indicator
                                                                            if (fks){ %>
                                                                                &nbsp;
                                                                                <a href="<%=request.getContextPath()%>/foreign_keys.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=Util.processForDisplay(elem.getShortName())%>&amp;ds_id=<%=dsID%>&amp;table_id=<%=tableID%>">
                                                                                    <span style="font: bold italic">(FK)</span>
                                                                                </a><%
                                                                                hasForeignKeys = true;
                                                                            }

                                                                            if (elem.isPrimaryKey()){ %>
                                                                                &nbsp;<span style="font: bold italic" title="Element participates in primary key">(PK)</span><%
                                                                                hasPrimaryKeys = true;
                                                                            }
                                                                            %>
                                                                        </td>
                                                                        <!-- datatype -->
                                                                        <td>
                                                                            <%=Util.processForDisplay(datatype)%>
                                                                        </td>
                                                                        <!-- element type -->
                                                                        <td>
                                                                            <%
                                                                            if (elem.getType().equals("CH1")){ %>
                                                                                <a href="<%=request.getContextPath()%>/fixedvalues/elem/<%=elem.getID()%>">
                                                                                    <%=Util.processForDisplay(elemType)%>
                                                                                </a> <%
                                                                            }
                                                                            else if (elem.getType().equals("CH3")){
                                                                                String vocabularyId = elem.getVocabularyId();
                                                                                VocabularyFolder vocabulary = null;
                                                                                if (vocabularyId != null) {
                                                                                    vocabulary  = searchEngine.getVocabulary(Integer.valueOf(vocabularyId));
                                                                                    %>
                                                                                    <a href="<%=request.getContextPath()%>/vocabulary/<%=vocabulary.getFolderName()%>/<%=vocabulary.getIdentifier()%>/view">
                                                                                        <%=Util.processForDisplay(elemType)%>
                                                                                    </a> <% if (!elem.isAllConceptsValid()) {%><span title="Only concepts created and accepted before releasing the element are valid" class="checkedout"><strong>*</strong></span><%}
                                                                                } else {
                                                                                    %><span><%=Util.processForDisplay(elemType)%></span><%
                                                                                }
                                                                            } else{ %>
                                                                                <%=Util.processForDisplay(elemType)%><%
                                                                            }

                                                                            if (isMulitvalElem){ %>
                                                                                <sup style="color:#858585;font-weight:bold;">+</sup><%
                                                                                hasMultivalElms = true;
                                                                            }
                                                                            %>
                                                                        </td>
                                                                    </tr><%
                                                                }
                                                                %>
                                                            </table>

                                                      <%
                                                      if (hasMandatoryElms){
                                                          %>
                                                          <div class="barfont">
                                                            (an asterisk in front of element name indicates that the element is mandatory in this table)
                                                        </div><%
                                                      }
                                                      if (user!=null && elems!=null && elems.size()>0 && hasMarkedElems){%>
                                                        <div class="barfont">
                                                                (a red wildcard stands for checked-out element)
                                                        </div><%
                                                    }
                                                    if (user!=null && elems!=null && elems.size()>0 && hasForeignKeys){%>
                                                        <div>
                                                                (the <em style="font-weight:bold;text-decoration:underline">(FK)</em> link indicates the element participating in a foreign key relation)
                                                        </div><%
                                                    }
                                                    if (user!=null && hasPrimaryKeys){%>
                                                        <div>
                                                                (<em style="font-weight:bold;text-decoration:underline">(PK)</em> marks elements participating in the table's primary key)
                                                        </div><%
                                                    }
                                                    if (elems!=null && elems.size()>0 && hasCommonElms){%>
                                                        <div class="barfont">
                                                            (the <sup style="color:#858585;">C</sup> sign marks a common element)
                                                        </div><%
                                                    }
                                                    if (elems!=null && elems.size()>0 && hasMultivalElms){%>
                                                        <div class="barfont">
                                                            (the <sup style="color:#858585;">+</sup> sign right after the "Fixed values" link marks an element that can have multiple values)
                                                        </div><%
                                                    }
                                                }
                                    }
                                    %>

                                    <!-- complex attributes -->
                                    <% if (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0) { %>
                                        <h2 id="cattrs">Complex attributes</h2>
                                            <table class="datatable results" id="dataset-attributes">
                                                <col style="width:30%"/>
                                                <col style="width:70%"/>

                                                <%
                                                displayed = 1;
                                                isOdd = Util.isOdd(displayed);
                                                for (int i=0; i<complexAttrs.size(); i++){
                                                    DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
                                                    attrID = attr.getID();
                                                    String attrName = attr.getName();
                                                    Vector attrFields = searchEngine.getAttrFields(attrID, DElemAttribute.FIELD_PRIORITY_HIGH);
                                                    %>

                                                    <tr class="<%=isOdd%>">
                                                        <td>
                                                            <a href="<%=request.getContextPath()%>/complex_attr.jsp?attr_id=<%=attrID%>&amp;parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=Util.processForDisplay(dsTable.getShortName())%>&amp;dataset_id=<%=dsID%>" title="Click here to view all the fields">
                                                                <%=Util.processForDisplay(attrName)%>
                                                                <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX"></a>
                                                            </a>
                                                        </td>
                                                        <td>
                                                            <%
                                                            StringBuffer rowValue=null;
                                                            Vector rows = attr.getRows();
                                                            for (int j=0; rows!=null && j<rows.size(); j++){

                                                                if (j>0){%>---<br/><%}

                                                                Hashtable rowHash = (Hashtable)rows.get(j);
                                                                rowValue = new StringBuffer();

                                                                for (int t=0; t<attrFields.size(); t++){
                                                                    Hashtable hash = (Hashtable)attrFields.get(t);
                                                                    String fieldID = (String)hash.get("id");
                                                                    String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
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

                <!-- end dotted -->
            </div>

            <div style="display:none">
                <%
                // hidden inputs
                ////////////////

                // dataset name
                if (dsName!=null && dsName.length()>0){ %>
                    <input type="hidden" name="ds_name" value="<%=Util.processForDisplay(dsName,true)%>"/> <%
                }
                else{ %>
                    <input type="hidden" name="ds_name"/><%
                }
                // table id number
                if (!mode.equals("add")){ %>
                    <input type="hidden" name="table_id" value="<%=tableID%>"/>
                    <input type="hidden" name="del_id" value="<%=tableID%>"/><%
                }
                // corresponding namespace id
                if (!mode.equals("add") && dsTable.getNamespace()!=null){ %>
                    <input type="hidden" name="corresp_ns" value="<%=dsTable.getNamespace()%>"/><%
                }
                // parent namespace id
                if (dataset.getNamespaceID()!=null){ %>
                    <input type="hidden" name="parent_ns" value="<%=dataset.getNamespaceID()%>"/><%
                }
                // submitter url, might be used by POST handler who might want to send back to POST submitter
                String submitterUrl = Util.getServletPathWithQueryString(request);
                if (submitterUrl!=null){
                    submitterUrl = Util.processForDisplay(submitterUrl);
                    %>
                    <input type="hidden" name="submitter_url" value="<%=submitterUrl%>"/><%
                }
                %>
                <input type="hidden" name="mode" value="<%=mode%>"/>
                <input type="hidden" name="ds_id" value="<%=dsID%>"/>
                <input type="hidden" name="copy_tbl_id" value=""/>
                <input type="hidden" name="changed" value="0"/>
                <input type="hidden" name="saveclose" value="false"/>
            </div>

        </form>
    </div> <!-- end workarea -->
    </div> <!-- container -->
    <%@ include file="footer.jsp" %>
</body>
</html>

<%
// end the whole page try block
}
catch (Exception e){
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
