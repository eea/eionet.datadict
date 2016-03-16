<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>


<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabulary Concepts" currentSection="vocabularies">

    <stripes:layout-component name="contents">
        <h1>Vocabulary Concepts</h1>

        <c:if test="${empty actionBean.user}">
            <p class="advise-msg">
                Note: Unauthenticated users can only see vocabulary concepts of vocabularies in <em>Released</em> and <em>Public Draft</em> statuses.
            </p>
        </c:if>

        <div id="drop-operations">
            <ul>
                <li class="back"><stripes:link href="/vocabularies" event="form">Back to vocabularies</stripes:link></li>
                <li class="search"><stripes:link id="searchConceptLnk" href="#">Search again</stripes:link></li>
            </ul>
        </div>

        <display:table name="${actionBean.vocabularyConceptResult}" class="results" id="item" requestURI="/vocabularies/searchConcepts" pagesize="20" style="width:100%">
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No vocabulary concepts found.</p>" />
            <display:column title="Vocabulary Set" sortable="true" sortProperty="vocabularySetIdentifier" >
                <c:out value="${item.vocabularySetIdentifier}" />
            </display:column>
            <display:column title="Vocabulary" sortable="true" sortProperty="vocabularyIdentifier" >
                <c:choose>
                    <c:when test="${item.workingCopy}">
                        <c:choose>
                            <c:when test="${actionBean.userLoggedIn}">
                                <c:choose>
                                    <c:when test="${item.userName eq actionBean.userName}">
                                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" title="${item.vocabularyLabel}">
                                            <stripes:param name="vocabularyFolder.folderName" value="${item.vocabularySetIdentifier}" />
                                            <stripes:param name="vocabularyFolder.identifier" value="${item.vocabularyIdentifier}" />
                                            <stripes:param name="vocabularyFolder.workingCopy" value="true" />
                                            <dd:attributeValue attrValue="${item.vocabularyIdentifier}" attrLen="30"/>
                                        </stripes:link>
                                        <span title="Your checked out copy" class="checkedout">*</span>
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${item.vocabularyIdentifier}"/> <span title="Checked out by ${item.userName}" class="checkedout">*</span>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${item.vocabularyIdentifier}" />
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:when test="${item.vocabularyStatus eq 'DRAFT' and not actionBean.userLoggedIn}">
                        <c:out value="${item.vocabularyIdentifier}" />
                    </c:when>
                    <c:otherwise>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" title="${item.vocabularyLabel}">
                            <stripes:param name="vocabularyFolder.folderName" value="${item.vocabularySetIdentifier}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${item.vocabularyIdentifier}" />
                            <dd:attributeValue attrValue="${item.vocabularyIdentifier}" attrLen="30"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Vocabulary Concept" sortable="true" sortProperty="identifier">
                <c:choose>
                    <c:when test="${item.workingCopy}">
                        <c:choose>
                            <c:when test="${actionBean.userLoggedIn}">
                                <c:choose>
                                    <c:when test="${item.userName eq actionBean.userName}">
                                        <stripes:link href="/vocabularyconcept/${item.vocabularySetIdentifier}/${item.vocabularyIdentifier}/${item.identifier}/view">
                                            <dd:attributeValue attrValue="${item.identifier}" attrLen="30"/>
                                            <stripes:param name="vocabularyFolder.workingCopy" value="true" />
                                        </stripes:link> <span title="Your checked out copy" class="checkedout">*</span>
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${item.identifier}" /> <span title="Checked out by ${item.userName}" class="checkedout">*</span>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${item.identifier}" />
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:when test="${item.vocabularyStatus eq 'DRAFT' and not actionBean.userLoggedIn}">
                        <c:out value="${item.identifier}" />
                    </c:when>
                    <c:otherwise>
                        <stripes:link href="/vocabularyconcept/${item.vocabularySetIdentifier}/${item.vocabularyIdentifier}/${item.identifier}/view">
                            <dd:attributeValue attrValue="${item.identifier}" attrLen="30"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Label" sortable="true" sortProperty="label">
                <c:out value="${item.label}" />
            </display:column>
            <display:column title="Status" sortable="true" sortProperty="vocabularyStatus">
                <c:out value="${item.vocabularyStatus.label}" />
            </display:column>
        </display:table>
    <jsp:include page="searchVocabulariesInc.jsp" />
    </stripes:layout-component>

</stripes:layout-render>

