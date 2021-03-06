<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Scheduled Task Details" >
    <stripes:layout-component name="contents">
      <h1>Task History details</h1>
        <div id="drop-operations">
            <ul>
                <c:if test="${not empty actionBean.user && ddfn:userHasPermission(actionBean.user, '/vocabularies', 'i')}">
                    <li class="maintain">
                        <stripes:link  href="${pageContext.request.contextPath}/vocabulary/viewScheduledJobHistory?scheduledTaskId=${fn:escapeXml(actionBean.scheduledTaskId)}">
                            Back to scheduled Task History
                        </stripes:link>
                    </li>
                </c:if>
            </ul>
        </div>
        <h2>Task Parameters</h2>

        <div id="outerframe">
            <table class="datatable results">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">Task Type</th>
                    <td class="simple_attr_value">
                        ${fn:escapeXml(actionBean.scheduledTaskView.type)}
                    </td>
                </tr>
                <c:forEach var="parameter" items="${actionBean.scheduledTaskView.taskParameters}">
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">${fn:escapeXml(parameter.key)}</th>
                        <td class="simple_attr_value">
                            ${fn:escapeXml(parameter.value)}
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>

        <h2> Task Execution Result</h2>
        <div id="outerframe">
            <table class="datatable results">
                <tr>
                    <td class="simple_attr_value">
                        ${fn:escapeXml(actionBean.scheduledTaskView.taskResult)}
                    </td>
                </tr>
            </table>
        </div>
        <h2>Task Execution Details</h2>
        <div id="outerframe">
            <table class="datatable results">
                <c:set var="details" value="${actionBean.scheduledTaskView.details}"/>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">Execution Status</th>
                    <td class="simple_attr_value">
                        ${fn:escapeXml(details.executionStatus)}
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">Start Date</th>
                    <td class="simple_attr_value">
                        ${fn:escapeXml(details.startDate)}
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">End Date </th>
                    <td class="simple_attr_value">
                        ${fn:escapeXml(details.endDate)}
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">Next Scheduled Date</th>
                    <td class="simple_attr_value">
                        ${fn:escapeXml(details.scheduledDate)}
                    </td>
                </tr>

            </table>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
