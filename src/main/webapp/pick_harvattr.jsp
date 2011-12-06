<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.sql.ConnectionUtil,eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
    
    DDUser user = SecurityUtil.getUser(request);
    
    ServletContext ctx = getServletContext();            
    
    String attr_id = request.getParameter("attr_id");
    if (attr_id == null || attr_id.length()==0) { %>
        <b>Attribute id paramater is missing!</b> <%
        return;
    }
    
    String parent_id = request.getParameter("parent_id");
    if (parent_id == null || parent_id.length()==0){ %>
        <b>Parent ID is missing!</b> <%
        return;
    }
    
    String parent_type = request.getParameter("parent_type");
    if (parent_type == null || parent_type.length()==0){ %>
        <b>Parent type is missing!</b> <%
        return;
    }
    
    String parentName = request.getParameter("parent_name");
    String attrName = request.getParameter("attrName");
    
    String position = request.getParameter("position");
    if (position == null || position.length()==0)
        position = "0";
    
    String requesterRedirUrl = request.getParameter("requester_redir_url");
    String requesterQrystr = request.getParameter("requester_qrystr");
    if (request.getMethod().equals("POST") && requesterQrystr==null){
        
        if (user==null || !user.isAuthentic()){
            %>
                  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
                  <body>
                      <h1>Error</h1><b>Not authorized to post any data!</b>
                  </body>
                  </html>
              <%
              return;
        }
        
        Connection userConn = null;                
        try{
            userConn = user.getConnection();
            
            AttrFieldsHandler handler = new AttrFieldsHandler(userConn, request, ctx);
            
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
        
        if (requesterRedirUrl!=null)
            response.sendRedirect(requesterRedirUrl);
    }

    Connection conn = null;

    try { // start the whole page try block
        
    conn = ConnectionUtil.getConnection();
    DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
    
    Vector harvAttrs = searchEngine.getHarvestedAttrs(attr_id);
    Vector harvFields = (harvAttrs!=null && harvAttrs.size()>0) ?
                        searchEngine.getHarvesterFieldsByAttr(attr_id) :
                        null;
    
    String colsp = (harvFields==null || harvFields.size()==0) ? "1" : String.valueOf(harvFields.size());
    HashSet added = searchEngine.getHarvestedAttrIDs(attr_id, parent_id, parent_type);
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <%@ include file="headerinfo.txt" %>
        <title>Meta</title>
        <script type="text/javascript">
        // <![CDATA[

            function closeme(){
                window.close();
            }
            
            function selected(id){
                
                    var elems = document.forms["form1"].elements;
                    var i;
                    for (i=0; i<elems.length; i++){
                        var name = elems[i].name;
                        if (name == "chk"){
                            if (elems[i].value!=id){
                                elems[i].checked=false;
                            }
                        }
                    }
                    
                    document.forms["form1"].elements["harv_attr_id"].value = id;
                    document.forms["form1"].submit();
            }
        // ]]>
        </script>
    </head>

<body>

<div id="container">

    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Pick attribute value"/>
    </jsp:include>
    <%@ include file="nmenu.jsp" %>
        
    <div id="workarea" style="clear:right">
    <%
    if (harvAttrs==null || harvAttrs.size()==0){ %>
        <h5>Nothing harvested for this attribute!</h5><%
    }
    else if (parent_type!=null && parentName!=null && attrName!=null){
        
        StringBuffer parentLink = new StringBuffer();
        String dispParentType = parent_type;
        if (dispParentType==null)
            dispParentType = "";
        else if (dispParentType.equals("DS")){
            dispParentType = "dataset";
            parentLink.append("dataset.jsp?ds_id=");
        }
        else if (dispParentType.equals("T")){
            dispParentType = "table";
            parentLink.append("dstable.jsp?table_id=");
        }
        else if (dispParentType.equals("E")){            
            dispParentType = "element";
            parentLink.append("data_element.jsp?delem_id=");
        }
        
        String dispParentName = parentName;
        if (dispParentName==null)
            dispParentName = "";
        
        if (parentLink.length()>0)
            parentLink.append(parent_id).append("&amp;mode=edit");
        
        %>
        <h2>You are selecting a harvested value for <a href="complex_attr.jsp?<%=Util.processForDisplay(requesterQrystr, true, true)%>"><%=attrName%></a> of <a href="<%=parentLink%>"><%=dispParentName%></a> <%=dispParentType%></h2><%
    }
    %>
    <h5>Select one of the harvested attribute values below.</h5>
    <form id="form1" action="pick_harvattr.jsp" method="post">
    <div style="overflow-y:auto">
    <table class="datatable">
        <%                
        if (harvFields!=null && harvFields.size()>0){
            %>
            <tr>
                <th>&nbsp;</th>
                <%
                for (int i=0; i<harvFields.size(); i++){ %>
                    <th align="left" style="padding-right:10">&nbsp;<%=(String)harvFields.get(i)%></th><%
                }
                %>
            </tr><%
        }
        
        int displayed = 0;
        for (int i=0; harvAttrs!=null && i<harvAttrs.size(); i++){
            Hashtable attrHash = (Hashtable)harvAttrs.get(i);
            String harvAttrID = (String)attrHash.get("harv_attr_id");
            if (added.contains(harvAttrID))
                continue;
            String trStyle = (i%2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
            %>
            <tr <%=trStyle%>>
                <td valign="top" style="padding-right:10">
                        <input type="checkbox"
                               name="chk"
                               value="<%=harvAttrID%>"
                               onclick="selected(this.value)"/>
                </td>
                <%
                for (int j=0; harvFields!=null && j<harvFields.size(); j++){
                    String field = (String)harvFields.get(j);
                    %>
                    <td style="padding-right:10">
                        &nbsp;<%=(String)attrHash.get(field)%>
                    </td><%
                }
                %>
            </tr><%
            
            displayed++;
        }
        %>        
    </table>
    </div>
    <%
    if (displayed==0 && !(harvAttrs==null || harvAttrs.size()==0)){ %>
        <p>(all have been already selected)</p><%
    }
    %>
    
    <div style="display:none">
        <input type="hidden" name="parent_id" value="<%=parent_id%>"/>
        <input type="hidden" name="parent_type" value="<%=parent_type%>"/>
        <input type="hidden" name="position" value="<%=position%>"/>
        
        <input type="hidden" name="attr_id" value="<%=attr_id%>"/>
        <input type="hidden" name="harv_attr_id" value=""/>
        
        <input type="hidden" name="mode" value="add"/>
        <input type="hidden" name="requester_redir_url" value="<%=Util.processForDisplay(requesterRedirUrl, true, true)%>"/>
    </div>    
    </form>
    </div>
    </div>
    <%@ include file="footer.txt" %>
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
