<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<h1>Documentation</h1>
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
