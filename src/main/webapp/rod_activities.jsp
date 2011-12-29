<%@page contentType="text/html;charset=UTF-8" import="java.util.*,eionet.meta.inservices.*,eionet.util.Props,eionet.util.PropsIF,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!class ActivityComparator implements Comparator {
    
    /**
    *
    */
    public int compare(Object o1, Object o2){
        
        Hashtable hash1 = (Hashtable)o1;
        Hashtable hash2 = (Hashtable)o2;
        String o1Title = (String)hash1.get("TITLE");
        String o2Title = (String)hash2.get("TITLE");
        return o1Title.compareTo(o2Title);
    }
}
%>

<%
///////////////////////////////////////////////
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
response.setHeader("Expires", Util.getExpiresDateString());

request.setCharacterEncoding("UTF-8");
Vector activities = (Vector)session.getAttribute(Attrs.ROD_ACTIVITIES);
if (activities!=null)
    Collections.sort(activities, new ActivityComparator());

String dstID = request.getParameter("dst_id");
if (dstID == null || dstID.length()==0)
    throw new Exception("Dataset ID is missing!");

String dstIdf = request.getParameter("dst_idf");
if (dstIdf == null || dstIdf.length()==0)
    throw new Exception("Dataset Identifier is missing!");

String dstName = request.getParameter("dst_name");
if (dstName == null || dstName.length()==0)
    throw new Exception("Dataset name is missing!");

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[
    
        function submitAdd(raID, raTitle, liID, liTitle){
            document.forms["link"].elements["mode"].value = "add";
            document.forms["link"].elements["ra_id"].value = raID;
            document.forms["link"].elements["ra_title"].value = raTitle;
            document.forms["link"].elements["li_id"].value = liID;
            document.forms["link"].elements["li_title"].value = liTitle;
            document.forms["link"].submit();
        }
        
    // ]]>
    </script>
</head>

<body>
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
    <jsp:param name="name" value="Rod links"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
    <form id="reload" action="InServices?client=webrod&amp;method=reload_activities" method="get">
        <div style="display:none">
            <input type="hidden" name="client" value="webrod"/>
            <input type="hidden" name="method" value="reload_activities"/>
            
            <input type="hidden" name="dst_id" value="<%=dstID%>"/>
            <input type="hidden" name="dst_idf" value="<%=dstIdf%>"/>
            <input type="hidden" name="dst_name" value="<%=dstName%>"/>
        </div>    
    </form>
    <div id="drop-operations">
        <h2>Operations:</h2>
        <ul>
            <li><a href="javascript:document.forms['reload'].submit();">Reload</a></li>
        </ul>
    </div>
    <%
    if (activities==null || activities.size()==0){
        %>
        <h1>Failed to receive the list of obligations from ROD!</h1><%
    }
    else{
        if (dstID!=null && dstName!=null){
            %>
            <h1>You are linking ROD obligations with <a href="<%=request.getContextPath()%>/datasets/<%=dstID%>"><%=dstName%></a> dataset</h1><%
        }
        else{
            %>        
            <h1>You are linking ROD obligations with dataset</h1><%
        }
        %>
        <div style="font-size:0.7em;clear:right;margin-bottom:10px;margin-top:10px">
            Below is the list of obligations in ROD.<br/>
            Click Title to link obligation with the dataset.<br/>
            Click Details to open the obligation's details in ROD.
        </div>
        <table class="datatable" cellspacing="0" cellpadding="0" style="width:auto">
            <tr>
                <th style="padding-left:5px;padding-right:10px">Title</th>
                <th style="padding-left:5px;padding-right:10px">Details</th>
            </tr>
            <%
            int displayed = 0;
            for (int i=0; i<activities.size(); i++){
                
                Hashtable activity = (Hashtable)activities.get(i);
                
                String raID = (String)activity.get("PK_RA_ID");
                if (raID==null || raID.length()==0) continue;
                
                String terminated = (String)activity.get("terminated");
                if (terminated!=null && terminated.equals("1")) continue;
                
                String raTitle = (String)activity.get("TITLE");
                raTitle = raTitle==null ? "?" : raTitle;
                
                String liID = (String)activity.get("PK_SOURCE_ID");
                liID = liID==null ? "0" : liID;
                
                String liTitle = (String)activity.get("TITLE");
                liTitle = liTitle==null ? "?" : liTitle;
                
                String raURL = Props.getProperty(PropsIF.INSERV_ROD_RA_URLPATTERN);
                int j = raURL.indexOf(PropsIF.INSERV_ROD_RA_IDPATTERN);
                if (j==-1) throw new Exception("Invalid property " + PropsIF.INSERV_ROD_RA_URLPATTERN);
                raURL = new StringBuffer(raURL).
                replace(j, j + PropsIF.INSERV_ROD_RA_IDPATTERN.length(), raID).toString();
                
                String colorStyle = displayed % 2 != 0 ? ";background-color:#E6E6E6;" : "";
                %>
                <tr>
                    <td style="padding-left:5px;padding-right:10px<%=colorStyle%>">
                        <a href="javascript:submitAdd('<%=raID%>', '<%=Util.processForDisplay(raTitle)%>', '<%=liID%>', '<%=Util.processForDisplay(liTitle)%>')"><%=Util.processForDisplay(raTitle)%></a>
                    </td>
                    <td style="padding-left:5px;padding-right:10px<%=colorStyle%>">
                        <a href="<%=Util.processForDisplay(raURL,true)%>"><%=Util.processForDisplay(raURL,true)%></a>
                    </td>
                </tr>
                <%
                displayed++;
            }
            %>
        </table><%
    }
    %>
    <form name="link" method="post" action="dstrod_links.jsp">
        <div style="display:none">
            <input type="hidden" name="mode" value="rmv"/>
            
            <input type="hidden" name="dst_id" value="<%=dstID%>"/>
            <input type="hidden" name="dst_idf" value="<%=dstIdf%>"/>
            <input type="hidden" name="dst_name" value="<%=dstName%>"/>
            
            <input type="hidden" name="ra_id" value=""/>
            <input type="hidden" name="ra_title" value=""/>
            <input type="hidden" name="li_id" value=""/>
            <input type="hidden" name="li_title" value=""/>
            
        </div>
    </form>
</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.jsp" %>
</body>
</html>
