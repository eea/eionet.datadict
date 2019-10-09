package eionet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class IsJUnitRuntime {

    /** */
    private static Logger LOGGER = LoggerFactory.getLogger(IsJUnitRuntime.class);

    /** */
    public static final boolean VALUE = isJUnitRuntime();

    /**
     *
     * @return
     */
    private static boolean isJUnitRuntime() {

        String stackTrace = Util.getStack(new Throwable());
        boolean result = Boolean.valueOf(stackTrace.indexOf("at junit.framework.TestCase.run") > 0);
        if (result == true) {
            LOGGER.info("Detected that the code is running in JUnit runtime");
        }
        return result;
    }
}
