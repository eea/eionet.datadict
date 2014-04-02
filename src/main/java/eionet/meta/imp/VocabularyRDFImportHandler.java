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

package eionet.meta.imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.util.Pair;

/**
 * Implementation of OpenRDF's {@link RDFHandler} that will be used by implementations of
 * {@link eionet.meta.service.IRDFVocabularyImportService}. Contains callback methods for listening to the content coming from
 * {@link eionet.meta.service.IRDFVocabularyImportService} and loading them into vocabulary.
 *
 * @author enver
 */
// @Configurable
public class VocabularyRDFImportHandler extends VocabularyImportBaseHandler implements RDFHandler {

    /* static constants */
    /**
     * Mapping for predicate ignorance. If a predicate needs to be ignored it can be put in to map with its String value as a key.
     * Value for that key is an instance of Pair holds a Class and a String. Class is used to check the type of Object (in triple).
     * String is a regular expression to match with Object's string value. This works as follows: Triple<Subject S, Predicate P,
     * Object O> T *****if rule set has a Rule (Pair) R in Map for P then *********if O is an instance of R.Class and O.stringValue
     * matches with R.String then **************ignore T
     */
    private static final Map<String, Pair<Class, String>> PREDICATE_IGNORANCE_RULES;

    /**
     * notation attribute of concept.
     */
    private static final String NOTATION = "notation";
    /**
     * label attribute of concept.
     */
    private static final String PREF_LABEL = "prefLabel";
    /**
     * definition attribute of concept.
     */
    private static final String DEFINITION = "definition";

    /**
     * used with concept attributes.
     */
    private static final String SKOS_CONCEPT_ATTRIBUTE_NS = "skos";
    /**
     * concept attribute namespaces to update concept fields instead of dataelements.
     */
    private static final List<String> SKOS_CONCEPT_ATTRIBUTES;

    static {
        SKOS_CONCEPT_ATTRIBUTES = new ArrayList<String>();
        SKOS_CONCEPT_ATTRIBUTES.add("notation");
        SKOS_CONCEPT_ATTRIBUTES.add("prefLabel");
        SKOS_CONCEPT_ATTRIBUTES.add("definition");
        PREDICATE_IGNORANCE_RULES = new HashMap<String, Pair<Class, String>>();
        PREDICATE_IGNORANCE_RULES.put(VocabularyXmlWriter.SKOS_NS + "inScheme", new Pair<Class, String>(Object.class, "(.)*"));
        PREDICATE_IGNORANCE_RULES.put(VocabularyXmlWriter.RDF_NS + "type", new Pair<Class, String>(URI.class,
                VocabularyXmlWriter.SKOS_NS + "Concept"));
    }

    /* member fields */
    /**
     * bound uri's to vocabulary.
     */
    private Map<String, String> boundURIs = null;

    /**
     * Binded elements of vocabulary.
     */
    protected Map<String, List<String>> bindedElements = null;

    /**
     * number of valid triples that are processed.
     */
    private int numberOfValidTriples = 0;
    /**
     * number of total triples that are processed.
     */
    private int totalNumberOfTriples = 0;
    /**
     * Temporary list for an element identifier (not to query all the time).
     */
    private List<DataElement> elementsOfConcept = null;
    /**
     * Temporary list for an element identifier with language (not to query all the time).
     */
    private List<DataElement> elementsOfConceptByLang = null;
    /**
     * Previous successful tiple's subject (concept) identifier.
     */
    private String prevConceptIdentifier = null;
    /**
     * Previous successful triple's predicate (data element) identifier.
     */
    private String prevDataElemIdentifier = null;
    /**
     * Previous successful triple's language.
     */
    private String prevLang = null;
    /**
     * Map to hold dataelement positions.
     */
    private Map<String, Map<String, Integer>> attributePositions = null;
    /**
     * Temporary object to hold last found concept not to iterate over again in lists.
     */
    private VocabularyConcept lastFoundConcept;
    /**
     * This map is used to detect if a predicate (prefLabel, definition or notation) is used for concept update or not.
     */
    private Map<String, Set<Integer>> conceptsUpdatedForAttributes = null;
    /**
     * Number of concepts updated per predicate.
     */
    private Map<String, Set<Integer>> predicateUpdatesAtConcepts = null;
    /**
     * Boolean to create new data elements for predicates.
     */
    private boolean createNewDataElementsForPredicates = false;
    /**
     * This set includes predicates which are not bound to vocabulary.
     */
    private Set<String> notBoundPredicates = null;
    /**
     * This is map to get data element identifier for a predicate.
     */
    private Map<String, String> identifierOfPredicate = null;

