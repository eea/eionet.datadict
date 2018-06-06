<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Administration" currentSection="administration">

    <stripes:layout-component name="contents">
        <h1>Administration functions</h1>
        <ul>
            <li><stripes:link href="/attributes">Attributes</stripes:link></li>
            <c:if test="${ddfn:userHasPermission(actionBean.user.userName, '/import', 'x')}">
                <li><a href="${pageContext.request.contextPath}/import.jsp">Import datasets</a></li>
            </c:if>
            <c:if test="${ddfn:userHasPermission(actionBean.user.userName, '/cleanup', 'x')}">
                <li><stripes:link href="/cleanup">Cleanup</stripes:link></li>
            </c:if>
             <c:if test="${ddfn:userHasPermission(actionBean.user.userName, '/vocabularies', 'i')}">
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="viewScheduledJobs"> 
                       View scheduled jobs
                    </stripes:link>
                </li>
             </c:if>
        </ul>
    </stripes:layout-component>

</stripes:layout-render>
