<%@page import="eionet.util.Props"%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Error page">

    <stripes:layout-component name="contents">

        <h1><c:out value="${actionBean.errorTypeMsg}" /></h1>
        <div class="error-msg">
            <strong>Error</strong><br />
            <c:choose>
                <c:when test="${not empty actionBean.message}">
                    <c:out value="${actionBean.message}" />
                </c:when>
                <c:otherwise>
                    Error message not specified...
                </c:otherwise>
            </c:choose>
        </div>
        <div style="padding-top:1em">
            Please contact <a href="mailto:helpdesk@eionet.europa.eu?subject=System error in ${ddfn:getProperty("app.displayName")}">Eionet helpdesk</a>
            or ${ddfn:getProperty("app.displayName")} administrators for more information about this error.
        </div>

    </stripes:layout-component>

</stripes:layout-render>