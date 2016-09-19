package eionet.meta.service;

import eionet.meta.ActionBeanUtils;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.InferenceRule.RuleType;
import eionet.meta.dao.domain.VocabularyConcept;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * DataService tests.
 *
 * @author Kaido Laine
 */
@SpringApplicationContext("mock-spring-context.xml")
public class DataServiceTest extends UnitilsJUnit4 {

    /**
     * Service instance.
     */
    @SpringBeanByType
    IDataService dataService;

    /**
     * Load seed data file.
     *
     * @throws Exception
     *             if loading fails
     */
    @BeforeClass
    public static void loadData() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.loadData("seed-dataelements.xml");
        DBUnitHelper.loadData("seed-inferenceRules.xml");
    }

    /**
     * Delete helper data.
     *
     * @throws Exception
     *             if delete fails
     */
    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-dataelements.xml");
        DBUnitHelper.deleteData("seed-inferenceRules.xml");
    }

    /**
     * Test on getting common data elements.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testGetCommonElements() throws Exception {
        List<DataElement> elements = dataService.getReleasedCommonDataElements();
        assertEquals(7, elements.size());
    }

    /**
     * test set element attribute values.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testSetElementAttributes() throws Exception {
        DataElement elem1 = dataService.getDataElement(1);
        dataService.setDataElementAttributes(elem1);

        DataElement elem2 = dataService.getDataElement(2);
        dataService.setDataElementAttributes(elem2);

        DataElement elem3 = dataService.getDataElement(3);
        dataService.setDataElementAttributes(elem3);

        assertEquals(2, elem1.getElemAttributeValues().size());
        assertEquals(elem1.getName(), "Common element");

        assertEquals(2, elem2.getElemAttributeValues().size());
        assertEquals(1, elem3.getElemAttributeValues().size());
        assertEquals(elem3.getElemAttributeValues().get("Definition").get(0), "Third definition");
    }

    /**
     * tests if datasets contain only released elements.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testHasElementsReleased() throws Exception {
        List<DataElement> elems1 = dataService.getUnreleasedCommonElements(1);
        List<DataElement> elems2 = dataService.getUnreleasedCommonElements(2);

        assertEquals("Dataset1 contains an unreleased element", 1, elems1.size());
        assertEquals("Dataset2 contains released elements", 0, elems2.size());
    }

    /**
     * test method receiving elements having a vocabulary as source.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testVocabularyElems() throws Exception {
        List<Integer> p1 = new ArrayList<Integer>();
        p1.add(1);

        List<Integer> p2 = new ArrayList<Integer>();
        p2.add(1);
        p2.add(2);

        List<DataElement> elems1 = dataService.getVocabularySourceElements(p1);
        List<DataElement> elems2 = dataService.getVocabularySourceElements(p2);

        assertEquals("Vocabulary1 is source for 2 elements ", 2, elems1.size());
        assertEquals("Vocabularies 1 and 2 are source for 3 elements ", 3, elems2.size());
    }

    /**
     * Test if correct count of concepts are bound to element. Especially important is obsolete date check functionality if element
     * type is not all concepts valid
     *
     * @throws Exception
     *             if error
     */
    @Test
    public void testElementConcepts() throws Exception {
        List<VocabularyConcept> concepts1 = dataService.getElementVocabularyConcepts(301);

        // this element does not have 2 concepts one marked obsolete before releasing and the other created after
        // releasing of the element:
        List<VocabularyConcept> concepts2 = dataService.getElementVocabularyConcepts(302);

        assertEquals("Element ID=301 has to have 5 concepts in fxvs ", 5, concepts1.size());
        assertEquals("Element ID=302 has to have 3 concepts in fxvs ", 3, concepts2.size());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSwitchDataElementType1() throws Exception {
        int elemId = 2;
        String oldType = "CH2";
        String newType = "CH1";
        List<String> newTypeIrrelAttrs = Arrays.asList("MinSize", "MaxSize");
        testSwitchDataElementType(elemId, oldType, newType, newTypeIrrelAttrs);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSwitchDataElementType2() throws Exception {
        int elemId = 2;
        String oldType = "CH2";
        String newType = "CH3";
        List<String> newTypeIrrelAttrs = Arrays.asList("MinSize", "MaxSize");
        testSwitchDataElementType(elemId, oldType, newType, newTypeIrrelAttrs);
    }

    @Test
    public void testRecentlyReleasedDatasets() throws Exception {
        int limit = 2;
        List<DataSet> recentlyReleasedDatasets = this.dataService.getRecentlyReleasedDatasets(limit);
        assertEquals("Returned dataset number does not match limit", limit, recentlyReleasedDatasets.size());

        //now check if correct values return
        DataSet dataSet = recentlyReleasedDatasets.get(0);
        assertEquals("ID does not match", 1, dataSet.getId());
        assertEquals("Identifier does not match", "NiD_testW", dataSet.getIdentifier());
        assertEquals("Short name does not match", "NiD_testW_SN", dataSet.getShortName());
        assertEquals("Date does not match", 1404286800000L, dataSet.getDate());
        assertEquals("Name does not match", "Name1 for NiD_testW v2", dataSet.getName());

        dataSet = recentlyReleasedDatasets.get(1);
        assertEquals("ID does not match", 4, dataSet.getId());
        assertEquals("Identifier does not match", "NiD_testE", dataSet.getIdentifier());
        assertEquals("Short name does not match", "NiD_testE_SN", dataSet.getShortName());
        assertEquals("Date does not match", 1400186800000L, dataSet.getDate());
        assertEquals("Name does not match", "Name0 for NiD_testE v1", dataSet.getName());
    }

    /**
     *
     * @param elemId
     * @param oldType
     * @param newType
     * @param newTypeIrrelAttrs
     * @throws ServiceException
     */
    private void testSwitchDataElementType(int elemId, String oldType, String newType, List<String> newTypeIrrelAttrs)
            throws ServiceException {
        DataElement dataElement = dataService.getDataElement(elemId);
        assertNotNull("Expected data element with id = " + elemId, dataElement);
        assertEquals("Unexpected current type of data element with id = " + elemId, oldType, dataElement.getType());

        Map<String, List<String>> attrsMap = dataService.getDataElementSimpleAttributeValues(elemId);
        assertNotNull("Expected simple attribute values for data element with id = " + elemId, attrsMap);

        for (String attrShortName : newTypeIrrelAttrs) {
            assertTrue("Expected attribute: " + attrShortName, attrsMap.containsKey(attrShortName));
        }

        try {
            dataService.switchDataElemType(elemId, oldType, newType);
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e);
        }

        dataElement = dataService.getDataElement(elemId);
        assertNotNull("Expected data element with id = " + elemId, dataElement);
        assertEquals("Unexpected new type of data element with id = " + elemId, newType, dataElement.getType());

        attrsMap = dataService.getDataElementSimpleAttributeValues(elemId);
        for (String attrShortName : newTypeIrrelAttrs) {
            assertFalse("Unexpected attribute: " + attrShortName, attrsMap.containsKey(attrShortName));
        }
    }
    
    @Test
    public void testRulesExistence() throws Exception {
        
        assertTrue("Expected inference rule does not exist", dataService.ruleExists(1, RuleType.INVERSE, 3));
        assertTrue("Expected inference rule does not exist", dataService.ruleExists(1, RuleType.INVERSE, 4));
        assertTrue("Expected inference rule does not exist", dataService.ruleExists(2, RuleType.INVERSE, 4));
    }
    
    @Test
    public void testGrepElements() throws Exception {
        String searchPattern = "comm";
        
        Collection<DataElement> matchedElements = dataService.grepDataElement(searchPattern);
        
        assertEquals("Size of matched elements list does not match", 3, matchedElements.size());
        
        Set expectedNames = new HashSet(Arrays.asList("common1", "common2", "common3"));
        
        Set searchedNames = new HashSet();
        for(DataElement element : matchedElements){
            searchedNames.add(element.getShortName());
        }
        
        assertEquals("Searched elements do not match", expectedNames, searchedNames);
    }
    
}
