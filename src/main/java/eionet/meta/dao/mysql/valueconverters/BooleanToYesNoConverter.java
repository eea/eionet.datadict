package eionet.meta.dao.mysql.valueconverters;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public final class BooleanToYesNoConverter implements ValueConverter<Boolean, String> {

    private static final String TOKEN_YES = "Y";
    private static final String TOKEN_NO = "N";
    
    @Override
    public String convert(Boolean value) {
        return value ? TOKEN_YES : TOKEN_NO;
    }

    @Override
    public Boolean convertBack(String value) {
        if (StringUtils.isBlank(value) || TOKEN_NO.equalsIgnoreCase(value)) {
            return false;
        }
        else if (TOKEN_YES.equalsIgnoreCase(value)) {
            return true;
        }
        
        throw new IllegalArgumentException();
    }
    
}
