<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*"%>

<%!

private Vector attrs = null;
private Vector def_attrs = null;
private Vector attr_ids = null;
ServletContext ctx = null;
private String sel_attr = null;
private Hashtable inputAttributes=null;

			    
private String getAttributeIdByName(String name){
	
	for (int i=0; i<attrs.size(); i++){
		DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}

private String getAttributeNameById(String id){
	
	for (int i=0; i<attrs.size(); i++){
		DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getID().equals(id))
        	return attr.getShortName();
	}
        
    return null;
}

private String setDefaultAttrs(String name){

	String id = getAttributeIdByName(name);
	if (id!=null)
		def_attrs.add(id);

	return null;
}

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

	DDuser user = getUser(request);
	ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	Connection conn = DBPool.getPool(appName).getConnection();

	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

	attrs = searchEngine.getDElemAttributes();
	if (attrs == null) attrs = new Vector();
	
	attr_ids = new Vector();
	def_attrs = new Vector();

	setDefaultAttrs("Name");
	setDefaultAttrs("Definition");

	String attrID = null;
	String attrValue = null;
	String attrName = null;
	StringBuffer collect_attrs=new StringBuffer();

	String sel_attr = request.getParameter("sel_attr");
	String sel_type = request.getParameter("sel_type");
	String short_name = request.getParameter("short_name");
	
	
	if (sel_attr == null) sel_attr="";
	if (sel_type == null) sel_type="";
	if (short_name == null) short_name="";

	///get inserted attributes
	String input_attr;
	inputAttributes = new Hashtable();
	for (int i=0; i<attrs.size(); i++){	
		DElemAttribute attribute = (DElemAttribute)attrs.get(i);		
		String attr_id = attribute.getID();
		
		input_attr = request.getParameter("attr_" + attr_id);
		if (input_attr!=null){
			inputAttributes.put(attr_id, input_attr);
			attr_ids.add(attr_id);
		}
	}

