<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute Vocabulary Values editor" currentSection="${actionBean.currentSection}">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test="${actionBean.attrOwnerType eq 'dataset'}">
                <c:set var="ddEntity" value="${actionBean.dataset}"/>
                <c:set var="ddEntityUrl" value="/datasets/${ddEntity.id}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'dataelement'}">
                <c:set var="ddEntity" value="${actionBean.dataElement}"/>
            </c:when>
            <c:when test="${actionBean.attrOwnerType eq 'table'}">
                <c:set var="ddEntity" value="${actionBean.datasetTable}"/>
            </c:when>
        </c:choose>
        <h1>
            Add value for <c:out value="${actionBean.attribute.shortName}"/> attribute corresponding to the 
            <stripes:link href="${actionBean.contextPath}${ddEntityUrl}">
                ${ddEntity.shortName}
            </stripes:link> 
            ${actionBean.attrOwnerType}
        </h1>
        <div id="drop-operations">
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
        <display:table class="datatable results" name="${actionBean.vocabularyConcepts}"
                       requestURI="${actionBean.contextPath}/vocabularyvalues/attribute/${actionBean.attributeId}/${actionBean.attrOwnerType}/${actionBean.attrOwnerId}"
                       id="concept" pagesize="30">
            <display:column title="Id">
                ${concept.identifier}
            </display:column>
            <display:column title="Label">
                ${concept.label}
            </display:column>
            <display:column>
                <stripes:link beanclass="${actionBean['class']}" event="saveAdd">
                    <stripes:param name="conceptIdentifier" value="${concept.identifier}"/>
                    <stripes:param name="attributeId" value="${actionBean.attributeId}"/>
                    <stripes:param name="attrOwnerType" value="${actionBean.attrOwnerType}"/>
                    <stripes:param name="attrOwnerId" value="${actionBean.attrOwnerId}"/>
                    [Select]
                </stripes:link>
            </display:column>
        </display:table>
    </stripes:layout-component>
</stripes:layout-render>
