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

import java.util.ArrayList;
import java.util.List;

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
import eionet.meta.service.data.ObsoleteStatus;

/**
 * JUnit integration test with Unitils for vocabulary service relation methods.
 *
 * @author Kaido Laine
 */
@SpringApplicationContext("spring-context.xml")
// @DataSet({"seed-vocabularies.xml"})
public class VocabularyServiceRelationsTest extends UnitilsJUnit4 {

    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(VocabularyServiceTest.class);


    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-vocabularyrelations.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-vocabularyrelations.xml");
    }

    /**
     * tests behaviour of keepRelationValues attribute.
     * value should be replaced by uri in concepts that have relation to the deletable vocabulary.
     * @throws Exception if fail
     */
    @Test
    public void testFolderDelete() throws Exception {

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(1);

        //should contain urls
        vocabularyService.deleteVocabularyFolders(ids, true);

        //VocabularyFolder vf2 = vocabularyService.getVocabularyFolder(2);
        List<VocabularyConcept> concepts2 =  vocabularyService.getValidConceptsWithAttributes(2);
        Assert.assertTrue(concepts2.size() == 2);

        VocabularyConcept concept22 = concepts2.get(1);

        //value in second relation attribute should have been replaced by url:
        DataElement skosBroader22 = concept22.getElementAttributes().get(0).get(0);
        Assert.assertEquals("http://www.baseuri.com/concept2", skosBroader22.getAttributeValue());

        //Assert.assertTrue(concept22.getElementAttributes().get(0).isEmpty());


        ids.set(0, 4);
        vocabularyService.deleteVocabularyFolders(ids, false);

        //VocabularyFolder vf3 = vocabularyService.getVocabularyFolder(3);
        List<VocabularyConcept> concepts3 = vocabularyService.getValidConceptsWithAttributes(3);

        Assert.assertTrue(concepts3.size() == 2);
        VocabularyConcept concept31 = concepts3.get(0);

        //both relations should be deleted as requested by "false" parameter in delete()
        Assert.assertTrue(concept31.getElementAttributes().get(0).isEmpty());

    }

}
