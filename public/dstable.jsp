<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

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
String disabled = user == null ? "disabled" : "";

if (request.getMethod().equals("POST")){
	if (user == null){ %>
		<b>Not authorized to post any data!</b> <%
		return;
	}
}

// make sure we have the mode parameter
mode = request.getParameter("mode");
if (mode == null || mode.length()==0) { %>
	<b>Mode paramater is missing!</b>
	<%
	return;
}

String tableIdf = request.getParameter("table_idf");
String pns = request.getParameter("pns");
String tableID = request.getParameter("table_id");

if (mode.equals("view")){
	if (Util.voidStr(tableID) && (Util.voidStr(tableIdf) || Util.voidStr(pns))){ %>
		<b>Missing ID or Identifier and parent namespace!</b> <%
		return;
	}
}
else if (mode.equals("edit") || mode.equals("copy")){
	if (Util.voidStr(tableID)){ %>
		<b>Missing ID!</b> <%
		return;
	}
}
boolean latestRequested = mode.equals("view") && !Util.voidStr(tableIdf) && !Util.voidStr(pns);

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
if (!Util.voidStr(tableID) || !Util.voidStr(tableIdf)){
	
	if (latestRequested){
		dsTable = searchEngine.getLatestTbl(tableIdf, pns);
		if (dsTable!=null) tableID = dsTable.getID();
	}
	else
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
				redirUrl = redirUrl + "dstable.jsp?mode=edit&table_id=" + id;
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
		else if (mode.equals("copy")){
			String id = handler.getLastInsertID();
			if (id != null && id.length()!=0)
				redirUrl = redirUrl + "dstable.jsp?mode=edit&table_id=" + id;
			if (history!=null){
			int idx = history.getCurrentIndex();
				if (idx>0)
					history.remove(idx);
			}
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
boolean isLatestDst = dataset==null ? false : verMan.isLatestDst(dataset.getID(), dataset.getIdentifier());

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
	    if (!isLatestDst){ %>
	    	<b>Trying to check out in a dataset that is not the latest!</b><%
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


// initialize some stuff
DElemAttribute attribute = null;
String attrID = null;
String attrValue = null;

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
			
			if (mode=="add"){
				var o = document.forms["form1"].ds_id;
				if (o!=null && o.value.length == 0){
					alert('Dataset not specified!');
					return;
				}
			}
			
			if (mode == "delete"){
				<%
				if (!mode.equals("add") && dsTable.isWorkingCopy()){ %>
					var b = confirm("This working copy will be deleted and the whole dataset released for others to edit! Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				else{ %>
					var b = confirm("This table will be deleted! You will be asked if you want this to update the dataset's CheckInNo as well. Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				%>
				if (b==false) return;
				
				<%
				if (dsTable!=null && dsTable.isWorkingCopy()){ %>
					document.forms["form1"].elements["upd_version"].value = "false";
					deleteReady();
					return;<%
				}
				else{ %>
					// now ask if the deletion should also result in the dataset's new version being created				
					openNoYes("yesno_dialog.html", "Do you want to update the dataset definition's CheckInNo with this deletion?", delDialogReturn,100, 400);
					return;<%
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
					alert("Identifier must start with a letter or underscore to be valid for usage as an XML tag!");
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
		
		function checkIn(){
			submitCheckIn();
		}
		
		function submitCheckIn(){
			document.forms["form1"].elements["check_in"].value = "true";
			document.forms["form1"].elements["mode"].value = "edit";
			document.forms["form1"].submit();
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
			
			if (str!=null && str.length>0){
				var ch = str.charCodeAt(0);
				if (ch==95 || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))
					return true;
			}
			
			return false;
		}
</script>

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
							<a target="_blank" href="help.jsp?screen=<%=hlpScreen%>&area=pagehelp" onclick="pop(this.href)">
								<img src="images/pagehelp.jpg" border=0 alt="Get some help on this page" />
							</a>
						</td>
					</tr>
					<tr>
						<td width="72%" height="40" class="head1">
							<%=verb%> table definition
							<%
							if (mode.equals("add") && dsID != null && dsID.length()!=0){ %>
								to <a target="_blank" onclick="pop(this.href)" href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view"><%=Util.replaceTags(dsName)%></a> dataset<%
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
									boolean inWorkByMe = workingUser==null ? false : workingUser.equals(user.getUserName());
									
									if (editPrm){		 
										if (dsTable.isWorkingCopy() ||
											(isLatestDst && topFree)   ||
											(isLatestDst && inWorkByMe)){
												
											%>
											<input type="button" class="smallbutton" value="Edit" onclick="goTo('edit', '<%=tableID%>')"/><%
										}
									}
									
									if (deletePrm){
										if (!dsTable.isWorkingCopy() && isLatestDst && topFree){ %>
											<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/><%
										}
									}
								}
							}
							// the working copy part
							else if (dsTable!=null && dsTable.isWorkingCopy()){ %>
								<span class="wrkcopy">Working copy</span><%
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
						if (mode.equals("edit") && dsTable!=null && dsTable.isWorkingCopy() && user!=null){%>
							<tr>
								<td align="right" class="smallfont_light" colspan="2">
									<input type="checkbox" name="upd_version" value="true">&nbsp;Update the dataset definition's CheckInNo when checking in</input>
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
												<%
												// display schema link only for users that have a right to edit a dataset
												if (user!=null && SecurityUtil.hasChildPerm(user.getUserName(), "/datasets/", "u")){ %>
													<tr>
														<td width="73%" valign="middle" align="left">
															Create an XML Schema for this table
														</td>
														<td width="27%" valign="middle" align="left">
															<a target="_blank" href="GetSchema?id=TBL<%=tableID%>">
																<img border="0" src="images/icon_xml.jpg" width="16" height="18"/>
															</a>
														</td>
													</tr><%
												}
												
												if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/", "xmli")){ %>
													<tr>
														<td width="73%" valign="middle" align="left">
															Create an instance XML for this table
														</td>
														<td width="27%" valign="middle" align="left">
															<a target="_blank" href="GetXmlInstance?id=<%=tableID%>&type=tbl">
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
														Create an MS Excel template for this table&nbsp;<a target="_blank" onclick="pop(this.href)" href="help.jsp?screen=table&area=excel"><img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/></a>
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
																[ <a target="_blank" onclick="pop(this.href)" href="GetCache?obj_id=<%=tableID%>&obj_type=tbl&idf=<%=dsTable.getIdentifier()%>">Open cache ...</a> ]
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
													<a target="_blank" href="help.jsp?screen=dataset&area=short_name" onclick="pop(this.href)">
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
													<a target="_blank" href="help.jsp?screen=table&area=dataset" onclick="pop(this.href)">
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
													else if (dsID != null && dsID.length()!=0){
														String link = "";
														if (latestRequested)
															link = "dataset.jsp?mode=view&ds_idf=" + dataset.getIdentifier();
														else
															link = "dataset.jsp?mode=view&ds_id=" + dsID;
														%>
														<a href="<%=link%>">
															<b><%=Util.replaceTags(dsName)%></b>
														</a><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- Reference URL -->
								    		<%
								    		String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
								    		if (mode.equals("view") && jspUrlPrefix!=null){
									    		String refUrl = jspUrlPrefix + "dstable.jsp?mode=view&table_idf=" +
									    						dsTable.getIdentifier() + "&pns=" + dsTable.getParentNs();
									    		%>
									    		<tr>
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														Reference URL
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="help.jsp?screen=dataset&area=refurl" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
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
														<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href)">
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
																<a target="_blank" href="visuals/<%=attrValue%>" onFocus="blur()" onclick="pop(this.href)">
																	<img src="visuals/<%=attrValue%>" border="0" height="100px" width="100px"/>
																</a><br/><%
															}
															// link
															if (mode.equals("edit") && user!=null){
																String actionText = Util.voidStr(attrValue) ? "add image" : "manage this image";
																%>
																<span class="barfont">
																	[Click <a target="_blank" onclick="pop(this.href)" href="imgattr.jsp?obj_id=<%=tableID%>&amp;obj_type=T&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=dsTable.getShortName()%>&amp;attr_name=<%=attribute.getShortName()%>"><b>HERE</b></a> to <%=actionText%>]
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
																	<a target="_blank" onclick="pop(this.href)" href="fixed_values.jsp?mode=view&amp;delem_id=<%=attrID%>&amp;delem_name=<%=attribute.getShortName()%>&amp;parent_type=attr">
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
										    
								    		<!-- Identifier -->
								    		<tr>
												<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
													Identifier
												</td>
												<td width="4%" class="simple_attr_help<%=isOdd%>">
													<a target="_blank" href="help.jsp?screen=dataset&area=identifier" onclick="pop(this.href)">
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
										if (mode.equals("view") && elems!=null && elems.size()>0 || mode.equals("view") && user!=null && editPrm && topFree){
											
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
												boolean hasCommonElms = false;
												%>
												<table border="0" width="100%" cellspacing="0" cellpadding="3">
													
													<tr height="10"><td colspan="<%=String.valueOf(colspan)%>"></td></tr>
													<tr>
														<td width="34%">
															<b><%=title%><a name="elements"></a></b>
														</td>
														
														<%
														// elements link
														if (user!=null && editPrm && topFree){
															String elemLink = "tblelems.jsp?table_id=" + tableID + "&ds_id=" + dsID + "&ds_name=" + dsName + "&ds_idf=" + dsIdf;
															%>
															<td class="barfont" width="66%">
																[Click <a href="<%=elemLink%>"><b>HERE</b></a> to manage all elements of this table]
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
																		
																		boolean elmCommon = elem.getNamespace()==null || elem.getNamespace().getID()==null;
																		
																		String elemLink = "";
																		if (latestRequested){
																			elemLink = "data_element.jsp?mode=view&delem_idf=" + elem.getIdentifier();
																			if (!elmCommon)
																				elemLink = elemLink + "&pns=" + elem.getNamespace().getID();
																		}
																		else
																			elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID();
																			
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
																				if (elmCommon){ %>
																					<span class="commonelm"><sup>C</sup></span><%
																					hasCommonElms = true;
																				}
																				
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
																	(the <u><b><i>(FK)</i></b></u> link indicates the element participating in a foreign key relation)
																</td>
															</tr><%
														}
														if (elems!=null && elems.size()>0 && hasCommonElms){%>
															<tr height="10">
																<td width="100%" class="barfont" colspan="<%=String.valueOf(colspan)%>">
																	(the <span class="commonelm"><sup>C</sup></span> sign marks a common element)
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
															[Click <a target="_blank" onclick="pop(this.href)" href="complex_attrs.jsp?parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=dsTable.getShortName()%>&amp;dataset_id=<%=dsID%>"><b>HERE</b></a> to manage complex attributes of this table]
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
																			<a target="_blank" onclick="pop(this.href)" href="complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=tableID%>&amp;parent_type=T&amp;parent_name=<%=dsTable.getShortName()%>&amp;dataset_id=<%=dsID%>" title="Click here to view all the fields">
																				<%=attrName%>
																			</a>
																		</td>
																		<td width="4%" class="complex_attr_help<%=isOdd%>">
																			<a target="_blank" onclick="pop(this.href)" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX">
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
				
				<%
				String latestID = dsTable==null ? null : verMan.getLatestTblID(dsTable);
				if (latestID!=null){%>
					<input type="hidden" name="latest_id" value="<%=latestID%>"><%
				}
				
				if (dsID!=null && dsID.length()>0){ %>
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
				
				if (mode.equals("view")){ %>
					<input type="hidden" name="upd_version" value="false"/><%
				}
				%>
				
				<input type="hidden" name="copy_tbl_id" value=""/>
				<input type="hidden" name="changed" value="0">
				
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
