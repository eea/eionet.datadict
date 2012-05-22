<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View schema set">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        $("#newVersionLink").click(function() {
                            $('#newVersionDialog').dialog('open');
                            return false;
                        });

                        $('#newVersionDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        $("#closeNewVersionDialog").click(function() {
                            $('#newVersionDialog').dialog('close');
                            return true;
                        });

                        ////////////////////

                        $("#checkInLink").click(function() {
                            $('#checkInDialog').dialog('open');
                            return false;
                        });

                        $('#checkInDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        $("#closeCheckInDialog").click(function() {
                            $('#checkInDialog').dialog('close');
                            return true;
                        });
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

    <%-- Dropdown operations menu --%>

    <c:if test="${not empty actionBean.userName}">
        <c:set var="isMyWorkingCopy" value="${actionBean.schemaSet.workingCopy && actionBean.userName==actionBean.schemaSet.workingUser}"/>
        <c:if test="${empty actionBean.schemaSet.workingUser || isMyWorkingCopy}">
            <div id="drop-operations">
                <h2>Operations:</h2>
                <ul>
                    <c:if test="${isMyWorkingCopy}">
                        <li>
                            <stripes:link beanclass="${actionBean.class.name}" event="edit">Edit metadata
                                <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
                            </stripes:link>
                        </li>
                        <li>
                            <stripes:link beanclass="${actionBean.class.name}" event="editSchemas">Edit schemas
                                <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
                            </stripes:link>
                        </li>
                        <c:choose>
                            <c:when test="${actionBean.checkInCommentsRequired}">
                                <li>
                                    <a href="#" id="checkInLink">Check in</a>
                                </li>
                            </c:when>
                            <c:otherwise>
                                <li>
                                   <stripes:link beanclass="${actionBean.class.name}" event="checkIn">Check in
                                       <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
                                   </stripes:link>
                               </li>
                            </c:otherwise>
                        </c:choose>
                        <li>
                            <stripes:link beanclass="${actionBean.class.name}" event="undoCheckout">Undo checkout
                                <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
                            </stripes:link>
                        </li>
                    </c:if>
                    <c:if test="${empty actionBean.schemaSet.workingUser && (actionBean.createAllowed || actionBean.checkoutAllowed)}">
                        <c:if test="${actionBean.createAllowed}">
                            <li>
                                <a href="#" id="newVersionLink">New version</a>
                            </li>
                        </c:if>
                        <c:if test="${actionBean.checkoutAllowed}">
                            <li>
                                <stripes:link beanclass="${actionBean.class.name}" event="checkOut">Check out
                                    <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
                                </stripes:link>
                            </li>
                        </c:if>
                    </c:if>
                </ul>
            </div>
        </c:if>
    </c:if>

    <%-- Page heading --%>

    <h1>View schema set</h1>

    <%-- Attributes div --%>

    <div id="outerframe" style="padding-top:20px">
        <table class="datatable">
            <colgroup>
                <col style="width:26%"/>
                <col style="width:4%"/>
                <col style="width:62%"/>
            </colgroup>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    Identifier
                </th>
                <td class="simple_attr_help">
                    <a href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                        <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="help"/>
                    </a>
                </td>
                <td class="simple_attr_value">
                    <c:out value="${actionBean.schemaSet.identifier}"/>
                </td>
            </tr>
            <tr>
                <th scope="row" class="scope-row simple_attr_title">
                    Registration status
                </th>
                <td class="simple_attr_help">
                    <a href="${pageContext.request.contextPath}/help.jsp?screen=dataset&amp;area=regstatus" onclick="pop(this.href);return false;">
                        <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="help"/>
                    </a>
                </td>
                <td class="simple_attr_value">
                    <c:out value="${actionBean.schemaSet.regStatus}"/>
                    <c:if test="${actionBean.userWorkingCopy}">
                        <span class="caution" title="Checked out on ${actionBean.schemaSet.dateModified}">(Working copy)</span>
                    </c:if>
                    <c:if test="${not empty actionBean.userName && not empty actionBean.schemaSet.workingUser && actionBean.userName!=actionBean.schemaSet.workingUser}">
                        <span class="caution">(checked out by <em>${actionBean.schemaSet.workingUser}</em>)</span>
                    </c:if>
                </td>
            </tr>
            <c:forEach items="${actionBean.attributes}" var="attributesEntry">
                <c:set var="attribute" value="${attributesEntry.value}"/>
                <c:if test="${not empty attribute.value}">
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            <c:out value="${attribute.shortName}"/>
                        </th>
                        <td class="simple_attr_help">
                            <a href="${pageContext.request.contextPath}/help.jsp?attrid=${attribute.ID}&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                                <img style="border:0" src="${pageContext.request.contextPath}/images/info_icon.gif" width="16" height="16" alt="Help"/>
                            </a>
                        </td>
                        <td style="word-wrap:break-word;wrap-option:emergency" class="simple_attr_value">
                            <c:if test="${not attribute.displayMultiple}">
                                <c:out value="${attribute.value}"/>
                            </c:if>
                            <c:if test="${attribute.displayMultiple}">
                                <c:out value="${ddfn:join(attribute.values, ', ')}"/>
                            </c:if>
                        </td>
                    </tr>
                </c:if>
            </c:forEach>
        </table>
    </div>

    <%-- Schemas section --%>

    <h2>Schemas</h2>

    <c:if test="${empty actionBean.schemas}">
        <div style="margin-top:3em">No schemas defined for this schema set yet!</div>
    </c:if>

    <c:if test="${not empty actionBean.schemas}">
        <display:table name="${actionBean.schemas}" class="datatable" id="schema" style="width:80%">
            <display:column title="File name">
                <stripes:link beanclass="eionet.web.action.SchemaActionBean" title="Open schema details">
                    <stripes:param name="schema.id" value="${schema.id}"/>
                    <c:out value="${schema.fileName}"/>
                </stripes:link>
            </display:column>
            <display:column title="Short description"><c:out value="${ddfn:join(schema.attributeValues['ShortDescription'],',')}"/></display:column>
        </display:table>
    </c:if>

    <%-- The section that displays versions of this schema set. --%>

    <c:if test="${not empty actionBean.otherVersions}">
        <h2>Other versions of this schema set</h2>
        <display:table name="${actionBean.otherVersions}" class="datatable" id="otherVersion" style="width:80%">
            <display:column title="Identifier">
                <stripes:link beanclass="${actionBean.class.name}" title="Open schema set details">
                    <stripes:param name="schemaSet.id" value="${otherVersion.id}"/>
                    <c:out value="${otherVersion.identifier}"/>
                </stripes:link>
                <c:if test="${actionBean.userWorkingCopy && actionBean.schemaSet.checkedOutCopyId==otherVersion.id}">
                    <span style="font-size:0.8em"><sup>(the checked-out version)</sup></span>
                </c:if>
            </display:column>
            <display:column title="Status"><c:out value="${otherVersion.regStatus}"/></display:column>
            <display:column title="Last modified">
                <fmt:formatDate value="${otherVersion.dateModified}" pattern="dd.MM.yy HH:mm:ss"/>
            </display:column>
            <c:if test="${actionBean.checkInCommentsRequired}">
                <display:column title="Comment" maxLength="30"><c:out value="${otherVersion.comment}"/></display:column>
            </c:if>
        </display:table>
    </c:if>

    <%-- The dialog for creating a new version (a div that is hidden unless activated) --%>

    <div id="newVersionDialog" title="Create new version">
        <stripes:form beanclass="${actionBean.class.name}" method="get">

            <div class="note-msg">
                <strong>Note</strong>
                <p>A new version requires a new identifier. Please enter it below.</p>
            </div>

            <stripes:text name="newIdentifier" id="txtNewIdentifier" size="30"/><br/>
            <stripes:submit name="newVersion" value="Submit"/>
            <input type="button" id="closeNewVersionDialog" value="Cancel"/>

            <div style="display:none">
                <stripes:hidden name="schemaSet.id"/>
                <stripes:hidden name="schemaSet.identifier"/>
            </div>
        </stripes:form>
    </div>

    <%-- The dialog for doing the check-in (a div that is hidden unless activated) --%>

    <div id="checkInDialog" title="Check in">
        <stripes:form beanclass="${actionBean.class.name}" method="get">

            <div class="note-msg">
                <strong>Note</strong>
                <p>A check-in comment is required. Please enter it below.</p>
            </div>

            <input type="text" name="schemaSet.comment" size="30"/><br/>
            <stripes:submit name="checkIn" value="Submit"/>
            <input type="button" id="closeCheckInDialog" value="Cancel"/>

            <div style="display:none">
                <stripes:hidden name="schemaSet.id"/>
            </div>
        </stripes:form>
    </div>

    </stripes:layout-component>

</stripes:layout-render>