<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Task results">
    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test="${actionBean.taskSuccess}">
                <div class="system-msg">
                    <strong>${actionBean.taskDisplayName}</strong>
                    <p>${actionBean.feedbackText} Click <a href="<stripes:url value="${actionBean.feedbackUrl}" />">here</a> for results.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="error-msg">
                    <strong>${actionBean.taskDisplayName}</strong>
                    <p>${actionBean.feedbackText}</p>
                </div>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>