<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.meta.exports.schema.*,com.tee.xmlserver.*"%>

<%
String dstID = request.getParameter("ds_id");
if (dstID==null || dstID.length()==0) throw new ServletException("Dataset ID is missing!");
String idf = request.getParameter("idf");
if (idf==null || idf.length()==0) throw new ServletException("Dataset Identifier is missing!");
%>
<html>
<head>
	<title>Data Dictionary</title>
	<META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="JavaScript" src='script.js'></script>
	<script LANGUAGE="JavaScript">
	
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
		
	</script>
</head>


<body class="popup">
		
<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
</div>

<br/>
<p class="head00">Upload document</p>
<p>
This is a function enabling you to upload documents relevant to the given dataset.
Data Dictionary recognizes the following document types: <b>doc, rtf, xls, ppt, zip, txt, html</b>.
However, you can upload any other types of files as well. Data Dictionary will simply display
their type as unknown. Whatever is the type of the file you upload, it can later be downloaded.
</p>
				
<form name="form1" action="DocUpload" method="POST" enctype="multipart/form-data">

	<table width="500" cellspacing="0">
		<tr>
			<td align="left">
				<input type="file" name="file" size="40"/>
			</td>
		</tr>
		<tr>
			<td align="left">
				<input type="text" class="smalltext" name="title" size="40"/>
			</td>
		</tr>
		<tr height="10"><td></td></tr>
		<tr>
			<td align="left">
				<input type="button" class="mediumbuttonb" value="Upload" onclick="submitForm()"/>&#160;&#160;
				<input type="reset"  class="mediumbuttonb" value="Clear"/>
			</td>
		</tr>
	</table>
	
</form>	
</body>
</html>
