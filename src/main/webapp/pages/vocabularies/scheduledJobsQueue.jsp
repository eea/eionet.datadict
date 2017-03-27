<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schedule Vocabulary Synchronization" currentSection="vocabularies">

    <stripes:layout-component name="head">
        <script type="text/javascript">
            window.setTimeout(function () {
                document.location.reload(true);
            }, 60000);
            (function ($) {
                $(document).ready(function () {
                    $('#scheduledTask').dataTable();
                    $('#pastScheduledTask').dataTable();
                    document.getElementById("defaultOpen").click();
                });
            })(jQuery);
        </script>
        <script>jQuery.noConflict();</script>
        <script>
            function openDataView(evt, cityName) {
                var i, tabcontent, tablinks;
                tabcontent = document.getElementsByClassName("tabcontent");
                for (i = 0; i < tabcontent.length; i++) {
                    tabcontent[i].style.display = "none";
                }
                tablinks = document.getElementsByClassName("tablinks");
                for (i = 0; i < tablinks.length; i++) {
                    tablinks[i].className = tablinks[i].className.replace(" active", "");
                }
                document.getElementById(cityName).style.display = "block";
                evt.currentTarget.className += " active";
            }
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Scheduled Jobs Queue</h1>
        <div id="drop-operations">
            <ul >
                <li class="scheduleJobsTab search " ><a href="javascript:void(0)" class="tablinks " onclick="openDataView(event, 'scheduledSynchronizationJobs')" id="defaultOpen">Current Scheduled Jobs</a></li>
                <li class="scheduleJobsHistoryTab search "><a  href="javascript:void(0)" class="tablinks " onclick="openDataView(event, 'ScheduledJobsHistory')"> Scheduled Jobs History</a></li>

            </ul>
        </div>
        <div id="scheduledSynchronizationJobs" class="tabcontent">
            <display:table name="actionBean.scheduledTaskViews" class="datatable results" id="scheduledTask"
                            requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#scheduledSynchronizationJobs">
                <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No scheduled Jobs found.</p>" />
                <display:setProperty name="paging.banner.item_name" value="scheduledTask" />
                <display:setProperty name="paging.banner.items_name" value="scheduledTasks" />
                <display:column title="Task Id"  escapeXml="false" style="width: 15%">
                   <stripes:link  href="${pageContext.request.contextPath}/vocabulary/viewScheduledTaskDetails?scheduledTaskId=${scheduledTask.details.taskId}">${scheduledTask.details.taskId}</stripes:link>
                </display:column>
                <display:column title="Task Type" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${scheduledTask.type}"/>
                </display:column>
                <display:column title="Additional Task Details" escapeXml="false" style="width: 15%">
                    <c:out value="${scheduledTask.additionalDetails}" />
                </display:column>
                <display:column title="Execution Status" escapeXml="false"  style="width: 15%">
                    <dd:attributeValue  attrValue="${scheduledTask.details.executionStatus}" />
                </display:column>
                <display:column title="Execution Start Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${scheduledTask.details.startDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column> 
                <display:column title="Execution End Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${scheduledTask.details.endDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>  
                <display:column title="Next Scheduled Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${scheduledTask.details.scheduledDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>
            </display:table>
        </div>
        <div id="ScheduledJobsHistory" class="tabcontent">
            <display:table name="actionBean.scheduledTaskHistoryViews" class="datatable results" id="pastScheduledTask"
                           style="width:100% !important" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#ScheduledJobsHistory">
                <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No scheduled Jobs found.</p>" />
                <display:setProperty name="paging.banner.item_name" value="pastScheduledTask" />
                <display:setProperty name="paging.banner.items_name" value="pastScheduledTasks" />
                <display:column title="Task Id"  href="${pageContext.request.contextPath}/vocabulary/viewScheduledTaskHistoryDetails?scheduledTaskHistoryId=${pastScheduledTask.asyncTaskExecutionEntryHistoryId}" escapeXml="false" style="width: 15%">
                    <stripes:link  href="${pageContext.request.contextPath}/vocabulary/viewScheduledTaskHistoryDetails?scheduledTaskHistoryId=${pastScheduledTask.asyncTaskExecutionEntryHistoryId}">${pastScheduledTask.details.taskId}</stripes:link>
                </display:column>
                <display:column title="Task Type" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${pastScheduledTask.type}"/>
                </display:column>
                <display:column title="Additional Task Details" escapeXml="false" style="width: 15% !important">
                    <c:out value="${pastScheduledTask.additionalDetails}" />
                </display:column>
                <display:column title="Execution Status" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${pastScheduledTask.details.executionStatus}"/>
                </display:column>
                <display:column title="Execution Start Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${pastScheduledTask.details.startDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column> 
                <display:column title="Execution End Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${pastScheduledTask.details.endDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>  
                <display:column title="Next Scheduled Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${pastScheduledTask.details.scheduledDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>
            </display:table>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
