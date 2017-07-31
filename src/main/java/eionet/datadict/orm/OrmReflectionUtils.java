package eionet.datadict.orm;

import eionet.datadict.commons.util.IterableUtils;
import eionet.datadict.commons.util.Predicate;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.apache.commons.lang3.reflect.FieldUtils;

class OrmReflectionUtils {
    
    public static Field getIdField(Class<?> entityType) {
        Field[] fields = FieldUtils.getFieldsWithAnnotation(entityType, Id.class);
        
        if (fields.length == 0) {
            String msg = String.format("Cannot find @Id property in %s.", entityType.getName());
            throw new NoSuchPropertyException(msg);
        }
        
        if (fields.length > 1) {
            String msg = String.format("Multiple definitions of @Id property in %s", entityType.getName());
            throw new AmbiguousMatchException(msg);
        }
        
        return fields[0];
    }
    
    public static Field[] getIdDrilldownFields(Class<?> entityType) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> type = entityType;
        
        do {
            Field f = OrmReflectionUtils.getIdField(type);
            fields.add(f);
            type = f.getType();
        }
        while (!Comparable.class.isAssignableFrom(type));
        
        return fields.toArray(new Field[fields.size()]);
    }
    
    public static RelationInfo getParentChildRelationInfo(Class<?> parentType, Class<?> childType, String childPropertyName) {
        return new OneToAnyRelationInfoExtractor().getRelationInfo(parentType, childType, childPropertyName);
    }
    
    public static RelationInfo inferParentChildRelationInfo(final Class<?> parentType, final Class<?> childType) {
        List<Field> childTypeCandidates = IterableUtils.filter(FieldUtils.getAllFieldsList(childType), new Predicate<Field>() {

            @Override
            public boolean evaluate(Field obj) {
                ManyToOne manyToOneAnnotation = obj.getAnnotation(ManyToOne.class);
                
                if (manyToOneAnnotation != null) {
                    return obj.getType().equals(parentType);
                }
          
                /**Class[] infs =  parentType.getInterfaces();
              
                OneToOne oneToOneAnnotation = parentType.getAnnotation(OneToOne.class);
                for (Class inf : infs) {
                    if (obj.getType().equals(inf)&& (oneToOneAnnotation != null||(obj.getAnnotation(OneToOne.class))!=null)) {
                        return true;
                    }
                }
                **/
                OneToOne oneToOneAnnotation = obj.getAnnotation(OneToOne.class);
                return oneToOneAnnotation != null && obj.getType().equals(parentType);
            }
            
        });
        
        if (childTypeCandidates.size() == 0) {
            String msg = String.format("No relation candidates found between %s and %s.", parentType.getName(), childType.getName());
            throw new InvalidRelationException(msg);
        }
        
        if (childTypeCandidates.size() > 1) {
            String msg = String.format("Multiple relation candidates found between %s and %s.", parentType.getName(), childType.getName());
            throw new AmbiguousMatchException(msg);
        }
        
        return getParentChildRelationInfo(parentType, childType, childTypeCandidates.get(0).getName());
    }
    
    public static Object readField(Field field, Object target) {
        try {
            return FieldUtils.readField(field, target, true);
           
        } 
        catch (IllegalAccessException ex) {
            throw new InaccessiblePropertyException(ex);
        }
    }
    
    public static void writeField(Field field, Object target, Object value) {
        try {
            FieldUtils.writeField(field, target, value, true);
        } 
        catch (IllegalAccessException ex) {
            throw new InaccessiblePropertyException(ex);
        }
    }
    
    public static Object readFieldDrillDown(Field[] drillDown, Object target) {
        Object obj = target;
        int i;
        
        for (i = 0; i < drillDown.length - 1; i++) {
            obj = OrmReflectionUtils.readField(drillDown[i], obj);
        }
        
        return OrmReflectionUtils.readField(drillDown[i], obj);
    }
    
}
