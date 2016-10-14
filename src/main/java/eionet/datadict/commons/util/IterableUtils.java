package eionet.datadict.commons.util;

import java.util.Iterator;

public class IterableUtils {

    public static <T> T firstOrDefault(Iterable<T> source) {
        return firstOrDefault(source, null);
    }
    
    public static <T> T firstOrDefault(Iterable<T> source, T defaultValue) {
        return nextOrDefault(source.iterator(), defaultValue);
    }
    
    public static <T> T nextOrDefault(Iterator<T> it) {
        return nextOrDefault(it, null);
    }
    
    public static <T> T nextOrDefault(Iterator<T> it, T defaultValue) {
        return it.hasNext() ? it.next() : defaultValue;
    }
    
}
