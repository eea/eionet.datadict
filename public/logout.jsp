<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%!

ServletContext ctx = null;
boolean wc = false;
Vector datasets=null;
Vector tables=null;
Vector dataElements=null;

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
	
	try { // start the whole page try block
	
		conn = pool.getConnection();

		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		searchEngine.setUser(user);		
		wc = searchEngine.hasUserWorkingCopies();

		if (wc){
			datasets = searchEngine.getDatasets(null, null, null, null, true);
			tables = searchEngine.getDatasetTables(null, null, null, null, null, true);
			dataElements = searchEngine.getDataElements(null, null, null, null, null, null, true);
		}

%>

<html>
<head>
	<title>Data Dictionary</title>
	<meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
	<link type="text/css" rel="stylesheet" href="eionet.css"/>
	<script language="javascript" src='script.js' type="text/javascript"></script>
	<script language="javascript" type="text/javascript">
	// <![CDATA[
		function onLoad(){
			<%
			if (wc==false){
			%>
				logout();
			<%
			}
			%>
		}
	// ]]>
	</script>
</head>
<body onload="onLoad()">

<%@ include file="header.htm"%>

<table border="0">
    <tr valign="top">
		<td nowrap="nowrap" width="125">
            <center>
                <%@ include file="menu.jsp" %>
            </center>
        </td>
        <td>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Logout"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>            

            <div style="margin-left:30">
            	<form name="form1" action="index.jsp" method="get">
				<table width="500">
				
				  <%
				  if (wc){
				  %>
	  			    <tr><td><font class="head00">Logging out</font></td></tr>
					<tr style="height:10px;"><td>&#160;</td></tr>
					
					<tr style="height:30px;"><td>You have checked out the following objects: </td></tr>
					<% 
					// DATASETS
					int d=0;
					
					if (datasets!=null){
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
				
							<tr valign="top">					
								<td align="left" style="padding-left:5;padding-right:10" <% if (d % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2" title="<%=dsFullName%>">
									Dataset definition: &#160;
									<a href="dataset.jsp?ds_id=<%=ds_id%>&amp;mode=view">
									<%=Util.replaceTags(dsFullName)%></a>&#160;
									<!--%=dsVersion%-->
								</td>
							</tr>
						<%
						}
					}
					//TABLES
					if (tables!=null){
						for (int i=0; i<tables.size(); i++){
							DsTable table = (DsTable)tables.get(i);
							String table_id = table.getID();
							String table_name = table.getShortName();
							String ds_id = table.getDatasetID();
							String ds_name = table.getDatasetName();
							String dsNs = table.getParentNs();
				
							if (table_name == null) table_name = "unknown";
							if (table_name.length() == 0) table_name = "empty";
				
							if (ds_name == null || ds_name.length() == 0) ds_name = "unknown";
				
							//String tblName = "";
							String tblName = table.getName()==null ? "" : table.getName();
				
							String tblFullName = tblName;
							tblName = tblName.length()>60 && tblName != null ? tblName.substring(0,60) + " ..." : tblName;

							String tableLink = "dstable.jsp?mode=view&amp;table_id=" + table_id + "&amp;ds_id=" + ds_id + "&amp;ds_name=" + ds_name;
							d++;
							%>
							<tr valign="top">					
								<td align="left" style="padding-left:5;padding-right:10" <% if (d % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
									Table definiton: &#160;
									<a href="<%=tableLink%>"><%=Util.replaceTags(table_name)%></a>&#160;
									in dataset: <%=Util.replaceTags(ds_name)%>
									<!--%=Util.replaceTags(tblName)%-->
								</td>
							</tr>
							<%
						}
					}
					//ELEMENTS
					if (dataElements!=null){
			        	for (int i=0; i<dataElements.size(); i++){
							DataElement dataElement = (DataElement)dataElements.get(i);
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
				
							String tblID = dataElement.getTableID();
							DsTable tbl = null;
							if (tblID != null) tbl = searchEngine.getDatasetTable(tblID);
							String dsID = null;
							Dataset ds = null;
							if (tbl != null) dsID = tbl.getDatasetID();
							if (dsID != null) ds = searchEngine.getDataset(dsID);
				
							String dispDs  = ds==null  ? "-" : ds.getShortName();
							String dispTbl = tbl==null ? "-" : tbl.getShortName();
							d++;
							%>
							<tr valign="top">					
								<td align="left" style="padding-left:5;padding-right:10" <% if (d % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
								Data element definition: &#160;
								<a href="data_element.jsp?delem_id=<%=delem_id%>&amp;type=<%=delem_type%>&amp;mode=view">
								<%=Util.replaceTags(delem_name)%></a>
								(<%=displayType%>)
								in table: <%=Util.replaceTags(dispTbl)%>
								in dataset: <%=Util.replaceTags(dispDs)%>
							</tr>
							<%
						}
					}
					%>
					
					<tr><td align="left">&#160;</td></tr>
					<tr style="height:30px;"><td><b>!Be aware that if you leave these objects as checked out, any other user cannot edit these or their parent objects.
						If you still want to log out, please click 'Logout' button below:</b>
					</td></tr>
					<tr style="height:30px;"><td align="left">
						<input type="button" onclick="logout()" value="Logout" class="smallbutton"/>
					</td></tr>
				  <%
			  	  }
			  	  else { %>
	  			    <tr><td><font class="head00">Logging out . . .</font></td></tr>
	  			  <%}%>
				</table>
				</form>
            </div>
		</td>
	</tr>
</table>
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
