<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" bodyClass="threecolumns">

    <c:if test="${not empty actionBean.newsSection}">
        <stripes:layout-component name="news">
            <div id="rightcolumn" class="quickjumps">
                ${actionBean.newsSection}
            </div>
        </stripes:layout-component>
    </c:if>

    <stripes:layout-component name="contents">
        <div id="outerframe">
            <jsp:include page="/releasedItems.action" flush="true" />
            <h2>Documentation</h2>
            <c:choose>
                <c:when test="${fn:length(actionBean.documents) > 0}">
                    <ul>
                        <c:forEach var="doc" items="${actionBean.documents}">
                            <li>
                                <c:set var="docTitle" value="${doc.title}"/>
                                <c:if test="${empty docTitle}">
                                    <c:set var="docTitle" value="${doc.pageId}"/>
                                </c:if>
                                <stripes:link href="/documentation/${fn:escapeXml(doc.pageId)}">${fn:escapeXml(docTitle)}</stripes:link>
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p>No documentation currently available in the database.</p>
                </c:otherwise>
            </c:choose>
            ${actionBean.supportSection}
        </div>
    </stripes:layout-component>

</stripes:layout-render>
