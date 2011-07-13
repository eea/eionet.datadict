<%@page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="eionet.util.sql.ConnectionUtil" %>
<%@ page import="eionet.meta.*" %>
<%@ page import="eionet.util.*" %>
<%@ page import="eionet.meta.notif.*" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

<%
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
response.setHeader("Expires", Util.getExpiresDateString());

Vector datasets = null;
Vector tables = null;
Vector commonElms = null;

DDUser user = null;
Connection conn = null;
ServletContext ctx = getServletContext();
try{
	conn = ConnectionUtil.getConnection();
	
	user = SecurityUtil.getUser(request);
	
	if (user!=null){
		
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);	
		searchEngine.setUser(user);
		// get datasets,
		// take out the ones that should be skipped by registration status
		HashSet nonSkippedDatasetIDs = new HashSet();
		Vector v = searchEngine.getDatasets();
		for (int i=0; v!=null && i<v.size(); i++){
			Dataset dst = (Dataset)v.get(i);
			if (!searchEngine.skipByRegStatus(dst.getStatus())){
				if (datasets==null) datasets = new Vector();
				datasets.add(dst);
				nonSkippedDatasetIDs.add(dst.getID());
			}
		}
		for (int i=0; datasets!=null && i<datasets.size(); i++){
			Dataset dst = (Dataset)datasets.get(i);
			dst.setComparation(dst.getShortName(), 1);
		}
		Collections.sort(datasets);	
		
		// get tables,
		// take out the ones in datasets that were skipped above
		int skippedTables = 0;
		v = searchEngine.getDatasetTables(null, null, null, null, null, null);
		for (int i=0; v!=null && i<v.size(); i++){
			DsTable tbl = (DsTable)v.get(i);			
			String dstID = tbl.getDatasetID();
			if (dstID!=null && nonSkippedDatasetIDs.contains(dstID)){
				if (tables==null) tables = new Vector();
				tables.add(tbl);
			}
			else{
				skippedTables++;
			}
		}
		for (int i=0; tables!=null && i<tables.size(); i++){
			DsTable tbl = (DsTable)tables.get(i);
			tbl.setCompStr(tbl.getShortName());
		}	
		Collections.sort(tables);
		
		// get common elements (we don't do skip-by-registration-status here,
		// because it's already done inside searchEngine.getCommonElements)
		commonElms = searchEngine.getCommonElements(null, null, null, null, false, null);
		for (int i=0; commonElms!=null && i<commonElms.size(); i++){
			DataElement elm = (DataElement)commonElms.get(i);
			elm.setComparation(elm.getShortName(), 1);
		}	
		Collections.sort(commonElms);
		
	} // end if (user!=null)
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {}
}
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Subscribe - Data Dictionary</title>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Subscribe"/>
    </jsp:include>
    <%@ include file="nmenu.jsp" %>
    
    <div id="workarea">
    
    	<h1>Get notifications in your email</h1>
    	
    	<%
    	if (user==null){
	    	%>
	    	<div class="error-msg">
	    		You must <a href="javascript:login()">log in</a> to be able to subscribe to any notifications!
	    	</div>
	    	<%
    	}
    	else{
	    	%>    	
	   		<form id="form" method="post" action="Subscribe">
	   	
		   		<%   		
		   		Object success = session.getAttribute("SUCCESS");
		   		if (success!=null){
			   		session.removeAttribute("SUCCESS");
			   		%>
			   		<div class="system-msg">Subscription successful!</div>
			   		<%	   		
		   		}
		   		String subscriptionsUrl = Props.getProperty(Subscribe.PROP_UNS_SUBSCRIPTIONS_URL);
		   		subscriptionsUrl = subscriptionsUrl + Props.getProperty(Subscribe.PROP_UNS_CHANNEL_NAME);
		   		%>
	   		
				<div class="note-msg">
			   		<strong>Note</strong>
					<p>This will make an additional subscription even if you have subscribed before.
					To change or delete your existing subscriptions, go to the <a href="<%=subscriptionsUrl%>">Unified Notification Service (UNS)</a>.
					</p>
				</div>
				<%
				if (user==null || !user.isAuthentic()){
					%>
					<p class="advise-msg">
						You are not authenticated!<br/>
						Definitions whose Registration status
						is not <em>Recorded</em> or <em>Released</em> are not listed for you!
					</p>
					<%
				}
				%>
				<fieldset>
					<legend>My interests:</legend>
				<table>
					<tr>
						<td>&nbsp;</td>
						<td>
							<input type="checkbox" name="new_datasets" id="new_datasets"/><label for="new_datasets">New datasets</label>
						</td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td>
							<input type="checkbox" name="new_tables" id="new_tables"/><label for="new_tables">New tables</label>
						</td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td>
							<input type="checkbox" name="new_common_elems" id="new_common_elems"/><label for="new_common_elems">New common elements</label>
						</td>
					</tr>
					</table>
				</fieldset>
				<fieldset>
					<legend>Changes in:</legend>
				<table>
					<tr>
						<td>
							<label for="dataset" class="question">Dataset</label>
						</td>
						<td>
							<%
							if (datasets==null || datasets.size()==0){
								%>
								no datasets found
								<%
							}
							else{
								%>
								<select name="dataset" id="dataset">
									<option value="_none_">-- none --</option>
									<option value="_all_">-- All datasets --</option>
									<%
									for (int i=0; i<datasets.size(); i++){
										Dataset dst = (Dataset)datasets.get(i);
										%>
										<option value="<%=dst.getIdentifier()%>"><%=Util.replaceTags(dst.getShortName())%></option>
										<%
									}
									%>
								</select>
								<%
							}
							%>
						</td>
					</tr>
					<tr>
						<td>
							<label for="table" class="question">Table</label>
						</td>
						<td>
							<%
							if (tables==null || tables.size()==0){
								%>
								no tables found
								<%
							}
							else{
								%>
								<select name="table" id="table">
									<option value="_none_">-- none --</option>
									<option value="_all_">-- All tables --</option>
									<%
									for (int i=0; i<tables.size(); i++){
										DsTable tbl = (DsTable)tables.get(i);									
										String text = tbl.getShortName();
										String dstShortName = tbl.getDatasetName();
										if (dstShortName!=null) text = text + " (" + dstShortName + ")";
										String value = tbl.getIdentifier();
										String dstIdf = tbl.getDstIdentifier();
										if (dstIdf!=null && dstIdf.length()>0)
											value = dstIdf + "/" + value;
										%>
										<option value="<%=value%>"><%=Util.replaceTags(text)%></option>
										<%
									}
									%>
								</select>
								<%
							}
							%>
						</td>
					</tr>
					<tr>
						<td>
							<label for="common_element" class="question">Common element</label>
						</td>
						<td>
							<%
							if (commonElms==null || commonElms.size()==0){
								%>
								no common elements found
								<%
							}
							else{
								%>
								<select name="common_element" id="common_element">
									<option value="_none_">-- none --</option>
									<option value="_all_">-- All common elements --</option>
									<%
									for (int i=0; i<commonElms.size(); i++){
										DataElement elm = (DataElement)commonElms.get(i);
										%>
										<option value="<%=elm.getIdentifier()%>"><%=Util.replaceTags(elm.getShortName())%></option>
										<%
									}
									%>
								</select>
								<%
							}
							%>
						</td>
					</tr>
					<tr>
						<td>
							<label for="reg_status" class="question">Registration Status</label>
						</td>
						<td>
							<select name="reg_status" id="reg_status">
								<option value="_none_">-- none --</option>
								<option value="_all_">-- All --</option>
								<option value="Recorded">Recorded</option>
								<option value="Qualified">Qualified</option>
								<option value="Released">Released</option>
								<option value="Incomplete">Incomplete</option>
								<option value="Candidate">Candidate</option>
							</select>
						</td>
					</tr>

				</table>
				</fieldset>
				<div style="margin-top:10px">
					<input type="submit" name="action" value="Subscribe"/>
				</div>
				
			</form>
			<%
		} // end if (user==null) else
		%>
    	
    </div> <!-- workarea -->
    </div> <!-- container -->
    
	<%@ include file="footer.txt" %>
</body>
</html>
