package eionet.datadict.dal;

import eionet.datadict.model.CacheEntry;
import eionet.meta.service.DBUnitHelper;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

@SpringApplicationContext("spring-context.xml")
public class CacheDaoTest extends UnitilsJUnit4 {

    @SpringBeanByType
    private CacheDao cacheDao;

    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-cache.xml");
    }

    @Test
    public void testGetCacheEntry() {
        CacheEntry c1 = this.cacheDao.getCacheEntry(1, CacheEntry.ObjectType.DATASET, CacheEntry.ArticleType.PDF);
        assertNotNull(c1);
        assertCacheEntry(createCacheEntry(1, CacheEntry.ObjectType.DATASET, CacheEntry.ArticleType.PDF, "1.pdf", 1473948503331L), c1);

        CacheEntry c2 = this.cacheDao.getCacheEntry(1, CacheEntry.ObjectType.DATASET, CacheEntry.ArticleType.XLS);
        assertNotNull(c2);
        assertCacheEntry(createCacheEntry(1, CacheEntry.ObjectType.DATASET, CacheEntry.ArticleType.XLS, "2.xls", 1473948503331L), c2);

        CacheEntry c3 = this.cacheDao.getCacheEntry(2, CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.XLS);
        assertNotNull(c3);
        assertCacheEntry(createCacheEntry(2, CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.XLS, "3.xls", 1473948503331L), c3);
        
        CacheEntry c4 = this.cacheDao.getCacheEntry(3, CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.XLS);
        assertNull(c4);

        CacheEntry c5 = this.cacheDao.getCacheEntry(1, CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.PDF);
        assertNull(c5);

        CacheEntry c6 = this.cacheDao.getCacheEntry(2, CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.PDF);
        assertNull(c6);
    }

    @Test
    public void testGetCacheEntriesForObjectType() {
        List<CacheEntry> cacheEntries = this.cacheDao.getCacheEntriesForObjectType(1, CacheEntry.ObjectType.DATASET);
        assertEquals(2, cacheEntries.size());
        
        cacheEntries = this.cacheDao.getCacheEntriesForObjectType(1, CacheEntry.ObjectType.TABLE);
        assertTrue(cacheEntries.isEmpty());

        cacheEntries = this.cacheDao.getCacheEntriesForObjectType(2, CacheEntry.ObjectType.TABLE);
        assertEquals(1, cacheEntries.size());

        cacheEntries = this.cacheDao.getCacheEntriesForObjectType(2, CacheEntry.ObjectType.DATASET);
        assertTrue(cacheEntries.isEmpty());

        cacheEntries = this.cacheDao.getCacheEntriesForObjectType(3, CacheEntry.ObjectType.TABLE);
        assertTrue(cacheEntries.isEmpty());
    }

    @Test 
    public void testDeleteCacheEntry() {
        List<CacheEntry> cacheEntries = this.cacheDao.getCacheEntriesForObjectType(1, CacheEntry.ObjectType.DATASET);
        assertEquals(2, cacheEntries.size());
        
        this.cacheDao.deleteCacheEntry(1, CacheEntry.ObjectType.DATASET, CacheEntry.ArticleType.PDF);
        cacheEntries = this.cacheDao.getCacheEntriesForObjectType(1, CacheEntry.ObjectType.DATASET);
        assertEquals(1, cacheEntries.size());

        this.cacheDao.deleteCacheEntry(1, CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.PDF);
        cacheEntries = this.cacheDao.getCacheEntriesForObjectType(1, CacheEntry.ObjectType.DATASET);
        assertEquals(1, cacheEntries.size());

        this.cacheDao.deleteCacheEntry(1, CacheEntry.ObjectType.DATASET, CacheEntry.ArticleType.XLS);
        cacheEntries = this.cacheDao.getCacheEntriesForObjectType(1, CacheEntry.ObjectType.DATASET);
        assertTrue(cacheEntries.isEmpty());
    }

    private CacheEntry createCacheEntry(int id, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType, String fileName, Long createdAt) {
        CacheEntry cacheEntry = new CacheEntry(objectType, articleType);
        cacheEntry.setObjectId(id);
        cacheEntry.setFileName(fileName);
        cacheEntry.setCreatedAt(createdAt);
        return cacheEntry;
    }

    private void assertCacheEntry(CacheEntry expected, CacheEntry actual) {
        assertEquals(expected.getObjectId(), actual.getObjectId());
        assertEquals(expected.getObjectType(), actual.getObjectType());
        assertEquals(expected.getArticleType(), actual.getArticleType());
        assertEquals(expected.getFileName(), actual.getFileName());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }

}
