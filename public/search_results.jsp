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
    public String oDsIdf;

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
	String idfier = request.getParameter("idfier");
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
					String dsidf = request.getParameter("ds_idf_" + delem_ids[i]);
					if (dsidf==null || !SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsidf, "u")){ %>
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
									short_name, idfier, null, dataset, wrkCopies, oper);
	}

%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
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
    	
    	function doLoad(){
	    	var wasDelPrm = document.forms["form1"].elements["was_del_prm"].value;
	    	if (wasDelPrm == "true")
	    		document.forms["form1"].elements["del_button"].disabled = false;
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
                <jsp:param name="name" value="Search results"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
			<div style="margin-left:30">
			
			<%
            
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
        	    if (dataElements == null || dataElements.size()==0){
	        	    %>
	            	<b>No results found!</b>
	            	<%
	    	        if (user==null || !user.isAuthentic()){ %>
	    	        	<br/>
	    	        		This might be due to fact that you have not been authorized and there are<br/>
	    	        		no datasets at the moment ready to be published for non-authorized users.<br/>
	    	        		Please go to the <a href="datasets.jsp?SearchType=SEARCH">list of datasets</a>
							to see which of them are in which status!
	    	        	<br/><%
    	        	}
    	        	%>
	            	</div></TD></TR></table></body></html>
	            	<%
	            	return;
            	}
            }
            %>
            
			<form id="form1" method="POST" action="search_results.jsp" onsubmit="setLocation()">
			
		<table width="700" cellspacing="0">
			<tr><td colspan="3"><span class="head00">Search results</span></td></tr>
			<tr height="10"><td colspan="3"></td></tr>
		</table>
		
		<table width="700" cellspacing="0" border="0" cellpadding="2">
		
			<!-- the buttons part -->
		
			<tr>
			
				<td colspan="4" align="left" style="padding-bottom:5">
					<%
					boolean dstPrm = user!=null && SecurityUtil.hasChildPerm(user.getUserName(), "/datasets/", "u");
					if (dstPrm){
						%>
						<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>&nbsp;
						<%
					}
					
					if (user!=null){%>
						<input type="button" name="del_button" value="Delete selected" class="smallbutton" disabled onclick="deleteElement()"/><%
					}
					%>
				</td>
				<td align="right">
					<a target="_blank" href="help.jsp?screen=elements&area=pagehelp" onclick="pop(this.href)">
						<img src="images/pagehelp.jpg" border=0 alt="Get some help on this page">
					</a><br/>
				</td>
			</tr>
			
			<!-- the table itself -->
		
			<tr>
				<th width="3%">&nbsp;</th>
				<th width="30%" style="border-left: 0">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Element"/>
			            <jsp:param name="mapName" value="Element"/>
			            <jsp:param name="sortColNr" value="1"/>
			            <jsp:param name="help" value="help.jsp?screen=elements&area=element"/>
			        </jsp:include>
				</th>
				<th width="20%" style="border-left: 0">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Type"/>
			            <jsp:param name="mapName" value="Type"/>
			            <jsp:param name="sortColNr" value="2"/>
			            <jsp:param name="help" value="help.jsp?screen=elements&area=type"/>
			        </jsp:include>
				</th>
				<th width="25%" style="border-left: 0">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Table"/>
			            <jsp:param name="mapName" value="Table"/>
			            <jsp:param name="sortColNr" value="3"/>
			            <jsp:param name="help" value="help.jsp?screen=tables&area=table"/>
			        </jsp:include>
				</th>
				<th width="22%" style="border-left: 0">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Dataset"/>
			            <jsp:param name="mapName" value="Dataset"/>
			            <jsp:param name="sortColNr" value="4"/>
			            <jsp:param name="help" value="help.jsp?screen=datasets&area=dataset"/>
			        </jsp:include>
				</th>
			</tr>
				
			<%
			
			boolean wasDelPrm = false;
			if (searchType != null && searchType.equals(TYPE_SEARCH)){

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
					if (delem_type.equals("CH1")){
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
					boolean delPrm = user!=null &&
									 ds!=null &&
									 SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + ds.getIdentifier(), "u");
									 
					oEntry.setDelPrm(delPrm);
					if (delPrm)
						wasDelPrm = true;
					
					if (ds!=null)
						oEntry.oDsIdf = ds.getIdentifier();
						
					oResultSet.oElements.add(oEntry);
					
					String workingUser = verMan.getWorkingUser(dataElement.getNamespace().getID(),
		    											dataElement.getIdentifier(), "elm");
					String topWorkingUser = verMan.getWorkingUser(dataElement.getTopNs());
					
					boolean canDelete = topWorkingUser==null ||
										(dataElement.isWorkingCopy() &&
										workingUser!=null && user!=null &&
										workingUser.equals(user.getUserName()));
					
					String styleClass  = i % 2 != 0 ? "search_result_odd" : "search_result";
					
					%>
				
					<tr>
						<td width="3%" align="right" class="<%=styleClass%>">
							<%
		    				if (delPrm){
		    					
		    					if (topWorkingUser!=null){ // mark checked-out elements
			    					%> <font title="<%=topWorkingUser%>" color="red">*</font> <%
		    					}
	    					
		    					if (canDelete){ %>
									<input type="checkbox" style="height:13;width:13" name="delem_id" value="<%=delem_id%>"/>
									<%
									if (ds!=null){ %>
										<input type="hidden" name="ds_idf_<%=delem_id%>" value="<%=ds.getIdentifier()%>"/><%
									}
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
						
						<td width="30%" class="<%=styleClass%>">
							<a href="data_element.jsp?delem_id=<%=delem_id%>&amp;type=<%=delem_type%>&amp;mode=view">
							<%=Util.replaceTags(delem_name)%></a>
						</td>
						
						<td width="20%" class="<%=styleClass%>">
							<%=displayType%>
						</td>
						
						<td width="25%" class="<%=styleClass%>">
							<%=Util.replaceTags(dispTbl)%>
						</td>
						
						<td width="22%" class="<%=styleClass%>" style="border-right: 1 solid #C0C0C0">
							<%=Util.replaceTags(dispDs)%>
						</td>
					</tr>
				
				<%
				}
				%>
               	<tr><td colspan="5">&#160;</td></tr>
				<tr><td colspan="5">Total results: <%=dataElements.size()%></td></tr><%
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
                    
                    boolean delPrm = oEntry.getDelPrm();
					if (delPrm)
						wasDelPrm = true;
                    
                    String styleClass  = i % 2 != 0 ? "search_result_odd" : "search_result";

                    %>
					<tr>
						<%
						if (user != null){ %>
							<td width="3%" align="right" class="<%=styleClass%>"> <%
								if (delPrm){ %>
									<input type="checkbox" style="height:13;width:13" name="delem_id" value="<%=oEntry.oID%>"/><%
									if (oEntry.oDsName != null){%>
										<input type="hidden" name="ds_idf_<%=oEntry.oID%>" value="<%=oEntry.oDsIdf%>"/><%
									}
								}
								%>
								<input type="hidden" name="delem_name_<%=oEntry.oID%>" value="<%=oEntry.oShortName%>"/>
								<input type="hidden" name="ns_name_<%=oEntry.oID%>" value="<%=oEntry.oNs%>"/>
							</td><%
						}
						%>
						
						<td width="30%" class="<%=styleClass%>">
							<a href="data_element.jsp?delem_id=<%=oEntry.oID%>&amp;type=<%=oEntry.oType%>&amp;mode=view">
							<%=Util.replaceTags(oEntry.oShortName)%></a>
						</td>
						
						<td width="20%" class="<%=styleClass%>">
							<%=oEntry.oType%>
						</td>
						<td width="25%" class="<%=styleClass%>">
							<%=Util.replaceTags(oEntry.oTblName)%>
						</td>
						<td width="22%" class="<%=styleClass%>" style="border-right: 1 solid #C0C0C0">
							<%=Util.replaceTags(oEntry.oDsName)%>
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
		<input type="hidden" name="searchUrl" />
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

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {}
}
%>
