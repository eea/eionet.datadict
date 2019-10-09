<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="id" required="true" %>
<%@ attribute name="name" required="false" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="fixedValues" required="true" type="java.util.ArrayList" %>
<%--
   Dropdown for fixed values
   - id - ID value for select control
   - name control name
   - value Bean property to be valued
   - fixedValues : list of fixed values to fill the dropdown
 --%>
<c:choose>
 <c:when test="${not empty name}">
  <select class="small" name="${name}" id="${id}">
 </c:when>
 <c:otherwise>
  <select id="${id}">
 </c:otherwise>
</c:choose>

<!--  emtpy value -->
<option value=""> </option>

<c:forEach var="fxv" items="${fixedValues}">

<c:choose>
 <c:when test="${fxv.value == value}">
  <option value="${fxv.value}" selected="selected">${fxv.label}</option>
 </c:when>
 <c:otherwise>
  <option value="${fxv.value}">${fxv.label}</option>
 </c:otherwise>
</c:choose>
</c:forEach>

</select>
