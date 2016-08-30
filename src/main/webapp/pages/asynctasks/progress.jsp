<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Task in progress">
    <stripes:layout-component name="head">
        <link type="text/css" media="all" href="<c:url value="/css/spinner.css"/>"  rel="stylesheet" />
        <style>
            
            .task.spinner-loader {
                position: absolute;
                top: 0px;
                right: 60px;
                bottom: 0px;
                margin: auto;
            }
            
        </style>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <div class="system-msg">
            <strong>${actionBean.taskDisplayName}</strong>
            <p><span class="asyncProgressContainer">Task in progress, please wait...</span></p>
            <div class="task spinner-loader"></div>
        </div>
        <script type="text/javascript">
            
            (function(window, $) {
                
                var $container = $('.asyncProgressContainer');
                
                var pollUrl = '<stripes:url value="/asynctasks/${actionBean.taskId}/status" />';
                var intervals = [3, 5, 10, 10, 30, 60];
                var currentInterval = 0;
                
                window.setTimeout(poll, getIntervalMillis());
                
                function poll() {
                    $.ajax(pollUrl).done(onPollResponse).fail(onPollFailure);
                }
                
                function onPollResponse(data) {
                    var status = data.status;
                    
                    if (status == 'SCHEDULED' || status == 'ONGOING') {
                        incrementInterval();
                        window.setTimeout(poll, getIntervalMillis());
                        return;
                    }
                    
                    window.location.href = '<stripes:url value="/asynctasks/${actionBean.taskId}/result" />';
                }
                
                function onPollFailure(jqXHR, textStatus, errorThrown) {
                    $container.html('Poll failure: ' + errorThrown);
                }
                
                function getIntervalMillis() {
                    return 1000 * intervals[currentInterval];
                }
                
                function incrementInterval() {
                    currentInterval = Math.min(currentInterval + 1, intervals.length - 1);
                }
                
            })(window, jQuery);
            
        </script>
    </stripes:layout-component>
</stripes:layout-render>