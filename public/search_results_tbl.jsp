<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String oSearchCacheAttrName="tbl_search_cache";%>
<%!final static String oSearchUrlAttrName="tbl_search_url";%>

<%@ include file="history.jsp" %>
<%@ include file="sorting.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oDsID;
    public String oShortName;
    public String oName;
    public String oFullName;
    public String oDsName;
    public String topWorkingUser = null;

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
	
	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);

	String short_name = request.getParameter("short_name");
	String idfier = request.getParameter("idfier");
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
    
    // if this is no sorting request, then remember the query string in session in order to come back if needed
    if (oSortCol==null){
		String query = request.getQueryString() == null ? "" : request.getQueryString();
		String searchUrl =  request.getRequestURI() + "?" + query;
		
       	session.setAttribute(oSearchUrlAttrName, searchUrl);
   	}
   	
   	// The following if block tries to identify if a login has happened in which
	// case it will redirect the response to the query string in session. This
	// happens regardless of weather it's a sorting request or search request.
	c_SearchResultSet rs = (c_SearchResultSet)session.getAttribute(oSearchCacheAttrName);
	if (rs!=null){
		if (rs.isAuth && user==null || !rs.isAuth && user!=null){
			session.removeAttribute(oSearchCacheAttrName);
			
			response.sendRedirect((String)session.getAttribute(oSearchUrlAttrName));
		}
	}
   	
    String searchType=request.getParameter("SearchType");
    String tableLink="";
    
    String _wrkCopies = request.getParameter("wrk_copies");
	boolean wrkCopies = (_wrkCopies!=null && _wrkCopies.equals("true")) ? true : false;	
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Tables - Data Dictionary</title>
	<script type="text/javascript" language="javascript">
	// <![CDATA[

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
    		if ((document.forms["sort_form"].elements["sort_column"].value != clmn)
       			|| (document.forms["sort_form"].elements["sort_order"].value != ordr)) {
        		document.forms["sort_form"].elements["sort_column"].value=clmn;
		    	document.forms["sort_form"].elements["sort_order"].value=ordr;
        		document.forms["sort_form"].submit();
    		}
		}
	// ]]>
	</script>
