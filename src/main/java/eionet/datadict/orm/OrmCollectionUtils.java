package eionet.datadict.orm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrmCollectionUtils {

    public static <T> Set<T> createChildCollection(Class<T> childType) {
        return new HashSet<T>();
    }
    
    public static <T> Set<T> createChildCollection(Collection<T> children) {
        return new HashSet<T>(children);
    }
    
    public static <T> List<T> toList(T item) {
        List<T> list = new ArrayList<T>(1);
        list.add(item);
        
        return list;
    }
    
}
