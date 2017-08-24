<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Inference Rule" currentSection="dataElements">

    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <h1>Inference rules of <stripes:link href="/dataelements/${actionBean.parentElement.id}">${actionBean.parentElement.shortName}</stripes:link> element </h1>
            
            <div id="drop-operations">
                <ul>
                    <li class="back">
                         <stripes:link href="/dataelements/${actionBean.parentElement.id}">
                            Back to data element
                        </stripes:link>
                    </li>
                    <li class="create">
                        <a href="${pageContext.servletContext.contextPath}/inference_rules/${actionBean.parentElement.id}/newRule">
                            Create new rule
                        </a>
                    </li>
                </ul>
            </div>
            <p>
                <c:choose>
                    <c:when test="${empty actionBean.rules}">
                        <p class="not-found">There are no rules for this element.</p>
                    </c:when>
                    <c:otherwise>
                        <table class="datatable results">
                            <thead>
                                <tr>
                                    <th>Inference Rule</th>
                                    <th>Element</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${actionBean.rules}" var="rule" varStatus="row">
                                    <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                                        <td>${fn:escapeXml(rule.type.name)}</td>
                                        <td><stripes:link href="/dataelements/${rule.targetDElement.id}">${fn:escapeXml(rule.targetDElement.identifier)}</stripes:link></td>
                                        <td>
                                            <form method="get" action="${pageContext.request.contextPath}/inference_rules/${actionBean.parentElement.id}/existingRule/${fn:escapeXml(rule.type)}/${rule.targetDElement.id}">
                                                <input class="actionButton" type="submit" value="Edit" />
                                            </form>
                                            <form method="get" action="${pageContext.request.contextPath}/inference_rules/${actionBean.parentElement.id}/deleteRule/${fn:escapeXml(rule.type)}/${rule.targetDElement.id}"
                                                           onsubmit="return confirm('Are you sure you want to delete this rule?');">
                                                <input class="actionButton" type="submit" value="Delete" />
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </p>
        </c:if>
    </stripes:layout-component>

</stripes:layout-render>