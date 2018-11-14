<%@page import="java.util.*,eionet.util.SecurityUtil,eionet.util.Props,eionet.util.PropsIF,eionet.meta.DDUser,eionet.meta.LoginServlet"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<%
ServletContext ctx = getServletContext();
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
            <a id="logoutlink" href="<%=request.getContextPath()%>/logout.jsp" title="Logout">Logout (<%=_user.getUserName()%>)</a><%
        }
        else{
            %>
            <a id="loginlink" href="<%=SecurityUtil.getLoginURL(request)%>" title="Login">Login</a><%
        }

        String helpScreen = request.getParameter("helpscreen");
        if (helpScreen!=null){
            %>
            <a id="pagehelplink" class="helpButton" title="Get help on this page" href="<%=request.getContextPath()%>/help.jsp?screen=<%=helpScreen%>&amp;area=pagehelp"><span>Page help</span></a><%
        }
        %>
        <a id="printlink" title="Print this page" href="javascript:this.print();"><span>Print</span></a>
        <a id="fullscreenlink" href="javascript:toggleFullScreenMode()" title="Switch to/from full screen mode"><span>Switch to/from full screen mode</span></a>
        <a id="acronymlink" target="_blank" href="https://www.eionet.europa.eu/acronyms" title="Look up acronyms"><span>Acronyms</span></a>
        <form action="https://google.com/search" method="get">
          <div id="freesrchform">
            <label for="freesrchfld">Search</label>
            <input type="text" id="freesrchfld" name="q"
             onfocus="if(this.value=='Search the site')this.value='';"
             onblur="if(this.value=='')this.value='Search the site';"
             value="Search the site"/>
             <input type="hidden" name="sitesearch" value="<%=Props.getProperty(PropsIF.JSP_URL_PREFIX)%>" />
            <input id="freesrchbtn" type="image" src="<%=request.getContextPath()%>/images/button_go.gif" alt="Go"/>
          </div>
        </form>
    </div>
</div> <!-- toolribbon -->

<div id="pagehead">
    <%@ include file="pagehead.jsp" %>
</div> <!-- pagehead -->


<div id="menuribbon">
    <%@ include file="dropdownmenus.txt" %>
</div>
<div class="breadcrumbtrail">
    <div class="breadcrumbhead">You are here:</div>
    <div class="breadcrumbitem eionetaccronym"><a href="https://www.eionet.europa.eu">Eionet</a></div>
    <%
    String contextName = request.getParameter("context_name");
    String contextPath = request.getParameter("context_path");
    if (contextPath==null)
        contextPath = "";
    String lastItemName = request.getParameter("name");
    if (lastItemName!=null && contextName==null){
        %>
        <div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/">${ddfn:getProperty("app.displayName")}</a></div>
        <div class="breadcrumbitemlast"><%=lastItemName%></div><%
    }
    else if (lastItemName==null && contextName!=null){
        %>
        <div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/">${ddfn:getProperty("app.displayName")}</a></div>
        <div class="breadcrumbitemlast"><%=contextName%></div><%
    }
    else if (lastItemName!=null && contextName!=null){
        %>
        <div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/">${ddfn:getProperty("app.displayName")}</a></div>
        <div class="breadcrumbitem"><a href="<%=contextPath%>"><%=contextName%></a></div>
        <div class="breadcrumbitemlast"><%=lastItemName%></div><%
    }
    else if (lastItemName==null && contextName==null){
        %>
        <div class="breadcrumbitemlast">${ddfn:getProperty("app.displayName")}</div><%
    }
    %>
    <div class="breadcrumbtail">
    </div>
</div>
