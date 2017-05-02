<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View scheduled jobs" currentSection="vocabularies">
    <stripes:layout-component name="head">
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>View scheduled jobs</h1>

        <display:table name="actionBean.futureScheduledTaskViews" class="datatable results" id="featureScheduledTask"
                       requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#futureScheduledSynchronizationJobs">
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No scheduled Jobs found.</p>" />
            <display:setProperty name="paging.banner.item_name" value="featureScheduledTask" />
            <display:setProperty name="paging.banner.items_name" value="featureScheduledTasks" />
            <display:column title="Task Id"  escapeXml="false" style="width: 15%">
                <dd:attributeValue attrValue="${featureScheduledTask.details.taskId}"/>
            </display:column>
            <display:column title="Task Type" escapeXml="false" style="width: 10%">
                <dd:attributeValue attrValue="${featureScheduledTask.type} "/>
            </display:column>
            <display:column title="Last Execution Status" escapeXml="false" style="width: 15%">
                <dd:attributeValue attrValue="${featureScheduledTask.details.executionStatus}"/>
            </display:column>
            <display:column title="Last Execution End Date" escapeXml="false" style="width: 10%">
                <fmt:formatDate value="${featureScheduledTask.details.endDate}" pattern="dd.MM.yyyy hh:mm"/>
            </display:column>
            <display:column title="Schedule Interval And Unit" escapeXml="false" style="width: 10%">
                <dd:attributeValue attrValue="${featureScheduledTask.taskParameters['scheduleInterval']} ${featureScheduledTask.taskParameters['scheduleIntervalUnit']}(S)"/>
            </display:column>
            <display:column title="Additional Task Details" escapeXml="false" style="width: 15% !important">
                <c:out value="${featureScheduledTask.additionalDetails}" />
            </display:column>           
            <display:column title="Actions" escapeXml="false" style="width: 10%">
                <stripes:form id="scheduleVocabularySync" method="post" beanclass="${actionBean['class'].name}" >
                    <stripes:param name="scheduledTaskId" value="${featureScheduledTask.details.taskId}" />
                    <stripes:submit value="Details" name="viewScheduledTaskDetails" class="mediumbuttonb"/>
                    <stripes:submit value="Edit" name="editScheduledJob"  class="mediumbuttonb"/>
                    <stripes:submit value="History" name="viewScheduledJobHistory" class="mediumbuttonb"/>
                    <stripes:submit value="Delete" name="deleteScheduledJob" onclick="return confirm('Scheduled Job will be Deleted')" id="delete" class="mediumbuttonb"/>
                </stripes:form>
            </display:column>
        </display:table>
    </stripes:layout-component>
</stripes:layout-render>
