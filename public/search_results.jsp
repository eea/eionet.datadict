<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!static int iPageLen=0;%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String oSearchCacheAttrName="search_cache";%>
<%!final static String oSearchUrlAttrName="search_url";%>

<%@ include file="history.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oType;
    public String oShortName;
    public String oDsName;
    public String oTblName;
    public String oNs;

    private String oCompStr=null;
    private int iO=0;
    
    boolean delPrm = false;
    
    public c_SearchResultEntry(String _oID,String _oType,String _oShortName,String _oDsName,String _oTblName, String _oNs) {
	    
            oID	= _oID==null ? "" : _oID;
            oType  = _oType==null ? "" : _oType;
            oShortName	= _oShortName==null ? "" : _oShortName;
            oDsName	= _oDsName==null ? "" : _oDsName;
            oTblName= _oTblName==null ? "" : _oTblName;
            oNs	= _oNs==null ? "" : _oNs;
    		
	};
    
    public void setComp(int i,int o) {
        switch(i) {
            case 2: oCompStr=oType; break;
            case 3: oCompStr=oTblName; break;
            case 4: oCompStr=oDsName; break;
            default: oCompStr=oShortName; break;
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
}%>

<%
	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	
	String type = request.getParameter("type");
	String ns_param = request.getParameter("ns");
	String short_name = request.getParameter("short_name");
	String dataset = request.getParameter("dataset");
	
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
	
    Vector dataElements=null;
    
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);

	DDSearchEngine searchEngine = null;
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	boolean wrkCopies = false;
	
	try { // start the whole page try block
	
	if (searchType != null && searchType.equals(TYPE_SEARCH)){

       	conn = pool.getConnection();

	
		if (request.getMethod().equals("POST")){
		
			if (user==null){ %>
				<b>Not allowed!</b> <%
				return;
			}
			else{
				String[] delem_ids = request.getParameterValues("delem_id");
				for (int i=0; delem_ids!=null && i<delem_ids.length; i++){
					if (!SecurityUtil.hasPerm(user.getUserName(), "/elements/" + delem_ids[i], "d")){ %>
						<b>Not allowed!</b> <%
					}
				}
			}
			
      		Connection userConn = null;
      		DataElementHandler handler = null;
      		try{
	      		userConn = user.getConnection();
				handler = new DataElementHandler(userConn, request, ctx, "delete");
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
		
		String _wrkCopies = request.getParameter("wrk_copies");
		wrkCopies = (_wrkCopies!=null && _wrkCopies.equals("true")) ? true : false;
	
		dataElements = searchEngine.getDataElements(params, type, ns_param,
									short_name, null, dataset, wrkCopies, oper);
	}

// prepare Vector of deletion rights for each element
Vector delPrms = new Vector();
for (int i=0; user!=null && dataElements!=null && i<dataElements.size(); i++){
	DataElement elm = (DataElement)dataElements.get(i);
	if (SecurityUtil.hasPerm(user.getUserName(), "/elements/" + elm.getID(), "d"))
		delPrms.add(elm.getID());
}

/*  Not needed currently
	int iCurrPage=0;
    try {
	    iCurrPage=Integer.parseInt(request.getParameter("page_number"));
    }
    catch(Exception e){
        iCurrPage=0;
    }
    if (iCurrPage<0)
        iCurrPage=0;
*/           
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
	<script language="JavaScript">
    	function openSchema(){
			window.open("station.xsd",null, "height=400,width=600,status=no,toolbar=no,menubar=no,location=no,scrollbars=yes,top=100,left=100");
		}

		function setLocation(){
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
		}
		
		function goTo(mode){
			if (mode == "add"){
				document.location.assign('data_element.jsp?mode=add');
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
		
    	function deleteElement(){
	    	
	    	var b = confirm("This will delete all the data elements you have selected. If any of them are working copies then the corresponding original copies will be released. Click OK, if you want to continue. Otherwise click Cancel.");
			if (b==false) return;

			document.forms["form1"].elements["mode"].value = "delete";			
	    	document.forms["form1"].elements["SearchType"].value='<%=TYPE_SEARCH%>';
       		document.forms["form1"].submit();
    	}
    	
    </script>
</head>
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0">
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
                <jsp:param name="name" value="Search results"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<%
            
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
        	    if (dataElements == null || dataElements.size()==0){
	        	    %>
	            	<b>No results found!</b></div></TD></TR></table></body></html>
	            	<%
	            	return;
            	}
            }
            %>
            
			<form id="form1" method="POST" action="search_results.jsp" onsubmit="setLocation()">
			
		<table width="500" cellspacing="0">
			<tr><td colspan="3"><span class="head00">Search results</span></td></tr>
			<tr height="10"><td colspan="3"></td></tr>
			<tr>
				<td colspan="3"><span class="mainfont">
					<%
					if (user!=null){ %>
						A red wildcard (<font color="red">*</font>) means that the definition of the data element itself or
						its parent dataset is under work and it cannot be deleted. Otherwise
						checkboxes enable to delete selected elements.<%
					}
					%>
				</td>
			</tr>
		</table>
		
		<table width="auto" cellspacing="0">
		
			<tr height="5"><td colspan="8"></td></tr>
			
			<tr>
				<td></td>
				<td align="left" colspan="8" style="padding-bottom:5">
					<% if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/elements", "i")){
						%>
						<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>
						<%
					}
					%>
				</td>
			</tr>
			
			<tr>
				<td align="right" style="padding-right:10">
					<% if (user!=null){
						String _disabled = delPrms.size()>0 ? "" : "disabled"; %>
						<input type="button" value="Delete" class="smallbutton" <%=_disabled%> onclick="deleteElement()"/><%
					}
					else{ %>
						&#160;<%
					}%>
				</td>
				<th align="left" style="padding-left:5;padding-right:10">Short name</th>
				<th align="right" style="padding-left:5;padding-right:5">
					<table border="0" width="auto">
						<tr>
							<th align="right">
								<a href="javascript:showSortedList(1, 1)"><img src="../images/sort_asc.gif" border="0" title="Sort ascending by short name"/></a>
							</th>
							<th align="right">
								<a href="javascript:showSortedList(1, -1)"><img src="../images/sort_desc.gif" border="0"title="Sort descending by short name"/></a>
							</th>
						</tr>
					</table>
				</th>
				<th align="left" style="padding-right:10">Type</th>
				<th align="right" style="padding-left:5;padding-right:5">
					<table border="0" width="auto">
						<tr>
							<th align="right">
								<a href="javascript:showSortedList(2, 1)"><img src="../images/sort_asc.gif" border="0" title="Sort ascending by type"/></a>
							</th>
							<th align="right">
								<a href="javascript:showSortedList(2, -1)"><img src="../images/sort_desc.gif" border="0"title="Sort descending by type"/></a>
							</th>
						</tr>
					</table>
				</th>
				<th align="left" style="padding-right:10">Table</th>
				<th align="right" style="padding-left:5;padding-right:5">
					<table border="0" width="auto">
						<tr>
							<th align="right">
								<a href="javascript:showSortedList(3, 1)"><img src="../images/sort_asc.gif" border="0" title="Sort ascending by table name"/></a>
							</th>
							<th align="right">
								<a href="javascript:showSortedList(3, -1)"><img src="../images/sort_desc.gif" border="0"title="Sort descending by table name"/></a>
							</th>
						</tr>
					</table>
				</th>
				<th align="left" style="padding-right:10">Dataset</th>
				<th align="right" style="padding-left:5;padding-right:5">
					<table border="0" width="auto">
						<tr>
							<th align="right">
								<a href="javascript:showSortedList(4, 1)"><img src="../images/sort_asc.gif" border="0" title="Sort ascending by dataset name"/></a>
							</th>
							<th align="right">
								<a href="javascript:showSortedList(4, -1)"><img src="../images/sort_desc.gif" border="0"title="Sort descending by dataset name"/></a>
							</th>
						</tr>
					</table>
				</th>
			</tr>
			
			<%
			if (searchType != null && searchType.equals(TYPE_SEARCH)){

				/* show all
				if (iPageLen==0)
					iPageLen = dataElements.size();
			
    	        int iBeginNode=iCurrPage*iPageLen;
        	    int iEndNode=(iCurrPage+1)*iPageLen;
            	if (iEndNode>=dataElements.size()) 
	            	iEndNode=dataElements.size();
            	for (int i=iBeginNode;i<iEndNode;i++) {*/
            	
				// init the VersionManager
				VersionManager verMan = new VersionManager(conn, searchEngine, user);
			
				c_SearchResultSet oResultSet=new c_SearchResultSet();
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(oSearchCacheAttrName,oResultSet);
	        	
	        	for (int i=0; i<dataElements.size(); i++){
		        	
					DataElement dataElement = (DataElement)dataElements.get(i);
					
					String regStatus = dataElement!=null ? dataElement.getStatus() : null;
					// for countries show only Recorded & Released
					/*if (regStatus!=null){
						if (user==null || !user.isAuthentic()){
							if (regStatus.equals("Incomplete") || regStatus.equals("Candidate") || regStatus.equals("Qualified"))
								continue;
						}
					}*/
			
					String delem_id = dataElement.getID();
					//String delem_name = dataElement.getAttributeValueByName("Name");
					String delem_name = dataElement.getShortName();
					if (delem_name == null) delem_name = "unknown";
					if (delem_name.length() == 0) delem_name = "empty";
					String delem_type = dataElement.getType();
					if (delem_type == null) delem_type = "unknown";
					
					String displayType = "unknown";
					if (delem_type.equals("AGG")){
						displayType = "Aggregate";
					}
					else if (delem_type.equals("CH1")){
						displayType = "Fixed values";
					}
					else if (delem_type.equals("CH2")){
						displayType = "Quantitative";
					}
				
					String tblID = dataElement.getTableID();
					DsTable tbl = null;
					if (tblID != null) tbl = searchEngine.getDatasetTable(tblID);
					String dsID = null;
					Dataset ds = null;
					if (tbl != null) dsID = tbl.getDatasetID();
					if (dsID != null) ds = searchEngine.getDataset(dsID);
				
					String dispDs  = ds==null  ? "-" : ds.getShortName();
					String dispTbl = tbl==null ? "-" : tbl.getShortName();
				
					c_SearchResultEntry oEntry = new c_SearchResultEntry(delem_id,
               															 displayType,
                														 delem_name,
                														 dispDs,
                														 dispTbl,
                														 null);                															 
					boolean delPrm = delPrms.contains(delem_id);
					oEntry.setDelPrm(delPrm);
					
					oResultSet.oElements.add(oEntry);
					
					String workingUser = verMan.getWorkingUser(dataElement.getNamespace().getID(),
		    											dataElement.getShortName(), "elm");					
					String topWorkingUser = verMan.getWorkingUser(dataElement.getTopNs());
					
					boolean canDelete = topWorkingUser==null ||
										(dataElement.isWorkingCopy() &&
										workingUser!=null && user!=null &&
										workingUser.equals(user.getUserName()));
					%>
				
					<tr>
						<td align="right" style="padding-right:10">
							<%
	    					//if (user!=null){
		    				if (delPrms.contains(delem_id)){
		    					
		    					if (topWorkingUser!=null){ // mark checked-out elements
			    					%> <font title="<%=topWorkingUser%>" color="red">*</font> <%
		    					}
	    					
		    					if (canDelete){ %>
									<input type="checkbox" style="height:13;width:13" name="delem_id" value="<%=delem_id%>"/>
									<input type="hidden" name="delem_name_<%=delem_id%>" value="<%=delem_name%>"/> <%
								}
							}
							%>
						</td>
						<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
							<a href="data_element.jsp?delem_id=<%=delem_id%>&#38;type=<%=delem_type%>&#38;mode=view">
							<%=Util.replaceTags(delem_name)%></a>
						</td>					
						<%
						/*if (ns != null){
							%>
							<td align="center" width="80pts">&#160;
								<a href="namespace.jsp?ns_id=<%=ns.getID()%>&#38;mode=edit">
								<%=ns.getShortName()%></a>
							</td>
							<%
						}
						else{
							%>
								<td align="center" width="80pts">Unknown</td>
								<%
							}*/
						%>
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2"><%=displayType%></td>
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2"><%=Util.replaceTags(dispTbl)%></td>
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2"><%=Util.replaceTags(dispDs)%></td>
					</tr>
				
				<%
				}
				%>
               	<tr><td colspan="8">&#160;</td></tr>
				<tr><td colspan="8">Total results: <%=dataElements.size()%></td></tr><%
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
					<tr>
						<% if (user != null){%>
							<td align="right" style="padding-right:10">
								<%
								if (oEntry.getDelPrm()){ %>
									<input type="checkbox" style="height:13;width:13" name="delem_id" value="<%=oEntry.oID%>"/><%
								}
								%>
								<input type="hidden" name="delem_name_<%=oEntry.oID%>" value="<%=oEntry.oShortName%>"/>
								<input type="hidden" name="ns_name_<%=oEntry.oID%>" value="<%=oEntry.oNs%>"/>
							</td>
						<% } %>
						<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
							<a href="data_element.jsp?delem_id=<%=oEntry.oID%>&#38;type=<%=oEntry.oType%>&#38;mode=view">
							<%=Util.replaceTags(oEntry.oShortName)%></a>
						</td>					
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2"><%=oEntry.oType%></td>
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2"><%=Util.replaceTags(oEntry.oTblName)%></td>
						<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2"><%=Util.replaceTags(oEntry.oDsName)%></td>
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
		
		<input type="hidden" name="searchUrl" />
        <input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
        <input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
		<input name='SearchType' type='hidden' value='NoSearch'/>
		
		<input type="hidden" name="mode" value="view"/>
		
		<br>

		<!--   Page footer  -->
		<%
		/*
		if (dataElements != null){
			int iTotal = dataElements.size();
			%>
	        <jsp:include page="search_results_footer.jsp" flush='true'>
		        <jsp:param name="total" value="<%=iTotal%>"/>
			    <jsp:param name="page_len" value="<%=iPageLen%>"/>
				<jsp:param name="curr_page" value="<%=iCurrPage%>"/>
	        </jsp:include>
			<%
		}
		*/
		%>


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
