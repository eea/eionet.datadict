package eionet.datadict.services.data.impl;

import eionet.datadict.services.data.CacheService;
import eionet.datadict.dal.CacheDao;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.ITableDAO;
import eionet.datadict.model.CacheEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class CacheServiceTest {

    private CacheService cacheService;

    @Mock
    private CacheDao cacheDao;

    @Mock
    private IDataSetDAO dataSetDao;

    @Mock
    private ITableDAO tableDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.cacheService = new CacheServiceImpl(cacheDao, dataSetDao, tableDao);
    }

    public void testGetPath() {
        ((CacheServiceImpl) this.cacheService).initCachePath();
    }

    @Test
    public void testGetCacheEntry() {
        int objectId = 1;
        CacheEntry.ObjectType objectType = CacheEntry.ObjectType.DATASET;
        CacheEntry.ArticleType articleType = CacheEntry.ArticleType.PDF;

        CacheEntry expected = new CacheEntry(objectType, articleType);
        expected.setObjectId(objectId);

        when(cacheDao.getCacheEntry(objectId, objectType, articleType)).thenReturn(expected);
        CacheEntry actual = this.cacheService.getCacheEntry(objectId, objectType, articleType);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCacheEntriesForObjectType() {
        int objectId = 1;
        CacheEntry.ObjectType objectType = CacheEntry.ObjectType.DATASET;

        this.cacheService.getCacheEntriesForObjectType(objectId, objectType);
        verify(cacheDao, times(1)).getCacheEntriesForObjectType(objectId, objectType);
    }

    @Test 
    public void testDeleteCacheEntry() {
        int objectId = 1;
        CacheEntry.ObjectType objectType = CacheEntry.ObjectType.DATASET;
        CacheEntry.ArticleType articleType = CacheEntry.ArticleType.PDF;

        this.cacheService.deleteCacheEntry(objectId, objectType, articleType);
        verify(cacheDao, times(1)).deleteCacheEntry(objectId, objectType, articleType);
    }

    @Test
    public void getCachableObjectIdentifier() {
        int objectId = 1;
        CacheEntry.ObjectType objectType = CacheEntry.ObjectType.DATASET;
        
        this.cacheService.getCachableObjectIdentifier(objectId, objectType);
        verify(dataSetDao, times(1)).getIdentifierById(1);

        objectType = CacheEntry.ObjectType.TABLE;
        this.cacheService.getCachableObjectIdentifier(objectId, objectType);
        verify(tableDao, times(1)).getIdentifierById(1);

        objectType = CacheEntry.ObjectType.ELEMENT;
        assertNull(this.cacheService.getCachableObjectIdentifier(objectId, objectType));
    }

}
