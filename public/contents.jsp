<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>


<%!private final static String CONTENTS_PREV_URL="CONTENTS_PREV_URL";%>
<%!private final static String ELM_CONTENTS_NAME="ELM_CONTENTS_NAME";%>
<%!private final static String CONTENTS_HISTORY="CONTENTS_HISTORY";%>

<%@ include file="history.jsp" %>

<%!

private boolean isIn(Vector elems, String id){
	
	for (int i=0; id!=null && i<elems.size(); i++){
		
		Object o = elems.get(i);
		Class oClass = o.getClass();
		if (oClass.getName().endsWith("Hashtable")) continue;
		
		DataElement elem = (DataElement)o;
        if (elem.getID().equalsIgnoreCase(id))
        	return true;
	}
        
    return false;
}


private String legalizeAlert(String in){
        
    in = (in != null ? in : "");
    StringBuffer ret = new StringBuffer(); 
  
    for (int i = 0; i < in.length(); i++) {
        char c = in.charAt(i);
        if (c == '\'')
            ret.append("\\'");
        else if (c == '\\')
        	ret.append("\\\\");
        else
            ret.append(c);
    }

    return ret.toString();
}

%>

<%

// set the response headers and authenticate user

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

XDBApplication.getInstance(getServletContext());
AppUserIF user = SecurityUtil.getUser(request);

ServletContext ctx = getServletContext();			
String appName = ctx.getInitParameter("application-name");

/*DDuser user = new DDuser(DBPool.getPool(appName));

String username = "root";
String password = "ABr00t";
boolean f = user.authenticate(username, password);*/

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
}						

// get the request parameters

String parent_id = request.getParameter("parent_id");
if (parent_id == null || parent_id.length()==0){ %>
	<b>Parent element ID is missing!</b> <%
	return;
}

String parent_type = request.getParameter("parent_type");
if (parent_type == null || parent_type.length()==0){ %>
	<b>Parent type is missing!</b> <%
	return;
}

String parent_name = request.getParameter("parent_name");
if (parent_name == null) parent_name = "anonymous";

String parent_ns = request.getParameter("parent_ns");
if (parent_ns == null) parent_ns = "anonymous";

String contentID   = request.getParameter("content_id");
String contentType = request.getParameter("content_type");

// -> deal with them God damned back urls


/*
String backUrl = null;
String thisUrl = null;
boolean reloadOpener = false;
HttpSession sess = request.getSession();
if (request.getMethod().equals("GET")){

	String qryString = request.getQueryString();
	thisUrl = qryString==null ? request.getRequestURI() : request.getRequestURI() + "?" + qryString;
	
	int i = -1;
	String opener = new String("&opener=true");
	if ((i = thisUrl.indexOf(opener)) != -1){
		thisUrl = thisUrl.substring(0, i) + thisUrl.substring(i + opener.length());
		reloadOpener = true;
	}
		
	String opening = new String("&open=true");
	Vector history = (Vector)sess.getAttribute(CONTENTS_HISTORY);
	if ((i= thisUrl.indexOf(opening)) != -1){
		history = new Vector();
		thisUrl = thisUrl.substring(0, i) + thisUrl.substring(i + opening.length());
	}
	
	if (history == null) history = new Vector();
	
	if (!history.contains(thisUrl)){
		history.add(thisUrl);
		sess.setAttribute(CONTENTS_HISTORY, history);
	}
	
	int curPos = history.indexOf(thisUrl);
	if (curPos > 0)
		backUrl = (String)history.get(curPos-1);
}
else{
	String backUrlParam = request.getParameter("backUrl");
	if (backUrlParam != null && backUrlParam.length()!=0){
		backUrl = backUrlParam;
		//sess.setAttribute(CONTENTS_PREV_URL, backUrl);
	}
}
*/
// <- end of dealing with those annoying back urls

