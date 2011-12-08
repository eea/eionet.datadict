<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.meta.exports.schema.*,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
response.setHeader("Expires", Util.getExpiresDateString());

request.setCharacterEncoding("UTF-8");

String dstID = request.getParameter("ds_id");
if (dstID==null || dstID.length()==0) throw new ServletException("Dataset ID is missing!");
String idf = request.getParameter("idf");
if (idf==null || idf.length()==0) throw new ServletException("Dataset Identifier is missing!");
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[
    
        function submitForm(){
            
            var f = document.forms["form1"].elements["file"].value;
            if (f==null || f.length==0){
                alert("You must provide at least the file!");
                return;
            }
            
            qryStr = "ds_id=<%=dstID%>&idf=<%=idf%>&title=" + document.forms["form1"].elements["title"].value + "&file=" + f;
            document.forms["form1"].action = document.forms["form1"].action + "?" + qryStr;
            document.forms["form1"].submit();
        }
    // ]]>
    </script>
</head>


<body>

<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Upload document"/>
        <jsp:param name="helpscreen" value="doc_upload"/>
    </jsp:include>
<%@ include file="nmenu.jsp" %>

<div id="workarea">

<h1>Upload document</h1>
<p>
This is a function enabling you to upload documents relevant to the given dataset.
Data Dictionary recognises the following document types: <strong>doc, rtf, xls, ppt, mdb, zip, txt, html</strong>.
However, you can upload any other types of files as well. Their type will simply be displayed as
unknown. But whatever the type of the file you upload, it can later be downloaded.
</p>
                
<form id="form1" action="DocUpload" method="post" enctype="multipart/form-data">

    <table width="660">
        <tr>
            <td><label for="filefld" class="question required">Location</label></td>
            <td>
                <input type="file" name="file" id="filefld" size="40"/>
            </td>
        </tr>
        <tr>
            <td><label for="titlefld" class="question">Title</label></td>
            <td>
                <input type="text" name="title" id="titlefld" size="40"/><span style="font-size:0.7em">(if left empty, file name will serve as title)</span>
            </td>
        </tr>
        <tr style="height:10px;"><td colspan="2"></td></tr>
        <tr>
            <td align="left" colspan="2">
                <input type="button" class="mediumbuttonb" value="Upload" onclick="submitForm()"/>&nbsp;&nbsp;
                <input type="reset"  class="mediumbuttonb" value="Clear"/>
            </td>
        </tr>
    </table>
    
</form>    
</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.jsp" %>

</body>
</html>
