<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>


<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabularies" currentSection="dataElements">

    <stripes:layout-component name="contents">
        <h1>Set Vocabulary for fixed values</h1>

        <display:table name="${actionBean.vocabularies.list}" class="datatable results" id="item" requestURI="/bindvocabulary" pagesize="20">
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No data elements found.</p>" />
            <display:column title="Vocabulary Set" sortable="true" sortProperty="folderName">
                ${item.folderName}
            </display:column>
            <display:column title="Vocabulary" sortable="true" sortProperty="label">
                <c:choose>
                    <c:when test="${item.workingCopy}">
                        <c:choose>
                            <c:when test="${actionBean.userName eq item.workingUser}">
                                <stripes:link href="/vocabulary/${item.folderName}/${item.identifier}/view">
                                    <c:out value="${item.label}"/>
                                    <stripes:param name="vocabularyFolder.workingCopy" value="${item.workingCopy}" />
                                </stripes:link>
                                <span title="Your working copy" class="checkedout">*</span>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${item.label}" /> <span title="Checked out by ${item.workingUser}" class="checkedout">*</span>
                            </c:otherwise>
                        </c:choose>
                    </c:when>

                    <c:when test="${not item.draftStatus || actionBean.userLoggedIn}">
                        <stripes:link href="/vocabulary/${item.folderName}/${item.identifier}/view"><c:out value="${item.label}" /></stripes:link>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${item.label}" />
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Status" sortable="true" sortProperty="regStatus">
                ${item.regStatus.label}
            </display:column>
            <c:choose>
                <c:when test="${item.canBeBoundToElements}">
                    <display:column title="Select" sortable="false">
                        <stripes:link beanclass="${actionBean['class'].name}" event="bind" title="Click to select the vocabulary as the source for the element values">
                            <stripes:param name="elementId" value="${actionBean.elementId}" />
                            <stripes:param name="vocabularyId" value="${item.id}" />
                            [Select]
                        </stripes:link>
                    </display:column>
                </c:when>
                <c:otherwise>
                    <display:column title="Select" sortable="false">
                        Concepts without notation detected
                    </display:column>
                </c:otherwise>
            </c:choose>
        </display:table>

    </stripes:layout-component>

</stripes:layout-render>

