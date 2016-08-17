package eionet.datadict.util.data;

public interface DataConverter<T1, T2> {
    
    T2 convert(T1 value);
    
    T1 convertBack(T2 value);
    
}
