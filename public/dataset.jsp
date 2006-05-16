<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private String currentUrl=null;%>

<%@ include file="history.jsp" %>

<%!

private String getValue(String id, String mode, Vector attributes){
	if (id==null) return null;
	if (mode.equals("add")) return null;
	
	for (int i=0; attributes!=null && i<attributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)attributes.get(i);
		if (id.equals(attr.getID()))
			return attr.getValue();
	}
	
	return null;
}
private Vector getValues(String id, String mode, Vector attributes){
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
			
			request.setCharacterEncoding("UTF-8");
			
			String mode=null;
			Vector mAttributes=null;
			Vector attributes=null;
			Dataset dataset=null;
			Vector complexAttrs=null;
			Vector tables=null;

			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
			ServletContext ctx = getServletContext();			
			
			if (request.getMethod().equals("POST")){
      			if (user == null){ %>
					<b>Not authorized to post any data!</b> <%
	      			return;
      			}
			}
			
			String dstIdf = request.getParameter("ds_idf");
			String ds_id = request.getParameter("ds_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b> <%
				return;
			}
			
			if (mode.equals("add")){
				if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){ %>
					<b>Not allowed!</b> <%
					return;
				}
			}
			
			if (mode.equals("view")){
				if (Util.voidStr(dstIdf) && Util.voidStr(ds_id)){ %>
					<b>Missing identifier or ID!</b> <%
					return;
				}
			}
			else if (mode.equals("edit")){
				if (Util.voidStr(ds_id)){ %>
					<b>Missing ID!</b> <%
					return;
				}
			}
			
			boolean latestRequested = mode.equals("view") && !Util.voidStr(dstIdf);
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
					String lid = request.getParameter("latest_id");
					if (lid!=null)
						redirUrl = redirUrl + "dataset.jsp?mode=view&ds_id=" + lid;
					else{
						redirUrl = history.gotoLastMatching("datasets.jsp");;
						redirUrl = (redirUrl!=null&&redirUrl.length()>0) ? redirUrl : redirUrl + "/index.jsp";
					}
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
				
				if (latestRequested){
					dataset = searchEngine.getLatestDst(dstIdf);
					if (dataset!=null) ds_id = dataset.getID();
				}
				else
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
			
			Vector rodLinks = dataset==null ? null : searchEngine.getRodLinks(dataset.getID());
			
			// set a flag if element has history
			boolean hasHistory = false;
			if (mode.equals("edit") && dataset!=null){
				Vector v = searchEngine.getDstHistory(dataset.getIdentifier(), dataset.getVersion() + 1);
				if (v!=null && v.size()>0)
					hasHistory = true;
			}
			
			// we get the registration status already here, because it's needed in javascript below
			String regStatus = dataset!=null ? dataset.getStatus() : null;
			%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
		<%@ include file="headerinfo.txt" %>
    <title>Dataset - Data Dictionary</title>
	<link type="text/css" rel="stylesheet" href="eionet_new.css" title="Default"/>
    <script type="text/javascript" src='modal_dialog.js'></script>
    <script type="text/javascript">
    // <![CDATA[

		function deleteDatasetReady(){
			document.forms["form1"].elements["mode"].value = "delete";
			document.forms["form1"].submit();
		}

		function submitForm(mode){
			
			if (mode == "delete"){
				
				<%
				if (regStatus!=null && !dataset.isWorkingCopy() && regStatus.equals("Released")){ %>
					alert("A dataset definition in Released status cannot be deleted, because it might be referenced by outer sources!");
					return;<%
				}
				%>
			
				<%
				if (dataset!=null && dataset.isWorkingCopy()){ %>
					var b = confirm("This working copy will be deleted and the whole dataset released for others to edit! Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				else{ %>
					var b = confirm("This dataset will be deleted! You will be given a chance to delete it permanently or save it for restoring later. Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				%>
				if (b==false) return;
				
				<%
				if (dataset!=null && dataset.isWorkingCopy()){ %>
					document.forms["form1"].elements["complete"].value = "true";
					deleteDatasetReady();
					return;<%
				}
				else{ %>
					// now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
					openNoYes("yesno_dialog.html", "Do you want the selected datasets to be deleted permanently?\n(Note that working copies will always be permanently deleted)", delDialogReturn,100, 400);
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
				
				if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)){
					alert("Identifier not valid for usage as an XML tag! " +
						  "In the first character only underscore or latin characters are allowed! " +
						  "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
					return;
				}
				
				slctAllValues();
			}			
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function delDialogReturn(){
			var v = dialogWin.returnValue;
			if (v==null || v=="" || v=="cancel") return;
			
			document.forms["form1"].elements["complete"].value = v;
			deleteDatasetReady();
		}

		function checkObligations(){
			
			var o = document.forms["form1"].ds_name;
			if (o!=null && o.value.length == 0) return false;
			
			var elems = document.forms["form1"].elements;
			for (var i=0; elems!=null && i<elems.length; i++){
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

		function checkIn(){
			submitCheckIn();
		}
		
		function submitCheckIn(){
			<%
			String latestRegStatus = dataset!=null ? searchEngine.getLatestRegStatus(dataset) : "";
			if (latestRegStatus.equals("Released")){ %>
				var b = confirm("Please note that you are checking your changes into a dataset definition that was in Released status prior to checking out! " +
								"By force, this will create a new version of that definition! If you want to continue, click OK. Otherwise click Cancel.");
				if (b==false) return;<%
			}
			%>
			
			var i;
			var stat = "";
			var optn;
			var optns = document.forms["form1"].elements["reg_status"].options;
			for (i=0; optns!=null && i<optns.length; i++){
				optn = optns[i];
				if (optn.selected){
					stat = optn.value;
					break;
				}
			}
			
			if (stat=="Released"){
				var b = confirm("You are checking in with Released status! This will automatically release your changes into public view. " +
							    "If you want to continue, click OK. Otherwise click Cancel.");
				if (b==false) return;
			}

			document.forms["form1"].elements["check_in"].value = "true";
			document.forms["form1"].elements["mode"].value = "edit";
			document.forms["form1"].submit();
		}
		
		function goTo(mode, id){
			if (mode == "edit"){
				<%
				if (regStatus!=null && regStatus.equals("Released")){ %>
					var b =  confirm("Please be aware that this is a definition in Released status. Unless " +
						  			 "you change the status back to something lower, your edits will become " +
						  			 "instantly visible for the public visitors after you check in the definition! " +
						  			 "Click OK, if you want to continue. Otherwise click Cancel.");
					if (b == false) return;<%
				}
				%>
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
		
		function validForXMLTag(str){
			
			// if empty string not allowed for XML tag
			if (str==null || str.length==0){
				return false;
			}
			
			// check the first character (only underscore or A-Z or a-z allowed)
			var ch = str.charCodeAt(0);
			if (!(ch==95 || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))){
				return false;
			}
			
			// check the rets of characters ((only underscore or hyphen or dot or 0-9 or A-Z or a-z allowed))
			if (str.length==1) return true;
			for (var i=1; i<str.length; i++){
				ch = str.charCodeAt(i);
				if (!(ch==95 || ch==45 || ch==46 || (ch>=48 && ch<=57) || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))){
					return false;
				}
			}
			
			return true;
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
   // ]]>
    </script>
</head>
<body>
                  <jsp:include page="nlocation.jsp" flush='true'>
                  <jsp:param name="name" value="Dataset"/>
                  <jsp:param name="back" value="true"/>
                </jsp:include>
    <%@ include file="nmenu.jsp" %>
<div id="workarea">
				<%

				String verb = "View";
				if (mode.equals("add"))
					verb = "Add";
				else if (mode.equals("edit"))
					verb = "Edit";
					
				%>
			<div id="operations">
				<%
				String hlpScreen = "dataset";
				if (mode.equals("edit"))
					hlpScreen = "dataset_edit";
				else if (mode.equals("add"))
					hlpScreen = "dataset_add";
				%>
				<ul>
					<li><a href="help.jsp?screen=<%=hlpScreen%>&amp;area=pagehelp" onclick="pop(this.href)" target="_blank">Page help</a></li>
					<%
					if (mode.equals("view") && user!=null && dataset!=null && dataset.getIdentifier()!=null){
						%>
						<li><a href="Subscribe?dataset=<%=Util.replaceTags(dataset.getIdentifier())%>">Subscribe</a></li>
						<%
					}
					%>
				</ul>
      		</div>
						
			<div style="clear:right; float:right">
							<%
							String topWorkingUser = null;
							boolean topFree = false;
				
							if (mode.equals("view") && dataset!=null){
								if (user!=null){
									// set the flag indicating if the corresponding namespace is in use
									topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
									topFree = topWorkingUser==null ? true : false;
										
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
								<input type="button" class="smallbutton" value="History" onclick="popNovr('dst_history.jsp?ds_id=<%=ds_id%>')"/><%
							}
							// the working copy part
							else if (dataset!=null && dataset.isWorkingCopy()){								
								%>
								<span class="wrkcopy">Working copy</span><%
							}
							%>
      		</div>
							<h1><%=Util.replaceTags(verb)%> dataset definition</h1>
							
			<div style="clear:both">
			<br/>
			<form name="form1" id="form1" method="post" action="dataset.jsp">
			
				<%
				if (!mode.equals("add")){ %>
					<input type="hidden" name="ds_id" value="<%=ds_id%>"/><%
				}
				else { %>
					<input type="hidden" name="dummy"/><%
				}
				
				%>
				
				<!--=======================-->
				<!-- main table inside div -->
				<!--=======================-->
				
				
	                
	                <!-- mandatory/optional/conditional bar -->
	                
	                <%
					if (!mode.equals("view")){ %>
					
								<table class="mnd_opt_cnd" border="0" width="100%" cellspacing="0" style="margin-top:10px; background: #ffffff; border:1px solid #FF9900">
									<tr>
										<td width="4%"><img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/></td>
										<td width="17%">Mandatory</td>
										<td width="4%"><img border="0" src="images/optional.gif" width="16" height="16" alt=""/></td>
										<td width="15%">Optional</td>
										<td width="4%"><img border="0" src="images/conditional.gif" width="16" height="16" alt=""/></td>
										<td width="56%">Conditional</td>
									</tr>
									<tr>
										<td width="100%" colspan="6">
											<b>NB! Edits will be lost if you leave the page without saving!</b>
										</td>
									</tr>
								</table>
						<%
					}	
					%>
	                
	                <!-- add, save, check-in, undo check-out buttons -->
					
					<%
					if (mode.equals("add") || mode.equals("edit")){ %>					
							<div style="margin-top:10px; text-align:right;" >
							<%
								// add case
								if (mode.equals("add")){
									boolean iPrm = user==null ? false : SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i");
									if (!iPrm){ %>
										<input class="mediumbuttonb" type="button" value="Add" disabled="disabled"/><%
									}
									else{ %>
										<input class="mediumbuttonb" type="button" value="Add" onclick="submitForm('add')"/><%
									}
								}
								// edit case
								else if (mode.equals("edit")){
									String isDisabled = editPrm ? "" : "disabled='disabled'";
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
						
						<%
						// update version checkbox
						if (mode.equals("edit") && dataset!=null && dataset.isWorkingCopy() && editPrm && hasHistory) {
							%>
							<br/>
								<span class="smallfont_light">
									<%
									if (!latestRegStatus.equals("Released")){ %>
										<input type="checkbox" id="upd_version" name="upd_version" value="true"/>
											&nbsp;<label for="upd_version">Update the definition's CheckInNo when checking in
										</label><%
									}
									else{ %>
										<input type="checkbox" id="upd_version" name="upd_version_DISABLED" checked="checked" disabled="disabled"/>
											&nbsp;<label for="upd_version">Update the definition's CheckInNo when checking in</label>
										<input type="hidden" name="upd_version" value="true"/><%
									}
									%>
								</span>
							<%
						} %>
						</div>
						<%
					}
					%>
	                
		                    
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
			                    	if (rodLinks!=null && rodLinks.size()>0)
			                    		quicklinks.add("Obligations in ROD | rodlinks");
			                    	
			                    	request.setAttribute("quicklinks", quicklinks);
			                    	System.out.println("======> okokokok");
			                    	%>
		                    		<jsp:include page="quicklinks.jsp" flush="true">
		                    		</jsp:include>
						            <%
								}
								%>
								
								<!-- pdfs & schema & docs -->
								
								<%
		                    	if (mode.equals("view")){
			                    	
			                    	Vector docs = searchEngine.getDocs(ds_id);
			                    	boolean dispAll = editPrm;
			                    	boolean dispPDF = dataset!=null && dataset.displayCreateLink("PDF");
									boolean dispXLS = dataset!=null && dataset.displayCreateLink("XLS");
									boolean dispODS = dataset!=null && dataset.displayCreateLink("ODS");
									boolean dispMDB = dataset!=null && dataset.displayCreateLink("MDB");
									boolean dispXmlSchema = dataset!=null && dataset.displayCreateLink("XMLSCHEMA");
									boolean dispXmlInstance = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xmli");
									boolean dispUploadAndCache = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
									boolean dispDocs = docs!=null && docs.size()>0;
									
									if (dispAll || dispPDF || dispXLS || dispXmlSchema || dispXmlInstance || dispUploadAndCache || dispDocs || dispMDB || dispODS){
				                    	%>
											<div id="createbox">
												<table border="0" width="100%" cellspacing="0">
													
													<%
													// PDF link
													if (dispAll || dispPDF){ %>
														<tr>
															<td width="73%" valign="middle" align="left">
																Create technical specification for this dataset
															</td>
															<td width="27%" valign="middle" align="left">
																<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;obj_id=<%=ds_id%>&amp;out_type=GDLN">
																	<img border="0" src="images/icon_pdf.jpg" width="17" height="18" alt="PDF" />
																</a>
															</td>
														</tr><%
													}
													
													// XML Schema link
													if (dispAll || dispXmlSchema){ %>
														<tr>
															<td width="73%" valign="middle" align="left">
																Create an XML Schema for this dataset
															</td>
															<td width="27%" valign="middle" align="left">
																<a target="_blank" href="GetSchema?id=DST<%=ds_id%>">
																	<img border="0" src="images/icon_xml.jpg" width="16" height="18" alt="XML icon"/>
																</a>
															</td>
														</tr><%
													}
													
													// XML Instance link
													if (dispAll || dispXmlInstance){ %>
														<tr>
															<td width="73%" valign="middle" align="left">
																Create an instance XML for this dataset
															</td>
															<td width="27%" valign="middle" align="left">
																<a target="_blank" href="GetXmlInstance?id=<%=dataset.getID()%>&amp;type=dst">
																	<img border="0" src="images/icon_xml.jpg" width="16" height="18" alt="XML icon"/>
																</a>
															</td>
														</tr><%
													}
													
													// MS Excel link
													if (dispAll || dispXLS){ %>
														<tr>
															<td width="73%" valign="middle" align="left">
																Create an MS Excel template for this dataset&nbsp;<a target="_blank" href="help.jsp?screen=dataset&amp;area=excel" onclick="pop(this.href)"><img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/></a>
															</td>
															<td width="27%" valign="middle" align="left">
																<a href="GetXls?obj_type=dst&amp;obj_id=<%=ds_id%>"><img border="0" src="images/icon_xls.gif" width="16" height="18" alt="XLS icon"/></a>
															</td>
														</tr><%
													}

													// OpenDocument spreadsheet link
													if (dispAll || dispODS){ %>
														<tr>
															<td width="73%" valign="middle" align="left">
																Create an OpenDocument spreadsheet template for this dataset&nbsp;<a target="_blank" href="help.jsp?screen=dataset&amp;area=ods" onclick="pop(this.href)"><img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/></a>
															</td>
															<td width="27%" valign="middle" align="left">
																<a href="GetOds?type=dst&amp;id=<%=ds_id%>"><img border="0" src="images/icon_ods.gif" alt="ODS icon"/></a>
															</td>
														</tr><%
													}

													// MS Access link
													if (dispAll || dispMDB){ %>
														<tr>
															<td width="73%" valign="middle" align="left">
																Create validation metadata for MS Access template&nbsp;<a target="_blank" href="help.jsp?screen=dataset&amp;area=access" onclick="pop(this.href)"><img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/></a>
															</td>
															<td width="27%" valign="middle" align="left">
																<a href="GetMdb?dstID=<%=ds_id%>&amp;vmdonly=true"><img border="0" src="images/icon_mdb.jpg" width="16" height="18" alt="MDB icon"/></a>
															</td>
														</tr><%
													}
													
													// codelist
													if (dispAll || dispXmlSchema){ %>
														<tr>
															<td width="73%" valign="middle" align="left">
																Get the comma-separated codelists of this dataset
															</td>
															<td width="27%" valign="middle" align="left">
																<a target="_blank" href="CodelistServlet?id=<%=dataset.getID()%>&amp;type=DST">
																	<img border="0" src="images/icon_txt.gif" width="16" height="18" alt=""/>
																</a>
															</td>
														</tr><%
													}
													
													// display links to uploaded documents
													for (int i=0; docs!=null && i<docs.size(); i++){
														Hashtable hash = (Hashtable)docs.get(i);
														String md5   = (String)hash.get("md5");
														String file  = (String)hash.get("file");
														String icon  = (String)hash.get("icon");												
														String title = (String)hash.get("title");
														%>
														<tr>
															<td width="73%" valign="middle" align="left"><%=Util.replaceTags(title)%></td>
															<td width="27%" valign="middle" align="left">
																<a href="DocDownload?file=<%=Util.replaceTags(md5)%>"><img border="0" src="images/<%=Util.replaceTags(icon)%>" width="16" height="18" alt="icon"/></a>
																<%
																if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u")){
																	%>&nbsp;<a target="_blank" href="DocUpload?delete=<%=Util.replaceTags(md5)%>&amp;idf=<%=Util.replaceTags(dataset.getIdentifier())%>"><img border="0" src="images/delete.gif" width="14" height="14"/></a><%
																}
																%>
															</td>
														</tr>
														<%
													}
													
													// display the "Upload document" and "Manage cache" links
													if (dispAll || dispUploadAndCache){
														%>
														<tr style="height:20px;">
															<td colspan="2" valign="bottom" align="left">
																<span class="barfont">
																	[ <a target="_blank" href="doc_upload.jsp?ds_id=<%=ds_id%>&amp;idf=<%=Util.replaceTags(dataset.getIdentifier())%>" onclick="pop(this.href)">Upload a document ...</a> ]
																</span>
																<span class="barfont">
																	[ <a target="_blank" href="GetCache?obj_id=<%=ds_id%>&amp;obj_type=dst&amp;idf=<%=Util.replaceTags(dataset.getIdentifier())%>" onclick="pop(this.href)">Open cache ...</a> ]
																</span>
															</td>
														</tr>
														<%
													}
													%>
												</table>
									</div>
										<%
									}
								}
								%>
								
								<!-- start dotted -->
								
									<div id="outerframe">

										<!-- attributes -->

										<%
										int displayed = 0;
										int colspan = mode.equals("view") ? 3 : 4;
										String titleWidth = colspan==3 ? "30" : "26";
										String valueWidth = colspan==3 ? "66" : "62";
										
										String isOdd = Util.isOdd(displayed);
										%>

										<table class="datatable">
								  		
								  			<!-- static attributes -->
								  			
											<!-- short name -->
								    		<tr>
												<td width="<%=titleWidth%>%" class="short_name">Short name</td>
												<td width="4%" class="short_name">
													<a target="_blank" href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href)">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
													</a>
												</td>
												<%
												if (colspan==4){
													%>
													<td width="4%" class="short_name">
														<img border="0" src="images/mandatory.gif" width="16" height="16" alt="mandatory"/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="short_name_value">
													<%
													if (mode.equals("view")){ %>
														<%=Util.replaceTags(dataset.getShortName())%>
														<input type="hidden" name="ds_name" value="<%=Util.replaceTags(dataset.getShortName(),true)%>"/><%
													}
													else if (mode.equals("add")){%>
														<input class="smalltext" type="text" size="30" name="ds_name"/><%
													}
													else{ %>
														<input class="smalltext" type="text" size="30" name="ds_name" value="<%=Util.replaceTags(dataset.getShortName())%>"/><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- RegistrationStatus -->
								    		
								    		<tr class="stribe<%=isOdd%>">
												<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
													RegistrationStatus
												</td>
												<td width="4%" class="simple_attr_help<%=isOdd%>">
													<a target="_blank" href="help.jsp?screen=dataset&amp;area=regstatus" onclick="pop(this.href)">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<img border="0" src="images/mandatory.gif" width="16" height="16" alt="mandatory"/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
													<%
													if (mode.equals("view")){ %>														
														<%=Util.replaceTags(regStatus)%><%
													}
													else{ %>
														<select name="reg_status" onchange="form_changed('form1')"> <%
															Vector regStatuses = verMan.getRegStatuses();
															for (int i=0; i<regStatuses.size(); i++){
																String stat = (String)regStatuses.get(i);
																String selected = stat.equals(regStatus) ? "selected='selected'" : ""; %>
																<option <%=selected%> value="<%=Util.replaceTags(stat)%>"><%=Util.replaceTags(stat)%></option><%
															} %>
														</select><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- Reference URL -->
								    		<%
								    		String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
								    		if (mode.equals("view") && jspUrlPrefix!=null){
									    		String refUrl = jspUrlPrefix + "dataset.jsp?mode=view&amp;ds_idf=" + dataset.getIdentifier();
									    		%>
								    		  <tr class="stribe<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														Reference URL
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="help.jsp?screen=dataset&amp;area=refurl" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
														<span class="barfont"><a target="_blank" href="<%=refUrl%>"><%=refUrl%></a></span>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}
								    		%>
								    		
								    										    		
								    		<!-- dynamic attributes -->
								    		
								    		<%
								    		for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
									    		
												attribute = (DElemAttribute)mAttributes.get(i);
												String dispType = attribute.getDisplayType();
												if (dispType == null) continue;
												
												String attrOblig = attribute.getObligation();
												String obligImg  = "optional.gif";
												String obligTxt  = "optional";
												if (attrOblig.equalsIgnoreCase("M")) {
													obligImg = "mandatory.gif";
													obligTxt  = "mandatory";
												}
												else if (attrOblig.equalsIgnoreCase("C")) {
													obligImg = "conditional.gif";
													obligTxt  = "conditional";
												}
												
												if (!attribute.displayFor("DST")) continue;
												
												attrID = attribute.getID();
												attrValue = getValue(attrID, mode, attributes);
												
												if (mode.equals("view") && (attrValue==null || attrValue.length()==0))
													continue;
												
												//displayed++; - done below
												
												String width  = attribute.getDisplayWidth();
												String height = attribute.getDisplayHeight();
												
												String disabled = user == null ? "disabled='disabled'" : "";
								
												boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
												Vector multiValues=null;
												if (dispMultiple){
													multiValues = getValues(attrID, mode, attributes);
												}
												
												%>
												
								    		<tr class="stribe<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														<%=Util.replaceTags(attribute.getShortName())%>
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help<%=isOdd%>">
															<img border="0" src="images/<%=Util.replaceTags(obligImg)%>" alt="<%=Util.replaceTags(obligTxt)%>" width="16" height="16"/>
														</td><%
													}
													%>
													
													<!-- dynamic attribute value display -->
													
													<td width="<%=valueWidth%>%" style="word-wrap:break-word;wrap-option: emergency" class="simple_attr_value<%=isOdd%>">
														<%
														
														// if mode is 'view', display simple a text, otherwise an input						
														if (mode.equals("view")){
															if (dispMultiple){
																for (int k=0; multiValues!=null && k<multiValues.size(); k++){
																	attrValue = (String)multiValues.get(k);
																	%><%if (k>0)%>, <%;%><%=Util.replaceTags(attrValue)%><%
																}
															}
															else{ %>
																<%=Util.replaceTags(attrValue)%> <%
															}
														}
														else{ // start display input
														
															if (dispMultiple){ // mutliple display
																%>							
																<select <%=disabled%> name="attr_mult_<%=attrID%>" multiple="multiple" style="width:auto"> <%
								
																	for (int k=0; multiValues!=null && k<multiValues.size(); k++){
																		attrValue = (String)multiValues.get(k);
																		%>
																		<option value="<%=Util.replaceTags(attrValue)%>"><%=Util.replaceTags(attrValue)%></option> <%
																	}
																	%>
																	
																</select>
																
																<%
																if (disabled.equals("")){ %>
																	<a href="javascript:rmvValue('<%=attrID%>')"><img src="images/button_remove.gif" border="0" title="Click here to remove selected value" alt=""/></a>
																	<a href="javascript:openAddBox('<%=attrID%>', 'dispType=<%=Util.replaceTags(dispType)%>&amp;width=<%=width%>')"><img src="images/button_plus.gif" border="0" title="Click here to add a new value" alt=""/></a> <%
																}
																
																if (dispType.equals("select")){ %>
																
																	<select class="small" name="hidden_attr_<%=attrID%>" style="display:none"> <%
																		Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
																		if (fxValues==null || fxValues.size()==0){ %>
																			<option selected="selected" value=""></option> <%
																		}
																		else{
																			for (int g=0; g<fxValues.size(); g++){
																				FixedValue fxValue = (FixedValue)fxValues.get(g); %>
																				<option value="<%=Util.replaceTags(fxValue.getValue())%>"><%=Util.replaceTags(fxValue.getValue())%></option> <%
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
																			<option selected="selected" value=""></option> <%
																		}
																		else{
																			for (int g=0; g<attrValues.size(); g++){
																				%>
																				<option value="<%=Util.replaceTags((String)attrValues.get(g))%>"><%=Util.replaceTags((String)attrValues.get(g))%></option> <%
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
																		<input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" value="<%=Util.replaceTags(attrValue)%>" onchange="form_changed('form1')"/>
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
																			<option selected="selected" value=""></option> <%
																		}
																		else{
																			boolean selectedByValue = false;
																			for (int g=0; g<fxValues.size(); g++){
																				FixedValue fxValue = (FixedValue)fxValues.get(g);
																				
																				String isSelected = (fxValue.getDefault() && !selectedByValue) ? "selected='selected'" : "";
																				
																				if (attrValue!=null && attrValue.equals(fxValue.getValue())){
																					isSelected = "selected='selected'";
																					selectedByValue = true;
																				}
																				
																				%>
																				<option <%=isSelected%> value="<%=Util.replaceTags(fxValue.getValue())%>"><%=Util.replaceTags(fxValue.getValue())%></option> <%
																			}
																		}
																		%>
																	</select>
																	<a target="_blank" href="fixed_values.jsp?mode=view&amp;delem_id=<%=attrID%>&amp;delem_name=<%=Util.replaceTags(attribute.getShortName())%>&amp;parent_type=attr" onclick="pop(this.href)">
																		<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
																	</a>
																	<%
																}
																else{ %>
																	Unknown display type!<%
																}
																
															} // end of no multiple display
															
														} // end display input
														%>
														<input type="hidden" name="oblig_<%=attrID%>" value="<%=Util.replaceTags(attribute.getObligation(),true)%>"/>
													</td>
													
													<!-- end of dynamic attribute value display -->
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr>
									    		
									    		
									    		<!-- end of dynamic attribute row -->
												
												<%
											}
											%>
											
											<!-- public outputs -->
											
											<%
											if (!mode.equals("add") && editPrm){
												String checkedPDF = dataset.displayCreateLink("PDF") ? "checked='checked'" : "";
												String checkedXLS = dataset.displayCreateLink("XLS") ? "checked='checked'" : "";
												String checkedODS = dataset.displayCreateLink("ODS") ? "checked='checked'" : "";
												String checkedMDB = dataset.displayCreateLink("MDB") ? "checked='checked'" : "";
												String checkedXmlSchema = dataset.displayCreateLink("XMLSCHEMA") ? "checked='checked'" : "";												
												%>
								    		  <tr class="stribe<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														Public outputs
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="help.jsp?screen=dataset&amp;area=public_outputs" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help<%=isOdd%>">
															<img border="0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</td><%
													}
													%>
													<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
														<%
														if(mode.equals("view")){ %>
															<input type="checkbox" disabled="disabled" <%=checkedPDF%>/>
																<span class="barfont">Technical specification in PDF format</span>
															<br/>
															<input type="checkbox" disabled="disabled" <%=checkedXLS%>/>
																<span class="barfont">MS Excel template</span>
															<br/>
															<input type="checkbox" disabled="disabled" <%=checkedXmlSchema%>/>
																<span class="barfont">The definition on XML Schema format</span>
															<%
														}
														else{ %>
															<input type="checkbox" name="disp_create_links" value="PDF" <%=checkedPDF%>/>
																<span class="barfont">Technical specification in PDF format</span>
															<br/>
															<input type="checkbox" name="disp_create_links" value="XLS" <%=checkedXLS%>/>
																<span class="barfont">MS Excel template</span>
															<br/>
															<input type="checkbox" name="disp_create_links" value="XMLSCHEMA" <%=checkedXmlSchema%>/>
																<span class="barfont">The definition on XML Schema format</span>															
															<%
														}
														%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}%>
											
											<!-- version (or the so-called CheckInNo) -->
								    		<%
								    		// display only in non-add mode and for users with edit prm
								    		if (!mode.equals("add") && editPrm){
												String dstVersion = dataset.getVersion(); %>
								    		  <tr class="stribe<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														CheckInNo
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="help.jsp?screen=dataset&amp;area=check_in_no" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help<%=isOdd%>">
															<img border="0" src="images/mandatory.gif" width="16" height="16" alt="Help"/>
														</td><%
													}
													%>
													<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
														<%=Util.replaceTags(dstVersion)%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}
								    		%>
								    		
								    		<!-- Identifier -->
								    		<tr class="stribe<%=isOdd%>">
												<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
													Identifier
												</td>
												<td width="4%" class="simple_attr_help<%=isOdd%>">
													<a target="_blank" href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href)">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<img border="0" src="images/mandatory.gif" width="16" height="16" alt="mandatory"/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
													<%
													if(!mode.equals("add")){ %>
														<b><%=Util.replaceTags(idfier)%></b>
														<input type="hidden" name="idfier" value="<%=Util.replaceTags(idfier,true)%>"/><%
													}
													else{ %>
														<input class="smalltext" type="text" size="30" name="idfier"/><%
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
											
											%>
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
												<tr>
													<%
													if (!mode.equals("view")){ %>
														<td width="<%=titleWidth%>%" class="simple_attr_title"><%
													}
													else{ %>
														<td width="34%" valign="top"><%
													}
													%>
														<a name="model"></a><b>Data model</b>
													</td>
													
													<%
													if (!mode.equals("view")){ %>
														<td width="4%" class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=dataset&amp;area=data_model_link" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
															</a>
														</td>
														<td width="4%" class="simple_attr_help">
															<img border="0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</td>
														<td width="<%=valueWidth%>%" class="barfont_bordered"><%
													}
													else{ %>
														<td width="66%" class="barfont"><%
													}
													
														// thumbnail
														if (mode.equals("view") && dataset.getVisual()!=null){
															
															if (imgVisual){ %>
																<a target="_blank" href="visuals/<%=Util.replaceTags(dsVisual)%>" onfocus="blur()" onclick="pop(this.href)">
																	<img src="visuals/<%=Util.replaceTags(dsVisual)%>" border="0" height="100px" width="100px"/>
																</a><br/>
																[Click thumbnail to view large version of the data model]<%
															}
															else{ %>
																The file representing the dataset stucture cannot be displayed on this web-page.
																But you can see it by pressing the following link:<br/>
																<a href="javascript:openStructure('visuals/<%=Util.replaceTags(dsVisual)%>')"><%=Util.replaceTags(dsVisual)%></a> <%
															}
														}
														
														// model link
														if (mode.equals("edit") && user!=null){ %>
															[Click <a href="dsvisual.jsp?ds_id=<%=ds_id%>"><b>HERE</b></a> to manage the model of this dataset]
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
										if (mode.equals("view") && tables!=null && tables.size()>0 || mode.equals("view") && user!=null && editPrm && topFree){
											
											colspan = user==null ? 1 : 2;

											%>
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
												
												<tr style="height:10px;"><td colspan="<%=String.valueOf(colspan)%>"></td></tr>
												<tr>
													<td width="34%">
														<b>Dataset tables<a name="tables"></a></b>
													</td>
													
													<%
													// tables link
													if (user!=null && editPrm && topFree){ %>
														<td class="barfont" width="66%">
															[Click <a href="dstables.jsp?ds_id=<%=ds_id%>&amp;ds_name=<%=Util.replaceTags(ds_name)%>"><b>HERE</b></a> to manage tables of this dataset]
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
															<table border="1" width="100%" cellspacing="0" style="bordercolorlight:#C0C0C0; bordercolordark:#C0C0C0;">
													        
																<tr>
																	<th width="50%" class="dst_tbls">Full name</th>
																	<th width="50%" class="dst_tbls">Short name</th>
																</tr>
																
																<%
																boolean hasMarkedTables = false;
																for (int i=0; tables!=null && i<tables.size(); i++){
																				
																	DsTable table = (DsTable)tables.get(i);
																	
																	String tableLink = "";
																	if (latestRequested)
																		tableLink = "dstable.jsp?mode=view&amp;table_idf=" + table.getIdentifier() + "&amp;pns=" + dataset.getNamespaceID();
																	else
																		tableLink = "dstable.jsp?mode=view&amp;table_id=" + table.getID();
											
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
																	
																	String escapedName = Util.replaceTags(tblName);
																	%>
																																		
																	<tr>
																		<td width="50%" class="dst_tbls">
																			<%
																			if (user!=null && tblWorkingUser!=null){ // mark checked-out elements
																				%> <font title="<%=Util.replaceTags(tblWorkingUser,true)%>" color="red">* </font><%
																				hasMarkedTables = true;
																			}
																			else if (tblElmWorkingUser!=null){ // mark tables having checked-out elements
																				%> <font title="<%=Util.replaceTags(tblElmWorkingUser,true)%>" color="red">* </font><%
																				hasMarkedTables = true;
																			}
																			%>
																			<a href="<%=tableLink%>" title="<%=Util.replaceTags(escapedName,true)%>">
																				<%=Util.replaceTags(escapedName)%>
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
										
										<!-- rod links -->
										
										<%
										if ((mode.equals("edit") && user!=null) || (mode.equals("view") && rodLinks!=null && rodLinks.size()>0)){
											
											%>
										
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
											
												<% if (mode.equals("view")){ %>
													<tr style="height:10px;"><td width="100%" colspan="2"></td></tr><%
												} %>
											
												<!-- title & link part -->
												<tr>
													<%
													if (!mode.equals("view")){ %>
														<td width="<%=titleWidth%>%" class="simple_attr_title"><%
													}
													else{ %>
														<td width="34%"><%
													}
													%>
														<b>Obligations in ROD<a name="rodlinks"></a></b>
													</td>
													
													<%
													if (!mode.equals("view")){ %>
														<td width="4%" class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=dataset&amp;area=rod_links_link" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
															</a>
														</td>
														<td width="4%" class="simple_attr_help">
															<img border="0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</td>
														<td width="<%=valueWidth%>%" class="barfont_bordered"><%
													}
													else{ %>
														<td width="66%" class="barfont"><%
													}
													
														// the link
														if (mode.equals("edit") && user!=null){
															String dstrodLink = "dstrod_links.jsp?dst_idf=" + dataset.getIdentifier() + "&amp;dst_id=" + dataset.getID() + "&amp;dst_name=" + dataset.getShortName();
															%>
															[Click <a href="<%=dstrodLink%>"><b>HERE</b></a> to manage the dataset's links to ROD]
															<%
														}
														%>
													</td>
												</tr>
												
												<!-- table part -->
												<%
												if (mode.equals("view") && rodLinks!=null && rodLinks.size()>0){%>
													<tr>
														<td width="100%" colspan="2">
															<table border="1" width="100%" style="bordercolorlight:#C0C0C0; bordercolordark:#C0C0C0;" cellspacing="0" cellpadding="2">
																<tr>
																	<th width="20%" class="tbl_elms">Obligation</th>
																	<th width="40%" class="tbl_elms">Legal instrument</th>
																	<th width="40%" class="tbl_elms">Details</th>																	
																</tr>
																<%
																// rows
																for (int i=0; i<rodLinks.size(); i++){
																	
																	Hashtable rodLink = (Hashtable)rodLinks.get(i);
																	String raTitle = (String)rodLink.get("ra-title");
																	String liTitle = (String)rodLink.get("li-title");
																	String raDetails = (String)rodLink.get("ra-url");
																	
																	%>
																	<tr>
																		<td width="20%" class="tbl_elms">
																			<%=Util.replaceTags(raTitle)%>
																		</td>
																		<td width="40%" class="tbl_elms">
																			<%=Util.replaceTags(liTitle)%>
																		</td>
																		<td width="40%" class="tbl_elms">
																			<a target="_blank" href="<%=Util.replaceTags(raDetails)%>"><%=Util.replaceTags(raDetails)%></a>
																		</td>																		
																	</tr><%
																}
																%>
															</table>
														</td>
													</tr><%
												}
												%>
											</table>
											<%
										}
										%>
										
										
										<!-- complex attributes -->
										
										<%
										if ((mode.equals("edit") && user!=null) || (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0)){
											
											colspan = user==null ? 1 : 2;
											%>
											
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
												<tr>
													<%
													if (!mode.equals("view")){ %>
														<td width="<%=titleWidth%>%" class="simple_attr_title"><%
													}
													else{ %>
														<td width="34%"><%
													}
													%>
														<b>Complex attributes<a name="cattrs"></a></b>
													</td>
													
													<%
													if (!mode.equals("view")){
														%>
														<td width="4%" class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=dataset&amp;area=complex_attrs_link" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
															</a>
														</td>
														<td width="4%" class="simple_attr_help">
															<img border="0" src="images/mandatory.gif" width="16" height="16" alt="mandatory"/>
														</td><%
													}
													
													// the link
													if (mode.equals("edit") && user!=null){ %>
														<td width="<%=valueWidth%>%" class="barfont_bordered">
															[Click <a target="_blank" onclick="pop(this.href)" href="complex_attrs.jsp?parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=Util.replaceTags(ds_name)%>&amp;ds=true"><b>HERE</b></a> to manage complex attributes of this dataset]
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
															<table border="1" width="100%" cellspacing="0" style="bordercolorlight:#C0C0C0; bordercolordark:#C0C0C0;">
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
																			<a target="_blank" onclick="pop(this.href)" href="complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=Util.replaceTags(ds_name)%>&amp;ds=true" title="Click here to view all the fields">
																				<%=Util.replaceTags(attrName)%>
																			</a>
																		</td>
																		<td width="4%" class="complex_attr_help<%=isOdd%>">
																			<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX" onclick="pop(this.href)">
																				<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
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
										
									</div>
								<!-- end dotted -->
								
				
				
				
				<!-- various hidden inputs -->
				
				<input type="hidden" name="mode" value="<%=mode%>"/>
				<input type="hidden" name="check_in" value="false"/>
				<input type="hidden" name="unlock" value="false"/>
				<input type="hidden" name="changed" value="0"/>
				<!-- Special input for 'delete' mode only. Indicates if dataset(s) should be deleted completely. -->
				<input type="hidden" name="complete" value="false"/>
				
				<%
				if (latestID!=null){%>
					<input type="hidden" name="latest_id" value="<%=latestID%>"/><%
				}
				%>
				
			</form>
			</div>
			
			</div>
			
			<jsp:include page="footer.jsp" flush="true">
			</jsp:include>
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
