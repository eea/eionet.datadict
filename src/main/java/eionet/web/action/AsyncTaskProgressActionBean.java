package eionet.web.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskBuilder;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskExecutionError;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

@UrlBinding("/asynctasks/{taskId}/{$event}")
public class AsyncTaskProgressActionBean extends AbstractActionBean {
    
    public static RedirectResolution createAwaitResolution(String taskId) {
        RedirectResolution resolution = new RedirectResolution(AsyncTaskProgressActionBean.class, "await");
        resolution.addParameter("taskId", taskId);
        
        return resolution;
    }
    
    @SpringBean
    private AsyncTaskBuilder asyncTaskBuilder;
    
    @SpringBean
    private AsyncTaskManager asyncTaskManager;
    
    @SpringBean
    private AsyncTaskDataSerializer asyncTaskDataSerializer;
    
    private String taskId;
    
    private String taskDisplayName;
    
    private boolean taskSuccess;
    
    private String feedbackText;
    private String feedbackUrl;
    
    @HandlesEvent("await")
    public Resolution await() throws JsonProcessingException, ResourceNotFoundException, ReflectiveOperationException {
        AsyncTaskExecutionEntry entry = this.asyncTaskManager.getTaskEntry(taskId);
        
        if (!AsyncTaskExecutionStatus.isPending(entry.getExecutionStatus())) {
            return new RedirectResolution("/asynctasks/" + taskId + "/result");
        }
        
        Map<String, Object> parameters = this.asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters());
        AsyncTask task = this.instantiateTask(entry.getTaskClassName(), parameters);
        this.taskDisplayName = task.getDisplayName();
        
        return new ForwardResolution("/pages/asynctasks/progress.jsp");
    }
    
    @HandlesEvent("status")
    public Resolution status() throws JsonProcessingException {
        Map<String, Object> result = new HashMap<String, Object>();
        final String contentType = "application/json";
        int httpStatus = HttpServletResponse.SC_OK;
        
        try {
            AsyncTaskExecutionStatus status = asyncTaskManager.getExecutionStatus(taskId);
            result.put("status", status.toString());
        }
        catch (ResourceNotFoundException ex) {
            LOGGER.info(ex.getMessage(), ex);
            httpStatus = HttpServletResponse.SC_NOT_FOUND;
            result.put("message", "The requested resource was not found.");
            result.put("detailMessage", ex.getMessage());
        }
        catch (Exception ex) {
            LOGGER.info(ex.getMessage(), ex);
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            result.put("message", "Unexpected server error.");
            result.put("detailMessage", ex.getMessage());
        }
        
        String jsonResult = new ObjectMapper().writeValueAsString(result);
        
        if (httpStatus == HttpServletResponse.SC_OK) {
            return new StreamingResolution(contentType, jsonResult);
        }
        
        final int calculatedHttpStatus = httpStatus;
        
        return new StreamingResolution(contentType, jsonResult) {

            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                response.setStatus(calculatedHttpStatus);
                super.stream(response);
            }

        };
    }
    
    @DefaultHandler
    @HandlesEvent("result")
    public Resolution result() throws ReflectiveOperationException, ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = this.asyncTaskManager.getTaskEntry(taskId);
        Map<String, Object> parameters = this.asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters());
        AsyncTask task = this.instantiateTask(entry.getTaskClassName(), parameters);
        this.taskDisplayName = task.getDisplayName();
        
        switch (entry.getExecutionStatus()) {
            case SCHEDULED:
            case ONGOING:
                return new RedirectResolution("/asynctasks/" + taskId + "/await");
            case ABORTED:
            case KILLED:
                this.onTaskAbortion(entry);
                break;
            case FAILED:
                this.onTaskFailure(entry);
                break;
            case COMPLETED:
                this.onTaskSuccess(entry, task);
                break;
            default:
                throw new IllegalStateException("Cannot handle task status: " + entry.getExecutionStatus());
        }
        
        return new ForwardResolution("/pages/asynctasks/results.jsp");
    }
    
    protected void onTaskAbortion(AsyncTaskExecutionEntry entry) {
        this.taskSuccess = false;
        this.feedbackText = "Task execution was aborted, please try again.";
    }
    
    protected void onTaskFailure(AsyncTaskExecutionEntry entry) throws ReflectiveOperationException {
        this.taskSuccess = false;
        AsyncTaskExecutionError error = (AsyncTaskExecutionError) this.asyncTaskDataSerializer.deserializeResult(entry.getSerializedResult());
        this.feedbackText = "Task execution failed; " + error.getMessage();
    }
    
    protected void onTaskSuccess(AsyncTaskExecutionEntry entry, AsyncTask task)
            throws ReflectiveOperationException {
        this.taskSuccess = true;
        this.feedbackText = "Task was successfully completed.";
        Object result = this.asyncTaskDataSerializer.deserializeResult(entry.getSerializedResult());
        this.feedbackUrl = task.composeResultUrl(entry.getTaskId(), result);
    }
    
    protected AsyncTask instantiateTask(String taskClassName, Map<String, Object> parameters) 
            throws ReflectiveOperationException {
        return this.asyncTaskBuilder.create((Class) Class.forName(taskClassName), parameters);
    }

    public AsyncTaskBuilder getAsyncTaskBuilder() {
        return asyncTaskBuilder;
    }

    public void setAsyncTaskBuilder(AsyncTaskBuilder asyncTaskBuilder) {
        this.asyncTaskBuilder = asyncTaskBuilder;
    }
    
    public AsyncTaskManager getAsyncTaskManager() {
        return asyncTaskManager;
    }

    public void setAsyncTaskManager(AsyncTaskManager asyncTaskManager) {
        this.asyncTaskManager = asyncTaskManager;
    }

    public AsyncTaskDataSerializer getAsyncTaskDataSerializer() {
        return asyncTaskDataSerializer;
    }

    public void setAsyncTaskDataSerializer(AsyncTaskDataSerializer asyncTaskDataSerializer) {
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
    }
    
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskDisplayName() {
        return taskDisplayName;
    }

    public void setTaskDisplayName(String taskDisplayName) {
        this.taskDisplayName = taskDisplayName;
    }

    public boolean isTaskSuccess() {
        return taskSuccess;
    }

    public void setTaskSuccess(boolean taskSuccess) {
        this.taskSuccess = taskSuccess;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public String getFeedbackUrl() {
        return feedbackUrl;
    }

    public void setFeedbackUrl(String feedbackUrl) {
        this.feedbackUrl = feedbackUrl;
    }
    
}
