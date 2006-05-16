<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String oSearchCacheAttrName="datasets_search_cache";%>
<%!final static String oSearchUrlAttrName="datasets_search_url";%>
<%!private Vector attributes=null;%>
<%!private boolean restore = false;%>

<%@ include file="history.jsp" %>
<%@ include file="sorting.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oShortName;
    public String oFullName;
    public String oFName;  //truncated full name
    public String oVersion;
    public Vector oTables;
    public String oIdentifier;

    private String oCompStr=null;
    private int iO=0;
    
    private boolean delPrm = false;
    private String regStatus = "";
    private String sortableStatus = "";
    public boolean clickable = false;
    
    public c_SearchResultEntry(String _oID, String _oShortName, String _oVersion, String _oFName, Vector _oTables) {
	    
            oID	= _oID==null ? "" : _oID;
            oShortName	= _oShortName==null ? "" : _oShortName;
            oFName	= _oFName==null ? "" : _oFName;
            oVersion	= _oVersion==null ? "" : _oVersion;
            oTables	= _oTables;
    		
            oFullName = oFName;

            if (oFName.length() > 60)
				oFName = oFName.substring(0,60) + " ...";
	};
    
    public void setComp(int i,int o) {
        switch(i) {
            case 1: oCompStr=oFName; break;
            case 2: oCompStr=sortableStatus; break;
            default: oCompStr=oFName; break;
		}
		
        iO=o;
	}
    
    public String toString() {
        return oCompStr;
    }

    public int compareTo(Object oC1) {
        return iO*oCompStr.compareToIgnoreCase(oC1.toString());
    }
    
    public void setDelPrm(boolean b){
	    delPrm = b;
    }
    
    public boolean getDelPrm(){
	    return delPrm;
    }
    
    public void setRegStatus(String stat){
	    regStatus = stat;
    }
    
    public String getRegStatus(){
	    return regStatus;
    }
    
    public void setSortableStatus(String sortableStatus){
	    this.sortableStatus = sortableStatus;
    }
}%>

<%!class c_SearchResultSet {
    private boolean isSorted=false;
    private int iSortColumn=0;
    private int iSortOrder=0;
    public boolean isAuth = false;

    public Vector oElements;
    public boolean SortByColumn(Integer oCol,Integer oOrder) {
        if ((iSortColumn!=oCol.intValue()) || (iSortOrder!=oOrder.intValue())) {
            for(int i=0; i<oElements.size(); i++) {
                c_SearchResultEntry oEntry=(c_SearchResultEntry)oElements.elementAt(i); 
                oEntry.setComp(oCol.intValue(),oOrder.intValue());
            }
            Collections.sort(oElements);
            return true;
        }
        return false;
    }
}%>

