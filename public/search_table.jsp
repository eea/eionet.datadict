<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,com.tee.xmlserver.*,eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!final static String POPUP="popup";%>


<%!

private Vector attrs = null;
private Vector def_attrs = null;
private Vector attr_ids = null;
ServletContext ctx = null;
private String sel_attr = null;
private Hashtable inputAttributes=null;

private String getAttributeIdByName(String name){
	
	for (int i=0; i<attrs.size(); i++){
		DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}

private String getAttributeNameById(String id){
	
	for (int i=0; i<attrs.size(); i++){
		DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getID().equals(id))
        	return attr.getShortName();
	}
        
    return null;
}

private String setDefaultAttrs(String name){

	String id = getAttributeIdByName(name);
	if (id!=null)
		def_attrs.add(id);

	return null;
}
%>

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	
	ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	try { // start the whole page try block
	
	conn = pool.getConnection();

	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

	attrs = searchEngine.getDElemAttributes();
	if (attrs == null) attrs = new Vector();
	
	attr_ids = new Vector();
	def_attrs = new Vector();

	setDefaultAttrs("Name");
	setDefaultAttrs("Definition");
	setDefaultAttrs("Keywords");

	String attrID = null;
	String attrValue = null;
	String attrName = null;
	StringBuffer collect_attrs=new StringBuffer();

	String sel_attr = request.getParameter("sel_attr");
	String sel_type = request.getParameter("sel_type");
	String short_name = request.getParameter("short_name");
	String idfier = request.getParameter("idfier");
	String full_name = request.getParameter("full_name");
	String definition = request.getParameter("definition");
	String search_precision = request.getParameter("search_precision");
	String contextParam = request.getParameter("ctx");

	
	String submitForm=null;
	if (contextParam != null && contextParam.equals(POPUP))
		submitForm = "pick_table.jsp";
	else
		submitForm = "search_results_tbl.jsp";

	if (sel_attr == null) sel_attr="";
	if (sel_type == null) sel_type="";
	if (short_name == null) short_name="";
	if (idfier == null) idfier="";
	if (full_name == null) full_name="";
	if (definition == null) definition="";
	if (search_precision == null) search_precision="substr";

	///get inserted attributes
	String input_attr;
	inputAttributes = new Hashtable();
	for (int i=0; i<attrs.size(); i++){	
		DElemAttribute attribute = (DElemAttribute)attrs.get(i);		
		String attr_id = attribute.getID();
		
		input_attr = request.getParameter("attr_" + attr_id);
		if (input_attr!=null){
			inputAttributes.put(attr_id, input_attr);
			attr_ids.add(attr_id);
		}
	}
	if (contextParam == null || !contextParam.equals(POPUP)){
		%><%@ include file="history.jsp"%><%
	}

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Search tables - Data Dictionary</title>
	<script type="text/javascript" language="javascript">
	// <![CDATA[
		attrWindow=null;

		function submitForm(action){
			
			document.forms["form1"].action=action;
			document.forms["form1"].submit();
		}

		function openAttributes(){
			var type = document.forms["form1"].type.value;
			var selected = document.forms["form1"].collect_attrs.value;
			attrWindow=window.open('pick_attribute.jsp?type=' + type + "&selected=" + selected,"Search","height=450,width=300,status=no,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
			if (window.focus) {attrWindow.focus()}
		}
		function checkalert()
		{
			if (attrWindow != null) {
			   if (!attrWindow.closed) attrWindow.focus();
			}
		}
		function selAttr(id, type){
			document.forms["form1"].sel_attr.value=id;
			document.forms["form1"].sel_type.value=type;
			submitForm('search_table.jsp');

		}
		
		function onLoad(){
			<%
				if (search_precision != null){
    			%>
					var sPrecision = '<%=search_precision%>';
					var o = document.forms["form1"].search_precision;
					for (i=0; o!=null && i<o.length; i++){
						if (o[i].value == sPrecision){
							o[i].checked = true;
							break;
						}
					}			
				<% 
				}
			%>
		}
	// ]]>
	</script>
</head>

<%
if (contextParam == null || !contextParam.equals(POPUP)){ %>
	<body onclick="checkalert()" onload="onLoad()">
<%
}
else { %>
	<body class="popup" onload="onLoad()">
<%
}
%>
		<%
			if (contextParam == null || !contextParam.equals(POPUP)){
		%>
                  <jsp:include page="nlocation.jsp" flush='true'>
                  <jsp:param name="name" value="Search"/>
                  <jsp:param name="back" value="true"/>
                </jsp:include>
    <%@ include file="nmenu.jsp" %>
		<%
		}
			else{ %>
			
				<div class="popuphead">
					<h1>Data Dictionary</h1>
					<hr/>
				</div><%
			}	
			%>
			
				<div id="workarea">

        <div id="operations">
          <ul>
              <li><a target="_blank" href="help.jsp?screen=search_table&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
          </ul>
        </div>

				<h1>Search for a dataset table definition</h1>
				<form name="form1" action="search_results_tbl.jsp" method="get"
 style="border-top:#008B8B solid 1pt;">
				
				<table width="auto" cellspacing="0">
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<b>Short name</b>
						</td>
						<td>
							<a target="_blank" href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
								<img border="0" src="images/info_icon.gif" width="16" height="16" alt=""/>
							</a>
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="59" name="short_name" value="<%=Util.replaceTags(short_name)%>"/>
						</td>
					</tr>
					
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<b>Identifier</b>
						</td>
						<td>
							<a target="_blank" href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
								<img border="0" src="images/info_icon.gif" width="16" height="16" alt=""/>
							</a>
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="59" name="idfier" value="<%=idfier%>"/>
						</td>
					</tr>

					<!--tr align="top">
						<td align="right" style="padding-right:10">
							<a href="javascript:openFulltName()"><span class="help">?</span></a>&#160;
							<span class="mainfont"><b>Full name</b></span>&#160;&#160;
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="40" name="full_name" value="<%=full_name%>"/>
						</td>
					</tr>
					<tr align="top">
						<td align="right" style="padding-right:10">
							<a href="javascript:openDefinition()"><span class="help">?</span></a>&#160;
							<span class="mainfont"><b>Definition</b></span>&#160;&#160;
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="40" name="definition" value="<%=definition%>"/>
						</td>
					</tr-->
					
					<%
					//get default attributes, which are always on the page (defined above)
					if (def_attrs!=null){
						for (int i=0; i < def_attrs.size(); i++){
							attrID = (String)def_attrs.get(i);
							attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
							
							attrName = getAttributeNameById(attrID);

							if (inputAttributes.containsKey(attrID)) inputAttributes.remove(attrID);

							if (attrID!=null){
								collect_attrs.append(attrID + "|");
								%>
								<tr valign="top">
									<td align="right" style="padding-right:10">
										<b><%=Util.replaceTags(attrName)%></b>
									</td>
									<td>
										<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
											<img border="0" src="images/info_icon.gif" width="16" height="16" alt=""/>
										</a>
									</td>
									<td colspan="2">
										<input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=Util.replaceTags(attrValue, true)%>"/>
									</td>
								</tr>
								<%
							}
						}
					}
					// get attributes selected from picked list (get the ids from url)
					if (attr_ids!=null){
						for (int i=0; i < attr_ids.size(); i++){
							attrID = (String)attr_ids.get(i);
							 
							if (!inputAttributes.containsKey(attrID)) continue;
							if (sel_type.equals("remove") && attrID.equals(sel_attr)) continue;

							attrName = getAttributeNameById(attrID);

							attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
							if (attrValue == null) attrValue="";
							collect_attrs.append(attrID + "|");
							%>
							<tr valign="top">
								<td align="right" style="padding-right:10">
									<b><%=Util.replaceTags(attrName)%></b>
								</td>
								<td>
									<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
										<img border="0" src="images/info_icon.gif" width="16" height="16" alt=""/>
									</a>
								</td>
								<td>
									<input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=Util.replaceTags(attrValue, true)%>"/>
								</td>
								<td>
									<a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" border="0" alt="Remove attribute from search criterias"/></a>
								</td>
							</tr>
							<%
						}
					}
					// add the last selection
					if (sel_type!=null && sel_attr!=null){
						if (sel_type.equals("add")){
							attrID = sel_attr;
							collect_attrs.append(attrID + "|");
							attrName = getAttributeNameById(attrID);
							%>
							<tr valign="top">
								<td align="right" style="padding-right:10">
									<b><%=Util.replaceTags(attrName)%></b>
								</td>
								<td>
										<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
											<img border="0" src="images/info_icon.gif" width="16" height="16" alt=""/>
										</a>
									</td>
								<td>
									<input type="text" class="smalltext" name="attr_<%=attrID%>" size="59" value=""/>
								</td>
								<td>
									<a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" border="0" alt="Remove attribute from search criterias"/></a>
								</td>
							</tr>
							<%
						}
					}
					%>
					
                    <tr valign="bottom">
                		<td width="150" colspan="2">&#160;</td>
                    	<td colspan="2">
                			<input type="radio" name="search_precision" id="ssubstr" value="substr" checked="checked"/><label for="ssubstr">Substring search</label>
                			<input type="radio" name="search_precision" id="sexact" value="exact"/><label for="sexact">Exact search</label>&#160;&#160;
                			<input type="radio" name="search_precision" id="sfree" value="free"/><label for="sfree">Free text search</label>&#160;&#160;
                		</td>
                    </tr>
                    
                    <%					
					// if authenticated user, enable to get working copies only
					if (user!=null && user.isAuthentic()){
						%>
						<tr valign="top">
							<td width="150" colspan="2"></td>
							<td colspan="2">
								<input type="checkbox" name="wrk_copies" value="true"/><span class="smallfont" style="font-weight: normal">Working copies only</span>
							</td>
						</tr>
						<%
					}
					%>
					
					<tr style="height:10px;"><td colspan="4"></td></tr>
					
					<tr valign="top">
						<td colspan="2"></td>
						<td>
							<input class="mediumbuttonb" type="button" value="Search" onclick="submitForm('<%=submitForm%>')"/>
							<input class="mediumbuttonb" type="reset" value="Reset"/>
						</td>
						<td align="right">
							<%
								if (contextParam == null || !contextParam.equals(POPUP)){
							%>
								<a href="javascript:openAttributes();"><img src="images/button_plus.gif" border="0" alt="Click here to add more search criterias"/></a>
							<%
							}
							%>
						</td>
					</tr>
				</table>
				
				<%
					if (contextParam == null || !contextParam.equals(POPUP)){
				%>
				<%
				boolean dstPrm = user!=null && SecurityUtil.hasChildPerm(user.getUserName(), "/datasets/", "u");
				if (dstPrm) { %>
					<table width="500">			
						<tr style="height:10px;"><td>&#160;</td></tr>				
						<tr><td style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
						<tr>
							<td valign="bottom">
								<input class="mediumbuttonb" type="button" value="Add" onclick="window.location.assign('dstable.jsp?mode=add')"/>
								&#160;&#160;<span class="head00">a new table</span>&#160;&#160;
							</td>
						</tr>		
					</table>
				<%
				}
				%>
				<%
				}
				%>
				
				
				<input type="hidden" name="sel_attr" value=""/>			
				<input type="hidden" name="sel_type" value=""/>
				<input type="hidden" name="type" value="TBL"/>
                <input name='SearchType' type='hidden' value='SEARCH'/>
				<!-- collect all the attributes already used in criterias -->
				<input type="hidden" name="collect_attrs" value="<%=Util.replaceTags(collect_attrs.toString(), true)%>"/>
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
