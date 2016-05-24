<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
                       pageTitle="Vocabularies">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">

        <h1>Select Vocabulary</h1>

        <c:url var="expandIcon" value="/images/expand.png" />
        <c:url var="collapseIcon" value="/images/collapse.png" />
        <stripes:form beanclass="eionet.datadict.action.AttributeActionBean" method="post" style="margin-top:1em">
            <div class="tree-nav">
                <ul class="menu">
                    <c:forEach var="folder" items="${actionBean.folders}">
                        <li>
                            <stripes:link href="${actionBean.contextPath}/vocabularies/selectVocabulary">
                                <stripes:param name="folderId" value="${folder.id}" />
                                <stripes:param name="expand" value="${not folder.expanded}" />
                                <stripes:param name="expanded" value="${actionBean.expanded}" />
                                <stripes:param name="attrId" value="${actionBean.attrId}"/>
                                <c:choose>
                                    <c:when test="${folder.expanded}"><img style="border:0" src="${collapseIcon}" alt="Collapse" /></c:when>
                                    <c:otherwise><img style="border:0" src="${expandIcon}" alt="Expand" /></c:otherwise>
                                </c:choose>
                            </stripes:link>

                            <stripes:link href="${actionBean.contextPath}" class="${folder.expanded ? 'expanded' : 'collapsed'}">
                                <stripes:param name="folderId" value="${folder.id}" />
                                <stripes:param name="expand" value="${not folder.expanded}" />
                                <stripes:param name="expanded" value="${actionBean.expanded}" />
                                <stripes:param name="attrId" value ="${actionBean.attrId}"/>
                                <c:out value="${folder.identifier}" />
                            </stripes:link>
                            (<c:out value="${folder.label}"/>)

                            <c:if test="${not empty folder.items}">
                                <ul class="menu" style="margin-left: 1.2em">
                                    <c:forEach var="item" items="${folder.items}" varStatus="itemLoop">
                                        <li class="zebra${itemLoop.index % 2 != 0 ? 'odd' : 'even'}">
                                            <c:if test="${not item.workingCopy}">
                                                <stripes:radio id="radio" name="viewModel.vocabularyId" value="${item.id}" />
                                                <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" class="link-folder">
                                                    <stripes:param name="vocabularyFolder.folderName" value="${item.folderName}" />
                                                    <stripes:param name="vocabularyFolder.identifier" value="${item.identifier}" />
                                                    <c:out value="${item.identifier}"/>
                                                </stripes:link>
                                            </c:if>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:if>

                            <c:if test="${folder.expanded && empty folder.items}">
                                <div style="padding-left: 1em;font-style:italic;">The folder is empty</div>
                            </c:if>
                        </li>
                    </c:forEach>
                </ul>
                <stripes:submit class="mediumbuttonb" name="selectVocabulary" value="SELECT" onclick="return validateEmptyVocab()"/>
                <stripes:submit class="mediumbuttonb" name="edit" value="BACK TO ATTRIBUTE"/>
                <stripes:hidden name="attrId"/>
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>

