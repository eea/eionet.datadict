package eionet.datadict.orm;

import java.lang.reflect.Field;
import javax.persistence.OneToOne;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

class OneToOneRelationInfoSubExtractor implements OneToAnyRelationInfoSubExtractor {

    @Override
    public boolean isMatch(Field childEndpoint) {
        return childEndpoint.getAnnotation(OneToOne.class) != null;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.ONE_TO_ONE;
    }

    @Override
    public Field getParentEndpoint(Class<?> parentType, Field childEndpoint) {
        final String mappedBy = childEndpoint.getName();

        return (Field) CollectionUtils.find(
            FieldUtils.getFieldsListWithAnnotation(parentType, OneToOne.class),
            new Predicate() {

                @Override
                public boolean evaluate(Object o) {
                    OneToOne annotation = ((Field) o).getAnnotation(OneToOne.class);
                    return StringUtils.equals(annotation.mappedBy(), mappedBy);
                }

            }
        );
    }

}
