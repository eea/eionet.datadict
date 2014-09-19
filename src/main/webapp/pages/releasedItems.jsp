<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<h1>Latest released definitions</h1>

<c:url var="datasetIconUrl" value="/images/pdf.png" />
    <c:url var="vocabularyIconUrl" value="/images/tar.png" />
    <c:url var="schemaIconUrl" value="/images/txt.png" />
    <display:table name="${actionBean.results}" class="datatable" id="recentlyReleased" style="width:100%" >
        <display:column style="width:5%">
            <c:choose>
                <c:when test="${recentlyReleased.type eq 'VOCABULARY'}">
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="rdf" title="Export RDF">
                        <stripes:param name="vocabularyFolder.folderName" value="${recentlyReleased.parameters['folderName']}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${recentlyReleased.parameters['identifier']}" />
                        <img src="${vocabularyIconUrl}" alt="" />
                    </stripes:link>
                </c:when>
                <c:when test="${recentlyReleased.type eq 'DATASET'}">
                    <a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;obj_id=${recentlyReleased.parameters['datasetId']}&amp;out_type=GDLN"
                       title="Definition as PDF file">
                        <img src="${datasetIconUrl}" alt="" />
                    </a>
                </c:when>
                <c:when test="${recentlyReleased.type eq 'SCHEMA'}">
                    <stripes:link beanclass="eionet.web.action.SchemaActionBean" title="View Schema">
                        <stripes:param name="schemaSet.identifier" value="${recentlyReleased.parameters['schemaSetIdentifier']}"/>
                        <stripes:param name="schema.fileName" value="${recentlyReleased.parameters['fileName']}"/>
                        <img src="${schemaIconUrl}" alt="" />
                    </stripes:link>
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
                <c:when test="${recentlyReleased.type eq 'SCHEMA'}">
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
