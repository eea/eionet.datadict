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
					String dsn = request.getParameter("ds_name_" + ds_ids[i]);
					if (dsn==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsn, "d")){ %>
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
			}
			restore = true;
			datasets = searchEngine.getDeletedDatasets();
		/*}
		else
			datasets = searchEngine.getDatasets(params, short_name, version, oper, wrkCopies);*/
		
		verMan = new VersionManager(conn, searchEngine, user);
	}	

%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
    <script language="JavaScript">
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
			window.open("dst_del_dialog.html", "", "height=130,width=300,status=yes,toolbar=no,scrollbars=no,resizable=yes,menubar=no,location=no");
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
    	
    	function deleteForGood(){
	    	document.forms["form1"].elements["complete"].value = "true";
	    	var b = confirm("This will delete the selected datasets permanently! Click OK, if you want to continue. " +
							"Otherwise click Cancel.");
			if (b==false) return;
			deleteDatasetReady();
    	}
    	
    	function doLoad(){
	    	var wasDelPrm = document.forms["form1"].elements["was_del_prm"].value;
	    	if (wasDelPrm == "true"){
	    		document.forms["form1"].elements["del_button"].disabled = false;
	    		document.forms["form1"].elements["rst_button"].disabled = false;
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
            
	            if (datasets == null || datasets.size()==0){
		            %>
	    	        <b>No results found!</b></div></TD></TR></table></body></html>
	        	    <%
	            	return;
	            }
        	}
            %>
            
			<form id="form1" method="POST" action="restore_datasets.jsp" onsubmit="setLocation()">
			
		<table width="450" border="0">
		
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
			if (user!=null){ %>
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
							Clicking on dataset will generate PDF where you can see the details.
						</td><%
					}
					%>
				</tr><%
			}
			%>				
			<tr height="5"><td colspan="2"></td></tr>
		</table>
		
		<table width="auto" cellspacing="0" border="0">
		
			<%
			if (!restore){%>
				<tr>
					<td></td>
					<td colspan="3" align="left" style="padding-bottom:5">
						<% if (user != null){
							%>
							<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>
							<%
						}
						%>
					</td>
					<td align="right">
						<a href="search_dataset.jsp"><img src="images/search_ds.gif" border=0 alt="Search datasets"></a><br/>
						<%
						if (user!=null && user.isAuthentic()){%>					
							<a href="datasets.jsp?SearchType=SEARCH&amp;restore=true">
								<img src="images/restore_dataset.gif" border=0 alt="Restore datasets">
							</a><%
						}
						%>
					</td>
				</tr><%
			}
			%>
			
			<tr height="5"><td colspan="5"></td></tr>
		
			<tr>
				<% if (user != null){%>
					<td align="right" style="padding-right:10">
						<%
						if (!restore){%>
							<input type="button" name="del_button" disabled value="Delete" class="smallbutton" onclick="deleteDataset()"/><%
						}
						else { %>
							<input type="button" name="del_button" value="Delete" disabled class="smallbutton" onclick="deleteForGood()"/><br/>
							<input type="button" name="rst_button" value="Restore" disabled class="smallbutton" onclick="restoreDataset()"/><%
						}
						%>
					</td>
				<% } else {%>
					<td></td>
				<% } %>
				<th align="left" style="padding-left:5;padding-right:10">Dataset name</th>
				<th align="right" style="padding-left:5;padding-right:5">
					<table border="0" width="auto">
						<tr>
							<th align="right">
								<a href="javascript:showSortedList(1, 1)"><img src="images/sort_asc.gif" border="0" title="Sort ascending by name"/></a>
							</th>
							<th align="right">
								<a href="javascript:showSortedList(1, -1)"><img src="images/sort_desc.gif" border="0"title="Sort descending by name"/></a>
							</th>
						</tr>
					</table>
				</th>
				<th align="left" style="padding-right:10">Version</th>
				<th align="left" style="padding-right:10">Tables</th>
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
					
					String ds_id = dataset.getID();
					String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
					String ds_name = Util.replaceTags(dataset.getShortName());
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";
					
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
									 SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dataset.getShortName(), "d");
									 
					oEntry.setDelPrm(delPrm);
					if (delPrm)
						wasDelPrm = true;
					
					oResultSet.oElements.add(oEntry);
					
					String workingUser    = verMan.getDstWorkingUser(dataset.getIdentifier());
					String topWorkingUser = verMan.getWorkingUser(dataset.getNamespaceID());
					
					boolean canDelete = topWorkingUser==null ||
										(dataset.isWorkingCopy() &&
										workingUser!=null && user!=null &&
										workingUser.equals(user.getUserName()));
										
					%>
				
					<tr valign="top">
					
						<td align="right" style="padding-right:10">
							<%
	    					//if (user!=null){
		    				if (delPrm){
		    					
		    					if (topWorkingUser!=null){ // mark checked-out datasets
			    					%> <font title="<%=topWorkingUser%>" color="red">*</font> <%
		    					}
	    					
		    					if (canDelete){ %>
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=ds_id%>"/>
									<input type="hidden" name="ds_name_<%=dataset.getID()%>" value="<%=dataset.getShortName()%>"/>
									<%
								}
							}
							%>
						</td>
						
						<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2" title="<%=dsFullName%>">
							<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;out_type=GDLN&amp;obj_id=<%=dataset.getID()%>">
								<%=Util.replaceTags(dsFullName)%>
							</a>
						</td>					
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
							<%=dsVersion%>
						</td>
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
							<!-- style="border-bottom-color:#10847B;border-bottom-style:solid;border-bottom-width:1pt;"-->
							<%
							for (int c=0; tables!=null && c<tables.size(); c++){
				
								DsTable table = (DsTable)tables.get(c);
								String tableLink = "dstable.jsp?mode=view&table_id=" + table.getID() + "&ds_id=" + ds_id + "&ds_name=" + ds_name;
								
								String tblWorkingUser = verMan.getWorkingUser(table.getParentNs(),
			    															  table.getIdentifier(), "tbl");
			
								%>
								<!--a href="javascript:openTables('<%=tableLink%>')"><%=table.getShortName()%></a><br/-->
									<%=Util.replaceTags(table.getShortName())%>
								<%
								if (user!=null && tblWorkingUser!=null){ // mark checked-out elements
									%>&#160;<font color="red">*</font> <%
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
        	    <tr><td colspan="8">&#160;</td></tr>
				<tr><td colspan="8">Total results: <%=datasets.size()%></td></tr><%
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

                        %>
						<tr valign="top">	
							<%
							//if (user != null){
							if (oEntry.getDelPrm()){
								wasDelPrm = true;
								%>
								<td align="right" style="padding-right:10">
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=oEntry.oID%>"/>
									<input type="hidden" name="ds_name_<%=oEntry.oID%>" value="<%=oEntry.oShortName%>"/>
								</td> <%
							}
							%>
							<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2"  title="<%=oEntry.oFullName%>">
								<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;out_type=GDLN&amp;obj_id=<%=oEntry.oID%>">
									<%=Util.replaceTags(oEntry.oFName)%>
								</a>
							</td>					
							<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
								<%=oEntry.oVersion%>
							</td>
							<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
								<!-- style="border-bottom-color:#10847B;border-bottom-style:solid;border-bottom-width:1pt;"-->
								<%
								Vector tables = oEntry.oTables;
								for (int c=0; tables!=null && c<tables.size(); c++){
				
									DsTable table = (DsTable)tables.get(c);
									String tableLink = "dstable.jsp?mode=view&table_id=" + table.getID() + "&ds_id=" + oEntry.oID + "&ds_name=" + oEntry.oShortName;
			
									%>
									<!--a href="javascript:openTables('<%=tableLink%>')"><%=table.getShortName()%></a><br/-->
										<%=Util.replaceTags(table.getShortName())%><br/>
									<%
								}
								%>
							</td>
						</tr>
					<%
					}
                	%>
                	<tr><td colspan="8">&#160;</td></tr>
					<tr><td colspan="8">Total results: <%=oResultSet.oElements.size()%></td></tr><%
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
		<input type="hidden" name="complete" value="true"/>
		
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