// check if we are defining an anonymous choice or a sequence
if (!parent_type.equals("elm")){
	
	if (contentType==null){ %>
		<b>Content type is missing!</b> <%
		return;
	}
}
/*else if (request.getMethod().equals("GET")){
	
	backUrl = (String)request.getSession().getAttribute(CONTENTS_PREV_URL);
	
	String qryString = request.getQueryString();
	if (qryString == null) qryString = "";
	
	HttpSession sess = request.getSession();
	sess.setAttribute(CONTENTS_PREV_URL, new String(request.getRequestURI() + "?" + qryString));
	ctx.log("set CONTENTS_PREV_URL to " + request.getRequestURI() + "?" + qryString);
	
	String thisUrl = request.getRequestURI() + "?" + qryString;
	String prevUrl = (String)request.getSession().getAttribute(CONTENTS_PREV_URL);
	if (prevUrl == null || !thisUrl.equals(prevUrl)){
		sess.setAttribute(CONTENTS_PREV_URL, thisUrl);
		ctx.log("set CONTENTS_PREV_URL to " + thisUrl);
	}
	
	String wasext = request.getParameter("wasext");
	String urlAttr  = wasext == null ? EXT_CONTENTS_URL : ELM_CONTENTS_URL;
	String nameAttr = wasext == null ? EXT_CONTENTS_NAME : ELM_CONTENTS_NAME;
	
	sess.setAttribute(urlAttr, new String(request.getRequestURI() + "?" + qryString));	
	sess.setAttribute(nameAttr, new String(parent_ns + ":" + parent_name));
	
	//ctx.log("sess.setAttribute("CONTENTS_PREV_URL", " + new String(request.getRequestURI() + "?" + qryString) + ");");
}*/

String extID = request.getParameter("ext_id");
String extChoiceID = request.getParameter("ext_chc_id");
String extSequenceID = request.getParameter("ext_seq_id");

String extContentID = null;
String extContentType = null;
if (extChoiceID != null){
	extContentID = extChoiceID;
	extContentType = "chc";
}
else if (extSequenceID != null){
	extContentID = extSequenceID;
	extContentType = "seq";
}

if (extContentType != null)
	contentType = extContentType;

if (contentType == null){ %>
	<b>There is no way to determine the type (choice or sequence) of the content!</b> <%
	return;
}

String wasext = request.getParameter("wasext");

// -> deal with POST
if (request.getMethod().equals("POST")){
	
	String prevContentID = contentID;
	Connection userConn = null;
	
	try{
		userConn = user.getConnection();
		SubElemsHandler handler = new SubElemsHandler(userConn, request, ctx);
		
		try{
			handler.execute();
			contentID = handler.getContentID();
		}
		catch (Exception e){
			%>
			<html><body><b><%=e.toString()%></b></body></html>
			<%
			return;
		}
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}
	
	//String redirUrl = request.getParameter("thisUrl");
	String redirUrl = currentUrl;
	if (redirUrl == null){
	
		StringBuffer buf = new StringBuffer("" + "/contents.jsp?");
		
		buf.append("wasPost=true");
		
		if (parent_id != null) buf.append("&parent_id=" + parent_id);
		if (parent_type != null) buf.append("&parent_type=" + parent_type);
		if (parent_name != null) buf.append("&parent_name=" + parent_name);
		if (parent_ns != null) buf.append("&parent_ns=" + parent_ns);
		
		if (contentID != null) buf.append("&content_id=" + contentID);
		if (contentType != null) buf.append("&content_type=" + contentType);
		
		if (extID != null) buf.append("&ext_id=" + extID);
		if (extChoiceID != null) buf.append("&ext_chc_id=" + extChoiceID);
		if (extSequenceID != null) buf.append("&ext_seq_id=" + extSequenceID);
		
		if (wasext != null) buf.append("&wasext=" + wasext);
		
		redirUrl = buf.toString();
	}
	else if (contentID != null && (prevContentID==null || !prevContentID.equals(contentID))){
		/*Vector history = (Vector)sess.getAttribute(CONTENTS_HISTORY);
		if (history != null){
			history.remove(redirUrl);
			sess.setAttribute(CONTENTS_HISTORY, history);
		}
		*/
		if (history!=null){
			int idx = history.getCurrentIndex();
			if (backUrl.indexOf("content.jsp")>0){ //EK  if user added first sub object 
				history.remove(idx-1);
				idx--;
		}
			if (idx>0)
				history.remove(idx);
		}
		//ctx.log("ja siia");
		redirUrl = redirUrl + "&content_id=" + contentID;
		if (contentType != null){
			int i = redirUrl.indexOf("&content_type=seq");
			int j = redirUrl.indexOf("&content_type=chc");
			if (i==-1 && j==-1)
				redirUrl = redirUrl + "&content_type=" + contentType;
		}
	}
	else if (contentID==null && prevContentID!=null){   //EK kui contenti ei ole lisatud
		
		//ctx.log("tulime siia");
		
		if (history!=null){
			int idx = history.getCurrentIndex();
			if (idx>0)
				history.remove(idx);
		}
		/*Vector history = (Vector)sess.getAttribute(CONTENTS_HISTORY);
		
		if (parent_type.equals("elm")){
			sess.setAttribute(CONTENTS_HISTORY, new Vector());
		}
		else{
			if (history != null){
				history.remove(redirUrl);
				sess.setAttribute(CONTENTS_HISTORY, history);
			}
		}
		*/	
		String s = new String("&content_id=" + prevContentID);
		int i = redirUrl.indexOf(s);
		if (i != -1)
			redirUrl = redirUrl.substring(0,i) + redirUrl.substring(i + s.length());
		
		String origContentType = request.getParameter("orig_content_type");
		if (origContentType!=null){
			//ctx.log("check0: " + origContentType);
		}
		
		if (origContentType != null && extContentType != null && !origContentType.equals(extContentType)){
			//ctx.log("check1: " + origContentType + " " + extContentType);
			s = new String("&content_type=" + origContentType);
			i = redirUrl.indexOf(s);
			if (i != -1){
				ctx.log("check2");
				redirUrl = redirUrl.substring(0,i) + redirUrl.substring(i + s.length());
			}
		}
	}
	
	// -> this is for checking if a sub was deleted or a new one added, the opener needs to be reloaded then
	
	/* EK 030711
	if (parent_type.equals("elm")){
		if (prevContentID==null){
			if (contentID!=null)
				redirUrl = redirUrl + "&opener=true";
		}
		else{
			if (contentID==null)
				redirUrl = redirUrl + "&opener=true";
			else if (!prevContentID.equals(contentID))
				redirUrl = redirUrl + "&opener=true";
		}
	}*/
	// <-
				
	
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setDateHeader("Expires", 0);
	
	response.sendRedirect(redirUrl);
	return;
}

