package eionet.datadict.web.asynctasks;

import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyService;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class VocabularyUndoCheckOutTaskTestIT {

    @Mock
    private IVocabularyService vocabularyService;
    
    @Spy
    @InjectMocks
    private VocabularyUndoCheckOutTask task;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testComposeResultUrl() {
        final String vocabularySetIdentifier = "set";
        final String vocabularyIdentifier = "vocab";
        final Map<String, Object> result = new HashMap<String, Object>();
        result.put(VocabularyUndoCheckOutTask.RESULT_VOCABULARY_SET_IDENTIFIER, vocabularySetIdentifier);
        result.put(VocabularyUndoCheckOutTask.RESULT_VOCABULARY_IDENTIFIER, vocabularyIdentifier);
        
        String resultUrl = this.task.composeResultUrl(null, result);
        
        assertThat(resultUrl, is(equalTo(String.format("/vocabulary/%s/%s", vocabularySetIdentifier, vocabularyIdentifier))));
    }
    
    @Test
    public void testCall() throws Exception {
        final int originalVocabularyId = 2;
        final int vocabularyId = 7;
        final String userName = "someuser";
        final VocabularyFolder originalVocabulary = new VocabularyFolder();
        originalVocabulary.setId(originalVocabularyId);
        originalVocabulary.setIdentifier("orig_identigier");
        originalVocabulary.setFolderName("orig_folder_name");
        final Map<String, Object> parameters = VocabularyUndoCheckOutTask.createParamsBundle(vocabularyId, userName);
        this.task.setUp(parameters);
        when(this.vocabularyService.undoCheckOut(vocabularyId, userName)).thenReturn(originalVocabularyId);
        when(this.vocabularyService.getVocabularyFolder(originalVocabularyId)).thenReturn(originalVocabulary);
        
        Object result = this.task.call();
        
        assertThat(result, is(instanceOf(Map.class)));
        Map<String, Object> mapResult = (Map) result;
        assertThat(mapResult.get(VocabularyUndoCheckOutTask.RESULT_VOCABULARY_SET_IDENTIFIER), is(equalTo((Object) originalVocabulary.getFolderName())));
        assertThat(mapResult.get(VocabularyUndoCheckOutTask.RESULT_VOCABULARY_IDENTIFIER), is(equalTo((Object) originalVocabulary.getIdentifier())));
        verify(this.task, times(1)).getVocabularyId();
        verify(this.task, times(1)).getUserName();
        verify(this.vocabularyService, times(1)).undoCheckOut(vocabularyId, userName);
        verify(this.vocabularyService, times(1)).getVocabularyFolder(originalVocabularyId);
    }
    
}
