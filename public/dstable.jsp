<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,eionet.util.QueryString"%>

<%!ServletContext ctx=null;%>
<%!private Vector mAttributes=null;%>
<%!private Vector attributes=null;%>
<%!private Vector complexAttrs=null;%>
<%!private Vector elems=null;%>
<%!private String mode=null;%>

<%@ include file="history.jsp" %>

<%!
private String getValue(String id){
	return getValue(id, 0);
}
/*
		int val indicates which type of value is requested. the default is 0
		0 - display value (if original value is null, then show inherited value)
		1 - original value
		2 - inherited value
*/
private String getValue(String id, int val){
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
private Vector getValues(String id){
	return getValues(id, 0);
}

/*
		int val indicates which group of values is requested. the default is 0
		0 - all
		1 - original
		2 - inherited
*/
private Vector getValues(String id, int val){
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

private String getAttributeIdByName(String name){
	

		for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}
private String getAttributeValue(DataElement elem, String name){
	
	String id = getAttributeIdByName(name);
	if (elem == null) return null;
	DElemAttribute attr = elem.getAttributeById(id);
	if (attr == null) return null;
	return attr.getValue();
}

%>

<%

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

XDBApplication.getInstance(getServletContext());

// check if the user is authorized
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

String disabled = user == null ? "disabled" : "";

mode = request.getParameter("mode");
if (mode == null || mode.length()==0) { %>
	<b>Mode paramater is missing!</b>
	<%
	return;
}

String tableID = request.getParameter("table_id");
if (!mode.equals("add") && (tableID == null || tableID.length()==0)){ %>
	<b>Table ID is missing!</b> <%
	return;
}

String dsID   = request.getParameter("ds_id");
String dsName = request.getParameter("ds_name");
String dsIdf = null;

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
searchEngine.setUser(user);

// get the table
DsTable dsTable = null;
if (tableID!=null){	
	dsTable = searchEngine.getDatasetTable(tableID);		
	if (dsTable == null){ %>
		<b>Table was not found!</b> <%
		return;
	}
}

// get the dataset
Dataset dataset = null;
String dsNs = null;
if (dsTable!=null){
	dsID = dsTable.getDatasetID();
	if (dsID!=null){		
		dataset = searchEngine.getDataset(dsID);
		if (dataset!=null){
			dsName = dataset.getShortName();
			dsNs = dataset.getNamespaceID();
			dsIdf = dataset.getIdentifier();
		}
	}
}

if (dataset==null && (mode.equals("view") || mode.equals("edit"))){ %>
	<b>Could not relate this table to any dataset, cannot continue!</b>	
	<%
	return;
}
else if (dsID!=null && dsID.length()>0){
	Dataset dst = searchEngine.getDataset(dsID);
	if (dst!=null)
		dsIdf = dst.getIdentifier();
}

// check permission for add
if (mode.equals("add")){
	if (dsIdf!=null){
		if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u")){ %>
			<b>Not allowed!</b><%
			return;
		}
	}
	else{
		if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){ %>
			<b>Not allowed!</b><%
			return;
		}
	}
}

String contextParam = request.getParameter("ctx");
if (contextParam == null) contextParam = "";

ctx = getServletContext();

boolean editPrm = false;
boolean deletePrm = false;

//handle the POST
if (request.getMethod().equals("POST")){
	
	if (dsIdf==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u")){%>
		<b>Not allowed!</b><%
		return;
	}
	
	Connection userConn = null;
	String redirUrl = "";
	DsTableHandler handler = null;
	
	try{
		userConn = user.getConnection();
		handler = new DsTableHandler(userConn, request, ctx);
		handler.setUser(user);
			
        String copy_tbl_id = request.getParameter("copy_tbl_id");//copy table function
        if (copy_tbl_id != null && copy_tbl_id.length()!=0){
			handler.setVersioning(false);
		}
		
		try{
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
		

		if (mode.equals("add")){
			String id = handler.getLastInsertID();
			if (id != null && id.length()!=0)
				redirUrl = redirUrl + "dstable.jsp?mode=edit&table_id=" + id +
								 "&ctx=" + contextParam;
				if (dsName!=null) redirUrl = redirUrl + "&ds_name=" + dsName;
				if (dsID!=null) redirUrl = redirUrl + "&ds_id=" + dsID;
				
				if (history!=null){
					int idx = history.getCurrentIndex();
					if (idx>0)
						history.remove(idx);
				}
		}
		else if (mode.equals("edit")){
			
			// if this was check in & new version was created , send to "view" mode
			QueryString qs = new QueryString(currentUrl);
			String checkIn = request.getParameter("check_in");			
        	if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
	        	qs.changeParam("mode", "view");
	        	
	        	//JH041203 - remove previous url (with edit mode) from history
				history.remove(history.getCurrentIndex());
        	}
	        else
	        	qs.changeParam("mode", "edit");
	        
			redirUrl =qs.getValue();
			//redirUrl = redirUrl + "dstable.jsp?mode=edit&table_id=" + tableID + "&ds_name=" + dsName + "&ds_id=" + dsID + "&ctx=" + contextParam;
		}
		else if (mode.equals("delete")){
			
			String lid = request.getParameter("latest_id");
			String newDstID = handler.getNewDstID();
			
			if (!Util.voidStr(newDstID))
				redirUrl = redirUrl + "dataset.jsp?mode=view&ds_id=" + newDstID;
			else if (!Util.voidStr(lid))
				redirUrl = redirUrl + "dstable.jsp?mode=view&table_id=" + lid;
			else{
				String[] pages={"datasets.jsp","search_results_tbl.jsp","dataset.jsp","dstables.jsp"};
				String	deleteUrl = history.gotoLastMatching(pages);
				redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
			}
		}
		else if (mode.equals("restore")){
			
			String restoredID = handler.getRestoredID();
			if (restoredID!=null)
				redirUrl = redirUrl + "dstable.jsp?mode=view&table_id=" + restoredID;
			else{
				String[] pages={"datasets.jsp","search_results_tbl.jsp","dataset.jsp","dstables.jsp"};
				String deleteUrl = history.gotoLastMatching(pages);
				redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ? deleteUrl:redirUrl + "/index.jsp";
			}
		}
		else if (mode.equals("force_status")){
			redirUrl = redirUrl + "dstable.jsp?mode=view&table_id=" + tableID;
		}
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}
	
	response.sendRedirect(redirUrl);
	return;
}