    /**
     * Constructor for RDFHandler to import rdf into vocabulary.
     *
     * @param folderContextRoot
     *            base uri for vocabulary.
     * @param concepts
     *            concepts of vocabulary
     * @param bindedElements
     *            binded elements to vocabulary.
     * @param bindedElementsIds
     *            binded elements ids.
     * @param createNewDataElementsForPredicates
     *            create new data elements for seen predicates
     */
    public VocabularyRDFImportHandler(String folderContextRoot, List<VocabularyConcept> concepts,
            Map<String, Integer> bindedElementsIds, Map<String, List<String>> bindedElements,
            boolean createNewDataElementsForPredicates) {
        super(folderContextRoot, concepts, bindedElementsIds);
        this.bindedElements = bindedElements;
        this.createNewDataElementsForPredicates = createNewDataElementsForPredicates;
        this.boundURIs = new HashMap<String, String>();
        this.attributePositions = new HashMap<String, Map<String, Integer>>();
        this.predicateUpdatesAtConcepts = new HashMap<String, Set<Integer>>();
        this.notBoundPredicates = new HashSet<String>();
        this.identifierOfPredicate = new HashMap<String, String>();
        this.conceptsUpdatedForAttributes = new HashMap<String, Set<Integer>>();
        this.conceptsUpdatedForAttributes.put(SKOS_CONCEPT_ATTRIBUTE_NS + ":" + PREF_LABEL, new HashSet<Integer>());
        this.conceptsUpdatedForAttributes.put(SKOS_CONCEPT_ATTRIBUTE_NS + ":" + DEFINITION, new HashSet<Integer>());
        this.conceptsUpdatedForAttributes.put(SKOS_CONCEPT_ATTRIBUTE_NS + ":" + NOTATION, new HashSet<Integer>());
    } // end of constructor

