package eionet.datadict.commons.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    
    public static <T> List<T> filter(Iterable<T> source, Predicate<T> predicate) {
        List<T> results = new ArrayList<T>();
        
        for (T item : source) {
            if (predicate.evaluate(item)) {
                results.add(item);
            }
        }
        
        return results;
    }
    
    public static <T, S> List<S> select(Iterable<T> source, Selector<T, S> selector) {
        List<S> results = new ArrayList<S>();
        
        for (T item : source) {
            results.add(selector.select(item));
        }
        
        return results;
    }
    
}
