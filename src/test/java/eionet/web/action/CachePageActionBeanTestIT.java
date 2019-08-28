package eionet.web.action;

import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.datadict.model.CacheEntry;
import eionet.datadict.services.data.CacheService;
import eionet.util.SecurityUtil;
import eionet.web.action.CachePageActionBean.CacheTypeConfig;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.uiservices.ErrorPageService;
import eionet.web.action.uiservices.impl.ErrorPageServiceImpl;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class CachePageActionBeanTestIT {

    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final CacheService cacheService;
        private final ErrorPageService errorPageService;

        public DependencyInjector(CacheService cacheService, ErrorPageService errorPageService) {
            this.cacheService = cacheService;
            this.errorPageService = errorPageService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof CachePageActionBean;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            CachePageActionBean actionBean = (CachePageActionBean) bean;
            actionBean.setCacheService(cacheService);
            actionBean.setErrorPageService(errorPageService);
        }
    }

    @Spy
    private ErrorPageServiceImpl errorPageService;

    @Mock
    private CacheService cacheService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = new CachePageActionBeanTestIT.DependencyInjector(cacheService, errorPageService);
    }

    @After
    public void tearDown() {
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
    }

    @Test
    public void testFailToViewBecauseOfNonAuthenticatedUser() throws Exception {
        testFailBecauseOfNonAuthenticatedUser("view");
    }

    @Test
    public void testFailToUpdateBecauseOfNonAuthenticatedUser() throws Exception {
        testFailBecauseOfNonAuthenticatedUser("update");
    }

    @Test
    public void testFailToDeleteBecauseOfNonAuthenticatedUser() throws Exception {
        testFailBecauseOfNonAuthenticatedUser("delete");
    }

    private void testFailBecauseOfNonAuthenticatedUser(String eventName) throws Exception {
        MockRoundtrip trip = this.createRoundtrip();
        trip.execute(eventName);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }

    @Test
    public void testFailToViewBecauseOfInvalidObjectTypeKey() throws Exception {
        testFailBecauseOfInvalidObjectTypeKey("view");
    }

    @Test
    public void testFailToUpdateBecauseOfInvalidObjectTypeKey() throws Exception {
        testFailBecauseOfInvalidObjectTypeKey("update");
    }

    @Test
    public void testFailToDeleteBecauseOfInvalidObjectTypeKey() throws Exception {
        testFailBecauseOfInvalidObjectTypeKey("delete");
    }

    private void testFailBecauseOfInvalidObjectTypeKey(String eventName) throws Exception {
        MockRoundtrip trip = this.prepareRoundTrip("1", "foo");
        trip.execute(eventName);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }

    @Test
    public void testFailToViewBecauseOfMalformedObjectId() throws Exception {
        testFailBecauseOfMalformedObjectId("view");
    }

    @Test
    public void testFailToUpdateBecauseOfMalformedObjectId() throws Exception {
        testFailBecauseOfMalformedObjectId("update");
    }

    @Test
    public void testFailToDeleteBecauseOfMalformedObjectId() throws Exception {
        testFailBecauseOfMalformedObjectId("delete");
    }

    private void testFailBecauseOfMalformedObjectId(String eventName) throws Exception {
        MockRoundtrip trip = this.prepareRoundTrip("foo", "dst");
        trip.execute(eventName);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.INVALID_INPUT), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }

    @Test
    public void testFailToViewBecauseOfNonExistingCachebleObject() throws Exception {
        testFailBecauseOfNonExistingCachebleObject("view");
    }

    @Test
    public void testFailToUpdateBecauseOfNonExistingCachebleObject() throws Exception {
        testFailBecauseOfNonExistingCachebleObject("update");
    }

    @Test
    public void testFailToDeleteBecauseOfNonExistingCachebleObject() throws Exception {
        testFailBecauseOfNonExistingCachebleObject("delete");
    }

    private void testFailBecauseOfNonExistingCachebleObject(String eventName) throws Exception {
        when(cacheService.getCachableObjectIdentifier(anyInt(), any(CacheEntry.ObjectType.class))).thenReturn(null);
        MockRoundtrip trip = this.prepareRoundTrip("100", "dst");
        trip.execute(eventName);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("/error.action"));
    }

    @Test
    public void testUpdateWhenNoArticleTypeKeys() throws Exception {
        testWhenNoArticleTypeKeys("update");
    }

    @Test
    public void testDeleteWhenNoArticleTypeKeys() throws Exception {
        testWhenNoArticleTypeKeys("delete");
    }

    private void testWhenNoArticleTypeKeys(String eventName) throws Exception {
        final String objectId = "100";
        final String objectTypeKey = "dst";
        final String identifier = "foo";
        
        when(cacheService.getCachableObjectIdentifier(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.DATASET))).thenReturn(identifier);
        MockRoundtrip trip = this.prepareRoundTrip(objectId, objectTypeKey);
        trip.execute(eventName);
        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("cache"));
        assertTrue(trip.getRedirectUrl().contains("objectId=100"));
        assertTrue(trip.getRedirectUrl().contains("objectTypeKey=dst"));
    }

    @Test
    public void testUpdateWhenInvalidArticleTypeKeys() throws Exception {
        testWhenInvalidArticleTypeKeys("update");
    }

    @Test
    public void testDeleteWhenInvalidArticleTypeKeys() throws Exception {
        testWhenInvalidArticleTypeKeys("delete");
    }

    private void testWhenInvalidArticleTypeKeys(String eventName) throws Exception {
        final String objectId = "100";
        final String objectTypeKey = "dst";
        final String identifier = "foo";
        final Set<String> articleTypeKeys = new HashSet<String>();
        articleTypeKeys.add("invalidKey1");
        articleTypeKeys.add("invalidKey2");
        
        when(cacheService.getCachableObjectIdentifier(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.DATASET))).thenReturn(identifier);
        MockRoundtrip trip = this.prepareRoundTrip(objectId, objectTypeKey, articleTypeKeys);
        trip.execute(eventName);
        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        assertTrue(trip.getRedirectUrl().contains("cache"));
        assertTrue(trip.getRedirectUrl().contains("objectId=100"));
        assertTrue(trip.getRedirectUrl().contains("objectTypeKey=dst"));
    }

    @Test 
    public  void testView() throws Exception {
        final CacheTypeConfig cacheTypeConfig = CacheTypeConfig.DATASET;
        final String objectId = "100";
        final String objectTypeKey = "dst";
        final String identifier = "foo";

        List<CacheEntry> cacheEntries = new ArrayList<CacheEntry>();
        for (CacheEntry.ArticleType articleType : cacheTypeConfig.getArticleTypes()) {
            cacheEntries.add(createCacheEntry(Integer.parseInt(objectId), cacheTypeConfig.getObjectType(), articleType, "file." + articleType.getKey(), System.currentTimeMillis()));
        }

        when(cacheService.getCachableObjectIdentifier(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.DATASET))).thenReturn(identifier);
        when(cacheService.getCacheEntriesForObjectType(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.DATASET))).thenReturn(cacheEntries);
        MockRoundtrip trip = this.prepareRoundTrip(objectId, objectTypeKey);
        trip.execute("view");
        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        assertEquals(2, cacheEntries.size());
        assertEquals(CachePageActionBean.CACHE_PAGE, trip.getForwardUrl());
    }

    @Test
    public  void testDeleteInvalidCacheEntriesAndView() throws Exception {
        final CacheTypeConfig cacheTypeConfig = CacheTypeConfig.TABLE;
        final String objectId = "100";
        final String objectTypeKey = "tbl";
        final String identifier = "foo";

        final List<CacheEntry> cacheEntries = new ArrayList<CacheEntry>();
        cacheEntries.add(createCacheEntry(Integer.parseInt(objectId), 
                cacheTypeConfig.getObjectType(), CacheEntry.ArticleType.PDF, "test.pdf", System.currentTimeMillis())); // invalid article type
        cacheEntries.add(createCacheEntry(Integer.parseInt(objectId), 
                cacheTypeConfig.getObjectType(), CacheEntry.ArticleType.XLS, null, System.currentTimeMillis())); // no file name
        cacheEntries.add(createCacheEntry(Integer.parseInt(objectId), 
                cacheTypeConfig.getObjectType(), CacheEntry.ArticleType.XLS, null, null)); // no creation date

        when(cacheService.getCachableObjectIdentifier(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.TABLE))).thenReturn(identifier);
        when(cacheService.getCacheEntriesForObjectType(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.TABLE))).thenReturn(cacheEntries);

        MockRoundtrip trip = this.prepareRoundTrip(objectId, objectTypeKey);
        trip.execute("view");
        
        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        verify(cacheService, times(1)).deleteCacheEntry(Integer.parseInt(objectId), CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.PDF);
        verify(cacheService, times(2)).deleteCacheEntry(Integer.parseInt(objectId), CacheEntry.ObjectType.TABLE, CacheEntry.ArticleType.XLS);
        assertEquals(1, cacheEntries.size());
        assertEquals(cacheEntries.get(0).getObjectType(), CacheEntry.ObjectType.TABLE);
        assertEquals(cacheEntries.get(0).getArticleType(),CacheEntry.ArticleType.XLS);
        assertEquals(CachePageActionBean.CACHE_PAGE, trip.getForwardUrl());
    }

    @Test
    public void testDelete() throws Exception {
        final String objectId = "100";
        final String objectTypeKey = "dst";
        final String identifier = "foo";
        final Set<String> articleTypeKeys = new HashSet<String>();
        final EnumSet<CacheEntry.ArticleType> articleTypes = EnumSet.allOf(CacheEntry.ArticleType.class);

        for (CacheEntry.ArticleType articleType : articleTypes) {
            articleTypeKeys.add(articleType.getKey());
            when(cacheService.getCacheEntry(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.DATASET), eq(articleType))).
                    thenReturn(createCacheEntry(Integer.parseInt(objectId), CacheEntry.ObjectType.DATASET, articleType, "file." + articleType.getKey(), System.currentTimeMillis()));
        }

        when(cacheService.getCachableObjectIdentifier(eq(Integer.parseInt(objectId)), eq(CacheEntry.ObjectType.DATASET))).thenReturn(identifier);

        MockRoundtrip trip = this.prepareRoundTrip(objectId, objectTypeKey, articleTypeKeys);
        trip.execute("delete");

        verify(errorPageService, times(0)).createErrorResolution(any(ErrorActionBean.ErrorType.class), any(String.class));
        for (CacheEntry.ArticleType articleType : articleTypes) {
            verify(cacheService, times(1)).deleteCacheEntry(Integer.parseInt(objectId), CacheEntry.ObjectType.DATASET, articleType);
            verify(cacheService, times(1)).deletePhysicalFile("file." + articleType.getKey());
        }
        assertTrue(trip.getRedirectUrl().contains("cache"));
        assertTrue(trip.getRedirectUrl().contains("objectId=100"));
        assertTrue(trip.getRedirectUrl().contains("objectTypeKey=dst"));
    }

    private MockRoundtrip prepareRoundTrip(String objectId, String objectTypeKey, Set<String> articleTypeKeys) {
        MockRoundtrip trip = this.createAuthenticatedRoundtrip();
        if (objectId != null) {
            trip.setParameter("objectId", objectId);
        }
        
        if (objectTypeKey != null) {
            trip.setParameter("objectTypeKey", objectTypeKey);
        }
        if (articleTypeKeys != null && !articleTypeKeys.isEmpty()) {
            trip.setParameter("articleTypeKeys", articleTypeKeys.toArray(new String[articleTypeKeys.size()]));
        }
        return trip;
    }

    private MockRoundtrip prepareRoundTrip(String objectId, String objectTypeKey) {
        return this.prepareRoundTrip(objectId, objectTypeKey, null);
    }

    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, CachePageActionBean.class);
        
        return trip;
    }

    private MockRoundtrip createAuthenticatedRoundtrip() {
        MockRoundtrip trip = this.createRoundtrip();
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        return trip;
    }

    private CacheEntry createCacheEntry(int id, CacheEntry.ObjectType objectType, CacheEntry.ArticleType articleType, String fileName, Long createdAt) {
        CacheEntry cacheEntry = new CacheEntry(objectType, articleType);
        cacheEntry.setObjectId(id);
        cacheEntry.setFileName(fileName);
        cacheEntry.setCreatedAt(createdAt);
        return cacheEntry;
    }

}
