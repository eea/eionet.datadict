<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.util.*,com.tee.xmlserver.*"%>
<%@ page import="eionet.meta.filters.EionetCASFilter" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!
ServletContext ctx = null;
boolean userHasWorkingCopies = false;
Vector datasets=null;
Vector commonElements=null;
%>

<%@ include file="history.jsp" %>

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	
	if (user==null){
		response.sendRedirect("index.jsp");
		return;
	}
	
	ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	// try-catch block of the whole page
	try {
	
		conn = pool.getConnection();

		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		searchEngine.setUser(user);		
		userHasWorkingCopies = searchEngine.hasUserWorkingCopies();

		if (userHasWorkingCopies){
			datasets = searchEngine.getDatasets(null, null, null, null, true);
			commonElements = searchEngine.getCommonElements(null, null, null, null, true, "=");
		}
		else{
			request.getRequestDispatcher("Logout").forward(request,response);
			return;
		}
			
			

%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Data Dictionary - Logging out</title>
</head>
<body>
<jsp:include page="nlocation.jsp" flush='true'>
	<jsp:param name="name" value="Logout"/>
	<jsp:param name="back" value="true"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>

<div id="workarea">
	<h2>Logging out</h2>	
	<br/>
	<p><strong>You have the following objects checked out:</strong></p>
	<table class="datatable">
		<tbody>
			<% 			
			int d=0;
			// DATASETS
			if (datasets!=null && datasets.size()>0){
				%>
				<%
				for (int i=0; i<datasets.size(); i++){
		
					Dataset dataset = (Dataset)datasets.get(i);
			
					String ds_id = dataset.getID();
					String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
					String ds_name = Util.replaceTags(dataset.getShortName());
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";									
					String dsFullName=dataset.getName();
					if (dsFullName == null) dsFullName = ds_name;
					if (dsFullName.length() == 0) dsFullName = ds_name;
					if (dsFullName.length()>60)
						dsFullName = dsFullName.substring(0,60) + " ...";
						d++;
					%>
		
					<tr>					
						<td colspan="2" title="<%=dsFullName%>">
							Dataset:&nbsp;
							<a href="dataset.jsp?ds_id=<%=ds_id%>&amp;mode=view">
								<%=Util.replaceTags(dsFullName)%>
							</a>
						</td>
					</tr>
				<%
				}
			}
			// COMMON ELEMENTS
			if (commonElements!=null && commonElements.size()>0){
				
				for (int i=0; i<commonElements.size(); i++){
					DataElement dataElement = (DataElement)commonElements.get(i);
					String delem_id = dataElement.getID();
					String delem_name = dataElement.getShortName();
					if (delem_name == null) delem_name = "unknown";
					if (delem_name.length() == 0) delem_name = "empty";
					String delem_type = dataElement.getType();
					if (delem_type == null) delem_type = "unknown";
			
					String displayType = "unknown";
					if (delem_type.equals("CH1")){
						displayType = "Fixed values";
					}
					else if (delem_type.equals("CH2")){
						displayType = "Quantitative";
					}
		
					d++;
					%>
					<tr>
						<td colspan="2">
							Common element:&nbsp;
							<a href="data_element.jsp?delem_id=<%=delem_id%>&amp;type=<%=delem_type%>&amp;mode=view">
								<%=Util.replaceTags(delem_name)%>
							</a>
						(<%=displayType%>)
					</tr>
					<%
				}
			}
			%>
		</tbody>
	</table>
	<br/>
	<p class="caution">
		NB!<br/>
		This is a reminder that you have the above objects checked out.<br/>
		To complete the logout, click the button below.
	</p>
	<form name="form1" action="Logout" method="get">
		<input type="submit" value="Logout" class="smallbutton"/>
	</form>
	
</div>
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
