package eionet.datadict.dal.impl.converters;

import eionet.datadict.data.DataConverter;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import org.springframework.stereotype.Component;

@Component("executionStatusToByteConverter")
public class ExecutionStatusToByteConverter implements DataConverter {

    @Override
    public Byte convert(Object value) {
        switch ((AsyncTaskExecutionStatus) value) {
            case SCHEDULED: return 0;
            case ONGOING: return 1;
            case COMPLETED: return 2;
            case ABORTED: return 3;
            case FAILED: return 4;
            case KILLED: return 5;
            default:
                throw new IllegalArgumentException("Unable to convert value: " + value);
        }
    }

    @Override
    public AsyncTaskExecutionStatus convertBack(Object value) {
        switch ((Byte) value) {
            case 0: return AsyncTaskExecutionStatus.SCHEDULED;
            case 1: return AsyncTaskExecutionStatus.ONGOING;
            case 2: return AsyncTaskExecutionStatus.COMPLETED;
            case 3: return AsyncTaskExecutionStatus.ABORTED;
            case 4: return AsyncTaskExecutionStatus.FAILED;
            case 5: return AsyncTaskExecutionStatus.KILLED;
            default:
                throw new IllegalArgumentException("Unable to convert byte value: " + value);
        }
    }
    
}
