<%@page contentType="text/html;charset=UTF-8" import="java.util.*,eionet.meta.inservices.*,eionet.util.Props,eionet.util.PropsIF,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
request.setCharacterEncoding("UTF-8");
Vector activities = (Vector)session.getAttribute(Attrs.ROD_ACTIVITIES);
//if (activities==null) activities = (Vector)request.getAttribute(Attrs.ACTIVITIES);

%>

<html>
<head>
	<%@ include file="headerinfo.txt" %>
    <title>Data Dictionary</title>
    <script language="javascript" type="text/javascript">
	// <![CDATA[
	
		function select(raID, raTitle,liID, liTitle){
			window.opener.submitAdd(raID, raTitle,liID, liTitle);
			window.close();
		}
		
		function load(){
	    	resize();
    	}
    	
    	function resize(){
	    	window.resizeTo(600, 600);
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

<div>
	<form name="reload" action="InServices?client=webrod&amp;method=reload_activities" method="get">
	<%
	if (activities==null || activities.size()==0){ %>
		<p>
			<b>No activities found!</b>
		</p>&nbsp;&nbsp;<input type="submit" class="smallbutton" value="Reload"/><%
	}
	else{ %>
		<span class="head00">
			Obligation in ROD
		</span>&nbsp;&nbsp;<input type="submit" class="smallbutton" value="Reload"/><br/><br/>
		<p class="small">
			Click <i>Title</i> to link the obligation with dataset. Click <i>Details</i> to open the obligation's details in ROD.
		</p>
		<table width="auto" cellspacing="0" cellpadding="0">
			<tr>
				<th style="padding-left:5;padding-right:10">Title</th>
				<th style="padding-left:5;padding-right:10;border-right:1px solid #FF9900">Details</th>
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
				
				String colorAttr = displayed % 2 != 0 ? "bgcolor=#CCCCCC" : "";
				%>
				<tr>
					<td style="padding-left:5;padding-right:10" <%=colorAttr%>>
						<a href="javascript:select('<%=raID%>', '<%=raTitle%>', '<%=liID%>', '<%=liTitle%>')"><%=Util.replaceTags(raTitle)%></a>
					</td>
					<td style="padding-left:5;padding-right:10" <%=colorAttr%>>
						<a target="_blank" href="<%=raURL%>"><%=raURL%>
					</td>
				<tr>
				<%
				displayed++;
			}
			%>
		</table><%
	}
	%>
	
	<input type="hidden" name="client" value="webrod"/>
	<input type="hidden" name="method" value="reload_activities"/>
	
	</form>
</div>
</body>
</html>
