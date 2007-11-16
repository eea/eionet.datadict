<%@page contentType="text/html;charset=UTF-8" import="java.util.*, eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%

request.setCharacterEncoding("UTF-8");

String objID = request.getParameter("obj_id");
String objType = request.getParameter("obj_type");
String idf = (String)request.getAttribute("identifier");
Vector entries = (Vector)request.getAttribute("entries");

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
		<%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
// <![CDATA[
    	function submitForm(action){
	    	document.forms["form1"].elements["action"].value = action;
	    	document.forms["form1"].submit();
    	}
// ]]>
    </script>
</head>
<body>
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Cache"/>
		<jsp:param name="helpscreen" value="cache"/>
	</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
<h1>Cached articles for <%=request.getAttribute("object_type")%>: <em><%=idf%></em></h1>
<br/>
<form id="form1" action="GetCache" method="post">
	<div>
		<input type="button" class="smallbutton" value="Update selected" onclick="submitForm('update')"/>
		<input type="button" class="smallbutton" value="Remove selected" onclick="submitForm('clear')"/>
	</div>
	<table class="datatable" style="width:auto">
		<thead>
		<tr>
			<th>&nbsp;</th>
			<th style="text-align:left">Article</th>
			<th style="text-align:left">Created</th>
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
				<td style="padding-right:10px">
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
	<div style="display:none">
		<input type="hidden" name="action" value="update"/>
		<input type="hidden" name="obj_id" value="<%=objID%>"/>
		<input type="hidden" name="obj_type" value="<%=objType%>"/>
		<input type="hidden" name="idf" value="<%=idf%>"/>
	</div>
</form>

</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.txt" %>
</body>
</html>
