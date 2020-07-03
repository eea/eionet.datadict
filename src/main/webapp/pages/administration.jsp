<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Administration" currentSection="administration">

    <stripes:layout-component name="contents">
        <h1>Administration functions</h1>
        <ul>
            <li><stripes:link href="/attributes">Attributes</stripes:link></li>
            <c:if test="${ddfn:userHasPermission(actionBean.user, '/import', 'x')}">
                <li><a href="${pageContext.request.contextPath}/import.jsp">Import datasets</a></li>
            </c:if>
            <c:if test="${ddfn:userHasPermission(actionBean.user, '/cleanup', 'x')}">
                <li><stripes:link href="/cleanup">Cleanup</stripes:link></li>
            </c:if>
             <c:if test="${ddfn:userHasPermission(actionBean.user, '/vocabularies', 'i')}">
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="viewScheduledJobs"> 
                       View scheduled jobs
                    </stripes:link>
                </li>
             </c:if>
            <c:if test="${ddfn:userHasPermission(actionBean.user, '/generateJWTToken', 'x')}">
                <li><stripes:link href="/generateJWTToken">Generate JWT Token</stripes:link></li>
            </c:if>
            <c:if test="${ddfn:userHasPermission(actionBean.user, '/admintools', 'v')}">
                <li><stripes:link href="/v2/admintools/list">Admin tools</stripes:link></li>
            </c:if>
        </ul>
    </stripes:layout-component>

</stripes:layout-render>
