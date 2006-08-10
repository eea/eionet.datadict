<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!private String mode=null;%>
<%!private FixedValue fxv=null;%>

<%@ include file="history.jsp" %>

<%

request.setCharacterEncoding("UTF-8");

XDBApplication.getInstance(getServletContext());
AppUserIF user = SecurityUtil.getUser(request);

ServletContext ctx = getServletContext();			
String appName = ctx.getInitParameter("application-name");

if (request.getMethod().equals("POST") && user == null){ %>
	<b>Not authorized to post any data!</b> <%
	return;
}						

String fxv_id = request.getParameter("fxv_id");

String delem_id = request.getParameter("delem_id");			
if (delem_id == null || delem_id.length()==0){ %>
	<b>Parent ID is missing!</b> <%
	return;
}
			
String parent_type = request.getParameter("parent_type");
if (parent_type == null)
	parent_type = "CH1";
else if (!parent_type.equals("CH1") && !parent_type.equals("CH2") && !parent_type.equals("attr")){ %>
	<b>Unknown parent type!</b> <%
	return;
}
			
String valsType = "CH1";
if (!parent_type.equals("attr")){
	valsType = parent_type;
	parent_type = "elem";
}
String typeParam = parent_type.equals("attr") ? "attr" : valsType;

String initCaseTitle = valsType.equals("CH1") ? "Allowable" : "Suggested";
String lowerCaseTitle = valsType.equals("CH1") ? "allowable" : "suggested";
String upperCaseTitle = valsType.equals("CH1") ? "ALLOWABLE" : "SUGGESTED";
			
String dispParentType = parent_type.equals("elem") ? "element" : "attribute";
			
String delem_name = request.getParameter("delem_name");
if (delem_name == null) delem_name = "?";
			
mode = request.getParameter("mode");
if (mode == null || mode.length()==0) { %>
	<b>Mode paramater is missing!</b>
	<%
	return;
}

if (!mode.equals("add") && (fxv_id == null || fxv_id.length()==0)){ %>
	<b>Value ID is missing!</b> <%
	return;
}

if (request.getMethod().equals("POST")){
	
	Connection userConn = null;
	String redirUrl = "";
	
	try{
		userConn = user.getConnection();
		FixedValuesHandler handler = new FixedValuesHandler(userConn, request, ctx);
		
		try {
			handler.execute();
		}
		catch (Exception e){
			%>
			<html><body>
				<b><%=e.toString()%></b><br/>
				<a href="javascript:window.location.replace('<%=currentUrl%>')">< back</a>
				
			</body></html>
			<%
			return;
		}
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}
	
	if (mode.equals("edit")){
		redirUrl=currentUrl;
	}
	else if (mode.equals("delete")){
		String deleteUrl = history.gotoLastNotMatching("fixed_value.jsp");

		if (deleteUrl!=null&&deleteUrl.length()>0) 
			redirUrl=deleteUrl;
		else 
			redirUrl = redirUrl + "fixed_values.jsp?mode=edit&delem_id=" + delem_id +
												 "&delem_name=" + delem_name +
												 "&parent_type=" + parent_type;
	}
	
	response.sendRedirect(redirUrl);
	return;
}
			
Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();
			
