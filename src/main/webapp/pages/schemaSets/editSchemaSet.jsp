<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="eionet.meta.dao.domain.SchemaSet"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search tables">

    <stripes:layout-component name="contents">
    <h1>Schema set</h1>

    <div class="form">
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
                                <input type="text" name="attr_${attribute.ID}" value="${fn:escapeXml(attribute.value)}"/>
                            </td>
                        </tr>
                    </c:forEach>
                    <tr>
                        <th>&nbsp;</th>
                        <td colspan="3">
                            <stripes:submit name="save" value="Save" class="mediumbuttonb"/>
                            <stripes:submit name="saveAndClose" value="Save & close" class="mediumbuttonb"/>
                            <stripes:submit name="cancel" value="Cancel" class="mediumbuttonb"/>
                        </td>
                    </tr>
                </table>
            </div>
        </stripes:form>
    </div>
    </stripes:layout-component>

</stripes:layout-render>