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
 *        Juhan Voolaid
 */

package eionet.meta.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.data.ObsoleteStatus;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.Triple;

/**
 * JUnit integration test with Unitils for vocabulary service.
 *
 * @author Juhan Voolaid
 */
@SpringApplicationContext("spring-context.xml")
// @DataSet({"seed-vocabularies.xml"})
public class VocabularyServiceTest extends UnitilsJUnit4 {

    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(VocabularyServiceTest.class);

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-vocabularies.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-vocabularies.xml");
    }

    @Test
    public void testGetVocabularyFolderById() throws ServiceException {
        VocabularyFolder result = vocabularyService.getVocabularyFolder(1);
        assertNotNull("Expected vocabulary folder", result);
    }

    @Test
    public void testGetVocabularyFolderByIdentifier() throws ServiceException {
        VocabularyFolder result = vocabularyService.getVocabularyFolder("common", "test_vocabulary2", true);
        assertEquals("Expected id", 3, result.getId());
    }

    @Test
    public void testGetVocabularyFoldersAnonymous() throws ServiceException {
        List<VocabularyFolder> result = vocabularyService.getVocabularyFolders(null);
        assertEquals("Result size", 3, result.size());
    }

    @Test
    public void testGetVocabularyFoldersTestUser() throws ServiceException {
        List<VocabularyFolder> result = vocabularyService.getVocabularyFolders("testUser");
        assertEquals("Result size", 4, result.size());
    }

    @Test
    public void testCreateVocabularyFolder() throws ServiceException {
        VocabularyFolder vocabularyFolder = new VocabularyFolder();
        vocabularyFolder.setFolderId(1);
        vocabularyFolder.setLabel("test");
        vocabularyFolder.setIdentifier("test");
        vocabularyFolder.setType(VocabularyType.COMMON);

        int id = vocabularyService.createVocabularyFolder(vocabularyFolder, null, "testUser");
        VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
        assertNotNull("Expected vocabulary folder", result);
    }

    @Test
    public void testCreateVocabularyFolderWithNewFolder() throws ServiceException {
        VocabularyFolder vocabularyFolder = new VocabularyFolder();
        vocabularyFolder.setLabel("test");
        vocabularyFolder.setIdentifier("test");
        vocabularyFolder.setType(VocabularyType.COMMON);

        Folder newFolder = new Folder();
        newFolder.setIdentifier("new");
        newFolder.setLabel("new");

        int id = vocabularyService.createVocabularyFolder(vocabularyFolder, newFolder, "testUser");
        VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
        assertNotNull("Expected vocabulary folder", result);
    }

    @Test
    public void testSearchVocabularyConcepts() throws ServiceException {
        VocabularyConceptFilter filter = new VocabularyConceptFilter();
        filter.setVocabularyFolderId(3);

        VocabularyConceptResult result = vocabularyService.searchVocabularyConcepts(filter);
        assertEquals("Result size", 2, result.getFullListSize());
    }

    /**
     * full text identifier must work in filter.
     *
     * @throws ServiceException
     *             if bad things happen
     */
    @Test
    public void testSearchVocabularyConceptsByIdentifier() throws ServiceException {
        VocabularyConceptFilter filter = new VocabularyConceptFilter();
        filter.setText("1234");

        VocabularyConceptResult result = vocabularyService.searchVocabularyConcepts(filter);
        assertEquals("Result size", 1, result.getFullListSize());
    }

    @Test
    public void testCreateVocabularyConcept() throws ServiceException {
        VocabularyConcept concept = new VocabularyConcept();
        concept.setIdentifier("test3");
        concept.setLabel("test3");

        vocabularyService.createVocabularyConcept(3, concept);

        VocabularyConcept result = vocabularyService.getVocabularyConcept(3, "test3", true);
        assertNotNull("Expected concept", result);
    }

    @Test
    public void testupdatevocabularyconcept() throws ServiceException {
        VocabularyConcept result = vocabularyService.getVocabularyConcept(3, "concept1", true);
        result.setLabel("modified");
        vocabularyService.updateVocabularyConcept(result);
        result = vocabularyService.getVocabularyConcept(3, "concept1", true);
        assertEquals("Modified label", "modified", result.getLabel());
    }

    @Test
    public void testUpdateVocabularyFolder() throws ServiceException {
        VocabularyFolder result = vocabularyService.getVocabularyFolder(1);
        result.setLabel("modified");
        vocabularyService.updateVocabularyFolder(result, null);
        result = vocabularyService.getVocabularyFolder(1);
        assertEquals("Modified label", "modified", result.getLabel());
    }

    @Test
    public void testUpdateVocabularyFolderWithNewFolder() throws ServiceException {
        Folder newFolder = new Folder();
        newFolder.setIdentifier("new");
        newFolder.setLabel("new");

        VocabularyFolder result = vocabularyService.getVocabularyFolder(1);
        result.setLabel("modified");
        vocabularyService.updateVocabularyFolder(result, newFolder);
        result = vocabularyService.getVocabularyFolder(1);
        assertEquals("Modified label", "modified", result.getLabel());
    }

    @Test
    public void testDeleteVocabularyConcepts() throws ServiceException {
        vocabularyService.deleteVocabularyConcepts(Collections.singletonList(1));

        Exception exception = null;
        try {
            vocabularyService.getVocabularyConcept(3, "concept1", true);

            fail("Expected concept not found exception");
        } catch (ServiceException e) {
            exception = e;
        }
        assertNotNull("Expected exception", exception);
    }

    @Test
    public void testMarkConceptsObsolete() throws ServiceException {
        vocabularyService.markConceptsObsolete(Collections.singletonList(1));
        VocabularyConcept concept = vocabularyService.getVocabularyConcept(3, "concept1", true);
        assertNotNull("Obsolete date", concept.getObsolete());
    }

    @Test
    public void testUnMarkConceptsObsolete() throws ServiceException {
        vocabularyService.markConceptsObsolete(Collections.singletonList(1));
        vocabularyService.unMarkConceptsObsolete(Collections.singletonList(1));
        VocabularyConcept concept = vocabularyService.getVocabularyConcept(3, "concept1", true);
        assertNull("Obsolete date", concept.getObsolete());
    }

    @Test
    public void testDeleteVocabularyFolders() throws ServiceException {
        vocabularyService.deleteVocabularyFolders(Collections.singletonList(1), false);

        Exception exception = null;
        try {
            vocabularyService.getVocabularyFolder(1);
            fail("Expected vocabulary not found exception");
        } catch (ServiceException e) {
            exception = e;
        }
        assertNotNull("Expected exception", exception);
    }

    @Test
    public void testCheckOutVocabularyFolder() throws ServiceException {
        vocabularyService.checkOutVocabularyFolder(1, "testUser");
        VocabularyFolder result = vocabularyService.getVocabularyFolder("common", "test_vocabulary1", true);

        assertNotNull("Working copy vocabulary", result);
        assertEquals("Working user", "testUser", result.getWorkingUser());
        assertEquals("Working copy", true, result.isWorkingCopy());
        assertEquals("Checked out copy id", 1, result.getCheckedOutCopyId());
    }

    @Test
    public void testCheckInVocabularyFolder() throws ServiceException {
        vocabularyService.checkInVocabularyFolder(3, "testUser");

        Exception exception = null;
        try {
            vocabularyService.getVocabularyFolder("common", "test_vocabulary2", true);
            fail("Expected vocabulary not found exception");
        } catch (ServiceException e) {
            exception = e;
        }
        assertNotNull("Expected exception", exception);

        VocabularyFolder result = vocabularyService.getVocabularyFolder("common", "test_vocabulary2", false);

        assertNotNull("Original vocabulary", result);
        assertNull("Working user", result.getWorkingUser());
        assertEquals("Working copy", false, result.isWorkingCopy());
        assertEquals("Checked out copy id", 0, result.getCheckedOutCopyId());
    }

    @Test
    public void testCreateVocabularyFolderCopy() throws ServiceException {
        VocabularyFolder vocabularyFolder = new VocabularyFolder();
        vocabularyFolder.setType(VocabularyType.COMMON);
        vocabularyFolder.setFolderId(1);
        vocabularyFolder.setLabel("copy");
        vocabularyFolder.setIdentifier("copy");
        int id = vocabularyService.createVocabularyFolderCopy(vocabularyFolder, 1, "testUser", null);
        VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
        assertNotNull("Expected vocabulary folder", result);

        List<VocabularyConcept> concepts = vocabularyService.getVocabularyConceptsWithAttributes(id, false, ObsoleteStatus.ALL);

        assertEquals("Expected concepts size ", 4, concepts.size());
        int expectedRelatedID = 0;
        int actualRelatedId = 0;
        // find concept5 and see if this has concept4 ID as related ID
        for (VocabularyConcept c : concepts) {
            // expect concept4 to appear before concept5 in the elem list
            if (c.getIdentifier().equals("concept4")) {
                expectedRelatedID = c.getId();
            }
            if (c.getIdentifier().equals("concept5")) {
                List<List<DataElement>> elems = c.getElementAttributes();

                for (List<DataElement> elem : elems) {
                    if (elem.get(0).getIdentifier().equals("skos:broader")) {
                        actualRelatedId = elem.get(0).getRelatedConceptId();
                        break;
                    }
                }
            }

        }

        assertEquals("Related ID not copied ", actualRelatedId, expectedRelatedID);

    }

    @Test
    public void testCreateVocabularyFolderCopyWithNewFolder() throws ServiceException {
        Folder newFolder = new Folder();
        newFolder.setIdentifier("new");
        newFolder.setLabel("new");

        VocabularyFolder vocabularyFolder = new VocabularyFolder();
        vocabularyFolder.setType(VocabularyType.COMMON);
        vocabularyFolder.setLabel("copy");
        vocabularyFolder.setIdentifier("copy");
        int id = vocabularyService.createVocabularyFolderCopy(vocabularyFolder, 1, "testUser", newFolder);
        VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
        assertNotNull("Expected vocabulary folder", result);
    }

    @Test
    public void testGetVocabularyFolderVersions() throws ServiceException {
        List<VocabularyFolder> result = vocabularyService.getVocabularyFolderVersions("123", 1, "testUser");
        assertEquals("Number of other versions", 2, result.size());
    }

    @Test
    public void testUndoCheckOut() throws ServiceException {
        vocabularyService.checkOutVocabularyFolder(1, "testUser");
        VocabularyFolder workingCopy = vocabularyService.getVocabularyFolder("common", "test_vocabulary1", true);
        vocabularyService.undoCheckOut(workingCopy.getId(), "testUser");

        Exception exception = null;
        try {
            vocabularyService.getVocabularyFolder("common", "test_vocabulary1", true);
            fail("Expected vocabulary not found exception");
        } catch (ServiceException e) {
            exception = e;
        }
        assertNotNull("Expected exception", exception);
    }

    @Test
    public void testGetVocabularyWorkingCopy() throws ServiceException {
        VocabularyFolder result = vocabularyService.getVocabularyWorkingCopy(2);
        assertNotNull("Expected vocabulary folder", result);
    }

    @Test
    public void testIsUniqueFolderIdentifier() throws ServiceException {
        boolean result = vocabularyService.isUniqueVocabularyFolderIdentifier(1, "test", null);
        assertEquals("Is unique", true, result);
    }

    @Test
    public void testiIUniqueConceptIdentifier() throws ServiceException {
        boolean result = vocabularyService.isUniqueConceptIdentifier("2", 3, 2);
        assertEquals("Is unique", true, result);
    }

    @Test
    public void testGetFolders() throws ServiceException {
        List<Folder> result = vocabularyService.getFolders("testUser", 1);
        assertEquals("Folders size", 5, result.size());
        Folder folderCommon = null;
        for (Folder folder : result) {
            if ("common".equals(folder.getIdentifier())) {
                folderCommon = folder;
            }
        }
        assertEquals("Items size", 3, folderCommon.getItems().size());
    }

    @Test
    public void testGetFolder() throws ServiceException {
        Folder result = vocabularyService.getFolder(1);
        assertNotNull("Folder", result);
    }

    @Test
    public void testIsFolderEmpty() throws ServiceException {
        assertFalse("Folder empty", vocabularyService.isFolderEmpty(1));
    }

    @Test
    public void testDeleteFolder() throws ServiceException {
        vocabularyService.deleteFolder(2);
        Exception exception = null;
        try {
            vocabularyService.getFolder(2);
            fail("Expected vocabulary not found exception");
        } catch (ServiceException e) {
            exception = e;
        }
        assertNotNull("Expected exception", exception);
    }

    @Test
    public void testDeleteFolderNotEmpty() throws ServiceException {
        Exception exception = null;
        try {
            vocabularyService.deleteFolder(1);
            fail("Expected folder not empty exception");
        } catch (ServiceException e) {
            exception = e;
        }
        assertNotNull("Expected exception", exception);
    }

    @Test
    public void testUpdateFolder() throws ServiceException {
        Folder folder = vocabularyService.getFolder(2);
        folder.setIdentifier("new");
        folder.setLabel("new");
        vocabularyService.updateFolder(folder);
        folder = vocabularyService.getFolder(2);

        assertEquals("Modified identifier", "new", folder.getIdentifier());
        assertEquals("Modified label", "new", folder.getLabel());
    }

    @Test
    public void testGetFolderByIdentifier() throws ServiceException {
        Folder result = vocabularyService.getFolderByIdentifier("test1");
        assertNotNull("Folder", result);
    }

    @Test
    public void testGetFoldersSorting() throws ServiceException {
        List<Folder> result = vocabularyService.getFolders(null, null);
        assertEquals("The first folder", "xxx", result.get(0).getLabel());
    }

    /**
     * The purpose is to test the {@link IVocabularyService#getReleasedVocabularyFolders(int)} function.
     *
     * @throws ServiceException
     *             An error happens in the called service(s).
     */
    @Test
    public void testReleasedVocabularyFolders() throws ServiceException {
        List<VocabularyFolder> releasedVocabularies = vocabularyService.getReleasedVocabularyFolders(1);
        int size = releasedVocabularies == null ? 0 : releasedVocabularies.size();
        assertEquals("Expected exactly 1 released vocabulary", 1, size);
        assertEquals("Expected released vocabulary with ID=2", 2, releasedVocabularies.iterator().next().getId());
    }

    /**
     * The purpose is to test the vocabularies' "enforce concept notation equals concept identifier" functionality.
     *
     * @throws ServiceException
     *             An error happens in the called services.
     */
    @Test
    public void testNotationEqualsIdentifier() throws ServiceException {
        String userName = "testUser";

        // First lets create a vocabulary with no particular setting on the enforce-notation-equals-identifier policy.

        VocabularyFolder vocabulary = new VocabularyFolder();
        vocabulary.setFolderId(1);
        vocabulary.setLabel("TestVoc1");
        vocabulary.setIdentifier("test_voc_1");
        vocabulary.setType(VocabularyType.COMMON);
        int vocId = vocabularyService.createVocabularyFolder(vocabulary, null, userName);
        vocabulary = vocabularyService.getVocabularyFolder(vocId);
        assertNotNull("Expected a vocabulary folder", vocabulary);
        assertFalse("Expected the enforcement flag to be down", vocabulary.isNotationsEqualIdentifiers());

        // Now lets check out the freshly created vocabulary, so that we can start adding concepts to it.

        vocId = vocabularyService.checkOutVocabularyFolder(vocId, userName);
        assertTrue("Expected working copy id to be greater than the original id", vocId > vocabulary.getId());
        vocabulary = vocabularyService.getVocabularyFolder(vocId);
        assertNotNull("Expected a vocbulary working copy", vocabulary);
        assertEquals("Expected a working user", userName, vocabulary.getWorkingUser());
        assertEquals("Expected working copy flag set", true, vocabulary.isWorkingCopy());

        // Now lets add concepts to the freshly created vocabulary working copy.

        VocabularyConcept concept1 = new VocabularyConcept();
        concept1.setIdentifier("conc1");
        concept1.setLabel("Concept 1");
        concept1.setNotation("Conc_1");
        vocabularyService.createVocabularyConcept(vocId, concept1);
        concept1 = vocabularyService.getVocabularyConcept(vocId, "conc1", true);
        assertNotNull("Expected a concept", concept1);
        assertNotEquals("Expected unequal notation and identifier", concept1.getNotation(), concept1.getIdentifier());

        VocabularyConcept concept2 = new VocabularyConcept();
        concept2.setIdentifier("conc2");
        concept2.setLabel("Concept 2");
        concept2.setNotation("Conc_2");
        vocabularyService.createVocabularyConcept(vocId, concept2);
        concept2 = vocabularyService.getVocabularyConcept(vocId, "conc2", true);
        assertNotNull("Expected a concept", concept2);
        assertNotEquals("Expected unequal notation and identifier", concept2.getNotation(), concept2.getIdentifier());

        // Now lets enforce the notation=identifier rule on the vocabulary working copy.

        vocabulary.setNotationsEqualIdentifiers(true);
        vocabularyService.updateVocabularyFolder(vocabulary, null);
        vocabulary = vocabularyService.getVocabularyFolder(vocabulary.getId());
        assertNotNull("Expected an updated vocbulary", vocabulary);
        assertTrue("Expected the enforcement flag to be up", vocabulary.isNotationsEqualIdentifiers());

        // Check that both concept notations have now been forcefully made equal to the identifiers.

        concept1 = vocabularyService.getVocabularyConcept(vocId, "conc1", true);
        assertEquals("Expected equal notation and identifier", concept1.getNotation(), concept1.getIdentifier());
        concept2 = vocabularyService.getVocabularyConcept(vocId, "conc2", true);
        assertEquals("Expected equal notation and identifier", concept2.getNotation(), concept2.getIdentifier());

        // Add one more concept, and check that its notation now gets forcefully overwritten with identifier.

        VocabularyConcept concept3 = new VocabularyConcept();
        concept3.setIdentifier("conc3");
        concept3.setLabel("Concept 3");
        concept3.setNotation("Conc_3");
        vocabularyService.createVocabularyConcept(vocId, concept3);
        concept3 = vocabularyService.getVocabularyConcept(vocId, "conc3", true);
        assertNotNull("Expected a concept", concept3);
        assertEquals("Expected equal notation and identifier", concept3.getNotation(), concept3.getIdentifier());
    }

    /**
     * tests vocabularyHasDataElementBinding() method.
     *
     * @throws ServiceException
     *             if bad things happen
     */
    @Test
    public void vocabularyBindingExistsTest() throws ServiceException {
        assertTrue(vocabularyService.vocabularyHasDataElementBinding(1, 1));
        assertTrue(!vocabularyService.vocabularyHasDataElementBinding(1, 2));
    }

    /**
     * test on getConceptsWithElementValue method.
     *
     * @throws ServiceException
     *             if bad things happen
     */
    @Test
    public void getValuedConceptsTest() throws ServiceException {
        assertTrue(vocabularyService.getConceptsWithElementValue(1, 1).size() == 1);
        assertTrue(vocabularyService.getConceptsWithElementValue(5, 1).size() == 1);
        assertTrue(vocabularyService.getConceptsWithElementValue(1, 2).size() == 0);
        assertTrue(vocabularyService.getConceptsWithElementValue(2, 1).size() == 0);
    }

    /**
     * test if namespaces of elements are generated correctly.
     *
     * @throws ServiceException
     *             if error happens
     */
    @Test
    public void getVocabularyNamespacesTest() throws ServiceException {
        VocabularyFolder vocabulary = vocabularyService.getVocabularyFolder(1);
        List<VocabularyFolder> vocabularyFolders = new ArrayList<VocabularyFolder>();
        vocabularyFolders.add(vocabulary);

        List<RdfNamespace> nss = vocabularyService.getVocabularyNamespaces(vocabularyFolders);
        assertTrue(nss.size() == 2);
    }

    /**
     * tsest on relational elements.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testRelationalElement() throws Exception {
        assertTrue(vocabularyService.isReferenceElement(6));
        assertTrue(!vocabularyService.isReferenceElement(1));
    }

    /**
     * tests getvocabularyFolder meta.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testFolderCSVInfo() throws Exception {
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(1);
        List<Triple<String, String, Integer>> attributeNames = vocabularyService.getVocabularyBoundElementNames(vocabularyFolder);

        assertTrue(attributeNames.size() == 2);
        Triple<String, String, Integer> dev = attributeNames.get(0);
        assertTrue(dev.getLeft().equals("HCO1"));
        assertTrue(dev.getRight() == 2);
        dev = attributeNames.get(1);
        assertTrue(dev.getLeft().equals("skos:broader"));
        assertTrue(dev.getRight() == 1);
    }// end of test step testFolderCSVInfo

    /**
     * tests getvocabularyFolder meta.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testFolderCSVInfo2() throws Exception {
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(4);
        List<Triple<String, String, Integer>> attributeNames = vocabularyService.getVocabularyBoundElementNames(vocabularyFolder);

        int numberOfElements = 9;
        assertTrue(attributeNames.size() == numberOfElements);
        ArrayList<Triple<String, String, Integer>> manualAttributeNames = new ArrayList<Triple<String, String, Integer>>();
        manualAttributeNames.add(new Triple<String, String, Integer>("HCO2", null, 2));
        manualAttributeNames.add(new Triple<String, String, Integer>("HCO3", null, 1));
        manualAttributeNames.add(new Triple<String, String, Integer>("skos:definition", "de", 2));
        manualAttributeNames.add(new Triple<String, String, Integer>("skos:definition", "en", 1));
        manualAttributeNames.add(new Triple<String, String, Integer>("skos:definition", "pl", 1));
        manualAttributeNames.add(new Triple<String, String, Integer>("skos:prefLabel", "bg", 2));
        manualAttributeNames.add(new Triple<String, String, Integer>("skos:prefLabel", "en", 1));
        manualAttributeNames.add(new Triple<String, String, Integer>("skos:prefLabel", "et", 1));
        manualAttributeNames.add(new Triple<String, String, Integer>("skos:prefLabel", "pl", 1));

        for (int i = 0; i < numberOfElements; i++) {
            Triple<String, String, Integer> attributeName = attributeNames.get(i);
            Triple<String, String, Integer> manualAttributeName = manualAttributeNames.get(i);
            assertTrue(StringUtils.equals(attributeName.getLeft(), manualAttributeName.getLeft()));
            assertTrue(StringUtils.equals(attributeName.getCentral(), manualAttributeName.getCentral()));
            assertTrue(attributeName.getRight() == manualAttributeName.getRight());
        }

    }// end of test step testFolderCSVInfo

    /**
     * test on search vocabularies.
     *
     * @throws Exception
     *             if fail
     */
    @Test
    public void testSearchVocabularies() throws Exception {
        VocabularyFilter filter = new VocabularyFilter();
        // search in identifier
        filter.setText("test_vocabulary");
        VocabularyResult result = vocabularyService.searchVocabularies(filter);

        assertTrue(result.getTotalItems() == 3);
        filter.setText("nothinglikethis");

        result = vocabularyService.searchVocabularies(filter);

        assertTrue(result.getTotalItems() == 0);

        // label
        filter.setText("test2");

        result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 2);

        filter.setWorkingCopy(true);
        filter.setText(null);

        result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 1);

        // related concepts search
        filter.setWorkingCopy(false);
        filter.setText(null);
        filter.setConceptText("XYZ1234");

        result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 1);

        filter.setWorkingCopy(null);
        filter.setConceptText(null);
        filter.setStatus(RegStatus.DRAFT);

        result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 1);
    }

    @Test
    public void testSearchVocabularyConceptsExact() throws ServiceException {
        VocabularyConceptFilter filter = new VocabularyConceptFilter();
        filter.setText("cept4");
        filter.setExactMatch(true);

        VocabularyConceptResult result = vocabularyService.searchVocabularyConcepts(filter);
        assertEquals("Result size", 0, result.getFullListSize());

        filter.setExactMatch(false);

        result = vocabularyService.searchVocabularyConcepts(filter);
        assertEquals("Result size", 1, result.getFullListSize());

        filter.setText(null);
        filter.setVocabularyText("vocabulary1");

        result = vocabularyService.searchVocabularyConcepts(filter);
        //has 4 concepts in seed
        assertEquals("Result size", 4, result.getFullListSize());

        filter.setExactMatch(true);
        result = vocabularyService.searchVocabularyConcepts(filter);
        assertEquals("Result size", 0, result.getFullListSize());

    }

    @Test
    public void testSearchVocabulariesExactMatch() throws Exception {
        VocabularyFilter filter = new VocabularyFilter();
        // search in identifier
        filter.setText("est1");
        filter.setExactMatch(true);
        VocabularyResult result = vocabularyService.searchVocabularies(filter);

        assertTrue(result.getTotalItems() == 0);

        // full text search
        filter.setExactMatch(false);
        result = vocabularyService.searchVocabularies(filter);

        assertTrue(result.getTotalItems() == 1);
    }

    @Test
    public void testSearchConceptsWordMatch() throws Exception {
        VocabularyConceptFilter filter = new VocabularyConceptFilter();
        // search in identifier
        filter.setText("XYZ");
        filter.setWordMatch(true);
        VocabularyConceptResult result = vocabularyService.searchVocabularyConcepts(filter);

        assertTrue(result.getTotalItems() == 1);

        filter.setText("YZ");
        //filter.setWordMatch(false);
        result = vocabularyService.searchVocabularyConcepts(filter);
        assertTrue(result.getTotalItems() == 0);

        filter.setWordMatch(false);
        result = vocabularyService.searchVocabularyConcepts(filter);
        assertTrue(result.getTotalItems() == 1);
    }

    @Test
    public void testSearchVocabulariesWordMatch() throws Exception {
        VocabularyFilter filter = new VocabularyFilter();
        // search in identifier
        filter.setText("thisis");
        // word match
        filter.setWordMatch(true);
        VocabularyResult result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 0);

        filter.setWordMatch(false);
        result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 1);

        filter.setText("thisisit");
        // word match0
        filter.setWordMatch(true);
        result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 1);


        filter.setText("csv");

        result = vocabularyService.searchVocabularies(filter);
        assertTrue(result.getTotalItems() == 1);

    }

}
