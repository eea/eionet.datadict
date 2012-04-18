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
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>
    
    <stripes:layout-component name="contents">
    
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
	                        <stripes:link beanclass="${actionBean.class.name}" event="checkIn">Check in
	                            <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
	                        </stripes:link>
	                    </li>
	                    <li>
	                        <stripes:link beanclass="${actionBean.class.name}" event="undoCheckout">Undo checkout
	                            <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
	                        </stripes:link>
	                    </li>
	                </c:if>
	                <c:if test="${empty actionBean.schemaSet.workingUser}">
	                    <li>
	                        <a href="#" id="newVersionLink">New version</a>
	                    </li>
	                    <li>
	                        <stripes:link beanclass="${actionBean.class.name}" event="checkOut">Check out
	                            <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
	                        </stripes:link>
	                    </li>
	                    <li>
	                        <stripes:link beanclass="${actionBean.class.name}" event="delete">Delete
	                            <stripes:param name="schemaSet.id" value="${actionBean.schemaSet.id}"/>
	                        </stripes:link>
	                    </li>
	                </c:if>
	            </ul>
	        </div>
        </c:if>
    </c:if>
    
    <h1>View schema set</h1>
    
    <div id="tabbedmenu">
        <ul>
            <c:forEach items="${actionBean.tabs}" var="tab">
                <li <c:if test="${tab.selected}">id="currenttab"</c:if>>
                    <stripes:link href="${tab.href}" title="${tab.hint}"><c:out value="${tab.title}"/></stripes:link>
                </li>
            </c:forEach>
        </ul>
    </div>

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
                <c:forEach items="${actionBean.attributes}" var="attribute">
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
        
        <div id="newVersionDialog" title="Create new version">
            <stripes:form beanclass="${actionBean.class.name}" method="post">
                
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
        
    </stripes:layout-component>

</stripes:layout-render>