<%@page import="java.util.*,eionet.util.SecurityUtil,com.tee.xmlserver.AppUserIF,eionet.meta.LoginServlet,eionet.meta.filters.EionetCASFilter"%>

<div id="toolribbon">
	<div id="lefttools">
      <a id="eealink" href="http://www.eea.europa.eu/">EEA</a>
      <a id="ewlink" href="http://www.ewindows.eu.org/">EnviroWindows</a>
    </div>
    <div id="righttools">    
    	<%
		AppUserIF _user = SecurityUtil.getUser(request);
		if (_user!=null){
			%>
			<a id="logoutlink" href="logout.jsp" title="Logout">Logout (<%=_user.getUserName()%>)</a><%
		}
		else{
	    	String loginUrl = EionetCASFilter.hasInitBeenCalled()==false ? "javascript:login()" : EionetCASFilter.getCASLoginURL(request);
			%>
			<a id="loginlink" href="<%=loginUrl%>" title="Login">Login</a><%
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
    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
    <div id="networktitle">Eionet</div>
    <div id="sitetitle">Data Dictionary (DD)</div>
    <div id="sitetagline">This service is part of Reportnet</div>
</div> <!-- pagehead -->


<div id="menuribbon">
	<%@ include file="dropdownmenus.txt" %>
</div>
<div class="breadcrumbtrail">
	<div class="breadcrumbhead">You are here:</div>
	<div class="breadcrumbitem eionetaccronym"><a href="http://www.eionet.europa.eu">Eionet</a></div>	
	<%
	String lastItemName = request.getParameter("name");
	if (lastItemName!=null && lastItemName.trim().length()>0 && !lastItemName.equals("null")){
		%>
		<div class="breadcrumbitem"><a href="index.jsp">Data Dictionary</a></div>
		<div class="breadcrumbitemlast"><%=lastItemName%></div><%
	}
	else{
		%>
		<div class="breadcrumbitemlast">Data Dictionary</div><%
	}
	%>
<div class="breadcrumbtail">
</div>
</div>