    @Override
    public void startRDF() throws RDFHandlerException {
    } // end of method startRDF

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
    } // end of method handleComment

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        if (this.bindedElements.containsKey(prefix)) {
            this.boundURIs.put(uri, prefix);
        }
    } // end of method handleNamespace

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        this.totalNumberOfTriples++;
        Resource subject = st.getSubject();
        URI predicate = st.getPredicate();
        Value object = st.getObject();

        if (!(subject instanceof URI)) {
            // this.logMessages.add(st.toString() + " NOT imported, subject is not a URI");
            return;
        }

        // object should a resource or a literal (value)
        if (!(object instanceof URI) && !(object instanceof Literal)) {
            // this.logMessages.add(st.toString() + " NOT imported, object is not instance of URI or Literal");
            return;
        }

        String conceptUri = subject.stringValue();
        if (!StringUtils.startsWith(conceptUri, this.folderContextRoot)) {
            // this.logMessages.add(st.toString() + " NOT imported, does not have base URI");
            return;
        }

        // if it does not a have conceptIdentifier than it may be an attribute for vocabulary or a wrong record, so just ignore it
        String conceptIdentifier = conceptUri.replace(this.folderContextRoot, "");
        if (StringUtils.isEmpty(conceptIdentifier) || StringUtils.contains(conceptIdentifier, "/")) {
            // this.logMessages.add(st.toString() + " NOT imported, contains a / in concept identifier or empty");
            return;
        }

        String predicateUri = predicate.stringValue();

        Pair<Class, String> ignoranceRule = PREDICATE_IGNORANCE_RULES.get(predicateUri);
        if (ignoranceRule != null) {
            if (ignoranceRule.getLeft().isInstance(object) && object.stringValue().matches(ignoranceRule.getRight())) {
                // ignore value
                return;
            }
        }

        String attributeIdentifier = null;
        String predicateNS = null;

        boolean candidateForConceptAttribute = false;
        if (StringUtils.startsWith(predicateUri, VocabularyXmlWriter.SKOS_NS)) {
            attributeIdentifier = predicateUri.replace(VocabularyXmlWriter.SKOS_NS, "");
            candidateForConceptAttribute = SKOS_CONCEPT_ATTRIBUTES.contains(attributeIdentifier);
            if (candidateForConceptAttribute) {
                predicateNS = SKOS_CONCEPT_ATTRIBUTE_NS;
            }
        }

        if (candidateForConceptAttribute && !(object instanceof Literal)) {
            // this.logMessages.add(st.toString() + " NOT imported, object is not a Literal for concept attribute");
            return;
        }

        if (!candidateForConceptAttribute) {
            for (String key : this.boundURIs.keySet()) {
                if (StringUtils.startsWith(predicateUri, key)) {
                    attributeIdentifier = predicateUri.replace(key, "");
                    predicateNS = this.boundURIs.get(key);
                    if (!this.bindedElements.get(predicateNS).contains(attributeIdentifier)) {
                        predicateNS = null;
                    }
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(predicateNS)) {
            // this.logMessages.add(st.toString() + " NOT imported, predicate is not a bound URI nor a concept attribute");
            this.notBoundPredicates.add(predicateUri);
            return;
        }

        // if execution comes here so we have a valid triple to import
        // first find the concept
        if (!StringUtils.equals(conceptIdentifier, this.prevConceptIdentifier)) {
            this.prevDataElemIdentifier = null;
            this.prevLang = null;
            this.lastFoundConcept = null;
        }
        this.prevConceptIdentifier = conceptIdentifier;

        if (this.lastFoundConcept == null) {
            this.lastFoundConcept = findOrCreateConcept(conceptIdentifier);
            // if vocabulary concept couldnt find or couldnt be created
            if (this.lastFoundConcept == null) {
                // this.logMessages.add(st.toString() + " NOT imported, duplicate of previously imported concept.");
                return;
            }
            // vocabulary concept found or created, add it to list
            this.toBeUpdatedConcepts.add(this.lastFoundConcept);
        }

        String dataElemIdentifier = predicateNS + ":" + attributeIdentifier;

        if (candidateForConceptAttribute
                && this.conceptsUpdatedForAttributes.get(dataElemIdentifier).contains(this.lastFoundConcept.getId())) {
            candidateForConceptAttribute = false;
        }

        if (candidateForConceptAttribute) {
            this.conceptsUpdatedForAttributes.get(dataElemIdentifier).add(this.lastFoundConcept.getId());
            // update concept value here
            String val = StringUtils.trimToNull(object.stringValue());
            if (StringUtils.equals(attributeIdentifier, NOTATION)) {
                this.lastFoundConcept.setNotation(val);
            } else if (StringUtils.equals(attributeIdentifier, DEFINITION)) {
                this.lastFoundConcept.setDefinition(val);
            } else if (StringUtils.equals(attributeIdentifier, PREF_LABEL)) {
                this.lastFoundConcept.setLabel(val);
            } else {
                // this.logMessages.add("this line shouldn't be reached");
                return;
            }
        } else {
            if (!this.bindedElementsIds.containsKey(dataElemIdentifier)) {
                this.notBoundPredicates.add(predicateUri);
                return;
            }

            Set<Integer> conceptIdsUpdatedWithPredicate = this.predicateUpdatesAtConcepts.get(predicateUri);
            if (conceptIdsUpdatedWithPredicate == null) {
                conceptIdsUpdatedWithPredicate = new HashSet<Integer>();
                this.predicateUpdatesAtConcepts.put(predicateUri, conceptIdsUpdatedWithPredicate);
            }
            // find the data element
            if (!this.identifierOfPredicate.containsKey(predicateUri)) {
                this.identifierOfPredicate.put(predicateUri, dataElemIdentifier);
            }
            if (!StringUtils.equals(dataElemIdentifier, this.prevDataElemIdentifier)) {
                elementsOfConcept = getDataElementValuesByName(dataElemIdentifier, lastFoundConcept.getElementAttributes());
                if (createNewDataElementsForPredicates && !conceptIdsUpdatedWithPredicate.contains(lastFoundConcept.getId())) {
                    if (this.elementsOfConcept != null) {
                        this.lastFoundConcept.getElementAttributes().remove(this.elementsOfConcept);
                    }
                    this.elementsOfConcept = null;
                }

                if (this.elementsOfConcept == null) {
                    this.elementsOfConcept = new ArrayList<DataElement>();
                    this.lastFoundConcept.getElementAttributes().add(this.elementsOfConcept);
                }
            }

            String elementValue = object.stringValue();
            String elemLang = null;
            VocabularyConcept foundRelatedConcept = null;
            // if object is a resource (i.e. URI), it can be a related concept
            if (object instanceof URI && StringUtils.isNotEmpty(elementValue)) {
                foundRelatedConcept = findRelatedConcept(elementValue);
            } else if (object instanceof Literal) {
                // it is literal
                elemLang = ((Literal) object).getLanguage();
            }

            if (!StringUtils.equals(dataElemIdentifier, prevDataElemIdentifier) || !StringUtils.equals(elemLang, prevLang)) {
                elementsOfConceptByLang =
                        getDataElementValuesByNameAndLang(dataElemIdentifier, elemLang, lastFoundConcept.getElementAttributes());
            }
            this.prevLang = elemLang;
            this.prevDataElemIdentifier = dataElemIdentifier;

            Map<String, Integer> attributePosition = this.attributePositions.get(conceptIdentifier);
            if (attributePosition == null) {
                attributePosition = new HashMap<String, Integer>();
                this.attributePositions.put(conceptIdentifier, attributePosition);
            }

            String dataElemIdentifierWithLang = dataElemIdentifier;
            if (StringUtils.isNotEmpty(elemLang)) {
                dataElemIdentifierWithLang += "@" + elemLang;
            }
            if (!attributePosition.containsKey(dataElemIdentifierWithLang)) {
                attributePosition.put(dataElemIdentifierWithLang, 0);
            }
            int index = attributePosition.get(dataElemIdentifierWithLang);

            // if object is empty, user wants to delete
            if (StringUtils.isNotEmpty(elementValue)) {
                DataElement elem;
                if (index < this.elementsOfConceptByLang.size()) {
                    elem = this.elementsOfConceptByLang.get(index);
                } else {
                    elem = new DataElement();
                    this.elementsOfConcept.add(elem);
                    elem.setAttributeLanguage(elemLang);
                    elem.setIdentifier(dataElemIdentifier);
                    elem.setId(this.bindedElementsIds.get(dataElemIdentifier));
                }

                if (foundRelatedConcept != null) {
                    elem.setRelatedConceptIdentifier(foundRelatedConcept.getIdentifier());
                    int id = foundRelatedConcept.getId();
                    elem.setRelatedConceptId(id);
                    if (id < 0) {
                        addToElementsReferringNotCreatedConcepts(id, elem);
                    }
                } else {
                    elem.setAttributeValue(elementValue);
                }
            } else {
                // if it is empty and if there is such a value then delete it, if there is no value just ignore it
                if (index < this.elementsOfConceptByLang.size()) {
                    DataElement elem = elementsOfConceptByLang.get(index);
                    elem.setAttributeValue(elementValue);
                }
            }
            attributePosition.put(dataElemIdentifierWithLang, ++index);
            conceptIdsUpdatedWithPredicate.add(this.lastFoundConcept.getId());
        }
        this.numberOfValidTriples++;
    } // end of method handleStatement

    @Override
    public void endRDF() throws RDFHandlerException {
        if (this.createNewDataElementsForPredicates) {
            // if purge per predicate is selected and rdf does not contain any for some concepts. Then those untouched concepts
            // should be updated to remove these predicates if they have
            // 1. first do it for toBeUpdateConcepts
            for (VocabularyConcept concept : this.toBeUpdatedConcepts) {
                for (String key : this.predicateUpdatesAtConcepts.keySet()) {
                    if (!this.predicateUpdatesAtConcepts.get(key).contains(concept.getId())) {
                        List<DataElement> conceptElements =
                                getDataElementValuesByName(this.identifierOfPredicate.get(key), concept.getElementAttributes());
                        if (conceptElements != null && conceptElements.size() > 0) {
                            concept.getElementAttributes().remove(conceptElements);
                        }
                    }
                }
            }
            // 2. do it for untouched concepts
            for (VocabularyConcept concept : this.concepts) {
                boolean conceptUpdated = false;
                for (String key : this.predicateUpdatesAtConcepts.keySet()) {
                    List<DataElement> conceptElements =
                            getDataElementValuesByName(this.identifierOfPredicate.get(key), concept.getElementAttributes());
                    if (conceptElements != null && conceptElements.size() > 0) {
                        concept.getElementAttributes().remove(conceptElements);
                        conceptUpdated = true;
                    }
                }
                if (conceptUpdated) {
                    this.toBeUpdatedConcepts.add(concept);
                }
            }
        }

        // process unseen concepts for related elements
        processUnseenConceptsForRelatedElements();

        // add some logs
        this.logMessages.add("Valid (" + this.numberOfValidTriples + ") / Total (" + this.totalNumberOfTriples + ")");
        //this.logMessages.add("Found related concept cache count: " + this.relatedConceptCache.keySet().size());
        this.logMessages.add("Number of predicates seen: " + this.predicateUpdatesAtConcepts.size());
        this.logMessages.add("Number updated concepts for predicates: ");
        for (String key : this.predicateUpdatesAtConcepts.keySet()) {
            this.logMessages.add("--> " + key + " (" + this.identifierOfPredicate.get(key) + "): "
                    + this.predicateUpdatesAtConcepts.get(key).size());
        }
        this.logMessages.add("Not imported predicates (" + this.notBoundPredicates.size()
                + ") which are not bound to vocabulary: ");
        for (String predicate : this.notBoundPredicates) {
            this.logMessages.add("--> " + predicate);
        }
    } // end of method endRDF

} // end of class VocabularyRDFImportHandler
