package eionet.datadict.orm;

import java.lang.reflect.Field;

class RelationInfo {
    
    private final Field parentEndpoint;
    private final Field childEndpoint;
    private final RelationType relationType;

    public RelationInfo(Field childEndpoint, RelationType relationType) {
        this(null, childEndpoint, relationType);
    }

    public RelationInfo(Field parentEndpoint, Field childEndpoint, RelationType relationType) {
        this.parentEndpoint = parentEndpoint;
        this.childEndpoint = childEndpoint;
        this.relationType = relationType;
    }

    public Field getParentEndpoint() {
        return parentEndpoint;
    }

    public Field getChildEndpoint() {
        return childEndpoint;
    }
    
    public boolean isBidirectional() {
        return this.parentEndpoint != null;
    }
    
    public RelationType getRelationType() {
        return relationType;
    }
    
}
