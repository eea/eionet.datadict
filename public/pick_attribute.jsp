<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*"%>

<%!private Vector mAttributes=null;%>
<%!private Vector selected=null;%>

<%!
private String getAttributeIdByName(String name){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}

%>

<%
	
	ServletContext ctx = getServletContext();			
	String appName = ctx.getInitParameter("application-name");

	
	String type = request.getParameter("type");
	if (type == null) type = "?";
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
			
	
	Vector mAttributes = searchEngine.getDElemAttributes(DElemAttribute.TYPE_SIMPLE);

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
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.")
				}
				//closeme()
				//return false
			}

			function closeme(){
				window.close()
			}
		</script>
	</head>
    <script language="JAVASCRIPT" for="window" event="onload">    	
	</script>

<body style="background-color:#f0f0f0;background-image:url('../images/eionet_background2.jpg');background-repeat:repeat-y;"
		topmargin="0" leftmargin="0" marginwidth="0" marginheight="0">
<div style="margin-left:30">
	<br>
	<font color="#006666" size="5" face="Arial"><strong><span class="head2">Data Dictionary</span></strong></font>
	<br>
	<!--table cellspacing="0" cellpadding="0" width="250" border="0">
		<tr>
         	<td align="bottom" width="20" background="../images/bar_filled.jpg" height="25">&#160;</td>
          	<td width="600" background="../images/bar_filled.jpg" height="25">
            <table height="8" cellSpacing="0" cellPadding="0" border="0">
            	<tr>
		         	<td valign="bottom" align="middle"><span class="barfont">EIONET</span></td>
		            <td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
		         	<td valign="bottom" align="middle"><span class="barfont">Data Dictionary</span></td>
					<td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
					<td valign="bottom" align="middle"><span class="barfont">Attributes</span></td>
					<td valign="bottom" width="28"><img src="../images/bar_dot.jpg"/></td>
				</tr>
			</table>
			</td>
		</tr>
		<tr><td>&#160;</td></tr>
	</table-->
	<br>
	<form name="form1">
	<table>
		<tr><td><b>Select attributes:</b></td></tr>
		<tr><td>&#160;</td></tr>
			<%
			for (int i=0; i<mAttributes.size(); i++){
				
				DElemAttribute attribute = (DElemAttribute)mAttributes.get(i);
				
				if (type.equals("")){		//search for all elements attributes
					if (!attribute.displayFor("AGG") &&
							!attribute.displayFor("CH1") &&
							!attribute.displayFor("CH2")) continue;					
				}
				else
					if (!attribute.displayFor(type)) continue;

				String attr_id = attribute.getID();
				if (selected.contains(attr_id)) continue;

				String attr_name = attribute.getShortName();

				%>
				<tr>
					<td align="left" width="300pts">	
						<a href='#' onClick='selectAttr(<%=attr_id%>, this)'>
							<%=attr_name%>
						</a>
					</td>
				</tr>
				<%
			}
			%>

		<tr><td>&#160;</td></tr>

	</table>
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