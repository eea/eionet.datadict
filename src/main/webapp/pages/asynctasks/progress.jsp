<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Task in progress">
    <stripes:layout-component name="contents">
        <div class="asyncProgressContainer">
            Please wait...
        </div>
        <script type="text/javascript">
            
            (function(window, $) {
                
                var $container = $('.asyncProgressContainer');
                
                var pollUrl = '<stripes:url value="/asynctasks/${actionBean.taskId}/status" />';
                var intervalSeconds = 10;
                var intervalId = window.setInterval(poll, intervalSeconds * 1000);
                
                function poll() {
                    $.ajax(pollUrl).done(onPollResponse).fail(onPollFailure);
                }
                
                function onPollResponse(data) {
                    var status = data.status;
                    
                    if (status == 'SCHEDULED' || status == 'ONGOING') {
                        return;
                    }
                    
                    window.clearInterval(intervalId);
                    window.location.href = '<stripes:url value="/asynctasks/${actionBean.taskId}/result" />';
                }
                
                function onPollFailure(jqXHR, textStatus, errorThrown) {
                    $container.html('Poll failure: ' + errorThrown);
                }
                
            })(window, jQuery);
            
        </script>
    </stripes:layout-component>
</stripes:layout-render>