<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!private Vector selected=null;%>


<%
	
	ServletContext ctx = getServletContext();			
	String appName = ctx.getInitParameter("application-name");
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	AppUserIF user = SecurityUtil.getUser(request);

	boolean wrkCopies = false;
	
	try { // start the whole page try block
	
	conn = pool.getConnection();

	String type = request.getParameter("type");
	String ns_param = request.getParameter("ns");
	String short_name = request.getParameter("short_name");
	String idfier = request.getParameter("idfier");
	String dataset = request.getParameter("dataset");
		
	String sel = request.getParameter("selected");
	
	String fk = request.getParameter("fk");
	
	StringBuffer buf = new StringBuffer("search.jsp?");
	if (fk!=null && fk.equals("true"))
		buf.append("fk=true&");
	if (dataset!=null && dataset.length()>0)
		buf.append("dataset=").append(dataset).append("&");
	buf.append("ctx=popup&selected=").append(sel);
	
	String backUrl = buf.toString();
	
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
	
	Vector dataElements = searchEngine.getDataElements(params, type, ns_param,
									short_name, idfier, null, dataset, wrkCopies, oper);

%>

<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
		<script language="JavaScript">
			function pickElem(id, i, name) {
				if (opener && !opener.closed) {
					if (window.opener.pickElem(id, name)==true)  //window opener should have function pickElem with 2 params - elem id & elem name
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
		</script>
	</head>
    <script language="JAVASCRIPT" for="window" event="onload">    	
	</script>

<body style="background-color:#f0f0f0;background-image:url('images/eionet_background2.jpg');background-repeat:repeat-y;"
		topmargin="0" leftmargin="0" marginwidth="0" marginheight="0">
<div style="margin-left:30">
	<br>
	<font color="#006666" size="5" face="Arial"><strong><span class="head2">Data Dictionary</span></strong></font>
	<br>
	<br>
	<form name="form1">
		<p><b>Select data element:</b></p>
		
		<table id="tbl">
			<tr><td colspan="4" align="right"><a href="<%=backUrl%>">back to search</a></td></tr>
			<tr>
				<th align="left" style="padding-left:5;padding-right:10">Short name</th>
				<th align="left" style="padding-right:10">Type</th>
				<th align="left" style="padding-right:10">Dataset</th>
				<th align="left" style="padding-right:10">Table</th>
			</tr>
			<tbody>
 			<%
			int c=0;
			for (int i=0; i<dataElements.size(); i++){
				
				DataElement dataElement = (DataElement)dataElements.get(i);
				String delem_id = dataElement.getID();
				String delem_name = dataElement.getShortName();
				
				if (selected.contains(delem_id))
					continue;

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
				c++;
				%>
				
				<tr>
					<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<a href="#" onclick="pickElem(<%=delem_id%>, <%=c%>, '<%=delem_name%>')">
						<%=Util.replaceTags(delem_name)%></a>
					</td>					
					<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=displayType%></td>
					<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=Util.replaceTags(dispDs)%></td>
					<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=Util.replaceTags(dispTbl)%></td>
				</tr>
				
				<%
			}
			%>
			</tbody>
		</table>
		<br>
	<input class="mediumbuttonb" type="button" value="Close" onclick="closeme()"></input>
	<input type="hidden" name="type" value="<%=type%>"></input>
	</form>
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