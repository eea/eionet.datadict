<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Your checkouts" currentSection="checkouts">

    <stripes:layout-component name="contents">
        <h1>Your checkouts</h1>
        
        <h2>Datasets</h2>
        <c:choose>
            <c:when test="${not empty actionBean.dataSets}">
                <table class="datatable results">
                    <c:forEach items="${actionBean.dataSets}" var="dataSet" varStatus="row">
                        <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                            <td>
                                <a href="${pageContext.request.contextPath}/datasets/${dataSet.id}">
                                    ${fn:escapeXml(dataSet.shortName)}
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:when>
            <c:otherwise>
                <p class="not-found">You have no datasets checked out.</p>
            </c:otherwise>
        </c:choose>

        <h2>Common elements</h2>
        <c:choose>
            <c:when test="${not empty actionBean.dataElements}">
                <table class="datatable results">
                    <c:forEach items="${actionBean.dataElements}" var="dataElement" varStatus="row">
                        <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                            <td>
                                <a href="${pageContext.request.contextPath}/dataelements/${dataElement.id}">
                                    ${fn:escapeXml(dataElement.shortName)}
                                </a>
                                (${fn:escapeXml(dataElement.valueTypeTitle)})
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:when>
            <c:otherwise>
                <p class="not-found">You have no common elements checked out.</p>
            </c:otherwise>
        </c:choose>

        <h2>Schema sets</h2>
        <c:choose>
            <c:when test="${not empty actionBean.schemaSets}">
                <table class="datatable results">
                    <c:forEach items="${actionBean.schemaSets}" var="schemaSet" varStatus="row">
                        <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                            <td>
                                <stripes:link beanclass="eionet.web.action.SchemaSetActionBean">
                                    <stripes:param name="schemaSet.identifier" value="${schemaSet.identifier}" />
                                    <stripes:param name="workingCopy" value="true" />
                                    ${fn:escapeXml(schemaSet.identifier)}
                                </stripes:link>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:when>
            <c:otherwise>
                <p class="not-found">You have no schema sets checked out.</p>
            </c:otherwise>
        </c:choose>

        <h2>Schemas</h2>
        <c:choose>
            <c:when test="${not empty actionBean.schemas}">
                <table class="datatable results">
                    <c:forEach items="${actionBean.schemas}" var="schema" varStatus="row">
                        <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                            <td>
                                <stripes:link beanclass="eionet.web.action.SchemaActionBean">
                                    <stripes:param name="schema.fileName" value="${schema.fileName}" />
                                    <stripes:param name="workingCopy" value="true" />
                                    ${fn:escapeXml(schema.fileName)}
                                </stripes:link>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:when>
            <c:otherwise>
                <p class="not-found">You have no schemas checked out.</p>
            </c:otherwise>
        </c:choose>

        <h2>Vocabularies</h2>
        <c:choose>
            <c:when test="${not empty actionBean.vocabularies}">
                <table class="datatable results">
                    <c:forEach items="${actionBean.vocabularies}" var="vocabulary" varStatus="row">
                        <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                            <td>
                                <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean">
                                    <stripes:param name="vocabularyFolder.folderName" value="${vocabulary.folderName}" />
                                    <stripes:param name="vocabularyFolder.identifier" value="${vocabulary.identifier}" />
                                    <stripes:param name="vocabularyFolder.workingCopy" value="true" />
                                    ${fn:escapeXml(vocabulary.label)}
                                </stripes:link>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:when>
            <c:otherwise>
                <p class="not-found">You have no vocabularies checked out.</p>
            </c:otherwise>
        </c:choose>
                
    </stripes:layout-component>

</stripes:layout-render>
