<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp">
    <stripes:layout-component name="title">
        <title>${initParam.appDispName}</title>
    </stripes:layout-component>
    <stripes:layout-component name="bodylabel">
        <body  class="threecolumns">
        </stripes:layout-component>
        <stripes:layout-component name="news">
            <div id="rightcolumn" class="quickjumps">
                ${actionBean.pageNews}
            </div>
        </stripes:layout-component>
        <stripes:layout-component name="contents">
            <div id="outerframe">
                <jsp:include page="/releasedItems.action" flush="true" />
                <h2>Documentation</h2>
                <ul>
                    <c:set var="count" value="0" scope="page"/>
                    <c:forEach var="doc" items="${actionBean.pageObject.docs}">
                        <li>
                            <c:set var="doctitle" value="${doc.title}"/>
                            <c:if test="${empty doctitle}">
                                <c:set var="doctitle" value="${doc.pageId}"/>
                            </c:if>
                            <stripes:link href="/documentation/${doc.pageId}">${doctitle}</stripes:link>
                            <c:set var="count" value="${count + 1}" scope="page"/>
                        </li>
                    </c:forEach>
                </ul>
                <c:if test="${count < 1}">
                    <p>No documentation currently available in the database.</p>
                </c:if>
                <div>
                    ${actionBean.pageSupport}
                </div>
            </div>
        </stripes:layout-component>
    </stripes:layout-render>
