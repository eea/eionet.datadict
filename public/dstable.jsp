<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,eionet.util.QueryString"%>

<%@ include file="history.jsp" %>

<%!
private String getValue(String id, String mode, Vector attributes){
	return getValue(id, 0, mode, attributes);
}
/*
		int val indicates which type of value is requested. the default is 0
		0 - display value (if original value is null, then show inherited value)
		1 - original value
		2 - inherited value
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
private Vector getValues(String id, String mode, Vector attributes){
	return getValues(id, 0, mode, attributes);
}

/*
		int val indicates which group of values is requested. the default is 0
		0 - all
		1 - original
		2 - inherited
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

private String getAttributeIdByName(String name, Vector mAttributes){
	

		for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}
private String getAttributeValue(DataElement elem, String name, Vector mAttributes){
	
	String id = getAttributeIdByName(name, mAttributes);
	if (elem == null) return null;
	DElemAttribute attr = elem.getAttributeById(id);
	if (attr == null) return null;
	return attr.getValue();
}

%>

<%

//
ServletContext ctx=null;
Vector mAttributes=null;
Vector attributes=null;
Vector complexAttrs=null;
Vector elems=null;
String mode=null;
//

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
	<link href="eionet_new.css" rel="stylesheet" type="text/css"/>	
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
					wComplexAttrs = window.open(url,"ComplexAttributes","height=600,width=600,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
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
            </center></p>
        </td>
        <td>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Dataset table"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
	
			<form name="form1" method="POST" action="dstable.jsp">
			
				<!--------------------------->
				<!-- main table inside div -->
				<!--------------------------->
				
				<table border="0" width="620" cellspacing="0" cellpadding="0">
				
					<!-- main table head -->
					
					<%
					boolean topFree = false;
					
					String verb = "View";
					if (mode.equals("add"))
						verb = "Add";
					else if (mode.equals("edit"))
						verb = "Edit";
					%>
					
					<tr>
						<td colspan="2" align="right">
							<%
							String hlpScreen = "table";
							if (mode.equals("edit"))
								hlpScreen = "table_edit";
							else if (mode.equals("add"))
								hlpScreen = "table_add";
							%>
							<a target="_blank" href="help.jsp?screen=<%=hlpScreen%>&area=pagehelp"><img src="images/pagehelp.jpg" border=0 alt="Get some help on this page" /></a>
						</td>
					</tr>
					<tr>
						<td width="72%" height="40" class="head1">
							<%=verb%> table definition
							<%
							if (mode.equals("add") && dsID != null && dsID.length()!=0){ %>
								to <a target="_blank" href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><%=Util.replaceTags(dsName)%></a> dataset<%
							}
							%>
						</td>
						<td width="28%" height="40" align="right">
							<%
							if (mode.equals("view") && dsTable!=null){
								if (user!=null){
									// set the flag indicating if the top namespace is in use
									String topWorkingUser = verMan.getWorkingUser(dsTable.getParentNs());
									topFree = topWorkingUser==null ? true : false;
										
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
											(isLatest && inWorkByMe)){
												
											%>
											<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=tableID%>')"/>&#160;<%
										}
									}
									
									if (deletePrm){
										if (!dsTable.isWorkingCopy() && isLatest && topFree){ %>
											<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/> <%
										}
									}
								}
								
								if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u")){ %>
									<input type="button" class="smallbutton" value="History" onclick="viewHistory()"/> <%
								}
							}
							// the working copy part
							else if (dsTable!=null && dsTable.isWorkingCopy()){ %>
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
									if (user==null){ %>
										<input type="button" class="mediumbuttonb" value="Add" disabled="true"/><%
									}
									else { %>
										<input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>&nbsp;
										<input type="button" class="mediumbuttonb" value="Copy" onclick="copyTbl()" title="Copies table structure and attributes from existing dataset table"/><%
									}
								}
								// edit case
								else if (mode.equals("edit")){
									String isDisabled = user==null ? "disabled" : "";
									%>
									<input type="button" class="mediumbuttonb" value="Save" <%=isDisabled%> onclick="submitForm('edit')"/>&nbsp;
									<%
									if (!dsTable.isWorkingCopy()){ %>
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
						if (mode.equals("edit") && dsTable!=null && dsTable.isWorkingCopy() && user!=null && hasHistory){%>
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
			                    	
			                    	if (elems!=null && elems.size()>0)
			                    		quicklinks.add("Elements | elements");
			                    	if (complexAttrs!=null && complexAttrs.size()>0)
			                    		quicklinks.add("Complex attributes | cattrs");
			                    	
			                    	request.setAttribute("quicklinks", quicklinks);
			                    	%>
		                    		<jsp:include page="quicklinks.jsp" flush="true">
		                    		</jsp:include>
						            <%
								}
								%>
								
								<!-- schema & MS Excel template-->
								
								<%
								if (mode.equals("view")){ %>
									<tr><td width="100%" height="10"></td></tr>
									<tr>
										<td width="100%" style="border: 1 solid #FF9900">
											<table border="0" width="100%" cellspacing="0">											
												<tr>
													<td width="73%" valign="middle" align="left">
														Create an XML Schema for this table
													</td>
													<td width="27%" valign="middle" align="left">
														<a target="_blank" href="GetSchema?id=TBL<%=tableID%>">
															<img border="0" src="images/icon_xml.jpg" width="16" height="18"/>
														</a>
													</td>
												</tr>
												
												<%
												if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xmli")){ %>
													<tr>
														<td width="73%" valign="middle" align="left">
															Create an instance XML for this table
														</td>
														<td width="27%" valign="middle" align="left">
															<a target="_blank" href="GetXmlInstance?id=<%=tableID%>">
																<img border="0" src="images/icon_xml.jpg" width="16" height="18"/>
															</a>
														</td>
													</tr><%
												}
												
												if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xfrm")){ %>
													<tr>
														<td width="73%" valign="middle" align="left">
															Create an XForm for this table
														</td>
														<td width="27%" valign="middle" align="left">
															<a target="_blank" href="GetXForm?id=<%=tableID%>">
																<img border="0" src="images/icon_xml.jpg" width="16" height="18"/>
															</a>
														</td>
													</tr><%
												}
												%>
												
												<tr>
													<td width="73%" valign="middle" align="left">
														Create an MS Excel template for this table&nbsp;<a target="_blank" href="help.jsp?screen=table&area=excel"><img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/></a>
													</td>
													<td width="27%" valign="middle" align="left">
														<a href="GetXls?obj_type=tbl&obj_id=<%=tableID%>"><img border="0" src="images/icon_xls.gif" width="16" height="18"/></a>
													</td>
												</tr>
												
												<%
												if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u")){
													%>
													<tr height="20">
														<td colspan="2" valign="bottom" align="left">
															<span class="barfont">
																[ <a target="_blank" href="GetCache?obj_id=<%=tableID%>&obj_type=tbl&idf=<%=dsTable.getIdentifier()%>">Open cache ...</a> ]
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
														<%=Util.replaceTags(dsTable.getShortName())%>
														<input type="hidden" name="short_name" value="<%=dsTable.getShortName()%>"/><%
													}
													else if (mode.equals("add")){%>
														<input class="smalltext" type="text" size="30" name="short_name"/><%
													}
													else{ %>
														<input class="smalltext" type="text" size="30" name="short_name" value="<%=dsTable.getShortName()%>"/><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- dataset -->
								    		<tr>
								    			<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
													Dataset
												</td>
												<td width="4%" class="simple_attr_help<%=isOdd%>">
													<a target="_blank" href="identification.html#dataset">
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
													// add case
													if (mode.equals("add") && Util.voidStr(request.getParameter("ds_id"))){ %>
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
														</select><%
													}
													// other cases
													else if (dsID != null && dsID.length()!=0){%>
														<a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view">
															<b><%=Util.replaceTags(dsName)%></b>
														</a><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- RegistrationStatus -->
								    		<%
								    		String regStatus = dsTable!=null ? dsTable.getStatus() : null;
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
															if (user!=null && topFree && editPrm){ %>
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
													
													<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>"><%
													
														// handle image attribute first
														if (dispType.equals("image")){
															if (!imagesQuicklinkSet){ %>
																<a name="images"></a><%
																imagesQuicklinkSet = true;
															}
															// thumbnail
															if (mode.equals("view") && !Util.voidStr(attrValue)){ %>
																<a target="_blank" href="visuals/<%=attrValue%>" onFocus="blur()">
																	<img src="visuals/<%=attrValue%>" border="0" height="100px" width="100px"/>
																</a><br/><%
															}
															// link
															if (mode.equals("edit") && user!=null){
																String actionText = Util.voidStr(attrValue) ? "add image" : "manage this image";
																%>
																<span class="barfont">
																	[Click <a target="_blank" href="imgattr.jsp?obj_id=<%=tableID%>&amp;obj_type=T&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=dsTable.getShortName()%>&amp;attr_name=<%=attribute.getShortName()%>"><b>HERE</b></a> to <%=actionText%>]
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
																	<%=sInhText%><%=inheritedValue%><br/><%
																}
															}
															
															// mutliple display
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
															// no multiple display
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
																	</select>
																	<a target="_blank" href="fixed_values.jsp?mode=view&amp;delem_id=<%=attrID%>&amp;delem_name=<%=attribute.getShortName()%>&amp;parent_type=attr">
																		<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
																	</a>
																	<%
																}
																else{ %>
																	Unknown display type!<%
																}
															}
															
														} // end display input
														%>
													</td>
													<!-- end dynamic attribute value display -->
													
													<%isOdd = Util.isOdd(++displayed);%>
											    </tr>
											    <input type="hidden" name="oblig_<%=attrID%>" value="<%=attribute.getObligation()%>"/>											    
											    <%
										    }
										    %>
										    
										    <!-- version (or the so-called LastCheckInNo) -->
								    		<%
								    		if (!mode.equals("add")){
												String tblVersion = dsTable.getVersion();
												boolean isFirst=false;
												if (mode.equals("edit") &&
													tblVersion.equals("1")){
														isFirst = verMan.isLastTbl(tableID, dsTable.getIdentifier(), dsNs);
												}
												%>												
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
														<%=tblVersion%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr>
									    		<input type="hidden" name="is_first" value="<%=isFirst%>">
									    		<%
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
														<b><%=Util.replaceTags(dsTable.getIdentifier())%></b>
														<input type="hidden" name="idfier" value="<%=dsTable.getIdentifier()%>"/><%
													}
													else{ %>
														<input <%=disabled%> type="text" class="smalltext" size="30" name="idfier"/><%
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
										if ((!mode.equals("add") && user!=null) || (mode.equals("view") && elems!=null && elems.size()>0)){
											
											// horizontal separator 1
											if (!separ1displayed){ %>
												<%@ include file="hor_separator.jsp" %><%
												separ1displayed = true;
											}
											
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
												String title = "Elements";
												if (mode.equals("view") && curMode.equals("NOGIS") && hasGIS)
													title = "Metadata elements";
												
												boolean hasMarkedElems = false;
												boolean hasForeignKeys = false;
												%>
												<table border="0" width="100%" cellspacing="0" cellpadding="3">
													
													<% if (mode.equals("view")){ %>
														<tr height="10"><td colspan="<%=String.valueOf(colspan)%>"></td></tr><%
													} %>
													
													<tr>
														<td width="34%">
															<b><%=title%><a name="elements"></a></b>
														</td>
														
														<%
														// elements link
														if (user!=null){
															String elemLink; 
															if (contextParam.equals("ds"))
																elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=" + contextParam + "&ds_idf=" + dsIdf;
															else
																elemLink="tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ctx=dstbl" + "&ds_idf=" + dsIdf;
															%>
															<td class="barfont" width="66%">
																[Click <a href="javascript:openUrl('<%=elemLink%>')"><b>HERE</b></a> to manage all elements of this table]
															</td><%
														}
														%>
													</tr>
													
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
														<tr>
											      			<td width="100%" colspan="<%=String.valueOf(colspan)%>">
											      				<table border="1" width="100%" bordercolorlight="#C0C0C0" cellspacing="0" cellpadding="2" bordercolordark="#C0C0C0">
																	<tr>
																		<th width="<%=widthShortName%>" class="tbl_elms">Short name</th>
																		<%
																		if (curMode.equals("GIS")){ %>
																			<th width="<%=widthType%>" class="tbl_elms">GIS type</th><%
																		}
																		%>
																		<th width="<%=widthDatatype%>" class="tbl_elms">Datatype</th>
																		<th width="<%=widthElemtype%>" class="tbl_elms">Element type</th>
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
																		
																		String elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID() + "&ds_id=" + dsID + "&table_id=" + tableID + "&ctx=" + contextParam;
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
																		String elemWorkingUser = verMan.getWorkingUser(elem.getNamespace().getID(),
													    										  		elem.getIdentifier(), "elm");
																		%>
																		<tr>
																			<!-- short name -->
																			<td width="<%=widthShortName%>" class="tbl_elms">
																				<%
																				// red wildcard
																				if (user!=null && elemWorkingUser!=null){ // mark checked-out elements
																					hasMarkedElems = true;
																					%> <font title="<%=elemWorkingUser%>" color="red">* </font> <%
																				}
																				// short name and link
																				%>
																				<a href="<%=elemLink%>" title="<%=linkTitle%>">
																					<%=Util.replaceTags(elem.getShortName())%>
																				</a>
																				<%
																				// FK inidcator
																				if (fks){ %>
																					&nbsp;
																					<a href="foreign_keys.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>&amp;ds_id=<%=dsID%>">
																						<b><i>(FK)</i></b>
																					</a><%
																					hasForeignKeys = true;
																				}
																				%>
																			</td>
																			<!-- gis type -->
																			<%
																			if (curMode.equals("GIS")){
																				gisType = (gisType==null || gisType.length()==0) ? "&nbsp" : gisType;
																				%>
																				<td width="<%=widthType%>" class="tbl_elms">
																					<%=gisType%>
																				</td><%
																			}
																			%>
																			<!-- datatype -->
																			<td width="<%=widthDatatype%>" class="tbl_elms">
																				<%=datatype%>
																			</td>
																			<!-- element type -->
																			<td width="<%=widthElemtype%>" class="tbl_elms">
																				<%
																				if (elem.getType().equals("CH1")){ %>
																					<a href="fixed_values.jsp?mode=view&amp;delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>">
																						<%=elemType%>
																					</a> <%
																				}
																				else{ %>
																					<%=elemType%><%
																				}
																				%>
																			</td>
																		</tr><%
																	}
																	%>
																</table>
											      			</td>
											      		</tr>
											      		<%
											      		if (user!=null && elems!=null && elems.size()>0 && hasMarkedElems){%>
															<tr height="10">
																<td width="100%" class="barfont" colspan="<%=String.valueOf(colspan)%>">
																	(a red wildcard stands for checked-out element)
																</td>
															</tr><%
														}
														if (user!=null && elems!=null && elems.size()>0 && hasForeignKeys){%>
															<tr height="10">
																<td width="100%" class="barfont" colspan="<%=String.valueOf(colspan)%>">
																	(the <u><b><i>(FK)</i></b></u> sign indicates the element participating in a foreign key relation)
																</td>
															</tr><%
														}
													}
													else if (mode.equals("edit")){
														// in edit case we display only the link anyway and we don't
														// wont to display it twice (once for GIS elems and once for simply elems)
														// So we break the loop here.
														break;
													}
													%>													
												</table><%
											}
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
															[Click <a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=dsTable.getShortName()%>&amp;dataset_id=<%=dsID%>')"><b>HERE</b></a> to manage complex attributes of this table]
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
																			<a href="javascript:complexAttr('complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=dsTable.getShortName()%>&amp;dataset_id=<%=dsID%>')" title="Click here to view all the fields">
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
											<%
										}
										%>
										<!-- end complex attributes -->
								        
									</td>
								</tr>
								
								<!-- end dotted -->
								
							</table>
						</td>
					</tr>
					
					<!-- end main table body -->
					
				</table>
				
				<!-- end main table -->
				
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
				
				<%
				if (latestID!=null){%>
					<input type="hidden" name="latest_id" value="<%=latestID%>"><%
				}
				%>
				
				<input type="hidden" name="force_status" value=""/>
				
			</form>
			
			<jsp:include page="footer.jsp" flush="true">
			</jsp:include>
			
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
