package eionet.datadict.orm;

import java.util.Collection;
import java.util.HashSet;

public class OrmCollectionUtils {

    public static <T> Collection<T> createChildCollection(Class<T> childType) {
        return new HashSet<T>();
    }
    
}
