<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static String oConnName="datadict";%>
<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private DataClass dataClass=null;%>
<%!private Vector namespaces=null;%>
<%!private Vector classElems=null;%>
<%!private Vector complexAttrs=null;%>

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

private String getValue(String id){
	if (id==null) return null;
	DElemAttribute attr = dataClass.getAttributeById(id);
	if (attr == null) return null;
	return attr.getValue();
}

private String getAttributeObligationById(String id){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getID().equalsIgnoreCase(id))
        	return attr.getObligation();
	}
        
    return null;
}


private String legalizeAlert(String in){
        
    in = (in != null ? in : "");
    StringBuffer ret = new StringBuffer(); 
  
    for (int i = 0; i < in.length(); i++) {
        char c = in.charAt(i);
        if (c == '\'')
            ret.append("\\'");
        else if (c == '\\')
        	ret.append("\\\\");
        else
            ret.append(c);
    }

    return ret.toString();
}

%>

			<%
					
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
		    String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
					
			String class_id = request.getParameter("class_id");
					
			if (class_id == null || class_id.length()==0){ %>
				<b>Data class ID is missing!</b> <%
				return;
			}	
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			String class_name = "";
			
			Namespace namespace = null;
			
			dataClass = searchEngine.getDataClass(class_id);
			if (dataClass!=null){
				class_name = dataClass.getShortName();
				if (class_name == null) class_name = "unknown";
				if (class_name.length() == 0) class_name = "empty";
				namespace = dataClass.getNamespace();
			}
			else{ %>
				<b>Data Class was not found!</b> <%
				return;
			}
			
			namespaces = searchEngine.getNamespaces();
			if (namespaces == null) namespaces = new Vector();		
		
			complexAttrs = searchEngine.getComplexAttributes(class_id, "C");
			
			if (complexAttrs == null) complexAttrs = new Vector();

			Vector classElems = searchEngine.getClass2Elems(class_id);
			
			if (classElems == null) classElems = new Vector(); 
			%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
    
		
		
		function startsWith(str, pattern){
			var i = str.indexOf(pattern,0);
			if (i!=-1 && i==0)
				return true;
			else
				return false;
		}
	
    </script>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0" style="background-image: url('')">
<table border="0">
    <tr valign="top">
        <TD>
            
			<div style="margin-left:30">
			<form name="form1" method="POST" action="data_class.jsp">
			
				<input type="hidden" name="class_id" value="<%=class_id%>"/>
			
			<table width="auto">
				<tr>
					<td colspan="3"><font class="head00">Data class</font></td>
				</tr>
				<tr height="10"><td colspan="3"></td></tr>
				<tr>
					<td colspan="3">				
						(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.
					</td>
				</tr>
				<tr height="10"><td colspan="3"></td></tr>
			</table>
			
			<table width="auto">
					
			<tr height="10"><td colspan="3"></td></tr>
			
			<tr>				
				<td width="150">
					<b><font color="black">Short name</font></b>:&#160;(M)
				</td>
				<td colspan="2">
					<font class="title2" color="#006666"><%=class_name%></font>
					<input type="hidden" name="class_name" value="<%=class_name%>"/>
				</td>
			</tr>
			
			<tr>				
				<td width="150">
					<b><font color="black">Namespace</font></b>:&#160;(M)
				</td>
				<td colspan="2">
					<%				
					
					String nsDisp = "unknown";
					if (namespace != null){
						String nsUrl = namespace.getUrl();
						if (nsUrl.startsWith("/")) nsUrl = urlPath + nsUrl;
						nsDisp = namespace.getShortName() + " - " + nsUrl;
					}
					
						%>
						<font class="head0" color="#006666"><%=nsDisp%></font>
						<input type="hidden" name="ns" value="<%=namespace.getID()%>"/>
					
				</td>
			</tr>
			<%
			// dynamical display of attributes, really cool... I hope...

			DElemAttribute attribute = null;
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				
				if (!attribute.displayFor("AGG")) continue;
				
				String attrID = attribute.getID();
				String attrValue = getValue(attrID);
				
				String attrNs = attribute.getNamespace().getShortName();			
				
				%>
				<tr>
					<td width="150">
						<b><font color="black"><%=attrNs%>:<%=attribute.getShortName()%></font></b>:&#160;(<%=attribute.getObligation()%>)
					</td>
					<td colspan="2">
				<%
				if (attrValue==null){
					%>&#160;<%
				}
				else{
					%><%=attrValue%>
				<% } 
			}
			%>
					</td>
				</tr>
			</table>	
