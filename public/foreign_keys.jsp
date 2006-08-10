<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private Vector elems=null;%>
<%!private ServletContext ctx=null;%>

<%@ include file="history.jsp" %>

<%

request.setCharacterEncoding("UTF-8");

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

XDBApplication.getInstance(getServletContext());
AppUserIF user = SecurityUtil.getUser(request);
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

//check if element id is specified
String delemID = request.getParameter("delem_id");
if (delemID == null || delemID.length()==0){ %>
	<b>Data element ID is missing!</b> <%
	return;
}

String delemName = request.getParameter("delem_name");
if (delemName == null || delemName.length()==0) delemName = "unknown";

String disabled = user == null ? "disabled" : "";

String dstID = request.getParameter("ds_id");

ctx = getServletContext();

//handle the POST

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

//handle the GET

String appName = ctx.getInitParameter("application-name");

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

elems = searchEngine.getFKRelationsElm(delemID, dstID);

StringBuffer collect_elems=new StringBuffer();

if (disabled.equals("")){
	boolean isWorkingCopy = searchEngine.isWorkingCopy(delemID, "elm");
	if (!isWorkingCopy) disabled = "disabled";
}

%>

<html>
<head>
	<%@ include file="headerinfo.txt" %>
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
	<jsp:include page="nlocation.jsp" flush='true'>
		<jsp:param name="name" value="Foreign keys"/>
		<jsp:param name="back" value="true"/>
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

	<table width="auto" cellspacing="0" cellpadding="0">
	
		<%
		String skipID = request.getParameter("orig_id");
		if (skipID!=null && skipID.length()>0) skipID = "&skip_id=" + skipID;
		%>
	
		<tr style="padding-bottom:2" >
			<td></td>
			<td colspan="3">
				<input type="button" <%=disabled%> class="smallbutton" value="Add" onclick="openAdd('search.jsp?fk=true&ctx=popup<%=skipID%>&noncommon')"/>
			</td>
		</tr>

		<tr>
			<td align="right" style="padding-right:10">
				<input type="button" <%=disabled%> value="Remove" class="smallbutton" onclick="submitForm('delete')"/>
			</td>				
			<th align="left" style="padding-left:5;padding-right:10">Element</th>
			<th align="left" style="padding-left:5;padding-right:10">Table</th>
			<th align="left" style="padding-left:5;padding-right:10;border-right:1px solid #FF9900">Cardinality</th>
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
