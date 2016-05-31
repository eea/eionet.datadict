package eionet.datadict.dal.impl.converters;

import eionet.datadict.util.data.DataConverter;
import org.apache.commons.lang.StringUtils;

public class BooleanToMySqlEnumConverter implements DataConverter<Boolean, String> {

    @Override
    public String convert(Boolean value) {
        if (value == null) {
            return null;
        }
        
        return value ? "1" : "0";
    }

    @Override
    public Boolean convertBack(String value) {
        if (value == null) {
            return null;
        }
        
        if (StringUtils.equals(value, "0")) {
            return false;
        }
        
        if (StringUtils.equals(value, "1")) {
            return true;
        }
        
        throw new IllegalArgumentException(String.format("Unable to convert string value to boolean: %s", value));
    }
    
}
