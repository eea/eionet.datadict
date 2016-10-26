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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CleanupServiceImpl implements CleanupService {
    
    private final CleanupDao cleanupDao;
    private final ITableDAO tableDao;
    private final IDataElementDAO dataElementDao;
    private final IAttributeDAO attributeDao;
    private final IFixedValueDAO fixedValueDao;
    private final CacheDao cacheDao;
    
    @Autowired
    public CleanupServiceImpl(CleanupDao cleanupDao, ITableDAO tableDao, IDataElementDAO dataElementDao,
            IAttributeDAO attributeDao, IFixedValueDAO fixedValueDao, CacheDao cacheDao) {
        this.cleanupDao = cleanupDao;
        this.tableDao = tableDao;
        this.dataElementDao = dataElementDao;
        this.attributeDao = attributeDao;
        this.fixedValueDao = fixedValueDao;
        this.cacheDao = cacheDao;
    }

    @Override
    public int deleteBrokenDatasetToTableRelations() {
        return this.cleanupDao.deleteBrokenDatasetToTableRelations();
    }

    /**
     * The function does not delete the table's elements
     */
    @Override
    public int deleteOrphanTables() {
        List<Integer> tableIds = this.tableDao.getOrphanTableIds();
        if (tableIds.isEmpty()) {
            return 0;
        }

        this.attributeDao.deleteAttributes(tableIds, CleanupDao.TABLE_PARENT_TYPE);
        this.cleanupDao.deleteDatasetToTableRelations(tableIds);
        this.cleanupDao.deleteTableRelationsWithElements(tableIds);
        this.cacheDao.deleteCacheEntries(tableIds, CacheEntry.ObjectType.TABLE);
        this.cleanupDao.deleteDocs(CleanupDao.TABLE_OWNER_TYPE, tableIds);

        return this.tableDao.delete(tableIds);
    }

    @Override
    public int deleteBrokenTableToElementRelations() {
        return this.cleanupDao.deleteBrokenTableToElementRelations();
    }

    @Override
    public int deleteOrphanNonCommonDataElements() {
        List<Integer> nonCommonElementIds = this.dataElementDao.getOrphanNonCommonDataElementIds();
        if (nonCommonElementIds.isEmpty()) {
            return 0;
        }

        this.attributeDao.deleteAttributes(nonCommonElementIds, DElemAttribute.ParentType.ELEMENT.toString());
        this.cleanupDao.deleteElementRelationsWithTables(nonCommonElementIds);
        this.cacheDao.deleteCacheEntries(nonCommonElementIds, CacheEntry.ObjectType.ELEMENT);
        this.cleanupDao.deleteDocs(CleanupDao.DATA_ELEMENT_OWNER_TYPE, nonCommonElementIds);
        this.cleanupDao.deleteForeignKeyRelations(nonCommonElementIds);
        this.fixedValueDao.delete(FixedValue.OwnerType.DATA_ELEMENT, nonCommonElementIds);
        this.cleanupDao.deleteInferenceRules(nonCommonElementIds);
        
        return this.dataElementDao.delete(nonCommonElementIds);
    }

    @Override
    public int deleteOrphanNamespaces() {
        return this.cleanupDao.deleteOrphanNamespaces();
    }

    @Override
    public int deleteOrphanAcls() {
        return this.cleanupDao.deleteOrphanAcls();
    }

}
