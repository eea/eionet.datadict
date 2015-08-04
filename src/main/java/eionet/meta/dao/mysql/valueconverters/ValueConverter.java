package eionet.meta.dao.mysql.valueconverters;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface ValueConverter<T1, T2> {
    
    T2 convert(T1 value);
    
    T1 convertBack(T2 value);
}
