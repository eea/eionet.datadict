package eionet.datadict.web.asynctasks;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.VocabularyOutputHelper;
import eionet.meta.service.ICSVVocabularyImportService;
import eionet.meta.service.IVocabularyService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class VocabularyCsvImportTask implements AsyncTask {
    
    public static final String PARAM_VOCABULARY_SET_IDENTIFIER = "vocabularySetIdentifier";
    public static final String PARAM_VOCABULARY_IDENTIFIER = "vocabularyIdentifier";
    public static final String PARAM_WORKING_COPY = "workingCopy";
    public static final String PARAM_CSV_FILE_NAME = "csvFileName";
    public static final String PARAM_CSV_PURGE_DATA = "purgeData";
    public static final String PARAM_CSV_PURGE_BOUND_ELEMENTS = "purgeBoundElements";
    
    public static Map<String, Object> createParamsBundle(String vocabularySetIdentifier, String vocabularyIdentifier, 
            boolean workingCopy, String csvFileName, boolean purgeData, boolean purgeBoundElements) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(PARAM_VOCABULARY_SET_IDENTIFIER, vocabularySetIdentifier);
        parameters.put(PARAM_VOCABULARY_IDENTIFIER, vocabularyIdentifier);
        parameters.put(PARAM_WORKING_COPY, workingCopy);
        parameters.put(PARAM_CSV_FILE_NAME, csvFileName);
        parameters.put(PARAM_CSV_PURGE_DATA, purgeData);
        parameters.put(PARAM_CSV_PURGE_BOUND_ELEMENTS, purgeBoundElements);
        
        return parameters;
    }
    
    private static final Logger LOGGER = Logger.getLogger(VocabularyCsvImportTask.class);
    
    private final IVocabularyService vocabularyService;
    private final ICSVVocabularyImportService vocabularyCsvImportService;
    
    private Map<String, Object> parameters;
    
    @Autowired
    public VocabularyCsvImportTask(IVocabularyService vocabularyService, ICSVVocabularyImportService vocabularyCsvImportService) {
        this.vocabularyService = vocabularyService;
        this.vocabularyCsvImportService = vocabularyCsvImportService;
    }
    
    @Override
    public String getDisplayName() {
        return String.format("Import CSV input into vocabulary %s/%s", 
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
        LOGGER.debug("Starting CSV import operation");
        
        try {
            this.importCsv();
        }
        finally {
            if (!this.deleteCsvFile()) {
                LOGGER.info("Failed to delete temporary CSV file: " + this.getCsvFileName());
            }
        }
        
        LOGGER.debug("CSV import completed");
        
        return null;
    }
    
    protected void importCsv() throws Exception {
        VocabularyFolder vocabulary = vocabularyService.getVocabularyFolder(this.getVocabularySetIdentifier(), 
                this.getVocabularyIdentifier(), this.isWorkingCopy());
        Reader csvFileReader = null;
        
        try {
            csvFileReader = new InputStreamReader(this.getInputStream(), CharEncoding.UTF_8);
            List<String> systemMessages = this.vocabularyCsvImportService.importCsvIntoVocabulary(
                    csvFileReader, vocabulary, this.isPurgeData(), this.isPurgeBoundElements());

            for (String systemMessage : systemMessages) {
                LOGGER.info(systemMessage);
            }
        }
        finally {
            if (csvFileReader != null) {
                csvFileReader.close();
            }
        }
    } 
    
    protected boolean deleteCsvFile() {
        return FileUtils.deleteQuietly(new File(this.getCsvFileName()));
    }
    
    protected InputStream getInputStream() throws IOException, BadRequestException {
        // consume stupid bom first!! if it exists!
        InputStream is = new FileInputStream(this.getCsvFileName());
        byte[] firstBomBytes = new byte[VocabularyOutputHelper.BOM_BYTE_ARRAY_LENGTH];
        int readBytes;
        
        try {
            readBytes = is.read(firstBomBytes);
        }
        catch (IOException ex) {
            is.close();
            throw ex;
        }
        
        if (readBytes != VocabularyOutputHelper.BOM_BYTE_ARRAY_LENGTH) {
            is.close();
            throw new BadRequestException("Input stream cannot be read");
        }

        if (!Arrays.equals(firstBomBytes, VocabularyOutputHelper.getBomByteArray())) {
            is.close();
            is = new FileInputStream(this.getCsvFileName());
        }
        
        return is;
    }
    
    protected String getCsvFileName() {
        return (String) this.parameters.get(PARAM_CSV_FILE_NAME);
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
    
    protected boolean isPurgeData() {
        return (Boolean) this.parameters.get(PARAM_CSV_PURGE_DATA);
    }
    
    protected boolean isPurgeBoundElements() {
        return (Boolean) this.parameters.get(PARAM_CSV_PURGE_BOUND_ELEMENTS);
    }
    
}
