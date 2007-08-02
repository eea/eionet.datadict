<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

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
	StringBuffer collect_attrs = new StringBuffer();
	HashSet displayedCriteria = new HashSet();

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
	<%@ include file="headerinfo.jsp" %>
	<title>Search elements - Data Dictionary</title>
	<script type="text/javascript">
	// <![CDATA[
		attrWindow=null;

		function submitForm(action){
			
			document.forms["form1"].action=action;
			document.forms["form1"].submit();
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
			
			if (document.forms["form1"].snoncom.checked)
				changeFormStateForNonCommon();
			else if (document.forms["form1"].scom.checked)
				changeFormStateForCommon();
		}
		
		function typeSelect(){
			var o = document.forms["form1"].type;			
			if (o!=null){
				var sel = o.selectedIndex;
				if (sel>=0){
				}
			}
		}
		
		function changeFormStateForCommon(){
			document.forms["form1"].dataset_idf.selectedIndex = 0;
			document.forms["form1"].dataset_idf.disabled = true;
			document.forms["form1"].wrk_copies.disabled = false;
		}
		
		function changeFormStateForNonCommon(){
			document.forms["form1"].dataset_idf.disabled = false;
			document.forms["form1"].wrk_copies.checked = false;
			document.forms["form1"].wrk_copies.disabled = true;
		}
		
	// ]]>
	</script>
</head>

<%
boolean isPopup = contextParam!=null && contextParam.equals(POPUP);
StringBuffer bodyAttrs = new StringBuffer("onload=\"onLoad()\"");
if (isPopup)
	bodyAttrs.append(" class=\"popup\"");
%>

<body <%=bodyAttrs.toString()%>>
	
<%
if (!isPopup){
	%>
	<div id="container">
	<jsp:include page="nlocation.jsp" flush="true">
		<jsp:param name="name" value="Search dataelements"/>
		<jsp:param name="helpscreen" value="search_element"/>
	</jsp:include>
	<%@ include file="nmenu.jsp" %>
	<div id="workarea">
<%
}
else{ %>
	<div id="pagehead">
	    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
	    <div id="networktitle">Eionet</div>
	    <div id="sitetitle">Data Dictionary (DD)</div>
	    <div id="sitetagline">This service is part of Reportnet</div>    
	</div> <!-- pagehead -->
	<div id="workarea">
	<%
}

boolean isDisplayOperations = isPopup;
if (isDisplayOperations==false)
	isDisplayOperations = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/elements", "i");
