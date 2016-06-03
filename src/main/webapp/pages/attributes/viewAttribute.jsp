<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute" currentSection="attributes">
    <stripes:layout-component name="contents">
        <h1>View attribute definition</h1>
        <div id="drop-operations">
            <c:if test="${ddfn:userHasPermission(actionBean.user.userName, '/attributes/', 'u')}">
                <ul>
                    <li class="edit">
                        <stripes:link beanclass="eionet.datadict.controllers.AttributeActionBean2" event="edit">
                            <stripes:param name="attribute.id" value="${actionBean.attribute.id}"/>
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
                    <em>${actionBean.attribute.shortName}</em>
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Name</th>
                <td>
                    ${actionBean.attribute.name}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <c:if test="${not empty actionBean.attribute.namespace.shortName}">
                <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                    <th scope="row" class="scope-row">Context</th>
                    <td>
                        <c:choose>
                            <c:when test="${not empty actionBean.attribute.namespace.fullName}">
                                ${actionBean.attribute.namespace.fullName}
                            </c:when>
                            <c:otherwise>
                                ${actionBean.attribute.namespace.shortName}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <c:set var="rowStatus" value="${rowStatus + 1}"/>
                </tr>
            </c:if>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Definition</th>
                <td>
                    ${actionBean.attribute.definition}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Obligation</th>
                <td>
                    ${actionBean.attribute.obligationType.label}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display type</th>
                <td>
                    ${actionBean.attribute.displayType.label}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <c:if test="${actionBean.attribute.displayType == 'VOCABULARY'}">
                <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                    <th scope="row" class="scope-row">
                        Vocabulary
                    </th>
                    <td>
                        <c:choose>
                            <c:when test="${not empty actionBean.attribute.vocabulary}">
                                <stripes:link href="${actionBean.contextPath}/vocabulary/${actionBean.attribute.vocabulary.folderLabel}/${actionBean.attribute.vocabulary.identifier}/view">
                                    ${actionBean.attribute.vocabulary.label}
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
            <c:if test="${actionBean.attribute.displayType == 'SELECT'}">
                <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                    <th scope="row" class="scope-row">
                        <a href="${actionBean.contextPath}/fixedvalues/attr/${actionBean.attribute.id}">
                            Fixed values
                        </a>
                    </th>
                    <td>
                        <c:forEach var="fixedValue" items="${actionBean.fixedValues}">
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
                        <c:when test="${actionBean.attribute.displayMultiple}">
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
                    ${actionBean.attribute.valueInheritanceMode.label}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display order</th>
                <td>
                    <c:if test="${not empty actionBean.attribute.displayOrder and actionBean.attribute.displayOrder != 999}">
                        ${actionBean.attribute.displayOrder}
                    </c:if>
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display for</th>
                <td>
                    <c:forEach var="targetEntity" items="${actionBean.attribute.targetEntities}">
                        ${targetEntity.label}<br/>
                    </c:forEach>
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display width</th>
                <td>${actionBean.attribute.displayWidth}</td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">Display height</th>
                <td>${actionBean.attribute.displayHeight}</td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">RDF property URI</th>
                <td>
                    ${actionBean.attribute.rdfNamespace.uri}
                </td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            <tr class="${(rowStatus+1) % 2 != 0 ? 'odd' : 'even'}">
                <th scope="row" class="scope-row">RDF property name</th>
                <td>${actionBean.attribute.rdfPropertyName}</td>
                <c:set var="rowStatus" value="${rowStatus + 1}"/>
            </tr>
        </table>
    </stripes:layout-component>
</stripes:layout-render>