</head>
<body>
	<jsp:include page="nlocation.jsp" flush='true'>
		<jsp:param name="name" value="Tables"/>
		<jsp:param name="back" value="true"/>
	</jsp:include>
	<%@ include file="nmenu.jsp" %>

		<div id="workarea">
			
            
				<!-- search buttons -->
				
				<div id="operations">
					<ul>
					<li><a target="_blank" href="help.jsp?screen=tables&amp;area=pagehelp" onclick="pop(this.href);return false;">Page help</a></li>
					<li><a href="search_table.jsp">Search</a></li>
					</ul>
				</div>
			
			<h1>
					<%
					if (wrkCopies){ %>
						Working copies of table definitions<%
					}
					else{ %>
						Dataset tables<%
					}
					%>
			</h1>
			<%
			if (user==null){ %>
				<p>	
    		    		NB! For un-authenticated users the definitions from datasets whose Registration status
    		    		is not <em>Recorded</em> or <em>Released</em> are not listed.<br/>
    		    		To see which datasets have such a Registration status, go to the
    		    		<a href="datasets.jsp?SearchType=SEARCH">datasets list</a>.
				</p><%
		    }
			%>
		
		<form id="form1" method="post" action="search_results_tbl.jsp" onsubmit="setLocation()">
		
		<div style="padding-bottom:5">
			<%
			boolean dstPrm = user!=null && SecurityUtil.hasChildPerm(user.getUserName(), "/datasets/", "u");
			if (dstPrm){ %>
				<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>
				<%
			}
			%>
		</div>
		
		<!-- the result table -->		
		<table width="700" class="sortable">
		 <col style="width:37%"/>
		 <col style="width:35%"/>
		 <col style="width:25%"/>
			<thead>
			<tr>
				<th>
					<%
					String sortedImg  = getSortedImg(3, oSortCol, oSortOrder);
					String sortedLink = getSortedLink(3, oSortCol, oSortOrder);
					%>
					<a title="Table" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Table&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
				<th>
					<%
					sortedImg  = getSortedImg(1, oSortCol, oSortOrder);
					sortedLink = getSortedLink(1, oSortCol, oSortOrder);
					%>
					<a title="Short name" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Short name&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
				<th>
					<%
					sortedImg  = getSortedImg(2, oSortCol, oSortOrder);
					sortedLink = getSortedLink(2, oSortCol, oSortOrder);
					%>
					<a title="Dataset" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Dataset&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
			</tr>
			</thead>
			<tbody>
			
            <%
            
            Connection conn = null;
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = xdbapp.getDBPool();
            
            try { // start the whole page try block
            
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
				
				session.removeAttribute(oSearchCacheAttrName);
	                        
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
					
					Vector dsTables = searchEngine.getDatasetTables(params, short_name, idfier, full_name, definition, oper, wrkCopies);
					
					// see if any result were found
        		    if (dsTables == null || dsTables.size()==0){ %>
        		    	<tr>
        		    		<td colspan="3"><br/>
        		    			<%
        		    			// prepare message trailer for un-authenticated users
	        		    		String msgTrailer = user==null ? " for un-authenticated users" : "";
	        		    		
			        		    // see if this is a search or just listing all the tables
			        		    if (Util.voidStr(request.getParameter("search_precision"))){ // listing all the tables
				        		    %>
				        		    <b>No table definitions were found<%=Util.replaceTags(msgTrailer)%>!</b><%
			        		    }
			        		    else{ // a search
			        		    	%>
			        		    	<b>No table definitions matching the search criteria were found<%=Util.replaceTags(msgTrailer)%>!</b><%
			        		    }
			        		    %>
			        		</td>
			        	</tr>
			        	</table></form></div></body></html> <%
	            		return;
            		}

            		DElemAttribute attr = null;
					
					c_SearchResultSet oResultSet=new c_SearchResultSet();
					oResultSet.isAuth = user!=null;
	        		oResultSet.oElements=new Vector(); 
	        		session.setAttribute(oSearchCacheAttrName,oResultSet);
	        		
	        		// set up the version manager for checking check-outs
	        		VersionManager verMan = new VersionManager(conn, searchEngine, user);

					for (int i=0; i<dsTables.size(); i++){
						DsTable table = (DsTable)dsTables.get(i);
						
						String table_id = table.getID();
						String table_name = table.getShortName();
						String ds_id = table.getDatasetID();
						String ds_name = table.getDatasetName();
						String dsNs = table.getParentNs();
						
						if (table_name == null) table_name = "unknown";
						if (table_name.length() == 0) table_name = "empty";
				
						if (ds_name == null || ds_name.length() == 0) ds_name = "unknown";
				
						String tblName = table.getName()==null ? "" : table.getName();
		
						String tblFullName = tblName;
						tblName = tblName.length()>60 && tblName != null ? tblName.substring(0,60) + " ..." : tblName;

						tableLink = "dstable.jsp?mode=view&amp;table_id=" + table_id + "&amp;ds_id=" + ds_id + "&amp;ds_name=" + ds_name;

						c_SearchResultEntry oEntry = new c_SearchResultEntry(table_id,
                															 ds_id,
                															 table_name,
                															 tblFullName,
                															 ds_name);
                															 
						String topWorkingUser = verMan.getWorkingUser(table.getParentNs());
						oEntry.topWorkingUser = topWorkingUser;
						oResultSet.oElements.add(oEntry);
						
						String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
						
						%>
						<tr class="<%=zebraClass%>">
		    				<td>
								<a href="<%=tableLink%>"><%=Util.replaceTags(tblName)%></a>
								<%
								// mark tables in a locked dataset
								if (dstPrm && topWorkingUser!=null){ %>
									<font title="<%=Util.replaceTags(topWorkingUser, true)%>" color="red">*</font><%
			    				}
			    				%>
							</td>
							<td>
								<%=Util.replaceTags(table_name)%>
							</td>
							<td>
								<%=Util.replaceTags(ds_name)%>
								<%
								// mark locked datasets
								if (dstPrm && topWorkingUser!=null){ %>
									<font title="<%=Util.replaceTags(topWorkingUser, true)%>" color="red">*</font><%
			    				}
			    				%>
							</td>
						</tr>
						<%
					}
					%>
			</tbody>
		</table>
		<p>Total results: <%=dsTables.size()%></p><%
				}
				catch(Exception e){
					%><b>ERROR: <%=e%></b><%
				}
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

                        tableLink = "dstable.jsp?mode=view&amp;table_id=" + oEntry.oID + "&amp;ds_id=" + oEntry.oDsID + "&amp;ds_name=" + oEntry.oDsName;
                        
												String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
						%>
						<tr class="<%=zebraClass%>">
							<td>
								<a href="<%=tableLink%>"><%=Util.replaceTags(oEntry.oName)%></a>
								<%
								// mark tables in a locked dataset
								if (dstPrm && oEntry.topWorkingUser!=null){ %>
									<font title="<%=Util.replaceTags(oEntry.topWorkingUser, true)%>" color="red">*</font><%
			    				}
			    				%>
							</td>
							<td>
								<%=Util.replaceTags(oEntry.oShortName)%>								
							</td>
							<td>
								<%=Util.replaceTags(oEntry.oDsName)%>
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
		
		<input type="hidden" name="searchUrl" value=""/>
		<input type='hidden' name='SearchType' value='<%=TYPE_SEARCH%>'/>

		<!--   Page footer  -->

		</form>
		
		<form name="sort_form" action="search_results_tbl.jsp" method="get">
			<input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
			<input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
			<input name='SearchType' type='hidden' value='NoSearch'/>
		</form>
		
			</div>
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
