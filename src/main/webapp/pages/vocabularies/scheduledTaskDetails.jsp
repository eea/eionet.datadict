<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Task details" >
    <stripes:layout-component name="contents">
        <script type="text/javascript">
            (function($) {
                $(document).ready(function () {
                    jav
                });
            })(jQuery);
        </script>

        <h1>Task details</h1>
        <div id="drop-operations">
            <ul>
                <c:if test="${not empty actionBean.user && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'i')}">
                    <li class="back">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="viewScheduledJobs"> 
                            Back to scheduled jobs
                        </stripes:link>
                    </li>
                </c:if>
            </ul>
        </div>

        <h2>Task parameters</h2>
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

        <h2> Task execution result</h2>
        <div id="outerframe">
            <table class="datatable results">
                <tr>
                    <td class="simple_attr_value">
                        ${fn:escapeXml(actionBean.scheduledTaskView.taskResult)}
                    </td>
                </tr>
            </table>
        </div>

        <h2>Task execution details</h2>
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
        <div class="deleteTaskButton">
            <stripes:form id="deleteScheduledJob" method="post" beanclass="${actionBean['class'].name}" style="padding-top:20px">
                <stripes:param name="scheduledTaskId" value="${actionBean.scheduledTaskId}" />
                <stripes:submit name="deleteScheduledJob" value="Delete" class="mediumbuttonb" id="delete"/>
            </stripes:form>
        </div>

        <div class="editTaskButton">
            <stripes:form id="editScheduledJob" method="post" beanclass="${actionBean['class'].name}" style="padding-top:20px">
                <stripes:param name="scheduledTaskId" value="${actionBean.scheduledTaskId}" />
                <stripes:submit name="editScheduledJob" value="Edit" class="mediumbuttonb" id="edit"/>
            </stripes:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
