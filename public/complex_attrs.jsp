<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,java.net.URL,java.net.URLEncoder,java.net.MalformedURLException"%>
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
			
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
			/*DDuser user = new DDuser(DBPool.getPool(appName));
	
			String username = "root";
			String password = "ABr00t";
			boolean f = user.authenticate(username, password);*/
			
			if (request.getMethod().equals("POST")){
      			if (user == null){
	      			%>
	      				<html>
	      				<body>
	      					<h1>Error</h1><p>Not authorized to post any data!</p>
	      				</body>
	      				</html>
	      			<%
	      			return;
      			}
			}						
			
			String parent_id = request.getParameter("parent_id");
			
			if (parent_id == null || parent_id.length()==0){ %>
				<span class="error">Parent ID is missing!</span> <%
				return;
			}
			
			String parent_type = request.getParameter("parent_type");
			
			if (parent_type == null || parent_type.length()==0){ %>
				<span class="error">Parent type is missing!</span> <%
				return;
			}
			
			String parent_name = request.getParameter("parent_name");
			if (parent_name == null) parent_name = "?";
			
			String parent_ns = request.getParameter("parent_ns");
			if (parent_ns == null) parent_ns = "?";
			
			String ds = request.getParameter("ds");
			
			// For getting inherited attributes
			String dataset_id = request.getParameter("dataset_id");
			if (dataset_id == null) dataset_id = "";
			String table_id = request.getParameter("table_id");
			if (table_id == null) table_id = "";

			if (request.getMethod().equals("POST")){

				Connection userConn = null;				
				try{
					userConn = user.getConnection();
					AttrFieldsHandler handler = new AttrFieldsHandler(userConn, request, ctx);
					
					try{
						handler.execute();
					}
					catch (Exception e){
						%>
						<html><body><b><%=e.toString()%></b></body></html>
						<%
						return;
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				String redirUrl = "complex_attrs.jsp?parent_id=" + parent_id +
															 "&parent_type=" + parent_type +
															 "&parent_name=" + parent_name +
															 "&parent_ns=" + parent_ns +
															 "&table_id=" + table_id +
															 "&dataset_id=" + dataset_id;
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
        	DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			Vector mComplexAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
			if (mComplexAttrs == null) mComplexAttrs = new Vector();
			
			complexAttrs = searchEngine.getComplexAttributes(parent_id, parent_type, null, table_id, dataset_id);
			
			if (complexAttrs == null) complexAttrs = new Vector();
			
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
			
			// JH170803
			// if the parent is not a working copy, its complex attributes cannot be edited.
			// so here we set the falg it is a working copy or not
			String _type = null;
			if (parent_type.equals("E"))
				_type="elm";
			else if (parent_type.equals("DS"))
				_type="dst";
			else if (parent_type.equals("T"))
				_type="tbl";
			boolean isWorkingCopy = _type==null ? true : searchEngine.isWorkingCopy(parent_id, _type);
			
			%>

<html>
<head>
	<%@ include file="headerinfo.txt" %>
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
<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>

<form name="form1" method="post" action="complex_attrs.jsp">
	<div id="operations">
		<ul>
				<li class="help"><a target="_blank" href="help.jsp?screen=complex_attrs&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
		</ul>
	</div>

    <h2>
        Complex attributes of <em><%=Util.replaceTags(parent_name)%></em>
    </h2>

<table width="600">

	<tr>
		<td class="mnd_opt_cnd" width="60%">
			<table border="0" width="100%" cellspacing="0">
				<tr>
					<td width="4%"><img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/></td>
					<td width="17%">Mandatory</td>
					<td width="4%"><img border="0" src="images/optional.gif" width="16" height="16" alt=""/></td>
					<td width="15%">Optional</td>
					<td width="4%"><img border="0" src="images/conditional.gif" width="16" height="16" alt=""/></td>
					<td width="56%">Conditional</td>
        		</tr>
      		</table>
		</td>
		<td align="right">
		</td>
	</tr>

	<%
	if (complexAttrs==null || complexAttrs.size() == 0){
		%>
		<tr height="10"><td colspan="2">None found!</td></tr>
		<%
	}
	%>
	
	
	<tr><td colspan="2">&nbsp;</td></tr>
	<%
	if (mComplexAttrs.size() != 0){
		%>
		<tr>
			<td colspan="2">
			<%
			if (user!=null){ %>
				<select class="small" name="new_attr_id"> <%
			} else{ %>
				<select class="small" name="new_attr_id" disabled="disabled"> <%
			}
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
						<option value="<%=attrID%>"><%=Util.replaceTags(attrName)%>&nbsp;&nbsp;&nbsp;<%=Util.replaceTags(obligStr)%></option>
						<%
					}
					%>
				</select>&#160;
				
				<%
				if (user != null && isWorkingCopy){ %>
					<input class="smallbutton" type="button" value="Add new" onclick="addNew()"/>&nbsp;
					<input class="smallbutton" type="button" value="Remove selected" onclick="submitForm('delete')"/><%
				}
				else{ %>
					<input class="smallbutton" type="button" value="Add new" disabled="disabled" />&nbsp;
					<input class="smallbutton" type="button" value="Remove selected" disabled="disabled"/><%
				}
				%>
				
			</td>
		</tr>
		
		<tr><td colspan="2">&nbsp;</td></tr>
		
		<%
	}
	%>
</table>

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
		
		%>
		
		<table cellspacing="0">
			<tr>
				<td align="right" valign="middle">
					<input type="checkbox" style="height:13;width:13" name="del_attr" value="<%=attrID%>"/>
				</td>
				<td valign="middle">
					<b>&#160;<%=Util.replaceTags(attrName)%></b>&nbsp;&nbsp;&nbsp;<img border="0" src="images/<%=Util.replaceTags(obligImg, true)%>" width="16" height="16" alt="<%=Util.replaceTags(obligStr, true)%>"/>
				</td>
			</tr>
			<tr>
				<td valign="top" style="padding-right:3;padding-top:3;">
					<%
					if (user != null && isWorkingCopy){
						%>
						<input class="smallbutton" type="button" value="Edit" onclick="edit('<%=attrID%>')"/>
						<%
					}
					else{
						%>&#160;
<!--						<input class="smallbutton" type="button" value="Edit" disabled/ -->
						<%
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
							String style = "padding-right:10";
							if (t == attrFields.size()-1)
								style = style + ";border-right:1px solid #FF9900";
							%>
							<th align="left" class="small" style="<%=style%>"><%=Util.replaceTags(name)%></th>
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
								//System.out.println("=================================================");
								//System.out.println("enne: " + fieldValue);
								fieldValue = Util.replaceTags(fieldValue);
								//System.out.println("pärast: " + fieldValue);
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
			<tr>
				<td colspan="2">&nbsp;</td>
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
