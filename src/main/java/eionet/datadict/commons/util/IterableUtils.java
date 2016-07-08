package eionet.datadict.commons.util;

import java.util.Iterator;

public class IterableUtils {

    public static <T> T firstOrDefault(Iterable<T> source) {
        return firstOrDefault(source, null);
    }
    
    public static <T> T firstOrDefault(Iterable<T> source, T defaultValue) {
        Iterator<T> iter = source.iterator();
        
        return iter.hasNext() ? iter.next() : defaultValue;
    }
    
}
