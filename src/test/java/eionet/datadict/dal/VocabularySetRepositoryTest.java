package eionet.datadict.dal;

import eionet.datadict.model.VocabularySet;
import eionet.meta.service.DBUnitHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
@SpringApplicationContext("spring-context.xml")
public class VocabularySetRepositoryTest extends UnitilsJUnit4 {

    @SpringBeanByType
    VocabularySetRepository vocabularySetRepository;

    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-vocabulary-data.xml");
    }

    @Test
    public void testExistsByIdentifier() {
        assertTrue(this.vocabularySetRepository.exists("common2"));
        assertFalse(this.vocabularySetRepository.exists("uncommon"));
        assertTrue(this.vocabularySetRepository.exists("test1"));
    }

    @Test
    public void testResolveByIdentifier() {
        assertNull(this.vocabularySetRepository.resolve("common1042"));
        assertEquals(Integer.valueOf(11), this.vocabularySetRepository.resolve("common2"));
    }

    @Test
    public void testGetByIdentifier() {
        assertNull(this.vocabularySetRepository.get("common1042"));
        
        VocabularySet expected = new VocabularySet();
        expected.setId(11);
        expected.setIdentifier("common2");
        expected.setLabel("Common2");
        VocabularySet actual = this.vocabularySetRepository.get(expected.getIdentifier());
        
        assertNotNull(actual);
        this.assertVocabularySet(expected, actual, false);
    }

    @Test
    public void testCreate() {
        VocabularySet vocabularySet = new VocabularySet();
        vocabularySet.setIdentifier("state");
        vocabularySet.setLabel("enemy");
        this.vocabularySetRepository.create(vocabularySet);
        VocabularySet vocSet = this.vocabularySetRepository.get(vocabularySet.getIdentifier());
        assertNotNull(vocSet);
        this.assertVocabularySet(vocabularySet, vocSet, true);
    }

    private void assertVocabularySet(VocabularySet expected, VocabularySet actual, boolean skipIdAssertion) {
        if (!skipIdAssertion) {
            assertEquals(expected.getId(), actual.getId());
        }
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.getLabel(), actual.getLabel());
    }

}
