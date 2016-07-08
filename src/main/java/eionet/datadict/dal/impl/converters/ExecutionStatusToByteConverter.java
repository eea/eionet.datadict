package eionet.datadict.dal.impl.converters;

import eionet.datadict.data.DataConverter;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import org.springframework.stereotype.Component;

@Component("executionStatusToByteConverter")
public class ExecutionStatusToByteConverter implements DataConverter {

    @Override
    public Byte convert(Object value) {
        switch ((AsyncTaskExecutionStatus) value) {
            case ONGOING: return 0;
            case COMPLETED: return 1;
            case ABORTED: return 2;
            case FAILED: return 3;
            case KILLED: return 4;
            default:
                throw new IllegalArgumentException("Unable to convert value: " + value);
        }
    }

    @Override
    public AsyncTaskExecutionStatus convertBack(Object value) {
        switch ((Byte) value) {
            case 0: return AsyncTaskExecutionStatus.ONGOING;
            case 1: return AsyncTaskExecutionStatus.COMPLETED;
            case 2: return AsyncTaskExecutionStatus.ABORTED;
            case 3: return AsyncTaskExecutionStatus.FAILED;
            case 4: return AsyncTaskExecutionStatus.KILLED;
            default:
                throw new IllegalArgumentException("Unable to convert byte value: " + value);
        }
    }
    
}
