package eionet.datadict.dal;

import eionet.datadict.model.SiteCode;
import eionet.meta.service.DBUnitHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
    private SiteCodeDao siteCodeDao;
        
    private void listsAreEqual(List<SiteCode> exp, List<SiteCode> act){
        assertThat(exp.size(), is(act.size()));
        for (int i=0; i < exp.size(); i++){
            assertThat(exp.get(i).getVocabularyConceptId(), is(act.get(i).getVocabularyConceptId()));
            assertThat(exp.get(i).getSiteCode(), is(act.get(i).getSiteCode()));
            assertThat(exp.get(i).getInitialSiteName(), is(act.get(i).getInitialSiteName()));
            assertThat(exp.get(i).getSiteCodeNat(), is(act.get(i).getSiteCodeNat()));
            assertThat(exp.get(i).getStatus(), is(act.get(i).getStatus()));
            assertThat(exp.get(i).getCcIso2(), is(act.get(i).getCcIso2()));
            assertThat(exp.get(i).getParentIso(), is(act.get(i).getParentIso()));
            assertThat(exp.get(i).getDateCreated(), is(act.get(i).getDateCreated()));
            assertThat(exp.get(i).getUserCreated(), is(act.get(i).getUserCreated()));
            assertThat(exp.get(i).getDateAllocated(), is(act.get(i).getDateAllocated()));
            assertThat(exp.get(i).getUserAllocated(), is(act.get(i).getUserAllocated()));
            assertThat(exp.get(i).getYearsDisappeared(), is(act.get(i).getYearsDisappeared()));
            assertThat(exp.get(i).getYearsDeleted(), is(act.get(i).getYearsDeleted()));
            assertThat(exp.get(i).getDateDeleted(), is(act.get(i).getDateDeleted())); 
        }
    }

    @Test
    public void testGetAllSiteCodesNoneExists() throws Exception {
        DBUnitHelper.loadData("seed-no-siteCodes.xml");
        List<SiteCode> act = siteCodeDao.getAllSiteCodes();
        assertThat(0, is(act.size()));
    }
    
    @Test
    public void testGetAllSiteCodesSuccessful() throws Exception {
        DBUnitHelper.loadData("seed-siteCode-vocabularyConcept-relationship.xml");
        List<SiteCode> act = siteCodeDao.getAllSiteCodes();
        
        /* Create expected list of site codes */
        List<SiteCode> exp = new ArrayList<SiteCode>();
        exp.add(new SiteCode("1", null, null, "1", "ASSIGNED", "A", "A", "2008-01-01 00:00:00.0", "user1", "2009-05-03 10:01:00.0", "user2", 0, 0, null));
        exp.add(new SiteCode("2", null, null, "2", "ASSIGNED", "A", "A", "2008-01-03 15:00:00.0", "user1", "2009-05-03 10:01:00.0", "user2", 0, 0, null));
        exp.add(new SiteCode("3", null, null, "3", "ASSIGNED", "A", "A", "2009-01-03 15:00:00.0", "user2", "2010-05-03 10:01:00.0", "user1", 0, 0, null));
        exp.add(new SiteCode("4", null, null, "4", "ASSIGNED", "A", "A", "2010-01-03 15:00:00.0", "user3", "2011-05-03 10:01:00.0", "user3", 0, 0, null));
        exp.add(new SiteCode("5", null, null, "5", "ASSIGNED", "A", "A", "2011-01-03 15:00:00.0", "user4", "2011-05-03 10:01:00.0", "user4", 0, 0, null));
        exp.add(new SiteCode("6", null, null, "6", "ASSIGNED", "A", "A", "2012-01-03 15:00:00.0", "user5", "2013-05-03 10:01:00.0", "user4", 0, 0, null));
        exp.add(new SiteCode("7", null, null, "7", "ASSIGNED", "A", "A", "2012-01-03 15:00:00.0", "user6", "2015-05-03 10:01:00.0", "user5", 0, 0, null));
        exp.add(new SiteCode("8", null, null, "8", "ASSIGNED", "A", "A", "2013-01-03 15:00:00.0", "user7", "2016-05-03 10:01:00.0", "user7", 0, 0, null));
        exp.add(new SiteCode("9", null, null, "9", "ASSIGNED", "A", "A", "2015-01-03 15:00:00.0", "user8", "2017-05-03 10:01:00.0", "user5", 0, 0, null));

        listsAreEqual(exp, act);
    }
    
    @Test
    public void testUpdateVocabularyConceptIdNoConceptsExist() throws Exception {
        DBUnitHelper.loadData("seed-no-vocabularyConcepts.xml");
        siteCodeDao.updateVocabularyConceptId();

        List<SiteCode> act = siteCodeDao.getAllSiteCodes();
        assertThat(0, is(act.size()));   
    }
    
    @Test
    public void testUpdateVocabularyConceptIdNoSiteCodesExist() throws Exception {
        DBUnitHelper.loadData("seed-no-siteCodes.xml");
        siteCodeDao.updateVocabularyConceptId();

        List<SiteCode> act = siteCodeDao.getAllSiteCodes();
        assertThat(0, is(act.size()));
    }
    
    @Test
    public void testUpdateVocabularyConceptIdSuccessful() throws Exception {
        DBUnitHelper.loadData("seed-siteCode-vocabularyConcept-relationship.xml");
        siteCodeDao.updateVocabularyConceptId();

        List<SiteCode> act = siteCodeDao.getAllSiteCodes();

        /* Create expected list of site codes */
        List<SiteCode> exp = new ArrayList<SiteCode>();
        exp.add(new SiteCode("10", null, null, "1", "ASSIGNED", "A", "A", "2008-01-01 00:00:00.0", "user1", "2009-05-03 10:01:00.0", "user2", 0, 0, null));
        exp.add(new SiteCode("11", null, null, "2", "ASSIGNED", "A", "A", "2008-01-03 15:00:00.0", "user1", "2009-05-03 10:01:00.0", "user2", 0, 0, null));
        exp.add(new SiteCode("12", null, null, "3", "ASSIGNED", "A", "A", "2009-01-03 15:00:00.0", "user2", "2010-05-03 10:01:00.0", "user1", 0, 0, null));
        exp.add(new SiteCode("13", null, null, "4", "ASSIGNED", "A", "A", "2010-01-03 15:00:00.0", "user3", "2011-05-03 10:01:00.0", "user3", 0, 0, null));
        exp.add(new SiteCode("14", null, null, "5", "ASSIGNED", "A", "A", "2011-01-03 15:00:00.0", "user4", "2011-05-03 10:01:00.0", "user4", 0, 0, null));
        exp.add(new SiteCode("15", null, null, "6", "ASSIGNED", "A", "A", "2012-01-03 15:00:00.0", "user5", "2013-05-03 10:01:00.0", "user4", 0, 0, null));
        exp.add(new SiteCode("16", null, null, "7", "ASSIGNED", "A", "A", "2012-01-03 15:00:00.0", "user6", "2015-05-03 10:01:00.0", "user5", 0, 0, null));
        exp.add(new SiteCode("17", null, null, "8", "ASSIGNED", "A", "A", "2013-01-03 15:00:00.0", "user7", "2016-05-03 10:01:00.0", "user7", 0, 0, null));
        exp.add(new SiteCode("18", null, null, "9", "ASSIGNED", "A", "A", "2015-01-03 15:00:00.0", "user8", "2017-05-03 10:01:00.0", "user5", 0, 0, null));

        listsAreEqual(exp, act);
    }

}
