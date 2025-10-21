package eionet.web.action;

import eionet.datadict.model.CacheEntry;
import eionet.datadict.model.CacheEntry.ArticleType;
import eionet.datadict.services.data.CacheService;
import eionet.meta.GetPrintout;
import eionet.meta.exports.CachableIF;
import eionet.meta.exports.pdf.DstPdfGuideline;
import eionet.meta.exports.pdf.PdfHandout;
import eionet.meta.exports.xls.DstXls;
import eionet.meta.exports.xls.TblXls;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.sql.ConnectionUtil;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

@UrlBinding("/cache")
public class CachePageActionBean extends AbstractActionBean {

    public static final String CACHE_PAGE = "/pages/cache.jsp";

    @SpringBean
    private CacheService cacheService;

    private String objectId;
    private String objectTypeKey;
    private Set<String> articleTypeKeys;
    
    private int cacheEntryId;
    private String identifier;
    private EnumSet<ArticleType> articleTypes;
    private CacheTypeConfig cacheTypeConfig;
    private List<CacheEntry> cacheEntries;

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectTypeKey() {
        return objectTypeKey;
    }

    public void setObjectTypeKey(String objectTypeKey) {
        this.objectTypeKey = objectTypeKey;
    }

    public Set<String> getArticleTypeKeys() {
        return articleTypeKeys;
    }

    public void setArticleTypeKeys(Set<String> articleTypeKeys) {
        this.articleTypeKeys = articleTypeKeys;
    }

    public int getCacheEntryId() {
        return cacheEntryId;
    }

