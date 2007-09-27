<%@page contentType="text/html;charset=UTF-8" import="java.util.*,eionet.meta.DocumentationServlet,com.tee.xmlserver.XDBApplication,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

<%
	request.setCharacterEncoding("UTF-8");
	XDBApplication.getInstance(getServletContext());
	
	String docString = (String)request.getAttribute(DocumentationServlet.DOC_STRING);
	Object docsListObject = request.getAttribute(DocumentationServlet.DOCS_LIST);
	if (docString==null && docsListObject==null){
		request.setAttribute("DD_ERR_MSG", "Found no documentation in the request object");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	
	String servletPathHelper = (String)request.getAttribute(DocumentationServlet.DISPATCHER_PATH);
	if (servletPathHelper==null)
		servletPathHelper = "";
		
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Documentation</title>
	<base href="<%=Util.getBaseHref(request)%>" />
</head>
<body>
<div id="container">

	<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Documentation"/>
	</jsp:include>
	<%@ include file="nmenu.jsp" %>
	<div id="workarea">
		<%
		if (docString!=null){
			%>
			<%=docString%><%
		}
		else{
			%>
			<h1>Data Dictionary documentation</h1>
			<%			
			List docsList = (List)docsListObject;
			if (docsList.size()>0){
				%>
				<ul>
					<%
					for (int i=0; i<docsList.size(); i++){
						Properties props = (Properties)docsList.get(i);
						String id = props.getProperty("id");
						String heading = props.getProperty("heading");
						%>
						<li>
							<a href="<%=servletPathHelper%>/<%=id%>"><%=heading%></a>
						</li><%
					}
					%>
				</ul><%
			}
			else{
				%><p>No documentation currently available in the database.</p><%
			}
		}
		%>
	</div> <!-- workarea -->
</div> <!-- container -->
<jsp:include page="footer.jsp" flush="true" />								
</body>
</html>