package eionet.datadict.web.asynctasks;

import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.model.enums.Enumerations;
import static eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask.PARAM_RDF_PURGE_OPTION;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IRDFVocabularyImportService;
import eionet.meta.service.IVocabularyImportService;
import eionet.meta.service.IVocabularyImportService.MissingConceptsAction;
import eionet.meta.service.IVocabularyImportService.UploadAction;
import eionet.meta.service.IVocabularyImportService.UploadActionBefore;
import eionet.meta.service.IVocabularyService;
import eionet.util.Props;
import eionet.util.PropsIF;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class VocabularyRdfImportFromApiTask implements AsyncTask {

    @Autowired
    private JavaMailSender mailSender;
    
    public static final String PARAM_RDF_CONTENT = "rdfContent";
    public static final String PARAM_VOCABULARY_SET_IDENTIFIER = "vocabularySetIdentifier";
    public static final String PARAM_VOCABULARY_IDENTIFIER = "vocabularyIdentifier";
    public static final String PARAM_UPLOAD_ACTION_BEFORE = "uploadActionBefore";
    public static final String PARAM_UPLOAD_ACTION = "uploadAction";
    public static final String PARAM_MISSING_CONCEPTS_ACTION = "missingConceptsAction";
    public static final String PARAM_NOTIFIERS_EMAILS = "emails";

    public static Map<String, Object> createParamsBundle(String base64EncodedCompressedRdf,
            String vocabularySetIdentifier, String vocabularyIdentifier,
            IVocabularyImportService.UploadActionBefore uploadActionBefore,
            IVocabularyImportService.UploadAction uploadAction,
            IVocabularyImportService.MissingConceptsAction missingConceptsAction,
            String emails) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PARAM_RDF_CONTENT, base64EncodedCompressedRdf);
        parameters.put(PARAM_VOCABULARY_SET_IDENTIFIER, vocabularySetIdentifier);
        parameters.put(PARAM_VOCABULARY_IDENTIFIER, vocabularyIdentifier);
        parameters.put(PARAM_UPLOAD_ACTION_BEFORE, uploadActionBefore);
        parameters.put(PARAM_UPLOAD_ACTION, uploadAction);
        parameters.put(PARAM_MISSING_CONCEPTS_ACTION, missingConceptsAction);
        parameters.put(PARAM_NOTIFIERS_EMAILS, emails);
        return parameters;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyRdfImportFromApiTask.class);

    private final IVocabularyService vocabularyService;
    private final IRDFVocabularyImportService vocabularyRdfImportService;
    private final IVocabularyFolderDAO vocabularyFolderDAO;

    private Map<String, Object> parameters;

    @Autowired
    public VocabularyRdfImportFromApiTask(IVocabularyService vocabularyService, IRDFVocabularyImportService vocabularyRdfImportService, IVocabularyFolderDAO vocabularyFolderDAO) {
        this.vocabularyService = vocabularyService;
        this.vocabularyRdfImportService = vocabularyRdfImportService;
        this.vocabularyFolderDAO = vocabularyFolderDAO;
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
        return String.format("/vocabulary/%s/%s", this.getVocabularySetIdentifier(), this.getVocabularyIdentifier());
    }

    @Override
    public Object call() throws Exception {
        Thread.currentThread().setName("RDF-IMPORT");
        LOGGER.info("uploadRdf API - Starting RDF import operation");

        List<String> systemMessages = new ArrayList<>();
        if (vocabularyService.hasVocabularyWorkingCopy(this.getVocabularySetIdentifier(), this.getVocabularyIdentifier())) {
            throw new Exception("Vocabulary With Folder Name: " + this.getVocabularySetIdentifier() + " and Identifier: " + this.getVocabularyIdentifier() + " is in working Copy Status");
        }

        VocabularyFolder vocabulary = vocabularyService.getVocabularyFolder(this.getVocabularySetIdentifier(), this.getVocabularyIdentifier(), false);
        
        // decode and decompress rdf content
        byte[] decodedRdf = Base64.getDecoder().decode(this.getBase64EncodedCompressedRdf());
        ByteArrayInputStream bais = new ByteArrayInputStream(decodedRdf);
        GZIPInputStream decompressedRdf = new GZIPInputStream(bais);

        BOMInputStream bomIn = new BOMInputStream(decompressedRdf, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
                ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE
        );

        if (bomIn.hasBOM()) {
            LOGGER.info("uploadRdf API - Detected BOM");
        }

        try (Reader rdfFileReader = new InputStreamReader(bomIn, StandardCharsets.UTF_8)) {
            systemMessages =  this.vocabularyRdfImportService.importRdfIntoVocabulary(rdfFileReader,
                    vocabulary, getUploadActionBefore(), getUploadAction(), getMissingConceptsAction());
            LOGGER.info("uploadRdf API - Vocabulary RDF import completed");
            
            Date dateModified = new Date();
            String userModified = PropsIF.API_USER_MODIFIED_IDENTIFIER;
            vocabularyFolderDAO.updateDateAndUserModified(dateModified, userModified, vocabulary.getId());
            LOGGER.info("uploadRdf API - DATE_MODIFIED was updated");

            LOGGER.info("Email Sending Mechanism invocation");
            systemMessages.add(0, "Vocabulary API RDF import completed: " + Props.getRequiredProperty(PropsIF.DD_URL) + 
                    String.format("/vocabulary/%s/%s", this.getVocabularySetIdentifier(), this.getVocabularyIdentifier()));
            this.notifyEmailusers(this.getNotifiersEmails(), systemMessages);
        } catch (Exception e) {
            this.notifyEmailusers(this.getNotifiersEmails(), Arrays.asList("Vocabulary API RDF import error", e.getMessage()));
            throw new Exception("Vocabulary API RDF import error:" + e.getMessage());
        }
        return systemMessages;
    }
    
    protected void notifyEmailusers(String emails, final List<String> messages) {
        if (emails == null || messages == null) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message);
            sb.append("\t");
        }

        String[] emailsList = emails.split(",");
        for (final String email : emailsList) {
            MimeMessagePreparator mimeMessagePreparator = (MimeMessage mimeMessage) -> {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false);
                message.setText(sb.toString(), false);
                message.setFrom(new InternetAddress(Props.getRequiredProperty(PropsIF.NOTIFICATION_EMAIL_FROM)));
                message.setSubject("Vocabulary API RDF Import");
                message.setTo(email);
            };
            try {
                mailSender.send(mimeMessagePreparator);
            } catch (MailException e) {
                LOGGER.error("Error sending email to users: " + e.getMessage(), e.getCause());
            }
        }
    }

    protected String getBase64EncodedCompressedRdf() {
        return (String) this.parameters.get(PARAM_RDF_CONTENT);
    }

    protected String getVocabularySetIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_SET_IDENTIFIER);
    }
    
    protected String getVocabularyIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_IDENTIFIER);
    }

    protected UploadActionBefore getUploadActionBefore() {
        return (UploadActionBefore) this.parameters.get(PARAM_UPLOAD_ACTION_BEFORE);
    }

    protected UploadAction getUploadAction() {
        return (UploadAction) this.parameters.get(PARAM_UPLOAD_ACTION);
    }
    
    protected MissingConceptsAction getMissingConceptsAction() {
        return (MissingConceptsAction) this.parameters.get(PARAM_MISSING_CONCEPTS_ACTION);
    }

    protected int getRdfPurgeOption() {
        return Enumerations.VocabularyRdfPurgeOption.valueOf((String) this.parameters.get(PARAM_RDF_PURGE_OPTION)).getRdfPurgeOption();
    }
    
    protected String getNotifiersEmails() {
        return (String) this.parameters.get(PARAM_NOTIFIERS_EMAILS);
    }

}
