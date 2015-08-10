<%@ include file="/pages/common/taglibs.jsp"%>

<h1>
    <c:out value="${actionBean.viewModel.fixedValueCategoryUpper}" />
    value of 
    <stripes:link href="${actionBean.viewModel.owner.uri}">
        ${actionBean.viewModel.owner.caption}
    </stripes:link> 
    <c:out value="${actionBean.viewModel.owner.entityName}" />
</h1>
