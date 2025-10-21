package eionet.datadict.infrastructure.asynctasks;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.web.asynctasks.VocabularyRdfImportFromUrlTask;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyImportService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT,
            value ="classpath:seed-datasetIT.xml")
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL,
            value ="classpath:seed-datasetIT.xml")
public class AsyncTaskManagerTestIT {
    
    @Autowired
    private AsyncTaskManager asyncTaskManager;

    @Test
    @Ignore
    public void testScheduleRDFVocabularyImportFromUrlTask() throws InterruptedException{
    
        VocabularyFolder vocFolder = new VocabularyFolder();
        vocFolder.setIdentifier("tco99");
        vocFolder.setFolderName("testFolder");
        vocFolder.setFolderId(1);
        String emails="test@eionet.com";
        Integer scheduleInterval = 1;
        
        Map<String, Object> paramsBundle = VocabularyRdfImportFromUrlTask.createParamsBundle(
                vocFolder.getFolderName(), vocFolder.getIdentifier(), scheduleInterval, Enumerations.SchedulingIntervalUnit.MINUTE, 
                "www.testurl.com/testrdf.rdf", emails, Enumerations.VocabularyRdfPurgeOption.PURGE_VOCABULARY_DATA.name(),IVocabularyImportService.MissingConceptsAction.keep);
           this.asyncTaskManager.scheduleTask(VocabularyRdfImportFromUrlTask.class, paramsBundle, Enumerations.SchedulingIntervalUnit.MINUTE.getMinutes() * scheduleInterval);
     Thread.sleep(90000);
     List<AsyncTaskExecutionEntry> entries = this.asyncTaskManager.getAllScheduledTaskEntries();
     Assert.assertEquals(1, entries.size());
     
    }
}
