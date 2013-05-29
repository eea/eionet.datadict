<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private Vector attrFields=null;%>

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

            request.setCharacterEncoding("UTF-8");

            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
            response.setHeader("Expires", Util.getExpiresDateString());

            DDUser user = SecurityUtil.getUser(request);


            ServletContext ctx = getServletContext();

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

            if (attr_id == null || attr_id.length()==0){ %>
                <b>Attribute ID is missing!</b> <%
                return;
            }

            String attr_name = request.getParameter("attr_name");
            if (attr_name == null) attr_name = "?";


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

                //String redirUrl = "" +
                //                    "/m_attr_fields.jsp?attr_id=" + attr_id + "&attr_name=" + attr_name;
                String redirUrl = currentUrl;
                response.sendRedirect(redirUrl);
                return;
            }

            Connection conn = null;

            try { // start the whole page try block

            conn = ConnectionUtil.getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "");

            attrFields = searchEngine.getAttrFields(attr_id);

            if (attrFields == null) attrFields = new Vector();

            String disabled = user == null ? "disabled" : "";
            %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <%@ include file="headerinfo.jsp" %>
        <title>Meta</title>
        <script type="text/javascript" src="dynamic_table.js"></script>
        <script type="text/javascript">
        // <![CDATA[
            function submitForm(mode){

                if (mode == "delete"){
                    var b = confirm("This will delete all the fields you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
                    if (b==false) return;
                }

                document.forms["form1"].elements["mode"].value = mode;
                document.forms["form1"].submit();
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
            function saveChanges(){
                tbl_obj.insertNumbers("pos_");
                submitForm("edit_pos");
            }
            function clickLink(sUrl){
                if (getChanged()==1){
                    if(!confirm("This link leads you to the next page, but you have changed the order of elements.\n Are you sure you want to loose the changes?"))
                        return;
                }
                window.location=sUrl;
            }
            // ]]>
            </script>
    </head>

<body onload="start()">
    <div id="container">
        <jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Complex attribute fields"/>
            <jsp:param name="helpscreen" value="complex_attr_fields"/>
        </jsp:include>
        <%@ include file="nmenu.jsp" %>
        <div id="workarea">
            <form id="form1" method="post" action="m_attr_fields.jsp">
                <h1>Fields of <em><%=Util.processForDisplay(attr_name)%></em></h1>
                <div>
                    <table style="width:auto">

                        <tr style="height:20px;"><td colspan="2"></td></tr>
                        <tr><td colspan="2" class="smallFont">Enter a new field here:</td></tr>
                        <tr>
                            <td class="small" align="left">Name:</td>
                            <td>
                                <input type="text" size="20" name="new_field"/>&nbsp;
                                <%
                                if (user!=null){
                                    %>
                                    <input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')"/>
                                    <%
                                }
                                else{
                                    %>
                                    <input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')" disabled="true"/>
                                    <%
                                }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td class="small" align="left">Definition:</td>
                            <td>
                                <textarea rows="2" cols="30" name="definition"></textarea>
                            </td>
                        </tr>
                        <tr style="height:10px;"><td colspan="2"></td></tr>
                    </table>
                </div>

                <%
                if (user != null) { %>
                    <div style="margin-bottom:10px">
                        <input type="button" <%=disabled%> value="Remove selected" class="smallbutton" onclick="submitForm('delete')"/>
                        <input type="button" value="Save order" class="smallbutton" onclick="saveChanges()" title="save the order of the fields"/>
                    </div><%
                }
                %>

                <div>
                    <table cellspacing="0" cellpadding="0" id="tbl">
                        <thead>
                            <tr>
                                <%
                                if (user != null){ %>
                                    <th style="background-color:#f6f6f6;">&nbsp;</th><%
                                }
                                %>
                                <th style="background-color:#f6f6f6;">Name</th>
                                <th style="padding-left:5px;background-color:#f6f6f6;">Definition</th>
                                <th style="padding-left:5px;background-color:#f6f6f6;">Priority</th>
                            </tr>
                        </thead>
                        <tbody id="tbl_body">
                            <%
                            if (user!=null && attrFields.size()>1){

                                %>
                                <tr>
                                    <td colspan="4"></td>
                                    <td rowspan="<%=attrFields.size()+1%>" class="row-movers">
                                        <a href="javascript:moveFirst()"><img src="images/move_first.gif" title="move selected row to top" alt=""/></a>
                                        <a href="javascript:moveRowUp()"><img src="images/move_up.gif" title="move selected row up" alt=""/></a>
                                        <img id="dot" src="images/dot.gif" alt=""/>
                                        <a href="javascript:moveRowDown()"><img src="images/move_down.gif" title="move selected row down" alt=""/></a>
                                        <a href="javascript:moveLast()"><img src="images/move_last.gif" title="move selected row last" alt=""/></a>
                                    </td>
                                </tr><%
                            }

                            int position = 0;

                            for (int i=0; i<attrFields.size(); i++){
                                Hashtable hash = (Hashtable)attrFields.get(i);
                                String id = (String)hash.get("id");
                                String name = (String)hash.get("name");
                                String definition = (String)hash.get("definition");
                                if (definition.length()>50) definition = definition.substring(0,50) + " ...";

                                String fieldLink = "m_attr_field.jsp?mode=edit&amp;attr_id=" + attr_id + "&amp;attr_name=" + attr_name + "&amp;field_id=" + id;

                                int pos = Integer.parseInt((String)hash.get("position"));
                                if (pos >= position) position = pos +1;

                                String priority = (String)hash.get("priority");
                                String pri = (priority!=null && priority.equals(DElemAttribute.FIELD_PRIORITY_HIGH)) ? "High" : "Low";
                                String trStyle = (i%2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
                                %>
                                <tr id="tr<%=id%>" onclick="tbl_obj.selectRow(this);" <%=trStyle%>>
                                    <%
                                    if (user != null){ %>
                                        <td align="right" style="padding-left:5px;padding-right:5px">
                                            <input type="checkbox" style="height:13;width:13" name="del_field" value="<%=id%>" onclick="tbl_obj.clickOtherObject();"/>
                                        </td><%
                                    }
                                    %>
                                    <td align="center" style="padding-left:5px;padding-right:5px">
                                        <a href="javascript:clickLink('<%=fieldLink%>')">
                                            <%=Util.processForDisplay(name)%>
                                        </a>
                                    </td>
                                    <td align="center" onmouseover="" style="padding-left:5px;padding-right:5px">
                                        <%=Util.processForDisplay(definition)%>
                                    </td>
                                    <td align="center" onmouseover="" style="padding-left:5px;padding-right:5px">
                                        <%=pri%>
                                    </td>
                                    <td style="display:none;width:0">
                                        <input type="hidden" name="pos_id" value="<%=id%>" size="5"/>
                                        <input type="hidden" name="oldpos_<%=id%>" value='<%=(String)hash.get("position")%>' size="5"/>
                                        <input type="hidden" name="pos_<%=id%>" value="0" size="5"/>
                                    </td>
                                </tr><%
                            }
                            %>
                        </tbody>
                    </table>
                </div>

                <div style="display:none">
                    <input type="hidden" name="mode" value="add"/>
                    <input type="hidden" name="position" value="<%=String.valueOf(position)%>"/>

                    <input type="hidden" name="attr_id" value="<%=attr_id%>"/>
                    <input type="hidden" name="attr_name" value="<%=Util.processForDisplay(attr_name, true)%>"/>
                    <input type="hidden" name="changed" value="0"/>
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
