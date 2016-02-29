<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.sql.ConnectionUtil,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%!private String type=null;%>
<%!private String mode=null;%>
<%!private DElemAttribute attribute=null;%>
<%!private Vector attrFields=null;%>
<%!private DDSearchEngine  searchEngine=null;%>
<%@ include file="history.jsp" %>

            <%
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
            response.setHeader("Expires", Util.getExpiresDateString());

            request.setCharacterEncoding("UTF-8");

            ServletContext ctx = getServletContext();

            String urlPath = ctx.getInitParameter("basens-path");
            if (urlPath == null) urlPath = "";

            DDUser user = SecurityUtil.getUser(request);

            if (request.getMethod().equals("POST")){
                  if (user == null){
                      %>
                          <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
                          <body>
                              <h1>Error</h1><b>Not authorized to post any data!</b>
                          </body>
                          </html>
                      <%
                      return;
                  }
            }

            String attr_id = request.getParameter("attr_id");

            mode = request.getParameter("mode");
            if (mode == null || mode.trim().length()==0) {
                mode = "view";
            }

            if (mode.equals("add")){
                boolean addPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/attributes", "i");
                if (!addPrm){ %>
                    <b>Not allowed!</b> <%
                    return;
                }
            }

            if (!mode.equals("add") && (attr_id == null || attr_id.length()==0)){ %>
                <b>Attribute ID is missing!</b> <%
                return;
            }

            String type = request.getParameter("type");
            if (type!=null && type.length()==0)
                type = null;

            String idPrefix = "";
            if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX))
                idPrefix = "c";
            else if (type!=null && type.equals(DElemAttribute.TYPE_SIMPLE))
                idPrefix = "s";

            // check permissions
            boolean editPrm = false;
            boolean deletePrm = false;
            if (!mode.equals("add")){
                editPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/attributes/" + idPrefix + attr_id, "u");
                deletePrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/attributes/" + idPrefix + attr_id, "d");
            }
            if (mode.equals("edit") && !editPrm){ %>
                <b>Not allowed!</b> <%
                return;
            }
            if (mode.equals("delete") && !deletePrm){ %>
                <b>Not allowed!</b> <%
                return;
            }

            if (request.getMethod().equals("POST")){

                Connection userConn = null;
                String redirUrl = "";

                try{
                    userConn = user.getConnection();

                    // if mode==delete, check whether the attribute is used somewhere. if Yes, then prompt user
                    if (mode.equals("delete")){
                        searchEngine = new DDSearchEngine(userConn, "");
                        int attrUseCount = searchEngine.getAttributeUseCount(attr_id, type);
                        if (attrUseCount>0){
                            String sName = request.getParameter("short_name");
                            response.sendRedirect("dialog_delete_attr.jsp?mode=delete&attr_id=" + attr_id + "&type=" + type + "&short_name=" + sName);
                            return;
                        }
                    }

                    AttributeHandler handler = new AttributeHandler(userConn, request, ctx);
                    handler.setUser(user);
                    handler.execute();

                    if (mode.equals("add")){
                        String id = handler.getLastInsertID();
                        if (id != null && id.length()!=0)
                            redirUrl = redirUrl + "delem_attribute.jsp?mode=edit&attr_id=" + id + "&type=" + type;

                        if (history!=null){
                            int idx = history.getCurrentIndex();
                            if (backUrl.indexOf("mode=add")>0){
                                history.remove(idx-1);
                                idx--;
                            }
                            if (idx>0)
                                history.remove(idx);
                        }
                    }
                    else if (mode.equals("edit")){
                        redirUrl = currentUrl;
                        //redirUrl = redirUrl + "delem_attribute.jsp?mode=edit&attr_id=" + attr_id + "&type=" + type;
                    }
                    else if (mode.equals("delete")){
                        String    deleteUrl = history.gotoLastMatching("attributes.jsp");
                        redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
                        //redirUrl = redirUrl + "delem_attribute.jsp?mode=add&type=SIMPLE";
                        //redirUrl = redirUrl + "index.jsp";
                    }
                }
                finally{
                    try { if (userConn!=null) userConn.close();
                    } catch (SQLException e) {}
                }

                response.sendRedirect(redirUrl);
                return;
            }

            Connection conn = null;

            try { // start the whole page try block

            conn = ConnectionUtil.getConnection();
            searchEngine = new DDSearchEngine(conn, "");

            String attr_name = null;
            String attr_shortname = null;
            Namespace attrNamespace = null;

            if (!mode.equals("add")){
                Vector v = searchEngine.getDElemAttributes(attr_id,type);
                if (v!=null && v.size()!=0)
                    attribute = (DElemAttribute)v.get(0);
                if (attribute!=null){
                    type = attribute.getType();
                    attr_name = attribute.getName();
                    attr_shortname = attribute.getShortName();
                    if (attr_name == null) attr_name = "unknown";
                    if (attr_shortname == null) attr_shortname = "unknown";

                    attrNamespace = attribute.getNamespace();

                    if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX)){
                        attrFields = searchEngine.getAttrFields(attr_id);
                        if (attrFields == null) attrFields = new Vector();
                    }
                }
                else{ %>
                    <b>Attribute was not found!</b> <%
                    return;
                }
            }

            String disabled = user == null ? "disabled='disabled'" : "";

            // init page title
            StringBuffer pageTitle = new StringBuffer();
            if (mode.equals("edit")){
                if (type==null)
                    pageTitle.append("Edit attribute");
                else if (type.equals(DElemAttribute.TYPE_COMPLEX))
                    pageTitle.append("Edit complex attribute");
                else if (type.equals(DElemAttribute.TYPE_SIMPLE))
                    pageTitle.append("Edit simple attribute");
            }
            else{
                if (type==null)
                    pageTitle.append("Attribute");
                else if (type.equals(DElemAttribute.TYPE_COMPLEX))
                    pageTitle.append("Complex attribute");
                else if (type.equals(DElemAttribute.TYPE_SIMPLE))
                    pageTitle.append("Simple attribute");
            }
            if (attribute!=null && attribute.getShortName()!=null)
                pageTitle.append(" - ").append(attribute.getShortName());


            %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
  <title><%=pageTitle.toString()%></title>
  <script type="text/javascript">
  // <![CDATA[
        function submitForm(mode){

            if (mode == "delete"){
                var b = confirm("This attribute will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
                if (b==false) return;
            }

            if (mode != "delete"){
                if (!checkObligations()){
                    alert("You have not specified one of the mandatory fields!");
                    return;
                }

                if (hasWhiteSpace("short_name")){
                    alert("Short name cannot contain any white space!");
                    return;
                }
            }

            document.forms["form1"].elements["mode"].value = mode;
            document.forms["form1"].submit();
        }

        function checkObligations(){

            var oName = document.forms["form1"].name;
            var name = oName==null ? null : oName.value;

            <%
            if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
                %>
                var oOblig = document.forms["form1"].obligation;
                var i = oOblig.selectedIndex;
                var oblig = oOblig==null ? null : oOblig.options[i].value;

                if (oblig == null || oblig.length==0) return false;
                <%
            }
            %>

            var oShort = document.forms["form1"].short_name;
            var shortn = oShort==null ? null : oShort.value;

            var oTypeSelect = document.forms["form1"].typeSelect;
            if (oTypeSelect != null){
                var type = oTypeSelect.value;
                if (type.length==0)
                    return false;
            }

            if (name == null || name.length==0) return false;
            if (shortn == null || shortn.length==0) return false;

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

        function fields(url){
            wAttrFields = window.open(url,"AttributeFields","height=600,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
            if (window.focus) {wAttrFields.focus()}
        }

        function onLoad(){
            <%
            String obligation = "";
            String dispType = "";
            if (!mode.equals("add")){

                obligation = attribute.getObligation();
                if (obligation != null && !mode.equals("view")){
                %>
                    var obligation = '<%=obligation%>';
                    var o = document.forms["form1"].obligation;
                    for (i=0; o!=null && i<o.options.length; i++){
                        if (o.options[i].value == obligation){
                            o.selectedIndex = i;
                            break;
                        }
                    }

                <% }

                dispType = attribute.getDisplayType();
                if (dispType == null)
                    dispType = "";

                if (!mode.equals("view")){
                    %>
                        var dispType = '<%=dispType%>';
                        var o = document.forms["form1"].dispType;
                        for (i=0; o!=null && i<o.options.length; i++){
                            if (o.options[i].value == dispType){
                                o.selectedIndex = i;
                                break;
                            }
                        }

                    <%
                }
            }

            %>
        }

        function goToEdit(){
            document.location.assign("delem_attribute.jsp?attr_id=<%=attr_id%>&type=<%=type%>&mode=edit");
        }

        function fixType(){
            var type = document.forms["form1"].typeSelect.value;
            if (type == null || type.length==0)
                return;
            document.location.assign("delem_attribute.jsp?mode=add&type=" + type);
        }

        function openFxValues(){
            var url = "<%=request.getContextPath()%>/fixedvalues/attr/<%=attr_id%>";
            wCh1Values = window.open(url,"AllowableValues","height=600,width=800,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
            if (window.focus) {wCh1Values.focus()}
        }

        function helpNamespace(){
            alert('Context is required to produce the XMLSchema exports of data defintions. ' +
                    'Basically it defines the context in which you define this attribute. Attributes can be roughly ' +
                    'divided into two contexts: those originating from ISO11179 and those specific to Data Dictionary.');
        }

        function harvest(){
            var msg = "This might take a couple of minutes, depending on the harvestign connection speed and " +
                        "the amount of objects to harvest!";
            wHarvest = window.open("HarvestingServlet","Harvest","height=200,width=300,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
            if (window.focus) {wHarvest.focus()}
        }

        // ]]>
    </script>
</head>

<%
String hlpScreen = "simple_attr_def_";
if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX))
    hlpScreen = "complex_attr_def_";

if (mode.equals("view"))
    hlpScreen = hlpScreen + "view";
else if (mode.equals("edit"))
    hlpScreen = hlpScreen + "edit";
else if (mode.equals("add")){
    if (type==null)
        hlpScreen = "attr_def_add";
    else
        hlpScreen = hlpScreen + "add";
}
else
    hlpScreen = hlpScreen + "view";
%>
<body onload="onLoad()">
<div id="container">
        <jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Attribute"/>
            <jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
        </jsp:include>
        <c:set var="currentSection" value="attributes" />
        <%@ include file="/pages/common/navigation.jsp" %>
<div id="workarea">

            <form id="form1" method="post" action="delem_attribute.jsp">
                <div style="display:none">
                    <%
                    if (!mode.equals("add")){
                        %>
                        <input type="hidden" name="attr_id" value="<%=attr_id%>" /><%

                        if (type!=null && type.equals(DElemAttribute.TYPE_SIMPLE)){
                            %>
                            <input type="hidden" name="simple_attr_id" value="<%=attr_id%>" /><%
                        }
                        else{
                            %>
                            <input type="hidden" name="complex_attr_id" value="<%=attr_id%>" /><%
                        }
                    }
                    %>
                </div>

                <%
                if (user!=null && mode.equals("view") && editPrm){
                    %>
                    <div id="drop-operations">
                    <h2>Operations:</h2>
                        <ul>
                            <li class="edit"><a href="javascript:goToEdit()">Edit</a></li>
                        </ul>
                    </div><%
                }

                if (mode.equals("add")){ %>
                    <h1>Add an attribute definition</h1> <%
                }
                else if (mode.equals("edit")){ %>
                    <h1>Edit attribute definition</h1> <%
                }
                else{ %>
                    <h1>View attribute definition</h1>
                    <%
                }

                if (!mode.equals("view") && type==null){ %>
                    <div class="attention">NB! Please select the attribute type first. Otherwise your entries will be lost.</div><%
                }

            int displayed = 1;

            if (mode.equals("view")){
                %>
                <table class="datatable results" style="clear:right">
                <col style="width:10em"/>
                <col style="width:35em"/>
                <%
            } else {
            %>
                <table class="datatable" style="clear:right">
                <col style="width:9em"/>
                <col style="width:2em"/>
                <col style="width:35em"/>
                <%
            }
            %>

            <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                <th scope="row" class="scope-row">Type</th>
                <%
                displayed++;
                if (!mode.equals("view")){
                    %>
                    <td><img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/></td>
                    <%
                }
                %>
                <td>
                    <%
                    if (mode.equals("add") && type==null){ %>
                        <select class="small" name="typeSelect" onchange="fixType()">
                            <option value="">-- Select attribute type --</option>
                            <option value="SIMPLE">Simple attribute</option>
                            <option value="COMPLEX">Complex attribute</option>
                        </select> <%
                    }
                    else{
                        if(type.equals(DElemAttribute.TYPE_SIMPLE)){ %>
                            <b>SIMPLE ATTRIBUTE</b>
                        <% }else if (type.equals(DElemAttribute.TYPE_COMPLEX)){ %>
                            <b>COMPLEX ATTRIBUTE</b>
                        <% } else{ %>
                            <b>SIMPLE ATTRIBUTE</b>
                        <% }
                    }
                    %>
                </td>
            </tr>

            <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                <th scope="row" class="scope-row">Short name</th>
                        <%
                        displayed++;
                        if (!mode.equals("view")){
                            %>
                            <td><img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/></td>
                            <%
                        }
                        %>
                <td>
                    <% if(!mode.equals("add")){ %>
                        <em><%=Util.processForDisplay(attr_shortname)%></em>
                        <input type="hidden" name="short_name" value="<%=Util.processForDisplay(attr_shortname,true)%>" />
                    <% } else{ %>
                        <input type="text" class="smalltext" size="30" name="short_name" />
                    <% } %>
                </td>
            </tr>



            <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                <th scope="row" class="scope-row">Name</th>
                        <%
                        displayed++;
                        if (!mode.equals("view")){
                            %>
                            <td><img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/></td>
                            <%
                        }
                        %>
                <td>
                    <% if(mode.equals("edit")){ %>
                        <input <%=disabled%> type="text" class="smalltext" size="30" name="name" value="<%=Util.processForDisplay(attr_name)%>" />
                    <% } else if (mode.equals("add")){ %>
                        <input <%=disabled%> type="text" class="smalltext" size="30" name="name" />
                    <% } else { %>
                        <%=Util.processForDisplay(attr_name)%>
                    <% } %>
                </td>
            </tr>

            <%
            boolean displayNamespace = mode.equals("view") && attrNamespace!=null && attrNamespace.getShortName()!=null;
            if (displayNamespace==false && mode.equals("edit")){
                int count = 0;
                Vector namespaces = searchEngine.getNamespaces();
                for (int k=0; namespaces!=null && k<namespaces.size(); k++){
                    Namespace ns = (Namespace)namespaces.get(k);
                    if (ns.getTable()!=null || ns.getDataset()!=null || (ns.getID()!=null && ns.getID().equals("1")))
                        continue;
                    String nsName = ns.getFullName();
                    if (nsName==null)
                        nsName = ns.getShortName();
                    if (nsName.indexOf("attributes") < 0)
                        continue;
                    count++;
                }
                displayNamespace = count>0;
            }
            if (displayNamespace){
                %>
                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>

                    <th scope="row" class="scope-row">Context</th>
                            <%
                            displayed++;
                            if (!mode.equals("view")){
                                %>
                                <td><img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/></td>
                                <%
                            }
                            %>
                    <td>
                        <%
                        if (mode.equals("view")){
                            String nsName = attrNamespace==null ? null : attrNamespace.getFullName();
                            if (nsName==null){
                                if (attrNamespace==null)
                                    nsName = "";
                                else
                                    nsName = attrNamespace.getShortName();
                            }
                            if (nsName == null) nsName = "";

                            %>
                            <%=Util.processForDisplay(nsName)%> <%
                        }
                        else{
                            %>
                            <select <%=disabled%> class="small" name="ns">
                                <%
                                Vector namespaces = searchEngine.getNamespaces();
                                for (int k=0; namespaces!=null && k<namespaces.size(); k++){
                                    Namespace ns = (Namespace)namespaces.get(k);

                                    if (ns.getTable()!=null || ns.getDataset()!=null || (ns.getID()!=null && ns.getID().equals("1")))
                                        continue;

                                    String nsName = null;
                                    if (ns!=null) nsName = ns.getFullName();
                                    if (nsName == null) nsName = ns.getShortName();
                                    if (nsName == null) nsName = "";

                                    if (nsName.indexOf("attributes") < 0)
                                        continue;

                                    String ifSelected = "";
                                    if (attrNamespace!=null){
                                        if (attrNamespace.getID().equals(ns.getID())){
                                            ifSelected = "selected=\"selected\"";
                                        }
                                    }
                                    else if (nsName.indexOf("Data Dictionary") != -1)
                                        ifSelected = "selected=\"selected\"";

                                    %>
                                    <option <%=ifSelected%> value="<%=ns.getID()%>"><%=Util.processForDisplay(nsName)%></option>
                                    <%
                                }
                                %>
                            </select>
                            <%
                        }
                        %>
                    </td>
                </tr><%
            }
            %>

            <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                <th scope="row" class="scope-row">Definition</th>
                        <%
                        displayed++;
                        if (!mode.equals("view")){
                            %>
                            <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                            <%
                        }
                        %>
                <td>
                    <%
                    if (!mode.equals("add")){
                        String definition = (attribute.getDefinition() == null) ? "" : attribute.getDefinition();
                        if (mode.equals("edit")){
                            %>
                            <textarea <%=disabled%> class="small" rows="5" cols="52" name="definition"><%=Util.processForDisplay(definition, true, true)%></textarea>
                            <%
                        }
                        else{
                            %>
                            <%=Util.processForDisplay(definition)%>
                            <%
                        }
                    }
                    else{
                        %>
                        <textarea <%=disabled%> class="small" rows="5" cols="52" name="definition"></textarea>
                        <%
                    }
                    %>
                </td>
            </tr>

            <%
            if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
                %>

                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <th scope="row" class="scope-row">Obligation</th>
                            <%
                            displayed++;
                            if (!mode.equals("view")){
                                %>
                                <td><img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/></td>
                                <%
                            }
                            %>
                    <td>
                        <%
                        if (mode.equals("view")){
                            String dispOblig = "";
                            if (obligation != null && obligation.equals("M"))
                                dispOblig = "Mandatory";
                            else if (obligation != null && obligation.equals("O"))
                                dispOblig = "Optional";
                            else if (obligation != null && obligation.equals("C"))
                                dispOblig = "Conditional";
                            %>
                            <%=dispOblig%>
                            <%
                        }
                        else{
                            %>
                            <select <%=disabled%> class="small" name="obligation">
                                <option selected="selected" value="M">Mandatory</option>
                                <option value="O">Optional</option>
                                <option value="C">Conditional</option>
                            </select>
                            <%
                        }
                        %>
                    </td>
                </tr>

                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <th scope="row" class="scope-row">Display type</th>
                            <%
                            displayed++;
                            if (!mode.equals("view")){
                                %>
                                <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                                <%
                            }
                            %>
                    <td>
                        <%
                        if (mode.equals("view")){
                            String dispDispType = "Not specified";
                            if (dispType.equals("text"))
                                dispDispType = "Text box";
                            else if (dispType.equals("textarea"))
                                dispDispType = "Text area";
                            else if (dispType.equals("select"))
                                dispDispType = "Select box";
                            else if (dispType.equals("image"))
                                dispDispType = "Image";
                            %>
                            <%=dispDispType%>
                            <%
                        }
                        else{
                            %>
                            <select <%=disabled%> class="small" name="dispType">
                                <option value="">- Do not display at all -</option>
                                <option selected="selected" value="text">Text box</option>
                                <option value="textarea">Text area</option>
                                <option value="select">Select box</option>
                                <option value="image">Image</option>
                            </select>
                            <%
                            if (mode.equals("edit") && dispType!=null && dispType.equals("select")){
                                %>
                                &nbsp;<span class="smallfont"><a href="<%=request.getContextPath()%>/fixedvalues/attr/<%=attr_id%>/edit">
                                <b>FIXED VALUES</b></a></span>
                                <%
                            }
                        }
                        %>
                    </td>
                </tr>
                <%
                if (mode.equals("view") && dispType!=null && dispType.equals("select")){
                %>
                    <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                        <th scope="row" class="scope-row">
                                <a href="<%=request.getContextPath()%>/fixedvalues/attr/<%=attr_id%>">
                                    Fixed values
                                </a>
                        </th>
                        <td>
                        <%
                            displayed++;
                            Vector fxValues = searchEngine.getFixedValues(attr_id, "attr");
                            if (fxValues!=null && fxValues.size()>0){
                                for (int g=0; g<fxValues.size(); g++){
                                    FixedValue fxValue = (FixedValue)fxValues.get(g);
                                    %>
                                    <%=Util.processForDisplay(fxValue.getValue())%><br/>
                                    <%
                                }
                            }
                        %>
                        </td>
                    </tr>
                <%
                }
                %>
                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <th scope="row" class="scope-row">Display multiple</th>
                        <%
                            displayed++;
                                if (!mode.equals("view")){
                                    %>
                                    <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                                    <%
                                }
                            %>
                    <td>
                        <%
                        if (!mode.equals("add")){
                            String multi = attribute.getDisplayMultiple();
                            String checked = (multi.equals("1")) ? "checked='checked'":"";
                            String checked_text = (multi.equals("1")) ? "True":"False";
                            if (mode.equals("edit")){
                                %>
                                <input <%=disabled%> <%=checked%> type="checkbox" class="smalltext" name="dispMultiple" value="1" />
                                <%
                            }
                            else{
                                %>
                                <%=Util.processForDisplay(checked_text)%>
                                <%
                            }
                        }
                    else {
                        %>
                        <input <%=disabled%> type="checkbox" class="smalltext" name="dispMultiple" value="1" />
                        <%
                    }
                    %>
                </td>
            </tr>
                <%
            }
            %>
            <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                <th scope="row" class="scope-row">Inheritance</th>
                        <%
                        displayed++;
                        if (!mode.equals("view")){
                            %>
                            <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                            <%
                        }
                        %>
                <td>
                    <%
                    String inh_text[]=new String[3];
                    inh_text[0] = "No inheritance";
                    inh_text[1] = "Inherit attribute values from parent level with possibilty to add new values";
                    inh_text[2] = "Inherit attribute values from parent level with possibilty to overwrite them";
                    int chk = 0;

                    if (!mode.equals("add")){
                        String inherit = attribute.getInheritable();
                        if (inherit==null) inherit="0";
                        chk =  Integer.parseInt(inherit);
                    }
                    if (mode.equals("view")){
                        %>
                        <%=inh_text[chk]%>
                        <%
                    }
                    else{
                        for (int i=0;i<3;i++){
                        %>
                            <input value="<%=i%>" <%=disabled%> <% if (i==chk) %>checked="checked"<%;%> type="radio" class="smalltext" name="inheritable" /><%=inh_text[i]%><br/>
                        <%
                        }
                    }
                %>
                </td>
            </tr>

            <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                <th scope="row" class="scope-row">Display order</th>
                        <%
                        displayed++;
                        if (!mode.equals("view")){
                            %>
                            <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                            <%
                        }
                        %>
                <td>
                    <%
                    if (!mode.equals("add")){
                        int i = attribute.getDisplayOrder();
                        String dispOrder = (i==999) ? "" : String.valueOf(i);
                        if (mode.equals("edit")){
                            %>
                            <input <%=disabled%> type="text" class="smalltext" size="5" name="dispOrder" value="<%=Util.processForDisplay(dispOrder)%>" />
                            <%
                        }
                        else{
                            %>
                            <%=Util.processForDisplay(dispOrder)%>
                            <%
                        }
                    }
                    else {
                        %>
                        <input <%=disabled%> type="text" class="smalltext" size="5" name="dispOrder" />
                        <%
                    }
                    %>
                </td>
            </tr>

            <%
            if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){ %>

                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <th scope="row" class="scope-row">Display for</th>
                            <%
                            displayed++;
                            if (!mode.equals("view")){
                                %>
                                <td><img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/></td>
                                <%
                            }
                            %>
                    <td>
                        <%
                        String ch1Checked = (!mode.equals("add") && attribute.displayFor("CH1")) ? "checked=\"checked\"" : "";
                        String ch2Checked = (!mode.equals("add") && attribute.displayFor("CH2")) ? "checked=\"checked\"" : "";
                        //String dclChecked = (!mode.equals("add") && attribute.displayFor("DCL")) ? "checked" : "";
                        String dstChecked = (!mode.equals("add") && attribute.displayFor("DST")) ? "checked=\"checked\"" : "";
                        String tblChecked = (!mode.equals("add") && attribute.displayFor("TBL")) ? "checked=\"checked\"" : "";
                        String fxvChecked = (!mode.equals("add") && attribute.displayFor("FXV")) ? "checked=\"checked\"" : "";
                        String schChecked = (!mode.equals("add") && attribute.displayFor(DElemAttribute.ParentType.SCHEMA.toString())) ? "checked=\"checked\"" : "";
                        String scsChecked = (!mode.equals("add") && attribute.displayFor(DElemAttribute.ParentType.SCHEMA_SET.toString())) ? "checked=\"checked\"" : "";
                        String vcfChecked = (!mode.equals("add") && attribute.displayFor(DElemAttribute.ParentType.VOCABULARY_FOLDER.toString())) ? "checked=\"checked\"" : "";

                        if (mode.equals("view")){
                            boolean hasOne = false;
                            if (ch1Checked.equals("checked=\"checked\"")) { hasOne = true; %>
                                Data elements with fixed values (code list and elements from a vocabulary) <%
                            }
                            if (ch2Checked.equals("checked=\"checked\"")) { hasOne = true; %>
                                <br/>Data elements with quanitative values <%
                            }
                            if (dstChecked.equals("checked=\"checked\"")) { hasOne = true; %>
                                <br/>Datasets <%
                            }
                            if (tblChecked.equals("checked=\"checked\"")) { hasOne = true; %>
                                <br/>Dataset tables <%
                            }
                            if (fxvChecked.equals("checked=\"checked\"")) { hasOne = true; %>
                                <br/>Fixed values (code list and vocabulary)<%
                            }
                            if (schChecked.equals("checked=\"checked\"")) { hasOne = true; %>
                                <br/>Schemas <%
                            }
                            if (scsChecked.equals("checked=\"checked\"")) { hasOne = true; %>
                                <br/>Schema sets <%
                            }
                            if (vcfChecked.equals("checked=\"checked\"")) { hasOne = true; %>
                                <br/>Vocabulary folders <%
                            }
                            if (!hasOne){ %>
                                Not specified<%
                            }
                        }
                        else {
                            %>
                            <input <%=disabled%> type="checkbox" <%=ch1Checked%> name="dispWhen" id="dispCH1" value="CH1"/><label for="dispCH1">Data elements with fixed values</label><br/>
                            <input <%=disabled%> type="checkbox" <%=ch2Checked%> name="dispWhen" id="dispCH2" value="CH2"/><label for="dispCH2">Data elements with quanitative values</label><br/>
                            <input <%=disabled%> type="checkbox" <%=dstChecked%> name="dispWhen" id="dispDST" value="DST"/><label for="dispDST">Datasets</label><br/>
                            <input <%=disabled%> type="checkbox" <%=tblChecked%> name="dispWhen" id="dispTBL" value="TBL"/><label for="dispTBL">Dataset tables</label><br/>
                            <input <%=disabled%> type="checkbox" <%=schChecked%> name="dispWhen" id="dispSCH" value="<%=DElemAttribute.ParentType.SCHEMA.toString()%>"/><label for="dispSCH">Schemas</label><br/>
                            <input <%=disabled%> type="checkbox" <%=scsChecked%> name="dispWhen" id="dispSCS" value="<%=DElemAttribute.ParentType.SCHEMA_SET.toString()%>"/><label for="dispSCS">Schema sets</label><br/>
                            <input <%=disabled%> type="checkbox" <%=vcfChecked%> name="dispWhen" id="dispVCF" value="<%=DElemAttribute.ParentType.VOCABULARY_FOLDER.toString()%>"/><label for="dispVCF">Vocabulary folders</label><br/>
                            <%
                        }
                        %>

                    </td>
                </tr>
            <%
            }
            %>
            <%
            if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
                %>
                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <th scope="row" class="scope-row">Display width</th>
                            <%
                            displayed++;
                            if (!mode.equals("view")){
                                %>
                                <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                                <%
                            }
                            %>
                    <td>
                        <%
                        if (!mode.equals("add")){
                            String dispWidth = attribute.getDisplayWidth();
                            if (mode.equals("edit")){
                                %>
                                <input <%=disabled%> type="text" class="smalltext" size="5" name="dispWidth" value="<%=dispWidth%>" />
                                <%
                            }
                            else{
                                %>
                                <%=dispWidth%>
                                <%
                            }
                        }
                        else {
                            %>
                            <input <%=disabled%> type="text" class="smalltext" size="5" name="dispWidth" />
                            <%
                        }
                        %>
                    </td>
                </tr>
                <%
            }

            if (type!=null && !type.equals(DElemAttribute.TYPE_COMPLEX)){
                %>
                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <th scope="row" class="scope-row">Display height</th>
                            <%
                            displayed++;
                            if (!mode.equals("view")){
                                %>
                                <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                                <%
                            }
                            %>
                    <td>
                        <%
                        if (!mode.equals("add")){
                            String dispHeight = attribute.getDisplayHeight();
                            if (mode.equals("edit")){
                                %>
                                <input <%=disabled%> type="text" class="smalltext" size="5" name="dispHeight" value="<%=dispHeight%>" />
                                <%
                            }
                            else{
                                %>
                                <%=dispHeight%>
                                <%
                            }
                        }
                        else {
                            %>
                            <input <%=disabled%> type="text" class="smalltext" size="5" name="dispHeight" />
                            <%
                        }
                        %>
                    </td>
                </tr>

                <%
                pageContext.setAttribute("mode", mode);
                pageContext.setAttribute("attr", attribute);
                if (mode.equals("edit") || mode.equals("add")) {
                    pageContext.setAttribute("rdfNamespaces", searchEngine.getRdfNamespaces());
                }
                %>
                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <% displayed++; %>
                    <th scope="row" class="scope-row">RDF property URI</th>
                    <c:if test="${mode eq 'edit' || mode eq 'add'}">
                        <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                    </c:if>
                    <td>
                        <c:if test="${mode eq 'view'}">
                            ${attr.rdfPropertyUri}
                        </c:if>
                        <c:if test="${mode eq 'edit' || mode eq 'add'}">
                            <c:if test="${not empty rdfNamespaces}">
                                <select name="rdfNamespaceId" class="small">
                                    <option value="0" />
                                    <c:forEach items="${rdfNamespaces}" var="rdfNamespace">
                                        <c:choose>
                                            <c:when test="${rdfNamespace.id == attr.rdfNamespaceId}">
                                                <option value="${rdfNamespace.id}" selected="selected">${rdfNamespace.uri}</option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${rdfNamespace.id}">${rdfNamespace.uri}</option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </c:if>
                        </c:if>
                    </td>
                </tr>
                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <% displayed++; %>
                    <th scope="row" class="scope-row">RDF property name</th>
                    <c:if test="${mode eq 'edit' || mode eq 'add'}">
                        <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                    </c:if>
                    <td>
                        <c:if test="${mode eq 'view'}">
                            ${attr.rdfPropertyName}
                        </c:if>
                        <c:if test="${mode eq 'edit' || mode eq 'add'}">
                            <input type="text" name="rdfPropertyName" value="${attr.rdfPropertyName}" />
                        </c:if>
                    </td>
                </tr>
                <%
            }


            // start HARVESTER LINK

            boolean dispHarvesterID = false;
            String harvesterID = null;
            if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX)){

                if (!mode.equals("add"))
                    harvesterID = attribute.getHarvesterID();

                if (mode.equals("view")){
                    if (!Util.isEmpty(harvesterID))
                        dispHarvesterID = true;
                }
                else
                    dispHarvesterID = true;
            }

            if (dispHarvesterID){

                Vector harvesters = null;
                if (!mode.equals("view"))
                    harvesters = searchEngine.getHarvesters();

                %>

                <tr <% if (mode.equals("view")) %> class="<%=Util.isOdd(displayed)%>" <%;%>>
                    <th scope="row" class="scope-row">Linked harvester</th>
                            <%
                            displayed++;
                            if (!mode.equals("view")){
                                %>
                                <td><img src="images/optional.gif" alt="Optional" title="Optional"/></td>
                                <%
                            }
                            %>
                    <td>
                        <%
                        if (!mode.equals("view")){
                            String noLinkSelected = Util.isEmpty(harvesterID) ? "selected=\"selected\"" : "";
                            %>
                            <select class="small" name="harv_id">
                                <option <%=noLinkSelected%> value="null">-- no link --</option>
                                <%
                                for (int i=0; harvesters!=null && i<harvesters.size(); i++){
                                    String harvID = (String)harvesters.get(i);
                                    String selected = "";
                                    if (!Util.isEmpty(harvesterID) && harvesterID.equals(harvID))
                                        selected = "selected=\"selected\"";
                                    %>
                                    <option <%=selected%> value="<%=harvID%>"><%=harvID%></option><%
                                }
                                %>
                            </select><%

                            if (user!=null && user.isAuthentic()){ %>
                                <input type="button" class="smallbutton" value="Harvest" onclick="harvest()" /><%
                            }
                        }
                        else { %>
                            <%=harvesterID%>
                        <%
                        }
                        %>
                    </td>
                </tr>
                <%
            }

            // end HARVESTER LINK
            %>

        <% if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX) && !mode.equals("add")){ // if COMPLEX and mode=add
        %>
        <tr>
            <th scope="row" class="scope-row">Fields</th>
            <% if (!mode.equals("view")){ %>
            <td></td>
            <% } %>
            <td>
                <table class="datatable results">
                    <col style="width:100px"/>
                    <col style="width:200px"/>
                    <tr>
                        <th scope="col">Name</th>
                        <th scope="col">Definition</th>
                    </tr>
                    <%

                    //String position = String.valueOf(attrFields.size() + 1);
                    int position = 0;
                    if (attrFields!=null){
                        for (int i=0; i<attrFields.size(); i++){
                            Hashtable hash = (Hashtable)attrFields.get(i);
                            String id = (String)hash.get("id");
                            String name = (String)hash.get("name");
                            String definition = (String)hash.get("definition");
                            if (definition.length()>50) definition = definition.substring(0,50) + " ...";
                            String fieldLink = "m_attr_field.jsp?attr_id=" + attr_id + "&amp;attr_name=" + attr_name + "&amp;attr_ns=basens&amp;field_id=" + id;

                            int pos = Integer.parseInt((String)hash.get("position"));
                            if (pos >= position) position = pos +1;
                            String zebraClass = (i + 1) % 2 != 0 ? "odd" : "even";

                            %>
                            <tr class="<%=zebraClass%>">
                                <td align="center"><a href="<%=fieldLink%>"><%=Util.processForDisplay(name)%></a></td>
                                <td align="center" onmouseover=""><%=Util.processForDisplay(definition)%></td>
                            </tr>
                            <%
                        }
                    }
                    %>
                </table>
            </td>
        </tr>
        <%
        if (user!=null){
            %>
            <tr>
                <td></td>
                <% if (!mode.equals("view")){ %>
                <td></td>
                <% } %>
                <td>
                    <b>*</b> <span class="smallfont"><a href="m_attr_fields.jsp?attr_id=<%=attr_id%>&amp;attr_name=<%=Util.processForDisplay(attr_shortname)%>">
                        <b>FIELDS</b></a></span>&nbsp;&nbsp;
                    <span class="smallfont" style="font-weight: normal">
                        &lt;&nbsp;click here to add/remove fields of this complex attribute
                    </span>
                </td>
            </tr>
            <%
        }
        %>

        <% } // end if COMPLEX and mode=add

        if (!mode.equals("view")){ %>
            <tr style="height:10px;"><td colspan="3"></td></tr>
            <tr>
                <td colspan="3" style="text-align:center">

                    <%

                    if (mode.equals("add")){ // if mode is "add"
                        if (user==null){ %>
                            <input type="button" class="mediumbuttonb" value="Add" disabled="disabled" />&nbsp;&nbsp;
                        <%} else {%>
                            <input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')" />&nbsp;&nbsp;
                        <% }
                    } // end if mode is "add"

                    if (!mode.equals("add")){ // if mode is not "add"
                        if (user==null){ %>
                            <input type="button" class="mediumbuttonb" value="Save" disabled="disabled" />&nbsp;&nbsp;
                            <input type="button" class="mediumbuttonb" value="Delete" disabled="disabled" />&nbsp;&nbsp;
                        <%} else {%>
                            <input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')" />&nbsp;&nbsp;
                            <input type="button" class="mediumbuttonb" value="Delete" onclick="submitForm('delete')" />&nbsp;&nbsp;
                        <% }
                    } // end if mode is not "add"

                    %>

                </td>
            </tr> <%
        }
        %>
    </table>
        <div style="display:none">
            <%
            if (type!=null){ %>
                <input type="hidden" name="type" value="<%=type%>" /> <%
            }
            %>
            <input type="hidden" name="mode" value="<%=mode%>" />
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
