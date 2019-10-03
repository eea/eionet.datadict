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

        $("#addNewConceptBtn").click(function() {
            $("#addNewConceptDiv").dialog('open');
            return false;
        });

        // Add concept dialog setup
        $("#addNewConceptDiv").dialog({
            autoOpen: false,
            width: 800
        });

        // Close add concept dialog
        $("#closeAddNewConceptButton").click(function() {
            $("#addNewConceptDiv").dialog("close");
            return false;
        });

    });
} ) ( jQuery );
//-->
</script>

<div id="addNewConceptDiv" title="New concept">
    <stripes:form id="addNewConceptForm" method="post" beanclass="${actionBean['class'].name}">

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
                    <label for="conceptIdentifier">Identifier</label>
                </th>
                <td class="simple_attr_help">
                    <dd:mandatoryIcon />
                </td>
                <td class="simple_attr_value">
                    <stripes:text id="conceptIdentifier" class="smalltext" size="30" name="vocabularyConcept.identifier" value="${actionBean.nextIdentifier}" />
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="conceptLabel">Label</label>
                </th>
                <td class="simple_attr_help">
                    <dd:mandatoryIcon />
                </td>
                <td class="simple_attr_value">
                    <stripes:text id="conceptLabel" name="vocabularyConcept.label" style="width: 500px;" class="smalltext"/>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="conceptDefinition">Definition</label>
                </th>
                <td class="simple_attr_help">
                    <dd:optionalIcon />
                </td>
                <td class="simple_attr_value">
                    <stripes:textarea id="conceptDefinition" name="vocabularyConcept.definition" rows="3" cols="60" class="smalltext"/>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="conceptNotation">Notation</label>
                </th>
                <td class="simple_attr_help">
                    <dd:optionalIcon />
                </td>
                <td class="simple_attr_value">
                    <c:choose>
                        <c:when test="${actionBean.vocabularyFolder != null && actionBean.vocabularyFolder.notationsEqualIdentifiers}">
                            <span style="font-size:0.7em">(forcefully equal to identifier in this vocabulary)</span>
                        </c:when>
                        <c:otherwise>
                            <stripes:text id="conceptNotation" class="smalltext" size="30" name="vocabularyConcept.notation" />
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <tr>
                <th>&nbsp;</th>
                <td></td>
                <td>
                    <stripes:submit name="saveConcept" value="Add" class="mediumbuttonb"/>
                    <input type="submit" value="Cancel" class="mediumbuttonb" id="closeAddNewConeptButton" />
                </td>
            </tr>
        </table>
    </div>
    </stripes:form>
</div>