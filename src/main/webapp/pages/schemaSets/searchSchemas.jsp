<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="${pageContext.request.contextPath}/schema.action?add=">Add root-level schema</a></li>
            </ul>
        </div>

        <h1>Search schemas</h1>

        <stripes:form id="searchResultsForm" action="/searchSchemas.action" method="get">
            <div style="margin-top:1em">
                <label class="question" style="width:18%;float:left;padding-top:0.2em" for="name">Schema file name:</label>
                <stripes:text id="name" name="searchFilter.fileName" />
                <br/>
                <label class="question" style="width:18%;float:left;padding-top:0.2em" for="schemaSetIdentifier">Schema set identifer:</label>
                <stripes:text id="schemaSetIdentifier" name="searchFilter.schemaSetIdentifier" />
                <span style="font-size:0.8em"><sup>(Leave empty for root-level schemas!)</sup></span>
                <br/>
                <label class="question" style="width:18%;float:left;padding-top:0.2em" for="regStatus">Registration status:</label>
                <stripes:select id="regStatus" name="searchFilter.regStatus" disabled="${not actionBean.authenticated}">
                    <stripes:options-collection collection="${actionBean.regStatuses}" />
                </stripes:select>
                <span style="font-size:0.8em"><sup>(Specify only for root-level schemas!)</sup></span>
                <c:forEach var="attr" items="${actionBean.searchFilter.attributes}" varStatus="row">
                    <br/>
                    <label class="question" style="width:18%;float:left;padding-top:0.2em" for="attr${row.index}">
                        <c:out value="${attr.shortName}" />:
                    </label>
                    <stripes:text id="attr${row.index}" name="searchFilter.attributes[${row.index}].value" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].id" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].name" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].shortName" />
                </c:forEach>
                <br/>
                <span style="width:18%;float:left;padding-top:0.2em">&nbsp;</span><stripes:submit name="search" value="Search"/>
            </div>

            <br />

            <display:table name="actionBean.schemasResult" class="sortable" id="item" requestURI="/searchSchemas.action">

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

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>