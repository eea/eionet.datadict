<%@ page import="eionet.meta.dao.domain.StandardGenericStatus" %>
<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"  pageTitle="Edit vocabulary concept" currentSection="vocabularies">

<stripes:layout-component name="head">
    <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {

                $(".delLink").click(function() {
                    clearSysMsg();
                    this.parentElement.remove();
                    return false;
                });

                openPopup = function(divId) {
                    $(divId).dialog('open');
                    return false;
                }

                closePopup = function(divId) {
                    $(divId).dialog("close");
                    return false;
                }

                clearSysMsg = function() {
                    $("#sysMsgDiv").remove();
                }
            });
        } ) ( jQuery );

        window.onload = function(){
            new JsDatePick({
                useMode:2,
                target:"txtStatusModified",
                dateFormat:"%d.%m.%Y",
                cellColorScheme:"eea",
                imgPath:"<c:url value='/css/jscalendar/img/'/>"
            });

            new JsDatePick({
                useMode:2,
                target:"txtAcceptedDate",
                dateFormat:"%d.%m.%Y",
                cellColorScheme:"eea",
                imgPath:"<c:url value='/css/jscalendar/img/'/>"
            });

            new JsDatePick({
                useMode:2,
                target:"txtNotAcceptedDate",
                dateFormat:"%d.%m.%Y",
                cellColorScheme:"eea",
                imgPath:"<c:url value='/css/jscalendar/img/'/>"
            });


        };

        // ]]>
    </script>
</stripes:layout-component>

