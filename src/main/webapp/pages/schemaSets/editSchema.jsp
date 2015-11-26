<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="eionet.meta.dao.domain.RegStatus"%>
<%@page import="eionet.meta.DElemAttribute"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Edit schema set">

    <stripes:layout-component name="contents">
        
    <script type="text/javascript" src="<%=request.getContextPath()%>/helpPopup.js"></script>
    <link type="text/css" href="<c:url value="/css/smoothness/jquery-ui-1.8.16.custom.css" />" rel="stylesheet" />
    <script type="text/javascript" src="<%=request.getContextPath()%>/scripts/jquery-1.6.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/scripts/jquery-ui-1.8.16.custom.min.js"></script>

    <h1>Edit schema</h1>

    <stripes:form id="form1" method="post" beanclass="${actionBean.class.name}" style="padding-top:20px">
        <%-- Using url parameters because schemaAction bean has default value for one parameter --%>
        <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}" />
        <stripes:param name="schema.fileName" value="${actionBean.schema.fileName}" />

        <stripes:hidden name="schemaSet.identifier" />
        <stripes:hidden name="schema.fileName" />
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
                        File name
                    </th>
                    <td class="simple_attr_help">
                        <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=identifier">
                            <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="help"/>
                        </a>
                    </td>
                    <td class="simple_attr_help">
                        <img style="border:0" src="${pageContext.request.contextPath}/images/mandatory.gif" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <a href="${actionBean.schemaDownloadLink}">
                            <c:out value="${actionBean.schema.fileName}"/>
                        </a>
                        <stripes:hidden name="schema.fileName"/>
                    </td>
                </tr>
                <c:if test="${!actionBean.rootLevelSchema}">
                    <th scope="row" class="scope-row simple_attr_title">
                        Schema set
                    </th>
                    <td class="simple_attr_help">
                        <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=identifier">
                            <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="help"/>
                        </a>
                    </td>
                    <td class="simple_attr_help">
                        <img style="border:0" src="${pageContext.request.contextPath}/images/mandatory.gif" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <stripes:link beanclass="eionet.web.action.SchemaSetActionBean" title="Open schema set details">
                            <stripes:param name="schemaSet.id" value="${actionBean.schema.schemaSetId}"/>
                            <c:out value="${actionBean.schemaSet.identifier}"/>
                        </stripes:link>
                    </td>
                </c:if>
                <c:if test="${actionBean.rootLevelSchema}">
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Registration status
                        </th>
                        <td class="simple_attr_help">
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=regstatus">
                                <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="help"/>
                            </a>
                        </td>
                        <td class="simple_attr_help">
                            <img style="border:0" src="${pageContext.request.contextPath}/images/mandatory.gif" width="16" height="16" alt=""/>
                        </td>
                        <td class="simple_attr_value">
                            <c:set var="regStatuses" value="<%=RegStatus.values()%>"/>
                            <stripes:select name="schema.regStatus" value="${actionBean.schema.regStatus}">
                                <c:forEach items="${regStatuses}" var="aRegStatus">
                                    <stripes:option value="${aRegStatus}" label="${aRegStatus}"/>
                                </c:forEach>
                            </stripes:select>
                        </td>
                    </tr>
                </c:if>
                <c:forEach items="${actionBean.attributes}" var="attributesEntry">
                    <c:set var="attribute" value="${attributesEntry.value}"/>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            <c:out value="${attribute.name}"/>
                        </th>
                        <td class="simple_attr_help">
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?attrid=${attribute.ID}&amp;attrtype=SIMPLE">
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
                                        <a href="${actionBean.contextPath}/imgattr.jsp?obj_id=${actionBean.schema.id}&amp;obj_type=<%=DElemAttribute.ParentType.SCHEMA%>&amp;attr_id=${attribute.ID}&amp;obj_name=${actionBean.schema.identifier}&amp;attr_name=${attribute.shortName}">
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
                </c:forEach>
                <tr>
                    <th>&nbsp;</th>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>
                        <stripes:submit name="save" value="Save" class="mediumbuttonb"/>
                        <stripes:submit name="saveAndClose" value="Save & close" class="mediumbuttonb"/>
                        <stripes:submit name="cancelEdit" value="Cancel" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </div>
        <div style="display:none">
            <stripes:hidden name="schema.id"/>
        </div>
    </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>