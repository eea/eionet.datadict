<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!static int iPageLen=0;%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String attrCommonElms="common_elms";%>
<%!final static String oSearchUrlAttrName="elms_search_url";%>

<%!private int reqno = 0;%>

<%@ include file="history.jsp" %>
<%@ include file="sorting.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oType;
    public String oShortName;
    public String oDsName;
    public String oTblName;
    public String oNs;
    public String oDsIdf;
    public String workingUser = null;
    public String status = null;
    public String checkInNo = null;
    public String href = null;
    public boolean clickable = true;

    private String oCompStr=null;
    private int iO=0;
    
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
            case 3: oCompStr=status; break;
            case 4: oCompStr=oID; break;
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
	
	// get user object from session
	DDUser user = SecurityUtil.getUser(request);

	// get search type
	String searchType=request.getParameter("SearchType");
	
	// see if popup
	boolean popup = request.getParameter("ctx")!=null && request.getParameter("ctx").equals("popup");
	
	// get sorting info
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
    
    // declare some global stuff
    Connection conn = null;
    Vector dataElements = null;
    DDSearchEngine searchEngine = null;
    boolean isIncludeHistoricVersions = request.getParameter("incl_histver")!=null && request.getParameter("incl_histver").equals("true");    
	
    // start the whole page try block
	try {
	
		// if in search mode
		if (searchType != null && searchType.equals(TYPE_SEARCH)){
			
			// remove the cached result set
			session.removeAttribute(attrCommonElms);
			
	       	// get the DB connection and set up search engine
			ServletContext ctx = getServletContext();
			conn = ConnectionUtil.getConnection();
			searchEngine = new DDSearchEngine(conn, "", ctx);
			searchEngine.setUser(user);
		
			// get statical search parameters
			String type = request.getParameter("type");
			String short_name = request.getParameter("short_name");
			String idfier = request.getParameter("idfier");
		
			String searchPrecision = request.getParameter("search_precision");
			String oper="=";
			if (searchPrecision != null && searchPrecision.equals("free"))
				oper=" match ";
			if (searchPrecision != null && searchPrecision.equals("substr"))
				oper=" like ";
			
			String parWrkCopies = request.getParameter("wrk_copies");
			boolean wrkCopies = (parWrkCopies!=null && parWrkCopies.equals("true")) ? true : false;
		
			// get dynamical search parameters
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
			
			// all set up for search, do it
			dataElements = searchEngine.getCommonElements(params, type, short_name, idfier, request.getParameter("reg_status"), wrkCopies, isIncludeHistoricVersions, oper);
			
		} // end if in search mode

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Search results - Data Dictionary</title>
	<script type="text/javascript">
	// <![CDATA[
    	function showSortedList(clmn,ordr) {
    		if ((document.forms["sort_form"].elements["sort_column"].value != clmn)
       			|| (document.forms["sort_form"].elements["sort_order"].value != ordr)) {
        		document.forms["sort_form"].elements["sort_column"].value=clmn;
		    	document.forms["sort_form"].elements["sort_order"].value=ordr;
        		document.forms["sort_form"].submit();
    		}
		}
		
		<%
		if (popup){ %>
			function pickElem(elmID, rowIndex){
				// make sure the opener exists and is not closed
				if (opener && !opener.closed) {
					// if the opener has pickElem(elmID) function at it returns true, close this popup
					// else don't close it (multiple selection might be wanted)
					if (window.opener.pickElem(elmID)==true)  
						closeme();
					else
						hideRow(rowIndex);
				}
				else 
					alert("You have closed the main window!\n\nNo action will be taken.")
			}
			function hideRow(i){
				var t = document.getElementById("tbl");
				var row = t.getElementsByTagName("TR")[i+1];
				row.style.display = "none";
			}
			function closeme(){
				window.close()
			}
			<%
		}
		%>
		// ]]>
    </script>
</head>

<%
if (popup){
	%>	
	<body class="popup">
		<div id="pagehead">
		    <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
		    <div id="networktitle">Eionet</div>
		    <div id="sitetitle">Data Dictionary (DD)</div>
		    <div id="sitetagline">This service is part of Reportnet</div>    
		</div> <!-- pagehead -->
		<div id="workarea">
		<%
}
else{ %>
		<body>
		<div id="container">
		<jsp:include page="nlocation.jsp" flush="true">
			<jsp:param name="name" value="Search results"/>
			<jsp:param name="helpscreen" value="common_element_search_results"/>
		</jsp:include>
    	<%@ include file="nmenu.jsp" %>
		<div id="workarea">		
		<%
}
%>
            
    		<%
			if (popup){
				%>
				<div id="operations">
    				<ul>
						<li><a href="javascript:window.close();">Close</a></li>
					</ul>
				</div><%
			}
			%>
            
			<h1>Search results</h1>
			
			<%
			if (user==null){ %>
				<p class="advise-msg">Note: Common elements NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.</p><%
		    }
		    
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
        	    if (dataElements==null || dataElements.size()==0){
	        	    %>
	            	<div class="system-msg">No results matching the search criteria were found!</div>
	            	</div></body></html>
	            	<%
	            	return;
            	}
            }
			%>
		
			<!-- result table -->
			
			<table class="sortable" style="width:auto;clear:right">
			<%
			boolean isDisplayVersionColumn = isIncludeHistoricVersions;
			if (isDisplayVersionColumn){
				%>
				<col style="width:31%"/>
				<col style="width:23%"/>
				<col style="width:23%"/>
				<col style="width:23%"/><%
			}
			else{
				%>
				<col style="width:40%"/>
				<col style="width:30%"/>
				<col style="width:30%"/><%
			}
			%>
			<thead>
			<tr>
				<th>
					<%
					String sortedImg  = getSortedImg(1, oSortCol, oSortOrder);
					String sortedLink = getSortedLink(1, oSortCol, oSortOrder);
					%>
					<a title="Element" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Element&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
				<th>
					<%
					sortedImg  = getSortedImg(2, oSortCol, oSortOrder);
					sortedLink = getSortedLink(2, oSortCol, oSortOrder);
					%>
					<a title="Type" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Type&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
				<%
				if (isDisplayVersionColumn){
					sortedImg  = getSortedImg(4, oSortCol, oSortOrder);
					sortedLink = getSortedLink(4, oSortCol, oSortOrder);
					%>					
					<th>
						<a title="Version" href="<%=Util.replaceTags(sortedLink, true)%>">
							Version&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
						</a>
					</th><%
				}
				%>
				<th>
					<%
					sortedImg  = getSortedImg(3, oSortCol, oSortOrder);
					sortedLink = getSortedLink(3, oSortCol, oSortOrder);
					%>
					<a title="Status" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Status&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
			</tr>
			</thead>
			<tbody>
				
			<%
			int displayed = 0;
			if (searchType != null && searchType.equals(TYPE_SEARCH)){

				// set up the search result set
				c_SearchResultSet oResultSet=new c_SearchResultSet();
				oResultSet.isAuth = user!=null;
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(attrCommonElms,oResultSet);
	        	
	        	// get IDs of elements to exclude
	        	HashSet excludeIDs = new HashSet();
	        	String strExcludeIDs = request.getParameter("exclude");
	        	if (strExcludeIDs!=null && strExcludeIDs.length()>0){
		        	StringTokenizer st = new StringTokenizer(strExcludeIDs, ",");
				     while (st.hasMoreTokens()) {
				         excludeIDs.add(st.nextToken());
				     }
	        	}
	        	
	        	// search results processing loop
	        	for (int i=0; i<dataElements.size(); i++){
		        	
		        	DataElement dataElement = (DataElement)dataElements.get(i);
					String delem_id = dataElement.getID();
					String delem_name = dataElement.getShortName();
					String delem_type = dataElement.getType();
					
					String displayType = delem_type;
					if (delem_type.equals("CH1"))
						displayType = "Fixed values";
					else if (delem_type.equals("CH2"))
						displayType = "Quantitative";
					
					String workingUser = dataElement.getWorkingUser();
					String status = dataElement.getStatus();
					String checkInNo = dataElement.getVersion();
					StringBuffer href = new StringBuffer();
					if (!popup)
						href.append("data_element.jsp?mode=view&amp;delem_id=").append(delem_id);
					else
						href.append("javascript:pickElem(").append(delem_id).append(",").append(displayed+1).append(")");
					
					boolean clickable = status!=null ? !searchEngine.skipByRegStatus(status) : true;
					if (clickable){
						if (excludeIDs.contains(delem_id))
							clickable = false;
					}
					String strDisabled = clickable ? "" : " disabled=\"disabled\"";
					String statusImg   = "images/" + Util.getStatusImage(status);
					String statusTxt   = Util.getStatusRadics(status);
					
					c_SearchResultEntry oEntry = new c_SearchResultEntry(delem_id,
               															 displayType,
                														 delem_name,
                														 null,
                														 null,
                														 null);                															 
					oEntry.clickable = clickable;
					oEntry.status = status;
					oEntry.checkInNo = checkInNo;
					oEntry.workingUser = workingUser;
					oEntry.href = href.toString();
					oResultSet.oElements.add(oEntry);
					
					String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
					%>
				
					<tr class="<%=zebraClass%>">
						<td<%=strDisabled%>>
							<%
							if (clickable){%>
								<a href="<%=href%>">
									<%=Util.replaceTags(delem_name)%>
								</a><%
							}
							else{%>
								<%=Util.replaceTags(delem_name)%><%
							}
							// mark checked-out elements
							if (user!=null && workingUser!=null){ %>
								<span title="<%=workingUser%>" class="checkedout">*</span><%
		    				}
		    				%>
		    			</td>
						<td<%=strDisabled%>>
							<%=Util.replaceTags(displayType)%>
						</td>
						<%
						if (isDisplayVersionColumn){ %>
							<td<%=strDisabled%>>
								<%=delem_id%>
							</td><%
						}
						%>
						<td<%=strDisabled%>>
							<%
							if (clickable){ %>
								<img src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=status%>" alt="<%=status%>" style="border:0"/><%
							}
							else{ %>
								<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=status%>">
									<strong><%=statusTxt%></strong>
								</span><%
							}
							%>
						</td>
					</tr><%
					displayed++;
				}
				%>
			</tbody>
		</table>
		<p>Total results: <%=dataElements.size()%></p><%
			}
			else{
				// No search - return from another result set or a total stranger...
                c_SearchResultSet oResultSet=(c_SearchResultSet)session.getAttribute(attrCommonElms);
                if (oResultSet==null) {
                    %><p>This page has experienced a time-out. Try searching again.</p><%
                }
                else {
                    if ((oSortCol!=null) && (oSortOrder!=null))
                        oResultSet.SortByColumn(oSortCol,oSortOrder);
                    
                    c_SearchResultEntry oEntry;
                    for (int i=0; i<oResultSet.oElements.size(); i++){
	                    
                    	oEntry=(c_SearchResultEntry)oResultSet.oElements.elementAt(i);
                    	String strDisabled = oEntry.clickable ? "" : " disabled=\"disabled\"";
	                    String statusImg   = "images/" + Util.getStatusImage(oEntry.status);
						String statusTxt   = Util.getStatusRadics(oEntry.status);
						
						String zebraClass  = i % 2 != 0 ? "zebraeven" : "zebraodd";
                    	%>
						<tr class="<%=zebraClass%>">
							<td<%=strDisabled%>>
								<%
								if (oEntry.clickable){%>
									<a href="<%=oEntry.href%>">
										<%=Util.replaceTags(oEntry.oShortName)%>
									</a><%
								}
								else{%>
									<%=Util.replaceTags(oEntry.oShortName)%><%
								}
								// mark checked-out elements
								if (user!=null && oEntry.workingUser!=null){ %>
									<span title="<%=oEntry.workingUser%>" class="checkedout">*</span><%
			    				}
								%>
							</td>
							<td<%=strDisabled%>>
								<%=oEntry.oType%>
							</td>
							<%
							if (isDisplayVersionColumn){ %>
								<td<%=strDisabled%>>
									<%=oEntry.oID%>
								</td><%
							}
							%>
							<td<%=strDisabled%>>
								<%
								if (oEntry.clickable){ %>
									<img border="0" src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=oEntry.status%>" alt="<%=oEntry.status%>"/><%
								}
								else{ %>
									<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=oEntry.status%>">
										<strong><%=statusTxt%></strong>
									</span><%
								}
								%>
							</td>
						</tr>
						<%
						displayed++;
                	}
                	%>
			</tbody>
		</table>
		<p>Total results: <%=oResultSet.oElements.size()%></p><%
                }

            }
			%>
			
		
		<form id="sort_form" action="common_elms.jsp" method="get">
			<div style="display:none">
				<input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
	        	<input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
				<input name='SearchType' type='hidden' value='NoSearch'/>
				<%
				if (popup){ %>
					<input type="hidden" name="ctx" value="popup"/><%
				}
				if (isIncludeHistoricVersions){%>
					<input name="incl_histver" type="hidden" value="true"/><%
				}
				%>
			</div>
		</form>
		
	</div> <!-- workarea -->
	<%
	if (!popup){
		%>
		</div> <!-- container -->
		<%@ include file="footer.txt" %><%
	}
	%>
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
