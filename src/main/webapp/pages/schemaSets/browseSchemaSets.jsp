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
                    <li>
                        <stripes:link beanclass="eionet.web.action.SchemaSetActionBean" event="add">Add schema set</stripes:link>
                    </li>
                </c:if>
                <c:if test="${ddfn:userHasPermission(actionBean.userName, '/schemas', 'i')}">
                    <li>
                        <stripes:link beanclass="eionet.web.action.SchemaActionBean" event="add">Add root-level schema</stripes:link>
                    </li>
                </c:if>
                <li><a href="${pageContext.request.contextPath}/schemasets/search/">Search schema sets</a></li>
                <li><a href="${pageContext.request.contextPath}/schema/search/">Search schemas</a></li>
                <c:if test="${not empty actionBean.user}">
                    <stripes:link beanclass="${actionBean.class.name}" event="workingCopies">
                        List my working copies
                    </stripes:link>
                </c:if>
            </ul>
        </div>

        <h1>Browse schema sets and schemas</h1>

        <c:if test="${empty actionBean.schemaSets && empty actionBean.schemas}">
            <div style="margin-top:1em">
                <c:choose>
                    <c:when test="${actionBean.context.eventName=='workingCopies' && not empty actionBean.user}">
                        No working copies found!
                    </c:when>
                    <c:otherwise>
                        No schemas or schema sets found!
                    </c:otherwise>
                </c:choose>
                <c:if test="${empty actionBean.user}">
                    <br/>
                    Please note that unauthenticated users can only see schemas and schema sets in Released status.
                </c:if>
            </div>
        </c:if>

        <c:if test="${not empty actionBean.user && actionBean.context.eventName!='workingCopies'}">
            <div class="advice-msg" style="margin-top:1em;font-size:0.8em">
                Hint: red asterisk marks your working copies, if you have any.
            </div>
        </c:if>

        <stripes:form id="schemaSetsForm" action="/schemasets/browse/" method="post" style="margin-top:1em">
            <ul class="menu">
                <!-- SCHEMA SETS LOOP, first it iterates on non-deprecated ones and then deprecated ones -->
                <c:forEach begin="0" end ="1" varStatus="outerLoop">
                    <c:forEach var="schemaSet" items="${actionBean.schemaSets}">
                        <c:if test="${schemaSet.deprecatedStatus eq (outerLoop.index eq 1)}">
                            <c:set var="schemaSetName" value="${schemaSet.attributeValues!=null ? ddfn:join(schemaSet.attributeValues['Name'],'') : ''}"/>
                            <c:set var="displayName" value="${not empty schemaSetName ? schemaSetName : schemaSet.identifier}"/>
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
                                <c:choose>
                                    <c:when test="${schemaSet.draftStatus && empty actionBean.user}">
                                        <span class="link-folder" style="color:gray;">
                                            <c:out value="${schemaSet.identifier}"/> (<c:out value="${schemaSetName}"/>)
                                            <sup style="font-size:0.7em"><c:out value="${schemaSet.regStatus}" /></sup>
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <stripes:link beanclass="eionet.web.action.SchemaSetActionBean" class="link-folder">
                                            <stripes:param name="schemaSet.identifier" value="${schemaSet.identifier}" />
                                            <c:if test="${schemaSet.workingCopy}"><stripes:param name="workingCopy" value="true" /></c:if>
                                            <c:out value="${schemaSet.identifier}"/>
                                        </stripes:link>
                                        (<c:out value="${schemaSetName}"/>)
                                        <c:if test="${ddfn:contains(actionBean.statusTextsToDisplay, schemaSet.regStatus)}">
                                            <sup style="font-size:0.7em"><c:out value="${schemaSet.regStatus}" /></sup>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                                <c:if test="${not empty actionBean.userName && schemaSet.workingCopy && actionBean.userName==schemaSet.workingUser}">
                                    <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                                </c:if>
                            </li>
                        </c:if>
                    </c:forEach>
                    <c:if test="${outerLoop.index == 0}">
                        <h3>Deprecated Schema Sets</h3>
                        <div class="advice-msg" style="margin-top:1em;font-size:0.8em">
                            Hint: Following schema sets are deprecated. They are not valid anymore!
                        </div>
                    </c:if>
                </c:forEach>
               <!-- ROOT LEVEL SCHEMAS -->
                <c:if test="${fn:length(actionBean.schemas) > 0}">
                    <h3>Root Level Schemas</h3>
                </c:if>
                <c:forEach var="schema" items="${actionBean.schemas}">
                    <c:set var="schemaName" value="${schema.attributeValues!=null ? ddfn:join(schema.attributeValues['Name'],'') : ''}"/>
                    <c:set var="displayName" value="${not empty schemaName ? schemaName : schema.fileName}"/>
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
                        <c:choose>
                            <c:when test="${schema.draftStatus && empty actionBean.user}">
                                <span style="color:gray;" class="link-xsd">
                                    <c:out value="${schema.fileName}"/> (<c:out value="${schemaName}"/>)&nbsp;<sup style="font-size:0.7em"><c:out value="${schema.regStatus}" /></sup>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <stripes:link beanclass="eionet.web.action.SchemaActionBean" class="link-xsd">
                                    <stripes:param name="schema.fileName" value="${schema.fileName}" />
                                    <c:if test="${schema.workingCopy}"><stripes:param name="workingCopy" value="true" /></c:if>
                                    <c:out value="${schema.fileName}"/>
                                </stripes:link>
                                (<c:out value="${schemaName}"/>)
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${not empty actionBean.userName && schema.workingCopy && actionBean.userName==schema.workingUser}">
                            <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
            <br/>

            <c:if test="${not empty actionBean.user && (not empty actionBean.schemaSets || not empty actionBean.schemas)}">
                <c:choose>
                    <c:when test="${not empty actionBean.deletableSchemaSets || not empty actionBean.deletableSchemas}">
                        <stripes:submit name="delete" value="Delete" onclick="return confirm('Are you sure you want to delete the selected schema sets and/or schemas?');"/>
                        <input type="button" onclick="toggleSelectAll('schemaSetsForm');return false" value="Select all" name="selectAll" />
                    </c:when>
                    <c:otherwise>
                        <stripes:submit disabled="disabled" name="delete" value="Delete" onclick="return confirm('Are you sure you want to delete the selected schema sets and/or schemas?');"/>
                        <input disabled="disabled" type="button" onclick="toggleSelectAll('schemaSetsForm');return false" value="Select all" name="selectAll" />
                    </c:otherwise>
                </c:choose>
            </c:if>

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>
