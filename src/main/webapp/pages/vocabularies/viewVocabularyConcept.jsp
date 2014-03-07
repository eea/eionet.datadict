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
                        <c:if test="${actionBean.vocabularyFolder.workingCopy}">
                            <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        </c:if>
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
                    <td class="simple_attr_value" style="font-weight:bold"><stripes:link href="${actionBean.conceptUri}"><c:out value="${actionBean.conceptUri}" /></stripes:link>
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
                     <td class="simple_attr_value"><span style="white-space:pre-wrap"><c:out value="${actionBean.vocabularyConcept.definition}" /></span></td>
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
                <!-- Data element attributes -->
                <c:forEach var="elementValues" items="${actionBean.vocabularyConcept.elementAttributes}">
                    <c:set var="elementMeta" value="${elementValues[0]}"/>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">${elementMeta.name}</th>
                        <td class="simple_attr_value">
                          <ul class="stripedmenu">
                            <c:forEach var="attr" items="${elementValues}" varStatus="innerLoop">
                              <li>
                                <c:choose>
                                  <c:when test="${attr.relationalElement}">
                                    <c:choose>
                                        <c:when test="${not actionBean.vocabularyFolder.workingCopy or attr.datatype eq 'reference'}">
                                            <a href="${actionBean.conceptViewPrefix}${attr.relatedConceptRelativePath}/view"><c:out value="${attr.relatedConceptIdentifier}" />
                                            <c:if test="${not empty attr.relatedConceptLabel}">
                                                (<c:out value="${attr.relatedConceptLabel}" />)
                                            </c:if></a>
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
                              </li>
                            </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>

    </stripes:layout-component>

</stripes:layout-render>
