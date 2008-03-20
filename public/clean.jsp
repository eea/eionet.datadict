<%@page contentType="text/html;charset=UTF-8" import="eionet.util.sql.ConnectionUtil,eionet.meta.CleanupServlet,eionet.meta.DDUser"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	DDUser user = SecurityUtil.getUser(request);
	if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/cleanup", "x")){
		request.setAttribute("DD_ERR_MSG", "You have no permission to access this page");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Data Dictionary</title>
	<script type="text/javascript">
	// <![CDATA[
		function submitCleanup(){
			if (confirm("Are you sure?"))
				pop('CleanupServlet?<%=CleanupServlet.PAR_ACTION%>=<%=CleanupServlet.ACTION_CLEANUP%>');
		}
	// ]]>
	</script>
</head>
<body>
	<div id="container">
		<jsp:include page="nlocation.jsp" flush="true">
			<jsp:param name="name" value="Cleanup"/>
		</jsp:include>
		<%@ include file="nmenu.jsp" %>
		<div id="workarea">
			<h1>Cleanup functions</h1>
			<p>
				Pressing the 'Cleanup' button below will execute the following cleanup operations<br/>
				in the database contents (in the given order!):
			</p>
			<ul>
				<li>delete DST2TBL relations where the dataset or the table does not actually exist</li>
				<li>delete tables with no parent dataset</li>
				<li>delete TBL2ELEM relations where the table or the element does not actually exist</li>
				<li>delete non-common elements with no parent table</li>
				<li>delete NAMESPACE entries that don't have a corresponding dataset, nor a corresponding table</li>
				<li>delete object ACLs of objects that do not actually exist</li>
			</ul>
			<p>
				If you have javascript enabled, a pop-up window is opened where you see messages about the cleanup progress.<br/>
				Othwerise the messages are displayed in the current window, in plain text format.<br/++>
				If the cleanup is finished, the message says ALL DONE!
				In an exception happens, you get a message too.
			</p>
			<form id="cleanupForm" method="post" action="CleanupServlet">
			
				<input type="submit" name="<%=CleanupServlet.PAR_ACTION%>" value="<%=CleanupServlet.ACTION_CLEANUP%>" onclick="submitCleanup();return false;"/>				
				<%
				if (request.getParameter("mi6")!=null){
					%>
					<p style="margin-top:20px">
						The following section enables to delete elements/tables/datasets by their IDs.<br/>
						Type in the ID or multiple IDs (separated by space) and press the button relevant for you.<br/>
						You will be notified of success or failure.
					</p>
					<div style="margin-bottom:5px">
						<label for="idsInput">IDs:</label>
						<input type="text" id="idsInput" name="<%=CleanupServlet.PAR_OBJ_IDS%>"/>
						<input type="hidden" name="mi6" value=""/>
					</div>
					<input type="submit" name="<%=CleanupServlet.PAR_ACTION%>" value="<%=CleanupServlet.ACTION_DELETE_ELM%>"/>
					<input type="submit" name="<%=CleanupServlet.PAR_ACTION%>" value="<%=CleanupServlet.ACTION_DELETE_TBL%>"/>
					<input type="submit" name="<%=CleanupServlet.PAR_ACTION%>" value="<%=CleanupServlet.ACTION_DELETE_DST%>"/>
					<%
					if (request.getAttribute(CleanupServlet.ATTR_DELETE_SUCCESS)!=null){
						%>
						<span style="color:red;border:1px dashed red;padding:3px;margin-left:5px">Delete successful!</span><%
					}
				}
				%>
			</form>
		</div> <!-- workarea -->
	</div> <!-- container -->
	<%@ include file="footer.txt" %>
</body>
</html>
