package eionet.meta.imp;

import eionet.meta.dao.domain.VocabularyType;
import java.security.MessageDigest;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * 
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class CreateVocabularyRDFImportHandler implements RDFHandler {

    private boolean NotationsEqualIdentifier;

    private boolean numericConceptIdentifier;

    private String folderContextRoot = null;
    private VocabularyType type;

    public static final String NOTATIONS_EQUAL_IDENTIFIER = "notationsEqualIdentifier";

    public static final String NUMERIC_CONCEPT_IDENTIFIER = "numericConceptIdentifier";

    public static final String TYPE = "type";

    protected List<String> logMessages = null;

    /**
     * Message Digest instance used for triple hashing.
     */
    private MessageDigest messageDigestInstance = null;

    @Override
    public void startRDF() throws RDFHandlerException {

        System.out.println("startRDF");

    }

    @Override
    public void endRDF() throws RDFHandlerException {

        System.out.println("endRDF");
    }

    @Override
    public void handleNamespace(String string, String string1) throws RDFHandlerException {

    }

    @Override
    public void handleStatement(Statement stmnt) throws RDFHandlerException {

        Resource subject = stmnt.getSubject();
        URI predicate = stmnt.getPredicate();
        String namespace = predicate.getNamespace();
        String clearedPredicate = (predicate.stringValue().replace(namespace, "")).replace("#", "");

        Value object = stmnt.getObject();
        // THe predicate contains the namespace too, so cut down the namespace to get only the predicate

        if (StringUtils.equals(NUMERIC_CONCEPT_IDENTIFIER, clearedPredicate)) {

            this.setNumericConceptIdentifier(Boolean.parseBoolean(object.stringValue()));

        }
        if (StringUtils.equals(NOTATIONS_EQUAL_IDENTIFIER, clearedPredicate)) {
            this.setNotationsEqualIdentifier(Boolean.parseBoolean(object.stringValue()));
        }
        if (StringUtils.equals(TYPE, clearedPredicate)) {
            this.setType(object.stringValue());
        }

        System.out.println("Inside CreateVocabularyRDFImportHandler handleStatement() \n");
        System.out.println(subject.toString() + "   " + predicate.toString() + "   " + object.toString());
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public CreateVocabularyRDFImportHandler(boolean NotationsEqualIdentifier, boolean numericConceptIdentifier) {
        this.NotationsEqualIdentifier = NotationsEqualIdentifier;
        this.numericConceptIdentifier = numericConceptIdentifier;
    }

    public CreateVocabularyRDFImportHandler() {
    }

    public String getFolderContextRoot() {
        return folderContextRoot;
    }

    public void setFolderContextRoot(String folderContextRoot) {
        this.folderContextRoot = folderContextRoot;
    }

    public boolean isNotationsEqualIdentifier() {
        return NotationsEqualIdentifier;
    }

    public void setNotationsEqualIdentifier(boolean NotationsEqualIdentifier) {
        this.NotationsEqualIdentifier = NotationsEqualIdentifier;
    }

    public boolean isNumericConceptIdentifier() {
        return numericConceptIdentifier;
    }

    public void setNumericConceptIdentifier(boolean numericConceptIdentifier) {
        this.numericConceptIdentifier = numericConceptIdentifier;
    }

    public VocabularyType getType() {
        return type;
    }

    public void setType(String type) {

        if (StringUtils.equals(VocabularyType.COMMON.toString(), type.toUpperCase())) {
            this.type = VocabularyType.COMMON;
        }
        if (StringUtils.equals(VocabularyType.SITE_CODE.toString(), type.toUpperCase())) {
            this.type = VocabularyType.SITE_CODE;
        }

    }

    public List<String> getLogMessages() {
        return logMessages;
    }

    public void setLogMessages(List<String> logMessages) {
        this.logMessages = logMessages;
    }

    public MessageDigest getMessageDigestInstance() {
        return messageDigestInstance;
    }

    public void setMessageDigestInstance(MessageDigest messageDigestInstance) {
        this.messageDigestInstance = messageDigestInstance;
    }

}
