package eionet.meta.dao;

import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularySet;
import eionet.meta.service.DBUnitHelper;
import eionet.util.Triple;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import static org.hamcrest.CoreMatchers.nullValue;

/**
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
@SpringApplicationContext("mock-spring-context.xml")
public class VocabularyFolderDAOTestIT extends UnitilsJUnit4 {
        
    @SpringBeanByType
    private IVocabularyFolderDAO vocabularyFolderDAO;

    
    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-vocabulary-relationships.xml");
    }
    
    @Test
    public void testGetVocabulariesRelation(){
        List<Triple<Integer,Integer,Integer>> triples = vocabularyFolderDAO.getVocabulariesRelation(1);
        
        Assert.assertThat("There are 3 relationships between Vocabulary ID:1 and other", triples.size(), CoreMatchers.is(3) );
        
        Assert.assertThat("1st triple: Vocabulary 1", triples.get(0).getLeft(), CoreMatchers.is(1));
        Assert.assertThat("1st triple: via data element with ID 6 is related to", triples.get(0).getCentral(), CoreMatchers.is(6));
        Assert.assertThat("1st triple: To Vocabulary 2", triples.get(0).getRight(), CoreMatchers.is(2));
        
        Assert.assertThat("1st triple: Vocabulary 1", triples.get(1).getLeft(), CoreMatchers.is(1));
        Assert.assertThat("1st triple: via data element with ID 5 is related to", triples.get(1).getCentral(), CoreMatchers.is(5));
        Assert.assertThat("1st triple: To Vocabulary 3", triples.get(1).getRight(), CoreMatchers.is(3));
        
        Assert.assertThat("2nd triple: Vocabulary 1", triples.get(2).getLeft(), CoreMatchers.is(1));
        Assert.assertThat("2nd triple: via data element with ID 5 is related to", triples.get(2).getCentral(), CoreMatchers.is(5));
        Assert.assertThat("2nd triple: To Vocabulary 2", triples.get(2).getRight(), CoreMatchers.is(2));
    }
    
    @Test
    public void testGetRelatedVocabularyConcepts(){
        List<Integer> concepts = vocabularyFolderDAO.getRelatedVocabularyConcepts(2, 5, 3);
        
        Assert.assertThat("There are 2 related concepts between Concept 2 and Data Element 5 and Vocabulary 3", concepts.size(), CoreMatchers.is(2) );
        
        Assert.assertThat("1st concept is with ID 8",  concepts.get(0), CoreMatchers.is(8) );
        Assert.assertThat("2nd concept is with ID 10", concepts.get(1), CoreMatchers.is(10) );
    }

    @Test
    public void testUpdateDateAndUserModifiedNullUsername(){
        Date testDate = new Date();
        Integer vocabularyId = 1;
        vocabularyFolderDAO.updateDateAndUserModified(testDate, null, vocabularyId);

        VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyId);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String expectedTime = formatter.format(testDate);
        String actualTime = formatter.format(vf.getDateModified());
        Assert.assertThat(actualTime, CoreMatchers.is(expectedTime) );
        Assert.assertThat(vf.getUserModified(), CoreMatchers.is("initialUser") );
    }

    @Test
    public void testUpdateDateAndUserModified(){
        Date testDate = new Date();
        String username = "test_user";
        Integer vocabularyId = 1;
        vocabularyFolderDAO.updateDateAndUserModified(testDate, username, vocabularyId);

        VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyId);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String expectedTime = formatter.format(testDate);
        String actualTime = formatter.format(vf.getDateModified());
        Assert.assertThat(actualTime, CoreMatchers.is(expectedTime) );
        Assert.assertThat(vf.getUserModified(), CoreMatchers.is(username) );
    }
}
