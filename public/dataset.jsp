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
			
			mode=null;
			mAttributes=null;
			attributes=null;
			dataset=null;
			complexAttrs=null;
			tables=null;

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
				if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){
					%>
					<b>Not allowed!</b> <%
					return;
				}
			}
			
			boolean editPrm = false;
			boolean delPrm = false;

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
			
			String idfier = "";
			String ds_name = "";
			String version = "";
			String dsVisual = null;
			boolean imgVisual = false;
			
			if (!mode.equals("add")){
				
				dataset = searchEngine.getDataset(ds_id);
					
				if (dataset!=null){
					
					idfier = dataset.getIdentifier();
					if (idfier == null) idfier = "unknown";
					if (idfier.length() == 0) idfier = "empty";
					
					ds_name = dataset.getShortName();
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";
					
					version = dataset.getVersion();
					if (version == null) version = "unknown";
					if (version.length() == 0) version = "empty";
					
					editPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
					delPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
					
					if (mode.equals("edit") && !editPrm){ %>
						<b>Not allowed!</b> <%
						return;
					}
					
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
				workingUser = verMan.getDstWorkingUser(dataset.getIdentifier());
			
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
			tables = searchEngine.getDatasetTables(ds_id, true);
			
			// set a flag if element has history
			boolean hasHistory = false;
			if (mode.equals("edit") && dataset!=null){
				Vector v = searchEngine.getDstHistory(dataset.getIdentifier(), dataset.getVersion() + 1);
				if (v!=null && v.size()>0)
					hasHistory = true;
			}
			
			%>

<html>
<head>
    <title>Data Dictionary</title>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript" src='modal_dialog.js'></script>
    <script language="JavaScript">
    
    	var dlgwin = null;
    
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
					dlgwn = window.open("dst_del_dialog.html", "", "height=130,width=400,status=yes,toolbar=no,scrollbars=no,resizable=yes,menubar=no,location=no,modal=yes");
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
				
				if (hasWhiteSpace("idfier")){
					alert("Identifier cannot contain any white space!");
					return;
				}
				
				slctAllValues();
			}			
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}

		function checkModal() {
   			if (dlgwn!=null && !dlgwn.closed) 
      			dlgwn.focus()
		}


		function checkObligations(){
			
			var o = document.forms["form1"].ds_name;
			if (o!=null)
				if (o.value.length == 0) return false;
			
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
			
			//openDialog("yesno_dialog.html", "Do you want to increment the dataset's internal version?", retVersionUpd,100, 400);
			
			submitCheckIn();
		}
		
		function submitCheckIn(){
			document.forms["form1"].elements["check_in"].value = "true";
			document.forms["form1"].elements["mode"].value = "edit";
			document.forms["form1"].submit();
		}
		
		function retVersionUpd(){
			var v = dialogWin.returnValue;
			if (v==null) v=true;			
			document.forms["form1"].elements["upd_version"].value = v;
			
			submitCheckIn();
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
            </center></p>
        </td>
        <td>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Dataset"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
						
			<form name="form1" id="form1" method="POST" action="dataset.jsp">
			
				<%
				if (!mode.equals("add")){ %>
					<input type="hidden" name="ds_id" value="<%=ds_id%>"/><%
				}
				else { %>
					<input type="hidden" name="dummy"/><%
				}
				
				String topWorkingUser = null;
				
				String verb = "View";
				if (mode.equals("add"))
					verb = "Add";
				else if (mode.equals("edit"))
					verb = "Edit";
					
				%>
				
				<!--------------------------->
				<!-- main table inside div -->
				<!--------------------------->
				
				<table border="0" width="620" cellspacing="0" cellpadding="0">
				
					<!-- main table head -->
					<tr>
						<td colspan="2" align="right">
							<%
							String hlpScreen = "dataset";
							if (mode.equals("edit"))
								hlpScreen = "dataset_edit";
							else if (mode.equals("add"))
								hlpScreen = "dataset_add";
							%>
							<a target="_blank" href="help.jsp?screen=<%=hlpScreen%>&area=pagehelp"><img src="images/pagehelp.jpg" border=0 alt="Get some help on this page" /></a>
						</td>
					</tr>
	                <tr>
						<td width="72%" height="40" class="head1">
							<%=verb%> dataset definition
						</td>
						<td width="28%" height="40" align="right">
						
							<%
							if (mode.equals("view") && dataset!=null){
								if (user!=null){
									// set the flag indicating if the corresponding namespace is in use
									topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
									boolean topFree = topWorkingUser==null ? true : false;
										
									boolean inWorkByMe = workingUser==null ?
											 false :
											 workingUser.equals(user.getUserName());
									
									if (editPrm){
										if ((dataset!=null && dataset.isWorkingCopy()) ||
											(isLatest && topFree)   ||
											(isLatest && inWorkByMe)){ %>
											<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=ds_id%>')"/>&nbsp;<%
										}
									}
									
									if (delPrm){
										if (dataset!=null && !dataset.isWorkingCopy() && isLatest && topFree){ %>
											<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/>&nbsp;<%
										}
									}
								} %>
								<input type="button" class="smallbutton" value="History" onclick="viewHistory()"/><%
							}
							// the working copy part
							else if (dataset!=null && dataset.isWorkingCopy()){								
								%>
								<span class="wrkcopy">!!! Working copy !!!</span><%
							}
							%>
														
						</td>
	                </tr>
	                
	                <!-- mandatory/optional/conditional bar -->
	                
	                <%
					if (!mode.equals("view")){ %>
					
						<tr><td width="100%" height="10" colspan="2" ></td></tr>
						<tr>
							<td width="100%" class="mnd_opt_cnd" colspan="2" >
								<table border="0" width="100%" cellspacing="0">
									<tr>
										<td width="4%"><img border="0" src="images/mandatory.gif" width="16" height="16"/></td>
										<td width="17%">Mandatory</td>
										<td width="4%"><img border="0" src="images/optional.gif" width="16" height="16"/></td>
										<td width="15%">Optional</td>
										<td width="4%"><img border="0" src="images/conditional.gif" width="16" height="16"/></td>
										<td width="56%">Conditional</td>
                            		</tr>
		                            <tr>
										<td width="100%" colspan="6">
											<b>NB! Edits will be lost if you leave the page without saving!</b>
										</td>
		                            </tr>
                          		</table>
                        	</td>
						</tr><%
					}	
					%>
	                
	                <!-- add, save, check-in, undo check-out buttons -->
					
					<%
					if (mode.equals("add") || mode.equals("edit")){ %>					
						<tr>
							<tr><td width="100%" colspan="2" height="10"></td></tr>
							<td width="100%" align="right" colspan="2">
							<%
								// add case
								if (mode.equals("add")){
									boolean iPrm = user==null ? false : SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i");
									if (!iPrm){ %>
										<input class="mediumbuttonb" type="button" value="Add" disabled="true"/><%
									}
									else{ %>
										<input class="mediumbuttonb" type="button" value="Add" onclick="submitForm('add')"/><%
									}
								}
								// edit case
								else if (mode.equals("edit")){
									String isDisabled = editPrm ? "" : "disabled";
									%>
									<input type="button" class="mediumbuttonb" value="Save" <%=isDisabled%> onclick="submitForm('edit')"/>&nbsp;
									<%
									if (!dataset.isWorkingCopy()){ %>
										<input class="mediumbuttonb" type="button" value="Delete" <%=isDisabled%> onclick="submitForm('delete')"/>&nbsp;<%
									}
									else{ %>
										<input class="mediumbuttonb" type="button" value="Check in" onclick="checkIn()" <%=isDisabled%>/>&nbsp;
										<input class="mediumbuttonb" type="button" value="Undo check-out" <%=isDisabled%> onclick="submitForm('delete')"/><%
									}
								}
							%>
							</td>
						</tr>
						
						<%
						// update version checkbox
						if (mode.equals("edit") && dataset!=null && dataset.isWorkingCopy() && editPrm && hasHistory){ %>
							<tr>
								<td align="right" class="smallfont_light" colspan="2">
									<input type="checkbox" name="upd_version" value="true">&nbsp;Update version when checking in</input>
								</td>
							</tr><%
						}
					}
					%>
	                
	                <!-- main table body -->
	                
					<tr>
						<td width="100%" colspan="2" height="10">
							<table border="0" width="100%" cellspacing="0" cellpadding="3">
		                    
		                    	<!-- quick links -->
		                    	
		                    	<%
		                    	if (mode.equals("view")){
			                    	Vector quicklinks = new Vector();
			                    	
			                    	if (dataset!=null && dataset.getVisual()!=null)
			                    		quicklinks.add("Data model | model");
			                    	if (tables!=null && tables.size()>0)
			                    		quicklinks.add("Tables | tables");
			                    	if (complexAttrs!=null && complexAttrs.size()>0)
			                    		quicklinks.add("Complex attributes | cattrs");
			                    	
			                    	request.setAttribute("quicklinks", quicklinks);
			                    	%>
		                    		<jsp:include page="quicklinks.jsp" flush="true">
		                    		</jsp:include>
						            <%
								}
								%>
								
								<!-- pdfs & schema & docs -->
								
								<%
		                    	if (mode.equals("view")){ %>
		                    		<tr><td width="100%" height="10"></td></tr>
									<tr>
										<td width="100%" style="border: 1 solid #FF9900">
											<table border="0" width="100%" cellspacing="0">
												<tr>
													<td width="73%" valign="middle" align="left">
														Create technical specification for this dataflow
													</td>
													<td width="27%" valign="middle" align="left">
														<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;obj_id=<%=ds_id%>&amp;out_type=GDLN">
															<img border="0" src="images/icon_pdf.jpg" width="17" height="18"/>
														</a>
													</td>
												</tr>
												<tr>
													<td width="73%" valign="middle" align="left">
														Create an XML Schema for this dataflow
													</td>
													<td width="27%" valign="middle" align="left">
														<a target="_blank" href="GetSchema?id=DST<%=ds_id%>">
															<img border="0" src="images/icon_xml.jpg" width="16" height="18"/>
														</a>
													</td>
												</tr>
												
												<%
												Vector docs = searchEngine.getDocs(ds_id);
												for (int i=0; docs!=null && i<docs.size(); i++){
													Hashtable hash = (Hashtable)docs.get(i);
													String md5   = (String)hash.get("md5");
													String file  = (String)hash.get("file");
													String icon  = (String)hash.get("icon");												
													String title = (String)hash.get("title");
													%>
													<tr>
														<td width="73%" valign="middle" align="left"><%=title%></td>
														<td width="27%" valign="middle" align="left">
															<a target="_blank" href="DocDownload?file=<%=md5%>"><img border="0" src="images/<%=icon%>" width="16" height="18"/></a>
															<%
															if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u")){
																%>&nbsp;<a target="_blank" href="DocUpload?delete=<%=md5%>&idf=<%=dataset.getIdentifier()%>"><img border="0" src="images/delete.gif" width="14" height="14"/></a><%
															}
															%>
														</td>
													</tr>
													<%
												}
												%>
												
												<%
												if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u")){
													%>
													<tr height="20">
														<td colspan="2" valign="bottom" align="left">
															<span class="barfont">
																[ <a target="_blank" href="doc_upload.jsp?ds_id=<%=ds_id%>&idf=<%=dataset.getIdentifier()%>">Upload a document ...</a> ]
															</span>
														</td>
													</tr>
													<%
												}
												%>
											</table>
										</td>
									</tr><%
								}
								%>
								
								<!-- start dotted -->
								
								<tr><td width="100%" height="10"></td></tr>
								<tr>
									<td width="100%" style="border: 1 dotted #C0C0C0">
									
										<!-- attributes -->
										
										<%
										int displayed = 0;
										int colspan = mode.equals("view") ? 3 : 4;
										String titleWidth = colspan==3 ? "30" : "26";
										String valueWidth = colspan==3 ? "66" : "62";
										
										String isOdd = Util.isOdd(displayed);
										%>
										
										<table border="0" width="100%" cellspacing="0" cellpadding="3">
								  		
								  			<!-- static attributes -->
								  			
											<!-- short name -->
								    		<tr>
												<td width="<%=titleWidth%>%" class="short_name">Short name</td>
												<td width="4%" class="short_name">
													<a target="_blank" href="identification.html#short_name">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
													</a>
												</td>
												<%
												if (colspan==4){
													%>
													<td width="4%" class="short_name">
														<img border="0" src="images/mandatory.gif" width="16" height="16"/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="short_name_value">
													<%
													if (mode.equals("view")){ %>
														<%=Util.replaceTags(dataset.getShortName())%>
														<input type="hidden" name="ds_name" value="<%=dataset.getShortName()%>"/><%
													}
													else if (mode.equals("add")){%>
														<input class="smalltext" type="text" size="30" name="ds_name"/><%
													}
													else{ %>
														<input class="smalltext" type="text" size="30" name="ds_name" value="<%=dataset.getShortName()%>"/><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- RegistrationStatus -->
								    		<%
								    		String regStatus = dataset!=null ? dataset.getStatus() : null;
								    		%>
								    		<tr>
												<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
													RegistrationStatus
												</td>
												<td width="4%" class="simple_attr_help<%=isOdd%>">
													<a target="_blank" href="statuses.html">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<img border="0" src="images/mandatory.gif" width="16" height="16"/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
													<%
													if (mode.equals("view")){ %>														
														<%=regStatus%>
														<span class="barfont"><%
															if (user!=null && topWorkingUser==null && editPrm){ %>
																&nbsp;&nbsp;&nbsp;
																<a href="javascript:forceStatus('<%=regStatus%>')">
																	&gt; force status to lower levels...
																</a><%
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
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    										    		
								    		<!-- dynamic attributes -->
								    		
								    		<%
								    		for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
									    		
												attribute = (DElemAttribute)mAttributes.get(i);
												String dispType = attribute.getDisplayType();
												if (dispType == null) continue;
												
												String attrOblig = attribute.getObligation();
												String obligImg  = "optional.gif";
												if (attrOblig.equalsIgnoreCase("M"))
													obligImg = "mandatory.gif";
												else if (attrOblig.equalsIgnoreCase("C"))
													obligImg = "conditional.gif";
												
												if (!attribute.displayFor("DST")) continue;
												
												attrID = attribute.getID();
												attrValue = getValue(attrID);
												
												if (mode.equals("view") && (attrValue==null || attrValue.length()==0))
													continue;
												
												//displayed++; - done below
												
												String width  = attribute.getDisplayWidth();
												String height = attribute.getDisplayHeight();
												
												String disabled = user == null ? "disabled" : "";
								
												boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
												Vector multiValues=null;
												if (dispMultiple){
													multiValues = getValues(attrID);
												}
												
												%>
												
												<tr>
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														<%=attribute.getShortName()%>
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<!-- a target="_blank" href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=SIMPLE&amp;mode=view" -->
														<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help<%=isOdd%>">
															<img border="0" src="images/<%=obligImg%>" width="16" height="16"/>
														</td><%
													}
													%>
													
													<!-- dynamic attribute value display -->
													
													<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
														<%
														
														// if mode is 'view', display simple a text, otherwise an input						
														if (mode.equals("view")){
															if (dispMultiple){
																for (int k=0; multiValues!=null && k<multiValues.size(); k++){
																	attrValue = (String)multiValues.get(k);
																	%><%if (k>0)%>, <%;%><%=attrValue%><%
																}
															}
															else{ %>
																<%=Util.replaceTags(attrValue)%> <%
															}
														}
														else{ // start display input
														
															if (dispMultiple){ // mutliple display
																%>							
																<select <%=disabled%> name="attr_mult_<%=attrID%>" multiple="true" style="width:auto"> <%
								
																	for (int k=0; multiValues!=null && k<multiValues.size(); k++){
																		attrValue = (String)multiValues.get(k);
																		%>
																		<option value="<%=attrValue%>"><%=attrValue%></option> <%
																	}
																	%>
																	
																</select>
																
																<%
																if (disabled.equals("")){ %>
																	<a href="javascript:rmvValue('<%=attrID%>')"><img src="images/button_remove.gif" border="0" title="Click here to remove selected value"/></a>
																	<a href="javascript:openAddBox('<%=attrID%>', 'dispType=<%=dispType%>&amp;width=<%=width%>')"><img src="images/button_plus.gif" border="0" title="Click here to add a new value"/></a> <%
																}
																
																if (dispType.equals("select")){ %>
																
																	<select class="small" name="hidden_attr_<%=attrID%>" style="display:none"> <%
																		Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
																		if (fxValues==null || fxValues.size()==0){ %>
																			<option selected value=""></option> <%
																		}
																		else{
																			for (int g=0; g<fxValues.size(); g++){
																				FixedValue fxValue = (FixedValue)fxValues.get(g); %>
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
															else{ // no multiple display
															
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
																	</select>
																	<a target="_blank" href="fixed_values.jsp?mode=view&amp;delem_id=<%=attrID%>&amp;delem_name=<%=attribute.getShortName()%>&amp;parent_type=attr">
																		<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
																	</a>
																	<%
																}
																else{ %>
																	Unknown display type!<%
																}
																
															} // end of no multiple display
															
														} // end display input
														%>
													</td>
													
													<!-- end of dynamic attribute value display -->
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr>
									    		<input type="hidden" name="oblig_<%=attrID%>" value="<%=attribute.getObligation()%>"/>
									    		
									    		<!-- end of dynamic attribute row -->
												
												<%
											}
											%>
											
											<!-- version (or the so-called LastCheckInNo) -->
								    		<%
								    		if (!mode.equals("add")){
												String dstVersion = dataset.getVersion(); %>
									    		<tr>
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														LastCheckInNo
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="identification.html#version">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help<%=isOdd%>">
															<img border="0" src="images/mandatory.gif" width="16" height="16"/>
														</td><%
													}
													%>
													<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
														<%=dstVersion%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}
								    		%>
								    		
								    		<!-- Identifier -->
								    		<tr>
												<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
													Identifier
												</td>
												<td width="4%" class="simple_attr_help<%=isOdd%>">
													<a target="_blank" href="identification.html">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<img border="0" src="images/mandatory.gif" width="16" height="16"/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
													<%
													if(!mode.equals("add")){ %>
														<b><%=Util.replaceTags(idfier)%></b>
														<input type="hidden" name="idfier" value="<%=idfier%>"/><%
													}
													else{ %>
														<input class="smalltext" type="text" size="30" name="idfier"></input><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    												    
										</table>
										
										<!-- end of attributes -->
										
										<%
										boolean separ1displayed = false;
										%>										
										
										<!-- data model -->
										
										<%
										if ((mode.equals("edit") && user!=null) || (mode.equals("view") && dataset.getVisual()!=null)){
											
											// horizontal separator 1
											if (!separ1displayed){ %>
												<%@ include file="hor_separator.jsp" %><%
												separ1displayed = true;
											}
											%>
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
												<tr>													
													<td width="34%" valign="top"><a name="model"></a><b>Data model</b></td>
													<td width="66%" class="barfont">
														<%
														// thumbnail
														if (mode.equals("view") && dataset.getVisual()!=null){
															
															if (imgVisual){ %>
																<a target="_blank" href="visuals/<%=dsVisual%>" onFocus="blur()">
																	<img src="visuals/<%=dsVisual%>" border="0" height="100px" width="100px"/>
																</a><br/>
																[Click thumbnail to view large version of the data model]<%
															}
															else{ %>
																The file representing the dataset stucture cannot be displayed on this web-page.
																But you can see it by pressing the following link:<br/>
																<a href="javascript:openStructure('visuals/<%=dsVisual%>')"><%=dsVisual%></a> <%
															}
														}
														
														// model link
														if (mode.equals("edit") && user!=null){ %>
															[Click <a href="javascript:openUrl('dsvisual.jsp?ds_id=<%=ds_id%>')"><b>HERE</b></a> to manage the model of this dataset]
															<%
														}
														%>
													</td>
												</tr>												
											</table>
											<%
										}
										%>
										
										
										<!-- tables list -->
										
										<%
										if ((!mode.equals("add") && user!=null) || (mode.equals("view") && tables!=null && tables.size()>0)){
											
											colspan = user==null ? 1 : 2;
											
											// horizontal separator 1
											if (!separ1displayed){ %>
												<%@ include file="hor_separator.jsp" %><%
												separ1displayed = true;
											}
											%>
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
												
												<% if (mode.equals("view")){ %>
													<tr height="10"><td colspan="<%=String.valueOf(colspan)%>"></td></tr><%
												} %>
												
												<tr>
													<td width="34%">
														<b>Dataset tables<a name="tables"></a></b>
													</td>
													
													<%
													// tables link
													if (user!=null){ %>
														<td class="barfont" width="66%">
															[Click <a href="javascript:openUrl('dstables.jsp?ds_id=<%=ds_id%>&amp;ds_name=<%=ds_name%>')"><b>HERE</b></a> to manage tables of this dataset]
														</td><%
													}
													%>
												</tr>
												
												<%
												// tables table
												if (mode.equals("view") && tables!=null && tables.size()>0){
													%>
													<tr>
										      			<td width="100%" colspan="<%=String.valueOf(colspan)%>">
															<table border="1" width="100%" cellspacing="0" bordercolorlight="#C0C0C0" bordercolordark="#C0C0C0">
													        
																<tr>
																	<th width="50%" class="dst_tbls">Full name</th>
																	<th width="50%" class="dst_tbls">Short name</th>
																</tr>
																
																<%
																boolean hasMarkedTables = false;
																for (int i=0; tables!=null && i<tables.size(); i++){
																				
																	DsTable table = (DsTable)tables.get(i);
																	String tableLink = "dstable.jsp?mode=view&table_id=" + table.getID() + "&ds_id=" + ds_id + "&ds_name=" + ds_name + "&ctx=ds";
											
																	String tblName = "";
										
																	attributes = searchEngine.getAttributes(table.getID(), "T", DElemAttribute.TYPE_SIMPLE);
										
																	for (int c=0; c<attributes.size(); c++){
																		DElemAttribute attr = (DElemAttribute)attributes.get(c);
								       									if (attr.getName().equalsIgnoreCase("Name"))
								       										tblName = attr.getValue();
																	}
								
																	String tblFullName = tblName;
																	tblName = tblName.length()>40 && tblName != null ? tblName.substring(0,40) + " ..." : tblName;
																	
																	String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(),
											    															  table.getIdentifier(), "tbl");
								
																	String tblElmWorkingUser = searchEngine.getTblElmWorkingUser(table.getID());																
																	%>
																																		
																	<tr>
																		<td width="50%" class="dst_tbls">
																			<%
																			if (user!=null && tblWorkingUser!=null){ // mark checked-out elements
																				%> <font title="<%=tblWorkingUser%>" color="red">* </font><%
																				hasMarkedTables = true;
																			}
																			else if (tblElmWorkingUser!=null){ // mark tables having checked-out elements
																				%> <font title="<%=tblElmWorkingUser%>" color="red">* </font><%
																				hasMarkedTables = true;
																			}
																			%>
																			<a href="<%=tableLink%>">																				
																				<%=Util.replaceTags(tblName)%>
																			</a>
																		</td>
																		<td width="50%" class="dst_tbls">
																			<%=Util.replaceTags(table.getShortName())%>
																		</td>
																	</tr>																	
																	<%
																}
																%>
													          															
													        </table>
														</td>
													</tr>
												
													<%
													if (user!=null && tables!=null && tables.size()>0 && hasMarkedTables){%>
														<tr height="10">
															<td width="100%" class="barfont" colspan="<%=String.valueOf(colspan)%>">
																(a red wildcard stands for checked-out table)
															</td>
														</tr><%
													}
												}
												%>
												
											</table><%
										}
										%>
										
										
										<!-- complex attributes -->
										
										<%
										if ((mode.equals("edit") && user!=null) || (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0)){
											
											colspan = user==null ? 1 : 2;
											%>
											
											<!-- horizontal separator 2 -->
											<%@ include file="hor_separator.jsp" %>
											
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
												<tr>
													<td width="34%">
														<b>Complex attributes<a name="cattrs"></a></b>
													</td>
													
													<%
													// the link
													if (mode.equals("edit") && user!=null){ %>
														<td class="barfont" width="66%">
															[Click <a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=ds_name%>&amp;ds=true')"><b>HERE</b></a> to manage compelx attributes of this dataset]
														</td><%
													}
													%>
												</tr>
												
												<%
												// the table
												if (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0){
													%>
													<tr>
											  			<td width="100%" colspan="<%=String.valueOf(colspan)%>">
															<table border="1" width="100%" cellspacing="0" bordercolorlight="#C0C0C0" bordercolordark="#C0C0C0">
													        	<%
													        	displayed = 1;
													        	isOdd = Util.isOdd(displayed);
													        	for (int i=0; i<complexAttrs.size(); i++){
														        	
																	DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
																	attrID = attr.getID();
																	String attrName = attr.getShortName();   
																	Vector attrFields = searchEngine.getAttrFields(attrID, DElemAttribute.FIELD_PRIORITY_HIGH);
																	%>
																	
																	<tr>
																		<td width="29%" class="complex_attr_title<%=isOdd%>">
																			<a href="javascript:complexAttr('complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=ds_name%>&amp;ds=true')" title="Click here to view all the fields">
																				<%=attrName%>
																			</a>
																		</td>
																		<td width="4%" class="complex_attr_help<%=isOdd%>">
																			<!-- a target="_blank" href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=COMPLEX&amp;mode=view" -->
																			<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX">
																				<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
																			</a>
																		</td>
																		<td width="63%" class="complex_attr_value<%=isOdd%>">
																			<%
																			StringBuffer rowValue=null;
																			Vector rows = attr.getRows();
																			for (int j=0; rows!=null && j<rows.size(); j++){
																				
																				if (j>0){%>---<br/><%}
																				
																				Hashtable rowHash = (Hashtable)rows.get(j);
																				rowValue = new StringBuffer();
																				
																				for (int t=0; t<attrFields.size(); t++){
																					Hashtable hash = (Hashtable)attrFields.get(t);
																					String fieldID = (String)hash.get("id");
																					String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
																					if (fieldValue == null) fieldValue = "";
																					if (fieldValue.trim().equals("")) continue;
																					
																					if (t>0 && fieldValue.length()>0  && rowValue.toString().length()>0)
																						rowValue.append(", ");
																						
																					rowValue.append(Util.replaceTags(fieldValue));
																					%>
																					<%=Util.replaceTags(fieldValue)%><br/><%
																				}	
																			}
																			%>
																		</td>
																		
																		<% isOdd = Util.isOdd(++displayed); %>
																	</tr><%
																}
																%>
													        </table>
														</td>
													</tr>
													<%
												}
												%>	
											</table>
											
											<!-- end complex attributes -->
											<%
										}
										%>
										
									</td>
								</tr>
								<!-- end dotted -->
								
							</table>
						</td>
					</tr>
					
					<!-- end main table body -->
					
				</table>
				
				<!-- end main table -->
				
				
				<!-- various hidden inputs -->
				
				<input type="hidden" name="mode" value="<%=mode%>"/>
				<input type="hidden" name="check_in" value="false"/>
				<input type="hidden" name="unlock" value="false"/>
				<input type="hidden" name="changed" value="0">
				<!-- Special input for 'delete' mode only. Inidcates if dataset(s) should be deleted completely. -->
				<input type="hidden" name="complete" value="false"/>
				<input type="hidden" name="force_status" value=""/>
				
			</form>
			
			<%@ include file="footer.htm" %>
			
			</div>
        </td>
	</tr>
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
