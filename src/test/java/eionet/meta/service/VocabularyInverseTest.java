/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Kaido Laine
 */


package eionet.meta.service;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.imp.VocabularyImportBaseHandler;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * JUnit integration test with Unitils for testing automatic inversions.
 *
 * @author Kaido Laine
 */
@SpringApplicationContext("spring-context.xml")
public class VocabularyInverseTest extends VocabularyImportServiceTestBase {

    /** Logger. */
    //protected static final Logger LOGGER = Logger.getLogger(VocabularyServiceTest.class);


    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @SpringBeanByType
    private IDataService dataService;

    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-vocabularyinverse.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-vocabularyinverse.xml");
    }
    @Test
    public void testGetInverseElem() throws Exception {

        Properties properties = new Properties();
        properties.setProperty("http://www.dbunit.org/properties/datatypeFactory", "org.dbunit.ext.mysql.MySqlDataTypeFactory");

        Connection conn =
                DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), Props.getProperty(PropsIF.DBUSR),
                        Props.getProperty(PropsIF.DBPSW));

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT GetInverseElemId(1) as X FROM dual");

        rs.next();
        int reverseId = rs.getInt("X");
        Assert.assertTrue("Should have inverse elem 2", 2 == reverseId);

        rs = st.executeQuery("SELECT GetInverseElemId(3) as X FROM dual");
        rs.next();

        Assert.assertTrue("ID 3 should have no inverse element", rs.getObject("X") == null);

        rs = st.executeQuery("SELECT GetInverseElemId(5) as X FROM dual");
        rs.next();
        reverseId = rs.getInt("X");

        Assert.assertTrue("ID 5 should have inverse element id=4", 4 == reverseId);

        rs.close();
        conn.close();
    }


    @Test
    public void testAddLocalrefElemToInverseConcept() throws Exception {

        int checkedOutID = vocabularyService.checkOutVocabularyFolder(1, "julius");

        VocabularyConcept concept1 =
                vocabularyService.getVocabularyConcept(checkedOutID, "concept1", false);

        VocabularyConcept concept2 =
                vocabularyService.getVocabularyConcept(checkedOutID, "concept2", false);

        int concept2IdAfter = concept2.getId();
        int concept1IdAfter = concept1.getId();

        DataElement skosNarrower = dataService.getDataElement(5);
        skosNarrower.setRelatedConceptId(concept2.getId());

        List<List<DataElement>> elemAttrs = new ArrayList<List<DataElement>>();
        List<DataElement> skosNarrowElems = new ArrayList<DataElement>();
        skosNarrowElems.add(skosNarrower);
        elemAttrs.add(skosNarrowElems);

        concept1.setElementAttributes(elemAttrs);

        vocabularyService.updateVocabularyConcept(concept1);
        //vocabularyService.updateVocabularyConcept(concept2);

        //for localref elems relations are created after save:
        concept2 =  vocabularyService.getVocabularyConcept(checkedOutID, "concept2", false);
        List<List<DataElement>> dataElements =  concept2.getElementAttributes();


        List<DataElement> skosBroaderElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", dataElements);

        Assert.assertEquals(1, concept2.getElementAttributes().size());
        Assert.assertEquals(1, skosBroaderElements.size());

        //skos:broader of C2 has to be C1 after C1 save
        Assert.assertEquals(Integer.valueOf(concept1IdAfter), skosBroaderElements.get(0).getRelatedConceptId());
        concept1 =  vocabularyService.getVocabularyConcept(checkedOutID, "concept1", false);

        //related IDs should stay alive after check-in as well:
        vocabularyService.checkInVocabularyFolder(checkedOutID, "julius");

        VocabularyConcept concept2CheckedIn = vocabularyService.getVocabularyConcept(1, "concept2", false);
        VocabularyConcept concept1CheckedIn = vocabularyService.getVocabularyConcept(1, "concept1", false);


        //skos:narrower of C1 has still to be C2.ID after check in
        dataElements =  concept1CheckedIn.getElementAttributes();
        List<DataElement> skosNarrowerElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", dataElements);

        Assert.assertEquals(Integer.valueOf(concept2IdAfter), skosNarrowerElements.get(0).getRelatedConceptId());

        //skos:broader of C2 has to be C1.ID after checkin
        dataElements =  concept2CheckedIn.getElementAttributes();
        skosBroaderElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", dataElements);

        Assert.assertEquals(Integer.valueOf(concept1IdAfter), skosBroaderElements.get(0).getRelatedConceptId());


        //check if skos:broader was bound (was not in seed data)
        boolean skosBroaderBound = false;
        List<DataElement> boundElems = vocabularyService.getVocabularyDataElements(1);
        for (DataElement elem : boundElems) {
            if ("skos:broader".equals(elem.getIdentifier())) {
                skosBroaderBound = true;
            }
        }
        Assert.assertTrue("skos:broader was not bound ", skosBroaderBound);

    }


    @Test
    public void testAddReferenceElemToInverseConcept() throws Exception {
        int checkedOutID = vocabularyService.checkOutVocabularyFolder(1, "julius");
        VocabularyConcept concept2 =
                vocabularyService.getVocabularyConcept(checkedOutID, "concept2", false);

        //concept gets this ID after checkin
        int concept2IdAfter = concept2.getId();

        //add narrower match in another vocabulary to concept 2:
        DataElement skosNarrowerMatch = dataService.getDataElement(2);
        skosNarrowerMatch.setRelatedConceptId(4);

        List<List<DataElement>> elemAttrs = new ArrayList<List<DataElement>>();
        List<DataElement> skosNarrowMatchElems = new ArrayList<DataElement>();
        skosNarrowMatchElems .add(skosNarrowerMatch);
        elemAttrs.add(skosNarrowMatchElems);

        concept2.setElementAttributes(elemAttrs);

        //nothing should happen in the referenced concept after save:
        vocabularyService.updateVocabularyConcept(concept2);

        VocabularyConcept referencedConcept4 = vocabularyService.getVocabularyConcept(2, "concept4", false);
        Assert.assertTrue(referencedConcept4.getElementAttributes().size() == 0);

        //nothing is bound in seed data:
        List<DataElement> boundElems = vocabularyService.getVocabularyDataElements(2);
        Assert.assertTrue("No elems should be bound to vocabulary 2", boundElems.size() == 0);

        vocabularyService.checkInVocabularyFolder(checkedOutID, "julius");
        referencedConcept4 = vocabularyService.getVocabularyConcept(2, "concept4", false);

        //now concept2 has to be in c4 attributes as skos:broaderMatch
        List<List<DataElement>> dataElements =  referencedConcept4.getElementAttributes();
        List<DataElement> skosBroaderMatchElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:broaderMatch", dataElements);

        Assert.assertEquals("cocnept4 has to have braoderMatch concept 2 new ID ",
                Integer.valueOf(concept2IdAfter), skosBroaderMatchElements.get(0).getRelatedConceptId());

    }


    /**
     * tests if after deletgin an element it is deleted from the oither side as well.
     * @throws Exception
     */
    @Test
    public void testDeleteLocalrefElemFromInverseConcept() throws Exception {

        int checkedOutID = vocabularyService.checkOutVocabularyFolder(3, "taburet");

        VocabularyConcept concept5 =
                vocabularyService.getVocabularyConcept(checkedOutID, "concept5", false);

        List<List<DataElement>> dataElements =  concept5.getElementAttributes();
        List<DataElement> skosBroaderElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:broader", dataElements);

        //remove broader relateion:
        skosBroaderElements.clear();
        vocabularyService.updateVocabularyConcept(concept5);

        //localref elems have to disappear after delete

        VocabularyConcept concept6 =
                vocabularyService.getVocabularyConcept(checkedOutID, "concept6", false);

        dataElements =  concept6.getElementAttributes();
        List<DataElement> skosNarrowElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", dataElements);

        Assert.assertNull("Referred localref must not be present before checkin", skosNarrowElements);

        //should remain after checkin
        vocabularyService.checkInVocabularyFolder(checkedOutID, "taburet");

        VocabularyConcept concept6After =
                vocabularyService.getVocabularyConcept(3, "concept6", false);

        dataElements =  concept6After.getElementAttributes();
        skosNarrowElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:narrower", dataElements);


        Assert.assertNull("Referred localref must not be present after checkin", skosNarrowElements);
    }


    /**
     * tests if after deletgin an element it is deleted from the oither side as well.
     * @throws Exception
     */
    @Test
    public void testDeleteReferenceElemFromInverseConcept() throws Exception {

        int checkedOutID = vocabularyService.checkOutVocabularyFolder(4, "taburet");

        VocabularyConcept concept7 =
                vocabularyService.getVocabularyConcept(checkedOutID, "concept7", false);

        List<List<DataElement>> dataElements =  concept7.getElementAttributes();
        List<DataElement> skosRelatedMatch =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:relatedMatch", dataElements);

        //remove relation:
        skosRelatedMatch.clear();
        vocabularyService.updateVocabularyConcept(concept7);

        //reference elems should not disappear after delete

        VocabularyConcept concept6 =
                vocabularyService.getVocabularyConcept(3, "concept6", false);

        dataElements =  concept6.getElementAttributes();
        List<DataElement> skosNarrowElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:relatedMatch", dataElements);

        Assert.assertEquals("Referred reference elem must still be present after save ", 1, skosNarrowElements.size());

        //should remain after checkin
        vocabularyService.checkInVocabularyFolder(checkedOutID, "taburet");

        VocabularyConcept concept6After =
                vocabularyService.getVocabularyConcept(3, "concept6", false);

        dataElements =  concept6After.getElementAttributes();
        skosNarrowElements =
                VocabularyImportBaseHandler.getDataElementValuesByName("skos:relatedMatch", dataElements);


        Assert.assertNull("Referred reference must not be present after checkin", skosNarrowElements);
    }


    @Override
    protected Reader getReaderFromResource(String resourceLoc) throws Exception {
        return null;
    }

}
