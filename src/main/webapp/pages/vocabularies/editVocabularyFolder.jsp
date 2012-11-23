<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

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
                    $(divId).dialog('close');
                    return false;
                }

                <c:forEach var="item" items="${actionBean.vocabularyConcepts}">
                    initPopup("#editConceptDiv${item.id}");
                </c:forEach>
            });

        } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <c:url var="mandatoryPic" value="/images/mandatory.gif" />
        <c:url var="optionalPic" value="/images/optional.gif" />

        <h1>Edit vocabulary</h1>

        <stripes:form id="form" method="post" beanclass="${actionBean.class.name}" style="padding-top:20px">
        <stripes:hidden name="vocabularyFolder.id" />
        <div id="outerframe">
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
                    <th>&nbsp;</th>
                    <td colspan="2">
                        <stripes:submit name="saveFolder" value="Save" class="mediumbuttonb"/>
                        <stripes:submit name="cancelSave" value="Cancel" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </div>
        </stripes:form>

        <a href="#" id="addNewConceptLink">New concept</a>

        <div id="addNewConceptDiv" title="New concept">
            <stripes:form id="form" method="post" beanclass="${actionBean.class.name}">
            <stripes:hidden name="vocabularyFolder.identifier" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />
            <stripes:hidden name="vocabularyFolder.id" />

            <div id="outerframe">
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
                            <stripes:text name="vocabularyConcept.definition" style="width: 500px;" class="smalltext"/>
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Notation
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
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

        <!-- Vocabulary concepts -->
        <stripes:form method="post" id="conceptsForm" beanclass="${actionBean.class.name}">
            <stripes:hidden name="vocabularyFolder.identifier" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />
            <display:table name="${actionBean.vocabularyConcepts}" class="datatable" id="item" style="width:80%">
                <display:column>
                    <stripes:checkbox name="conceptIds" value="${item.id}" />
                </display:column>
                <display:column title="Identifier" property="identifier" />
                <display:column title="Label">
                    <a href="#" onClick="openPopup('#editConceptDiv${item.id}')">${item.label}</a>
                </display:column>
                <display:column title="Definition" property="definition" />
                <display:column title="Notation" property="notation" />
            </display:table>
            <stripes:submit name="deleteConcepts" value="Delete" />
            <input type="button" onclick="toggleSelectAll('conceptsForm');return false" value="Select all" name="selectAll">
        </stripes:form>

        <!-- Vocabulary concept edit forms -->
        <c:forEach var="item" items="${actionBean.vocabularyConcepts}">
            <div id="editConceptDiv${item.id}" title="Edit concept">
                <stripes:form id="form" method="post" beanclass="${actionBean.class.name}">
                    <stripes:hidden name="vocabularyFolder.identifier" />
                    <stripes:hidden name="vocabularyFolder.workingCopy" />
                    <stripes:hidden name="vocabularyFolder.id" />
                    <stripes:hidden name="vocabularyConcept.id" value="${item.id}" />

                    <div id="outerframe">
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
                                    <stripes:text class="smalltext" size="30" name="vocabularyConcept.identifier" value="${item.identifier}" />
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
                                    <stripes:text name="vocabularyConcept.label" value="${item.label}" style="width: 500px;" class="smalltext"/>
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
                                    <stripes:text name="vocabularyConcept.definition" value="${item.definition}" style="width: 500px;" class="smalltext"/>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row" class="scope-row simple_attr_title">
                                    Notation
                                </th>
                                <td class="simple_attr_help">
                                    <dd:mandatoryIcon />
                                </td>
                                <td class="simple_attr_value">
                                    <stripes:text class="smalltext" value="${item.notation}" size="30" name="vocabularyConcept.notation" />
                                </td>
                            </tr>
                            <tr>
                                <th>&nbsp;</th>
                                <td colspan="2">
                                    <stripes:submit name="saveConcept" value="Save" class="mediumbuttonb"/>
                                    <button class="mediumbuttonb" onClick="closePopup('#editConceptDiv${item.id}')">Cancel</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </stripes:form>
            </div>
        </c:forEach>
    </stripes:layout-component>

</stripes:layout-render>