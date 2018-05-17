<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schema sets" currentSection="schemas">

    <stripes:layout-component name="head">
        <script type="text/javascript">
            // <![CDATA[
            (function($) {
                $(document).ready(function() {
                    applySearchToggle("searchResultsForm");
                });
            })(jQuery);
            // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <h1>Search schema sets</h1>

        <div id="drop-operations">
            <ul>
                <li class="search open"><a class="searchSection" href="#" title="Search schema sets">Search</a></li>
                <c:if test="${ddfn:userHasPermission(actionBean.userName, '/schemasets', 'i')}">
                    <li class="add">
                        <stripes:link beanclass="eionet.web.action.SchemaSetActionBean" event="add">Add schema set</stripes:link>
                    </li>
                    <li class="search"><a href="${pageContext.request.contextPath}/schema/search/">Search schemas</a></li>
                </c:if>
            </ul>
        </div>

        <stripes:form id="searchResultsForm" action="/schemasets/search/" method="get">
            <div id="filters">
                <table class="filter">
                    <tr>
                        <td class="label">
                            <label for="identifier">Identifier</label>
                        </td>
                        <td class="input">
                            <stripes:text id="identifier" name="searchFilter.identifier" />
                        </td>
                    </tr>
                    <c:if test="${not empty actionBean.userName}">
                        <tr>
                            <td class="label">
                                <label for="regStatus">Registration status</label>
                            </td>
                            <td class="input">
                                <stripes:select id="regStatus" name="searchFilter.regStatus" disabled="${not actionBean.authenticated}">
                                    <stripes:options-collection collection="${actionBean.regStatuses}" />
                                </stripes:select>
                            </td>
                        </tr>
                    </c:if>
                    <c:forEach var="attr" items="${actionBean.searchFilter.attributes}" varStatus="row">
                        <tr>
                            <td class="label">
                                <label for="attr${row.index}">
                                    <c:out value="${attr.shortName}" />
                                </label>
                            </td>
                            <td class="input">
                                <stripes:text id="attr${row.index}" name="searchFilter.attributes[${row.index}].value" />
                                <stripes:hidden name="searchFilter.attributes[${row.index}].id" />
                                <stripes:hidden name="searchFilter.attributes[${row.index}].name" />
                                <stripes:hidden name="searchFilter.attributes[${row.index}].shortName" />
                            </td>
                        </tr>
                    </c:forEach>
                    <tr>
                        <td class="label">
                            <label for="pageSize">Page size</label>
                        </td>
                        <td class="input">
                            <stripes:select id="pageSize" name="searchFilter.pageSize">
                                <stripes:options-collection collection="${actionBean.searchFilter.possibleResultsPerPage}" />
                            </stripes:select>
                        </td>
                    </tr>
                </table>
                <p class="actions">
                    <stripes:submit class="mediumbuttonb searchButton" name="search" value="Search"/>
                    <input class="mediumbuttonb" type="reset" value="Reset" />
                </p>
            </div>
        </stripes:form>

        <display:table name="actionBean.schemaSetsResult" class="datatable results" id="item" requestURI="/schemasets/search/">
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No schema sets found.</p>" />
            <display:column title="Name" sortable="true" sortName="sortName" sortProperty="NAME_ATTR">
                <stripes:link beanclass="eionet.web.action.SchemaSetActionBean">
                    <stripes:param name="schemaSet.identifier" value="${item.identifier}" />
                    <c:if test="${item.workingCopy}"><stripes:param name="workingCopy" value="true"/></c:if>
                    <c:choose>
                        <c:when test="${not empty item.nameAttribute}">
                            <c:out value="${item.nameAttribute}" />
                        </c:when>
                        <c:otherwise>
                            <c:out value="${item.identifier}" />
                        </c:otherwise>
                    </c:choose>
                </stripes:link>
                <c:if test="${not empty actionBean.userName && item.workingCopy && actionBean.userName==item.workingUser}">
                    <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                </c:if>
            </display:column>
            <display:column title="Reg. status" sortable="true" sortProperty="reg_status">
                <c:out value="${item.regStatus}" />
            </display:column>
        </display:table>

    </stripes:layout-component>

</stripes:layout-render>