%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
		attrWindow=null;

		function submitForm(action){
			
			document.forms["form1"].action=action;
			document.forms["form1"].submit();
		}

		function openAttributes(){
			var type = document.forms["form1"].type.value;
			var selected = document.forms["form1"].collect_attrs.value;
			attrWindow=window.open('pick_attribute.jsp?type=' + type + "&selected=" + selected,"Search","height=450,width=300,status=no,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
			if (window.focus) {attrWindow.focus()}
		}
		function checkalert()
		{
			if (attrWindow != null) {
			   if (!attrWindow.closed) attrWindow.focus();
			}
		}
		function selAttr(id, type){
			document.forms["form1"].sel_attr.value=id;
			document.forms["form1"].sel_type.value=type;
			submitForm('search_dataset.jsp');

		}
		
	</script>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0" onfocus="checkalert()">
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
                <jsp:param name="name" value="Search"/>
            </jsp:include>
            
			<div style="margin-left:30">
				<form name="form1" action="datasets.jsp" method="GET">
				<table width="500">
					<tr><td><font class="head00">Search for a dataset</font></td></tr>
					<tr height="10"><td>&#160;</td></tr>
					<tr>
						<td>
							
							To find out more about the search criteria provided below, please
							click on their titles.  It is possible to add more criteria by clicking
							the '+' button underneath. To remove added criteria, use the '-' buttons
							appearing next to them.
						</td>
					</tr>
					<tr height"10"><td>&#160;</td></tr>
					<tr><td colspan="3" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
				</table>
				
				<table width="auto" cellspacing="0">
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<a href="javascript:openShortName()"><span class="help">?</span></a>&#160;
							<span class="mainfont"><b>Short name</b></span>&#160;&#160;
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="40" name="short_name" value="<%=short_name%>"/>
						</td>
					</tr>

					<%
					//get default attributes, which are always on the page (defined above)
					if (def_attrs!=null){
						for (int i=0; i < def_attrs.size(); i++){
							attrID = (String)def_attrs.get(i);
							attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
							
							attrName = getAttributeNameById(attrID);

							if (inputAttributes.containsKey(attrID)) inputAttributes.remove(attrID);

							if (attrID!=null){
								collect_attrs.append(attrID + "|");
								%>
								<tr valign="top">
									<td align="right" style="padding-right:10">
										<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=view"><span class="help">?</span></a>&#160;
										<span class="mainfont"><b><%=attrName%></b></span>&#160;&#160;
									</td>
									<td colspan="2">
										<input type="text" class="smalltext" name="attr_<%=attrID%>" size="40"  value="<%=attrValue%>"/>
									</td>
								</tr>
								<%
							}
						}
					}
					// get attributes selected from picked list (get the ids from url)
					if (attr_ids!=null){
						for (int i=0; i < attr_ids.size(); i++){
							attrID = (String)attr_ids.get(i);
							 
							if (!inputAttributes.containsKey(attrID)) continue;
							if (sel_type.equals("remove") && attrID.equals(sel_attr)) continue;

							attrName = getAttributeNameById(attrID);

							attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
							if (attrValue == null) attrValue="";
							collect_attrs.append(attrID + "|");
							%>
							<tr valign="top">
								<td align="right" style="padding-right:10">
									<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=view"><span class="help">?</span></a>&#160;
									<span class="mainfont"><b><%=attrName%></b></span>&#160;&#160;
								</td>
								<td>
									<input type="text" class="smalltext" name="attr_<%=attrID%>" size="40"  value="<%=attrValue%>"/>
								</td>
								<td>
									<a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="../images/button_remove.gif" border="0" alt="Remove attribute from search criterias"/></a>
								</td>
							</tr>
							<%
						}
					}
					// add the last selection
					if (sel_type!=null && sel_attr!=null){
						if (sel_type.equals("add")){
							attrID = sel_attr;
							collect_attrs.append(attrID + "|");
							attrName = getAttributeNameById(attrID);
							%>
							<tr valign="top">
								<td align="right" style="padding-right:10">
									<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=edit"><span class="help">?</span></a>&#160;
									<span class="mainfont"><b><%=attrName%></b></span>&#160;&#160;
								</td>
								<td>
									<input type="text" class="smalltext" name="attr_<%=attrID%>" size="40" value=""/>
								</td>
								<td>
									<a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="../images/button_remove.gif" border="0" alt="Remove attribute from search criterias"/></a>
								</td>
							</tr>
							<%
						}
					}
					%>
					
					<tr height="10"><td colspan="3"></td></tr>
					
					<tr valign="top">
						<td></td>
						<td>
							<input class="mediumbuttonb" type="button" value="Search" onclick="submitForm('datasets.jsp')"/>
							<input class="mediumbuttonb" type="reset" value="Reset"/>
						</td>
						<td align="right">
							<a href="javascript:openAttributes();"><img src="../images/button_plus.gif" border="0" alt="Click here to add more search criterias"/></a>
						</td>
					</tr>
				</table>
				<!-- table for 'Add' -->
				
				<% if (user != null){ %>
					<table width="500">
						<tr height"10"><td>&#160;</td></tr>
						<tr><td style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
						<tr>
							<td valign="bottom">
								<input class="mediumbuttonb" type="button" value="Add" onclick="window.location.assign('dataset.jsp?mode=add')"/>
								&#160;&#160;<span class="head00">a new dataset</span>&#160;&#160;
							</td>
						</tr>
					</table>
				<% } %>
				<input type="hidden" name="sel_attr" value=""></input>			
				<input type="hidden" name="sel_type" value=""></input>
				<input type="hidden" name="type" value="DST"></input>
				<!--// collect all the attributes already used in criterias -->
				<input type="hidden" name="collect_attrs" value="<%=collect_attrs.toString()%>"></input>
                <input name='SearchType' type='hidden' value='SEARCH'/>
				</form>
			</div>
        </TD>
</TR>
</table>
</body>
</html>
