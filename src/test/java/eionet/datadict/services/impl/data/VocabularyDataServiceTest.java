package eionet.datadict.services.impl.data;

import eionet.datadict.dal.VocabularyRepository;
import eionet.datadict.dal.VocabularySetRepository;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.model.VocabularySet;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import org.apache.commons.lang.builder.EqualsBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class VocabularyDataServiceTest {

    @Mock
    private VocabularySetRepository vocabularySetRepository;
    @Mock
    private VocabularyRepository vocabularyRepository;
    @Mock
    private IVocabularyService legacyVocabularyService;

    private VocabularyDataService vocabularyDataService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.vocabularyDataService = new VocabularyDataServiceImpl(vocabularySetRepository, vocabularyRepository, legacyVocabularyService);
    }

    @Test
    public void testFailToCreateVocabularySetBecauseOfEmptyIdentifier() throws EmptyParameterException, DuplicateResourceException {
        VocabularySet vocSet = new VocabularySet();
        try {
            this.vocabularyDataService.createVocabularySet(vocSet);
            fail("Should throw an exception for missing identifier parameter");
        } catch (EmptyParameterException e) {
            assertEquals(e.getParamName(), "identifier");
        }

    }

    @Test
    public void testFailToCreateVocabularySetBecauseOfEmptyLabel() throws EmptyParameterException, DuplicateResourceException {
        VocabularySet vocSet = new VocabularySet();
        vocSet.setIdentifier("someIdentifier");
        try {
            this.vocabularyDataService.createVocabularySet(vocSet);
            fail("Should throw an exception for missing label parameter");
        } catch (EmptyParameterException e) {
            assertEquals(e.getParamName(), "label");
        }
    }

    @Test(expected = DuplicateResourceException.class)
    public void testFailToCreateVocabularySetBecauseOfDuplicate() throws EmptyParameterException, DuplicateResourceException {
        VocabularySet inSet = new VocabularySet();
        inSet.setIdentifier("state");
        inSet.setLabel("enemy");
        when(vocabularySetRepository.exists(inSet.getIdentifier())).thenReturn(true);
        this.vocabularyDataService.createVocabularySet(inSet);
    }
    
    @Test
    public void testCreateVocabularySet() throws EmptyParameterException, DuplicateResourceException{
        VocabularySet vocSet = new VocabularySet();
        vocSet.setId(1);
        vocSet.setIdentifier("identifier");
        vocSet.setLabel("label");
        when(vocabularySetRepository.exists(vocSet.getIdentifier())).thenReturn(false);
        this.vocabularyDataService.createVocabularySet(vocSet);
        ArgumentCaptor<VocabularySet> toInsertCaptor = ArgumentCaptor.forClass(VocabularySet.class);
         verify(vocabularySetRepository, times(1)).create(toInsertCaptor.capture());
         VocabularySet toBeInserted = toInsertCaptor.getValue();
        assertTrue(EqualsBuilder.reflectionEquals(vocSet, toBeInserted));
    }



    @Test
    public void testFailToCreateVocabularyBecauseOfEmptyVocabularySetIdentifier() throws ResourceNotFoundException, DuplicateResourceException{
       VocabularyFolder vocabulary = new VocabularyFolder();
       DDUser ddUser = new DDUser();
       try {
            this.vocabularyDataService.createVocabulary(null,vocabulary ,ddUser);
            fail("Should throw an exception for missing vocabularySetIdentifier parameter");
        } catch (EmptyParameterException e) {
            assertEquals(e.getParamName(), "vocabularySetIdentifier");
        }
    }
    
    
    
    
    @Test
    public void testFailToCreateVocabularyBecauseOfEmptyVocabularyIdentifier() throws ResourceNotFoundException, DuplicateResourceException{
    VocabularyFolder vocabulary = new VocabularyFolder();
       DDUser ddUser = new DDUser();
       try {
            this.vocabularyDataService.createVocabulary("setIdentifier",vocabulary ,ddUser);  
            fail("Should throw an exception for missing vocabularyIdentifier parameter");
        } catch (EmptyParameterException e) {
            assertEquals(e.getParamName(), "vocabularyIdentifier");
        }             
    }
    
    
    @Test
    public void testFailToCreateVocabularyBecauseOfEmptyVocabularyLabel() throws ResourceNotFoundException, DuplicateResourceException{
    VocabularyFolder vocabulary = new VocabularyFolder();
    vocabulary.setIdentifier("identifier");
       DDUser ddUser = new DDUser();
       try {
            this.vocabularyDataService.createVocabulary("setIdentifier",vocabulary ,ddUser);  
            fail("Should throw an exception for missing vocabularyLabel parameter");
        } catch (EmptyParameterException e) {
            assertEquals(e.getParamName(), "vocabularyLabel");
        } 
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToCreateVocabularyBecauseOfNullVocabularySet() throws ResourceNotFoundException, DuplicateResourceException, EmptyParameterException {
        VocabularyFolder vocabulary = new VocabularyFolder();
        String vocabularySetIdentifier = "setIdentifier";
        vocabulary.setIdentifier("identifier");
        vocabulary.setLabel("label");
        DDUser ddUser = new DDUser();
        when(vocabularySetRepository.get(vocabularySetIdentifier)).thenReturn(null);
        this.vocabularyDataService.createVocabulary(vocabularySetIdentifier, vocabulary, ddUser);
    }

    
    @Test(expected = DuplicateResourceException.class)
    public void testFailToCreateVocabularyBecauseOfDuplicate() throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException{
        VocabularyFolder vocabulary = new VocabularyFolder();
        String vocabularySetIdentifier = "setIdentifier";
        vocabulary.setIdentifier("identifier");
        vocabulary.setLabel("label");
        VocabularySet vocabularySet = new VocabularySet();
        DDUser ddUser = new DDUser();
        Integer vocabularySetId = 102;
        vocabularySet.setId(vocabularySetId);
        when(vocabularySetRepository.get(vocabularySetIdentifier)).thenReturn(vocabularySet);
        when(vocabularyRepository.exists(vocabularySetId,vocabulary.getIdentifier())).thenReturn(true);
       this.vocabularyDataService.createVocabulary(vocabularySetIdentifier, vocabulary, ddUser);
    }
    
    
    @Test
    public void testCreateVocabulary() throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException, ServiceException{
        VocabularyFolder vocabulary = new VocabularyFolder();
        String vocabularySetIdentifier = "setIdentifier";
        vocabulary.setIdentifier("identifier");
        vocabulary.setLabel("label");
        DDUser ddUser = new DDUser();
        VocabularySet vocabularySet = new VocabularySet();
        vocabularySet.setIdentifier(vocabularySetIdentifier); 
        Integer vocabularySetId = 102;
        vocabularySet.setId(vocabularySetId);
        when(vocabularySetRepository.get(vocabularySetIdentifier)).thenReturn(vocabularySet);
        when(vocabularyRepository.exists(vocabularySetId,vocabulary.getIdentifier())).thenReturn(false);
        this.vocabularyDataService.createVocabulary(vocabularySetIdentifier, vocabulary, ddUser);
                ArgumentCaptor<VocabularyFolder> toInsertVocabularyCaptor = ArgumentCaptor.forClass(VocabularyFolder.class);
                ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
                ArgumentCaptor<String> creatorCaptor = ArgumentCaptor.forClass(String.class);
         verify(legacyVocabularyService, times(1)).createVocabularyFolder(toInsertVocabularyCaptor.capture(),folderCaptor.capture(),creatorCaptor.capture());
         VocabularyFolder vocFolderToBeInserted = toInsertVocabularyCaptor.getValue();
         assertTrue(EqualsBuilder.reflectionEquals(vocabulary, vocFolderToBeInserted));
    }
}

