package eionet.datadict.errors;

public class XmlMalformedException extends Exception {

    public XmlMalformedException() {
    }

    public XmlMalformedException(String message) {
        super(message);
    }


    public XmlMalformedException(String message, Throwable cause) {
        super(message, cause);
    }
}