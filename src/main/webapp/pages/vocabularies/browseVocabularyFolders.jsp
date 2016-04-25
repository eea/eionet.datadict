<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabularies" currentSection="vocabularies">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {
                $("#toggleSelectAll").click(function() {
                    toggleSelectAll('vocabulariesForm');
                    $(this).val() === "Select all" ? $("li", ".menu").removeClass("selected") : $("li", ".menu").not(".disabled").addClass("selected");
                    return false;
                });

                $("#keep-relations").dialog({
                        autoOpen: false,
                        resizable: false,
                        maxHeight: 300,
                        width: 500,
                        modal: true,
                    buttons: {
                        "Yes, replace the relations with URI's" : function() {
                            document.getElementById("txtKeepRelations").value = true;
                            $(this).dialog("close");
                            $( "#vocabulariesForm" ).attr('action', 'vocabularies/delete').submit();

                        },
                        "No, delete the relations completely": function() {
                          document.getElementById("txtKeepRelations").value = false;
                          $(this).dialog("close");
                           $( "#vocabulariesForm" ).attr('action', 'vocabularies/delete').submit();
                        }
                    }

                });

                $("#deleteBtn").click(function() {
                    var deleteOk = confirm('Are you sure you want to delete the selected vocabularies?');
                    var vocabularyIdsBaseUri = [<c:forEach items='${actionBean.vocabulariesWithBaseUri}' var='id'>'${id}',</c:forEach>];
                    var keepRelations = false;

                    if (deleteOk==true) {
                        var vocabularyIds = document.getElementsByName("folderIds");
                        var containsVocabularyWithBaseUri = false;

                        // loops checked vocabularies, if any of them has bas uri asks if relations in other vocabularies
                        // should be replaced with uri values
                        for (var i = 0, ref =  vocabularyIds.length; i < ref; i++) {
                            if (vocabularyIds[i].checked == true && vocabularyIdsBaseUri.indexOf(vocabularyIds[i].value) != -1) {
                                containsVocabularyWithBaseUri = true;
                                break;
                            }
                        }
                    }
                    if (containsVocabularyWithBaseUri==true) {
                        $('#keep-relations').dialog('open');
                        return false;
                    } else {
                        if (deleteOk==true) {
                            $( "#vocabulariesForm" ).attr('action', 'vocabularies/delete').submit();
                        }
                    }
                    return false;
                });

                $(".editFolderDiv").dialog({
                    autoOpen: false,
                    width: 800
                });

                $(".closeFolderButton").click(function() {
                    var divId = "#" + $(this).data("divid");
                    $(divId).dialog('close');
                    return false;
                });

                $(".openFolderLink").click(function() {
                    var divId = "#" + $(this).data("divid");
                    $(divId).dialog('open');
                    return false;
                });

                $("#maintainLnk").click(function() {
                    $( "#vocabulariesForm" ).attr('action', 'vocabularies/maintain').submit();
                    return false;
                });

                <c:if test="${not empty actionBean.editDivId}">
                    $("#${actionBean.editDivId}").dialog('open');
                </c:if>
            });

        })(jQuery);

        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <h1>Browse vocabularies</h1>
        <c:if test="${empty actionBean.user}">
            <p class="advise-msg">
                Note: Unauthenticated users can only see vocabularies in <em>Released</em> and <em>Public Draft</em> statuses.
            </p>
        </c:if>

        <div id="drop-operations">
            <ul>
                <li class="search"><stripes:link id="searchLnk" href="#">Search vocabularies</stripes:link></li>
                <li class="search"><stripes:link id="searchConceptLnk" href="#">Search concepts</stripes:link></li>
                <c:if test="${not empty actionBean.user && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'i')}">
                    <li class="add"><stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="add">Add vocabulary</stripes:link></li>
                </c:if>
                <c:if test="${not empty actionBean.user && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'i')}">
                    <li class="maintain"><stripes:link id="maintainLnk"  href="#">Maintain vocabularies</stripes:link></li>
                </c:if>
            </ul>
        </div>

        <c:url var="editIcon" value="/images/edit.gif" />
        <c:url var="rdfIcon" value="/images/rdf-icon.gif" />
        <stripes:form id="vocabulariesForm" beanclass="${actionBean['class'].name}" method="post" style="margin-top:1em">
            <ul class="tree-nav">
                <c:forEach var="folder" items="${actionBean.folders}">
                    <li<c:if test="${folder.expanded}"> class="expanded"</c:if>>
                        <stripes:link beanclass="${actionBean['class'].name}" class="title">
                            <stripes:param name="folderId" value="${folder.id}" />
                            <stripes:param name="expand" value="${not folder.expanded}" />
                            <stripes:param name="expanded" value="${actionBean.expanded}" />
                            <c:out value="${folder.identifier}" />
                        </stripes:link>
                        <span class="description">(<c:out value="${folder.label}"/>)</span>


                    <c:if test="${folder.expanded && not empty actionBean.user && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'u')}">
                        <a href="#" data-divid="editFolderDiv${folder.id}" class="openFolderLink" title="Edit folder">
                            <img style="border:0" src="${editIcon}" alt="Edit folder" />
                        </a>
                    </c:if>

                    <c:if test="${folder.expanded && not empty folder.items}">
                        <stripes:link beanclass="eionet.web.action.FolderActionBean" event="rdf" title="Export RDF with all vocabularies in the folder" class="export-rdf">
                            <stripes:param name="folder.identifier" value="${folder.identifier}" />
                            <img style="border:0" src="${rdfIcon}" alt="Export RDF"/>
                        </stripes:link>
                    </c:if>

                    <c:if test="${not empty folder.items}">
                        <ul class="menu">
                            <c:forEach var="item" items="${folder.items}" varStatus="itemLoop">
                                <li${item.workingCopy || not empty item.workingUser ? ' class="disabled"' : ''}>
                                    <c:if test="${not empty actionBean.user  && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'd')}">
                                        <stripes:checkbox class="selectable" name="folderIds" value="${item.id}" disabled="${item.workingCopy || not empty item.workingUser}" />
                                    </c:if>
                                    <c:choose>
                                        <c:when test="${item.draftStatus && empty actionBean.user}">
                                            <span class="link-folder" style="color:gray;">
                                                <c:out value="${item.identifier}"/>
                                                (<c:out value="${item.label}"/>)
                                                <sup style="font-size:0.7em"><c:out value="${item.regStatus}" /></sup>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" class="link-folder">
                                                <stripes:param name="vocabularyFolder.folderName" value="${item.folderName}" />
                                                <stripes:param name="vocabularyFolder.identifier" value="${item.identifier}" />
                                                <c:if test="${item.workingCopy}">
                                                    <stripes:param name="vocabularyFolder.workingCopy" value="${item.workingCopy}" />
                                                </c:if>
                                                <c:out value="${item.identifier}"/>
                                            </stripes:link>
                                            <c:if test="${item.draftStatus}">
                                                <span class="link-folder" style="color:gray;">
                                            </c:if>
                                            <span class="description">(<c:out value="${item.label}"/>)</span>
                                            <c:if test="${ddfn:contains(actionBean.statusTextsToDisplay, item.regStatus)}">
                                                <sup style="font-size:0.7em"><c:out value="${item.regStatus}" /></sup>
                                            </c:if>
                                            <c:if test="${item.draftStatus}">
                                                </span>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:if test="${item.workingCopy && actionBean.userName==item.workingUser}">
                                        <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                                    </c:if>
                                </li>
                            </c:forEach>
                         </ul>
                     </c:if>
                     <c:if test="${folder.expanded && empty folder.items}">
                        <div style="padding-left: 1em;font-style:italic;">The folder is empty</div>
                     </c:if>
                 </li>
                 </c:forEach>
            </ul>
            <stripes:hidden id="txtKeepRelations" name="keepRelationsOnDelete" value="false"></stripes:hidden>
            <c:if test="${not empty actionBean.user && actionBean.visibleEditableVocabularies && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'd')}">
                <stripes:button id="deleteBtn" name="delete" value="Delete" />
                <input type="button" id="toggleSelectAll" value="Select all" name="selectAll" />
            </c:if>
         </stripes:form>


        <%-- Editable folder popups --%>
        <c:forEach var="item" items="${actionBean.folders}" varStatus="loop">
          <c:if test="${item.expanded && not empty actionBean.user  && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'u')}">
            <div id="editFolderDiv${item.id}" title="Edit folder" class="editFolderDiv">
                <stripes:form id="form${item.id}" method="post" beanclass="${actionBean['class'].name}">

                    <c:set var="divId" value="editFolderDiv${item.id}" />
                    <c:if test="${actionBean.editDivId eq divId}">
                        <!--  validation errors -->
                        <stripes:errors/>
                    </c:if>

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
                                <stripes:hidden name="folders[${loop.index}].id" />
                                <stripes:text class="smalltext" size="30" name="folders[${loop.index}].identifier" />
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
                                <stripes:text name="folders[${loop.index}].label" style="width: 500px;" class="smalltext"/>
                            </td>
                        </tr>
                        <tr>
                            <th>&nbsp;</th>
                            <td colspan="2">
                                <stripes:submit name="saveFolder" value="Save" class="mediumbuttonb"/>
                                <stripes:submit name="deleteFolder" value="Delete" class="mediumbuttonb"/>
                                <button type="button" class="closeFolderButton" data-divid="editFolderDiv${item.id}">Cancel</button>
                            </td>
                        </tr>
                    </table>
                </stripes:form>
            </div>
          </c:if>
        </c:forEach>
        <div id="keep-relations" title="Handle relations" style="display:none">
            <p>Some of the selected vocabularies have base URI entered</p>
            <p>Do you want to replace relations in other vocabularies that are pointing to the deletable vocabulary with fully
                qualified URI's?</p>
        </div>
        <jsp:include page="searchVocabulariesInc.jsp" />
    </stripes:layout-component>
</stripes:layout-render>
