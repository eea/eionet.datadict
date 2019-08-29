package eionet.meta.dao;

import eionet.meta.ActionBeanUtils;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.DBUnitHelper;
import eionet.meta.service.IVocabularyService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import java.util.List;

/**
 * DAOUtils test.
 */
@SpringApplicationContext("mock-spring-context.xml")
public class DAOUtilsTestIT extends UnitilsJUnit4 {

private static  final  String SEEDFILE = "seed-daoutils.xml";

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @BeforeClass
    public static void loadData() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.loadData(SEEDFILE);
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData(SEEDFILE);
    }

    @Test
    public void testGetVocabularyDefinition() throws  Exception {
        VocabularyFolder vocabulary = vocabularyService.getVocabularyFolder("wise", "BWClosed", false);

        String definition = DAOUtils.getVocabularyAttributeByName(vocabulary, "definition");

        Assert.assertNotNull(definition);
    }

    @Test
    public  void  testIsVocabularyValid() throws  Exception {
        VocabularyFolder vocabulary ;
        List<VocabularyConcept> concepts;


        vocabulary = vocabularyService.getVocabularyWithConcepts("BWType", "wise");
        concepts = vocabulary.getConcepts();
        vocabulary.setConcepts(concepts);

        Assert.assertTrue(DAOUtils.anyConceptValid(vocabulary));

        vocabulary = vocabularyService.getVocabularyWithConcepts("BWClosed", "wise");
        concepts = vocabulary.getConcepts();
        vocabulary.setConcepts(concepts);

        Assert.assertTrue(!DAOUtils.anyConceptValid(vocabulary));
    }
}
