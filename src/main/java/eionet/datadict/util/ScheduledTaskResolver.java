package eionet.datadict.util;

import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class ScheduledTaskResolver {
    
    private static final String VOCABULARY_CHECK_IN_TASK = "Vocabulary Check In Task";  
    private static final String VOCABULARY_CHECK_OUT_TASK = "Vocabulary Check Out Task"; 
    private static final String VOCABULARY_CREATE_COPY_TASK = "Vocabulary Create Copy Task"; 
    private static final String VOCABULARY_CSV_IMPORT_TASK = "Vocabulary CSV Import Task";   
    private static final String VOCABULARY_RDF_IMPORT_TASK = "Vocabulary RDF Import Task";
    private static final String VOCABULARY_RDF_IMPORT_FROM_URL_TASK = "Import Vocabulary RDF from Url Task";
    private static final String VOCABULARY_RDF_IMPORT_FROM_API_TASK = "Import Vocabulary RDF from API Task";
    private static final String VOCABULARY_UNDO_CHECKOUT_TASK = "Vocabulary undo Checkout Task";
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;

    @Autowired
    public ScheduledTaskResolver(AsyncTaskDataSerializer asyncTaskDataSerializer) {
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
    }

    /**
     *@param The Task Class Name
     *@return the Scheduled Task Name in a more readable format, or null if the Task Class Name is not found
     **/
    public String resolveTaskTypeFromTaskClassName(String className) {
        if(className.contains("VocabularyCheckInTask")) {
            return VOCABULARY_CHECK_IN_TASK;
        }
        if(className.contains("VocabularyCheckOutTask")) {
            return VOCABULARY_CHECK_OUT_TASK;
        }
        if(className.contains("VocabularyCreateCopyTask")) {
            return VOCABULARY_CREATE_COPY_TASK;
        }
        if(className.contains("VocabularyCsvImportTask")) {
            return VOCABULARY_CSV_IMPORT_TASK;
        }
        if(className.contains("VocabularyRdfImportFromUrlTask")) {
            return VOCABULARY_RDF_IMPORT_FROM_URL_TASK;
        }
        if(className.contains("VocabularyRdfImportTask")) {
            return VOCABULARY_RDF_IMPORT_TASK;
        }
        if(className.contains("VocabularyRdfImportFromApiTask")) {
            return VOCABULARY_RDF_IMPORT_FROM_API_TASK;
        }
        if (className.contains("VocabularyUndoCheckOutTask")) {
            return VOCABULARY_UNDO_CHECKOUT_TASK;
        }
        return null;
    }
    
    /**
     *@param the Async Task Execution Entry object
     *@return the URL to import the Vocabulary RDF from, or null if no Import URL is found
     **/
    public String extractImportUrlFromVocabularyImportTask(AsyncTaskExecutionEntry entry) {
        if (entry.getTaskClassName().contains("VocabularyRdfImportFromUrlTask")) {
            String rdfFileUrl = (asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters())).get("rdfFileURL").toString();
            return "ImportURL: "+rdfFileUrl;
        }
        return null;
    }
}
