<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,java.io.*,eionet.meta.*,com.tee.xmlserver.*,com.tee.uit.help.Helps,eionet.util.Util"%>

<%@ include file="history.jsp" %>

<%

request.setCharacterEncoding("UTF-8");

Connection conn = null;
DBPoolIF pool = null;
ServletContext ctx = getServletContext();
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());

String page_id = request.getParameter("page");
if (page_id==null || page_id.length()==0)
	page_id = "0";

String page_name=null;	
if (page_id.equals("1"))
	page_name = "Functions";
else if (page_id.equals("2"))
	page_name = "Concepts";
else if (page_id.equals("3"))
	page_name = "Login mode";

try{
	pool = xdbapp.getDBPool();	
	conn = pool.getConnection();
	
	AppUserIF user = SecurityUtil.getUser(request);
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);	
	searchEngine.setUser(user);
	
	Vector datasets = searchEngine.getDatasets(null, null, null, null, null, false);
	Vector releasedDatasets = new Vector();
	for (int i=0; datasets!=null && i<datasets.size(); i++){
		Dataset dst = (Dataset)datasets.get(i);
		String status = dst.getStatus();
		if (status!=null && status.equals("Released"))
			releasedDatasets.add(dst);
	}
	
	request.setAttribute("rlsd_datasets", releasedDatasets);
}
catch (Exception e){
	
	request.setAttribute("DD_ERR_MSG", e.toString());
	
	ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
	e.printStackTrace(new PrintStream(bytesOut));
	String trace = bytesOut.toString(response.getCharacterEncoding());
	if (trace!=null)
		request.setAttribute("DD_ERR_TRC", trace);
}

	
%>
<html>
<head>
    <title>Data Dictionary</title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
    <link type="text/css" rel="stylesheet" href="boxes.css">
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
    <script language="javascript" src='script.js'></script>
</head>
<body>
    <%@ include file="header.htm" %>
    <table border="0" cellspacing="0" cellpadding="0">
        <tr valign="top">
            <td nowrap="nowrap" width="130">
                <p><center>
                    <%@ include file="menu.jsp" %>
                </center></p>
            </td>
            <td>
               	<% if (page_name == null){%>
	                <jsp:include page="location.jsp" flush='true'/>
           		<%} else{ %>
	                <jsp:include page="location.jsp" flush='true'>
            			<jsp:param name="name" value="<%=page_name%>"/>
            			<jsp:param name="back" value="true"/>
		            </jsp:include>
	            <% } %>

				<div style="margin-left:20">
				
					<%
					
					// exceptionous part
					String errMsg = (String)request.getAttribute("DD_ERR_MSG");
					if (errMsg!=null){
						String errTrc = (String)request.getAttribute("DD_ERR_TRC");
						%>
						<b>DD encountered the following error:</b><br/>
						<%=errMsg%>
						<%
						if (errTrc!=null){
							%>
							<form acceptcharset="UTF-8" name="errtrc" action="http://">
								<input type="hidden" name="errtrc" value="<%=errTrc%>"/>
							</form>
							<%
						}
					}
					// no exceptions
					else{
						%>
					
						<table border="0" cellspacing="0" cellpadding="0">
							<tr height="10"><td></td></tr>					
							<tr>
								<td width="620" style="border: 1 dotted #C0C0C0">												
									<table border="0" width="100%" cellspacing="4" cellpadding="0">
									
										<!-- released data definitions part -->
										
					                	<tr>
					                  		<td width="100%" colspan="2">
					                    		<jsp:include page="released_datasets.jsp" flush="true">
				                    			</jsp:include>
					                  		</td>
					                	</tr>
					                	<tr>
					                  		<td width="101%" colspan="2" height="10"></td>
					                	</tr>
					                	
					                	<tr>
					                	
					                		<!-- the login part -->
					                		
					                  		<jsp:include page="protarea.jsp" flush="true"></jsp:include>
					                  		
					                  		<!-- the support part -->
					                  		
					                  		<td width="50%" style="border: 1px solid #FF9900" valign="top">
					                  			<%=Helps.get("front_page", "support")%>
					                  		</td>
					                	</tr>
					                	<tr>
					                	
					                		<!-- the documentation part -->
					                		
					                  		<td width="50%" style="border: 1px solid #FF9900" valign="top">
												<%=Helps.get("front_page", "documentation")%>
					                  		</td>
					                  		
					                  		<!-- the news part -->
					                  		
					                  		<td width="50%" style="border: 1px solid #FF9900" valign="top">
												<%=Helps.get("front_page", "news")%>
		                  					</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
						
						<jsp:include page="footer.jsp" flush="true">
						</jsp:include>
						
						<%
					} // end of excpetions if/else
					%>
								
				</div>
            </td>
        </tr>
    </table>
    
</body>
</html>