<%
	request.setCharacterEncoding("UTF-8");

	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setDateHeader("Expires", 0);

	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");

	
	Integer oSortCol=null;
    Integer oSortOrder=null;
    try {
    	oSortCol=new Integer(request.getParameter("sort_column"));
        oSortOrder=new Integer(request.getParameter("sort_order"));
    }
    catch(Exception e){
    	oSortCol=null;
        oSortOrder=null;
    }
    
    // if this is no sorting request, then remember the query string in session in order to come back if needed
    if (oSortCol==null){
		String query = request.getQueryString() == null ? "" : request.getQueryString();
		String searchUrl =  request.getRequestURI() + "?" + query;
       	session.setAttribute(oSearchUrlAttrName, searchUrl);
   	}
	
    String searchType=request.getParameter("SearchType");
	
    Vector datasets=null;
    
	DDSearchEngine searchEngine = null;
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	AppUserIF user = SecurityUtil.getUser(request);
	
	// The following if block tries to identify if a login has happened in which
	// case it will redirect the response to the query string in session. This
	// happens regardless of weather it's a sorting request or search request.
	c_SearchResultSet rs = (c_SearchResultSet)session.getAttribute(oSearchCacheAttrName);
	if (rs!=null){		
		if (rs.isAuth && user==null || !rs.isAuth && user!=null){
			session.removeAttribute(oSearchCacheAttrName);
			searchType = TYPE_SEARCH;
		}
	}
	
	String _wrkCopies = request.getParameter("wrk_copies");
	boolean wrkCopies = (_wrkCopies!=null && _wrkCopies.equals("true")) ? true : false;
	
	VersionManager verMan = null;

	try { // start the whole page try block
		
	if (searchType != null && searchType.equals(TYPE_SEARCH)){
		
		conn = pool.getConnection();
	
		if (request.getMethod().equals("POST")){
		
			if (user==null){ %>
				<b>Not allowed!</b> <%
				return;
			}
			else{
				String[] ds_ids = request.getParameterValues("ds_id");
				for (int i=0; ds_ids!=null && i<ds_ids.length; i++){
					String dsIdf = request.getParameter("ds_idf_" + ds_ids[i]);
					if (dsIdf==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "d")){ %>
						<b>Not allowed!</b><%
					}
				}
			}
			
      		Connection userConn = null;
      		DatasetHandler handler = null;
      		
      		try{
	      		userConn = user.getConnection();
				handler = new DatasetHandler(userConn, request, ctx);
				handler.setUser(user);
				handler.execute();
			}
			finally{
				handler.cleanup();
				try { if (userConn!=null) userConn.close();
				} catch (SQLException e) {}
			}
		}
       	
       	session.removeAttribute(oSearchCacheAttrName);
		
       	searchEngine = new DDSearchEngine(conn, "", ctx);	
       	searchEngine.setUser(user);

		String srchType = request.getParameter("search_precision");
		String oper="=";
		if (srchType != null && srchType.equals("free"))
			oper=" match ";
		if (srchType != null && srchType.equals("substr"))
			oper=" like ";

			
		Vector params = new Vector();	
		Enumeration parNames = request.getParameterNames();
		while (parNames.hasMoreElements()){
			String parName = (String)parNames.nextElement();
			if (!parName.startsWith(ATTR_PREFIX))
				continue;
		
			String parValue = request.getParameter(parName);
			if (parValue.length()==0)
				continue;
			
			DDSearchParameter param =
				new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");
		
            if (oper!= null && oper.trim().equalsIgnoreCase("like"))
				param.addValue("'%" + parValue + "%'");
			else
				param.addValue("'" + parValue + "'");
			params.add(param);
		}
		String short_name = request.getParameter("short_name");
		String idfier = request.getParameter("idfier");
		String version = request.getParameter("version");

		// see if looking for deleted datasets		
		String _restore = request.getParameter("restore");
		if (_restore!=null && _restore.equals("true")){
			if (user==null || !user.isAuthentic()){
				Exception e = new Exception("User not authorized!");
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
			restore = true;
			datasets = searchEngine.getDeletedDatasets();
		}
		else{
			datasets = searchEngine.getDatasets(params, short_name, idfier, version, oper, wrkCopies);
		}
		
		verMan = new VersionManager(conn, searchEngine, user);
	}
	
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
		<%@ include file="headerinfo.txt" %>
    <title>Datasets - Data Dictionary</title>
    <script type="text/javascript" src='modal_dialog.js'></script>
    <script type="text/javascript" language="javascript">
    // <![CDATA[
		function setLocation(){
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
		}
		
		function goTo(mode){
			if (mode == "add"){
				document.location.assign('dataset.jsp?mode=add');
			}
		}
    	function showSortedList(clmn,ordr) {
    		if ((document.forms["sort_form"].elements["sort_column"].value != clmn)
       			|| (document.forms["sort_form"].elements["sort_order"].value != ordr)) {
        		document.forms["sort_form"].elements["sort_column"].value=clmn;
		    	document.forms["sort_form"].elements["sort_order"].value=ordr;
        		document.forms["sort_form"].submit();
    		}
		}
		
    	function deleteDataset(){
	    	
	    	// first confirm if the deletetion is about to take place at all
			var b = confirm("Selected datasets will be deleted! You will be given a chance to delete them permanently or save them for restoring later. Click OK, if you want to continue. Otherwise click Cancel.");
			if (b==false) return;
				
			// now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
			openNoYes("yesno_dialog.html", "Do you want the selected datasets to be deleted permanently?\n(Note that working copies will always be permanently deleted)", delDialogReturn,100, 400);
    	}
    	
    	function delDialogReturn(){
			var v = dialogWin.returnValue;
			if (v==null || v=="" || v=="cancel") return;
			
			document.forms["form1"].elements["complete"].value = v;
			deleteDatasetReady();
		}

    	function deleteDatasetReady(){
	    	document.forms["form1"].elements["mode"].value = "delete";
	    	document.forms["form1"].elements["SearchType"].value='<%=TYPE_SEARCH%>';
       		document.forms["form1"].submit();
    	}
    	
    	function restoreDataset(){
	    	document.forms["form1"].elements["mode"].value = "restore";
	    	document.forms["form1"].elements["SearchType"].value='<%=TYPE_SEARCH%>';
       		document.forms["form1"].submit();
    	}
    	
    	function alertReleased(id){
	    	alert("A dataset definition in Released status cannot be deleted, because it might be referenced by outer sources!");
	    	var checkBoxes = document.forms["form1"].elements["ds_id"];
	    	for (var i=0; checkBoxes!=null && i<checkBoxes.length; i++){
	    		var checkBox = checkBoxes[i];
	    		if (checkBox.value==id){
		    		checkBox.checked = false;
	    		}
	    	}
    	}
    	
    	function doLoad(){
	    	if (document.forms["form1"]!=null && document.forms["form1"].elements["was_del_prm"]!=null){
		    	var wasDelPrm = document.forms["form1"].elements["was_del_prm"].value;
		    	if (wasDelPrm == "true")
		    		document.forms["form1"].elements["del_button"].disabled = false;
    		}
    	}
    // ]]>
    </script>
</head>
<body onload="doLoad()">
                  <jsp:include page="nlocation.jsp" flush='true'>
                  <jsp:param name="name" value="Datasets"/>
                  <jsp:param name="back" value="true"/>
                </jsp:include>
    <%@ include file="nmenu.jsp" %>
<div id="workarea">

			<%
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
            
	            // check if any results found
				if (datasets == null || datasets.size()==0){
					
		            // see if this is a search or just listing all the datasets
					if (Util.voidStr(request.getParameter("search_precision"))){ // listing all the datasets
						%>
						<b>No dataset definitions were found!</b><%
					}
					else{ // a search
						%>
						<b>No dataset definitions matching the search criteria were found!</b><%
					}
    	        	%>
    	        	
	    	        </div></body></html>
	        	    <%
	            	return;
	            }
        	}
            %>
            
				
				<!-- search, restore, page help buttons -->
				
				<div id="operations">
				<ul>
					<li><a target="_blank" href="help.jsp?screen=datasets&amp;area=pagehelp" onclick="pop(this.href)">Page help</a></li>
					<li><a href="search_dataset.jsp" title="Search datasets">Search</a></li>
					<%
					if (user!=null && user.isAuthentic() && !restore){%>
						<li><a href="restore_datasets.jsp?SearchType=SEARCH&amp;restore=true" title="Restore datasets">Restore</a></li><%
					}
					%>
				</ul>
				</div>
					<%
					if (!restore && wrkCopies){ %>
						<h1>Working copies of dataset definitions</h1><%
					}
					else if (!restore){%>
						<h1>Datasets</h1><%
					}
					else{%>
						<h1>Restore datasets</h1><%
					}
					%>
			
		
			
			<%
			if (user==null){ %>
				<p>	
		        		NB! For un-authenticated users dataset definitions whose Registration status<br/>
		        		is not <em>Recorded</em> or <em>Released</em> are displayed as inacessible.
			  </p><%
		    }
			%>
			
			<form id="form1" method="post" action="datasets.jsp" onsubmit="setLocation()">
			<!-- the buttons part -->
				<!-- update buttons -->
				<div style="padding-bottom:5">
					<%
					if (user != null){
						if (!wrkCopies && SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){ %>							
							<input type="button" class="smallbutton" value="Add new" onclick="goTo('add')"/><%
						}
						if (!restore && !wrkCopies){ %>
							&nbsp;<input type="button" name="del_button" value="Delete selected" class="smallbutton" disabled="disabled" onclick="deleteDataset()"/><%
						}
						else if (restore){%>
							&nbsp;<input type="button" name="rst_button" value="Restore selected" class="smallbutton" disabled="disabled" onclick="restoreDataset()"/><%
						}
					}
					%>
		
				</div>
		
		<table class="sortable" width="700" cellspacing="0" border="0" cellpadding="2">
		
			<%
			// Set the colspan. Users with no edit rights must not see the CheckInNo
			boolean userHasEditRights = user!=null && SecurityUtil.hasChildPerm(user.getUserName(), "/datasets/", "u");
			int colSpan = userHasEditRights ? 5 : 3;
			%>
		
		
			
				
			<!-- the table itself -->
	   <thead>	
			<tr>
				<%
				if (userHasEditRights){ %>
					<th width="3%">&nbsp;</th>
					<th width="32%" style="border-left:0"><%
				}
				else{ %>
					<th width="32%"><%
				}
				String sortedImg  = getSortedImg(1, oSortCol, oSortOrder);
				String sortedLink = getSortedLink(1, oSortCol, oSortOrder);
				String sortedAlt  = getSortedAlt(sortedImg);
				%>
					<a title="Dataset" href="<%=Util.replaceTags(sortedLink,true)%>">
						Dataset&nbsp;<img src="<%=Util.replaceTags(sortedImg,true)%>" width="12" height="12" alt="<%=Util.replaceTags(sortedAlt,true)%>"/>
					</a>
				</th>
				<%
				if (userHasEditRights){ %>
					<th width="10%">
						CheckInNo
					</th><%
				}
				%>
				<th width="15%">
					<%
					sortedImg  = getSortedImg(2, oSortCol, oSortOrder);
					sortedLink = getSortedLink(2, oSortCol, oSortOrder);
					sortedAlt  = getSortedAlt(sortedImg);
					%>
					<a title="Status" href="<%=Util.replaceTags(sortedLink,true)%>">
	                      Status&nbsp;<img src="<%=Util.replaceTags(sortedImg,true)%>" width="12" height="12" alt="<%=Util.replaceTags(sortedAlt,true)%>"/>
					</a>
				</th>
				<th width="40%">
					Tables
				</th>
			</tr>
      </thead>
      <tbody>
			
			<%
			
			boolean wasDelPrm = false;
			
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
				
				c_SearchResultSet oResultSet=new c_SearchResultSet();
				oResultSet.isAuth = user!=null;
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(oSearchCacheAttrName,oResultSet);
	        	
	        	DElemAttribute attr = null;
	        	
				for (int i=0; i<datasets.size(); i++){
				
					Dataset dataset = (Dataset)datasets.get(i);
					
					String regStatus = dataset!=null ? dataset.getStatus() : null;
					boolean clickable = searchEngine.skipByRegStatus(regStatus) ? false : true;
					//String linkDisabled = clickable ? "" : "disabled";
					String linkDisabled = clickable ? "" : "class=\"disabled\"";
					
					// for countries show only Recorded & Released
					/*if (regStatus!=null){
						if (user==null || !user.isAuthentic()){
							if (regStatus.equals("Incomplete") || regStatus.equals("Candidate") || regStatus.equals("Qualified"))
								continue;
						}
					}*/
					
					String ds_id = dataset.getID();
					String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
					String ds_name = Util.replaceTags(dataset.getShortName());
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";
					
					String dsLink = clickable ? "dataset.jsp?mode=view&amp;ds_id=" + ds_id : "#";
					
					Vector tables = searchEngine.getDatasetTables(ds_id);
					/*attributes = searchEngine.getAttributes(ds_id, "DS", DElemAttribute.TYPE_SIMPLE);
		
					String dsFullName=null;
					for (int c=0; c<attributes.size(); c++){
						attr = (DElemAttribute)attributes.get(c);
       					if (attr.getName().equalsIgnoreCase("Name"))
       						dsFullName = attr.getValue();
					}*/
					
					String dsFullName=dataset.getName();
					
					if (dsFullName == null) dsFullName = ds_name;
					if (dsFullName.length() == 0) dsFullName = ds_name;
					if (dsFullName.length()>60)
						dsFullName = dsFullName.substring(0,60) + " ...";
				
					c_SearchResultEntry oEntry = new c_SearchResultEntry(ds_id,
               															 ds_name,
               															 dsVersion,
               															 dsFullName,
                														 tables);
                														 
					boolean delPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
					oEntry.setDelPrm(delPrm);
					if (delPrm) wasDelPrm = true;
					
					
					oEntry.setRegStatus(regStatus);
					oEntry.setSortableStatus(Util.getStatusSortString(regStatus));
					oEntry.clickable = clickable;
					oEntry.oIdentifier = dataset.getIdentifier();
					
					oResultSet.oElements.add(oEntry);
					
					String workingUser    = verMan.getDstWorkingUser(dataset.getIdentifier());
					String topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
					
					boolean canDelete = topWorkingUser==null ||
										(dataset.isWorkingCopy() &&
										workingUser!=null && user!=null &&
										workingUser.equals(user.getUserName()));
					
					String statusImg   = "images/" + Util.getStatusImage(regStatus);
					String statusTxt   = Util.getStatusRadics(regStatus);
					String styleClass  = i % 2 != 0 ? "search_result_odd" : "search_result";
					String oddevenClass  = i % 2 != 0 ? "zebraodd" : "zebraeven";
					
					String alertReleased = regStatus.equals("Released") ? "onclick='alertReleased(" + ds_id + ")'" : "";
					%>
				
					<tr valign="top" class="<%=oddevenClass%>">
						<%
						if (delPrm){ %>
							<td width="3%" align="right" class="<%=styleClass%>">
								<%
		    					if (topWorkingUser!=null){ // mark checked-out datasets
			    					%> <font title="<%=Util.replaceTags(topWorkingUser,true)%>" color="red">*</font> <%
		    					}
		    					else if (canDelete){ %>
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=ds_id%>" <%=Util.replaceTags(alertReleased)%>/>
									<input type="hidden" name="ds_idf_<%=dataset.getID()%>" value="<%=dataset.getIdentifier()%>"/>
									<%
								}
								else{ %>
									&nbsp;<%
								}
								%>
							</td><%
						}
						%>
						
						<td width="30%" class="<%=styleClass%>" title="<%=Util.replaceTags(dsFullName,true)%>">
							<a <%=linkDisabled%> href="<%=Util.replaceTags(dsLink,true)%>">
							<%=Util.replaceTags(dsFullName)%></a>
						</td>
						
						<%
						if (userHasEditRights){ %>
							<td width="10%" class="<%=styleClass%>">
								<%
								if (clickable){ %>
									<%=Util.replaceTags(dsVersion)%><%
								}
								else{ %>
									<a disabled href="#" style="text-decoration:none"><%=Util.replaceTags(dsVersion)%></a><%
								}
								%>
							</td><%
						}
						%>
						<td width="12%" class="<%=styleClass%>">
							<%
							if (clickable){ %>
								<img border="0" src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=regStatus%>" alt="<%=regStatus%>"/><%
							}
							else{ %>
								<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=regStatus%>">
									<strong><%=statusTxt%></strong>
								</span><%
							}
							%>
						</td>
						<td width="45%" class="<%=styleClass%>">
							<%
							for (int c=0; tables!=null && c<tables.size(); c++){
				
								DsTable table = (DsTable)tables.get(c);
								String tableLink = clickable ?
												   "dstable.jsp?mode=view&amp;table_id=" +
												   table.getID() + "&amp;ds_id=" + ds_id +
												   "&amp;ds_name=" + ds_name :
												   "#";
								
								String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(),
			    															  table.getIdentifier(), "tbl");

								String tblElmWorkingUser = searchEngine.getTblElmWorkingUser(table.getID());
								
								if (wrkCopies){ %>
									<%=Util.replaceTags(table.getShortName())%><%
								}
								else{ %>
									<a <%=linkDisabled%> href="<%=tableLink%>">
										<%=Util.replaceTags(table.getShortName())%>
									</a><%
								}
								
								if (user!=null && tblWorkingUser!=null){ // mark checked-out tables
									%>&#160;<font color="red">*</font> <%
								}
								else if (tblElmWorkingUser!=null){ // mark tables having checked-out elements
									%> <font title="<%=Util.replaceTags(tblElmWorkingUser,true)%>" color="red">* </font> <%
								}
								%>
								<br/><%
							}
							%>
						</td>
					</tr>
					<%
				}
				%>
		</tbody>	
		</table>
		<p>Total results: <%=datasets.size()%></p><%
			}
			else {
				
				// No search - return from another result set or a total stranger...
                c_SearchResultSet oResultSet=(c_SearchResultSet)session.getAttribute(oSearchCacheAttrName);
                if (oResultSet==null) {
                    %><p>This page has experienced a time-out. Try searching again.</p><%
                }
                else {
	                
                    if ((oSortCol!=null) && (oSortOrder!=null))
                        oResultSet.SortByColumn(oSortCol,oSortOrder);
                    
                    c_SearchResultEntry oEntry;
                    for (int i=0;i<oResultSet.oElements.size();i++) {
	                    
                        oEntry=(c_SearchResultEntry)oResultSet.oElements.elementAt(i);
                        
                        String linkDisabled = oEntry.clickable ? "" : "class=\"disabled\"";
                        String dsLink = oEntry.clickable ? "dataset.jsp?mode=view&amp;ds_id=" + oEntry.oID : "#";
                        String statusImg = "images/" + Util.getStatusImage(oEntry.getRegStatus());
                        String statusTxt   = Util.getStatusRadics(oEntry.getRegStatus());
                        String styleClass  = i % 2 != 0 ? "search_result_odd" : "search_result";
					              String oddevenClass  = i % 2 != 0 ? "zebraodd" : "zebraeven";
                        String alertReleased = oEntry.getRegStatus().equals("Released") ? "onclick='alertReleased(" + oEntry.oID + ")'" : "";
                        
                        %>
						<tr valign="top" class="<%=oddevenClass%>">
						
							<%
							if (oEntry.getDelPrm()){
								wasDelPrm = true;
								%>
								<td width="3%" align="right" class="<%=styleClass%>">
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=oEntry.oID%>" <%=Util.replaceTags(alertReleased)%>/>
									<input type="hidden" name="ds_idf_<%=oEntry.oID%>" value="<%=Util.replaceTags(oEntry.oIdentifier,true)%>"/>
								</td><%
							}
							%>
							
							<td width="30%" class="<%=styleClass%>" title="<%=Util.replaceTags(oEntry.oFullName,true)%>">
								<a <%=linkDisabled%> href="<%=Util.replaceTags(dsLink)%>">
								<%=Util.replaceTags(oEntry.oFName)%></a>
							</td>
							
							<%
							if (userHasEditRights){ %>
								<td width="10%" class="<%=styleClass%>">
									<%=oEntry.oVersion%>
								</td><%
							}
							%>
							
							<td width="12%" class="<%=styleClass%>">
								<%
								if (oEntry.clickable){ %>
									<img border="0" src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=oEntry.getRegStatus()%>" alt="<%=oEntry.getRegStatus()%>"/><%
								}
								else{ %>
									<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=oEntry.getRegStatus()%>">
										<strong><%=statusTxt%></strong>
									</span><%
								}
								%>
							</td>
							
							<td width="45%" class="<%=styleClass%>">
								<%
								Vector tables = oEntry.oTables;
								for (int c=0; tables!=null && c<tables.size(); c++){
				
									DsTable table = (DsTable)tables.get(c);
									String tableLink = oEntry.clickable ? "dstable.jsp?mode=view&amp;table_id=" + table.getID() + "&amp;ds_id=" + oEntry.oID + "&amp;ds_name=" + oEntry.oShortName : "#";
									if (wrkCopies){ %>
										<%=Util.replaceTags(table.getShortName())%><%
									}
									else{ %>
										<a <%=linkDisabled%> href="<%=tableLink%>"><%=Util.replaceTags(table.getShortName())%></a><br/><%
									}
								}
								%>
							</td>
						</tr>
					<%
					}
                	%>
		</tbody>	
		</table>
		<p>Total results: <%=oResultSet.oElements.size()%></p><%
                }

            }
			%>
		
		<input name="was_del_prm" type="hidden" value="<%=wasDelPrm%>"/>
		<input type="hidden" name="searchUrl" value=""/>
		<input name='SearchType' type='hidden' value='<%=TYPE_SEARCH%>'/>
		
		<input type="hidden" name="mode" value="view"/>
		
		<!-- Special input for 'delete' mode only. Indicates if dataset(s) should be deleted completely. -->
		<input type="hidden" name="complete" value="false"/>
		
		<%
		if (wrkCopies){ %>
			<input name='wrk_copies' type='hidden' value='true'/><%
		}
		%>
		
		</form>
		
		<form name="sort_form" action="datasets.jsp" method="get">
			<input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
	        <input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
			<input name='SearchType' type='hidden' value='NoSearch'/>
			<%
			if (wrkCopies){ %>
				<input name='wrk_copies' type='hidden' value='true'/><%
			}
			%>
		</form>
		
			</div> <!-- workarea -->
			
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
