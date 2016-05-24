<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute">
    <stripes:layout-component name="contents">
        <c:set value="${actionBean.viewModel}" var="model"/>
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <c:if test="${ddfn:userHasPermission(actionBean.user.userName, '/attributes/', 'u')}">
                    <li>
                        <stripes:link beanclass="eionet.datadict.action.AttributeActionBean" event="edit">
                            <stripes:param name="attrId" value="${actionBean.attrId}"/>
                            Edit
                        </stripes:link>
                    </li>
                </c:if>
            </ul>
        </div>
        <h1>View attribute definition</h1>
        <table class="datatable" style="clear:right">
            <col style="width:10em"/>
            <col style="width:35em"/>
            <tr>
                <th scope="row" class="scope-row">Type</th>
                <td><b>Simple Attribute</b></td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Short name</th>
                <td>
                    <em>${model.attributeDefinition.shortName}</em>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Name</th>
                <td>
                    ${model.attributeDefinition.name}
                </td>
            </tr>
            <c:if test="${not empty model.attributeDefinition.namespace.shortName}">
                <tr>
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
                </tr>
            </c:if>
            <tr>
                <th scope="row" class="scope-row">Definition</th>
                <td>
                    ${model.attributeDefinition.definition}
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Obligation</th>
                <td>
                    ${model.attributeDefinition.obligationLevel.label}
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Display type</th>
                <td>
                    ${model.attributeDefinition.displayType.displayLabel}
                </td>
            </tr>
            <c:if test="${model.attributeDefinition.displayType == 'VOCABULARY'}">
                <tr>
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
            </c:if>
            <c:if test="${model.attributeDefinition.displayType == 'SELECT'}">
                <tr>
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
                </tr>
            </c:if>
            <tr>
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
            </tr>  
            <tr>
                <th scope="row" class="scope-row">Inheritance</th>
                <td>
                    ${model.attributeDefinition.inherit.label}
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Display order</th>
                <td>
                    <c:if test="${not empty model.attributeDefinition.displayOrder and model.attributeDefinition.displayOrder != 999}">
                        ${model.attributeDefinition.displayOrder}
                    </c:if>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Display for</th>
                <td>
                    <c:forEach var="displayType" items="${model.displayForTypes}">
                        ${displayType.label}<br/>
                    </c:forEach>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Display width</th>
                <td>${model.attributeDefinition.displayWidth}</td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">Display height</th>
                <td>${model.attributeDefinition.displayHeight}</td>
            </tr>
            <tr>
                <th scope="row" class="scope-row">RDF property URI</th>
                <td>
                    ${model.attributeDefinition.rdfNamespace.uri}
                </td>
            <tr>
                <th scope="row" class="scope-row">RDF property name</th>
                <td>${model.attributeDefinition.rdfPropertyName}</td>
            </tr>
        </table>
        <c:out value="${model.attributeDefinition.vocabulary.label}"/>
    </stripes:layout-component>
</stripes:layout-render>