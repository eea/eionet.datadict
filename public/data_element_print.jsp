<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!final static String oConnName="datadict";%>
<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private DataElement dataElement=null;%>
<%!private Vector namespaces=null;%>
<%!private Vector fixedValues=null;%>
<%!private Vector subElems=null;%>
<%!private Vector complexAttrs=null;%>
<%!private Vector fxvAttributes=null;%>

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
	if (mode.equals("add")) return null;
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
			
		    String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
			
			
		
			String delem_id = request.getParameter("delem_id");
			
			mode = request.getParameter("mode");
			if (!mode.equals("add") && (delem_id == null || delem_id.length()==0)){ %>
				<b>Data element ID is missing!</b> <%
				return;
			}
			
			String type = request.getParameter("type");
			
			
			Connection conn = DBPool.getPool(appName).getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			String delem_name = "";
			
			Namespace namespace = null;
			
			dataElement = searchEngine.getDataElement(delem_id);
			if (dataElement!=null){
				type = dataElement.getType();
				//delem_name = dataElement.getAttributeValueByName("Name");
				delem_name = dataElement.getShortName();
				if (delem_name == null) delem_name = "unknown";
				if (delem_name.length() == 0) delem_name = "empty";
				namespace = dataElement.getNamespace();
			}
			else{ %>
				<b>Data element was not found!</b> <%
				return;
			}
			
			namespaces = searchEngine.getNamespaces();
			if (namespaces == null) namespaces = new Vector();
			
			DElemAttribute attribute = null;

			
			complexAttrs = searchEngine.getComplexAttributes(delem_id, "E");
			
			if (complexAttrs == null) complexAttrs = new Vector();
			
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
    <script language="JAVASCRIPT" for="window" event="onload">    
    
			<%
			String attrID = getAttributeIdByName("Datatype");
			String attrValue = getValue(attrID);
			
			if (dataElement != null){
				namespace = dataElement.getNamespace();
				if (namespace != null && mode.equalsIgnoreCase("add")){
					String namespace_id = namespace.getID();
				}
			}
			%>
			
	</script>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0" style="background-image: url('')">
<table border="0">
    <tr valign="top">
        <TD>
			<div style="margin-left:30">
			<form id="form1">
			
			<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
			
			<table width="auto">
				<tr>
					<td colspan="3">
						<font class="head00">Data element definition</font>
					</td>
				</tr>
				
				<tr height="10"><td colspan="3"></td></tr>
				
				<tr>
					<td colspan="3">
					(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.
					</td>
				</tr>
				
				<tr height="20"><td colspan="3"></td></tr>
				
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
					<b><font color="black">Type</font></b>:
				</td>
				<td colspan="2">
					<% if(type.equals("AGG")){ %>
						<b>AGGREGATE DATA ELEMENT</b>
					<% }else if (type.equals("CH1")){ %>
						<b>DATA ELEMENT WITH FIXED VALUES</b>
					<% }else if (type.equals("CH2")){ %>					
						<b>DATA ELEMENT WITH QUANTITATIVE VALUES</b>
					<% } else{ %>
						<b>AGGREGATE DATA ELEMENT</b>
					<% } %>
				</td>
			</tr>
			
			<tr height="10"><td colspan="3"></td></tr>
			
			<tr>		
				<td width="150">
					<b><font color="black">Short name</font></b>:&#160;(M)
				</td>
				<td colspan="1">
					<font class="title2" color="#006666"><%=delem_name%></font>
				</td>
				<td align="right"></td>
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
					
				</td>
			</tr>
			
			<%
			
			Vector classes = searchEngine.getDataClasses();
			if (classes != null && classes.size()!=0){
				
				String dataClassID = null;
				dataClassID = dataElement.getDataClass();
				
				%>
				<tr>		
					<td width="150">
						<b><font color="black">Class</font></b>:&#160;(O)
					</td>
					<td colspan="2">
							<%
							
							for (int i=0; i<classes.size(); i++){
								DataClass dataClass = (DataClass)classes.get(i);
								Namespace dataClassNs = dataClass.getNamespace();
								if (dataClassID != null && dataClassID.equals(dataClass.getID())){
									%>
										<%=dataClassNs.getShortName()%>:<%=dataClass.getShortName()%>
									<%
								}
							}
							%>
					</td>
				</tr>
				<%
			}			
			%>
								
			<%
				
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				
				if (!attribute.displayFor(type)) continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				
				String attrNs = attribute.getNamespace().getShortName();
				
				//String width  = attribute.getDisplayWidth();
				//String height = attribute.getDisplayHeight();
				
				//String disabled = user == null ? "disabled" : "";
				
				%>
				<tr>		
					<td width="150">
						<b><font color="black"><!--%=attrNs%>:--><%=attribute.getShortName()%></font></b>:&#160;(<%=attribute.getObligation()%>)
					</td>
					<td colspan="2">
						<% if (attrValue!=null){ %>
							<%=attrValue%>
						<% } %>
					</td>
				</tr>
			<% } %>
		</table>

