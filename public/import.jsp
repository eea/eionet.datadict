<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.meta.schema.*"%>
<%!
private DDuser getUser(HttpServletRequest req) {
	
	DDuser user = null;
    
    HttpSession httpSession = req.getSession(false);
    if (httpSession != null) {
    	user = (DDuser)httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
	}
      
    if (user != null)
    	return user.isAuthentic() ? user : null;
	else 
    	return null;
}
%>
<%
	DDuser user = getUser(request);
%>
<html>
<head>
	<title>Data Dictionary</title>
	<META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="JavaScript" src='script.js'></script>
	<SCRIPT LANGUAGE="JavaScript">

	function submitForm(){

		var radio
		var o;

		for (var i=0; i<document.forms["Upload"].elements.length; i++){
			o = document.forms["Upload"].elements[i];
			if (o.name == "fileORurl")
				if (o.checked == true){
					radio = o.value;
					//break;

				}
			if (o.name == "type")
				if (o.checked == true){
					type = o.value;
					//break;
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
			document.forms["Upload"].action = document.forms["Upload"].action + "?fileORurl=" + radio + "&url_input=" + url + "&type=" + type;
			//alert(document.forms["Upload"].action);
			document.forms["Upload"].submit();
		}
	}
	</SCRIPT>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0"">
<%@ include file="header.htm"%>

<table border="0">
    <tr valign="top">
		<td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Import"/>
            </jsp:include>
            
            	<div style="margin-left:30">
            
				<table>
					<tr>
						<td colspan="3"><br></br><font class="head00">Import data</font><br></br>
							
							This is a function that enables you to import new datasets and data elements as XML files. <br/>
							Please define <!--data type at first and then the file location, where--> location of the file at first.<br/>
							It is possible to import the file from yout local machine or from the specified URL location.
						</td>
				</tr>
				</table>
				<FORM NAME="Upload" ACTION="Import" METHOD="POST" ENCTYPE="multipart/form-data">

				<table width="auto" cellspacing="0">
					<tr>
						<td align="left" colspan="2">
							<input type="radio" name="type" value="DST" checked="true"/>&#160;<span class="smallfont">DATASETS</span>
						</td>
					</tr>
					<tr height="10"><td colspan="2"></td></tr>
					<tr>
						<td align="left" style="padding-right:5">
							<input type="radio" name="fileORurl" value="file" checked="true"></input>&#160;File:</td>
						<td align="left">
							<input type="file" class="smalltext" name="file_input" size="40"/>
						</td>
					</tr>
					<tr>
						<td align="left" style="padding-right:5">
							<input type="radio" class="smalltext" name="fileORurl" value="url"></input>&#160;URL:
						</td>
						<td align="left">
							<input type="text" class="smalltext" name="url_input" size="52"></input>
						</td>
					</tr>
					<tr height="10"><td colspan="2"></td></tr>
					<tr>
						<td></td>
						<td align="left">
							<% if (user!=null){ %>									
								<input name="SUBMIT" type="button" class="mediumbuttonb" value="Import" onclick="submitForm()" onkeypress="submitForm()"></input>&#160;&#160;
							<%}%>
								<input name="RESET" type="reset" class="mediumbuttonb" value="Clear"></input>
						</td>
					</tr>
				</table>
			</FORM>	
		</TD>
	</tr>
</table>
</div>
</center>
</body>
</html>