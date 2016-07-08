package eionet.datadict.data;

public interface DataConverter {

    Object convert(Object value);
    
    Object convertBack(Object value);
    
}
