package eionet.datadict.infrastructure.asynctasks;

import java.util.Map;

public interface AsyncTaskBuilder {

    AsyncTask create(Class<? extends AsyncTask> taskType, Map<String, Object> parameters);
    
}
