<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="value" required="true" %>

<c:set var="status" value="${ddfn:getDatasetRegStatusFromString(value)}" />
<c:if test="${not empty status}">
    <div class="status ${fn:escapeXml(fn:toLowerCase(value))}">
        <span>${fn:escapeXml(value)}</span>
        <c:if test="${status.phaseOrder gt 0}">
            <ul>
                <li<c:if test="${status.phaseOrder ge 1}"> class="done"</c:if>>Step 1</li>
                <li<c:if test="${status.phaseOrder ge 2}"> class="done"</c:if>>Step 2</li>
                <li<c:if test="${status.phaseOrder ge 3}"> class="done"</c:if>>Step 3</li>
                <li<c:if test="${status.phaseOrder ge 4}"> class="done"</c:if>>Step 4</li>
                <li<c:if test="${status.phaseOrder eq 5}"> class="done"</c:if>>Step 5</li>
                </ul>
        </c:if>
    </div>
</c:if>