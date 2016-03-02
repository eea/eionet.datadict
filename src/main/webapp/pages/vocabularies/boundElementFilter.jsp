<%@include file="/pages/common/taglibs.jsp"%>

<stripes:form partial="true" beanclass="${actionBean.class.name}">
    <span class="filterItem boundElementFilter" data-filter-id="${actionBean.boundElementFilter.id}">
        <label for="boundElementFilter-${actionBean.boundElementFilter.id}"><span style="white-space:nowrap;"><c:out value="${actionBean.boundElementFilter.label}" /></span></label>
        <stripes:hidden name="filter.boundElements[${actionBean.boundElementFilterIndex}].id" value="${actionBean.boundElementFilter.id}" class="boundElementFilterId" />
        <stripes:select name="filter.boundElements[${actionBean.boundElementFilterIndex}].value" class="boundElementFilterSelect">
            <stripes:option value="" label="All" />
            <stripes:options-map map="${actionBean.boundElementFilter.options}" />
        </stripes:select>
        <a href="#" class="deleteButton"></a>
    </span>
</stripes:form>