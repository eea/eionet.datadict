<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabulary" currentSection="vocabularies">

    <stripes:layout-component name="contents">
        <h1>Vocabulary: <em><c:out value="${actionBean.vocabularyFolder.label}" /></em></h1>

        <c:if test="${actionBean.vocabularyFolder.workingCopy && actionBean.vocabularyFolder.siteCodeType}">
            <div class="system-msg">
                <strong>Note</strong>
                <p>
                For checked out site codes, vocabulary concepts are not visible. To view them, see the
                <stripes:link href="/services/siteCodes">site codes page</stripes:link>.
                </p>
            </div>
        </c:if>

        <c:if test="${actionBean.checkedOutByUser}">
            <div class="system-msg">
                <strong>Note</strong>
                <p>You have a
                    <stripes:link beanclass="${actionBean['class'].name}" event="viewWorkingCopy">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}"/>
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}"/>
                        working copy
                    </stripes:link> of this vocabulary!</p>
            </div>
        </c:if>
        
        <div id="drop-operations">
            <ul>
                <li class="back">
                    <stripes:link beanclass="eionet.web.action.VocabularyFoldersActionBean">
                        <stripes:param name="folderId" value="${actionBean.vocabularyFolder.folderId}" />
                        <stripes:param name="expand" value="true" />
                        <stripes:param name="expanded" value="" />
                            Back to set
                    </stripes:link>
                </li>
                <c:if test="${not empty actionBean.user}">
                    <c:if test="${not actionBean.vocabularyFolder.siteCodeType}">
                        <li class="create">
                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="add">
                                <stripes:param name="copyId" value="${actionBean.vocabularyFolder.id}" />
                                Create new copy
                            </stripes:link>
                        </li>
                    </c:if>
                    <c:if test="${actionBean.userWorkingCopy}">
                        <li class="add">
                            <a href="#" id="addNewConceptLink">Add new concept</a>
                        </li>
                        <li class="edit">
                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="edit">
                                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                                Edit vocabulary
                            </stripes:link>
                        </li>
                        <li class="checkin">
                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkIn">
                                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                                Check in
                            </stripes:link>
                        </li>
                        <li class="undo">
                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="undoCheckOut">
                                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                Undo checkout
                            </stripes:link>
                        </li>
                    </c:if>
                    <c:if test="${not actionBean.vocabularyFolder.workingCopy}">
                        <li class="maintain">
                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="scheduleSynchronization"> 
                                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                Schedule  synchronization
                            </stripes:link>
                        </li>
                        <li class="checkout">
                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkOut">
                                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                                Check out
                            </stripes:link>
                        </li>
                    </c:if>
                </c:if>
            </ul>
        </div>

        <c:if test="${not actionBean.vocabularyFolder.draftStatus && not actionBean.vocabularyFolder.workingCopy}">
            <script type="text/javascript">
                jQuery(function() {
                    applyExportOptionsToggle();
                });
            </script>
            <div id="createbox">
                <ul>
                    <li>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="rdf" title="Export RDF" class="rdf">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            Get RDF output of this vocabulary
                        </stripes:link>
                    </li>
                    <li>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="csv" title="Export CSV" class="csv">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            Get CSV output of this vocabulary
                        </stripes:link>
                    </li>
                    <li>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="codelist" title="Export XML in INSPIRE codelist format" class="xml">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            Get XML output in INSPIRE codelist format
                        </stripes:link>
                    </li>
                    <li>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="json" title="Export JSON" class="json">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            Get JSON-LD output of this vocabulary
                        </stripes:link>
                    </li>
                </table>
            </div>
        </c:if>

        <!-- Vocabulary folder -->
        <div id="outerframe">
            <table class="datatable results">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Folder
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.folderName} (${actionBean.vocabularyFolder.folderLabel})" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.identifier}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Label
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.label}" />
                    </td>
                </tr>
                <c:if test="${not empty actionBean.vocabularyFolder.baseUri}">
                  <tr>
                      <th scope="row" class="scope-row simple_attr_title">
                          Base URI
                      </th>
                      <td class="simple_attr_value">
                          <c:out value="${actionBean.vocabularyFolder.baseUri}" />
                      </td>
                  </tr>
                </c:if>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Registration status
                    </th>
                    <td class="simple_attr_value">
                        <fmt:setLocale value="en_GB" />
                        <fmt:formatDate pattern="dd MMM yyyy HH:mm:ss" value="${actionBean.vocabularyFolder.dateModified}" var="dateFormatted"/>
                        <c:out value="${actionBean.vocabularyFolder.regStatus}"/>
                        <c:if test="${not empty actionBean.userName && actionBean.userWorkingCopy}">
                            <span class="caution" title="Checked out on ${dateFormatted}">(Working copy)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && actionBean.checkedOutByOther}">
                            <span class="caution">(checked out by <em>${actionBean.vocabularyFolder.workingUser}</em>)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && empty actionBean.vocabularyFolder.workingUser || actionBean.checkedOutByUser}">
                            <span style="color:#A8A8A8;font-size:0.8em">(checked in by ${actionBean.vocabularyFolder.userModified} on ${dateFormatted})</span>
                        </c:if>
                        <c:if test="${empty actionBean.userName}">
                            <span>${dateFormatted}</span>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Type
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.type.label}" />
                    </td>
                </tr>
                <!-- Simple attributes -->
                <c:forEach var="attributeValues" items="${actionBean.vocabularyFolder.attributes}">
                    <c:set var="attrMeta" value="${attributeValues[0]}"/>
                    <c:if test="${not empty attrMeta.value}">
                        <tr>
                            <th scope="row" class="scope-row simple_attr_title">${attrMeta.label}</th>
                            <td class="simple_attr_value">
                                <c:choose>
                                    <c:when test="${attrMeta.inputType=='vocabulary'}">
                                        <c:set var="vocabularyConcepts" value="${actionBean.getVocabularyConcepts(attrMeta.attributeId)}" />
                                        <c:set var="vocabularyBindingFolder" value="${actionBean.getVocabularyBindingFolder(attrMeta.attributeId)}" />
                                        <c:if test="${not empty vocabularyConcepts}">
                                            <ul class="stripedmenu">
                                                <c:forEach var="vocabularyConcept" items="${vocabularyConcepts}">
                                                    <li>
                                                        <stripes:link href="/vocabularyconcept/${vocabularyBindingFolder.folderName}/${vocabularyBindingFolder.identifier}/${vocabularyConcept.identifier}/view" title="${vocabularyConcept.label}">
                                                            <c:out value="${vocabularyConcept.label}"/>
                                                        </stripes:link>
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <ul class="stripedmenu">
                                            <c:forEach var="attr" items="${attributeValues}" varStatus="innerLoop">
                                                <li>
                                                    <span style="white-space:pre-wrap"><c:out value="${attr.value}" /></span>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table>
        </div>

        <!-- Bound data elements -->
        <jsp:include page="boundElementsInc.jsp" />

        <c:if test="${actionBean.userWorkingCopy}">
            <jsp:include page="newConceptInc.jsp" />
        </c:if>

        <!-- Vocabulary concepts search -->
        <h2>Vocabulary concepts</h2>
        <stripes:form method="get" id="searchForm" beanclass="${actionBean['class'].name}">
            <div id="filters">
                <table class="filter">
                    <stripes:hidden name="vocabularyFolder.folderName" />
                    <stripes:hidden name="vocabularyFolder.identifier" />
                    <stripes:hidden name="vocabularyFolder.workingCopy" />
                    <tr>
                        <td class="label">
                            <label for="status">Status</label>
                        </td>
                        <td class="input">
                            <stripes:select name="filter.conceptStatusInt" id="status">
                                <stripes:option value="255" label="All"/>
                                <stripes:options-collection collection="<%=eionet.meta.dao.domain.StandardGenericStatus.valuesAsList()%>" label="label" value="value"/>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label for="filterText">Filtering text</label>
                        </td>
                        <td class="input">
                            <stripes:text class="smalltext" size="30" name="filter.text" id="filterText"/>
                        </td>
                    </tr>
                    <c:forEach items="${actionBean.boundElementFilters}" var="boundElementFilter" varStatus="loop">
                        <tr class="boundElementFilter" data-filter-id="${boundElementFilter.id}">
                            <td class="label">
                                <label for="boundElementFilter-${boundElementFilter.id}"><c:out value="${boundElementFilter.label}" /></label>
                            </td>
                            <td class="input">
                                <stripes:hidden name="filter.boundElements[${loop.index}].id" value="${boundElementFilter.id}" class="boundElementFilterId" />
                                <stripes:select name="filter.boundElements[${loop.index}].value" class="boundElementFilterSelect">
                                    <stripes:option value="" label="All" />
                                    <stripes:options-map map="${boundElementFilter.options}" />
                                </stripes:select>
                                <a href="#" class="deleteButton" title="Remove from search criteria"></a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${fn:length(actionBean.boundElements)>0}">
                        <link type="text/css" media="all" href="<c:url value="/css/spinner.css"/>"  rel="stylesheet" />
                        <tr id="addFilterRow">
                            <td></td>
                            <td class="input">
                                <select id="addFilter">
                                    <option value="">Add criteria</option>
                                    <c:forEach var="boundElement" items="${actionBean.boundElements}">
                                        <option value="${boundElement.id}"<c:if test="${ddfn:contains(actionBean.boundElementFilterIds, boundElement.id)}">disabled="disabled"</c:if>><c:out value="${boundElement.identifier}" /></option>
                                    </c:forEach>
                                </select>
                            </td>
                        </tr>
                    </c:if>
                    <tr>
                        <td class="label">
                            <label>Columns</label>
                        </td>
                        <td class="input bordered multi-select-container">
                            <c:forEach var="column" items="${actionBean.columns}">
                                <label for="${fn:escapeXml(column)}" class="smallfont">
                                    <stripes:checkbox name="filter.visibleColumns" id="${fn:escapeXml(column)}" value="${fn:escapeXml(column)}" />
                                    ${fn:escapeXml(column)}
                                </label>
                            </c:forEach>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label for="visibleDefinition">Show definition</label>
                        </td>
                        <td class="input bordered">
                            <stripes:checkbox name="filter.visibleDefinition" id="visibleDefinition" />
                            <label for="visibleDefinition" class="smallfont">Yes</label>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label for="pageSize">Page size</label>
                        </td>
                        <td class="input">
                            <stripes:select id="pageSize" name="filter.pageSize">
                                <stripes:options-collection collection="${actionBean.filter.possibleResultsPerPage}" />
                            </stripes:select>
                        </td>
                    </tr>
                </table>
                <p class="actions">
                    <stripes:submit name="view" value="Search" class="mediumbuttonb searchButton" />
                    <input class="mediumbuttonb" type="reset" value="Reset" />
                </p>
            </div>
            <script type="text/javascript" src="<c:url value="/scripts/jquery.balloon.min.js" />"></script>
            <script type="text/javascript">
            // <![CDATA[
                (function($) {
                    $(document).ready(function() {
                        <stripes:url var="url" beanclass="${actionBean['class'].name}"></stripes:url>
                        applyBoundElementsFiltererInteractions("${url}", ${actionBean.vocabularyFolder.id});
                        applyConceptDefinitionBalloon();
                   }); 
                }) (jQuery);
            // ]]>
            </script>
        </stripes:form>

        <%-- Vocabulary concepts --%>
        <div id="vocabularyConceptResults">
        <display:table name="actionBean.vocabularyConcepts" class="datatable results" id="concept"
            style="width:100%" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#vocabularyConceptResults"
            excludedParams="view vocabularyFolder.identifier vocabularyFolder.folderName" >
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No vocabulary concepts found.</p>" />
            <display:setProperty name="paging.banner.item_name" value="concept" />
            <display:setProperty name="paging.banner.items_name" value="concepts" />

            <display:column title="Id" class="${actionBean.vocabularyFolder.numericConceptIdentifiers ? 'number' : ''}" style="width: 10%" media="html">
                <c:choose>
                    <c:when test="${!concept.status.accepted}">
                        <span style="text-decoration:line-through"><c:out value="${concept.identifier}" /></span>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${concept.identifier}" />
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Label" media="html" style="width: 30%">
                <c:choose>
                    <c:when test="${not actionBean.vocabularyFolder.workingCopy}">
                        <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${concept.identifier}/view" title="${concept.label}">
                            <stripes:param name="facet" value="HTML Representation"/> <!-- Discourage people from copy-paste of the link -->
                            <dd:attributeValue attrValue="${concept.label}" attrLen="40"/>
                        </stripes:link>
                    </c:when>
                    <c:otherwise>
                        <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${concept.identifier}/view" title="${concept.label}">
                            <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                            <dd:attributeValue attrValue="${concept.label}" attrLen="40"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
                <c:if test="${actionBean.filter.visibleDefinition}">
                    <c:choose>
                        <c:when test="${not empty concept.definition}">
                            <p><span class="conceptDefinition" title="${fn:escapeXml(concept.definition)}">Definition</span></p>
                        </c:when>
                        <c:otherwise>
                            <p><span class="noDefinition">No definition available</span></p>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </display:column>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Status')}">
                <display:column title="Status" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${concept.status.label}"/>
                </display:column>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Status Modified')}">
                <display:column title="Status Modified" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${concept.statusModified}" pattern="dd.MM.yyyy"/>
                </display:column>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Notation')}">
                <display:column title="Notation" escapeXml="true" property="notation" style="width: 10%"/>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Accepted Date')}">
                <display:column title="Accepted Date" escapeXml="false">
                    <fmt:formatDate value="${concept.acceptedDate}" pattern="dd.MM.yyyy" />
                </display:column>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Not Accepted Date')}">
                <display:column title="Not Accepted Date" escapeXml="false">
                    <fmt:formatDate value="${concept.notAcceptedDate}" pattern="dd.MM.yyyy" />
                </display:column>
            </c:if>
            <c:forEach var="boundElementIdentifier" items="${actionBean.filter.boundElementVisibleColumns}">
                <display:column title="${fn:escapeXml(boundElementIdentifier)}" escapeXml="false">
                    <ul class="stripedmenu">
                        <c:forEach var="elementValues" items="${concept.elementAttributes}">
                            <c:set var="elementMeta" value="${elementValues[0]}"/>
                            <c:if test="${elementMeta.identifier == boundElementIdentifier}">
                                <c:forEach var="attr" items="${elementValues}" varStatus="innerLoop">
                                    <li>
                                        <c:choose>
                                            <c:when test="${attr.relationalElement}">
                                                <c:choose>
                                                    <c:when test="${not actionBean.vocabularyFolder.workingCopy or attr.datatype eq 'reference'}">
                                                        <stripes:link href="/vocabularyconcept/${attr.relatedConceptRelativePath}/view">
                                                            <c:out value="${attr.relatedConceptIdentifier}" />
                                                            <c:if test="${not empty attr.relatedConceptLabel}">
                                                                (<c:out value="${attr.relatedConceptLabel}" />)
                                                            </c:if>
                                                            <c:if test="${not empty attr.relatedConceptVocSet}">
                                                                in <c:out value="${attr.relatedConceptVocSet}/${attr.relatedConceptVocabulary}" />
                                                            </c:if>
                                                          </stripes:link>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <stripes:link beanclass="eionet.web.action.VocabularyConceptActionBean">
                                                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                                            <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                                                            <stripes:param name="vocabularyConcept.identifier" value="${attr.relatedConceptIdentifier}" />
                                                            <dd:attributeValue attrValue="${attr.relatedConceptLabel}" attrLen="40" />
                                                        </stripes:link>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                  <dd:linkify value="${attr.attributeValue}" /><c:if test="${not empty attr.attributeLanguage}"> [${attr.attributeLanguage}]</c:if>
                                            </c:otherwise>
                                        </c:choose>
                                    </li>
                                </c:forEach>
                            </c:if>
                        </c:forEach>
                    </ul>
                </display:column>
            </c:forEach>
        </display:table>
        </div>
    <%-- The section that displays versions of this vocabulary. --%>

    <c:if test="${not empty actionBean.vocabularyFolderVersions}">
        <h2 id="otherVersions">Other versions of this vocabulary</h2>
        <display:table name="${actionBean.vocabularyFolderVersions}" class="datatable results" id="item" style="width:100%">
            <display:column title="Label">
                <c:choose>
                    <c:when test="${item.draftStatus && empty actionBean.user}">
                        <span class="link-folder" style="color:gray;">
                            <c:out value="${item.label}"/>
                        </span>
                    </c:when>
                    <c:otherwise>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" class="link-folder">
                            <stripes:param name="vocabularyFolder.folderName" value="${item.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${item.identifier}" />
                            <c:if test="${item.workingCopy}">
                                <stripes:param name="vocabularyFolder.workingCopy" value="${item.workingCopy}" />
                            </c:if>
                            <c:out value="${item.label}"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
                <c:if test="${item.workingCopy && actionBean.userName==item.workingUser}">
                    <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                </c:if>
            </display:column>
            <display:column title="Status"><c:out value="${item.regStatus}"/></display:column>
            <display:column title="Last modified">
                <fmt:formatDate value="${item.dateModified}" pattern="dd.MM.yy HH:mm:ss"/>
            </display:column>
        </display:table>
    </c:if>

    </stripes:layout-component>

</stripes:layout-render>
