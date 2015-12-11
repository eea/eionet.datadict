<%@include file="/pages/common/taglibs.jsp"%>

<stripes:form partial="true" beanclass="${actionBean.class.name}">
    <tr id="boundElementFilterRow-${actionBean.boundElementFilter.id}" data-filter-id="${actionBean.boundElementFilter.id}" class="boundElementFilter">
        <th scope="row" class="scope-row simple_attr_title" title="${fn:escapeXml(actionBean.boundElementFilter.label)}">
            <label for="boundElementFilter-${actionBean.boundElementFilter.id}"><span style="white-space:nowrap;"><c:out value="${actionBean.boundElementFilter.label}" /></span></label>
        </th>
        <td class="simple_attr_value" style="padding-right: 5em;">
            <stripes:hidden name="filter.boundElements[${actionBean.boundElementFilterIndex}].id" value="${actionBean.boundElementFilter.id}" class="boundElementFilterId" />
            <stripes:select name="filter.boundElements[${actionBean.boundElementFilterIndex}].value" class="boundElementFilterSelect">
                <stripes:option value="" label="All" />
                <stripes:options-map map="${actionBean.boundElementFilter.options}" />
            </stripes:select>
            <c:url var="delIcon" value="/images/button_remove.gif" />
            <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
        </td>
    </tr>
</stripes:form>