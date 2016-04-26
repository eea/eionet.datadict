<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<div class="warning-msg">
    <strong>DD encountered the following error:</strong>
    <p>
        ${errorMessage}
    </p>
</div>
<c:choose>
    <c:when test="${not empty errorTrace}">
        <form id="errtrc" action="http://">
            <div style="display:none">
                <input type="hidden" name="errtrc" value="${errorTrace}"/>
            </div>
        </form>
    </c:when>
</c:choose>