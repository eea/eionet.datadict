package eionet.datadict.services.data.impl;

import eionet.datadict.dal.CacheDao;
import eionet.datadict.dal.CleanupDao;
import eionet.datadict.model.CacheEntry;
import eionet.datadict.services.data.CleanupService;
import eionet.meta.DElemAttribute;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.ITableDAO;
import eionet.meta.dao.domain.FixedValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class CleanupServiceTest {
    
    private CleanupService cleanupService;

    @Mock
    private CleanupDao cleanupDao;

    @Mock
    private ITableDAO tableDao;

    @Mock
    private IDataElementDAO dataElementDao;

    @Mock
    private IAttributeDAO attributeDao;

    @Mock
    private IFixedValueDAO fixedValueDao;

    @Mock
    private CacheDao cacheDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.cleanupService = new CleanupServiceImpl(cleanupDao, tableDao, dataElementDao, attributeDao, fixedValueDao, cacheDao);
    }

    @Test 
    public void testDeleteBrokenDatasetToTableRelations() {
        final int expectedResult = 3;
        when(cleanupDao.deleteBrokenDatasetToTableRelations()).thenReturn(expectedResult);
        int actualResult = this.cleanupService.deleteBrokenDatasetToTableRelations();
        verify(cleanupDao, times(1)).deleteBrokenDatasetToTableRelations();
        assertEquals(expectedResult, actualResult);
    }

    @Test 
    public void testDeleteOrphanTables() {
        final List<Integer> tableIds = Arrays.asList(new Integer[] {1, 2});
        when(tableDao.getOrphanTableIds()).thenReturn(tableIds);
        when(tableDao.delete(tableIds)).thenReturn(tableIds.size());
        int actualResult = this.cleanupService.deleteOrphanTables();

        verify(tableDao, times(1)).getOrphanTableIds();
        verify(attributeDao, times(1)).deleteAttributes(tableIds, CleanupDao.TABLE_PARENT_TYPE);
        verify(cleanupDao, times(1)).deleteDatasetToTableRelations(tableIds);
        verify(cleanupDao, times(1)).deleteTableRelationsWithElements(tableIds);
        verify(cacheDao, times(1)).deleteCacheEntries(tableIds, CacheEntry.ObjectType.TABLE);
        verify(cleanupDao, times(1)).deleteDocs(CleanupDao.TABLE_OWNER_TYPE, tableIds);
        verify(tableDao, times(1)).delete(tableIds);

        assertEquals(tableIds.size(), actualResult);
    }

    @Test 
    public void testDeleteOrphanTablesForEmptyTableIds() {
        final List<Integer> emptyTableIds = Collections.EMPTY_LIST;
        when(tableDao.getOrphanTableIds()).thenReturn(emptyTableIds);
        int actualResult = this.cleanupService.deleteOrphanTables();

        verify(tableDao, times(1)).getOrphanTableIds();
        verify(attributeDao, times(0)).deleteAttributes(emptyTableIds, CleanupDao.TABLE_PARENT_TYPE);
        verify(cleanupDao, times(0)).deleteDatasetToTableRelations(emptyTableIds);
        verify(cleanupDao, times(0)).deleteTableRelationsWithElements(emptyTableIds);
        verify(cacheDao, times(0)).deleteCacheEntries(emptyTableIds, CacheEntry.ObjectType.TABLE);
        verify(cleanupDao, times(0)).deleteDocs(CleanupDao.TABLE_OWNER_TYPE, emptyTableIds);
        verify(tableDao, times(0)).delete(emptyTableIds);
        
        assertEquals(emptyTableIds.size(), actualResult);
    }

    @Test 
    public void testDeleteBrokenTableToElementRelations() {
        final int expectedResult = 3;
        when(cleanupDao.deleteBrokenTableToElementRelations()).thenReturn(expectedResult);
        int actualResult = this.cleanupService.deleteBrokenTableToElementRelations();
        verify(cleanupDao, times(1)).deleteBrokenTableToElementRelations();
        assertEquals(expectedResult, actualResult);
    }

    @Test 
    public void testDeleteOrphanNonCommonDataElements() {
        final List<Integer> nonCommonElementIds = Arrays.asList(new Integer[] {1, 2});
        when(dataElementDao.getOrphanNonCommonDataElementIds()).thenReturn(nonCommonElementIds);
        when(dataElementDao.delete(nonCommonElementIds)).thenReturn(nonCommonElementIds.size());
        int actualResult = this.cleanupService.deleteOrphanNonCommonDataElements();

        verify(dataElementDao, times(1)).getOrphanNonCommonDataElementIds();
        verify(attributeDao, times(1)).deleteAttributes(nonCommonElementIds, DElemAttribute.ParentType.ELEMENT.toString());
        verify(cleanupDao, times(1)).deleteElementRelationsWithTables(nonCommonElementIds);
        verify(cacheDao, times(1)).deleteCacheEntries(nonCommonElementIds, CacheEntry.ObjectType.ELEMENT);
        verify(cleanupDao, times(1)).deleteDocs(CleanupDao.DATA_ELEMENT_OWNER_TYPE, nonCommonElementIds);
        verify(cleanupDao, times(1)).deleteForeignKeyRelations(nonCommonElementIds);
        verify(fixedValueDao, times(1)).delete(FixedValue.OwnerType.DATA_ELEMENT, nonCommonElementIds);
        verify(cleanupDao, times(1)).deleteInferenceRules(nonCommonElementIds);
        verify(dataElementDao, times(1)).delete(nonCommonElementIds);

        assertEquals(nonCommonElementIds.size(), actualResult);
    }

    @Test 
    public void testDeleteOrphanNonCommonDataElementsForEmptyElementIds() {
        final List<Integer> emptyNonCommonElementIds = Collections.EMPTY_LIST;
        when(dataElementDao.getOrphanNonCommonDataElementIds()).thenReturn(emptyNonCommonElementIds);
        int actualResult = this.cleanupService.deleteOrphanNonCommonDataElements();

        verify(dataElementDao, times(1)).getOrphanNonCommonDataElementIds();
        verify(attributeDao, times(0)).deleteAttributes(emptyNonCommonElementIds, DElemAttribute.ParentType.ELEMENT.toString());
        verify(cleanupDao, times(0)).deleteElementRelationsWithTables(emptyNonCommonElementIds);
        verify(cacheDao, times(0)).deleteCacheEntries(emptyNonCommonElementIds, CacheEntry.ObjectType.ELEMENT);
        verify(cleanupDao, times(0)).deleteDocs(CleanupDao.DATA_ELEMENT_OWNER_TYPE, emptyNonCommonElementIds);
        verify(cleanupDao, times(0)).deleteForeignKeyRelations(emptyNonCommonElementIds);
        verify(fixedValueDao, times(0)).delete(FixedValue.OwnerType.DATA_ELEMENT, emptyNonCommonElementIds);
        verify(cleanupDao, times(0)).deleteInferenceRules(emptyNonCommonElementIds);
        verify(dataElementDao, times(0)).delete(emptyNonCommonElementIds);

        assertEquals(emptyNonCommonElementIds.size(), actualResult);
    }

    @Test 
    public void testDeleteOrphanNamespaces() {
        final int expectedResult = 3;
        when(cleanupDao.deleteOrphanNamespaces()).thenReturn(expectedResult);
        int actualResult = this.cleanupService.deleteOrphanNamespaces();
        verify(cleanupDao, times(1)).deleteOrphanNamespaces();
        assertEquals(expectedResult, actualResult);
    }

    @Test 
    public void testDeleteOrphanAcls() {
        final int expectedResult = 3;
        when(cleanupDao.deleteOrphanAcls()).thenReturn(expectedResult);
        int actualResult = this.cleanupService.deleteOrphanAcls();
        verify(cleanupDao, times(1)).deleteOrphanAcls();
        assertEquals(expectedResult, actualResult);
    }

}
