<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Fixed Value">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <%@ include file="/pages/fixedValues/fixed_value_header.jsp"%>
            
            <table class="datatable" style="width:auto" cellspacing="0">
                <tbody>
                    <tr>
                        <th scope="row">Code:</th>
                        <td><c:out value="${actionBean.viewModel.fixedValue.value}" /></td>
                    </tr>
                    <c:if test="${actionBean.viewModel.defaultValueRequired}">
                        <tr>
                            <th scope="row">Default:</th>
                            <td>${ddfn:checkmark(actionBean.viewModel.fixedValue.defaultValue)}</td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row">Label:</th>
                        <td><c:out value="${actionBean.viewModel.fixedValue.shortDescription}" /></td>
                    </tr>
                    <tr>
                        <th scope="row">Definition:</th>
                        <td><c:out value="${actionBean.viewModel.fixedValue.definition}" /></td>
                    </tr>
                </tbody>
            </table>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>