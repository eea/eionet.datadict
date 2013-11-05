<%@ include file="/pages/common/taglibs.jsp"%>

<script type="text/javascript">
<!--
( function($) {
    $(document).ready(function() {

        // Add concept dialog setup
        $("#addConceptDiv").dialog({
            autoOpen: false,
            width: 600

        });
        $("#findVocabularyDiv").dialog({

            autoOpen: false,
            width: 600,
            open:function() {
                //var elementId=$(this).data('elementId');
               // $("#txtElemName").attr("value",elementId);
            }
        });

        // Close add concept dialog
        $("#closeAddElementsButton").click(function() {
            $("#addConceptDiv").dialog("close");
            return false;
        });


      <c:if test="${not empty actionBean.editDivId}">
        openPopup("#${actionBean.editDivId}");
        <c:if test="${not empty actionBean.elementId}">
            $("#txtElemName").attr("value","${actionBean.elementId}");
        </c:if>
      </c:if>
    });
} ) ( jQuery );
//-->
</script>


<!--  Search concepts div -->
<div id="addConceptDiv" title="Find a vocabulary concept">
        <stripes:form method="post" beanclass="${actionBean.class.name}">
        <div>
        <stripes:hidden name="vocabularyConcept.identifier" />
        <stripes:hidden name="vocabularyFolder.folderName" />
        <stripes:hidden name="vocabularyFolder.identifier" />
        <stripes:hidden name="vocabularyFolder.workingCopy" />

        <stripes:hidden name="folderId" />


        </div>
        <table class="datatable">
            <colgroup>
                <col style="width:10em;"/>
                <col />
                <col />
            </colgroup>
            <tr>
                <th scope="row" class="scope-row simple_attr_title" title="Vocabulary concept identifier or label ">
                    <label for="filterText"><span style="white-space:nowrap;">Vocabulary Concept</span></label>
                </th>
                <td class="simple_attr_value">
                    <stripes:text class="smalltext" size="30" name="relatedConceptsFilter.text" id="filterText"/>
                </td>
                <td>
                    <stripes:submit name="searchConcepts" value="Search" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>
    <div>
        <display:table name="actionBean.relatedVocabularyConcepts.list" class="sortable" id="item" pagesize="20"
            requestURI="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${actionBean.vocabularyConcept.identifier}/searchConcepts">
            <display:column title="Concept" sortable="true" sortProperty="identifier">
                    <stripes:link beanclass="${actionBean.class.name}" event="addRelatedConcept">
                        <stripes:param name="conceptId" value="${item.id}" />
                        <c:if test="${not empty actionBean.elementId}">
                            <stripes:param name="elementId" value="${actionBean.elementId}" />
                        </c:if>
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        <stripes:param name="vocabularyConcept.identifier" value="${actionBean.vocabularyConcept.identifier}" />
                        ${item.identifier}
                    </stripes:link>


            </display:column>

            <display:column title="Label" sortable="true" sortProperty="label">
                ${item.label}
            </display:column>
        </display:table>
    </div>
    </stripes:form>
</div>



<!--  Search concepts div -->
<div id="findVocabularyDiv" title="Find a vocabulary">
        <stripes:form method="post" beanclass="${actionBean.class.name}">
        <div>
          <stripes:hidden name="vocabularyFolder.folderName" />
          <stripes:hidden name="vocabularyFolder.identifier" />
          <stripes:hidden name="vocabularyFolder.workingCopy" />
          <stripes:hidden name="vocabularyConcept.identifier" />
          <stripes:hidden id="txtElemName" name="elementId" />
          <stripes:hidden id="txtFolderId" name="folderId" />

        </div>
        <table class="datatable">
            <colgroup>
                <col style="width:10em;"/>
                <col />
                <col />
            </colgroup>
            <tr>
                <th scope="row" class="scope-row simple_attr_title" title="Vocabulary identifier or label ">
                    <label for="filterText"><span style="white-space:nowrap;">Vocabulary</span></label>
                </th>
                <td class="simple_attr_value">
                    <stripes:text class="smalltext" size="30" name="vocabularyFilter.text" id="vocFilterText"/>
                </td>
                <td>
                    <stripes:submit name="searchVocabularies" value="Search" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>
    <div>
        <display:table name="actionBean.vocabularies.list" class="sortable" id="item" pagesize="20"
            requestURI="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${actionBean.vocabularyConcept.identifier}/searchVocabularies">
            <display:column title="Vocabulary" sortable="true" sortProperty="identifier">
                    <stripes:link beanclass="${actionBean.class.name}" event="searchConcepts">
                           <c:if test="${not empty actionBean.elementId}">
                            <stripes:param name="elementId" value="${actionBean.elementId}" />
                        </c:if>
                        <stripes:param name="vocabularyFolder.identifier"  value="${actionBean.vocabularyFolder.identifier}"/>
                        <stripes:param name="vocabularyFolder.folderName"  value="${actionBean.vocabularyFolder.folderName}"/>
                        <stripes:param name="vocabularyConcept.identifier"  value="${actionBean.vocabularyConcept.identifier}"/>
                        <stripes:param name="folderId" value="${item.id}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        ${item.identifier}
                    </stripes:link>
            </display:column>

            <display:column title="Label" sortable="true" sortProperty="label">
                ${item.label}
            </display:column>
        </display:table>
    </div>
    </stripes:form>
</div>

