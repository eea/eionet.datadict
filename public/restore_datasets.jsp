<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String oSearchCacheAttrName="restore_datasets_search_cache";%>
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
}%>

<%!class c_SearchResultSet {
    private boolean isSorted=false;
    private int iSortColumn=0;
    private int iSortOrder=0;

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
}

%>

<%
	request.setCharacterEncoding("UTF-8");
	
	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
	response.setHeader("Expires", Util.getExpiresDateString());

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
	
    String searchType=request.getParameter("SearchType");
	
    Vector datasets=null;
    
	DDSearchEngine searchEngine = null;
	
	Connection conn = null;
	
	DDUser user = SecurityUtil.getUser(request);
	
	boolean wrkCopies = false;
	
	VersionManager verMan = null;

	try { // start the whole page try block
		
	if (searchType != null && searchType.equals(TYPE_SEARCH)){
		
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
		
			response.sendRedirect("restore_datasets.jsp?SearchType=SEARCH&restore=true");
			return;
		}
		
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
		String version = request.getParameter("version");

		String _wrkCopies = request.getParameter("wrk_copies");
		wrkCopies = (_wrkCopies!=null && _wrkCopies.equals("true")) ? true : false;
				
		// see if looking for deleted datasets		
		/*String _restore = request.getParameter("restore");
		if (_restore!=null && _restore.equals("true")){*/
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
		/*}
		else
			datasets = searchEngine.getDatasets(params, short_name, version, oper, wrkCopies);*/
		
		verMan = new VersionManager(conn, searchEngine, user);
	}	

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Restore datasets - Data Dictionary</title>
	<script type="text/javascript">
	// <![CDATA[
		function setLocation(){
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
		}
		function openTables(uri){
			uri = uri + "&open=true";
			wElems = window.open(uri,"DatasetTables","height=500,width=750,status=yes,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=yes");
			if (window.focus) {wElems.focus()}
		}
		
		function goTo(mode){
			if (mode == "add"){
				document.location.assign('dataset.jsp?mode=add');
			}
		}
    	function showSortedList(clmn,ordr) {
    		if ((document.forms["form1"].elements["sort_column"].value != clmn)
       			|| (document.forms["form1"].elements["sort_order"].value != ordr)) {
        		document.forms["form1"].elements["sort_column"].value=clmn;
		    	document.forms["form1"].elements["sort_order"].value=ordr;
        		document.forms["form1"].submit();
    		}
		}
		
    	function deleteDataset(){
	    	<%
	    	if (restore){ %>
	    		var msg = "This will permanently delete the selected datasets! Click OK, if you want to continue. Otherwise click Cancel.";<%
	    	}
	    	else{ %>
	    		var msg = "This will delete all the datasets you have selected. " +
					  "If any of them are working copies then the corresponding " +
					  "original copies will be released. Click OK, if you want to continue. " +
					  "Otherwise click Cancel.";<%
	    	}
	    	%>
	    	
			var b = confirm(msg);
			if (b==false) return;
			
			<%
			if (!restore){ %>
				// now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
				openNoYes("yesno_dialog.html", "Do you want the selected datasets to be deleted permanently?\n(Note that working copies will always be permanently deleted)", delDialogReturn,100, 400);<%
			}
			else{ %>
				document.forms["form1"].elements["complete"].value = "true";
				deleteDatasetReady();<%
			}
			%>
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
    	
    	function doLoad(){
	    	var wasDelPrm = document.forms["form1"].elements["was_del_prm"].value;
	    	if (wasDelPrm == "true"){
	    		document.forms["form1"].elements["del_button"].disabled = false;
	    		document.forms["form1"].elements["rst_button"].disabled = false;
    		}
    	}
		// ]]>
    </script>
</head>
<body onload="doLoad()">
<div id="container">
	<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Restore datasets"/>
		<jsp:param name="helpscreen" value="restore_datasets"/>
	</jsp:include>
    <%@ include file="nmenu.jsp" %>
	<div id="workarea">
			
		<%
		if (searchType != null && searchType.equals(TYPE_SEARCH)){
        
            if (datasets == null || datasets.size()==0){
	            %>
    	        <strong>No results found!</strong></div></div><%@ include file="footer.txt" %></body></html>
        	    <%
            	return;
            }
    	}
    	%>
    	
    	<%
		if (!restore){%>
			<h1>Datasets</h1><%
		}
		else{%>
			<h1>Restore datasets</h1><%
		}
		%>
            
		<form id="form1" method="post" action="restore_datasets.jsp" onsubmit="setLocation()">
		
		<div style="padding-bottom:5px">
			<%
			if (user != null){
				if (restore){ %>
					<input type="button" name="rst_button" value="Restore selected" class="smallbutton" disabled="disabled" onclick="restoreDataset()"/>&nbsp;<%
					
				}
				%>
				<input type="button" name="del_button" value="Delete selected" class="smallbutton" disabled="disabled" onclick="deleteDataset()"/><%
			}
			%>
		</div>

		<!--  result table -->			
		<table width="700" class="sortable">
		
			<thead>
			<tr>
				<th style="width:3%">&nbsp;</th>
				<th style="width:30%;border-left:0">
					<%
					String sortedImg  = getSortedImg(1, oSortCol, oSortOrder);
					String sortedLink = getSortedLink(1, oSortCol, oSortOrder);
					%>
					<a title="Dataset" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Dataset&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
				<!--th width="20%">Version</th-->
				<th style="width:10%">
					CheckInNo
				</th>
				<th style="width:47%">
					Tables
				</th>
			</tr>
			</thead>
			<tbody>
		
			<%
			
			boolean wasDelPrm = false;
			
			if (searchType != null && searchType.equals(TYPE_SEARCH)){

				c_SearchResultSet oResultSet=new c_SearchResultSet();
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(oSearchCacheAttrName,oResultSet);
	        	
	        	DElemAttribute attr = null;
	        	
				for (int i=0; i<datasets.size(); i++){
				
					Dataset dataset = (Dataset)datasets.get(i);
					
					String ds_id = dataset.getID();
					String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
					String ds_name = Util.replaceTags(dataset.getShortName());
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";
					
					Vector tables = searchEngine.getDatasetTables(ds_id, true);
					
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
                															 
					boolean delPrm = user!=null &&
									 SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "u");
									 
					oEntry.setDelPrm(delPrm);
					if (delPrm)
						wasDelPrm = true;
					
					oEntry.oIdentifier = dataset.getIdentifier();
					oResultSet.oElements.add(oEntry);
					
					String workingUser    = verMan.getDstWorkingUser(dataset.getIdentifier());
					String topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
					
					boolean canDelete = topWorkingUser==null ||
										(dataset.isWorkingCopy() &&
										workingUser!=null && user!=null &&
										workingUser.equals(user.getUserName()));
					
					String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
										
					%>
				
					<tr valign="top" class="<%=zebraClass%>">
					
						<td style="width:3%;text-align:right">
							<%
		    				if (delPrm){
		    					
		    					if (topWorkingUser!=null){ // mark checked-out datasets
			    					%> <span title="<%=Util.replaceTags(topWorkingUser, true)%>" class="checkedout">*</span> <%
		    					}
	    					
		    					if (canDelete){ %>
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=ds_id%>"/>
									<input type="hidden" name="ds_idf_<%=dataset.getID()%>" value="<%=dataset.getIdentifier()%>"/>
									<%
								}
							}
							%>
						</td>
						
						<td style="width:30%" title="<%=Util.replaceTags(dsFullName, true)%>">
							<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;out_type=GDLN&amp;obj_id=<%=dataset.getID()%>">
								<%=Util.replaceTags(dsFullName)%>
							</a>
						</td>					
						<td style="width:20%">
							<%=dsVersion%>
						</td>
						<td style="border-right: 1px solid #C0C0C0;width:47%">
							<%
							for (int c=0; tables!=null && c<tables.size(); c++){
				
								DsTable table = (DsTable)tables.get(c);
								String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(),
			    															  table.getIdentifier(), "tbl");
			
								%>
									<%=Util.replaceTags(table.getShortName())%>
								<%
								if (user!=null && tblWorkingUser!=null){ // mark checked-out elements
									%>&nbsp;<span class="checkedout">*</span> <%
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
			else{
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
                        
												String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";

                        %>
						<tr valign="top" class="<%=zebraClass%>">	
							<%
							if (oEntry.getDelPrm()){
								wasDelPrm = true;
								%>
								<td style="width:3%;text-align:right">
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=oEntry.oID%>"/>
									<input type="hidden" name="ds_idf_<%=oEntry.oID%>" value="<%=Util.replaceTags(oEntry.oIdentifier, true)%>"/>
								</td> <%
							}
							%>
							
							<td style="width:30%" title="<%=Util.replaceTags(oEntry.oFullName, true)%>">
								<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;out_type=GDLN&amp;obj_id=<%=oEntry.oID%>">
									<%=Util.replaceTags(oEntry.oFName)%>
								</a>
							</td>
							
							<td style="width:20%">
								<%=oEntry.oVersion%>
							</td>
							
							<td style="border-right: 1px solid #C0C0C0;width:47%">
								<%
								Vector tables = oEntry.oTables;
								for (int c=0; tables!=null && c<tables.size(); c++){
				
									DsTable table = (DsTable)tables.get(c);
			
									%>
										<%=Util.replaceTags(table.getShortName())%><br/>
									<%
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
		
			<div style="display:none">
				<input type="hidden" name="was_del_prm" value="<%=wasDelPrm%>"/>
				<input type="hidden" name="searchUrl" value=""/>
		        <input type="hidden" name="sort_column" value="<%=(oSortCol==null)? "":oSortCol.toString()%>"/>
		        <input type="hidden" name="sort_order" value="<%=(oSortOrder==null)? "":oSortOrder.toString()%>"/>
				<input type="hidden" name="SearchType" value="NoSearch"/>
				<input type="hidden" name="mode" value="view"/>	
				<!-- Special input for 'delete' mode only. Inidcates if dataset(s) should be deleted completely. -->
				<input type="hidden" name="complete" value="true"/>
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
