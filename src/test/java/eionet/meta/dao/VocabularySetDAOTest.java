package eionet.meta.dao;

import eionet.meta.dao.domain.VocabularySet;
import eionet.meta.service.DBUnitHelper;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
@SpringApplicationContext("spring-context.xml")
public class VocabularySetDAOTest extends UnitilsJUnit4 {
        
    @SpringBeanByType
    private IVocabularySetDAO vocabularySetDAO;

    
    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-vocabularies.xml");
    }
    
    @Test
    public void testGet(){
        VocabularySet set = vocabularySetDAO.get(11);
        
        Assert.assertThat( "Vocabulary Set identifier is 'common2'", set.getIdentifier(), CoreMatchers.is("common2") );
        Assert.assertThat( "Vocabulary Set lebel is 'Common2'", set.getLabel(), CoreMatchers.is("Common2") );
    }
}
