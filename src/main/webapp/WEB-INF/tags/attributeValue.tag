<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="attrValue" required="true" %>
<%@ attribute name="attrLen" required="false" %>
    <c:if test="${not empty attrValue}">
        <c:set var="valueLen" value="${fn:length(attrValue)}" />
    </c:if>
    <c:if test="${empty attrLen}">
        <c:set var="attrLen" value="50" />
    </c:if>
    <c:out value="${ddfn:cutAtSpace(attrValue, attrLen)}" escapeXml="true"/>


