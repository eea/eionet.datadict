<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabulary">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFoldersActionBean">
                        <stripes:param name="folderId" value="${actionBean.vocabularyFolder.folderId}" />
                        <stripes:param name="expand" value="true" />
                        <stripes:param name="expanded" value="" />
                                Back to set
                    </stripes:link>
                </li>
                <c:if test="${not empty actionBean.user}">
                  <c:if test="${not actionBean.vocabularyFolder.siteCodeType}">
                      <li>
                          <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="add">
                              <stripes:param name="copyId" value="${actionBean.vocabularyFolder.id}" />
                              Create new copy
                          </stripes:link>
                      </li>
                  </c:if>
                  <c:if test="${actionBean.userWorkingCopy}">
                  <li>
                      <a href="#" id="addNewConceptLink">Add new concept</a>
                  </li>
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="edit">
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                          Edit vocabulary
                      </stripes:link>
                  </li>
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkIn">
                          <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                          Check in
                      </stripes:link>
                  </li>
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="undoCheckOut">
                          <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          Undo checkout
                      </stripes:link>
                  </li>
                  </c:if>
                  <c:if test="${not actionBean.vocabularyFolder.workingCopy}">
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkOut">
                          <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                          Check out
                      </stripes:link>
                  </li>
                  </c:if>
                </c:if>
            </ul>
        </div>

        <h1>Vocabulary: <em><c:out value="${actionBean.vocabularyFolder.label}" /></em></h1>

        <c:if test="${actionBean.vocabularyFolder.workingCopy && actionBean.vocabularyFolder.siteCodeType}">
            <div class="note-msg">
                <strong>Notice</strong>
                <p>
                For checked out site codes, vocabulary concepts are not visible. To view them, see the
                <stripes:link href="/services/siteCodes">site codes page</stripes:link>.
                </p>
            </div>
        </c:if>

        <c:if test="${actionBean.checkedOutByUser}">
            <div class="note-msg">
                <strong>Note</strong>
                <p>You have a
                    <stripes:link beanclass="${actionBean['class'].name}" event="viewWorkingCopy">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}"/>
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}"/>
                        working copy
                    </stripes:link> of this vocabulary!</p>
            </div>
        </c:if>

        <c:if test="${not actionBean.vocabularyFolder.draftStatus && not actionBean.vocabularyFolder.workingCopy}">
        <c:url var="rdfIconUrl" value="/images/rdf-icon.gif" />
        <c:url var="csvIconUrl" value="/images/csv_icon_sm.gif" />
        <c:url var="codelistIconUrl" value="/images/inspire_icon.gif" />
        <c:url var="jsonIconUrl" value="/images/json_file_icon.gif" />
        <div id="createbox" style="clear:right">
            <table id="outputsmenu">
                <tr>
                    <td style="width:73%">Get RDF output of this vocabulary</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="rdf" title="Export RDF">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${rdfIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
                <tr>
                    <td style="width:73%">Get CSV output of this vocabulary</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="csv" title="Export CSV">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${csvIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
                <tr>
                    <td style="width:73%">Get XML output in INSPIRE codelist format</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="codelist" title="Export XML in INSPIRE codelist format">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${codelistIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
                <tr>
                    <td style="width:73%">Get JSON-LD output of this vocabulary</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="json" title="Export JSON">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${jsonIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
            </table>
        </div>
        </c:if>

        <!-- Vocabulary folder -->
        <div id="outerframe" style="padding-top:20px">
            <table class="datatable">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Folder
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.folderName} (${actionBean.vocabularyFolder.folderLabel})" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.identifier}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Label
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.label}" />
                    </td>
                </tr>
                <c:if test="${not empty actionBean.vocabularyFolder.baseUri}">
                  <tr>
                      <th scope="row" class="scope-row simple_attr_title">
                          Base URI
                      </th>
                      <td class="simple_attr_value">
                          <c:out value="${actionBean.vocabularyFolder.baseUri}" />
                      </td>
                  </tr>
                </c:if>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Registration status
                    </th>
                    <td class="simple_attr_value">
                        <fmt:setLocale value="en_GB" />
                        <fmt:formatDate pattern="dd MMM yyyy HH:mm:ss" value="${actionBean.vocabularyFolder.dateModified}" var="dateFormatted"/>
                        <c:out value="${actionBean.vocabularyFolder.regStatus}"/>
                        <c:if test="${not empty actionBean.userName && actionBean.userWorkingCopy}">
                            <span class="caution" title="Checked out on ${dateFormatted}">(Working copy)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && actionBean.checkedOutByOther}">
                            <span class="caution">(checked out by <em>${actionBean.vocabularyFolder.workingUser}</em>)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && empty actionBean.vocabularyFolder.workingUser || actionBean.checkedOutByUser}">
                            <span style="color:#A8A8A8;font-size:0.8em">(checked in by ${actionBean.vocabularyFolder.userModified} on ${dateFormatted})</span>
                        </c:if>
                        <c:if test="${empty actionBean.userName}">
                            <span>${dateFormatted}</span>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Type
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.type.label}" />
                    </td>
                </tr>
                <!-- Simple attributes -->
                <c:forEach var="attributeValues" items="${actionBean.vocabularyFolder.attributes}">
                    <c:set var="attrMeta" value="${attributeValues[0]}"/>
                    <c:if test="${not empty attrMeta.value}">
                      <tr>
                          <th scope="row" class="scope-row simple_attr_title">${attrMeta.label}</th>
                          <td class="simple_attr_value">
                              <ul class="stripedmenu">
                                <c:forEach var="attr" items="${attributeValues}" varStatus="innerLoop">
                                  <li>
                                    <span style="white-space:pre-wrap"><c:out value="${attr.value}" /></span>
                                  </li>
                                </c:forEach>
                              </ul>
                          </td>
                      </tr>
                    </c:if>
                </c:forEach>
            </table>
        </div>

        <c:if test="${actionBean.userWorkingCopy}">
            <jsp:include page="newConceptInc.jsp" />
        </c:if>

        <!-- Vocabulary concepts search -->
        <h2>Vocabulary concepts</h2>
        <stripes:form method="get" id="searchForm" beanclass="${actionBean['class'].name}">
            <div id="searchframe">
                <stripes:hidden name="vocabularyFolder.folderName" />
                <stripes:hidden name="vocabularyFolder.identifier" />
                <stripes:hidden name="vocabularyFolder.workingCopy" />
                <table class="datatable" width="100%">
                    <c:if test="${fn:length(actionBean.boundElements)>0}">
                        <link type="text/css" media="all" href="<c:url value="/css/spinner.css"/>"  rel="stylesheet" />
                        <tr>
                            <td>
                                <table class="addFilter">
                                    <tr>
                                        <th scope="row" title="Add a filter from the list">
                                            <label for="addFilter"><span style="white-space:nowrap;">Add filter</span></label>
                                        </th>
                                        <td>
                                            <select id="addFilter">
                                                <option value=""></option>
                                                <c:forEach var="boundElement" items="${actionBean.boundElements}">
                                                    <option value="${boundElement.id}"<c:if test="${ddfn:contains(actionBean.boundElementFilterIds, boundElement.id)}">disabled="disabled"</c:if>><c:out value="${boundElement.identifier}" /></option>
                                                </c:forEach>
                                            </select>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </c:if>
                    <tr>
                        <td>
                            <table id="filtersTable">
                                <tr>
                                    <th scope="row" class="scope-row simple_attr_title" title="Text to filter from label, notation and definition">
                                        <label for="filterText"><span style="white-space:nowrap;">Filtering text</span></label>
                                    </th>
                                    <td class="simple_attr_value">
                                        <stripes:text class="smalltext" size="30" name="filter.text" id="filterText"/>
                                    </td>
                                </tr>
                                <tr>
                                    <th scope="row" class="scope-row simple_attr_title" title="Concept's status">
                                        <label for="status"><span style="white-space:nowrap;">Status</span></label>
                                    </th>
                                    <td class="simple_attr_value" style="padding-right: 5em;">
                                        <stripes:select name="filter.conceptStatusInt" id="status">
                                            <stripes:option value="255" label="All concepts"/>
                                            <stripes:options-collection collection="<%=eionet.meta.dao.domain.StandardGenericStatus.valuesAsList()%>" label="label" value="value"/>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <c:forEach items="${actionBean.boundElementFilters}" var="boundElementFilter" varStatus="loop">
                                    <tr id="boundElementFilterRow-${boundElementFilter.id}" data-filter-id="${boundElementFilter.id}" class="boundElementFilter">
                                        <th scope="row" class="scope-row simple_attr_title" title="${fn:escapeXml(boundElementFilter.label)}">
                                            <label for="boundElementFilter-${boundElementFilter.id}"><span style="white-space:nowrap;"><c:out value="${boundElementFilter.label}" /></span></label>
                                        </th>
                                        <td class="simple_attr_value" style="padding-right: 5em;">
                                            <stripes:hidden name="filter.boundElements[${loop.index}].id" value="${boundElementFilter.id}" class="boundElementFilterId" />
                                            <stripes:select name="filter.boundElements[${loop.index}].value" class="boundElementFilterSelect">
                                                <stripes:option value="" label="All" />
                                                <stripes:options-map map="${boundElementFilter.options}" />
                                            </stripes:select>
                                            <c:url var="delIcon" value="/images/button_remove.gif" />
                                            <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <tr id="visibleColumnsRow">
                                    <th scope="row" title="Columns">
                                        <label for="visibleColumns"><span style="white-space:nowrap;">Columns</span></label>
                                    </th>
                                    <td colspan="2">
                                        <stripes:select name="filter.visibleColumns" id="visibleColumns" multiple="multiple" class="buckets">
                                            <stripes:options-collection collection="${actionBean.columns}" />
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <th scope="row" class="scope-row simple_attr_title" title="Show concept's definition">
                                        <label for="visibleDefinition"><span style="white-space:nowrap;">Show definition</span></label>
                                    </th>
                                    <td class="simple_attr_value">
                                        <stripes:checkbox name="filter.visibleDefinition" id="visibleDefinition" />
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td>
                                        <stripes:submit name="view" value="Search" class="mediumbuttonb"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </div>
            <script type="text/javascript" src="<c:url value="/scripts/jquery.balloon.min.js" />"></script>
            <script type="text/javascript">
            // <![CDATA[
                (function($) {
                    $(document).ready(function() {
                        $("#addFilter").change(function() {
                            if ($(this).val()==="") {
                                return;
                            }
                            $("#visibleColumnsRow").before('<div class="spinner-loader">Loading...</div>');
                            $.ajax({
                                url: '/datadict/vocabulary',
                                data: { 
                                    'boundElementFilterId': $(this).val(),
                                    'boundElementFilterIndex': $(".boundElementFilter").length,
                                    'vocabularyFolderId': ${actionBean.vocabularyFolder.id},
                                    '_eventName': 'constructBoundElementFilter'
                                },
                                success:function(data) {
                                    $("#visibleColumnsRow").prev("div.spinner-loader").remove();
                                    $("#visibleColumnsRow").before(data);
                                },
                                error: function() {
                                    $("#visibleColumnsRow").prev("div.spinner-loader").removeClass().addClass("ajaxError").text("Something went wrong. Please try again.");
                                    setTimeout(function(){
                                        $("#visibleColumnsRow").prev("div.ajaxError").remove();
                                    }, 2000);
                                }
                            });
                            var $selectedOption = $("#addFilter option:selected");
                            $selectedOption.prop("disabled", true);
                            $(this).val("");
                        });
                        
                        $("a.delLink").live("click", function() {
                            var $row = $(this).closest("tr.boundElementFilter");
                            var filterId = $row.data("filterId");
                            $('#addFilter option[value=' + filterId +']').prop('disabled', false);
                            $row.remove();

                            // recalculate bound element names
                            $('.boundElementFilterId').each(function(index) {
                                $(this).attr("name", "filter.boundElements[" + index + "].id");
                            });
                            $('.boundElementFilterSelect').each(function(index) {
                                $(this).attr("name", "filter.boundElements[" + index + "].value");
                            });
                            
                            return false;
                        });

                        $(".conceptDefinition").balloon({
                            css: {
                                border: 'solid 1px #000',
                                padding: '10px',
                                backgroundColor: '#f6f6f6',
                                color: '#000'
                            }
                        });

                        applyMultipleSelectDropdowns();
                        function applyMultipleSelectDropdowns() {
                            //private functions
                            function initMultipleSelects(originalSelect, fromSelect, toSelect) {
                                for(var i = 0, length = originalSelect.options.length; i<length; i++) {
                                    var option = originalSelect.options[i];
                                    var newOption = option.cloneNode(true);
                                    newOption.selected=false;
                                    if (option.selected) {
                                        toSelect.appendChild(newOption);
                                    } else {
                                        fromSelect.appendChild(newOption);
                                    }
                                }
                            }

                            function transferSelected(originalSelect, fromSelect, toSelect) {
                                for(var i = fromSelect.options.length - 1; i>=0; i--) {
                                    var option = fromSelect.options[i];
                                    if (option.selected) {
                                        toSelect.appendChild(fromSelect.removeChild(option));
                                    }
                                }
                            }

                            function applySelected(originalSelect, cloneSelect) {
                                for(var i=0, length=originalSelect.options.length; i<length; i++) {
                                    var selectedOption = false;
                                    for(var j=0, lengthj=cloneSelect.options.length; j<lengthj; j++) {
                                        if (originalSelect.options[i].value==cloneSelect.options[j].value) {
                                            selectedOption = true;
                                            break;
                                        }
                                    }
                                    originalSelect.options[i].selected = selectedOption;
                                }
                            }

                            function findSelect(button)    {return button.parentNode.parentNode.getElementsByTagName("select")[0]}
                            function findFromSelect(button){return button.parentNode.parentNode.getElementsByTagName("select")[1]}
                            function findToSelect(button)  {return button.parentNode.parentNode.getElementsByTagName("select")[2]}

                            var selects = document.getElementsByTagName("select");
                            for (var i = selects.length-1; i >=0; i--) {
                                if (selects[i].getAttribute("multiple") &&
                                    selects[i].className.indexOf("buckets")>=0) {
                                    var select = selects[i];
                                    select.style.display='none';

                                    var fromSelect;
                                    var toSelect;

                                    /*hack for IE which has a problem creating form elements http://www.webdeveloper.com/forum/archive/index.php/t-34494.html */
                                    try{
                                        fromSelect = document.createElement('<select multiple>');
                                        toSelect = document.createElement('<select multiple>');
                                    } catch(e) {
                                        fromSelect = document.createElement('select');
                                        toSelect = document.createElement('select');
                                    }

                                    fromSelect.setAttribute("multiple", "multiple");
                                    fromSelect.multiple = true;
                                    fromSelect.setAttribute("size", 7);
                                    fromSelect.className="left";
                                    toSelect.setAttribute("multiple", "multiple");
                                    toSelect.setAttribute("size", 7);
                                    toSelect.multiple = true;
                                    toSelect.className="right";

                                    initMultipleSelects(select, fromSelect, toSelect);

                                    var removeButton = document.createElement("input");
                                    var addButton = document.createElement("input");
                                    removeButton.setAttribute("type", "button");
                                    removeButton.setAttribute("value", "Remove");
                                    removeButton.className="button moveup";
                                    addButton.setAttribute("type", "button");
                                    addButton.setAttribute("value", "Add");
                                    addButton.className="button movedown";
                                    var par = document.createElement("p");
                                    par.className="middle";
                                    removeButton.onclick=function() {
                                        transferSelected(findSelect(this), findToSelect(this), findFromSelect(this));
                                        applySelected(findSelect(this), findToSelect(this));
                                    };
                                    addButton.onclick=function() {
                                        transferSelected(findSelect(this), findFromSelect(this), findToSelect(this));
                                        applySelected(findSelect(this), findToSelect(this));
                                    };
                                    select.parentNode.appendChild(fromSelect);
                                    par.appendChild(addButton);
                                    par.appendChild(removeButton);
                                    select.parentNode.appendChild(par);
                                    select.parentNode.appendChild(toSelect);
                                    select.parentNode.className='cartcontainer';
                                }
                            }
                        }
                    });
                }) (jQuery);
            // ]]>
            </script>
        </stripes:form>
        <%-- Vocabulary concepts --%>
        <div style="overflow: auto;">
        <display:table name="actionBean.vocabularyConcepts" class="datatable" id="concept"
            style="width:80%" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view"
            excludedParams="view vocabularyFolder.identifier vocabularyFolder.folderName">
            <display:setProperty name="basic.msg.empty_list" value="No vocabulary concepts found." />
            <display:setProperty name="paging.banner.placement" value="both" />
            <display:setProperty name="paging.banner.item_name" value="concept" />
            <display:setProperty name="paging.banner.items_name" value="concepts" />

            <display:column title="Id" class="${actionBean.vocabularyFolder.numericConceptIdentifiers ? 'number' : ''}" style="width: 10%" media="html">
                <c:choose>
                    <c:when test="${!concept.status.accepted}">
                        <span style="text-decoration:line-through"><c:out value="${concept.identifier}" /></span>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${concept.identifier}" />
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Preferred label" media="html">
                <c:choose>
                    <c:when test="${not actionBean.vocabularyFolder.workingCopy}">
                        <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${concept.identifier}/view" title="${concept.label}">
                            <stripes:param name="facet" value="HTML Representation"/> <!-- Discourage people from copy-paste of the link -->
                            <dd:attributeValue attrValue="${concept.label}" attrLen="40"/>
                        </stripes:link>
                    </c:when>
                    <c:otherwise>
                        <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${concept.identifier}/view" title="${concept.label}">
                            <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                            <dd:attributeValue attrValue="${concept.label}" attrLen="40"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
                <c:if test="${actionBean.filter.visibleDefinition}">
                    <c:choose>
                        <c:when test="${not empty concept.definition}">
                            <p><span class="conceptDefinition" title="${fn:escapeXml(concept.definition)}">Definition</span></p>
                        </c:when>
                        <c:otherwise>
                            <p><span class="noDefinition">No definition available</span></p>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </display:column>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Status')}">
                <display:column title="Status" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${concept.status.label}"/>
                </display:column>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Status Modified')}">
                <display:column title="Status Modified" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${concept.statusModified}" pattern="dd.MM.yyyy"/>
                </display:column>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Notation')}">
                <display:column title="Notation" escapeXml="true" property="notation" style="width: 10%"/>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Accepted Date')}">
                <display:column title="Accepted Date" escapeXml="false">
                    <fmt:formatDate value="${concept.acceptedDate}" pattern="dd.MM.yyyy" />
                </display:column>
            </c:if>
            <c:if test="${ddfn:contains(actionBean.filter.visibleColumns, 'Not Accepted Date')}">
                <display:column title="Not Accepted Date" escapeXml="false">
                    <fmt:formatDate value="${concept.notAcceptedDate}" pattern="dd.MM.yyyy" />
                </display:column>
            </c:if>
            <c:forEach var="boundElementIdentifier" items="${actionBean.filter.boundElementVisibleColumns}">
                <display:column title="${fn:escapeXml(boundElementIdentifier)}" escapeXml="false">
                    <c:forEach var="elementValues" items="${concept.elementAttributes}">
                        <c:set var="elementMeta" value="${elementValues[0]}"/>
                        <c:if test="${elementMeta.identifier == boundElementIdentifier}">
                            <c:forEach var="attr" items="${elementValues}" varStatus="innerLoop">
                                <c:choose>
                                    <c:when test="${attr.relationalElement}">
                                      <c:choose>
                                          <c:when test="${not actionBean.vocabularyFolder.workingCopy or attr.datatype eq 'reference'}">
                                              <stripes:link href="/vocabularyconcept/${attr.relatedConceptRelativePath}/view">
                                                  <c:out value="${attr.relatedConceptIdentifier}" />
                                                  <c:if test="${not empty attr.relatedConceptLabel}">
                                                      (<c:out value="${attr.relatedConceptLabel}" />)
                                                  </c:if>
                                                  <c:if test="${not empty attr.relatedConceptVocSet}">
                                                      in <c:out value="${attr.relatedConceptVocSet}/${attr.relatedConceptVocabulary}" />
                                                  </c:if>
                                                </stripes:link>
                                          </c:when>
                                          <c:otherwise>
                                            <stripes:link beanclass="eionet.web.action.VocabularyConceptActionBean">
                                                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                                                <stripes:param name="vocabularyConcept.identifier" value="${attr.relatedConceptIdentifier}" />
                                                <dd:attributeValue attrValue="${attr.relatedConceptLabel}" attrLen="40" />
                                            </stripes:link>
                                          </c:otherwise>
                                      </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                          <dd:linkify value="${attr.attributeValue}" /><c:if test="${not empty attr.attributeLanguage}"> [${attr.attributeLanguage}]</c:if>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:if>
                    </c:forEach>
                </display:column>
            </c:forEach>
        </display:table>
        </div>
    <%-- The section that displays versions of this vocabulary. --%>

    <c:if test="${not empty actionBean.vocabularyFolderVersions}">
        <h2>Other versions of this vocabulary</h2>
        <display:table name="${actionBean.vocabularyFolderVersions}" class="datatable" id="item" style="width:80%">
            <display:column title="Label">
                <c:choose>
                    <c:when test="${item.draftStatus && empty actionBean.user}">
                        <span class="link-folder" style="color:gray;">
                            <c:out value="${item.label}"/>
                        </span>
                    </c:when>
                    <c:otherwise>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" class="link-folder">
                            <stripes:param name="vocabularyFolder.folderName" value="${item.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${item.identifier}" />
                            <c:if test="${item.workingCopy}">
                                <stripes:param name="vocabularyFolder.workingCopy" value="${item.workingCopy}" />
                            </c:if>
                            <c:out value="${item.label}"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
                <c:if test="${item.workingCopy && actionBean.userName==item.workingUser}">
                    <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                </c:if>
            </display:column>
            <display:column title="Status"><c:out value="${item.regStatus}"/></display:column>
            <display:column title="Last modified">
                <fmt:formatDate value="${item.dateModified}" pattern="dd.MM.yy HH:mm:ss"/>
            </display:column>
        </display:table>
    </c:if>

    </stripes:layout-component>

</stripes:layout-render>
