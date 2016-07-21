package eionet.datadict.errors;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class ClassPathLoadResourceException extends Exception {

    public ClassPathLoadResourceException() {
    }

    public ClassPathLoadResourceException(String message) {
        super(message);
    }

    public ClassPathLoadResourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
