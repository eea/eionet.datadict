package eionet.datadict.infrastructure.scheduling;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class ScheduleJobServiceException extends RuntimeException{

    public ScheduleJobServiceException() {
    }

    public ScheduleJobServiceException(String message) {
        super(message);
    }

    public ScheduleJobServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScheduleJobServiceException(Throwable cause) {
        super(cause);
    }
}