<!-- COMPLEX ATTRIBUTES table -->
	<%
	if (complexAttrs.size() != 0){
		%>
		<table width="100%">
			<tr><td><hr></td></tr>
			<tr valign="bottom">
				<td><font class="head00">Complex attributes of <font class="title2" color="#006666"><!-- %=namespace.getShortName()%>: --><%=delem_name%></font></font></td>
			</tr>
			<tr height="10"><td></td></tr>
		</table>
	
	<%
		for (int i=0; i<complexAttrs.size(); i++){
		
			DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
			attrID = attr.getID();
			//String attrName = attr.getNamespace().getShortName() + ":" + attr.getShortName();
			String attrName = attr.getShortName();   //show short name without namespace
		
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

		<table width ="100%">
			<tr><td><hr></td></tr>
		</table>
		
		<table width="auto">

<!-- SUBELEMENTS table -->
		
		<% if (type.equals("AGG")){ // if AGG
			String seqID = dataElement.getSequence();
			String chcID = dataElement.getChoice();
			String relType=null;
			String sub_id=null;

			if (seqID != null){
				sub_id=seqID;
				relType="seq";
			} else if (chcID != null){
				sub_id=chcID;
				relType="chc";
			}
			
			String extID = dataElement.getExtension();
			DataElement extElem = null;
			if (extID != null)
				extElem = searchEngine.getDataElement(extID);
		%>
			
			<tr valign="bottom">
				<td colspan="3"><font class="head00">Subelements of <font class="title2" color="#006666"><!--%=namespace.getShortName()%>:--><%=delem_name%></font></font></td>
			</tr>
			<tr valign="bottom">
				<td colspan="3">
					<% if (relType!=null && relType.equals("seq")){%>
						Contents of this data element have been specified to form a <font color="black"><b>sequence</b></font>.
					<% }else if (relType!=null && relType.equals("chc")){ %>
						Contents of this data element have been specified to form a <font color="black"><b>choice</b></font>.
					<% } else { %>
						No content, nor its type has been specified for this data element yet.						
					<% }
					
					if (extElem != null){
						
						StringBuffer link = new StringBuffer("data_element_print.jsp?mode=print&delem_id=");
						link.append(extID);
						link.append("&type=");
						link.append(extElem.getType());
						
						if (relType != null){
							%>
							<br/>They extend the contents of <a href="<%=link%>"><!--%=extElem.getNamespace().getShortName()%>: --><%=extElem.getShortName()%></a>
							<%
						}
						else{
							%>
							<br/>But whatever it will be specified, it will extend the contents of <a href="<%=link%>"><!--%=extElem.getNamespace().getShortName()%>: --><%=extElem.getShortName()%></a>
							<%
						}
					}
					%>
				</td>
			</tr>
			<tr height="10"><td colspan="3">&#160;</td></tr>
		</table>
		<table width="520" border="1">
			<tr>
			
				<% if (relType!=null && relType.equals("seq")){
					%>
					<th width="320" colspan="2">ShortName</th>
					<th width="100">Min Occurs</th>
					<th width="100">Max Occurs</th>
					<%
				}
				else if (relType!=null && relType.equals("chc")){
					%>
					<th width="520" colspan="2">ShortName</th>
					<%
				}
				%>
			</tr>
		
			<%
		
			subElems=null;
			if (sub_id != null){
				if (relType.equals("seq"))
					subElems = searchEngine.getSequence(sub_id);
				else
					subElems = searchEngine.getChoice(sub_id);
			}
			else{
				ctx.log("sub_id=null");
			}
			
			if (subElems == null) subElems = new Vector();
		
			for (int i=0; i<subElems.size(); i++){
				
				Object o = subElems.get(i);
		        Class oClass = o.getClass();
		        String oClassName = oClass.getName();
        		
		        DataElement elem = null;
		        Hashtable child = null;
		        if (oClassName.endsWith("DataElement"))
			        elem = (DataElement)o;
		        else if (oClassName.endsWith("Hashtable"))
			        child = (Hashtable)o;
		        else
			        continue;
			        
				
			    String childName = "";
			    String childMinOcc = "";
			    String childMaxOcc = "";
			    
			    if (elem != null){
				    //childName = elem.getNamespace().getShortName() + ":" + elem.getShortName();
				    childName = elem.getShortName();
				    childMinOcc = elem.getMinOccurs();
				    childMaxOcc = elem.getMaxOccurs();
				    String childLink = "data_element_print.jsp?mode=print&delem_id=" + elem.getID() + "&type=" + elem.getType();
					if (relType!=null && relType.equals("seq")){			
						%>
						<tr>
							<td align="left" width="320" colspan="2"><a href="<%=childLink%>"><%=childName%></a></td>
							<td align="center" width="100"><%=childMinOcc%></td>
							<td align="center" width="100"><%=childMaxOcc%></td>
						</tr>
						<%
					}
					else{
						%>
						<tr>
							<td align="left" width="520" colspan="2"><%=childName%></td>
						</tr>
						<%
					}
			    }
			    else if (child != null){
				    String childType = (String)child.get("child_type");			    
				    childName = childType.equals("seq") ? "sequence_" : "choice_";
					String child_id = (String)child.get("child_id");
				    childName = childName + child_id;
				    childMinOcc = (String)child.get("child_min_occ");
				    childMaxOcc = (String)child.get("child_max_occ");
					if (relType!=null && relType.equals("seq")){			
						%>
						<tr>
							<td align="left" width="150"><%=childName%><br>(one of the data elements)</td>
							<td>
								<table width="auto">
									<tr>
										<th width="200">child</th>
									</tr>
									<%
									Vector childSubElems = searchEngine.getChoice(child_id);
									for (int j=0; j<childSubElems.size(); j++){		
										elem = (DataElement)childSubElems.get(j);
										//String childSubElemName = elem.getNamespace().getShortName() + ":" + elem.getShortName();
										String childSubElemName = elem.getShortName();
										String childSubElemLink = "data_element_print.jsp?mode=print&delem_id=" + elem.getID() + "&type=" + elem.getType();
										%>
										  <tr>
											<td align="center" width="200"><a href="<%=childSubElemLink%>"><%=childSubElemName%></a></td>
										  </tr>
										<%
									}
									%>
								</table>
							</td>
							<td align="center" width="100"><%=childMinOcc%></td>
							<td align="center" width="100"><%=childMaxOcc%></td>
						</tr>
					<%
						
					}
					else{
						%>
						<tr>
							<td align="left" width="250"><%=childName%><br>(list of data elements in specified order)</td>
							<td>
							<table width="auto">
							  <tr>
								<th width="200">child</th>
								<th width="100">Min Occurs</th>
								<th width="100">Max Occurs</th>
							  </tr>
						<%
							Vector childSubElems = searchEngine.getSequence(child_id);
							for (int j=0; j<childSubElems.size(); j++){		
								elem = (DataElement)childSubElems.get(j);
							    //String childSubElemName = elem.getNamespace().getShortName() + ":" + elem.getShortName();
							    String childSubElemName = elem.getShortName();
							    String childSubElemMinOcc = elem.getMinOccurs();
							    String childSubElemMaxOcc = elem.getMaxOccurs();

								%>
								  <tr>
									<td align="left" width="320"><%=childSubElemName%></td>
									<td align="center" width="100"><%=childSubElemMinOcc%></td>
									<td align="center" width="100"><%=childSubElemMaxOcc%></td>
								  </tr>
								<%
							}
							%>
							</table>
							</td>
						</tr>
						<%
					}
			    }
			    
			}
			%>

			
		<% } // end if AGG
		%>

		<!-- FIXED VALUES TABLE -->

		<% if (type.equals("CH1") && !mode.equals("add")){ // if CH1 and mode=add
			fxvAttributes = new Vector();

			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType != null &&
						attribute.displayFor("FXV")){
					fxvAttributes.add(attribute);
				}
			}

			fixedValues = searchEngine.getFixedValues(delem_id);		
			if (fixedValues == null) fixedValues = new Vector();

		%>
			<tr valign="bottom">
				<td colspan="3"><font class="head00">Allowable values of <font class="title2" color="#006666"><!--%=namespace.getShortName()%>: --><%=delem_name%></font></font></td>
			</tr>
			<tr height="10"><td colspan="3">&#160;</td></tr>
			<tr>
				<th width="100">Value</th>
				<%
					for (int i=0; fxvAttributes!=null && i<fxvAttributes.size(); i++){
						
						attribute = (DElemAttribute)fxvAttributes.get(i);
						%>
						<th width="150"><%=attribute.getShortName()%></th>
						<%
					}
				%>
			</tr>
	
			<%
			for (int i=0; i<fixedValues.size(); i++){
				FixedValue fxv = (FixedValue)fixedValues.get(i);
				String value = fxv.getValue();
				String fxvID = fxv.getID();
				String fxvAttrValue = null;
				String fxvAttrValueShort = null;


				%>
				<tr>
					<td align="center" width="100">
						<b>
							<%=value%>
						</b>
					</td>
					<%
					for (int c=0; fxvAttributes!=null && c<fxvAttributes.size(); c++){
			
						attribute = (DElemAttribute)fxvAttributes.get(c);
				
						fxvAttrValue = fxv.getAttributeValueByID(attribute.getID());
						if (fxvAttrValue==null || fxvAttrValue.length()==0)
						%>
							<td align="center" width="100" onmouseover=""></td>
						<%
						else{
							if (fxvAttrValue.length()>12) 
								fxvAttrValueShort = fxvAttrValue.substring(0,12) + " ...";
							else
								fxvAttrValueShort = fxvAttrValue;

							%>
							<td align="center" width="100" title="<%=fxvAttrValue%>"><%=fxvAttrValueShort%></td>
							<%
						}
					}
					%>
				</tr>
				<%
			}
			%>
		
		<% } // end if CH1 and mode=add
		%>
		
	</table>
		
		<input type="hidden" name="type" value="<%=type%>"/>
		<input type="hidden" name="mode" value="<%=mode%>"/>
		
	</form>
</div>
        </TD>
</TR>
</table>
</body>
</html>