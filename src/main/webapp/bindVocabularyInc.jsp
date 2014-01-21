<%@ include file="/pages/common/taglibs.jsp"%>

<link type="text/css" href="<c:url value="/css/smoothness/jquery-ui-1.8.16.custom.css" />" rel="stylesheet" />
  <script type="text/javascript" src="<c:url value="/scripts/jquery-1.6.2.min.js" />"></script>
  <script type="text/javascript" src="<c:url value="/scripts/jquery-ui-1.8.16.custom.min.js" />"></script>
  <script type="text/javascript" src="<c:url value="/scripts/jquery-timers.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/scripts/jquery.autocomplete.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/scripts/jquery.form.min.js"/>"></script>

  <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {


                $("#selectVocabularyLnk").live("click", function(event){

                    var context = "<%=request.getContextPath()%>";
                    var elemId = document.getElementById("txtElemId").value;
                    $('#searchVocabulariesDiv').dialog('open');
                    $('#vocabularySearchForm').find("input[id='txtDElemId']").attr("value", elemId);
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

                // Function for opening popups
                openPopup = function(divId) {
                    $(divId).dialog('open');
                    return false;
                }

                // Function for closing popups
                closePopup = function(divId) {
                    $(divId).dialog("close");
                    return false;
                }
        });


        } ) ( jQuery );
        // ]]>
        </script>


    <div id="searchVocabulariesDiv" title="Search vocabularies">
        <form id="vocabularySearchForm" method="post" action="<%=request.getContextPath()%>/bindvocabulary"  style="margin-top:1em">
          <input type="hidden" id ="txtDElemId" name = "elementId"/>
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
                  <td></td>
                  <td>
                      <!--input type="button" id="searchBtn" name="search" value="Search" class="mediumbuttonb"/-->
                      <input type="submit" id="submitBtn" name="search" value="Search" class="mediumbuttonb"/>
                      <input type="button" id="cancelBtn" name="cancelSearch" value="Cancel" class="mediumbuttonb"/>
                  </td>
              </tr>
          </table>
        </form>
    </div>

