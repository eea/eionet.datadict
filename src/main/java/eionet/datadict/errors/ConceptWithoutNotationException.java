package eionet.datadict.errors;

public class ConceptWithoutNotationException extends  Exception{


    public ConceptWithoutNotationException() {
    }

    public ConceptWithoutNotationException(String message) {
        super(message);
    }

    public ConceptWithoutNotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConceptWithoutNotationException(Throwable cause) {
        super(cause);
    }

}
