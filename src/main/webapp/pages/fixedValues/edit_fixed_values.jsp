<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Fixed Values" currentSection="dataElements">
    <stripes:layout-component name="head">

        <script type="text/javascript">

            (
                    function ($, window, document) {
                        $(document).ready(function () {

                            $('#btnDeleteAllFixedValues').click(function () {
                                var msg = 'This action will delete all existing <c:out value="${actionBean.viewModel.fixedValueCategoryLower}" /> values. Are you sure you want to continue?';

                                if (!window.confirm(msg)) {
                                    return false;
                                }

                                return true;
                            });

                        });
                    }
            )(jQuery, window, document);

        </script>

    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <%@ include file="/pages/fixedValues/fixed_values_header.jsp"%>
            <div id="drop-operations">
                <ul>
                    <li class="add">
                        <stripes:link beanclass="${actionBean.viewModel.actionBeanName}" event="add">
                            <stripes:param name="ownerId" value="${actionBean.ownerId}" />
                            Add value
                        </stripes:link>
                    </li>
                    <li class="import">
                        <stripes:link href="/import.jsp">
                            <stripes:param name="mode" value="FXV" />
                            <stripes:param name="delem_id" value="${actionBean.viewModel.owner.id}" />
                            <stripes:param name="short_name" value="${actionBean.viewModel.owner.caption}" />
                            Import values
                        </stripes:link>
                    </li>
                    <li class="delete">
                        <stripes:link id="btnDeleteAllFixedValues" beanclass="${actionBean.viewModel.actionBeanName}" event="delete">
                            <stripes:param name="ownerId" value="${actionBean.ownerId}" />
                            Delete all values
                        </stripes:link>
                    </li>
                </ul>
            </div>
            <c:choose>
                <c:when test="${empty actionBean.viewModel.fixedValues}">
                    <p class="not-found">
                    There are no 
                    <c:out value="${actionBean.viewModel.fixedValueCategoryLower}" /> 
                    values for this 
                    <c:out value="${actionBean.viewModel.owner.entityName}" />
                    </p>
                </c:when>
                <c:otherwise>
                    <table class="datatable">
                        <tr>
                            <th></th>
                            <th>Code</th>
                            <c:if test="${actionBean.viewModel.defaultValueRequired}">
                                <th>Default</th>
                            </c:if>
                            <th>Label</th>
                            <th>Definition</th>
                        </tr>
                        <c:forEach items="${actionBean.viewModel.fixedValues}" var="fixedValue">
                            <tr>
                                <td>
                                    <stripes:form beanclass="${actionBean.viewModel.actionBeanName}" 
                                                  onclick="return confirm('Are you sure you want to delete this value?');" >
                                        <stripes:hidden name="ownerId" value="${actionBean.viewModel.owner.id}" />
                                        <stripes:hidden name="fixedValueId" value="${fixedValue.id}" />
                                        <stripes:submit name="delete" value="Delete" />
                                    </stripes:form>
                                </td>
                                <td>
                                    <stripes:link beanclass="${actionBean.viewModel.actionBeanName}" event="edit">
                                        <stripes:param name="ownerId" value="${actionBean.viewModel.owner.id}" />
                                        <stripes:param name="fixedValueId" value="${fixedValue.id}" />
                                        ${fixedValue.value}
                                    </stripes:link>
                                </td>
                                <c:if test="${actionBean.viewModel.defaultValueRequired}">
                                    <td>
                                        ${ddfn:checkmark(fixedValue.defaultValue)}
                                    </td>
                                </c:if>
                                <td>${fixedValue.shortDescription}</td>
                                <td>${fixedValue.definition}</td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>