//handle the GET

try {
	
if (dsTable!=null){
	editPrm = user!=null && dsIdf!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u");
	deletePrm = editPrm;
}

if (mode.equals("edit") && !editPrm){ %>
	<b>Not allowed!</b> <%
	return;
}

// version management

VersionManager verMan = new VersionManager(conn, searchEngine, user);

// find out if it's the latest version of such table
String latestID = dsTable==null ? null : verMan.getLatestTblID(dsTable);
boolean isLatest = Util.voidStr(latestID) ? true : latestID.equals(dsTable.getID());

//
String workingUser = null;
if (dsTable!=null){
	workingUser = verMan.getTblWorkingUser(dsTable.getIdentifier(), dsNs);
}

// implementing check-out/check-in
if (mode.equals("edit") && user!=null && user.isAuthentic()){
	
	// see if table is checked out
	if (Util.voidStr(workingUser)){
	    // table not checked out, create working copy
	    // but first make sure it's the latest version
	    if (!isLatest){ %>
	    	<b>Trying to check out a version that is not the latest!</b><%
	    	return;
	    }
	    
	    String copyID = verMan.checkOut(tableID, "tbl");
	    if (!tableID.equals(copyID)){
		    
		    // send to copy if created successfully
		    // but first remove previous url (edit original) from history
			history.remove(history.getCurrentIndex());
			
		    String qryStr = "mode=edit";
		    qryStr+= "&table_id=" + copyID;
		    qryStr+= "&ds_id=" + dsID;
		    qryStr+= "&ds_name=" + dsName;
	        response.sendRedirect("dstable.jsp?" + qryStr);
        }
    }
    else if (!workingUser.equals(user.getUserName())){
	    // table is chekced out by another user
	    %>
	    <b>This table is already checked out by another user: <%=workingUser%></b>
	    <%
	    return;
    }
    else if (dsTable!=null && !dsTable.isWorkingCopy()){
	    
	    // table is checked out by THIS user.
	    // If it's not the working copy, send the user to it
	    String copyID = verMan.getWorkingCopyID(dsTable);
	    if (copyID!=null && !copyID.equals(tableID)){
		    
		    // first remove previous url (edit original) from history
			history.remove(history.getCurrentIndex());
		    
			String qryStr = "mode=edit";
			qryStr+= "&table_id=" + copyID;
			qryStr+= "&ds_id=" + dsID;
		    qryStr+= "&ds_name=" + dsName;
			response.sendRedirect("dstable.jsp?" + qryStr);
		}
    }
}
			
// get attributes metadata
mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);

// get simple & complex attributes
//attributes = searchEngine.getAttributes(tableID, "T", DElemAttribute.TYPE_SIMPLE, null, );
// EK we want to show also inherited attributes from parent level
attributes = searchEngine.getAttributes(tableID, "T", DElemAttribute.TYPE_SIMPLE, null, dsID);
complexAttrs = searchEngine.getComplexAttributes(tableID, "T", null, null, dsID);		
if (complexAttrs == null) complexAttrs = new Vector();


// get the table elements
if (!mode.equals("add"))
	elems = searchEngine.getDataElements(null, null, null, null, tableID);


// find out if the table's latest dataset is also the latest such dataset
if (isLatest){
	if (dataset!=null){
		String latestDstId = verMan.getLatestDstID(dataset);
		if (!latestDstId.equals(dataset.getID()))
			isLatest = false;
	}
}
	
// initialize some stuff
DElemAttribute attribute = null;
String attrID = null;
String attrValue = null;

boolean hasHistory = false;
if (mode.equals("edit") && dsTable!=null){
	Vector v = searchEngine.getTblHistory(dsTable.getIdentifier(),
									  dsTable.getDatasetName(),
									  dsTable.getVersion() + 1);
	if (v!=null && v.size()>0)
		hasHistory = true;
}

%>

<html>
<head>
	<title>Meta</title>
	<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
	<link href="eionet.css" rel="stylesheet" type="text/css"/>	
