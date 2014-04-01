<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="id" required="false" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="false" %>
<c:set var="languages" value="<%=eionet.util.PropsLanguages.getValues()%>"/>

<c:choose>
 <c:when test="${not empty id}">
  <select name="${name}" id="${id}">
 </c:when>
 <c:otherwise>
  <select name="${name}">
 </c:otherwise>
</c:choose>

<c:forEach var="lang" items="${languages}">
<c:choose>
 <c:when test="${lang.code eq value}">
  <option value="${lang.code}" selected="selected">${lang.code}</option>
 </c:when>
 <c:otherwise>
  <option value="${lang.code}">${lang.code}</option>
 </c:otherwise>
</c:choose>
</c:forEach>

</select>
