<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<h1>Latest released definitions</h1>

<c:url var="datasetIconUrl" value="/images/tar.png" />
    <c:url var="vocabularyIconUrl" value="/images/txt.png" />
    <c:url var="schemaIconUrl" value="/images/xsd.png" />
    <display:table name="${actionBean.results}" class="datatable results" id="recentlyReleased" style="width:100%" >
        <display:column style="width:5%">
            <c:choose>
                <c:when test="${recentlyReleased.type eq 'VOCABULARY'}">
                    <img src="${vocabularyIconUrl}" alt="Vocabulary" />
                </c:when>
                <c:when test="${recentlyReleased.type eq 'DATASET'}">
                    <img src="${datasetIconUrl}" alt="Dataset definition" />
                </c:when>
                <c:when test="${recentlyReleased.type eq 'SCHEMA_SET'}">
                    <img src="${schemaIconUrl}" alt="XML schema" />
                </c:when>
            </c:choose>
        </display:column>
        <display:column style="width:70%">
            <c:choose>
                <c:when test="${recentlyReleased.type eq 'VOCABULARY'}">
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" title="View Vocabulary">
                        <stripes:param name="vocabularyFolder.folderName" value="${recentlyReleased.parameters['folderName']}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${recentlyReleased.parameters['identifier']}" />
                        <c:out value="${recentlyReleased.name}" />
                    </stripes:link>
                </c:when>
                <c:when test="${recentlyReleased.type eq 'DATASET'}">
                    <a href="<%=request.getContextPath()%>/datasets/${recentlyReleased.parameters['datasetId']}" title="View Dataset">
                        <c:out value="${recentlyReleased.name}" />
                    </a>
                </c:when>
                <c:when test="${recentlyReleased.type eq 'SCHEMA_SET'}">
                    <stripes:link beanclass="eionet.web.action.SchemaSetActionBean" title="View Schemaset">
                        <stripes:param name="schemaSet.identifier" value="${recentlyReleased.parameters['schemaSetIdentifier']}"/>
                        <c:out value="${recentlyReleased.name}" />
                    </stripes:link>
                </c:when>
            </c:choose>
        </display:column>
        <display:column style="width:25%">
            <fmt:formatDate value="${recentlyReleased.releasedDate}" type="DATE" pattern="dd MMMMMMMMM yyyy"/>
        </display:column>
    </display:table>
