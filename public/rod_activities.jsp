<%@page contentType="text/html;charset=UTF-8" import="java.util.*,eionet.meta.inservices.*,eionet.util.Props,eionet.util.PropsIF,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
request.setCharacterEncoding("UTF-8");
Vector activities = (Vector)session.getAttribute(Attrs.ROD_ACTIVITIES);
//if (activities==null) activities = (Vector)request.getAttribute(Attrs.ACTIVITIES);

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
	// <![CDATA[
	
		function select(raID, raTitle,liID, liTitle){
			window.opener.submitAdd(raID, raTitle,liID, liTitle);
			window.close();
		}
		
	// ]]>
	</script>
</head>

<body class="popup">
<div id="pagehead">
    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
    <div id="networktitle">Eionet</div>
    <div id="sitetitle">Data Dictionary (DD)</div>
    <div id="sitetagline">This service is part of Reportnet</div>    
</div> <!-- pagehead -->
<div id="workarea">
<div>
	<form id="reload" action="InServices?client=webrod&amp;method=reload_activities" method="get">
	<div id="operations">
		<ul>
			<li><a href="javascript:window.close();">Close</a></li>
			<li><a href="javascript:document.forms['reload'].submit();">Reload</a></li>
		</ul>
	</div>
	<%
	if (activities==null || activities.size()==0){
		%>
		<h1>No activities found!</h1><%
	}
	else{ %>
		<h1>Obligations in ROD</h1>
		<div style="font-size:0.7em;clear:right;margin-bottom:10px">
			Click Title to link obligation with dataset.<br/>Click Details to open obligation details in ROD.
		</div>
		<table class="datatable" cellspacing="0" cellpadding="0" style="width:auto">
			<tr>
				<th style="padding-left:5px;padding-right:10px">Title</th>
				<th style="padding-left:5px;padding-right:10px">Details</th>
			</tr>
			<%
			int displayed = 0;
			for (int i=0; i<activities.size(); i++){
				
				Hashtable activity = (Hashtable)activities.get(i);
				
				String raID = (String)activity.get("PK_RA_ID");
				if (raID==null || raID.length()==0) continue;
				
				String terminated = (String)activity.get("terminated");
				if (terminated!=null && terminated.equals("1")) continue;
				
				String raTitle = (String)activity.get("TITLE");
				raTitle = raTitle==null ? "?" : raTitle;
				
				String liID = (String)activity.get("PK_SOURCE_ID");
				liID = liID==null ? "0" : liID;
				
				String liTitle = (String)activity.get("TITLE");
				liTitle = liTitle==null ? "?" : liTitle;
				
				String raURL = Props.getProperty(PropsIF.INSERV_ROD_RA_URLPATTERN);
				int j = raURL.indexOf(PropsIF.INSERV_ROD_RA_IDPATTERN);
				if (j==-1) throw new Exception("Invalid property " + PropsIF.INSERV_ROD_RA_URLPATTERN);
				raURL = new StringBuffer(raURL).
				replace(j, j + PropsIF.INSERV_ROD_RA_IDPATTERN.length(), raID).toString();
				
				String colorStyle = displayed % 2 != 0 ? ";background-color:#E6E6E6;" : "";
				%>
				<tr>
					<td style="padding-left:5px;padding-right:10px<%=colorStyle%>">
						<a href="javascript:select('<%=raID%>', '<%=Util.replaceTags(raTitle)%>', '<%=liID%>', '<%=Util.replaceTags(liTitle)%>')"><%=Util.replaceTags(raTitle)%></a>
					</td>
					<td style="padding-left:5px;padding-right:10px<%=colorStyle%>">
						<a href="<%=Util.replaceTags(raURL,true)%>"><%=Util.replaceTags(raURL,true)%></a>
					</td>
				</tr>
				<%
				displayed++;
			}
			%>
		</table><%
	}
	%>
	<div style="display:none">
		<input type="hidden" name="client" value="webrod"/>
		<input type="hidden" name="method" value="reload_activities"/>
	</div>	
	</form>
</div>
</div> <!-- workarea -->
</body>
</html>
