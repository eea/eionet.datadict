<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="eionet.meta.dao.domain.RegStatus"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Edit vocabulary">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {
                // Open add concept dialog
                $("#addNewConceptLink").click(function() {
                    $("#addNewConceptDiv").dialog('open');
                    return false;
                });

                // Add concept dialog setup
                $("#addNewConceptDiv").dialog({
                    autoOpen: false,
                    width: 800
                });

                // Close add concept dialog
                $("#closeAddNewConeptButton").click(function() {
                    $("#addNewConceptDiv").dialog("close");
                    return false;
                });

                var initPopup = function(divId) {
                    $(divId).dialog({
                        autoOpen: false,
                        width: 800
                    });
                }

                openPopup = function(divId) {
                    $(divId).dialog('open');
                    return false;
                }

                closePopup = function(divId) {
                    $(divId).dialog("close");
                    return false;
                }

                <c:forEach var="item" items="${actionBean.vocabularyConcepts.list}">
                    initPopup("#editConceptDiv${item.id}");
                </c:forEach>

                <c:if test="${not empty actionBean.editDivId}">
                    openPopup("#${actionBean.editDivId}");
                </c:if>
            });

        } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <c:if test="${actionBean.vocabularyFolder.commonType}">
                    <li><a href="#" id="addNewConceptLink">Add new concept</a></li>
                </c:if>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkIn">
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        Check in
                    </stripes:link>
                </li>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="undoCheckOut">
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        Undo checkout
                    </stripes:link>
                </li>
            </ul>
        </div>

        <h1>Edit vocabulary</h1>

        <c:if test="${actionBean.vocabularyFolder.workingCopy && actionBean.vocabularyFolder.siteCodeType}">
            <div class="note-msg">
                <strong>Notice</strong>
                <p>
                For checked out site codes, vocabulary concepts are not visible. To view them, see the
                <stripes:link href="/services/siteCodes">site codes page</stripes:link>.
                </p>
            </div>
        </c:if>

        <stripes:form id="editVocabularyFolderForm" method="post" beanclass="${actionBean.class.name}" style="padding-top:20px">
        <div id="outerframe">
            <stripes:hidden name="vocabularyFolder.id" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />
            <stripes:hidden name="vocabularyFolder.checkedOutCopyId" />

            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
                    <col style="width:4%"/>
                    <col />
                </colgroup>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="vocabularyFolder.identifier"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Label
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text name="vocabularyFolder.label" style="width: 500px;" class="smalltext"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Base URI
                    </th>
                    <td class="simple_attr_help">
                        <dd:optionalIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text name="vocabularyFolder.baseUri" style="width: 500px;" class="smalltext"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Registration status
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <c:set var="regStatuses" value="<%=RegStatus.values()%>"/>
                        <stripes:select name="vocabularyFolder.regStatus" value="${actionBean.vocabularyFolder.regStatus}">
                            <c:forEach items="${regStatuses}" var="aRegStatus">
                                <stripes:option value="${aRegStatus}" label="${aRegStatus}"/>
                            </c:forEach>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Type
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.type.label}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Numeric concept identifiers
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:checkbox name="vocabularyFolder.numericConceptIdentifiers" />
                    </td>
                </tr>
                <tr>
                    <th>&nbsp;</th>
                    <td colspan="2">
                        <stripes:submit name="saveFolder" value="Save" class="mediumbuttonb"/>
                        <stripes:submit name="cancelSave" value="Cancel" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </div>
        </stripes:form>

        <div id="addNewConceptDiv" title="New concept">
            <stripes:form id="addNewConceptForm" method="post" beanclass="${actionBean.class.name}">

            <c:set var="divId" value="addNewConceptDiv" />
            <c:if test="${actionBean.editDivId eq divId}">
                <!--  validation errors -->
                <stripes:errors/>
            </c:if>

            <div>
                <stripes:hidden name="vocabularyFolder.identifier" />
                <stripes:hidden name="vocabularyFolder.workingCopy" />
                <stripes:hidden name="vocabularyFolder.id" />
                <stripes:hidden name="vocabularyFolder.numericConceptIdentifiers" />

                <table class="datatable">
                    <colgroup>
                        <col style="width:26%"/>
                        <col style="width:4%"/>
                        <col />
                    </colgroup>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Identifier
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="30" name="vocabularyConcept.identifier" value="${actionBean.nextIdentifier}" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Label
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text name="vocabularyConcept.label" style="width: 500px;" class="smalltext"/>
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Definition
                        </th>
                        <td class="simple_attr_help">
                            <dd:optionalIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:textarea name="vocabularyConcept.definition" rows="3" cols="60" class="smalltext"/>
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Notation
                        </th>
                        <td class="simple_attr_help">
                            <dd:optionalIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="30" name="vocabularyConcept.notation" />
                        </td>
                    </tr>
                    <tr>
                        <th>&nbsp;</th>
                        <td colspan="2">
                            <stripes:submit name="saveConcept" value="Add" class="mediumbuttonb"/>
                            <button class="mediumbuttonb" id="closeAddNewConeptButton">Cancel</button>
                        </td>
                    </tr>
                </table>
            </div>
            </stripes:form>
        </div>

        <!-- Vocabulary concepts search -->
        <stripes:form method="get" id="searchForm" beanclass="${actionBean.class.name}">
            <div id="searchframe">
            <stripes:hidden name="vocabularyFolder.identifier" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />

            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
                    <col />
                    <col />
                </colgroup>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title" title="Text to filter from label, notation and definition">
                        <span style="white-space:nowrap;">Filtering text</span>
                    </th>
                    <td class="simple_attr_value" style="width: 400px; padding-right: 10px;">
                        <stripes:text class="smalltext" size="30" name="filter.text"/>
                    </td>
                    <td>
                        <stripes:submit name="edit" value="Search" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
            </div>
        </stripes:form>

        <!-- Vocabulary concepts -->
        <stripes:form method="post" id="conceptsForm" beanclass="${actionBean.class.name}">
            <display:table name="${actionBean.vocabularyConcepts}" class="datatable" id="item" style="width:80%"
                requestURI="/vocabulary/${actionBean.vocabularyFolder.identifier}/edit">
                <display:setProperty name="basic.msg.empty_list" value="No vocabulary concepts found." />

                <display:column style="width: 1%">
                    <stripes:checkbox name="conceptIds" value="${item.id}" />
                </display:column>
                <display:column title="Id" escapeXml="true" property="identifier" class="${actionBean.vocabularyFolder.numericConceptIdentifiers ? 'number' : ''}" style="width: 1%" />
                <display:column title="Label">
                    <a href="#" onClick="openPopup('#editConceptDiv${item.id}')"><c:out value="${item.label}" /></a>
                </display:column>
                <display:column title="Definition" escapeXml="true" property="definition" />
                <display:column title="Notation" escapeXml="true" property="notation" />
            </display:table>
            <c:if test="${not empty actionBean.vocabularyConcepts.list}">
                <div>
                    <stripes:hidden name="vocabularyFolder.identifier" />
                    <stripes:hidden name="vocabularyFolder.workingCopy" />
                    <stripes:submit name="deleteConcepts" value="Delete" />
                    <input type="button" onclick="toggleSelectAll('conceptsForm');return false" value="Select all" name="selectAll">
                </div>
            </c:if>
        </stripes:form>

        <!-- Vocabulary concept edit forms -->
        <c:forEach var="item" items="${actionBean.vocabularyConcepts.list}" varStatus="loop">
            <div id="editConceptDiv${item.id}" title="Edit concept">
                <stripes:form id="form${item.id}" method="post" beanclass="${actionBean.class.name}">

                    <c:set var="divId" value="editConceptDiv${item.id}" />
                    <c:if test="${actionBean.editDivId eq divId}">
                        <!--  validation errors -->
                        <stripes:errors/>
                    </c:if>

                    <div>
                        <stripes:hidden name="vocabularyFolder.identifier" />
                        <stripes:hidden name="vocabularyFolder.workingCopy" />
                        <stripes:hidden name="vocabularyFolder.id" />
                        <stripes:hidden name="vocabularyFolder.numericConceptIdentifiers" />
                        <stripes:hidden name="page" />
                        <stripes:hidden name="filter.text" />
                        <stripes:hidden name="vocabularyConcepts.list[${loop.index}].id" />

                        <table class="datatable">
                            <colgroup>
                                <col style="width:26%"/>
                                <col style="width:4%"/>
                                <col />
                            </colgroup>
                            <tr>
                                <th scope="row" class="scope-row simple_attr_title">
                                    Identifier
                                </th>
                                <td class="simple_attr_help">
                                    <dd:mandatoryIcon />
                                </td>
                                <td class="simple_attr_value">
                                    <stripes:text class="smalltext" size="30" name="vocabularyConcepts.list[${loop.index}].identifier" />
                                </td>
                            </tr>
                            <tr>
                                <th scope="row" class="scope-row simple_attr_title">
                                    Label
                                </th>
                                <td class="simple_attr_help">
                                    <dd:mandatoryIcon />
                                </td>
                                <td class="simple_attr_value">
                                    <stripes:text name="vocabularyConcepts.list[${loop.index}].label" style="width: 500px;" class="smalltext"/>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row" class="scope-row simple_attr_title">
                                    Definition
                                </th>
                                <td class="simple_attr_help">
                                    <dd:optionalIcon />
                                </td>
                                <td class="simple_attr_value">
                                    <stripes:textarea name="vocabularyConcepts.list[${loop.index}].definition" rows="3" cols="60" class="smalltext"/>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row" class="scope-row simple_attr_title">
                                    Notation
                                </th>
                                <td class="simple_attr_help">
                                    <dd:optionalIcon />
                                </td>
                                <td class="simple_attr_value">
                                    <stripes:text class="smalltext" size="30" name="vocabularyConcepts.list[${loop.index}].notation" />
                                </td>
                            </tr>
                            <tr>
                                <th>&nbsp;</th>
                                <td colspan="2">
                                    <stripes:submit name="saveConcept" value="Save" class="mediumbuttonb"/>
                                    <button type="button" onClick="closePopup('#editConceptDiv${item.id}')">Cancel</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </stripes:form>
            </div>
        </c:forEach>

    </stripes:layout-component>

</stripes:layout-render>