try { // start the whole page try block
			
	conn = pool.getConnection();
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	String value = "";
	if (!mode.equals("add")){
		fxv = searchEngine.getFixedValue(fxv_id);
		if (fxv!=null){
			value = fxv.getValue();
			if (value == null) value = "unknown";
			if (value.length() == 0) value = "empty";
		}
		else{ %>
			<b>Value was not found!</b> <%
			return;
		}
	}
		
	String disabled = user == null ? "disabled='disabled'" : "";
	
	boolean isWorkingCopy = parent_type.equals("elem") ? searchEngine.isWorkingCopy(delem_id, "elm") : true;

	//find parent url from history
	String parentUrl="";
	if (parent_type.equals("elem")){
		parentUrl="data_element.jsp?mode=view&amp;delem_id="+delem_id;
		if (history!=null){
			String elemUrl = history.getLastMatching("data_element.jsp");
		
			if (elemUrl.indexOf("delem_id=" + delem_id)>-1)
				parentUrl = elemUrl;
			if (delem_name.equals("?")){
				DataElement elem = searchEngine.getDataElement(delem_id);
				if (elem!=null)	delem_name=elem.getShortName();
				if (delem_name == null) delem_name = "?";
			}
		}
	}
	else{
		String mm = mode.equals("print") ? "view" : mode;
		parentUrl="delem_attribute.jsp?attr_id=" + delem_id + "&amp;type=SIMPLE&amp;mode=" + mm;
		if (history!=null){
			String attrUrl = history.getLastMatching("delem_attribute.jsp");
		
			if (attrUrl.indexOf("delem_id=" + delem_id)>-1)
				parentUrl = attrUrl;
		}
	}
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Data Dictionary - Fixed value</title>
	<script type="text/javascript">
	// <![CDATA[

		function submitForm(mode){
			
			if (mode == "delete"){
				var b = confirm("This value will be deleted! Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			if (mode != "delete"){
				if (!checkObligations()){
					alert("You have not specified one of the mandatory atttributes!");
					return;
				}
									
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function checkObligations(){
			
			var o = document.forms["form1"].class_name;
			if (o!=null){
				if (o.value.length == 0) return false;
			}
			
			var elems = document.forms["form1"].elements;
			if (elems == null) return true;
			
			for (var i=0; i<elems.length; i++){
				var elem = elems[i];
				var elemName = elem.name;
				var elemValue = elem.value;
				if (startsWith(elemName, "attr_")){
					var o = document.forms["form1"].elements[i+1];
					if (o == null) return false;
					if (!startsWith(o.name, "oblig_"))
						continue;
					if (o.value == "M" && (elemValue==null || elemValue.length==0)){
						return false;
					}
				}
			}
			
			return true;
		}
		
		function startsWith(str, pattern){
			var i = str.indexOf(pattern,0);
			if (i!=-1 && i==0)
				return true;
			else
				return false;
		}
		
		function endsWith(str, pattern){
			var i = str.indexOf(pattern, str.length-pattern.length);
			if (i!=-1)
				return true;
			else
				return false;
		}

		function onLoad(){    
			<%
			String isDefault = "";
			if (!mode.equals("add")){
				isDefault = fxv.getDefault() ? "true" : "false";
    			%>
    			var isDefault = '<%=isDefault%>';
				var o = document.forms["form1"].is_default;
				for (i=0; o!=null && i<o.options.length; i++){
					if (o.options[i].value == isDefault){
						o.selectedIndex = i;
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
<body onload="onLoad()">
        	<%
        	if (valsType.equals("CH1")){ %>
	            <jsp:include page="nlocation.jsp" flush='true'>
	                <jsp:param name="name" value="Allowable value"/>
	                <jsp:param name="back" value="true"/>
	            </jsp:include><%
            }
            else{ %>
            	<jsp:include page="nlocation.jsp" flush='true'>
	                <jsp:param name="name" value="Suggested value"/>
	                <jsp:param name="back" value="true"/>
	            </jsp:include><%
        	}
        	%>
            
    <%@ include file="nmenu.jsp" %>
<div id="workarea">

	<%
	String backURL = "" + "/fixed_values.jsp?delem_id=" + delem_id +
															 "&delem_name=" + delem_name +
															 "&parent_type=" + parent_type;
	%>
			
		<div id="operations">
		<ul>
			<li class="help"><a target="_blank" href="help.jsp?screen=fixed_value&amp;area=pagehelp" title="Get some help on this page" onclick="pop(this.href);return false;">Page help</a></li>

		</ul>
		</div>
    <h1><%=Util.replaceTags(initCaseTitle)%> value of <a href="<%=Util.replaceTags(parentUrl, true)%>"><%=Util.replaceTags(delem_name, true)%></a> <%=dispParentType%></h1>
			
		<form name="form1" method="post" action="fixed_value.jsp">
		<table width="auto" cellspacing="0" cellpadding="0">
			<tr>				
				<td align="right" style="padding-right:10px" valign="top">
					<b><font color="black">Value</font></b>(M)
				</td>
				<td colspan="1" valign="top">
					<% if(!mode.equals("add")){ %>
						<em><%=Util.replaceTags(value)%></em>
						<input type="hidden" name="fxv_value" value="<%=Util.replaceTags(value, true)%>"/>
					<% } else{ %>
						<input class="smalltext" type="text" size="30" name="fxv_value"/>
					<% } %>
				</td>
			</tr>
			
			<%
			if (parent_type.equals("attr")){ %>
				<tr>				
					<td align="right" style="padding-right:10" valign="top">
						<b><font color="black">Default</font></b>(O)
					</td>
					<td colspan="1" valign="top">
						<select <%=disabled%> class="small" name="is_default">
							<option selected="selected" value="false">No</option>
							<option value="true">Yes</option>
						</select>
					</td>
				</tr> <%
			}
			%>
			
			<tr>
				<td align="right" style="padding-right:10" valign="top">
					<b><font color="black">Definition</font></b>(O)
				</td>
				<td valign="top">
					<textarea class="small" rows="3" cols="60" name="definition"><%=Util.replaceTags(fxv.getDefinition(), true, true)%></textarea>
				</td>
			</tr>
			
			<tr>
				<td align="right" style="padding-right:10" valign="top">
					<b><font color="black">Short description</font></b>(O)
				</td>
				<td valign="top">
					<textarea class="small" rows="3" cols="60" name="short_desc"><%=Util.replaceTags(fxv.getShortDesc(), true, true)%></textarea>
				</td>
			</tr>
		
		<tr>
			<td>&#160;</td>
			<td colspan="2">
			
				<% 
				
				if (mode.equals("add")){ // if mode is "add"
					if (user==null){ %>									
						<input class="mediumbuttonb" type="button" value="Add" disabled="disabled"/>&#160;&#160;
					<%} else {%>
						<input class="mediumbuttonb" type="button" value="Add" onclick="submitForm('add')"/>&#160;&#160;
					<% }
				} // end if mode is "add"
				
				if (!mode.equals("add") && !mode.equals("print")){ // if mode is not "add" and not "print"
					if (user==null || !isWorkingCopy){ %>									
						<input class="mediumbuttonb" type="button" value="Save" disabled="disabled"/>&#160;&#160;
						<input class="mediumbuttonb" type="button" value="Delete" disabled="disabled"/>&#160;&#160;
					<%} else {%>
						<input class="mediumbuttonb" type="button" value="Save" onclick="submitForm('edit')"/>&#160;&#160;
						<input class="mediumbuttonb" type="button" value="Delete" onclick="submitForm('delete')"/>&#160;&#160;
					<% }
				} // end if mode is not "add"
				
				%>
				
			</td>
		</tr>
		
		
	</table>
	<input type="hidden" name="mode" value="<%=mode%>"/>
	<input type="hidden" name="fxv_id" value="<%=fxv_id%>"/>
	<input type="hidden" name="del_id" value="<%=fxv_id%>"/>
	<input type="hidden" name="delem_id" value="<%=delem_id%>"/>
	<input type="hidden" name="delem_name" value="<%=Util.replaceTags(delem_name, true)%>"/>
	
	<input type="hidden" name="parent_type" value="<%=Util.replaceTags(valsType, true)%>"/>
	
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
	} catch (SQLException e){}
}
%>
