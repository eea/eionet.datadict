<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="eionet.meta.dao.domain.SchemaSet"%>
<%@page import="eionet.meta.DElemAttribute"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add schema set">

    <stripes:layout-component name="contents">
    <h1>Create schema set</h1>

    <stripes:form method="post" beanclass="eionet.web.action.SchemaSetActionBean">
        <div id="outerframe">
            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
                    <col style="width:4%"/>
                    <col style="width:4%"/>
                    <col style="width:62%"/>
                </colgroup>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_help">
                        <a href="/help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                            <img style="border:0" src="<%=request.getContextPath()%>/images/info_icon.gif" width="16" height="16" alt="help"/>
                        </a>
                    </td>
                    <td class="simple_attr_help">
                        <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="schemaSet.identifier"/>
                    </td>
                </tr>
                <c:forEach items="${actionBean.attributes}" var="attributesEntry">
                    <c:set var="attribute" value="${attributesEntry.value}"/>
                    <c:if test="${attribute.mandatory}">
                        <tr>
                            <th scope="row" class="scope-row simple_attr_title">
                                <c:out value="${attribute.shortName}"/>
                            </th>
                            <td class="simple_attr_help">
                                <a href="${pageContext.request.contextPath}/help.jsp?attrid=${attribute.ID}&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                                    <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="Help"/>
                                </a>
                            </td>
                            <td class="simple_attr_help">
                                <c:if test="${attribute.obligation=='M'}">
                                    <img style="border:0" src="${pageContext.request.contextPath}/images/mandatory.gif" width="16" height="16" alt=""/>
                                </c:if>
                                <c:if test="${attribute.obligation=='O'}">
                                    <img style="border:0" src="${pageContext.request.contextPath}/images/optional.gif" width="16" height="16" alt=""/>
                                </c:if>
                                <c:if test="${attribute.obligation=='C'}">
                                    <img style="border:0" src="${pageContext.request.contextPath}/images/conditional.gif" width="16" height="16" alt=""/>
                                </c:if>
                            </td>
                            <td style="word-wrap:break-word;wrap-option:emergency" class="simple_attr_value">
                                <c:if test="${!attribute.multipleValuesAllowed || attribute.displayType=='image'}">
                                    <c:if test="${attribute.displayType=='text'}">
                                        <input type="text" name="attr_${attribute.ID}" value="${fn:escapeXml(attribute.value)}" style="width: 500px;" class="smalltext"/>
                                    </c:if>
                                    <c:if test="${attribute.displayType=='textarea'}">
                                        <textarea name="attr_${attribute.ID}" rows="${attribute.displayHeight}" style="width: 500px;" class="small"><c:out value="${attribute.value}"/></textarea>
                                    </c:if>
                                    <c:if test="${attribute.displayType=='select'}">
                                        <select name="attr_${attribute.ID}" class="small">
                                            <c:forEach items="${actionBean.fixedValuedAttributeValues[attribute.ID]}" var="attrFixedValue">
                                                <c:set var="inputSelectedString" value="${ddfn:inputSelectedString(attribute.value!=null && attribute.value==attrFixedValue)}"/>
                                                <option value="${fn:escapeXml(attrFixedValue)}" ${inputSelectedString}>${fn:escapeXml(attrFixedValue)}</option>
                                            </c:forEach>
                                        </select>
                                    </c:if>
                                    <c:if test="${attribute.displayType=='image'}">
                                        <span class="barfont">
                                            <a href="${actionBean.contextPath}/imgattr.jsp?obj_id=${actionBean.schemaSet.id}&amp;obj_type=<%=DElemAttribute.ParentType.SCHEMA_SET%>&amp;attr_id=${attribute.ID}&amp;obj_name=${actionBean.schemaSet.identifier}&amp;attr_name=${attribute.shortName}">
                                                <c:if test="${empty attribute.value}">Click to add image</c:if>
                                                <c:if test="${not empty attribute.value}">Click to manage this image</c:if>
                                            </a>
                                        </span>
                                    </c:if>
                                </c:if>
                                <c:if test="${attribute.multipleValuesAllowed && attribute.displayType!='image'}">
                                    <input type="text" name="other_value_attr_${attribute.ID}" value="insert other value" style="font-size:0.9em" onfocus="this.value=''"/>
                                    <input type="button" value="-&gt;" style="font-size:0.8em;" onclick="addMultiSelectRow(document.forms['form1'].elements['other_value_attr_${attribute.ID}'].value, 'attr_mult_${attribute.ID}','multiselect_div_attr_${attribute.ID}')"/>
                                    <div id="multiselect_div_attr_${attribute.ID}" class="multiselect" style="height:7.5em;width:25em;">
                                        <c:forEach items="${actionBean.multiValuedAttributeValues[attribute.ID]}" var="possibleValue">
                                            <c:set var="inputCheckedString" value="${ddfn:inputCheckedString(ddfn:contains(attribute.values,possibleValue))}"/>
                                            <label style="display:block">
                                                <input type="checkbox" name="attr_mult_${attribute.ID}" value="${fn:escapeXml(possibleValue)}" ${inputCheckedString} style="margin-right:5px"/><c:out value="${possibleValue}"/>
                                            </label>
                                        </c:forEach>
                                    </div>
                                </c:if>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                <tr>
                    <th>&nbsp;</th>
                    <td colspan="3">
                        <stripes:submit name="add" value="Add" class="mediumbuttonb"/>
                        <stripes:submit name="cancelAdd" value="Cancel" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
            <fieldset style="display:none">
                <stripes:hidden name="schemaSet.workingCopy" value="true"/>
            </fieldset>
        </div>
    </stripes:form>
    </stripes:layout-component>

</stripes:layout-render>