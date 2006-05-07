<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!private Vector selected=null;%>


<%
	request.setCharacterEncoding("UTF-8");
	
	ServletContext ctx = getServletContext();			
	String appName = ctx.getInitParameter("application-name");
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	AppUserIF user = SecurityUtil.getUser(request);

	boolean wrkCopies = false;
	
	try { // start the whole page try block
	
	conn = pool.getConnection();

	String short_name = request.getParameter("short_name");
	String idfier = request.getParameter("idfier");
	String full_name = request.getParameter("full_name");
	String definition = request.getParameter("definition");

	Integer oSortCol=null;
    Integer oSortOrder=null;
    try {
    	oSortCol=new Integer(request.getParameter("sort_column"));
        oSortOrder=new Integer(request.getParameter("sort_order"));
    }
    catch(Exception e){
    	oSortCol=null;
        oSortOrder=null;
    }
	
    String searchType=request.getParameter("SearchType");
	
    String tableLink="";	
	
	String sel = request.getParameter("selected");
	
	String backUrl = "search_table.jsp?ctx=popup&amp;selected=" + sel;
	String id=null;
	selected= new Vector();
	if (sel!=null && sel.length()>0){
		int i=sel.indexOf("|");
		while (i>0){
			id = sel.substring(0, i);
			sel = sel.substring(i+1);
			selected.add(id);
			i=sel.indexOf("|");
		}
	}
	
	if (sel==null) sel="";
	
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	searchEngine.setUser(user);
	
	String srchType = request.getParameter("search_precision");
	String oper="=";
	if (srchType != null && srchType.equals("free"))
		oper=" match ";
	if (srchType != null && srchType.equals("substr"))
		oper=" like ";

	Vector params = new Vector();	
	Enumeration parNames = request.getParameterNames();
	while (parNames.hasMoreElements()){
		String parName = (String)parNames.nextElement();
		if (!parName.startsWith(ATTR_PREFIX))
			continue;
					
		String parValue = request.getParameter(parName);
		if (parValue.length()==0)
			continue;
						
		DDSearchParameter param =
			new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");
        if (oper!= null && oper.trim().equalsIgnoreCase("like"))
			param.addValue("'%" + parValue + "%'");
		else
			param.addValue("'" + parValue + "'");
		params.add(param);
	}
					
	String _wrkCopies = request.getParameter("wrk_copies");
	wrkCopies = (_wrkCopies!=null && _wrkCopies.equals("true")) ? true : false;

	Vector dsTables = searchEngine.getDatasetTables(params, short_name, idfier, full_name, definition, oper, wrkCopies);

%>

<html>
	<head>
		<title>Meta</title>
		<meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
		<script language="javascript" type="text/javascript">
		// <![CDATA[
			function pickTable(id, i, name) {
				if (opener && !opener.closed) {
					if (window.opener.pickTable(id, name)==true)  //window opener should have function pickTABLE with 2 params - tbl id & tbl name
																// and if it returns true, then the popup window is closed, 
																// otherwise multiple selection is allowed
						closeme();
					hideRow(i);
				} else {
					alert("You have closed the main window.\n\nNo action will be taken.")
				}
			}
			function hideRow(i){
				var t = document.getElementById("tbl");
				var row = t.getElementsByTagName("TR")[i+1];
				row.style.display = "none";
			}
			function closeme(){
				window.close()
			}
		// ]]>
		</script>
	</head>
    <!--script language="javascript" for="window" event="onload">    	
	</script-->

<body class="popup">

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
	<form name="form1" action="">
		<p><b>Select dataset table:</b></p>
		
		<table id="tbl">
			<thead>
				<tr><td colspan="4" align="right"><a href="<%=backUrl%>">back to search</a></td></tr>
				<tr>
					<th align="left" style="padding-left:5;padding-right:10">Short name</th>
					<th align="left" style="padding-right:10">Dataset</th>
					<th align="left" style="padding-right:10">Full name</th>
				</tr>
			</thead>
			<tbody>
 			<%
        		    if (dsTables == null || dsTables.size()==0){
		            %>
			            <tr><td colspan="4"><b>No results found!</b></td></tr></tbody></table></form></div></td></tr></table></body></html>
	            	<%
	            		return;
            		}

            		int c=0;
					DElemAttribute attr = null;
					
					for (int i=0; i<dsTables.size(); i++){
						DsTable table = (DsTable)dsTables.get(i);
						String table_id = table.getID();
						String table_name = table.getShortName();
						String ds_id = table.getDatasetID();
						String ds_name = null;
						if (ds_id!=null){
							Dataset ds = (Dataset)searchEngine.getDataset(ds_id);
							ds_name = ds.getShortName();
						}
				
						if (table_name == null) table_name = "unknown";
						if (table_name.length() == 0) table_name = "empty";
				
						if (ds_name == null || ds_name.length() == 0) ds_name = "unknown";
				
						//String fullName = table.getName();
						String tblName = "";
		
						Vector attributes = searchEngine.getAttributes(table_id, "T", DElemAttribute.TYPE_SIMPLE);
		
						for (int j=0; j<attributes.size(); j++){
							attr = (DElemAttribute)attributes.get(j);
        					if (attr.getName().equalsIgnoreCase("Name"))
        						tblName = attr.getValue();
						}
				
						String tblFullName = tblName;
						tblName = tblName.length()>60 && tblName != null ? tblName.substring(0,60) + " ..." : tblName;
						c++;
					%>
				
					<tr>
						<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
							<a href="#" onclick="pickTable(<%=table_id%>, <%=c%>, '<%=table_name%>')">
							<%=Util.replaceTags(table_name)%></a>
						</td>					
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=Util.replaceTags(ds_name)%></td>
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=Util.replaceTags(tblName)%></td>
					</tr>
				
				<%
			}
			%>
			</tbody>
		</table>
		<br/>
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
