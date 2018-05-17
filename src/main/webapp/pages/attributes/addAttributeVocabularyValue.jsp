<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute Vocabulary Values editor" currentSection="${actionBean.currentSection}">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test="${actionBean.attrOwnerType eq 'dataset'}">
                <c:set var="ddEntity" value="${actionBean.dataSet}"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/datasets/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'dataelement'}">
                <c:set var="ddEntity" value="${actionBean.dataElement}"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/dataelements/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'table'}">
                <c:set var="ddEntity" value="${actionBean.datasetTable}"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/tables/${ddEntity.id}"/>
            </c:when>
        </c:choose>
        <h1>
            Add value for <c:out value="${actionBean.attribute.shortName}"/> attribute corresponding to the 
            <stripes:link href="${ddEntityUrl}">
                ${ddEntity.shortName}
            </stripes:link> 
            ${actionBean.attrOwnerType}
        </h1>
        <div id="quickbar">
            <ul>
                <li class="back">
                    <stripes:link beanclass="${actionBean['class']}">
                        <stripes:param name="attributeId" value="${actionBean.attributeId}"/>
                        <stripes:param name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                        <stripes:param name="attrOwnerId" value="${actionBean.attrOwnerId}"/>
                        Back to attribute values
                    </stripes:link>
                </li>
            </ul>
        </div>
        <div id="drop-operations">
            <ul>
                <li class="search open"><a class="searchSection" href="#" title="Search vocabulary concepts">Search</a></li>
            </ul>
        </div>

        <stripes:form method="get" id="searchForm" beanclass="${actionBean['class'].name}">
            <div id="filters">
                <table class="filter">
                    <stripes:param name="attributeId" value="${actionBean.attributeId}"/>
                    <stripes:param name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                    <stripes:param name="attrOwnerId" value="${actionBean.attrOwnerId}"/>

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
                    <stripes:submit name="add" value="Search" class="mediumbuttonb searchButton" />
                    <input class="mediumbuttonb" type="reset" value="Reset" />
                </p>
            </div>
            <script type="text/javascript">
            // <![CDATA[
                (function($) {
                    $(document).ready(function() {
                        applySearchToggle("searchForm");
                        <stripes:url var="url" beanclass="eionet.web.action.VocabularyFolderActionBean"></stripes:url>
                        applyBoundElementsFiltererInteractions("${url}", ${actionBean.attribute.vocabulary.id});
                        
                        $("input.massSelector").click(function() {
                            $("input.conceptSelector").prop('checked', $(this).prop('checked'));
                            $(this).prop('checked')? $("#conceptsForm tbody tr").addClass("selected") : $("#conceptsForm tbody tr").removeClass("selected");
                        });
                   }); 
                }) (jQuery);
            // ]]>
            </script>
        </stripes:form>

        <stripes:form method="post" id="conceptsForm" beanclass="${actionBean['class'].name}">
            <display:table class="datatable results" name="${actionBean.vocabularyConceptResult}"
                           requestURI="${actionBean.contextPath}/vocabularyvalues/attribute/${actionBean.attributeId}/${actionBean.attrOwnerType}/${actionBean.attrOwnerId}"
                           id="concept" excludedParams="attributeId attrOwnerType attrOwnerId">
                <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No vocabulary concepts found.</p>" />
                <display:setProperty name="paging.banner.item_name" value="concept" />
                <display:setProperty name="paging.banner.items_name" value="concepts" />

                <display:column style="width: 5%" title="<input type='checkbox' class='massSelector'  />">
                    <stripes:checkbox name="conceptIds" class="selectable conceptSelector" value="${concept.id}" />
                </display:column>
                <display:column title="Identifier" class="${actionBean.attribute.vocabulary.numericConceptIdentifiers ? 'number' : ''}" style="min-width:30px">
                    <c:out value="${concept.identifier}" />
                </display:column>
                <display:column title="Label">
                    <c:out value="${concept.label}" />
                </display:column>
                <display:footer>
                    <tr>
                        <td></td>
                        <td colspan="2">
                            <stripes:hidden name="attributeId" value="${actionBean.attributeId}"/>
                            <stripes:hidden name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                            <stripes:hidden name="attrOwnerId" value="${actionBean.attrOwnerId}"/>
                            <stripes:submit name="saveAdd" value="Add selected concepts" />
                        </td>
                    </tr>
                </display:footer>
            </display:table>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
