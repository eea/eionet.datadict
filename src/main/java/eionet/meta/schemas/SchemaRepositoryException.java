package eionet.meta.schemas;

import java.io.IOException;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class SchemaRepositoryException extends IOException {

    /**
     *
     */
    public SchemaRepositoryException() {
        super();
    }

    /**
     *
     * @param message
     */
    public SchemaRepositoryException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public SchemaRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public SchemaRepositoryException(Throwable cause) {
        super(cause);
    }
}
