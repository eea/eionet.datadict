<%@page contentType="text/html;charset=UTF-8" import="eionet.meta.*,eionet.util.Util,java.sql.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

<%
    request.setCharacterEncoding("UTF-8");
    
    DDUser user = SecurityUtil.getUser(request);
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.txt" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[

    function submitForm(mode){
        
        if (mode == 'remove'){
            var trailer = "?mode=remove&ds_id=" + document.forms["Upload"].elements["ds_id"].value;
            var oVisual = document.forms["Upload"].elements["visual"];
            if (oVisual != null)
                trailer = trailer + "&visual=" + oVisual.value;
            var oStrType = document.forms["Upload"].elements["str_type"];
            if (oStrType != null)
                trailer = trailer + "&str_type=" + oStrType.value;
            document.forms["Upload"].action = document.forms["Upload"].action + trailer;
            document.forms["Upload"].submit();
            return;
        }

        var radio
        var o;

        for (var i=0; i<document.forms["Upload"].elements.length; i++){
            o = document.forms["Upload"].elements[i];
            if (o.name == "fileORurl"){
                if (o.checked == true){
                    radio = o.value;
                    //break;

                }
            }
        }
        
        var url = document.forms["Upload"].elements["url_input"].value;
        var file = document.forms["Upload"].elements["file_input"].value;
        var ok = true;

        if (radio == "url"){
            if (url == ""){
                alert("URL is not specified, there is nothing to import!");
                ok = false;
            }
        }
        if (radio == "file"){
            if (file == ""){
                alert("File location is not specified, there is nothing to import!");
                ok = false;
            }
        }

        if (ok == true){
            var trailer = "?fileORurl=" + radio + "&url_input=" + url + "&file_input=" + file;
            trailer = trailer + "&ds_id=" + document.forms["Upload"].elements["ds_id"].value;
            var oVisual = document.forms["Upload"].elements["visual"];
            if (oVisual != null)
                trailer = trailer + "&visual=" + oVisual.value;
            var oStrType = document.forms["Upload"].elements["str_type"];
            if (oStrType != null)
                trailer = trailer + "&str_type=" + oStrType.value;
            document.forms["Upload"].action = document.forms["Upload"].action + trailer;
            //alert(document.forms["Upload"].action);
            document.forms["Upload"].submit();
        }
    }
    
    function openStructure(url){
        window.open(url,null,"height=600,width=800,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=yes");
    }
    // ]]>
    </script>
</head>

<%
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
response.setHeader("Expires", Util.getExpiresDateString());

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

String ds_id = request.getParameter("ds_id");
if (ds_id == null || ds_id.length()==0){ %>
    <b>Dataset ID is missing!</b> <%
    return;
}

String type = request.getParameter("str_type");
if (type==null || type.length()==0)
    type = "simple";

Connection conn = null;