if (isDisplayOperations){
	%>
	<div id="operations">
		<ul>
			<%
			if (isPopup){ %>
				<li><a href="javascript:window.close();">Close</a></li>
				<li class="help"><a href="help.jsp?screen=search_element&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li><%
			}
			else if (user!=null && SecurityUtil.hasPerm(user.getUserName(), "/elements", "i")){
				%>
				<li><a title="Add a definition of a new common element" href="javascript:window.location.assign('data_element.jsp?mode=add&amp;common=true')">New common element</a></li><%
			}
			%>
		</ul>
	</div><%
}
%>
				
				<h1>Search data elements</h1>
				<br/>
				<form id="form1" action="search_results.jsp" method="get">
					<%
					String fk = request.getParameter("fk");
					if (fk!=null && fk.equals("true") && sel_ds!=null && sel_ds.length()>0){
						%>
						<div style="display:none">
							<input type="hidden" name="dataset" value="<%=sel_ds%>"/>
							<input type="hidden" name="fk" value="<%=fk%>"/>
						</div><%
					}
					%>
				<table width="auto" cellspacing="0" style="clear:right">

					<%					
					if ((fk!=null && fk.equals("true") && sel_ds!=null && sel_ds.length()>0)==false){
						%>
						<tr valign="top">
							<td align="right" style="padding-right:10">
								<b>Dataset</b>
							</td>
							<td>
								<a href="help.jsp?screen=search_element&amp;area=dataset" onclick="pop(this.href);return false;">
									<img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
								</a>
							</td>
							<td colspan="2">
								<select name="dataset_idf" class="small">
									<option value="">All</option>
									<%
									Vector datasets = searchEngine.getDatasets();
									for (int i=0; datasets!=null && i<datasets.size(); i++){
										Dataset ds = (Dataset)datasets.get(i);
										String selected = (sel_ds!=null && sel_ds.equals(ds.getID())) ? "selected" : "";
										%>
										<option <%=selected%> value="<%=ds.getIdentifier()%>"><%=Util.replaceTags(ds.getShortName())%></option>
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
							<b>Type</b>
						</td>
						<td>
							<a href="help.jsp?screen=element&amp;area=type" onclick="pop(this.href);return false;">
								<img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
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
					
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<b>Short name</b>
						</td>
						<td>
							<a href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
								<img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
							</a>
						</td>
						<td colspan="2">
							<input type="text" class="smalltext" size="59" name="short_name" value="<%=Util.replaceTags(short_name, true)%>"/>
						</td>
					</tr>
					
					<tr valign="top">
						<td align="right" style="padding-right:10">
							<b>Identifier</b>
						</td>
						<td>
							<a href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
								<img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
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
								<a href="delem_attribute.jsp?attr_id=<%=attrID%>&amp;type=SIMPLE&amp;mode=edit"><img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/></a>&nbsp;
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
								displayedCriteria.add(attrID);
								%>
								<tr valign="top">
									<td align="right" style="padding-right:10">
										<b><%=Util.replaceTags(attrName)%></b>
									</td>
									<td>
										<a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
											<img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
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
							displayedCriteria.add(attrID);
							%>
							<tr valign="top">
								<td align="right" style="padding-right:10">
									<b><%=Util.replaceTags(attrName)%></b>
								</td>
								<td>
									<a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
										<img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
									</a>
								</td>
								<td>
									<input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=Util.replaceTags(attrValue, true)%>"/>
								</td>
								<td>
									<a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" style="border:0" alt="Remove attribute from search criterias"/></a>
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
							displayedCriteria.add(attrID);
							attrName = getAttributeNameById(attrID);
							%>
							<tr valign="top">
								<td align="right" style="padding-right:10">
									<b><%=Util.replaceTags(attrName)%></b>
								</td>
								<td>
									<a href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
										<img style="border:0" src="images/info_icon.gif" alt="Help" width="16" height="16"/>
									</a>
								</td>
								<td>
									<input type="text" class="smalltext" name="attr_<%=attrID%>" size="59" value=""/>
								</td>
								<td>
									<a href="javascript:selAttr(<%=attrID%>, 'remove');"><img src="images/button_remove.gif" style="border:0" alt="Remove attribute from search criterias"/></a>
								</td>
							</tr>
							<%
						}
					}
					%>
					
					<tr valign="bottom">
						<td style="width:150px" colspan="2">&nbsp;</td>
                		<td colspan="2" class="smallfont_light">
                			<input type="radio" name="search_precision" id="ssubstr" value="substr" checked="checked"/><label for="ssubstr">Substring search</label>
                			<input type="radio" name="search_precision" id="sexact" value="exact"/><label for="sexact">Exact search</label>&nbsp;&nbsp;
                		</td>
                    </tr>
                    
                    <%
                    boolean commonOnly = request.getParameter("common")!=null;
                    boolean nonCommonOnly = request.getParameter("noncommon")!=null;
					
					if (!commonOnly && !nonCommonOnly){ %>
						<tr>
							<td colspan="2">&nbsp;</td>
							<td colspan="2" align="left" class="smallfont_light">
								<input type="radio" name="common" value="false" id="snoncom" checked="checked" onchange="changeFormStateForNonCommon()"/><label for="snoncom">Non-common elements</label>
								<input type="radio" name="common" id="scom" value="true" onchange="changeFormStateForCommon()"/><label for="scom">Common elements</label>
							</td>
						</tr><%
					}
					
					// if authenticated user, enable to get working copies only
					if (user!=null){
						if (fk==null || !fk.equals("true")){ %>
							<tr>
								<td colspan="2">&nbsp;</td>
								<td colspan="2" align="left">
									<input type="checkbox" name="wrk_copies" id="wrk_copies" value="true"/>
									<label for="wrk_copies" class="smallfont" style="font-weight: normal">Working copies only</label>
								</td>
							</tr> <%
						}
						else{ %>
							<input type="hidden" name="wrk_copies" id="wrk_copies" value="true"/><%
						}
					}
					
					if (fk==null || !fk.equals("true")){ %>
						<tr>
							<td colspan="2">&nbsp;</td>
							<td colspan="2" align="left">
								<input type="checkbox" name="incl_histver" id="incl_histver" value="true"/>
								<label for="incl_histver" class="smallfont" style="font-weight: normal">Include historic versions</label>
							</td>
						</tr><%
					}
					%>
                    
					<tr style="height:10px;"><td colspan="4"></td></tr>
					
					<tr valign="top">
						<td colspan="2"></td>
						<td>
							<input class="mediumbuttonb" type="button" value="Search" onclick="submitForm('search_results.jsp')"/>
							<input class="mediumbuttonb" type="reset" value="Reset"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						</td>
					</tr>
					<%
					Vector addCriteria = new Vector();
					for (int i=0; attrs!=null && i<attrs.size(); i++){
						
						DElemAttribute attribute = (DElemAttribute)attrs.get(i);
						
						if (type.equals("")){
							if (!attribute.displayFor("CH1") && !attribute.displayFor("CH2"))
								continue;
						}
						else if (!attribute.displayFor(type))
							continue;
					
						if (!displayedCriteria.contains(attribute.getID())){
							Hashtable hash = new Hashtable();
							hash.put("id", attribute.getID());
							hash.put("name", attribute.getShortName());
							addCriteria.add(hash);
						}
					}
					
					if (addCriteria.size()>0){
						%>
						<tr>
							<td colspan="4" style="text-align:right">
								<label for="add_criteria">Add criteria</label>
								<select name="add_criteria" id="add_criteria" onchange="selAttr(this.options[this.selectedIndex].value, 'add')">
									<option value=""></option>
									<%
									for (int i=0; i<addCriteria.size(); i++){
										Hashtable hash = (Hashtable)addCriteria.get(i);
										%>
										<option value="<%=hash.get("id")%>"><%=hash.get("name")%></option><%
									}
									%>
								</select>
							</td>
						</tr><%
					}
					%>
				</table>

					<div style="display:none"> <!-- hidden inputs -->
						<input type="hidden" name="sel_attr" value=""/>
						<input type="hidden" name="sel_type" value=""/>
						
						<!-- collect all the attributes already used in criterias -->
						
						<input type="hidden" name="collect_attrs" value="<%=Util.replaceTags(collect_attrs.toString(), true)%>"/>
		                <input name='SearchType' type='hidden' value='SEARCH'/>
		                <input name="ctx" type="hidden" value="<%=Util.replaceTags(contextParam, true)%>"/>
		                
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
		        		String strExclude = request.getParameter("exclude");
		        		if (strExclude!=null){%>
		        			<input type="hidden" name="exclude" value="<%=strExclude%>"/><%
		    			}
		    			String skipTableID = request.getParameter("skip_table_id");
		    			if (skipTableID!=null && skipTableID.length()>0){ %>
		    				<input type="hidden" name="skip_table_id" value="<%=skipTableID%>"/><%
						}
		    			if (fk!=null && fk.equals("true")){ %>
		    				<input type="hidden" name="for_fk_use" value="true"/><%
						}
		
		        		%>
					</div>                
				</form>
			</div> <!-- workarea -->
	<%
	if (!isPopup){
		%>
		</div> <!-- container -->
		<jsp:include page="footer.jsp" flush="true" /><%
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
