<%@page contentType="text/html;charset=UTF-8" import="java.net.URLEncoder,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*,java.io.*,javax.servlet.http.HttpUtils"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final int MAX_CELL_LEN=40;%>
<%!private static final int MAX_ATTR_LEN=500;%>
<%!private static final int MAX_DISP_VALUES=30;%>

<%!
	// servlet-scope helper functions
	//////////////////////////////////

	/**
	 *
	 */
	private DElemAttribute getAttributeByName(String name, Vector mAttributes){
		
		for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
			DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
	        //if (attr.getName().equalsIgnoreCase(name))
	        if (attr.getShortName().equalsIgnoreCase(name))
	        	return attr;
		}
	        
		    return null;
	}
	/**
	 *
	 */
	private String getAttributeIdByName(String name, Vector mAttributes){
		
		for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
			DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
	        //if (attr.getName().equalsIgnoreCase(name))
	        if (attr.getShortName().equalsIgnoreCase(name))
	        	return attr.getID();
		}
	        
	    return null;
	}
	/**
	 *
	 */
	private String getValue(String id, String mode, DataElement dataElement, DataElement newDataElement){
		return getValue(id, 0, mode, dataElement, newDataElement);
	}
	/**
	 *  int val indicates which type of value is requested. the default is 0
	 *  0 - display value (if original value is null, then show inherited value)
	 *  1 - original value
	 *  2 - inherited value
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
	/**
	 *
	 */
	private Vector getValues(String id, String mode, DataElement dataElement, DataElement newDataElement){
		return getValues(id, 0, mode, dataElement, newDataElement);
	}	
	/**
	 *  int val indicates which group of values is requested. the default is 0
	 *  0 - all
	 *  1 - original
	 *  2 - inherited
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
	/**
	 *
	 */
	private String getAttributeObligationById(String id, Vector mAttributes){
		
		for (int i=0; mAttributes!=null && i<mAttributes.size(); i++){
			DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
	        if (attr.getID().equalsIgnoreCase(id))
	        	return attr.getObligation();
		}
	        
	    return null;
	}
	/**
	 *
	 */
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
	// implementation of the servlet's service method
	//////////////////////////////////////////////////

	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setDateHeader("Expires", 0);
	response.setHeader("Cache-Control", "no-store");
	request.setCharacterEncoding("UTF-8");
	
	String mode=null;
	Vector mAttributes=null;
	DataElement dataElement=null;
	DataElement newDataElement=null;
	Vector complexAttrs=null;
	Vector fixedValues=null;
	
	ServletContext ctx = getServletContext();
	XDBApplication.getInstance(ctx);
	AppUserIF user = SecurityUtil.getUser(request);
	
	// POST request not allowed for anybody who hasn't logged in			
	if (request.getMethod().equals("POST") && user==null){
		request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}

	%>
	<%@ include file="history.jsp" %>
	<%

	// init the flag indicating if this is a common element
	boolean elmCommon = request.getParameter("common")!=null;
		
	// get values of several request parameters:
	// - mode
	// - delem_id
	// - delem_idf
	// - copy_elem_id
	// - ds_id
	// - table_id
	// - type (indicates whether element is fixed values or quantitative)
	mode = request.getParameter("mode");
	String delem_id = request.getParameter("delem_id");	
	String delem_name = request.getParameter("delem_name");
	String copy_elem_id = request.getParameter("copy_elem_id");
	String dsID = request.getParameter("ds_id");
	String tableID = request.getParameter("table_id");
	String type = request.getParameter("type"); // indicates whether element is fixed values or quantitative
	if (type!=null && type.length()==0)
		type = null;
	
	// for historic reasons reference URL uses "delem_idf" while as internally DD pages are used to "idfier"
	String idfier = "";
	String delemIdf = request.getParameter("delem_idf");
	if (delemIdf!=null && delemIdf.length()>0)
		idfier = delemIdf;
	else if (request.getParameter("idfier")!=null)
		idfier = request.getParameter("idfier");

	// security for add common element
	if (mode.equals("add") && elmCommon){
		if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/elements", "i")){
			request.setAttribute("DD_ERR_MSG", "You have no permission to create new common element!");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}

	// check missing request parameters
	if (mode == null || mode.length()==0){
		request.setAttribute("DD_ERR_MSG", "Missing request parameter: mode");
		request.getRequestDispatcher("error.jsp").forward(request, response);
		return;
	}
	if (mode.equals("add") && !elmCommon){
		if (Util.voidStr(tableID)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: table_id");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	else if (mode.equals("view")){
		if (Util.voidStr(delem_id) && Util.voidStr(delemIdf)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: delem_id or delem_idf");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	else if (mode.equals("edit")){
		if (Util.voidStr(delem_id)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: delem_id");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	else if (mode.equals("copy")){
		if (Util.voidStr(copy_elem_id)){
			request.setAttribute("DD_ERR_MSG", "Missing request parameter: copy_elem_id");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
	
	// as of Sept 2006,  parameter "action" is a helper to add some extra context to parameter "mode"
	String action = request.getParameter("action");
	if (action!=null && action.trim().length()==0) action = null;
	
	
	//// handle the POST request //////////////////////
	//////////////////////////////////////////////////
	if (request.getMethod().equals("POST")){
		
		DataElementHandler handler = null;
		Connection userConn = null;		
		try{
			userConn = user.getConnection();
			handler = new DataElementHandler(userConn, request, ctx);
			handler.setUser(user);
			try{
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
		if (mode.equals("add") || mode.equals("copy")){
			String id = handler.getLastInsertID();
			if (id!=null && id.length()>0)
				redirUrl = redirUrl + "data_element.jsp?mode=view&delem_id=" + id;
			if (history!=null)
				history.remove(history.getCurrentIndex());
		}
		else if (mode.equals("edit")){

			// if this was a "saveclose", send to view mode
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
						qs.changeParam("delem_id", handler.getCheckedInCopyID());
					redirUrl = qs.getValue();
				}
				// if this was just a save, send back to edit page
				else{
					QueryString qs = new QueryString(currentUrl);
					redirUrl = qs.getValue();
				}
			}
		}
		else if (mode.equals("delete")){
			String checkedoutCopyID = request.getParameter("checkedout_copy_id");
			if (checkedoutCopyID!=null && checkedoutCopyID.length()>0)
				redirUrl = "data_element.jsp?mode=view&delem_id=" + checkedoutCopyID;
			else
				redirUrl = "index.jsp";
			if (!elmCommon){
				if (tableID!=null && tableID.length()>0)
					redirUrl = "dstable.jsp?mode=view&table_id=" + tableID;
			}
		}
		
		response.sendRedirect(redirUrl);
		return;		
	}
	//// end of handle the POST request //////////////////////
	//////////////////////////////////////////////////////////

	// if requested by alphanumeric identifier, it means the common element's latest version is requested 
	boolean isLatestRequested = mode.equals("view") && !Util.voidStr(delemIdf);

	Dataset dataset = null;
	DsTable dsTable = null;
	String dstWorkingUser = null;
	String elmWorkingUser = null;
	String elmRegStatus = null;
	VersionManager verMan = null;
	Vector refTables = null;
	Vector otherVersions = null;
	String latestID = null;
	
	// security flag for non-common elements only
	boolean editDstPrm = false;
	
	// security flags for common elements only
	boolean editPrm = false;
	boolean editReleasedPrm = false;
	boolean canCheckout = false;
	boolean canNewVersion = false;
	boolean isMyWorkingCopy = false;
	boolean isLatestElm = false;
		
	Connection conn = null;
	DBPoolIF pool = XDBApplication.getDBPool();
	
	// the whole page's try block
	try {
		
		// get db connection, init search engine object
		conn = pool.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
		searchEngine.setUser(user);
		
		// get metadata of attributes
		mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
				
		// if not in add mode, get the element object, set some parameters based on it
		if (!mode.equals("add")){
			
			if (isLatestRequested){
				Vector v = new Vector();
				v.add("Released");
				v.add("Recorded");
				dataElement = searchEngine.getLatestElm(delemIdf, v);				
			}
			else
				dataElement = searchEngine.getDataElement(delem_id);
			
			if (dataElement==null){
				request.setAttribute("DD_ERR_MSG", "No data element found with this id number or alphanumeric identifier!");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}


			// set parameters regardless of common or non-common elements
			elmCommon = dataElement.getNamespace()==null || dataElement.getNamespace().getID()==null;
			delem_id = dataElement.getID();
			delem_name = dataElement.getShortName();
			delemIdf = dataElement.getIdentifier();
			idfier = dataElement.getIdentifier();
			type = dataElement.getType();
			if (type!=null && type.length()==0)
				type = null;			
			complexAttrs = searchEngine.getComplexAttributes(delem_id, "E", null, tableID, dsID);
			if (complexAttrs == null)
				complexAttrs = new Vector();
			
			// set parameters specific to NON-COMMON elements
			if (!elmCommon){				
				tableID = dataElement.getTableID();
				if (tableID==null || tableID.length()==0){
					request.setAttribute("DD_ERR_MSG", "Missing table id number in the non-common element object");
					request.getRequestDispatcher("error.jsp").forward(request, response);
					return;
				}
			}
			// set parameters and security flags specific to COMMON elements
			else{
				elmWorkingUser = dataElement.getWorkingUser();
				elmRegStatus = dataElement.getStatus();
				refTables = searchEngine.getReferringTables(delem_id);
				
				Vector v = null;
				if (user==null){
					v = new Vector();
					v.add("Released");
					v.add("Recorded");								
				}
				latestID = searchEngine.getLatestElmID(delemIdf, v);
				isLatestElm = latestID!=null && delem_id.equals(latestID);
				
				editPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/elements/" + delemIdf, "u");
				editReleasedPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/elements/" + delemIdf, "er");
				
				canNewVersion = !dataElement.isWorkingCopy() && elmWorkingUser==null && elmRegStatus!=null && user!=null && isLatestElm;
				if (canNewVersion){
					canNewVersion = elmRegStatus.equals("Released") || elmRegStatus.equals("Recorded");
					if (canNewVersion)
						canNewVersion = editPrm || editReleasedPrm;
				}
				
				canCheckout = !dataElement.isWorkingCopy() && elmWorkingUser==null && elmRegStatus!=null && user!=null && isLatestElm;
				if (canCheckout){
					if (elmRegStatus.equals("Released") || elmRegStatus.equals("Recorded"))
						canCheckout = editReleasedPrm;
					else
						canCheckout = editPrm || editReleasedPrm;
				}
				
				isMyWorkingCopy = elmCommon && dataElement.isWorkingCopy() &&
								  elmWorkingUser!=null && user!=null && elmWorkingUser.equals(user.getUserName());
				
				// get the element's other versions (does not include working copies)
				if (mode.equals("view"))
					otherVersions = searchEngine.getElmOtherVersions(dataElement.getIdentifier(), dataElement.getID());
			}
		}

		// if non-common element, get the table object (by this point the table id must not be null if non-common element)
		if (!elmCommon){
			
			dsTable = searchEngine.getDatasetTable(tableID);
			if (dsTable==null){
				request.setAttribute("DD_ERR_MSG", "No table found with this id number: " + tableID);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			
			// overwrite the dataset id number parameter with the value from table object
			dsID = dsTable.getDatasetID();
			if (dsID==null || dsID.length()==0){
				request.setAttribute("DD_ERR_MSG", "Missing dataset id number in the table object");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			
			dataset = searchEngine.getDataset(dsID);
			if (dataset==null){
				request.setAttribute("DD_ERR_MSG", "No dataset found with this id number: " + dsID);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			
			// set some parameters based on the table & dataset objects
			dstWorkingUser = dataset.getWorkingUser();
			editDstPrm = user!=null && dataset.isWorkingCopy() && dstWorkingUser!=null && dstWorkingUser.equals(user.getUserName());
			
			// security checks
			if (!mode.equals("view") && editDstPrm==false){
				request.setAttribute("DD_ERR_MSG", "You have no permission to do modifications in this dataset!");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			// anonymous users should not be allowed to see elements from a dataset working copy
			if (mode.equals("view") && user==null && dataset.isWorkingCopy()){
				request.setAttribute("DD_ERR_MSG", "Anonymous users are not allowed to view elements from a dataset working copy");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			// anonymous users should not be allowed to see elements from datasets that are NOT in Recorded or Released status
			if (mode.equals("view") && user==null && dataset.getStatus()!=null && !dataset.getStatus().equals("Recorded") && !dataset.getStatus().equals("Released")){
				request.setAttribute("DD_ERR_MSG", "Elements from datasets NOT in Recorded or Released status are inaccessible for anonymous users.");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			// if add mode, populate the helper newDataElement object with attribute values inherited from table & dataset
			if(mode.equals("add")){
				newDataElement = new DataElement();
				newDataElement.setDatasetID(dsID);
				newDataElement.setTableID(tableID);
				newDataElement.setAttributes(searchEngine.getSimpleAttributes(null, "E", tableID, dsID));
			}
		}

		// FOR COMMON ELEMENTS ONLY - security checks, checkin/checkout operations, dispatching of the GET request
		if (elmCommon){
			
			verMan = new VersionManager(conn, searchEngine, user);
			if (mode.equals("edit")){
				if (!dataElement.isWorkingCopy() || user==null || (elmWorkingUser!=null && !elmWorkingUser.equals(user.getUserName()))){
					request.setAttribute("DD_ERR_MSG", "You have no permission to edit this common element!");
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
		
				// check out the element
			    String copyID = verMan.checkOut(delem_id, "elm");
			    if (!delem_id.equals(copyID)){
				    // send to copy if created successfully, remove previous url (edit original) from history
				    history.remove(history.getCurrentIndex());			    
				    StringBuffer buf = new StringBuffer("data_element.jsp?mode=view&delem_id=");
				    buf.append(copyID);
			        response.sendRedirect(buf.toString());
		        }
			}
			else if (mode.equals("view")){
				// anonymous users should not be allowed to see anybody's working copy
				if (dataElement.isWorkingCopy() && user==null){
					request.setAttribute("DD_ERR_MSG", "Anonymous users are not allowed to view a working copy!");
					request.getRequestDispatcher("error.jsp").forward(request, response);
					return;
				}
				// anonymous users should not be allowed to see definitions that are NOT in Recorded or Released status
				if (user==null && elmRegStatus!=null && !elmRegStatus.equals("Recorded") && !elmRegStatus.equals("Released")){
					request.setAttribute("DD_ERR_MSG", "Definitions NOT in Recorded or Released status are inaccessible for anonymous users.");
					request.getRequestDispatcher("error.jsp").forward(request, response);
					return;
				}
				// redircet user to his working copy of this element (if such exists)
				String workingCopyID = verMan.getWorkingCopyID(dataElement);
				if (workingCopyID!=null && workingCopyID.length()>0){
					StringBuffer buf = new StringBuffer("data_element.jsp?mode=view&delem_id=");
				    buf.append(workingCopyID);
			        response.sendRedirect(buf.toString());
				}
			}
		}
		
		// prepare the page's HTML title, shown in browser title bar
		StringBuffer pageTitle = new StringBuffer();
		if (mode.equals("edit")){
			if (elmCommon)
				pageTitle.append("Edit common element");
			else
				pageTitle.append("Edit element");
		}
		else{
			if (elmCommon)
				pageTitle.append("Common element");
			else
				pageTitle.append("Element");
		}
		if (dataElement!=null && dataElement.getShortName()!=null)
			pageTitle.append(" - ").append(dataElement.getShortName());
		if (dsTable!=null && dataset!=null){
			if (dsTable.getShortName()!=null && dataset.getShortName()!=null)
				pageTitle.append("/").append(dsTable.getShortName()).append("/").append(dataset.getShortName());
		}
%>

<%
// start HTML //////////////////////////////////////////////////////////////
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
    <title><%=pageTitle.toString()%></title>
    <script type="text/javascript" src="querystring.js"></script>
    <script type="text/javascript" src="modal_dialog.js"></script>
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

		function checkIn(){
			submitCheckIn();
		}
		
		function submitCheckIn(){
			<%
			if (elmRegStatus!=null && elmRegStatus.equals("Released")){
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
				document.location.assign("data_element.jsp?mode=edit&delem_id=" + id);
			}
			else if (mode=="checkout"){
				document.location.assign("data_element.jsp?mode=view&action=checkout&delem_id=" + id);
			}
			else if (mode=="newversion"){
				document.location.assign("data_element.jsp?mode=view&action=newversion&delem_id=" + id);
			}
			else if (mode=="view"){
				document.location.assign("data_element.jsp?mode=view&delem_id=" + id);
			}
		}
		
		function submitForm(mode){

			// if element type select is present, make sure a value is selected
			if (document.forms["form1"].elements["typeSelect"]!=undefined){
				var elmTypeSelectedValue = document.forms["form1"].elements["typeSelect"].value;
				if (elmTypeSelectedValue==null || elmTypeSelectedValue==""){
					alert('Element type not specified!');
					return false;
				}
			}

			if (mode == "delete"){
				<%
				String confirmationText = "Are you sure you want to delete this element? Click OK, if yes. Otherwise click Cancel.";
				if (dataElement!=null && elmCommon){
					if (dataElement.isWorkingCopy())
						confirmationText = "This working copy will be deleted! Click OK, if you want to continue. Otherwise click Cancel.";
					else if (elmRegStatus!=null && !dataElement.isWorkingCopy() && elmRegStatus.equals("Released"))
						confirmationText = "You are about to delete a Released common element! Are you sure you want to do this? Click OK, if yes. Otherwise click Cancel.";
				}
				%>

				var b = confirm("<%=confirmationText%>");
				if (b==false) return;
				
				document.forms["form1"].elements["mode"].value = "delete";
				document.forms["form1"].submit();
				return;
			}

			// if not delete mode, do validation of inputs
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
			
			//slctAllValues();
			
			if (mode=="editclose"){
				mode = "edit";
				document.forms["form1"].elements["saveclose"].value = "true";
			}
			
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
			if (elmRegStatus!=null && elmRegStatus.equals("Released")){ %>
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
			
			var strType = document.forms["form1"].elements["typeSelect"].value;
			if (strType == null || strType.length==0)
				return;
				
			var requestQS = new Querystring();
			var arr = new Array();
			arr[0] = strType;
			requestQS.setValues_("type", arr);
			
			//alert("requestQS = " + requestQS.toString());
			
			slctAllValues();
			var s = visibleInputsToQueryString("form1");
			var inputsQS = new Querystring(s);
			inputsQS.remove("typeSelect");
			
			requestQS.removeAll(inputsQS);
			var newLocation = "data_element.jsp?" + requestQS.toString() + "&" + inputsQS.toString();
			document.location.assign(newLocation);
		}
		
		function onBodyLoad(){
			
			var formName = "form1";
			var inputName;
			var popValues;
			<%
			Hashtable qryStrHash1 = HttpUtils.parseQueryString(request.getQueryString());
			if (qryStrHash1!=null && qryStrHash1.size()>0){
				Enumeration keys = qryStrHash1.keys();
				while (keys!=null && keys.hasMoreElements()){
					String name = (String)keys.nextElement();
					String[] values = (String[])qryStrHash1.get(name);
					if (values!=null && values.length>0){
						%>
						inputName = "<%=name%>";
						popValues = new Array();							
						<%
						for (int i=0; i<values.length; i++){
							String value = values[i];
							%>
							popValues[<%=i%>] = "<%=value%>";
							<%
						}
						%>
						populateInput(formName, inputName, popValues);
						<%
					}
				}
			}
			%>
		}
		
		function changeDatatype(){
			
			<%
			if (type!=null && type.equals("CH1")){ %>
				return;<%
			}
			else{
				String datatypeID = getAttributeIdByName("Datatype", mAttributes);
				if (datatypeID!=null && datatypeID.length()>0){
					datatypeID = "attr_" + datatypeID;
					%>
					var elmDataType = document.forms["form1"].<%=datatypeID%>.value;
					if (elmDataType == null || elmDataType.length==0)
						return;
					
					var requestQS = new Querystring();
					var arr = new Array();
					arr[0] = elmDataType;
					requestQS.setValues_("elm_datatype", arr);
					requestQS.remove("<%=datatypeID%>");
					
					slctAllValues();
					var s = visibleInputsToQueryString("form1");
					var inputsQS = new Querystring(s);
					inputsQS.remove("<%=datatypeID%>");
					
					requestQS.removeAll(inputsQS);
					var newLocation = "data_element.jsp?" + requestQS.toString() + "&" + inputsQS.toString();
					document.location.assign(newLocation);
					<%
				}
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
					alert("There can not be duplicate values!");
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
String hlpScreen = "element";
if (mode.equals("view")){
	if (elmCommon && !dataElement.isWorkingCopy())
		hlpScreen = "common_element";
	else if (elmCommon && dataElement.isWorkingCopy())
		hlpScreen = "common_element_working_copy";
}

if (mode.equals("edit") && !elmCommon)
	hlpScreen = "element_edit";
else if (mode.equals("edit") && elmCommon)
	hlpScreen = "common_element_edit";
else if (mode.equals("add") && elmCommon)
	hlpScreen = "common_element_add";
else if (mode.equals("add") && !elmCommon)
	hlpScreen = "element_add";
		
// start HTML body ///////////////////////////////////////
boolean popup = request.getParameter("popup")!=null;
if (popup){
	%>
	<body class="popup" onload="onBodyLoad()">
	<div id="pagehead">
	    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
	    <div id="networktitle">Eionet</div>
	    <div id="sitetitle">Data Dictionary (DD)</div>
	    <div id="sitetagline">This service is part of Reportnet</div>    
	</div> <!-- pagehead -->
	<div id="workarea"><%
}
else{
	%>
	<body onload="onBodyLoad()">
	<div id="container">
		<jsp:include page="nlocation.jsp" flush="true">
			<jsp:param name="name" value="Data element"/>
			<jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
        </jsp:include>
		<%@ include file="nmenu.jsp" %>
		<div id="workarea">
<%
} // end if popup
%>
			
			<div id="operations">
				
				<ul>
					<%
					if (popup){ %>
						<li><a href="javascript:window.close();">Close</a></li>
						<li class="help"><a href="help.jsp?screen=<%=hlpScreen%>&amp;area=pagehelp" onclick="pop(this.href);return false;">Page help</a></li><%
					}
					%>					
					<%
					if (mode.equals("view") && user!=null && dataElement!=null && elmCommon && dataElement.getIdentifier()!=null){
						%>
						<li><a href="Subscribe?common_element=<%=Util.replaceTags(dataElement.getIdentifier())%>">Subscribe</a></li>
						<%
					}
					if (mode.equals("view") && elmCommon && !dataElement.isWorkingCopy()){
						if (user!=null || (user==null && !isLatestRequested)){							
							if (latestID!=null && !latestID.equals(dataElement.getID())){%>
								<li><a href="data_element.jsp?mode=view&amp;delem_id=<%=latestID%>">Go to newest</a></li><%
							}
						}
					}
					%>
				</ul>
			</div>
			
			<%
			String verb = "View";
			if (mode.equals("add"))
				verb = "Add";
			else if (mode.equals("edit"))
				verb = "Edit";	
			String strCommon = elmCommon ? "common" : "";
			%>
			<h1><%=verb%> <%=strCommon%> element definition</h1>
			
			<form id="form1" method="post" action="data_element.jsp" style="clear:right;margin-top:10px">
				<div style="display:none">
					<%
					if (!mode.equals("add")){ %>
						<input type="hidden" name="delem_id" value="<%=delem_id%>"/><%
					}
					else { %>
						<input type="hidden" name="dummy"/><%
					}
					%>
				</div>				
				
<!-- The buttons displayed in view mode -->

						<%
						if (mode.equals("view")){
							%>
				    		<div style="float:right;clear:left">
				    			<%
								if (!elmCommon && editDstPrm){ %>
									<input type="button" class="mediumbuttonb" value="Edit" onclick="goTo('edit', '<%=delem_id%>')"/><%
								}
								if (elmCommon && canNewVersion){%>
									<input type="button" class="smallbutton" value="New version" onclick="goTo('newversion', '<%=delem_id%>')"/><%
								}
								if (elmCommon && canCheckout){%>
									&nbsp;<input type="button" class="smallbutton" value="Check out" onclick="goTo('checkout', '<%=delem_id%>')"/><%
								}
								if ((elmCommon && canCheckout) || (!elmCommon && editDstPrm)){%>
									&nbsp;<input type="button" class="smallbutton" value="Delete" onclick="submitForm('delete')"/><%
								}
								%>
							</div><%
						}
						%>
			           
						<!-- add, save, check-in, undo check-out buttons -->
					
						<div style="float:right;clear:left">
							<%
							// add case
							if (mode.equals("add")){ %>
								<input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>
								<input type="button" class="mediumbuttonb" value="Copy"
									onclick="alert('This feature is currently disabled! Please contact helpdesk@eionet.europa.eu for more information.');"
									title="Copies data element attributes from existing data element"/>
								<%
							}
							// view case
							else if (mode.equals("view") && isMyWorkingCopy){
								%>
								<input type="button" class="mediumbuttonb" value="Edit" onclick="goTo('edit', '<%=delem_id%>')"/>
								&nbsp;<input type="button" class="mediumbuttonb" value="Check in" onclick="checkIn()" />
								&nbsp;<input type="button" class="mediumbuttonb" value="Undo checkout" onclick="submitForm('delete')"/>
								<%
							}
							// edit case
							else if (mode.equals("edit")){
								%>
								<input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&nbsp;
								<input type="button" class="mediumbuttonb" value="Save &amp; close" onclick="submitForm('editclose')"/>&nbsp;
								<input type="button" class="mediumbuttonb" value="Cancel" onclick="goTo('view', '<%=delem_id%>')"/>
								<%
							}
							%>
						</div>
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
			                    	
			                    	if (fixedValues!=null && fixedValues.size()>0){
				                    	String s = type.equals("CH1") ? "Allowable values" : "Suggested values";
			                    		quicklinks.add(s + " | values");
		                    		}
			                    	if (fKeys!=null && fKeys.size()>0)
			                    		quicklinks.add("Foreign key relations | fkeys");
			                    	if (complexAttrs!=null && complexAttrs.size()>0)
			                    		quicklinks.add("Complex attributes | cattrs");
			                    	
			                    	request.setAttribute("quicklinks", quicklinks);
			                    	%>
		                    		<jsp:include page="quicklinks.jsp" flush="true" />
						            <%
								}
								%>
								
								<!-- schema && codelist links-->
								
								<%
								// display schema link only in view mode and only for users that have a right to edit a dataset
								if (mode.equals("view")){
									boolean dispOutputs = elmCommon;
									if (dispOutputs==false)
										dispOutputs = dataset!=null && dataset.displayCreateLink("XMLSCHEMA");
									if (!popup && dispOutputs){ %>
										<div id="createbox" style="clear:right">
											<table id="outputsmenu">
												<tr>
													<td style="width:73%">
														Create an XML Schema for this element
													</td>
													<td style="width:27%">
														<a href="GetSchema?id=ELM<%=delem_id%>">
															<img style="border:0" src="images/icon_xml.jpg" width="16" height="18" alt=""/>
														</a>
													</td>
												</tr>
												<%
												if (dataElement.getType().equals("CH1") && fixedValues!=null && fixedValues.size()>0){%>
													<tr>
														<td style="width:73%">
															Get the comma-separated codelist of this element
														</td>
														<td style="width:27%">
															<a href="CodelistServlet?id=<%=dataElement.getID()%>&amp;type=ELM">
																<img style="border:0" src="images/icon_txt.gif" width="16" height="18" alt=""/>
															</a>
														</td>
													</tr>
													<tr>
														<td style="width:73%">
															Get the codelist of this element in XML format
														</td>
														<td style="width:27%">
															<a href="CodelistServlet?id=<%=dataElement.getID()%>&amp;type=ELM&amp;format=xml">
																<img style="border:0" src="images/icon_xml.jpg" width="16" height="18" alt=""/>
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
											<a href="help.jsp?screen=element&amp;area=type" onclick="pop(this.href);return false;">
												<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
											<col style="width: <%=titleWidth%>%"/>
											<col style="width: 4%"/>
											<% if (colspan==4){ %>
											<col style="width: 4%"/>
											<% } %>
											<col style="width: <%=valueWidth%>%"/>
								  		
								  			<!-- short name -->								  			
								    		<tr id="short_name_row">
												<th scope="row" class="scope-row short_name">Short name</th>
												<td class="short_name simple_attr_help">
													<a href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
								    		if (!elmCommon){
								    		
									    		// dataset
												%>
									    		<tr class="zebra<%=isOdd%>">
									    			<th scope="row" class="scope-row simple_attr_title">
														Dataset
														</th>
													<td class="simple_attr_help">
														<a href="help.jsp?screen=table&amp;area=dataset" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
														<em>
															<a href="dataset.jsp?mode=view&amp;ds_id=<%=dsID%>">
																<b><%=Util.replaceTags(dataset.getShortName())%></b>
															</a>
														</em>
														<%
														if (mode.equals("view") && dataset.isWorkingCopy()){ %>
															<span class="caution">(Working copy)</span><%
														}
														%>
														<input type="hidden" name="ds_id" value="<%=dsID%>"/>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr>
										    	<%
									    		// table
									    		%>
												<tr class="zebra<%=isOdd%>">
									    			<th scope="row" class="scope-row simple_attr_title">
														Table
													</th>
													<td class="simple_attr_help">
														<a href="help.jsp?screen=element&amp;area=table" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
														<em>
															<a href="dstable.jsp?mode=view&amp;table_id=<%=dsTable.getID()%>">
																<%=Util.replaceTags(dsTable.getShortName())%>
															</a>
														</em>
														<input type="hidden" name="table_id" value="<%=dsTable.getID()%>"/>
													</td>
													
													<%isOdd = Util.isOdd(++displayed);%>
									    		</tr><%
											} // end of dataset & table part (relevant only for non-common elements)
											%>
											
											<!-- RegistrationStatus, relevant for common elements only -->
								    		<%
								    		if (elmCommon){
									    		%>
									    		<tr class="zebra<%=isOdd%>">
													<th scope="row" class="scope-row simple_attr_title">
														RegistrationStatus
													</th>
													<td class="simple_attr_help">
														<a href="help.jsp?screen=dataset&amp;area=regstatus" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
														if (mode.equals("view")){ %>
															<%=Util.replaceTags(elmRegStatus)%>
															<%
															if (elmWorkingUser!=null){
																if (dataElement.isWorkingCopy() && user!=null && elmWorkingUser.equals(user.getUserName())){
																	%>
																	<span class="caution">(Working copy)</span><%
																}
																else{
																	%>
																	<span class="caution">(checked out by <em><%=elmWorkingUser%></em>)</span><%
																}
															}
														}
														else{ %>
															<select name="reg_status" onchange="form_changed('form1')"> <%
																Vector regStatuses = verMan.getRegStatuses();
																for (int i=0; i<regStatuses.size(); i++){
																	String stat = (String)regStatuses.get(i);
																	String selected = stat.equals(elmRegStatus) ? "selected=\"selected\"" : ""; %>
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
													<th scope="row" class="scope-row simple_attr_title">
														GIS type
													</th>
													<td class="simple_attr_help">
														<a href="help.jsp?screen=element&amp;area=GIS" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
														</a>
													</td>
													<%
													if (colspan==4){%>
														<td class="simple_attr_help">
															<img style="border:0" src="images/optional.gif" width="16" height="16" alt=""/>
														</td><%
													}
													%>
													<td class="simple_attr_value">
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
													<th scope="row" class="scope-row simple_attr_title">
														Reference URL
													</th>
													<td class="simple_attr_help">
														<a href="help.jsp?screen=dataset&amp;area=refurl" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
														</a>
													</td>
													<td class="simple_attr_value">
														<small><a href="<%=refUrl%>"><%=refUrl%></a></small>
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
								    		String attrID = null;
											String attrValue = null;
											DElemAttribute attribute = null;
											boolean isBoolean = false;
											boolean imagesQuicklinkSet = false;
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
													<th scope="row" class="scope-row simple_attr_title">
														<%=Util.replaceTags(attribute.getShortName())%>
													</th>
													<td class="simple_attr_help">
														<a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
															if (mode.equals("view") && !Util.voidStr(attrValue)){ %>
																<a href="visuals/<%=Util.replaceTags(attrValue)%>" onclick="pop(this.href);return false;">
																	<img src="visuals/<%=Util.replaceTags(attrValue)%>" style="border:0" height="100" width="100" alt=""/>
																</a><br/><%
															}
															// link
															if (mode.equals("edit") && user!=null){
																String actionText = Util.voidStr(attrValue) ? "add image" : "manage this image";
																%>
																<span class="barfont">
																	[Click <a onclick="pop(this.href);return false;" href="imgattr.jsp?obj_id=<%=delem_id%>&amp;obj_type=E&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=Util.replaceTags(dataElement.getShortName())%>&amp;attr_name=<%=Util.replaceTags(attribute.getShortName())%>"><b>HERE</b></a> to <%=Util.replaceTags(actionText)%>]
																</span><%
															}
														}
														// if view mode, display simple text
														else if (mode.equals("view") || (mode.equals("edit") && attribute.getShortName().equalsIgnoreCase("Datatype"))){ %>
															<%=Util.replaceTags(attrValue)%>
															<%
															if (mode.equals("edit") && attribute.getShortName().equalsIgnoreCase("Datatype")){
																%>
																<input type="hidden" name="attr_<%=attrID%>" value="<%=Util.replaceTags(attrValue)%>"/><%
															}																
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
																</div><%
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
																	<a onclick="pop(this.href);return false;" href="fixed_values.jsp?mode=view&amp;delem_id=<%=attrID%>&amp;delem_name=<%=Util.replaceTags(attribute.getShortName())%>&amp;parent_type=attr">
																		<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
												<th scope="row" class="scope-row simple_attr_title">
													Is ROD parameter
												</th>
												<td class="simple_attr_help">
													<a href="help.jsp?screen=element&amp;area=is_rod_param" onclick="pop(this.href);return false;">
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
													</a>
												</td>
												<%
												if (colspan==4){%>
													<td class="simple_attr_help">
														<img style="border:0" src="images/optional.gif" width="16" height="16" alt=""/>
													</td><%
												}
												%>
												<td class="simple_attr_value">
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
													<th scope="row" class="scope-row simple_attr_title">
														CheckInNo
													</th>
													<td class="simple_attr_help">
														<a href="help.jsp?screen=dataset&amp;area=check_in_no" onclick="pop(this.href);return false;">
															<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
														<%=elmVersion%>
													</td>
													
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
														<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
														<b><%=Util.replaceTags(idfier)%></b>
														<input type="hidden" name="idfier" value="<%=Util.replaceTags(delemIdf,true)%>"/><%
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
										if (type!=null && key){
																						
											String title = type.equals("CH1") ? "Allowable values" : "Suggested values";
											String helpAreaName = type.equals("CH1") ? "allowable_values_link" : "suggested_values_link";
											%>
										
											
												<!-- title & link part -->
												<h2>
														<%=title%><a id="values"></a>
													
													<%
													if (!mode.equals("view")){
														%>
														<span class="simple_attr_help">
															<a href="help.jsp?screen=element&amp;area=<%=Util.replaceTags(helpAreaName)%>" onclick="pop(this.href);return false;">
																<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img style="border:0" src="images/optional.gif" width="16" height="16" alt="optional"/>
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
															<table class="datatable subtable">
																<col style="width:20%"/>
																<col style="width:40%"/>
																<col style="width:40%"/>
																<tr>
																	<th>Value</th>
																	<th>Definition</th>
																	<th>ShortDescription</th>																	
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
																		<td>
																			<a href="<%=valueLink%>">
																				<%=Util.replaceTags(value)%>
																			</a>
																		</td>
																		<td title="<%=Util.replaceTags(defin,true)%>">
																			<%=Util.replaceTags(dispDefin)%>
																		</td>
																		<td title="<%=Util.replaceTags(shortDesc,true)%>">
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
										if (!elmCommon && (mode.equals("edit") || (mode.equals("view") && fKeys!=null && fKeys.size()>0))){
											%>										
												<!-- title & link part -->
												<h2>
													Foreign key relations<a id="fkeys"></a>													
													<%
													if (!mode.equals("view")){
														%>
														<span class="simple_attr_help">
															<a href="help.jsp?screen=element&amp;area=fks_link" onclick="pop(this.href);return false;">
																<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img style="border:0" src="images/optional.gif" width="16" height="16" alt="optional"/>
														</span><%
													}
													// the link
													if (mode.equals("edit")){
														%>
														<span class="barfont_bordered">
															[Click <a href="foreign_keys.jsp?delem_id=<%=delem_id%>&amp;delem_name=<%=Util.replaceTags(delem_name)%>&amp;ds_id=<%=dsID%>&amp;table_id=<%=tableID%>"><b>HERE</b></a> to manage foreign keys of this element]
														</span><%
													}
													%>
												</h2>
												
												<!-- table part -->
												<%												
												if (mode.equals("view") && fKeys!=null && fKeys.size()>0){%>
															<table class="datatable subtable">
																<tr>
																	<th style="width:50%">Element</th>
																	<th style="width:50%">Table</th>
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
																		<td style="width:50%">
																			<a href="data_element.jsp?delem_id=<%=fkElmID%>&amp;mode=view">
																				<%=Util.replaceTags(fkElmName)%>
																			</a>
																		</td>
																		<td style="width:50%">
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
														Tables using this common element<a id="fkeys"></a>
												</h2>
												
												<!-- table part -->
														<table class="datatable subtable">
															<col style="width: 43%"/>
															<col style="width: 43%"/>
															<col style="width: 14%"/>
															<tr>
																<th>Table</th>
																<th>Dataset</th>
																<th>Owner</th>
															</tr>
															<%
															// rows
															for (int i=0; i<refTables.size(); i++){
																
																DsTable tbl = (DsTable)refTables.get(i);
																String tblLink = "";
																String dstLink = "";
																if (isLatestRequested){
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
																	<td>
																		<a href="<%=tblLink%>">
																			<%=Util.replaceTags(tbl.getShortName())%>
																		</a>
																	</td>
																	<td>
																		<a href="<%=dstLink%>">
																			<%=Util.replaceTags(tbl.getDatasetName())%>
																		</a>
																	</td>
																	<td>
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
														Complex attributes<a id="cattrs"></a>
													
													<%
													if (!mode.equals("view")){
														%>
														<span class="simple_attr_help">
															<a href="help.jsp?screen=dataset&amp;area=complex_attrs_link" onclick="pop(this.href);return false;">
																<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="Help"/>
															</a>
														</span>
														<span class="simple_attr_help">
															<img style="border:0" src="images/mandatory.gif" width="16" height="16" alt="mandatory"/>
														</span><%
													}
													
													// the link
													if (mode.equals("edit") && user!=null){ %>
														<span class="barfont_bordered">
															[Click <a href="complex_attrs.jsp?parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=Util.replaceTags(delem_name)%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>"><b>HERE</b></a> to manage complex attributes of this element]
														</span><%
													}
													%>
												</h2>
												
												<%
												// the table
												if (mode.equals("view") && complexAttrs!=null && complexAttrs.size()>0){
													%>
															<table class="datatable" id="dataset-attributes">
																<col style="width: 29%"/>
																<col style="width: 4%"/>
																<col style="width: 63%"/>
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
																			<a href="complex_attr.jsp?attr_id=<%=attrID%>&amp;mode=view&amp;parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=Util.replaceTags(delem_name)%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>" title="Click here to view all the fields">
																				<%=Util.replaceTags(attrName)%>
																			</a>
																		</td>
																		<td>
																			<a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX" onclick="pop(this.href);return false;">
																				<img style="border:0" src="images/info_icon.gif" width="16" height="16" alt="help"/>
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
											<%
										}
										%>
										<!-- end complex attributes -->
										
										<%
										// other versions
										if (mode.equals("view") && elmCommon && otherVersions!=null && otherVersions.size()>0){
											%>
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
														<th>Element number</th>
														<th>Status</th>
														<th>Release date</th>
														<th></th>
													</tr>
												</thead>
												<tbody>
												<%
												DataElement otherVer;
												for (int i=0; i<otherVersions.size(); i++){
													otherVer = (DataElement)otherVersions.get(i);
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
																[<a href="data_element.jsp?mode=view&amp;delem_id=<%=otherVer.getID()%>">view</a>]<%
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
										%>
								        
								<!-- end dotted -->
								
						</div>
					
					<!-- end main table body -->
					
				<!-- end main table -->
				<div style="display:none">
					<input type="hidden" name="mode" value="<%=mode%>"/>
					<input type="hidden" name="check_in" value="false"/>
					<input type="hidden" name="copy_elem_id" value=""/>
					<input type="hidden" name="changed" value="0"/>
					<input type="hidden" name="saveclose" value="false"/>
					
					<%
					if (type!=null){ %>
						<input type="hidden" name="type" value="<%=type%>"/><%
					}
	
					if (elmCommon){
						String checkedoutCopyID = dataElement==null ? null : dataElement.getCheckedoutCopyID();
						if (checkedoutCopyID!=null){%>
							<input type="hidden" name="checkedout_copy_id" value="<%=checkedoutCopyID%>"/><%
						}
						if (dataElement!=null){
							String checkInNo = dataElement.getVersion();
							if (checkInNo.equals("1")){%>
								<input type="hidden" name="upd_version" value="true"/><%
							}
						}
						%>
						<input type="hidden" name="common" value="true"/><%
					}
					else{
						String dstNamespaceID = dataset.getNamespaceID();
						if (dstNamespaceID!=null && dstNamespaceID.length()>0){ %>
							<input type="hidden" name="dst_namespace_id" value="<%=dstNamespaceID%>"/><%
						}
						String tblNamespaceID = dsTable.getNamespace();
						if (tblNamespaceID!=null && tblNamespaceID.length()>0){ %>
							<input type="hidden" name="tbl_namespace_id" value="<%=tblNamespaceID%>"/><%
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
			</div> <!-- workarea -->
			<%
			if (!popup){ %>
				</div> <!-- container -->
				<jsp:include page="footer.jsp" flush="true" />
				<%
			}
			%>
			

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
	} catch (SQLException e) {
	}
}
%>
