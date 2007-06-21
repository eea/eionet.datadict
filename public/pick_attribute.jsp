<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

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
	request.setCharacterEncoding("UTF-8");
		
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

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
	<head>
		<%@ include file="headerinfo.jsp" %>
		<title>Meta</title>
		<script language="javascript" type="text/javascript">
		// <![CDATA[
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
		// ]]>
		</script>
	</head>
   
<body class="popup" style="min-width:350px">
	<div id="pagehead">
	    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
	    <div id="networktitle">Eionet</div>
	    <div id="sitetitle">Data Dictionary (DD)</div>
	    <div id="sitetagline">This service is part of Reportnet</div>    
	</div> <!-- pagehead -->
	<div id="operations" style="margin-top:10px">
		<ul>
			<li><a href="javascript:window.close();">Close</a></li>
		</ul>
	</div>
	
	<div id="workarea" style="clear:right">
	<h2>Select attributes:</h2>
	<form name="form1" action="">
	<table>
			<%
			for (int i=0; i<mAttributes.size(); i++){
				
				DElemAttribute attribute = (DElemAttribute)mAttributes.get(i);
				
				if (type.equals("")){		//search for all elements attributes
					if (!attribute.displayFor("CH1") && !attribute.displayFor("CH2"))
						continue;
				}
				else
					if (!attribute.displayFor(type)) continue;

				String attr_id = attribute.getID();
				if (selected.contains(attr_id)) continue;

				String attr_name = attribute.getShortName();

				%>
				<tr>
					<td align="left" width="300pts">	
						<a href='#' onclick='selectAttr(<%=attr_id%>, this)'>
							<%=Util.replaceTags(attr_name)%>
						</a>
					</td>
				</tr>
				<%
			}
			%>

		<tr><td>&#160;</td></tr>

	</table>
	<input type="hidden" name="type" value="<%=type%>"/>
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
