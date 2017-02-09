package eionet.datadict.util;

import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class ScheduledTaskResolver {
    
    private static final String VOCABULARY_CHECK_IN_TASK="Vocabulary Check In Task";  
    private static final String VOCABULARY_CHECK_OUT_TASK="Vocabulary Check Out Task"; 
    private static final String VOCABULARY_CSV_IMPORT_TASK="Vocabulary CSV Import Task";   
    private static final String VOCABULARY_RDF_IMPORT_TASK=" Vocabulary RDF Import Task";
    private static final String VOCABULARY_RDF_IMPORT_FROM_URL_TASK="Import Vocabulary RDF from Url Task";
    private static final String VOCABULARY_UNDO_CHECKOUT_TASK="Vocabulary undo Checkout Task";
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;

    @Autowired
    public ScheduledTaskResolver(AsyncTaskDataSerializer asyncTaskDataSerializer) {
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
    }
   
    public String resolveTaskTypeFromTaskClassName(String className){
    
        if(className.contains("VocabularyCheckInTask")){
            return VOCABULARY_CHECK_IN_TASK;
        }
        if(className.contains("VocabularyCheckOutTask")){
            return VOCABULARY_CHECK_OUT_TASK;
        }
        if(className.contains("VocabularyCsvImportTask")){
            return VOCABULARY_CSV_IMPORT_TASK;
        }
        if(className.contains("VocabularyRdfImportFromUrlTask")){
            return VOCABULARY_RDF_IMPORT_FROM_URL_TASK;
        }
        if(className.contains("VocabularyRdfImportTask")){
            return VOCABULARY_RDF_IMPORT_TASK;
        }
        if (className.contains("VocabularyUndoCheckOutTask")) {
            return VOCABULARY_UNDO_CHECKOUT_TASK;
        }
    return null;
    }
    
    public String extractImportUrlFromVocabularyImportTask(AsyncTaskExecutionEntry entry){
    
        if (entry.getTaskClassName().contains("VocabularyRdfImportFromUrlTask")) {
            String rdfFileUrl = (asyncTaskDataSerializer.deserializeParameters(entry.getSerializedParameters())).get("rdfFileURL").toString();
            System.out.println("RDF FILE URL IS"+rdfFileUrl);
            return "ImportURL: "+rdfFileUrl;
        }
    return null;
    }
}
