<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Fixed Value Editor" currentSection="dataElements">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <%@ include file="/pages/fixedValues/fixed_value_header.jsp"%>

            <div id="drop-operations">
                <ul>
                    <li class="back">
                        <stripes:link beanclass="${actionBean.viewModel.actionBeanName}" event="edit">
                            <stripes:param name="ownerId" value="${actionBean.viewModel.owner.id}" />
                            Back to 
                            <c:out value="${actionBean.viewModel.fixedValueCategoryLower}" /> 
                            values
                        </stripes:link>
                    </li>
                </ul>
            </div>

            <stripes:form beanclass="${actionBean.viewModel.actionBeanName}">
                <table class="datatable results">
                    <tbody>
                        <tr>
                            <th scope="row" class="scope-row">Code</th>
                            <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                            <td><textarea rows="3" cols="50" name="viewModel.fixedValue.value" ><c:out value="${actionBean.viewModel.fixedValue.value}"/></textarea></td>
                        </tr>
                        <c:if test="${actionBean.viewModel.defaultValueRequired}">
                            <tr>
                                <th scope="row" class="scope-row">Default</th>
                                <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                                <td>
                                    <stripes:checkbox name="viewModel.fixedValue.defaultValue" />
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <th scope="row" class="scope-row">Label</th>
                            <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                            <td><stripes:textarea class="small" rows="3" cols="50" name="viewModel.fixedValue.shortDescription" /></td>
                        </tr>
                        <tr>
                            <th scope="row" class="scope-row">Definition</th>
                            <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                            <td><stripes:textarea class="small" rows="3" cols="50" name="viewModel.fixedValue.definition" /></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td></td>
                            <td><stripes:submit name="save" value="Save" /></td>
                        </tr>
                    </tbody>
                </table>

                <stripes:hidden name="ownerId" value="${actionBean.ownerId}" />
                <%--
                <stripes:hidden name="fixedValue" value="${actionBean.fixedValue}" />
                --%>
                <stripes:hidden name="viewModel.fixedValue.id" value="${actionBean.viewModel.fixedValue.id}" />
            </stripes:form>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>