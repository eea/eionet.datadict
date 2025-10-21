package eionet.datadict.dal.impl.converters;

import eionet.datadict.util.data.DataConverter;
import org.apache.commons.lang3.StringUtils;

public class BooleanToMySqlEnumConverter implements DataConverter<Boolean, String> {

    private final Boolean defaultValue;
    
    public BooleanToMySqlEnumConverter(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
    
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
        //Some DB entries have "" value which is used as a valid entry for invalid values.
        //Here invalid entries are translated into the default value for this field    
        if (StringUtils.equals(value, "")) {
            return defaultValue;
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
