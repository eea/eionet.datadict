package eionet.meta.service.impl;

import eionet.meta.dao.CacheDao;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.ITableDAO;
import eionet.meta.dao.domain.CacheEntry;
import eionet.meta.service.CacheService;
import eionet.util.Props;
import eionet.util.PropsIF;
import java.io.File;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    private final CacheDao cacheDao;
    private final IDataSetDAO dataSetDao;
    private final ITableDAO tableDao;
    
    private String cachePath;

    @Autowired
    public CacheServiceImpl(CacheDao cacheDao, IDataSetDAO dataSetDao, ITableDAO tableDao) {
        this.cacheDao = cacheDao;
        this.dataSetDao = dataSetDao;
        this.tableDao = tableDao;
    }

    @PostConstruct
    public void initCachePath() {
        cachePath = Props.getProperty(PropsIF.DOC_PATH);
        if (StringUtils.isBlank(cachePath)) {
            throw new BeanInitializationException("Missing the path to cache directory.");
        }

        cachePath = cachePath.trim();
        if (!cachePath.endsWith(File.separator)) {
            cachePath = cachePath + File.separator;
        }
    }

    @Override
    public String getCachePath() {
        return cachePath;
    }

    @Override
    public CacheEntry getCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType) {
        return this.cacheDao.getCacheEntry(objectId, objectType, articleType);
    }

    @Override
    public List<CacheEntry> getCacheEntriesForObjectType(int objectId, CacheEntry.ObjectType objectType) {
        return this.cacheDao.getCacheEntriesForObjectType(objectId, objectType);
    }

    @Override
    public void deleteCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType) {
        this.cacheDao.deleteCacheEntry(objectId, objectType, articleType);
    }

    @Override
    public boolean deletePhysicalFile(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            File file = new File(cachePath + fileName);
            if (file.exists() && file.isFile()) {
                return file.delete();
            }
        }
        return false;
    }

    @Override
    public String getCachableObjectIdentifier(int objectId, CacheEntry.ObjectType objectType) {
        if (objectType == CacheEntry.ObjectType.DATASET) {
            return this.dataSetDao.getIdentifierById(objectId);
        }
        if (objectType == CacheEntry.ObjectType.TABLE) {
            return this.tableDao.getIdentifierById(objectId);
        }
        // ObjectType.ELEMENT not supported yet
        return null;
    }

}