// <- deal with POST

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block
	
conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

Vector dataElements = searchEngine.getDataElements(); // ask for all data elements
Vector subElems = (contentID==null) ? null : searchEngine.getSubElements(contentType, contentID); // ask for this content
Vector extElems = (extContentID==null) ? null : searchEngine.getSubElements(extContentType, extContentID);

int extElemCount = extElems==null ? 0 : extElems.size();

String origContentType = request.getParameter("content_type");
if (contentID!=null && origContentType!=null && !origContentType.equals(extContentType) && extElemCount > 0){
	Hashtable hash = new Hashtable();
	hash.put("child_type", origContentType);
	hash.put("child_id", contentID);
	hash.put("child_min_occ", "1");
	hash.put("child_max_occ", "1");
	
	subElems = new Vector();
	subElems.add(hash);
}


// add extElems to subElems
if (extElems != null && extElemCount > 0){
	if (subElems == null)
		subElems = new Vector();
	for (int i=extElems.size()-1; i>=0; i--)
		subElems.add(0,extElems.get(i));
}

int position = 0;

StringBuffer collect_elems=new StringBuffer();
if (parent_type.equals("elm"))
	collect_elems.append(parent_id + "|");

%>

<html>
	<head>
		<title>Meta</title>
		<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
		<link href="eionet.css" rel="stylesheet" type="text/css"/>
	
    	<script language="JavaScript" src='script.js'></script>
	</head>
	<script language="JavaScript">
			function submitForm(mode){
				if (mode=="add"){
					if (checkElement()==false)
						return false;
				}
				if (mode=="delete"){
					var b = confirm("This will delete all the subelements you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
					if (b==false) return;
				}

				document.forms["form1"].elements["mode"].value = mode;
				document.forms["form1"].submit();
			}
			
			function openSequenceHelp(){
				alert("Sequence is an ordered list of data elements. Each of its elements in XML document must appear in the specified order. " +
					"For each element it is possible to specify how many times it can occur at its specified location. This can vary from " +
					"zero to unlimited number of occurabces.");
			}

			function openChoiceHelp(){
				alert("Choice is a group of data elements from which  o n l y  o n e  can appear in the XML document! A choice is naturally not ordered " +
						"and it can contain as many elements as you like.");
			}
			
			function load(){
				<% 
				/*if (reloadOpener){
					%>
					window.opener.location.reload(false);
					<%
				}*/
				%>
			}
			function openAdd(url){
			
				var selected = document.forms["form1"].collect_elems.value;
				if (url != null) url = url + "&selected=" + selected;
				wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
				if (window.focus) {wAdd.focus()}
			}
			function pickElem(id, name){
				document.forms["form1"].child.value=id;
				n = document.getElementById("child_name");
				while (name.indexOf(">")!=-1) {
					name=name.replace(">", "&gt;");
				}
				while (name.indexOf("<")!=-1) {
					name=name.replace("<", "&lt;");
				}
				n.innerHTML = name;

				return true;
			
			}
			//checks, if element is selected
			function checkElement(){
				for (i=0; i<document.forms["form1"].childType.length;i++){
					if (document.forms["form1"].childType[i].value == "elm"){
						if (document.forms["form1"].childType[i].checked == true){
							if (document.forms["form1"].child.value == "0"){
								alert("Data element is not selected!");
								return false;
							}
						}
						
					}
				}
				if (document.forms["form1"].parent_type.value != "elm"){
					if (document.forms["form1"].child.value == "0"){
						alert("Data element is not selected!");
						return false;
					}
				}
				return true;
			}
	</script>
	
<body onload="load()">
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
                <jsp:param name="name" value="Allowable value"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">
	
<form name="form1" method="POST" action="contents.jsp">

<table width="560">

	<%
	String dispName = parent_name;
	if (parent_type.equals("elm"))
		dispName = parent_ns + ":" + parent_name;
	%>

	<tr valign="bottom">
		<td colspan="4"><font class="head00">Subelements of <font class="title2" color="#006666"><%=Util.replaceTags(dispName)%></font></font></td>
	</tr>
	
	<%
	String type = contentType.equals("chc") ? "choice" : "sequence";
	String contraType = contentType.equals("chc") ? "sequence" : "choice";
	String help = contentType.equals("chc") ? "javascript:openChoiceHelp()" : "javascript:openSequenceHelp()";
	String contraHelp = contentType.equals("chc") ? "javascript:openSequenceHelp()" : "javascript:openChoiceHelp()";
	
	if (!parent_type.equals("elm")){
		String name = (String)request.getSession().getAttribute(ELM_CONTENTS_NAME);
		if (name == null)
			name = "?";
		%>
		<tr valign="bottom">
			<td colspan="4">
				You are viewing the contents of a <a href="<%=help%>"><font color="black"><b><%=type%></b></font></a> which is part of
				the contents of <%=name%>. You can add a new <a href="<%=contraHelp%>"><font color="black"><b><%=contraType%></b>
				</font></a> or element by using the radio buttons, select box(es) and 'Add' button below. If you select to add a new
				<%=contraType%>, an empty <%=contraType%> will be created for you and you can specify its contents by clikcing on its
				link in the list. To remove contents, use the select boxes and 'Remove' button.
			</td>
		</tr>
		<%
	}	
	else if (extID == null){
		%>
		<tr valign="bottom">
			<td colspan="4">
				The contents of this data element have been specified to form a <a href="<%=help%>"><font color="black"><b><%=type%></b>
				</font></a> and are displayed below. If you don't see any, none have been specified. You can add a new
				<a href="<%=contraHelp%>"><font color="black"><b><%=contraType%></b></font></a> or element by using the radio buttons,
				select box(es) and 'Add' button below. If you select to add a new <%=contraType%>, an empty <%=contraType%> will be
				created for you and you can specify its contents by clikcing on its link in the list. To remove contents, use the select
				boxes and 'Remove' button.
			</td>
		</tr>
		<%
	}
	else if (extID != null && extElemCount ==0){
		DataElement extElem = searchEngine.getDataElement(extID);
		if (extElem != null){
			%>
			<tr valign="bottom">
				<td colspan="4">
					This data element extends the contents of <b><%=extElem.getShortName()%></b>.
					Since no contents have  been specified for the latter, none is also displayed in the list below. However, this
					data element can still have its own contents which have been specified to form a <a href="<%=help%>"><font color="black">
					<b><%=type%></b></font></a> and are displayed below. If you don't see any, none have been specified. You can add a new
					<a href="<%=contraHelp%>"><font color="black"><b><%=contraType%></b></font></a> or element by using the radio buttons,
					select box(es) and 'Add' button below. If you select to add a new <%=contraType%>, an empty <%=contraType%> will be
					created for you and you can specify its contents by clikcing on its link in the list. To remove contents, use the select
					boxes and 'Remove' button.
				</td>
			</tr>
			<%
		}
	}
	else if (extID != null && extElemCount > 0 && contentID==null){
		DataElement extElem = searchEngine.getDataElement(extID);
		if (extElem != null){
			%>
			<tr valign="bottom">
				<td colspan="4">
					This data element extends the contents of <b><%=extElem.getShortName()%></b>
					which are displayed in the beginning of the subelements list below, in <font color="#006666">light green</font>
					color. They have been specified to form a <a href="<%=help%>"><font color="black"><b><%=type%></b></font></a> and you
					can not remove them. The contents of this data element itself will become an addition to the base element's
					<%=type%>. You can add a new <%=contraType%> or element by using the radio buttons, select box(es) and 'Add' button
					below. If you select to add a new <%=contraType%>, an empty <%=contraType%> will be created for you and you can
					specify its contents by clikcing on its link in the list. To remove contents, use the select boxes and 'Remove'
					button.
				</td>
			</tr>
			<%
		}
	}
	//else if (extID != null && extElemCount > 0 && contentID!=null){
	else if (contentID!=null && origContentType!=null && !origContentType.equals(extContentType) && extElemCount > 0){
		
		String origType = origContentType.equals("chc") ? "choice" : "sequence";
		String origHelp = origContentType.equals("chc") ? "javascript:openChoiceHelp()" : "javascript:openSequenceHelp()";
		
		DataElement extElem = searchEngine.getDataElement(extID);
		if (extElem != null){
			%>
			<tr valign="bottom">
				<td colspan="4">
					This data element extends the contents of <b><%=extElem.getShortName()%></b>
					which are displayed in the beginning of the subelements list below, in <font color="#006666">light green</font>
					color. They have been specified to form a <a href="<%=help%>"><font color="black"><b><%=type%></b></font></a> and you
					can not remove them. The contents of this data element itself have previously been specified to form a 
					<a href="<%=origHelp%>"><font color="black"><b><%=origType%></b></font></a> which you can view and edit by clicking
					on its link at the bottom of the below list. This <%=origType%> acts as an addition to base element's <%=type%>.
					You can delete it by using the checkbox and 'Remove' button or by removing all its contents in its editing view.
				</td>
			</tr>
			<%
		}
	}
	else if (contentID!=null && origContentType!=null && origContentType.equals(extContentType) && extElemCount > 0){
		
		DataElement extElem = searchEngine.getDataElement(extID);
		if (extElem != null){
			%>
			<tr valign="bottom">
				<td colspan="4">
					This data element extends the contents of <b><%=extElem.getShortName()%></b>
					which are displayed in the beginning of the subelements list below, in <font color="#006666">light green</font>
					color. They have been specified to form a <a href="<%=help%>"><font color="black"><b><%=type%></b></font></a> and you
					can not remove them. The contents of this data element itself act as an addition to the base element's
					<%=type%>. You can add a new <%=contraType%> or element by using the radio buttons, select box(es) and 'Add' button
					below. If you select to add a new <%=contraType%>, an empty <%=contraType%> will be created for you and you can
					specify its contents by clikcing on its link in the list. To remove contents, use the select boxes and 'Remove'
					button.
				</td>
			</tr>
			<%
		}
	}
	/*
	if (backUrl != null){
		%>
		<tr valign="bottom">
			<td colspan="4" align="left"><a href="<%=backUrl%>">< back</a></td>
		</tr>
		<%
	}
	*/
	//if (!parent_type.equals("elm")){
	
	%>
	
	<tr height="5"><td colspan="10">&#160;</td></tr>
</table>

<%
//if (!(contentID!=null && !origContentType.equals(extContentType) && extElemCount > 0)){
if (!(contentID!=null && origContentType!=null && !origContentType.equals(extContentType) && extElemCount > 0)){
	
	if (user != null){ %>
		<input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')"/> <%
	}
	else{ %>
		<input type="button" value="Add" style="font-family:Arial;font-size:10px;font-weight:bold" onclick="submitForm('add')" disabled="true"/> <%
	}
	
	if (contentType.equals("seq")){
		%>
		<table style="padding-left:10;border: 1 solid #808080" cellspacing="0" cellpadding="0">
		<%
	}
	else{
		%>
		<table style="padding-left:10" cellspacing="0" cellpadding="0">
		<%
	}
	%>
	<tr>
		<td rowspan="2">
			<%
		
			if (parent_type.equals("elm")){
				%>
						<% if (contentType.equals("chc")){ %>
							<input name="childType" type="radio" value="seq"><span class="mainfont"><b>Sequence</b></span></input>
							<%
						}
						else{ %>
							<input name="childType" type="radio" value="chc"><span class="mainfont"><b>Choice</b></span></input>
							<%
						} %>
				<%
			}
			%>

			<br/>
			<% if (contentType.equals("seq")){
				%> <br/>
				<%
			} %>
			
					<%
					if (parent_type.equals("elm")){
						%>
						<input name="childType" type="radio" value="elm" checked><b>Element</b>:</input>&#160;
						<%
					}
					else{
						%>
						<input name="childType" type="hidden" value="elm"/>
						&#160;<b>Element</b>:</input>&#160;
						<%
					}
					%>
					
					<input name="child" type="hidden" value="0"/>
					<span id="child_name">element is not selected</span>
					<a href="javascript:openAdd('search.jsp?ctx=popup')">select</a>
			</br>			
		</td>
		<% if (contentType.equals("seq")){ %>
			<td rowspan="2" style="padding-bottom:3">
				<font class="smallFont"><b>Min occurs</b></font><br/>
				<select class="small" name="min_occ" style="width:100">
					<option selected="true" value="0">0</option>
					<option value="1">1</option>
				</select><br/>
				<font class="smallFont"><b>Max occurs</b></font><br/>
				<select class="small" name="max_occ" style="width:100">
					<option selected="true" value="1">1</option>
					<option value="unbounded">unbounded</option>
				</select>
			</td>
			<%
		}
		%>
	</tr>
	</table>
	<%
}
%>

<!-- end of table for new child / start of table of existing children -->

<br/>
<table width="auto" cellspacing="0">

	<tr>
		<td align="right" style="padding-right:10">
			<% if (user!=null){ %>
				<input class="smallbutton" type="button" value="Remove" onclick="submitForm('delete')"/> <%
			}
			else{ %>
				<input class="smallbutton" type="button" value="Remove" disabled="true"/> <%
			} %>
		</td>
		
		<% if (contentType.equals("seq")){
			%>
			<th align="left" style="padding-left:5;padding-right:10">Child</th>
			<th align="left" style="padding-right:10">Min Occurs</th>
			<th align="left" style="padding-right:10">Max Occurs</th>
			<%
		}
		else{
			%>
			<th align="left" style="padding-left:5">Child</th>
			<%
		}
		%>
	</tr>
	
	<%
	
	for (int i=0; subElems!=null && i<subElems.size(); i++){
	
		Object o = subElems.get(i);
		Class oClass = o.getClass();
		String oClassName = oClass.getName();
		
		DataElement elem = null;
		Hashtable child = null;
		if (oClassName.endsWith("DataElement"))
			elem = (DataElement)o;
		else if (oClassName.endsWith("Hashtable"))
			child = (Hashtable)o;
		else
			continue;
		
		String childID = "";
		String childType = "";
		String childName = "";
		String childMinOcc = "";
		String childMaxOcc = "";
		String childPos = "";
		
		StringBuffer childLink = new StringBuffer();
		
		if (elem != null){
			
			childID = elem.getID();
			collect_elems.append(childID + "|");
			childType = "elm";
			
			String elemName = elem.getShortName();
			if (elemName == null) elemName = "unknown";
			if (elemName.length()==0) elemName = "empty";
				
			childName = elemName;
			
			childMinOcc = elem.getMinOccurs();
			childMaxOcc = elem.getMaxOccurs();
			childPos = elem.getPosition();
			childLink.append("data_element.jsp?mode=view&delem_id=");
			childLink.append(elem.getID());
			childLink.append("&type=");
			childLink.append(elem.getType());
			
			/*if (elem.getPosition() != null){
				int pos = Integer.parseInt(elem.getPosition());
				if (pos >= position) position = pos +1;
			}*/
		}
		else{
			
			childID = (String)child.get("child_id");
			childType = (String)child.get("child_type");
			if (childType.equals("chc"))
				childName = "choice_";
			else
				childName = "sequence_";
			
			if (childID != null) childName = childName + childID;
			
			childMinOcc = (String)child.get("child_min_occ");
			childMaxOcc = (String)child.get("child_max_occ");
			childPos = (String)child.get("child_pos");
			
			childLink.append("contents.jsp?parent_type=");
			
			if (contentID!=null && origContentType!=null && !origContentType.equals(extContentType) && extElemCount > 0){
				childLink.append(parent_type);
				childLink.append("&parent_id=");
				childLink.append(parent_id);
				childLink.append("&parent_ns=");
				childLink.append(parent_ns);
				childLink.append("&parent_name=");
				childLink.append(parent_name);
				childLink.append("&wasext=true");
			}
			else{
				childLink.append(contentType);
				childLink.append("&parent_id=");
				childLink.append(contentID);
				childLink.append("&parent_name=");
				childLink.append(childName);
			}
				
			childLink.append("&content_type=");
			childLink.append(childType);
			childLink.append("&content_id=");
			childLink.append(childID);			
		}
		
		String color = i<extElemCount ? "#006666" : "black";
		
		int pos = 0;
		try{
			pos = Integer.parseInt(childPos);
		}
		catch (Exception e){}
		
		if (contentType.equals("seq")){
			%>
			<tr>
				<td align="right" style="padding-right:10">
					<% if (i>=extElemCount){						
						if (pos >= position) position = pos +1;
						%>
						<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=childID%>"/>
						<input type="hidden" name="del_type_<%=childID%>" value="<%=childType%>"/>
						<%
					} %>
				</td>
				
				<% if (childType.equals("elm") || i<extElemCount){
					%>
					<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<font color="<%=color%>"><%=Util.replaceTags(childName)%></font>&#160;<a href="<%=childLink%>"><span class="info"><b>(i)<b></span></a>
					</td>
					<%
				}
				else{
					%>
					<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<a href="<%=childLink%>"><font color="<%=color%>"><%=childName%></font></a>
					</td>
					<%
				}
				%>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<font color="<%=color%>"><%=childMinOcc%></font>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<font color="<%=color%>"><%=childMaxOcc%></font>
				</td>
			</tr>
			<%
		}
		else{
			%>
			<tr>
				<td align="right" style="padding-right:10">
					<% if (i>=extElemCount){
						%>
						<input type="checkbox" style="height:13;width:13" name="del_id" value="<%=childID%>"/>
						<input type="hidden" name="del_type_<%=childID%>" value="<%=childType%>"/>
						<%
					} %>
				</td>
				
				<% if (childType.equals("elm") || i<extElemCount){
					%>
						<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
							<font color="<%=color%>"><%=Util.replaceTags(childName)%></font>&#160;&#160;<a href="<%=childLink%>"><span class="info"><b>(i)<b></span></a>
						</td>
					<%
				}
				else{
					%>
					<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<a href="<%=childLink%>"><font color="<%=color%>"><%=childName%></font></a>
					</td>
					<%
				}
				%>
			</tr>
			<%
		}
	}
	%>

</table>

<input type="hidden" name="mode" value="add"></input>

<input type="hidden" name="position" value="<%=String.valueOf(position)%>"></input>

<%
/*if (backUrl != null){
	%>
	<input type="hidden" name="backUrl" value="<%=backUrl%>"></input>
	<%
}

if (thisUrl != null){
	%>
	<input type="hidden" name="thisUrl" value="<%=thisUrl%>"></input>
	<%
}*/


if (parent_id != null){ %>
	<input type="hidden" name="parent_id" value="<%=parent_id%>"></input> <%
}

if (parent_type != null){ %>
	<input type="hidden" name="parent_type" value="<%=parent_type%>"></input> <%
}

if (parent_name != null){ %>
	<input type="hidden" name="parent_name" value="<%=parent_name%>"></input> <%
}
	
if (parent_ns != null){ %>
	<input type="hidden" name="parent_ns" value="<%=parent_ns%>"></input> <%
}

if (contentID != null){ %>
	<input type="hidden" name="content_id" value="<%=contentID%>"></input> <%
}

if (contentType != null){ %>
	<input type="hidden" name="content_type" value="<%=contentType%>"></input> <%
}

if (origContentType != null){ %>
	<input type="hidden" name="orig_content_type" value="<%=origContentType%>"></input> <%
}

if (extID != null){ %>
	<input type="hidden" name="ext_id" value="<%=extID%>"></input> <%
}

if (extChoiceID != null){ %>
	<input type="hidden" name="ext_chc_id" value="<%=extChoiceID%>"></input> <%
}

if (extSequenceID != null){ %>
	<input type="hidden" name="ext_seq_id" value="<%=extSequenceID%>"></input> <%
}
%>
<input type="hidden" name="collect_elems" value="<%=collect_elems.toString()%>"></input>

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
