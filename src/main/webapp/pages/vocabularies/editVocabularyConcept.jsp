<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"  pageTitle="Edit vocabulary concept">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {

                $(".delLink").click(function() {
                    this.parentElement.remove();
                });
            });
        } ) ( jQuery );
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
                    <stripes:link beanclass="eionet.web.action.VocabularyConceptActionBean" event="view">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        <stripes:param name="vocabularyConcept.identifier" value="${actionBean.vocabularyConcept.identifier}" />
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
                            <stripes:text class="smalltext" size="30" name="vocabularyConcept.identifier" />
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
                            <stripes:text class="smalltext" size="30" name="vocabularyConcept.notation" />
                        </td>
                    </tr>

                    <%-- Additional attributes --%>
                    <tr>
                        <td colspan="3">
                            <h2>Additional attributes:</h2>
                            <table class="datatable" width="100%">
                                <colgroup>
                                    <col style="width:26%"/>
                                    <col style="width:4%"/>
                                    <col />
                                </colgroup>
                                <c:forEach var="attributeValues" items="${actionBean.vocabularyConcept.attributes}" varStatus="outerLoop">
                                <c:set var="attrMeta" value="${attributeValues[0]}"/>
                                <tr>
                                    <th scope="row" class="scope-row simple_attr_title">
                                        ${attrMeta.label}
                                    </th>
                                    <td class="simple_attr_help">
                                        <dd:optionalIcon />
                                    </td>
                                    <td class="simple_attr_value">
                                        <c:choose>
                                            <c:when test="${attrMeta.inputType == 'textarea'}">
                                                <c:choose>
                                                    <c:when test="${attrMeta.languageUsed}">
                                                        <dd:multiTextAreaLang attributes="${attributeValues}"
                                                            fieldName="vocabularyConcept.attributes[${outerLoop.index}]"
                                                            uniqueId="conceptAttr${outerLoop.index}"
                                                            attributeId="${attrMeta.attributeId}"
                                                            fieldCols="${attrMeta.width}"
                                                            fieldRows="${attrMeta.height}"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <dd:multiTextArea attributes="${attributeValues}"
                                                            fieldName="vocabularyConcept.attributes[${outerLoop.index}]"
                                                            uniqueId="conceptAttr${outerLoop.index}"
                                                            attributeId="${attrMeta.attributeId}"
                                                            fieldCols="${attrMeta.width}"
                                                            fieldRows="${attrMeta.height}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <c:choose>
                                                    <c:when test="${attrMeta.languageUsed}">
                                                        <dd:multiTextLang attributes="${attributeValues}"
                                                            fieldName="vocabularyConcept.attributes[${outerLoop.index}]"
                                                            uniqueId="conceptAttr${outerLoop.index}"
                                                            attributeId="${attrMeta.attributeId}"
                                                            fieldSize="${attrMeta.width}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <dd:multiText attributes="${attributeValues}"
                                                            fieldName="vocabularyConcept.attributes[${outerLoop.index}]"
                                                            uniqueId="conceptAttr${outerLoop.index}"
                                                            attributeId="${attrMeta.attributeId}"
                                                            fieldSize="${attrMeta.width}" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                                </c:forEach>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <th>&nbsp;</th>
                        <td colspan="2">
                            <stripes:submit name="saveConcept" value="Save" class="mediumbuttonb"/>
                        </td>
                    </tr>
                </table>
            </div>
        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>