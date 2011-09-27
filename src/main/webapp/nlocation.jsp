<%@page import="java.util.*,eionet.util.SecurityUtil,eionet.meta.DDUser,eionet.meta.LoginServlet"%>
<%
ServletContext ctx = getServletContext();
String appName = ctx.getInitParameter("application-name");
%>
<div id="toolribbon">
	<div id="lefttools">
        <%@ include file="topleftlinks.txt" %>
    </div>
    <div id="righttools">
    	<%
		DDUser _user = SecurityUtil.getUser(request);
		if (_user!=null){
			%>
			<a id="logoutlink" href="logout.jsp" title="Logout">Logout (<%=_user.getUserName()%>)</a><%
		}
		else{
			%>
			<a id="loginlink" href="<%=SecurityUtil.getLoginURL(request)%>" title="Login">Login</a><%
		}

		String helpScreen = request.getParameter("helpscreen");
		if (helpScreen!=null){
			%>
			<a id="pagehelplink" title="Get help on this page" href="help.jsp?screen=<%=helpScreen%>&amp;area=pagehelp" onclick="pop(this.href);return false;"><span>Page help</span></a><%
		}
		%>
		<a id="printlink" title="Print this page" href="javascript:this.print();"><span>Print</span></a>
        <a id="fullscreenlink" href="javascript:toggleFullScreenMode()" title="Switch to/from full screen mode"><span>Switch to/from full screen mode</span></a>
        <a id="acronymlink" href="http://www.eionet.europa.eu/acronyms" title="Look up acronyms"><span>Acronyms</span></a>
        <form action="http://search.eionet.europa.eu/search.jsp" method="get">
			<div id="freesrchform"><label for="freesrchfld">Search</label>
				<input type="text" id="freesrchfld" name="query"/>

				<input id="freesrchbtn" type="image" src="images/button_go.gif" alt="Go"/>
			</div>
		</form>
    </div>
</div> <!-- toolribbon -->

<div id="pagehead">
    <%@ include file="pagehead.txt" %>
</div> <!-- pagehead -->


<div id="menuribbon">
	<%@ include file="dropdownmenus.txt" %>
</div>
<div class="breadcrumbtrail">
	<div class="breadcrumbhead">You are here:</div>
	<div class="breadcrumbitem eionetaccronym"><a href="http://www.eionet.europa.eu">Eionet</a></div>
	<%
	String contextName = request.getParameter("context_name");
	String contextPath = request.getParameter("context_path");
	if (contextPath==null)
		contextPath = "";
	String lastItemName = request.getParameter("name");
	if (lastItemName!=null && contextName==null){
		%>
		<div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/index.jsp"><%=appName %></a></div>
		<div class="breadcrumbitemlast"><%=lastItemName%></div><%
	}
	else if (lastItemName==null && contextName!=null){
		%>
		<div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/index.jsp"><%=appName %></a></div>
		<div class="breadcrumbitemlast"><%=contextName%></div><%
	}
	else if (lastItemName!=null && contextName!=null){
		%>
		<div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/index.jsp"><%=appName %></a></div>
		<div class="breadcrumbitem"><a href="<%=contextPath%>"><%=contextName%></a></div>
		<div class="breadcrumbitemlast"><%=lastItemName%></div><%
	}
	else if (lastItemName==null && contextName==null){
		%>
		<div class="breadcrumbitemlast"><%=appName %></div><%
	}
	%>
	<div class="breadcrumbtail">
	</div>
</div>