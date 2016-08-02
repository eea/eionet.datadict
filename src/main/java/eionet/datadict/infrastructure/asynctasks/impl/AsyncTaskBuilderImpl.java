package eionet.datadict.infrastructure.asynctasks.impl;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskBuilder;
import eionet.meta.spring.SpringApplicationContext;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskBuilderImpl implements AsyncTaskBuilder {

    @Override
    public AsyncTask create(Class<? extends AsyncTask> taskType, Map<String, Object> parameters) {
        AsyncTask task = SpringApplicationContext.getBean(taskType);
        task.setUp(parameters);
        
        return task;
    }
    
}
