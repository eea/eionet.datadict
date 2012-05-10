<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="${pageContext.request.contextPath}/schemaSet.action?add=">Add schema set</a></li>
            </ul>
        </div>

        <h1>Search schema sets</h1>

        <stripes:form id="searchResultsForm" action="/searchSchemaSets.action" method="get">
            <div style="margin-top:1em">
                <label class="question" style="width:16%;float:left;padding-top:0.2em" for="identifier">Identifier:</label>
                <stripes:text id="identifier" name="searchFilter.identifier" />
                <br/>
                <label class="question" style="width:16%;float:left;padding-top:0.2em" for="regStatus">Registration status:</label>
                <stripes:select id="regStatus" name="searchFilter.regStatus" disabled="${not actionBean.authenticated}">
                    <stripes:options-collection collection="${actionBean.regStatuses}" />
                </stripes:select>
                <c:forEach var="attr" items="${actionBean.searchFilter.attributes}" varStatus="row">
                    <br/>
                    <label class="question" style="width:16%;float:left;padding-top:0.2em" for="attr${row.index}">
                        <c:out value="${attr.shortName}" />:
                    </label>
                    <stripes:text id="attr${row.index}" name="searchFilter.attributes[${row.index}].value" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].id" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].name" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].shortName" />
                </c:forEach>
                <br/>
                <span style="width:16%;float:left;padding-top:0.2em">&nbsp;</span><stripes:submit name="search" value="Search"/>
            </div>

            <br />

            <display:table name="actionBean.schemaSetsResult" class="sortable" id="item" requestURI="/searchSchemaSets.action">
                <%--
                <c:if test="${not empty actionBean.user}">
                    <display:column title="" sortable="true" sortName="sortName" sortProperty="identifier">
                        <c:choose>
                            <c:when test="${ddfn:contains(actionBean.deletable,item.id)}">
                                <stripes:checkbox name="selected" value="${item.id}" />
                            </c:when>
                            <c:otherwise>
                                <input type="checkbox" disabled="disabled" title="Schema set in registered status or currently checked out"/>
                            </c:otherwise>
                    </c:choose>
                    </display:column>
                </c:if>
                --%>
                <display:column title="Identifier" sortable="true" sortName="sortName" sortProperty="identifier">
                    <stripes:link href="/schemaSet.action">
                        <stripes:param name="schemaSet.id" value="${item.id}" />
                        <c:out value="${item.identifier}" />
                    </stripes:link>
                    <c:if test="${not empty item.workingUser}">
                        <span title="Checked out by ${item.workingUser}" class="checkedout"><strong>*</strong></span>
                    </c:if>
                </display:column>
                <display:column title="Reg. status" sortable="true" sortProperty="reg_status">
                    <c:out value="${item.regStatus}" />
                </display:column>
            </display:table>
            <%--
            <c:if test="${not empty actionBean.user && not empty actionBean.schemaSetsResult.list}">
                <c:choose>
                    <c:when test="${not empty actionBean.deletable}">
                        <stripes:submit name="delete" value="Delete"/>
                        <input type="button" onclick="toggleSelectAll('schemaSetsForm');return false" value="Select all" name="selectAll" />
                    </c:when>
                    <c:otherwise>
                        <stripes:submit disabled="disabled" name="delete" value="Delete"/>
                        <input disabled="disabled" type="button" onclick="toggleSelectAll('schemaSetsForm');return false" value="Select all" name="selectAll" />
                    </c:otherwise>
                </c:choose>
            </c:if>
            --%>
        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>