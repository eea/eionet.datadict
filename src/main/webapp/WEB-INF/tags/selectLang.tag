<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="id" required="true" %>
<%@ attribute name="name" required="false" %>
<%@ attribute name="value" required="false" %>
<c:set var="languages" value="<%=eionet.util.EuropeanLanguages.values()%>"/>

<c:choose>
 <c:when test="${not empty name}">
  <select name="${name}" id="${id}">
 </c:when>
 <c:otherwise>
  <select id="${id}">
 </c:otherwise>
</c:choose>

<c:forEach var="lang" items="${languages}">
<c:choose>
 <c:when test="${lang.code eq value}">
  <option value="${lang.code}" selected="true">${lang.code}</option>
 </c:when>
 <c:otherwise>
  <option value="${lang.code}">${lang.code}</option>
 </c:otherwise>
</c:choose>
</c:forEach>

</select>
