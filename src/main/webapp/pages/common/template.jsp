<%@ include file="/pages/common/taglibs.jsp"%>
<%--
<%@page import="eionet.cr.web.util.BaseUrl"%>
--%>
<stripes:layout-definition>
    <%@ page contentType="text/html;charset=UTF-8" language="java"%>

    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <meta name="viewport" content="initial-scale=1.0" />
            <meta name="Publisher" content="EEA, The European Environment Agency" />
            <%--
            <base href="<%= BaseUrl.getBaseUrl(request) %>"/>
            --%>

            <title>${initParam.appDispName} - ${pageTitle}</title>

            <link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/print.css" media="print" />
            <link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/handheld.css" media="handheld" />
            <link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/screen.css" media="screen" title="Eionet 2007 style" />
            <link rel="stylesheet" type="text/css" href="<c:url value="/css/eionet2007.css"/>" media="screen" title="Eionet 2007 style"/>
            <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />

            <link type="text/css" href="<c:url value="/css/smoothness/jquery-ui-1.8.16.custom.css" />" rel="stylesheet" />
            <script type="text/javascript" src="<c:url value="/scripts/jquery-1.6.2.min.js" />"></script>
            <script type="text/javascript" src="<c:url value="/scripts/jquery-ui-1.8.16.custom.min.js" />"></script>
            <script type="text/javascript" src="<c:url value="/scripts/jquery-timers.js"/>"></script>
            <script type="text/javascript" src="<c:url value="/scripts/jquery.autocomplete.js"/>"></script>
            <script type="text/javascript" src="<c:url value="/scripts/prototype.js"/>"></script>
            <script type="text/javascript" src="<c:url value="/scripts/util.js"/>"></script>
            <script type="text/javascript" src="<c:url value="/script.js"/>"></script>
            <stripes:layout-component name="head"/>
        </head>
        <body>
            <div id="container">
                <div id="toolribbon">
                    <div id="lefttools">
                        <a id="eealink" href="http://www.eea.europa.eu/">EEA</a>
                        <a id="ewlink" href="http://www.ewindows.eu.org/">EnviroWindows</a>
                    </div>
                    <div id="righttools">

                        <c:choose>
                            <c:when test="${empty actionBean.user}">
                                <stripes:link id="loginlink" href="${actionBean.loginUrl}" title="Login">Login</stripes:link>
                            </c:when>
                            <c:otherwise>
                                <stripes:link id="logoutlink" href="${actionBean.logoutUrl}" title="Logout">Logout ${actionBean.user.userName}</stripes:link>
                            </c:otherwise>
                        </c:choose>

                        <a id="printlink" title="Print this page" href="javascript:this.print();"><span>Print</span></a>
                        <a id="fullscreenlink" href="javascript:toggleFullScreenMode()" title="Switch to/from full screen mode"><span>Switch to/from full screen mode</span></a>
                        <a id="acronymlink" href="about.action" title="About ${initParam.appDispName}"><span>About</span></a>
                        <form action="http://google.com/search" method="get">
                          <div id="freesrchform">
                            <label for="freesrchfld">Search</label>
                            <input type="text" id="freesrchfld" name="q"
                             onfocus="if(this.value=='Search the site')this.value='';"
                             onblur="if(this.value=='')this.value='Search the site';"
                             value="Search the site"/>
                             <input type="hidden" name="sitesearch" value="${actionBean.sitePrefix}" />
                            <input id="freesrchbtn" type="image" src="<c:url value="/images/button_go.gif"/>" alt="Go"/>
                          </div>
                        </form>
                    </div>
                </div> <!-- toolribbon -->

                <div id="pagehead">
                    <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
                    <div id="networktitle">Eionet</div>
                    <div id="sitetitle">${initParam.appDispName}</div>
                    <div id="sitetagline">This service is part of Reportnet</div>
                </div> <!-- pagehead -->

                <div id="menuribbon">
                    <%@ include file="/pages/common/dropdownmenus.txt" %>
                </div>

                <div class="breadcrumbtrail">
                    <div class="breadcrumbhead">You are here:</div>
                    <div class="breadcrumbitem eionetaccronym">
                        <a href="http://www.eionet.europa.eu">Eionet</a>
                    </div>
                    <c:choose>
                        <c:when test="${empty pageTitle}">
                            <div class="breadcrumbitemlast">${initParam.appDispName}</div>
                        </c:when>
                        <c:otherwise>
                            <div class="breadcrumbitem"><a href="${pageContext.request.contextPath}/">${initParam.appDispName}</a></div>
                            <div class="breadcrumbitemlast"><c:out value="${pageTitle}"/></div>
                        </c:otherwise>
                    </c:choose>
                    <div class="breadcrumbtail"></div>
                </div>

                <stripes:layout-component name="navigation">
                    <jsp:include page="/pages/common/navigation.jsp"/>
                </stripes:layout-component>

                <div id="workarea">

                    <!--  validation errors -->
                    <stripes:errors/>

                    <!--  messages -->
                    <stripes:layout-component name="messages">
                        <c:if test="${not empty systemMessages}">
                            <div class="system-msg">
                                <stripes:messages key="systemMessages"/>
                            </div>
                        </c:if>
                        <c:if test="${not empty cautionMessages}">
                            <div class="caution-msg">
                                <strong>Caution ...</strong>
                                <stripes:messages key="cautionMessages"/>
                            </div>
                        </c:if>
                        <c:if test="${not empty warningMessages}">
                            <div class="warning-msg">
                                <strong>Warning ...</strong>
                                <stripes:messages key="warningMessages"/>
                            </div>
                        </c:if>
                    </stripes:layout-component>

                    <stripes:layout-component name="contents"/>
                    <!--  Home headers, content or default content -->
                    <%--
                    <c:choose>
                        <c:when test="${actionBean.homeContext}">
                            <c:choose>
                                <c:when test="${actionBean.userAuthorized || actionBean.showPublic}" >
                                    <div id="tabbedmenu">
                                        <ul>
                                            <c:forEach items="${actionBean.tabs}" var="tab">
                                                <c:if test="${actionBean.userAuthorized || tab.showPublic == actionBean.showpublicYes }" >
                                                    <c:choose>
                                                          <c:when test="${actionBean.section == tab.tabType}" >
                                                            <li id="currenttab"><span><c:out value="${tab.title}"/></span></li>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <li>
                                                                <stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/${tab.tabType}">
                                                                    <c:out value="${tab.title}"/>
                                                                </stripes:link>
                                                            </li>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:if>
                                            </c:forEach>

                                        </ul>
                                    </div>
                                    <br style="clear:left" />
                                    <div style="margin-top:10px">
                                        <stripes:layout-component name="contents"/>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                        <div class="error-msg">
                                        ${actionBean.authenticationMessage}
                                        </div>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <stripes:layout-component name="contents"/>
                        </c:otherwise>
                    </c:choose>
                    --%>
                </div>
                <div id="pagefoot" style="max-width: none;">
                    <p><a href="mailto:cr@eionet.europa.eu">E-mail</a> | <a href="mailto:helpdesk@eionet.europa.eu?subject=Feedback from the ${initParam.appDispName} website">Feedback</a></p>
                    <p><a href="http://www.eea.europa.eu/"><b>European Environment Agency</b></a>
                    <br/>Kgs. Nytorv 6, DK-1050 Copenhagen K, Denmark - Phone: +45 3336 7100</p>
                </div>
            </div>
        </body>
    </html>
</stripes:layout-definition>
