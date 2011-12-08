<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil,java.net.URL,java.net.URLEncoder,java.net.MalformedURLException"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private Vector complexAttrs=null;%>

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
    
    // POST request not allowed for anybody who hasn't logged in            
    if (request.getMethod().equals("POST") && user==null){
        request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    // get some vital request parameters
    String parent_id = request.getParameter("parent_id");
    if (parent_id == null || parent_id.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: parent_id");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
    
    String parent_type = request.getParameter("parent_type");
    if (parent_type == null || parent_type.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: parent_type");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
    String parent_name = request.getParameter("parent_name");
    String parent_ns = request.getParameter("parent_ns");
    String ds = request.getParameter("ds");
    
    // for getting inherited attributes
    String dataset_id = request.getParameter("dataset_id");
    if (dataset_id == null) dataset_id = "";
    String table_id = request.getParameter("table_id");
    if (table_id == null) table_id = "";
    
    // handle POST request
    if (request.getMethod().equals("POST")){
        Connection userConn = null;                
        try{
            userConn = user.getConnection();
            AttrFieldsHandler handler = new AttrFieldsHandler(userConn, request, ctx);            
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
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        }
        finally{
            try { if (userConn!=null) userConn.close();
            } catch (SQLException e) {}
        }
        // dispatch the POST request
        String redirUrl = "complex_attrs.jsp?parent_id=" + parent_id +
                                                     "&parent_type=" + parent_type +
                                                     "&parent_name=" + parent_name +
                                                     "&parent_ns=" + parent_ns +
                                                     "&table_id=" + table_id +
                                                     "&dataset_id=" + dataset_id;
        response.sendRedirect(redirUrl);
        return;
    }
    //// end of handle the POST request, all following code deals with GET //////////////////////
    
    Connection conn = null;
    
    // the whole page's try block
    try {    
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
        Vector mComplexAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
        if (mComplexAttrs == null)
            mComplexAttrs = new Vector();
        
        complexAttrs = searchEngine.getComplexAttributes(parent_id, parent_type, null, table_id, dataset_id);
        if (complexAttrs == null)
            complexAttrs = new Vector();
        
        for (int i=0; mComplexAttrs.size()!=0 && i<complexAttrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
            String attrID = attr.getID();
            for (int j=0; j<mComplexAttrs.size(); j++){
                DElemAttribute mAttr = (DElemAttribute)mComplexAttrs.get(j);
                String mAttrID = mAttr.getID();
                if (attrID.equals(mAttrID)){
                    mComplexAttrs.remove(j);
                    j--;
                }
            }
        }        
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Complex attributes</title>
    <script type="text/javascript">
    // <![CDATA[
            function submitForm(mode){
                
                if (mode == "delete"){
                    var b = confirm("This will delete all the attributes you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
                    if (b==false) return;    
                }
                
                document.forms["form1"].elements["mode"].value = mode;
                document.forms["form1"].submit();
            }
            
            <% String redirUrl = ""; %>
            
            function addNew(){
                var id = document.forms["form1"].elements["new_attr_id"].value;
                var url = "<%=redirUrl%>" + "complex_attr.jsp?mode=add&attr_id=" + id + 
                            "&parent_id=<%=parent_id%>&parent_type=<%=parent_type%>&parent_name=<%=parent_name%>&parent_ns=<%=parent_ns%>&table_id=<%=table_id%>&dataset_id=<%=dataset_id%>";
                
                <%
                if (ds!=null && ds.equals("true")){
                    %>
                    url = url + "&ds=true";
                    <%
                }
                %>
                
                window.location.replace(url);
            }
            
            function edit(id){
                var url = "<%=redirUrl%>" + "complex_attr.jsp?mode=edit&attr_id=" + id + 
                            "&parent_id=<%=parent_id%>&parent_type=<%=parent_type%>&parent_name=<%=parent_name%>&parent_ns=<%=parent_ns%>&table_id=<%=table_id%>&dataset_id=<%=dataset_id%>";
                <%
                if (ds!=null && ds.equals("true")){
                    %>
                    url = url + "&ds=true";
                    <%
                }
                %>
                
                window.location.replace(url);
            }
            
    // ]]>
    </script>
</head>
<body>

<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Complex attributes"/>
        <jsp:param name="helpscreen" value="complex_attrs"/>
    </jsp:include>
<%@ include file="nmenu.jsp" %>

<div id="workarea">

<%
StringBuffer parentLink = new StringBuffer();
String dispParentType = request.getParameter("parent_type");
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

String dispParentName = request.getParameter("parent_name");
if (dispParentName==null)
    dispParentName = "";

if (parentLink.length()>0)
    parentLink.append(request.getParameter("parent_id"));
        
%>

<h1>Complex attributes of <a href="<%=parentLink%>"><%=dispParentName%></a> <%=dispParentType%></h1>
<%
if (complexAttrs==null || complexAttrs.size() == 0){
    %>
    <p>None found!</p><%
}
%>

<div style="clear:right;padding-top:10px">
<form id="form1" method="post" action="complex_attrs.jsp">
    <%
    if (mComplexAttrs!=null && mComplexAttrs.size()>0){
        %>
        <div>
            <select class="small" name="new_attr_id">
                <%
                for (int i=0; i<mComplexAttrs.size(); i++){
                    DElemAttribute attr = (DElemAttribute)mComplexAttrs.get(i);
                    String attrID = attr.getID();
                    String attrName = attr.getShortName();
                    
                    String attrOblig = attr.getObligation();
                    String obligStr  = "(O)";
                    if (attrOblig.equalsIgnoreCase("M"))
                        obligStr = "(M)";
                    else if (attrOblig.equalsIgnoreCase("C"))
                        obligStr = "(C)";
                    %>
                    <option value="<%=attrID%>"><%=Util.processForDisplay(attrName)%>&nbsp;&nbsp;&nbsp;<%=Util.processForDisplay(obligStr)%></option><%
                }
                %>
            </select>
            <input class="smallbutton" type="button" value="Add new" onclick="addNew()"/>
            <input class="smallbutton" type="button" value="Remove selected" onclick="submitForm('delete')"/>
        </div><%
    }
    %>


<%
for (int i=0; i<complexAttrs.size(); i++){ // loop over attributes
        
        DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
        String attrID = attr.getID();
        String attrName = attr.getShortName();
        boolean inherit = attr.getInheritable().equals("1") ? true:false;
        
        Vector attrFields = searchEngine.getAttrFields(attrID);
        
        String attrOblig = attr.getObligation();
        String obligStr  = "optional";
        if (attrOblig.equalsIgnoreCase("M"))
            obligStr = "mandatory";
        else if (attrOblig.equalsIgnoreCase("C"))
            obligStr = "conditional";
        
        String obligImg = obligStr + ".gif";
        
        String inherited = null;
        Vector rows = attr.getRows();
        for (int j=0; rows!=null && j<rows.size(); j++){
            Hashtable rowHash = (Hashtable)rows.get(j);
            inherited = (String)rowHash.get("inherited");
            if (inherited!=null){
                if (inherited.equals("DS"))
                    inherited = "(inherited from dataset)";
                else if (inherited.equals("DT"))
                    inherited = "(inherited from table)";
                else
                    inherited = null;
                break;
            }
        }

        %>
        <div style="overflow:auto">
        <table cellspacing="0" class="datatable">
            <tr>
                <td align="right" valign="middle">
                    <%
                    if (inherited==null){%>
                        <input type="checkbox" style="height:13;width:13" name="del_attr" value="<%=attrID%>"/><%
                    }
                    else{ %>
                        &nbsp;<%
                    }
                    %>
                </td>
                <td valign="middle">
                    <b>&nbsp;<%=Util.processForDisplay(attrName)%></b>&nbsp;&nbsp;&nbsp;
                    <%
                    if (inherited==null){%>
                        <img src="images/<%=Util.processForDisplay(obligImg, true)%>" width="16" height="16" alt="<%=Util.processForDisplay(obligStr, true)%>" style="border:0"/><%
                    }
                    else{ %>
                        <%=inherited%><%
                    }
                    %>
                </td>
            </tr>
            <tr>
                <td valign="top" style="padding-right:3;padding-top:3;">
                    <%
                    if (inherited==null){%>
                        <input class="smallbutton" type="button" value="Edit" onclick="edit('<%=attrID%>')"/><%
                    }
                    else{ %>
                        &nbsp;<%
                    }
                    %>
                </td>                
                <td style="padding-left:3;padding-top:3">
                    <table cellspacing="0">
                        <tr>
                        <%                        
                        for (int t=0; attrFields!=null && t<attrFields.size(); t++){
                            Hashtable hash = (Hashtable)attrFields.get(t);
                            String name = (String)hash.get("name");
                            String style = "padding-right:10px";
                            %>
                            <th align="left" class="small"><%=Util.processForDisplay(name)%></th>
                            <%
                        }
                        %>
                        </tr>
                        
                        <%
                        for (int j=0; rows!=null && j<rows.size(); j++){
                            Hashtable rowHash = (Hashtable)rows.get(j);
                            %>
                            <tr>
                            <%
                            
                            for (int t=0; t<attrFields.size(); t++){
                                Hashtable hash = (Hashtable)attrFields.get(t);
                                String fieldID = (String)hash.get("id");
                                String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
                                if (fieldValue == null) fieldValue = " ";
                                fieldValue = Util.processForDisplay(fieldValue);                            
                                %>
                                <td class="small" style="padding-right:10;background-color:#D3D3D3;" <% if (j % 2 != 0) %> <%;%>><%=fieldValue%></td>
                                <%
                            }
                            %>
                            </tr>                
                            <%
                        }
                        %>
                    </table>
                </td>
            </tr>
        </table>
        </div>
        <%
    }
%>

<div style="display:none">
    <input type="hidden" name="mode" value="delete"/>
    <input type="hidden" name="parent_id" value="<%=parent_id%>"/>
    <input type="hidden" name="parent_name" value="<%=Util.processForDisplay(parent_name, true)%>"/>
    <input type="hidden" name="parent_type" value="<%=parent_type%>"/>
    <input type="hidden" name="parent_ns" value="<%=parent_ns%>"/>
    <input type="hidden" name="table_id" value="<%=table_id%>"/>
    <input type="hidden" name="dataset_id" value="<%=dataset_id%>"/>
    <%
    if (ds != null){
        %>
        <input type="hidden" name="ds" value="<%=ds%>"/>
        <%
    }
    %>
</div>
</form>
</div>
</div> <!-- workarea -->
</div> <!-- container -->
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
