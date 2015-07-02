<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="New Fixed Value">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <h1>Fixed value of <stripes:link href="/dataelements/${actionBean.owner.id}">${actionBean.owner.shortName}</stripes:link> attribute</h1>
            <div id="operations">
                <ul>
                    <li>
                        <stripes:link href="/fixed_values/${actionBean.ownerType}/${actionBean.owner.id}">back to fixed values</stripes:link>
                    </li>
                </ul>
            </div>
            <stripes:form beanclass="eionet.web.action.FixedValuesActionBean">
                <table class="datatable" style="width:auto">
                    <tbody>
                        <tr>
                            <th>Value</th>
                            <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                            <td><stripes:text name="fixedValue.value" /></td>
                        </tr>
                        <tr>
                            <th>Definition</th>
                            <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                            <td><stripes:textarea class="small" rows="3" cols="60" name="fixedValue.definition" /></td>
                        </tr>
                        <tr>
                            <th>Short Description</th>
                            <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                            <td><stripes:textarea class="small" rows="3" cols="60" name="fixedValue.shortDescription" /></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td></td>
                            <td><stripes:submit name="edit" value="Edit" /></td>
                        </tr>
                    </tbody>
                </table>
                <stripes:hidden name="fixedValue.id" value="${actionBean.fixedValue.id}" />
                <stripes:hidden name="ownerId" value="${ownerId}" />
                <stripes:hidden name="ownerType" value="${ownerType}" />
                <stripes:hidden name="fixedValue.isDefault" value="NO" />        
            </stripes:form>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>