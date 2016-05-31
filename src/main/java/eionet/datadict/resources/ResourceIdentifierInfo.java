package eionet.datadict.resources;

import org.apache.commons.lang.StringUtils;

public class ResourceIdentifierInfo implements ResourceIdInfo {

    private final String[] identifierParts;
    
    public ResourceIdentifierInfo(String... identifierParts) {
        this.identifierParts = identifierParts;
    }
    
    @Override
    public String getIdDescription() {
        return "identifier: " + StringUtils.join(identifierParts, "/");
    }
    
}
