package eionet.datadict.errors;

public class IllegalParameterException extends BadRequestException {
    
    private final String paramName;
    private final String paramValue;
    
    public IllegalParameterException(String paramName, String paramValue) {
        super(String.format("'%s' is not an acceptable value for parameter %s", paramValue, paramName));
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamValue() {
        return paramValue;
    }
    
}
