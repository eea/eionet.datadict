<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%!private Vector objects=null;%>
<%!
ServletContext ctx = null;
%>

<%@ include file="history.jsp" %>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    DDUser user = SecurityUtil.getUser(request);

    String attr_id = request.getParameter("attr_id");
    String type = request.getParameter("type");
    String short_name = request.getParameter("short_name");

    if (request.getMethod().equals("POST")){
        if (user == null){
            %><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"><body><h1>Error</h1><b>Not authorized to post any data!</b></body></html><%
              return;
          }
        Connection userConn = null;
        String redirUrl = "";

        try{
            userConn = user.getConnection();

            AttributeHandler handler = new AttributeHandler(userConn, request, ctx);
            handler.setUser(user);
            handler.execute();

            String    deleteUrl = history.gotoLastMatching("attributes.jsp");
            redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
        }
        finally{
            try { if (userConn!=null) userConn.close();
            } catch (SQLException e) {}
        }

        response.sendRedirect(redirUrl);
        return;
    }

    if (attr_id==null || type==null){
        %><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"><body><h1>Error</h1><b>Attribute type or attribute id is not specified!</b></body></html><%
        return;
    }

    ctx = getServletContext();

    Connection conn = null;
    int attrUseCount = 0;
    try { // start the whole page try block

        conn = ConnectionUtil.getConnection();

        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");

        objects = null;//searchEngine.getAttributeObjects(attr_id, type);
        attrUseCount = searchEngine.getAttributeUseCount(attr_id, type);
        if (attrUseCount<=0)
            throw new Exception("Attribute not use at all, so this page should not have been reached");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[
        function deleteAttr(){
            document.forms["form1"].submit();
        }

        function cancel(){
            window.history.back();
        }
    // ]]>
    </script>

</head>
<body>
<div id="container">
        <jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Attribute"/>
        </jsp:include>
<%@ include file="nmenu.jsp" %>

<div id="workarea">
    <form id="form1" action="dialog_delete_attr.jsp" method="post">
    <h1>Deleting attribute</h1>
    <div class="attention" style="padding-top:20px">Are you sure you want to delete attribute "<%=Util.processForDisplay(short_name)%>"?<br/>It is used in <%=attrUseCount%> definitions.</div>
                <table width="500">
                    <%
                    // DATASETS
                    int d=0;
                    if (objects!=null){
                        %>
                        <%
                        for (int i=0; i<objects.size(); i++){

                            Hashtable object = (Hashtable)objects.get(i);

                            String parent_type =(String)object.get("parent_type");
                            String parent_id =(String)object.get("parent_id");
                            String parent_name =(String)object.get("parent_name");

                            String type_name="";
                            String link="";
                            String version="";
                            if (parent_type.equals("DS")){
                                type_name = "Dataset";
                                link = request.getContextPath() + "/datasets/" + parent_id;
                            }
                            else if (parent_type.equals("T")){
                                type_name = "Table";
                                link = request.getContextPath() + "/tables/" + parent_id;
                            }
                            else if (parent_type.equals("CSI")){
                                type_name="Allowable value";
                                String comp_id = (String)object.get("component_id");
                                String comp_type = (String)object.get("component_type");
                                link= request.getContextPath() + "/fixedvalues/" + comp_type + "/" + comp_id ;
                            }
                            else if (parent_type.equals("E")){
                                type_name="Data element";
                                link = request.getContextPath() + "/dataelements/" + parent_id;
                            }
                            if (!parent_type.equals("CSI")){
                                version = "version: " + (String)object.get("version");
                            }
                            d++;
                            %>

                            <tr valign="top">
                                <td align="left" style="padding-left:5;padding-right:10" <% if (d % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
                                    <%=type_name%>:&nbsp;
                                    <a href="<%=link%>">
                                    <%=Util.processForDisplay(parent_name)%></a>&nbsp;<%=version%>
                                </td>
                            </tr>
                        <%
                        }
                    }
                    %>

                    <tr><td align="left">&nbsp;</td></tr>
                    <tr style="height:30px;"><td align="left">
                        <input type="button" onclick="cancel()" value="Cancel" class="mediumbuttonb"/>
                        <input type="button" onclick="deleteAttr()" value="Delete" class="mediumbuttonb"/>
                    </td></tr>
                </table>

                    <div style="display:none">
                        <input type="hidden" name="mode" value="delete"/>
                        <input type="hidden" name="type" value="<%=type%>"/>
                        <%
                        if (type!=null && type.equals(DElemAttribute.TYPE_SIMPLE)){
                            %>
                            <input type="hidden" name="simple_attr_id" value="<%=attr_id%>"/>
                            <%
                        }
                        else{
                            %>
                            <input type="hidden" name="complex_attr_id" value="<%=attr_id%>"/>
                            <%
                        }
                        %>
                        <input type="hidden" name="attr_id" value="<%=attr_id%>"/>
                    </div>
                </form>
</div> <!-- workarea -->
</div> <!-- container -->
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
