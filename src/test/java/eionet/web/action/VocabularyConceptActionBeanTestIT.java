//package eionet.web.action;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import eionet.config.ApplicationTestContext;
//import eionet.datadict.dal.DataElementDao;
//import eionet.datadict.dal.DatasetDao;
//import eionet.datadict.dal.impl.DataElementDaoImpl;
//import eionet.datadict.dal.impl.DatasetDaoImpl;
//import eionet.datadict.model.ContactDetails;
//import eionet.datadict.services.data.ContactService;
//import eionet.datadict.services.data.impl.ContactServiceImpl;
//import eionet.meta.ActionBeanUtils;
//import eionet.meta.DDUser;
//import eionet.meta.dao.IAttributeDAO;
//import eionet.meta.dao.domain.VocabularyConcept;
//import eionet.meta.dao.domain.VocabularyFolder;
//import eionet.meta.dao.mysql.AttributeDAOImpl;
//import eionet.meta.service.DBUnitHelper;
//import eionet.meta.service.DataServiceImpl;
//import eionet.meta.service.IDataService;
//import eionet.meta.service.IVocabularyService;
//import eionet.util.SecurityUtil;
//import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
//import eionet.web.action.di.ActionBeanDependencyInjector;
//import net.sourceforge.stripes.action.ActionBean;
//import net.sourceforge.stripes.mock.MockHttpSession;
//import net.sourceforge.stripes.mock.MockRoundtrip;
//import net.sourceforge.stripes.mock.MockServletContext;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {ApplicationTestContext.class})
//public class VocabularyConceptActionBeanTestIT {
//
//    private static class DependencyInjector implements ActionBeanDependencyInjector {
//
//        private final IVocabularyService vocabularyService;
//        private final IDataService dataService;
//        private final ContactService contactService;
//        private final IAttributeDAO attributeDao;
//        private final DataElementDao dataElementDao;
//        private final DatasetDao datasetDao;
//
//        public DependencyInjector(IVocabularyService vocabularyService, IDataService dataService, ContactService contactService, IAttributeDAO attributeDao,
//                                  DataElementDao dataElementDao, DatasetDao datasetDao) {
//            this.vocabularyService = vocabularyService;
//            this.dataService = dataService;
//            this.contactService = contactService;
//            this.attributeDao = attributeDao;
//            this.dataElementDao = dataElementDao;
//            this.datasetDao = datasetDao;
//        }
//
//        @Override
//        public boolean accepts(ActionBean bean) {
//            return bean instanceof VocabularyConceptActionBean;
//        }
//
//        @Override
//        public void injectDependencies(ActionBean bean) {
//            VocabularyConceptActionBean actionBean = (VocabularyConceptActionBean) bean;
//            VocabularyFolder vocabularyFolder = new VocabularyFolder();
//            vocabularyFolder.setId(3);
//            vocabularyFolder.setFolderName("test");
//            vocabularyFolder.setIdentifier("contacts");
//            vocabularyFolder.setWorkingCopy(false);
//            actionBean.setVocabularyFolder(vocabularyFolder);
//            VocabularyConcept vocabularyConcept = new VocabularyConcept();
//            vocabularyConcept.setId(11);
//            actionBean.setVocabularyConcept(vocabularyConcept);
//            ObjectMapper mapper = new ObjectMapper();
//            Set<ContactDetails> contactDetailsSet = new HashSet<>();
//            ContactDetails contactDetails = getDataElemDetails();
//            ContactDetails contactDetails1 = getDatasetDetails();
//            contactDetailsSet.add(contactDetails);
//            contactDetailsSet.add(contactDetails1);
//            try {
//                actionBean.setContactDetailsString(mapper.writeValueAsString(contactDetailsSet));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//            actionBean.setConceptIdentifier("testUser");
//        }
//
//        private ContactDetails getDatasetDetails() {
//            ContactDetails contactDetails1 = new ContactDetails();
//            contactDetails1.setmAttributeId(61);
//            contactDetails1.setValue("11");
//            contactDetails1.setDataElemId(200);
//            contactDetails1.setParentType("Dataset");
//            contactDetails1.setDatasetIdentifier("dataset1");
//            contactDetails1.setDatasetShortName("dataset1");
//            contactDetails1.setDatasetWorkingCopy("N");
//            contactDetails1.setDatasetRegStatus("Incomplete");
//            return contactDetails1;
//        }
//
//        private ContactDetails getDataElemDetails() {
//            ContactDetails contactDetails = new ContactDetails();
//            contactDetails.setmAttributeId(62);
//            contactDetails.setValue("11");
//            contactDetails.setParentType("DataElement");
//            contactDetails.setDataElemParentNs(50);
//            contactDetails.setDataElemId(265578);
//            contactDetails.setDataElementShortName("regular1");
//            contactDetails.setDataElementIdentifier("E3");
//            contactDetails.setDataElemWorkingCopy("N");
//            contactDetails.setDataElementDatasetId(200);
//            contactDetails.setDataElemTableId(101);
//            contactDetails.setDataElemParentNs(58);
//            contactDetails.setDataElemTopNs(100);
//            contactDetails.setDataElemTableIdentifier("tabIdentifier");
//            return contactDetails;
//        }
//
//    }
//
//    private IVocabularyService vocabularyService;
//    private DataServiceImpl dataService;
//    private ContactServiceImpl contactService;
//    private AttributeDAOImpl attributeDao;
//    private DataElementDaoImpl dataElementDao;
//    private DatasetDaoImpl datasetDao;
//    private VocabularyFolder vocabularyFolder;
//
//    private MockRoundtrip createRoundtrip() {
//        MockServletContext ctx = ActionBeanUtils.getServletContext();
//        MockHttpSession mocKSession = new MockHttpSession(ctx.getContext("test"));
//        DDUser ddUser = mock(DDUser.class);
//        when(ddUser.getGroupResults()).thenReturn(null);
//        when(ddUser.getUserName()).thenReturn("heinlja");
//        when(ddUser.isAuthentic()).thenReturn(true);
//        mocKSession.setAttribute(SecurityUtil.REMOTEUSER, ddUser);
//        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyConceptActionBean.class, mocKSession);
//        return trip;
//    }
//
//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//        ActionBeanDependencyInjectionInterceptor.dependencyInjector = new DependencyInjector(vocabularyService, dataService, contactService, attributeDao, dataElementDao, datasetDao);
//        DBUnitHelper.loadData("seed-vocabulary-concept-action-bean.xml");
//    }
//
//    @AfterClass
//    public static void tearDown() throws Exception {
//        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
//        DBUnitHelper.deleteData("seed-vocabulary-concept-action-bean.xml");
//    }
//
//    @Test
//    public void deleteContactFromAllElements() throws Exception {
//        MockRoundtrip trip = createRoundtrip();
//        trip.execute("deleteContactFromAllElements");
//        VocabularyConceptActionBean actionBean = trip.getActionBean(VocabularyConceptActionBean.class);
//        assertEquals(0, actionBean.getContactDetails().size());
//    }
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
