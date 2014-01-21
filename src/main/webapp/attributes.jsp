<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!static int iPageLen=0;%>

<%@ include file="history.jsp" %>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
    ServletContext ctx = getServletContext();

    Connection conn = null;

    try { // start the whole page try block

    conn = ConnectionUtil.getConnection();

    DDUser user = SecurityUtil.getUser(request);
    if (user==null || !user.isAuthentic()){ %>
        <b>Not allowed!</b><%
        return;
    }

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

        Connection userConn = null;

        try{
            userConn = user.getConnection();
            AttributeHandler handler = new AttributeHandler(userConn, request, ctx, "delete");
            handler.setUser(user);

            handler.execute();

            String redirUrl = request.getParameter("searchUrl");
            if (redirUrl != null && redirUrl.length()!=0){
                ctx.log("redir= " + redirUrl);
                response.sendRedirect(redirUrl);
            }
        }
        finally{
            try { if (userConn!=null) userConn.close();
            } catch (SQLException e) {}
        }
    }

    DDSearchEngine searchEngine = new DDSearchEngine(conn, "");

    Vector attributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
    Vector complexAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_COMPLEX, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
    for (int i=0; complexAttributes!=null && i<complexAttributes.size(); i++)
        attributes.add(complexAttributes.get(i));


    int iCurrPage=0;
    try {
        iCurrPage=Integer.parseInt(request.getParameter("page_number"));
    }
    catch(Exception e){
        iCurrPage=0;
    }
    if (iCurrPage<0)
        iCurrPage=0;

    String mode = request.getParameter("mode");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
  <title>Data Dictionary - Attributes</title>
  <script type="text/javascript">
  // <![CDATA[
        function setLocation(){
            var o = document.forms["form1"].searchUrl;
            if (o!=null)
                o.value=document.location.href;
        }

  // ]]>
  </script>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Attributes"/>
        <jsp:param name="helpscreen" value="attributes"/>
    </jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
            <%

            if (attributes == null || attributes.size()==0){
                %>
                <div class="error-msg">No attributes were found!</div>
            </div></div>
              <%@ include file="footer.jsp" %>
            </body></html>
                <%
                return;
            }
            %>

        <h1>Attributes</h1>

            <%
            if (user != null && mode==null){
                boolean addPrm = SecurityUtil.hasPerm(user.getUserName(), "/attributes", "i");
                if (addPrm){
                    %>
                    <div id="drop-operations">
                    <h2>Operations:</h2>
                        <ul>
                            <li><a href="delem_attribute.jsp?mode=add">Add attribute</a></li>
                        </ul>
                    </div>
                    <%
                }
            }
            %>

            <form id="form1" method="post" action="attributes.jsp">
        <p>
            This is a list of all definition attributes used in Data Dictionary.
            Every attribute is uniquely identifed by its short name. Click page help
            and question marks in column headers to to find out more.
            To view <% if (user != null && mode==null){ %> or modify <%}%> an attribute's
            definition, click its short name.
            <% if (false && user != null && mode==null){ %>
                To add a new attribute, click the 'Add' button on top of the list.
                The left-most column enables you to delete selected attributes.
            <%}%>
        </p>

        <table class="datatable">
            <tr>
                <th scope="col" class="scope-col">
                                Short name
                </th>
                <th scope="col" class="scope-col">
                                Type
                </th>
                <th scope="col" class="scope-col">
                                Datasets
                </th>
                <th scope="col" class="scope-col">
                                Tables
                </th>
                <th scope="col" class="scope-col">
                                Data elements<br/>with fixed values
                </th>
                <th scope="col" class="scope-col">
                                Data elements<br/>with quantitative values
                </th>
                <th scope="col" class="scope-col">
                                Schemas
                </th>
                <th scope="col" class="scope-col">
                                Schema sets
                </th>
                <th scope="col" class="scope-col">
                                Vocabulary folders
                </th>
            </tr>

            <%
            // show all
            if (iPageLen==0)
                iPageLen = attributes.size();

            int iBeginNode=iCurrPage*iPageLen;
            int iEndNode=(iCurrPage+1)*iPageLen;
            if (iEndNode>=attributes.size())
                iEndNode=attributes.size();
            for (int i=0; i<attributes.size(); i++){

                DElemAttribute attribute = (DElemAttribute)attributes.get(i);

                String attr_id = attribute.getID();
                String attr_name = attribute.getShortName();
                if (attr_name == null) attr_name = "unknown";
                if (attr_name.length() == 0) attr_name = "empty";
                String attr_oblig = attribute.getObligation();

                String attrType = attribute.getType();

                String displayOblig = "Mandatory";
                if (attr_oblig.equals("M")){
                    displayOblig = "Mandatory";
                }
                else if (attr_oblig.equals("O")){
                    displayOblig = "Optional";
                }
                else if (attr_oblig.equals("C")){
                    displayOblig = "Conditional";
                }

                String attrTypeDisp = "Simple";
                %>

                <tr <% if (i % 2 != 0) %> class="zebradark" <%;%>>
                    <%
                    if (attrType.equals(DElemAttribute.TYPE_COMPLEX))
                        attrTypeDisp = "Complex";
                    %>
                    <td>
                        <a href="delem_attribute.jsp?attr_id=<%=attr_id%>&amp;type=<%=attrType%>">
                        <%=Util.processForDisplay(attr_name)%></a>
                    </td>
                    <td><%=Util.processForDisplay(attrTypeDisp)%></td>
                    <td class="center">
                        <% if (attribute.displayFor("DST")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
                    </td>
                    <td class="center">
                        <% if (attribute.displayFor("TBL")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
                    </td>
                    <td class="center">
                        <!--  CH1 and CH3 attributes are same -->
                        <% if (attribute.displayFor("CH1") || attribute.displayFor("CH3")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
                    </td>
                    <td class="center">
                        <% if (attribute.displayFor("CH2")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
                    </td>
                    <td class="center">
                        <% if (attribute.displayFor(DElemAttribute.ParentType.SCHEMA.toString())){ %><img src="images/ok.gif" alt="Yes"/><%}%>
                    </td>
                    <td class="center">
                        <% if (attribute.displayFor(DElemAttribute.ParentType.SCHEMA_SET.toString())){ %><img src="images/ok.gif" alt="Yes"/><%}%>
                    </td>
                    <td class="center">
                        <% if (attribute.displayFor(DElemAttribute.ParentType.VOCABULARY_FOLDER.toString())){ %><img src="images/ok.gif" alt="Yes"/><%}%>
                    </td>
                </tr>

                <%
            }
            %>

        </table>

        <fieldset style="display:hidden">
            <input type="hidden" name="searchUrl" value=""/>
        </fieldset>
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
