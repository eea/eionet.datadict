<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*"%>

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String oSearchCacheAttrName="search_cache";%>
<%!final static String oSearchUrlAttrName="search_url";%>
<%!
	private DDuser getUser(HttpServletRequest req) {
	
		DDuser user = null;
	    
	    HttpSession httpSession = req.getSession(false);
	    if (httpSession != null) {
	    	user = (DDuser)httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
		}
	      
	    if (user != null)
	    	return user.isAuthentic() ? user : null;
		else 
	    	return null;
	}
%>
<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oShortName;
    public String oVersion;
    public Vector oTables;

    private String oCompStr=null;
    private int iO=0;
    
    public c_SearchResultEntry(String _oID, String _oShortName, String _oVersion,Vector _oTables) {
	    
            oID	= _oID==null ? "" : _oID;
            oShortName	= _oShortName==null ? "" : _oShortName;
            oVersion	= _oVersion==null ? "" : _oVersion;
            oTables	= _oTables;
    		
	};
    
    public void setComp(int i,int o) {
        switch(i) {
            case 2: oCompStr=oVersion; break;
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
    
	DDuser user = getUser(request);
	DDSearchEngine searchEngine = null;
	

	if (searchType != null && searchType.equals(TYPE_SEARCH)){
		
		Connection conn = DBPool.getPool(appName).getConnection();
	
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
		
			DatasetHandler handler =
					new DatasetHandler(user.getConnection(), request, ctx, "delete");
				
			handler.execute();
		
			//String redirUrl = request.getParameter("searchUrl");
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
				new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, " like ", "=");
		
			param.addValue("'%" + parValue + "%'");
			params.add(param);
		}
	
		String short_name = request.getParameter("short_name");
		String version = request.getParameter("version");
		Vector v = searchEngine.getSimpleAttributes("", "");

		//String jama = searchEngine.getJama();
		datasets = searchEngine.getDatasets(params, short_name, version);
		//Vector datasets = searchEngine.getDatasets();	
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
	    	
	    	var b = confirm("This will delete all the datasets you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
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
                <jsp:param name="name" value="Datasets"/>
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
            
			<form id="form1" method="POST" action="datasets.jsp" onsubmit="setLocation()">
			
		<table width="450" border="0">
		
			<tr>
				<td><font class="head00">Datasets</font></td>
			</tr>
			<tr height="10"><td></td></tr>
			<tr>
				<td>
					<% if (user != null) { %>
						To view or modify a dataset or its table, click on the corresponding titles.
						To add a dataset, click the 'Add' button on top of the list. The left-most column enables
						you to delete selected datasets.
					<% } else { %>
						To view a dataset or its table, click on the corresponding titles.
					<% } %>
				</td>
			</tr>
			<tr height="5"><td colspan="2"></td></tr>
		</table>
		
		<table width="auto" cellspacing="0" border="0">
		
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
				<td align="right"><a href="search_dataset.jsp"><img src="../images/search_ds.gif" border=0 alt="Search datasets"></a></td>
			</tr>
			<tr height="5"><td colspan="5"></td></tr>
		
			<tr>
				<% if (user != null){%>
					<td align="right" style="padding-right:10">
						<input type="button" value="Delete" class="smallbutton" onclick="deleteDataset()"/>
					</td>
				<% } else {%>
					<td></td>
				<% } %>
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
				<th align="left" style="padding-right:10">Version</th>
				<th align="left" style="padding-right:10">Tables</th>
			</tr>
			
			<%
			
			if (searchType != null && searchType.equals(TYPE_SEARCH)){

				c_SearchResultSet oResultSet=new c_SearchResultSet();
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(oSearchCacheAttrName,oResultSet);
	        	
				for (int i=0; i<datasets.size(); i++){
				
					Dataset dataset = (Dataset)datasets.get(i);
					String ds_id = dataset.getID();
					String ds_name = dataset.getShortName();
					Vector tables = searchEngine.getDatasetTables(ds_id);
				
					String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
				
					if (ds_name == null) ds_name = "unknown";
					if (ds_name.length() == 0) ds_name = "empty";
				
					c_SearchResultEntry oEntry = new c_SearchResultEntry(ds_id,
               															 ds_name,
               															 dsVersion,
                														 tables);
                															 
					oResultSet.oElements.add(oEntry);
					%>
				
					<tr valign="top">	
						<% if (user != null){%>
							<td align="right" style="padding-right:10">
								<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=ds_id%>"/>
							</td>
						<% } else {%>
							<td></td>
						<% } %>
						
						<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
							<a href="dataset.jsp?ds_id=<%=ds_id%>&#38;mode=view">
							<%=ds_name%></a>
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
			
								%>
								<!--a href="javascript:openTables('<%=tableLink%>')"><%=table.getShortName()%></a><br/-->
								<a href="<%=tableLink%>"><%=table.getShortName()%></a><br/>
								<%
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
							<% if (user != null){%>
								<td align="right" style="padding-right:10">
									<input type="checkbox" style="height:13;width:13" name="ds_id" value="<%=oEntry.oID%>"/>
								</td>
							<% } %>
							<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
								<a href="dataset.jsp?ds_id=<%=oEntry.oID%>&#38;mode=view">
								<%=oEntry.oShortName%></a>
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
									<a href="<%=tableLink%>"><%=table.getShortName()%></a><br/>
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
		
		<input type="hidden" name="searchUrl" value=""/>
        <input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
        <input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
		<input name='SearchType' type='hidden' value='NoSearch'/>
		
		<input type="hidden" name="mode" value="view"/>
		
		</form>
			</div>
			
		</TD>
</TR>
</table>
</body>
</html>