<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String oSearchCacheAttrName="search_cache";%>
<%!final static String oSearchUrlAttrName="search_url";%>
<%!private Vector attributes=null;%>
<%!private boolean restore = false;%>

<%@ include file="history.jsp" %>

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
	
    String searchType=request.getParameter("SearchType");
	
    Vector datasets=null;
    
	DDSearchEngine searchEngine = null;
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	AppUserIF user = SecurityUtil.getUser(request);
	
	boolean wrkCopies = false;
	
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
		
			String redirUrl = (String)session.getAttribute(oSearchUrlAttrName);
			if (redirUrl != null && redirUrl.length()!=0){
				response.sendRedirect(redirUrl);
				return;
			}
		}
		
		String query = request.getQueryString() == null ? "" : request.getQueryString();
		String searchUrl =  request.getRequestURI() + "?" + query;
       	session.setAttribute(oSearchUrlAttrName, searchUrl);

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

		String _wrkCopies = request.getParameter("wrk_copies");
		wrkCopies = (_wrkCopies!=null && _wrkCopies.equals("true")) ? true : false;
				
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
		else
			datasets = searchEngine.getDatasets(params, short_name, idfier, version, oper, wrkCopies);
		
		verMan = new VersionManager(conn, searchEngine, user);
	}
	
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
    	var dialogWin=null;
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
	    	
	    	// first confirm if the deletetion is about to take place at all
			var b = confirm("This will delete all the datasets you have selected. " +
							"If any of them are working copies then the corresponding " +
							"original copies will be released. Click OK, if you want to continue. " +
							"Otherwise click Cancel.");
			if (b==false) return;
			
			// now ask if the deletion should be complete (as opposed to settign the 'deleted' flag)
			dialogWin=window.open("dst_del_dialog.html", "", "height=130,width=400,status=yes,toolbar=no,scrollbars=no,resizable=yes,menubar=no,location=no");
			window.onfocus= checkModal;
    	}

    	function checkModal() {
   			if (dialogWin!=null && !dialogWin.closed) 
      			dialogWin.focus()
		}
    	
    	function deleteDatasetReady(){
	    	document.forms["form1"].elements["mode"].value = "delete";
	    	document.forms["form1"].elements["SearchType"].value='<%=TYPE_SEARCH%>';
       		document.forms["form1"].submit();
    	}
    	
    	function restoreDataset(){
	    	//alert("Not supported right now!");
	    	document.forms["form1"].elements["mode"].value = "restore";
	    	document.forms["form1"].elements["SearchType"].value='<%=TYPE_SEARCH%>';
       		document.forms["form1"].submit();
    	}
    	
    	function doLoad(){
	    	if (document.forms["form1"]!=null){
		    	var wasDelPrm = document.forms["form1"].elements["was_del_prm"].value;
		    	if (wasDelPrm == "true")
		    		document.forms["form1"].elements["del_button"].disabled = false;
    		}
    	}
    	
    </script>
