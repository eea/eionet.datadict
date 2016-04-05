<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="history.jsp" %>

<%!
    // servlet-scope helper functions
    //////////////////////////////////

    /**
     *
     */
    private String getAttributeIdByName(String name, Vector mAttributes){


            for (int i=0; i<mAttributes.size(); i++){
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

    /**
     *
     */
    private boolean isIn(Vector elems, String id){

        for (int i=0; id!=null && i<elems.size(); i++){

            Object o = elems.get(i);
            Class oClass = o.getClass();
            if (oClass.getName().endsWith("Hashtable")) continue;

            DataElement elem = (DataElement)o;
            if (elem.getID().equalsIgnoreCase(id))
                return true;
        }

        return false;
    }
%>

<%
    // implementation of the servlet's service method
    //////////////////////////////////////////////////

    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
    ServletContext ctx = getServletContext();
    DDUser user = SecurityUtil.getUser(request);

    // POST request not allowed for anybody who hasn't logged in
    if (request.getMethod().equals("POST") && user==null){
        request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    // get values of several request parameters:
    // - table's id number
    // - dataset's id number
    String tableID = request.getParameter("table_id");
    if (tableID == null || tableID.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: table_id");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
    String dsID = request.getParameter("ds_id");
    if (dsID == null || dsID.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
    String dsName = request.getParameter("ds_name");

    //// handle the POST request //////////////////////
    //////////////////////////////////////////////////
    if (request.getMethod().equals("POST")){

        Connection userConn = null;
        DsTableHandler tblHandler = null;
        DataElementHandler elmHandler = null;
        String link_elm = request.getParameter("link_elm");
        String rplc_elm = request.getParameter("rplc_elm");
        try{
            try{
                userConn = user.getConnection();
                if ((link_elm!=null && link_elm.length()>0) || (rplc_elm!=null && rplc_elm.length()>0)){
                    tblHandler = new DsTableHandler(userConn, request, ctx);
                    tblHandler.setUser(user);
                    tblHandler.execute();
                }
                else{
                    elmHandler = new DataElementHandler(userConn, request, ctx);
                    elmHandler.setUser(user);
                    elmHandler.execute();
                }
            }
            catch (Exception e){
                if (elmHandler!=null)
                    elmHandler.cleanup();

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
        finally{
            try { if (userConn!=null) userConn.close();
            } catch (SQLException e) {}
        }

        // disptach the POST request
        if (Util.isEmpty(link_elm) && Util.isEmpty(rplc_elm)){

            String redirUrl = "";
            String mode = request.getParameter("mode");
            if (mode.equals("add") || mode.equals("copy"))
                redirUrl = request.getContextPath() + "/dataelements/" + elmHandler.getLastInsertID();
            else{
                redirUrl = "tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID;
                if (dsName!=null && dsName.length()>0)
                    redirUrl = redirUrl + "&ds_name=" + dsName;
            }

            response.sendRedirect(redirUrl);
            return;
        }
    }
    //// end of handle the POST request //////////////////////

    Connection conn = null;

    Vector elems = null;
    Vector mAttributes = null;

    // the whole page's try block
    try {
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
        searchEngine.setUser(user);

        // get the table object
        DsTable dsTable = searchEngine.getDatasetTable(tableID);
        if (dsTable == null){
            request.setAttribute("DD_ERR_MSG", "No table found with this id number: " + tableID);
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
        String tableName = dsTable.getShortName();

        // overwrite dataset id parameter with the one from table object
        dsID = dsTable.getDatasetID();
        if (dsID==null || dsID.length()==0){
            request.setAttribute("DD_ERR_MSG", "Missing dataset id number in the table object");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        // get the dataset object (having reached this point, dataset id number is not null)
        String dstWorkingUser = null;
        Dataset dataset = searchEngine.getDataset(dsID);
        if (dataset==null){
            request.setAttribute("DD_ERR_MSG", "No dataset found with this id number: " + dsID);
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
        dsName = dataset.getShortName();
        dstWorkingUser = dataset.getWorkingUser();
        boolean editDstPrm = user!=null && dataset.isWorkingCopy() && dstWorkingUser!=null && dstWorkingUser.equals(user.getUserName());

        // get the table's elements and metadata of attributes
        elems = searchEngine.getDataElements(null, null, null, null, tableID);
        mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);

        int colCount = 4;
%>

<%
// start HTML //////////////////////////////////////////////////////////////
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Meta</title>

<script src="dynamic_table.js" type="text/javascript"></script>
<script src="modal_dialog.js" type="text/javascript"></script>

<script type="text/javascript">
// <![CDATA[
        function submitForm(mode){

            if (mode=="delete"){
                var b = confirm("This will remove the selected elements from this table. Click OK, if you want to continue. Otherwise click Cancel.");
                if (b==false)
                    return;
            }

            if (mode=="add"  && document.forms["form1"].elements["idfier"].value==""){
                alert("Identifier cannot be empty!");
                return;
            }

            if (mode=="add" && hasWhiteSpace("idfier")){
                alert("Identifier cannot contain any white space!");
                return;
            }

            if (mode=="add" && !validForXMLTag(document.forms["form1"].elements["idfier"].value)){
                alert("Identifier not valid for usage as an XML tag! " +
                          "In the first character only underscore or latin characters are allowed! " +
                          "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
                return;
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


        function saveChanges(){
            if (ensureLegalPrimaryKeys()==false){
                return false;
            }
            tbl_obj.insertNumbers("pos_");
            submitForm("edit_tblelems");
        }

        function ensureLegalPrimaryKeys(){

            var elems = document.forms["form1"].elements;
            if (elems == null) return false;
            for (var i=0; i<elems.length; i++){
                var elem = elems[i];
                if (elem.name.length >= 6 && elem.name.substr(0,6)=="delim_"){
                    if (elem.type=="select-one" && elem.selectedIndex>=0){
                        if (elem.options[elem.selectedIndex].value.length > 0){
                            var elem_id = elem.name.substr(6);
                            var primKeyElem = document.forms["form1"].elements["primkey_" + elem_id];
                            if (primKeyElem!=null && primKeyElem.type=="checkbox" && primKeyElem.checked){
                                alert("Elements marked as primary keys must not have a value delimiter specified! i.e. they can not have multiple values.");
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }

        function clickLink(sUrl){
            if (getChanged()==1){
                if(!confirm("This link leads you to the next page, but you have changed the order of elements.\n Are you sure you want to loose the changes?"))
                    return;
            }
            window.location=sUrl;
        }
        function start() {
            tbl_obj=new dynamic_table("tbl"); //create dynamic_table object
        }

        //call to dynamic table methods. Originated from buttons or click on tr.
        function sel_row(o){
            tbl_obj.selectRow(o);
        }
        function moveRowUp(){
            tbl_obj.moveup();
            setChanged();
        }
        function moveRowDown(){
            tbl_obj.movedown();
            setChanged();
        }
        function moveFirst(){
            tbl_obj.movefirst();
            setChanged();
        }
        function moveLast(){
            tbl_obj.movelast();
            setChanged();
        }
        function setChanged(){
            document.forms["form1"].elements["changed"].value = 1;
        }
        function getChanged(){
            return document.forms["form1"].elements["changed"].value;
        }

        var pickMode = "";
        function copyElem(){

            if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)){
                alert("Identifier not valid for usage as an XML tag! " +
                          "In the first character only underscore or latin characters are allowed! " +
                          "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
                return;
            }

            if (hasWhiteSpace("idfier")){
                alert("Identifier cannot contain any white space!");
                return;
            }

            pickMode = "copy";
            var url="search.jsp?ctx=popup&noncommon";
            wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
            if (window.focus){
                wAdd.focus();
            }
        }

        function linkElem(){
            pickMode = "link";
            var url="search.jsp?ctx=popup&common=&link=&exclude=" + document.forms["form1"].str_elem_ids.value;
            wLink = window.open(url,"Search","height=800,width=1200,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=no,location=no");
            if (window.focus){
                wLink.focus();
            }
        }

        var rplcId = "";
        var rplcPos = "";
        function getNewerReleases(elmId, elmIdf, position){
            pickMode = "rplc";
            rplcId = elmId;
            rplcPos = position;
            var url="common_elms.jsp?ctx=popup&search_precision=exact&idfier=" + elmIdf + "&newerThan=" + elmId;
            wLink = window.open(url,"Search","height=500,width=700,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=no,location=no");
            if (window.focus){
                wLink.focus();
            }
        }

        function pickElem(id){
            if (pickMode=="copy"){
                document.forms["form1"].copy_elem_id.value=id;
                document.forms["form1"].mode.value="copy";
                submitForm('copy');
            }
            else if (pickMode=="link"){
                document.forms["common_elm_link_form"].link_elm.value=id;
                document.forms["common_elm_link_form"].submit();
            }
            else if (pickMode=="rplc"){
                document.forms["common_elm_rplc_form"].rplc_id.value=rplcId;
                document.forms["common_elm_rplc_form"].rplc_pos.value=rplcPos;
                document.forms["common_elm_rplc_form"].rplc_elm.value=id;
                document.forms["common_elm_rplc_form"].submit();
            }
            else
                alert("Unknown pick mode: " + pickMode);

            return true;
        }

        function goToAddForm(){
            var url = "<%=request.getContextPath()%>/dataelements/add/?table_id=<%=tableID%>&ds_id=<%=dsID%>";
            document.location.assign(url);
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

<body onload="start()">
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Table elements"/>
        <jsp:param name="helpscreen" value="table_elements"/>
    </jsp:include>
    <c:set var="currentSection" value="dataElements" />
    <%@ include file="/pages/common/navigation.jsp" %>
<div id="workarea">

<%
String messages = RequestMessages.get(request, RequestMessages.system, RequestMessages.htmlLineBreak);
if (messages.trim().length()>0){
    %>
    <div class="system-msg"><strong><%=messages%></strong></div><%
}
%>

<form id="form1" method="post" action="tblelems.jsp">

    <!-- page title & the add new part -->
    <h1>
        Elements in
        <em>
            <a href="<%=request.getContextPath()%>/tables/<%=tableID%>">
                <%=Util.processForDisplay(tableName)%>
            </a>
        </em>
        table,
        <em>
            <a href="<%=request.getContextPath()%>/datasets/<%=dsID%>">
                <%=Util.processForDisplay(dsName)%>
            </a>
        </em>
        dataset.
    </h1>

    <%
    if (editDstPrm){
        %>
        <p style="margin-top:20px;">
            <input type="button" class="smallbutton" value="Add" onclick="goToAddForm()" title="Create a new element in this table."/>
            <input type="button" class="smallbutton" value="Link" onclick="linkElem()" title="Link a common element into this table."/>
        </p><%
    }
    %>

    <!-- following is a table consisting of two columns -->
    <!-- the first column contains the table of elements,      -->
    <!-- the second one contains the ordering buttons          -->

    <table width="100%" cellspacing="0"  style="border:0">
        <tr>

            <!-- table of elements -->

            <td style="width:90%">
                <table width="100%" cellspacing="0" id="tbl" class="datatable">

                    <thead>

                    <!-- Delete & Save buttons -->

                    <%
                    if (editDstPrm && elems!=null && elems.size()>0){ %>
                        <tr>
                            <td colspan="<%=String.valueOf(colCount)%>">
                                <input type="button" value="Remove selected" class="smallbutton" onclick="submitForm('delete')"/>
                                <input type="button" value="Save form" class="smallbutton" onclick="saveChanges()" title="Save changes you made on this form (e.g. changes to elements order, value delimiters, etc)"/>
                            </td>
                        </tr><%
                    }
                    %>

                    <!-- column headers -->

                    <tr>
                        <th align="right" style="padding-right:10px">&nbsp;</th> <!-- checkboxes column -->

                        <th scope="col" class="scope-col">Short name</th>
                        <th scope="col" class="scope-col">Datatype</th>
                        <th scope="col" class="scope-col">Elem. type</th>
                        <th scope="col" class="scope-col">Mandatory</th>

                        <%
                        boolean hasQuantitativeElements = false;
                        boolean hasFixedValueElements = false;
                        for (int i=0; elems!=null && i<elems.size(); i++){
                            String type = ((DataElement)elems.get(i)).getType();
                            if (type!=null && type.equals("CH1")){
                                hasFixedValueElements = true;
                            }
                            else if (type!=null && type.equals("CH2")){
                                hasQuantitativeElements = true;
                            }
                        }
                        if (hasFixedValueElements){
                            %>
                            <th scope="col" class="scope-col">Value delimiter</th><%
                        }
                        if (hasQuantitativeElements){
                            %>
                            <th scope="col" class="scope-col">Prim. key</th><%
                        }
                        %>
                    </tr>
                    </thead>

                    <tbody id="tbl_body">

                    <%

                    Hashtable types = new Hashtable();
                    types.put("CH1", "Fixed values - code list");
                    types.put("CH2", "Quantitative");
                    types.put("CH3", "Fixed values - vocabulary");

                    int maxPos = 0;

                    // the elements display loop
                    boolean hasMarkedElems = false;
                    boolean hasForeignKeys = false;
                    boolean hasPrimaryKeys = false;
                    boolean hasCommonElms = false;
                    StringBuffer strElemIDs = new StringBuffer();
                    for (int i=0; elems!=null && i<elems.size(); i++){

                        DataElement elem = (DataElement)elems.get(i);

                        if (strElemIDs.length()>0)
                            strElemIDs.append(",");
                        strElemIDs.append(elem.getID());

                        String delem_name=elem.getShortName();
                        String elemType = (String)types.get(elem.getType());
                        String datatype = getAttributeValue(elem, "Datatype", mAttributes);
                        if (datatype == null) datatype="";
                        String max_size = getAttributeValue(elem, "MaxSize", mAttributes);
                        if (max_size == null) max_size="";
                        int posInTable = Integer.parseInt(elem.getPositionInTable());
                        if (posInTable > maxPos) maxPos = posInTable;
                        boolean elmCommon = elem.getNamespace()==null || elem.getNamespace().getID()==null;

                        String elemLink = request.getContextPath() + "/dataelements/" + elem.getID();

                        // see if the element is part of any foreign key relations
                        Vector _fks = searchEngine.getFKRelationsElm(elem.getID(), dataset.getID());
                        boolean fks = (_fks!=null && _fks.size()>0) ? true : false;

                        // see if this element is common, and has any newer released version
                        boolean hasNewerReleases = elmCommon && searchEngine.hasNewerReleases(elem);

                        String elemDefinition = elem.getAttributeValueByShortName("Definition");
                        if (fks){
                            hasForeignKeys = true;
                        }
                        if (elmCommon){
                            hasCommonElms = true;
                        }

                        String valueDelimiter = elem.getValueDelimiter();
                        if (valueDelimiter==null){
                            valueDelimiter = "";
                        }

                        String mandatoryFlag = elem.isMandatoryFlag() ? "T" : "F";
                        String mandatoryFlagChecked = elem.isMandatoryFlag() ? "checked=\"checked\"" : "";

                        String primaryKeyFlag = elem.isPrimaryKey() ? "T" : "F";
                        String primaryKeyFlagChecked = elem.isPrimaryKey() ? "checked=\"checked\"" : "";
                        if (elem.isPrimaryKey()){
                            hasPrimaryKeys = true;
                        }

                        String trStyle = (i%2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
                    %>

                        <!-- element row -->

                        <tr id="tr<%=elem.getID()%>" onclick="tbl_obj.selectRow(this);">

                            <td style="text-align: right; padding-right:10px">
                                <%
                                if (editDstPrm){
                                    String name = elmCommon ? "linkelm_id" : "delem_id";
                                    %>
                                    <input onclick="tbl_obj.clickOtherObject();"
                                            type="checkbox"
                                            style="height:13px; width:13px" name="<%=Util.processForDisplay(name)%>" value="<%=elem.getID()%>"/>
                                    <%
                                }
                                %>
                            </td>

                            <td style="text-align:left; padding-left:5px; padding-right:10px">
                                <%
                                // red asterisk
                                if (false){
                                    String elemWorkingUser = "";
                                    %>
                                    <span title="<%=Util.processForDisplay(elemWorkingUser, true)%>" style="color:red">* </span><%
                                    hasMarkedElems = true;
                                }

                                // short name
                                if (elemDefinition!=null){ %>
                                    <a title="<%=Util.processForDisplay(elemDefinition, true)%>" href="<%=elemLink%>"><%=Util.processForDisplay(elem.getShortName())%></a><%
                                }
                                else { %>
                                    <a href="<%=elemLink%>"><%=Util.processForDisplay(delem_name)%></a><%
                                }

                                // common elm indicator
                                if (elmCommon){ %>
                                    <sup class="commonelm">C</sup>
                                    <%
                                    if (hasNewerReleases){ %>
                                        <a href="javascript:getNewerReleases('<%=elem.getID()%>', '<%=elem.getIdentifier()%>', <%=elem.getPositionInTable()%>)">
                                            <img style="border:0" src="images/new.png" width="16" height="16" title="Has newer releases" alt="Has newer releases"/>
                                        </a><%
                                    }
                                }

                                // FK indicator
                                if (fks){ %>
                                    &nbsp;
                                    <span style="font-size: 70%">
                                        <a href="foreign_keys.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=Util.processForDisplay(elem.getShortName())%>&amp;ds_id=<%=dsID%>&amp;table_id=<%=tableID%>">
                                            <b><i>(FK)</i></b>
                                        </a>
                                    </span><%
                                }
                                %>
                            </td>

                            <td style="text-align: left; padding-right:10px">
                                <%=Util.processForDisplay(datatype)%>
                            </td>

                            <td style="text-align: left; padding-right:10px">
                                <% if (elem.getType().equals("CH1")){ %>
                                <a href="javascript:clickLink('<%=request.getContextPath()%>/fixedvalues/elem/<%=elem.getID() + "/" + ("view".equals(request.getParameter("mode")) ? "view" : "edit" ) %>')">
                                        <%=Util.processForDisplay(elemType)%>
                                    </a>
                                <%} else{ %>
                                    <%=Util.processForDisplay(elemType)%>
                                <% } %>
                                <input type="hidden" name="oldpos_<%=elem.getID()%>" value="<%=elem.getPositionInTable()%>"/>
                                <input type="hidden" name="pos_<%=elem.getID()%>" value="<%=elem.getPositionInTable()%>"/>
                            </td>

                            <td style="text-align: left; padding-right:10px">
                                <input type="hidden" name="oldmndtry_<%=elem.getID()%>" value="<%=mandatoryFlag%>"/>
                                <input type="checkbox" name="mndtry_<%=elem.getID()%>"  value="T" onclick="tbl_obj.clickOtherObject();" <%=mandatoryFlagChecked%>/>
                            </td>

                            <%
                            if (hasFixedValueElements){
                                if (elem.getType()!=null && elem.getType().equals("CH1")){
                                    %>
                                    <td style="text-align: left; padding-right:10px">
                                        <select name="delim_<%=elem.getID()%>" onclick="tbl_obj.clickOtherObject();">
                                            <%
                                            for (Iterator iter = DataElementHandler.valueDelimiters.entrySet().iterator(); iter.hasNext();){
                                                Map.Entry entry = (Map.Entry)iter.next();
                                                String selected = entry.getKey().equals(valueDelimiter) ? "selected=\"selected\"" : "";
                                                %>
                                                <option value="<%=entry.getKey()%>" <%=selected%>><%=entry.getValue()%></option><%
                                            }
                                            %>
                                        </select>
                                        <input type="hidden" name="olddelim_<%=elem.getID()%>" value="<%=valueDelimiter%>"/>
                                    </td><%
                                }
                                else{
                                    %><td style="text-align: left; padding-right:10px">&nbsp;</td><%
                                }
                            }

                             %>
                             <td style="text-align: left; padding-right:10px">
                                 <input type="hidden" name="oldprimkey_<%=elem.getID()%>" value="<%=primaryKeyFlag%>"/>
                                 <input type="checkbox" name="primkey_<%=elem.getID()%>"  value="T" onclick="tbl_obj.clickOtherObject();" <%=primaryKeyFlagChecked%>/>
                             </td>

                        </tr>
                        <%
                    } // end elements display loop
                    %>

                    <tr style="height:10px;">
                        <td style="width:100%" colspan="<%=String.valueOf(colCount)%>"></td>
                    </tr>

                    <%
                    // explanations about red asterisks, fks and c-signs
                    if (false){%>
                        <tr style="height:10px;">
                            <td style="font-size:70%;width:100%" colspan="<%=String.valueOf(colCount)%>">
                                (a red wildcard stands for checked-out element)
                            </td>
                        </tr><%
                    }
                    if (user!=null && elems!=null && elems.size()>0 && hasForeignKeys){%>
                        <tr style="height:10px;">
                            <td style="font-size:70%;width:100%" colspan="<%=String.valueOf(colCount)%>">
                                (the <em><strong style="text-decoration:underline">(FK)</strong></em> link indicates the element participating in a foreign key relation)
                            </td>
                        </tr><%
                    }
                    if (user!=null && elems!=null && elems.size()>0 && hasPrimaryKeys){%>
                        <tr style="height:10px;">
                            <td style="font-size:70%;width:100%" colspan="<%=String.valueOf(colCount)%>">
                                (the <em><strong style="text-decoration:underline">(PK)</strong></em> marks elements participating in the table's primary key)
                            </td>
                        </tr><%
                    }
                    if (elems!=null && elems.size()>0 && hasCommonElms){%>
                        <tr style="height:10px;">
                            <td style="font-size:70%;width:100%" colspan="<%=String.valueOf(colCount)%>">
                                (the <sup class="commonelm">C</sup> sign marks a common element)
                            </td>
                        </tr><%
                    }
                    %>

                    </tbody>
                </table>
            </td>

            <!-- ordering buttons -->

            <%
            if (elems.size()>1 && editDstPrm){ %>
                <td style="text-align:left;padding-right:10px;vertical-align:middle;height:10px;width:10%">
                    <table cellspacing="2" cellpadding="2" style="border:0">
                        <tr>
                            <td>
                                <a href="javascript:moveFirst()"><img src="images/move_first.gif" style="border:0" alt="" title="move selected row to top"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="javascript:moveRowUp()"><img src="images/move_up.gif" style="border:0" alt="" title="move selected row up"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <img src="images/dot.gif" alt=""/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="javascript:moveRowDown()"><img alt="" src="images/move_down.gif" style="border:0" title="move selected row down"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="javascript:moveLast()"><img alt="" src="images/move_last.gif" style="border:0" title="move selected row last"/></a>
                            </td>
                        </tr>
                    </table>
                </td><%
            }
            %>
        </tr>

    </table>

    <div style="display:none">
        <input type="hidden" name="mode" value="delete"/>
        <input type="hidden" name="ds_id" value="<%=dsID%>"/>
        <input type="hidden" name="ds_name" value="<%=Util.processForDisplay(dsName, true)%>"/>
        <input type="hidden" name="table_id" value="<%=tableID%>"/>
        <input type="hidden" name="changed" value="0"/>
        <input type="hidden" name="copy_elem_id" value=""/>
        <input type="hidden" name="upd_version" value="false"/>
        <input type="hidden" name="str_elem_ids" value="<%=strElemIDs%>"/>
    </div>
</form>

<form id="common_elm_link_form" method="post" action="tblelems.jsp">
    <div style="display:none">
        <input type="hidden" name="link_elm" value=""/>
        <input type="hidden" name="mode" value="add"/>
        <input type="hidden" name="table_id" value="<%=tableID%>"/>
        <input type="hidden" name="ds_id" value="<%=dsID%>"/>
        <input type="hidden" name="ds_name" value="<%=Util.processForDisplay(dsName, true)%>"/>
        <input type="hidden" name="elmpos" value="<%=maxPos+1%>"/>
    </div>
</form>

<form id="common_elm_rplc_form" method="post" action="tblelems.jsp">
    <div style="display:none">
        <input type="hidden" name="rplc_id" value=""/>
        <input type="hidden" name="rplc_elm" value=""/>
        <input type="hidden" name="rplc_pos" value="<%=maxPos+1%>"/>
        <input type="hidden" name="mode" value="add"/>
        <input type="hidden" name="table_id" value="<%=tableID%>"/>
        <input type="hidden" name="ds_id" value="<%=dsID%>"/>
        <input type="hidden" name="ds_name" value="<%=Util.processForDisplay(dsName, true)%>"/>
        <input type="hidden" name="elmpos" value="<%=maxPos+1%>"/>
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
