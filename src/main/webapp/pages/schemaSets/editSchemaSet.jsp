<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="eionet.meta.dao.domain.SchemaSet"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search tables">

    <stripes:layout-component name="contents">
    <h1>Schema set</h1>

    <stripes:form id="form1" method="post" beanclass="eionet.web.action.SchemaSetActionBean">
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
                        <a href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                            <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="help"/>
                        </a>
                    </td>
                    <td class="simple_attr_help">
                        <img style="border:0" src="${pageContext.request.contextPath}/images/mandatory.gif" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text name="schemaSet.identifier" size="30" class="smalltext"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Registration status
                    </th>
                    <td class="simple_attr_help">
                        <a href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=regstatus" onclick="pop(this.href);return false;">
                            <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="help"/>
                        </a>
                    </td>
                    <td class="simple_attr_help">
                        <img style="border:0" src="${pageContext.request.contextPath}/images/mandatory.gif" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <c:set var="regStatuses" value="<%=SchemaSet.RegStatus.values()%>"/>
                        <stripes:select name="schemaSet.regStatus" value="${actionBean.schemaSet.regStatus}">
                            <c:forEach items="${regStatuses}" var="aRegStatus">
                                <stripes:option value="${aRegStatus}" label="${aRegStatus}"/>
                            </c:forEach>
                        </stripes:select>
                    </td>
                </tr>
                <c:forEach items="${actionBean.attributes}" var="attribute">
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
                            <c:if test="${not attribute.multipleValuesAllowed}">
                                <c:if test="${attribute.displayType=='text'}">
                                    <input type="text" name="attr_${attribute.ID}" value="${fn:escapeXml(attribute.value)}" size="${attribute.displayWidth}" class="smalltext"/>
                                </c:if>
                                <c:if test="${attribute.displayType=='textarea'}">
                                    <textarea name="attr_${attribute.ID}" rows="${attribute.displayHeight}" cols="${attribute.displayWidth}" class="small"><c:out value="${attribute.value}"/></textarea>
                                </c:if>
                            </c:if>
                            <c:if test="${attribute.multipleValuesAllowed}">
                                <input type="text" name="other_value_attr_${attribute.ID}" value="insert other value" style="font-size:0.9em" onfocus="this.value=''"/>
                                <input type="button" value="-&gt;" style="font-size:0.8em;" onclick="addMultiSelectRow(document.forms['form1'].elements['other_value_attr_${attribute.ID}'].value, 'attr_mult_${attribute.ID}','multiselect_div_attr_${attribute.ID}')"/>
                                <div id="multiselect_div_attr_${attribute.ID}" class="multiselect" style="height:7.5em;width:25em;">
                                    <c:forEach items="${actionBean.possibleAttributeValues[attribute.ID]}" var="possibleValue">
                                        <c:set var="inputCheckedString" value="${ddfn:inputCheckedString(ddfn:contains(attribute.values,possibleValue))}"/>
                                     <label style="display:block">
                                         <input type="checkbox" name="attr_mult_${attribute.ID}" value="${fn:escapeXml(possibleValue)}" ${inputCheckedString} style="margin-right:5px"/><c:out value="${possibleValue}"/>
                                     </label>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <tr>
                    <th>&nbsp;</th>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>
                        <stripes:submit name="save" value="Save" class="mediumbuttonb"/>
                        <stripes:submit name="saveAndClose" value="Save & close" class="mediumbuttonb"/>
                        <stripes:submit name="cancel" value="Cancel" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </div>
    </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>