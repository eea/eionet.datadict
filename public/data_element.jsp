<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,java.io.*"%>

<%!private String mode=null;%>
<%!private Vector mAttributes=null;%>
<%!private DataElement dataElement=null;%>
<%!private DataElement newDataElement=null;%>
<%!private Vector complexAttrs=null;%>
<%!private Vector fixedValues=null;%>
<%!private Vector fxvAttributes=null;%>


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
						
						String dsn = request.getParameter("ds_name");
						if (dsn==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsn, "u")){%>
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
			if (mode.equals("edit") || mode.equals("view")){
				
				dataElement = searchEngine.getDataElement(delem_id);
					
				if (dataElement!=null){
					type = dataElement.getType();
					//delem_name = dataElement.getAttributeValueByName("Name");
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
			    											dataElement.getShortName(), "elm");
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
					  SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getShortName(), "u");
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
			
			%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript" src='modal_dialog.js'></script>
    <script language="JavaScript">
    
    	function openSchema(){
			window.open("station.xsd",null, "height=400,width=600,status=no,toolbar=no,menubar=no,location=no,scrollbars=yes,top=100,left=100");
		}

		function submitPick(){
			document.forms["form1"].elements["pick"].value = "true";
			document.forms["form1"].submit();
		}
		
		function checkIn(){
			
			if (document.forms["form1"].elements["is_first"]){
				if (document.forms["form1"].elements["is_first"].value=="true"){
					openDialog("yesno_dialog.html", "Do you want to update parent table and dataset versions?",updateParent,100, 400);
					return;
				}
			}

			document.forms["form1"].elements["check_in"].value = "true";
			submitForm('edit');
			
			
			//document.forms["form1"].elements["mode"].value = "edit";
			//document.forms["form1"].submit();
		}
		function updateParent(){
			value = dialogWin.returnValue;
			if (value==null)
				value=true;
			document.forms["form1"].elements["ver_upw"].value=value;
			document.forms["form1"].elements["check_in"].value = "true";
			submitForm('edit');
		}
		
		function submitForm(mode){
			
			if (mode=="add"){
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
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return;
				}
				
				if (hasWhiteSpace("delem_name")){
					alert("Short name cannot contain any white space!");
					return;
				}
				
				var identifierInputName = document.forms["form1"].IdentifierInputName.value;
						
				if (identifierInputName!=null && hasWhiteSpace(identifierInputName)){
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
    	
    	function setPickedDataset(dsName, dsID){
	    	document.forms["form1"].ds_name.value=dsName;
	    	document.forms["form1"].ds_id.value=dsID;
    	}
    	
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
		
		function viewHistory(){
			var url = "elm_history.jsp?delem_id=<%=delem_id%>";
			window.open(url,null,"height=400,width=400,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=yes");
		}
		
		function fixType(){
			
			var type = document.forms["form1"].typeSelect.value;
			if (type == null || type.length==0)
				return;
				
			if (type=='AGG'){
				alert("Not supported right now!");
				document.forms["form1"].typeSelect.selectedIndex = 0;
				return;
			}
			
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
			
			if (document.forms["form1"].elements["delem_name"].value==""){
				alert("Short name cannot be empty!");
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
	        </center></P>
	    </TD>
        <TD>
		        <jsp:include page="location.jsp" flush='true'>
		            <jsp:param name="name" value="Data Element"/>
		            <jsp:param name="back" value="true"/>
		        </jsp:include>
		        
		        <div style="margin-left:30">
		        
			<form name="form1" id="form1" method="POST" action="data_element.jsp">
			
			<% if (!mode.equals("add")){ %>
				<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
			<% } else { %>
				<input type="hidden" name="dummy"/>
			<% } %>
			
			<table width="500" cellspacing="0">
				<tr>
					<%
					if (mode.equals("add")){ %>
						<td colspan="2"><span class="head00">Add a data element definition</span></td> <%
					}
					else if (mode.equals("edit")){ %>
						<td colspan="2"><span class="head00">Edit data element definition</span></td> <%
					}
					else{
						
						// set the flag indicating if the top namespace is in use
						String topWorkingUser = verMan.getWorkingUser(dataElement.getTopNs());
						boolean topFree = topWorkingUser==null ? true : false;
			
						%>
						<td><span class="head00">View data element definition</span></td>
						<td align="right">
							<input type="button" class="smallbutton" value="History" onclick="viewHistory()"/>&#160;
							<%
							if (user!=null && dataElement!=null){
								
								boolean isDeleted = searchEngine.isElmDeleted(dataElement.getID());
								if (isDeleted && topFree && deletePrm){ %>
									<input type="button" class="smallbutton" value="Restore" onclick="restore()"/>&#160;<%
								}
								
								boolean inWorkByMe = workingUser==null ?
											 false :
											 workingUser.equals(user.getUserName());
								
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
							else{
								%>&#160;<%
							}
							%>
						</td> <%
					}
					%>
				</tr>
				
				<%
				if (dataElement!=null && dataElement.isWorkingCopy()){ %>
					<tr><td colspan="2"><font color="red"><b>WORKING COPY!!!</b></font></td></tr><%
				}
				
				if (!mode.equals("view")){ %>
				
					<tr height="5"><td colspan="2"></td></tr>
				
					<tr>
						<td colspan="2"><span class="Mainfont">
						(M), (O) and (C) behind the titles stand for Mandatory, Optional and Conditional.<br/>
						NB! Edits will be lost if you leave the page without saving!
						<%
						if (type==null){ %>
							<br/><br/><b>NB! Please select the element type first. Otherwise your entries will be lost!</b> <%
						}
						%>
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
					<a target="_blank" href="types.html"><span class="help">?</span></a>&#160;
					<span class="mainfont"><b>Type</b>
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
					if (mode.equals("add") && (type==null || type.length()==0)){ %>
						<select class="small" name="typeSelect" onchange="fixType()">
							<option value="">-- Select element type --</option>
							<option value="CH1">Data element with fixed values (codes)</option>
							<option value="CH2">Data element with quantitative values (e.g. measurements)</option>
						</select> <%
					}
					else{
						if(type.equals("AGG")){ %>
							<b>AGGREGATE DATA ELEMENT</b>
						<% }else if (type.equals("CH1")){ %>
							<b>DATA ELEMENT WITH FIXED VALUES</b>
						<% }else if (type.equals("CH2")){ %>					
							<b>DATA ELEMENT WITH QUANTITATIVE VALUES</b>
						<% } else{ %>
							<b>AGGREGATE DATA ELEMENT</b> <%
						}
					}
					%>
				</td>
			</tr>
			
			<tr height="10"><td colspan="3"></td></tr>
			
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
						<font class="title2" color="#006666"><%=Util.replaceTags(delem_name)%></font>
						<input type="hidden" name="delem_name" value="<%=delem_name%>"/>
					<% } else{ %>
						<input class="smalltext" class="smalltext" type="text" size="30" name="delem_name" onchange="form_changed('form1')" value="<%=delem_name%>"></input>
					<% } %>
				</td>
			</tr>
			
			
			<%
			Dataset dst = null;
			boolean isAGG = type==null ? false : type.equals("AGG");
			if (!isAGG && (mode.equals("add") || dataset!=null)){ %>
			
				<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a target="_blank" href="identification.html#dataset"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Dataset</b>
							<%
							displayed++;
							if (!mode.equals("view")){
								%>
								&#160;(O)
								<%
							}
							%>
						</span>
					</td>
					<td colspan="2">
						<%
						if (dataset!=null && !mode.equals("add")){
								%>
								<font class="title2" color="#006666"><a href="dataset.jsp?ds_id=<%=dsID%>&mode=view"><%=Util.replaceTags(dataset.getShortName())%></a></font>
								<input type="hidden" name="ds_id" value="<%=dsID%>"/>
								<%
						}
						else{
							dsID = request.getParameter("ds_id");
							if (dsID!=null && dsID.length()!=0)
								dst = searchEngine.getDataset(dsID);
							%>
							<select class="small" name="ds_id" onchange="submitPick('<%%>')"> <%
							
								/*if (dsID==null || dataset==null){ %>
									<option selected value="">-- select a dataset --</option>
									<%
								}
								else{ %>
									<option value="">-- no particular dataset --</option>
									<%
								}*/
								
								if (dst==null){ %>
									<option selected value="">-- select a dataset --</option><%
								}
								
								Vector datasets = searchEngine.getDatasets();
								for (int i=0; datasets!=null && i<datasets.size(); i++){
									Dataset ds = (Dataset)datasets.get(i);
									
									if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + ds.getShortName(), "u"))
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
							</select>&#160;
							<span class="smallfont" style="font-weight: normal">
								(datasets in work are not displayed)
							</span>
							<%
						}
						%>
					</td>
				</tr>
				<%
			}
			%>
			
			<%
			if (!isAGG && (mode.equals("add") || dsTable!=null)){
				
				%>
				<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" style="padding-right:10">
						<a target="_blank" href="identification.html#table"><span class="help">?</span></a>&#160;
						<span class="mainfont"><b>Table</b>
							<%
							displayed++;
							if (!mode.equals("view")){
								%>
								&#160;(O)
								<%
							}
							%>
						</span>
					</td>
					<td colspan="2">
						<%
						if (dsTable!=null && !mode.equals("add")){
								%>
								<font class="title2" color="#006666">
								<a href="dstable.jsp?mode=view&table_id=<%=tableID%>&ds_id=<%=dsID%>"><%=Util.replaceTags(dsTable.getShortName())%></a></font>
								<input type="hidden" name="table_id" value="<%=dsTable.getID()%>"/>
								<%
						}
						else{
							
							tableID = request.getParameter("table_id");
							DsTable tbl = null;
							if (tableID!=null && tableID.length()!=0)
								tbl = searchEngine.getDatasetTable(tableID);
								
							%>
							<select class="small" name="table_id"">
								<%
								if (dst!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dst.getShortName(), "u")){
									if (tbl==null){ %>
										<option selected value="">-- select a table --</option>
										<%
									}
									
									Vector tables = searchEngine.getDatasetTables(dst.getID());
									for (int i=0; tables!=null && i<tables.size(); i++){
										DsTable tb = (DsTable)tables.get(i);
										boolean isTheOne = tbl==null ? false : tbl.getID().equals(tb.getID());
										String selected = isTheOne ? "selected" : "";
										//String selected = (tableID!=null && dsTable!=null && tableID.equals(tbl.getID())) ? "selected" : "";
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
				</tr>
				<%
			}
			%>
			
			<%
			
			if (type!=null && type.equals("AGG")){
				
				if (mode.equals("add")){
					
					String disabled = user == null ? "disabled" : "";
				
					%>
					<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<td align="right" style="padding-right:10">
							<a href="javascript:openExtends()"><span class="help">?</span></a>&#160;
							<span class="mainfont"><b>Extends</b>
								<%
								displayed++;
								if (!mode.equals("view")){
									%>
									&#160;(O)
									<%
								}
								%>
							</span>
						</td>
						<td colspan="2">					
							<select class="small" name="extends" <%=disabled%> >
								<option value="">-- none --</option>
								<%
								
								if (user != null){
									
									Vector dataElements = searchEngine.getDataElements();
									String thisExtension = dataElement == null ? null : dataElement.getExtension();
								
									for (int i=0; dataElements!=null && i<dataElements.size(); i++){
										
										DataElement elem = (DataElement)dataElements.get(i);
										
										if (!elem.getType().equals("AGG"))
											continue;
										
										String elemID = elem.getID();										
										
										if (thisExtension != null && thisExtension.equals(elemID)){
											%>
											<option selected value="<%=elemID%>"><%=elem.getShortName()%></option>
											<%
										}
										else{
											%>								
											<option value="<%=elemID%>"><%=elem.getShortName()%></option>
											<%
										}
									}
								}
								
								%>
							</select>
						</td>
					</tr>
					<%
				}
				else{
					String extensionID = dataElement.getExtension();
					if (extensionID != null){
						
						DataElement extElem = searchEngine.getDataElement(extensionID);
						String dispExtension = extElem.getShortName();
						
						%>
						<tr <% if (mode.equals("view") && displayed % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
							<td align="right" style="padding-right:10">
								<a href="javascript:openExtends()"><span class="help">?</span></a>&#160;
								<span class="mainfont"><b>Extends</b>
									<%
									displayed++;
									if (!mode.equals("view")){
										%>
										&#160;(O)
										<%
									}
									%>
								</span>
							</td>
							<td colspan="2">
								<font class="head0" color="#006666"><%=dispExtension%></font>
							</td>
						</tr>
						<%
					}
				}
			}
			
			
			// display Version, if not "add" mode.
			// Users cannot specify Version, it is always generated by the code.
			// First make sure you don't display Version for a status that doesn't require it.
			
			String regStatus = dataElement!=null ? dataElement.getStatus() : null;
			
			if (verMan==null) verMan = new VersionManager();
			
			if (!mode.equals("add")){
				String elmVersion = dataElement.getVersion();
				
				boolean isFirst=false;
				if (mode.equals("edit") && elmVersion.equals("1")){
					isFirst = verMan.isLastElm(delem_id, delem_name, dsTable.getNamespace());
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
					<td colspan="2"><font class="title2" color="#006666"><%=elmVersion%></font></td>
				</tr>
				
				<input type="hidden" name="is_first" value="<%=isFirst%>">
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
						<span class="barfont" style="width:400"><%=regStatus%></span> <%
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
			
			boolean isBoolean = false;
			
			for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
				
				attribute = (DElemAttribute)mAttributes.get(i);
				String dispType = attribute.getDisplayType();
				if (dispType == null)
					continue;
				
				boolean dispFor = type==null ? attribute.displayFor("AGG") : attribute.displayFor(type);
				
				if (!dispFor)
					continue;
				
				attrID = attribute.getID();
				attrValue = getValue(attrID);
				String attrOblig = attribute.getObligation();
				
				// set isBoolean if the element is of boolean datatype
				if (attribute.getShortName().equalsIgnoreCase("Datatype"))
					if (attrValue!=null && attrValue.equalsIgnoreCase("boolean"))
						isBoolean = true;
				
				// if element is of CH1 type, don't display MinSize and MaxSize
				if (attribute.getShortName().equalsIgnoreCase("MaxSize") || attribute.getShortName().equalsIgnoreCase("MinSize"))
					if (type.equalsIgnoreCase("CH1"))
						continue;
				
				if (mode.equals("view") && (attrValue==null || attrValue.length()==0) && !attrOblig.equals("M"))
					continue;
				
				if (dispType.equals("image") && mode.equals("add")) continue;
				
				displayed++;
				
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
				<tr <% if (mode.equals("view") && displayed % 2 == 0) %> bgcolor="#D3D3D3" <%;%>>
					<td align="right" valign="top" style="padding-right:10">
						<a href="javascript:openUrl('delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=SIMPLE&mode=view')">
						<span class="help">?</span></a>&#160;
						<span class="mainfont"><b><%=attribute.getShortName()%></b>
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
							<input type="hidden" name="IdentifierInputName" value="attr_<%=attrID%>" onchange="form_changed('form1')"/>
							<%
						}
						
						if (dispType.equals("image")){%>
							<span class="barfont" style="width:400">
								<a target="_blank" href="imgattr.jsp?obj_id=<%=delem_id%>&obj_type=E&attr_id=<%=attribute.getID()%>&obj_name=<%=dataElement.getShortName()%>&attr_name=<%=attribute.getShortName()%>">image(s)</a>
							</span><%
						}
						// if mode is 'view', display a span, otherwise an input
						else if (mode.equals("view")){
							%>
								<span class="barfont" style="width:400"><%=Util.replaceTags(attrValue)%></span>
							<%
						}
						else{
							
							/*if (mode.equals("add") && inherit && dsID!=null){
								%>
								<input <%=disabled%> type="checkbox" name="inherit_<%=attrID%>" checked/> Inherit this attribute from table level<br/>
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
									<select <%=disabled%> id="attr_mult_<%=attrID%>" name="attr_mult_<%=attrID%>" multiple="true" style="width:auto">
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
												if (type.equals("CH2") && fxValue.getValue().equalsIgnoreCase("boolean")) continue;
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
								</select>&#160;
								<a target="_blank" href="fixed_values.jsp?mode=view&delem_id=<%=attrID%>&delem_name=<%=attribute.getShortName()%>&parent_type=attr">
									<span class="help">?</span>
								</a>
								<%
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
							<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=COMPLEX&mode=view">
							<span class="help">?</span></a>&#160;
							<span class="mainfont"><b>
								<a href="javascript:complexAttr('complex_attr.jsp?attr_id=<%=attrID%>&#38;mode=view&#38;parent_id=<%=delem_id%>&#38;parent_type=E&#38;parent_name=<%=delem_name%>&#38;table_id=<%=tableID%>&#38;dataset_id=<%=dsID%>')" title="Click here to view all the fields">
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
								}*/
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
										<span class="barfont"><%=mark%> <%=Util.replaceTags(fieldValue)%></span><br>
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
			if (mode.equals("add") && dsID!=null){
				complexAttrs = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_COMPLEX, null, "1");
				
				if (complexAttrs.size()>0){
					%>
					
					<tr height="5"><td colspan="2"></td></tr>
					<tr valign="top">
						<td align="left" style="padding-right:10" colspan="2">
							<span class="mainfont">Inherit the following complex attributes from table level:</span>
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
								<a href="delem_attribute.jsp?attr_id=<%=attrID%>&#38;type=COMPLEX&mode=view">
								<span class="help">?</span></a>&#160;
								<span class="mainfont"><b><%=attrName%></b></span>
							</td>
							<td>
								<input type="checkbox" name="inherit_complex_<%=attrID%>" checked/><br>
							</td>
						</tr>
						<%
					}

				}
			}*/
			%>

			<%
		
			if (!mode.equals("add") && !mode.equals("view")
					|| (mode.equals("view") && user!=null)){ // if mode is not 'add'
			%>
			
			<tr height="5"><td colspan="2"></td></tr>
			<tr>
				<td>&#160;</td>
				<td>
					<b>*</b> <span class="smallfont"><a href="javascript:complexAttrs('complex_attrs.jsp?parent_id=<%=delem_id%>&#38;parent_type=E&#38;parent_name=<%=delem_name%>&#38;table_id=<%=tableID%>&#38;dataset_id=<%=dsID%>')">
						<b>COMPLEX ATTRIBUTES</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						&lt;&#160;click here to view/edit complex attributes specified for this data element
					</span>
				</td>
			</tr>
			<%
			}
			%>
		
		<!-- ALLOWABLE VALUES table -->
		<% if (type!=null && type.equals("CH1") && !mode.equals("add") && !isBoolean){ // if CH1 and mode=add
			boolean bShowLink=false;
			if (mode.equals("view")){
				fxvAttributes = new Vector();

				for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
					attribute = (DElemAttribute)mAttributes.get(i);
					String dispType = attribute.getDisplayType();
					if (dispType != null &&
						attribute.displayFor("FXV")){
					fxvAttributes.add(attribute);
					}
				}

				//fixedValues = searchEngine.getFixedValues(delem_id, "elem", false);
				fixedValues = searchEngine.getAllFixedValues(delem_id, "elem");
				if (fixedValues == null) fixedValues = new Vector();

				Vector fxvRelElems = searchEngine.getRelatedElements(delem_id, "elem", null, "CH1");
				if (fxvRelElems == null) fxvRelElems = new Vector();
				Vector relElemId = new Vector();
		
				if (fixedValues.size()>0){
				%>
					<tr height="5"><td colspan="2"></td></tr>
					<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<span class="mainfont"><b>Allowable values</b></span>
						</td>
						<td>
							<table width="auto" cellspacing="0">
								<tr>
									<th align="left" style="padding-left:5;padding-right:10" width="100">Value</th>
									<%
										for (int i=0; fxvAttributes!=null && i<fxvAttributes.size(); i++){
											
											attribute = (DElemAttribute)fxvAttributes.get(i);
											%>
											<th align="left" style="padding-left:5;padding-right:10" width="150"><%=attribute.getShortName()%></th>
											<%
										}
										for (int i=0; fxvRelElems!=null && i<fxvRelElems.size(); i++){
			
											CsiItem item = (CsiItem)fxvRelElems.get(i);
											String compID = item.getComponentID();
											if (compID == null) continue;
											relElemId.add(compID);
											%>
											<th align="left" style="padding-left:5;padding-right:10" width="150"><%=Util.replaceTags(item.getValue())%></th>
											<%
										}
									%>
								</tr>
	
								<%
								String mode= (user == null) ? "print" : "edit";

								for (int i=0; i<fixedValues.size(); i++){
									if (i==30){	// it's possible to see only the first 30 values on element page
										%>
										<tr><td colspan="<%=fxvAttributes.size() + 1 %>">
											<span class="barfont">... &#160; to view the whole list of allowable values, click the link below</span>
										</td></tr>
										<%
										if (user == null) bShowLink=true;
										break;
									}
									else{
										FixedValue fxv = (FixedValue)fixedValues.get(i);
										String value = fxv.getValue();
										String fxvID = fxv.getID();
										int level=fxv.getLevel();
										String fxvAttrValue = null;
										String fxvAttrValueShort = null;
										String spaces="";
										for (int j=1; j<level; j++){
											spaces +="&#160;&#160;&#160;";
										}
										
										%>
										<tr>
											<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
												<%=spaces%> <b><a href="fixed_value.jsp?fxv_id=<%=fxvID%>&#38;mode=<%=mode%>&delem_id=<%=delem_id%>&delem_name=<%=delem_name%>&parent_type=elem">
													<%=Util.replaceTags(value)%>
												</a></b>
											</td>
											<%
											for (int c=0; fxvAttributes!=null && c<fxvAttributes.size(); c++){
						
												attribute = (DElemAttribute)fxvAttributes.get(c);
						
												fxvAttrValue = fxv.getAttributeValueByID(attribute.getID());
												if (fxvAttrValue==null || fxvAttrValue.length()==0){
												%>
													<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>></td>
												<%
												}
												else{
													if (fxvAttrValue.length()>35){
														fxvAttrValueShort = fxvAttrValue.substring(0,35) + " ...";
												}
													else{
														fxvAttrValueShort = fxvAttrValue;
													}
													%>
													<td valign="bottom" align="left" title="<%=fxvAttrValue%>" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
														<span class="barfont"><%=Util.replaceTags(fxvAttrValueShort)%></span>
													</td>
													<%
												}
											}
											for (int k=0; relElemId!=null && k<relElemId.size(); k++){
												String component_id = (String)relElemId.get(k);
												String relElemValue = component_id!=null ? fxv.getItemValueByComponentId(component_id):"";
												%>
													<td valign="bottom" align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
														<span class="barfont"><%=Util.replaceTags(relElemValue)%></span>
													</td>
												<%
											}
											%>
										</tr>
									<%
									}
									%>
								<%
								}
								%>
							</table>
						</td>
					</tr>
					<%
				}
		 	}  	// end if (mode.equals("view"))
		
			if (!mode.equals("add") && !mode.equals("view")
					|| (mode.equals("view") && user!=null)
					|| bShowLink){ // if mode is not 'add'
			%>
			<tr height="5"><td colspan="2"></td></tr>
			<tr>
				<td>&#160;</td><td>
					<b>*</b> <span class="smallfont"><a href="javascript:openUrl('fixed_values.jsp?delem_id=<%=delem_id%>&#38;delem_name=<%=delem_name%>&mode=view')">
						<b>ALLOWABLE VALUES</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						<% if (user != null) %>
							&lt;&#160;click here to view/add/remove fixed values of this data element
					</span>
				</td>
			</tr>
		
			<% 
			}
		} // end if CH1 and mode=add
		%>
		<%
		if (type!=null && type.equals("AGG") && !mode.equals("add")){ // if AGG and mode is not 'add'
		
			String seqID = dataElement.getSequence();
			String chcID = dataElement.getChoice();
			
			String extID = dataElement.getExtension();
			
			String uri = request.getRequestURI();
			StringBuffer url = new StringBuffer(uri.substring(0, uri.lastIndexOf("/") + 1));
			
			if (seqID != null && chcID != null)
				url.append("javascript:alert('A data element is expected to have only a sequence or only a choice of sub-elements, not both!");
			else{
				url.append("content.jsp?parent_type=elm");
				url.append("&parent_id=" + delem_id);
				url.append("&parent_name=" + delem_name);
				
				if (extID != null)
					url.append("&ext_id=" + extID);
			}
			
			if (seqID != null){				
				url.append("&content_id=" + seqID);
				url.append("&content_type=seq");
			}
			else if (chcID != null){
				url.append("&content_id=" + chcID);
				url.append("&content_type=chc");
			}
			
			//url.append("')");
				
		%>
			
		<tr>
			<td>&#160;</td>
			<td colspan="2">
				<b>*</b> <span class="smallfont"><a href="javascript:openUrl('<%=url.toString()%>')">
					<b>SUBELEMENTS</b></a></span>&#160;&#160;
				<span class="smallfont" style="font-weight: normal">
					&lt;&#160;click here to view/add/remove subelements of this aggregate
				</span>
			</td>
		</tr>
			
		<% 
		} // if AGG and mode is not 'add'		
		%>
		
		<!-- FOREIGN KEYS -->
		
		<%
		if (mode.equals("view")){
			Vector fKeys = searchEngine.getFKRelationsElm(delem_id, dataset.getID());
			if (fKeys.size()>0){
				%>
				<tr height="5"><td colspan="2"></td></tr>
				<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
				<tr valign="top">
					<td align="right" style="padding-right:10">
						<span class="mainfont"><b>Foreign keys</b></span>
					</td>
					<td>
						<table width="600" cellspacing="0">
							<tr>
								<th align="left" style="padding-left:5;padding-right:10">Element</th>
								<th align="left" style="padding-right:10">Table</th>
							</tr>

							<%
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
									<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
										<a href="data_element.jsp?delem_id=<%=fkElmID%>&#38;mode=view"><%=fkElmName%></a>
									</td>
									<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
										<%=fkTblName%>
									</td>
								</tr>
							<%
							}
							%>
						</table>
					</td>
				</tr>
				<%
			}
		}
		
		if (user!=null && !mode.equals("add")){
			%>
			<tr height="5"><td colspan="2"></td></tr>
			<tr>
				<td>&#160;</td>
				<td colspan="2">
					<b>*</b> <span class="smallfont"><a href="foreign_keys.jsp?delem_id=<%=delem_id%>&#38;delem_name=<%=delem_name%>&#38;ds_id=<%=dsID%>">
						<b>FOREIGN KEYS</b></a></span>&#160;&#160;
					<span class="smallfont" style="font-weight: normal">
						&lt;&#160;click here to edit the foreign key relations of this data element
					</span>
				</td>
			</tr>
		<%
		}
		%>
		
		<!-- RELATED ELEMENTS table -->
		<% if ( !mode.equals("add")){ // if mode!=add
			if (mode.equals("view")){

				Vector relElems = searchEngine.getRelatedElements(delem_id, "elem");
				if (relElems == null) relElems = new Vector();

				if (relElems.size()>0){
				%>
					<tr height="5"><td colspan="2"></td></tr>
					<tr><td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<span class="mainfont"><b>Related elements</b></span>
						</td>
						<td>
							<table width="600" cellspacing="0">
								<tr>
									<th align="left" style="padding-left:5;padding-right:10">Short name</th>
									<th align="left" style="padding-right:10">Description</th>
								</tr>
	
								<%
								String mode= (user == null) ? "print" : "edit";

								for (int i=0; i<relElems.size(); i++){
									CsiItem item = (CsiItem)relElems.get(i);
										
									%>
									<tr>
										<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<a href="data_element.jsp?delem_id=<%=item.getComponentID()%>&#38;mode=view">
												<%=Util.replaceTags(item.getValue())%>
											</a>
										</td>
										<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
											<%=Util.replaceTags(item.getRelDescription())%>
										</td>
									</tr>
								<%
								}
								%>
							</table>
						</td>
					</tr>
					<%
				}
		 	}  	// end if (mode.equals("view"))
			if (user!=null){
				%>
				<tr height="5"><td colspan="2"></td></tr>
				<tr>
					<td>&#160;</td>
					<td colspan="2">
						<b>*</b> <span class="smallfont"><a href="javascript:openUrl('rel_elements.jsp?delem_id=<%=delem_id%>&#38;delem_name=<%=delem_name%>')">
							<b>RELATED ELEMENTS</b></a></span>&#160;&#160;
						<span class="smallfont" style="font-weight: normal">
							&lt;&#160;click here to view/add/remove related data elements of this data element
						</span>
					</td>
				</tr>
			<%
			}
		}
		if (!mode.equals("view")){
			%>	
			<tr height="10"><td colspan="3"></td></tr>
			<tr>
				<td>&#160;</td>
				<td colspan="2">
				
					<% 
					
					if (mode.equals("add")){ // if mode is "add"
						if (user==null){ %>									
							<input class="mediumbuttonb" type="button" value="Add" disabled="true"/>&#160;&#160;
						<%} else {%>
							<input class="mediumbuttonb" type="button" value="Add" onclick="submitForm('add')"/>&#160;&#160;
							<input type="button" class="mediumbuttonb" value="Copy" onclick="copyElem()" title="Copies data element attributes from existing data element"/>&#160;&#160;
						<% }
					} // end if mode is "add"
					
					if (!mode.equals("add")){ // if mode is not "add"
						if (user==null){ %>									
							<input class="mediumbuttonb" type="button" value="Save" disabled="true"/>&#160;&#160;
							<%
							if (!dataElement.isWorkingCopy()){ %>
								<input class="mediumbuttonb" type="button" value="Delete" disabled="true"/>&#160;&#160;<%
							}
							else{ %>
								<input class="mediumbuttonb" type="button" value="Check in" onclick="checkIn()" disabled="true"/>&#160;&#160;
								<input class="mediumbuttonb" type="button" value="Undo check-out" disabled="true"/>&#160;&#160;<%
							}
						}
						else { %>
							<input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
							<%
							if (!dataElement.isWorkingCopy()){ %>
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
						* <a href="GetPrintout?format=PDF&obj_type=ELM&obj_id=<%=delem_id%>&dstID=<%=dataset.getID()%>">
							Create factsheet (PDF)
						</a>
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
										! If you see a blank page instead of the PDF, try setting off your Acrobat Reader's Web browser integration.<br>Acrobat 6.0 is recommended.
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
						* <a target="_blank" href="GetSchema?comp_id=<%=delem_id%>&comp_type=ELM">Create an XML Schema</a>
					</td>
				</tr>
				
				<%
			}
		
		//if (!mode.equals("add")){ // if mode is not "add"
		if (false){ // if mode is not "add"
			%>
			<tr height="20"><td colspan="3"></td></tr>
			<tr>
				<td colspan="3">
					<a href="javascript:alert('Under repairement!')">Printable page</a>
					<!--a href="javascript:printable('data_element_print.jsp?mode=print&delem_id=<%=delem_id%>&type=<%=type%>')">
						Printable page
					  </a-->
				</td>
			</tr>
			<%
		}
		
		if (type!=null){
			%>
			<input type="hidden" name="type" value="<%=type%>"/>
			<%
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
		
		String dsn = dataset==null ? null : dataset.getShortName();
		if (dsn==null && dst!=null)
			dsn = dst.getShortName();
		
		if (dsn!=null){%>
			<input type="hidden" name="ds_name" value="<%=dsn%>"/><%
		}
		%>
		
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