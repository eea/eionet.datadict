<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"  pageTitle="Edit vocabulary concept">

<stripes:layout-component name="head">
    <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {

                $(".delLink").click(function() {
                    clearSysMsg();
                    this.parentElement.remove();
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
                target:"txtObsoleteDate",
                dateFormat:"%d.%m.%Y",
                cellColorScheme:"eea",
                imgPath:"<c:url value='/css/jscalendar/img/'/>"
            });
            new JsDatePick({
                useMode:2,
                target:"txtCreatedDate",
                dateFormat:"%d.%m.%Y",
                cellColorScheme:"eea",
                imgPath:"<c:url value='/css/jscalendar/img/'/>"
            });


        };

        // ]]>
    </script>
</stripes:layout-component>

<stripes:layout-component name="contents">

    <div id="drop-operations">
        <h2>Operations:</h2>
        <ul>
            <li>
                <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="edit">
                    <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                    <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                    <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                    Back to vocabulary
                </stripes:link>
                <!-- beanClass usage interprets some symbols incorrect because of a Stripes bug. Will be fixed in Stripes 1.5.8 -->
                <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${actionBean.vocabularyConcept.identifier}/view">
                    <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                    View concept
                </stripes:link>
            </li>
        </ul>
    </div>

    <h1>Vocabulary concept</h1>

    <stripes:form id="editForm" method="post" beanclass="${actionBean.class.name}">

        <div>
            <stripes:hidden name="vocabularyFolder.folderName" />
            <stripes:hidden name="vocabularyFolder.identifier" />
            <stripes:hidden name="vocabularyFolder.workingCopy" />
            <stripes:hidden name="vocabularyFolder.id" />
            <stripes:hidden name="vocabularyFolder.numericConceptIdentifiers" />
            <stripes:hidden name="vocabularyConcept.id" />
            <stripes:hidden name="vocabularyConcept.vocabularyId" />
            <stripes:hidden name="vocabularyConcept.identifier" />
            <stripes:hidden id="txtEditDivId" name="editDivId" />



            <table class="datatable">
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
                        Created</th>
                    <td class="simple_attr_help"></td>
                    <td class="simple_attr_value">
                        <stripes:text id="txtCreatedDate" formatType="date" formatPattern="dd.MM.yyyy" name="vocabularyConcept.created" class="smalltext" size="12"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Obsolete</th>
                    <td class="simple_attr_help"></td>
                    <td class="simple_attr_value">
                        <stripes:text id="txtObsoleteDate" formatType="date" formatPattern="dd.MM.yyyy" name="vocabularyConcept.obsolete" class="smalltext" size="12"/>
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
                        <c:choose>
                            <c:when test="${actionBean.vocabularyConcept.obsolete != null}">
                                <stripes:submit name="unMarkConceptObsolete" value="Remove obsolete status" class="mediumbuttonb"/>
                            </c:when>
                            <c:otherwise>
                                <stripes:submit name="markConceptObsolete" value="Mark obsolete" class="mediumbuttonb"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </table>
        </div>
    </stripes:form>


    <!-- search concepts popup -->
    <jsp:include page="searchConceptsInc.jsp" />

</stripes:layout-component>

</stripes:layout-render>
