package eionet.datadict.errors;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class EmptyParameterException extends BadRequestException {
    
    private final String paramName;
    
    public EmptyParameterException(String paramName) {
        super(String.format("Parameter %s cannot be null", paramName));
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }
    
}
