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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */

package eionet.meta.service;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * JUnit integration test with Unitils for Vocabulary Import Services.
 *
 * @author enver
 */
@SpringApplicationContext("mock-spring-context.xml")
public abstract class VocabularyImportServiceTestBase extends UnitilsJUnit4 {

    /**
     * Valid vocabulary id.
     */
    protected static final int TEST_VALID_VOCABULARY_ID = 4;

    /**
     * tst references.
     */
    protected static final int TEST_REFERENCES_ID = 8;

    /**
     * Invalid vocabulary id.
     */
    protected static final int TEST_INVALID_VOCABULARY_ID = 16;

    /**
     * Vocabulary service.
     */
    @SpringBeanByType
    protected IVocabularyService vocabularyService;

    @SpringBeanByType
    protected PlatformTransactionManager transactionManager;

    /**
     * Generic date formatter for test case.
     */
    protected DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Get a reader from given RDF file location. If there is a BOM character, skip it.
     *
     * @param resourceLoc RDF file location
     * @return Reader object (BOM skipped)
     * @throws Exception if an error occurs
     */
    protected abstract Reader getReaderFromResource(String resourceLoc) throws Exception;

    /**
     * Utility code to make test code more readable. Returns all vocabulary concepts with attributes by delegating call to
     * vocabularyService
     *
     * @param vf VocabularyFolder which holds concepts
     * @return List of vocabulary concepts of given folder
     * @throws Exception if an error occurs
     */
    protected List<VocabularyConcept> getAllVocabularyConceptsWithAttributes(VocabularyFolder vf) throws Exception {
        return vocabularyService.getAllConceptsWithAttributes(vf.getId());
    }

    /**
     * Utility code to make test code more readable. Finds DataElement with given name in a list
     *
     * @param elems     DataElements to be searched
     * @param attrValue Value for comparison
     * @return First found DataElement
     */
    public static DataElement findDataElemByAttrValue(List<DataElement> elems, String attrValue) {
        return (DataElement) CollectionUtils.find(elems, new DataElementEvaluateOnAttributeValuePredicate(attrValue));
    }

    /**
     * Utility code to make test code more readable. Finds VocabularyConcept with given id in a list
     *
     * @param concepts VocabularyConcepts to be searched
     * @param id       Id for comparison
     * @return First found VocabularyConcept
     */
    public static VocabularyConcept findVocabularyConceptById(List<VocabularyConcept> concepts, int id) {
        return (VocabularyConcept) CollectionUtils.find(concepts, new VocabularyConceptEvaluateOnIdPredicate(id));
    }

    /**
     * Utility code to make test code more readable. Finds VocabularyConcept with given identifier in a list
     *
     * @param concepts   VocabularyConcepts to be searched
     * @param identifier identifier for comparison
     * @return First found VocabularyConcept
     */
    public static VocabularyConcept findVocabularyConceptByIdentifier(List<VocabularyConcept> concepts, String identifier) {
        return (VocabularyConcept) CollectionUtils.find(concepts, new VocabularyConceptEvaluateOnIdentifierPredicate(identifier));
    }

    /**
     * Inner class used to search for a VocabularyConcept in a Collection using it's id
     */
    public static class VocabularyConceptEvaluateOnIdPredicate implements Predicate {
        private int id = -1;

        public VocabularyConceptEvaluateOnIdPredicate(int id) {
            this.id = id;
        }

        @Override
        public boolean evaluate(Object object) {
            VocabularyConcept vc = (VocabularyConcept) object;
            return this.id == vc.getId();
        }
    }

    /**
     * Inner class used to search for a VocabularyConcept in a Collection using it's id `
     */
    public static class VocabularyConceptEvaluateOnIdentifierPredicate implements Predicate {
        private String identifier = null;

        public VocabularyConceptEvaluateOnIdentifierPredicate(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public boolean evaluate(Object object) {
            VocabularyConcept vc = (VocabularyConcept) object;
            return StringUtils.equals(this.identifier, vc.getIdentifier());
        }
    }

    /**
     * Inner class used to search for a DataElement using it's attribute value in a Collection
     */
    public static class DataElementEvaluateOnAttributeValuePredicate implements Predicate {
        private String value = null;

        public DataElementEvaluateOnAttributeValuePredicate(String value) {
            this.value = value;
        }

        @Override
        public boolean evaluate(Object object) {
            DataElement elem = (DataElement) object;
            return StringUtils.equals(value, elem.getAttributeValue());
        }
    }
}