</head>
<body onload="doLoad()">
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
                <jsp:param name="name" value="Datasets"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<%
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
            
	            if (datasets == null || datasets.size()==0){ %>		            
	    	        <b>No results found!</b>
	    	        <%
	    	        if (user==null || !user.isAuthentic()){ %>
	    	        	<br/>
	    	        		This might be due to fact that you have not been authorized and there are<br/>
	    	        		no datasets at the moment ready to be published for non-authorized users.
	    	        	<br/><%
    	        	}
    	        	%>
	    	        </div></TD></TR></table></body></html>
	        	    <%
	            	return;
	            }
        	}
            %>
            
			<form id="form1" method="POST" action="datasets.jsp" onsubmit="setLocation()">
			
		<table width="700" border="0">
		
			<tr>
				<td>
					<%
					if (!restore){%>
						<font class="head00">Datasets</font><%
					}
					else{%>
						<font class="head00">Restore datasets</font><%
					}
					%>
				</td>
			</tr>
			<tr height="10"><td></td></tr>
			
			<%
			if (false){ %>
				<tr>
					<%
					if (!restore){%>
						<td colspan="3"><span class="mainfont">
							A red wildcard (<font color="red">*</font>) means that the definition of the dataset is under work
							and cannot be deleted. Otherwise checkboxes enable to delete selected datasets.
						</td><%
					}
					else{%>
						<td colspan="3"><span class="mainfont">
							Checkboxes and 'Restore' button enable to restore the selected datasets.
						</td><%
					}
					%>
				</tr><%
			}
			%>				
			<tr height="5"><td colspan="2"></td></tr>
		</table>
		
		<table width="700" cellspacing="0" border="0" cellpadding="2">
		
			<!-- the buttons part -->
		
			<tr>
			
				<!-- update buttons -->
				
				<td colspan="2" align="left" style="padding-bottom:5">
					<% if (user != null){
						if (SecurityUtil.hasPerm(user.getUserName(), "/datasets", "i")){ %>							
							<input type="button" class="smallbutton" value="Add new" onclick="goTo('add')"/><%
						}
						if (!restore){ %>
							&nbsp;<input type="button" name="del_button" value="Delete selected" class="smallbutton" disabled onclick="deleteDataset()"/><%
						}
						else{%>
							&nbsp;<input type="button" name="rst_button" value="Restore selected" class="smallbutton" disabled onclick="restoreDataset()"/><%
						}
					}
					%>
				</td>
				
				<!-- search, restore, page help buttons -->
				
				<td align="right" colspan="3">
					<a target="_blank" href="help.jsp?screen=datasets&area=pagehelp"><img src="images/pagehelp.jpg" border=0 alt="Get some help on this page"></a><br/>
					<a href="search_dataset.jsp"><img src="images/search.jpg" border=0 alt="Search datasets"></a><br/>
					<%
					if (user!=null && user.isAuthentic() && !restore){%>
						<a href="restore_datasets.jsp?SearchType=SEARCH&amp;restore=true">
							<img src="images/restore.jpg" border=0 alt="Restore datasets">
						</a><%
					}
					%>
				</td>
			</tr>
		
			<!-- the table itself -->
		
			<tr>
				<th width="3%">&nbsp;</th>
				<th width="32%" style="border-left: 0">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Dataset"/>
			            <jsp:param name="mapName" value="Dataset"/>
			            <jsp:param name="sortColNr" value="1"/>
			            <jsp:param name="help" value="help.jsp?screen=datasets&area=dataset"/>
			        </jsp:include>
				</th>
				<!-- th width="10%">Version</th -->
				<th width="10%">
					<table width="100%">
						<tr>
							<td align="right" width="50%">
								<b>Last CheckIn No</b>
							</td>
							<td align="left" width="50%">
								<a target="_blank" href="help.jsp?screen=datasets&area=version">
									<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
								</a>
							</td>
						</tr>
					</table>
				</th>
				<th width="15%">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Status"/>
			            <jsp:param name="mapName" value="Status"/>
			            <jsp:param name="sortColNr" value="2"/>
			            <jsp:param name="help" value="help.jsp?screen=datasets&area=status"/>
			        </jsp:include>
				</th>
				<th width="40%" style="border-right: 1 solid #FF9900">
					<table width="100%">
						<tr>
							<td align="right" width="50%">
								<b>Tables</b>
							</td>
							<td align="left" width="50%">
								<a target="_blank" href="help.jsp?screen=datasets&area=tables">
									<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
								</a>
							</td>
						</tr>
					</table>
				</th>
			</tr>
			
			<%
			
			boolean wasDelPrm = false;
			
			if (searchType != null && searchType.equals(TYPE_SEARCH)){

				c_SearchResultSet oResultSet=new c_SearchResultSet();
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(oSearchCacheAttrName,oResultSet);
	        	
	        	DElemAttribute attr = null;
	        	
				for (int i=0; i<datasets.size(); i++){
				
					Dataset dataset = (Dataset)datasets.get(i);
					
					String regStatus = dataset!=null ? dataset.getStatus() : null;
					boolean clickable = searchEngine.skipByRegStatus(regStatus) ? false : true;
					String linkDisabled = clickable ? "" : "disabled";
					
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
					
					String dsLink = clickable ? "dataset.jsp?mode=view&ds_id=" + ds_id : "javascript:;";
					
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
                														 
					boolean delPrm = user!=null &&
									 SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getIdentifier(), "d");
					oEntry.setDelPrm(delPrm);
					if (delPrm)
						wasDelPrm = true;
					
					
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
					%>
				
					<tr valign="top">
					
						<td width="3%" align="right" class="<%=styleClass%>">
							<%
							if (delPrm){
		    					
		    					if (topWorkingUser!=null){ // mark checked-out datasets
			    					%> <font title="<%=topWorkingUser%>" color="red">*</font> <%
		    					}
		    					else if (canDelete){ %>
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=ds_id%>"/>
									<input type="hidden" name="ds_idf_<%=dataset.getID()%>" value="<%=dataset.getIdentifier()%>"/>
									<%
								}
								else{ %>
									&nbsp;<%
								}
							}
							else{ %>
								&nbsp;<%
							}
							%>
						</td>
						
						<td width="30%" class="<%=styleClass%>" title="<%=dsFullName%>">
							<a <%=linkDisabled%> href="<%=dsLink%>">
							<%=Util.replaceTags(dsFullName)%></a>
						</td>					
						<td width="10%" class="<%=styleClass%>">
							<%
							if (clickable){ %>
								<%=dsVersion%><%
							}
							else{ %>
								<a disabled href="javascript:;" style="text-decoration:none"><%=dsVersion%></a><%
							}
							%>
						</td>
						<td width="12%" class="<%=styleClass%>">
							<%
							if (clickable){ %>
								<img border="0" src="<%=statusImg%>" width="56" height="12"/><%
							}
							else{ %>
								<a disabled href="javascript:;" style="text-decoration:none;font-size:8pt"><b><%=statusTxt%></b></a><%
							}
							%>							
						</td>
						<td width="45%" class="<%=styleClass%>" style="border-right: 1 solid #C0C0C0">
							<%
							for (int c=0; tables!=null && c<tables.size(); c++){
				
								DsTable table = (DsTable)tables.get(c);
								String tableLink = clickable ?
												   "dstable.jsp?mode=view&table_id=" +
												   table.getID() + "&ds_id=" + ds_id +
												   "&ds_name=" + ds_name :
												   "javascript:;";
								
								String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(),
			    															  table.getIdentifier(), "tbl");

								String tblElmWorkingUser = searchEngine.getTblElmWorkingUser(table.getID());
								%>
								<!--a href="javascript:openTables('<%=tableLink%>')"><%=table.getShortName()%></a><br/-->
								<a <%=linkDisabled%> href="<%=tableLink%>">
									<%=Util.replaceTags(table.getShortName())%>
								</a>
								<%
								if (user!=null && tblWorkingUser!=null){ // mark checked-out tables
									%>&#160;<font color="red">*</font> <%
								}
								else if (tblElmWorkingUser!=null){ // mark tables having checked-out elements
									%> <font title="<%=tblElmWorkingUser%>" color="red">* </font> <%
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
        	    <tr><td colspan="5">&#160;</td></tr>
				<tr><td colspan="5">Total results: <%=datasets.size()%></td></tr><%
			}
			else{
				// No search - return from another result set or a total stranger...
                c_SearchResultSet oResultSet=(c_SearchResultSet)session.getAttribute(oSearchCacheAttrName);
                if (oResultSet==null) {
                    %><P>This page has experienced a time-out. Try searching again.<%
                }
                else {
                    if ((oSortCol!=null) && (oSortOrder!=null))
                        oResultSet.SortByColumn(oSortCol,oSortOrder);
                    
                    c_SearchResultEntry oEntry;
                    for (int i=0;i<oResultSet.oElements.size();i++) {
                        oEntry=(c_SearchResultEntry)oResultSet.oElements.elementAt(i);
                        String linkDisabled = oEntry.clickable ? "" : "disabled";
                        
                        String statusImg = "images/" + Util.getStatusImage(oEntry.getRegStatus());
                        String styleClass  = i % 2 != 0 ? "search_result_odd" : "search_result";
                        
                        %>
						<tr valign="top">	
						
							<td width="3%" align="right" class="<%=styleClass%>">
								<%
								if (oEntry.getDelPrm()){
									wasDelPrm = true;
									%>
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=oEntry.oID%>"/>
									<input type="hidden" name="ds_idf_<%=oEntry.oID%>" value="<%=oEntry.oIdentifier%>"/><%
								}
								%>
							</td>
							
							<td width="30%" class="<%=styleClass%>" title="<%=oEntry.oFullName%>">
								<a <%=linkDisabled%> href="dataset.jsp?ds_id=<%=oEntry.oID%>&amp;mode=view">
								<%=Util.replaceTags(oEntry.oFName)%></a>
							</td>
							
							<td width="10%" class="<%=styleClass%>">
								<%=oEntry.oVersion%>
							</td>
							
							<td width="12%" class="<%=styleClass%>">
								<img border="0" src="<%=statusImg%>" width="56" height="12"/>
							</td>
							
							<td width="45%" class="<%=styleClass%>" style="border-right: 1 solid #C0C0C0">
								<%
								Vector tables = oEntry.oTables;
								for (int c=0; tables!=null && c<tables.size(); c++){
				
									DsTable table = (DsTable)tables.get(c);
									String tableLink = "dstable.jsp?mode=view&table_id=" + table.getID() + "&ds_id=" + oEntry.oID + "&ds_name=" + oEntry.oShortName;
			
									%>
									<!--a href="javascript:openTables('<%=tableLink%>')"><%=table.getShortName()%></a><br/-->
									<a <%=linkDisabled%> href="<%=tableLink%>"><%=Util.replaceTags(table.getShortName())%></a><br/>
									<%
								}
								%>
							</td>
						</tr>
					<%
					}
                	%>
                	<tr><td colspan="5">&#160;</td></tr>
					<tr><td colspan="5">Total results: <%=oResultSet.oElements.size()%></td></tr><%
                }

            }
			%>
			
		</table>
		
		<input name="was_del_prm" type="hidden" value="<%=wasDelPrm%>"/>
		<input type="hidden" name="searchUrl" value=""/>
        <input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
        <input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
		<input name='SearchType' type='hidden' value='NoSearch'/>
		
		<input type="hidden" name="mode" value="view"/>
		
		<!-- Special input for 'delete' mode only. Inidcates if dataset(s) should be deleted completely. -->
		<input type="hidden" name="complete" value="false"/>
		
		</form>
		
			<jsp:include page="footer.jsp" flush="true">
			</jsp:include>
			
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
