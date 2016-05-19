<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%!private String mode=null;%>
<%!private Hashtable attrField=null;%>

<%@ include file="history.jsp" %>

<%!

private String legalizeAlert(String in){

    in = (in != null ? in : "");
    StringBuffer ret = new StringBuffer();

    for (int i = 0; i < in.length(); i++) {
        char c = in.charAt(i);
        if (c == '\'')
            ret.append("\\'");
        else if (c == '\\')
            ret.append("\\\\");
        else
            ret.append(c);
    }

    return ret.toString();
}

%>

            <%
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
            response.setHeader("Expires", Util.getExpiresDateString());

            request.setCharacterEncoding("UTF-8");

            ServletContext ctx = getServletContext();

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

            String field_id = request.getParameter("field_id");

            String attr_name = request.getParameter("attr_name");
            String attr_id = request.getParameter("attr_id");

            if (attr_id == null || attr_id.length()==0){ %>
                <b>Attribute ID is missing!</b> <%
                return;
            }
            if (field_id == null || field_id.length()==0){ %>
                <b>Attribute field ID is missing!</b> <%
                return;
            }

            if (attr_name == null) attr_name = "?";


            mode = request.getParameter("mode");
            if (mode == null || mode.trim().length()==0) {
                mode = "view";
            }

            if (request.getMethod().equals("POST")){

                Connection userConn = null;

                try{
                    userConn = user.getConnection();

                    MAttrFieldsHandler handler = new MAttrFieldsHandler(userConn, request, ctx);

                    try{
                        handler.execute();
                    }
                    catch (Exception e){
                        %>
                        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"><body><b><%=e.toString()%></b></body></html>
                        <%
                        return;
                    }
                }
                finally{
                    try { if (userConn!=null) userConn.close();
                    } catch (SQLException e) {}
                }
                String redirUrl=null;
                if (mode.equals("delete")){
                    String    deleteUrl = history.gotoLastNotMatching("m_attr_field.jsp");
                    redirUrl = (deleteUrl!=null && deleteUrl.length() > 0) ? deleteUrl : request.getContextPath();
                    //redirUrl = "" +
                    //                "/m_attr_fields.jsp?mode=edit&attr_id=" + attr_id + "&attr_name=" + attr_name;
                }
                else {
                    redirUrl=currentUrl;
                    //redirUrl = "" +
                    //                "/m_attr_field.jsp?mode=edit&attr_id=" + attr_id + "&attr_name=" + attr_name + "&field_id=" + field_id;
                }
                response.sendRedirect(redirUrl);
                return;
            }

            Connection conn = null;

            try { // start the whole page try block

            conn = ConnectionUtil.getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "");

            attrField = searchEngine.getAttrField(field_id);
            if (attrField == null) attrField = new Hashtable();
            String disabled = user == null ? "disabled='disabled'" : "";

            String name = (String)attrField.get("name");
            String definition = (String)attrField.get("definition");
            String priority = (String)attrField.get("priority");

            %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <%@ include file="headerinfo.jsp" %>
        <title>Meta</title>
        <script type="text/javascript">
        // <![CDATA[

            function submitForm(mode){

                if (mode == "delete"){
                    var b = confirm("This value will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
                    if (b==false) return;
                }


                document.forms["form1"].elements["mode"].value = mode;
                document.forms["form1"].submit();
            }
            function openPriority(){
                alert("Click the checkbox, if the field has high priority. Otherwise it has low priority.");
            }
            function onLoad(){
                <%
                    if (priority != null){
                    %>
                        var pri = '<%=priority%>';
                        var o = document.forms["form1"].priority;
                        for (i=0; o!=null && i<o.options.length; i++){
                            if (o.options[i].value == pri){
                                o.selectedIndex = i;
                                break;
                            }
                        }
                    <%
                    }
                %>
            }

        // ]]>
        </script>
    </head>
<body onload="onLoad()">
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Complex attribute field"/>
        <jsp:param name="helpscreen" value="complex_attr_field"/>
    </jsp:include>
<c:set var="currentSection" value="attributes" />
<%@ include file="/pages/common/navigation.jsp" %>
<div id="workarea">
    <%
    String backURL = "" + "/m_attr_fields.jsp?attr_id=" + attr_id + "&attr_name=" + attr_name;
    %>
    <form id="form1" method="post" action="m_attr_field.jsp">

    <h1>Field of <em><%=Util.processForDisplay(attr_name)%></em> attribute</h1>

            <table class="datatable">
            <tr>
                <th scope="row" class="scope-row">
                    Field name
                </th>
                <td>
                    <%=Util.processForDisplay(name)%>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">
                    Definition
                </th>
                <td>
                    <textarea <%=disabled%> class="small" rows="5" cols="60" name="definition"><%=Util.processForDisplay(definition, true, true)%></textarea>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">
                    <!--a href="javascript:openPriority()"><span class="help">?</span></a>&nbsp;-->
                    Priority
                </th>
                <td>
                    <select <%=disabled%> name="priority" class="small">
                        <option value="<%=DElemAttribute.FIELD_PRIORITY_HIGH%>">High</option>
                        <option value="<%=DElemAttribute.FIELD_PRIORITY_LOW%>">Low</option>
                    </select>
                </td>
            </tr>
            <%
            HashSet includeFields = new HashSet();
            String harvFld = (String)attrField.get("harv_fld");
            if (harvFld!=null)
                includeFields.add(harvFld);
            Vector harvFlds = searchEngine.getHarvesterFieldsByAttr(attr_id, false, includeFields);
            if (harvFlds!=null && harvFlds.size()>0){
                %>
                <tr>
                    <th scope="row" class="scope-row">
                        Linked harvester field
                    </th>
                    <td>
                        <%

                        %>
                        <select <%=disabled%> name="harv_fld" class="small">
                            <option value="null"></option>
                            <%
                            for (int i=0; harvFlds!=null && i<harvFlds.size(); i++){
                                String s = (String)harvFlds.get(i);
                                String strSelected = harvFld!=null && harvFld.equals(s) ? "selected=\"selected\"" : "";
                                %>
                                <option <%=strSelected%> value="<%=Util.processForDisplay(s, true)%>"><%=Util.processForDisplay(s)%></option> <%
                            }
                            %>
                        </select>
                    </td>
                </tr><%
            }
            %>

    </table>
        <div>
        <%
        if (user==null){
            %>
            <input class="mediumbuttonb" type="button" value="Save" disabled="disabled"/>&nbsp;&nbsp;
            <input class="mediumbuttonb" type="button" value="Delete" disabled="disabled"/>&nbsp;&nbsp;<%
        }
        else{
            %>
            <input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&nbsp;&nbsp;
            <input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&nbsp;&nbsp;<%
        }
        %>
        </div>
        <div style="display:none">
            <input type="hidden" name="mode" value="<%=mode%>"/>
            <input type="hidden" name="field_id" value="<%=field_id%>"/>
            <input type="hidden" name="del_field" value="<%=field_id%>"/>
            <input type="hidden" name="attr_id" value="<%=attr_id%>"/>
            <input type="hidden" name="attr_name" value="<%=Util.processForDisplay(attr_name, true)%>"/>
        </div>
    </form>
</div>
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