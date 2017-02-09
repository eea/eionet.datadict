<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Scheduled Task Details" >
    <stripes:layout-component name="contents">
        <div id="drop-operations">
            <ul>

                <c:if test="${not empty actionBean.user && ddfn:userHasPermission(actionBean.userName, '/vocabularies', 'i')}">
                    <li class="maintain">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="ScheduledJobsQueue"> 
                           Back To Scheduled Vocabulary Jobs Queue</stripes:link>
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
                        ${actionBean.scheduledTaskView.type}
                    </td>
                </tr>
                <c:forEach var="parameter" items="${actionBean.scheduledTaskView.taskParameters}">
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">${parameter.key}</th>
                        <td class="simple_attr_value">
                            ${parameter.value}
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
        <h2>Task Execution Details</h2>
        <div id="outerframe">
            <table class="datatable results">
                <c:set var="details" value="${actionBean.scheduledTaskView.details}"/>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">Execution Status</th>
                    <td class="simple_attr_value">
                        ${details.executionStatus}
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">Start Date</th>
                    <td class="simple_attr_value">
                        ${details.startDate}
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">End Date </th>
                    <td class="simple_attr_value">
                        ${details.endDate}
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">Next Scheduled Date</th>
                    <td class="simple_attr_value">
                        ${details.scheduledDate}
                    </td>
                </tr>
            </table>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
