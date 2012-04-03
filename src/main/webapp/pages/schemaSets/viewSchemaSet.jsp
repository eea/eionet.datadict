<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

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
                            <a href="/help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                                <img style="border:0" src="<%=request.getContextPath()%>/images/info_icon.gif" width="16" height="16" alt="help"/>
                            </a>
                        </td>
                        <td class="simple_attr_help">
                            <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
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
                            <a href="/help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                                <img style="border:0" src="<%=request.getContextPath()%>/images/info_icon.gif" width="16" height="16" alt="help"/>
                            </a>
                        </td>
                        <td class="simple_attr_help">
                            <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                        </td>
                        <td class="simple_attr_value">
                            <c:out value="${actionBean.schemaSet.regStatus}"/>
                            <c:if test="${actionBean.userWorkingCopy}">
                                <span class="caution" title="Checked out on ${actionBean.schemaSet.date}">(Working copy)</span>
                            </c:if>
                            <c:if test="${not empty actionBean.userName && actionBean.userName!=actionBean.schemaSet.workingUser}">
                                <span class="caution">(checked out by <em>${actionBean.schemaSet.workingUser}</em>)</span>
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <th>&nbsp;</th>
                        <td colspan="3">
                            <stripes:submit name="add" value="Add" class="mediumbuttonb"/>
                        </td>
                    </tr>
                </table>
            </div>
        </stripes:form>
    </div>
    </stripes:layout-component>

</stripes:layout-render>