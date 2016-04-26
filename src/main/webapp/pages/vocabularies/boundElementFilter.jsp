<%@include file="/pages/common/taglibs.jsp"%>

<stripes:form partial="true" beanclass="${actionBean.class.name}">
    <tr class="boundElementFilter" data-filter-id="${actionBean.boundElementFilter.id}">
        <td class="label">
            <label for="boundElementFilter-${actionBean.boundElementFilter.id}"><c:out value="${actionBean.boundElementFilter.label}" /></label>
        </td>
        <td class="input">
            <stripes:hidden name="filter.boundElements[${actionBean.boundElementFilterIndex}].id" value="${actionBean.boundElementFilter.id}" class="boundElementFilterId" />
            <stripes:select name="filter.boundElements[${actionBean.boundElementFilterIndex}].value" class="boundElementFilterSelect">
                <stripes:option value="" label="All" />
                <stripes:options-map map="${actionBean.boundElementFilter.options}" />
            </stripes:select>
            <a href="#" class="deleteButton" title="Remove from search criteria"></a>
        </td>
    </tr>
</stripes:form>