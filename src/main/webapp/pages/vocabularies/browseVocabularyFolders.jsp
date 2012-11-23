<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Vocabularies">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="add">Add vocabulary</stripes:link></li>
            </ul>
        </div>

        <h1>Browse vocabularies</h1>

        <c:if test="${empty actionBean.vocabularyFolders}">
            <div style="margin-top:1em">
                No vocabularies found!
                <c:if test="${empty actionBean.user}">
                    <br/>
                    Please note that unauthenticated users can only see vocabularies in Released status.
                </c:if>
            </div>
        </c:if>

        <ul class="menu">
            <c:forEach var="item" items="${actionBean.vocabularyFolders}">
                <li>
                    <c:choose>
                        <c:when test="${item.draftStatus && empty actionBean.user}">
                            <span class="link-folder" style="color:gray;">
                                <c:out value="${item.label}"/>&nbsp;<sup style="font-size:0.7em">(<c:out value="${item.regStatus}" />)</sup>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" class="link-folder">
                                <stripes:param name="vocabularyFolder.identifier" value="${item.identifier}" />
                                <stripes:param name="vocabularyFolder.workingCopy" value="${item.workingCopy}" />
                                <c:out value="${item.label}"/>
                            </stripes:link>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${not empty actionBean.userName && item.workingCopy && actionBean.userName==item.workingUser}">
                        <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                    </c:if>
                </li>
            </c:forEach>
         </ul>

    </stripes:layout-component>

</stripes:layout-render>