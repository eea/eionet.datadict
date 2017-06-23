package eionet.datadict.orm;

import java.lang.reflect.Field;

interface OneToAnyRelationInfoSubExtractor {
        
    boolean isMatch(Field childEndpoint);

    RelationType getRelationType();

    Field getParentEndpoint(Class<?> parentType, Field childEndpoint);

}
