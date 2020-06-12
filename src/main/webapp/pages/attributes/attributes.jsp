<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attributes" currentSection="administration">
    <stripes:layout-component name="contents">
        <h1>Attributes</h1>
        <div id="drop-operations">
            <ul>
                <li class="back">
                    <stripes:link href="/administration">Back to administration</stripes:link>
                </li>
                <c:if test="${ddfn:userHasPermission(actionBean.user, '/attributes', 'i')}">
                    <li class="add">
                        <stripes:link beanclass="eionet.web.action.AttributeActionBean" event="add">
                            Add attribute
                        </stripes:link>
                    </li>
                </c:if>
            </ul>
        </div>
        <c:choose>
            <c:when test="${empty actionBean.attributes}">
                <p class='not-found'>No attributes found.</p>
            </c:when>
            <c:otherwise>
                <p>
                    This is a list of all definition attributes used in Data Dictionary.
                    Every attribute is uniquely identifed by its short name. Click page help
                    and question marks in column headers to to find out more.
                    To view<c:if test="${ddfn:userHasPermission(actionBean.user, '/attributes', 'i')}"> or modify</c:if> an attribute's
                    definition, click its short name.
                    <c:if test="${ddfn:userHasPermission(actionBean.user, '/attributes', 'i')}">
                        To add a new attribute, click the 'Add' button on top of the list.
                    </c:if>
                </p>
                <table class="datatable results">
                    <thead>
                        <tr>
                            <th style="width:10%">Short name</th>
                            <c:forEach var="targetEntity" items="${ddfn:getEnumValues('eionet.datadict.model.Attribute$TargetEntity')}"> 
                                <th>
                                   ${fn:escapeXml(targetEntity.label)}
                                </th>
                            </c:forEach>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="attribute" items="${actionBean.attributes}" varStatus="row">
                            <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                                <td>
                                    <a href="${pageContext.request.contextPath}/attribute/view/${attribute.id}">${fn:escapeXml(attribute.shortName)}</a>
                                </td>
                                <c:forEach var="targetEntity" items="${ddfn:getEnumValues('eionet.datadict.model.Attribute$TargetEntity')}"> 
                                    <td class="center">
                                        <c:if test="${ddfn:contains(attribute.targetEntities, targetEntity)}">
                                            <span class="check">Yes</span>
                                        </c:if>
                                    </td>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>