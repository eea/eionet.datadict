<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*"%>

<%!private Vector selected=null;%>

<%
	
	ServletContext ctx = getServletContext();			
	String appName = ctx.getInitParameter("application-name");

	
	String attr_id = request.getParameter("id");
	String dispType = request.getParameter("dispType");
	String width = request.getParameter("width");
	String sel = request.getParameter("selected");
	
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
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	try { // start the whole page try block
	
	conn = pool.getConnection();
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
%>

<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
		<script language="JavaScript">
			function selectAttr(id, oControl) {
				if (opener && !opener.closed) {
					window.opener.selAttr(id, 'add');
					oControl.style.display = "none";

				} else {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.");
				}
			}

			function closeme(){
				window.close()
			}
			function ok(){
				if (opener && !opener.closed) {
					var id = document.forms["form1"].attr_id.value;
				  	<%
				  	if(dispType.equals("select") || dispType.equals("text")){
				  	%>
						if (document.all){
							var optns=document.all('val').options;
							for (var i=0; i<optns.length; i++){
								var optn = optns.item(i);
								if (optn.selected){
									window.opener.addValue(id, optn.value);
								}	
							}
						}
						else{
							var slct = document.forms["form1"].elements['val'];
							for (var i=0; i<slct.length; i++){
								if (slct.options[i].selected){
									window.opener.addValue(id, slct.options[i].value);
								}
							}
						}
					
					<%
					}
				  	if(dispType.startsWith("text"))
					{
					%>
						var new_val = document.forms["form1"].text_val.value;
						window.opener.addValue(id, new_val);
					<%
					}
					%>

				} else {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.")
				}
				closeme();
			}
			function onLoad(){
				
			  <%
			  if(dispType.equals("select") || dispType.equals("text")){
			  %>
				if (opener==null || opener.closed) {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.");
					return;
				}
				var id = "<%=attr_id%>";
				if (opener.document.all){
					var optns=opener.document.all('hidden_attr_'+id).options;
					for (var i=0; i<optns.length; i++){
						var optn = optns.item(i);
						if (!opener.hasValue(id, optn.value)){
							addValue(optn.value);
						}	
					}
				}
				else{
					var slct = opener.document.forms["form1"].elements['hidden_attr_'+id];
					for (var i=0; i<slct.length; i++){
						if (!opener.hasValue(id, slct.options[i].value)){
							addValue(slct.options[i].value);
						}
					}
				}				

				if (document.forms["form1"].elements['val'].length>6)
					document.forms["form1"].elements['val'].size=6;
				if (document.forms["form1"].elements['val'].length==0){
					addValue("There is nothing to select");
					document.forms["form1"].elements['val'].disabled=true;
				}
			  <%
			  }
			  %>
			}
			function addValue(val){
				if (val.length > 0){
					if (document.all){
						var oOption = document.createElement("option");
						document.all('val').options.add(oOption);
						oOption.text = val;
						oOption.value = val;
						oOption.size=oOption.length;
					}
					else{
						var oOption = new Option(val, val, false, false);
						var slct = document.forms["form1"].elements["val"]; 
						slct.options[slct.length] = oOption;
						slct.size=oOption.length;
					}
				}
			}
		</script>
	</head>

<body class="popup" onload="onLoad()">

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
	<form name="form1" onsubmit="ok()">
	<table>
		<%
		if(dispType.equals("select") || dispType.equals("text")){
		%>
			<tr><td><b>Select value:</b></td></tr>
			<tr><td>&#160;</td></tr>
			<tr><td>
				<select class="small" name="val" multiple="true">
				</select>
			</td></tr>
			<tr><td>&#160;</td></tr>
		<%
		}
		if (dispType.startsWith("text")){
		%>
			<tr><td><b>Insert value:</b></td></tr>
			<tr><td>&#160;</td></tr>
			<tr><td>
				<%
				if (dispType.equals("text")){
				%>
					<input class="smalltext" class="smalltext" type="text" size="<%=width%>" name="text_val"/>
				<%
				}
				else if(dispType.equals("textarea")){
				%>
					<textarea class="small" rows="5" cols="<%=width%>"  name="text_val"/></textarea>
				<%
			}
			%>
			</td></tr>
			<tr><td>&#160;</td></tr>
		<%
		}
		%>
	</table>
	<input class="mediumbuttonb" type="button" value="OK" onclick="ok()"></input>
	<input class="mediumbuttonb" type="button" value="Cancel" onclick="closeme()"></input>
	<input type="hidden" name="attr_id" value="<%=attr_id%>"></input>
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