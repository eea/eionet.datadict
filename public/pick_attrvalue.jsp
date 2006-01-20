<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*,eionet.util.*"%>

<%
	request.setCharacterEncoding("UTF-8");
	
	ServletContext ctx = getServletContext();			
	String appName = ctx.getInitParameter("application-name");

	
	String type = request.getParameter("type");
	String attr_id = request.getParameter("attr_id");
	if (type == null) type = "?";

	if (attr_id == null || attr_id.length()==0) { %>
		<b>Attribute id paramater is missing!</b>
		<%
		return;
		
	}

	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();

	try { // start the whole page try block
		
	conn = pool.getConnection();
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	Vector attrValues=null;

	Vector v = searchEngine.getDElemAttributes(attr_id,type);
	DElemAttribute attribute = (v==null || v.size()==0) ? null : (DElemAttribute)v.get(0);
	String attrName = attribute.getName();

	Vector attrFields = searchEngine.getAttrFields(attr_id);

	if (type.equals(DElemAttribute.TYPE_COMPLEX))
		 attrValues = searchEngine.getComplexAttributeValues(attr_id);	
	else
		 attrValues = searchEngine.getSimpleAttributeValues(attr_id);	

%>

<html>
	<head>
		<title>Meta</title>
		<meta content="text/html; charset=UTF-8" http-equiv="Content-Type">
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
		<script language="javascript">
		// <![CDATA[

			function closeme(){
				window.close()
			}
			function selectComplex(idx){
				var field_ids = document.forms["form1"].elements["field_ids"];
				if (opener && !opener.closed) {
					if (field_ids.length>0){
						for (var i=0; i<field_ids.length;i++){
							
							field_id=document.forms["form1"].elements["field_ids"][i].value;
							if (opener.document.forms["form1"].elements["field_"+field_id])
								opener.document.forms["form1"].elements["field_"+field_id].value=document.forms["form1"].elements["field_"+field_id][idx+1].value;
						}
					}
				} else {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.")
				}
				closeme()
			}
			function selectSimple(val){
			//EK this function will finished if somebody want to use it
				if (opener && !opener.closed) {
						alert(val);
				} else {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.")
				}
				closeme()
			}
		// ]]>
		</script>
	</head>

<body class="popup">

<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form acceptcharset="UTF-8" name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>

<div>	
	<form acceptcharset="UTF-8" name="form1">
	<table>
		<tr><td><b>Select attribute (<%=attrName%>) value:</b></td></tr>
		<tr><td>&#160;</td></tr>
		<tr>
			<%
			if (type.equals(DElemAttribute.TYPE_COMPLEX)){
				for (int t=0; t<attrFields.size(); t++){
					Hashtable hash = (Hashtable)attrFields.get(t);
					String name = (String)hash.get("name");
					String f_id = (String)hash.get("id");
						%>
						<th align="left" style="padding-right:10">&#160;<%=name%>
							<input type="hidden" name="field_ids" value="<%=f_id%>"
						</th>
						<input type="hidden" name="field_<%=f_id%>" value=" "></input>
						<%
				}
			}
			else{
				%>
				<th align="left" style="padding-right:10">&#160;Value</th>
				<%
			}
		%>
		</tr>
		<%
		if (attrValues!= null){
		if (attrValues.size()>0){
			for (int j=0; attrValues!=null && j<attrValues.size();j++){
			
				if (type.equals(DElemAttribute.TYPE_COMPLEX)){
					Hashtable rowHash = (Hashtable)attrValues.get(j);
					%>
					<tr>
					<%
			
					for (int t=0; t<attrFields.size(); t++){
						Hashtable hash = (Hashtable)attrFields.get(t);
						String fieldID = (String)hash.get("id");
						String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
						if (fieldValue == null) fieldValue = "";
							%>
							<td <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-right:10">&#160;
								<%
								if (t==0){
								%>
									<a href="javascript:selectComplex(<%=j%>)"><%=Util.replaceTags(fieldValue, true)%></a>
								<%
								}
								else{
								%>
									<%=Util.replaceTags(fieldValue)%>
								<%
								}
								%>
								<input type="hidden" name="field_<%=fieldID%>" value="<%=Util.replaceTags(fieldValue, true)%>"></input>
								</td>
							<%
					}
				
					%>
					</tr>				
					<%
				}
				else{
					String value = (String)attrValues.get(j);
					%>
					<tr><td <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-right:10">&#160;
						<a href="javascript:selectSimple('<%=value%>')"><%=Util.replaceTags(value, true)%></a>
						</td></tr>
					<%
				}
			}
		}}
		%>	
		<tr><td>&#160;</td></tr>

	</table>
	<!--input type="hidden" name="type" value="<%=type%>"></input-->
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
