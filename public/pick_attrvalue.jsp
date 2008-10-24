<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.sql.ConnectionUtil,eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
	response.setHeader("Expires", Util.getExpiresDateString());

	request.setCharacterEncoding("UTF-8");
	
	ServletContext ctx = getServletContext();			
	
	String type = request.getParameter("type");
	String attr_id = request.getParameter("attr_id");
	if (type == null) type = "?";

	if (attr_id == null || attr_id.length()==0) { %>
		<b>Attribute id paramater is missing!</b>
		<%
		return;
		
	}

	Connection conn = null;

	try { // start the whole page try block
		
	conn = ConnectionUtil.getConnection();
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
		 
	String requesterQrystr = request.getParameter("requester_qrystr");
	if (requesterQrystr==null){
		%>
		<b>Missing request parameter: requester_qrystr</b><%
		return;		
	}
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<%@ include file="headerinfo.txt" %>
		<title>Meta</title>
		<script type="text/javascript">
		// <![CDATA[

			function selectComplex(idx){
				
				var url = "complex_attr.jsp?<%=requesterQrystr%>";
				var field_ids = document.forms["form1"].elements["field_ids"];
				if (field_ids.length>0){
					for (var i=0; i<field_ids.length;i++){						
						field_id = document.forms["form1"].elements["field_ids"][i].value;
						url = url + "&<%=AttrFieldsHandler.FLD_PREFIX%>" + field_id + "=" + escape(document.forms["form1"].elements["field_"+field_id][idx+1].value);
					}
				}
				
				document.location.assign(url);
			}
			
		// ]]>
		</script>
	</head>

<body>
<div id="container">

	<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Pick attribute value"/>
	</jsp:include>
	<%@ include file="nmenu.jsp" %>

	<div id="workarea">
	
		<%
		StringBuffer parentLink = new StringBuffer();
		String dispParentType = request.getParameter("parent_type");
		if (dispParentType==null)
			dispParentType = "";
		else if (dispParentType.equals("DS")){
			dispParentType = "dataset";
			parentLink.append("dataset.jsp?ds_id=");
		}
		else if (dispParentType.equals("T")){
			dispParentType = "table";
			parentLink.append("dstable.jsp?table_id=");
		}
		else if (dispParentType.equals("E")){			
			dispParentType = "element";
			parentLink.append("data_element.jsp?delem_id=");
		}
		
		String dispParentName = request.getParameter("parent_name");
		if (dispParentName==null)
			dispParentName = "";
		
		if (parentLink.length()>0)
			parentLink.append(request.getParameter("parent_id")).append("&amp;mode=edit");
		
		%>
		<h2>You are selecting a value for <a href="complex_attr.jsp?<%=Util.replaceTags(requesterQrystr, true, true)%>"><%=attrName%></a> of <a href="<%=parentLink%>"><%=dispParentName%></a> <%=dispParentType%></h2>
		
		<div style="font-size:0.7em;clear:right;margin-bottom:10px;margin-top:10px">			
			Click the link in the first column of the value-row you want to select.<br/>
			Links in other columns are outside links.
		</div>
				
		<form id="form1" action="">
			<div style="overflow:auto">
			<table class="datatable" style="width:100%" cellspacing="0" cellpadding="0">
				<tr>
					<%
					for (int t=0; t<attrFields.size(); t++){
						Hashtable hash = (Hashtable)attrFields.get(t);
						String name = (String)hash.get("name");
						String f_id = (String)hash.get("id");
							%>
							<th align="left" style="padding-right:10">&nbsp;<%=Util.replaceTags(name)%>
								<input type="hidden" name="field_ids" value="<%=f_id%>"/>
								<input type="hidden" name="field_<%=f_id%>" value=" "/>
							</th><%
					}
					%>
				</tr>
				
				<%
				if (attrValues!=null && attrValues.size()>0){
					
					for (int j=0; attrValues!=null && j<attrValues.size();j++){
						
						String trStyle = (j%2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
						Hashtable rowHash = (Hashtable)attrValues.get(j);
						%>
						<tr <%=trStyle%>>
							<%
							for (int t=0; t<attrFields.size(); t++){
								
								Hashtable hash = (Hashtable)attrFields.get(t);
								String fieldID = (String)hash.get("id");
								String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
								if (fieldValue == null)
									fieldValue = "";
								%>
								<td style="padding-right:10">&nbsp;
									<%
									if (t==0){
										%>
										<a href="javascript:selectComplex(<%=j%>)"><%=Util.replaceTags(fieldValue, true)%></a><%
									}
									else{ %>
										<%=Util.replaceTags(fieldValue)%><%
									}
									%>
									<input type="hidden" name="field_<%=fieldID%>" value="<%=Util.replaceTags(fieldValue, true)%>"/>
								</td><%
							}					
							%>
						</tr><%
					}
				}
				%>	
				<tr><td>&nbsp;</td></tr>
			</table>
			</div>
		</form>
	</div>
</div>
<%@ include file="footer.txt" %>
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
