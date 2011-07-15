package eionet.meta;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDRuntimeException extends RuntimeException{

    /**
     *
     */
    public DDRuntimeException(){
        super();
    }
    
    /**
     * 
     * @param message
     */
    public DDRuntimeException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message
     * @param cause
     */
    public DDRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 
     * @param cause
     */
    public DDRuntimeException(Throwable cause) {
        super(cause);
    }
}
