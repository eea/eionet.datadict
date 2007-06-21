<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,java.net.URL,java.net.URLEncoder,java.net.MalformedURLException"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private Vector complexAttrs=null;%>

<%!
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
	request.setCharacterEncoding("UTF-8");
	
	ServletContext ctx = getServletContext();
	XDBApplication.getInstance(ctx);
	AppUserIF user = SecurityUtil.getUser(request);	
	
	// POST request not allowed for anybody who hasn't logged in			
	if (request.getMethod().equals("POST") && user==null){
		request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
		request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
		return;
	}

	// get some vital request parameters
	String parent_id = request.getParameter("parent_id");
	if (parent_id == null || parent_id.length()==0){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: parent_id");
		request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
		return;
	}	
	String parent_type = request.getParameter("parent_type");
	if (parent_type == null || parent_type.length()==0){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: parent_type");
		request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
		return;
	}
	String parent_name = request.getParameter("parent_name");
	String parent_ns = request.getParameter("parent_ns");
	String ds = request.getParameter("ds");
	
	// for getting inherited attributes
	String dataset_id = request.getParameter("dataset_id");
	if (dataset_id == null) dataset_id = "";
	String table_id = request.getParameter("table_id");
	if (table_id == null) table_id = "";
	
	// handle POST request
	if (request.getMethod().equals("POST")){
		Connection userConn = null;				
		try{
			userConn = user.getConnection();
			AttrFieldsHandler handler = new AttrFieldsHandler(userConn, request, ctx);			
			try{
				handler.execute();
			}
			catch (Exception e){
				String msg = e.getMessage();					
				ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(bytesOut));
				String trace = bytesOut.toString(response.getCharacterEncoding());					
				request.setAttribute("DD_ERR_MSG", msg);
				request.setAttribute("DD_ERR_TRC", trace);
				request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
				return;
			}
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}
		// dispatch the POST request
		String redirUrl = "complex_attrs.jsp?parent_id=" + parent_id +
													 "&parent_type=" + parent_type +
													 "&parent_name=" + parent_name +
													 "&parent_ns=" + parent_ns +
													 "&table_id=" + table_id +
													 "&dataset_id=" + dataset_id;
		response.sendRedirect(redirUrl);
		return;
	}
	//// end of handle the POST request, all following code deals with GET //////////////////////
	
	Connection conn = null;
	DBPoolIF pool = XDBApplication.getDBPool();
	
	// the whole page's try block
	try {	
		conn = pool.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		Vector mComplexAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
		if (mComplexAttrs == null)
			mComplexAttrs = new Vector();
		
		complexAttrs = searchEngine.getComplexAttributes(parent_id, parent_type, null, table_id, dataset_id);
		if (complexAttrs == null)
			complexAttrs = new Vector();
		
		for (int i=0; mComplexAttrs.size()!=0 && i<complexAttrs.size(); i++){
			DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
			String attrID = attr.getID();
			for (int j=0; j<mComplexAttrs.size(); j++){
				DElemAttribute mAttr = (DElemAttribute)mComplexAttrs.get(j);
				String mAttrID = mAttr.getID();
				if (attrID.equals(mAttrID)){
					mComplexAttrs.remove(j);
					j--;
				}
			}
		}		
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Complex attributes</title>
    <script type="text/javascript" src="script.js"></script>
	<script type="text/javascript">
	// <![CDATA[
			function submitForm(mode){
				
				if (mode == "delete"){
					var b = confirm("This will delete all the attributes you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;	
				}
				
				document.forms["form1"].elements["mode"].value = mode;
				document.forms["form1"].submit();
			}
			
			<% String redirUrl = ""; %>
			
			function addNew(){
				var id = document.forms["form1"].elements["new_attr_id"].value;
				var url = "<%=redirUrl%>" + "complex_attr.jsp?mode=add&attr_id=" + id + 
							"&parent_id=<%=parent_id%>&parent_type=<%=parent_type%>&parent_name=<%=parent_name%>&parent_ns=<%=parent_ns%>&table_id=<%=table_id%>&dataset_id=<%=dataset_id%>";
				
				<%
				if (ds!=null && ds.equals("true")){
					%>
					url = url + "&ds=true";
					<%
				}
				%>
				
				window.location.replace(url);
			}
			
			function edit(id){
				var url = "<%=redirUrl%>" + "complex_attr.jsp?mode=edit&attr_id=" + id + 
							"&parent_id=<%=parent_id%>&parent_type=<%=parent_type%>&parent_name=<%=parent_name%>&parent_ns=<%=parent_ns%>&table_id=<%=table_id%>&dataset_id=<%=dataset_id%>";
				<%
				if (ds!=null && ds.equals("true")){
					%>
					url = url + "&ds=true";
					<%
				}
				%>
				
				window.location.replace(url);
			}
			
			function load(){
				resize();
			}
			
			function resize(){
		    	window.resizeTo(700, 700);
			}
			
	// ]]>
	</script>
</head>
<body class="popup" onload="load()">
<div id="pagehead">
    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
    <div id="networktitle">Eionet</div>
    <div id="sitetitle">Data Dictionary (DD)</div>
    <div id="sitetagline">This service is part of Reportnet</div>    
</div> <!-- pagehead -->
<div id="workarea">

<div id="operations">
	<ul>
		<li><a href="javascript:window.close();">Close</a></li>
		<li class="help"><a target="_blank" href="help.jsp?screen=complex_attrs&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
	</ul>
</div>

<h1>Complex attributes of <em><%=Util.replaceTags(parent_name)%></em></h1>
<%
if (complexAttrs==null || complexAttrs.size() == 0){
	%>
	<p>None found!</p><%
}
%>

<div style="clear:right;padding-top:10px">
<form name="form1" method="post" action="complex_attrs.jsp">
	<%
	if (mComplexAttrs!=null && mComplexAttrs.size()>0){
		%>
		<select class="small" name="new_attr_id">
			<%
			for (int i=0; i<mComplexAttrs.size(); i++){
				DElemAttribute attr = (DElemAttribute)mComplexAttrs.get(i);
				String attrID = attr.getID();
				String attrName = attr.getShortName();
				
				String attrOblig = attr.getObligation();
				String obligStr  = "(O)";
				if (attrOblig.equalsIgnoreCase("M"))
					obligStr = "(M)";
				else if (attrOblig.equalsIgnoreCase("C"))
					obligStr = "(C)";
				%>
				<option value="<%=attrID%>"><%=Util.replaceTags(attrName)%>&nbsp;&nbsp;&nbsp;<%=Util.replaceTags(obligStr)%></option><%
			}
			%>
		</select>
		<input class="smallbutton" type="button" value="Add new" onclick="addNew()"/>
		<input class="smallbutton" type="button" value="Remove selected" onclick="submitForm('delete')"/><%
	}
	%>


<%
for (int i=0; i<complexAttrs.size(); i++){ // loop over attributes
		
		DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
		String attrID = attr.getID();
		String attrName = attr.getShortName();
		boolean inherit = attr.getInheritable().equals("1") ? true:false;
		
		Vector attrFields = searchEngine.getAttrFields(attrID);
		
		String attrOblig = attr.getObligation();
		String obligStr  = "optional";
		if (attrOblig.equalsIgnoreCase("M"))
			obligStr = "mandatory";
		else if (attrOblig.equalsIgnoreCase("C"))
			obligStr = "conditional";
		
		String obligImg = obligStr + ".gif";
		
		String inherited = null;
		Vector rows = attr.getRows();
		for (int j=0; rows!=null && j<rows.size(); j++){
			Hashtable rowHash = (Hashtable)rows.get(j);
			inherited = (String)rowHash.get("inherited");
			if (inherited!=null){
				if (inherited.equals("DS"))
					inherited = "(inherited from dataset)";
				else if (inherited.equals("DT"))
					inherited = "(inherited from table)";
				else
					inherited = null;
				break;
			}
		}

		%>
		
		<table cellspacing="0" class="datatable">
			<tr>
				<td align="right" valign="middle">
					<%
					if (inherited==null){%>
						<input type="checkbox" style="height:13;width:13" name="del_attr" value="<%=attrID%>"/><%
					}
					else{ %>
						&nbsp;<%
					}
					%>
				</td>
				<td valign="middle">
					<b>&#160;<%=Util.replaceTags(attrName)%></b>&nbsp;&nbsp;&nbsp;
					<%
					if (inherited==null){%>
						<img border="0" src="images/<%=Util.replaceTags(obligImg, true)%>" width="16" height="16" alt="<%=Util.replaceTags(obligStr, true)%>"/><%
					}
					else{ %>
						<%=inherited%><%
					}
					%>
				</td>
			</tr>
			<tr>
				<td valign="top" style="padding-right:3;padding-top:3;">
					<%
					if (inherited==null){%>
						<input class="smallbutton" type="button" value="Edit" onclick="edit('<%=attrID%>')"/><%
					}
					else{ %>
						&nbsp;<%
					}
					%>
				</td>				
				<td style="padding-left:3;padding-top:3">
					<table cellspacing="0">
						<tr>
						<%						
						for (int t=0; attrFields!=null && t<attrFields.size(); t++){
							Hashtable hash = (Hashtable)attrFields.get(t);
							String name = (String)hash.get("name");
							String style = "padding-right:10px";
							%>
							<th align="left" class="small"><%=Util.replaceTags(name)%></th>
							<%
						}
						%>
						</tr>
						
						<%
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
								fieldValue = Util.replaceTags(fieldValue);								
								%>
								<td class="small" style="padding-right:10" <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%>><%=Util.replaceTags(fieldValue)%></td>
								<%
							}
							%>
							</tr>				
							<%
						}
						%>
					</table>
				</td>
			</tr>
		</table>
		<%
	}
%>

<input type="hidden" name="mode" value="delete"/>

<input type="hidden" name="parent_id" value="<%=parent_id%>"/>
<input type="hidden" name="parent_name" value="<%=Util.replaceTags(parent_name, true)%>"/>
<input type="hidden" name="parent_type" value="<%=parent_type%>"/>
<input type="hidden" name="parent_ns" value="<%=parent_ns%>"/>
<input type="hidden" name="table_id" value="<%=table_id%>"/>
<input type="hidden" name="dataset_id" value="<%=dataset_id%>"/>

<%
if (ds != null){
	%>
	<input type="hidden" name="ds" value="<%=ds%>"/>
	<%
}
%>
															 
</form>
</div>
</div> <!-- workarea -->
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