<stripes:layout-component name="contents">
    <h1>Vocabulary concept</h1>

    <div id="drop-operations">
        <ul>
            <li class="back">
                <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="edit">
                    <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                    <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                    <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                    Back to vocabulary
                </stripes:link>
            </li>
            <li class="view">
                <!-- beanClass usage interprets some symbols incorrect because of a Stripes bug. Will be fixed in Stripes 1.5.8 -->
                <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${actionBean.vocabularyConcept.identifier}/view">
                    <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                    View concept
                </stripes:link>
            </li>
        </ul>
    </div>

    <stripes:form id="editForm" method="post" beanclass="${actionBean['class'].name}">

        <div>
            <stripes:hidden name="vocabularyFolder.folderName" />
            <stripes:hidden name="vocabularyFolder.identifier" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />
            <stripes:hidden name="vocabularyFolder.id" />
            <stripes:hidden name="vocabularyFolder.numericConceptIdentifiers" />
            <stripes:hidden name="vocabularyConcept.id" />
            <stripes:hidden name="vocabularyConcept.vocabularyId" />
            <stripes:hidden name="vocabularyConcept.identifier" />
            <stripes:hidden name="vocabularyConcept.originalConceptId" />
            <stripes:hidden id="txtEditDivId" name="editDivId" />

            <table class="datatable results">
                <colgroup>
                    <col style="width:26%"/>
                    <col style="width:4%"/>
                    <col />
                </colgroup>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Concept URI</th>
                    <td></td>
                    <td class="simple_attr_value"><c:out value="${actionBean.conceptUri}" />
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
                        <stripes:text class="smalltext" size="30" name="conceptIdentifier" />
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
                        <stripes:text name="vocabularyConcept.label" style="width: 500px;" class="smalltext"/>
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
                        <stripes:textarea name="vocabularyConcept.definition" rows="3" cols="60" class="smalltext"/>
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
                                <span title="Forcefully equal to identifier in this vocabulary."><c:out value="${actionBean.vocabularyConcept.notation}"/></span>
                            </c:when>
                            <c:otherwise>
                                <stripes:text class="smalltext" size="30" name="vocabularyConcept.notation" />
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Status</th>
                    <td class="simple_attr_help"></td>
                    <td class="simple_attr_value">
                        <c:set var="statuses" value="<%=StandardGenericStatus.uiValues()%>"/>
                        <stripes:select name="vocabularyConcept.status" value="${actionBean.vocabularyConcept.status}">
                            <c:forEach items="${statuses}" var="aStatus">
                                <stripes:option value="${aStatus}" label="${aStatus.label}"/>
                            </c:forEach>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Status Modified</th>
                    <td class="simple_attr_help"></td>
                    <td class="simple_attr_value">
                        <stripes:text id="txtStatusModified" formatType="date" formatPattern="dd.MM.yyyy" name="vocabularyConcept.statusModified" class="smalltext" size="12"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Accepted Date</th>
                    <td class="simple_attr_help"></td>
                    <td class="simple_attr_value">
                        <stripes:text id="txtAcceptedDate" formatType="date" formatPattern="dd.MM.yyyy" name="vocabularyConcept.acceptedDate" class="smalltext" size="12"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Not Accepted Date</th>
                    <td class="simple_attr_help"></td>
                    <td class="simple_attr_value">
                        <stripes:text id="txtNotAcceptedDate" formatType="date" formatPattern="dd.MM.yyyy" name="vocabularyConcept.notAcceptedDate" class="smalltext" size="12"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Delete Contact from all elements</th>
                    <td class="simple_attr_help"></td>
                    <td class="simple_attr_value">
                        <stripes:select name="vocabularyConcept.deleteContactFromAllElements" class="small">
                            <stripes:option value="no" label="no" />
                            <stripes:option value="yes" label="yes" />
                        </stripes:select>
                    </td>
                </tr>
                    <%-- Additional attributes --%>
                <!-- Data element attributes -->
                <c:forEach var="elementValues" items="${actionBean.vocabularyConcept.elementAttributes}" varStatus="outerLoop">
                    <c:set var="attrMeta" value="${elementValues[0]}"/>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                                ${attrMeta.name}
                        </th>
                        <td class="simple_attr_help">
                            <dd:optionalIcon />
                        </td>
                        <td class="simple_attr_value">
                            <c:choose>
                                <c:when test="${attrMeta.fixedValuesElement}">
                                    <dd:elementMultiSelect
                                            dataElements="${elementValues}"
                                            elementId="${attrMeta.id}"
                                            fieldName="vocabularyConcept.elementAttributes[${outerLoop.index}]"
                                            uniqueId="conceptElement${outerLoop.index}" fixedValues="${attrMeta.fixedValues}"/>
                                </c:when>
                                <c:when test="${attrMeta.datatype eq 'localref'}">
                                    <dd:relatedElemConcepts  dataElements="${elementValues}"
                                                             vocabularyConcepts="${actionBean.vocabularyConcepts}"
                                                             elementId="${attrMeta.id}"
                                                             fieldName="vocabularyConcept.elementAttributes[${outerLoop.index}]"
                                                             uniqueId="conceptElement${outerLoop.index}" />
                                </c:when>
                                <c:when test="${attrMeta.datatype eq 'reference'}">
                                    <dd:relatedReferenceConcepts dataElements="${elementValues}"
                                                                 elementId="${attrMeta.id}"
                                                                 fieldName="vocabularyConcept.elementAttributes[${outerLoop.index}]"
                                                                 elemVocName="${actionBean.elemVocabularyNames[outerLoop.index]}"
                                                                 uniqueId="conceptElement${outerLoop.index}" />
                                </c:when>

                                <c:when test="${!attrMeta.fixedValuesElement and attrMeta.languageUsed}">
                                    <dd:elementMultiTextLang dataElements="${elementValues}"
                                                             fieldName="vocabularyConcept.elementAttributes[${outerLoop.index}]"
                                                             uniqueId="conceptElement${outerLoop.index}"
                                                             elementId="${attrMeta.id}"/>
                                </c:when>
                                <c:otherwise>
                                    <dd:elementMultiText dataElements="${elementValues}"
                                                         fieldName="vocabularyConcept.elementAttributes[${outerLoop.index}]"
                                                         uniqueId="conceptElement${outerLoop.index}"
                                                         elementId="${attrMeta.id}"/>
                                </c:otherwise>
                            </c:choose>
                            <stripes:hidden name="vocabularyConcept.elementAttributes[${outerLoop.index}][0].id"/>
                            <stripes:hidden name="vocabularyConcept.elementAttributes[${outerLoop.index}][0].name"/>
                        </td>
                    </tr>
                </c:forEach>
                <tr>
                    <td>&nbsp;</td>
                    <td colspan="2">
                        <stripes:submit id="saveButton" name="saveConcept" value="Save" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </div>
    </stripes:form>

    <c:if test="${not empty actionBean.contactDetails}">
        <!-- attribute values -->
        <div>
            <table class="datatable results">
                <thead>
                <tr>
                    <th>Type of actor</th>
                    <th>DataElement id</th>
                    <th>Type</th>
                    <th>Dataset short name</th>
                    <th>Dataset identifier</th>
                    <th>DataElement dataset id</th>
                    <th>DataElement short name</th>
                    <th>DataElement identifier</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="contactRecord" items="${actionBean.contactDetails}" varStatus="loop">
                    <tr>
                        <td class="simple_attr_value"><c:out value="${contactRecord.mAttributeName}" /></td>
                        <c:choose>
                            <c:when test="${contactRecord.parentType eq 'Dataset'}">
                                <td style="font-weight:bold"><stripes:link href="/datasets/${contactRecord.dataElemId}"><c:out value="${contactRecord.dataElemId}" /></stripes:link></td>
                            </c:when>
                            <c:otherwise>
                                <td style="font-weight:bold"><stripes:link href="/dataelements/${contactRecord.dataElemId}"><c:out value="${contactRecord.dataElemId}" /></stripes:link></td>
                            </c:otherwise>
                        </c:choose>
                        <td class="simple_attr_value"><c:out value="${contactRecord.parentType}" /></td>
                        <td class="simple_attr_value"><span style="white-space:pre-wrap"><c:out value="${contactRecord.datasetShortName}" /></span></td>
                        <td class="simple_attr_value"><span style="white-space:pre-wrap"><c:out value="${contactRecord.datasetIdentifier}" /></span></td>
                        <c:choose>
                            <c:when test="${not empty contactRecord.dataElementDatasetId}">
                                <td style="font-weight:bold"><stripes:link href="/datasets/${contactRecord.dataElementDatasetId}"><c:out value="${contactRecord.dataElementDatasetId}" /></stripes:link></td>
                            </c:when>
                            <c:otherwise>
                                <td style="font-weight:bold"></td>
                            </c:otherwise>
                        </c:choose>
                        <td class="simple_attr_value"><span style="white-space:pre-wrap"><c:out value="${contactRecord.dataElementShortName}" /></span></td>
                        <td class="simple_attr_value"><span style="white-space:pre-wrap"><c:out value="${contactRecord.dataElementIdentifier}" /></span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <!-- search concepts popup -->
    <jsp:include page="searchConceptsInc.jsp" />

</stripes:layout-component>

</stripes:layout-render>
