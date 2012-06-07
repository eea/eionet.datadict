<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <c:if test="${ddfn:userHasPermission(actionBean.userName, '/schemasets', 'i')}">
            <div id="drop-operations">
                <h2>Operations:</h2>
                <ul>
                    <li>
                        <stripes:link beanclass="eionet.web.action.SchemaSetActionBean" event="add">Add schema set</stripes:link>
                    </li>
                </ul>
            </div>
        </c:if>

        <h1>Search schema sets</h1>

        <stripes:form id="searchResultsForm" action="/schemasets/search/" method="get">
            <div style="margin-top:1em">
                <label class="question" style="width:16%;float:left;" for="identifier">Identifier:</label>
                <stripes:text id="identifier" name="searchFilter.identifier" />
                <br/>
                <c:if test="${not empty actionBean.userName}">
                    <label class="question" style="width:16%;float:left;" for="regStatus">Registration status:</label>
                    <stripes:select id="regStatus" name="searchFilter.regStatus" disabled="${not actionBean.authenticated}">
                        <stripes:options-collection collection="${actionBean.regStatuses}" />
                    </stripes:select>
                    <br/>
                </c:if>
                <c:forEach var="attr" items="${actionBean.searchFilter.attributes}" varStatus="row">
                    <label class="question" style="width:16%;float:left;" for="attr${row.index}">
                        <c:out value="${attr.shortName}" />:
                    </label>
                    <stripes:text id="attr${row.index}" name="searchFilter.attributes[${row.index}].value" />
                    <br/>
                    <stripes:hidden name="searchFilter.attributes[${row.index}].id" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].name" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].shortName" />
                </c:forEach>
                <span style="width:16%;float:left;padding-top:0.2em">&nbsp;</span><stripes:submit name="search" value="Search"/>
            </div>

            <br />

            <display:table name="actionBean.schemaSetsResult" class="sortable" id="item" requestURI="/schemasets/search/">
                <display:column title="Identifier" sortable="true" sortName="sortName" sortProperty="identifier">
                    <stripes:link beanclass="eionet.web.action.SchemaSetActionBean">
                        <stripes:param name="schemaSet.identifier" value="${item.identifier}" />
                        <c:if test="${item.workingCopy}"><stripes:param name="workingCopy" value="true"/></c:if>
                        <c:out value="${item.identifier}" />
                    </stripes:link>
                    <c:if test="${not empty actionBean.userName && item.workingCopy && actionBean.userName==item.workingUser}">
                        <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                    </c:if>
                </display:column>
                <display:column title="Reg. status" sortable="true" sortProperty="reg_status">
                    <c:out value="${item.regStatus}" />
                </display:column>
            </display:table>
        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>