</head>
<script language="JavaScript" src='script.js'></script>
<script language="JavaScript" src='modal_dialog.js'></script>
<script language="JavaScript">
    
		function submitForm(mode){
			
			if (mode == "delete"){
				var b = "";
				
				<%
				if (!mode.equals("add") && dsTable.isWorkingCopy()){ %>
					b = confirm("This working copy will be deleted and the corresponding table released for others to edit! Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				else{ %>
					b = confirm("This table's latest version will be deleted! This will also result in updating the version" +
								"of a dataset where this table might belong to. Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				%>
				
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations(mode)){
					alert("You have not specified one of the mandatory fields!");
					return;
				}
				
				if (hasWhiteSpace("idfier")){
					alert("Identifier cannot contain any white space!");
					return;
				}
			}

			slctAllValues();
			
			document.forms["form1"].elements["mode"].value = mode;
			
			var oDsId = document.forms["form1"].ds_id;
			if (mode=="add" && oDsId!=null && oDsId.options!=null){
				document.forms["form1"].ds_name.value = oDsId.options[oDsId.selectedIndex].text;
			}
			
			document.forms["form1"].submit();
		}
		
		function checkObligations(mode){
			
			var o = document.forms["form1"].short_name;
			if (o!=null)
				if (o.value.length == 0) return false;
				
			var oDsId = document.forms["form1"].ds_id;
			if (mode=="add" && (oDsId == null || oDsId.value==null || oDsId.value.length==0))
				return false;
			
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
			if (mode == "edit"){
				document.location.assign("dstable.jsp?mode=edit&table_id=" + id + "&ds_id=<%=dsID%>&ds_name=<%=dsName%>");
			}
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
		function startsWith(str, pattern){
			var i = str.indexOf(pattern,0);
			if (i!=-1 && i==0)
				return true;
			else
				return false;
		}
		
		function complexAttrs(url){
					wComplexAttrs = window.open(url,"ComplexAttributes","height=500,width=500,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
					if (window.focus) {wComplexAttrs.focus()}
		}
		function complexAttr(url){
					wComplexAttrs = window.open(url,"ComplexAttribute","height=600,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
					if (window.focus) {wComplexAttrs.focus()}
		}
		
		function checkIn(){
			
			<%
			/*
			if (hasHistory){ %>
				openDialog("yesno_dialog.html", "Do you want to increment the table's internal version?", retVersionUpd,100, 400);
				return; <%
			}
			else{
				*/
				%>
				if (document.forms["form1"].elements["is_first"]){
					if (document.forms["form1"].elements["is_first"].value=="true"){
						openDialog("yesno_dialog.html",
								   "Do you want to update parent dataset version?",
								   retParentUpd,100, 400);
						return;
					}
				}
				
				submitCheckIn();
			<%
			//}
			%>
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
			
			if (document.forms["form1"].elements["is_first"]){
				if (document.forms["form1"].elements["is_first"].value=="true"){
					openDialog("yesno_dialog.html",
							   "Do you want to update parent dataset version?",
							   retParentUpd,100, 400);
					return;
				}
			}
			
			submitCheckIn();
		}
		
		function retParentUpd(){
			var v = dialogWin.returnValue;
			if (v==null) v=true;
			document.forms["form1"].elements["ver_upw"].value = v;
			
			submitCheckIn();
		}
		
		function viewHistory(){
			var url = "tbl_history.jsp?table_id=<%=tableID%>";
			window.open(url,null,"height=400,width=400,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=yes");
		}
		function copyTbl(){
			
			if (document.forms["form1"].elements["idfier"].value==""){
				alert("Identifier cannot be empty!");
				return;
			}

			var url='search_table.jsp?ctx=popup';
			
			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
			if (window.focus) {wAdd.focus()}
		}
		function pickTable(id, name){
			//alert(id + " - " + name);
			document.forms["form1"].copy_tbl_id.value=id;
			document.forms["form1"].mode.value="add";
			submitForm('add');

			return true;
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
		
		function restore(){
			var b = confirm("This version of the table will now become the new latest version. " +
							"The version of the parent dataset will be updated as well. " +
							"Click OK, if you want to continue. Otherwise click Cancel.");
			if (b==false) return;
	    	document.forms["form1"].elements["mode"].value = "restore";
       		document.forms["form1"].submit();
    	}
    	
    	function forceStatus(status){
			var b = confirm("This will force the '" + status + "' to lower levels as well, affecting all " +
								"data elements within this table. Click OK, if you " +
								"still want to continue. Otherwise click Cancel.");			
			if (b==false) return;
			
			document.forms["form1"].elements["mode"].value = "force_status";
			document.forms["form1"].elements["force_status"].value = status;
			document.forms["form1"].submit();
		}
</script>

<body>
<%@ include file="header.htm" %>

<script language="JAVASCRIPT" for="window" event="onload">    
			<%
			String type = "";
			if (!mode.equals("add")){
				
				type = dsTable.getType();
				if (type != null && !mode.equals("view")){
    				%>
					var type = '<%=type%>';
					var o = document.forms["form1"].type;
					for (i=0; o!=null && i<o.options.length; i++){
						if (o.options[i].value == type){
							o.selectedIndex = i;
							break;
						}
					}
					<%
				}
			}
			%>
	</script>
<table border="0">
    <tr valign="top">
		    <td nowrap="true" width="125">
		        <p><center>
		            <%@ include file="menu.jsp" %>
		        </center></P>
		    </td>
        <TD>      
	        <jsp:include page="location.jsp" flush='true'>
	            <jsp:param name="name" value="Dataset table"/>
                <jsp:param name="back" value="true"/>
	        </jsp:include>
	        
	        <div style="margin-left:30">
	
<form name="form1" method="POST" action="dstable.jsp">
	
	<table width="600" cellspacing="0">
	
		<%
		//if (contextParam.equals("ds")){
		if (false){ %>
			<tr>
				<td colspan="2">
					<a href="dstables.jsp?ds_id=<%=dsID%>&amp;ds_name=<%=dsName%>">&lt; back to dataset tables</a>
				</td>
			</tr>
		<%
		}
		%>
		
		<tr height="10"><td></td></tr>
		
		<tr>
			<%
			
			boolean topFree = false;
			
			if (mode.equals("add")){ %>
				<td colspan="2">
					<span class="head00">Add a new table definition</span>
					<%
					if (dsID != null && dsID.length()!=0){ %>
						<span class="head00">to</span>
						<span class="title2">
						<a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><%=Util.replaceTags(dsName)%></a></span>
						<span class="head00">dataset</span> <%
					}
					%>
				</td> <%
			}
			else if (mode.equals("edit")){ %>
				<td colspan="2">
					<span class="head00">Edit table definition</span>
					<!--span class="title2"><%=Util.replaceTags(dsTable.getShortName())%></span>
					<span class="head00">table in</span>
					<a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><span class="title2"><%=Util.replaceTags(dsName)%></span></a>
					<span class="head00">dataset</span-->
				</td> <%
			}
			else{
				
				// set the flag indicating if the top namespace is in use
				String topWorkingUser = verMan.getWorkingUser(dsTable.getParentNs());
				topFree = topWorkingUser==null ? true : false;
			
				%>
				<td><span class="head00">View table definition</span></td>
				<!--td>
					<span class="head00">View definition of </span>
					<span class="title2"><%=Util.replaceTags(dsTable.getShortName())%></span>
					<span class="head00">table in</span>
					<a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><span class="title2"><%=Util.replaceTags(dsName)%></span></a>
					<span class="head00">dataset</span>
				</td-->
				
				<td align="right">
					<input type="button" class="smallbutton" value="History" onclick="viewHistory()"/>&#160;
					<%
					if (user!=null && dsTable!=null){
						
						boolean isDeleted = searchEngine.isTblDeleted(dsTable.getID());
						if (isDeleted && topFree && deletePrm){ %>
							<input type="button" class="smallbutton" value="Restore" onclick="restore()"/>&#160;<%
						}
						
						boolean inWorkByMe = workingUser==null ?
											 false :
											 workingUser.equals(user.getUserName());
						
						if (editPrm){		 
							if (dsTable.isWorkingCopy() ||
								(isLatest && topFree)   ||
								(isLatest && inWorkByMe)){ %>
								<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=tableID%>')"/>&#160;<%
							}
						}
						
						if (deletePrm){
							if (!dsTable.isWorkingCopy() && isLatest && topFree){ %>
								<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/> <%
							}
						}
					}
					else{
						%>&#160;<%
					}
					%>
				</td> <%
			}
			%>
		</tr>
		
		<%
		if (dsTable!=null && dsTable.isWorkingCopy()){ %>
			<tr><td colspan="2"><font color="red"><b>WORKING COPY!!!</b></font></td></tr><%
		}
		if (!mode.equals("view")){ %>
				
			<tr height="5"><td colspan="2"></td></tr>		
			<tr>
				<td colspan="2"><span class="Mainfont">
					NB! Edits will be lost if you leave the page without saving!
				</td>
			</tr> 
		<%
		}
		%>
		
		<tr height="5"><td colspan="2"></td></tr>
		
		<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
		
	</table>

	<%
	int displayed = 0;
	%>
	
	<table width="auto" cellspacing="0">
	
		<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<td align="right" style="padding-right:10">
				<a target="_blank" href="identification.html#short_name">
				<span class="help">?</span></a>&#160;<span class="mainfont"><b>Short name</b>
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
				<%
				if(mode.equals("view")){ %>
					<font class="title2" color="#006666"><%=Util.replaceTags(dsTable.getShortName())%></font>
					<input type="hidden" name="short_name" value="<%=dsTable.getShortName()%>"/><%
				}
				else if (mode.equals("add")){ %>
					<input class="smalltext" type="text" size="30" name="short_name"/><%
				}
				else { %>
					<input class="smalltext" type="text" size="30" name="short_name" value="<%=dsTable.getShortName()%>"/><%
				}
				%>
			</td>
		</tr>
		
		<%
		if (mode.equals("add") && Util.voidStr(request.getParameter("ds_id"))){ %>
			<tr valign="top">
				<td align="right" style="padding-right:10">
					<a target="_blank" href="identification.html#dataset"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Dataset</b>&#160;(M)</span>
				</td>
				<td>
					<select name="ds_id" class="small" <%=disabled%> >
						<option value="">-- select a dataset --</option>
						<%
						Vector datasets = searchEngine.getDatasets();
						for (int i=0; datasets!=null && i<datasets.size(); i++){
							Dataset ds = (Dataset)datasets.get(i);
							String dsn = ds.getShortName();
							if (!SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + ds.getIdentifier(), "u")) continue;
							%>
							<option value="<%=ds.getID()%>"><%=Util.replaceTags(ds.getShortName())%></option> <%
						}
						%>
					</select>
				</td>
			</tr> <%
		}
		else{
			%>
			<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
				<td align="right" style="padding-right:10">
					<a target="_blank" href="identification.html#dataset"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Dataset</b>
						<%
						if (!mode.equals("view")){
							%>
							&#160;(M)
							<%
						}
						displayed++;
						%>
					</span>
				</td>
				<td>
					<%
					if (dsID != null && dsID.length()!=0){
						%>
						<font class="title2" color="#006666">
							<a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><%=Util.replaceTags(dsName)%></a>
						</font>
						<%
					}
					%>
				</td>
			</tr> <%
		}
		
		// display Version, if not "add" mode.
		// Users cannot specify Version, it is always generated by the code.
		// First make sure you don't display Version for a status that doesn't require it.
		
		String regStatus = dsTable!=null ? dsTable.getStatus() : null;
		
		if (!mode.equals("add")){
			String tblVersion = dsTable.getVersion();

			boolean isFirst=false;
			if (mode.equals("edit") &&
				tblVersion.equals("1")){
					isFirst = verMan.isLastTbl(tableID, dsTable.getIdentifier(), dsNs);
			}
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
				<td colspan="2">
					<font class="title2" color="#006666"><%=tblVersion%></font>
				</td>
			</tr>
			
			<input type="hidden" name="is_first" value="<%=isFirst%>"><%
		}
		%>
		
		<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
			<td align="right" style="padding-right:10">
				<a target="_blank" href="identification.html"><span class="help">?</span></a>&#160;
				<span class="mainfont"><b>Identifier</b>
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
			<td>
				<% if(!mode.equals("add")){ %>
					<b><%=Util.replaceTags(dsTable.getIdentifier())%></b>
					<input type="hidden" name="idfier" value="<%=dsTable.getIdentifier()%>"/>
				<% } else{ %>
					<input <%=disabled%> type="text" class="smalltext" size="30" name="idfier"></input>
				<% } %>
			</td>
		</tr>
		
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
					<span class="barfont" style="width:400"><%=regStatus%>
						<%
						if (user!=null && topFree && editPrm){ %>
							&#160;&#160;&#160;
							<a href="javascript:forceStatus('<%=regStatus%>')">&gt; force status to lower levels...</a><%
						}
						%>
					</span>
					<%
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

			// dynamical display of attributes
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null) continue;
				
				String attrOblig = attribute.getObligation();
				
				if (!attribute.displayFor("TBL")) continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
								
				String width  = attribute.getDisplayWidth();
				String height = attribute.getDisplayHeight();
				
				boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
				boolean inherit = attribute.getInheritable().equals("0") ? false:true;

				if (mode.equals("view") && (attrValue==null || attrValue.length()==0) && !attrOblig.equals("M"))
					continue;
				
				if (dispType.equals("image") && mode.equals("add")) continue;
				
				Vector multiValues=null;
				String inheritedValue=null;

				if (!mode.equals("view")){
					
					if (inherit) inheritedValue = getValue(attrID, 2);
						
					if (mode.equals("edit")){
						if (dispMultiple){
							if (inherit){
								multiValues = getValues(attrID, 1); //original values only
							}
							else{
								multiValues = getValues(attrID, 0);  //all values
							}
						
						}
						else{
							if (inherit) attrValue = getValue(attrID, 1);  //get original value
						}
					}
				}
					
				%>
				<tr valign="top" <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a href="javascript:openUrl('delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=SIMPLE&amp;mode=view')"><span class="help">?</span></a>&#160;
						<span class="mainfont"><!--%=attrNs%>:--><b><%=attribute.getShortName()%></b>
							<%
							//displayed++;
							if (!mode.equals("view")){
								%>
								&#160;(<%=attribute.getObligation()%>)
								<%
							}
							displayed++;
							%>
						</span>
					</td>
					<td>
						<%
						
						if (attribute.getShortName().equalsIgnoreCase("Identifier")){					
							%>
							<input type="hidden" name="IdentifierInputName" value="attr_<%=attrID%>" onchange="form_changed('form1')"/>
							<%
						}
						
						else if (dispType.equals("image")){%>
							<span class="barfont" style="width:400">
								<a target="_blank" href="imgattr.jsp?obj_id=<%=tableID%>&amp;obj_type=T&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=dsTable.getShortName()%>&amp;attr_name=<%=attribute.getShortName()%>">image(s)</a>
							</span><%
						}
						// if mode is 'view', display a span, otherwise an input						
						else if (mode.equals("view")){%>
							<span class="barfont" style="width:400"><%=Util.replaceTags(attrValue)%></span><%
						}
						else{ // start display input
							/*if (mode.equals("add") && inherit){
								%>
								<input <%=disabled%> type="checkbox" name="inherit_<%=attrID%>" checked/> Inherit this attribute from dataset level<br>
								<%
							}
							else if (mode.equals("edit") && inherit){
								%>
								<input <%=disabled%> type="checkbox" name="inherit_<%=attrID%>"/> Copy this attribute value to data elment level<br>
								<%
							}
							*/
							if (inherit && inheritedValue!=null){
								String sInhText = (((dispMultiple && multiValues!=null) || (!dispMultiple && attrValue!=null)) && attribute.getInheritable().equals("2")) ? "Overriding parent level value: ":"Inherited from parent level: ";
								if (sInhText.startsWith("Inherited")){
									%>
									<span class="barfont" style="width:400"><%=sInhText%><%=inheritedValue%></span><br>
									<%
								}
							}
							
							
							if (dispMultiple && !dispType.equals("image")){
								
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
									<a href="javascript:rmvValue('<%=attrID%>')"><img src="images/button_remove.gif" border="0" title="Click here to remove selected value"/></a>
									<a href="javascript:openAddBox('<%=attrID%>', 'dispType=<%=dispType%>&amp;width=<%=width%>')"><img src="images/button_plus.gif" border="0" title="Click here to add a new value"/></a>
								
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
								else if (dispType.equals("text")){ 
									
									%>
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
									</select>
									 <%
								}
							}
							else{
								if (dispType.equals("text")){
									if (attrValue!=null){
										%>
										<input <%=disabled%> type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>" value="<%=attrValue%>" onchange="form_changed('form1')"/>
										<%
									}
									else{
										%>
										<input <%=disabled%> type="text" class="smalltext" size="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"/>
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
						}
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
				<tr height="5"><td></td><td></td></tr>
				<%
				for (int i=0; i<complexAttrs.size(); i++){
	
					DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
					attrID = attr.getID();
					String attrName = attr.getShortName();   
					Vector attrFields = searchEngine.getAttrFields(attrID, DElemAttribute.FIELD_PRIORITY_HIGH);
		
					%>		
					<tr valign="top" <% if (displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<td align="right" style="padding-right:10">
							<a href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=COMPLEX&amp;mode=view">
							<span class="help">?</span></a>&#160;
							<span class="mainfont"><b>
								<a href="javascript:complexAttr('complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=dsTable.getShortName()%>&amp;dataset_id=<%=dsID%>');" title="Click here to view all the fields">
									<%=attrName%>
								</a></b>
							</span>
						</td>
						<td>
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
										<span class="barfont"><%=mark%>&#160;<%=Util.replaceTags(fieldValue)%></span><br>
										<!--td style="padding-right:10" <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<span class="barfont"><%=Util.replaceTags(fieldValue)%>
										</td-->
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
			/*
			This was needed, when inherited attributes were saved on all levels
			
			if (mode.equals("add")){
				complexAttrs = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_COMPLEX, null, "1");
				if (complexAttrs.size()>0){
					%>
					<tr height="5"><td></td><td></td></tr>
					<tr valign="top">
						<td align="left" style="padding-right:10" colspan="2">
							<span class="mainfont">Inherit the following complex attributes from dataset level:</span>
						</td>
					</tr>
					<%

					for (int i=0; i<complexAttrs.size(); i++){
	
						DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
						attrID = attr.getID();
						String attrName = attr.getShortName();   
						%>
						<tr valign="top">
							<td align="right" style="padding-right:10">
								<a href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=COMPLEX&amp;mode=view">
								<span class="help">?</span></a>&#160;
								<span class="mainfont"><b><%=attrName%></b></span>
							</td>
							<td>
								<input <%=disabled%> type="checkbox" name="inherit_complex_<%=attrID%>" checked/><br>
							</td>
						</tr>
						<%
					}

				}
			}
			*/
			%>
			<tr><td>&#160;</td><td><br><span class="barfont" style="width:400">! General information can be found from <a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view">dataset</a> level</span></td></tr>
			<%
			if (!mode.equals("add") && !mode.equals("view")
					|| (mode.equals("view") && user!=null)){ // if mode is not 'add'
			%>
			
				<tr height="5"><td colspan="2"></td></tr>
				<tr>
					<td>&#160;</td>
					<td>
						<b>*</b> <span class="smallfont"><a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=dsTable.getShortName()%>&amp;dataset_id=<%=dsID%>')">
							<b>COMPLEX ATTRIBUTES</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to view/edit complex attributes specified for this table
						</span>
					</td>
				</tr>
			<%
			}
			
			if (mode.equals("view") && elems!=null && elems.size()>0){ %>
				<tr height="10"><td colspan="2"></td></tr>
				<tr height="5"><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr><%
			}
			else if (mode.equals("edit")){ %>
				<tr>
					<td>&#160;</td>
					<td>
						<b>*</b> <span class="smallfont">
							<% 
							String elemLink; 
							if (contextParam.equals("ds"))
								elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=" + contextParam + "&ds_idf=" + dsIdf;
							else
								elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=dstbl" + "&ds_idf=" + dsIdf;
							%>
							<a href="javascript:openUrl('<%=elemLink%>')">
							<b>ELEMENTS</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to specify/remove elements of this table
						</span>
					</td>
				</tr>
				
				<%
			}
			
			if (!mode.equals("view")){ %>
				<tr height="10"><td colspan="2"></td></tr><%
			}
			
			if (mode.equals("edit") && dsTable!=null && dsTable.isWorkingCopy() && user!=null && hasHistory){%>
				<tr height="15">
					<td>&#160;</td>
					<td colspan="2" style="padding-left:47">
						<input type="checkbox" name="upd_version" value="true">Update version when checking in</input>
					</td>
				</tr><%
			}
			
			if (!mode.equals("view")){ %>
				<tr>
					<td>&#160;</td>
					<td>
						<% 
						if (mode.equals("add")){ // if mode is "add"
							if (user==null){ %>									
								<input type="button" class="mediumbuttonb" value="Add" disabled="true"/>&#160;&#160;
							<%} else {%>
								<input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>&#160;&#160;
								<input type="button" class="mediumbuttonb" value="Copy" onclick="copyTbl()" title="Copies table structure and attributes from existing dataset table"/>&#160;&#160;
							<% }
						} // end if mode is "add"
						
						if (!mode.equals("add")){ // if mode is not "add"
							if (user==null){ %>									
								<input type="button" class="mediumbuttonb" value="Save" disabled="true"/>&#160;&#160;
								<%
								if (!dsTable.isWorkingCopy()){ %>
									<input class="mediumbuttonb" type="button" value="Delete" disabled="true"/>&#160;&#160;<%
								}
								else{ %>
									<input class="mediumbuttonb" type="button" value="Check in" onclick="checkIn()" disabled="true"/>&#160;&#160;
									<input class="mediumbuttonb" type="button" value="Undo check-out" disabled="true"/>&#160;&#160;<%
								}
							} else {%>
								<input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
								<%
								if (!dsTable.isWorkingCopy()){ %>
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
			%>
			
		</table>

		<!-- elements list goes here, if user is in view mode  -->
				
		<%

		boolean hasGIS = false;	
		boolean hasNormal = false;
		for (int i=0; elems!=null && i<elems.size(); i++){
			DataElement elm = (DataElement)elems.get(i);
			if (elm.getGIS()==null)
				hasNormal = true;
			else
				hasGIS = true;
		}
		
		if (mode.equals("view")){
			
			boolean hasRedWildcard = false;
			%>
			<table width="auto" cellspacing="0">
			
			<tr valign="top">
			
				<%
				if (hasNormal){ %>
					<td align="left" style="padding-right:10">
					
						<%
						if (elems != null && elems.size()>0){
							
							String tbltitle = hasGIS ? "Metadata elements" : "Elements";
							
							%>						
							<table width="auto" cellspacing="0">
								<tr>
									<td align="right" style="padding-right:10"></td>
									<td colspan="4">
										<span class="mainfont"><b><%=tbltitle%></b></span><br/>
									</td>
								</tr>
								<tr>
									<td align="right" style="padding-right:10"></td>
									<th align="left" style="padding-left:5;padding-right:10">Short name</th>
									<th align="left" style="padding-right:10">Datatype</th>
									<th align="left" style="padding-right:10">Elem type</th>
									<th align="left" style="padding-right:10"></th>
								</tr>
				
								<%
				
								Hashtable types = new Hashtable();
								types.put("CH1", "Fixed values");
								types.put("CH2", "Quantitative");
								
								int count=-1;
								for (int i=0; elems!=null && i<elems.size(); i++){
						
									DataElement elem = (DataElement)elems.get(i);
									
									if (elem.getGIS() != null){
										hasGIS = true;
										continue;
									}
									
									String elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID() + "&ds_id=" + dsID + "&table_id=" + tableID + "&ctx=" + contextParam;
						
									String elemType = (String)types.get(elem.getType());
					
									String datatype = getAttributeValue(elem, "Datatype");		
									if (datatype == null) datatype="";
				
									String max_size = getAttributeValue(elem, "MaxSize");		
									if (max_size == null) max_size="";
									
									// see if the element is part of any foreign key relations
									Vector _fks = searchEngine.getFKRelationsElm(elem.getID(), dataset.getID());
									boolean fks = (_fks!=null && _fks.size()>0) ? true : false;
									
									String elemDefinition = elem.getAttributeValueByShortName("Definition");
									
									String elemWorkingUser = verMan.getWorkingUser(elem.getNamespace().getID(),
				    															   elem.getIdentifier(), "elm");
				    															   
				    				count++;
				    				
									%>
									<tr <% if (count % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
										<td align="right" style="padding-right:5" bgcolor="#f0f0f0">
											<%
											if (user!=null && elemWorkingUser!=null){ // mark checked-out elements
												hasRedWildcard = true;
												%> <font title="<%=elemWorkingUser%>" color="red">* </font> <%
											}
											%>
										</td>
										<td align="left" style="padding-left:5;padding-right:10">
										
											<%
											if (elemDefinition!=null){ %>
												<a title="<%=elemDefinition%>" href="<%=elemLink%>"><%=Util.replaceTags(elem.getShortName())%></a><%
											} else { %>
												<a href="<%=elemLink%>"><%=Util.replaceTags(elem.getShortName())%></a><%
											} %>
										</td>
										<td align="left" style="padding-right:10">
											<span class="barfont"><%=datatype%></span>
										</td>
										<td align="left" style="padding-right:10">
											<% if (elem.getType().equals("CH1")){ %>
												<span class="barfont"><a href="fixed_values.jsp?mode=view&amp;delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>"><%=elemType%></a></span>
											<%} else{ %>
												<span class="barfont"><%=elemType%></span>
											<% } %>
										</td>
										<td align="left" style="padding-right:10">
											<%
											if (fks){ %>
												<a href="foreign_keys.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>&amp;ds_id=<%=dsID%>">(FK)</a><%
											}
											%>
										</td>
									</tr>
								<%
								}
								%>
								</table> <%
						}
						else { %>
							<span class="barfont" style="width:400">There is no elements in the table</span> <%
						}
						%>				
					</td><%
				} // end of normal elements table
				
				if (hasGIS){ %>
				
					<td>
						<table width="auto" cellspacing="0">
							<tr>
								<td align="right" style="padding-right:10"></td>
								<td colspan="5">
									<span class="mainfont"><b>Elements</b></span><br/>									
								</td>
							</tr>
							<tr>
								<td align="right" style="padding-right:10"></td>
								<th align="left" style="padding-left:5;padding-right:10">Short name</th>
								<th align="left" style="padding-right:10">Type</th>
								<th align="left" style="padding-right:10">Datatype</th>
								<th align="left" style="padding-right:10">Elem type</th>
								<th align="left" style="padding-right:10"></th>
							</tr>
			
							<%
			
							Hashtable types = new Hashtable();
							types.put("CH1", "Fixed values");
							types.put("CH2", "Quantitative");
							
							int count=-1;					
							for (int i=0; elems!=null && i<elems.size(); i++){
					
								DataElement elem = (DataElement)elems.get(i);
								
								String gisType = elem.getGIS();
								if (gisType == null)
									continue;
								
								String elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID() + "&ds_id=" + dsID + "&table_id=" + tableID + "&ctx=" + contextParam;
					
								String elemType = (String)types.get(elem.getType());
				
								String datatype = getAttributeValue(elem, "Datatype");		
								if (datatype == null) datatype="";
			
								String max_size = getAttributeValue(elem, "MaxSize");		
								if (max_size == null) max_size="";
								
								// see if the element is part of any foreign key relations
								Vector _fks = searchEngine.getFKRelationsElm(elem.getID(), dataset.getID());
								boolean fks = (_fks!=null && _fks.size()>0) ? true : false;
								
								String elemDefinition = elem.getAttributeValueByShortName("Definition");
								
								String elemWorkingUser = verMan.getWorkingUser(elem.getNamespace().getID(),
			    															   elem.getIdentifier(), "elm");
								
								count++;
								
								%>
								<tr <% if (count % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
									<td align="right" style="padding-right:5" bgcolor="#f0f0f0">
										<%
										if (user!=null && elemWorkingUser!=null){ // mark checked-out elements
											hasRedWildcard = true;
											%> <font title="<%=elemWorkingUser%>" color="red">* </font> <%
										}
										%>
									</td>
									<td align="left" style="padding-left:5;padding-right:10">
									
										<%
										if (elemDefinition!=null){ %>
											<a title="<%=elemDefinition%>" href="<%=elemLink%>"><%=Util.replaceTags(elem.getShortName())%></a><%
										} else { %>
											<a href="<%=elemLink%>"><%=Util.replaceTags(elem.getShortName())%></a><%
										} %>
									</td>
									<td align="left" style="padding-right:10">
										<span class="barfont"><%=gisType%></span>
									</td>
									<td align="left" style="padding-right:10">
										<span class="barfont"><%=datatype%></span>
									</td>
									<td align="left" style="padding-right:10">
										<% if (elem.getType().equals("CH1")){ %>
											<span class="barfont"><a href="fixed_values.jsp?mode=view&amp;delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>"><%=elemType%></a></span>
										<%} else{ %>
											<span class="barfont"><%=elemType%></span>
										<% } %>
									</td>
									<td align="left" style="padding-right:10">
										<%
										if (fks){ %>
											<a href="foreign_keys.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>&amp;ds_id=<%=dsID%>">(FK)</a><%
										}
										%>
									</td>
								</tr>
							<%
							}
							%>
							</table>
					</td><%
				}
				%>
			</tr>
			
			<%
			if (user!=null && hasRedWildcard){
				String align = hasGIS ? "center" : "left";
				%>
				<tr>
					<td colspan="2" style="padding-left:15" align="<%=align%>">
						<span class="smallfont" style="font-weight: normal">
							(a red wildcard stands for checked-out element)
						</span>
					</td>
				</tr><%
			}
			
			if (user!=null){ %>
				
				<tr height="10"><td colspan="2"></td></tr>
				<tr>
					<td colspan="2" style="padding-left:10">
						<b>*</b> <span class="smallfont">
							<% 
							String elemLink; 
							if (contextParam.equals("ds"))
								elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=" + contextParam + "&ds_idf=" + dsIdf;
							else
								elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=dstbl" + "&ds_idf=" + dsIdf;
							%>
							<a href="javascript:openUrl('<%=elemLink%>')">
							<b>ELEMENTS</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to specify/remove elements of this table
						</span>
					</td>
				</tr> <%
			}
			%>	
			
			</table> <%
		}
		%>
		
		<!-- end of elements list -->

		<table width="auto" cellspacing="0">
			<%
			if (mode.equals("view")){
				%>
				
				<tr height="15"><td colspan="2"></td></tr>
				<tr height="20" valign="top">
					<td align="right" style="padding-right:10;padding-left:10">
						<span class="mainfont"><b>Templates</b></span>
					</td>
					<td>
						* <a target="_blank" href="GetSchema?comp_id=<%=tableID%>&amp;comp_type=TBL">Create an XML Schema</a>
					</td>
				</tr>
			
				<%
			}
		
			%>

		</table>

<input type="hidden" name="mode" value="<%=mode%>"/>

<input type="hidden" name="check_in" value="false"/>

<% if (dsID!=null && dsID.length()>0){ %>
	<input type="hidden" name="ds_id" value="<%=dsID%>"/> <%
} %>

<% if (dsName!=null && dsName.length()>0){ %>
	<input type="hidden" name="ds_name" value="<%=dsName%>"/> <%
}
else{ %>
	<input type="hidden" name="ds_name"/><%
}%>
	
<%
if (!mode.equals("add")){ %>
	<input type="hidden" name="table_id" value="<%=tableID%>"/>
	<input type="hidden" name="del_id" value="<%=tableID%>"/>
	 <%
}
%>

<input type="hidden" name="ctx" value="<%=contextParam%>"/>
<input type="hidden" name="copy_tbl_id" value=""/>
<input type="hidden" name="changed" value="0">
<input type="hidden" name="ver_upw" value="true">
<!-- input type="hidden" name="upd_version" value="false" -->

<%
if (latestID!=null){%>
	<input type="hidden" name="latest_id" value="<%=latestID%>"><%
}
%>

<input type="hidden" name="force_status" value=""/>
	
</form>
</div>
</TD></TR></table>
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
