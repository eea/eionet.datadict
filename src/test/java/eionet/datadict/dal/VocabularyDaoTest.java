package eionet.datadict.dal;

import eionet.meta.service.DBUnitHelper;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@SpringApplicationContext("mock-spring-context.xml")
public class VocabularyDaoTest extends UnitilsJUnit4 {

    @SpringBeanByType
    private VocabularyDao vocabularyDao;

    @Before
    public void setup() throws Exception {
        DBUnitHelper.loadData("seed-vocabulary-data.xml");
    }

    @Test
    public void existsByVocabularySetIdAndVocabularyIdentifier() {
        assertTrue(this.vocabularyDao.exists(1, "test_vocabulary1"));
        assertFalse(this.vocabularyDao.exists(101, "test_vocabulary1"));
        assertFalse(this.vocabularyDao.exists(1, "test_vocabulary_not_here"));
    }

    @Test
    public void existsByVocabularySetIdentifierAndVocabularyIdentifier() {
        assertTrue(this.vocabularyDao.exists("common", "test_vocabulary2"));
        assertFalse(this.vocabularyDao.exists("common", "test_vocabulary102"));
        assertFalse(this.vocabularyDao.exists("xxx", "test_vocabulary2"));
    }

}