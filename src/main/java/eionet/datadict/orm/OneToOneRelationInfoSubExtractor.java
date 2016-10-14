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
        return this.getChildEndpointAnnotation(childEndpoint) != null;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.ONE_TO_ONE;
    }

    @Override
    public Field getParentEndpoint(Class<?> parentType, Field childEndpoint) {
        OneToOne childEndpointAnnotation = this.getChildEndpointAnnotation(childEndpoint);

        if (StringUtils.isBlank(childEndpointAnnotation.mappedBy())) {
            return this.getParentEndpointByMatch(parentType, childEndpoint.getName());
        }
        else {
            return this.getParentEndpointByChildAnnotationMapping(parentType, childEndpoint.getName(), childEndpointAnnotation);
        }
    }

    private OneToOne getChildEndpointAnnotation(Field childEndpoint) {
        return childEndpoint.getAnnotation(OneToOne.class);
    }

    private Field getParentEndpointByChildAnnotationMapping(Class<?> parentType, String childPropertyName, OneToOne childEndpointAnnotation) {
        Field parentEndpoint = FieldUtils.getField(parentType, childEndpointAnnotation.mappedBy(), true);

        if (parentEndpoint == null) {
            return parentEndpoint;
        }

        OneToOne parentEndpointAnnotation = parentEndpoint.getAnnotation(OneToOne.class);

        if (parentEndpointAnnotation == null || StringUtils.isBlank(parentEndpointAnnotation.mappedBy())) {
            return parentEndpoint;
        }

        if (!StringUtils.equals(parentEndpointAnnotation.mappedBy(), childPropertyName)) {
            String msg = String.format("Invalid relation endpoint target in %s. Found: %s; required: %s.", 
                    parentType.getName(), parentEndpointAnnotation.mappedBy(), childPropertyName);
            throw new InvalidRelationException(msg);
        }

        return parentEndpoint;
    }

    private Field getParentEndpointByMatch(Class<?> parentType, String childPropertyName) {
        final String mappedBy = childPropertyName;

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
