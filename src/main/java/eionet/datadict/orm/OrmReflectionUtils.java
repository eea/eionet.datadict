package eionet.datadict.orm;

import java.lang.reflect.Field;
import javax.persistence.Id;
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
            throw new AmbiguousPropertyMatchException(msg);
        }
        
        return fields[0];
    }
    
    public static RelationInfo getParentChildRelationInfo(Class<?> parentType, Class<?> childType, String childPropertyName) {
        return new OneToAnyRelationInfoExtractor().getRelationInfo(parentType, childType, childPropertyName);
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
