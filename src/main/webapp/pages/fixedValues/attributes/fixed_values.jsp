<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Fixed Values">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <h1>Fixed Values of <stripes:link href="/delem_attribute.jsp?type=SIMPLE&attr_id=${actionBean.owner.id}">${actionBean.owner.shortName}</stripes:link> attribute </h1>
            <div style="margin:10px">
                <stripes:form beanclass="eionet.web.action.FixedValuesActionBean" method="GET">
                    <stripes:hidden name="ownerType" value="${ownerType}" />
                    <stripes:hidden name="ownerId" value="${ownerId}" />
                    <stripes:submit name="new" value="Add new Value" />
                </stripes:form>
            </div>
            <c:choose>
                <c:when test="${empty actionBean.fixedValues}">
                    There are no allowable values for this attribute
                </c:when>
                <c:otherwise>
                    <table class="datatable">
                        <tr>
                            <th>Value</th>
                            <th>Default</th>
                            <th>Definition</th>
                            <th>Short Description</th>
                            <th>Edit</th>
                            <th>Delete</th>
                        </tr>
                        <c:forEach items="${actionBean.fixedValues}" var="fixedValue">
                            <tr>
                                <td>${fixedValue.value}</td>
                                <td>${fixedValue.isDefault}</td>
                                <td>${fixedValue.definition}</td>
                                <td>${fixedValue.shortDescription}</td>
                                <td>
                                    <stripes:form beanclass="eionet.web.action.FixedValuesActionBean" >
                                        <stripes:hidden name="ownerType" value="${ownerType}" />
                                        <stripes:hidden name="ownerId" value="${actionBean.ownerId}" />
                                        <stripes:hidden name="fixedValue.id" value="${fixedValue.id}" />
                                        <stripes:submit name="existing" value="Edit" />
                                    </stripes:form>
                                </td>
                                <td>
                                    <stripes:form beanclass="eionet.web.action.FixedValuesActionBean" onclick="return confirm('Are you sure you want to delete ${fixedValue.value} fixed value ?');" >
                                        <stripes:hidden name="fixedValue.id" value="${fixedValue.id}" />
                                        <stripes:hidden name="ownerType" value="${actionBean.ownerType}" />
                                        <stripes:hidden name="ownerId" value="${actionBean.ownerId}" />
                                        <stripes:submit name="delete" value="Delete" />
                                    </stripes:form>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>
