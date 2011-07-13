<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,eionet.util.sql.ConnectionUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!static int iPageLen=0;%>
<%!final static String attrCommonElms="common_elms";%>
<%!final static String oSearchCacheAttrName="elms_search_cache";%>
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
    public String dstID = null;
    public String dstWorkingUser = null;
    public String dstRegStatus = null;
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
            case 3: oCompStr=oTblName; break;
            case 4: oCompStr=oDsName; break;
            case 5: oCompStr=dstRegStatus; break;
            case 6: oCompStr=dstID; break;            
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
	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
	response.setHeader("Expires", Util.getExpiresDateString());

	request.setCharacterEncoding("UTF-8");
	
	// if this is a search for common elements only, forward the request to common_elms.jsp
	String common = request.getParameter("common");
	if (common!=null && common.equals("true")){
		session.removeAttribute(attrCommonElms);
		request.getRequestDispatcher("common_elms.jsp").forward(request, response);
	}

	// get user object from session
	DDUser user = SecurityUtil.getUser(request);
	
	// get page mode
	String pageMode = request.getParameter("sort_column")!=null ? "sort" : "search";
	
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
    
    // if this is no sorting request, then remember the query string in session in order to come back if needed
    if (oSortCol==null){
		String query = request.getQueryString() == null ? "" : request.getQueryString();
		String searchUrl =  request.getRequestURI() + "?" + query;
       	session.setAttribute(oSearchUrlAttrName, searchUrl);
   	}
    
    // declare some global stuff
    Connection conn = null;
    Vector dataElements = null;
    DDSearchEngine searchEngine = null;
    boolean isIncludeHistoricVersions = request.getParameter("incl_histver")!=null && request.getParameter("incl_histver").equals("true");
	
    // start the whole page try block
	try {
	
		// if in search mode
		if (pageMode.equals("search")){
			
			// remove the cached result set
			session.removeAttribute(oSearchCacheAttrName);
			
	       	// get the DB connection and set up search engine
			ServletContext ctx = getServletContext();
			conn = ConnectionUtil.getConnection();
			searchEngine = new DDSearchEngine(conn, "", ctx);
			searchEngine.setUser(user);
		
			// get statical search parameters
			String type = request.getParameter("type");
			String ns_param = request.getParameter("ns");
			String short_name = request.getParameter("short_name");
			String idfier = request.getParameter("idfier");
			String dataset = request.getParameter("dataset");
			String datasetIdf = request.getParameter("dataset_idf");
		
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
			dataElements =
			searchEngine.getDataElements(params, type, datasetIdf, short_name, idfier, null, dataset, wrkCopies, isIncludeHistoricVersions, oper);
			
			// if searching for use as foreign key, prune out certain ones
			String strForForeignKeyUse = request.getParameter("for_fk_use");			
			if (strForForeignKeyUse!=null && strForForeignKeyUse.equals("true")){
				String skipTableID = request.getParameter("skip_table_id");
				HashSet alreadySelected = Util.tokens2hash(request.getParameter("selected"), "|");
				if ((skipTableID!=null && skipTableID.length()>0) || (alreadySelected!=null && alreadySelected.size()>0)){
					for (int i=0; dataElements!=null && i<dataElements.size();i++){
						DataElement elm = (DataElement)dataElements.get(i);
						if (skipTableID!=null && elm.getTableID()!=null && elm.getTableID().equals(skipTableID)){
							dataElements.remove(i);
							i--;
						}
						else if (alreadySelected.contains(elm.getID())){
							dataElements.remove(i);
							i--;
						}
					}
				}
			}
			
		} // end if in search mode

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Search results - Data Dictionary</title>
	<script type="text/javascript">
	// <![CDATA[
		function setLocation(){
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
		}
		function goTo(mode){
			if (mode == "add")
				document.location.assign("data_element.jsp?mode=add");
			else if (mode=="search"){
				alert("");
				document.location.assign("search.jsp");
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
if (popup){ %>	

	<body class="popup">
	
	<div id="pagehead">
	    <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
	    <div id="networktitle">Eionet</div>
	    <div id="sitetitle">Data Dictionary (DD)</div>
	    <div id="sitetagline">This service is part of Reportnet</div>    
	</div> <!-- pagehead -->
	<%
}
else{ %>

	<body>
		<div id="container">
		<jsp:include page="nlocation.jsp" flush="true">
			<jsp:param name="name" value="Search results"/>
			<jsp:param name="helpscreen" value="elements"/>
		</jsp:include>
    	<%@ include file="nmenu.jsp" %>
		<div id="workarea">
		<%
}
            
			if (pageMode.equals("search")){
				
				// see if any results were found
        	    if (dataElements == null || dataElements.size()==0){
	        	    
        	    	// prepare message trailer for un-authenticated users
		    		String msgTrailer = user==null ? " for unauthenticated users" : "";
		    		%>
        		    <div class="error-msg">No element definitions matching the search criteria were found<%=Util.replaceTags(msgTrailer)%>!</div>
            		</div></body></html> <%
            		return;
        		}
            }
            %>
            
    		<%
    		if (popup){
        		%>
        		<div id="operations">
    				<ul>
            			<li><a href="javascript:window.close();">Close</a></li>
            			<li class="help"><a href="help.jsp?screen=elements&amp;area=pagehelp" onclick="pop(this.href);return false;">Page help</a></li>
            		</ul>
    			</div><%
			}
			
            String strAllOrLatest = isIncludeHistoricVersions ? "all " : "latest";
            %>
            <h1>Non-common elements from <%=strAllOrLatest%> versions of datasets in any status</h1>

			<%
			if (user==null){ %>
				<p class="advise-msg">
					Note: Elements from datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.
		        </p><%
		    }
			%>
            
			<form id="form1" method="post" action="search_results.jsp" onsubmit="setLocation()">

			<!-- search results table -->
						
			<table width="700" class="sortable" style="display:block">
			<%
			boolean isDisplayDstVersionColumn = isIncludeHistoricVersions;
			if (isDisplayDstVersionColumn){%>
				<col style="width:20%"/>
				<col style="width:16%"/>
				<col style="width:16%"/>
				<col style="width:16%"/>
				<col style="width:16%"/>
				<col style="width:16%"/>
				<%
			}
			else{%>
				<col style="width:24%"/>
				<col style="width:19%"/>
				<col style="width:19%"/>
				<col style="width:19%"/>
				<col style="width:19%"/>
				<%
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
				<th>
					<%
					sortedImg  = getSortedImg(3, oSortCol, oSortOrder);
					sortedLink = getSortedLink(3, oSortCol, oSortOrder);
					%>
					<a title="Table" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Table&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
				<th>
					<%
					sortedImg  = getSortedImg(4, oSortCol, oSortOrder);
					sortedLink = getSortedLink(4, oSortCol, oSortOrder);
					%>
					<a title="Dataset" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Dataset&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
				<%
				if (isDisplayDstVersionColumn){
					sortedImg  = getSortedImg(6, oSortCol, oSortOrder);
					sortedLink = getSortedLink(6, oSortCol, oSortOrder);
					%>
					<th>
						<a title="Dataset version" href="<%=Util.replaceTags(sortedLink, true)%>">
		                      Dataset version&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
						</a>
					</th><%
				}
				%>
				<th>
					<%
					sortedImg  = getSortedImg(5, oSortCol, oSortOrder);
					sortedLink = getSortedLink(5, oSortCol, oSortOrder);
					%>
					<a title="Dataset status" href="<%=Util.replaceTags(sortedLink, true)%>">
	                      Dataset status&nbsp;<img src="<%=Util.replaceTags(sortedImg, true)%>" width="12" height="12" alt=""/>
					</a>
				</th>
			</tr>
			</thead>
			<tbody>	
			<%
			
			int displayed = 0;
			if (pageMode.equals("search")){

				// set up the search result set
				c_SearchResultSet oResultSet=new c_SearchResultSet();
				oResultSet.isAuth = user!=null;
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(oSearchCacheAttrName,oResultSet);
	        	
	        	// search results processing loop
	        	String skipID = request.getParameter("skip_id");
	        	for (int i=0; i<dataElements.size(); i++){
		        	
					// set up the element
		        	DataElement dataElement = (DataElement)dataElements.get(i);
		        	
					// skip_id is used for skipping the element for which we might be searching for foreign keys here
		        	String delem_id = dataElement.getID();
					if (skipID!=null && skipID.equals(delem_id))
						continue;
					
					String delem_name = dataElement.getShortName();
					String delem_type = dataElement.getType();
					String displayType = "unknown";
					if (delem_type.equals("CH1"))
						displayType = "Fixed values";
					else if (delem_type.equals("CH2"))
						displayType = "Quantitative";
					
					String dstID = dataElement.getDatasetID();
					String dstWorkingUser = dataElement.getDstWorkingUser();
					String dispDs = dataElement.getDstShortName();
					if (dispDs==null)
					dispDs = "-";
					String dispTbl = dataElement.getTblShortName();
					if (dispTbl==null)
					dispTbl = "-";

					String dstRegStatus = dataElement.getDstStatus();
					boolean clickable = dstRegStatus!=null ? !searchEngine.skipByRegStatus(dstRegStatus) : true;
					String strDisabled = clickable ? "" : " disabled=\"disabled\"";
					String statusImg   = "images/" + Util.getStatusImage(dstRegStatus);
					String statusTxt   = Util.getStatusRadics(dstRegStatus);

					StringBuffer href = new StringBuffer();
					if (!popup)
						href.append("data_element.jsp?delem_id=").append(delem_id);
					else
						href.append("javascript:pickElem(").append(delem_id).append(",").append(displayed+1).append(")");
						
					c_SearchResultEntry oEntry = new c_SearchResultEntry(delem_id,
               															 displayType,
                														 delem_name,
                														 dispDs,
                														 dispTbl,
                														 null);
                	oEntry.clickable = clickable;
                	oEntry.dstID = dstID;
					oEntry.dstRegStatus = dstRegStatus;
					oEntry.dstWorkingUser = dstWorkingUser;
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
							%>
		    			</td>
						<td>
							<%=displayType%>
						</td>
						<td>
							<%=Util.replaceTags(dispTbl)%>
						</td>
						<td>
							<%=Util.replaceTags(dispDs)%>
							<%
							// mark checked-out datasets
							if (user!=null && dstWorkingUser!=null){ %>
								<span title="<%=dstWorkingUser%>" class="checkedout">*</span><%
		    				}
		    				%>
						</td>
						<%
						if (isDisplayDstVersionColumn){ %>
							<td>
								<%=dstID%>
							</td><%
						}
						%>
						<td>
							<%
							if (clickable){ %>
								<img style="border:0" src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=dstRegStatus%>" alt="<%=dstRegStatus%>"/><%
							}
							else{ %>
								<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=dstRegStatus%>">
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
                c_SearchResultSet oResultSet=(c_SearchResultSet)session.getAttribute(oSearchCacheAttrName);
                if (oResultSet==null) {
                    %><p>This page has experienced a time-out. Try searching again.</p><%
                }
                else {
                    if ((oSortCol!=null) && (oSortOrder!=null))
                        oResultSet.SortByColumn(oSortCol,oSortOrder);
                    
                    for (int i=0;i<oResultSet.oElements.size();i++) {
	                    
	                    c_SearchResultEntry oEntry=(c_SearchResultEntry)oResultSet.oElements.elementAt(i);
	                    String strDisabled = oEntry.clickable ? "" : " disabled=\"disabled\"";
	                    String statusImg   = "images/" + Util.getStatusImage(oEntry.dstRegStatus);
						String statusTxt   = Util.getStatusRadics(oEntry.dstRegStatus);
						
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
								%>
							</td>
							<td>
								<%=oEntry.oType%>
							</td>						
							<td>
								<%=Util.replaceTags(oEntry.oTblName)%>
							</td>
							<td>
								<%=Util.replaceTags(oEntry.oDsName)%>
								<%
								// mark checked-out datasets
								if (user!=null && oEntry.dstWorkingUser!=null){ %>
									<span title="<%=oEntry.dstWorkingUser%>" class="attention">*</span><%
			    				}
			    				%>
							</td>
							<%
							if (isDisplayDstVersionColumn){ %>
								<td>
									<%=oEntry.dstID%>
								</td><%
							}
							%>
							<td>
								<%
								if (oEntry.clickable){ %>
									<img style="border:0" src="<%=Util.replaceTags(statusImg)%>" width="56" height="12" title="<%=oEntry.dstRegStatus%>" alt="<%=oEntry.dstRegStatus%>"/><%
								}
								else{ %>
									<span style="color:gray;text-decoration:none;font-size:8pt" title="<%=oEntry.dstRegStatus%>">
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
			<div style="display:none">
				<input type="hidden" name="searchUrl" />
				<input type="hidden" name="mode" value="view"/>
				
				<%
				if (isIncludeHistoricVersions){%>
					<input name="incl_histver" type="hidden" value="true"/><%
				}
				%>
			</div>
		</form>
		
		<form id="sort_form" action="search_results.jsp" method="get">
			<div style="display:none">
				<input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
	        	<input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
				<%
				if (popup){ %>
					<input type='hidden' name='ctx' value='popup'/><%
				}
				if (isIncludeHistoricVersions){%>
					<input name="incl_histver" type="hidden" value="true"/><%
				}
				%>
			</div>
		</form>
	</div> <!-- workarea -->
	<%
	if (!popup){%>
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
