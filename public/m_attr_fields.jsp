<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private Vector attrFields=null;%>

<%@ include file="history.jsp" %>

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
			
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			
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
	      					<h1>Error</h1><b>Not authorized to post any data!</b>
	      				</body>
	      				</html>
	      			<%
	      			return;
      			}
			}						
			
			String attr_id = request.getParameter("attr_id");
			
			if (attr_id == null || attr_id.length()==0){ %>
				<b>Attribute ID is missing!</b> <%
				return;
			}
			
			String attr_name = request.getParameter("attr_name");
			if (attr_name == null) attr_name = "?";
			
		
			if (request.getMethod().equals("POST")){

				Connection userConn = null;
								
				try{
					userConn = user.getConnection();
					
					MAttrFieldsHandler handler = new MAttrFieldsHandler(userConn, request, ctx);
					
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
					
				//String redirUrl = "" +
				//					"/m_attr_fields.jsp?attr_id=" + attr_id + "&attr_name=" + attr_name;
				String redirUrl = currentUrl;
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			attrFields = searchEngine.getAttrFields(attr_id);
			
			if (attrFields == null) attrFields = new Vector();
			
			String disabled = user == null ? "disabled" : "";
			%>

<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet_new.css" rel="stylesheet" type="text/css"/>
	</head>
	<script language="JavaScript" src='script.js'></script>
	<script language="JavaScript" src='dynamic_table.js'></script>
	<script language="JavaScript">
		function submitForm(mode){
				
			if (mode == "delete"){
				var b = confirm("This will delete all the fields you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		function start() {
			tbl_obj=new dynamic_table("tbl"); //create dynamic_table object
		}

		//call to dynamic table methods. Originated from buttons or click on tr.
		function sel_row(o){
			tbl_obj.selectRow(o);
		}
		function moveRowUp(){
			tbl_obj.moveup();
			setChanged();
		}
		function moveRowDown(){
			tbl_obj.movedown();
			setChanged();
		}
		function moveFirst(){
			tbl_obj.movefirst();
			setChanged();
		}
		function moveLast(){
			tbl_obj.movelast();
			setChanged();
		}
		function setChanged(){
			document.forms["form1"].elements["changed"].value = 1;
		}
		function getChanged(){
			return document.forms["form1"].elements["changed"].value;
		}
		function saveChanges(){
			tbl_obj.insertNumbers("pos_");
			submitForm("edit_pos");
		}
		function clickLink(sUrl){
			if (getChanged()==1){
				if(!confirm("This link leads you to the next page, but you have changed the order of elements.\n Are you sure you want to loose the changes?"))
					return;
			}
			window.location=sUrl;
		}	</script>
<body onload="start()">
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
                <jsp:param name="name" value="Complex attribute fields"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">

<form name="form1" method="POST" action="m_attr_fields.jsp">

<table width="500">
	<tr>
		<td>
			<span class="head00">Fields of <span class="title2" color="#006666"><%=attr_name%></span></span>
		</td>		
		<td align="right">
			<a target="_blank" href="help.jsp?screen=complex_attr_fields&area=pagehelp" onclick="pop(this.href)">
				<img src="images/pagehelp.jpg" border=0 alt="Get some help on this page" />
			</a>
		</td>
</table>

<table width="auto">
	<tr height="20"><td colspan="2"></td></tr>
	<tr><td colspan="2" class="smallFont">Enter a new field here:</td></tr>
	<tr>
		<td class="small" align="left">Name:</td>
		<td>
			<input type="text" size="20" name="new_field"></input>&#160;
			<%
			if (user!=null){
				%>
				<input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')">
				<%
			}
			else{
				%>
				<input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')" disabled="true"/>
				<%
			}
			%>
		</td>
	</tr>
	<tr>
		<td class="small" align="left">Definition:</td>
		<td>
			<textarea rows="2" cols="30" name="definition"></textarea>
		</td>
	</tr>
	<tr height="10"><td colspan="2"></td></tr>
</table>

<table width="auto" cellspacing="0" cellpadding="0" border="0"><tr><td rowspan="2">	
<table width="auto" id="tbl" cellspacing="0" cellpadding="0" >
	<%
	if (user != null) { %>
		<tr>
			<td colspan="4" style="padding-right:10">
				<input type="button" <%=disabled%> value="Remove selected" class="smallbutton" onclick="submitForm('delete')"/>
				<input type="button" value="Save order" class="smallbutton" onclick="saveChanges()" title="save the order of the fields"/>
			</td>
		</tr>
		<tr height="3"><td colspan="4"></td></tr>
		<%
	}
	%>
	<tr>
		<th>&nbsp;</th>
		<th style="padding-left:5;padding-right:5;border-left:0">Name</th>
		<th style="padding-left:5;padding-right:5">Definition</th>
		<th style="padding-left:5;padding-right:5;border-right:1 solid #FF9900">Priority</th>
	</tr>
	<tbody id="tbl_body">	
	
	<%
	
	//String position = String.valueOf(attrFields.size() + 1);
	int position = 0;
	
	for (int i=0; i<attrFields.size(); i++){
		Hashtable hash = (Hashtable)attrFields.get(i);
		String id = (String)hash.get("id");
		String name = (String)hash.get("name");
		String definition = (String)hash.get("definition");
		if (definition.length()>50) definition = definition.substring(0,50) + " ...";
		
		String fieldLink = "m_attr_field.jsp?mode=edit&attr_id=" + attr_id + "&attr_name=" + attr_name + "&field_id=" + id;
		
		int pos = Integer.parseInt((String)hash.get("position"));
		if (pos >= position) position = pos +1;
			
		String priority = (String)hash.get("priority");
		String pri = (priority!=null && priority.equals(DElemAttribute.FIELD_PRIORITY_HIGH)) ? "High" : "Low";
		%>
		<tr id="<%=id%>" onclick="tbl_obj.selectRow(this);" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<%
			if (user != null){ %>
				<td align="right" style="padding-left:5;padding-right:5">
					<input type="checkbox" style="height:13;width:13" name="del_field" value="<%=id%>" onclick="tbl_obj.clickOtherObject();"/>
				</td><%
			}
			%>
			<td align="center" style="padding-left:5;padding-right:5">
				<a href="javascript:clickLink('<%=fieldLink%>')">
					<%=Util.replaceTags(name)%>
				</a>
			</td>
			<td align="center" onmouseover="" style="padding-left:5;padding-right:5">
				<%=Util.replaceTags(definition)%>
			</td>
			<td align="center" onmouseover="" style="padding-left:5;padding-right:5">
				<%=pri%>
			</td>
			<td width="0" style="display:none">
				<input type="hidden" name="pos_id" value="<%=id%>" size="5">
				<input type="hidden" name="oldpos_<%=id%>" value='<%=(String)hash.get("position")%>' size="5">
				<input type="hidden" name="pos_<%=id%>" value="0" size="5">
			</td>
		</tr>
		<%
	}
	%>
	</tbody>	
</table>
	</td>
	<%
		if (user!=null && attrFields.size()>1){ %>
		<td align="left" style="padding-right:10" valign="top" height="10">&nbsp;</td>
		</tr><tr><td>
				<table cellspacing="2" cellpadding="2" border="0">
					<tr>
					</tr>
					<td>
						<a href="javascript:moveFirst()"><img src="images/move_first.gif" border="0" title="move selected row to top"/></a>			
					</td></tr>
					<td>
						<a href="javascript:moveRowUp()"><img src="images/move_up.gif" border="0" title="move selected row up"/></a>			
					</td></tr>
					<tr><td>
						<img src="images/dot.gif"/>
					</td></tr>
					<tr><td>
						<a href="javascript:moveRowDown()"><img src="images/move_down.gif" border="0" title="move selected row down"/></a>			
					</td>
					<tr><td>
						<a href="javascript:moveLast()"><img src="images/move_last.gif" border="0" title="move selected row last"/></a>			
						</td>
					</tr>
				<% } %>
			</table> 
		</td>
	</tr>
</table>

<input type="hidden" name="mode" value="add"></input>
<input type="hidden" name="position" value="<%=String.valueOf(position)%>"></input>

<input type="hidden" name="attr_id" value="<%=attr_id%>"/>
<input type="hidden" name="attr_name" value="<%=attr_name%>"/>
<input type="hidden" name="changed" value="0"/>
</form>
</div>
        </TD>
</TR>
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
