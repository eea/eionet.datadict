<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Tables" helpScreen="tables" currentSection="tables">

    <stripes:layout-component name="head">
        <script type="text/javascript">
            // <![CDATA[
            (function($) {
                $(document).ready(function() {
                    applySearchToggle("searchTablesForm");
                });
            })(jQuery);
            // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <h1>Tables from latest versions of datasets in any status</h1>
        <c:if test="${empty actionBean.user}">
            <p class="advise-msg">
                Note: Tables from datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.
            </p>
        </c:if>
        <div id="drop-operations">
            <ul>
                <li class="search open"><a class="searchSection" href="#" title="Search tables">Search</a></li>
            </ul>
        </div>

        <stripes:form method="get" id="searchTablesForm" beanclass="${actionBean['class'].name}">
            <div id="filters">
                <table class="filter">
                    <tr>
                        <td class="label">
                            <label for="shortName">Short name</label>
                            <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name"></a>
                        </td>
                        <td class="input">
                            <stripes:text id="shortName" name="tableFilter.shortName" class="smalltext" size="59" />
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label for="identifier">Identifier</label>
                            <a class="helpButton" href="help.jsp?screen=dataset&amp;area=identifier"></a>
                        </td>
                        <td class="input">
                            <stripes:text id="identifier" name="tableFilter.identifier" class="smalltext" size="59" />
                        </td>
                    </tr>
                    <c:forEach var="attr" items="${actionBean.tableFilter.attributes}" varStatus="row">
                        <tr>
                            <td class="label">
                                <label for="attr${row.index}">${fn:escapeXml(attr.name)}</label>
                                <a class="helpButton" href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE"></a>
                            </td>
                            <td class="input">
                                <stripes:text id="attr${row.index}" name="tableFilter.attributes[${row.index}].value" class="smalltext" size="59" />
                                <stripes:hidden name="tableFilter.attributes[${row.index}].id" />
                                <stripes:hidden name="tableFilter.attributes[${row.index}].name" />
                            </td>
                        </tr>
                    </c:forEach>
                </table>
                <p class="actions">
                    <stripes:submit name="search" value="Search" class="mediumbuttonb searchButton" />
                    <input class="mediumbuttonb" type="reset" value="Reset" />
                </p>
            </div>
        </stripes:form>

        <display:table name="${actionBean.tableResult}" class="datatable results" id="item"
            decorator="eionet.web.decorators.TableResultDecorator" requestURI="/searchtables">
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No tables found.</p>" />
            <display:column title="Full name" sortable="true" sortProperty="NAME">
                <c:choose>
                    <c:when test="${item.statusReleased || actionBean.userLoggedIn}">
                        <stripes:link href="/tables/${item.id}"><c:out value="${item.name}" /></stripes:link>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${item.name}" />
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column property="shortName" title="Short name" sortable="true" sortProperty="SHORT_NAME" />
            <display:column property="dataSetName" title="Dataset" sortable="true" sortProperty="DATASET" />
            <display:column title="Dataset status" sortable="true" sortProperty="DATASET_STATUS">
                <dd:datasetRegStatus value="${item.dataSetStatus}" />
            </display:column>
        </display:table>

    </stripes:layout-component>

</stripes:layout-render>
