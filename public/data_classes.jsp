<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static int iPageLen=10;%>

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
	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	Connection conn = DBPool.getPool(appName).getConnection();
	
	DDuser user = getUser(request);
	
	/*DDuser user = new DDuser(DBPool.getPool(appName));
	
	String username = "root";
	String password = "ABr00t";
	boolean f = user.authenticate(username, password);*/
	
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
		
		
			
		DataClassHandler handler =
					new DataClassHandler(user.getConnection(), request, ctx, "delete");
				
		handler.execute();
		
		String redirUrl = request.getParameter("searchUrl");
		if (redirUrl != null && redirUrl.length()!=0){
			ctx.log("redir= " + redirUrl);
			response.sendRedirect(redirUrl);
		}
	}	
	
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	Vector classes = searchEngine.getDataClasses();	

    int iCurrPage=0;
    try {
	    iCurrPage=Integer.parseInt(request.getParameter("page_number"));
    }
    catch(Exception e){
        iCurrPage=0;
    }
    if (iCurrPage<0)
        iCurrPage=0;
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
		function setLocation(){
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
		}
    </script>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0">
<%@ include file="header.htm" %>
<table border="0">
    <tr valign="top">
        <td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Data Classes"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<%
            
            if (classes == null || classes.size()==0){
	            %>
	            <b>No classes were found!</b></div></TD></TR></table></body></html>
	            <%
	            return;
            }
            %>
            
			<form id="form1" method="POST" action="data_classes.jsp" onsubmit="setLocation()">
			
		<table width="450">
			<tr><td colspan="3"><font class="head00">Data classes</font></td></tr>
			<tr height="10"><td colspan="3"></td></tr>
			<tr>
				<td colspan="3">
					A data class is for specifying the class into which a data element belongs to.
					Classes could be for example 'Chemical', 'Parameter', etc. They are needed to
					distinguish groups of similarity between data elements and provide a way for
					example to get a picklist of all data elements that are chemicals by nature.</br></br>
					Below is a list of the data classes found in the database.
					To view, modify or delete a class, please click on its Name in the
					list below. To delete several classes together, use the checkboxes
					and the Delete button in the left-most column of the list.
				</td>
			</tr>
			<tr height="10"><td colspan="3"></td></tr>
			<tr>
				<td align="center">
					<% if (user != null){
						%>
						<input type="submit" value="Delete" style="font-family:Arial;font-size:10px;font-weight:bold"/>
						<%
					}
					else{
						%>
						<input type="submit" value="Delete" style="font-family:Arial;font-size:10px;font-weight:bold" disabled="true"/>
						<%
					}
					%>
				</td>
				<th align="center" width="300pts">&#160;Short name</th>
				<th align="center" width="100pts">&#160;Namespace</th>
			</tr>
			
			<%
			
	        int iBeginNode=iCurrPage*iPageLen;
		    int iEndNode=(iCurrPage+1)*iPageLen;
			if (iEndNode>=classes.size()) 
				iEndNode=classes.size();
	        for (int i=iBeginNode;i<iEndNode;i++) {
			//for (int i=0; i<classes.size(); i++){
				
				DataClass dataClass = (DataClass)classes.get(i);
				
				String class_id = dataClass.getID();
				String class_name = dataClass.getShortName();
				if (class_name == null) class_name = "unknown";
				if (class_name.length() == 0) class_name = "empty";
				Namespace ns = dataClass.getNamespace();
				
				%>
				
				<tr>
					<td align="center">
						<input type="checkbox" name="class_id" value="<%=class_id%>"/>
					</td>
					<td align="center" width="300pts">&#160;
						<a href="data_class.jsp?class_id=<%=class_id%>&#38;mode=edit">
						<%=class_name%></a>
					</td>
					<%
					if (ns != null){
						%>
						<td align="center" width="80pts">&#160;
							<a href="namespace.jsp?ns_id=<%=ns.getID()%>&#38;mode=edit">
							<%=ns.getShortName()%></a>
						</td>
						<%
					}
					else{
						%>
						<td align="center" width="80pts">Unknown</td>
						<%
					}
					%>
				</tr>
				
				<%
			}
			%>
			
		</table>

		<!--   Page footer  -->
		<%
			if (classes != null){
				int iTotal = classes.size();
				%>
				<jsp:include page="search_results_footer.jsp" flush='true'>
					<jsp:param name="total" value="<%=iTotal%>"/>
				    <jsp:param name="page_len" value="<%=iPageLen%>"/>
					<jsp:param name="curr_page" value="<%=iCurrPage%>"/>
			    </jsp:include>
				<%
			}
		%>
		
		<input type="hidden" name="searchUrl" value=""/>
		
		</form>
			</div>
			
		</TD>
</TR>
</table>
</body>
</html>