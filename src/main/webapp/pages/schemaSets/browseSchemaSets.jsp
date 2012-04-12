<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <h1>Schema sets</h1>

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="${pageContext.request.contextPath}/schemaSet.action?add=">Add schema set</a></li>
            </ul>
        </div>

        <stripes:form action="/schemaSets.action" method="post">
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
            </c:if>

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>