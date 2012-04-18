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
                <li><a href="${pageContext.request.contextPath}/schemaSet.action?add=">Add schema set</a></li>
                <li><a href="${pageContext.request.contextPath}/searchSchemaSets.action">Search schema sets</a></li>
                <c:if test="${not empty actionBean.user}">
                    <stripes:link beanclass="${actionBean.class.name}">List my working copies
                        <stripes:param name="workingCopy" value="true"/>
                    </stripes:link>
                </c:if>
            </ul>
        </div>
        
        <h1>Schema sets</h1>

        <c:if test="${empty actionBean.schemaSets}">
            <div style="margin-top:1em">
                No schema sets found!
                <c:if test="${empty actionBean.user}">
	                <br/>
	                Please note that unauthenticated users can only see schema sets in released status.
                </c:if>
            </div>
        </c:if>

        <stripes:form id="schemaSetsForm" action="/schemaSets.action" method="post" style="margin-top:1em">
            <ul class="menu">
                <c:forEach var="item" items="${actionBean.schemaSets}">
                    <li>
                        <c:if test="${not empty actionBean.user}">
	                        <c:choose>
	                            <c:when test="${ddfn:contains(actionBean.deletable,item.id)}">
	                                <stripes:checkbox name="selected" value="${item.id}" />
	                            </c:when>
	                            <c:otherwise>
	                                <input type="checkbox" disabled="disabled" title="Schema set in registered status or currently checked out"/>
	                            </c:otherwise>
	                        </c:choose>
                        </c:if>
	                    <stripes:link href="/schemaSet.action" class="link-folder">
	                        <stripes:param name="schemaSet.id" value="${item.id}" />
	                        <c:out value="${item.identifier}"/>
	                    </stripes:link>
	                    <c:if test="${not empty item.workingUser}">
                            <span title="Checked out by ${item.workingUser}" class="checkedout"><strong>*</strong></span>
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
            <br/>
            
            <c:if test="${not empty actionBean.user && not empty actionBean.schemaSets}">
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
            
        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>