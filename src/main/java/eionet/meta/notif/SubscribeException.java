package eionet.meta.notif;

import eionet.meta.DDException;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class SubscribeException extends DDException {

    /**
     *
     */
    private static final long serialVersionUID = -7452665576448394852L;

    /**
     *
     */
    public SubscribeException() {
        super();
    }

    /**
     * @param msg
     *            the detail message.
     */
    public SubscribeException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public SubscribeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     *
     * @param cause
     */
    public SubscribeException(Throwable cause) {
        super(cause);
    }
}
