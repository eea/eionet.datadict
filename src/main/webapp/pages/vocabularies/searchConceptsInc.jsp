<%@ include file="/pages/common/taglibs.jsp"%>
<c:url var="delIcon" value="/images/button_remove.gif" />
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

            $("#cancelCH3ConceptBtn").click(function(){
                $("#addCH3ConceptDiv").dialog("close");
                return false;
            });

            // Add concept dialog setup
            $("#addConceptDiv").dialog({
                autoOpen: false,
                width: 600,
                modal: true

            });

            $("#addCH3ConceptDiv").dialog({
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

            $("#filterText").change(function() {
                if ($("#filterText").val().length == 1) {
                    alert ("Search Text is too short");
                    $("#filterText").attr("value", "");
                    $("#filterText").focus();
                }
            });

            <c:if test="${not empty actionBean.editDivId}">
            $("#ch3SearchResults").attr('style', 'display:inline');
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

    function openCH3Search(elementId, vocabularyId, vocabularyLabel) {
        document.getElementById('txtEditDivId').value='addCH3ConceptDiv';
        document.getElementById('txtConceptElementId').value=elementId;
        document.getElementById('txtVocabularyId').value=vocabularyId;
        document.getElementById("relatedVocName").innerHTML=vocabularyLabel;
        openPopup("#addCH3ConceptDiv");

    }

</script>


<!--  Search concepts div -->
<div id="addCH3ConceptDiv" title="Find a concept" style="display:inline">
    <stripes:form method="post" beanclass="${actionBean.class.name}" id="frmSearchCH3Concept">
        <div>
            <stripes:hidden name="vocabularyConcept.identifier" />
            <stripes:hidden name="vocabularyFolder.folderName" />
            <stripes:hidden name="vocabularyFolder.identifier" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />
            <stripes:hidden id="txtConceptElementId" name="elementId" />
            <stripes:hidden id = "txtVocabularyId" name="elemVocabularyId" />
            <c:set var="fieldSize" value="68" />

        </div>

        <table class="datatable" style="width:100%">
            <colgroup>
                <col style="width:20em;"/>
                <col style="width:40em;"/>
            </colgroup>


            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <span style="white-space:nowrap;">Vocabulary</span>
                </th>
                <td class="simple_attr_value">
                    <span id="relatedVocName"><c:if test="${not empty actionBean.relatedVocabulary}">${actionBean.relatedVocabulary.label}</c:if></span>
                </td>
            </tr>

            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="filterCH3Text"><span style="white-space:nowrap;">Vocabulary Concept</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="relatedConceptsFilter.text" id="filterCH3Text" value="${actionBean.relatedConceptsFilter.text}" placeholder="Search by concept identifier, label or definition"/>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <stripes:submit name="searchConcepts" value="Search" class="mediumbuttonb"/>
                    <stripes:button id="cancelCH3ConceptBtn" name="cancelConceptSearch" value="Cancel" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>
        <div id="ch3SearchResults" style='display:none'>
            <c:if test="${not empty actionBean.relatedVocabularyConcepts and not empty actionBean.editDivId}">
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
                        <span title="${item.definition}">${item.label}</span>
                    </display:column>
                </display:table>
            </c:if>
        </div>
    </stripes:form>
</div>


<!--  Search concepts div -->
<div id="addConceptDiv" title="Step 2/2: Find concept">
    <stripes:form method="post" beanclass="${actionBean.class.name}" id="frmSearchConcept">
        <div>
            <stripes:hidden name="vocabularyConcept.identifier" />
            <stripes:hidden name="vocabularyFolder.folderName" />
            <stripes:hidden name="vocabularyFolder.identifier" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />
            <!-- this is vocabulary ID, param name is misleading -->
            <stripes:hidden name="folderId" />
            <c:set var="fieldSize" value="68" />


        </div>

        <table class="datatable" style="width:100%">
            <colgroup>
                <col style="width:20em;"/>
                <col style="width:40em;"/>
            </colgroup>

            <c:if test="${not empty actionBean.relatedVocabulary}">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        <span style="white-space:nowrap;">Selected Vocabulary</span>
                    </th>
                    <td class="simple_attr_value">
                            ${actionBean.relatedVocabulary.label} [<a href="#" class="delLink" id="backToVoc">Find another vocabulary</a>]
                    </td>
                </tr>
            </c:if>
            <c:if test="${empty actionBean.relatedVocabulary}">

                <tr>
                    <th></th>
                    <td class="simple_attr_value">

                        [<a href="#" class="delLink" id="backToSearch">Back to search</a>]

                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Found Vocabulary Sets
                    </th>
                    <td class="simple_attr_value">
                        <c:forEach var="vocabularySet" items="${actionBean.relatedVocabularyConcepts.vocabularySets}">
                            <c:out value="${vocabularySet.label} "/>
                            <stripes:link beanclass="${actionBean.class.name}" event="searchConcepts" title="Exclude the vocabulary set from search">
                                <c:if test="${not empty actionBean.elementId}">
                                    <stripes:param name="elementId" value="${actionBean.elementId}" />
                                </c:if>
                                <stripes:param name="vocabularyFolder.identifier"  value="${actionBean.vocabularyFolder.identifier}"/>
                                <stripes:param name="vocabularyFolder.folderName"  value="${actionBean.vocabularyFolder.folderName}"/>
                                <stripes:param name="vocabularyConcept.identifier"  value="${actionBean.vocabularyConcept.identifier}"/>
                                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />

                                <stripes:param name="excludeVocSetId" value="${vocabularySet.id}" />
                                <stripes:param name="excludeVocSetLabel" value="${vocabularySet.label}" />
                                <stripes:param name="excludedVocSetIds" value="${actionBean.excludedVocSetIds}" />
                                <stripes:param name="excludedVocSetLabels" value="${actionBean.excludedVocSetLabels}" />

                                <stripes:param name="relatedConceptsFilter.text" value="${actionBean.relatedConceptsFilter.text}" />

                                <img style='border:0' src='${delIcon}' alt=""/>
                            </stripes:link>
                            <br/>

                        </c:forEach>
                    </td>
                </tr>


                <c:if test="${not empty actionBean.excludedVocSetLabels}">
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Excluded Vocabulary Sets
                        </th>
                        <td class="simple_attr_value">
                            <c:forEach var="item" items="${actionBean.excludedVocSetLabels}">
                                <c:out value="${item};"/>
                            </c:forEach>
                            <div class="advice-msg" style="margin-top:1em;font-size:0.7em">
                                Searching again will reset excluded vocabulary sets
                            </div>
                        </td>
                    </tr>
                </c:if>
            </c:if>



            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    <label for="filterText"><span style="white-space:nowrap;">Vocabulary Concept</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="relatedConceptsFilter.text" id="filterText" value="${actionBean.relatedConceptsFilter.text}" placeholder="Search by concept identifier, label or definition"/>
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
                        <c:choose>
                            <c:when test="${not empty item.obsolete}">
                                <span style="text-decoration:line-through"><span title="${item.definition} (Obsolete)">${item.label}</span></span>
                            </c:when>
                            <c:otherwise>
                                <span title="${item.definition}">${item.label}</span>
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                </display:table>
            </c:if>
        </div>
    </stripes:form>
</div>

<!--  Search vocabularies div -->
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
