<%@ include file="/pages/common/taglibs.jsp"%>

<script type="text/javascript">
<!--
( function($) {
    $(document).ready(function() {

           $("#backToVoc").click(function(){
              var elementId="${actionBean.elementId}";

              $("#addConceptDiv").dialog("close");
              openVocabularySearch(elementId);
              return false;
            });

           $("#backToSearch").click(function(){
               var elementId="${actionBean.elementId}";

               $("#addConceptDiv").dialog("close");
               openVocabularySearch(elementId);
               return false;
             });


        $("#cancelVocBtn").click(function(){
              $("#findVocabularyDiv").dialog("close");
                return false;
            });


        $("#cancelConceptBtn").click(function(){
            $("#addConceptDiv").dialog("close");
              return false;
          });


        // Add concept dialog setup
        $("#addConceptDiv").dialog({
            autoOpen: false,
            width: 600,
            modal: true

        });
        $("#findVocabularyDiv").dialog({
            autoOpen: false,
            width: 600,
            modal: true
        });

        // Close add concept dialog
        $("#closeAddElementsButton").click(function() {
            $("#addConceptDiv").dialog("close");
            return false;
        });

        $("#conceptFilterText").change(function() {
            if ($("#conceptFilterText").val().length == 1) {
                alert ("Search Text is too short");
                $("#conceptFilterText").attr("value", "");
                $("#conceptFilterText").focus();
            }
        });

      <c:if test="${not empty actionBean.editDivId}">
        openPopup("#${actionBean.editDivId}");
        $("#txtElemName").attr("value","${actionBean.elementId}");
      </c:if>
    });
} ) ( jQuery );
//-->

function openVocabularySearch(elementId) {
    document.getElementById('txtEditDivId').value='findVocabularyDiv';
    document.getElementById('txtElemName').value=elementId;
    openPopup("#findVocabularyDiv");
};
</script>


<!--  Search concepts div -->
<div id="addConceptDiv" title="Step 2/2: Find concept">
        <stripes:form method="post" beanclass="${actionBean.class.name}">
        <div>
        <stripes:hidden name="vocabularyConcept.identifier" />
        <stripes:hidden name="vocabularyFolder.folderName" />
        <stripes:hidden name="vocabularyFolder.identifier" />
        <stripes:hidden name="vocabularyFolder.workingCopy" />
        <stripes:hidden name="elementId" />
        <stripes:hidden name="folderId" />

        </div>
        <table class="datatable" style="width:100%">
            <colgroup>
                <col style="width:10em;"/>
                <col style="width:30em;"/>
            </colgroup>

             <c:if test="${not empty actionBean.relatedVocabulary}">
                <tr>
                  <th scope="row" class="scope-row simple_attr_title">
                      <label for="selectedVocabulary"><span style="white-space:nowrap;">Selected Vocabulary</span></label>
                  </th>
                  <td class="simple_attr_value" colspan="2">
                       ${actionBean.relatedVocabulary.label} [<a href="#" class="delLink" id="backToVoc">Find another vocabulary</a>]
                  </td>
                </tr>
            </c:if>
            <c:if test="${empty actionBean.relatedVocabulary}">
                <tr>
                  <th></th>
                  <td class="simple_attr_value" colspan="2">
                       [<a href="#" class="delLink" id="backToSearch">Back to search</a>]
                  </td>
                </tr>
            </c:if>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="filterText"><span style="white-space:nowrap;">Vocabulary Concept</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="relatedConceptsFilter.text" id="filterText" placeholder="Search by concept identifier, label or definition"/>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <stripes:submit name="searchConcepts" value="Search" class="mediumbuttonb"/>
                    <stripes:button id="cancelConceptBtn" name="cancelConceptSearch" value="Cancel" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>
    <div>
    <c:if test="${not empty actionBean.relatedVocabularyConcepts}">
        <display:table name="actionBean.relatedVocabularyConcepts.list" class="sortable" id="item" pagesize="20"
            requestURI="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${actionBean.vocabularyConcept.identifier}/${actionBean.searchEventName}">
            <c:if test="${empty actionBean.relatedVocabulary}">
                <display:column title="Vocabulary Set" sortable="true" sortProperty="vocabularySetLabel">
                    ${item.vocabularySetLabel}
                </display:column>
                <display:column title="Vocabulary" sortable="true" sortProperty="vocabularyLabel">
                    ${item.vocabularyLabel}
                </display:column>
            </c:if>
            <display:column title="Concept" sortable="true" sortProperty="identifier">
                    <stripes:link beanclass="${actionBean.class.name}" event="addRelatedConcept" title="Select the concept">
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
        </c:if>
    </div>
    </stripes:form>
</div>

<!--  Search concepts div -->
<div id="findVocabularyDiv" title="Step 1/2: Find vocabulary or concept">
        <stripes:form method="post" beanclass="${actionBean.class.name}">
        <div>
          <stripes:hidden name="vocabularyFolder.folderName" />
          <stripes:hidden name="vocabularyFolder.identifier" />
          <stripes:hidden name="vocabularyFolder.workingCopy" />
          <stripes:hidden name="vocabularyConcept.identifier" />
          <stripes:hidden id="txtElemName" name="elementId" />
          <!--  stripes:text id="txtFolderId" name="folderId" / -->

        </div>
        <table class="datatable" style="width:100%">
            <colgroup>
                <col style="width:10em;"/>
                <col style="width:30em;"/>
            </colgroup>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="vocFilterText"><span style="white-space:nowrap;">Vocabulary</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="vocabularyFilter.text" id="vocFilterText"  placeholder="Search by vocabulary identifier or label"/>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="conceptFilterText"><span style="white-space:nowrap;">Concept</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="vocabularyFilter.conceptText" id="conceptFilterText"  placeholder="Search by concept identifier, label or definition"/>
                </td>
            </tr>
            <tr>
                <td>
                </td>
                <td>
                    <stripes:submit name="searchVocabularies" value="Search" class="mediumbuttonb"/>
                    <stripes:button id="cancelVocBtn" name="cancelVocabularySearch" value="Cancel" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>

    <div>
      <c:if test="${not empty actionBean.vocabularies}">
          <display:table name="actionBean.vocabularies.list" class="sortable" id="item" pagesize="20"
              requestURI="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${actionBean.vocabularyConcept.identifier}/searchVocabularies">
              <display:column title="Vocabulary Set" sortable="true" sortProperty="folderName">
                  ${item.folderName}
              </display:column>
              <display:column title="Vocabulary" sortable="true" sortProperty="identifier">
                      <stripes:link beanclass="${actionBean.class.name}" event="searchConcepts" title="Show concepts">
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
      </c:if>
    </div>
    </stripes:form>
</div>

