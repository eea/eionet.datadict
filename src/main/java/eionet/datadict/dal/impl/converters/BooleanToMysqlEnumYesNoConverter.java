package eionet.datadict.dal.impl.converters;

import eionet.datadict.util.data.DataConverter;
import org.apache.commons.lang3.StringUtils;


public class BooleanToMysqlEnumYesNoConverter implements DataConverter<Boolean, String>{
    private final Boolean defaultValue;
    
    public BooleanToMysqlEnumYesNoConverter(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @Override
    public String convert(Boolean value) {
        if (value == null) return null;
        
        if (value) return "Y";
        else return "N";
    }

    @Override
    public Boolean convertBack(String value) {
        
        if (value == null) return null;
        
        //Some DB entries have "" value which is used as a valid entry for invalid values.
        //Here invalid entries are translated into the default value for this field    
        if (StringUtils.equals(value, "")) {
            return defaultValue;
        } 
        
        if (StringUtils.equals(value, "Y")){
            return true;
        }
        
        if (StringUtils.equals(value, "N")) {
            return false;
        }
        
        throw new IllegalArgumentException(String.format("Unable to convert string value to boolean: %s", value));
        
    }
    
}
