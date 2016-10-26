package eionet.datadict.services.data.impl;

import eionet.datadict.services.data.CheckoutsService;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.ISchemaDAO;
import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

public class CheckoutsServiceTest {
    
    private CheckoutsService checkoutsService;

    @Mock
    private IDataSetDAO dataSetDao;

    @Mock
    private IDataElementDAO dataElementDao;

    @Mock
    private ISchemaSetDAO schemaSetDao;

    @Mock
    private ISchemaDAO schemaDao;

    @Mock
    private IVocabularyFolderDAO vocabularyFolderDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.checkoutsService = new CheckoutsServiceImpl(dataSetDao, dataElementDao, schemaSetDao, schemaDao, vocabularyFolderDao);
    }

    @Test 
    public void testGetDataSetsWorkingCopies() {
        this.checkoutsService.getDataSetsWorkingCopies(getDummyUserName());
        verify(dataSetDao, times(1)).getWorkingCopiesOf(getDummyUserName());
    }

    @Test 
    public void testGetCommonDataElementsWorkingCopies() {
        this.checkoutsService.getCommonDataElementsWorkingCopies(getDummyUserName());
        verify(dataElementDao, times(1)).getCommonDataElementsWorkingCopiesOf(getDummyUserName());
    }

    @Test 
    public void testGetSchemaSetsWorkingCopies() {
        this.checkoutsService.getSchemaSetsWorkingCopies(getDummyUserName());
        verify(schemaSetDao, times(1)).getWorkingCopiesOf(getDummyUserName());
    }

    @Test 
    public void testGetSchemasWorkingCopies() {
        this.checkoutsService.getSchemasWorkingCopies(getDummyUserName());
        verify(schemaDao, times(1)).getWorkingCopiesOf(getDummyUserName());
    }

    @Test 
    public void testGetVocabulariesWorkingCopies() {
        this.checkoutsService.getVocabulariesWorkingCopies(getDummyUserName());
        verify(vocabularyFolderDao, times(1)).getWorkingCopies(getDummyUserName());
    }

    private String getDummyUserName() {
        return "userName";
    }

}
