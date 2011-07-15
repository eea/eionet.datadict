package eionet.meta.exports;


public interface CachableIF {
    
    public abstract void updateCache(String id) throws Exception;
    public abstract void clearCache(String id) throws Exception;
    public abstract void setCachePath(String path) throws Exception;
    public abstract boolean isCached(String id) throws Exception;
}
