<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Fixed Values">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <%@ include file="/pages/fixedValues/fixed_values_header.jsp"%>
            <c:choose>
                <c:when test="${empty actionBean.viewModel.fixedValues}">
                    There are no <c:out value="${actionBean.viewModel.fixedValueCategoryLower}" /> values for this element
                </c:when>
                <c:otherwise>
                    <table class="datatable">
                        <tr>
                            <th>Value</th>
                            <c:if test="${actionBean.viewModel.defaultValueRequired}">
                                <th>Default</th>
                            </c:if>
                            <th>Definition</th>
                            <th>Short Description</th>
                        </tr>
                        <c:forEach items="${actionBean.viewModel.fixedValues}" var="fixedValue">
                            <tr>
                                <td>${fixedValue.value}</td>
                                <c:if test="${actionBean.viewModel.defaultValueRequired}">
                                    <td>
                                        <c:out value="${fixedValue.isDefault.value}" />
                                    </td>
                                </c:if>
                                <td>${fixedValue.definition}</td>
                                <td>${fixedValue.shortDescription}</td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>
