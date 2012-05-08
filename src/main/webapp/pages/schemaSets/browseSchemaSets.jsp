<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="eionet.meta.dao.domain.SchemaSet"%>
<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <c:if test="${ddfn:userHasPermission(actionBean.userName, '/schemasets', 'i')}">
                    <li><a href="${pageContext.request.contextPath}/schemaSet.action?add=">Add schema set</a></li>
                    <li><a href="${pageContext.request.contextPath}/schema.action?add=">Add root-level schema</a></li>
                </c:if>
                <li><a href="${pageContext.request.contextPath}/searchSchemaSets.action">Search schema sets</a></li>
                <li><a href="${pageContext.request.contextPath}/searchSchemas.action">Search schemas</a></li>
                <c:if test="${not empty actionBean.user}">
                    <stripes:link beanclass="${actionBean.class.name}" event="workingCopies">
                        List my working copies
                    </stripes:link>
                </c:if>
            </ul>
        </div>

        <h1>Browse schema sets and schemas</h1>

        <c:if test="${empty actionBean.schemaSets}">
            <div style="margin-top:1em">
                No schema sets found!
                <c:if test="${empty actionBean.user}">
                    <br/>
                    Please note that unauthenticated users can only see schema sets in released status.
                </c:if>
            </div>
        </c:if>

        <c:if test="${not empty actionBean.user}">
	        <div class="advice-msg" style="margin-top:1em;font-size:0.8em">
	            Hint: this page does not list working copies. Use Operations menu or "Your checkouts" page
	            to list your working copies.
	        </div>
        </c:if>

        <stripes:form id="schemaSetsForm" action="/schemaSets.action" method="post" style="margin-top:1em">
            <ul class="menu">
                <c:forEach var="schemaSet" items="${actionBean.schemaSets}">
                    <li>
                        <c:if test="${not empty actionBean.user}">
                            <c:choose>
                                <c:when test="${ddfn:contains(actionBean.deletableSchemaSets,schemaSet.id)}">
                                    <stripes:checkbox name="selectedSchemaSets" value="${schemaSet.id}" />
                                </c:when>
                                <c:otherwise>
                                    <input type="checkbox" disabled="disabled" title="Schema set in registered status or currently checked out"/>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <stripes:link href="/schemaSet.action" class="link-folder">
                            <stripes:param name="schemaSet.id" value="${schemaSet.id}" />
                            <c:out value="${schemaSet.identifier}"/>
                        </stripes:link>
                        <c:if test="${not empty actionBean.user && not empty schemaSet.workingUser}">
                            <span title="Checked out by ${schemaSet.workingUser}" class="checkedout"><strong>*</strong></span>
                        </c:if>
                    </li>
                </c:forEach>
                <c:forEach var="schema" items="${actionBean.schemas}">
                    <li>
                        <c:if test="${not empty actionBean.user}">
                            <c:choose>
                                <c:when test="${ddfn:contains(actionBean.deletableSchemas,schema.id)}">
                                    <stripes:checkbox name="selectedSchemas" value="${schema.id}" />
                                </c:when>
                                <c:otherwise>
                                    <input type="checkbox" disabled="disabled" title="Schema in registered status or currently checked out"/>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <stripes:link href="/schema.action">
                            <stripes:param name="schema.id" value="${schema.id}" />
                            <c:out value="${schema.fileName}"/>
                        </stripes:link>
                        <c:if test="${not empty actionBean.user && not empty schema.workingUser}">
                            <span title="Checked out by ${schema.workingUser}" class="checkedout"><strong>*</strong></span>
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
            <br/>

            <c:if test="${not empty actionBean.user && (not empty actionBean.schemaSets || not empty actionBean.schemas)}">
                <c:choose>
                    <c:when test="${not empty actionBean.deletableSchemaSets || not empty actionBean.deletableSchemas}">
                        <stripes:submit name="delete" value="Delete"/>
                        <input type="button" onclick="toggleSelectAll('schemaSetsForm');return false" value="Select all" name="selectAll" />
                    </c:when>
                    <c:otherwise>
                        <stripes:submit disabled="disabled" name="delete" value="Delete"/>
                        <input disabled="disabled" type="button" onclick="toggleSelectAll('schemaSetsForm');return false" value="Select all" name="selectAll" />
                    </c:otherwise>
                </c:choose>
            </c:if>

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>