package eionet.meta;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDException extends Exception{

    /**
     *
     */
    public DDException(){
        super();
    }

    /**
     * @param msg the detail message.
     */
    public DDException(String msg) {
        super(msg);
    }
    
    /**
     * 
     * @param message
     * @param cause
     */
    public DDException(String msg, Throwable cause){
        super(msg, cause);
    }

    /**
     * 
     * @param cause
     */
    public DDException(Throwable cause){
        super(cause);
    }
}
