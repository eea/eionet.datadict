<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,java.io.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private static final int MAX_CELL_LEN=40;%>
<%!private static final int MAX_ATTR_LEN=500;%>
<%!private static final int MAX_DISP_VALUES=30;%>

<%!

private DElemAttribute getAttributeByName(String name, Vector mAttributes){
	
	for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr;
	}
        
	    return null;
}

private String getAttributeIdByName(String name, Vector mAttributes){
	
	for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}

private String getValue(String id, String mode, DataElement dataElement, DataElement newDataElement){
	return getValue(id, 0, mode, dataElement, newDataElement);
}
/*
		int val indicates which type of value is requested. the default is 0
		0 - display value (if original value is null, then show inherited value)
		1 - original value
		2 - inherited value
*/
private String getValue(String id, int val, String mode, DataElement dataElement, DataElement newDataElement){
	
	if (id==null) return null;
	DElemAttribute attr = null;

	if (mode.equals("add")){
		if (val<2) 
			return null;
		else{
			if (newDataElement==null) return null;
			attr = newDataElement.getAttributeById(id);
		}
	}
	else{
		if (dataElement==null) return null;
		attr = dataElement.getAttributeById(id);
	}

	if (attr == null) return null;
	if (val==1)
		return attr.getOriginalValue();
	else if (val==2)
		return attr.getInheritedValue();
	else
		return attr.getValue();
}

private Vector getValues(String id, String mode, DataElement dataElement, DataElement newDataElement){
	return getValues(id, 0, mode, dataElement, newDataElement);
}

/*
		int val indicates which group of values is requested. the default is 0
		0 - all
		1 - original
		2 - inherited
*/
private Vector getValues(String id, int val, String mode, DataElement dataElement, DataElement newDataElement){
	if (id==null) return null;
	DElemAttribute attr = null;

	if (mode.equals("add")){
		if (val<2) 
			return null;
		else{
			if (newDataElement==null) return null;
			attr = newDataElement.getAttributeById(id);
		}
	}
	else{
		if (dataElement==null) return null;
		attr = dataElement.getAttributeById(id);
	}

	if (attr == null) return null;
	if (val==1)
		return attr.getOriginalValues();
	else if (val==2)
		return attr.getInheritedValues();
	else
		return attr.getValues();
}

private String getAttributeObligationById(String id, Vector mAttributes){
	
	for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getID().equalsIgnoreCase(id))
        	return attr.getObligation();
	}
        
    return null;
}

private String legalizeAlert(String in){
        
    in = (in != null ? in : "");
    StringBuffer ret = new StringBuffer(); 
  
    for (int i = 0; i < in.length(); i++) {
        char c = in.charAt(i);
        if (c == '\'')
            ret.append("\\'");
        else if (c == '\\')
        	ret.append("\\\\");
        else
            ret.append(c);
    }

    return ret.toString();
}

