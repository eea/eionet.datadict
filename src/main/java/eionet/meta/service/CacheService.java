package eionet.meta.service;

import eionet.meta.dao.domain.CacheEntry;
import java.util.List;

public interface CacheService {

    String getCachePath();

    CacheEntry getCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType);

    List<CacheEntry> getCacheEntriesForObjectType(int objectId, CacheEntry.ObjectType objectType);

    void deleteCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType);

    boolean deletePhysicalFile(String fileName);

    String getCachableObjectIdentifier(int objectId, CacheEntry.ObjectType objectType);

}