    public void setCacheEntryId(int cacheEntryId) {
        this.cacheEntryId = cacheEntryId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public EnumSet<ArticleType> getArticleTypes() {
        return articleTypes;
    }

    public void setArticleTypes(EnumSet<ArticleType> articleTypes) {
        this.articleTypes = articleTypes;
    }

    public CacheTypeConfig getCacheTypeConfig() {
        return cacheTypeConfig;
    }

    public List<CacheEntry> getCacheEntries() {
        return cacheEntries;
    }

    public void setCacheEntries(List<CacheEntry> cacheEntries) {
        this.cacheEntries = cacheEntries;
    }

    @Before(on = "view")
    private Resolution viewInterceptor() {
        return interceptorChecks(false);
    }

    @Before(on = {"update", "delete"})
    private Resolution updateDeleteInterceptor() {
        return interceptorChecks(true);
    }

    private Resolution interceptorChecks(boolean forUpdateDelete) {
        if (!isUserLoggedIn()) {
            return createErrorResolution(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "You have to login to access the cache page");
        }

        cacheTypeConfig = CacheTypeConfig.getInstance(objectTypeKey);
        if (cacheTypeConfig == null) {
            String message = String.format("Malformed cache object type: %s", this.objectTypeKey);
            return createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, message);
        }

        try {
            cacheEntryId = Integer.parseInt(this.objectId);
            identifier = this.cacheService.getCachableObjectIdentifier(this.cacheEntryId, cacheTypeConfig.getObjectType());
            if (identifier == null) {
                String message = String.format("Could not find %s with id %s", this.cacheTypeConfig.getObjectType().getTitle(), this.objectId);
                return createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, message);
            }
        } catch (NumberFormatException ex) {
            String message = String.format("Malformed %s id: %s", this.cacheTypeConfig.getObjectType().getTitle(), this.objectId);
            return createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, message);
        }
        if (forUpdateDelete) {
            if (articleTypeKeys == null || articleTypeKeys.isEmpty()) {
                addWarningMessage("Please select one or more articles");
                return redirectToView();
            }

            articleTypes = EnumSet.noneOf(ArticleType.class);
            for (String articleTypeKey : articleTypeKeys) {
                ArticleType articleType = ArticleType.getInstance(articleTypeKey);
                if (articleType != null && cacheTypeConfig.allowsArticleType(articleType)) {
                    articleTypes.add(articleType);
                }
            }
            if (articleTypes.isEmpty()) {
                addWarningMessage("Please select one or more articles");
                return redirectToView();
            }
        }
        return null;
    }

    @DefaultHandler
    public Resolution view() {
        cacheEntries = this.cacheService.getCacheEntriesForObjectType(this.cacheEntryId, cacheTypeConfig.getObjectType());

        List<CacheEntry.ArticleType> cacheEntriesArticleTypes = new ArrayList<CacheEntry.ArticleType>();
        for (Iterator<CacheEntry> it = cacheEntries.iterator(); it.hasNext();) {
            CacheEntry cacheEntry = it.next();
            boolean deleteCacheEntry = false;
            if (!cacheTypeConfig.allowsArticleType(cacheEntry.getArticleType()) || StringUtils.isBlank(cacheEntry.getFileName()) || !cacheEntry.hasCreationDate()) {
                deleteCacheEntry = true;
            }
            if (StringUtils.isNotBlank(cacheEntry.getFileName())) {
                String pathName = this.cacheService.getCachePath() + cacheEntry.getFileName();
                File file = new File(pathName);
                if (!file.exists()) {
                    deleteCacheEntry = true;
                }
            }
            if (deleteCacheEntry) {
                this.cacheService.deleteCacheEntry(cacheEntry.getObjectId(), cacheEntry.getObjectType(), cacheEntry.getArticleType());
                it.remove();
            } else {
                cacheEntriesArticleTypes.add(cacheEntry.getArticleType());
            }
        }

        for (CacheEntry.ArticleType articleType : cacheTypeConfig.getArticleTypes()) {
            if (!cacheEntriesArticleTypes.contains(articleType)) {
                cacheEntries.add(new CacheEntry(cacheTypeConfig.getObjectType(), articleType));
            }
        }

        Collections.sort(cacheEntries, new Comparator<CacheEntry>() {
                @Override
                public int compare(CacheEntry c1, CacheEntry c2) {
                    return c1.getArticleType().compareTo(c2.getArticleType());
                }
        });

        return new ForwardResolution(CACHE_PAGE);
    }

    public Resolution delete() {
        Thread.currentThread().setName("DELETE-CACHE-ENTRIES");
        ActionMethodUtils.setLogParameters(getContext());
        for (ArticleType articleType : articleTypes) {
            CacheEntry cacheEntry = this.cacheService.getCacheEntry(this.cacheEntryId, cacheTypeConfig.getObjectType(), articleType);
            if (cacheEntry != null) {
                this.cacheService.deleteCacheEntry(this.cacheEntryId, cacheTypeConfig.getObjectType(), articleType);
                this.cacheService.deletePhysicalFile(cacheEntry.getFileName());
            }
        }

        addSystemMessage(articleTypes.size() > 1 ? articleTypes.size() + " cache entries were successfully deleted" : "Cache entry was successfully deleted");
        return redirectToView();
    }

    public Resolution update() throws ServiceException {
        Thread.currentThread().setName("UPDATE-CACHE");
        ActionMethodUtils.setLogParameters(getContext());
        for (ArticleType articleType : articleTypes) {
            CachableIF cachable = null;
            try {
                if (articleType == ArticleType.PDF && cacheTypeConfig == CacheTypeConfig.DATASET) {
                    cachable = new DstPdfGuideline(ConnectionUtil.getConnection());
                    String fileStorePath = Props.getRequiredProperty(PropsIF.FILESTORE_PATH);
                    ((PdfHandout) cachable).setVisualsPath(new File(fileStorePath, "visuals").toString());
                    ((PdfHandout) cachable).setLogo(this.getContext().getServletContext().getRealPath(GetPrintout.PDF_LOGO_PATH));
                } else if (articleType == ArticleType.XLS && cacheTypeConfig == CacheTypeConfig.DATASET) {
                    cachable = new DstXls(ConnectionUtil.getConnection());
                } else if (articleType == ArticleType.XLS && cacheTypeConfig == CacheTypeConfig.TABLE) {
                    cachable = new TblXls(ConnectionUtil.getConnection());
                } else {
                    throw new Exception("Article <" + articleType + "> for object <" + cacheTypeConfig.getObjectType() + "> is not handled right now!");
                }
                cachable.setCachePath(this.cacheService.getCachePath());
                cachable.updateCache(this.objectId);
            } catch(Exception ex) {
                throw new ServiceException("Failed to update cache");
            }
        }

        addSystemMessage(articleTypes.size() > 1 ? articleTypes.size() + " cache entries were successfully updated" : "Cache entry was successfully updated");
        return redirectToView();
    }

    private Resolution redirectToView() {
        return new RedirectResolution(this.getClass()).
                addParameter("objectId", objectId).
                addParameter("objectTypeKey", objectTypeKey);
    }

    public enum CacheTypeConfig {
        DATASET (CacheEntry.ObjectType.DATASET, Arrays.asList(new CacheEntry.ArticleType[] {CacheEntry.ArticleType.PDF, CacheEntry.ArticleType.XLS}), "datasets"),
        TABLE (CacheEntry.ObjectType.TABLE, Arrays.asList(new CacheEntry.ArticleType[] {CacheEntry.ArticleType.XLS}), "tables"),
        ELEMENT (CacheEntry.ObjectType.ELEMENT, Collections.EMPTY_LIST, "dataElements");
        
        private final CacheEntry.ObjectType objectType;
        private final List<CacheEntry.ArticleType> articleTypes;
        private final String viewSection;

        CacheTypeConfig(CacheEntry.ObjectType objectType, List<CacheEntry.ArticleType> articleTypes, String viewSection) {
            this.objectType = objectType;
            this.articleTypes = articleTypes;
            this.viewSection = viewSection;
        }

        public CacheEntry.ObjectType getObjectType() {
            return objectType;
        }

        public List<CacheEntry.ArticleType> getArticleTypes() {
            return articleTypes;
        }

        public String getViewSection() {
            return viewSection;
        }

        public static CacheTypeConfig getInstance(String key) {
            if (StringUtils.isBlank(key)) {
                return null;
            }
            for (CacheTypeConfig config : CacheTypeConfig.values()) {
                if (config.getObjectType().getKey().equalsIgnoreCase(key)) {
                    return config;
                }
            }
            return null;
        }

        public boolean allowsArticleType(ArticleType articleType) {
            return articleTypes.contains(articleType);
        }

    }

}
