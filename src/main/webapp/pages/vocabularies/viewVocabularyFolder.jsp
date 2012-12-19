<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Vocabulary">

    <stripes:layout-component name="contents">

        <c:if test="${not empty actionBean.user}">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="add">
                        <stripes:param name="copyId" value="${actionBean.vocabularyFolder.id}" />
                        Create new copy
                    </stripes:link>
                </li>
                <c:if test="${actionBean.userWorkingCopy}">
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="edit">
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        Edit vocabulary
                    </stripes:link>
                </li>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkIn">
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        Check in
                    </stripes:link>
                </li>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="undoCheckOut">
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        Undo checkout
                    </stripes:link>
                </li>
                </c:if>
                <c:if test="${not actionBean.vocabularyFolder.workingCopy}">
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkOut">
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        Check out
                    </stripes:link>
                </li>
                </c:if>
            </ul>
        </div>
        </c:if>

        <h1>Vocabulary</h1>

        <c:if test="${actionBean.checkedOutByUser}">
            <div class="note-msg">
                <strong>Note</strong>
                <p>You have a
                    <stripes:link beanclass="${actionBean.class.name}" event="viewWorkingCopy">
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}"/>
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}"/>
                        working copy
                    </stripes:link> of this vocabulary!</p>
            </div>
        </c:if>

        <!-- Vocabulary folder -->
        <div id="outerframe" style="padding-top:20px">
            <table class="datatable">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.identifier}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Label
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.label}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Base URI
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.baseUri}" />
                    </td>
                </tr>
                <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    Registration status
                </th>
                <td class="simple_attr_value">
                    <fmt:setLocale value="en_GB" />
                    <fmt:formatDate pattern="dd MMM yyyy HH:mm:ss" value="${actionBean.vocabularyFolder.dateModified}" var="dateFormatted"/>
                    <c:out value="${actionBean.vocabularyFolder.regStatus}"/>
                    <c:if test="${not empty actionBean.userName && actionBean.userWorkingCopy}">
                        <span class="caution" title="Checked out on ${dateFormatted}">(Working copy)</span>
                    </c:if>
                    <c:if test="${not empty actionBean.userName && actionBean.checkedOutByOther}">
                        <span class="caution">(checked out by <em>${actionBean.vocabularyFolder.workingUser}</em>)</span>
                    </c:if>
                    <c:if test="${not empty actionBean.userName && empty actionBean.vocabularyFolder.workingUser || actionBean.checkedOutByUser}">
                        <span style="color:#A8A8A8;font-size:0.8em">(checked in by ${actionBean.vocabularyFolder.userModified} on ${dateFormatted})</span>
                    </c:if>
                    <c:if test="${empty actionBean.userName}">
                        <span>${dateFormatted}</span>
                    </c:if>
                </td>
            </tr>
            </table>
        </div>

        <%-- Vocabulary concepts --%>

        <display:table name="${actionBean.vocabularyConcepts}" class="datatable" id="item" style="width:80%">
            <display:setProperty name="basic.msg.empty_list" value="No vocabulary concepts found." />
            <display:column title="Identifier" property="identifier" />
            <display:column title="Label" property="label" />
            <display:column title="Definition" property="definition" />
            <display:column title="Notation" property="notation" />
        </display:table>

    <%-- The section that displays versions of this schema set. --%>

    <c:if test="${not empty actionBean.vocabularyFolderVersions}">
        <h2>Other versions of this vocabulary</h2>
        <display:table name="${actionBean.vocabularyFolderVersions}" class="datatable" id="item" style="width:80%">
            <display:column title="Label">
                <c:choose>
                    <c:when test="${item.draftStatus && empty actionBean.user}">
                        <span class="link-folder" style="color:gray;">
                            <c:out value="${item.label}"/>
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
                <c:if test="${item.workingCopy && actionBean.userName==item.workingUser}">
                    <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                </c:if>
            </display:column>
            <display:column title="Status"><c:out value="${item.regStatus}"/></display:column>
            <display:column title="Last modified">
                <fmt:formatDate value="${item.dateModified}" pattern="dd.MM.yy HH:mm:ss"/>
            </display:column>
        </display:table>
    </c:if>

    </stripes:layout-component>

</stripes:layout-render>