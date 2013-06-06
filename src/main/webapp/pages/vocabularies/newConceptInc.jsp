<%@ include file="/pages/common/taglibs.jsp"%>

<script type="text/javascript">
<!--
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

    });
} ) ( jQuery );
//-->
</script>

<div id="addNewConceptDiv" title="New concept">
    <stripes:form id="addNewConceptForm" method="post" beanclass="${actionBean.class.name}">

    <c:set var="divId" value="addNewConceptDiv" />
    <c:if test="${actionBean.editDivId eq divId}">
        <!--  validation errors -->
        <stripes:errors/>
    </c:if>

    <div>
        <stripes:hidden name="vocabularyFolder.folderName" />
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