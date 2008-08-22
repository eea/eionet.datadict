<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private String currentUrl=null;%>
<%@ include file="history.jsp" %>

	<%!
	// servlet-scope helper functions
	//////////////////////////////////
	/**
	*
	*/
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
	/**
	*
	*/
	
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
	// implementation of the servlet's service method
	//////////////////////////////////////////////////
	
	request.setCharacterEncoding("UTF-8");
	
	String mode=null;
	Vector mAttributes=null;
	Vector attributes=null;
	Dataset dataset=null;
	Vector complexAttrs=null;
	Vector tables=null;
	Vector otherVersions = null;
	
	ServletContext ctx = getServletContext();
	DDUser user = SecurityUtil.getUser(request);	
	
	// POST request not allowed for anybody who hasn't logged in			
	if (request.getMethod().equals("POST") && user==null){
		request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	
	// get values of most important request parameters:
	// - id number
	// - alphanumeric identifier
	// - mode			
	String dstIdf = request.getParameter("ds_idf");
	String ds_id = request.getParameter("ds_id");
	mode = request.getParameter("mode");
	if (mode == null || mode.length()==0){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: mode");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	if (mode.equals("add")){
		if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){
			request.setAttribute("DD_ERR_MSG", "You have no permission to add a dataset!");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	if (mode.equals("view")){
		if (Util.voidStr(dstIdf) && Util.voidStr(ds_id)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id or ds_idf");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	else if (mode.equals("edit")){
		if (Util.voidStr(ds_id)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	
	// as of Sept 2006,  parameter "action" is a helper to add some extra context to parameter "mode"
	String action = request.getParameter("action");
	if (action!=null && action.trim().length()==0) action = null;
	
	// if requested by alphanumeric identifier, it means the dataset's latest version is requested 
	boolean isLatestRequested = mode.equals("view") && !Util.voidStr(dstIdf);
	
	
	//// handle the POST request//////////////////////
	//////////////////////////////////////////////////
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
				String msg = e.getMessage();					
				ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(bytesOut));
				String trace = bytesOut.toString(response.getCharacterEncoding());					
				request.setAttribute("DD_ERR_MSG", msg);
				request.setAttribute("DD_ERR_TRC", trace);
				String backLink = request.getParameter("submitter_url");
				if (backLink==null || backLink.length()==0)
					backLink = history.getBackUrl();
				request.setAttribute("DD_ERR_BACK_LINK", backLink);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}

		// disptach the POST request
		////////////////////////////
		String redirUrl = "";
		if (mode.equals("add")){
			String id = handler.getLastInsertID();
			if (id!=null && id.length()>0)
				redirUrl = redirUrl + "dataset.jsp?mode=view&ds_id=" + id;
			if (history!=null)
				history.remove(history.getCurrentIndex());
		}
		else if (mode.equals("edit")){
			// if this was a "saveclose" of a working copy in edit, send to view mode of that same working copy
			String strSaveclose = request.getParameter("saveclose");
			if (strSaveclose!=null && strSaveclose.equals("true")){
				QueryString qs = new QueryString(currentUrl);
				qs.changeParam("mode", "view");
				redirUrl = qs.getValue();
			}
			else{
				// if this was check in, go to the view of checked in copy
				String checkIn = request.getParameter("check_in");
				if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
					if (history!=null)
						history.remove(history.getCurrentIndex());
					QueryString qs = new QueryString(currentUrl);
					qs.changeParam("mode", "view");
					if (handler.getCheckedInCopyID()!=null)
						qs.changeParam("ds_id", handler.getCheckedInCopyID());
					redirUrl =qs.getValue();
				}
				else{
					QueryString qs = new QueryString(currentUrl);
					redirUrl =qs.getValue();
				}
			}
		}
		else if (mode.equals("delete")){
			if (history!=null)
				history.remove(history.getCurrentIndex());
			redirUrl = "datasets.jsp?SearchType=SEARCH";
		}
		
		response.sendRedirect(redirUrl);
		return;
	}	
	//// end of handle the POST request //////////////////////
	// any code below must not be reached when POST request!!!
	
	Connection conn = null;
	
	// the whole page's try block
	try {
	
		// get db connection, init search engine object
		conn = ConnectionUtil.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		searchEngine.setUser(user);
		
		// initialize the metadata of attributes
		mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
		
		String idfier = "";
		String ds_name = "";
		String version = "";
		String dsVisual = null;		
		String workingUser = null;
		String regStatus = null;
		String latestID = null;
		boolean isLatestDst = false;
		boolean imgVisual = false;
		boolean editPrm = false;
		boolean editReleasedPrm = false;
		boolean canCheckout = false;
		boolean canNewVersion = false;
		
		// if not in add mode, get the dataset object and some parameters based on it
		if (!mode.equals("add")){
	
			// get the dataset object		
			if (isLatestRequested){
				Vector v = new Vector();
				v.add("Released");
				v.add("Recorded");
				dataset = searchEngine.getLatestDst(dstIdf, v);
				if (dataset!=null)
					ds_id = dataset.getID();
			}
			else
				dataset = searchEngine.getDataset(ds_id);
			
			// if dataset object found, populate some parameters based on it
			if (dataset!=null){
				
				idfier = dataset.getIdentifier();
				ds_name = dataset.getShortName();
				version = dataset.getVersion();
				
				regStatus = dataset.getStatus();
				workingUser = dataset.getWorkingUser();
				editPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");				
				editReleasedPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "er");
				
				Vector v = null;
				if (user==null){
					v = new Vector();
					v.add("Released");
					v.add("Recorded");
				}
				latestID = searchEngine.getLatestDstID(idfier, v);
				isLatestDst = latestID!=null && ds_id.equals(latestID);
				
				canNewVersion = !dataset.isWorkingCopy() && workingUser==null && regStatus!=null && user!=null && isLatestDst;
				if (canNewVersion){
					canNewVersion = regStatus.equals("Released") || regStatus.equals("Recorded");					
					if (canNewVersion)
						canNewVersion = editPrm || editReleasedPrm;						
				}
				
				canCheckout = !dataset.isWorkingCopy() && workingUser==null && regStatus!=null && user!=null && isLatestDst;
				if (canCheckout){
					if (regStatus.equals("Released") || regStatus.equals("Recorded"))
						canCheckout = editReleasedPrm;
					else
						canCheckout = editPrm || editReleasedPrm;
				}
				
				// get the visual structure, so it will be displayed already in the dataset view
				dsVisual = dataset.getVisual();
				if (dsVisual!=null && dsVisual.length()!=0){
					int i = dsVisual.lastIndexOf(".");
					if (i != -1){
						String visualType = dsVisual.substring(i+1, dsVisual.length()).toUpperCase();
						if (visualType.equals("GIF") || visualType.equals("JPG") || visualType.equals("JPEG") || visualType.equals("PNG"))
							imgVisual = true;
					}
				}
				
				// get the dataset's other versions (does not include working copies)
				if (mode.equals("view"))
					otherVersions = searchEngine.getDstOtherVersions(dataset.getIdentifier(), dataset.getID());
			}
			else{
				request.setAttribute("DD_ERR_MSG", "No dataset found with this id number or alphanumeric identifier!");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
		}
		
		// populate attribute values of the dataset
		DElemAttribute attribute = null;
		String attrID = null;
		String attrValue = null;
		attributes = searchEngine.getAttributes(ds_id, "DS", DElemAttribute.TYPE_SIMPLE);
		complexAttrs = searchEngine.getComplexAttributes(ds_id, "DS");		
		if (complexAttrs == null) complexAttrs = new Vector();

		// get the dataset's tables and links to ROD		
		tables = searchEngine.getDatasetTables(ds_id, true);
		Vector rodLinks = dataset==null ? null : searchEngine.getRodLinks(dataset.getID());
			
		// init version manager object
		VersionManager verMan = new VersionManager(conn, searchEngine, user);
		
		// security checks, checkin/checkout operations, dispatching of the GET request
		if (mode.equals("edit")){
			if (!dataset.isWorkingCopy() || user==null || (workingUser!=null && !workingUser.equals(user.getUserName()))){
				request.setAttribute("DD_ERR_MSG", "You have no permission to edit this dataset!");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
		}
		else if (mode.equals("view") && action!=null && (action.equals("checkout") || action.equals("newversion"))){
			
			if (action.equals("checkout") && !canCheckout){
				request.setAttribute("DD_ERR_MSG", "You have no permission to check out this dataset!");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			if (action.equals("newversion") && !canNewVersion){
				request.setAttribute("DD_ERR_MSG", "You have no permission to create new version of this dataset!");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			
			// if creating new version, let VersionManager know about this
			if (action.equals("newversion")){
				eionet.meta.savers.Parameters pars = new eionet.meta.savers.Parameters();
				pars.addParameterValue("resetVersionAndStatus", "resetVersionAndStatus");
				verMan.setServlRequestParams(pars);
			}
	
			// check out the dataset
		    String copyID = verMan.checkOut(ds_id, "dst");
		    if (!ds_id.equals(copyID)){
			    // send to copy if created successfully, remove previous url (edit original) from history
			    history.remove(history.getCurrentIndex());			    
			    StringBuffer buf = new StringBuffer("dataset.jsp?mode=view&ds_id=");
			    buf.append(copyID);
		        response.sendRedirect(buf.toString());
	        }
		}
		else if (mode.equals("view") && dataset!=null){
			// anonymous users should not be allowed to see anybody's working copy
			if (dataset.isWorkingCopy() && user==null){
				request.setAttribute("DD_ERR_MSG", "Anonymous users are not allowed to view a working copy!");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			// anonymous users should not be allowed to see definitions that are NOT in Recorded or Released status
			if (user==null && regStatus!=null && !regStatus.equals("Recorded") && !regStatus.equals("Released")){
				request.setAttribute("DD_ERR_MSG", "Definitions NOT in Recorded or Released status are inaccessible for anonymous users.");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			// redircet user to his working copy of this dataset (if such exists)
			String workingCopyID = verMan.getWorkingCopyID(dataset);
			if (workingCopyID!=null && workingCopyID.length()>0){
				StringBuffer buf = new StringBuffer("dataset.jsp?mode=view&ds_id=");
			    buf.append(workingCopyID);
		        response.sendRedirect(buf.toString());
			}
		}
		
		// prepare the page's HTML title, shown in browser title bar
		StringBuffer pageTitle = new StringBuffer();
		if (mode.equals("edit"))
			pageTitle.append("Edit dataset");
		else
			pageTitle.append("Dataset");
		if (dataset!=null && dataset.getShortName()!=null)
			pageTitle.append(" - ").append(dataset.getShortName());
	%>

<%
// start HTML //////////////////////////////////////////////////////////////
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
		<%@ include file="headerinfo.jsp" %>
    <title><%=pageTitle.toString()%></title>
    <script type="text/javascript" src="modal_dialog.js"></script>
    <script type="text/javascript">
    // <![CDATA[

		function deleteDatasetReady(){			
			document.forms["form1"].elements["mode"].value = "delete";
			document.forms["form1"].submit();
		}
		
		function submitForm(mode){
			
			if (mode == "delete"){				
				<%
				if (regStatus!=null && dataset!=null && !dataset.isWorkingCopy() && regStatus.equals("Released")){
					if (!canCheckout){
						%>
						alert("You have no permission to delete this dataset!");
						return;
						<%
					}
				}
				
				String confirmationText = "Are you sure you want to delete this dataset? Click OK, if yes. Otherwise click Cancel.";
				if (dataset!=null && dataset.isWorkingCopy())
					confirmationText = "This working copy will be deleted! Click OK, if you want to continue. Otherwise click Cancel.";
				else if (regStatus!=null && dataset!=null && !dataset.isWorkingCopy() && regStatus.equals("Released"))
					confirmationText = "You are about to delete a Released dataset! Are you sure you want to do this? Click OK, if yes. Otherwise click Cancel.";
				%>
				
				var b = confirm("<%=confirmationText%>");
				if (b==false) return;
				
				<%
				if (dataset!=null && dataset.isWorkingCopy()){ %>
					document.forms["form1"].elements["complete"].value = "true";
					deleteDatasetReady();
					return;<%
				}
				else{ %>
					// now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
					openNoYes("yesno_dialog.html", "Do you want the dataset to be deleted permanently (answering No will enable to restore it later)?", delDialogReturn,100, 400);
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
				
				//slctAllValues();
			}
			
			if (mode=="editclose"){
				mode = "edit";
				document.forms["form1"].elements["saveclose"].value = "true";
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
			if (regStatus!=null && regStatus.equals("Released")){
				%>
				var b = confirm("You are checking in with Released status! This will automatically release your changes into public view. " +
							    "If you want to continue, click OK. Otherwise click Cancel.");
				if (b==false) return;
				<%
			}
			%>
			document.forms["form1"].elements["check_in"].value = "true";
			document.forms["form1"].elements["mode"].value = "edit";
			document.forms["form1"].submit();
		}
		
		function goTo(mode, id){
			if (mode == "edit"){				
				document.location.assign("dataset.jsp?mode=edit&ds_id=" + id);
			}
			else if (mode=="checkout"){
				document.location.assign("dataset.jsp?mode=view&action=checkout&ds_id=" + id);
			}
			else if (mode=="newversion"){
				document.location.assign("dataset.jsp?mode=view&action=newversion&ds_id=" + id);
			}
			else if (mode=="view"){
				document.location.assign("dataset.jsp?mode=view&ds_id=" + id);
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
 			form_changed("form1");
 		}

		function addValue(id, val){

			if (val.length > 0){
				if (hasValue(id,val)){
					alert("There can not be dublicate values!");
					return
				}
				var selectName = "attr_mult_" + id;
				var oOption = new Option(val, val, false, false);
				var slct = document.forms["form1"].elements[selectName];
				if (slct.length==1 && slct.options[0].value=="" && slct.options[0].text=="")
					slct.remove(0);
				slct.options[slct.length] = oOption;
				slct.size=oOption.length;
			}
 			form_changed("form1");
		}
		function slctAllValues(){
		
			var elems = document.forms["form1"].elements;
			if (elems == null) return true;
			
			for (var j=0; j<elems.length; j++){
				var elem = elems[j];
				var elemName = elem.name;
				if (startsWith(elemName, "attr_mult_")){					
					var slct = document.forms["form1"].elements[elemName];
					if (slct.options && slct.length){
						if (slct.length==1 && slct.options[0].value=="" && slct.options[0].text==""){
							slct.remove(0);
							slct.length = 0;
						}
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
		
   // ]]>
    </script>
</head>

<%
String hlpScreen = dataset!=null && dataset.isWorkingCopy() ? "dataset_working_copy" : "dataset";
if (mode.equals("edit"))
	hlpScreen = "dataset_edit";
else if (mode.equals("add"))
	hlpScreen = "dataset_add";
%>

<body>
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
	<jsp:param name="name" value="Dataset"/>
	<jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
			<%
			String verb = "View";
			if (mode.equals("add"))
				verb = "Add";
			else if (mode.equals("edit"))
				verb = "Edit";
			
			boolean isDisplayOperations = mode.equals("view") && user!=null && dataset!=null && dataset.getIdentifier()!=null;
			if (isDisplayOperations==false)
				isDisplayOperations = (mode.equals("view") && !dataset.isWorkingCopy()) && (user!=null || (user==null && !isLatestRequested)) && (latestID!=null && !latestID.equals(dataset.getID()));
			
			if (isDisplayOperations){
				%>
				<div id="operations">
					<ul>
						<%
						if (mode.equals("view") && user!=null && dataset!=null && dataset.getIdentifier()!=null){%>
							<li><a href="Subscribe?dataset=<%=Util.replaceTags(dataset.getIdentifier())%>">Subscribe</a></li><%
						}
						if (mode.equals("view") && !dataset.isWorkingCopy()){
							if (user!=null || (user==null && !isLatestRequested)){
								if (latestID!=null && !latestID.equals(dataset.getID())){%>
									<li><a href="dataset.jsp?mode=view&amp;ds_id=<%=latestID%>">Go to newest</a></li><%
								}
							}
						}
						%>
					</ul>
	      		</div><%
      		}
      		%>
						
			<div style="clear:right; float:right">
				<%
				if (mode.equals("view")){
					if (canNewVersion){
						%>
						<input type="button" class="smallbutton" value="New version" onclick="goTo('newversion', '<%=ds_id%>')"/><%
					}
					if (canCheckout){
						if (canNewVersion){
							%>&nbsp;<%
						}
						%>
						<input type="button" class="smallbutton" value="Check out" onclick="goTo('checkout', '<%=ds_id%>')"/>&nbsp;
						<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/><%
					}
				}
				%>
      		</div>
      		
			<h1><%=Util.replaceTags(verb)%> dataset definition</h1>
							
			<div style="clear:both">
			<br/>
			<form id="form1" method="post" action="dataset.jsp">
				<div style="display:none">
				<%
				if (!mode.equals("add")){ %>
					<input type="hidden" name="ds_id" value="<%=ds_id%>"/><%
				}
				else { %>
					<input type="hidden" name="dummy"/><%
				}
				
				%>
				</div>
				
				<!--=======================-->
				<!-- main table inside div -->
				<!--=======================-->
	                
	                			<!-- add, save, check-in, undo check-out buttons -->
					
								<div style="text-align:right;clear:left">
									<%
									// add case
									if (mode.equals("add")){ %>
										<input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>
										<%
									}
									// view case
									else if (mode.equals("view") && dataset!=null && dataset.isWorkingCopy()){
										if (workingUser!=null && user!=null && workingUser.equals(user.getUserName())){
											%>
											<input type="button" class="mediumbuttonb" value="Edit" onclick="goTo('edit', '<%=ds_id%>')"/>&nbsp;
											<input type="button" class="mediumbuttonb" value="Check in" onclick="checkIn()" />&nbsp;
											<input type="button" class="mediumbuttonb" value="Undo checkout" onclick="submitForm('delete')"/>
											<%
										}
									}
									// edit case
									else if (mode.equals("edit") && dataset!=null && dataset.isWorkingCopy()){
										if (workingUser!=null && user!=null && workingUser.equals(user.getUserName())){
											%>
											<input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&nbsp;
											<input type="button" class="mediumbuttonb" value="Save &amp; close" onclick="submitForm('editclose')"/>&nbsp;
											<input type="button" class="mediumbuttonb" value="Cancel" onclick="goTo('view', '<%=ds_id%>')"/>
											<%
										}
									}
									%>
								</div>
		                    
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
			                    	%>
		                    		<jsp:include page="quicklinks.jsp" flush="true" />
						            <%
								}
								%>
								
								<!-- pdfs & schema & docs -->
								
								<%
		                    	if (mode.equals("view")){
			                    	
			                    	Vector docs = searchEngine.getDocs(ds_id);
			                    	boolean dispAll = editPrm || editReleasedPrm;
			                    	boolean dispPDF = dataset!=null && dataset.displayCreateLink("PDF");
									boolean dispXLS = dataset!=null && dataset.displayCreateLink("XLS");
									boolean dispODS = dataset!=null && dataset.displayCreateLink("ODS");
									boolean dispMDB = dataset!=null && dataset.displayCreateLink("MDB");
									boolean dispXmlSchema = dataset!=null && dataset.displayCreateLink("XMLSCHEMA");
									boolean dispXmlInstance = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xmli");
									boolean dispUploadAndCache = editPrm || editReleasedPrm;
									boolean dispDocs = docs!=null && docs.size()>0;
									
									if (dispAll || dispPDF || dispXLS || dispXmlSchema || dispXmlInstance || dispUploadAndCache || dispDocs || dispMDB || dispODS){
				                    	%>
											<div id="createbox">
												<table id="outputsmenu">
													<col style="width:73%"/>
													<col style="width:27%"/>
													<%
													// PDF link
													if (dispAll || dispPDF){ %>
														<tr>
															<td>
																Create technical specification for this dataset
															</td>
															<td>
																<a rel="nofollow" href="GetPrintout?format=PDF&amp;obj_type=DST&amp;obj_id=<%=ds_id%>&amp;out_type=GDLN">
																	<img style="border:0" src="images/pdf.png" width="16" height="16" alt="PDF" />
																</a>
															</td>
														</tr><%
													}
													
													// XML Schema link
													if (dispAll || dispXmlSchema){ %>
														<tr>
															<td>
																Create an XML Schema for this dataset
															</td>
															<td>
																<a rel="nofollow" href="GetSchema?id=DST<%=ds_id%>">
																	<img style="border:0" src="images/xsd.png" width="16" height="16" alt="XML icon"/>
																</a>
															</td>
														</tr><%
													}
													
													// XML Instance link
													if (dispAll || dispXmlInstance){ %>
														<tr>
															<td>
																Create an instance XML for this dataset
															</td>
															<td>
																<a rel="nofollow" href="GetXmlInstance?id=<%=dataset.getID()%>&amp;type=dst">
																	<img style="border:0" src="images/xml.png" width="16" height="16" alt="XML icon"/>
																</a>
															</td>
														</tr><%
													}
													
													// MS Excel link
													if (dispAll || dispXLS){ %>
														<tr>
															<td>
																Create an MS Excel template for this dataset&nbsp;<a href="help.jsp?screen=dataset&amp;area=excel" onclick="pop(this.href);return false;"><img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/></a>
															</td>
															<td>
																<a rel="nofollow" href="GetXls?obj_type=dst&amp;obj_id=<%=ds_id%>"><img style="border:0" src="images/xls.png" width="16" height="16" alt="XLS icon"/></a>
															</td>
														</tr><%
													}

													// OpenDocument spreadsheet link
													if (dispAll || dispODS){ %>
														<tr>
															<td>
																Create an OpenDocument spreadsheet template for this dataset&nbsp;<a href="help.jsp?screen=dataset&amp;area=ods" onclick="pop(this.href);return false;"><img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/></a>
															</td>
															<td>
																<a rel="nofollow" href="GetOds?type=dst&amp;id=<%=ds_id%>"><img style="border:0" src="images/ods.png" width="16" height="16" alt="ODS icon"/></a>
															</td>
														</tr><%
													}

													// MS Access link
													if (dispAll || dispMDB){ %>
														<tr>
															<td>
																Create validation metadata for MS Access template&nbsp;<a  href="help.jsp?screen=dataset&amp;area=access" onclick="pop(this.href);return false;"><img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/></a>
															</td>
															<td>
																<a rel="nofollow" href="GetMdb?dstID=<%=ds_id%>&amp;vmdonly=true"><img style="border:0" src="images/mdb.png" width="16" height="16" alt="MDB icon"/></a>
															</td>
														</tr><%
													}
													
													// codelists
													if (dispAll || dispXmlSchema){ %>
														<tr>
															<td>
																Get the comma-separated codelists of this dataset
															</td>
															<td>
																<a rel="nofollow" href="CodelistServlet?id=<%=dataset.getID()%>&amp;type=DST">
																	<img style="border:0" src="images/txt.png" width="16" height="16" alt=""/>
																</a>
															</td>
														</tr>
														<tr>
															<td>
																Get the codelists of this dataset in XML format
															</td>
															<td>
																<a rel="nofollow" href="CodelistServlet?id=<%=dataset.getID()%>&amp;type=DST&amp;format=xml">
																	<img style="border:0" src="images/xml.png" width="16" height="16" alt=""/>
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
															<td><%=Util.replaceTags(title)%></td>
															<td>
																<a rel="nofollow" href="DocDownload?file=<%=Util.replaceTags(md5)%>"><img style="border:0" src="images/<%=Util.replaceTags(icon)%>" width="16" height="16" alt="icon"/></a>
																<%
																if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u")){
																	%>&nbsp;<a  href="DocUpload?ds_id=<%=ds_id%>&amp;delete=<%=Util.replaceTags(md5)%>&amp;idf=<%=Util.replaceTags(dataset.getIdentifier())%>"><img style="border:0" src="images/delete.gif" width="14" height="14"/></a><%
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
															<td colspan="2">
																<small>
																	[ <a rel="nofollow" href="doc_upload.jsp?ds_id=<%=ds_id%>&amp;idf=<%=Util.replaceTags(dataset.getIdentifier())%>">Upload a document ...</a> ]
																</small>
																<small>
																	[ <a rel="nofollow" href="GetCache?obj_id=<%=ds_id%>&amp;obj_type=dst&amp;idf=<%=Util.replaceTags(dataset.getIdentifier())%>">Open cache ...</a> ]
																</small>
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
											<col style="width:<%=titleWidth%>%"/>
											<col style="width:4%"/>
											<% if (colspan==4){ %>
											<col style="width:4%"/>
											<% } %>
											<col style="width:<%=valueWidth%>%"/>

								  		
								  			<!-- static attributes -->
								  			
											<!-- short name -->
								    		<tr id="short_name_row">
												<th class="scope-row short_name">Short name</th>
												<td class="short_name simple_attr_help">
													<a  href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
													</a>
												</td>
												<%
												if (colspan==4){
													%>
													<td class="short_name simple_attr_help">
														<img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/>
													</td><%
												}
												%>
												<td class="short_name_value">
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
								    		
								    		<tr class="zebra<%=isOdd%>">
												<th scope="row" class="scope-row simple_attr_title">
													RegistrationStatus
												</th>
												<td class="simple_attr_help">
													<a  href="help.jsp?screen=dataset&amp;area=regstatus" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td class="simple_attr_help">
														<img src="images/mandatory.gif" alt="Mandatory"  title="Mandatory"/>
													</td><%
												}
												%>
												<td class="simple_attr_value">
													<%
													if (mode.equals("view")){ %>														
														<%=Util.replaceTags(regStatus)%>
														<%
														if (workingUser!=null){
															if (dataset.isWorkingCopy() && user!=null && workingUser.equals(user.getUserName())){
																%>
																<span class="caution">(Working copy)</span><%
															}
															else if (user!=null){
																%>
																<span class="caution">(checked out by <em><%=workingUser%></em>)</span><%
															}
														}
														else if (regStatus.equalsIgnoreCase("RELEASED") && dataset.getDate()!=null){
															%><%=eionet.util.Util.releasedDate(Long.parseLong(dataset.getDate()))%><%
														}
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
								    		  <tr class="zebra<%=isOdd%>">
													<th scope="row" class="scope-row simple_attr_title">
														Reference URL
													</th>
													<td class="simple_attr_help">
														<a  href="help.jsp?screen=dataset&amp;area=refurl" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<td class="simple_attr_value">
														<small><a  href="<%=refUrl%>"><%=refUrl%></a></small>
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
												String obligTxt  = "Optional";
												if (attrOblig.equalsIgnoreCase("M")) {
													obligImg = "mandatory.gif";
													obligTxt  = "Mandatory";
												}
												else if (attrOblig.equalsIgnoreCase("C")) {
													obligImg = "conditional.gif";
													obligTxt  = "Conditional";
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
												
								    		<tr class="zebra<%=isOdd%>">
													<th scope="row" class="scope-row simple_attr_title">
														<%=Util.replaceTags(attribute.getShortName())%>
													</th>
													<td class="simple_attr_help">
														<a  href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td class="simple_attr_help">
															<img src="images/<%=Util.replaceTags(obligImg)%>" alt="<%=Util.replaceTags(obligTxt)%>" title="<%=Util.replaceTags(obligTxt)%>"/>
														</td><%
													}
													%>
													
													<!-- dynamic attribute value display -->
													
													<td style="word-wrap:break-word;wrap-option:emergency" class="simple_attr_value">
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
															
																Vector allPossibleValues = null;
																if (dispType.equals("select"))
																	allPossibleValues = searchEngine.getFixedValues(attrID, "attr");
																else if (dispType.equals("text"))
																	allPossibleValues = searchEngine.getSimpleAttributeValues(attrID);
																
																String divHeight = "7.5em";
																String textName = "other_value_attr_" + attrID;
																String divID = "multiselect_div_attr_" + attrID;
																String checkboxName = "attr_mult_" + attrID;
																Vector displayValues = new Vector();
																if (multiValues!=null && multiValues.size()>0)
																	displayValues.addAll(multiValues);
																if (allPossibleValues!=null && allPossibleValues.size()>0)
																	displayValues.addAll(allPossibleValues);
																%>
																<input type="text" name="<%=textName%>" value="insert other value" style="font-size:0.9em" onfocus="this.value=''"/>
																<input type="button" value="-&gt;" style="font-size:0.8em;" onclick="addMultiSelectRow(document.forms['form1'].elements['<%=textName%>'].value, '<%=checkboxName%>','<%=divID%>')"/>
																<div id="<%=divID%>" class="multiselect" style="height:<%=divHeight%>;width:25em;">
																	<%
																	HashSet displayedSet = new HashSet();
																	for (int k=0; displayValues!=null && k<displayValues.size(); k++){
																		
																		Object valueObject = displayValues.get(k);
																		attrValue = (valueObject instanceof FixedValue) ? ((FixedValue)valueObject).getValue() : valueObject.toString();
																		if (displayedSet.contains(attrValue))
																			continue;
																			
																		String strChecked = "";
																		if (multiValues!=null && multiValues.contains(attrValue))
																			strChecked = "checked=\"checked\"";
																		%>		
																		<label style="display:block">
																			<input type="checkbox" name="<%=checkboxName%>" value="<%=attrValue%>" <%=strChecked%> style="margin-right:5px"/><%=attrValue%>
																		</label>
																		<%
																		displayedSet.add(attrValue);
																	}
																	%>
																</div>
																<%
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
																		<textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"><%=Util.replaceTags(attrValue, true, true)%></textarea>
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
																	<a  href="fixed_values.jsp?mode=view&amp;delem_id=<%=attrID%>&amp;delem_name=<%=Util.replaceTags(attribute.getShortName())%>&amp;parent_type=attr" onclick="pop(this.href);return false;">
																		<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
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
								    		  <tr class="zebra<%=isOdd%>">
													<th scope="row" class="scope-row simple_attr_title">
														Public outputs
													</th>
													<td class="simple_attr_help">
														<a  href="help.jsp?screen=dataset&amp;area=public_outputs" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td class="simple_attr_help">
															<img style="border:0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</td><%
													}
													%>
													<td class="simple_attr_value">
														<%
														if(mode.equals("view")){ %>
															<input type="checkbox" disabled="disabled" <%=checkedPDF%>/>
																<small>Technical specification in PDF format</small>
															<br/>
															<input type="checkbox" disabled="disabled" <%=checkedXLS%>/>
																<small>MS Excel template</small>
															<br/>
															<input type="checkbox" disabled="disabled" <%=checkedXmlSchema%>/>
																<small>The definition on XML Schema format</small>
															<br/>
															<input type="checkbox" disabled="disabled" <%=checkedODS%>/>
																<small>OpenDocument spreadsheet</small>
															<%
														}
														else{ %>
															<input type="checkbox" name="disp_create_links" value="PDF" <%=checkedPDF%>/>
																<small>Technical specification in PDF format</small>
															<br/>
															<input type="checkbox" name="disp_create_links" value="XLS" <%=checkedXLS%>/>
																<small>MS Excel template</small>
															<br/>
															<input type="checkbox" name="disp_create_links" value="XMLSCHEMA" <%=checkedXmlSchema%>/>
																<small>The definition on XML Schema format</small>
															<br/>
															<input type="checkbox" name="disp_create_links" value="ODS" <%=checkedODS%>/>
																<small>OpenDocument spreadsheet</small>
															<%
														}
														%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}%>
											
											<!-- dataset number -->
								    		<%
								    		// display only in non-add mode and for users with edit prm
								    		if (!mode.equals("add") && editPrm){
												%>
												<tr class="zebra<%=isOdd%>">
													<th scope="row" class="scope-row simple_attr_title">
														Dataset number
													</th>
													<td class="simple_attr_help">
														<a  href="help.jsp?screen=dataset&amp;area=dataset_number" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td class="simple_attr_help">
															<img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/>
														</td><%
													}
													%>
													<td class="simple_attr_value">
														<%=dataset.getID()%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}
								    		%>
								    		
								    		<!-- Identifier -->
								    		<tr class="zebra<%=isOdd%>">
												<th scope="row" class="scope-row simple_attr_title">
													Identifier
												</th>
												<td class="simple_attr_help">
													<a  href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td class="simple_attr_help">
														<img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/>
													</td><%
												}
												%>
												<td class="simple_attr_value">
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
											<h2>
												Data model<a id="model"></a>
												<%
												if (!mode.equals("view")){ %>
													<a  href="help.jsp?screen=dataset&amp;area=data_model_link" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
													</a>
													<img style="border:0" src="images/optional.gif" width="16" height="16" alt="optional"/>
													[Click <a href="dsvisual.jsp?ds_id=<%=ds_id%>"><b>HERE</b></a> to manage the model of this dataset]<%
												}
												%>
											</h2>
											<%											
											// thumbnail
											if (mode.equals("view") && dataset.getVisual()!=null){
												if (imgVisual){ %>
<div class="figure-plus-container">
  <div class="figure-plus">
    <div class="figure-image">
      <a href="visuals/<%=Util.replaceTags(dsVisual)%>"><img src="visuals/<%=Util.replaceTags(dsVisual)%>"
         alt="thumbnail" class="scaled poponmouseclick"/></a>
    </div>
    <div class="figure-note">
      Click thumbnail to view large version of the data model
    </div>
  </div>
</div>
													<%
												}
												else{ %>
													<div style="text-align:right">
														The file representing the dataset stucture cannot be displayed on this web-page.
														But you can see it by pressing the following link:<br/>
														<a href="javascript:openStructure('visuals/<%=Util.replaceTags(dsVisual)%>')"><%=Util.replaceTags(dsVisual)%></a>
													</div><%
												}
											}
										}
										%>
										
										<!-- tables list -->
										
										<%
										if ((mode.equals("view") && tables!=null && tables.size()>0) || mode.equals("edit")){
											%>
											<h2>
												Dataset tables<a id="tables"></a>
												<%
												// tables link
												if (mode.equals("edit")){
													%>
													<a  href="help.jsp?screen=dataset&amp;area=tables_link" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
													</a>
													<img style="border:0" src="images/optional.gif" width="16" height="16" alt="optional"/>
													[Click <a href="dstables.jsp?ds_id=<%=ds_id%>&amp;ds_name=<%=Util.replaceTags(ds_name)%>"><b>HERE</b></a> to manage tables of this dataset]<%
												}
												%>
											</h2>												
											<%
											// tables table
											if (mode.equals("view")){
												%>
												<table class="datatable" id="dataset-tables">
													<col style="width:50%"/>
													<col style="width:50%"/>
													<thead>
													<tr>
														<th>Full name</th>
														<th>Short name</th>
													</tr>
													</thead>
													<tbody>
													<%
													boolean hasMarkedTables = false;
													for (int i=0; i<tables.size(); i++){
																	
														DsTable table = (DsTable)tables.get(i);
														String tableLink = "";
														if (isLatestRequested)
															tableLink = "dstable.jsp?mode=view&amp;table_idf=" + table.getIdentifier() + "&amp;pns=" + dataset.getNamespaceID();
														else
															tableLink = "dstable.jsp?mode=view&amp;table_id=" + table.getID();
								
														String tblFullName = "";
														attributes = searchEngine.getAttributes(table.getID(), "T", DElemAttribute.TYPE_SIMPLE);
														for (int c=0; c<attributes.size(); c++){
															DElemAttribute attr = (DElemAttribute)attributes.get(c);
					       									if (attr.getName().equalsIgnoreCase("Name"))
					       										tblFullName = attr.getValue();
														}					
														if (tblFullName!=null && tblFullName.length()>40)
															tblFullName = tblFullName.substring(0,40) + " ...";
														String escapedFullName = Util.replaceTags(tblFullName,true,true);
														%>								
														<tr>
															<td>
																<a href="<%=tableLink%>" title="<%=escapedFullName%>">
																	<%=escapedFullName%>
																</a>
															</td>
															<td>
																<%=Util.replaceTags(table.getShortName())%>
															</td>
														</tr>																	
														<%
													}
													%>
													</tbody>
										        </table><%
											}
										}
										%>
										
										<!-- rod links -->
										
										<%
										if (mode.equals("edit") || (mode.equals("view") && rodLinks!=null && rodLinks.size()>0)){
											
											%>
										
											
												<!-- title & link part -->
												<h2>
														<b>Obligations in ROD<a id="rodlinks"></a></b>
													
													<%
													if (!mode.equals("view")){ %>
														<span class="simple_attr_help">
															<a  href="help.jsp?screen=dataset&amp;area=rod_links_link" onclick="pop(this.href);return false;">
																<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img style="border:0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</span>
														<span class="barfont_bordered"><%
													}
													else{ %>
														<span class="barfont"><%
													}
													
													// the link
													if (mode.equals("edit")){
														String dstrodLink = "dstrod_links.jsp?dst_idf=" + dataset.getIdentifier() + "&amp;dst_id=" + dataset.getID() + "&amp;dst_name=" + dataset.getShortName();
														%>
														[Click <a href="<%=dstrodLink%>"><b>HERE</b></a> to manage the dataset's links to ROD]
														<%
													}
													%>
													</span>
												</h2>
												
												<!-- table part -->
												<%
												if (mode.equals("view") && rodLinks!=null && rodLinks.size()>0){%>
															<table class="datatable subtable">
																<col style="width:20%"/>
																<col style="width:40%"/>
																<col style="width:40%"/>
																<tr>
																	<th>Obligation</th>
																	<th>Legal instrument</th>
																	<th>Details</th>																	
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
																		<td>
																			<%=Util.replaceTags(raTitle)%>
																		</td>
																		<td>
																			<%=Util.replaceTags(liTitle)%>
																		</td>
																		<td>
																			<a  href="<%=Util.replaceTags(raDetails, true)%>"><%=Util.replaceTags(raDetails, true)%></a>
																		</td>																		
																	</tr><%
																}
																%>
															</table>
													<%
												}
										}
										%>
										
										
										<!-- complex attributes -->
										
										<%
										if ((mode.equals("edit") && user!=null) || (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0)){
											
											colspan = user==null ? 1 : 2;
											%>
											
												<h2>
													Complex attributes<a id="cattrs"></a>
													<%
													if (!mode.equals("view")){
														%>
														<span class="simple_attr_help">
															<a  href="help.jsp?screen=dataset&amp;area=complex_attrs_link" onclick="pop(this.href);return false;">
																<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img src="images/mandatory.gif" alt="Mandatory" title="Mandatory"/>
														</span><%
													}
													
													// the link
													if (mode.equals("edit")){ %>
														<span style="width:<%=valueWidth%>%" class="barfont_bordered">
															[Click <a href="complex_attrs.jsp?parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=Util.replaceTags(ds_name)%>&amp;ds=true"><b>HERE</b></a> to manage complex attributes of this dataset]
														</span><%
													}
													%>
												</h2>
												
												<%
												// the table
												if (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0){
													%>
															<table class="datatable" id="dataset-attributes">																
																<col style="width:29%"/>
																<col style="width:4%"/>
																<col style="width:63%"/>
													        	<%
													        	displayed = 1;
													        	isOdd = Util.isOdd(displayed);
													        	for (int i=0; i<complexAttrs.size(); i++){
														        	
																	DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
																	attrID = attr.getID();
																	String attrName = attr.getShortName();   
																	Vector attrFields = searchEngine.getAttrFields(attrID, DElemAttribute.FIELD_PRIORITY_HIGH);
																	%>
																	
																	<tr class="zebra<%=isOdd%>">
																		<td>
																			<a href="complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=ds_id%>&amp;parent_type=DS&amp;parent_name=<%=Util.replaceTags(ds_name)%>&amp;ds=true" title="Click here to view all the fields">
																				<%=Util.replaceTags(attrName)%>
																			</a>
																		</td>
																		<td>
																			<a  href="help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX" onclick="pop(this.href);return false;">
																				<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
																			</a>
																		</td>
																		<td>
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
													<%
												}
												%>	
											
											<!-- end complex attributes -->
											
											<%
											// other versions
											if (mode.equals("view") && otherVersions!=null && otherVersions.size()>0){%>
												<h2>
													Other versions<a id="versions"></a>
												</h2>
												<table class="datatable" id="other-versions">
													<col style="width:25%"/>
													<col style="width:25%"/>
													<col style="width:25%"/>
													<col style="width:25%"/>
													<thead>
														<tr>
															<th>Dataset number</th>
															<th>Status</th>
															<th>Release date</th>
															<th></th>
														</tr>
													</thead>
													<tbody>
													<%
													Dataset otherVer;
													for (int i=0; i<otherVersions.size(); i++){
														otherVer = (Dataset)otherVersions.get(i);
														String status = otherVer.getStatus();
														String releaseDate = null;
														if (status.equals("Released"))
															releaseDate = otherVer.getDate();
														if (releaseDate!=null)
															releaseDate = eionet.util.Util.releasedDate(Long.parseLong(releaseDate));
														else
															releaseDate = "";
														%>
														<tr>
															<td><%=otherVer.getID()%></td>
															<td><%=status%></td>
															<td><%=releaseDate%></td>
															<td>
																<%
																if (searchEngine.skipByRegStatus(otherVer.getStatus())){ %>
																	&nbsp;<%
																}
																else{ %>
																	[<a href="dataset.jsp?mode=view&amp;ds_id=<%=otherVer.getID()%>">view</a>]<%
																}
																%>
															</td>
														</tr>
														<%
													}
													%>
													</tbody>
												</table>
												<%
											}
										}
										%>
										
									</div>
								<!-- end dotted -->
				
				<!-- various hidden inputs -->
				<div style="display:none">				
					<input type="hidden" name="mode" value="<%=mode%>"/>
					<input type="hidden" name="check_in" value="false"/>
					<input type="hidden" name="changed" value="0"/>
					<input type="hidden" name="complete" value="false"/>
					<input type="hidden" name="saveclose" value="false"/>
					<%
					String checkedoutCopyID = dataset==null ? null : dataset.getCheckedoutCopyID();
					if (checkedoutCopyID!=null){%>
						<input type="hidden" name="checkedout_copy_id" value="<%=checkedoutCopyID%>"/><%
					}
					if (dataset!=null){
						String checkInNo = dataset.getVersion();
						if (checkInNo.equals("1")){
							%>
							<input type="hidden" name="upd_version" value="true"/><%
						}
					}
					// submitter url, might be used by POST handler who might want to send back to POST submitter
					String submitterUrl = Util.getServletPathWithQueryString(request);
					if (submitterUrl!=null){
						submitterUrl = Util.replaceTags(submitterUrl);
						%>
						<input type="hidden" name="submitter_url" value="<%=submitterUrl%>"/><%
					}
					%>
				</div>				
			</form>
			</div>
			
			</div> <!-- workarea -->
			</div> <!-- container -->
			<%@ include file="footer.txt" %>
<script type="text/javascript" src="popbox.js"></script>
<script type="text/javascript">
// <![CDATA[
        var popclickpop = {
         'onclick' : function() { Pop(this,-50,'PopBoxImageLarge'); },
         'pbShowPopBar' : false,
         'pbShowPopImage' : false,
         'pbShowPopText' : false,
         'pbShowRevertImage': true,
         'pbShowPopCaption' : true
        };
        PopRegisterClass('poponmouseclick', popclickpop);
	popBoxShowPopBar = false;
// ]]>
</script>
</body>
</html>

<%
// end the whole page try block
}
catch (Exception e){
	if (response.isCommitted())
		e.printStackTrace(System.out);
	else{
		String msg = e.getMessage();
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
		e.printStackTrace(new PrintStream(bytesOut));
		String trace = bytesOut.toString(response.getCharacterEncoding());
		String backLink = history.getBackUrl();
		request.setAttribute("DD_ERR_MSG", msg);
		request.setAttribute("DD_ERR_TRC", trace);
		request.setAttribute("DD_ERR_BACK_LINK", backLink);
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {}
}
%>
