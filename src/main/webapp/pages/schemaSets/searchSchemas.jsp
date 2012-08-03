<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Schema sets">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        $('#newNameDialog').dialog({
                            autoOpen: ${actionBean.askNewName},
                            width: 500
                        });

                        $("#closeNewNameDialog").click(function() {
                            $('#newNameDialog').dialog('close');
                            return true;
                        });
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <c:if test="${ddfn:userHasPermission(actionBean.userName, '/schemas', 'i')}">
            <div id="drop-operations">
                <h2>Operations:</h2>
                <ul>
                <li><stripes:link beanclass="eionet.web.action.SchemaActionBean" event="add">Add root-level schema</stripes:link></li>
            </ul>
            </div>
        </c:if>

        <h1>Search schemas</h1>

        <stripes:form id="searchResultsForm" action="/schema/search/" method="get">
            <stripes:hidden name="schemaSetId" />
            <div style="margin-top:1em">
                <label class="question" style="width:18%;float:left;" for="name">Schema file name:</label>
                <stripes:text id="name" name="searchFilter.fileName" />
                <br/>
                <label class="question" style="width:18%;float:left;" for="schemaSetIdentifier">Schema set identifer:</label>
                <stripes:text id="schemaSetIdentifier" name="searchFilter.schemaSetIdentifier" />
                <span style="font-size:0.8em"><sup>(Not relevant for root-level schemas!)</sup></span>
                <br/>
                <c:if test="${not empty actionBean.userName}">
                    <label class="question" style="width:18%;float:left;" for="regStatus">Registration status:</label>
                    <stripes:select id="regStatus" name="searchFilter.regStatus" disabled="${not actionBean.authenticated}">
                        <stripes:options-collection collection="${actionBean.regStatuses}" />
                    </stripes:select><br/>
                </c:if>
                <c:forEach var="attr" items="${actionBean.searchFilter.attributes}" varStatus="row">
                    <label class="question" style="width:18%;float:left;" for="attr${row.index}">
                        <c:out value="${attr.shortName}" />:
                    </label>
                    <stripes:text id="attr${row.index}" name="searchFilter.attributes[${row.index}].value" />
                    <br/>
                    <stripes:hidden name="searchFilter.attributes[${row.index}].id" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].name" />
                    <stripes:hidden name="searchFilter.attributes[${row.index}].shortName" />
                </c:forEach>
                <span style="width:18%;float:left;">&nbsp;</span><stripes:submit name="search" value="Search"/>
            </div>

            <br />

            <display:table name="actionBean.schemasResult" class="sortable" id="item" requestURI="/schema/search/">
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
            </display:table>

            <c:if test="${actionBean.schemaSetId != 0}">
                <stripes:submit name="copyToSchemaSet" value="Copy to schema set" />
                <stripes:submit name="cancelCopy" value="Cancel" />
            </c:if>
        </stripes:form>

        <div id="newNameDialog" title="New file name">
            In the schema set, there is already schema with such name.
            <stripes:form beanclass="${actionBean.class.name}" method="get">
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