<!-- COMPLEX ATTRIBUTES table -->
	<%
	if (complexAttrs.size() != 0){
		%>
		<table width="100%">
			<tr><td><hr></td></tr>
			<tr valign="bottom">
				<td><font class="head00">Complex attributes of <font class="title2" color="#006666"><%=namespace.getShortName()%>:<%=class_name%></font></font></td>
			</tr>
			<tr height="10"><td></td></tr>
		</table>
	
	<%
		for (int i=0; i<complexAttrs.size(); i++){
		
			DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
			String attrID = attr.getID();
			String attrName = attr.getNamespace().getShortName() + ":" + attr.getShortName();
		
			Vector attrFields = searchEngine.getAttrFields(attrID);
		
			%>		
			<table border="0">
			<tr>
				<td><b>&#160;<%=attrName%></b></td>
			</tr>
			<tr>
				<td>
				<table border="0">
				<tr>
				<%
			
				for (int t=0; t<attrFields.size(); t++){
					Hashtable hash = (Hashtable)attrFields.get(t);
					String name = (String)hash.get("name");
					%>
					<th><%=name%></th>
					<%
				}
			
				%>
				</tr>
				<%
			
				Vector rows = attr.getRows();
				for (int j=0; rows!=null && j<rows.size(); j++){
					Hashtable rowHash = (Hashtable)rows.get(j);
					%>
					<tr>
					<%
					
					for (int t=0; t<attrFields.size(); t++){
						Hashtable hash = (Hashtable)attrFields.get(t);
						String fieldID = (String)hash.get("id");
						String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
						if (fieldValue == null) fieldValue = " ";
						%>
						<td><%=fieldValue%></td>
						<%
					}	
					%>
					</tr>				
					<%
				}
				%></table></td></tr>		
			</table>
			<% } %>
		<% } %>

<!-- ELEMENTS table -->
	<%
	if (classElems.size() != 0){
		%>
		<table width="auto">
			<tr><td><hr></td></tr>
			<tr valign="bottom">
				<td><font class="head00">Elements belonging to class <font class="title2" color="#006666"><%=namespace.getShortName()%>:<%=class_name%></font></font></td>
			</tr>
			<tr height="10"><td></td></tr>
			<tr>
				<th width="320">Namespace:ShortName</th>
			</tr>
	
			<%
	
			for (int i=0; i<classElems.size(); i++){
				DataElement elem = (DataElement)classElems.get(i);
				String elemName = elem.getShortName();
				if (elemName == null) elemName = "unknown";
				if (elemName.length()==0) elemName = "empty";
		
				Namespace ns = elem.getNamespace();
				String nsName = ns == null ? "unknown" : ns.getShortName();
				if (nsName == null) nsName = "unknown";
				if (nsName.length()==0) nsName = "empty";
		
				elemName = nsName + ":" + elemName;
				
				String elemLink = "data_element_print.jsp?mode=print&delem_id=" + elem.getID() + "&type=" + elem.getType();
				%>	
				<tr>
					<td align="center"><a href="<%=elemLink%>"><%=elemName%></a></td>
				</tr>
			<%
				}
			%>
		</table>
	<% } %>
	</form>
</div>
        </TD>
</TR>
</table>
</body>
</html>