<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*,eionet.util.SecurityUtil,eionet.util.QueryString"%>

<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private Vector attributes=null;%>
<%!private Dataset dataset=null;%>
<%!private Vector complexAttrs=null;%>
<%!private Vector tables=null;%>
<%!private String currentUrl=null;%>

<%@ include file="history.jsp" %>

<%!

private String getValue(String id){
	if (id==null) return null;
	if (mode.equals("add")) return null;
	
	for (int i=0; attributes!=null && i<attributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)attributes.get(i);
		if (id.equals(attr.getID()))
			return attr.getValue();
	}
	
	return null;
}
private Vector getValues(String id){
	if (id==null) return null;
	if (mode.equals("add")) return null;

	for (int i=0; attributes!=null && i<attributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)attributes.get(i);
		if (id.equals(attr.getID()))
			return attr.getValues();
	}

	return null;
}

%>

			<%
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
			
		    String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
			

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
			
			String ds_id = request.getParameter("ds_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (!mode.equals("add") && (ds_id == null || ds_id.length()==0)){ %>
				<b>Dataset ID is missing!</b> <%
				return;
			}
			
			if (mode.equals("add")){
				if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){ %>
					<b>Not allowed!</b> <%
					return;
				}
			}
			if (mode.equals("edit")){
				if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + ds_id, "u")){ %>
					<b>Not allowed!</b> <%
					return;
				}
			}

			boolean wPrm = false;
			if (!mode.equals("add"))
				wPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + ds_id, "w");

			//// HANDLE THE POST //////////////////////
			
			if (request.getMethod().equals("POST")){
				
				Connection userConn = null;
				DatasetHandler handler = null;
				try {
					userConn = user.getConnection();
					handler = new DatasetHandler(userConn, request, ctx);
					handler.setUser(user);
					try {
						handler.execute();
					}
					catch (Exception e){
						handler.cleanup();
						//e.printStackTrace(new PrintStream(response.getOutputStream()));
						//return;
						
						String msg = e.getMessage();
							
						ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
						e.printStackTrace(new PrintStream(bytesOut));
						String trace = bytesOut.toString(response.getCharacterEncoding());
						
						String backLink = history.getBackUrl();
						
						request.setAttribute("DD_ERR_MSG", msg);
						request.setAttribute("DD_ERR_TRC", trace);
						request.setAttribute("DD_ERR_BACK_LINK", backLink);
						
						request.getRequestDispatcher("error.jsp").forward(request, response);
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				String redirUrl = "";
				
				if (mode.equals("add")){
					String id = handler.getLastInsertID();
					if (id != null && id.length()!=0)
						redirUrl = redirUrl + "dataset.jsp?mode=edit&ds_id=" + id;
					if (history!=null){
						int idx = history.getCurrentIndex();
						if (idx>0)
							history.remove(idx);
					}
				}
				else if (mode.equals("edit")){
					
					// if this was check in & new version was created,
					// or if this was an unlock, send to "view" mode
					QueryString qs = new QueryString(currentUrl);
					
					String checkIn = request.getParameter("check_in");
					String unlock  = request.getParameter("unlock");
					
					String newMode = "edit";
					if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
						newMode = "view";
						//JH041203 - remove previous url (with edit mode) from history
						history.remove(history.getCurrentIndex());
					}
					else if (unlock!=null && !unlock.equals("false"))
						newMode = "view";
					
					qs.changeParam("mode", newMode);
					redirUrl =qs.getValue();
				}
				else if (mode.equals("delete")){
					String deleteUrl = history.gotoLastMatching("datasets.jsp");
					redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
				}
				else if (mode.equals("force_status")){
					redirUrl = redirUrl + "dataset.jsp?mode=view&ds_id=" + ds_id;
				}
				
				response.sendRedirect(redirUrl);
				return;
			}
			
			Connection conn = null;
			XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = XDBApplication.getDBPool();
			
			try { // start the whole page try block
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			searchEngine.setUser(user);
			
			String ds_name = "";
			String version = "";
			String dsVisual = null;
			boolean imgVisual = false;
			
			if (!mode.equals("add")){
				
				dataset = searchEngine.getDataset(ds_id);
					
				if (dataset!=null){
					ds_name = dataset.getShortName();
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";
					
					version = dataset.getVersion();
					if (version == null) version = "unknown";
					if (version.length() == 0) version = "empty";
					
					// get the visual structure, so it will be displayed in the dataset view already
					dsVisual = dataset.getVisual();
					if (dsVisual!=null && dsVisual.length()!=0){
						int i = dsVisual.lastIndexOf(".");
						if (i != -1){
							String visualType = dsVisual.substring(i+1, dsVisual.length()).toUpperCase();
							if (visualType.equals("GIF") || visualType.equals("JPG") || visualType.equals("JPEG") || visualType.equals("PNG"))
								imgVisual = true;
						}
					}
				}
				else{ %>
					<b>Dataset was not found!</b> <%
					return;
				}
			}
			else{
				if (mAttributes.size()==0){ %>
					<b>No metadata on attributes found! Nothing to add.</b> <%
					return;
				}
			}
			
			// JH220803
			// version management
			VersionManager verMan = new VersionManager(conn, searchEngine, user);
			String latestID = dataset==null ? null : verMan.getLatestDstID(dataset);
			boolean isLatest = Util.voidStr(latestID) ? true : latestID.equals(dataset.getID());

			String workingUser = null;
			if (dataset!=null)
				workingUser = verMan.getDstWorkingUser(dataset.getShortName());
			
			// JH220803 - implementing check-in/check-out
			if (mode.equals("edit") && user!=null && user.isAuthentic()){
				// see if dataset is checked out
				if (Util.voidStr(workingUser)){
				    // dataset not checked out, create working copy
				    // but first make sure it's the latest version
				    if (!isLatest){ %>
				    	<b>Trying to check out a version that is not the latest!</b><%
				    	return;
				    }
				    
				    String copyID = verMan.checkOut(ds_id, "dst");
				    if (!ds_id.equals(copyID)){
					    // send to copy if created successfully
					    // But remove previous url (edit original) from history
					    history.remove(history.getCurrentIndex());
					    String qryStr = "mode=edit";
					    qryStr+= "&ds_id=" + copyID;
				        response.sendRedirect("dataset.jsp?" + qryStr);
			        }
			    }
			    else if (!workingUser.equals(user.getUserName())){
				    // dataset is chekced out by another user
				    %>
				    <b>This dataset is already checked out by another user: <%=workingUser%></b>
				    <%
				    return;
			    }
			    else if (dataset!=null && !dataset.isWorkingCopy()){
				    // Dataset is checked out by THIS user.
				    // If it's not the working copy, send the user to it.				    
				    String copyID = verMan.getWorkingCopyID(dataset);
				    if (copyID!=null && !copyID.equals(ds_id)){
					    // Before resending, remove previous url (edit original) from history.
					    history.remove(history.getCurrentIndex());
						String qryStr = "mode=edit";
						qryStr+= "&ds_id=" + copyID;
						response.sendRedirect("dataset.jsp?" + qryStr);
					}
			    }
			}
			
			attributes = searchEngine.getAttributes(ds_id, "DS", DElemAttribute.TYPE_SIMPLE);
			
			DElemAttribute attribute = null;
			String attrID = null;
			String attrValue = null;
			
			complexAttrs = searchEngine.getComplexAttributes(ds_id, "DS");		
			if (complexAttrs == null) complexAttrs = new Vector();
			tables = searchEngine.getDatasetTables(ds_id);
			
			%>

<html>
<head>
    <title>Data Dictionary</title>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
    
    	var dialogWin = null
    
    	function openSchema(){
			window.open("station.xsd",null, "height=400,width=600,status=no,toolbar=no,menubar=no,location=no,scrollbars=yes,top=100,left=100");
		}
		
		function deleteDatasetReady(){
			
			/*alert(document.forms["form1"].elements["complete"].value);
	    	return;*/
	    	
			document.forms["form1"].elements["mode"].value = "delete";
			document.forms["form1"].submit();
		}

		function submitForm(mode){
			
			if (mode == "delete"){
				var b;
				<%
				if (!mode.equals("add") && dataset.isWorkingCopy()){ %>
					b = confirm("This working copy will be deleted and the corresponding dataset released for others to edit! Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				else{ %>
					b = confirm("This dataset's latest version will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				%>
				if (b==false) return;
				
				<%
				if (dataset!=null && dataset.isWorkingCopy()){ %>
					document.forms["form1"].elements["complete"].value = "true";
					deleteDatasetReady();<%
				}
				else{ %>
					// now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
					dialogWin = window.open("dst_del_dialog.html", "", "height=130,width=400,status=yes,toolbar=no,scrollbars=no,resizable=yes,menubar=no,location=no,modal=yes");
					window.onfocus = checkModal;
					
					return;<%
				}
				%>
			}
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return;
				}
				
				if (hasWhiteSpace("ds_name")){
					alert("Short name cannot contain any white space!");
					return;
				}
				
				var identifierInputName = document.forms["form1"].IdentifierInputName.value;
						
				if (identifierInputName!=null && hasWhiteSpace(identifierInputName)){
					alert("Identifier cannot contain any white space!");
					return;
				}
				
				slctAllValues();
			}			
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}

		function checkModal() {
   			if (dialogWin!=null && !dialogWin.closed) 
      			dialogWin.focus()
		}


		function checkObligations(){
			
			var o = document.forms["form1"].delem_name;
			if (o!=null){
				if (o.value.length == 0) return false;
			}
			
			var o = document.forms["form1"].version;
			if (o!=null){
				if (o.value.length == 0) return false;
			}
			
			var elems = document.forms["form1"].elements;
			if (elems == null) return true;
			
			for (var i=0; i<elems.length; i++){
				var elem = elems[i];
				var elemName = elem.name;
				var elemValue = elem.value;
				if (startsWith(elemName, "attr_")){
					var o = document.forms["form1"].elements[i+1];
					if (o == null) return false;
					if (!startsWith(o.name, "oblig_"))
						continue;
					if (o.value == "M" && (elemValue==null || elemValue.length==0)){
						return false;
					}
				}
			}
			
			return true;
		}
		
		function hasWhiteSpace(input_name){
			
			var elems = document.forms["form1"].elements;
			if (elems == null) return false;
			for (var i=0; i<elems.length; i++){
				var elem = elems[i];
				if (elem.name == input_name){
					var val = elem.value;
					if (val.indexOf(" ") != -1) return true;
				}
			}
			
			return false;
		}			
		
		function startsWith(str, pattern){
			var i = str.indexOf(pattern,0);
			if (i!=-1 && i==0)
				return true;
			else
				return false;
		}
		
		function endsWith(str, pattern){
			var i = str.indexOf(pattern, str.length-pattern.length);
			if (i!=-1)
				return true;
			else
				return false;
		}

		function openTables(uri){
			uri = uri + "&open=true";
			wElems = window.open(uri,"DatasetTables","height=500,width=670,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wElems.focus()}
		}

		function complexAttrs(url){
					wComplexAttrs = window.open(url,"ComplexAttributes","height=600,width=500,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
					if (window.focus) {wComplexAttrs.focus()}
		}
		function complexAttr(url){
					wComplexAttrs = window.open(url,"ComplexAttribute","height=600,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
					if (window.focus) {wComplexAttrs.focus()}
		}
		
		function checkIn(){
			document.forms["form1"].elements["check_in"].value = "true";
			document.forms["form1"].elements["mode"].value = "edit";
			document.forms["form1"].submit();
		}
		
		function viewHistory(){
			var url = "dst_history.jsp?ds_id=<%=ds_id%>";
			window.open(url,null,"height=400,width=580,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=yes");
		}
		
		function goTo(mode, id){
			if (mode == "edit"){
				document.location.assign("dataset.jsp?mode=edit&ds_id=" + id);
			}
		}
		
		function rmvValue(id){

			var optns;
			var optnsLength;
				
			if (document.all){
				optns = document.all('attr_mult_' + id).options;
				optnsLength = optns.length;
			}
			else{
				optns = document.forms["form1"].elements["attr_mult_" + id].options;
				optnsLength = document.forms["form1"].elements["attr_mult_" + id].length;
			}

			var selected = new Array();
			var count=0;
			var i;

			for (i=0; i<optnsLength; i++){
				if (optns[i].selected){
					selected[count]=i;
					count++;
				}
			}

			count=0;
			for (i=0; i<selected.length; i++){
				if (document.all){
					document.all('attr_mult_' + id).options.remove(selected[i]-count);
				}
				else{
					document.forms["form1"].elements["attr_mult_" + id].options[selected[i]-count] = null;
				}
				
				count++;
			}
 			form_changed('form1');
 		}

		function addValue(id, val){

			if (val.length > 0){
				if (hasValue(id,val)){
					alert("There can not be dublicate values!");
					return
				}
				if (document.all){
					var oOption = document.createElement("option");				
					document.all('attr_mult_' + id).options.add(oOption);
					oOption.text = val;
					oOption.value = val;
					oOption.size=oOption.length;
				}
				else{
					var oOption = new Option(val, val, false, false);
					var slct = document.forms["form1"].elements["attr_mult_" + id]; 
					slct.options[slct.length] = oOption;
					slct.size=oOption.length;
				}
			}
 			form_changed('form1');
		}
		function slctAllValues(){
		
			var elems = document.forms["form1"].elements;
			if (elems == null) return true;
			
			for (var j=0; j<elems.length; j++){
				var elem = elems[j];
				var elemName = elem.name;
				if (startsWith(elemName, "attr_mult_")){
					
					if (document.all){
						var optns=document.all(elemName).options;
						for (var i=0; i<optns.length; i++){
							var optn = optns.item(i);
							optn.selected = "true";
						}
					}
					else{
						var slct = document.forms["form1"].elements[elemName];
						for (var i=0; i<slct.length; i++){
							slct.options[i].selected = "true";
						}
					}
				}
			}
		}
		function hasValue(id, val){
			
			var elemName = "attr_mult_" + id;
			if (document.all){
				var optns=document.all(elemName).options;
				for (var i=0; i<optns.length; i++){
					var optn = optns.item(i);
					if (optn.value == val)
						return true;
				}	
			}
			else{
				var slct = document.forms["form1"].elements[elemName];
				for (var i=0; i<slct.length; i++){
					if (slct.options[i].value == val)
						return true;
				}
			}
			return false;
		}
		function openAddBox(id, dispParams){
			attrWindow=window.open('multiple_value_add.jsp?id=' + id + '&' + dispParams,"Search","height=350,width=500,status=no,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
			if (window.focus) {attrWindow.focus()}
		}
		function openUrl(url){
			if (document.forms["form1"].elements["changed"].value=="1"){
				if (confirm_saving()){
					document.location=url;
				}
			}
			else
				document.location=url;
		}
		
		<%
		if (!mode.equals("add")){%>
			function unlockDataset(){
				
				var b = confirm('This will unlock the dataset for others to edit. ' +
								'Please note that you should be doing this only in ' +
								'extreme cases, where you are unable to unlock the dataset ' +
								'through normal checkout/checkin procedures. Click OK, if you ' +
								'still want to continue. Otherwise click Cancel.');
				
				if (b==false) return;
								
				document.forms["form1"].elements["unlock"].value = "<%=dataset.getNamespaceID()%>";
				document.forms["form1"].elements["mode"].value = "edit";
				document.forms["form1"].submit();
			}<%
		}
		%>
		
		function forceStatus(status){
			var b = confirm("This will force the '" + status + "' to lower levels as well, affecting all " +
								"tables and data elements within this dataset. Click OK, if you " +
								"still want to continue. Otherwise click Cancel.");				
			if (b==false) return;
			
			document.forms["form1"].elements["mode"].value = "force_status";
			document.forms["form1"].elements["force_status"].value = status;
			document.forms["form1"].submit();
		}
		
    </script>
</head>
<body>
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
                <jsp:param name="name" value="Dataset"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
			<form name="form1" id="form1" method="POST" action="dataset.jsp">
			
			<% if (!mode.equals("add")){ %>
				<input type="hidden" name="ds_id" value="<%=ds_id%>"/>
			<% } else { %>
				<input type="hidden" name="dummy"/>
			<% } %>
			
			<% String topWorkingUser = null; %>
			
			<table width="500" cellspacing="0">
				<tr>
						<%
						if (mode.equals("add")){ %>
							<td colspan="2"><span class="head00">Add a dataset definition</span></td> <%
						}
						else if (mode.equals("edit")){ %>
							<td colspan="2"><span class="head00">Edit dataset definition</span></td> <%
						}
						else{
							
							// set the flag indicating if the corresponding namespace is in use
							topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
							boolean topFree = topWorkingUser==null ? true : false;
				
							%>
							<td><span class="head00">View dataset definition</span></td>
							
							<td align="right">
								<input type="button" class="smallbutton" value="History" onclick="viewHistory()"/>&#160;
								<%
								if (user!=null && dataset!=null){
									
									boolean inWorkByMe = workingUser==null ?
											 false :
											 workingUser.equals(user.getUserName());
									
									String aclp = "/datasets/" + ds_id;
									if (SecurityUtil.hasPerm(user.getUserName(), aclp, "u")){
										if (dataset!=null && dataset.isWorkingCopy() ||
											(isLatest && topFree)   ||
											(isLatest && inWorkByMe)){ %>
											<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=ds_id%>')"/>&#160;<%
										}
									}
									
									if (SecurityUtil.hasPerm(user.getUserName(), aclp, "d")){
										if (dataset!=null && !dataset.isWorkingCopy() && isLatest && topFree){ %>
											<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/> <%
										}
									}
								}
								else{
									%>&#160;<%
								}
								%>
							</td>
							<%
						}
						%>
				</tr>
				
				<%
				if (dataset!=null && dataset.isWorkingCopy()){ %>
					<tr><td colspan="2"><font color="red"><b>WORKING COPY!!!</b></font></td></tr><%
				}
				if (!mode.equals("view")){ %>
				
					<tr height="5"><td colspan="2"></td></tr>
				
					<tr>
						<td colspan="2"><span class="Mainfont">
						(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.
						NB! Edits will be lost if you leave the page without saving!
						</span></td>
					</tr> <%
				}

				%>
				
				<tr height="5"><td colspan="2"></td></tr>
				
				<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
				
			</table>
			
			<table width="auto" cellspacing="0" cellpadding="0" border="0">
			
			<%
			int displayed = 0;
			%>
			
			<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a target="_blank" href="identification.html"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Short name</b>
						<%
						displayed++;
						if (!mode.equals("view")){
							%>
							&#160;(M)
							<%
						}
						%>
					</span>
				</td>
				<td colspan="2">
					<% if(!mode.equals("add")){ %>
						<font class="title2" color="#006666"><%=Util.replaceTags(ds_name)%></font>
						<input type="hidden" name="ds_name" value="<%=ds_name%>"/>
					<% } else{ %>
						<input class="smalltext" type="text" size="30" name="ds_name"></input>
					<% } %>
				</td>
			</tr>
			
			<%	
			// display Version, if not "add" mode.
			// Users cannot specify Version, it is always generated by the code.
			// First make sure you don't display Version for a status that doesn't require it.
			
			String regStatus = dataset!=null ? dataset.getStatus() : null;
			
			if (!mode.equals("add")){
				String dstVersion = dataset.getVersion();
				%>
				<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a target="_blank" href="identification.html#version"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Version</b>
							<%
							displayed++;
							if (!mode.equals("view")){
								%>
								&#160;(M)
								<%
							}
							%>
						</span>
					</td>
					<td colspan="2"><font class="title2" color="#006666"><%=dstVersion%></font></td>
				</tr>
				<%
			}
			
			// display Registration Status
			%>
			<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" valign="top" style="padding-right:10">
					<a target="_blank" href="statuses.html">
					<span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Registration status</b>
						<% if (!mode.equals("view")){ %>
							&#160;(M) <%
						} 
						displayed++;
						%>
					</span>
				</td>
				<td colspan="2">
					<%
					if (mode.equals("view")){ %>
						<span class="barfont" style="width:400">
							<%=regStatus%>
							<%
							if (user!=null && topWorkingUser==null && wPrm){ %>
								&#160;&#160;&#160;
								<a href="javascript:forceStatus('<%=regStatus%>')">&gt; force status to lower levels...</a><%
							}
							%>
						</span><%
					}
					else{ %>
						<select name="reg_status" onchange="form_changed('form1')"> <%
							Vector regStatuses = verMan.getRegStatusesOrdered();
							for (int i=0; i<regStatuses.size(); i++){
								String stat = (String)regStatuses.get(i);
								String selected = stat.equals(regStatus) ? "selected" : ""; %>
								<option <%=selected%> value="<%=stat%>"><%=stat%></option><%
							} %>
						</select><%
					}
					%>
				</td>
			</tr>
			
			
			<%

			// dynamical display of attributes, really cool... I hope...
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				
				String attrOblig = attribute.getObligation();
				
				if (!attribute.displayFor("DST")) continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				
				if (mode.equals("view") && (attrValue==null || attrValue.length()==0) && !attrOblig.equals("M"))
					continue;
				
				displayed++;
				
				String width  = attribute.getDisplayWidth();
				String height = attribute.getDisplayHeight();
				
				String disabled = user == null ? "disabled" : "";

				boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
				Vector multiValues=null;
				if (dispMultiple){
					multiValues = getValues(attrID);
				}
				
				%>
				<tr <% if (mode.equals("view") && displayed % 2 == 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" valign="top" style="padding-right:10">
						<a href="javascript:openUrl('delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=view')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><!--%=attrNs%>:--><b><%=attribute.getShortName()%></b>
							<%
							if (!mode.equals("view")){
								%>
								&#160;(<%=attrOblig%>)
								<%
							}
							%>
						</span>
					</td>
					
					<td colspan="2">
						<%				
						if (attribute.getShortName().equalsIgnoreCase("Identifier")){					
							%>
							<input type="hidden" name="IdentifierInputName" value="attr_<%=attrID%>"/>
							<%
						}
						
						// if mode is 'view', display a span, otherwise an input
						
						if (mode.equals("view")){
							if (dispMultiple){
								%>
									<span class="barfont" style="width:400">
								<%
								for (int k=0; multiValues!=null && k<multiValues.size(); k++){
									attrValue = (String)multiValues.get(k);
									%><%if (k>0)%>, <%;%><%=attrValue%><%
								}	
								%>										
								</span>
								<%
							}
							else{
							%>
								<span class="barfont" style="width:400"><%=Util.replaceTags(attrValue)%></span>
							<%
							}
						}
						else{ // start display input
							if (dispMultiple){
								%>
									<select <%=disabled%> name="attr_mult_<%=attrID%>" multiple="true" style="width:auto">
								<%
								for (int k=0; multiValues!=null && k<multiValues.size(); k++){
									attrValue = (String)multiValues.get(k);
									%>
									<option value="<%=attrValue%>"><%=attrValue%></option>
									<%
								}											
								%>
								</select>
								<% if (disabled.equals("")){ %>
									<a href="javascript:rmvValue('<%=attrID%>')"><img src="../images/button_remove.gif" border="0" title="Click here to remove selected value"/></a>
									<a href="javascript:openAddBox('<%=attrID%>', 'dispType=<%=dispType%>&#38;width=<%=width%>')"><img src="../images/button_plus.gif" border="0" title="Click here to add a new value"/></a>
								
								<%
								}
								if (dispType.equals("select")){ %>							
									<select class="small" name="hidden_attr_<%=attrID%>" style="display:none">
										<%
										Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
										if (fxValues==null || fxValues.size()==0){ %>
											<option selected value=""></option> <%
										}
										else{
											for (int g=0; g<fxValues.size(); g++){
												FixedValue fxValue = (FixedValue)fxValues.get(g);
												%>
												<option value="<%=fxValue.getValue()%>"><%=Util.replaceTags(fxValue.getValue())%></option> <%
											}
										}
										%>
									</select> <%
								}
								else if (dispType.equals("text")){ %>
									<select class="small" name="hidden_attr_<%=attrID%>" style="display:none">
										<%
										Vector attrValues = searchEngine.getSimpleAttributeValues(attrID);
										if (attrValues==null || attrValues.size()==0){ %>
											<option selected value=""></option> <%
										}
										else{
											for (int g=0; g<attrValues.size(); g++){
												%>
												<option value="<%=(String)attrValues.get(g)%>"><%=Util.replaceTags((String)attrValues.get(g))%></option> <%
											}
										}
										%>
									</select> <%
								}
							}
							else{
						
							if (dispType.equals("text")){
								if (attrValue!=null){
									%>
									<input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>" onchange="form_changed('form1')"/>
									<%
								}
								else{
									%>
									<input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"/>
									<%
								}
							}
							else if (dispType.equals("textarea")){
								if (attrValue!=null){
									%>
									<textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"><%=Util.replaceTags(attrValue, true)%></textarea>
									<%
								}
								else{
									%>
									<textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"></textarea>
									<%
								}
							}
							else if (dispType.equals("select")){ %>							
								<select <%=disabled%> class="small" name="attr_<%=attrID%>" onchange="form_changed('form1')">
									<%
									Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
									if (fxValues==null || fxValues.size()==0){ %>
										<option selected value=""></option> <%
									}
									else{
										boolean selectedByValue = false;
										for (int g=0; g<fxValues.size(); g++){
											FixedValue fxValue = (FixedValue)fxValues.get(g);
											
											String isSelected = (fxValue.getDefault() && !selectedByValue) ? "selected" : "";
											
											if (attrValue!=null && attrValue.equals(fxValue.getValue())){
												isSelected = "selected";
												selectedByValue = true;
											}
											
											%>
											<option <%=isSelected%> value="<%=fxValue.getValue()%>"><%=Util.replaceTags(fxValue.getValue())%></option> <%
										}
									}
									%>
								</select> <%
							}
							else{ %>
								<span class="barfont" style="width:400">Unknown display type!</span> <%
							}
						}
						} // end display input			
						%>
					</td>
				</tr>
				<input type="hidden" name="oblig_<%=attrID%>" value="<%=attribute.getObligation()%>"/>
				<%
			}
			%>
			
			<!-- COMPLEX ATTRIBUTES table -->
			<%
			if (mode.equals("view") && complexAttrs.size()> 0){
				%>
				<tr><td>&#160;</td><td>&#160;</td></tr>
				<%
				for (int i=0; i<complexAttrs.size(); i++){
	
					DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
					attrID = attr.getID();
					String attrName = attr.getShortName();   
					Vector attrFields = searchEngine.getAttrFields(attrID, DElemAttribute.FIELD_PRIORITY_HIGH);
		
					%>		
					<tr valign="top" <% if (displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<td align="right" style="padding-right:10">
							<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=COMPLEX&mode=view">
							<span class="help">?</span></a>&#160;
							<span class="mainfont"><b>
								<a href="javascript:complexAttr('complex_attr.jsp?attr_id=<%=attrID%>&#38;mode=view&#38;parent_id=<%=ds_id%>&#38;parent_type=DS&#38;parent_name=<%=ds_name%>&#38;ds=true')" title="Click here to view all the fields">
									<%=attrName%>
								</a></b>
							</span>
						</td>
						<td>
							<!--table width="auto" cellspacing="0">
								<tr-->
								<%
								/*
								for (int t=0; t<attrFields.size(); t++){
									Hashtable hash = (Hashtable)attrFields.get(t);
									String name = (String)hash.get("name");
									%>
									<th><%=name%></th>
									<%
								}
								*/
								%>
								<!--/tr-->
								<%
								displayed++;
								StringBuffer rowValue=null;
								
								Vector rows = attr.getRows();
								for (int j=0; rows!=null && j<rows.size(); j++){
									Hashtable rowHash = (Hashtable)rows.get(j);
									rowValue = new StringBuffer();
									%>
									<!--tr-->
									<%
							
									for (int t=0; t<attrFields.size(); t++){
										Hashtable hash = (Hashtable)attrFields.get(t);
										String fieldID = (String)hash.get("id");
										String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
										if (fieldValue == null) fieldValue = "";
										if (fieldValue.trim().equals("")) continue;
										
										if (t>0 && fieldValue.length()>0  && rowValue.toString().length()>0)
											rowValue.append(", ");
											
										rowValue.append(Util.replaceTags(fieldValue));
										
										String mark = (t == 0) ? "-":"&#160;";
										%>
										<!--td style="padding-right:10" <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<span class="barfont"><%=Util.replaceTags(fieldValue)%>
										</td-->
										
										<span class="barfont"><%=mark%> <%=Util.replaceTags(fieldValue)%></span><br>
										<%
									}	
									%>
									<!--/tr-->
									<!--span class="barfont">- <%=rowValue%></span><br-->
								<%
								}%>
							<!--/table-->
						</td>
					</tr>
				<%
				}
			}
			%>

			<%
		
			if (!mode.equals("add") && !mode.equals("view")
					|| (mode.equals("view") && user!=null)){ // if mode is not 'add'
			%>
			
			<tr height="5"><td colspan="2"></td></tr>
			<tr>
				<td>&#160;</td>
				<td>
					<b>*</b> <span class="smallfont"><a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=ds_id%>&#38;parent_type=DS&#38;parent_name=<%=ds_name%>&#38;ds=true')">
						<b>COMPLEX ATTRIBUTES</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						&lt;&#160;click here to view/edit complex attributes specified for this data element
					</span>
				</td>
			</tr>
			<%
			}
			%>
		<!-- DATASET data model -->
			<%if ((mode.equals("view") && dataset.getVisual()!=null)){
				%>
				<tr height="5"><td colspan="2"></td></tr>
				<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
				<tr valign="top">
					<td align="right" style="padding-right:10">
						<span class="mainfont"><b>Data model</b></span>
					</td>
					<td colspan="2">
						<% if (imgVisual){ %>
							<img src="../visuals/<%=dsVisual%>"/> <%
						}
						else{ %>
							The file representing the dataset stucture cannot be displayed on this web-page.
							But you can see it by pressing the following link:<br/>
							<a href="javascript:openStructure('../visuals/<%=dsVisual%>')"><%=dsVisual%></a> <%
						} %>
					</td>
				</tr>
				<%
			}
			 if (mode.equals("edit") && user!=null){
				 %>
				<tr height="5"><td colspan="2"></td></tr>
				<tr valign="top">
					<td align="right" style="padding-right:10">&#160;</td>
					<td colspan="2">
						<b>*</b> <span class="smallfont"><a href="javascript:openUrl('dsvisual.jsp?ds_id=<%=ds_id%>')">
							<b>DATA MODEL</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to view/modify<!-- if (mode.equals("edit")){ >/modify<}--> the dataset's model
						</span>
					</td>
				</tr>
				<%
			}
			%>
		<!-- DATASET TABLES table -->
		<% if (!mode.equals("add")){ // if CH1 and mode=add
			boolean bShowLink=false;
			if (mode.equals("view")){

			
				if (tables.size()>0){
					DElemAttribute attr = null;
	
					%>
					<tr height="15"><td colspan="2"></td></tr>
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<span class="mainfont"><b>Dataset tables</b></span><br/>
							<%
							if (user!=null && tables!=null && tables.size()>0){%>
								<span class="smallfont" style="font-weight: normal">
									(a red wildcard stands<br/>for checked-out table)
								</span><%
							}
							%>
						</td>
						<td>
							<table width="auto" cellspacing="0">
								<tr>
									<td align="right" style="padding-right:10"></td>
									<th align="left" style="padding-left:5;padding-right:10">Short name</th>
									<th align="left" style="padding-right:10">Full name</th>
									<th align="left" style="padding-right:10">Type</th>
								</tr>
								<%
								for (int i=0; tables!=null && i<tables.size(); i++){
			
									DsTable table = (DsTable)tables.get(i);
									String tableLink = "dstable.jsp?mode=view&table_id=" + table.getID() + "&ds_id=" + ds_id + "&ds_name=" + ds_name + "&ctx=ds";
			
									String tblName = "";
		
									attributes = searchEngine.getAttributes(table.getID(), "T", DElemAttribute.TYPE_SIMPLE);
		
									for (int c=0; c<attributes.size(); c++){
										attr = (DElemAttribute)attributes.get(c);
       									if (attr.getName().equalsIgnoreCase("Name"))
       										tblName = attr.getValue();
									}

									String tblFullName = tblName;
									tblName = tblName.length()>40 && tblName != null ? tblName.substring(0,40) + " ..." : tblName;
									
									String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(),
			    															  table.getShortName(), "tbl");

									String tblElmWorkingUser = searchEngine.getTblElmWorkingUser(table.getID());
									
									%>
									<tr>
										<td align="right" style="padding-right:5" bgcolor="#f0f0f0">
										
											<%
											if (user!=null && tblWorkingUser!=null){ // mark checked-out elements
												%> <font title="<%=tblWorkingUser%>" color="red">* </font> <%
											}
											else if (tblElmWorkingUser!=null){ // mark tables having checked-out elements
												%> <font title="<%=tblElmWorkingUser%>" color="red">* </font> <%
											}
											%>
											
										</td>
										<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<a href="<%=tableLink%>"><%=Util.replaceTags(table.getShortName())%></a>
										</td>
										<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=tblFullName%>">
											<span class="barfont"><%=Util.replaceTags(tblName)%></span>
										</td>
										<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<span class="barfont"><%=table.getType()%></span>
										</td>
									</tr>
								<%
								}
								%>
							</table>
						</td>
					</tr>
				<% } %>
			<% } %>
			
			<tr height="10"><td colspan="3"></td></tr>
			
			<%			
			if (!mode.equals("view")
					|| (mode.equals("view") && user!=null)){ // if mode is not 'add'
			%>
				<tr>
					<td>&#160;</td>
					<td colspan="2">
						<b>*</b> <span class="smallfont"><a href="javascript:openUrl('dstables.jsp?ds_id=<%=ds_id%>&#38;ds_name=<%=ds_name%>')">
							<b>TABLES</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to specify/remove tables of this dataset
						</span>
					</td>
				</tr> <%
			}
			
		} // if mode is not 'add'
		
		if (!mode.equals("view")){
			%>
			
			<tr height="15"><td colspan="3"></td></tr>
			
			<tr>
				<td>&#160;</td>
				<td colspan="2">
				
					<%
					
					boolean iPrm = user==null ? false : SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i");
					
					if (mode.equals("add")){ // if mode is "add"
						if (!iPrm){ %>
							<input class="mediumbuttonb" type="button" value="Add" disabled="true"/>&#160;
						<%} else {%>
							<input class="mediumbuttonb" type="button" value="Add" onclick="submitForm('add')"/>&#160;
						<% }
					} // end if mode is "add"
					
					if (!mode.equals("add")){ // if mode is not "add"
					
						String aclp = "/datasets/" + ds_id;
						boolean uPrm = user==null ? false : SecurityUtil.hasPerm(user.getUserName(), aclp, "u");
					
						if (!uPrm){ %>
							<input type="button" class="mediumbuttonb" value="Save" disabled="true"/>&#160;&#160;
							<%
							if (!dataset.isWorkingCopy()){ %>
								<input class="mediumbuttonb" type="button" value="Delete" disabled="true"/>&#160;&#160;<%
							}
							else{ %>
								<input class="mediumbuttonb" type="button" value="Check in" onclick="checkIn()" disabled="true"/>&#160;&#160;
								<input class="mediumbuttonb" type="button" value="Undo check-out" disabled="true"/>&#160;&#160;<%
							}
						} else {%>
							<input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
							<%
							if (!dataset.isWorkingCopy()){ %>
								<input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;<%
							}
							else{ %>
								<input class="mediumbuttonb" type="button" value="Check in" onclick="checkIn()"/>&#160;&#160;
								<input class="mediumbuttonb" type="button" value="Undo check-out" onclick="submitForm('delete')"/>&#160;&#160;<%
							}
						}
					} // end if mode is not "add"
					
					%>
					
				</td>
			</tr>
			<%
		}
		
		if (mode.equals("view")){
			%>
			<tr height="15"><td colspan="3"></td></tr>
			<tr height="20" valign="top">
				<td align="right" style="padding-right:10">
					<span class="mainfont"><b>Documentation</b></span>
				</td>
				<td colspan="2">
					* <a href="GetPrintout?format=PDF&obj_type=DST&obj_id=<%=ds_id%>">Create dataset factsheet (PDF)</a> <BR>
					* <a href="GetPrintout?format=PDF&obj_type=DST&obj_id=<%=ds_id%>&out_type=GDLN">Create full dataset specification (PDF)</a>
				</td>
			</tr>
			
			<%
			String userAgent = request.getHeader("User-Agent");
			if (userAgent != null && userAgent.length()!=0){
				int isMSIE = userAgent.toUpperCase().indexOf("MSIE");
				if (isMSIE != -1){
					//if (! userAgent.substring(isMSIE + 4).trim().startsWith("6")){
						%>
						<tr height="20" valign="top">
							<td></td>
							<td colspan="2">
								<span class="smallfont" style="font-weight: normal">
									! If you see a blank page instead of the PDF, try setting off your Acrobat Reader's Web integration.
									<br>Acrobat 6.0 is recommended.
								</span>
							</td>
						</tr>
						<%
					//}
				}
			}
			%>
			
			<tr height="15"><td colspan="3"></td></tr>
			<tr height="20" valign="top">
				<td align="right" style="padding-right:10">
					<span class="mainfont"><b>Templates</b></span>
				</td>
				<td colspan="2">
					* <a target="_blank" href="GetSchema?comp_id=<%=ds_id%>&comp_type=DST">Create an XML Schema</a>					
				</td>
			</tr>
			
			<tr height="15"><td colspan="3"></td></tr>

			<%
			boolean dispUnlock = user!=null && topWorkingUser!=null &&
										topWorkingUser.equals(user.getUserName());
			dispUnlock = dispUnlock && isLatest;
			dispUnlock = false; // for now we don't allow it. too risky.
			if (dispUnlock){ %>
				<tr>
					<td align="right" style="padding-right:10">
						<input type="button" class="smallbutton" value="Unlock" onclick="unlockDataset()" title="Unlock the dataset. Use only in exceptional cases where you are unable to release the dataset through normal checkout/checkin procedures.">
						</input>
					</td>
					<td colspan="2">&#160;</td>
				</tr><%
			}
		}
		
		//if (!mode.equals("add")){ // if mode is not "add"
		if (false){ // if mode is not "add"
			%>
			<tr height="20"><td colspan="3"></td></tr>
			<tr>
				<td colspan="3">
					<a href="javascript:alert('Under repairement!')">Printable page</a>
					<!--a href="javascript:printable('dataset_print.jsp?ds_id=<%=ds_id%>&open=true')">
						Printable page
					  </a-->
				</td>
			</tr>
			<%
		}
		%>

		<input type="hidden" name="mode" value="<%=mode%>"/>
		
		<input type="hidden" name="check_in" value="false"/>
		<input type="hidden" name="unlock" value="false"/>
		
		<input type="hidden" name="changed" value="0">
		
		<!-- Special input for 'delete' mode only. Inidcates if dataset(s) should be deleted completely. -->
		<input type="hidden" name="complete" value="false"/>
		
		<input type="hidden" name="force_status" value=""/>
		
	</table>
	</form>
</div>
        </TD>
</TR>
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
