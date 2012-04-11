<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search tables">

    <stripes:layout-component name="contents">
    
    <c:if test="${not empty actionBean.dropdownOperations}">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <c:forEach items="${actionBean.dropdownOperations}" var="dropdownOperation">
                    <li><a href="${dropdownOperation.href}"><c:out value="${dropdownOperation.title}"/></a></li>
                </c:forEach>
            </ul>
        </div>
    </c:if>
    
    <h1>View schema set</h1>

    <stripes:form method="post" beanclass="eionet.web.action.SchemaSetActionBean">
        <div id="outerframe">
            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
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
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.schemaSet.identifier}"/>
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
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.schemaSet.regStatus}"/>
                        <c:if test="${actionBean.userWorkingCopy}">
                            <span class="caution" title="Checked out on ${actionBean.schemaSet.dateModified}">(Working copy)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && not empty actionBean.schemaSet.workingUser && actionBean.userName!=actionBean.schemaSet.workingUser}">
                            <span class="caution">(checked out by <em>${actionBean.schemaSet.workingUser}</em>)</span>
                        </c:if>
                    </td>
                </tr>
                <c:forEach items="${actionBean.attributes}" var="attribute">
                    <c:if test="${not empty attribute.value}">
	                    <tr>
	                        <th scope="row" class="scope-row simple_attr_title">
	                            <c:out value="${attribute.shortName}"/>
	                        </th>
	                        <td class="simple_attr_help">
	                            <a href="${pageContext.request.contextPath}/help.jsp?attrid=${attribute.ID}&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
	                                <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="Help"/>
	                            </a>
	                        </td>
	                        <td style="word-wrap:break-word;wrap-option:emergency" class="simple_attr_value">
	                            <c:if test="${not attribute.displayMultiple}">
	                                <c:out value="${attribute.value}"/>
	                            </c:if>
	                            <c:if test="${attribute.displayMultiple}">
	                                <c:out value="${ddfn:join(attribute.values, ', ')}"/>
	                            </c:if>
	                        </td>
                        </tr>
                    </c:if>                            
                </c:forEach>
            </table>
        </div>
    </stripes:form>
    </stripes:layout-component>

</stripes:layout-render>