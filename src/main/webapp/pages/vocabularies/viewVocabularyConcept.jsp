<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Vocabulary">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="view">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        Back to vocabulary
                    </stripes:link>
                </li>
                <c:if test="${actionBean.vocabularyFolder.workingCopy}">
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyConceptActionBean" event="edit">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        <stripes:param name="vocabularyConcept.identifier" value="${actionBean.vocabularyConcept.identifier}" />
                        Edit concept
                    </stripes:link>
                </li>
                </c:if>
            </ul>
        </div>

        <h1>Vocabulary concept</h1>

        <!-- Vocabulary folder -->
        <div id="outerframe" style="padding-top: 20px">
            <table class="datatable">
                <col style="min-width:8em"/>
                <col />
                <tr>
                    <th scope="row" class="scope-row simple_attr_title" style="min-width:7em">
                        Concept URI</th>
                    <td class="simple_attr_value" style="font-weight:bold"><c:out value="${actionBean.conceptUri}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Preferred label</th>
                    <td class="simple_attr_value"><c:out value="${actionBean.vocabularyConcept.label}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Definition</th>
                    <td class="simple_attr_value"><c:out value="${actionBean.vocabularyConcept.definition}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Notation</th>
                    <td class="simple_attr_value"><c:out value="${actionBean.vocabularyConcept.notation}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title" style="background-color: #FFFFFF;"><br/>Additional attributes</th>
                    <td></td>
                </tr>
                <c:forEach var="attributeValues" items="${actionBean.vocabularyConcept.attributes}">
                    <c:set var="attrMeta" value="${attributeValues[0]}"/>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">${attrMeta.label}</th>
                        <td class="simple_attr_value">
                            <c:forEach var="attr" items="${attributeValues}" varStatus="innerLoop">
                                <c:out value="${attr.value}" />
                                <c:if test="${not empty attr.language}">[${attr.language}]</c:if>
                                <c:if test="${fn:length(attributeValues) - innerLoop.index - 1 >= 1}">
                                    <hr />
                                </c:if>
                            </c:forEach>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>

    </stripes:layout-component>

</stripes:layout-render>
