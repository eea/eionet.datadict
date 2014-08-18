package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;

/**
 * Util methods for handling domain objects.
 */
public final class DAOUtils {

    /** prevent initialization ot util class. */
    private DAOUtils() {
    }

    /**
     * Finds vocabulary simple attribute by name, returns first value.
     *
     * @param voc
     *            Vocabulary
     * @param attrName
     *            attribute name
     * @return Attribute value
     */
    public static String getVocabularyAttributeByName(VocabularyFolder voc, String attrName) {
        List<List<SimpleAttribute>> vocAttributes = voc.getAttributes();
        if (vocAttributes != null) {
            for (List<SimpleAttribute> attributeList : vocAttributes) {
                SimpleAttribute attribute = attributeList.get(0);
                if (attribute.getIdentifier().equalsIgnoreCase(attrName)) {
                    return attribute.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Checks vocabulary concepts status.
     *
     * @param vocabulary
     *            Vocabulary
     * @return true if at least one concept is valid
     */
    public static boolean anyConceptValid(VocabularyFolder vocabulary) {
        List<VocabularyConcept> concepts = vocabulary.getConcepts();
        if (concepts != null && concepts.size() > 0) {
            for (VocabularyConcept concept : vocabulary.getConcepts()) {
                if (concept.getStatus().isValid()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

}
