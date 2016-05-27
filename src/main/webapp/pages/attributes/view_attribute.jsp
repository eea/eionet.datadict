<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute" currentSection="attributes">
    <stripes:layout-component name="contents">
        <c:set value="${actionBean.viewModel}" var="model"/>
        <h1>View attribute definition</h1>
        <div id="drop-operations">
            <c:if test="${ddfn:userHasPermission(actionBean.user.userName, '/attributes/', 'u')}">
                <ul>
                    <li class="edit">
                        <stripes:link beanclass="eionet.datadict.action.AttributeActionBean" event="edit">
                            <stripes:param name="attrId" value="${actionBean.attrId}"/>
                            Edit
                        </stripes:link>
                    </li>
                </ul>
            </c:if>
        </div>
        <table class="datatable results" style="clear:right">
            <col style="width:10em"/>
            <col style="width:35em"/>
            <c:set var="rowStatus" value="0"/>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Type</th>
                <td><b>Simple Attribute</b></td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Short name</th>
                <td>
                    <em>${model.attributeDefinition.shortName}</em>
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Name</th>
                <td>
                    ${model.attributeDefinition.name}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <c:if test="${not empty model.attributeDefinition.namespace.shortName}">
                <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                    <th scope="row" class="scope-row">Context</th>
                    <td>
                        <c:choose>
                            <c:when test="${not empty model.attributeDefinition.namespace.fullName}">
                                ${model.attributeDefinition.namespace.fullName}
                            </c:when>
                            <c:otherwise>
                                ${model.attributeDefinition.namespace.shortName}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <c:set var="rowStatus" value="${rowStatus + 1}"/>
                </tr>
            </c:if>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Definition</th>
                <td>
                    ${model.attributeDefinition.definition}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Obligation</th>
                <td>
                    ${model.attributeDefinition.obligationLevel.label}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display type</th>
                <td>
                    ${model.attributeDefinition.displayType.displayLabel}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <c:if test="${model.attributeDefinition.displayType == 'VOCABULARY'}">
                <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                    <th scope="row" class="scope-row">
                        Vocabulary
                    </th>
                    <td>
                        <c:choose>
                            <c:when test="${not empty model.attributeDefinition.vocabulary}">
                                <stripes:link href="${actionBean.contextPath}/vocabulary/${model.attributeDefinition.vocabulary.folderLabel}/${model.attributeDefinition.vocabulary.identifier}/view">
                                    ${model.attributeDefinition.vocabulary.label}
                                </stripes:link>
                            </c:when>
                            <c:otherwise>
                                <em>None</em>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </c:if>
            <c:if test="${model.attributeDefinition.displayType == 'SELECT'}">
                <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                    <th scope="row" class="scope-row">
                        <a href="${actionBean.contextPath}/fixedvalues/attr/${actionBean.attrId}">
                            Fixed values
                        </a>
                    </th>
                    <td>
                        <c:forEach var="fixedValue" items="${model.fixedValues}">
                            ${fixedValue.value}</br>
                        </c:forEach>
                    </td>
                    <c:set var="rowStatus" value="${rowStatus + 1}"/>
                </tr>
            </c:if>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display multiple</th>
                <td>
                    <c:choose>
                        <c:when test="${model.attributeDefinition.displayMultiple}">
                            True
                        </c:when>
                        <c:otherwise>
                            False
                        </c:otherwise>
                    </c:choose>
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>  
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Inheritance</th>
                <td>
                    ${model.attributeDefinition.inherit.label}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display order</th>
                <td>
                    <c:if test="${not empty model.attributeDefinition.displayOrder and model.attributeDefinition.displayOrder != 999}">
                        ${model.attributeDefinition.displayOrder}
                    </c:if>
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display for</th>
                <td>
                    <c:forEach var="displayType" items="${model.displayForTypes}">
                        ${displayType.label}<br/>
                    </c:forEach>
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display width</th>
                <td>${model.attributeDefinition.displayWidth}</td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display height</th>
                <td>${model.attributeDefinition.displayHeight}</td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">RDF property URI</th>
                <td>
                    ${model.attributeDefinition.rdfNamespace.uri}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">RDF property name</th>
                <td>${model.attributeDefinition.rdfPropertyName}</td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
        </table>
    </stripes:layout-component>
</stripes:layout-render>