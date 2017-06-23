package eionet.datadict.commons.util;

public interface Selector<T, S> {
    
    S select(T obj);
    
}
