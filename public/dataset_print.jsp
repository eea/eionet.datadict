<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static String oConnName="datadict";%>
<%!private Vector mAttributes=null;%>
<%!private DataElement dataElement=null;%>

<%!
private DElemAttribute getAttributeByName(String name){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr;
	}
        
    return null;
}

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
	DElemAttribute attr = dataElement.getAttributeById(id);
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
			
			String delem_id = request.getParameter("delem_id");
			
			if (delem_id == null || delem_id.length()==0){ %>
				<b>Dataset ID is missing!</b> <%
				return;
			}
			
			String type = "AGG";
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			String delem_name = "";
			
			dataElement = searchEngine.getDataElement(delem_id);
			if (dataElement!=null){
				delem_name = dataElement.getShortName();
				if (delem_name == null) delem_name = "unknown";
				if (delem_name.length() == 0) delem_name = "empty";
			}
			else{ %>
				<b>Dataset was not found!</b> <%
				return;
			}
			
			DElemAttribute attribute = null;
			String attrID = null;
			String attrValue = null;
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
			
			<table width="auto">
				<tr>
					<td colspan="3">
						<font class="head00">Dataset definition</font>
					</td>
				</tr>
				
				<tr height="10"><td colspan="3"></td></tr>
				
				<%
				String opening = request.getParameter("open");
				if (opening == null || opening.equals("false")){
					%>
					<tr height="20">
						<td align="left" colspan="3">
							<a href="javascript:history.back()">< back</a>
						</td>
					</tr>
					<tr height="20"><td colspan="3"></td></tr>
					<%
				}
				%>
				
			</table>
			
			<table width="auto">
			
			<tr>		
				<td width="150">
					<b><font color="black">Short name</font></b>:&#160;(M)
				</td>
				<td colspan="1">
					<font class="title2" color="#006666"><%=delem_name%></font>
				</td>
				<td align="right"></td>
			</tr>
			
			
			<%
				
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				
				String attrOblig = attribute.getObligation();
				if (attrOblig != null && !attrOblig.equals("M")) continue;
				
				if (!attribute.displayFor(type)) continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				
				String attrNs = attribute.getNamespace().getShortName();
				
				%>
				<tr>		
					<td width="150">
						<b><font color="black"><%=attrNs%>:<%=attribute.getShortName()%></font></b>:
					</td>
					<td colspan="2">
						<% if (attrValue!=null){ %>
							<%=attrValue%>
						<% } %>
					</td>
				</tr>
			<% } %>
		</table>

		
		<table width="auto">

<!-- start of SUBELEMENTS table -->
		
			<% 
		    Vector elems = null;
			String seqID = dataElement.getSequence();
			if (seqID !=null)
				elems = searchEngine.getSequence(seqID);
				
			if (elems!=null && elems.size()!=0){
				%>

				<tr height="10"><td colspan="3">&#160;</td></tr>
							
				<tr valign="bottom">
					<td colspan="3"><font class="head00">Elements in this dataset:</font></td>
				</tr>
			
				<tr>
					<th width="320">Short name</th>
					<th width="100">Namespace</th>
				</tr>
		
				<%
				for (int i=0; i<elems.size(); i++){
					DataElement elem = (DataElement)elems.get(i);
					String elemLink = "data_element_print.jsp?mode=print&delem_id=" + elem.getID() + "&type=" + elem.getType();
					%>
					<tr>
						<td align="center" width="320"><a href="<%=elemLink%>"><%=elem.getShortName()%></a></td>
						<td align="center" width="100"><%=elem.getNamespace().getShortName()%></td>
					</tr>
					<%
				}
			}
			else{
				%>
				<tr>
					<td colspan="2">No elements have yet been specified into this dataset.</td>
				</tr>
				<%
			}
			%>
			
		</table>
		
<!-- end of SUBELEMENTS table -->		
		
	</table>
</div>
</TD>
</TR>
</table>
</body>
</html>