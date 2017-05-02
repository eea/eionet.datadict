package eionet.datadict.errors;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class FetchVocabularyRDFfromUrlException extends Exception {

    public FetchVocabularyRDFfromUrlException() {
        this("Error Fetching the Vocabulary RDF from Specified URL");
    }

    public FetchVocabularyRDFfromUrlException(String string) {
        super(string);
    }

    public FetchVocabularyRDFfromUrlException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public FetchVocabularyRDFfromUrlException(Throwable thrwbl) {
        super(thrwbl);
    }
}
