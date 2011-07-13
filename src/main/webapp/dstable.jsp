<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

	<%!
	// servlet-scope helper functions
	//////////////////////////////////

	/**
	 *
	 */
	private String getValue(String id, String mode, Vector attributes){
		return getValue(id, 0, mode, attributes);
	}

	/**
	 *	int val indicates which type of value is requested. the default is 0
	 *	0 - display value (if original value is null, then show inherited value)
	 *	1 - original value
	 *	2 - inherited value
	 */
	private String getValue(String id, int val, String mode, Vector attributes){
		if (id==null) return null;
		if (mode.equals("add") && val<2) return null;
		for (int i=0; attributes!=null && i<attributes.size(); i++){
			DElemAttribute attr = (DElemAttribute)attributes.get(i);
			if (id.equals(attr.getID())){
				if (val==1)
					return attr.getOriginalValue();
				else if (val==2)
					return attr.getInheritedValue();
				else
					return attr.getValue();

			}
		}
		return null;
	}

	/**
	 *
	 */
	private Vector getValues(String id, String mode, Vector attributes){
		return getValues(id, 0, mode, attributes);
	}

	/**
	 *  int val indicates which group of values is requested. the default is 0
	 *  0 - all
	 *  1 - original
	 *  2 - inherited
	 */
	private Vector getValues(String id, int val, String mode, Vector attributes){
		if (id==null) return null;
		if (mode.equals("add") && val<2) return null;
		for (int i=0; attributes!=null && i<attributes.size(); i++){
			DElemAttribute attr = (DElemAttribute)attributes.get(i);
			if (id.equals(attr.getID())){
				if (val==1)
					return attr.getOriginalValues();
				else if (val==2)
					return attr.getInheritedValues();
				else
					return attr.getValues();
			}
		}
		return null;
	}

	/**
	 *
	 */
	private String getAttributeIdByName(String name, Vector mAttributes){
		for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
			DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
	        if (attr.getShortName().equalsIgnoreCase(name))
	        	return attr.getID();
		}
	    return null;
	}

	/**
	 *
	 */
	private String getAttributeValue(DataElement elem, String name, Vector mAttributes){
		String id = getAttributeIdByName(name, mAttributes);
		if (elem == null) return null;
		DElemAttribute attr = elem.getAttributeById(id);
		if (attr == null) return null;
		return attr.getValue();
	}
	%>

	<%
	// implementation of the servlet's service method
	//////////////////////////////////////////////////

	request.setCharacterEncoding("UTF-8");
	ServletContext ctx = getServletContext();
	Vector mAttributes = null;
	Vector attributes = null;
	Vector complexAttrs = null;
	Vector elems = null;
	String mode = null;
	String dsIdf = null;

	// make sure page is not cached
	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
	response.setHeader("Expires", Util.getExpiresDateString());

	DDUser user = SecurityUtil.getUser(request);

	// POST request not allowed for anybody who hasn't logged in
	if (request.getMethod().equals("POST") && user==null){
		request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}

	// get values of several request parameters:
	// - mode
	// - table's id number
	// - table's alphanumeric identifier
	// - id number of table to copy
	// - dataset's id number
	String tableIdf = request.getParameter("table_idf");
	String tableID = request.getParameter("table_id");
	String copy_tbl_id = request.getParameter("copy_tbl_id");
	String dsID = request.getParameter("ds_id");
	String dsName = request.getParameter("ds_name");
	String parentNs = request.getParameter("pns");

	mode = request.getParameter("mode");

	if (mode == null || mode.trim().length()==0){
		mode = "view";
	}
	
	if (mode.equals("add")){
		if (Util.voidStr(dsID)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: ds_id");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	else if (mode.equals("view")){
		if (Util.voidStr(tableID) && (Util.voidStr(tableIdf) || Util.voidStr(parentNs))){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: table_id or (table_idf and pns)");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	else if (mode.equals("edit")){
		if (Util.voidStr(tableID)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: table_id");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	else if (mode.equals("copy")){
		if (Util.voidStr(copy_tbl_id)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: copy_tbl_id");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}

	// if requested by alphanumeric identifier and not by auto-generated id,
	// then it means the table's latest version is requested
	boolean isLatestRequested = mode.equals("view") && !Util.voidStr(tableIdf) && !Util.voidStr(parentNs) && Util.voidStr(tableID);

	//// handle the POST request//////////////////////
	//////////////////////////////////////////////////
	if (request.getMethod().equals("POST")){

		Connection userConn = null;
		DsTableHandler handler = null;
		try{
			userConn = user.getConnection();
			handler = new DsTableHandler(userConn, request, ctx);
			handler.setUser(user);
			handler.setVersioning(false);

			try{
				handler.execute();
			}
			catch (Exception e){
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
			// if this was add, send to the added copy
			String id = handler.getLastInsertID();
			if (id!=null && id.length()>0)
				redirUrl = redirUrl + "dstable.jsp?table_id=" + id;
			if (dsName!=null)
				redirUrl = redirUrl + "&ds_name=" + dsName;
			if (dsID!=null)
				redirUrl = redirUrl + "&ds_id=" + dsID;

			if (history!=null)
				history.remove(history.getCurrentIndex());
		}
		else if (mode.equals("edit")){
			// if this was a "saveclose", send to view mode, otherwise stay in edit mode
			QueryString qs = new QueryString(currentUrl);
			String strSaveclose = request.getParameter("saveclose");
			if (strSaveclose!=null && strSaveclose.equals("true"))
				qs.changeParam("mode", "view");
			else
				qs.changeParam("mode", "edit");
			redirUrl =qs.getValue();
		}
		else if (mode.equals("delete")){
			// if dataset id number given, send to view mode of the dataset working copy, otherwise to home page
			if (dsID!=null && dsID.length()>0)
				redirUrl = "dataset.jsp?ds_id=" + dsID;
			else
				redirUrl = "index.jsp";
		}
		else if (mode.equals("copy")){
			String id = handler.getLastInsertID();
			if (id!=null && id.length()>0)
				redirUrl = redirUrl + "dstable.jsp?mode=edit&table_id=" + id;
			if (history!=null)
				history.remove(history.getCurrentIndex());
		}

		response.sendRedirect(redirUrl);
		return;
	}
	//// end of handle the POST request //////////////////////
	// any code below must not be reached when POST request!!!

	Connection conn = null;

	// the whole page's try block
	try {
		conn = ConnectionUtil.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		searchEngine.setUser(user);

		// if not in add mode, get the table object
		DsTable dsTable = null;
		if (!mode.equals("add")){

			if (isLatestRequested){
				
				Vector v = new Vector();
				if (user==null){
					v.add("Recorded");
					v.add("Released");
				}
				dsTable = searchEngine.getLatestTbl(tableIdf, parentNs, v);
				if (dsTable!=null){
					tableID = dsTable.getID();
				}
			}
			else{
				dsTable = searchEngine.getDatasetTable(tableID);
			}

			if (dsTable == null){
				if (user!=null){
					request.setAttribute("DD_ERR_MSG", "Could not find a table of this id or identifier in any status");
				}
				else{
					request.setAttribute("DD_ERR_MSG", "Could not find a table of this id or identifier in 'Recorded' or 'Released' status! " +
					"As an anonymous user, you are not allowed to see definitions in any other status.");
				}
				session.setAttribute(AfterCASLoginServlet.AFTER_LOGIN_ATTR_NAME, SecurityUtil.buildAfterLoginURL(request));
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}

			// overwrite dataset id parameter with the one from table object
			dsID = dsTable.getDatasetID();
			if (dsID==null || dsID.length()==0){
				request.setAttribute("DD_ERR_MSG", "Missing dataset id number in the table object");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
		}

		// get the dataset object (having reached this point, dataset id number is not null)
		String workingUser = null;
		Dataset dataset = searchEngine.getDataset(dsID);
		if (dataset==null){
			request.setAttribute("DD_ERR_MSG", "No dataset found with this id number: " + dsID);
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
		// anonymous users should not be allowed to see tables of a dataset working copy
		if (mode.equals("view") && user==null && dataset.isWorkingCopy()){
			request.setAttribute("DD_ERR_MSG", "Anonymous users are not allowed to view tables from a dataset working copy!");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
		// anonymous users should not be allowed to see tables from datasets that are NOT in Recorded or Released status
		if (mode.equals("view") && user==null && dataset.getStatus()!=null && !dataset.getStatus().equals("Recorded") && !dataset.getStatus().equals("Released")){
			request.setAttribute("DD_ERR_MSG", "Tables from datasets NOT in Recorded or Released status are inaccessible for anonymous users.");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}

		// set some helper variables
		dsName = dataset.getShortName();
		dsIdf = dataset.getIdentifier();
		workingUser = dataset.getWorkingUser();
		boolean editDstPrm = user!=null && dataset.isWorkingCopy() && workingUser!=null && workingUser.equals(user.getUserName());

		// security checks for identified users
		if (!mode.equals("view") && editDstPrm==false){
			request.setAttribute("DD_ERR_MSG", "You have no permission to do modifications in this dataset!");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}

		// get metadata of attributes
		mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
		// get values of attributes
		attributes = searchEngine.getAttributes(tableID, "T", DElemAttribute.TYPE_SIMPLE, null, dsID);
		complexAttrs = searchEngine.getComplexAttributes(tableID, "T", null, null, dsID);
		if (complexAttrs==null)
			complexAttrs = new Vector();

		// get the table's elements
		if (mode.equals("view") && dsTable!=null)
			elems = searchEngine.getDataElements(null, null, null, null, tableID);

		// prepare the page's HTML title, shown in browser title bar
		StringBuffer pageTitle = new StringBuffer();
		if (mode.equals("edit"))
			pageTitle.append("Edit table");
		else
			pageTitle.append("Table");
		if (dsTable!=null && dsTable.getShortName()!=null)
			pageTitle.append(" - ").append(dsTable.getShortName());
		if (dataset!=null && dataset.getShortName()!=null)
			pageTitle.append("/").append(dataset.getShortName());
	%>

<%
// start HTML //////////////////////////////////////////////////////////////
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title><%=pageTitle.toString()%></title>
	<script type="text/javascript" src="modal_dialog.js"></script>
	<script type="text/javascript">
// <![CDATA[

		function submitForm(mode){

			if (mode == "delete"){
				<%
				if (dataset!=null && dataset.getStatus().equals("Released")){
					%>
					var a = confirm("Please be aware that you are about to remove a table from a dataset definition " +
					  		"in Released status. Unless you change the dataset definition's status back to something lower, " +
					  		"this removal will become instantly visible for the public visitors! " +
					  		"Click OK, if you want to continue. Otherwise click Cancel.");
					if (a==false) return;
					<%
				}
				%>
			}

			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory fields!");
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
			}

			//slctAllValues();

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

			document.forms["form1"].elements["upd_version"].value = v;
			deleteReady();
		}

		function deleteReady(){
			document.forms["form1"].elements["mode"].value = "delete";
			document.forms["form1"].submit();
		}

		function checkObligations(){

			var o = document.forms["form1"].short_name;
			if (o!=null && o.value.length == 0) return false;

			o = document.forms["form1"].idfier;
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

		function goTo(mode, id){
			document.location.assign("dstable.jsp?mode=" + mode + "&table_id=" + id + "&ds_id=<%=dsID%>&ds_name=<%=dsName%>");
		}

		function openElements(uri){
			//uri = uri + "&open=true";
			wElems = window.open(uri,"TableElements","height=500,width=750,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wElems.focus()}
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
				var selectName = "attr_mult_" + id;
				var oOption = new Option(val, val, false, false);
				var slct = document.forms["form1"].elements[selectName];
				if (slct.length==1 && slct.options[0].value=="" && slct.options[0].text=="")
					slct.remove(0);
				slct.options[slct.length] = oOption;
				slct.size=oOption.length;
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

		function startsWith(str, pattern){
			var i = str.indexOf(pattern,0);
			if (i!=-1 && i==0)
				return true;
			else
				return false;
		}

		function copyTbl(){

			if (document.forms["form1"].elements["idfier"].value==""){
				alert("Identifier cannot be empty!");
				return;
			}

			var url='search_table.jsp?ctx=popup';

			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
			if (window.focus){wAdd.focus()}
		}

		function pickTable(id, name){

			document.forms["form1"].elements["copy_tbl_id"].value=id;
			document.forms["form1"].elements["mode"].value="copy";
			document.forms["form1"].submit();
			return true;
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
String hlpScreen = "table";
if (mode.equals("edit"))
	hlpScreen = "table_edit";
else if (mode.equals("add"))
	hlpScreen = "table_add";

%>

<body>
<div id="container">
	<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Dataset table"/>
		<jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
	</jsp:include>
	<%@ include file="nmenu.jsp" %>

	<div id="workarea">

		<%
		if (mode.equals("view") && user!=null && dsTable!=null && dsTable.getIdentifier()!=null && dataset!=null && dataset.getIdentifier()!=null){
			%>
			<div id="operations">
				<ul>
					<li><a href="Subscribe?table=<%=dataset.getIdentifier()%>%2F<%=dsTable.getIdentifier()%>">Subscribe</a></li>
				</ul>
			</div><%
		}

		// main table head

		String pageHeadingVerb = "View";
		if (mode.equals("add"))
			pageHeadingVerb = "Add";
		else if (mode.equals("edit"))
			pageHeadingVerb = "Edit";
		%>
		<h1><%=pageHeadingVerb%> table <%if (mode.equals("add")){ %>to <a href="dataset.jsp?ds_id=<%=dsID%>"><%=Util.replaceTags(dsName)%></a> dataset<%}%></h1>

		<!-- The buttons displayed in view mode -->
			<%
			if (mode.equals("view") && editDstPrm==true){
			%>
			<div id="auth-operations">
				<h2>Operations:</h2>
				<ul>
				<li><a href="dstable.jsp?mode=edit&amp;table_id=<%=tableID%>&amp;ds_id=<%=dsID%>&amp;ds_name=<%=dsName%>">Edit</a></li>
				<li><a href="javascript:submitForm('delete')">Delete</a></li>
			<%
			// elements link
			String elemLink = "tblelems.jsp?table_id=" + tableID + "&amp;ds_id=" + dsID + "&amp;ds_name=" + dsName + "&amp;ds_idf=" + dsIdf;
			%>
				<li><a href="<%=elemLink%>">Manage elements</a></li>
				<li><a href="complex_attrs.jsp?parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=Util.replaceTags(dsTable.getShortName())%>&amp;dataset_id=<%=dsID%>">Manage complex attributes</a></li>
				</ul>
			</div>
			<%
			}
			%>
		<form id="form1" method="post" action="dstable.jsp" style="clear:both">

				<!-- add, save, check-in, undo check-out buttons -->
				<%
				if (mode.equals("add") || mode.equals("edit")){
					%>
					<div style="float:right">
						<%
						// add case
						if (mode.equals("add")){
							%>
							<input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>&nbsp;
							<input type="button" class="mediumbuttonb" value="Copy"
								onclick="alert('This feature is currently disabled! Please contact helpdesk@eionet.europa.eu for more information.');"
								title="Copies table structure and attributes from existing dataset table"/><%
						}
						// edit case
						else if (mode.equals("edit")){
							%>
							<input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&nbsp;
							<input type="button" class="mediumbuttonb" value="Save &amp; close" onclick="submitForm('editclose')"/>&nbsp;
							<input type="button" class="mediumbuttonb" value="Cancel" onclick="goTo('view', '<%=tableID%>')"/>
							<%
						}
						%>
					</div><%
				}
				%>

			<!--=======================-->
			<!-- main table inside div -->
			<!--=======================-->

			<table cellspacing="0" cellpadding="0" style="clear:right;border:0">

                <!-- main table body -->

				<tr>
					<td style="width:100%;height:10" colspan="2">

	                    	<!-- quick links -->

	                    	<%
	                    	if (mode.equals("view")){
		                    	Vector quicklinks = new Vector();

		                    	if (elems!=null && elems.size()>0)
		                    		quicklinks.add("Elements | elements");
		                    	if (complexAttrs!=null && complexAttrs.size()>0)
		                    		quicklinks.add("Complex attributes | cattrs");

		                    	request.setAttribute("quicklinks", quicklinks);
		                    	%>
	                    		<jsp:include page="quicklinks.jsp" flush="true" />
					            <%
							}
							%>

							<!-- schema, MS Excel template, XForm, XmlInst, etc -->

							<%
							if (mode.equals("view")){

								boolean dispAll = editDstPrm;
								boolean dispXLS = dataset!=null && dataset.displayCreateLink("XLS");
								boolean dispODS = dataset!=null && dataset.displayCreateLink("ODS");
								boolean dispXmlSchema = dataset!=null && dataset.displayCreateLink("XMLSCHEMA");
								boolean dispXmlInstance = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xmli");
								boolean dispXForm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xfrm");
								boolean dispCache = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u");

								if (dispAll || dispXLS || dispXmlSchema || dispXmlInstance || dispXForm || dispCache || dispODS){
									%>
									<div id="createbox">
											<table id="outputsmenu">
											<col style="width:73%"/>
											<col style="width:27%"/>
												<%
												// XML Schema link
												if (dispAll || dispXmlSchema){ %>
													<tr>
														<td>
															Create an XML Schema for this table
														</td>
														<td>
															<a rel="nofollow" href="GetSchema?id=TBL<%=tableID%>">
																<img style="border:0" src="images/xsd.png" width="16" height="16" alt=""/>
															</a>
														</td>
													</tr><%
												}

												// XML Instance link
												if (dispAll || dispXmlInstance){ %>
													<tr>
														<td>
															Create an instance XML for this table
														</td>
														<td>
															<a rel="nofollow" href="GetXmlInstance?id=<%=tableID%>&amp;type=tbl">
																<img style="border:0" src="images/xml.png" width="16" height="16" alt=""/>
															</a>
														</td>
													</tr><%
												}

												// XForm link
												if (dispAll || dispXForm){ %>
													<tr>
														<td>
															Create an XForm for this table
														</td>
														<td>
															<a rel="nofollow" href="GetXForm?id=<%=tableID%>">
																<img style="border:0" src="images/xml.png" width="16" height="16" alt=""/>
															</a>
														</td>
													</tr><%
												}

												// MS Excel link
												if (dispAll || dispXLS){ %>
													<tr>
														<td>
															Create an MS Excel template for this table&nbsp;<a onclick="pop(this.href);return false;" href="help.jsp?screen=table&amp;area=excel"><img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" /></a>
														</td>
														<td>
															<a rel="nofollow" href="GetXls?obj_type=tbl&amp;obj_id=<%=tableID%>"><img style="border:0" src="images/xls.png" width="16" height="16" alt=""/></a>
														</td>
													</tr><%
												}

												// OpenDocument spreadsheet template link
												if (dispAll || dispODS){ %>
													<tr>
														<td>
															Create an OpenDocument spreadsheet template for this table&nbsp;<a onclick="pop(this.href);return false;" href="help.jsp?screen=table&amp;area=ods"><img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" /></a>
														</td>
														<td>
															<a rel="nofollow" href="GetOds?type=tbl&amp;id=<%=tableID%>"><img style="border:0" src="images/ods.png" width="16" height="16" alt=""/></a>
														</td>
													</tr><%
												}

												// codelist
												if (dispAll || dispXmlSchema){ %>
													<tr>
														<td>
															Get the comma-separated codelists of this table
														</td>
														<td>
															<a rel="nofollow" href="CodelistServlet?id=<%=dsTable.getID()%>&amp;type=TBL">
																<img style="border:0" src="images/txt.png" width="16" height="16" alt=""/>
															</a>
														</td>
													</tr>
													<tr>
														<td>
															Get the codelists of this table in XML format
														</td>
														<td>
															<a rel="nofollow" href="CodelistServlet?id=<%=dsTable.getID()%>&amp;type=TBL&amp;format=xml">
																<img style="border:0" src="images/xml.png" width="16" height="16" alt=""/>
															</a>
														</td>
													</tr><%
												}

												// TESTING the link for creating dBase II format
												if (user!=null){
													String userName = user.getUserName();
													if (userName.equals("roug") || userName.equals("heinlja") || userName.equals("cryan")){
														%>
														<tr>
															<td>Create dBaseII</td>
															<td>
																<a rel="nofollow" href="GetDbf/<%=dsTable.getID()%>">
																	<img style="border:0" src="images/txt.png" width="16" height="16" alt=""/>
																</a>
															</td>
														</tr>
														<%
													}
												}


												// display the link about cache
												if (dispAll || dispCache){
													%>
													<tr style="height:20px;">
														<td colspan="2">
															<span class="barfont">
																[ <a rel="nofollow" onclick="pop(this.href);return false;" href="GetCache?obj_id=<%=tableID%>&amp;obj_type=tbl&amp;idf=<%=dsTable.getIdentifier()%>">Open cache ...</a> ]
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

									<table class="datatable" width="100%">
											<col style="width:<%=titleWidth%>%"/>
											<col style="width:4%"/>
											<% if (colspan==4){ %>
											<col style="width:4%"/>
											<% } %>
											<col style="width:<%=valueWidth%>%"/>

							  			<!-- static attributes -->

										<!-- short name -->
							    		<tr id="short_name_row">
											<th scope="row" class="scope-row short_name">Short name</th>
											<td class="short_name simple_attr_help">
												<a href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
													<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" />
												</a>
											</td>
											<%
											if (colspan==4){
												%>
												<td class="short_name simple_attr_help">
													<img style="border:0" src="images/mandatory.gif" width="16" height="16" alt=""/>
												</td><%
											}
											%>
											<td class="short_name_value">
												<%
												if (mode.equals("view")){ %>
													<%=Util.replaceTags(dsTable.getShortName())%>
													<input type="hidden" name="short_name" value="<%=Util.replaceTags(dsTable.getShortName(),true)%>"/><%
												}
												else if (mode.equals("add")){%>
													<input class="smalltext" type="text" size="30" name="short_name"/><%
												}
												else{ %>
													<input class="smalltext" type="text" size="30" name="short_name" value="<%=Util.replaceTags(dsTable.getShortName())%>"/><%
												}
												%>
											</td>

											<%isOdd = Util.isOdd(++displayed);%>
							    		</tr>

							    		<!-- dataset -->

							    		<tr class="zebra<%=isOdd%>">
							    			<th scope="row" class="scope-row simple_attr_title">
												Dataset
											</th>
											<td class="simple_attr_help">
												<a href="help.jsp?screen=table&amp;area=dataset" onclick="pop(this.href);return false;">
													<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" />
												</a>
											</td>
											<%
											if (colspan==4){%>
												<td class="simple_attr_help">
													<img style="border:0" src="images/mandatory.gif" width="16" height="16" alt=""/>
												</td><%
											}
											%>
											<td class="simple_attr_value">
												<a href="dataset.jsp?ds_id=<%=dsID%>">
													<b><%=Util.replaceTags(dsName)%></b>
												</a>
												<%
												if (mode.equals("view") && dataset.isWorkingCopy()){ %>
													<span class="caution">(Working copy)</span><%
												}
												%>
											</td>
											<%isOdd = Util.isOdd(++displayed);%>
							    		</tr>

							    		<!-- Reference URL -->
							    		<%
							    		String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
							    		if (mode.equals("view") && jspUrlPrefix!=null){
								    		String refUrl = jspUrlPrefix + "dstable.jsp?table_idf=" +
								    						dsTable.getIdentifier() + "&amp;pns=" + dsTable.getParentNs();
								    		%>
								    		<tr class="zebra<%=isOdd%>">
												<th scope="row" class="scope-row simple_attr_title">
													Reference URL
												</th>
												<td class="simple_attr_help">
													<a href="help.jsp?screen=dataset&amp;area=refurl" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" />
													</a>
												</td>
												<td class="simple_attr_value">
													<small><a href="<%=refUrl%>"><%=refUrl%></a></small>
												</td>

												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr><%
							    		}
							    		%>

							    		<!-- dynamic attributes -->

							    		<%
							    		String attrID = null;
										String attrValue = null;
							    		DElemAttribute attribute = null;
							    		boolean imagesQuicklinkSet = false;

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

											if (!attribute.displayFor("TBL")) continue;

											attrID = attribute.getID();
											attrValue = getValue(attrID, mode, attributes);

											String width  = attribute.getDisplayWidth();
											String height = attribute.getDisplayHeight();

											boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
											boolean inherit = attribute.getInheritable().equals("0") ? false:true;

											if (mode.equals("view") && (attrValue==null || attrValue.length()==0))
												continue;

											// if image attribute and no reason to display then skip
											if (dispType.equals("image")){
												if (mode.equals("add") || (mode.equals("edit") && user==null) || (mode.equals("view") && Util.voidStr(attrValue)))
													continue;
											}

											Vector multiValues=null;
											String inheritedValue=null;

											if (!mode.equals("view")){

												if (inherit) inheritedValue = getValue(attrID, 2, mode, attributes);

												if (mode.equals("edit")){
													if (dispMultiple){
														if (inherit){
															multiValues = getValues(attrID, 1, mode, attributes); //original values only
														}
														else{
															multiValues = getValues(attrID, 0, mode, attributes);  //all values
														}

													}
													else{
														if (inherit) attrValue = getValue(attrID, 1, mode, attributes);  //get original value
													}
												}
											}

								    		%>

										    <tr class="zebra<%=isOdd%>">
												<th scope="row" class="scope-row simple_attr_title">
													<%=Util.replaceTags(attribute.getShortName())%>
												</th>
												<td class="simple_attr_help">
													<a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" />
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td class="simple_attr_help">
														<img style="border:0" src="images/<%=Util.replaceTags(obligImg)%>" width="16" height="16" alt=""/>
													</td><%
												}
												%>

												<!-- dynamic attribute value display -->

												<td class="simple_attr_value"><%

													// handle image attribute first
													if (dispType.equals("image")){
														
														if (!imagesQuicklinkSet){ %>
															<a id="images"></a><%
															imagesQuicklinkSet = true;
														}

														// thumbnail
														if (mode.equals("view") && !Util.voidStr(attrValue)){
															%>
															<div class="figure-plus-container">
																<div class="figure-plus">
																	<div class="figure-image">
																		<a href="visuals/<%=Util.replaceTags(attrValue)%>">
																			<img src="visuals/<%=Util.replaceTags(attrValue)%>" alt="Image file could not be found on the server" class="scaled poponmouseclick"/>
																		</a>
																	</div>
																</div>
															</div><%
														}
														
														// link to image edit page
														if (mode.equals("edit") && user!=null){
															String actionText = Util.voidStr(attrValue) ? "add image" : "manage this image";
															%>
															<span class="barfont">
																<a href="imgattr.jsp?obj_id=<%=tableID%>&amp;obj_type=T&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=Util.replaceTags(dsTable.getShortName())%>&amp;attr_name=<%=Util.replaceTags(attribute.getShortName())%>">Click to <%=Util.replaceTags(actionText)%></a>]
															</span><%
														}
													}
													// if view mode, display simple text
													else if (mode.equals("view")){ %>
														<%=Util.replaceTags(attrValue)%><%
													}
													// if non-view mode, display input
													else{

														// inherited value(s)
														if (inherit && inheritedValue!=null){

															String sInhText = (((dispMultiple && multiValues!=null) ||
																				(!dispMultiple && attrValue!=null)) &&
																				attribute.getInheritable().equals("2")) ?
																				"Overriding parent level value: " :
																				"Inherited from parent level: ";

															if (sInhText.startsWith("Inherited")){ %>
																<%=sInhText%><%=Util.replaceTags(inheritedValue)%><br/><%
															}
														}

														// mutliple display
														if (dispMultiple && !dispType.equals("image")){

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
															<input type="text" name="<%=textName%>" value="insert other value" style="font-size:0.9em;color:#76797C" onfocus="this.value=''"/>
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
															</div><%
														}
														// no multiple display
														else{

															if (dispType.equals("text")){
																if (attrValue!=null){
																	%>
																	<input type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>" onchange="form_changed('form1')"/>
																	<%
																}
																else{
																	%>
																	<input type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"/>
																	<%
																}
															}
															else if (dispType.equals("textarea")){
																if (attrValue!=null){
																	%>
																	<textarea class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"><%=Util.replaceTags(attrValue, true, true)%></textarea>
																	<%
																}
																else{
																	%>
																	<textarea class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"></textarea>
																	<%
																}
															}
															else if (dispType.equals("select")){ %>
																<select class="small" name="attr_<%=attrID%>" onchange="form_changed('form1')">
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
																			<option <%=isSelected%> value="<%=Util.replaceTags(fxValue.getValue())%>"><%=Util.replaceTags(fxValue.getValue())%></option> <%
																		}
																	}
																	%>
																</select>
																<a onclick="pop(this.href);return false;" href="fixed_values.jsp?delem_id=<%=attrID%>&amp;delem_name=<%=Util.replaceTags(attribute.getShortName())%>&amp;parent_type=attr">
																	<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" />
																</a>
																<%
															}
															else{ %>
																Unknown display type!<%
															}
														}

													} // end display input
													%>
													<input type="hidden" name="oblig_<%=attrID%>" value="<%=Util.replaceTags(attribute.getObligation(),true)%>"/>
												</td>
												<!-- end dynamic attribute value display -->

												<%isOdd = Util.isOdd(++displayed);%>
										    </tr>
										    <%
									    }
									    %>

							    		<!-- Identifier -->
							    		<tr class="zebra<%=isOdd%>">
											<th scope="row" class="scope-row simple_attr_title">
												Identifier
											</th>
											<td class="simple_attr_help">
												<a href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
													<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help" />
												</a>
											</td>
											<%
											if (colspan==4){%>
												<td class="simple_attr_help">
													<img style="border:0" src="images/mandatory.gif" width="16" height="16" alt=""/>
												</td><%
											}
											%>
											<td class="simple_attr_value">
												<%
												if(!mode.equals("add")){ %>
													<b><%=Util.replaceTags(dsTable.getIdentifier())%></b>
													<input type="hidden" name="idfier" value="<%=dsTable.getIdentifier()%>"/><%
												}
												else{ %>
													<input type="text" class="smalltext" size="30" name="idfier"/><%
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

									<!-- table elements -->

									<%
									if ((mode.equals("view") && elems!=null && elems.size()>0) || mode.equals("edit")){


										colspan = user==null ? 1 : 2;

										// check if has GIS elements
										boolean hasGIS = false;
										for (int i=0; elems!=null && i<elems.size(); i++){
											DataElement elm = (DataElement)elems.get(i);
											if (elm.getGIS()!=null){
												hasGIS = true;
												break;
											}
										}

										// start the table elements loop
										int loops = hasGIS ? 2 : 1;
										for (int j=1; j<=loops; j++){

											String curMode = hasGIS && j==2 ? "GIS" : "NOGIS";
											String title = "";
											if (!hasGIS)
												title = "Elements";
											else if (mode.equals("view")){
												if (curMode.equals("NOGIS"))
													title = "Elements";
												else
													title = "Metadata elements";
											}

											boolean hasMarkedElems = false;
											boolean hasForeignKeys = false;
											boolean hasMultivalElms = false;
											boolean hasCommonElms = false;
											boolean hasMandatoryElms = false;
											%>

												<h2><%=Util.replaceTags(title)%></h2>
												<p>
													<%
													if (j<=1){%>
														<a id="elements"></a><%
													}
													%>
												</p>

												<%
												// elements (or GIS elements) table
												if (mode.equals("view") && elems!=null && elems.size()>0){

													// set colwidths depending on current mode (GIS or NOGIS)
													String widthShortName = curMode.equals("NOGIS") ? "50%" : "40%";
													String widthDatatype  = curMode.equals("NOGIS") ? "19%" : "15%";
													String widthElemtype  = curMode.equals("NOGIS") ? "31%" : "30%";
													String widthType      = "15%";

													Hashtable types = new Hashtable();
													types.put("CH1", "Fixed values");
													types.put("CH2", "Quantitative");
													%>
										      				<table class="datatable subtable">
																	<col style="width:<%=widthShortName%>"/>
																	<% if (curMode.equals("GIS")){ %>
																	<col style="width:<%=widthType%>"/>
																	<% } %>
																	<col style="width:<%=widthDatatype%>"/>
																	<col style="width:<%=widthElemtype%>"/>
																<tr>
																	<th>Short name</th>
																	<%
																	if (curMode.equals("GIS")){ %>
																		<th>GIS type</th><%
																	}
																	%>
																	<th>Datatype</th>
																	<th>Element type</th>
																</tr>

																<%
																// rows loop
																for (int i=0; i<elems.size(); i++){

																	DataElement elem = (DataElement)elems.get(i);
																	String gisType = elem.getGIS();

																	if (curMode.equals("GIS") && gisType==null)
																		continue;
																	if (curMode.equals("NOGIS") && gisType!=null)
																		continue;

																	boolean elmCommon = elem.getNamespace()==null || elem.getNamespace().getID()==null;
																	String elemLink = "data_element.jsp?delem_id=" + elem.getID();
																	String elemDefinition = elem.getAttributeValueByShortName("Definition");
																	String linkTitle = elemDefinition==null ? "" : elemDefinition;
																	String elemType = (String)types.get(elem.getType());
																	String datatype = getAttributeValue(elem, "Datatype", mAttributes);
																	if (datatype == null) datatype="";
																	String max_size = getAttributeValue(elem, "MaxSize", mAttributes);
																	if (max_size == null) max_size="";

																	// see if the element is part of any foreign key relations
																	Vector _fks = searchEngine.getFKRelationsElm(elem.getID(), dataset.getID());
																	boolean fks = (_fks!=null && _fks.size()>0) ? true : false;
																	
																	// flag indicating if element can have multiple values
																	boolean isMulitvalElem = elem.getValueDelimiter()!=null;
																	%>
																	<tr>
																		<!-- short name -->
																		<td>
																			<%
																			if (elem.isMandatoryFlag()){
																				%>
																				<span style="font:bold;font-size:1.2em">*</span>
																				<%
																				hasMandatoryElms = true;
																			}
																			%>
																			<a href="<%=elemLink%>" title="<%=Util.replaceTags(linkTitle, true, true)%>">
																				<%=Util.replaceTags(elem.getShortName())%>
																			</a>

																			<%
																			if (elmCommon){ %>
																				<sup style="color:#858585;font-weight:bold;">C</sup><%
																				hasCommonElms = true;
																			}

																			// FK indicator
																			if (fks){ %>
																				&nbsp;
																				<a href="foreign_keys.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=Util.replaceTags(elem.getShortName())%>&amp;ds_id=<%=dsID%>&amp;table_id=<%=tableID%>">
																					<span style="font: bold italic">(FK)</span>
																				</a><%
																				hasForeignKeys = true;
																			}
																			%>
																		</td>
																		<!-- gis type -->
																		<%
																		if (curMode.equals("GIS")){
																			gisType = (gisType==null || gisType.length()==0) ? "&nbsp;" : gisType;
																			%>
																			<td>
																				<%=gisType%>
																			</td><%
																		}
																		%>
																		<!-- datatype -->
																		<td>
																			<%=Util.replaceTags(datatype)%>
																		</td>
																		<!-- element type -->
																		<td>
																			<%
																			if (elem.getType().equals("CH1")){ %>
																				<a href="fixed_values.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=Util.replaceTags(elem.getShortName())%>">
																					<%=Util.replaceTags(elemType)%>
																				</a> <%
																			}
																			else{ %>
																				<%=Util.replaceTags(elemType)%><%
																			}
																			
																			if (isMulitvalElem){ %>
																				<sup style="color:#858585;font-weight:bold;">+</sup><%
																				hasMultivalElms = true;
																			}
																			%>
																		</td>
																	</tr><%
																}
																%>
															</table>

										      		<%
										      		if (hasMandatoryElms){
											      		%>
											      		<div class="barfont">
															(an asterisk in front of element short name indicates that the element is mandatory in this table)
														</div><%
										      		}
										      		if (user!=null && elems!=null && elems.size()>0 && hasMarkedElems){%>
														<div class="barfont">
																(a red wildcard stands for checked-out element)
														</div><%
													}
													if (user!=null && elems!=null && elems.size()>0 && hasForeignKeys){%>
														<div>
																(the <em style="font-weight:bold;text-decoration:underline">(FK)</em> link indicates the element participating in a foreign key relation)
														</div><%
													}
													if (elems!=null && elems.size()>0 && hasCommonElms){%>
														<div class="barfont">
															(the <sup style="color:#858585;">C</sup> sign marks a common element)
														</div><%
													}
													if (elems!=null && elems.size()>0 && hasMultivalElms){%>
														<div class="barfont">
															(the <sup style="color:#858585;">+</sup> sign right after the "Fixed values" link marks an element that can have multiple values)
														</div><%
													}
												}
												else if (mode.equals("edit")){
													// in edit case we display only the link anyway and we don't
													// wont to display it twice (once for GIS elems and once for simply elems)
													// So we break the loop here.
													break;
												}
										}
									}
									%>


									<!-- complex attributes -->

									<%
									if ((mode.equals("edit") && user!=null) || (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0)){

										colspan = user==null ? 1 : 2;
										%>


											<h2 id="cattrs">Complex attributes</h2>
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
																		<a href="complex_attr.jsp?attr_id=<%=attrID%>&amp;parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=Util.replaceTags(dsTable.getShortName())%>&amp;dataset_id=<%=dsID%>" title="Click here to view all the fields">
																			<%=Util.replaceTags(attrName)%>
																		</a>
																	</td>
																	<td>
																		<a onclick="pop(this.href);return false;" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX">
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
									}
									%>
									<!-- end complex attributes -->

							<!-- end dotted -->
			</div>
					</td>
				</tr>


			</table>

				<!-- end main table body -->
			<!-- end main table -->

			<div style="display:none">
				<%
				// hidden inputs
				////////////////

				// dataset name
				if (dsName!=null && dsName.length()>0){ %>
					<input type="hidden" name="ds_name" value="<%=Util.replaceTags(dsName,true)%>"/> <%
				}
				else{ %>
					<input type="hidden" name="ds_name"/><%
				}
				// table id number
				if (!mode.equals("add")){ %>
					<input type="hidden" name="table_id" value="<%=tableID%>"/>
					<input type="hidden" name="del_id" value="<%=tableID%>"/><%
				}
				// corresponding namespace id
				if (!mode.equals("add") && dsTable.getNamespace()!=null){ %>
					<input type="hidden" name="corresp_ns" value="<%=dsTable.getNamespace()%>"/><%
				}
				// parent namespace id
				if (dataset.getNamespaceID()!=null){ %>
					<input type="hidden" name="parent_ns" value="<%=dataset.getNamespaceID()%>"/><%
				}
				// submitter url, might be used by POST handler who might want to send back to POST submitter
				String submitterUrl = Util.getServletPathWithQueryString(request);
				if (submitterUrl!=null){
					submitterUrl = Util.replaceTags(submitterUrl);
					%>
					<input type="hidden" name="submitter_url" value="<%=submitterUrl%>"/><%
				}
				%>
				<input type="hidden" name="mode" value="<%=mode%>"/>
				<input type="hidden" name="ds_id" value="<%=dsID%>"/>
				<input type="hidden" name="copy_tbl_id" value=""/>
				<input type="hidden" name="changed" value="0"/>
				<input type="hidden" name="saveclose" value="false"/>
			</div>

		</form>
	</div> <!-- end workarea -->
	</div> <!-- container -->
	<%@ include file="footer.txt" %>
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
