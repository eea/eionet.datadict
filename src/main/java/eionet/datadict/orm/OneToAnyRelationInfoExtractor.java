package eionet.datadict.orm;

import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;

class OneToAnyRelationInfoExtractor {

    private static final OneToAnyRelationInfoSubExtractor[] SUB_EXTRACTORS;
    
    static {
        SUB_EXTRACTORS = new OneToAnyRelationInfoSubExtractor[] { 
            new OneToManyRelationInfoSubExtractor(), new OneToOneRelationInfoSubExtractor()
        };
    }
    
    public RelationInfo getRelationInfo(Class<?> parentType, Class<?> childType, String childPropertyName) {
        Field childEndpoint = FieldUtils.getField(childType, childPropertyName, true);
        
        if (childEndpoint == null) {
            String msg = String.format("Cannot find property %s in %s.", childPropertyName, childType.getName());
            throw new NoSuchPropertyException(msg);
        }
        
        OneToAnyRelationInfoSubExtractor subExtractor = null;
        
        for (OneToAnyRelationInfoSubExtractor subextr : SUB_EXTRACTORS) {
            if (subextr.isMatch(childEndpoint)) {
                subExtractor = subextr;
                break;
            }
        }
        
        if (subExtractor == null) {
            String msg = String.format("Property %s does not suggest a * to one relation in %s.", childPropertyName, childType.getName());
            throw new InvalidRelationException(msg);
        }
        
        Field parentEndpoint = subExtractor.getParentEndpoint(parentType, childEndpoint);
        
        return new RelationInfo(parentEndpoint, childEndpoint, subExtractor.getRelationType());
    }
    
}
