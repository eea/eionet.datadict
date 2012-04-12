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
                <li><a href="${pageContext.request.contextPath}/searchSchemaSets.action">Search schema sets</a></li>
            </ul>
        </div>
        
        <h1>Schema sets</h1>

        <c:if test="${empty actionBean.schemaSets}">
            <div style="margin-top:1em">
                No schema sets found!<br/>
                Please note that unauthenticated users can only see schema sets in released status.
            </div>
        </c:if>

        <stripes:form id="schemaSetsForm" action="/schemaSets.action" method="post" style="margin-top:1em">
            <ul class="menu">
                <c:forEach var="item" items="${actionBean.schemaSets}">
                    <li>
                    <c:if test="${actionBean.deletePermission}">
                        <stripes:checkbox name="selected" value="${item.id}" />
                    </c:if>
                    <stripes:link href="/schemaSet.action" class="link-folder">
                        <stripes:param name="schemaSet.id" value="${item.id}" />
                        <c:out value="${item.identifier}" />
                    </stripes:link>
                    </li>
                </c:forEach>
            </ul>
            <br />
            <c:if test="${actionBean.deletePermission && not empty actionBean.schemaSets}">
                <stripes:submit name="delete" value="Delete" />
                <input type="button" onclick="toggleSelectAll('schemaSetsForm');return false" value="Select all" name="selectAll">
            </c:if>

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>