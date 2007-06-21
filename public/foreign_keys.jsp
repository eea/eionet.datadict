<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private Vector elems=null;%>
<%!private ServletContext ctx=null;%>

<%@ include file="history.jsp" %>

<%
	request.setCharacterEncoding("UTF-8");
	
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setDateHeader("Expires", 0);
	
	ServletContext ctx = getServletContext();
	XDBApplication.getInstance(ctx);
	AppUserIF user = SecurityUtil.getUser(request);	
	
	// POST request not allowed for anybody who hasn't logged in			
	if (request.getMethod().equals("POST") && user==null){
		request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
		request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
		return;
	}
	
	// get vital request parameters
	String delemID = request.getParameter("delem_id");
	if (delemID == null || delemID.length()==0){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: delem_id");
		request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
		return;
	}
	
	String delemName = request.getParameter("delem_name");
	String dstID = request.getParameter("ds_id");
	
	// handle POST request
	if (request.getMethod().equals("POST")){		
		Connection userConn = null;
		try{
			userConn = user.getConnection();
			FKHandler handler = new FKHandler(userConn, request, ctx);
			try{
				handler.execute();
			}
			catch (Exception e){
				e.printStackTrace(new PrintStream(response.getOutputStream()));
				return;
			}
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}
	}
	//// end of handle the POST request //////////////////////
	
	Connection conn = null;
	DBPoolIF pool = XDBApplication.getDBPool();
	
	// the whole page's try block
	try {	
		conn = pool.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		elems = searchEngine.getFKRelationsElm(delemID, dstID);
		StringBuffer collect_elems = new StringBuffer();
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Meta</title>
</head>

<script language="javascript" src='script.js'></script>

<script language="javascript">
// <![CDATA[
		function submitForm(mode){
			
			if (mode=="delete"){
				var b = confirm("This will delete the foreign key relations you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function openAdd(url){
			<%
			String selDS = dstID;
			if (selDS!=null){%>
				if (url != null) url = url + "&dataset=" + <%=selDS%>;<%
			}
			%>
			
			var selected = document.forms["form1"].collect_elems.value;
			if (url != null) url = url + "&selected=" + selected;
			
			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
			if (window.focus){
				wAdd.focus();
			}
		}
		
		function pickElem(id){
			document.forms["form1"].b_id.value=id;
			document.forms["form1"].mode.value="add";
			submitForm('add');
			return true;
		}
		
// ]]>
</script>
	
<body>
<div id="container">
	<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Foreign keys"/>
		
	</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">

<form acceptcharset="UTF-8" name="form1" method="POST" action="foreign_keys.jsp">
  <div id="operations">
    <ul>
        <li class="help"><a target="_blank" href="help.jsp?screen=foreign_keys&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
    </ul>
  </div>

	<h1>Foreign keys associated with
	<em><a href="data_element.jsp?mode=edit&delem_id=<%=delemID%>"><%=Util.replaceTags(delemName)%></a></em>.
</h1>

	<table cellspacing="0" cellpadding="0" style="width:auto;clear:right;margin-top:20px" class="datatable">
	
		<%
		String skipTableID = request.getParameter("table_id");
		if (skipTableID!=null)
			skipTableID = "&skip_table_id=" + skipTableID;
		else
			skipTableID = "";
		%>
	
		<tr style="padding-bottom:2" >
			<td></td>
			<td colspan="3">
				<input type="button" class="smallbutton" value="Add" onclick="openAdd('search.jsp?fk=true&ctx=popup<%=skipTableID%>&noncommon')"/>
			</td>
		</tr>

		<tr>
			<td align="right" style="padding-right:10">
				<input type="button" value="Remove" class="smallbutton" onclick="submitForm('delete')"/>
			</td>				
			<th style="padding-left:5px;padding-right:10px;text-align:left;">Element</th>
			<th style="padding-left:5px;padding-right:10px;text-align:left;">Table</th>
			<th style="padding-left:5px;padding-right:10px;text-align:left;">Cardinality</th>
		</tr>
			
		<%
		
		collect_elems.append(delemID + "|");
		for (int i=0; elems!=null && i<elems.size(); i++){
			
			Hashtable fkRel  = (Hashtable)elems.get(i);
			String fkElmID   = (String)fkRel.get("elm_id");
			String fkElmName = (String)fkRel.get("elm_name");
			String fkTblName = (String)fkRel.get("tbl_name");
			String fkRelID   = (String)fkRel.get("rel_id");
			
			String aCardin   = (String)fkRel.get("a_cardin");
			String bCardin   = (String)fkRel.get("b_cardin");
			String relDefin   = (String)fkRel.get("definition");
			String cardinality = aCardin + " to " + bCardin;
			
			if (fkElmID==null || fkElmID.length()==0)
				continue;
			
			collect_elems.append(fkElmID + "|");
			
			%>
			<tr>
				<td align="right" style="padding-right:10">
					<input type="checkbox" name="rel_id" value="<%=fkRelID%>"/>
				</td>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a href="data_element.jsp?delem_id=<%=fkElmID%>&amp;mode=view"><%=fkElmName%></a>
				</td>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<%=fkTblName%>
				</td>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a  title="<%=relDefin%>"
						href="fk_relation.jsp?rel_id=<%=fkRelID%>&amp;mode=view"><%=cardinality%></a>
				</td>
			</tr>
			<%
		}
		
		%>

	</table>
	
	<input type="hidden" name="mode" value="delete"/>
	<input type="hidden" name="delem_id" value="<%=delemID%>"/>
	<input type="hidden" name="delem_name" value="<%=delemName%>"/>
	<input type="hidden" name="ds_id" value="<%=dstID%>"/>
	
	<input type="hidden" name="a_id" value="<%=delemID%>"/>
	<input type="hidden" name="b_id" value=""/>
	
	<input type="hidden" name="collect_elems" value="<%=collect_elems.toString()%>"/>
	
	<%
	if (request.getParameter("table_id")!=null){ %>
		<input type="hidden" name="table_id" value="<%=request.getParameter("table_id")%>"/><%
	}
	%>
		
</form>
</div> <!-- workarea -->
</div> <!-- container -->
<jsp:include page="footer.jsp" flush="true" />
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
