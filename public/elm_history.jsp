<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

			<%
			
			request.setCharacterEncoding("UTF-8");
			
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
			String elmID = request.getParameter("id");
			if (elmID == null || elmID.length()==0){ %>
				<b>ID is missing!</b> <%
				return;
			}
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			DataElement elm = searchEngine.getDataElement(elmID);
			
			Vector v = searchEngine.getElmHistory(elm.getIdentifier(), elm.getVersion());
			if (v==null || v.size()==0){ %>
				<b>No history found for this element!</b> <%
				return;
			}
			
			%>

<html>
	<head>
		<%@ include file="headerinfo.txt" %>
		<title>Element history</title>
	    <script language="javascript" src='script.js'></script>
	<script language="javascript">
	// <![CDATA[
		function view(id){
			window.opener.location="data_element.jsp?mode=view&delem_id=" + id;
			window.close();
		}
	// ]]>
	</script>
	</head>
<body class="popup">
<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>

    <span class="head00">
        History of <em><%=Util.replaceTags(elm.getShortName())%></em>
        below CheckInNo <em><%=elm.getVersion()%></em>
    </span>

	<table width="auto" cellspacing="0" id="tbl">
	
		<tr><td colspan="3">&nbsp;</td></tr>
    	<tr>
    		<td align="right" colspan="3">
    			<a target="_blank" href="help.jsp?screen=history&area=pagehelp" onclick="pop(this.href)">
					<img src="images/pagehelp.jpg" border="0" alt="Get some help on this page" />
				</a>
    		</td>
    	</tr>
	
        <tr>
            <th align="left" style="padding-left:5;padding-right:10">CheckInNo</th>
            <th align="left" style="padding-left:5;padding-right:10">User</th>
            <th align="left" style="padding-left:5;padding-right:10;border-right:1px solid #FF9900">Date</th>
        </tr>
		
		<%
		for (int i=0; i<v.size(); i++){
			
			Hashtable hash = (Hashtable)v.get(i);
			String id = (String)hash.get("id");
			String version = (String)hash.get("version");
			String usr = (String)hash.get("user");
			String date = (String)hash.get("date");
			
			%>
			<tr>
				<td align="left" style="padding-left:5;padding-right:10">
					<a href="javascript:view('<%=id%>')">&#160;<%=version%>&#160;</a>
				</td>
				<td align="left" style="padding-left:5;padding-right:10"><%=usr%></td>
				<td align="left" style="padding-left:5;padding-right:10"><%=date%></td>
			</tr>
			<%
		}
		%>

	</table>

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
