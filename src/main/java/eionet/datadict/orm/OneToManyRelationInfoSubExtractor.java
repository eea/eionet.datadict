package eionet.datadict.orm;

import java.lang.reflect.Field;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

class OneToManyRelationInfoSubExtractor implements OneToAnyRelationInfoSubExtractor {

    @Override
    public boolean isMatch(Field childEndpoint) {
        return childEndpoint.getAnnotation(ManyToOne.class) != null;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.ONE_TO_MANY;
    }

    @Override
    public Field getParentEndpoint(Class<?> parentType, Field childEndpoint) {
        final String mappedBy = childEndpoint.getName();

        return (Field) CollectionUtils.find(
            FieldUtils.getFieldsListWithAnnotation(parentType, OneToMany.class), 
            new Predicate() {

                @Override
                public boolean evaluate(Object o) {
                    OneToMany annotation = ((Field) o).getAnnotation(OneToMany.class);
                    return StringUtils.equals(annotation.mappedBy(), mappedBy);
                }
            }
        );
    }

}
