<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schema sets" currentSection="schemas">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            (function($) {
                $(document).ready(function() {
                    $('#newNameDialog').dialog({
                        autoOpen: ${actionBean.askNewName},
                        width: 500
                    });

                    $("#closeNewNameDialog").click(function() {
                        $('#newNameDialog').dialog('close');
                        return true;
                    });

                    applySearchToggle("searchResultsForm");
                });
            })(jQuery);
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <h1>Search schemas</h1>

        <div id="drop-operations">
            <ul>
                <li class="search open"><a class="searchSection" href="#" title="Search schemas">Search</a></li>
                <c:if test="${ddfn:userHasPermission(actionBean.userName, '/schemas', 'i')}">
                    <li class="add"><stripes:link beanclass="eionet.web.action.SchemaActionBean" event="add">Add root-level schema</stripes:link></li>
                </c:if>
                <li class="search"><a href="${pageContext.request.contextPath}/schemasets/search/">Search schema sets</a></li>
            </ul>
        </div>

        <stripes:form id="searchResultsForm" action="/schema/search/" method="get">
            <div id="filters">
                <table class="filter">
                    <stripes:hidden name="schemaSetId" />
                    <tr>
                        <td class="label">
                            <label for="name">Schema file name</label>
                        </td>
                        <td class="input">
                            <stripes:text id="name" name="searchFilter.fileName" />
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label for="schemaSetIdentifier">Schema set identifer</label>
                        </td>
                        <td class="input">
                            <stripes:text id="schemaSetIdentifier" name="searchFilter.schemaSetIdentifier" />
                            <span style="font-size:0.8em"><sup>(Not relevant for root-level schemas!)</sup></span>
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
                                </stripes:select><br/>
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

        <stripes:form action="/schema/search/" method="get">
            <display:table name="actionBean.schemasResult" class="datatable results" id="item" requestURI="/schema/search/">
                <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No schemas found.</p>" />
                <c:if test="${actionBean.schemaSetId != 0}">
                    <display:column title="" sortable="false">
                        <stripes:radio value="${item.id}" name="schemaId" />
                    </display:column>
                </c:if>
                <display:column title="Name" sortable="true" sortName="sortName" sortProperty="NAME_ATTR">
                    <stripes:link beanclass="eionet.web.action.SchemaActionBean">
                        <c:if test="${item.schemaSetId > 0}">
                            <stripes:param name="schemaSet.identifier" value="${item.schemaSetIdentifier}" />
                        </c:if>
                        <stripes:param name="schema.fileName" value="${item.fileName}" />
                        <c:if test="${item.workingCopy || item.schemaSetWorkingCopy}"><stripes:param name="workingCopy" value="true"/></c:if>
                        <c:choose>
                            <c:when test="${not empty item.nameAttribute}">
                                <c:out value="${item.nameAttribute}" />
                            </c:when>
                            <c:otherwise>
                                <c:out value="${item.fileName}" />
                            </c:otherwise>
                        </c:choose>
                    </stripes:link>
                    <c:if test="${not empty actionBean.userName && item.workingCopy && actionBean.userName==item.workingUser}">
                        <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                    </c:if>
                </display:column>
                <display:column title="Schema set name" sortable="true" sortProperty="SS_NAME_ATTR">
                    <c:if test="${item.schemaSetId > 0}">
                        <stripes:link beanclass="eionet.web.action.SchemaSetActionBean">
                            <stripes:param name="schemaSet.identifier" value="${item.schemaSetIdentifier}" />
                            <c:if test="${item.schemaSetWorkingCopy}"><stripes:param name="workingCopy" value="true"/></c:if>
                            <c:choose>
                                <c:when test="${not empty item.schemaSetNameAttribute}">
                                    <c:out value="${item.schemaSetNameAttribute}" />
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${item.schemaSetIdentifier}" />
                                </c:otherwise>
                            </c:choose>
                        </stripes:link>
                        <c:if test="${not empty actionBean.userName && item.schemaSetWorkingCopy && actionBean.userName==item.schemaSetWorkingUser}">
                            <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                        </c:if>
                    </c:if>
                </display:column>
                <c:if test="${actionBean.schemaSetId > 0}">
                    <display:footer>
                        <tr>
                            <td></td>
                            <td colspan="2">
                                <stripes:hidden name="schemaSetId" value="${actionBean.schemaSetId}" />
                                <stripes:submit name="copyToSchemaSet" value="Copy to schema set" />
                                <stripes:submit name="cancelCopy" value="Cancel" />
                            </td>
                        </tr>
                    </display:footer>
                </c:if>
            </display:table>
        </stripes:form>

        <div id="newNameDialog" title="New file name">
            In the schema set, there is already schema with such name.
            <stripes:form beanclass="${actionBean['class'].name}" method="get">
                <stripes:hidden name="schemaSetId" />
                <stripes:hidden name="schemaId" />

                <label for="fileToUpload">New schema file name*:</label>
                <stripes:text name="newSchemaName" />
                <br/><br/>
                <stripes:submit name="copyToSchemaSet" value="Copy"/>
                <input type="button" id="closeNewNameDialog" value="Cancel"/>
            </stripes:form>
        </div>
    </stripes:layout-component>

</stripes:layout-render>