<%@page contentType="text/html" import="eionet.meta.*,java.sql.*,com.tee.xmlserver.*"%>

<%@ include file="history.jsp" %>

<%
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
%>

<html>
<head>
	<title>Data Dictionary</title>
	<META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="JavaScript" src='script.js'></script>
	<SCRIPT LANGUAGE="JavaScript">

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
	</SCRIPT>
</head>

<%

ServletContext ctx = getServletContext();			
String appName = ctx.getInitParameter("application-name");

if (request.getMethod().equals("POST")){
	if (user == null){
		%>
			<html>
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
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
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
                <jsp:param name="name" value="Dataset model"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
            	<div style="margin-left:30">
            
				<table width="500">
					<tr>
						<td colspan="3"><br></br>
							<font class="head00">
								Data model of
								<font class="title2" color="#006666"><%=dataset.getShortName()%></font>
								Dataset
							</font>
						</td>
					</tr>
					
					<tr height="5"><td colspan="2"></td></tr>
				</table>
				
				<table width="500" cellpadding="0" cellspacing="0" border="0">
					
					<%
					if (type.equals("simple")){ %>
						<tr height="10">
					        <td bgcolor="#10847B" valign="top" align="left" width="5"><img src="../images/ltop.gif" width="5" height="5"></td>
						    <th width="50">Simple</th>
						    <td bgcolor="#10847B" valign="top" align="right" width="5"><img src="../images/rtop.gif" width="5" height="5"></td>
						    <td bgcolor="#f0f0f0" width="1"></td>
	          				<td bgcolor="#20B2AA" valign="top" align="left" width="5"><img src="../images/ltop.gif" width="5" height="5"></td>
						    <th class="light" width="50"><a href="dsvisual.jsp?ds_id=<%=ds_id%>&str_type=detailed">
						    	<font color="#FFFFFF">Detailed</font></a>
						    </th>
						    <td bgcolor="#20B2AA" valign="top" align="right" width="5"><img src="../images/rtop.gif" width="5" height="5"></td>
	    					<td bgcolor="#f0f0f0">&#160;</td>
						</tr> <%
					}
					else{ %>
						<tr height="10">
					        <td bgcolor="#20B2AA" valign="top" align="left" width="5"><img src="../images/ltop.gif" width="5" height="5"></td>
						    <th class="light" width="50"><a href="dsvisual.jsp?ds_id=<%=ds_id%>&str_type=simple">
						    	<font color="#FFFFFF">Simple</font></a>
						    </th>
						    <td bgcolor="#20B2AA" valign="top" align="right" width="5"><img src="../images/rtop.gif" width="5" height="5"></td>
						    <td bgcolor="#f0f0f0" width="1"></td>
	          				<td bgcolor="#10847B" valign="top" align="left" width="5"><img src="../images/ltop.gif" width="5" height="5"></td>
						    <th width="50">Detailed</th>
						    <td bgcolor="#10847B" valign="top" align="right" width="5"><img src="../images/rtop.gif" width="5" height="5"></td>
	    					<td bgcolor="#f0f0f0">&#160;</td>
						</tr> <%
					}
					%>					
					<tr>
						<td colspan="8" style="border-top-color:#10847B;border-top-style:solid;border-top-width:1pt;">&#160;</td>
					</tr>
				</table>
				
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
								<img src="../visuals/<%=dsVisual%>"/> <%
							}
							else{ %>
								The file representing the dataset <%=type%> structure cannot be displayed on this web-page.
								But you can see it by pressing the following link:<br/>
								<a href="javascript:openStructure('../visuals/<%=dsVisual%>')"><%=dsVisual%></a> <%
							} %>
							</td>
						</tr>
						
						<%
						if (user!=null){
							%>
							<tr height="5"><td colspan="2"></td></tr>
							<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
							
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
					<FORM NAME="Upload" ACTION="DsVisualUpload" METHOD="POST" ENCTYPE="multipart/form-data">

						<table width="auto" cellspacing="0">
							
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
									<input name="SUBMIT" type="button" class="mediumbuttonb" value="Upload" onclick="submitForm('upload')" onkeypress="submitForm('upload')"></input>&#160;&#160;
									<input name="REMOVE" type="button" class="mediumbuttonb" value="Remove" onclick="submitForm('remove')" onkeypress="submitForm('remove')"></input>
								</td>
							</tr>
						</table>
						
						<input type="hidden" name="ds_id" value="<%=ds_id%>"/>
						<%
						if (dsVisual != null && dsVisual.length()!=0){
							%>
							<input type="hidden" name="visual" value="<%=dsVisual%>"/>
							<%
						}
						%>
						
						<input type="hidden" name="str_type" value="<%=type%>"/>
					</FORM>
					<%
				}
				%>
		</TD>
	</tr>
</table>
</div>
</center>
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