<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute Vocabulary Values editor" currentSection="${actionBean.currentSection}">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test="${actionBean.attrOwnerType eq 'dataset'}">
                <c:set var="ddEntity" value="${actionBean.dataset}"/>
                <c:set var="ddEntityTitle" value="${ddEntity.shortName}"/>
                <c:set var="backLabel" value="Back to dataset edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/datasets/${ddEntity.id}/edit"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/datasets/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'dataelement'}">
                <c:set var="ddEntity" value="${actionBean.dataElement}"/>
                <c:set var="ddEntityTitle" value="${ddEntity.shortName}"/>
                <c:set var="backLabel" value="Back to data element edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/dataelements/${ddEntity.id}/edit"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/dataelements/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'table'}">
                <c:set var="ddEntity" value="${actionBean.datasetTable}"/>
                <c:set var="ddEntityTitle" value="${ddEntity.shortName}"/>
                <c:set var="backLabel" value="Back to dataset table edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/tables/${ddEntity.id}/edit"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/tables/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'schemaset'}">
                <c:set var="ddEntity" value="${actionBean.schemaSet}"/>
                <c:set var="ddEntityTitle" value="${ddEntity.identifier}"/>
                <c:set var="backLabel" value="Back to schema set edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/schemaset/${ddEntity.identifier}/edit"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/schemaset/${ddEntity.identifier}/view?workingCopy=true"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'schema'}">
                <c:set var="ddEntity" value="${actionBean.schema}"/>
                <c:set var="ddEntityTitle" value="${ddEntity.fileName}"/>
                <c:set var="backLabel" value="Back to schema edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/schema/${ddEntity.schemaSetId > 0 ? ddEntity.schemaSetIdentifier : 'root'}/${ddEntity.fileName}/edit?workingCopy=true"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/schema/${ddEntity.schemaSetId > 0 ? ddEntity.schemaSetIdentifier : 'root'}/${ddEntity.fileName}/view?workingCopy=true"/>
            </c:when>
        </c:choose>
        <h1>Values for the <c:out value="${actionBean.attribute.shortName}"/> attribute corresponding to the 
            <stripes:link href="${ddEntityUrl}">
               <c:out value="${ddEntityTitle}" />
            </stripes:link> 
            <c:out value="${actionBean.attrOwnerType}" />
        </h1>

         <div id="drop-operations">
            <ul>
                <li class="back">
                    <stripes:link href="${backLink}">
                        <c:out value="${backLabel}" />
                    </stripes:link>
                </li>
                <li class="add">
                    <stripes:link beanclass="${actionBean['class']}" event="add">
                        Add value
                        <stripes:param name="attributeId" value="${actionBean.attributeId}"/>
                        <stripes:param name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                        <stripes:param name="attrOwnerId" value="${actionBean.attrOwnerId}"/>
                    </stripes:link>
                </li>
                <c:if test="${not empty actionBean.vocabularyConcepts}">
                    <li class="delete">
                        <stripes:link beanclass="${actionBean['class']}" event="deleteAll" 
                                      onclick="return confirm('Are you sure you want to remove all values?');" >
                            Delete all values
                            <stripes:param name="attributeId" value="${actionBean.attributeId}"/>
                            <stripes:param name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                            <stripes:param name="attrOwnerId" value="${actionBean.attrOwnerId}"/>
                        </stripes:link>
                    </li>
                </c:if>
            </ul>
        </div>

        <c:choose>
            <c:when test="${not empty actionBean.vocabularyConcepts}">
                <table class="datatable results">
                    <thead>
                        <tr>
                            <th>Identifier</th>
                            <th>Label</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:set var="zebra" value="even"/>
                        <c:forEach var="concept" items="${actionBean.vocabularyConcepts}" varStatus="count">
                            <c:choose>
                                <c:when test="${zebra eq 'even'}">
                                    <c:set var="zebra" value="odd"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="zebra" value="even"/>
                                </c:otherwise>
                            </c:choose>
                            <tr class="${zebra}">
                                <td>
                                    <c:out value="${concept.identifier}" />
                                </td>
                                <td>
                                     <stripes:link href="/vocabularyconcept/${actionBean.attribute.vocabulary.folderLabel}/${actionBean.attribute.vocabulary.identifier}/${concept.identifier}/view" title="${concept.label}">
                                        <c:out value="${concept.label}" />
                                     </stripes:link>
                                </td>
                                <td>
                                    <stripes:form beanclass="${actionBean['class']}" 
                                                  onclick="return confirm('Are you sure you want to remove this attribute value?');" >
                                        <stripes:hidden name="conceptIds[0]" value="${concept.id}"/>
                                        <stripes:hidden name="attributeId"/>
                                        <stripes:hidden name="attrOwnerType"/>
                                        <stripes:hidden name="attrOwnerId"/>
                                        <stripes:submit name="delete" value="Delete" />
                                    </stripes:form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p class='not-found'>No links found.</p>
            </c:otherwise>
        </c:choose>

    </stripes:layout-component>
</stripes:layout-render>