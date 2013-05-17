<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Vocabularies">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {

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

                <c:if test="${not empty actionBean.editDivId}">
                    $("#${actionBean.editDivId}").dialog('open');
                </c:if>
            });

        } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <c:if test="${not empty actionBean.user}">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="add">Add vocabulary</stripes:link></li>
            </ul>
        </div>
        </c:if>

        <h1>Browse vocabularies</h1>

        <c:if test="${empty actionBean.user}">
            <div style="margin-top:1em">
                Please note that unauthenticated users can only see vocabularies in Released status.
            </div>
        </c:if>

        <c:url var="expandIcon" value="/images/img_plus.gif" />
        <c:url var="collapseIcon" value="/images/img_minus.gif" />
        <c:url var="editIcon" value="/images/edit.gif" />
        <c:url var="rdfIcon" value="/images/rdf-icon.gif" />
        <stripes:form id="vocabulariesForm" beanclass="${actionBean.class.name}" method="post" style="margin-top:1em">
            <ul class="menu">
                <c:forEach var="folder" items="${actionBean.folders}">
                    <li>
                        <stripes:link beanclass="${actionBean.class.name}">
                            <stripes:param name="folderId" value="${folder.id}" />
                            <stripes:param name="expand" value="${not folder.expanded}" />
                            <stripes:param name="expanded" value="${actionBean.expanded}" />
                            <c:choose>
                                <c:when test="${folder.expanded}"><img style="border:0" src="${collapseIcon}" alt="Collapse" /></c:when>
                                <c:otherwise><img style="border:0" src="${expandIcon}" alt="Expand" /></c:otherwise>
                            </c:choose>
                        </stripes:link>

                        <stripes:link beanclass="${actionBean.class.name}">
                            <stripes:param name="folderId" value="${folder.id}" />
                            <stripes:param name="expand" value="${not folder.expanded}" />
                            <stripes:param name="expanded" value="${actionBean.expanded}" />
                            <c:out value="${folder.label}" />
                        </stripes:link>
                    </li>

                    <c:if test="${folder.expanded && not empty actionBean.user}">
                        <a href="#" data-divid="editFolderDiv${folder.id}" class="openFolderLink"><img style="border:0" src="${editIcon}" alt="Edit folder" /></a>
                    </c:if>

                    <c:if test="${not empty folder.items}">

                        <c:if test="${folder.expanded}">
                        <stripes:link beanclass="${actionBean.class.name}" event="rdf">
                            <stripes:param name="folderId" value="${folder.id}" />
                            <img style="border:0" src="${rdfIcon}" alt="Export RDF" />
                        </stripes:link>
                        </c:if>

                        <ul class="menu" style="padding-left: 1em">
                            <c:forEach var="item" items="${folder.items}" varStatus="itemLoop">
                                <li class="zebra${itemLoop.index % 2 != 0 ? 'odd' : 'even'}">
                                    <c:if test="${not empty actionBean.user}">
                                        <stripes:checkbox name="folderIds" value="${item.id}" disabled="${item.workingCopy || not empty item.workingUser}" />
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
                                                (<c:out value="${item.label}"/>)
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
                        The folder is empty
                     </c:if>
                 </c:forEach>
             </ul>
             <c:if test="${not empty actionBean.user}">
                 <stripes:submit name="delete" value="Delete" onclick="return confirm('Are you sure you want to delete the selected vocabularies?');"/>
                 <input type="button" onclick="toggleSelectAll('vocabulariesForm');return false" value="Select all" name="selectAll" />
             </c:if>
         </stripes:form>


         <%-- Editable folder popups --%>
        <c:forEach var="item" items="${actionBean.folders}" varStatus="loop">
            <div id="editFolderDiv${item.id}" title="Edit folder" class="editFolderDiv">
                <stripes:form id="form${item.id}" method="post" beanclass="${actionBean.class.name}">

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
        </c:forEach>

    </stripes:layout-component>

</stripes:layout-render>
