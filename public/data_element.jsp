<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,java.io.*"%>

<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private DataElement dataElement=null;%>
<%!private DataElement newDataElement=null;%>
<%!private Vector complexAttrs=null;%>
<%!private Vector fixedValues=null;%>
<%!private static final int MAX_CELL_LEN=40;%>
<%!private static final int MAX_ATTR_LEN=500;%>
<%!private static final int MAX_DISP_VALUES=30;%>


<%!

private DElemAttribute getAttributeByName(String name){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr;
	}
        
	    return null;
}

private String getAttributeIdByName(String name){
	
	for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        //if (attr.getName().equalsIgnoreCase(name))
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}

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

private String getAttributeObligationById(String id){
	
	for (int i=0; i<mAttributes.size(); i++){
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

			
			XDBApplication.getInstance(getServletContext());
			AppUserIF user = SecurityUtil.getUser(request);
			
			if (user!=null){ //if user wants has logged in, we disable caching
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Cache-Control", "no-cache");
				response.setDateHeader("Expires", 0);
				response.setHeader("Cache-Control", "no-store");
			}
	
			%><%@ include file="history.jsp" %><%

			ServletContext ctx = getServletContext();			
			String appName = ctx.getInitParameter("application-name");
		    String urlPath = ctx.getInitParameter("basens-path");
			if (urlPath == null) urlPath = "";
			
			/*DDuser user = new DDuser(DBPool.getPool(appName));
	
			String username = "root";
			String password = "ABr00t";
			boolean f = user.authenticate(username, password);*/
			
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
			
			String delem_id = request.getParameter("delem_id");
			
			mode = request.getParameter("mode");
			if (mode == null || mode.length()==0) { %>
				<b>Mode paramater is missing!</b>
				<%
				return;
			}
			
			if (!mode.equals("add")&& !mode.equals("copy") && (delem_id == null || delem_id.length()==0)){ %>
				<b>Data element ID is missing!</b> <%
				return;
			}
			
			if (mode.equals("add"))
				dataElement = null;
			
			String dsID = request.getParameter("ds_id");
			String tableID = request.getParameter("table_id");
			
			boolean editPrm = false;
			boolean deletePrm = false;
			
			if (mode.equals("add") && dsID==null){
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
			
			String contextParam = request.getParameter("ctx");
			if (contextParam == null) contextParam = "";
			
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
						
						String dsidf = request.getParameter("ds_idf");
						if (dsidf==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsidf, "u")){%>
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
						if (id != null && id.length()!=0)
							redirUrl = redirUrl + "data_element.jsp?mode=edit&delem_id=" + id;
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
						
						/* if this was check in & new version was created , send to "view" mode
						String checkIn = request.getParameter("check_in");
			        	if (checkIn!=null && checkIn.equalsIgnoreCase("true"))
				        	redirUrl = redirUrl + "data_element.jsp?mode=view";
				        else
				        	redirUrl = redirUrl + "data_element.jsp?mode=edit";
			        	
						// EK - I think it should be here
						if (delem_id!=null) redirUrl = redirUrl + "&delem_id=" + delem_id;
						if (type!=null) redirUrl = redirUrl + "&type=" + type;
						
						if (dsID != null) redirUrl = redirUrl + "&ds_id=" + dsID;
						if (tableID != null) redirUrl = redirUrl + "&table_id=" + tableID;
						if (contextParam != null) redirUrl = redirUrl + "&ctx=" + contextParam;*/
			/*- EK*/
					}
					else if (mode.equals("delete") && !wasPick){
						
						String lid = request.getParameter("latest_id");
						String newTblID = handler.getNewTblID();
						if (!Util.voidStr(newTblID)){
							redirUrl = redirUrl + "dstable.jsp?mode=view&table_id=" + newTblID;
							
						}
						else if (!Util.voidStr(lid)){
							redirUrl = redirUrl + "data_element.jsp?mode=view&delem_id=" + lid;							
						}
						else{
							String	deleteUrl = history.gotoLastNotMatching("data_element.jsp");
							redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ?
										deleteUrl :
										redirUrl + "index.jsp";
						}
					}
					else if (wasPick){
						redirUrl = redirUrl + "data_element.jsp?&mode=" + mode;
						
						if (delem_id!=null) redirUrl = redirUrl + "&delem_id=" + delem_id;
						if (type!=null) redirUrl = redirUrl + "&type=" + type;
						
						if (dsID != null) redirUrl = redirUrl + "&ds_id=" + dsID;
						if (tableID != null) redirUrl = redirUrl + "&table_id=" + tableID;
					}
					else if (mode.equals("restore")){
						
						String restoredID = handler.getRestoredID();
						if (restoredID!=null)
							redirUrl = redirUrl + "data_element.jsp?mode=view&delem_id=" + restoredID;
						else{
							String	deleteUrl = history.gotoLastNotMatching("data_element.jsp");
							redirUrl = (deleteUrl!=null&&deleteUrl.length()>0) ?
										deleteUrl :
										redirUrl + "index.jsp";
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
			
			Connection conn = null;
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
			
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
			

			// find out if it's the latest version of this data element			
			VersionManager verMan = new VersionManager(conn, searchEngine, user);
			String latestID = dataElement==null ? null : verMan.getLatestElmID(dataElement);
			boolean isLatest = Util.voidStr(latestID) ? true : latestID.equals(dataElement.getID());
			
			// implementing check-in/check-out
			
			String workingUser = null;
			if (dataElement!=null)
				workingUser = verMan.getWorkingUser(dataElement.getNamespace().getID(),
			    											dataElement.getIdentifier(), "elm");
			if (mode.equals("edit") && user!=null && user.isAuthentic()){
				// see if element is checked out
				if (Util.voidStr(workingUser)){
				    // element not checked out, create working copy
				    // but first make sure it's the latest version
				    if (!isLatest){ %>
				    	<b>Trying to check out a version that is not the latest!</b><%
				    	return;
				    }
				    
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
			
		    // some business with the contextParam
			if (!contextParam.startsWith("ds") && dsID==null && tableID==null && dataElement!=null)
				tableID = dataElement.getTableID();
			
			// get the dataset and table
			Dataset dataset = null;
			DsTable dsTable = null;
			
			if (dataElement!=null){
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
			
			editPrm = user!=null &&
					  dataset!=null &&
					  SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
			deletePrm = editPrm;
			
			/*if (tableID != null && tableID.length()!=0){
				dsTable = searchEngine.getDatasetTable(tableID);
				if (dsTable != null)
					dsID = dsTable.getDatasetID();
			}
			
			if (dsID != null && dsID.length()!=0){
				dataset = searchEngine.getDataset(dsID);
			}*/
			
			if (contextParam.startsWith("ds") && (dataset==null || dsTable==null)){ %>
				<b>Dataset and table were not found!</b> <%
				return;
			}
			
			// find out if the elem's latest table is also the latest such table
			if (isLatest){
				if (dsTable!=null){
					String latestTblId = verMan.getLatestTblID(dsTable);
					if (latestTblId!=null && !latestTblId.equals(dsTable.getID()))
						isLatest = false;
				}
			}
			
			// find out if the elem's latest dataset is also the latest such dataset
			if (isLatest){
				if (dataset!=null){
					String latestDstId = verMan.getLatestDstID(dataset);
					if (latestDstId!=null && !latestDstId.equals(dataset.getID()))
						isLatest = false;
				}
			}
			
			// get the complex attributes
			complexAttrs = searchEngine.getComplexAttributes(delem_id, "E", null, tableID, dsID);
			if (complexAttrs == null) complexAttrs = new Vector();
			
			DElemAttribute attribute = null;
			
			// set a flag if element has history
			boolean hasHistory = false;
			if (mode.equals("edit") && dataElement!=null){
				Vector v = searchEngine.getElmHistory(dataElement.getIdentifier(),
													  dataElement.getNamespace().getID(),
													  dataElement.getVersion() + 1);
				if (v!=null && v.size()>0)
					hasHistory = true;
			}
			
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript" src='modal_dialog.js'></script>
    <script language="JavaScript">
    
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
			
			<%
			/*
			if (hasHistory){
			%>
				openDialog("yesno_dialog.html", "Do you want to increment the element's internal version?",
						   retVersionUpd,100, 400);
				return; <%
			}
			else{
				*/
			%>
				if (document.forms["form1"].elements["is_first"]){
					if (document.forms["form1"].elements["is_first"].value=="true"){
						openDialog("yesno_dialog.html",
								   "Do you want to update parent table and dataset versions?",
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
			submitForm('edit');
		}
		
		function retVersionUpd(){
			var v = dialogWin.returnValue;
			if (v==null) v=true;
			document.forms["form1"].elements["upd_version"].value = v;
			
			if (document.forms["form1"].elements["is_first"]){
				if (document.forms["form1"].elements["is_first"].value=="true"){
					openDialog("yesno_dialog.html",
							   "Do you want to update parent table and dataset versions?",
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
		
		function submitForm(mode){
			
			if (mode=="add"){
				
				forceAttrMaxLen();
				
				var ds = document.forms["form1"].elements["ds_id"].value;
				if (ds==null || ds==""){
					alert('Dataset not specified!');
					return;
				}
				
				var tbl = document.forms["form1"].elements["table_id"].value;
				if (tbl==null || tbl==""){
					alert('Table not specified!');
					return;
				}
			}
			
			if (mode == "delete"){
				var b;
				<%
				if (!mode.equals("add") && dataElement.isWorkingCopy()){ %>
					b = confirm("This working copy will be deleted and the corresponding element released for others to edit! Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				else{ %>
					b = confirm("This data element's latest version will be deleted! This will also result in updating the version" +
								"of a table where this element might belong to. Click OK, if you want to continue. Otherwise click Cancel.");<%
				}
				%>
				if (b==false) return;
			}
			
			if (mode != "delete"){
				
				forceAttrMaxLen();
				
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return;
				}
				
				if (hasWhiteSpace("idfier")){
					alert("Identifier cannot contain any white space!");
					return;
				}
			}
			
			slctAllValues();
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function checkObligations(){
			
			var o = document.forms["form1"].delem_name;
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
		
		function subelems(uri){
			uri = uri + "&open=true";
			wSubElems = window.open(uri,"SubElements","height=700,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wSubElems.focus()}
		}

		function ch1values(url){
					wCh1Values = window.open(url,"AllowableValues","height=600,width=800,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
					if (window.focus) {wCh1Values.focus()}
		}
		
		function complexAttrs(url){
					wComplexAttrs = window.open(url,"ComplexAttributes","height=500,width=500,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
					if (window.focus) {wComplexAttrs.focus()}
		}
		function complexAttr(url){
					wComplexAttrs = window.open(url,"ComplexAttribute","height=600,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
					if (window.focus) {wComplexAttrs.focus()}
		}
		
		function printable(url){
					wPrintablePage = window.open(url + "&open=true","PrintablePage","height=600,width=700,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=no");
					if (window.focus) {wPrintablePage.focus()}
		}
		
		function openDatasetPick(){
	    	wPick = window.open("dspick.jsp","PickDataset",'height=400,width=400,status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes');
    	}
    	
    	//function setPickedDataset(dsName, dsID){
	    //	document.forms["form1"].ds_name.value=dsName;
	    //	document.forms["form1"].ds_id.value=dsID;
    	//}
    	
    	function edit(){
	    	<%
	    	String modeString = new String("mode=view&");
	    	String qryStr = request.getQueryString();
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
			%>
		}
		
		<%
		if (dataElement!=null){%>
			function viewHistory(){
				var url = "elm_history.jsp?id=<%=dataElement.getID()%>";
				window.open(url,null,"height=400,width=400,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=yes");
			}<%
		}
		%>			
		
		function fixType(){
			
			var type = document.forms["form1"].typeSelect.value;
			if (type == null || type.length==0)
				return;
				
			document.location.assign("data_element.jsp?mode=add&type=" + type);
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
			
			if (document.forms["form1"].elements["idfier"].value==""){
				alert("Identifier cannot be empty!");
				return;
			}
			var url='search.jsp?ctx=popup';
			var type;
			if (document.forms["form1"].elements["type"]){
				type = document.forms["form1"].elements["type"].value;
				url += "&type=" + type;
			}
		
			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
			if (window.focus) {wAdd.focus()}
		}
		function pickElem(id, name){
			//alert(id);
			document.forms["form1"].elements["copy_elem_id"].value=id;
			document.forms["form1"].elements["mode"].value = "copy";
			document.forms["form1"].submit();

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
			var b = confirm("This version of the data element will now become the new latest version. " +
							"Also, the versions of the parent table and dataset will be updated as well. " +
							"Click OK, if you want to continue. Otherwise click Cancel.");
			if (b==false) return;
	    	document.forms["form1"].elements["mode"].value = "restore";
       		document.forms["form1"].submit();
    	}
	</script>
</head>

<%
			
String attrID = null;
String attrValue = null;
%>
			
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
                <jsp:param name="name" value="Data element"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
		        
			<div style="margin-left:30">
		        
			<form name="form1" id="form1" method="POST" action="data_element.jsp">
			
				<%
				if (!mode.equals("add")){ %>
					<input type="hidden" name="delem_id" value="<%=delem_id%>"/><%
				}
				else { %>
					<input type="hidden" name="dummy"/><%
				}
				
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
							String hlpScreen = "element";
							if (mode.equals("edit"))
								hlpScreen = "element_edit";
							else if (mode.equals("add"))
								hlpScreen = "element_add";
							%>
							<a target="_blank" href="help.jsp?screen=<%=hlpScreen%>&area=pagehelp"><img src="images/pagehelp.jpg" border=0 alt="Get some help on this page" /></a>
						</td>
					</tr>
	                <tr>
						<td width="72%" height="40" class="head1">
							<%=verb%> element definition
						</td>
						<td width="28%" height="40" align="right">
							<%
							// the buttons displayed in view mode
							if (mode.equals("view") && dataElement!=null){
								if (user!=null){
									
									// set the flag indicating if the top namespace is in use
									String topWorkingUser = verMan.getWorkingUser(dataElement.getTopNs());
									boolean topFree = topWorkingUser==null ? true : false;
									
									boolean isDeleted = searchEngine.isElmDeleted(dataElement.getID());
									if (isDeleted && topFree && deletePrm){ %>
										<input type="button" class="smallbutton" value="Restore" onclick="restore()"/>&#160;<%
									}
									
									boolean inWorkByMe = workingUser==null ?
												 false :
												 workingUser.equals(user.getUserName());
									
									/*
									System.out.println("editPrm = " + editPrm);
									System.out.println("dataElement.isWorkingCopy() = " + dataElement.isWorkingCopy());
									System.out.println("isLatest = " + isLatest);
									System.out.println("topFree = " + topFree);
									System.out.println("inWorkByMe = " + inWorkByMe);*/
									
									if (editPrm){
										if (dataElement.isWorkingCopy() ||
											(isLatest && topFree)       ||
											(isLatest && inWorkByMe)){ %>
											<input type="button" class="smallbutton" value="Edit" onclick="edit()"/>&#160;<%
										}
									}
									
									if (deletePrm){
										if (!dataElement.isWorkingCopy() && isLatest && topFree){ %>
											<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/><%
										}
									}
								}
								%>
								<input type="button" class="smallbutton" value="History" onclick="viewHistory()"/><%
							}
							// the working copy part
							else if (dataElement!=null && dataElement.isWorkingCopy()){ %>
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
						if (mode.equals("edit") && dataElement!=null && dataElement.isWorkingCopy() && user!=null && hasHistory){%>
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
		                    	fixedValues = mode.equals("add") ? null : searchEngine.getFixedValues(delem_id, "elem");
		                    	Vector fKeys = mode.equals("add") ? null : searchEngine.getFKRelationsElm(delem_id, dataset.getID());
		                    	Vector relElems = mode.equals("add") ? null : searchEngine.getRelatedElements(delem_id, "elem");
		                    	
		                    	if (mode.equals("view")){
			                    	Vector quicklinks = new Vector();
			                    	
			                    	if (fixedValues!=null && fixedValues.size()>0)
			                    		quicklinks.add("Allowable values | values");
			                    	if (fKeys!=null && fKeys.size()>0)
			                    		quicklinks.add("Foreign key relations | fkeys");
			                    	//if (relElems!=null && relElems.size()>0) quicklinks.add("Related elements | rels");
			                    	if (complexAttrs!=null && complexAttrs.size()>0)
			                    		quicklinks.add("Complex attributes | cattrs");
			                    	
			                    	request.setAttribute("quicklinks", quicklinks);
			                    	%>
		                    		<jsp:include page="quicklinks.jsp" flush="true">
		                    		</jsp:include>
						            <%
								}
								%>
								
								<!-- schema -->
								
								<%
								if (mode.equals("view")){ %>
									<tr height="10"><td width="100%"></td></tr>
									<tr>
										<td width="100%" style="border: 1 solid #FF9900">
											<table border="0" width="100%" cellspacing="0">											
												<tr>
													<td width="73%" valign="middle" align="left">
														Create an XML Schema for this element
													</td>
													<td width="27%" valign="middle" align="left">
														<a target="_blank" href="GetSchema?id=ELM<%=delem_id%>">
															<img border="0" src="images/icon_xml.jpg" width="16" height="18"/>
														</a>
													</td>
												</tr>
											</table>
										</td>
									</tr><%
								}
								%>
								
								<!-- type -->
								<tr height="10"><td width="100%"></td></tr>
								<tr>
									<td width="100%">
										<table border="0" width="100%" cellspacing="0" cellpadding="3">
											<tr>
												<td width="7%"><b>Type</b></td>
												<td width="6%">
													<a target="_blank" href="types.html">
														<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
													</a>
												</td>
												<td width="88%">
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
												</td>
											</tr>
										</table>
									</td>
								</tr>
								
								
								<!-- start dotted -->
								
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
													if(mode.equals("view")){ %>
														<%=Util.replaceTags(dataElement.getShortName())%>
														<input type="hidden" name="delem_name" value="<%=dataElement.getShortName()%>"/><%
													}
													else if (mode.equals("add")){ %>
														<input class="smalltext" type="text" size="30" name="delem_name"/><%
													}
													else { %>
														<input class="smalltext" type="text" size="30" name="delem_name" value="<%=dataElement.getShortName()%>"/><%
													}
													%>
												</td>
												
												<%isOdd = Util.isOdd(++displayed);%>
								    		</tr>
								    		
								    		<!-- dataset -->
								    		<%
								    		Dataset dst = null;
											if (mode.equals("add") || dataset!=null){ %>
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
														// the case when dataset has been specified and mode is view or edit
														if (dataset!=null && !mode.equals("add")){ %>
															<a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view">
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
																	<option selected value="">-- select a dataset --</option><%
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
																	String selected = isTheOne ? "selected" : "";
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
								    		%>
								    		
								    		<!-- table -->
								    		<%
											if (mode.equals("add") || dsTable!=null){ %>
												<tr>
									    			<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														Table
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="identification.html#table">
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
														// the case when table has been specified and mode is view or edit
														if (dsTable!=null && !mode.equals("add")){
																%>
																<font class="title2" color="#006666">
																<a href="dstable.jsp?mode=view&amp;table_id=<%=tableID%>&amp;ds_id=<%=dsID%>"><%=Util.replaceTags(dsTable.getShortName())%></a></font>
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
																		<option selected value="">-- select a table --</option>
																		<%
																	}
																	
																	Vector tables = searchEngine.getDatasetTables(dst.getID());
																	for (int i=0; tables!=null && i<tables.size(); i++){
																		DsTable tb = (DsTable)tables.get(i);
																		boolean isTheOne = tbl==null ? false : tbl.getID().equals(tb.getID());
																		String selected = isTheOne ? "selected" : "";
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
											%>
											
											<!-- RegistrationStatus -->
								    		<%
								    		String regStatus = dataElement!=null ? dataElement.getStatus() : null;
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
														<%=regStatus%><%
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
											
								    		<!-- GIS -->
								    		<%
								    		String gisType = dataElement!=null ? dataElement.getGIS() : null;
											if (!mode.equals("view") || gisType!=null){ %>
									    		<tr>
													<td width="<%=titleWidth%>%" class="simple_attr_title<%=isOdd%>">
														GIS type
													</td>
													<td width="4%" class="simple_attr_help<%=isOdd%>">
														<a target="_blank" href="identification.html">
															<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td width="4%" class="simple_attr_help<%=isOdd%>">
															<img border="0" src="images/optional.gif" width="16" height="16"/>
														</td><%
													}
													%>
													<td width="<%=valueWidth%>%" class="simple_attr_value<%=isOdd%>">
														<%
														if (mode.equals("view")){
															gisType = (gisType==null || gisType.length()==0) ? "&nbsp" : gisType;
															%>
															<%=gisType%><%
														}
														else{
															String selected = gisType==null ? "selected" : "";
															%>
															<select name="gis">
																<option value="nogis" <%=selected%>>-- no GIS element --</option><%
																Vector gisTypes = DataElement.getGisTypes();
																for (int i=0; i<gisTypes.size(); i++){
																	String gist = (String)gisTypes.get(i);
																	selected = gisType!=null && gist.equals(gisType) ? "selected" : "";
																	String gisDisp = gist.equals("") ? "[ ]" : gist;
																	%>
																	<option <%=selected%> value="<%=gist%>"><%=gisDisp%></option><%
																} %>
															</select><%
														}
														%>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
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
												
												attrID = attribute.getID();
												attrValue = getValue(attrID);
												String attrOblig = attribute.getObligation();
												String obligImg  = "optional.gif";
												if (attrOblig.equalsIgnoreCase("M"))
													obligImg = "mandatory.gif";
												else if (attrOblig.equalsIgnoreCase("C"))
													obligImg = "conditional.gif";
												
												// set isBoolean if the element is of boolean datatype
												if (attribute.getShortName().equalsIgnoreCase("Datatype"))
													if (attrValue!=null && attrValue.equalsIgnoreCase("boolean"))
														isBoolean = true;
												
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
												boolean inherit = attribute.getInheritable().equals("0") ? false:true;
												
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
																	[Click <a target="_blank" href="imgattr.jsp?obj_id=<%=delem_id%>&amp;obj_type=E&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=dataElement.getShortName()%>&amp;attr_name=<%=attribute.getShortName()%>"><b>HERE</b></a> to <%=actionText%>]
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
																		<%=sInhText%><%=inheritedValue%><br><%
																}
															}
															
															// mutliple display
															if (dispMultiple && !dispType.equals("image")){ %>
															
																<select <%=disabled%> id="attr_mult_<%=attrID%>" name="attr_mult_<%=attrID%>" multiple="true" style="width:auto"><%
																	for (int k=0; multiValues!=null && k<multiValues.size(); k++){
																		attrValue = (String)multiValues.get(k);
																		%>
																		<option value="<%=attrValue%>"><%=attrValue%></option><%
																	}
																	%>
																</select>
																
																<%
																if (disabled.equals("")){ %>
																	<a href="javascript:rmvValue('<%=attrID%>')"><img src="images/button_remove.gif" border="0" title="Click here to remove selected value"/></a>
																	<a href="javascript:openAddBox('<%=attrID%>', 'dispType=<%=dispType%>&amp;width=<%=width%>')"><img src="images/button_plus.gif" border="0" title="Click here to add a new value"/></a><%
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
															
															// no multiple display
															else{
																if (dispType.equals("text")){
																	if (attrValue!=null){
																		%>
																		<input <%=disabled%> class="smalltext" class="smalltext" type="text" size="<%=width%>" name="attr_<% if (dispMultiple)%> mult_<%;%><%=attrID%>" value="<%=attrValue%>" onchange="form_changed('form1')"/>
																		<%
																	}
																	else{
																		%>
																		<input <%=disabled%> class="smalltext" class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"/>
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
																				
																				if (attribute.getShortName().equalsIgnoreCase("Datatype")){
																					if (type!=null &&
																						type.equals("CH2") &&
																						fxValue.getValue().equalsIgnoreCase("boolean"))
																						continue;
																				}
									
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
												<%
							    			}
							    			%>
								    		<!-- end dynamic attributes -->
								    		
								    		<!-- version (or the so-called LastCheckInNo) -->
											<%											
											if (verMan==null) verMan = new VersionManager();
											
								    		if (!mode.equals("add")){
												String elmVersion = dataElement.getVersion();
				
												boolean isFirst=false;
												if (mode.equals("edit") && elmVersion.equals("1")){
													isFirst = verMan.isLastElm(delem_id, idfier, dsTable.getNamespace());
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
														<%=elmVersion%>
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
														<b><%=Util.replaceTags(idfier)%></b>
														<input type="hidden" name="idfier" value="<%=idfier%>"/><%
													}
													else{ %>
														<input class="smalltext" type="text" size="30" name="idfier" onchange="form_changed('form1')" value="<%=idfier%>"/><%
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
											
											// horizontal separator 1
											if (!separ1displayed){ %>
												<%@ include file="hor_separator.jsp" %><%
												separ1displayed = true;
											}
																						
											String title = type.equals("CH1") ? "Allowable values" : "Suggested values";
											%>
										
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
											
												<!-- title & link part -->
												<tr>
													<td width="34%">
														<b><%=title%><a name="values"></a></b>
													</td>
													
													<%
													// the link
													String valuesLink = "fixed_values.jsp?delem_id=" + delem_id + "&amp;delem_name=" + delem_name + "&amp;mode=view&amp;parent_type=" + type;
													if (mode.equals("edit") && user!=null){
														%>
														<td class="barfont" width="66%">
															[Click <a href="<%=valuesLink%>"><b>HERE</b></a> to manage <%=title.toLowerCase()%> of this element]
														</td><%
													}
													%>
												</tr>
												
												<!-- table part -->
												<%
												if (mode.equals("view") && fixedValues!=null && fixedValues.size()>0){%>
													<tr>
														<td width="100%" colspan="2">
															<table border="1" width="100%" bordercolorlight="#C0C0C0" cellspacing="0" cellpadding="2" bordercolordark="#C0C0C0">
																<tr>
																	<th width="20%" class="tbl_elms">Value</th>
																	<th width="40%" class="tbl_elms">Definition</th>
																	<th width="40%" class="tbl_elms">ShortDescription</th>																	
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
																		<td width="20%" class="tbl_elms">
																			<a href="<%=valueLink%>">
																				<%=Util.replaceTags(value)%>
																			</a>
																		</td>
																		<td width="40%" class="tbl_elms" title="<%=Util.replaceTags(defin)%>">
																			<%=Util.replaceTags(dispDefin)%>
																		</td>
																		<td width="40%" class="tbl_elms" title="<%=Util.replaceTags(shortDesc)%>">
																			<%=Util.replaceTags(dispShortDesc)%>
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
										
										
										<!-- foreign key relations -->
										
										<%										
										if ((mode.equals("edit") && user!=null) || (mode.equals("view") && fKeys!=null && fKeys.size()>0)){
											
											// horizontal separator 1
											if (!separ1displayed){ %>
												<%@ include file="hor_separator.jsp" %><%
												separ1displayed = true;
											}
											
											%>										
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
											
												<!-- title & link part -->
												<tr>
													<td width="34%">
														<b>Foreign key relations<a name="fkeys"></a></b>
													</td>
													
													<%
													// the link
													if (mode.equals("edit") && user!=null){
														%>
														<td class="barfont" width="66%">
															[Click <a href="foreign_keys.jsp?delem_id=<%=delem_id%>&amp;delem_name=<%=delem_name%>&amp;ds_id=<%=dsID%>"><b>HERE</b></a> to manage foreign keys of this element]
														</td><%
													}
													%>
												</tr>
												
												<!-- table part -->
												<%												
												if (mode.equals("view") && fKeys!=null && fKeys.size()>0){%>
													<tr>
														<td width="100%" colspan="2">
															<table border="1" width="100%" bordercolorlight="#C0C0C0" cellspacing="0" cellpadding="2" bordercolordark="#C0C0C0">
																<tr>
																	<th width="50%" class="tbl_elms">Element</th>
																	<th width="50%" class="tbl_elms">Table</th>
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
																		<td width="50%" class="tbl_elms">
																			<a href="data_element.jsp?delem_id=<%=fkElmID%>&amp;mode=view">
																				<%=Util.replaceTags(fkElmName)%>
																			</a>
																		</td>
																		<td width="50%" class="tbl_elms">
																			<%=fkTblName%>
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
										
										
										<!-- related elements -->
										<%										
										if (false && (mode.equals("edit") && user!=null) || (mode.equals("view") && relElems!=null && relElems.size()>0)){
											
											// horizontal separator 1
											if (!separ1displayed){ %>
												<%@ include file="hor_separator.jsp" %><%
												separ1displayed = true;
											}
											%>
																					
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
											
												<!-- title & link part -->
												<tr>
													<td width="34%">
														<b>Related elements<a name="rels"></a></b>
													</td>
													
													<%
													// the link
													if (mode.equals("edit") && user!=null){
														%>
														<td class="barfont" width="66%">
															[Click <a href="rel_elements.jsp?delem_id=<%=delem_id%>&amp;delem_name=<%=delem_name%>"><b>HERE</b></a> to manage this element's relations to others]
														</td><%
													}
													%>
												</tr>
												
												<!-- table part -->
												<%												
												if (mode.equals("view") && relElems!=null && relElems.size()>0){%>
													<tr>
														<td width="100%" colspan="2">
															<table border="1" width="100%" bordercolorlight="#C0C0C0" cellspacing="0" cellpadding="2" bordercolordark="#C0C0C0">
																<tr>
																	<th width="50%" class="tbl_elms">Element</th>
																	<th width="50%" class="tbl_elms">Relation</th>
																</tr>
																<%
																// rows
																for (int i=0; i<relElems.size(); i++){
																	
																	CsiItem item = (CsiItem)relElems.get(i);
																	String elem = item.getValue();
																	String elemID = item.getComponentID();
																	String relation = item.getRelDescription();
																	if (relation==null || relation.trim().length()==0)
																		relation = "&nbsp";
																		
																	%>
																	<tr>
																		<td width="50%" class="tbl_elms">
																			<a href="data_element.jsp?delem_id=<%=elemID%>&amp;mode=view">
																				<%=Util.replaceTags(elem)%>
																			</a>
																		</td>
																		<td width="50%" class="tbl_elms">
																			<%=Util.replaceTags(relation)%>
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
															[Click <a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=delem_name%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>')"><b>HERE</b></a> to manage complex attributes of this element]
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
																			<a href="javascript:complexAttr('complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=delem_name%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>')" title="Click here to view all the fields">
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
				
				<%
				if (type!=null){ %>
					<input type="hidden" name="type" value="<%=type%>"/> <%
				}
				%>
				<input type="hidden" name="mode" value="<%=mode%>"/>
				<input type="hidden" name="ctx" value="<%=contextParam%>"/>
				<input type="hidden" name="pick" value="false"/>
				<input type="hidden" name="check_in" value="false"/>
				<input type="hidden" name="ns" value="1"/>
				<input type="hidden" name="copy_elem_id" value=""/>
				<input type="hidden" name="changed" value="0">
				<input type="hidden" name="ver_upw" value="true">
				
				<%
				if (latestID!=null){%>
					<input type="hidden" name="latest_id" value="<%=latestID%>"><%
				}
				
				String dsidf = dataset==null ? null : dataset.getIdentifier();
				if (dsidf==null && dst!=null)
					dsidf = dst.getIdentifier();
				
				if (dsidf!=null){%>
					<input type="hidden" name="ds_idf" value="<%=dsidf%>"/><%
				}
				%>
				
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
	} catch (SQLException e) {
	}
}
%>