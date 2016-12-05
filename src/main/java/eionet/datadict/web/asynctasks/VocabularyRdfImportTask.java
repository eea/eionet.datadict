package eionet.datadict.web.asynctasks;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IRDFVocabularyImportService;
import eionet.meta.service.IVocabularyImportService;
import eionet.meta.service.IVocabularyService;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class VocabularyRdfImportTask implements AsyncTask {
    
    public static final String PARAM_VOCABULARY_SET_IDENTIFIER = "vocabularySetIdentifier";
    public static final String PARAM_VOCABULARY_IDENTIFIER = "vocabularyIdentifier";
    public static final String PARAM_WORKING_COPY = "workingCopy";
    public static final String PARAM_RDF_FILE_NAME = "rdfFileName";
    public static final String PARAM_RDF_PURGE_OPTION = "rdfPurgeOption";
    public static final String PARAM_MISSING_CONCEPTS_ACTION = "missingConceptsAction";
    
    public static Map<String, Object> createParamsBundle(String vocabularySetIdentifier, String vocabularyIdentifier, 
            boolean workingCopy, String rdfFileName, int rdfPurgeOption, IVocabularyImportService.MissingConceptsAction missingConceptsAction) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(PARAM_VOCABULARY_SET_IDENTIFIER, vocabularySetIdentifier);
        parameters.put(PARAM_VOCABULARY_IDENTIFIER, vocabularyIdentifier);
        parameters.put(PARAM_WORKING_COPY, workingCopy);
        parameters.put(PARAM_RDF_FILE_NAME, rdfFileName);
        parameters.put(PARAM_RDF_PURGE_OPTION, rdfPurgeOption);
        parameters.put(PARAM_MISSING_CONCEPTS_ACTION, missingConceptsAction);
        
        return parameters;
    }
    
    private static final Logger LOGGER = Logger.getLogger(VocabularyRdfImportTask.class);
    
    private final IVocabularyService vocabularyService;
    private final IRDFVocabularyImportService vocabularyRdfImportService;
    
    private Map<String, Object> parameters;
    
    @Autowired
    public VocabularyRdfImportTask(IVocabularyService vocabularyService, IRDFVocabularyImportService vocabularyRdfImportService) {
        this.vocabularyService = vocabularyService;
        this.vocabularyRdfImportService = vocabularyRdfImportService;
    }
    
    @Override
    public String getDisplayName() {
        return String.format("Importing RDF input into vocabulary %s/%s", 
                this.getVocabularySetIdentifier(), this.getVocabularyIdentifier());
    }

    @Override
    public Class getResultType() {
        return Void.TYPE;
    }

    @Override
    public void setUp(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String composeResultUrl(String taskId, Object result) {
        return String.format("/vocabulary/%s/%s/edit?vocabularyFolder.workingCopy=true", this.getVocabularySetIdentifier(), this.getVocabularyIdentifier());
    }

    @Override
    public Object call() throws Exception {
        LOGGER.debug("Starting RDF import operation");
        
        try {
            this.importRdf();
        }
        finally {
            if (!this.deleteRdfFile()) {
                LOGGER.info("Failed to delete temporary RDF file: " + this.getRdfFileName());
            }
        }
        
        LOGGER.debug("RDF import completed");
        
        return null;
    }
    
    protected void importRdf() throws Exception {
        VocabularyFolder vocabulary = vocabularyService.getVocabularyFolder(this.getVocabularySetIdentifier(), 
                this.getVocabularyIdentifier(), this.isWorkingCopy());
        Reader rdfFileReader = null;
        
        try {
            rdfFileReader = new FileReader(this.getRdfFileName());
            // TODO use enum instead for rdf purge option
            int rdfPurgeOption = this.getRdfPurgeOption();
            List<String> systemMessages = this.vocabularyRdfImportService.importRdfIntoVocabulary(
                    rdfFileReader, vocabulary, rdfPurgeOption == 4, rdfPurgeOption == 3, rdfPurgeOption == 2, this.getMissingConceptsAction());

            for (String systemMessage : systemMessages) {
                LOGGER.info(systemMessage);
            }
        }
        finally {
            if (rdfFileReader != null) {
                rdfFileReader.close();
            }
        }
    }
    
    protected boolean deleteRdfFile() {
        return FileUtils.deleteQuietly(new File(this.getRdfFileName()));
    }
    
    protected String getRdfFileName() {
        return (String) this.parameters.get(PARAM_RDF_FILE_NAME);
    }
    
    protected String getVocabularySetIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_SET_IDENTIFIER);
    }
    
    protected String getVocabularyIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_IDENTIFIER);
    }
    
    protected boolean isWorkingCopy() {
        return (Boolean) this.parameters.get(PARAM_WORKING_COPY);
    }
    
    protected int getRdfPurgeOption() {
        return (Integer) this.parameters.get(PARAM_RDF_PURGE_OPTION);
    }
    
    protected IVocabularyImportService.MissingConceptsAction getMissingConceptsAction() {
        return (IVocabularyImportService.MissingConceptsAction) this.parameters.get(PARAM_MISSING_CONCEPTS_ACTION);
    }
    
}
