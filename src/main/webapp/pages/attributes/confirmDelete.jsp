<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute editor" currentSection="attributes">

    <stripes:layout-component name="contents">
        <h1>Deleting attribute 
            <stripes:link beanclass="eionet.datadict.controllers.AttributeActionBean" event="view">
                <stripes:param name="attribute.id" value="${actionBean.attribute.id}"/>
                ${actionBean.attribute.shortName}
            </stripes:link></h1>
        <div>
            The action you are about to perform will remove attribute values of ${actionBean.attributeValuesCount} entities:
        </div>
        <br>
        <table class="datatable">
            <thead>
                <tr>
                    <th>Type</th>
                    <th>Number of Entities</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="entityTypeAndCardinality" items="${actionBean.entityTypesWithAttributeValues}">
                    <tr>
                        <td>
                            ${entityTypeAndCardinality.key.label}
                        </td>
                        <td>
                            ${entityTypeAndCardinality.value}
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <br>
        <div>
            <stripes:form beanclass="eionet.datadict.controllers.AttributeActionBean">
                <stripes:submit name="delete" value="Delete"/>
                <stripes:submit name="edit" value="Cancel"/>
                <stripes:hidden name="attribute.id"/>
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>