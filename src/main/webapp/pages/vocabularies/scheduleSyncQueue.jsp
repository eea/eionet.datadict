<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schedule Vocabulary Synchronization" currentSection="vocabularies">

    <stripes:layout-component name="head">
        <script type="text/javascript">
            (function ($) {
             $(document).ready(function() {
    $('#scheduledTask').dataTable();
} );
            })(jQuery);
        </script>
        <script>jQuery.noConflict();</script>

    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Scheduled Synchronizations Queue</h1>
        <div id="scheduledSynchronizationJobs">
        <display:table name="actionBean.asyncTaskEntries" class="datatable results" id="scheduledTask"
            style="width:100%" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#scheduledSynchronizationJobs">
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No scheduled Jobs found.</p>" />
            <display:setProperty name="paging.banner.item_name" value="scheduledTask" />
            <display:setProperty name="paging.banner.items_name" value="scheduledTasks" />
            <display:column title="Task Id" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${scheduledTask.taskId}"/>
                </display:column>
                <display:column title="Execution Status" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${scheduledTask.executionStatus}"/>
                </display:column>
                <display:column title="Scheduled Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${scheduledTask.scheduledDate}" pattern="dd.MM.yyyy"/>
                </display:column>
        </display:table>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
