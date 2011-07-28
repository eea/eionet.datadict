package eionet.util;

import org.apache.log4j.Logger;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class IsJUnitRuntime {

    /** */
    private static Logger LOGGER = Logger.getLogger(IsJUnitRuntime.class);

    /** */
    public static final boolean VALUE = isJUnitRuntime();

    /**
     *
     * @return
     */
    private static boolean isJUnitRuntime(){

        String stackTrace = Util.getStack(new Throwable());
        boolean result = Boolean.valueOf(stackTrace.indexOf("at junit.framework.TestCase.run") > 0);
        if (result == true){
            LOGGER.info("Detected that the code is running in JUnit runtime");
        }
        return result;
    }
}
