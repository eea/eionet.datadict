<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View schema set" currentSection="schemas">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        $("#newVersionLink").click(function() {
                            $('#newVersionDialog').dialog('open');
                            return false;
                        });

                        $('#newVersionDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        $("#closeNewVersionDialog").click(function() {
                            $('#newVersionDialog').dialog('close');
                            return true;
                        });

                        ////////////////////

                        $("#checkInLink").click(function() {
                            $('#checkInDialog').dialog('open');
                            return false;
                        });

                        $('#checkInDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        $("#closeCheckInDialog").click(function() {
                            $('#checkInDialog').dialog('close');
                            return true;
                        });

                        ////////////////////

                        $("#uploadSchemaLink").click(function() {
                            $('#uploadSchemaDialog').dialog('open');
                            return false;
                        });

                        $('#uploadSchemaDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        $("#closeUploadSchemaDialog").click(function() {
                            $('#uploadSchemaDialog').dialog("close");
                            return true;
                        });
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <%-- Page heading --%>
        <h1>View schema</h1>

        <c:if test="${actionBean.schemaSet.deprecatedStatus}">
            <div class="system-msg">
                <strong>Note</strong>
                <p>This schema set is deprecated. It is not valid anymore!</p>
            </div>
        </c:if>

        <c:set var="schemaWorkingCopy" value="${actionBean.schemaWorkingCopy}"/>
        <c:if test="${not empty schemaWorkingCopy}">
            <div class="system-msg">
                <strong>Note</strong>
                <p>You have a
                    <stripes:link beanclass="${actionBean['class'].name}">
                        <stripes:param name="schema.fileName" value="${actionBean.schema.fileName}"/>
                        <stripes:param name="workingCopy" value="true"/>
                        working copy
                    </stripes:link> of this schema!</p>
            </div>
        </c:if>

    <%-- Dropdown operations menu --%>

    <c:if test="${not empty actionBean.userName}">

        <div id="drop-operations">
            <ul>
                <c:set var="isMySchemaWorkingCopy" value="${actionBean.schema.workingCopy && actionBean.userName==actionBean.schema.workingUser}"/>
                <c:set var="isMySchemaSetWorkingCopy" value="${!actionBean.rootLevelSchema && actionBean.mySchemaSetWorkingCopy}"/>
                <c:set var="isNonCheckedOutSchema" value="${actionBean.rootLevelSchema && empty actionBean.schema.workingUser}"/>

                <c:if test="${isMySchemaWorkingCopy || isMySchemaSetWorkingCopy || isNonCheckedOutSchema}">

                    <c:if test="${isMySchemaWorkingCopy || isMySchemaSetWorkingCopy}">
                        <li class="edit">
                            <stripes:link beanclass="${actionBean['class'].name}" event="edit">Edit metadata
                                <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}"/>
                                <stripes:param name="schema.fileName" value="${actionBean.schema.fileName}"/>
                                <stripes:param name="workingCopy" value="true"/>
                            </stripes:link>
                        </li>
                        <li class="upload">
                            <a href="#" id="uploadSchemaLink">Re-upload file</a>
                        </li>
                    </c:if>
                    <c:if test="${actionBean.rootLevelSchema}">
                        <c:if test="${isMySchemaWorkingCopy}">
                            <c:choose>
                                <c:when test="${actionBean.checkInCommentsRequired}">
                                    <li class="checkin">
                                        <a href="#" id="checkInLink">Check in</a>
                                    </li>
                                </c:when>
                                <c:otherwise>
                                    <li class="checkin">
                                        <stripes:link beanclass="${actionBean['class'].name}" event="checkIn">Check in
                                            <stripes:param name="schema.id" value="${actionBean.schema.id}"/>
                                        </stripes:link>
                                    </li>
                                </c:otherwise>
                            </c:choose>
                            <li class="checkout">
                                <stripes:link beanclass="${actionBean['class'].name}" event="undoCheckout">Undo checkout
                                    <stripes:param name="schema.id" value="${actionBean.schema.id}"/>
                                </stripes:link>
                            </li>
                        </c:if>
                        <c:if test="${isNonCheckedOutSchema && (actionBean.createAllowed || actionBean.checkoutAllowed)}">
                            <c:if test="${actionBean.createAllowed}">
                                <li class="newVersion">
                                    <a href="#" id="newVersionLink">New version</a>
                                </li>
                            </c:if>
                            <c:if test="${actionBean.checkoutAllowed}">
                                <li class="checkout">
                                    <stripes:link beanclass="${actionBean['class'].name}" event="checkOut">Check out
                                        <stripes:param name="schema.id" value="${actionBean.schema.id}"/>
                                    </stripes:link>
                                </li>
                            </c:if>
                        </c:if>
                    </c:if>
                </c:if>

                <li class="switch">
                    <stripes:link beanclass="${actionBean['class'].name}" event="validate">Validate
                        <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}"/>
                            <stripes:param name="schema.fileName" value="${actionBean.schema.fileName}"/>
                            <stripes:param name="workingCopy" value="${actionBean.schema.workingCopy || actionBean.schema.schemaSetWorkingCopy}"/>
                    </stripes:link>
                </li>
            </ul>
        </div>
    </c:if>

    <%-- Attributes div --%>

    <div id="outerframe" style="padding-top:20px">
        <table class="datatable results">
            <colgroup>
                <col style="width:30%"/>
                <col style="width:70%"/>
            </colgroup>
            <c:if test="${!actionBean.schema.workingCopy && !(actionBean.schemaSet!=null && actionBean.schemaSet.workingCopy)}">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Schema URL
                        <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=schema&amp;area=url"></a>
                    </th>
                    <td class="simple_attr_value">
                        <a href="${actionBean.schemaUrl}">
                            <c:out value="${actionBean.schemaUrl}"/>
                        </a>
                    </td>
                </tr>
            </c:if>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    File name
                    <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=schema&amp;area=filename"></a>
                </th>
                <td class="simple_attr_value">
                    <a href="${actionBean.schemaDownloadLink}">
                        <c:out value="${actionBean.schema.fileName}"/>
                    </a>
                </td>
            </tr>
            <c:if test="${!actionBean.rootLevelSchema}">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Schema set
                        <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=schema&amp;area=schemaSet"></a>
                    </th>
                    <td class="simple_attr_value">
                        <stripes:link beanclass="eionet.web.action.SchemaSetActionBean" title="Open schema set details">
                            <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}"/>
                            <stripes:param name="workingCopy" value="${actionBean.workingCopy}"/>
                            <c:out value="${actionBean.schemaSet.identifier}"/>
                        </stripes:link>
                        <c:if test="${isMySchemaSetWorkingCopy}">
                            <span class="caution" title="Checked out on ${actionBean.schemaSet.dateModified}">(Working copy)</span>
                        </c:if>
                    </td>
                </tr>
            </c:if>
            <c:if test="${actionBean.rootLevelSchema}">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Registration status
                        <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=regstatus"></a>
                    </th>
                    <td class="simple_attr_value">
                        <fmt:setLocale value="en_GB" />
                        <fmt:formatDate pattern="dd MMM yyyy HH:mm:ss" value="${actionBean.schema.dateModified}" var="dateFormatted"/>
                        <c:out value="${actionBean.schema.regStatus}"/>
                        <c:if test="${not empty actionBean.userName && actionBean.userWorkingCopy}">
                            <span class="caution" title="Checked out on ${dateFormatted}">(Working copy)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && actionBean.checkedOut && !actionBean.checkedOutByUser}">
                            <span class="caution">(checked out by <em>${actionBean.schema.workingUser}</em>)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && empty actionBean.schema.workingUser || actionBean.checkedOutByUser}">
                            <span style="color:#A8A8A8;font-size:0.8em">(checked in by ${actionBean.schema.userModified} on ${dateFormatted})</span>
                        </c:if>
                        <c:if test="${empty actionBean.userName}">
                            <span>${dateFormatted}</span>
                        </c:if>
                    </td>
                </tr>
            </c:if>
            <c:forEach items="${actionBean.attributes}" var="attributesEntry">
                <c:set var="attribute" value="${attributesEntry.value}"/>
                <c:if test="${not empty attribute.value}">
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            <c:out value="${attribute.name}"/>
                            <a class="helpButton" href="${pageContext.request.contextPath}/help.jsp?attrid=${attribute.ID}&amp;attrtype=SIMPLE"></a>
                        </th>
                        <td style="word-wrap:break-word;wrap-option:emergency" class="simple_attr_value">
                            <c:choose>
                                <c:when test="${attribute.displayType=='vocabulary'}">
                                    <c:set var="vocabularyConcepts" value="${actionBean.getVocabularyConcepts(attribute)}" />
                                    <c:set var="vocabularyBindingFolder" value="${actionBean.getVocabularyBindingFolder(attribute.ID)}" />
                                    <c:if test="${not empty vocabularyConcepts}">
                                        <ul class="stripedmenu">
                                            <c:forEach var="vocabularyConcept" items="${vocabularyConcepts}">
                                                <li>
                                                    <stripes:link href="/vocabularyconcept/${vocabularyBindingFolder.folderName}/${vocabularyBindingFolder.identifier}/${vocabularyConcept.identifier}/view" title="${vocabularyConcept.label}">
                                                        <c:out value="${vocabularyConcept.label}"/>
                                                    </stripes:link>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${not attribute.displayMultiple}">
                                        <c:out value="${attribute.value}"/>
                                    </c:if>
                                    <c:if test="${attribute.displayMultiple}">
                                        <c:out value="${ddfn:join(attribute.values, ', ')}"/>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:if>
            </c:forEach>
        </table>
    </div>

    <%-- If root-level schema, display its versions if any. --%>

    <c:if test="${actionBean.rootLevelSchema && not empty actionBean.otherVersions}">
        <h2>Other versions of this schema</h2>
        <display:table name="${actionBean.otherVersions}" class="datatable results" id="otherVersion">
            <display:column title="File name">
                <stripes:link href="/schema/root/${otherVersion.fileName}" title="Open schema details">
                    <c:out value="${otherVersion.fileName}"/>
                </stripes:link>
                <c:if test="${actionBean.userWorkingCopy && actionBean.schema.checkedOutCopyId==otherVersion.id}">
                    <span style="font-size:0.8em"><sup>(the checked-out version)</sup></span>
                </c:if>
            </display:column>
            <display:column title="Status"><c:out value="${otherVersion.regStatus}"/></display:column>
            <display:column title="Last modified">
                <fmt:formatDate value="${otherVersion.dateModified}" pattern="dd.MM.yy HH:mm:ss"/>
            </display:column>
            <c:if test="${actionBean.checkInCommentsRequired}">
                <display:column title="Comment" maxLength="30"><c:out value="${otherVersion.comment}"/></display:column>
            </c:if>
        </display:table>
    </c:if>

    <%-- Show XML CONV data. --%>
    <c:if test="${not actionBean.workingCopy && not empty actionBean.xmlConvData}">
        <p>
        There are ${actionBean.xmlConvData.numberOfQAScripts} QA scripts and ${actionBean.xmlConvData.numberOfConversions} conversion scripts registered for this schema.
        <c:if test="${actionBean.xmlConvData.numberOfQAScripts > 0 || actionBean.xmlConvData.numberOfConversions > 0}">
            <br />
            <stripes:link href="${actionBean.xmlConvData.xmlConvUrl}">
                <stripes:param name="schemaUrl" value="${actionBean.schemaUrl}" />
                Link to the schema page on XMLCONV
            </stripes:link>
        </c:if>
        </p>
    </c:if>

    <%-- Show the contents of the schema file. --%>
    <pre><c:out value="${actionBean.schemaString}" /></pre>

    <%-- The check-in dialog for root-level schemas. Hidden unless activated. --%>

    <div id="checkInDialog" title="Check in">
        <stripes:form beanclass="${actionBean['class'].name}" method="get">

            <div class="system-msg">
                <strong>Note</strong>
                <p>A check-in comment is required. Please enter it below.</p>
            </div>

            <input type="text" name="schema.comment" style="width:100%"/><br/>
            <stripes:submit name="checkIn" value="Submit"/>
            <input type="button" id="closeCheckInDialog" value="Cancel"/>

            <div style="display:none">
                <stripes:hidden name="schema.id"/>
            </div>
        </stripes:form>
    </div>

    <%-- The upload dialog. Hidden unless activated. --%>

    <div id="uploadSchemaDialog" title="Re-upload schema">
        <stripes:form beanclass="${actionBean['class'].name}" method="post">

            <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}" />
            <stripes:param name="schema.fileName" value="${actionBean.schema.fileName}" />

            <div class="system-msg">
                <strong>Note</strong>
                <p>
                    The schema's file name will not be changed, regardless of the name of the file you upload.
                    Only the file contents will be overwritten!
                </p>
            </div>

            <stripes:file name="uploadedFile" id="fileToUpload" size="40"/>
            <stripes:submit name="reupload" value="Upload"/>
            <input type="button" id="closeUploadSchemaDialog" value="Cancel"/>

            <div style="display:none">
                <stripes:hidden name="schema.id"/>
                <stripes:hidden name="workingCopy"/>
            </div>
        </stripes:form>
    </div>

    <%-- The dialog for creating a new version (relevant for root-level schemas only, hidden unless activated) --%>

    <div id="newVersionDialog" title="Create new version">
        <stripes:form beanclass="${actionBean['class'].name}" method="get">

            <div class="system-msg">
                <strong>Note</strong>
                <p>A new version requires a new file with a new name:</p>
            </div>

            <stripes:file name="uploadedFile" id="fileToUpload" size="40"/>
            <stripes:submit name="newVersion" value="Upload"/>
            <input type="button" id="closeNewVersionDialog" value="Cancel"/>

            <div style="display:none">
                <stripes:hidden name="schema.id"/>
                <stripes:hidden name="schema.fileName"/>
            </div>
        </stripes:form>
    </div>

    </stripes:layout-component>

</stripes:layout-render>
