<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String SUBMIT_ADD_NEW = "Add new";%>
<%!private static final String SUBMIT_REMOVE_SELECTED = "Remove selected";%>

<%!class RodLinkComparator implements Comparator {

    /**
    *
    */
    public int compare(Object o1, Object o2){

        Hashtable hash1 = (Hashtable)o1;
        Hashtable hash2 = (Hashtable)o2;
        String o1Title = (String)hash1.get("ra-title");
        String o2Title = (String)hash2.get("ra-title");
        return o1Title.compareTo(o2Title);
    }
}
%>

<%
request.setCharacterEncoding("UTF-8");

response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
response.setHeader("Expires", Util.getExpiresDateString());

Connection conn = null;
try{
    // check if the user exists
    DDUser user = SecurityUtil.getUser(request);
    if (user == null)
        throw new Exception("Not authenticated!");

    // get the dataset Identifier
    String dstIdf = request.getParameter("dst_idf");
    if (dstIdf == null || dstIdf.length()==0)
        throw new Exception("Dataset Identifier is missing!");

    // check if the user is authorised
    boolean prm = SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dstIdf, "u");
    if (!prm)
        throw new Exception("User is missing the required permissions: " + user.getUserName());

    // get dataset id
    String dstID = request.getParameter("dst_id");
    if (dstID == null || dstID.length()==0)
        throw new Exception("Dataset ID is missing!");

    // init connection
    ServletContext ctx = getServletContext();
    conn = ConnectionUtil.getConnection();

    // handle the POST
    if (request.getMethod().equals("POST")){
        String submit = request.getParameter("submit");
        if (submit!=null && submit.equals(SUBMIT_ADD_NEW))
            request.getRequestDispatcher("InServices").forward(request, response);
        else if (submit==null || (submit!=null && submit.equals(SUBMIT_REMOVE_SELECTED))){
            RodLinksHandler handler = new RodLinksHandler(conn, ctx);
            handler.execute(request);
        }
    }

    // ...
    // handle the GET
    // ...

    // get dataset name
    String dstName = request.getParameter("dst_name");
    if (dstName == null || dstName.length()==0) dstName = "?";

    DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
    Vector rodLinks = searchEngine.getRodLinks(dstID);
    if (rodLinks!=null)
        Collections.sort(rodLinks, new RodLinkComparator());
    %>

    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
        <head>
            <%@ include file="headerinfo.jsp" %>
            <title>Data Dictionary</title>
            <script type="text/javascript">
            // <![CDATA[
                function submitAdd(raID, raTitle, liID, liTitle){

                    document.forms["rodlinks"].elements["mode"].value = "add";
                    document.forms["rodlinks"].elements["ra_id"].value = raID;
                    document.forms["rodlinks"].elements["ra_title"].value = raTitle;
                    document.forms["rodlinks"].elements["li_id"].value = liID;
                    document.forms["rodlinks"].elements["li_title"].value = liTitle;
                    document.forms["rodlinks"].submit();
                }
            // ]]>
            </script>
        </head>
        <body>
            <div id="container">
            <jsp:include page="nlocation.jsp" flush="true">
                <jsp:param name="name" value="Rod links"/>
                <jsp:param name="helpscreen" value="dataset_rod"/>
            </jsp:include>
            <%@ include file="nmenu.jsp" %>
            <div id="workarea"> <!-- start work area -->
                <form id="rodlinks" action="dstrod_links.jsp" method="post">
                    <h1>ROD obligations corresponding to <a href="dataset.jsp?ds_id=<%=dstID%>"><%=Util.processForDisplay(dstName)%></a> dataset</h1>
                    <div style="float:left;margin-top:20px;">
                        <input type="submit" name="submit" value="<%=SUBMIT_ADD_NEW%>"/>
                        <%
                        if (rodLinks!=null && rodLinks.size()>0){ %>
                            <input type="submit" name="submit" value="<%=SUBMIT_REMOVE_SELECTED%>"/><%
                        }
                        %>
                    </div>
                                <table cellspacing="0" cellpadding="0" class="datatable" style="margin-top:0;width:auto;clear:both">
                                    <tr>
                                        <th>&nbsp;</th>
                                        <th style="padding-left:5px;padding-right:10px;border-left:0">Title</th>
                                        <th style="padding-left:5px;padding-right:10px;">Details</th>
                                    </tr>
                                    <%
                                    int displayed = 0;
                                    for (int i=0; rodLinks!=null && i<rodLinks.size(); i++){

                                        Hashtable rodLink = (Hashtable)rodLinks.get(i);
                                        String raID = (String)rodLink.get("ra-id");
                                        String raTitle = (String)rodLink.get("ra-title");
                                        String raDetails = (String)rodLink.get("ra-url");

                                        String colorAttr = displayed % 2 != 0 ? "bgcolor=#CCCCCC" : "";
                                        %>
                                        <tr>
                                            <td <%=colorAttr%>>
                                                <input type="checkbox" name="del_id" value="<%=raID%>"/>
                                            </td>
                                            <td style="padding-left:5;padding-right:10" <%=colorAttr%>>
                                                <%=Util.processForDisplay(raTitle)%>
                                            </td>
                                            <td style="padding-left:5;padding-right:10" <%=colorAttr%>>
                                                <a href="<%=Util.processForDisplay(raDetails, true)%>"><%=Util.processForDisplay(raDetails, true)%></a>
                                            </td>
                                        </tr>
                                        <%
                                        displayed++;
                                    }
                                    %>
                                </table>
                                <div style="display:none">
                                    <input type="hidden" name="mode" value="rmv"/>
                                    <input type="hidden" name="dst_id" value="<%=dstID%>"/>
                                    <input type="hidden" name="dst_idf" value="<%=Util.processForDisplay(dstIdf, true)%>"/>
                                    <input type="hidden" name="dst_name" value="<%=Util.processForDisplay(dstName, true)%>"/>

                                    <input type="hidden" name="ra_id" value=""/>
                                    <input type="hidden" name="ra_title" value=""/>
                                    <input type="hidden" name="li_id" value=""/>
                                    <input type="hidden" name="li_title" value=""/>

                                    <input type="hidden" name="client" value="webrod"/>
                                    <input type="hidden" name="method" value="get_activities"/>
                                </div>
                            </form>
                        </div> <!-- workarea -->
                        </div> <!-- container -->
                        <%@ include file="footer.txt" %>
        </body>
    </html>

    <%
}
catch (Exception e){

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(bytesOut));
    String trace = bytesOut.toString(response.getCharacterEncoding());

    request.setAttribute("DD_ERR_MSG", e.getMessage());
    request.setAttribute("DD_ERR_TRC", trace);

    /*String qryStr = request.getQueryString();
    qryStr = qryStr==null ? "" : "?" + qryStr;
    request.setAttribute("DD_ERR_BACK_LINK", request.getRequestURL().append(qryStr).toString());*/

    request.getRequestDispatcher("error.jsp").forward(request, response);
}
finally {
    try { if (conn!=null) conn.close(); } catch (SQLException e) {}
}
%>