try { // start the whole page try block

conn = ConnectionUtil.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

Dataset dataset = searchEngine.getDataset(ds_id);

boolean imgVisual = false;
String dsVisual = type.equals("simple") ? dataset.getVisual() : dataset.getDetailedVisual();
if (dsVisual!=null && dsVisual.length()!=0){
    int i = dsVisual.lastIndexOf(".");
    if (i != -1){
        String visualType = dsVisual.substring(i+1, dsVisual.length()).toUpperCase();
        if (visualType.equals("GIF") || visualType.equals("JPG") || visualType.equals("JPEG") || visualType.equals("PNG"))
            imgVisual = true;
    }
}
            
%>

<body>
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
    <jsp:param name="name" value="Dataset Model"/>
    <jsp:param name="helpscreen" value="dataset_model"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
    <h1>
        Data model of
        <a href="dataset.jsp?ds_id=<%=ds_id%>"><%=Util.processForDisplay(dataset.getShortName())%></a>
        dataset
    </h1>
    <div id="tabbedmenu">
    <ul>
        <% if (type.equals("simple")){ %>
            <li id="currenttab"><span>Simple</span></li>
            <li><a href="dsvisual.jsp?ds_id=<%=ds_id%>&amp;str_type=detailed">Detailed</a></li>
        <% } else { %>
            <li><a href="dsvisual.jsp?ds_id=<%=ds_id%>&amp;str_type=simple">Simple</a></li>
            <li id="currenttab"><span>Detailed</span></li>
        <% } %>
        </ul>
    </div>
    <br style="clear:left" />

                <table width="500">
                    
                    <%
                    if (dsVisual==null){
                        %>
                        <tr>
                            <td>
                            This dataset has no <%=type%> model uploaded. You can do it below.
                            The structure can be represented as any type of file. If it's an
                            image file (GIF, JPEG or PNG), it will be automatically displayed here.
                            Otherwise a link to the uploaded file is displayed.
                            </td>
                        </tr>
                        <%
                    }
                    
                    if (dsVisual!=null){
                        %>
                        <tr>
                            <td colspan="2">
                            <% if (imgVisual){ %>
                                <img src="visuals/<%=Util.processForDisplay(dsVisual, true)%>" alt=""/> <%
                            }
                            else{ %>
                                The file representing the dataset <%=type%> structure cannot be displayed on this web-page.
                                But you can see it by pressing the following link:<br/>
                                <a href="javascript:openStructure('visuals/<%=Util.processForDisplay(dsVisual, true)%>')"><%=Util.processForDisplay(dsVisual)%></a> <%
                            } %>
                            </td>
                        </tr>
                        
                        <%
                        if (user!=null){
                            %>
                            <tr style="height:5px;"><td colspan="2"></td></tr>
                            <tr><td colspan="2" style="border-top:1px solid #008B8B;">&nbsp;</td></tr>
                            
                            <tr>
                                <td colspan="2">
                                    You can replace this structure by uploading a new one below.
                                    The structure can be represented as any type of file. If it's an
                                    image file (GIF, JPEG or PNG), it will be automatically displayed here.
                                    Otherwise a link to the uploaded file is displayed.
                                    If you want to set no <%=type%> model at all, press 'Remove'.
                                </td>
                            </tr>
                            <%
                        }
                    }
                    %>
                </table>

                <%
                if (user!=null){
                    %>
                    <form id="Upload" action="DsVisualUpload" method="post" enctype="multipart/form-data">

                        <table width="auto" cellspacing="0">
                            
                            <tr>
                                <td align="left" style="padding-right:5">
                                    <input type="radio" name="fileORurl" value="file" checked="checked"/>&nbsp;File:</td>
                                <td align="left">
                                    <input type="file" class="smalltext" name="file_input" size="40"/>
                                </td>
                            </tr>
                            <tr>
                                <td align="left" style="padding-right:5">
                                    <input type="radio" class="smalltext" name="fileORurl" value="url"/>&nbsp;URL:
                                </td>
                                <td align="left">
                                    <input type="text" class="smalltext" name="url_input" size="52"/>
                                </td>
                            </tr>
                            <tr style="height:10px;"><td colspan="2"></td></tr>
                            <tr>
                                <td></td>
                                <td align="left">
                                    <input name="SUBMIT" type="button" class="mediumbuttonb" value="Upload" onclick="submitForm('upload')" onkeypress="submitForm('upload')"/>&nbsp;&nbsp;
                                    <input name="REMOVE" type="button" class="mediumbuttonb" value="Remove" onclick="submitForm('remove')" onkeypress="submitForm('remove')"/>
                                </td>
                            </tr>
                        </table>
                        <div style="display:none">
                            <input type="hidden" name="ds_id" value="<%=ds_id%>"/>
                            <%
                            if (dsVisual != null && dsVisual.length()!=0){
                                %>
                                <input type="hidden" name="visual" value="<%=Util.processForDisplay(dsVisual, true)%>"/>
                                <%
                            }
                            %>
                            
                            <input type="hidden" name="str_type" value="<%=type%>"/>
                        </div>
                    </form>
                    <%
                }
                %>
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
