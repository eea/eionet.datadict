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
                <!-- beanClass usage interprets some symbols incorrect because of a Stripes bug. Will be fixed in Stripes 1.5.8 -->
                <li>
                    <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${actionBean.vocabularyConcept.identifier}/edit">
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        <c:out value="${item.label}" />
                        Edit concept
                    </stripes:link>
                </li>
                </c:if>
            </ul>
        </div>

        <h1>Concept: <em><c:out value="${actionBean.vocabularyConcept.label}" /></em> in the <em><c:out value="${actionBean.vocabularyFolder.identifier}" /></em> vocabulary</h1>

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
                    <th scope="row" class="scope-row simple_attr_title">
                        Created</th>
                    <td class="simple_attr_value">
                        <fmt:formatDate pattern="dd.MM.yyyy" value="${actionBean.vocabularyConcept.created}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Obsolete</th>
                    <td class="simple_attr_value"><fmt:formatDate pattern="dd.MM.yyyy" value="${actionBean.vocabularyConcept.obsolete}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title" style="background-color: #FFFFFF;"><br/>Additional attributes</th>
                    <td></td>
                </tr>

                <!-- Data element attributes -->
                <c:forEach var="elementValues" items="${actionBean.vocabularyConcept.elementAttributes}">
                    <c:set var="elementMeta" value="${elementValues[0]}"/>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">${elementMeta.name}</th>
                        <td class="simple_attr_value">
                            <c:forEach var="attr" items="${elementValues}" varStatus="innerLoop">
                                <c:choose>
                                  <c:when test="${attr.relationalElement}">
                                      <c:choose>
                                       <c:when test="${not actionBean.vocabularyFolder.workingCopy}">
                                            <a href="${actionBean.uriPrefix}${attr.relatedConceptIdentifier}"><c:out value="${actionBean.uriPrefix}${attr.relatedConceptIdentifier}" /></a>
                                            <c:if test="${not empty attr.relatedConceptLabel}">
                                                (<c:out value="${attr.relatedConceptLabel}" />)
                                            </c:if>
                                       </c:when>
                                       <c:otherwise>
                                        <stripes:link beanclass="eionet.web.action.VocabularyConceptActionBean">
                                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                                            <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                                            <stripes:param name="vocabularyConcept.identifier" value="${attr.relatedConceptIdentifier}" />
                                            <c:out value="${attr.relatedConceptLabel}" />
                                         </stripes:link>
                                       </c:otherwise>
                                       </c:choose>
                                  </c:when>
                                  <c:otherwise>
                                      <dd:linkify value="${attr.attributeValue}" /><c:if test="${not empty attr.attributeLanguage}"> [${attr.attributeLanguage}]</c:if>
                                  </c:otherwise>
                                </c:choose>
                                <c:if test="${fn:length(elementValues) - innerLoop.index - 1 >= 1}">
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
