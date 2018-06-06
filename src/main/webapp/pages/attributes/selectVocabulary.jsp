<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
                       pageTitle="Vocabularies"
                       currentSection="administration">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">

        <h1>Select Vocabulary</h1>

        <c:url var="expandIcon" value="/images/expand.png" />
        <c:url var="collapseIcon" value="/images/collapse.png" />
        <stripes:form beanclass="eionet.web.action.AttributeActionBean" method="post" style="margin-top:1em">
                <ul class="tree-nav">
                    <c:forEach var="folder" items="${actionBean.folders}">
                        <li<c:if test="${folder.expanded}"> class="expanded"</c:if>>
                            <stripes:link href="${actionBean.contextPath}/vocabularies/selectVocabulary" class="title">
                                <stripes:param name="folderId" value="${folder.id}" />
                                <stripes:param name="expand" value="${not folder.expanded}" />
                                <stripes:param name="expanded" value="${actionBean.expanded}" />
                                <stripes:param name="attrId" value="${actionBean.attrId}"/>
                                <c:out value="${folder.identifier}"/>
                            </stripes:link>

                            (<c:out value="${folder.label}"/>)

                            <c:if test="${not empty folder.items}">
                                <ul class="menu">
                                    <c:forEach var="item" items="${folder.items}" varStatus="itemLoop">
                                        <c:if test="${not item.workingCopy}">
                                            <li>
                                                <stripes:radio id="radio" name="vocabularyId" value="${item.id}" />
                                                <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" class="link-folder">
                                                    <stripes:param name="vocabularyFolder.folderName" value="${item.folderName}" />
                                                    <stripes:param name="vocabularyFolder.identifier" value="${item.identifier}" />
                                                    <c:out value="${item.identifier}"/>
                                                </stripes:link>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                </ul>
                            </c:if>

                            <c:if test="${folder.expanded && empty folder.items}">
                                <div style="padding-left: 1em;font-style:italic;">The folder is empty</div>
                            </c:if>
                        </li>
                    </c:forEach>
                </ul>
                <div style="text-align:center">
                    <stripes:submit class="mediumbuttonb" name="editVocabulary" value="SELECT" onclick="return validateEmptyVocab()"/>
                    <stripes:submit class="mediumbuttonb" name="edit" value="BACK TO ATTRIBUTE"/>
                </div>
                    <stripes:hidden name="attribute.id" value="${actionBean.attrId}"/>
            </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>

