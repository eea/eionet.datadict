package eionet.util.sql;

import eionet.meta.DDException;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDConnectionException extends DDException {

    /**
     *
     */
    public DDConnectionException() {
        super();
   }

    /**
     * @param msg the detail message.
     */
   public DDConnectionException(String msg) {
       super(msg);
   }

   /**
     *
     * @param message
     * @param cause
     */
    public DDConnectionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     *
     * @param cause
     */
    public DDConnectionException(Throwable cause) {
        super(cause);
    }
}
