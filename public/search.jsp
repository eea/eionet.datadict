<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!final static String POPUP="popup";%>

<%!

private Vector attrs = null;
private Vector def_attrs = null;
private Vector attr_ids = null;
private Vector namespaces = null;
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
	
	namespaces = searchEngine.getNamespaces();
	if (namespaces == null) namespaces = new Vector();
	
	attr_ids = new Vector();
	def_attrs = new Vector();

	setDefaultAttrs("Name");
	setDefaultAttrs("Definition");
	setDefaultAttrs("Keywords");
	setDefaultAttrs("EEAissue");


	String attrID = null;
	String attrValue = null;
	String attrName = null;
	StringBuffer collect_attrs=new StringBuffer();

	String sel_attr = request.getParameter("sel_attr");
	String sel_type = request.getParameter("sel_type");
	String short_name = request.getParameter("short_name");
	String idfier = request.getParameter("idfier");
	String type = request.getParameter("type");
	String contextParam = request.getParameter("ctx");
    String sel_ds = request.getParameter("dataset");
	String search_precision = request.getParameter("search_precision");
	
	if (sel_attr == null) sel_attr="";
	if (sel_type == null) sel_type="";
	if (short_name == null) short_name="";
	if (idfier == null) idfier="";
	if (type == null) type="";
	if (sel_ds == null) sel_ds="";
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
    <title>Search elements - Data Dictionary</title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <link rel="stylesheet" type="text/css" href="layout-print.css" media="print" />
    <link rel="stylesheet" type="text/css" href="layout-screen.css" media="screen" />
    <link rel="stylesheet" type="text/css" href="layout-handheld.css" media="handheld" />
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
    <script type="text/javascript" src='script.js'></script>
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
			submitForm('search.jsp');

		}
		function onLoad(){
			<%
				if (type != null){
    			%>
					var sType = '<%=type%>';
					var o = document.forms["form1"].type;
					for (i=0; o!=null && i<o.options.length; i++){
						if (o.options[i].value == sType){
							o.selectedIndex = i;
							break;
						}
					}			
				<% 
				}
			%>
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
		
		function typeSelect(){
			var o = document.forms["form1"].type;			
			if (o!=null){
				var sel = o.selectedIndex;
				if (sel>=0){
				}
			}
		}
		
	// ]]>
	</script>
</head>

<%
if (contextParam == null || !contextParam.equals(POPUP)){ %>
	<body onfocus="checkalert()" onload="onLoad()">
	<%
}
else{ %>
	<body class="popup" onload="onLoad()"> <%
}
%>

		<%
			if (contextParam == null || !contextParam.equals(POPUP)){
		%>
                  <jsp:include page="nlocation.jsp" flush='true'>
                  <jsp:param name="name" value="Search dataelements"/>
                  <jsp:param name="back" value="true"/>
                </jsp:include>
    <%@ include file="nmenu.jsp" %>
		<%
		} else{ %>
				<div class="popuphead">
					<h1>Data Dictionary</h1>
					<hr/>
				</div><%
			}	
			%>
			
			<div id="workarea">
				<div id="operations">
					<ul>
							<li><a target="_blank" href="help.jsp?screen=search_element&amp;area=pagehelp" onclick="pop(this.href)" title="Get some help on this page">Page help</a></li>
					</ul>
				</div>
				<h1>Search for a data element definition</h1>
				<form acceptcharset="UTF-8" name="form1" action="search_results.jsp" method="get">
				
				<table width="auto" cellspacing="0" border="0">

					<tr valign="top">
						<td align="right" style="padding-right:10">
							<b>Type</b>
						</td>
						<td>
							<a target="_blank" href="help.jsp?screen=element&amp;area=type" onclick="pop(this.href)">
								<img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/>
							</a>
						</td>
						<td colspan="2">
							<select name="type" class="small" onchange="typeSelect()">
								<option value="">All</option>
								<option value="CH1">Data element with fixed values (codes)</option>
								<option value="CH2">Data element with quantitative values (e.g. measurements)</option>
							</select>
						</td>
					</tr>
					
					<%
					String fk = request.getParameter("fk");
					if (fk!=null && fk.equals("true") && sel_ds!=null && sel_ds.length()>0){%>
						<input type="hidden" name="dataset" value="<%=sel_ds%>"/>
						<input type="hidden" name="fk" value="<%=fk%>"/><%
					}
					else{%>

						<tr valign="top">
							<td align="right" style="padding-right:10">
								<b>Dataset</b>
							</td>
							<td>
								<a target="_blank" href="help.jsp?screen=table&amp;area=dataset" onclick="pop(this.href)">
									<img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/>
								</a>
							</td>
							<td colspan="2">
								<select name="dataset" class="small">
									<option value="">All</option>
									<option value="-1" <% if (sel_ds.equals("-1"))%>selected<%;%>>Not defined</option>
									<%
									Vector datasets = searchEngine.getDatasets();
									for (int i=0; datasets!=null && i<datasets.size(); i++){
										Dataset ds = (Dataset)datasets.get(i);
										String selected = (sel_ds!=null && sel_ds.equals(ds.getID())) ? "selected" : "";
										%>
										<option <%=selected%> value="<%=ds.getID()%>"><%=Util.replaceTags(ds.getShortName())%></option>
										<%
									}
									%>
								</select>
							</td>
						</tr><%
					}
					%>
					
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<b>Short name</b>
						</td>
						<td>
							<a target="_blank" href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href)">
								<img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/>
							</a>
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="59" name="short_name" value="<%=short_name%>"/>
						</td>
					</tr>
					
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<b>Identifier</b>
						</td>
						<td>
							<a target="_blank" href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href)">
								<img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/>
							</a>
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="59" name="idfier" value="<%=idfier%>"/>
						</td>
					</tr>
					
					<%
					/*
					attrID = getAttributeIdByName("Name");
					attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
					if (inputAttributes.containsKey(attrID)) inputAttributes.remove(attrID);
						
					if (attrID!=null){
						%>
						<tr valign="top">
							<td width="100"><b>
								<a href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=SIMPLE&amp;mode=edit">
									<font color="black">Name</font></a></b>:</td>
							<td width="300"><input type="text" size="40" name="attr_<%=attrID%>" value="<%=attrValue%>"/></td>
						</tr>
						<%
					}
					*/
					attrID = getAttributeIdByName("Language");
					if (attrID!=null){
						%>
						<tr valign="top">
							<td align="right" style="padding-right:10">
								<span class="mainfont"><b>Language</b></span>
							</td>
							<td align="right" style="padding-right:10">
								<a href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=SIMPLE&amp;mode=edit"><img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/></a>&#160;
							</td>
							<td colspan="2">
								<select name="attr_<%=attrID%>" class="small">
									<option selected value="">All</option>
									<option value="SQ">Albanian</option>
									<option value="BG">Bulgarian</option>
									<option value="CS">Czech</option>
									<option value="DA">Danish</option>
									<option value="NL">Ducth</option>
									<option value="ET">Estonian</option>
									<option value="FI">Finnish</option>
									<option value="FR">French</option>
									<option value="DE">German</option>
									<option value="EN">English</option>
									<option value="EL">Greek</option>
									<option value="HU">Hungarian</option>
									<option value="IS">Icelandic</option>
									<option value="GA">Irish</option>
									<option value="IT">Italian</option>
									<option value="LV">Latvian</option>
									<option value="LT">Lithuanian</option>
									<option value="NO">Norwegian</option>
									<option value="PL">Polish</option>
									<option value="PT">Portugese</option>
									<option value="RO">Romanian</option>
									<option value="RU">Russian</option>
									<option value="SK">Slovak</option>
									<option value="SL">Slovenian</option>
									<option value="ES">Spanish</option>
									<option value="SV">Sweden</option>
									<option value="TR">Turkish</option>
									<option value="UK">Ukranian</option>
								</select>
							</td>
						</tr>
						<%
					}
					/*
					attrID = getAttributeIdByName("Definition");
					attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
					if (inputAttributes.containsKey(attrID)) inputAttributes.remove(attrID);

					if (attrID!=null){
						%>
						<tr valign="top">
							<td width="100"><b>
								<a href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=SIMPLE&amp;mode=edit">
								<font color="black">Definition</font></a></b>:
							</td>
							<td width="300"><input type="text" name="attr_<%=attrID%>" size="40"  value="<%=attrValue%>"/></td>
						</tr>
						<%
					}
					
					attrID = getAttributeIdByName("Keywords");
					if (attrID!=null){
						%>					
						<tr name="r" style="display:none">
							<td width="100"><b>
								<a href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=SIMPLE&amp;mode=edit">
								<font color="black">Keywords</font></a></b>:
							</td>
							<td width="300"><input type="text" name="attr_<%=attrID%>" size="40" /></td>
						</tr>
						<%
					}
					*/

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
										<b><%=attrName%></b>
									</td>
									<td>
										<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href)">
											<img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/>
										</a>
									</td>
									<td colspan="2">
										<input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=attrValue%>"/>
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
									<b><%=attrName%></b>
								</td>
								<td>
									<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href)">
										<img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/>
									</a>
								</td>
								<td>
									<input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=attrValue%>"/>
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
									<b><%=attrName%></b>
								</td>
								<td>
									<a target="_blank" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href)">
										<img border="0" src="images/icon_questionmark.jpg" alt="Help" width="16" height="16"/>
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
						<td width="150" colspan="2">&nbsp;</td>
                		<td colspan="2" class="smallfont_light">
                			<input type="radio" name="search_precision" id="ssubstr" value="substr" checked="checked"/><label for="ssubstr">Substring search</label>
                			<input type="radio" name="search_precision" id="sexact" value="exact"/><label for="sexact">Exact search</label>&#160;&#160;
                			<input type="radio" name="search_precision" id="sfree" value="free"/><label for="sfree">Free text search</label>&#160;&#160;
                		</td>
                    </tr>
                    
                    <%
                    boolean commonOnly = request.getParameter("common")!=null;
                    boolean nonCommonOnly = request.getParameter("noncommon")!=null;
					
					if (!commonOnly && !nonCommonOnly){ %>
						<tr>
							<td colspan="2">&nbsp;</td>
							<td colspan="2" align="left" class="smallfont_light">
								<input type="radio" name="common" value="false" id="snoncom" checked="checked"/><label for="snoncom">Non-common elements</label>
								<input type="radio" name="common" id="scom" value="true"/><label for="scom">Common elements</label>
							</td>
						</tr><%
					}
					
					// if authenticated user, enable to get working copies only
					if (user!=null){ %>
						<tr>
							<td colspan="2">&nbsp;</td>
							<td colspan="2" align="left">
								<input type="checkbox" name="wrk_copies" value="true"/>
								<span class="smallfont_light">Working copies only</span>
							</td>
						</tr> <%
					}
					%>
					
                    
					<tr height="10"><td colspan="4"></td></tr>
					
					<tr valign="top">
						<td colspan="2"></td>
						<td>
							<input class="mediumbuttonb" type="button" value="Search" onclick="submitForm('search_results.jsp')"/>
							<input class="mediumbuttonb" type="reset" value="Reset"/>
						</td>
						<td align="left">
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
				
				<!-- table for 'Add' -->
				<%
				
					boolean dstPrm = user!=null &&
									 (SecurityUtil.hasChildPerm(user.getUserName(), "/datasets/", "u") ||
									 SecurityUtil.hasChildPerm(user.getUserName(), "/elements", "i"));
					if (dstPrm) { %>
					<table width="520">
						<tr height"10">
							<td colspan="2">&#160;</td>
						</tr>
						<tr>
							<td colspan="2" style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td>
						</tr>	
						<tr>
							<td width="10">&#160;</td>
							<td valign="bottom">
								<input class="mediumbuttonb" type="button" value="Add" onclick="window.location.assign('data_element.jsp?mode=add')"/>
								&#160;&#160;<span class="head00">a definition of a new non-common element</span>&#160;&#160;
							</td>
						</tr>
						<tr>
							<td width="10">&#160;</td>
							<td valign="bottom">
								<input class="mediumbuttonb" type="button" value="Add" onclick="window.location.assign('data_element.jsp?mode=add&common=true')"/>
								&#160;&#160;<span class="head00">a definition of a new common element</span>&#160;&#160;
							</td>
						</tr>
					</table> <%
					}
					%>
					
				<%
				}
				%>
				<input type="hidden" name="sel_attr" value=""/>
				<input type="hidden" name="sel_type" value=""/>
				
				<!-- collect all the attributes already used in criterias -->
				
				<input type="hidden" name="collect_attrs" value="<%=collect_attrs.toString()%>"/>
                <input name='SearchType' type='hidden' value='SEARCH'/>
                <input name="ctx" type="hidden" value="<%=contextParam%>"/>
                
                <%
                String skipID = request.getParameter("skip_id");
                if (skipID!=null && skipID.length()!=0){ %>
                	<input type="hidden" name="skip_id" value="<%=skipID%>"/><%
            	}
            	
                String selected = request.getParameter("selected");
                if (selected!=null && selected.length()!=0){
	                %>
	                <input name='selected' type='hidden' value='<%=selected%>'/>
	                <%
                }                
                
                if (commonOnly){ %>
                	<input type="hidden" name="common" value="true"/><%
            	}
            	
            	if (nonCommonOnly){ %>
            		<input type="hidden" name="common" value="false"/><%
        		}
        		%>
                
				</form>
			</div>
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
