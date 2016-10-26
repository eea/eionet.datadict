package eionet.datadict.model;

import eionet.util.Util;
import org.apache.commons.lang.StringUtils;

public class CacheEntry {
    
    private int objectId;
    private ObjectType objectType;
    private ArticleType articleType;
    private String fileName;
    private Long createdAt;

    public CacheEntry() {}

    public CacheEntry(ObjectType objectType, ArticleType articleType) {
        this.objectType = objectType;
        this.articleType = articleType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public ArticleType getArticleType() {
        return articleType;
    }

    public void setArticleType(ArticleType articleType) {
        this.articleType = articleType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean hasCreationDate() {
        return createdAt != null && createdAt.longValue() > 0;
    }

    public String getCreationDate() {
        return hasCreationDate() ? Util.historyDate(createdAt) : "-- not in cache --";
    }

    public enum ObjectType {
        DATASET ("dst", "dataset"),
        TABLE   ("tbl", "table"),
        ELEMENT ("elm", "element");

        private final String key;
        private final String title;

        ObjectType(String key, String title) {
            this.key = key;
            this.title = title;
        }

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        public static ObjectType getInstance(String key) {
            if (StringUtils.isBlank(key)) {
                return null;
            }
            for (ObjectType objectType : ObjectType.values()) {
                if (objectType.getKey().equalsIgnoreCase(key)) {
                    return objectType;
                }
            }
            return null;
        }

    }

    public enum ArticleType {
        PDF ("pdf", "Technical specification"), 
        XLS ("xls", "MS Excel template");

        private final String key;
        private final String title;

        ArticleType(String key, String title) {
            this.key = key;
            this.title = title;
        }

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        public static ArticleType getInstance(String key) {
            if (StringUtils.isBlank(key)) {
                return null;
            }
            for (ArticleType articleType : ArticleType.values()) {
                if (articleType.getKey().equalsIgnoreCase(key)) {
                    return articleType;
                }
            }
            return null;
        }

    }

}

