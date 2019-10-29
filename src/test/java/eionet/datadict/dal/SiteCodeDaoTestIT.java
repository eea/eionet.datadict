package eionet.datadict.dal;

import eionet.datadict.model.SiteCode;
import eionet.meta.service.DBUnitHelper;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;


/**
 *
 * @author nta@eworx.gr
 */
@SpringApplicationContext("mock-spring-context.xml")
public class SiteCodeDaoTestIT extends UnitilsJUnit4 {

    @SpringBeanByType
    private SiteCodeDao siteCodeDAO;

    @Before
    public void setUp() throws Exception {
        //TODO must have different xmls for different test cases
        //DBUnitHelper.loadData("seed-siteCode-vocabularyConcept-relationship.xml");
    }

    @Test
    public void testGetAllSiteCodesNoneExists() {
       assertEquals(true, false);
    }
    
    @Test
    public void testGetAllSiteCodesSuccessful() {
        assertEquals(true, false);
    }
    
    @Test
    public void testUpdateVocabularyConceptIdNoConceptsExist() {
        assertEquals(true, false);
    }
    
    @Test
    public void testUpdateVocabularyConceptIdNoSiteCodesExist() {
        assertEquals(true, false);
    }
    
    @Test
    public void testUpdateVocabularyConceptIdSuccessful() {
        /*siteCodeDAO.updateVocabularyConceptId();

        List<SiteCode> siteCodes = siteCodeDAO.getAllSiteCodes();
        System.out.println("size: " + siteCodes.size());
        //System.out.println(siteCodes.get(0).getId());
        */
        assertEquals(true, false);
    }

}
