<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
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

    private String regStatus = "";
    private String sortableStatus = "";
    public boolean clickable = false;
    public String workingUser = null;
    public boolean canDelete = false;

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
            case 3: oCompStr=oID; break;
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
	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
	response.setHeader("Expires", Util.getExpiresDateString());

	request.setCharacterEncoding("UTF-8");

	ServletContext ctx = getServletContext();

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

    Vector datasets=null;
	DDSearchEngine searchEngine = null;
	Connection conn = null;
	DDUser user = SecurityUtil.getUser(request);
	String _isSearchForWorkingCopies = request.getParameter("wrk_copies");
	boolean isSearchForWorkingCopies = (_isSearchForWorkingCopies!=null && _isSearchForWorkingCopies.equals("true")) ? true : false;
	boolean isIncludeHistoricVersions = request.getParameter("incl_histver")!=null && request.getParameter("incl_histver").equals("true");

	String pageMode = request.getParameter("sort_column")!=null ? "sort" : "search";

	try { // start the whole page try block

		if (pageMode.equals("search")){

		conn = ConnectionUtil.getConnection();

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
				return;
			}
			restore = true;
			datasets = searchEngine.getDeletedDatasets();
		}
		else{
			HashSet statuses = null;
			String requestedStatus = request.getParameter("reg_status");
			if (requestedStatus!=null && requestedStatus.length()>0){
				statuses = new HashSet();
				statuses.add(requestedStatus);
			}
			datasets = searchEngine.getDatasets(params, short_name, idfier, version, oper, isSearchForWorkingCopies, isIncludeHistoricVersions, statuses);
		}
}
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
		<%@ include file="headerinfo.txt" %>
    <title>Datasets - Data Dictionary</title>
    <script type="text/javascript" src="modal_dialog.js"></script>
    <script type="text/javascript">
    // <![CDATA[

		function setLocation(){
			if (document.forms["form1"].searchUrl)
				document.forms["form1"].searchUrl.value = document.location.href;
		}

		function goTo(mode){
			if (mode == "add"){
				document.location.assign("dataset.jsp?mode=add");
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
			if (b==false)
				return;

			// now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
			openNoYes("yesno_dialog.html", "Do you want the selected datasets to be deleted permanently?\n(Note that working copies will always be permanently deleted)", delDialogReturn,100, 400);
    	}

    	function delDialogReturn(){
			var v = dialogWin.returnValue;
			if (v==null || v=="" || v=="cancel")
				return;

			document.forms["form1"].elements["complete"].value = v;
			deleteDatasetReady();
		}

    	function deleteDatasetReady(){
	    	document.forms["form1"].elements["mode"].value = "delete";
       		document.forms["form1"].submit();
    	}

    	function restoreDataset(){
	    	document.forms["form1"].elements["mode"].value = "restore";
       		document.forms["form1"].submit();
    	}

    	function alertReleased(chkbox){
	    	if (chkbox.checked==true)
	    		alert("Please note that you selected a dataset in Released status!");
    	}

    	function doLoad(){
	    	if (document.forms["form1"] && document.forms["form1"].elements["count_checkboxes"] && document.forms["form1"].elements["del_button"]){
		    	if (document.forms["form1"].elements["count_checkboxes"].value <= 0){
		    		document.forms["form1"].elements["del_button"].disabled = true;
		    	}
    		}
    	}

    // ]]>
    </script>
</head>
<body onload="doLoad()">
<div id="container">
	<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Datasets"/>
		<jsp:param name="helpscreen" value="datasets"/>
	</jsp:include>
    <%@ include file="nmenu.jsp" %>
<div id="workarea">

				<!-- search, restore -->
				<div id="operations">
				<ul>
					<li><a href="search_dataset.jsp" title="Search datasets">Search</a></li>
				</ul>
				</div>
					<%
					if (!restore && isSearchForWorkingCopies){ %>
						<h1>Working copies of dataset definitions</h1><%
					}
					else if (!restore){
						String strAllOrLatest = isIncludeHistoricVersions ? "All " : "Latest";
						%>
						<h1><%=strAllOrLatest%> versions of datasets in any status</h1><%
					}
					else{%>
						<h1>Restore datasets</h1><%
					}
			if (user != null){
			%>
				<div id="auth-operations">
				<h2>Operations:</h2>
				<ul>
					<%
					if (user.isAuthentic() && !restore){%>
						<li><a href="restore_datasets.jsp?SearchType=SEARCH&amp;restore=true" title="Restore datasets">Restore</a></li><%
					}
					// update buttons
					if (!isSearchForWorkingCopies && SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")) {
					%>
					<li><a href="dataset.jsp?mode=add">Add</a></li>
					<%
					}
					if (!isSearchForWorkingCopies) {
					%>
					<li><a href="javascript:deleteDataset()">Delete selected</a></li>
					<%
					}
					%>
				</ul>
				</div>


			<%
			}
			else { %>
				<p class="advise-msg">Note: Datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.</p><%
		    }
			%>
			<form id="form1" method="post" action="datasets.jsp" onsubmit="setLocation()">
			<!-- the buttons part -->
				<%
				if (pageMode.equals("search")){

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

		    	        </div></div><%@ include file="footer.txt" %></body></html>
		        	    <%
		            	return;
		            }
	        	}
	            %>

		<table class="sortable" width="100%" style="clear:both">

			<%
			// temporarly we do not display version aka CheckInNo, because for the time being it doesn't function properly anyway
			boolean isDisplayVersionColumn = isIncludeHistoricVersions;//false;//user!=null;
			boolean isDisplayHelperColumn = user!=null;

			int colSpan = 3;
			if (isDisplayHelperColumn)
				colSpan++;
			if (isDisplayVersionColumn)
				colSpan++;

			if (isDisplayHelperColumn){ %>
				<col style="width: 3%"/>
				<col style="width: 32%"/><%
			}
			else { %>
				<col style="width: 35%"/><%
			}

			if (isDisplayVersionColumn){ %>
				<col style="width: 10%"/>
				<col style="width: 15%"/>
				<col style="width: 40%"/><%
			}
		    else { %>
				<col style="width: 20%"/>
				<col style="width: 45%"/><%
			}
			%>

			<!-- the table itself -->
	   <thead>
			<tr>
				<%
				if (isDisplayHelperColumn){%>
					<th></th><%
				}
				String sortedImg  = getSortedImg(1, oSortCol, oSortOrder);
				String sortedLink = getSortedLink(1, oSortCol, oSortOrder);
				String sortedAlt  = getSortedAlt(sortedImg);
				%>
				<th>
					<a title="Sort on Dataset" href="<%=Util.replaceTags(sortedLink,true)%>">
						Dataset&nbsp;<img src="<%=Util.replaceTags(sortedImg,true)%>" width="12" height="12" alt="<%=Util.replaceTags(sortedAlt,true)%>"/>
					</a>
				</th>
				<%
				if (isDisplayVersionColumn){
					sortedImg  = getSortedImg(3, oSortCol, oSortOrder);
					sortedLink = getSortedLink(3, oSortCol, oSortOrder);
					sortedAlt  = getSortedAlt(sortedImg);
					%>
					<th>
						<a title="Sort on Version" href="<%=Util.replaceTags(sortedLink,true)%>">
	                      Version&nbsp;<img src="<%=Util.replaceTags(sortedImg,true)%>" width="12" height="12" alt="<%=Util.replaceTags(sortedAlt,true)%>"/>
						</a>
					</th><%
				}
				%>
				<th>
					<%
					sortedImg  = getSortedImg(2, oSortCol, oSortOrder);
					sortedLink = getSortedLink(2, oSortCol, oSortOrder);
					sortedAlt  = getSortedAlt(sortedImg);
					%>
					<a title="Sort on Status" href="<%=Util.replaceTags(sortedLink,true)%>">
	                      Status&nbsp;<img src="<%=Util.replaceTags(sortedImg,true)%>" width="12" height="12" alt="<%=Util.replaceTags(sortedAlt,true)%>"/>
					</a>
				</th>
				<th>
					Tables
				</th>
			</tr>
      </thead>
      <tbody>

			<%
			DElemAttribute attr = null;
			int countCheckboxes = 0;
			if (pageMode.equals("search")){

				c_SearchResultSet oResultSet=new c_SearchResultSet();
				oResultSet.isAuth = user!=null;
	        	oResultSet.oElements=new Vector();
	        	session.setAttribute(oSearchCacheAttrName,oResultSet);

				for (int i=0; i<datasets.size(); i++){

					Dataset dataset = (Dataset)datasets.get(i);

					String ds_id = dataset.getID();
					Vector tables = searchEngine.getDatasetTables(ds_id);
					String regStatus = dataset.getStatus();
					boolean clickable = searchEngine.skipByRegStatus(regStatus) ? false : true;
					String linkDisabled = clickable ? "" : "class=\"disabled\"";
					String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
					String ds_name = Util.replaceTags(dataset.getShortName());
					String dsLink = clickable ? "dataset.jsp?mode=view&amp;ds_id=" + ds_id : "#";
					String dsFullName=dataset.getName();
					if (dsFullName!=null && dsFullName.length()>60)
						dsFullName = dsFullName.substring(0,60) + " ...";
					String workingUser = dataset.getWorkingUser();

					String statusImg   = "images/" + Util.getStatusImage(regStatus);
					String statusTxt   = Util.getStatusRadics(regStatus);
					String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
					String alertReleased = regStatus.equals("Released") ? "onclick=\"alertReleased(this)\"" : "";

					boolean canDelete = !dataset.isWorkingCopy() && workingUser==null && regStatus!=null && user!=null;
					if (canDelete){
						boolean editPrm = SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
						boolean editReleasedPrm = SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "er");
						if (regStatus.equals("Released") || regStatus.equals("Recorded"))
							canDelete = editReleasedPrm;
						else
							canDelete = editPrm || editReleasedPrm;
					}

					c_SearchResultEntry oEntry = new c_SearchResultEntry(ds_id,
               															 ds_name,
               															 dsVersion,
               															 dsFullName,
                														 tables);
					oEntry.setRegStatus(regStatus);
					oEntry.workingUser = workingUser;
					oEntry.setSortableStatus(Util.getStatusSortString(regStatus));
					oEntry.clickable = clickable;
					oEntry.oIdentifier = dataset.getIdentifier();
					oEntry.canDelete = canDelete;

					oResultSet.oElements.add(oEntry);
					%>

					<tr valign="top" class="<%=zebraClass%>">
						<%
						// the 1st column: checkbox, red asterisk or nbsp
						if (isDisplayHelperColumn){ %>
							<td align="right">
								<%
								if (canDelete){
									%>
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=ds_id%>" <%=alertReleased%>/>
									<input type="hidden" name="ds_idf_<%=dataset.getID()%>" value="<%=dataset.getIdentifier()%>"/>
									<%
									countCheckboxes++;
								}
								else if (workingUser!=null){ %>
									<div title="<%=Util.replaceTags(workingUser,true)%>" class="checkedout">*</div><%
		    					}
		    					else{ %>
		    						&nbsp;<%
	    						}
								%>
							</td><%
						}

						// the 2nd column: full name link
						if (clickable==false){%>
							<td title="<%=Util.replaceTags(dsFullName,true)%>" class="disabled">
								<%=Util.replaceTags(dsFullName, true)%>
							</td><%
						}
						else{ %>
							<td title="<%=Util.replaceTags(dsFullName,true)%>">
								<a href="<%=Util.replaceTags(dsLink,true)%>">
									<%=Util.replaceTags(dsFullName, true)%>
								</a>
							</td><%
						}
						%>

						<%
						// 3rd column: version aka CheckInNo
						if (isDisplayVersionColumn){ %>
							<td>
								<%=dataset.getID()%>
							</td><%
						}

						// 4th column: Registration status
						%>
						<td>
							<%
							if (clickable){ %>
								<img style="border:0" src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=regStatus%>" alt="<%=regStatus%>"/><%
							}
							else{ %>
								<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=regStatus%>">
									<strong><%=statusTxt%></strong>
								</span><%
							}
							%>
						</td>
						<%
						// 5th column: tables in this dataset
						%>
						<td>
							<%
							for (int c=0; tables!=null && c<tables.size(); c++){

								DsTable table = (DsTable)tables.get(c);
								StringBuffer tableLink = new StringBuffer("dstable.jsp?mode=view&amp;table_id=");
								tableLink.append(table.getID()).append("&amp;ds_id=").append(ds_id).append("&amp;ds_name=").append(ds_name);

								// it is probably less confusing if there are no links for tables of working copies
								if (isSearchForWorkingCopies){ %>
									<%=Util.replaceTags(table.getShortName())%><%
								}
								else{
									if (clickable){ %>
										<a href="<%=tableLink%>">
											<%=Util.replaceTags(table.getShortName())%>
										</a><%
									}
									else{ %>
										<span class="disabled"><%=Util.replaceTags(table.getShortName())%></span><%
									}
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
					              String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
                        String alertReleased = oEntry.getRegStatus().equals("Released") ? "onclick=\"alertReleased(this)\"" : "";
                        %>
						<tr valign="top" class="<%=zebraClass%>">

							<%
							// the 1st column: checkbox, red asterisk or nbsp
							if (isDisplayHelperColumn){%>
								<td align="right">
									<%
									if (oEntry.canDelete){%>
										<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=oEntry.oID%>" <%=alertReleased%>/>
										<input type="hidden" name="ds_idf_<%=oEntry.oID%>" value="<%=Util.replaceTags(oEntry.oIdentifier,true)%>"/>
										<%
										countCheckboxes++;
									}
			    					else if (oEntry.workingUser!=null){%>
			    						<div title="<%=Util.replaceTags(oEntry.workingUser,true)%>" class="checkedout">*</div><%
			    					}
									else{ %>
										&nbsp;<%
									}
									%>
								</td><%
							}

							// 2nd column: full name link
							if (oEntry.clickable==false){%>
								<td title="<%=Util.replaceTags(oEntry.oFullName,true)%>" class="disabled">
									<%=Util.replaceTags(oEntry.oFullName, true)%>
								</td><%
							}
							else{%>
								<td title="<%=Util.replaceTags(oEntry.oFullName,true)%>">
									<a href="<%=Util.replaceTags(dsLink,true)%>">
										<%=Util.replaceTags(oEntry.oFullName, true)%>
									</a>
								</td><%
							}

							// 3nd column: version aka CheckInNo
							if (isDisplayVersionColumn){ %>
								<td>
									<%=oEntry.oID%>
								</td><%
							}

							// 4th column: Registration status
							%>
							<td>
								<%
								if (oEntry.clickable){ %>
									<img style="border:0" src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=oEntry.getRegStatus()%>" alt="<%=oEntry.getRegStatus()%>"/><%
								}
								else{ %>
									<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=oEntry.getRegStatus()%>">
										<strong><%=statusTxt%></strong>
									</span><%
								}
								%>
							</td>
							<%
							// 5th column: tables in this dataset
							%>
							<td>
								<%
								Vector tables = oEntry.oTables;
								for (int c=0; tables!=null && c<tables.size(); c++){

									DsTable table = (DsTable)tables.get(c);
									StringBuffer tableLink = new StringBuffer("dstable.jsp?mode=view&amp;table_id=");
									tableLink.append(table.getID()).append("&amp;ds_id=").
									append(oEntry.oID).append("&amp;ds_name=").append(oEntry.oShortName);

									// it is probbaly less confusing if there are no links for tables of working copies
									if (isSearchForWorkingCopies){ %>
										<%=Util.replaceTags(table.getShortName())%><%
									}
									else{
										if (oEntry.clickable){%>
											<a href="<%=tableLink%>">
												<%=Util.replaceTags(table.getShortName())%>
											</a><%
										}
										else{%>
											<span class="disabled"><%=Util.replaceTags(table.getShortName())%></span><%
										}
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
			<p>
				Total results: <%=oResultSet.oElements.size()%>
			</p><%
                }
            }
			%>

			<div style="display:none">
				<input type="hidden" name="searchUrl" value=""/>
				<input type="hidden" name="mode" value="view"/>
				<input type="hidden" name="complete" value="false"/>
				<%
				if (isSearchForWorkingCopies){ %>
					<input name="wrk_copies" type="hidden" value="true"/><%
				}
				if (isIncludeHistoricVersions){%>
					<input name="incl_histver" type="hidden" value="true"/><%
				}
				// helper hidden input so that we can disable delete button if no checkboxes were displayed
				%>
				<input name="count_checkboxes" type="hidden" value="<%=countCheckboxes%>"/>
			</div>
		</form>

		<form id="sort_form" action="datasets.jsp" method="get">
			<div style="display:none">
				<input name="sort_column" type="hidden" value="<%=(oSortCol==null)? "":oSortCol.toString()%>"/>
		        <input name="sort_order" type="hidden" value="<%=(oSortOrder==null)? "":oSortOrder.toString()%>"/>
				<%
				if (isSearchForWorkingCopies){ %>
					<input name="wrk_copies" type="hidden" value="true"/><%
				}
				if (isIncludeHistoricVersions){%>
					<input name="incl_histver" type="hidden" value="true"/><%
				}
				%>
			</div>
		</form>

</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.txt" %>
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
