<%@page contentType="text/html;charset=UTF-8" import="java.util.*, eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%

request.setCharacterEncoding("UTF-8");

String objID = request.getParameter("obj_id");
String objType = request.getParameter("obj_type");
String idf = (String)request.getAttribute("identifier");
Vector entries = (Vector)request.getAttribute("entries");
%>

<html>
<head>
    <title>Data Dictionary</title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
    <link type="text/css" rel="stylesheet" href="eionet_new.css" />
    <script type="text/javascript">
    
    	function submitForm(action){
	    	document.forms["form1"].elements["action"].value = action;
	    	document.forms["form1"].submit();
    	}
    	
    	function load(){
	    	resize();
    	}
    	
    	function resize(){
	    	window.resizeTo(600, 450);
    	}
    	
    </script>
</head>
<body class="popup" onload="load()">
<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>
<div>

<form name="form1" action="GetCache" method="post">
	<table width="500" cellpadding="1" cellspacing="0">
	
		<tr>
			<td colspan="3" class="head0">
				Cache for <%=request.getAttribute("object_type")%> <font color="006666"><%=idf%></font>
			</td>
		</tr>
	
		<tr>
			<td colspan="3">
				<input type="button" class="smallbutton" value="Update selected" onclick="submitForm('update')"/>
				<input type="button" class="smallbutton" value="Remove selected" onclick="submitForm('clear')"/>
			</td>
		</tr>
		
		<tr>
			<th width="3%">&nbsp;</th>
			<th width="67%" align="left" style="padding-left:10; border-left:0">Article</th>
			<th width="30%" align="left" style="padding-left:10; border-right:1px solid #FF9900">Created</th>
		</tr>
			
		<%
		for (int i=0; i<entries.size(); i++){
			Hashtable hash = (Hashtable)entries.get(i);
			String article = (String)hash.get("article");
			String text = (String)hash.get("text");
			Long created = (Long)hash.get("created");
			String date = created==null ? "-- not in cache --" : Util.historyDate(created.longValue());
			%>
			<tr>
				<td width="3%">
					<input type="checkbox" name="article" value="<%=Util.replaceTags(article, true)%>"/>
				</td>
				<td width="67%" style="padding-left:10">
					<%=Util.replaceTags(text)%>
				</td>
				<td width="30%" style="padding-left:10">
					<%=date%>
				</td>
			</tr>
			<%
		}
		%>
	</table>
	
	<input type="hidden" name="action" value="update"/>
	<input type="hidden" name="obj_id" value="<%=objID%>"/>
	<input type="hidden" name="obj_type" value="<%=objType%>"/>
	<input type="hidden" name="idf" value="<%=idf%>"/>	
</form>
</div>
</body>
</html>
