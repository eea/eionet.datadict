<%@ include file="/pages/common/taglibs.jsp"%>

  <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {


                $("#searchLnk").live("click", function(event){
                    $('#searchVocabulariesDiv').dialog('open');
                    return false;
                });

                $("#searchVocabulariesDiv").dialog({
                    autoOpen: false,
                    width: 600,
                    modal: true
                });

                $("#cancelBtn").click(function(){
                    $("#searchVocabulariesDiv").dialog("close");
                    return false;
                });

                //copncepts
                $("#searchConceptLnk").live("click", function(event){
                    $('#searchConceptsDiv').dialog('open');
                    return false;
                });

                $("#searchConceptsDiv").dialog({
                    autoOpen: false,
                    width: 600,
                    modal: true
                });

                $("#cancelConceptsBtn").click(function(){
                    $("#searchConceptsDiv").dialog("close");
                    return false;
                });

        });

        } ) ( jQuery );
        // ]]>
        </script>


    <div id="searchVocabulariesDiv" title="Search vocabularies">
        <stripes:form id="vocabularySearchForm" beanclass="${actionBean.class.name}" method="post" style="margin-top:1em">
        <table class="datatable" style="width:100%">
            <colgroup>
                <col style="width:10em;"/>
                <col style="width:30em;"/>
            </colgroup>

            <tr>
                <th scope="row" class="scope-row simple_attr_title" title="Vocabulary concept identifier or label ">
                    <label for="filterText"><span style="white-space:nowrap;">Vocabulary</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="vocabularyFilter.text" id="filterText" placeholder="Search by Vocabulary identifier or label"/>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row simple_attr_title" title="Vocabularies concept identifier or label ">
                    <label for="filterText"><span style="white-space:nowrap;">Vocabulary Concept</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="vocabularyFilter.conceptText" id="filterConceptText" placeholder="Search by Vocabulary Concept identifier or label"/>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <stripes:submit name="search" value="Search" class="mediumbuttonb"/>
                    <stripes:button id="cancelBtn" name="cancelSearch" value="Cancel" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>
        </stripes:form>
    </div>

     <div id="searchConceptsDiv" title="Search vocabulary concepts">
        <stripes:form id="conceptSearchForm" beanclass="${actionBean.class.name}" method="post" style="margin-top:1em">
        <table class="datatable" style="width:100%">
            <colgroup>
                <col style="width:10em;"/>
                <col style="width:30em;"/>
            </colgroup>

            <tr>
                <th scope="row" class="scope-row simple_attr_title" title="Vocabularies concept identifier or label ">
                    <label for="filterText"><span style="white-space:nowrap;">Vocabulary Concept</span></label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" name="vocabularyConceptFilter.text" id="conceptFilterText" placeholder="Search by Vocabulary Concept identifier or label"/>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <stripes:submit name="searchConcepts" value="Search" class="mediumbuttonb"/>
                    <stripes:button id="cancelConceptBtn" name="cancelSearch" value="Cancel" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>
        </stripes:form>
    </div>
