package eionet.meta;

import java.util.HashMap;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDException extends Exception {
    /** param namd for element ids. */
    public static final String ERR_ELEMS_KEY = "elementIds";
    /**
     * concainer for additional error info.
     * can be used in response
     */
    private HashMap<String, Object> errorParameters;
    /**
     *
     */
    public DDException() {
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
    public DDException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     *
     * @param cause
     */
    public DDException(Throwable cause) {
        super(cause);
    }

    public HashMap<String, Object> getErrorParameters() {
        return errorParameters;
    }

    public void setErrorParameters(HashMap<String, Object> errorParameters) {
        this.errorParameters = errorParameters;
    }
    
    public void setErrorParameter(String key, Object errorParameter) {
        if (errorParameters == null) {
            errorParameters = new HashMap<String, Object>();
        }
        errorParameters.put(key, errorParameter);
    }


}
