package eionet.datadict.dal;

import eionet.datadict.model.CacheEntry;
import java.util.List;

public interface CacheDao {

    CacheEntry getCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType);

    List<CacheEntry> getCacheEntriesForObjectType(int objectId, CacheEntry.ObjectType objectType);

    void deleteCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType);
    
    void saveCacheEntry(CacheEntry cacheEntry);

}
