<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Fixed Value Editor">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <%@ include file="/pages/fixedValues/fixed_value_header.jsp"%>

            <div id="operations">
                <ul>
                    <li>
                        <stripes:link beanclass="${actionBean.viewModel.actionBeanName}" event="edit">
                            <stripes:param name="ownerId" value="${actionBean.viewModel.owner.id}" />
                            back to 
                            <c:out value="${actionBean.viewModel.fixedValueCategoryLower}" /> 
                            values
                        </stripes:link>
                    </li>
                </ul>
            </div>

            <stripes:form beanclass="${actionBean.viewModel.actionBeanName}">
                <table class="datatable" style="width:auto">
                    <tbody>
                        <tr>
                            <th scope="row">Value:</th>
                            <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                            <td><stripes:text name="viewModel.fixedValue.value" /></td>
                        </tr>
                        <c:if test="${actionBean.viewModel.defaultValueRequired}">
                            <tr>
                                <th scope="row">Default:</th>
                                <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                                <td>
                                    <stripes:checkbox name="viewModel.fixedValue.defaultValue" />
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <th scope="row">Definition:</th>
                            <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                            <td><stripes:textarea class="small" rows="3" cols="60" name="viewModel.fixedValue.definition" /></td>
                        </tr>
                        <tr>
                            <th scope="row">Short Description:</th>
                            <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                            <td><stripes:textarea class="small" rows="3" cols="60" name="viewModel.fixedValue.shortDescription" /></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td></td>
                            <td><stripes:submit name="save" value="Save" /></td>
                        </tr>
                    </tbody>
                </table>

                <stripes:hidden name="ownerId" value="${actionBean.viewModel.owner.id}" />
                <stripes:hidden name="viewModel.fixedValue.id" value="${actionBean.viewModel.fixedValue.id}" />
            </stripes:form>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>