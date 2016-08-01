package eionet.web.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManager;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import eionet.meta.spring.SpringApplicationContext;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

@UrlBinding("/asynctasks/{taskId}/{$event}")
public class AsyncTaskProgressActionBean extends AbstractActionBean {
    
    @SpringBean
    private AsyncTaskManager asyncTaskManager;
    
    @SpringBean
    private AsyncTaskDataSerializer asyncTaskDataSerializer;
    
    private String taskId;
    
    private boolean taskSuccess;
    
    private String feedbackText;
    private String feedbackUrl;
    
    @HandlesEvent("await")
    public Resolution await() throws JsonProcessingException {
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
    
    @HandlesEvent("result")
    public Resolution result() throws ReflectiveOperationException, ResourceNotFoundException {
        AsyncTaskExecutionEntry entry = this.asyncTaskManager.getTaskEntry(taskId);
        
        switch (entry.getExecutionStatus()) {
            case SCHEDULED:
            case ONGOING:
                return new RedirectResolution("/asynctasks/" + taskId);
            case ABORTED:
            case KILLED:
                this.onTaskAbortion(entry);
                break;
            case FAILED:
                this.onTaskFailure(entry);
                break;
            case COMPLETED:
                this.onTaskSuccess(entry);
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
        Exception error = (Exception) this.asyncTaskDataSerializer.deserializeResult(entry.getSerializedResult());
        this.feedbackText = "Task execution failed; " + error.getMessage();
    }
    
    protected void onTaskSuccess(AsyncTaskExecutionEntry entry)
            throws ReflectiveOperationException {
        this.taskSuccess = true;
        this.feedbackText = "Task was successfully completed.";
        AsyncTask task = this.instantiateTask(entry);
        Map<String, Object> parameters = this.asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters());
        Object result = this.asyncTaskDataSerializer.deserializeResult(entry.getSerializedResult());
        this.feedbackUrl = task.getResultUrl(taskId, parameters, result);
    }
    
    protected AsyncTask instantiateTask(AsyncTaskExecutionEntry entry) 
            throws ReflectiveOperationException {
        return (AsyncTask) SpringApplicationContext.getBean(Class.forName(entry.getTaskClassName()));
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
