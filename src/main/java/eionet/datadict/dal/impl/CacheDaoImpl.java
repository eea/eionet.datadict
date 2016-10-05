package eionet.datadict.dal.impl;

import eionet.datadict.dal.CacheDao;
import eionet.datadict.model.CacheEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CacheDaoImpl extends JdbcDaoBase implements CacheDao {

    @Autowired
    public CacheDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public CacheEntry getCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType) {
        String sql = "select * from CACHE where OBJ_ID = :objectId and OBJ_TYPE = :objectType and ARTICLE = :articleType";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectId", objectId);
        params.put("objectType", objectType.getKey());
        params.put("articleType", articleType.getKey());

        List<CacheEntry> result = getNamedParameterJdbcTemplate().query(sql, params, new CacheEntryRowMapper());
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<CacheEntry> getCacheEntriesForObjectType(int objectId, CacheEntry.ObjectType objectType) {
        String sql = "select * from CACHE where OBJ_ID = :objectId and OBJ_TYPE = :objectType";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectId", objectId);
        params.put("objectType", objectType.getKey());

        return getNamedParameterJdbcTemplate().query(sql, params, new CacheEntryRowMapper());
    }

    @Override
    public void deleteCacheEntry(int objectId, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType) {
        String sql = "delete from CACHE where OBJ_ID = :objectId and OBJ_TYPE = :objectType and ARTICLE = :articleType";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectId", objectId);
        params.put("objectType", objectType.getKey());
        params.put("articleType", articleType.getKey());

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int deleteCacheEntries(List<Integer> objectIds, CacheEntry.ObjectType objectType) {
        String sql = "delete from CACHE where OBJ_TYPE = :objectType and OBJ_ID in (:objectIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectType", objectType.getKey());
        params.put("objectIds", objectIds);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void saveCacheEntry(CacheEntry cacheEntry) {
        String sql = "insert into CACHE (OBJ_ID, OBJ_TYPE, ARTICLE, FILENAME, CREATED) " +
                "values (:objectId, :objectType, :articleType, :fileName, :createdAt)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectId", cacheEntry.getObjectId());
        params.put("objectType", cacheEntry.getObjectType().getKey());
        params.put("articleType", cacheEntry.getArticleType().getKey());
        params.put("fileName", cacheEntry.getFileName());
        params.put("fileName", cacheEntry.getCreatedAt());

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    private static class CacheEntryRowMapper implements RowMapper<CacheEntry> {

        @Override
        public CacheEntry mapRow(ResultSet rs, int i) throws SQLException {
            CacheEntry ce = new CacheEntry();
            ce.setObjectId(rs.getInt("OBJ_ID"));
            ce.setObjectType(CacheEntry.ObjectType.getInstance(rs.getString("OBJ_TYPE")));
            ce.setArticleType(CacheEntry.ArticleType.getInstance(rs.getString("ARTICLE")));
            ce.setFileName(rs.getString("FILENAME"));
            ce.setCreatedAt(rs.getLong("CREATED"));
            return ce;
        }

    }

}
