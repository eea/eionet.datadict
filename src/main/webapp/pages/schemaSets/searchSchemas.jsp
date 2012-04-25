<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="${pageContext.request.contextPath}/schemaSet.action?add=">Add schema set</a></li>
            </ul>
        </div>

        <h1>Search schema sets</h1>

        <stripes:form id="searchResultsForm" action="/searchSchemas.action" method="get">
            <div style="margin-top:1em">
                <fieldset style="border: none">
                    <label class="question" style="width:16%;float:left;padding-top:0.2em" for="name">Schema file name:</label>
                    <stripes:text id="name" name="searchFilter.fileName" />
                </fieldset>
                <fieldset style="border: none">
                    <label class="question" style="width:16%;float:left;padding-top:0.2em" for="schemaSetIdentifier">Schema set identifer:</label>
                    <stripes:text id="schemaSetIdentifier" name="searchFilter.schemaSetIdentifier" />
                </fieldset>
                <c:forEach var="attr" items="${actionBean.searchFilter.attributes}" varStatus="row">
                    <fieldset style="border: none">
                        <label class="question" style="width:16%;float:left;padding-top:0.2em" for="attr${row.index}">
                            <c:out value="${attr.name}" />:
                        </label>
                        <stripes:text id="attr${row.index}" name="searchFilter.attributes[${row.index}].value" />
                        <stripes:hidden name="searchFilter.attributes[${row.index}].id" />
                        <stripes:hidden name="searchFilter.attributes[${row.index}].name" />
                    </fieldset>
                </c:forEach>
                <stripes:submit name="search" value="Search"/>
            </div>

            <br />

            <display:table name="actionBean.schemasResult" class="sortable" id="item" requestURI="/searchSchemas.action">

                <c:if test="${not empty actionBean.user}">
                    <display:column title="" sortable="false">
                        <stripes:checkbox name="selected" value="${item.id}" />
                    </display:column>
                </c:if>

                <display:column title="File name" sortable="true" sortName="sortName" sortProperty="fileName">
                    <stripes:link href="#">
                        <stripes:param name="schema.id" value="${item.id}" />
                        <c:out value="${item.fileName}" />
                    </stripes:link>
                </display:column>
                <display:column title="Schema set identifier" sortable="true" sortProperty="schemaSetIdentifier">
                    <stripes:link href="/schemaSet.action">
                        <stripes:param name="schemaSet.id" value="${item.schemaSetId}" />
                        <c:out value="${item.schemaSetIdentifier}" />
                    </stripes:link>
                </display:column>
            </display:table>

            <c:if test="${not empty actionBean.user && not empty actionBean.schemasResult.list}">
                <stripes:submit name="delete" value="Delete"/>
                <input type="button" onclick="toggleSelectAll('searchResultsForm');return false" value="Select all" name="selectAll" />
            </c:if>

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>