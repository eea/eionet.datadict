package eionet.datadict.web.asynctasks;

import eionet.meta.service.IVocabularyService;
import java.util.Map;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class VocabularyCheckInTaskTest {

    @Mock
    private IVocabularyService vocabularyService;
    
    @Spy
    @InjectMocks
    private VocabularyCheckInTask task;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testGetDisplayName() {
        final String vocabularySetIdentifier = "!@set$%";
        final String vocabularyIdentifier = "!@vocab#$";
        final Map<String, Object> parameters = VocabularyCheckInTask.createParamsBundle(0, null, vocabularySetIdentifier, vocabularyIdentifier);
        this.task.setUp(parameters);
        
        String displayName = this.task.getDisplayName();
        
        assertThat(displayName, containsString(vocabularySetIdentifier));
        assertThat(displayName, containsString(vocabularyIdentifier));
        verify(this.task, times(1)).getVocabularySetIdentifier();
        verify(this.task, times(1)).getVocabularyIdentifier();
    }
    
    @Test
    public void testComposeResultUrl() {
        final String vocabularySetIdentifier = "set";
        final String vocabularyIdentifier = "vocab";
        final Map<String, Object> parameters = VocabularyCheckInTask.createParamsBundle(0, null, vocabularySetIdentifier, vocabularyIdentifier);
        this.task.setUp(parameters);
        
        String resultUrl = this.task.composeResultUrl(null, null);
        
        assertThat(resultUrl, is(equalTo(String.format("/vocabulary/%s/%s/", vocabularySetIdentifier, vocabularyIdentifier))));
        verify(this.task, times(1)).getVocabularySetIdentifier();
        verify(this.task, times(1)).getVocabularyIdentifier();
    }
    
    @Test
    public void testCall() throws Exception {
        final int vocabularyId = 7;
        final String userName = "someuser";
        final Map<String, Object> parameters = VocabularyCheckInTask.createParamsBundle(vocabularyId, userName, null, null);
        this.task.setUp(parameters);
        
        this.task.call();
        
        verify(this.vocabularyService, times(1)).checkInVocabularyFolder(vocabularyId, userName);
        verify(this.task, times(1)).getVocabularyId();
        verify(this.task, times(1)).getUserName();
    }
    
}
