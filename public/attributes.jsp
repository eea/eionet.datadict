<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!static int iPageLen=0;%>

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
		
		
			
		AttributeHandler handler =
					new AttributeHandler(user.getConnection(), request, ctx, "delete");
				
		handler.execute();
		
		String redirUrl = request.getParameter("searchUrl");
		if (redirUrl != null && redirUrl.length()!=0){
			ctx.log("redir= " + redirUrl);
			response.sendRedirect(redirUrl);
		}
	}	
	
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	Vector attributes = searchEngine.getDElemAttributes(DElemAttribute.TYPE_SIMPLE);
	Vector complexAttributes = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
	for (int i=0; complexAttributes!=null && i<complexAttributes.size(); i++)
		attributes.add(complexAttributes.get(i));


	int iCurrPage=0;
    try {
	    iCurrPage=Integer.parseInt(request.getParameter("page_number"));
    }
    catch(Exception e){
        iCurrPage=0;
    }
    if (iCurrPage<0)
        iCurrPage=0;
    
    String mode = request.getParameter("mode");
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
		
		function submitForm(){
			var b = confirm("This will delete all the attributes you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
			if (b==true){
				document.forms["form1"].submit();
			}
		}
		
		function goTo(mode){
			if (mode == "add"){
				document.location.assign('delem_attribute.jsp?mode=add');
			}
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
                <jsp:param name="name" value="Attributes"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<%
            
            if (attributes == null || attributes.size()==0){
	            %>
	            <b>No attributes were found!</b></div></TD></TR></table></body></html>
	            <%
	            return;
            }
            %>
            
			<form id="form1" method="POST" action="attributes.jsp" onsubmit="setLocation()">
			
		<table width="600">
		
			<tr><td><span class="head00">Attributes</span></td></tr>

			<tr height="5"><td></td></tr>
			<tr>
				<td>
					<span class="mainfont">
						This is a list of all the attributes used in Data Dictionary.
						The columns indicate for which concepts exactly the attribute can be used.
						To view <% if (user != null && mode==null){ %> or modify <%}%> an attribute's
						definition, click on its short name.
						<% if (user != null && mode==null){ %>
							To add a new attribute, click the 'Add' button on top of the list.
							The left-most column enables you to delete selected attributes.
						<%}%>
					</span>
				</td>
			</tr>
			<tr height="10"><td></td></tr>
			
		</table>
		
		<table width="auto" cellspacing="0" cellpadding="0" border="0">
		
			<tr>
				<%
				if (user != null && mode==null){
					%>
					<td valign="top" align="right" style="padding-right:10;padding-top:3">
						<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>
					</td>
					<%
				}
				%>
				<th rowspan="2" valign="bottom" align="left" style="padding-left:5;padding-right:10;padding-bottom:5;padding-top:5">Short name</th>
				<th rowspan="2" valign="bottom" align="left" style="padding-right:10;padding-bottom:5;padding-top:5">Type</th>
				<th rowspan="2" valign="bottom" align="left" style="padding-right:10;padding-bottom:5;padding-top:5">Datasets</th>
				<th rowspan="2" valign="bottom" align="left" style="padding-right:10;padding-bottom:5;padding-top:5">Tables</th>
				<th rowspan="2" valign="bottom" align="left" style="padding-right:10;padding-bottom:5;padding-top:5">Aggregate data elements</th>
				<th rowspan="2" valign="bottom" align="left" style="padding-right:10;padding-bottom:5;padding-top:5">Data elements with fixed values</th>
				<th rowspan="2" valign="bottom" align="left" style="padding-right:10;padding-bottom:5;padding-top:5">Data elements with quantitative values</th>
				<th rowspan="2" valign="bottom" align="left" style="padding-right:10;padding-bottom:5;padding-top:5">Fixed values</th>
			</tr>
			
			<%
			if (user != null && mode==null){
				%>
				<tr>
					<td valign="bottom" align="right" style="padding-right:10;padding-bottom:3">
						<input class="smallbutton" type="button" value="Delete" onclick="submitForm()"/>
					</td>
				</tr>
				<%
			}
			%>
			
			<tr height="5"><td colspan="5"></td></tr>
			
			<%
			// show all
			if (iPageLen==0)
				iPageLen = attributes.size();

	        int iBeginNode=iCurrPage*iPageLen;
		    int iEndNode=(iCurrPage+1)*iPageLen;
			if (iEndNode>=attributes.size()) 
				iEndNode=attributes.size();
	        //for (int i=iBeginNode;i<iEndNode;i++) {
			for (int i=0; i<attributes.size(); i++){
				
				DElemAttribute attribute = (DElemAttribute)attributes.get(i);
				
				String attr_id = attribute.getID();
				String attr_name = attribute.getShortName();
				if (attr_name == null) attr_name = "unknown";
				if (attr_name.length() == 0) attr_name = "empty";
				String attr_oblig = attribute.getObligation();
				//Namespace ns = attribute.getNamespace();
				
				String attrType = attribute.getType();
				
				String displayOblig = "Mandatory";
				if (attr_oblig.equals("M")){
					displayOblig = "Mandatory";
				}
				else if (attr_oblig.equals("O")){
					displayOblig = "Optional";
				}
				else if (attr_oblig.equals("C")){
					displayOblig = "Conditional";
				}
				
				String attrTypeDisp = "Simple";
				%>
				
				<tr>
					<%
					if (user != null && mode==null){
						%>
						<td align="right" style="padding-right:10">
							<%
							if (attrType.equals(DElemAttribute.TYPE_SIMPLE)){
								%>
								<input type="checkbox" style="height:13;width:13" name="simple_attr_id" value="<%=attr_id%>"/>
								<%
							}
							else{
								attrTypeDisp = "Complex";
								%>
								<input type="checkbox" style="height:13;width:13" name="complex_attr_id" value="<%=attr_id%>"/>
								<%							
							}
							%>
						</td>
						<%
					}
					%>
					<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<a href="delem_attribute.jsp?attr_id=<%=attr_id%>&#38;type=<%=attrType%>&#38;mode=view">
						<%=attr_name%></a>
					</td>
					<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=attrTypeDisp%></td>
					<td align="left" width="50" style="padding-right:10;padding-left:3" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<% if (attribute.displayFor("DST")){ %><img src="../images/ok.gif"/><%}%>
					</td>
					<td align="left" width="50" style="padding-right:10;padding-left:3" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<% if (attribute.displayFor("TBL")){ %><img src="../images/ok.gif"/><%}%>
					</td>
					<td align="left" width="50" style="padding-right:10;padding-left:3" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<% if (attribute.displayFor("AGG")){ %><img src="../images/ok.gif"/><%}%>
					</td>
					<td align="left" width="50" style="padding-right:10;padding-left:3" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<% if (attribute.displayFor("CH1")){ %><img src="../images/ok.gif"/><%}%>
					</td>
					<td align="left" width="50" style="padding-right:10;padding-left:3" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<% if (attribute.displayFor("CH2")){ %><img src="../images/ok.gif"/><%}%>
					</td>
					<td align="left" width="50" style="padding-right:10;padding-left:3" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<% if (attribute.displayFor("FXV")){ %><img src="../images/ok.gif"/><%}%>
					</td>
				</tr>
				
				<%
			}
			%>
			
		</table>
		
		<input type="hidden" name="searchUrl" value=""/>
		<!--   Page footer NOT NEEDED -->
		<!-- 
		<%
			if (attributes != null){
				int iTotal = attributes.size();
				%>
				<jsp:include page="search_results_footer.jsp" flush='true'>
					<jsp:param name="total" value="<%=iTotal%>"/>
				    <jsp:param name="page_len" value="<%=iPageLen%>"/>
					<jsp:param name="curr_page" value="<%=iCurrPage%>"/>
			    </jsp:include>
				<%
			}
		%>
		-->
		</form>
			</div>
			
		</TD>
</TR>
</table>
</body>
</html>