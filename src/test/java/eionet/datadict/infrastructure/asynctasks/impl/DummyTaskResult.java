package eionet.datadict.infrastructure.asynctasks.impl;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public class DummyTaskResult {

    private int value;
    private String message;

    public DummyTaskResult() { }

    public DummyTaskResult(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof DummyTaskResult)) {
            return false;
        }
        
        DummyTaskResult other = (DummyTaskResult) obj;
        
        return this.value == other.value && StringUtils.equals(this.message, other.message);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] { this.value, this.message });
    }
    
}