%>

			<%
			
			request.setCharacterEncoding("UTF-8");

			//
			String mode=null;
			Vector mAttributes=null;
			DataElement dataElement=null;
			DataElement newDataElement=null;
			Vector complexAttrs=null;
			Vector fixedValues=null;
			//
			
			ServletContext ctx = getServletContext();
			XDBApplication xdbApp = XDBApplication.getInstance(ctx);
			AppUserIF user = SecurityUtil.getUser(request);
			
			if (user!=null){ //if user has logged in, we disable caching
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Cache-Control", "no-cache");
				response.setDateHeader("Expires", 0);
				response.setHeader("Cache-Control", "no-store");
			}
	
			%>
			<%@ include file="history.jsp" %>
			<%

			if (request.getMethod().equals("POST") && user==null){ %>
				<b>Not authorized to post any data!</b> <%
	      		return;
			}						
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			else if (mode.equals("add")){
				if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/elements", "i")){ %>
					<b>Not allowed!</b> <%
	      			return;
				}
			}
			
			String delem_id = request.getParameter("delem_id");
			String delemIdf = request.getParameter("delem_idf");
			String pns = request.getParameter("pns");
			String copy_elem_id = request.getParameter("copy_elem_id");
			
			if (mode.equals("view")){
				if (Util.voidStr(delem_id) && Util.voidStr(delemIdf)){ %>
					<b>Missing ID or Identifier and parent namespace!</b> <%
					return;
				}
			}
			else if (mode.equals("edit")){
				if (Util.voidStr(delem_id)){ %>
					<b>Missing ID to edit!</b> <%
					return;
				}
			}
			else if (mode.equals("copy")){
				if (Util.voidStr(copy_elem_id)){ %>
					<b>Missing ID to copy!</b> <%
					return;
				}
			}
			
			boolean latestRequested = mode.equals("view") && !Util.voidStr(delemIdf);
			
			if (mode.equals("add"))
				dataElement = null;
			
			String dsID = request.getParameter("ds_id");
			String tableID = request.getParameter("table_id");
			
			boolean editPrm = false;
			boolean deletePrm = false;
			
			boolean elmCommon = request.getParameter("common")!=null;
			if (mode.equals("add") && !elmCommon && dsID==null){
				if (request.getMethod().equals("POST")){ %>
					<b>Dataset ID missing in POST!</b><%
					return;
				}
				else if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){ %>
					<b>Not allowed!</b><%
					return;
				}
			}
			
			String type = request.getParameter("type");
			if (type!=null && type.length()==0)
				type = null;
			
			String s = request.getParameter("pick");
			boolean wasPick = (s==null || !s.equals("true")) ? false : true;
			
			if (wasPick)
				tableID = null;

			// handle the POST
			///////////////////////////////////////////
						
			if (request.getMethod().equals("POST")){
				
				DataElementHandler handler = null;
				Connection userConn = null;
				String redirUrl = "";
				
				try{
					if (!wasPick){
						
						if (!elmCommon){
							String dsidf = request.getParameter("ds_idf");
							if (dsidf==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsidf, "u")){%>
								<b>Not allowed!</b><%
								return;
							}
						}
						else if (!mode.equals("add") && !SecurityUtil.hasPerm(user.getUserName(), "/elements", "u")){%>
							<b>Not allowed!</b><%
							return;
						}
						
						userConn = user.getConnection();
						handler = new DataElementHandler(userConn, request, ctx);
						handler.setUser(user);
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
					}
					
					if ((mode.equals("add") && !wasPick) || mode.equals("copy")){
						String id = handler.getLastInsertID();
						if (id != null && id.length()!=0) redirUrl = redirUrl + "data_element.jsp?mode=edit&delem_id=" + id;
						if (history!=null){
						int idx = history.getCurrentIndex();
							if (idx>0)
								history.remove(idx);
						}
					}
					else if (mode.equals("edit") && !wasPick){
						
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
					else if (mode.equals("delete") && !wasPick){
						
						if (request.getParameter("common")!=null){
							String latestCommonElmID = handler.getLatestCommonElmID();
							if (!Util.voidStr(latestCommonElmID))
								redirUrl = redirUrl + "data_element.jsp?mode=view&delem_id=" + latestCommonElmID;
							else
								redirUrl = redirUrl + "index.jsp";
						}
						else{
							String lid = request.getParameter("latest_id");
							String newTblID = handler.getNewTblID();
							if (!Util.voidStr(newTblID))
								redirUrl = redirUrl + "dstable.jsp?mode=view&table_id=" + newTblID;
							else if (!Util.voidStr(lid))
								redirUrl = redirUrl + "data_element.jsp?mode=view&delem_id=" + lid;
							else{
								String	deleteUrl = history.gotoLastNotMatching("data_element.jsp");
								redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ?
											deleteUrl :
											redirUrl + "index.jsp";
							}
						}
					}
					else if (wasPick){
						redirUrl = redirUrl + "data_element.jsp?&mode=" + mode;
						
						if (delem_id!=null) redirUrl = redirUrl + "&delem_id=" + delem_id;
						if (type!=null) redirUrl = redirUrl + "&type=" + type;
						
						if (dsID != null) redirUrl = redirUrl + "&ds_id=" + dsID;
						if (tableID != null) redirUrl = redirUrl + "&table_id=" + tableID;
					}
				}
				finally{
					try { if (userConn!=null) userConn.close();
					} catch (SQLException e) {}
				}
				
				response.sendRedirect(redirUrl);
				return;
				
			} // end of handle the POST
			
			Connection conn = null;
			DBPoolIF pool = xdbApp.getDBPool();
			
			// start the whole page try block
			
			try {
			
			conn = pool.getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			searchEngine.setUser(user);
			
			// get metadata on all attributes
			mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
			
			// get the data element if not "add" mode
			String delem_name = "";
			String idfier = "";
			
			if (mode.equals("edit") || mode.equals("view")){
				
				if (latestRequested){
					dataElement = searchEngine.getLatestElm(delemIdf, pns);
					if (dataElement!=null) delem_id = dataElement.getID();
				}
				else
					dataElement = searchEngine.getDataElement(delem_id);
					
				if (dataElement!=null){
					type = dataElement.getType();
					
					idfier = dataElement.getIdentifier();
					if (idfier == null) idfier = "unknown";
					if (idfier.length() == 0) idfier = "empty";
					
					delem_name = dataElement.getShortName();
					if (delem_name == null) delem_name = "unknown";
					if (delem_name.length() == 0) delem_name = "empty";
				}
				else{ %>
					<b>Data element was not found!</b> <%
					return;
				}
			}
			else if (mAttributes.size()==0){ %>
				<b>No metadata on attributes found!</b> <%
				return;
			}
			else if(mode.equals("add")){
				
				idfier = request.getParameter("idfier");
				if (idfier==null) idfier="";
				
				delem_name = request.getParameter("delem_name");
				if (delem_name==null) delem_name="";
				
				//get inherited attribues
				if (dsID!=null && tableID!=null){
					if (!dsID.equals("") && !tableID.equals("")){
						newDataElement = new DataElement();
						newDataElement.setDatasetID(dsID);
						newDataElement.setTableID(tableID);
						newDataElement.setAttributes(searchEngine.getSimpleAttributes(null, "E", tableID, dsID));
					}
				}
			}
			
			// set the flag indicating if the data element is a common one or not
			Namespace elmNs = dataElement==null ? null : dataElement.getNamespace();
    		if (!mode.equals("add")) elmCommon = elmNs==null || elmNs.getID()==null;
			
			// get the dataset and table
			Dataset dataset = null;
			DsTable dsTable = null;
			
			if (dataElement!=null && !elmCommon){
				tableID = dataElement.getTableID();
				if (tableID!=null){
					dsTable = searchEngine.getDatasetTable(tableID);
					if (dsTable!=null){
						dsID = dsTable.getDatasetID();
						if (dsID!=null){							
							dataset = searchEngine.getDataset(dsID);
						}
					}
				}
			}

			// find out if this is the dataset's latest version
			VersionManager verMan = new VersionManager(conn, searchEngine, user);
			boolean isLatest = false;
			if (elmCommon)
				isLatest = dataElement==null ? false : verMan.isLatestCommonElm(dataElement.getID(), dataElement.getIdentifier());
			else if (dataset!=null)
				isLatest = verMan.isLatestDst(dataset.getID(), dataset.getIdentifier());
			
			// implementing check-in/check-out
			
			String workingUser = null;
			if (dataElement!=null)
				workingUser = verMan.getWorkingUser(dataElement.getNamespace().getID(),
			    											dataElement.getIdentifier(), "elm");
			if (mode.equals("edit") && user!=null){
				
				// see if element is checked out
				if (Util.voidStr(workingUser)){
					
				    // element not checked out, create working copy
				    // but first make sure it's the latest version
				    if (!isLatest){
					    if (elmCommon){ %>
					    	<b>Trying to check out a common element that is not the latest!</b><%
				    	}
				    	else{ %>
					    	<b>Trying to check out in a dataset that is not the latest!</b><%
				    	}
					    return;
				    }
				    
				    //String copyID = verMan.checkOutElm(delem_id, elmCommon);
				    String copyID = verMan.checkOut(delem_id, "elm");
				    if (!delem_id.equals(copyID)){
					    
					    // send to copy if created successfully
					    // but first remove previous url (edit original) from history					    
						history.remove(history.getCurrentIndex());
						
					    String qryStr = "mode=edit&type=" + type;
					    qryStr+= "&delem_id=" + copyID;
				        response.sendRedirect("data_element.jsp?" + qryStr);
			        }
			    }
			    else if (!workingUser.equals(user.getUserName())){
				    // element is chekced out by another user
				    %>
				    <b>This element is already checked out by another user: <%=workingUser%></b>
				    <%
				    return;
			    }
			    else if (dataElement!=null && !dataElement.isWorkingCopy()){
				    
				    // element is checked out by THIS user.
				    // If it's not the working copy, send the user to it
				    String copyID = verMan.getWorkingCopyID(dataElement);
				    
				    if (copyID!=null && !copyID.equals(delem_id)){
					    
					    // first remove previous url (edit original) from history
						history.remove(history.getCurrentIndex());
						
						String qryStr = "mode=edit&type=" + type;
						qryStr+= "&delem_id=" + verMan.getWorkingCopyID(dataElement);
						response.sendRedirect("data_element.jsp?" + qryStr);
					}
			    }
		    }
			
			// set permissions
			editPrm = user!=null;
			if (editPrm){
				if (elmCommon)
					editPrm = SecurityUtil.hasPerm(user.getUserName(), "/elements" , "u");
				else
					editPrm = dataset!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
			}
			deletePrm = editPrm;
			
			// get the complex attributes
			complexAttrs = searchEngine.getComplexAttributes(delem_id, "E", null, tableID, dsID);
			if (complexAttrs == null) complexAttrs = new Vector();
			
			DElemAttribute attribute = null;
			
			// we get the Registration status in order to know to warn if somebody wnats to edit a Released definition
			String regStatus = !elmCommon || dataElement==null ? null : dataElement.getStatus();
			
			// set up referring tables (if common element)
        	Vector refTables = null;
        	if (!mode.equals("add") && elmCommon)
        		refTables = searchEngine.getReferringTables(delem_id);
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
    <title>Data element - Data Dictionary</title>
    <script type="text/javascript" src='script.js'></script>
    <script type="text/javascript" src='modal_dialog.js'></script>
    <script type="text/javascript">
		// <![CDATA[
    
    	function forceAttrMaxLen(){
	    	var i = 0;
	    	var elms = document.forms["form1"].elements;
	    	for (i=0; elms!=null && i<elms.length; i++){
		    	var elmName = elms[i].name;
		    	if (startsWith(elmName, "attr_")){
			    	if (elms[i].value.length > <%=MAX_ATTR_LEN%>){
			    		alert("Maximum length of attribute values for data element definitions is <%=MAX_ATTR_LEN%>!");
			    		return;
		    		}
				}
	    	}
    	}
    
    	function openSchema(){
			window.open("station.xsd",null, "height=400,width=600,status=no,toolbar=no,menubar=no,location=no,scrollbars=yes,top=100,left=100");
		}

		function submitPick(){
			document.forms["form1"].elements["pick"].value = "true";
			document.forms["form1"].submit();
		}
		
		function checkIn(){
			submitCheckIn();
		}
		
		function submitCheckIn(){

			<%
			if (!elmCommon && dataset!=null && dataset.getStatus().equals("Released")){ %>
				var b = confirm("Please note that you are checking in a non-common element definition in a dataset definition that is in Released status! " +
								"This will create a new version of that dataset definition and it will be automatically released for public view. " +
								"If you want to continue, click OK. Otherwise click Cancel.");
				if (b==false) return;<%
			}
			
			String latestRegStatus = elmCommon ? searchEngine.getLatestRegStatus(dataElement) : "";
			if (latestRegStatus.equals("Released")){ %>
				var b = confirm("Please note that you are checking your changes into a common element definition that was in Released status prior to checking out! " +
				"By force, this will create a new version of that definition! If you want to continue, click OK. Otherwise click Cancel.");
				if (b==false) return;<%
			}
			
			if (elmCommon){
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
				}<%
			}
			%>

			document.forms["form1"].elements["check_in"].value = "true";
			var b = submitForm('edit');
			if (b==false)
				document.forms["form1"].elements["check_in"].value = "false";
		}
		
		function submitForm(mode){
			
			if (mode=="add"){
				
				<%
				if (!elmCommon){ %>
					var ds = document.forms["form1"].elements["ds_id"].value;
					if (ds==null || ds==""){
						alert('Dataset not specified!');
						return false;
					}
					
					var tbl = document.forms["form1"].elements["table_id"].value;
					if (tbl==null || tbl==""){
						alert('Table not specified!');
						return false;
					}<%
				}
				%>
			}
			
			if (mode == "delete"){
				<%
				String confirmDelTxt = "";
				if (elmCommon){
					if (!mode.equals("add") && dataElement.isWorkingCopy())
						confirmDelTxt = "This working copy will be deleted! Click OK, if you want to continue. Otherwise click Cancel.";
					else if (refTables!=null && refTables.size()>0){
						confirmDelTxt = "Please note that this common element definition is used in some tables " +
										"(see the relevant list on this page)! If you still want to delete it, click OK. " +
										"Otherwise click Cancel.";
					}
					else if (regStatus!=null && regStatus.equals("Released")){
						confirmDelTxt = "Please note that a common element definition in Released status might be referenced by other " +
										"applications! If you still want to delete it, click OK. Otherwise click Cancel.";
					}
				}
				else{
					if (!mode.equals("add") && dataElement.isWorkingCopy())
						confirmDelTxt = "This working copy will be deleted and the whole dataset released for others to edit! " +
							            "Click OK, if you want to continue. Otherwise click Cancel.";
					else
						confirmDelTxt = "This element will be deleted! You will be asked if you want this to update the dataset's " +
							  		    "CheckInNo as well. Click OK, if you want to continue. Otherwise click Cancel.";
				}
				%>
				
				if (confirm("<%=Util.replaceTags(confirmDelTxt)%>")==false)
					return false;
				
				<%
				if (dataElement!=null && dataElement.isWorkingCopy()){ %>
					document.forms["form1"].elements["upd_version"].value = "false";
					deleteReady();
					return false;<%
				}
				else if (!elmCommon){ %>
					// now ask if the deletion should also result in the dataset's new version being created				
					openNoYes("yesno_dialog.html", "Do you want to update the dataset definition's CheckInNo with this deletion?", delDialogReturn,100, 400);
					return false;<%
				}
				%>
			}
			
			if (mode != "delete"){
				
				forceAttrMaxLen();
				
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return false;
				}
				
				if (hasWhiteSpace("idfier")){
					alert("Identifier cannot contain any white space!");
					return false;
				}
				
				if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)){
					alert("Identifier not valid for usage as an XML tag! " +
						  "In the first character only underscore or latin characters are allowed! " +
						  "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
					return false;
				}
			}
			
			slctAllValues();
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
			return true;
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
			
			var o = document.forms["form1"].delem_name;
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
		
    	function edit(){
	    	<%
	    	// check if this is a common element in Released status
			if (regStatus!=null && regStatus.equals("Released")){ %>
				var a = confirm("Please be aware that this is a definition in Released status. Unless " +
					  			"you change the status back to something lower, your edits will become " +
					  			"instantly visible for the public visitors after you check in the definition! " +
					  			"Click OK, if you want to continue. Otherwise click Cancel.");
				if (a == false) return;<%
			}
			
			// check if this is a non-common element in a Released dataset
			if (dataset!=null && dataset.getStatus().equals("Released")){ %>
				var b = confirm("Please be aware that you are about to edit an element definition in a dataset definition " +
					  			"in Released status. Unless you change the dataset definition's status back to something lower, " +
					  			"your edits will become instantly visible for the public visitors after you check in this " +
					  			"element definition! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b == false) return;<%
			}
			
			String qryStr = request.getQueryString();
			// if no query string or it does not contain "delem_id=" and it's not "add" mode,
			// then it means it must be a refernce URL, and so in that case
			// construct new location url from scratch,
			// else construct on the basis of query string
			if (!mode.equals("add") && (qryStr==null || qryStr.indexOf("delem_id=")<0)){
				%>
				document.location.assign("data_element.jsp?mode=edit&delem_id=" + <%=dataElement.getID()%>);
				<%
			}			
			else{
		    	String modeString = new String("mode=view&");	    	
		    	int modeStart = qryStr.indexOf(modeString);
		    	if (modeStart == -1){
			    	modeString = new String("mode=view");
			    	modeStart = qryStr.indexOf(modeString);
		    	}
		    	
		    	if (modeStart != -1){
			    	StringBuffer buf = new StringBuffer(qryStr.substring(0, modeStart));
			    	buf.append("mode=edit&");
			    	buf.append(qryStr.substring(modeStart + modeString.length()));
			    	%>
					document.location.assign("data_element.jsp?<%=buf.toString()%>");
					<%
				}
			}
			%>
		}
		
		function fixType(){
			
			var type = document.forms["form1"].typeSelect.value;
			if (type == null || type.length==0)
				return;
				
			<%
			if (elmCommon){ %>
				document.location.assign("data_element.jsp?mode=add&common=true&type=" + type);<%
			}
			else{ %>
				document.location.assign("data_element.jsp?mode=add&type=" + type);<%
			}%>
		}

		function changeDatatype(){
			<%
			if (type!=null && type.equals("CH1")){ %>
				return;<%
			}
			else{
				String datatypeID = getAttributeIdByName("Datatype", mAttributes);
				if (datatypeID!=null && datatypeID.length()>0) datatypeID = "attr_" + datatypeID;
				%>
				var elmDataType = document.forms["form1"].<%=datatypeID%>.value;
				if (elmDataType == null || elmDataType.length==0)
					return;
					
				<%
				//String reLocation = elmCommon ? "data_element.jsp?mode=add&common=true" : "data_element.jsp?mode=add";				
				//if (type!=null && type.length()>0) reLocation = reLocation + "&type=" + type;
				
				String reLocation = "data_element.jsp?" + request.getQueryString();
				int i = reLocation.indexOf("elm_datatype=");
				if (i>=0){
					StringBuffer buf = new StringBuffer(reLocation.substring(0,i));
					int j = reLocation.indexOf("&", i);
					if (j>=0) buf.append(reLocation.substring(j+1));
					reLocation = buf.toString();
				}
				
				if (!reLocation.endsWith("&")) reLocation = reLocation + "&";
				%>
				
				document.location.assign("<%=reLocation%>elm_datatype=" + elmDataType);<%
			}
			%>
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
 			form_changed("form1");
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
		
		function copyElem(){
			
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
			
			<%
			if (!elmCommon){ %>
				var ds = document.forms["form1"].elements["ds_id"].value;
				if (ds==null || ds==""){
					alert('Dataset not specified!');
					return;
				}
				
				var tbl = document.forms["form1"].elements["table_id"].value;
				if (tbl==null || tbl==""){
					alert('Table not specified!');
					return;
				}<%
			}
			%>
			
			if (!checkObligations()){
				alert("You have not specified one of the mandatory atttributes!");
				return;
			}
			
			forceAttrMaxLen();
			
			var type;
			var url="search.jsp?ctx=popup&noncommon";
			if (document.forms["form1"].elements["type"]){
				type = document.forms["form1"].elements["type"].value;
				url += "&type=" + type;
			}
		
			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
			if (window.focus){
				wAdd.focus();
			}
		}
		function pickElem(id){
			
			document.forms["form1"].elements["copy_elem_id"].value=id;
			document.forms["form1"].elements["mode"].value = "copy";
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
			
String attrID = null;
String attrValue = null;

boolean popup = request.getParameter("popup")!=null;

if (popup){ %>	

	<body class="popup">
	
	<div class="popuphead">
		<h1>Data Dictionary</h1>
		<hr/>
		<div align="right">
			<form acceptcharset="UTF-8" name="close" action="javascript:window.close()">
				<input type="submit" class="smallbutton" value="Close"/>
			</form>
		</div>
	</div>
	<div><%
}
else{
	%>
	<body>
		<jsp:include page="nlocation.jsp" flush='true'>
          <jsp:param name="name" value="Data element"/>
          <jsp:param name="back" value="true"/>
        </jsp:include>
		<%@ include file="nmenu.jsp" %>
		<div id="workarea">
<%
} // end if not popup
%>
			<div id="operations">
				<%
				String hlpScreen = "element";
				if (mode.equals("edit"))
					hlpScreen = "element_edit";
				else if (mode.equals("add"))
					hlpScreen = "element_add";
				%>
				<ul>
					<li><a target="_blank" href="help.jsp?screen=<%=hlpScreen%>&amp;area=pagehelp" onclick="pop(this.href)">Page help</a></li>
					<%
					if (mode.equals("view") && user!=null && dataElement!=null && elmCommon && dataElement.getIdentifier()!=null){
						%>
						<li><a href="Subscribe?common_element=<%=Util.replaceTags(dataElement.getIdentifier())%>">Subscribe</a></li>
						<%
					}
					%>
				</ul>
			</div>
			
			<!-- The buttons displayed in view mode -->
			<div style="clear:right; float:right">
				<%
				// the buttons displayed in view mode
				if (!popup && mode.equals("view") && dataElement!=null){
					if (user!=null){
						
						// set some helper flags
						String topWorkingUser = verMan.getWorkingUser(dataElement.getTopNs());
						boolean topFree = elmCommon ? workingUser==null : topWorkingUser==null;
						boolean inWorkByMe = workingUser==null ? false : workingUser.equals(user.getUserName());
						
						/*System.out.println("===> editPrm = " + editPrm);
						System.out.println("===> dataElement.isWorkingCopy() = " + dataElement.isWorkingCopy());
						System.out.println("===> topFree = " + topFree);
						System.out.println("===> isLatest = " + isLatest);
						System.out.println("===> inWorkByMe = " + inWorkByMe);*/
						
						if (editPrm){
							if (dataElement.isWorkingCopy() ||
								(isLatest && topFree) ||
								(isLatest && inWorkByMe)){ %>
								<input type="button" class="smallbutton" value="Edit" onclick="edit()"/><%
							}
						}
						
						/*System.out.println("===> deletePrm = " + deletePrm);
						System.out.println("===> dataElement.isWorkingCopy() = " + dataElement.isWorkingCopy());
						System.out.println("===> isLatest = " + isLatest);
						System.out.println("===> topFree = " + topFree);*/
						
						if (deletePrm){
							if (!dataElement.isWorkingCopy() && isLatest && topFree){ %>
								<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/><%
							}
						}
						
						if (elmCommon && editPrm){ %>
							<input type="button" class="smallbutton" value="History" onclick="popNovr('elm_history.jsp?id=<%=dataElement.getID()%>')"/> <%
						}
					}
				}
				// the working copy part
				else if (!popup && dataElement!=null && dataElement.isWorkingCopy()){ %>
					<span class="wrkcopy">Working copy</span><%
				}
				%>
			</div>
			
			<%
			String verb = "View";
			if (mode.equals("add"))
				verb = "Add";
			else if (mode.equals("edit"))
				verb = "Edit";	
				
			String sCommon = elmCommon ? "common" : "";
			%>
			<h1><%=verb%> <%=sCommon%> element definition</h1>
		    
			<div style="clear:both">
			<br/>
			<form name="form1" id="form1" method="post" action="data_element.jsp">
			
				<%
				if (!mode.equals("add")){ %>
					<input type="hidden" name="delem_id" value="<%=delem_id%>"/><%
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
					
								<table border="0" width="620" class="mnd_opt_cnd" cellspacing="0">
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
					if (mode.equals("add") || mode.equals("edit")){
						%>				
				<table border="0" width="620" cellspacing="0" cellpadding="0">
						
							<tr><td width="100%" colspan="2" height="10"></td></tr>
							<tr>
							<td width="100%" align="right" colspan="2">
							<%
								// add case
								if (mode.equals("add")){
									if (user==null){ %>
										<input type="button" class="mediumbuttonb" value="Add" disabled="true"/><%
									}
									else { %>
										<input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>&nbsp;
										<input type="button" class="mediumbuttonb" value="Copy" onclick="copyElem()" title="Copies data element attributes from existing data element"/><%
									}
								}
								// edit case
								else if (mode.equals("edit")){
									String isDisabled = user==null ? "disabled" : "";
									%>
									<input type="button" class="mediumbuttonb" value="Save" <%=isDisabled%> onclick="submitForm('edit')"/>&nbsp;
									<%
									if (!dataElement.isWorkingCopy()){ %>
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
						boolean brandNew = dataElement!=null && elmCommon && verMan.isFirstCommonElm(dataElement.getIdentifier());
						if (mode.equals("edit") && dataElement!=null && dataElement.isWorkingCopy() && user!=null){
							
							boolean allowOverwrite = false;
							if (elmCommon && !latestRegStatus.equals("Released"))
								allowOverwrite = true;
							else if (!elmCommon && (dataset==null || !dataset.getStatus().equals("Released")))
								allowOverwrite = true;
							
							String updVerText = elmCommon ?
												"Update this element definition's CheckInNo when checking in" :
												"Update the dataset definition's CheckInNo when checking in";
							
							String hidden = "";
							StringBuffer checkbox = new StringBuffer("<input type=\"checkbox\" value=\"true\" name=\"");
							if (allowOverwrite)
								checkbox.append("upd_version\"");
							else{
								checkbox.append("upd_version_DISABLED\" checked=\"true\" disabled=\"true\"");
								hidden = "<input type=\"hidden\" name=\"upd_version\" value=\"true\"/>";
							}
							checkbox.append("/>").append("&nbsp;"+updVerText);
							
							if (!brandNew){ %>
								<tr>
									<td align="right" class="smallfont_light" colspan="2"><%=checkbox%><%=hidden%></td>
								</tr><%
							}
							else{
								%>
								<input type="hidden" name="upd_version" value="false"/>
								<%
							}
						}
								%>
					</table>
								<%
					}
					%>
	                
	                <!-- main table body -->
	                
		                    
		                    	<!-- quick links -->
		                    	
		                    	<%
		                    	// set up fixed values
		                    	fixedValues = mode.equals("add") ? null : searchEngine.getFixedValues(delem_id, "elem");
		                    	
		                    	// set up foreign key relations (if non-common element)
		                    	Vector fKeys = null;
		                    	if (!mode.equals("add") && !elmCommon && dataset!=null)
		                    		fKeys = searchEngine.getFKRelationsElm(delem_id, dataset.getID());
		                    	
		                    	if (mode.equals("view")){
			                    	Vector quicklinks = new Vector();
			                    	
			                    	if (fixedValues!=null && fixedValues.size()>0)
			                    		quicklinks.add("Allowable values | values");
			                    	if (fKeys!=null && fKeys.size()>0)
			                    		quicklinks.add("Foreign key relations | fkeys");
			                    	if (complexAttrs!=null && complexAttrs.size()>0)
			                    		quicklinks.add("Complex attributes | cattrs");
			                    	
			                    	request.setAttribute("quicklinks", quicklinks);
			                    	%>
		                    		<jsp:include page="quicklinks.jsp" flush="true">
		                    		</jsp:include>
						            <%
								}
								%>
								
								<!-- schema && codelist links-->
								
								<%
								// display schema link only in view mode and only for users that have a right to edit a dataset
								if (mode.equals("view")){
									boolean dispAll = editPrm;
									boolean dispXmlSchema = dataset!=null && dataset.displayCreateLink("XMLSCHEMA");
									//user!=null && SecurityUtil.hasChildPerm(user.getUserName(), "/datasets/", "u")
									if (!popup && (dispAll || dispXmlSchema)){ %>
								<div id="createbox">
									<table class="datatable1">
										<tr>
											<td width="73%">
												Create an XML Schema for this element
											</td>
											<td width="27%">
												<a target="_blank" href="GetSchema?id=ELM<%=delem_id%>">
													<img border="0" src="images/icon_xml.jpg" width="16" height="18" alt=""/>
												</a>
											</td>
										</tr>
										<%
										if (dataElement.getType().equals("CH1") && fixedValues!=null && fixedValues.size()>0){%>
											<tr>
												<td width="73%">
													Get a comma-separated codelist of this element
												</td>
												<td width="27%">
													<a target="_blank" href="CodelistServlet?id=<%=dataElement.getID()%>&amp;type=ELM">
														<img border="0" src="images/icon_txt.gif" width="16" height="18" alt=""/>
													</a>
												</td>
											</tr><%
										}
										%>
									</table>
								</div>
										<%
									}
								}
								%>
								
								<!-- type -->
								<div style="margin: 3px">
										<b>Type:</b>
											<%
											if (mode.equals("add") && (type==null || type.length()==0)){ %>
												<select class="small" name="typeSelect" onchange="fixType()">
													<option value="">-- Select element type --</option>
													<option value="CH1">Data element with fixed values (codes)</option>
													<option value="CH2">Data element with quantitative values (e.g. measurements)</option>
												</select> <%
											}
											else{														
												if (type.equals("CH1")){ %>
													<b>DATA ELEMENT WITH FIXED VALUES</b>
												<% }else if (type.equals("CH2")){ %>					
													<b>DATA ELEMENT WITH QUANTITATIVE VALUES</b>
												<% } else{ %>
													<b>DATA ELEMENT WITH QUANTITATIVE VALUES</b> <%
												}
											}
											%>
											<a target="_blank" href="help.jsp?screen=element&amp;area=type" onclick="pop(this.href)">
												<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
											</a>
								</div>
								
								
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
								  		
								  			<!-- short name -->								  			
								    		<tr>
												<td width="<%=titleWidth%>%" class="short_name">Short name</td>
												<td width="4%" class="short_name simple_attr_help">
													<a target="_blank" href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href)">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
													</a>
												</td>
												<%
												if (colspan==4){
													%>
													<td width="4%" class="short_name simple_attr_help">
														<img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="short_name_value">
													<%
													if(mode.equals("view")){ %>
														<%=Util.replaceTags(dataElement.getShortName())%>
														<input type="hidden" name="delem_name" value="<%=Util.replaceTags(dataElement.getShortName(),true)%>"/><%
													}
													else if (mode.equals("add")){ %>
														<input class="smalltext" type="text" size="30" name="delem_name"/><%
													}
													else { %>
														<input class="smalltext" type="text" size="30" name="delem_name" value="<%=Util.replaceTags(dataElement.getShortName())%>"/><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- dataset & table part, relevant for non-common elements only -->
								    		
								    		<%
								    		Dataset dst = null;
								    		if (!elmCommon){
								    		
									    		// dataset
									    		
												if (mode.equals("add") || dataset!=null){ %>
										    		<tr class="zebra<%=isOdd%>">
										    			<td width="<%=titleWidth%>%" class="simple_attr_title">
															Dataset
														</td>
														<td width="4%" class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=table&amp;area=dataset" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
															</a>
														</td>
														<%
														if (colspan==4){%>
															<td width="4%" class="simple_attr_help">
																<img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/>
															</td><%
														}
														%>
														<td width="<%=valueWidth%>%" class="simple_attr_value">
															<%
															String link = "";
															if (latestRequested)
																link = "dataset.jsp?mode=view&amp;ds_idf=" + dataset.getIdentifier();
															else
																link = "dataset.jsp?mode=view&amp;ds_id=" + dsID;
															// the case when dataset has been specified and mode is view or edit
															if (dataset!=null && !mode.equals("add")){ %>
																<a href="<%=link%>">
																	<b><%=Util.replaceTags(dataset.getShortName())%></b>
																</a>
																<input type="hidden" name="ds_id" value="<%=dsID%>"/><%
															}
															// other cases
															else {
															
																dsID = request.getParameter("ds_id");
																if (dsID!=null && dsID.length()!=0)
																	dst = searchEngine.getDataset(dsID);
																%>
																<select class="small" name="ds_id" onchange="submitPick('<%%>')"> <%
																
																	if (dst==null){ %>
																		<option selected="selected" value="">-- select a dataset --</option><%
																	}
																	
																	Vector datasets = searchEngine.getDatasets();
																	for (int i=0; datasets!=null && i<datasets.size(); i++){
																		Dataset ds = (Dataset)datasets.get(i);
																		
																		if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + ds.getIdentifier(), "u"))
																			continue;
																		
																		// skip datasets in work
																		if (verMan.getWorkingUser(ds.getNamespaceID()) != null)
																			continue;
																		
																		boolean isTheOne = dst==null ? false : dst.getID().equals(ds.getID());
																		String selected = isTheOne ? "selected=\"selected\"" : "";
																		// String selected = (dsID!=null && dataset!=null && dsID.equals(ds.getID())) ? "selected" : "";
																		%>
																		<option <%=selected%> value="<%=ds.getID()%>"><%=Util.replaceTags(ds.getShortName())%></option>
																		<%
																	}
																	
																	%>
																</select>&nbsp;
																<span class="barfont">(datasets in work are not displayed)</span><%
															}
															%>
														</td>
														
														<%isOdd = Util.isOdd(++displayed);%>
										    		</tr><%
									    		}
									    		
									    		// table
									    		
												if (mode.equals("add") || dsTable!=null){ %>
													<tr class="zebra<%=isOdd%>">
										    			<td width="<%=titleWidth%>%" class="simple_attr_title">
															Table
														</td>
														<td width="4%" class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=element&amp;area=table" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
															</a>
														</td>
														<%
														if (colspan==4){%>
															<td width="4%" class="simple_attr_help">
																<img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/>
															</td><%
														}
														%>
														<td width="<%=valueWidth%>%" class="simple_attr_value">
															<%
															// the case when table has been specified and mode is view or edit
															if (dsTable!=null && !mode.equals("add")){
																	String link = "";
																	if (latestRequested)
																		link = "dstable.jsp?mode=view&amp;table_idf=" + dsTable.getIdentifier() + "&pns=" + dsTable.getParentNs();
																	else
																		link = "dstable.jsp?mode=view&amp;table_id=" + dsTable.getID();
																	%>
																	<font class="title2" color="#006666">
																	<a href="<%=link%>"><%=Util.replaceTags(dsTable.getShortName())%></a></font>
																	<input type="hidden" name="table_id" value="<%=dsTable.getID()%>"/>
																	<%
															}
															// other cases
															else{															
																tableID = request.getParameter("table_id");
																DsTable tbl = null;
																if (tableID!=null && tableID.length()!=0)
																	tbl = searchEngine.getDatasetTable(tableID);																
																%>
																<select class="small" name="table_id"">
																	<%
																	if (dst!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dst.getIdentifier(), "u")){
																		if (tbl==null){ %>
																			<option selected="selected" value="">-- select a table --</option>
																			<%
																		}
																		
																		Vector tables = searchEngine.getDatasetTables(dst.getID());
																		for (int i=0; tables!=null && i<tables.size(); i++){
																			DsTable tb = (DsTable)tables.get(i);
																			boolean isTheOne = tbl==null ? false : tbl.getID().equals(tb.getID());
																			String selected = isTheOne ? "selected=\"selected\"" : "";
																			%>
																			<option <%=selected%> value="<%=tb.getID()%>"><%=Util.replaceTags(tb.getShortName())%></option>
																			<%
																		}
																	}
																	else{ %>
																		<option value="">-- pick a dataset first! --</option>
																		<%
																	}
																	%>
																</select>
																<%
															}
															%>
														</td>
														
														<%isOdd = Util.isOdd(++displayed);%>
										    		</tr><%
												}
											} // end of dataset & table part, relevant only if not a common element
											%>
											
											<!-- RegistrationStatus, relevant for common elements only -->
								    		<%
								    		if (elmCommon){
									    		%>
									    		<tr class="zebra<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title">
														RegistrationStatus
													</td>
													<td width="4%" class="simple_attr_help">
														<a target="_blank" href="help.jsp?screen=dataset&amp;area=regstatus" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help">
															<img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/>
														</td><%
													}
													%>
													<td width="<%=valueWidth%>%" class="simple_attr_value">
														<%
														if (mode.equals("view")){ %>
															<%=Util.replaceTags(regStatus)%><%
														}
														else{ %>
															<select name="reg_status" onchange="form_changed('form1')"> <%
																Vector regStatuses = verMan.getRegStatuses();
																for (int i=0; i<regStatuses.size(); i++){
																	String stat = (String)regStatuses.get(i);
																	String selected = stat.equals(regStatus) ? "selected=\"selected\"" : ""; %>
																	<option <%=selected%> value="<%=Util.replaceTags(stat)%>"><%=Util.replaceTags(stat)%></option><%
																} %>
															</select><%
														}
														%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}
								    		%>
											
								    		<!-- GIS -->
								    		<%
								    		String gisType = dataElement!=null ? dataElement.getGIS() : null;
											if (!mode.equals("view") || gisType!=null){ %>
									    		<tr class="zebra<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title">
														GIS type
													</td>
													<td width="4%" class="simple_attr_help">
														<a target="_blank" href="help.jsp?screen=element&amp;area=GIS" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help">
															<img border="0" src="images/optional.gif" width="16" height="16" alt=""/>
														</td><%
													}
													%>
													<td width="<%=valueWidth%>%" class="simple_attr_value">
														<%
														if (mode.equals("view")){
															gisType = (gisType==null || gisType.length()==0) ? "&nbsp" : gisType;
															%>
															<%=Util.replaceTags(gisType)%><%
														}
														else{
															String selected = gisType==null ? "selected=\"selected\"" : "";
															%>
															<select name="gis">
																<option value="nogis" <%=selected%>>-- no GIS element --</option><%
																Vector gisTypes = DataElement.getGisTypes();
																for (int i=0; i<gisTypes.size(); i++){
																	String gist = (String)gisTypes.get(i);
																	selected = gisType!=null && gist.equals(gisType) ? "selected=\"selected\"" : "";
																	String gisDisp = gist.equals("") ? "[ ]" : gist;
																	%>
																	<option <%=selected%> value="<%=Util.replaceTags(gist)%>"><%=Util.replaceTags(gisDisp)%></option><%
																} %>
															</select><%
														}
														%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}
								    		%>
								    		
								    		<!-- Reference URL -->
								    		<%
								    		String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
								    		if (mode.equals("view") && jspUrlPrefix!=null){
									    		String refUrl = jspUrlPrefix + "data_element.jsp?mode=view&amp;delem_idf=" + dataElement.getIdentifier();
									    		if (dataElement.getNamespace()!=null && dataElement.getNamespace().getID()!=null)
									    			refUrl = refUrl + "&amp;pns=" + dataElement.getNamespace().getID();
									    		%>
									    		<tr class="zebra<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title">
														Reference URL
													</td>
													<td width="4%" class="simple_attr_help">
														<a target="_blank" href="help.jsp?screen=dataset&amp;area=refurl" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
														</a>
													</td>
													<td width="<%=valueWidth%>%" class="simple_attr_value">
														<span class="barfont"><a target="_blank" href="<%=refUrl%>"><%=refUrl%></a></span>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
								    		}
								    		
								    		String elmDataType = "string";
								    		if (mode.equals("add")){
									    		String _elmDataType = request.getParameter("elm_datatype");
									    		if (_elmDataType!=null && _elmDataType.length()>0) elmDataType = _elmDataType;
								    		}
								    		else{
									    		String _elmDataType = dataElement==null ? null : dataElement.getAttributeValueByShortName("Datatype");
									    		if (_elmDataType!=null && _elmDataType.length()>0) elmDataType = _elmDataType;
								    		}
								    		%>
								    		
								    		<!-- dynamic attributes -->
								    		
								    		<%
								    		boolean imagesQuicklinkSet = false;
								    		boolean isBoolean = false;
											for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
												
												attribute = (DElemAttribute)mAttributes.get(i);
												String dispType = attribute.getDisplayType();
												if (dispType == null)
													continue;
												
												boolean dispFor = type==null ? attribute.displayFor("CH2") : attribute.displayFor(type);
												
												if (!dispFor)
													continue;
												
												if (Util.skipAttributeByDatatype(attribute.getShortName(), elmDataType)) continue;
												
												attrID = attribute.getID();
												
												if (attribute.getShortName().equalsIgnoreCase("Datatype"))
													attrValue = elmDataType;
												else
													attrValue = getValue(attrID, mode, dataElement, newDataElement);
													
												String attrOblig = attribute.getObligation();
												String obligImg  = "optional.gif";
												if (attrOblig.equalsIgnoreCase("M"))
													obligImg = "mandatory.gif";
												else if (attrOblig.equalsIgnoreCase("C"))
													obligImg = "conditional.gif";
												
												// set isBoolean if the element is of boolean datatype
												if (attribute.getShortName().equalsIgnoreCase("Datatype")){
													if (attrValue!=null && attrValue.equalsIgnoreCase("boolean")) isBoolean = true;
												}
												
												// if element is of CH1 type, don't display MinSize and MaxSize
												if (attribute.getShortName().equalsIgnoreCase("MaxSize") || attribute.getShortName().equalsIgnoreCase("MinSize"))
													if (type!=null && type.equalsIgnoreCase("CH1"))
														continue;
												
												if (mode.equals("view") && (attrValue==null || attrValue.length()==0))
													continue;
												
												// if image attribute and no reason to display then skip
												if (dispType.equals("image")){
													if (mode.equals("add") || (mode.equals("edit") && user==null) || (mode.equals("view") && Util.voidStr(attrValue)))
														continue;
												}
												
												//displayed++; - done below
												
												String width  = attribute.getDisplayWidth();
												String height = attribute.getDisplayHeight();
												
												String disabled = user == null ? "disabled" : "";
												boolean dispMultiple = attribute.getDisplayMultiple().equals("1") ? true:false;
												boolean inherit = attribute.getInheritable().equals("0") || elmCommon ? false : true;
												
												Vector multiValues=null;
												String inheritedValue=null;
								
												if (!mode.equals("view")){
													if (inherit){
														inheritedValue = getValue(attrID, 2, mode, dataElement, newDataElement);
													}
														
													if (mode.equals("edit")){
														if (dispMultiple){
															if (inherit){
																multiValues = getValues(attrID, 1, mode, dataElement, newDataElement); //original values only
															}
															else{
																multiValues = getValues(attrID, 0, mode, dataElement, newDataElement);  //all values
															}
														}
														else{
															if (inherit) attrValue = getValue(attrID, 1, mode, dataElement, newDataElement);  //get original value
														}
													}
												}
				
												%>
								    			<tr class="zebra<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title">
														<%=Util.replaceTags(attribute.getShortName())%>
													</td>
													<td width="4%" class="simple_attr_help">
														<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help">
															<img border="0" src="images/<%=Util.replaceTags(obligImg)%>" width="16" height="16" alt=""/>
														</td><%
													}
													%>
													
													<!-- dynamic attribute value display -->
													
													<td width="<%=valueWidth%>%" class="simple_attr_value"><%
													
														// handle image attribute first
														if (dispType.equals("image")){
															if (!imagesQuicklinkSet){ %>
																<a name="images"></a><%
																imagesQuicklinkSet = true;
															}
															// thumbnail
															if (mode.equals("view") && !Util.voidStr(attrValue)){ %>
																<a target="_blank" href="visuals/<%=Util.replaceTags(attrValue)%>" onFocus="blur()" onclick="pop(this.href)">
																	<img src="visuals/<%=Util.replaceTags(attrValue)%>" border="0" height="100" width="100" alt=""/>
																</a><br/><%
															}
															// link
															if (mode.equals("edit") && user!=null){
																String actionText = Util.voidStr(attrValue) ? "add image" : "manage this image";
																%>
																<span class="barfont">
																	[Click <a target="_blank" onclick="pop(this.href)" href="imgattr.jsp?obj_id=<%=delem_id%>&amp;obj_type=E&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=Util.replaceTags(dataElement.getShortName())%>&amp;attr_name=<%=Util.replaceTags(attribute.getShortName())%>"><b>HERE</b></a> to <%=Util.replaceTags(actionText)%>]
																</span><%
															}
														}
														// if view mode, display simple text
														else if (mode.equals("view") || (mode.equals("edit") && attribute.getShortName().equalsIgnoreCase("Datatype"))){ %>
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
																		<%=Util.replaceTags(sInhText)%><%=Util.replaceTags(inheritedValue)%><br/><%
																}
															}
															
															// mutliple display
															if (dispMultiple && !dispType.equals("image")){ %>
															
																<select <%=disabled%> id="attr_mult_<%=attrID%>" name="attr_mult_<%=attrID%>" multiple="multiple" style="width:auto"><%
																	for (int k=0; multiValues!=null && k<multiValues.size(); k++){
																		attrValue = (String)multiValues.get(k);
																		%>
																		<option value="<%=Util.replaceTags(attrValue)%>"><%=Util.replaceTags(attrValue)%></option><%
																	}
																	%>
																</select>
																
																<%
																if (disabled.equals("")){ %>
																	<a href="javascript:rmvValue('<%=attrID%>')"><img src="images/button_remove.gif" border="0" title="Click here to remove selected value" alt=""/></a>
																	<a href="javascript:openAddBox('<%=attrID%>', 'dispType=<%=Util.replaceTags(dispType)%>&amp;width=<%=width%>')"><img src="images/button_plus.gif" border="0" title="Click here to add a new value" alt=""/></a><%
																}
																
																if (dispType.equals("select")){ %>							
																	<select class="small" name="hidden_attr_<%=attrID%>" style="display:none">
																		<%
																		Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
																		if (fxValues==null || fxValues.size()==0){ %>
																			<option selected="selected" value=""></option> <%
																		}
																		else{
																			for (int g=0; g<fxValues.size(); g++){
																				FixedValue fxValue = (FixedValue)fxValues.get(g);
																				%>
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
															
															// no multiple display
															else{
																if (dispType.equals("text")){
																	if (attrValue!=null){
																		%>
																		<input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<% if (dispMultiple)%> mult_<%;%><%=attrID%>" value="<%=Util.replaceTags(attrValue)%>" onchange="form_changed('form1')"/>
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
																else if (dispType.equals("select")){
																	String onchange = "";
																	if (attribute.getShortName().equalsIgnoreCase("Datatype"))
																		onchange = " onchange=\"changeDatatype()\"";
																	else
																		onchange = " onchange=\"form_changed('form1')\"";
																	
																	%>							
																	<select <%=disabled%> class="small" name="attr_<%=attrID%>"<%=onchange%>>
																		<%
																		Vector fxValues = searchEngine.getFixedValues(attrID, "attr");
																		if (fxValues==null || fxValues.size()==0){ %>
																			<option selected="selected" value=""></option> <%
																		}
																		else{
																			boolean selectedByValue = false;
																			for (int g=0; g<fxValues.size(); g++){
																				FixedValue fxValue = (FixedValue)fxValues.get(g);
																				
																				String isSelected = (fxValue.getDefault() && !selectedByValue) ? "selected=\"selected\"" : "";
																				
																				if (attribute.getShortName().equalsIgnoreCase("Datatype")){
																					if (type!=null &&
																						type.equals("CH2") &&
																						fxValue.getValue().equalsIgnoreCase("boolean"))
																						continue;
																				}
									
																				if (attrValue!=null && attrValue.equals(fxValue.getValue())){
																					isSelected = "selected=\"selected\"";
																					selectedByValue = true;
																				}
																				
																				%>
																				<option <%=isSelected%> value="<%=Util.replaceTags(fxValue.getValue())%>"><%=Util.replaceTags(fxValue.getValue())%></option> <%
																			}
																		}
																		%>
																	</select>
																	<a target="_blank" onclick="pop(this.href)" href="fixed_values.jsp?mode=view&amp;delem_id=<%=attrID%>&amp;delem_name=<%=Util.replaceTags(attribute.getShortName())%>&amp;parent_type=attr">
																		<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
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
								    		<!-- end dynamic attributes -->
								    		
								    		<!-- IsRodParam -->
								    		<%
								    		boolean isRodParam = mode.equals("add") ? true : dataElement.isRodParameter();
								    		%>
								    		<tr class="zebra<%=isOdd%>">
												<td width="<%=titleWidth%>%" class="simple_attr_title">
													Is ROD parameter
												</td>
												<td width="4%" class="simple_attr_help">
													<a target="_blank" href="help.jsp?screen=element&amp;area=is_rod_param" onclick="pop(this.href)">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td width="4%" class="simple_attr_help">
														<img border="0" src="images/optional.gif" width="16" height="16" alt=""/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="simple_attr_value">
													<%
													if (mode.equals("view")){ %>
														<%=isRodParam%><%
													}
													else{
														boolean[] options = {true, false};
														%>
														<select name="is_rod_param" onchange="form_changed('form1')">
															<%
															for (int ii=0; ii<options.length; ii++){
																String selected = isRodParam==options[ii] ? "selected=\"selected\"" : "";
																%>
																<option <%=selected%> value="<%=options[ii]%>"><%=options[ii]%></option><%
															}
															%>
														</select><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- version (or the so-called CheckInNo), relevant for common elements only -->
											<%											
											if (verMan==null) verMan = new VersionManager();
											
								    		if (elmCommon && !mode.equals("add")){
												String elmVersion = dataElement.getVersion();
												%>												
									    		<tr class="zebra<%=isOdd%>">
													<td width="<%=titleWidth%>%" class="simple_attr_title">
														CheckInNo
													</td>
													<td width="4%" class="simple_attr_help">
														<a target="_blank" href="help.jsp?screen=dataset&amp;area=check_in_no" onclick="pop(this.href)">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help">
															<img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/>
														</td><%
													}
													%>
													<td width="<%=valueWidth%>%" class="simple_attr_value">
														<%=elmVersion%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr>
									    		<%
								    		}
								    		%>
								    		
								    		<!-- Identifier -->
								    		
								    		<tr class="zebra<%=isOdd%>">
												<td width="<%=titleWidth%>%" class="simple_attr_title">
													Identifier
												</td>
												<td width="4%" class="simple_attr_help">
													<a target="_blank" href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href)">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td width="4%" class="simple_attr_help">
														<img border="0" src="images/mandatory.gif" width="16" height="16" alt=""/>
													</td><%
												}
												%>
												<td width="<%=valueWidth%>%" class="simple_attr_value">
													<%
													if(!mode.equals("add")){ %>
														<b><%=Util.replaceTags(idfier)%></b>
														<input type="hidden" name="idfier" value="<%=Util.replaceTags(idfier,true)%>"/><%
													}
													else{ %>
														<input class="smalltext" type="text" size="30" name="idfier" onchange="form_changed('form1')" value="<%=Util.replaceTags(idfier)%>"/><%
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
										
										<!-- allowable/suggested values -->
										
										<%										
										boolean key = (mode.equals("edit") && user!=null) || (mode.equals("view") && fixedValues!=null && fixedValues.size()>0);
										if (type!=null && !isBoolean && key){
																						
											String title = type.equals("CH1") ? "Allowable values" : "Suggested values";
											String helpAreaName = type.equals("CH1") ? "allowable_values_link" : "suggested_values_link";
											%>
										
											
												<!-- title & link part -->
												<h2>
														<%=title%><a name="values"></a>
													
													<%
													if (!mode.equals("view")){
														%>
														<span class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=element&amp;area=<%=Util.replaceTags(helpAreaName)%>" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img border="0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</span><%
													}
													
													// the link
													String valuesLink = "fixed_values.jsp?delem_id=" + delem_id + "&amp;delem_name=" + delem_name + "&amp;mode=view&amp;parent_type=" + type;
													if (mode.equals("edit") && user!=null){
														%>
														<span class="barfont_bordered">
															[Click <a href="<%=valuesLink%>"><b>HERE</b></a> to manage <%=Util.replaceTags(title.toLowerCase())%> of this element]
														</span><%
													}
													%>
												</h2>
												
												<!-- table part -->
												<%
												if (mode.equals("view") && fixedValues!=null && fixedValues.size()>0){%>
															<table class="datatable3">
																<tr>
																	<th width="20%">Value</th>
																	<th width="40%">Definition</th>
																	<th width="40%">ShortDescription</th>																	
																</tr>
																<%
																// rows
																for (int i=0; i<fixedValues.size() && i<MAX_DISP_VALUES+1; i++){
																	
																	FixedValue fxv = null;
																	String value = "";
																	String fxvID = "";
																	String defin = "";
																	String shortDesc = "";
																	String valueLink = "";
																	
																	if (i==MAX_DISP_VALUES){
																		value = ". . .";
																		fxvID = "";
																		defin = ". . .";
																		shortDesc = ". . .";
																		valueLink = valuesLink;
																	}
																	else{
																		fxv = (FixedValue)fixedValues.get(i);
																		value = fxv.getValue();
																		fxvID = fxv.getID();
																		defin = fxv.getDefinition();
																		shortDesc = fxv.getShortDesc();
																		valueLink = "fixed_value.jsp?fxv_id=" + fxvID + "&amp;mode=" + mode + "&amp;delem_id=" + delem_id + "&amp;delem_name=" + delem_name + "&amp;parent_type=" + type;
																	}
																	
																	if (shortDesc.length()==0) shortDesc = "&nbsp;";
																	if (defin.length()==0) defin = "&nbsp;";
																	
																	defin = defin==null ? "" : defin;
																	String dispDefin = defin.length()>MAX_CELL_LEN ?
																		defin.substring(0,MAX_CELL_LEN) + "..." : defin;
										
																	shortDesc = shortDesc==null ? "" : shortDesc;
																	String dispShortDesc = shortDesc.length()>MAX_CELL_LEN ?
																		shortDesc.substring(0,MAX_CELL_LEN) + "..." : shortDesc;
																	
																	%>
																	<tr>
																		<td width="20%">
																			<a href="<%=valueLink%>">
																				<%=Util.replaceTags(value)%>
																			</a>
																		</td>
																		<td width="40%" title="<%=Util.replaceTags(defin,true)%>">
																			<%=Util.replaceTags(dispDefin)%>
																		</td>
																		<td width="40%" title="<%=Util.replaceTags(shortDesc,true)%>">
																			<%=Util.replaceTags(dispShortDesc)%>
																		</td>																		
																	</tr><%
																}
																%>
															</table>
													<%
												}
										}
										%>
										
										
										<!-- foreign key relations, relevant for non-common elements only -->
										
										<%
										if (!elmCommon &&
											((mode.equals("edit") && user!=null) || (mode.equals("view") && fKeys!=null && fKeys.size()>0))){

											
											%>										
												<!-- title & link part -->
												<h2>
														Foreign key relations<a name="fkeys"></a>
													
													<%
													if (!mode.equals("view")){
														%>
														<span class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=element&amp;area=fks_link" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img border="0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</span><%
													}
													
													// the link
													if (mode.equals("edit") && user!=null){
														String origID = verMan.getLatestElmID(dataElement);
														if (origID!=null && origID.length()>0) origID = "&orig_id=" + origID;
														%>
														<span class="barfont_bordered">
															[Click <a href="foreign_keys.jsp?delem_id=<%=delem_id%>&amp;delem_name=<%=Util.replaceTags(delem_name)%>&amp;ds_id=<%=dsID%><%=origID%>"><b>HERE</b></a> to manage foreign keys of this element]
														</span><%
													}
													%>
												</h2>
												
												<!-- table part -->
												<%												
												if (mode.equals("view") && fKeys!=null && fKeys.size()>0){%>
															<table class="datatable3">
																<tr>
																	<th width="50%">Element</th>
																	<th width="50%">Table</th>
																</tr>
																<%
																// rows
																for (int i=0; i<fKeys.size(); i++){
																	
																	Hashtable fkRel  = (Hashtable)fKeys.get(i);
																	String fkElmID   = (String)fkRel.get("elm_id");
																	String fkElmName = (String)fkRel.get("elm_name");
																	String fkTblName = (String)fkRel.get("tbl_name");
																	String fkRelID   = (String)fkRel.get("rel_id");
																	
																	if (fkElmID==null || fkElmID.length()==0)
																		continue;
																		
																	%>
																	<tr>
																		<td width="50%">
																			<a href="data_element.jsp?delem_id=<%=fkElmID%>&amp;mode=view">
																				<%=Util.replaceTags(fkElmName)%>
																			</a>
																		</td>
																		<td width="50%">
																			<%=Util.replaceTags(fkTblName)%>
																		</td>
																	</tr><%
																}
																%>
															</table>
													<%
												}
										}
										%>
										
										<!-- referring tables , relevant for common elements only -->
										
										<%
										if (elmCommon && mode.equals("view") && refTables!=null && refTables.size()>0){

											
											%>										
											
												<!-- title part -->
												<h2>
														Tables using this common element<a name="fkeys"></a>
												</h2>
												
												<!-- table part -->
														<table class="datatable3">
															<tr>
																<th width="43%">Table</th>
																<th width="43%">Dataset</th>
																<th width="14%">Owner</th>
															</tr>
															<%
															// rows
															for (int i=0; i<refTables.size(); i++){
																
																DsTable tbl = (DsTable)refTables.get(i);
																String tblLink = "";
																String dstLink = "";
																if (latestRequested){
																	tblLink = "dstable.jsp?mode=view&amp;table_idf=" + tbl.getIdentifier() + "&amp;pns=" + tbl.getParentNs();
																	dstLink = "dataset.jsp?mode=view&amp;ds_idf=" + tbl.getDstIdentifier();
																}
																else{
																	tblLink = "dstable.jsp?mode=view&amp;table_id=" + tbl.getID();
																	dstLink = "dataset.jsp?mode=view&amp;ds_id=" + tbl.getDatasetID();
																}
																
																String owner = tbl.getOwner();
																owner = owner==null ? "Not specified" : owner;
																	
																%>
																<tr>
																	<td width="43%">
																		<a target="_blank" href="<%=tblLink%>">
																			<%=Util.replaceTags(tbl.getShortName())%>
																		</a>
																	</td>
																	<td width="43%">
																		<a target="_blank" href="<%=dstLink%>">
																			<%=Util.replaceTags(tbl.getDatasetName())%>
																		</a>
																	</td>
																	<td width="14%">
																		<%=Util.replaceTags(owner)%>
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

											
												<h2>
														Complex attributes<a name="cattrs"></a>
													
													<%
													if (!mode.equals("view")){
														%>
														<span class="simple_attr_help">
															<a target="_blank" href="help.jsp?screen=dataset&amp;area=complex_attrs_link" onclick="pop(this.href)">
																<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img border="0" src="images/mandatory.gif" width="16" height="16" alt="mandatory"/>
														</span><%
													}
													
													// the link
													if (mode.equals("edit") && user!=null){ %>
														<span class="barfont_bordered">
															[Click <a target="_blank" onclick="pop(this.href)" href="complex_attrs.jsp?parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=Util.replaceTags(delem_name)%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>"><b>HERE</b></a> to manage complex attributes of this element]
														</span><%
													}
													%>
												</h2>
												
												<%
												// the table
												if (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0){
													%>
															<table class="datatable4">
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
																		<td width="29%">
																			<a target="_blank" onclick="pop(this.href)" href="complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=Util.replaceTags(delem_name)%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>" title="Click here to view all the fields">
																				<%=Util.replaceTags(attrName)%>
																			</a>
																		</td>
																		<td width="4%">
																			<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX" onclick="pop(this.href)">
																				<img border="0" src="images/icon_questionmark.jpg" width="16" height="16" alt="help"/>
																			</a>
																		</td>
																		<td width="63%">
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
											<%
										}
										%>
										<!-- end complex attributes -->
								        
								<!-- end dotted -->
								
						</div>
					
					<!-- end main table body -->
					
				<!-- end main table -->
				
				<%
				if (type!=null){ %>
					<input type="hidden" name="type" value="<%=type%>"/> <%
				}
				%>
				<input type="hidden" name="mode" value="<%=mode%>"/>
				<input type="hidden" name="pick" value="false"/>
				<input type="hidden" name="check_in" value="false"/>
				<input type="hidden" name="ns" value="1"/>
				<input type="hidden" name="copy_elem_id" value=""/>
				<input type="hidden" name="changed" value="0"/>
				
				<%
				
				String latestID = dataElement==null ? null : verMan.getLatestElmID(dataElement);
				if (latestID!=null){%>
					<input type="hidden" name="latest_id" value="<%=latestID%>"/><%
				}
				
				String dsidf = dataset==null ? null : dataset.getIdentifier();
				if (dsidf==null && dst!=null)
					dsidf = dst.getIdentifier();
				
				if (dsidf!=null){%>
					<input type="hidden" name="ds_idf" value="<%=dsidf%>"/><%
				}
				
				// for deletion from view mode we need upd_version parameter
				if (mode.equals("view")){ %>
					<input type="hidden" name="upd_version" value="false"/><%
				}
				
				if (elmCommon){ %>
					<input type="hidden" name="common" value="true"/><%
				}
				%>
				
			</form>
			</div>
			
			<%
			if (!popup){ %>
				<jsp:include page="footer.jsp" flush="true">
				</jsp:include><%
			}
			%>
			
			</div>

</body>
</html>

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {
	}
}
%>
