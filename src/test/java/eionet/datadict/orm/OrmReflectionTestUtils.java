package eionet.datadict.orm;

import java.lang.reflect.Field;

public class OrmReflectionTestUtils {
    
    public static <T> T newEntityWithSameIdAs(T entity) {
        T newEntity;
        
        try {
            newEntity = (T) entity.getClass().newInstance();
        }
        catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        
        Field idField = OrmReflectionUtils.getIdField(entity.getClass());
        OrmReflectionUtils.writeField(idField, newEntity, OrmReflectionUtils.readField(idField, entity));
        
        return newEntity;
    }
    
}
