<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute Vocabulary Values editor" currentSection="${actionBean.currentSection}">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test="${actionBean.attrOwnerType eq 'dataset'}">
                <c:set var="ddEntity" value="${actionBean.dataset}"/>
                <c:set var="backLabel" value="Back to dataset edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/datasets/${ddEntity.id}/edit"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/datasets/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'dataelement'}">
                <c:set var="ddEntity" value="${actionBean.dataElement}"/>
                <c:set var="backLabel" value="Back to data element edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/dataelements/${ddEntity.id}/edit"/>
                 <c:set var="ddEntityUrl" value="${actionBean.contextPath}/dataelements/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'table'}">
                <c:set var="ddEntity" value="${actionBean.datasetTable}"/>
                <c:set var="backLabel" value="Back to dataset table edit page"/>
                <c:set var="backLink" value="${actionBean.contextPath}/tables/${ddEntity.id}/edit"/>
                <c:set var="ddEntityUrl" value="${actionBean.contextPath}/tables/${ddEntity.id}"/>
            </c:when>
        </c:choose>
        <h1>Values for the <c:out value="${actionBean.attribute.shortName}"/> attribute corresponding to the 
            <stripes:link href="${ddEntityUrl}">
                ${ddEntity.shortName}
            </stripes:link> 
            ${actionBean.attrOwnerType}
        </h1>

         <div id="drop-operations">
                <ul>
                    <li class="back">
                        <stripes:link href="${backLink}">
                            ${backLabel}
                        </stripes:link>
                    </li>
                    <li class="add">
                        <stripes:link beanclass="${actionBean['class']}" event="add">
                            Add value
                            <stripes:param name="currentSection" value="${actionBean.currentSection}"/>
                            <stripes:param name="attributeId" value="${actionBean.attributeId}"/>
                            <stripes:param name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                            <stripes:param name="attrOwnerId" value="${actionBean.attrOwnerId}"/>
                        </stripes:link>
                    </li>
                    <li class="delete">
                        <stripes:link beanclass="${actionBean['class']}" event="deleteAll" 
                                      onclick="return confirm('Are you sure you want to remove all values?');" >
                            Delete all values
                            <stripes:param name="attributeId" value="${actionBean.attributeId}"/>
                            <stripes:param name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                            <stripes:param name="attrOwnerId" value="${actionBean.attrOwnerId}"/>
                        </stripes:link>
                    </li>
                </ul>
            </div>
        <table class="datatable results" style="clear:right">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>Identifier</th>
                    <th>Label</th>
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
                            <stripes:form beanclass="${actionBean['class']}" 
                                          onclick="return confirm('Are you sure you want to remove this attribute value?');" >
                                <stripes:hidden name="conceptIdentifier" value="${concept.identifier}"/>
                                <stripes:hidden name="attributeId"/>
                                <stripes:hidden name="attrOwnerType"/>
                                <stripes:hidden name="attrOwnerId"/>
                                <stripes:submit name="delete" value="Delete" />
                            </stripes:form>
                        </td>
                        <td>
                            <stripes:link href="${actionBean.contextPath}/vocabularyconcept/${fn:toLowerCase(actionBean.attribute.vocabulary.folderLabel)}/${fn:toLowerCase(actionBean.attribute.vocabulary.label)}/${concept.identifier}">
                                ${concept.identifier}
                            </stripes:link>
                        </td>
                        <td>
                            <c:out value="${concept.label}"/>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </stripes:layout-component>
</stripes:layout-render>