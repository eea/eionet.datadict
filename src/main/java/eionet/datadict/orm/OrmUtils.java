package eionet.datadict.orm;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import eionet.datadict.commons.util.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;

public class OrmUtils {

    public static <T> void sortById(List<T> entities) {
        if (entities.isEmpty()) {
            return;
        }
        
        Class<?> entityType = entities.get(0).getClass();
        Comparator<T> cmp = new ReflectiveDrillDownComparator<T>(OrmReflectionUtils.getIdField(entityType));
        Collections.sort(entities, cmp);
    }
    
    public static <T, S> void link(T parentEntity, S childEntity) {
        link(OrmCollectionUtils.toList(parentEntity), OrmCollectionUtils.toList(childEntity));
    }
    
    public static <T, S> void link(T parentEntity, S childEntity, String childEndpointName) {
        link(OrmCollectionUtils.toList(parentEntity), OrmCollectionUtils.toList(childEntity), childEndpointName);
    }
    
    public static <T, S> void link(T parentEntity, List<S> childEntities) {
        link(OrmCollectionUtils.toList(parentEntity), childEntities);
    }
    
    public static <T, S> void link(T parentEntity, List<S> childEntities, String childEndpointName) {
        link(OrmCollectionUtils.toList(parentEntity), childEntities, childEndpointName);
    }
    
    public static <T, S> void link(List<T> parentEntities, List<S> childEntities) {
        link(parentEntities, childEntities, new RelationRetrievalByInferenceStrategy());
    }
    
    public static <T, S> void link(List<T> parentEntities, List<S> childEntities, String childEndpointName) {
        link(parentEntities, childEntities, new RelationRetrievalByNamedEndpointStrategy(childEndpointName));
    }
    
    static <T, S> void link(List<T> parentEntities, List<S> childEntities, RelationRetrievalStrategy relationRetrievalStrategy) {
        if (parentEntities.isEmpty() || childEntities.isEmpty()) {
            return;
        }
        
        Class<?> parentType = parentEntities.get(0).getClass();
        Class<?> childType = childEntities.get(0).getClass();
        RelationInfo relationInfo = relationRetrievalStrategy.retrieveRelationInfo(parentType, childType);
        Field[] parentIdFields = OrmReflectionUtils.getIdDrilldownFields(parentType);
              Field[] childIdFields = OrmReflectionUtils.getIdDrilldownFields(childType);
              
        Field[] childDrillDownFields = ArrayUtils.addAll(new Field[] { relationInfo.getChildEndpoint() }, childIdFields);
        
        
       //    Field[] childDrillDownFields = ArrayUtils.addAll(new Field[] { relationInfo.getChildEndpoint() }); 
        Collections.sort(parentEntities, new ReflectiveDrillDownComparator<T>(parentIdFields));
        Collections.sort(childEntities, new ReflectiveDrillDownComparator<S>(childDrillDownFields));
        
        OneToAnyPairLinker<T, S> pairLinker = createPairLinker(relationInfo.getRelationType());
        Iterator<T> parentIt = parentEntities.iterator();
        Iterator<S> childIt = childEntities.iterator();
        T parentObject = IterableUtils.nextOrDefault(parentIt);
        S childObject = IterableUtils.nextOrDefault(childIt);
        
        while (parentObject != null && childObject != null) {
            Comparable parentObjectKey = (Comparable) OrmReflectionUtils.readFieldDrillDown(parentIdFields, parentObject);
            Comparable childObjectKey = (Comparable) OrmReflectionUtils.readFieldDrillDown(childDrillDownFields, childObject);
            int cmpResult = parentObjectKey.compareTo(childObjectKey);
            
            if (cmpResult == 0) {
                pairLinker.onPairMatch(parentObject, childObject, relationInfo);
                childObject = IterableUtils.nextOrDefault(childIt);
            }
            else if (cmpResult < 0) {
                parentObject = IterableUtils.nextOrDefault(parentIt);
                pairLinker.onPairMismatch();
            }
            else {
                childObject = IterableUtils.nextOrDefault(childIt);
                pairLinker.onPairMismatch();
            }
        }
    }
    
    static <T, S> OneToAnyPairLinker<T, S> createPairLinker(RelationType relationType) {
        switch (relationType) {
            case ONE_TO_MANY:
                return new OneToManyPairLinker<T, S>();            
            case ONE_TO_ONE:
                return new OneToOnePairLinker<T, S>();
            default:
                throw new IllegalStateException(String.format("Unexpected relation type: %s", relationType));
        }
    }
    
    static interface RelationRetrievalStrategy {
        
        RelationInfo retrieveRelationInfo(Class<?> parentType, Class<?> childType);
        
    }
    
    static class RelationRetrievalByInferenceStrategy implements RelationRetrievalStrategy {

        @Override
        public RelationInfo retrieveRelationInfo(Class<?> parentType, Class<?> childType) {
            return OrmReflectionUtils.inferParentChildRelationInfo(parentType, childType);
        }
        
    }
    
    static class RelationRetrievalByNamedEndpointStrategy implements RelationRetrievalStrategy {

        private final String childEndpointName;
        
        public RelationRetrievalByNamedEndpointStrategy(String childEndpointName) {
            this.childEndpointName = childEndpointName;
        }
        
        @Override
        public RelationInfo retrieveRelationInfo(Class<?> parentType, Class<?> childType) {
            return OrmReflectionUtils.getParentChildRelationInfo(parentType, childType, childEndpointName);
        }
        
    }
    
    static class ReflectiveDrillDownComparator<T> implements Comparator<T> {

        private final Field[] drillDownFields;

        public ReflectiveDrillDownComparator(Field... drillDownFields) {
            if (drillDownFields.length == 0) {
                throw new IllegalArgumentException("Must provide at least one field.");
            }

            this.drillDownFields = drillDownFields;
        }

        @Override
        public int compare(T o1, T o2) {
            Comparable value1 = (Comparable) OrmReflectionUtils.readFieldDrillDown(drillDownFields, o1);
            Comparable value2 = (Comparable) OrmReflectionUtils.readFieldDrillDown(drillDownFields, o2);

            return value1.compareTo(value2);
        }

    }
    
}
