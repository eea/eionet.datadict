/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.web.asynctasks;

import eionet.datadict.errors.FetchVocabularyRDFfromUrlException;
import eionet.datadict.infrastructure.asynctasks.AsyncTask;
import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.model.enums.Enumerations.SchedulingIntervalUnit;
import eionet.datadict.web.ViewUtils;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IRDFVocabularyImportService;
import eionet.meta.service.IVocabularyImportService;
import eionet.meta.service.IVocabularyService;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Component
@Scope("prototype")
public class VocabularyRdfImportFromUrlTask implements AsyncTask {

    @Autowired
    private JavaMailSender mailSender;

    public static final String PARAM_VOCABULARY_SET_IDENTIFIER = "vocabularySetIdentifier";
    public static final String PARAM_VOCABULARY_IDENTIFIER = "vocabularyIdentifier";
    public static final String PARAM_RDF_FILE_URL = "rdfFileURL";
    public static final String PARAM_RDF_PURGE_OPTION = "rdfPurgeOption";
    public static final String PARAM_MISSING_CONCEPTS_ACTION = "missingConceptsAction";
    public static final String PARAM_NOTIFIERS_EMAILS = "emails";
    public static final String PARAM_SCHEDULE_INTERVAL = "scheduleInterval";
    public static final String PARAM_SCHEDULE_INTERVAL_UNIT = "scheduleIntervalUnit";

    public static Map<String, Object> createParamsBundle(String vocabularySetIdentifier, String vocabularyIdentifier, Integer scheduleInterval,
            SchedulingIntervalUnit schedulingIntervalUnit, String rdfFileURL, String emails, String rdfPurgeOption, IVocabularyImportService.MissingConceptsAction missingConceptsAction) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(PARAM_VOCABULARY_SET_IDENTIFIER, vocabularySetIdentifier);
        parameters.put(PARAM_VOCABULARY_IDENTIFIER, vocabularyIdentifier);
        parameters.put(PARAM_RDF_FILE_URL, rdfFileURL);
        parameters.put(PARAM_RDF_PURGE_OPTION, rdfPurgeOption);
        parameters.put(PARAM_NOTIFIERS_EMAILS, emails);
        parameters.put(PARAM_MISSING_CONCEPTS_ACTION, missingConceptsAction);
        parameters.put(PARAM_SCHEDULE_INTERVAL_UNIT, schedulingIntervalUnit);
        parameters.put(PARAM_SCHEDULE_INTERVAL, scheduleInterval);
        return parameters;
    }

    private static final Logger LOGGER = Logger.getLogger(VocabularyRdfImportFromUrlTask.class);

    private final IVocabularyService vocabularyService;
    private final IRDFVocabularyImportService vocabularyRdfImportService;

    private Map<String, Object> parameters;

    @Autowired
    public VocabularyRdfImportFromUrlTask(IVocabularyService vocabularyService, IRDFVocabularyImportService vocabularyRdfImportService) {
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
        List<String> systemMessages = new ArrayList<String>();
        try {
            systemMessages = this.importRdf();
            LOGGER.debug("RDF import completed");
            LOGGER.info("Email Sending Mechanism invocation");

        } catch (Exception e) {
            this.notifyEmailusers(this.getNotifiersEmails(), Arrays.asList(e.getMessage()));
            throw new Exception("Error Importing RDF into Vocabulary:" + e.getMessage());
        }
        return systemMessages;
    }

    protected List<String> importRdf() throws Exception {
        if (vocabularyService.hasVocabularyWorkingCopy(this.getVocabularySetIdentifier(), this.getVocabularyIdentifier())) {
            throw new Exception("Vocabulary With Folder Name: " + this.getVocabularySetIdentifier() + " and Identifier: " + this.getVocabularyIdentifier() + " is in working Copy Status");
        }
        VocabularyFolder vocabulary = vocabularyService.getVocabularyFolder(this.getVocabularySetIdentifier(), this.getVocabularyIdentifier(), false);
        Reader rdfFileReader = null;

        try {
            rdfFileReader = new InputStreamReader(new ByteArrayInputStream(this.downloadVocabularyRdf(this.getRdfFileURL())));
            int rdfPurgeOption = this.getRdfPurgeOption();
            List<String> systemMessages = this.vocabularyRdfImportService.importRdfIntoVocabulary(
                    rdfFileReader, vocabulary, rdfPurgeOption == 4, rdfPurgeOption == 3, rdfPurgeOption == 2, this.getMissingConceptsAction());
            return systemMessages;
        } finally {
            if (rdfFileReader != null) {
                rdfFileReader.close();
            }
        }
    }

    protected byte[] downloadVocabularyRdf(String url) throws FetchVocabularyRDFfromUrlException {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(
                new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET, entity, byte[].class, "1");
            return response.getBody();
        } catch (Exception e) {
            throw new FetchVocabularyRDFfromUrlException("Error fetching vocabulary RDF from URL:" + url + ViewUtils.HTML_NEW_LINE + ViewUtils.SYSTEM_EXCEPTION_MESSAGE
                    + e.getMessage(), e.getCause());
        }
    }

    protected void notifyEmailusers(String emails, final List<String> messages) {
        if (emails == null) {
            throw new MailSendException("No emails to send to");
        }
        if (messages == null) {
            throw new MailPreparationException("No messages to send");
        }
        final StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message);
            sb.append("\t");
        }
        String[] emailsList = emails.split(",");
        for (final String email : emailsList) {
            MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {
                @Override
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false);
                    message.setText(sb.toString(), false);
                    message.setFrom(new InternetAddress("no-reply@eea.europa.eu"));
                    message.setSubject("Scheduled RDF Import into Vocabulary Completed");
                    message.setTo(email);
                }
            };
            try {
                mailSender.send(mimeMessagePreparator);
            } catch (Exception e) {
                // We silence this exception and only log it, in order to prevent escalating the Exception to higher levels, which will cause the Scheduler to catch it and mark the Task
                // as failed.
                LOGGER.error("Error sending email to users: " + e.getMessage(), e.getCause());
            }
        }
    }

    protected String getRdfFileURL() {
        return (String) this.parameters.get(PARAM_RDF_FILE_URL);
    }

    protected String getNotifiersEmails() {
        return (String) this.parameters.get(PARAM_NOTIFIERS_EMAILS);
    }

    protected String getVocabularySetIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_SET_IDENTIFIER);
    }

    protected String getVocabularyIdentifier() {
        return (String) this.parameters.get(PARAM_VOCABULARY_IDENTIFIER);
    }

    protected int getRdfPurgeOption() {
        return Enumerations.VocabularyRdfPurgeOption.valueOf((String) this.parameters.get(PARAM_RDF_PURGE_OPTION)).getRdfPurgeOption();
    }

    protected IVocabularyImportService.MissingConceptsAction getMissingConceptsAction() {
        return (IVocabularyImportService.MissingConceptsAction) this.parameters.get(PARAM_MISSING_CONCEPTS_ACTION);
    }

}
