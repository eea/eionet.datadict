<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String oSearchCacheAttrName="search_cache";%>

<%@ include file="history.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oDsID;
    public String oShortName;
    public String oName;
    public String oFullName;
    public String oDsName;

    private String oCompStr=null;
    private int iO=0;
    
    public c_SearchResultEntry(String _oID,String _oDsID,String _oShortName,String _oName,String _oDsName) {
	    
            oID		= _oID==null ? "" : _oID;
            oDsID  = _oDsID==null ? "" : _oDsID;
            oShortName	= _oShortName==null ? "" : _oShortName;
            oName= _oName==null ? "" : _oName;
            oDsName	= _oDsName==null ? "" : _oDsName;
    		
            oFullName = oName;

            if (oName.length() > 60)
				oName = oName.substring(0,60) + " ...";
	};
    
    public void setComp(int i,int o) {
        switch(i) {
            case 2: oCompStr=oDsName; break;
            case 3: oCompStr=oName; break;
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
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);

	String short_name = request.getParameter("short_name");
	String full_name = request.getParameter("full_name");
	String definition = request.getParameter("definition");

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
	
    String tableLink="";	



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
		
		function goTo(mode){
			if (mode == "add"){
				document.location.assign('dstable.jsp?mode=add');
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
			
            
			<form id="form1" method="POST" action="search_results_tbl.jsp" onsubmit="setLocation()">
			
		<table width="500" cellspacing="0">
			<tr>
				<td><span class="head00">Dataset tables</span></td>
			</tr>
			<tr height="10"><td></td></tr>

			<tr>
				<td colspan="3"><span class="mainfont">					
					<% if (user != null){ %>
						Rows marked with <font color="red">*</font> indicate checked-out tables.<%
					}
					%>
					</span>
				</td>
			</tr>
		</table>
		<table width="auto" cellspacing="0">
			<tr>
				<td align="right" style="padding-right:10">&#160;</td>
				<td align="left" colspan="3" style="padding-bottom:5">
					<% if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/tables", "i")){ %>
						<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>
						<%
					}
					%>
				</td>
				<td align="right" colspan="3"><a href="search_table.jsp"><img src="../images/search_tbl.gif" border=0 alt="Search tables"></a></td>
			</tr>
				
			<tr height="5"><td colspan="6"></td></tr>
			
			
			<tr>
				<td align="right" style="padding-right:10">&#160;</td>
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
				<th align="left" style="padding-right:10">Dataset</th>
				<th align="right" style="padding-left:5;padding-right:5"">
					<table border="0" width="auto">
						<tr>
							<th align="right">
								<a href="javascript:showSortedList(2, 1)"><img src="../images/sort_asc.gif" border="0" title="Sort ascending by dataset name"/></a>
							</th>
							<th align="right">
								<a href="javascript:showSortedList(2, -1)"><img src="../images/sort_desc.gif" border="0"title="Sort descending by short name"/></a>
							</th>
						</tr>
					</table>
				</th>
				<th align="left" style="padding-right:10">Full name</th>
				<th align="right" style="padding-left:5;padding-right:5"">
					<table border="0" width="auto">
						<tr>
							<th align="right">
								<a href="javascript:showSortedList(3, 1)"><img src="../images/sort_asc.gif" border="0" title="Sort ascending by full name"/></a>
							</th>
							<th align="right">
								<a href="javascript:showSortedList(3, -1)"><img src="../images/sort_desc.gif" border="0"title="Sort descending by full name"/></a>
							</th>
						</tr>
					</table>
				</th>
				<!--th align="left" style="padding-right:10">Definition</th-->
			</tr>
			
            <%-- Handle Request Parameters and queries --%>
            
            <%
            
            Connection conn = null;
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
            
			boolean wrkCopies = false;
			
            try { // start the whole page try block
            
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
	                        
	            try {
		            
		            // we establish a database connection and create a search engine
					conn = pool.getConnection();
					DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
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

					Vector dsTables = searchEngine.getDatasetTables(params, short_name, full_name, definition, oper, wrkCopies);
		           
        		    if (dsTables == null || dsTables.size()==0){
		            %>
			            <tr><td colspan="4"><b>No results found!</b></td></tr></table></form></div></TD></TR></table></body></html>
	            	<%
	            		return;
            		}

            		DElemAttribute attr = null;
					
					c_SearchResultSet oResultSet=new c_SearchResultSet();
	        		oResultSet.oElements=new Vector(); 
	        		session.setAttribute(oSearchCacheAttrName,oResultSet);
	        		
	        		// set up the version manager for checking check-outs
	        		VersionManager verMan = new VersionManager(conn, searchEngine, user);

					for (int i=0; i<dsTables.size(); i++){
						DsTable table = (DsTable)dsTables.get(i);
						
						String regStatus = table!=null ? table.getStatus() : null;			
						// for countries show only Recorded & Released
						/*if (regStatus!=null){
							if (user==null || !user.isAuthentic()){
								if (regStatus.equals("Incomplete") || regStatus.equals("Candidate") || regStatus.equals("Qualified"))
									continue;
							}
						}*/
						
						String table_id = table.getID();
						String table_name = table.getShortName();
						String ds_id = table.getDatasetID();
						String ds_name = table.getDatasetName();
						String dsNs = table.getParentNs();
				
						if (table_name == null) table_name = "unknown";
						if (table_name.length() == 0) table_name = "empty";
				
						if (ds_name == null || ds_name.length() == 0) ds_name = "unknown";
				
						//String tblName = "";
						String tblName = table.getName()==null ? "" : table.getName();
		
						/*Vector attributes = searchEngine.getAttributes(table_id, "T", DElemAttribute.TYPE_SIMPLE);
		
						for (int c=0; c<attributes.size(); c++){
							attr = (DElemAttribute)attributes.get(c);
        					if (attr.getName().equalsIgnoreCase("Name"))
        						tblName = attr.getValue();
						}*/
				
						String tblFullName = tblName;
						tblName = tblName.length()>60 && tblName != null ? tblName.substring(0,60) + " ..." : tblName;

						tableLink = "dstable.jsp?mode=view&table_id=" + table_id + "&ds_id=" + ds_id + "&ds_name=" + ds_name;

						c_SearchResultEntry oEntry = new c_SearchResultEntry(table_id,
                															 ds_id,
                															 table_name,
                															 tblFullName,
                															 ds_name);
                															 
						oResultSet.oElements.add(oEntry);
						
						String workingUser = verMan.getTblWorkingUser(table.getShortName(), dsNs);
						String topWorkingUser = verMan.getWorkingUser(table.getParentNs());			
						%>
						<tr>
							<td align="right" style="padding-right:10">
								<%
								if (user!=null && (topWorkingUser!=null)){ // mark checked-out tables
			    					%> <font title="<%=topWorkingUser%>" color="red">*</font> <%
		    					}
		    					%>
		    				</td>
							<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
								<a href="<%=tableLink%>"><%=Util.replaceTags(table_name)%></a>
							</td>
							<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
								<%=Util.replaceTags(ds_name)%>
							</td>
							<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=tblFullName%>" colspan="2">
								<%=Util.replaceTags(tblName)%>
							</td>
						</tr>
						<%
					}
					%>
    	           	<tr><td colspan="8">&#160;</td></tr>
					<tr><td colspan="8">Total results: <%=dsTables.size()%></td></tr><%
				}
				catch(Exception e){
					%><B>ERROR: <%=e%></B><%
				}
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

                        tableLink = "dstable.jsp?mode=view&table_id=" + oEntry.oID + "&ds_id=" + oEntry.oDsID + "&ds_name=" + oEntry.oDsName;
						%>
						<tr>
							<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
								<a href="<%=tableLink%>"><%=Util.replaceTags(oEntry.oShortName)%></a>
							</td>
							<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> colspan="2">
								<%=Util.replaceTags(oEntry.oDsName)%>
							</td>
							<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%> title="<%=oEntry.oFullName%>" colspan="2">
								<%=Util.replaceTags(oEntry.oName)%>
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
          <br>

		<!--   Page footer  -->

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
