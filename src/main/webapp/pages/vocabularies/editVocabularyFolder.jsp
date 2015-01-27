<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Edit vocabulary">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {

            	$("#uploadCSVLink").click(function() {
                    $('#uploadCSVDialog').dialog('open');
                    return false;
                });

            	$("#purgeVocabularyData").click(function() {
            		if ($('input#purgeVocabularyData').is(':checked')) {
	                   alert("If you check this option all data will be deleted! If you are not sure about this, please uncheck it!");
	                   $('input#purgeBoundElements').removeAttr("disabled");
            		}
            		else{
            		   $('input#purgeBoundElements').attr("disabled", true);
            		   $('input#purgeBoundElements').attr("checked", false);
            		}
                    return true;
                });

                $("#rdfPurgeVocabularyData").click(function() {
                    if ($('input#rdfPurgeVocabularyData').is(':checked')) {
                        alert("If you check this option all data will be deleted! If you are not sure about this, please uncheck it!");
                    }
                    return true;
                });

                $('#uploadCSVDialog').dialog({
                    autoOpen: false,
                    width: 500
                });

                $("#closeUploadCSVDialog").click(function() {
                    $('#uploadCSVDialog').dialog("close");
                    return true;
                });

                $("#uploadRDFLink").click(function() {
                    $('#uploadRDFDialog').dialog('open');
                    return false;
                });

                $('#uploadRDFDialog').dialog({
                    autoOpen: false,
                    width: 500,
                    closeOnEscape: false,
                    open: function(event, ui) { $(".ui-dialog-titlebar-close").hide();}
                });

                $("#closeUploadRDFDialog").click(function() {
                    $('#uploadRDFDialog').dialog("close");
                    return true;
                });

                $("#uploadRdf").click(function(){
                    $('input#uploadRdf').attr("disabled", true);
                    $('input#closeUploadRDFDialog').attr("disabled", true);
                    $('#spinningImage').show();
                    alert("This operation can take several minutes depending on size of RDF file, please do not press back button of your browser!");
                    //$("#uploadRDFForm").attr('action', 'uploadRdf').submit();
                    $("#uploadRDFForm").submit();
                    return true;
                });

                $(".folderChoice").click(function() {
                    handleFolderChoice();
                });

                handleFolderChoice = function() {
                    var value = $("input:radio[name=folderChoice]:checked").val();

                    if (value == "new") {
                        $("#newFolderDiv").show();
                        $("#existingFolderDiv").hide();
                    } else if (value == "existing") {
                        $("#newFolderDiv").hide();
                        $("#existingFolderDiv").show();
                    } else {
                        $("#newFolderDiv").hide();
                        $("#existingFolderDiv").hide();
                    }
                }

                handleFolderChoice();


                $(".delLink").click(function() {
                    this.parentElement.remove();
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

        /**
        * require user confirmation if noattions equal identifier is set to tru during this edit session
        */
        function doSaveFolder() {
            //TODO this can be added also for vocabulary set changes as a future enhancement!!!
           var initialFolderIdentifier = "${actionBean.vocabularyFolder.identifier}";
           var initialBaseUri = "${actionBean.vocabularyFolder.baseUri}";

           var currentFolderIdentifier = document.getElementById("vocabularyFolderIdentifier").value;
           var currentBaseUri = document.getElementById("vocabularyFolderBaseUri").value;

           if (initialFolderIdentifier != currentFolderIdentifier && initialBaseUri.indexOf(initialFolderIdentifier) > 0 && currentBaseUri.indexOf(initialFolderIdentifier) > 0){
               var proposalBaseUri = currentBaseUri.replace(initialFolderIdentifier, currentFolderIdentifier);
               var result = confirm("You have changed folder identifier but not Base Uri. In this case, import operations may fail to match references with this vocabulary concepts.\nDo you want to change '"+ currentBaseUri + "' with '" + proposalBaseUri + "' ?");
               if (result){
                   document.getElementById("vocabularyFolderBaseUri").value = proposalBaseUri;
               }
           }

          var chkNotationEqualsIdentifiersValue = document.getElementById("chkNotationsEqualIdentifiers").checked;
          var prevNotationsEqualIdentifiersValue = document.getElementById("prevNotationsEqualIdentifiers").value == 'true';
            if (chkNotationEqualsIdentifiersValue && !prevNotationsEqualIdentifiersValue) {
                return confirm("You have chosen notations to be equal with identifiers. This will overwrite the notations of all sub-ordinating concepts with their identifiers! Click OK if you are absolutely sure you want to continue, otherwise click Cancel.");
            } else {
                return true;
            }
        }

        // ]]>

        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkIn">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        Check in
                    </stripes:link>
                </li>
                <c:if test="${actionBean.userWorkingCopy}">
          <li>
              <a href="#" id="addNewConceptLink">Add new concept</a>
          </li>
          <li>
              <a href="#" id="uploadCSVLink">Upload CSV</a>
          </li>
          <li>
              <a href="#" id="uploadRDFLink">Upload RDF</a>
          </li>
        </c:if>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="undoCheckOut">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
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
            <stripes:hidden name="vocabularyFolder.folderName" />
            <stripes:hidden name="origIdentifier" />


            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
                    <col style="width:4%"/>
                    <col />
                </colgroup>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Folder
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:radio name="folderChoice" id="existingFolderChoice" value="existing" class="folderChoice"/> <label for="existingFolderChoice">Existing folder</label>
                        <stripes:radio name="folderChoice" id="newFolderChoice" value="new" class="folderChoice" /> <label for="newFolderChoice">New folder</label>
                        <div id="existingFolderDiv">
                            <stripes:select name="vocabularyFolder.folderId">
                                <stripes:options-collection collection="${actionBean.folders}" label="identifierLabel" value="id"/>
                            </stripes:select>
                        </div>
                        <div id="newFolderDiv">
                            <fieldset style="border: none">
                                <label for="folderIdentifier" style="float: left; width: 6em;">Identifier:</label>
                                <stripes:text id="folderIdentifier" class="smalltext" size="30" name="folder.identifier"/>
                            </fieldset>
                            <fieldset style="border: none">
                                <label for="folderLabel" style="float: left; width: 6em;">Label:</label>
                                <stripes:text id="folderLabel" class="smalltext" size="30" name="folder.label"/>
                            </fieldset>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="vocabularyFolder.identifier" id="vocabularyFolderIdentifier"/>
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
                        <stripes:text name="vocabularyFolder.baseUri" style="width: 500px;" class="smalltext" id="vocabularyFolderBaseUri"/>
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
                        <c:set var="regStatuses" value="${actionBean.vocabularyFolder.validRegStatusForVocabulary}"/>
                        <stripes:select name="vocabularyFolder.regStatus" value="${actionBean.vocabularyFolder.regStatus}">
                            <c:forEach items="${regStatuses}" var="aRegStatus">
                                <stripes:option value="${aRegStatus}" label="${aRegStatus.label}"/>
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
                    <th scope="row" class="scope-row simple_attr_title">
                        <label for="chkNotationsEqualIdentifiers" title="Enforce that concept notations in this vocabulary are always equal to concept identifiers. Saving with this checked will overwirte the notations of all sub-ordinating concepts with their identifiers!">Notations equal identifiers</label>
                    </th>
                    <td class="simple_attr_help">
                        <dd:optionalIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:checkbox name="vocabularyFolder.notationsEqualIdentifiers" id="chkNotationsEqualIdentifiers"/>
                        <stripes:hidden id="prevNotationsEqualIdentifiers" name="prevNotationsEqualIdentifiers" value="${actionBean.vocabularyFolder.notationsEqualIdentifiers}" />
                    </td>
                </tr>
                <!-- Simple attributes -->
                <c:forEach var="attributeValues" items="${actionBean.vocabularyFolder.attributes}" varStatus="outerLoop">
                    <c:set var="attrMeta" value="${attributeValues[0]}"/>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            ${attrMeta.label}
                        </th>
                        <td class="simple_attr_help">
                            <c:choose>
                                <c:when test="${attrMeta.mandatory}">
                                    <dd:mandatoryIcon />
                                </c:when>
                                <c:otherwise>
                                    <dd:optionalIcon />
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="simple_attr_value">
                            <dd:simpleAttribute fieldName="vocabularyFolder.attributes[${outerLoop.index}]"
                                attributes="${attributeValues}" uniqueId="attr${outerLoop.index}"/>
                        </td>
                    </tr>
                </c:forEach>
                <tr>
                    <th>&nbsp;</th>
                    <td colspan="2">
                        <stripes:submit name="saveFolder" value="Save" class="mediumbuttonb" onclick="doSaveFolder()"/>
                        <stripes:submit name="cancelSave" value="Cancel" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </div>
        </stripes:form>

        <!-- Bound data elements -->
        <jsp:include page="editBoundElementsInc.jsp" />

        <!-- New concept popup -->
        <jsp:include page="newConceptInc.jsp" />

        <!-- Vocabulary concepts search -->
        <h2>Vocabulary concepts</h2>

        <stripes:form method="get" id="searchForm" beanclass="${actionBean.class.name}">
            <div id="searchframe">
                <stripes:hidden name="vocabularyFolder.folderName" />
                <stripes:hidden name="vocabularyFolder.workingCopy" />
                <stripes:hidden name="origIdentifer" />
                <stripes:hidden name="vocabularyFolder.identifier" />

                <table class="datatable">
                    <colgroup>
                        <col style="width:10em;"/>
                        <col />
                        <col style="width:10em;"/>
                        <col />
                        <col />
                    </colgroup>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title" title="Text to filter from label, notation and definition">
                            <label for="filterText"><span style="white-space:nowrap;">Filtering text</span></label>
                        </th>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="30" name="filter.text" id="filterText"/>
                        </td>
                        <th scope="row" class="scope-row simple_attr_title" title="Concept's status">
                            <label for="status"><span style="white-space:nowrap;">Status</span></label>
                        </th>
                        <td class="simple_attr_value" style="padding-right: 5em;">
                            <stripes:select name="filter.conceptStatus" id="status">
                                <stripes:option value="" label="All concepts"/>
                                <stripes:options-enumeration enum="eionet.meta.dao.domain.StandardGenericStatus" label="label"/>
                            </stripes:select>
                        </td>
                        <td>
                            <c:set var="disableSearch" value="${(empty actionBean.vocabularyFolder.identifier) or not (actionBean.origIdentifier eq actionBean.vocabularyFolder.identifier)}"/>
                            <stripes:submit name="edit" value="Search" class="mediumbuttonb" disabled="${disableSearch}"/>
                        </td>
                    </tr>
                </table>
            </div>
        </stripes:form>

        <!-- Vocabulary concepts -->
        <c:url var="editIcon" value="/images/edit.gif" />
        <stripes:form method="post" id="conceptsForm" beanclass="${actionBean.class.name}">
            <display:table name="${actionBean.vocabularyConcepts}" class="datatable" id="concept" style="width:80%"
                requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.origIdentifier}/edit">
                <display:setProperty name="basic.msg.empty_list" value="No vocabulary concepts found." />
                <display:setProperty name="paging.banner.placement" value="both" />
                <display:setProperty name="paging.banner.item_name" value="concept" />
                <display:setProperty name="paging.banner.items_name" value="concepts" />

                <display:column style="width: 1%">
                    <stripes:checkbox name="conceptIds" value="${item.id}" />
                </display:column>
                <display:column title="Id" class="${actionBean.vocabularyFolder.numericConceptIdentifiers ? 'number' : ''}" style="width: 1%">
                    <c:choose>
                    <c:when test="${!concept.status.accepted}">
                        <span style="text-decoration:line-through"><c:out value="${concept.identifier}" /></span>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${concept.identifier}" />
                        </c:otherwise>
                    </c:choose>
                </display:column>
                <display:column title="Label">
                    <!-- beanClass encodes incorrectly identifiers containing '+'. Can be replaced back after upgrading to Stripes 1.5.8 -->
                    <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.origIdentifier}/${concept.identifier}/edit">
                       <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        <c:out value="${concept.label}" />
                    </stripes:link>
                    <a href="#" onClick="openPopup('#editConceptDiv${item.id}')"><img src="${editIcon}" title="Quick edit" alt="Quick edit" style="border:0" /></a>
                </display:column>
                <display:column title="Definition" escapeXml="true" property="definition" />
                <display:column title="Notation" escapeXml="true" property="notation" />
                <display:column title="Status" escapeXml="false" property="status.label" />
                <display:column title="Status Modified" escapeXml="false">
                    <fmt:formatDate value="${concept.statusModified}" pattern="dd.MM.yyyy"/>
                </display:column>
                <display:column title="Not Accepted from">
                    <c:if test="${!concept.status.accepted}">
                       <fmt:formatDate value="${concept.notAcceptedDate}" pattern="dd.MM.yyyy"/>
                    </c:if>
                </display:column>
            </display:table>
            <c:if test="${not empty actionBean.vocabularyConcepts.list}">
                <div style="padding-top: 10px;">
                    <stripes:hidden name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                    <stripes:hidden name="vocabularyFolder.identifier" />
                    <stripes:hidden name="vocabularyFolder.workingCopy" />
                    <input type="button" onclick="toggleSelectAll('conceptsForm');return false" value="Select all" name="selectAll">
                    <stripes:submit name="deleteConcepts" value="Delete" />
                    <stripes:submit name="markConceptsInvalid" value="Mark invalid" />
                    <stripes:submit name="markConceptsValid" value="Mark valid" />

                    <c:if test="${actionBean.vocabularyFolder.commonType}">
                        <button id="addNewConceptBtn">Add new concept</button>
                    </c:if>
                </div>
            </c:if>
        </stripes:form>

        <c:if test="${actionBean.vocabularyConcepts.fullListSize == 0}">
            <c:if test="${actionBean.vocabularyFolder.commonType}">
                <br />
                <button id="addNewConceptBtn">Add new concept</button>
            </c:if>
        </c:if>

        <!-- Vocabulary concept edit forms -->
        <c:forEach var="item" items="${actionBean.vocabularyConcepts.list}" varStatus="loop">
            <div id="editConceptDiv${item.id}" title="Edit concept" style="display:none">
                <stripes:form id="form${item.id}" method="post" beanclass="${actionBean.class.name}">

                    <c:set var="divId" value="editConceptDiv${item.id}" />
                    <c:if test="${actionBean.editDivId eq divId}">
                        <!--  validation errors -->
                        <stripes:errors/>
                    </c:if>

                    <div>
                        <stripes:hidden name="vocabularyFolder.workingCopy" />
                        <stripes:hidden name="vocabularyFolder.id" />
                        <stripes:hidden name="vocabularyFolder.identifier" />
                        <stripes:hidden name="vocabularyFolder.folderName" />
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
                                    <c:choose>
                                    <c:when test="${actionBean.vocabularyFolder != null && actionBean.vocabularyFolder.notationsEqualIdentifiers}">
                                        <span title="Forcefully equal to identifier in this vocabulary."><c:out value="${item.notation}"/></span>
                                    </c:when>
                                    <c:otherwise>
                                        <stripes:text class="smalltext" size="30" name="vocabularyConcepts.list[${loop.index}].notation" />
                                    </c:otherwise>
                                </c:choose>
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

	    <%-- The upload CSV dialog. Hidden unless activated. --%>
	    <div id="uploadCSVDialog" title="Upload CSV">
	        <stripes:form beanclass="${actionBean.class.name}" method="post">
	        	<stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />

	            <div class="note-msg">
                    CSV Import
                    <br><br>The CSV file should contain a header row for element names and data rows for concepts.
                    <br>It is strongly recommended to use an exported CSV file as a template for bulk editing. Columns and rows can be added to or deleted from the template file.
                    <br>A concept can be ignored by prepending a double-slash '//' to the concept row in the CSV
                    <ul>
                        Notes:
                        <li>If the header row contains unknown elements the import will aborted and data will be rolled back</li>
                        <li>Erroneous concept rows are ignored, valid data rows are still imported</li>
                        <li>Successful import cannot be undone (unless "undo checkout" is performed)</li>
                        <li>If a concept with the same identifier already exists in the vocabulary it will be overwritten</li>
                        <li>"Purge Vocabulary" option deletes all the vocabulary concepts before import</li>
                    </ul>
	            </div>

				<div>
					<stripes:checkbox id="purgeVocabularyData" name="purgeVocabularyData"/><label for="purgeVocabularyData" class="question">Purge Vocabulary Data</label>
				</div>
				<div>
					<stripes:checkbox id="purgeBoundElements" name="purgeBoundElements" disabled="true"/><label for="purgeBoundElements" class="question">Purge Bound Elements</label>
				</div>
	            <stripes:file name="uploadedFileToImport" id="fileToUpload" size="40" accept="text/csv" title="Select a .csv file to import"/>
	            <stripes:submit name="uploadCsv" value="Upload"/>
	            <input type="button" id="closeUploadCSVDialog" value="Cancel"/>

	        </stripes:form>
	    </div>

	    <%-- The upload RDF dialog. Hidden unless activated. --%>
	    <div id="uploadRDFDialog" title="Upload RDF">
	        <stripes:form id="uploadRDFForm" beanclass="${actionBean.class.name}" method="post" action="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.origIdentifier}/uploadRdf">
	        	<stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                <div class="note-msg">
				    <strong>Note</strong>
				       <ul>
				          <li>With this operation, contents of RDF file will be imported into vocabulary folder.</li>
				          <li>Only a working copy can be updated with a RDF file upload.</li>
                          <li>If user select "Purge Per Predicate" option. All seen predicates will be removed from vocabulary.</li>
                          <li>If user select "Purge All Vocabulary Data" option. All data will be removed from vocabulary.</li>
                          <li>Once import is successful, operation cannot be undone. If an error occurs during import, then all data will be rolled back.</li>
				       </ul>
				</div>

                <div>
                    <stripes:radio id="rdfDontPurge" name="rdfPurgeOption" value="1"/><label for="rdfDontPurge" class="question">Don't purge vocabulary data</label>
                </div>
                <div>
                    <stripes:radio id="rdfPurgePerPredicate" name="rdfPurgeOption" value="2"/><label for="rdfPurgePerPredicate" class="question">Purge Per Predicate</label>
                </div>
                <div>
                    <stripes:radio id="rdfPurgeVocabularyData" name="rdfPurgeOption" value="3"/><label for="rdfPurgeVocabularyData" class="question">Purge All Vocabulary Data</label>
                </div>

	            <stripes:file name="uploadedFileToImport" id="fileToUpload" size="40"  title="Select a .rdf file to import"/>
	            <stripes:submit id="uploadRdf" name="uploadRdf" value="Upload"/>
	            <input type="button" id="closeUploadRDFDialog" value="Cancel"/>
                <img id="spinningImage" src="<%=request.getContextPath()%>/images/indicator.gif" alt="Importing..." style="display: none;"/>

	        </stripes:form>
	    </div>

    </stripes:layout-component>

</stripes:layout-render>
