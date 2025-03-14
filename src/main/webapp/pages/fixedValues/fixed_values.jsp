<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Fixed Values" currentSection="dataElements">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <%@ include file="/pages/fixedValues/fixed_values_header.jsp"%>
            <c:choose>
                <c:when test="${empty actionBean.viewModel.fixedValues}">
                    There are no 
                    <c:out value="${actionBean.viewModel.fixedValueCategoryLower}" /> 
                    values for this 
                    <c:out value="${actionBean.viewModel.owner.entityName}" />
                </c:when>
                <c:otherwise>
                    <table class="datatable results">
                        <thead>
                            <tr>
                                <th>Code</th>
                                <c:if test="${actionBean.viewModel.defaultValueRequired}">
                                    <th>Default</th>
                                </c:if>
                                <th>Label</th>
                                <th>Definition</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${actionBean.viewModel.fixedValues}" var="fixedValue">
                                <tr>
                                    <td><c:out value="${fixedValue.value}" /></td>
                                    <c:if test="${actionBean.viewModel.defaultValueRequired}">
                                        <td>
                                            ${ddfn:checkmark(fixedValue.defaultValue)}
                                        </td>
                                    </c:if>
                                    <td><c:out value="${fixedValue.shortDescription}" /></td>
                                    <td><c:out value="${fixedValue.definition}" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>
