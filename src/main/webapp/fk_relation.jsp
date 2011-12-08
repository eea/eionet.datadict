<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

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

String relID = request.getParameter("rel_id");
if (relID == null || relID.length()==0) {%>
    <b>FK relation ID not specified!</b><%
    return;
}

String mode = request.getParameter("mode");
if (mode==null || mode.trim().length()==0){
    mode = "view";
}

if (request.getMethod().equals("POST")){
    
    Connection userConn = null;
    String id = null;
    
    try{
        userConn = user.getConnection();
        FKHandler handler = new FKHandler(userConn, request, ctx);
        handler.execute();
    }
    catch (Exception e){
        
        String msg = e.getMessage();
        
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();                            
        e.printStackTrace(new PrintStream(bytesOut));
        String trace = bytesOut.toString(response.getCharacterEncoding());
        
        String backLink = history.getBackUrl();
        
        request.setAttribute("DD_ERR_MSG", msg);
        request.setAttribute("DD_ERR_TRC", trace);
        request.setAttribute("DD_ERR_BACK_LINK", backLink);
        
        request.getRequestDispatcher("error.jsp").forward(request, response);
    }
    finally{
        try { if (userConn!=null) userConn.close();
        } catch (SQLException e) {}
    }
    
    if (mode.equals("delete"))
        response.sendRedirect(history.getBackUrl());
    else
        response.sendRedirect(currentUrl);
    
    return;
}

Connection conn = null;

try { // start the whole page try block

conn = ConnectionUtil.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

Hashtable fkRel = searchEngine.getFKRelation(relID);

String disabled = user == null ? "disabled" : "";

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
        // <![CDATA[
    
        function submitForm(mode){
            
            if (mode == "delete"){
                var b = confirm("This relation will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
                if (b==false) return;
            }
            
            document.forms["form1"].elements["mode"].value = mode;
            document.forms["form1"].submit();
        }
        
        // ]]>
    </script>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Foreign key relation"/>
        <jsp:param name="helpscreen" value="foreign_key_rel"/>
    </jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
    <form id="form1" method="post" action="fk_relation.jsp">
  <h1>Foreign key relation</h1> 
    <p>
        This is the foreign key relation between elements
        <em><%=Util.processForDisplay((String)fkRel.get("a_name"))%></em> and
        <em><%=Util.processForDisplay((String)fkRel.get("b_name"))%></em>.<br/>
        The relation is direction-less, so it doesn't matter which
        exactly is A or B.
    </p>
                
            
            <table cellspacing="0" cellpadding="0" class="datatable">
            
            <tr style="height:10"><td colspan="2">&nbsp;</td></tr>
            
            <tr>
                <th scope="row">Element A</th>
                <td><em><%=Util.processForDisplay((String)fkRel.get("a_name"))%></em></td>
            </tr>
            
            <tr>
                <th scope="row">Element B</th>
                <td><em><%=Util.processForDisplay((String)fkRel.get("b_name"))%></em></td>
            </tr>
            
            <tr>
                <th scope="row">Cardinality (A to B)</th>
                <td>
                    <%
                    Vector cardins = new Vector();
                    cardins.add("0");
                    cardins.add("1");
                    cardins.add("+");
                    cardins.add("*");
                    %>
                    <select name="a_cardin">
                        <%
                        String aCardin = (String)fkRel.get("a_cardin");
                        for (int i=0; i<cardins.size(); i++){
                            String cardin = (String)cardins.get(i);
                            String selected = aCardin.equals(cardin) ? "selected=\"selected\"" : ""; %>
                            <option <%=selected%> value="<%=Util.processForDisplay(cardin, true)%>"><%=Util.processForDisplay(cardin)%></option><%
                        }
                        %>
                    </select>&nbsp;to&nbsp;
                    <select name="b_cardin">
                        <%
                        String bCardin = (String)fkRel.get("b_cardin");
                        for (int i=0; i<cardins.size(); i++){
                            String cardin = (String)cardins.get(i);
                            String selected = bCardin.equals(cardin) ? "selected=\"selected\"" : ""; %>
                            <option <%=selected%> value="<%=Util.processForDisplay(cardin, true)%>"><%=Util.processForDisplay(cardin)%></option><%
                        }
                        %>
                    </select>&nbsp;
                    <a href="help.jsp?screen=foreign_key_rel&amp;area=cardinality" onclick="pop(this.href);return false;">
                        <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Get help on this item"/>
                    </a>
                </td>
            </tr>
            
            <tr>    
                <th scope="row">Description</th>
                <td>
                    <textarea <%=disabled%>
                              class="small"
                              rows="3" cols="52"
                              name="definition"><%=Util.processForDisplay((String)fkRel.get("definition"), true, true)%></textarea>
                </td>
            </tr>
            
            <tr style="height:20"><td colspan="2"></td></tr>
            
            <tr>
                <td></td>
                <td>
                    <input type="button" <%=disabled%> class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&nbsp;&nbsp;
                    <input type="button" <%=disabled%> class="mediumbuttonb" value="Delete" onclick="submitForm('delete')"/>
                </td>
            </tr>
    </table>
    <div style="display:none">
        <input type="hidden" name="rel_id" value="<%=relID%>"/>
        <input type="hidden" name="mode" value="<%=mode%>"/>
    </div>    
    </form>
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
