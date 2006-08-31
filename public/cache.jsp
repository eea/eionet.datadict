<%@page contentType="text/html;charset=UTF-8" import="java.util.*, eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%

request.setCharacterEncoding("UTF-8");

String objID = request.getParameter("obj_id");
String objType = request.getParameter("obj_type");
String idf = (String)request.getAttribute("identifier");
Vector entries = (Vector)request.getAttribute("entries");
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
		<%@ include file="headerinfo.txt" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
// <![CDATA[
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
// ]]>
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

<form name="form1" action="GetCache" method="post">
	
		<h1>
				Cache for <%=request.getAttribute("object_type")%> <em><%=idf%></em>
		</h1>
	
		<p>
				<input type="button" class="smallbutton" value="Update selected" onclick="submitForm('update')"/>
				<input type="button" class="smallbutton" value="Remove selected" onclick="submitForm('clear')"/>
		</p>
		
	<table width="500" class="datatable">
		<col style="width:3%"/>
		<col style="width:67%"/>
		<col style="width:30%"/>
		<thead>
		<tr>
			<th>&nbsp;</th>
			<th>Article</th>
			<th>Created</th>
		</tr>
		</thead>
		<tbody>
			
		<%
		for (int i=0; i<entries.size(); i++){
			Hashtable hash = (Hashtable)entries.get(i);
			String article = (String)hash.get("article");
			String text = (String)hash.get("text");
			Long created = (Long)hash.get("created");
			String date = created==null ? "-- not in cache --" : Util.historyDate(created.longValue());
			%>
			<tr>
				<td class="center">
					<input type="checkbox" name="article" value="<%=Util.replaceTags(article, true)%>"/>
				</td>
				<td>
					<%=Util.replaceTags(text)%>
				</td>
				<td>
					<%=date%>
				</td>
			</tr>
			<%
		}
		%>
		</tbody>
	</table>
	
	<input type="hidden" name="action" value="update"/>
	<input type="hidden" name="obj_id" value="<%=objID%>"/>
	<input type="hidden" name="obj_type" value="<%=objType%>"/>
	<input type="hidden" name="idf" value="<%=idf%>"/>	
</form>
</body>
</html>
