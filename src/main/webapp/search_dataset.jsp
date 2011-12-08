<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.util.sql.ConnectionUtil,eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>


<%!

private Vector attrs = null;
private Vector def_attrs = null;
private Vector attr_ids = null;
ServletContext ctx = null;
private String sel_attr = null;
private Hashtable inputAttributes=null;

                
private String getAttributeIdByName(String name){
    
    for (int i=0; i<attrs.size(); i++){
        DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getName().equalsIgnoreCase(name))
            return attr.getID();
    }
        
    return null;
}

private String getAttributeNameById(String id){
    
    for (int i=0; i<attrs.size(); i++){
        DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getID().equals(id))
            return attr.getShortName();
    }
        
    return null;
}

private String setDefaultAttrs(String name){

    String id = getAttributeIdByName(name);
    if (id!=null)
        def_attrs.add(id);

    return null;
}

%>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
    
    DDUser user = SecurityUtil.getUser(request);
    
    ctx = getServletContext();
    
    Connection conn = null;
    
    try { // start the whole page try block
    
    conn = ConnectionUtil.getConnection();

    DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

    attrs = searchEngine.getDElemAttributes();
    if (attrs == null) attrs = new Vector();
    
    attr_ids = new Vector();
    def_attrs = new Vector();

    setDefaultAttrs("Name");
    setDefaultAttrs("Definition");
    setDefaultAttrs("Keywords");
    setDefaultAttrs("EEAissue");

    String attrID = null;
    String attrValue = null;
    String attrName = null;
    StringBuffer collect_attrs = new StringBuffer();
    HashSet displayedCriteria = new HashSet();

    String sel_attr = request.getParameter("sel_attr");
    String sel_type = request.getParameter("sel_type");
    String short_name = request.getParameter("short_name");
    String idfier = request.getParameter("idfier");
    String search_precision = request.getParameter("search_precision");
    
    
    if (sel_attr == null) sel_attr="";
    if (sel_type == null) sel_type="";
    if (short_name == null) short_name="";
    if (idfier == null) idfier="";
    if (search_precision == null) search_precision="substr";

    ///get inserted attributes
    String input_attr;
    inputAttributes = new Hashtable();
    for (int i=0; i<attrs.size(); i++){    
        DElemAttribute attribute = (DElemAttribute)attrs.get(i);        
        String attr_id = attribute.getID();
        
        input_attr = request.getParameter("attr_" + attr_id);
        if (input_attr!=null){
            inputAttributes.put(attr_id, input_attr);
            attr_ids.add(attr_id);
        }
    }

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
        // <![CDATA[
        attrWindow=null;

        function submitForm(action){
            
            document.forms["form1"].action=action;
            document.forms["form1"].submit();
        }

        function selAttr(id, type){
            document.forms["form1"].sel_attr.value=id;
            document.forms["form1"].sel_type.value=type;
            submitForm('search_dataset.jsp');

        }
        function onLoad(){
            <%
                if (search_precision != null){
                %>
                    var sPrecision = '<%=search_precision%>';
                    var o = document.forms["form1"].search_precision;
                    for (i=0; o!=null && i<o.length; i++){
                        if (o[i].value == sPrecision){
                            o[i].checked = true;
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
    <jsp:param name="name" value="Search datasets"/>
    <jsp:param name="helpscreen" value="search_dataset"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
    
        <%
        if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){
            %>
            <div id="drop-operations">
            <h2>Operations:</h2>
                <ul>
                    <li><a title="Create a new dataset" href="dataset.jsp?mode=add">Add dataset</a></li>
                </ul>
            </div><%
        }
        %>
        
        <form id="form1" action="datasets.jsp" method="get">
        <h1>Search datasets</h1>
        <table width="600" cellspacing="0" style="padding-top:10px">
            <col style="width: 14em"/>
            <col style="width: 16px"/>
            <col span="2"/>
            <tr>
                <td align="right">
                    <label for="reg_status" class="question">Registration Status</label>
                </td>
                <td>
                    <a href="help.jsp?screen=dataset&amp;area=regstatus" onclick="pop(this.href);return false;">
                        <img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
                    </a>
                </td>
                <td colspan="2">
                    <select name="reg_status" id="reg_status" class="small">
                        <option value="">All</option>
                        <option value="Released">Released</option>
                        <option value="Recorded">Recorded</option>
                        <option value="Qualified">Qualified</option>
                        <option value="Candidate">Candidate</option>
                        <option value="Incomplete">Incomplete</option>
                    </select>
                </td>
            </tr>
                                
            <tr style="vertical-align:top">
                <td align="right">
                    <label for="short_name" class="question">Short name</label>
                </td>
                <td>
                    <a href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
                        <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                    </a>
                </td>
                <td colspan="2">
                    <input type="text" class="smalltext" size="59" name="short_name" id="short_name" value="<%=Util.processForDisplay(short_name, true)%>"/>
                </td>
            </tr>
            
            <tr style="vertical-align:top">
                <td align="right">
                    <label class="question">Identifier</label>
                </td>
                <td>
                    <a href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                        <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                    </a>
                </td>
                <td colspan="2">
                    <input type="text" class="smalltext" size="59" name="idfier" value="<%=idfier%>"/>
                </td>
            </tr>

            <%
            //get default attributes, which are always on the page (defined above)
            if (def_attrs!=null){
                for (int i=0; i < def_attrs.size(); i++){
                    attrID = (String)def_attrs.get(i);
                    attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
                    
                    attrName = getAttributeNameById(attrID);

                    if (inputAttributes.containsKey(attrID)) inputAttributes.remove(attrID);

                    if (attrID!=null){
                        collect_attrs.append(attrID + "|");
                        displayedCriteria.add(attrID);
                        %>
                        <tr style="vertical-align:top">
                            <td align="right">
                                <label class="question"><%=Util.processForDisplay(attrName)%></label>
                            </td>
                            <td>
                                <a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                                </a>
                            </td>
                            <td colspan="2">
                                <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=Util.processForDisplay(attrValue, true)%>"/>
                            </td>
                        </tr>
                        <%
                    }
                }
            }
            // get attributes selected from picked list (get the ids from url)
            if (attr_ids!=null){
                for (int i=0; i < attr_ids.size(); i++){
                    attrID = (String)attr_ids.get(i);
                     
                    if (!inputAttributes.containsKey(attrID)) continue;
                    if (sel_type.equals("remove") && attrID.equals(sel_attr)) continue;

                    attrName = getAttributeNameById(attrID);

                    attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
                    if (attrValue == null) attrValue="";
                    collect_attrs.append(attrID + "|");
                    displayedCriteria.add(attrID);
                    %>
                    <tr style="vertical-align:top">
                        <td align="right">
                            <label class="question"><%=Util.processForDisplay(attrName)%></label>
                        </td>
                        <td>
                            <a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                                <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                            </a>
                        </td>
                        <td>
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59" value="<%=Util.processForDisplay(attrValue, true)%>"/>
                        </td>
                        <td>
                            <a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" style="border:0" alt="Remove attribute from search criterias"/></a>
                        </td>
                    </tr>
                    <%
                }
            }
            // add the last selection
            if (sel_type!=null && sel_attr!=null){
                if (sel_type.equals("add")){
                    attrID = sel_attr;
                    collect_attrs.append(attrID + "|");
                    displayedCriteria.add(attrID);
                    attrName = getAttributeNameById(attrID);
                    %>
                    <tr style="vertical-align:top">
                        <td align="right">
                            <label class="question"><%=Util.processForDisplay(attrName)%></label>
                        </td>
                        <td>
                            <a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                                <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                            </a>
                        </td>
                        <td>
                            <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59" value=""/>
                        </td>
                        <td>
                            <a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" style="border:0" alt="Remove attribute from search criterias"/></a>
                        </td>
                    </tr>
                    <%
                }
            }
            %>
                <tr>
                    <td colspan="2">&nbsp;</td>
                    <td colspan="2">
                        <input type="radio" name="search_precision" id="ssubstr" value="substr" checked="checked"/><label for="ssubstr">Substring search</label>
                        <input type="radio" name="search_precision" id="sexact" value="exact"/><label for="sexact">Exact search</label>
                    </td>
                </tr>
            
            <%                    
            // if authenticated user, enable to get working copies only
            if (user!=null && user.isAuthentic()){
                %>
                <tr style="vertical-align:top">
                    <td colspan="2"></td>
                    <td colspan="2">
                        <input type="checkbox" name="wrk_copies" id="wrk_copies" value="true"/>
                        <label for="wrk_copies" class="smallfont">Working copies only</label>
                    </td>
                </tr>
                <%
            }
            %>
            <tr style="vertical-align:top">
                <td colspan="2"></td>
                <td colspan="2">
                    <input type="checkbox" name="incl_histver" id="incl_histver" value="true"/>
                    <label for="incl_histver" class="smallfont">Include historic versions</label>
                </td>
            </tr>
            <tr style="vertical-align:top">
                <td colspan="2"></td>
                <td>
                    <input class="mediumbuttonb" type="button" value="Search" onclick="submitForm('datasets.jsp')"/>
                    <input class="mediumbuttonb" type="reset" value="Reset"/>
                </td>
            </tr>
            <%
            Vector addCriteria = new Vector();
            for (int i=0; attrs!=null && i<attrs.size(); i++){
                
                DElemAttribute attribute = (DElemAttribute)attrs.get(i);
                if (!attribute.displayFor("DST"))
                    continue;
            
                if (!displayedCriteria.contains(attribute.getID())){
                    Hashtable hash = new Hashtable();
                    hash.put("id", attribute.getID());
                    hash.put("name", attribute.getShortName());
                    addCriteria.add(hash);
                }
            }
            
            if (addCriteria.size()>0){
                %>
                <tr>
                    <td colspan="4" style="text-align:right">
                        <label for="add_criteria">Add criteria</label>
                        <select name="add_criteria" id="add_criteria" onchange="selAttr(this.options[this.selectedIndex].value, 'add')">
                            <option value=""></option>
                            <%
                            for (int i=0; i<addCriteria.size(); i++){
                                Hashtable hash = (Hashtable)addCriteria.get(i);
                                %>
                                <option value="<%=hash.get("id")%>"><%=hash.get("name")%></option><%
                            }
                            %>
                        </select>
                    </td>
                </tr><%
            }
            %>
        </table>
        <!-- table for 'Add' -->
        
            <div style="display:none">
                <input type="hidden" name="sel_attr" value=""/>            
                <input type="hidden" name="sel_type" value=""/>
                <input type="hidden" name="type" value="DST"/>
                <!-- collect all the attributes already used in criterias -->
                <input type="hidden" name="collect_attrs" value="<%=Util.processForDisplay(collect_attrs.toString(), true)%>"/>
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
