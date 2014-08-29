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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import eionet.meta.service.ServiceException;
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
     * Private static final String Hashing Algorithm for Triples.
     */
    private static final String HASHING_ALGORITHM = "MD5";
    /**
     * Used when getting bytes of a string to hash.
     */
    private static final String DEFAULT_ENCODING_OF_STRINGS = "UTF-8";
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
     * Bound elements of vocabulary.
     */
    protected Map<String, List<String>> boundElements = null;

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
     * Working language, should be two letters language code in lower case.
     */
    private String workingLanguage = null;
    /**
     * Local instance namespace.
     */
    private final String ddNamespace;
    /**
     * Number of duplicated triples.
     */
    private int numberOfDuplicatedTriples = 0;
    /**
     * In this set seen statements hascodes are stored not to process same statement once again.
     */
    private Set<BigInteger> seenStatementsHashCodes = null;
    /**
     * Message Digest instance used for triple hashing.
     */
    private MessageDigest messageDigestInstance = null;
    /**
     * This map store last seen candidate for DEFINITION and LABEL. Key value should be conceptId+dataelemIdentifier.
     */
    private Map<String, Literal> lastCandidateForConceptAttribute = null;

    /**
     * Constructor for RDFHandler to import rdf into vocabulary.
     *
     * @param folderContextRoot
     *            base uri for vocabulary.
     * @param concepts
     *            concepts of vocabulary
     * @param boundElements
     *            bound elements to vocabulary.
     * @param boundElementsToIds
     *            bound elements ids.
     * @param boundURIs
     *            rdf namespaces for bound elements
     * @param workingLanguage
     *            working language, only first two letters are used
     * @param createNewDataElementsForPredicates
     *            create new data elements for seen predicates
     * @param ddNamespace
     *            dd instance namespace
     * @throws ServiceException
     *             when digest algorithm cannot be found
     */
    public VocabularyRDFImportHandler(String folderContextRoot, List<VocabularyConcept> concepts,
            Map<String, Integer> boundElementsToIds, Map<String, List<String>> boundElements, Map<String, String> boundURIs,
            boolean createNewDataElementsForPredicates, String workingLanguage, String ddNamespace) throws ServiceException {
        super(folderContextRoot, concepts, boundElementsToIds);
        this.boundElements = boundElements;
        this.createNewDataElementsForPredicates = createNewDataElementsForPredicates;
        this.boundURIs = boundURIs;
        this.attributePositions = new HashMap<String, Map<String, Integer>>();
        this.predicateUpdatesAtConcepts = new HashMap<String, Set<Integer>>();
        this.notBoundPredicates = new HashSet<String>();
        this.identifierOfPredicate = new HashMap<String, String>();
        this.conceptsUpdatedForAttributes = new HashMap<String, Set<Integer>>();
        this.conceptsUpdatedForAttributes.put(SKOS_CONCEPT_ATTRIBUTE_NS + ":" + PREF_LABEL, new HashSet<Integer>());
        this.conceptsUpdatedForAttributes.put(SKOS_CONCEPT_ATTRIBUTE_NS + ":" + DEFINITION, new HashSet<Integer>());
        this.conceptsUpdatedForAttributes.put(SKOS_CONCEPT_ATTRIBUTE_NS + ":" + NOTATION, new HashSet<Integer>());
        this.lastCandidateForConceptAttribute = new HashMap<String, Literal>();
        // get first two letters of working language since, it can be like en-US
        this.workingLanguage = StringUtils.substring(workingLanguage, 0, 2);
        this.ddNamespace = ddNamespace;
        this.seenStatementsHashCodes = new HashSet<BigInteger>();
        try {
            this.messageDigestInstance = MessageDigest.getInstance(HASHING_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException(e.getMessage());
        }
    } // end of constructor

    @Override
    public void startRDF() throws RDFHandlerException {
    } // end of method startRDF

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
    } // end of method handleComment

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
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

        this.messageDigestInstance.reset();
        byte[] digested;
        try {
            digested = this.messageDigestInstance.digest(st.toString().getBytes(DEFAULT_ENCODING_OF_STRINGS));
        } catch (UnsupportedEncodingException e) {
            throw new RDFHandlerException(e);
        }
        BigInteger statementHashCode = new BigInteger(1, digested);
        if (this.seenStatementsHashCodes.contains(statementHashCode)) {
            // this.logMessages.add(st.toString() + " NOT imported, duplicates a previous triple");
            this.numberOfDuplicatedTriples++;
            return;
        }
        this.seenStatementsHashCodes.add(statementHashCode);

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
                    if (!this.boundElements.get(predicateNS).contains(attributeIdentifier)) {
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
            Pair<VocabularyConcept, Boolean> foundConceptWithFlag = findOrCreateConcept(conceptIdentifier);
            // if vocabulary concept couldnt find or couldnt be created
            if (foundConceptWithFlag == null) {
                return;
            }

            this.lastFoundConcept = foundConceptWithFlag.getLeft();
            if (!foundConceptWithFlag.getRight()) {
                // vocabulary concept found or created, add it to list
                this.toBeUpdatedConcepts.add(this.lastFoundConcept);
            }
        }

        String dataElemIdentifier = predicateNS + ":" + attributeIdentifier;
        if (StringUtils.equals(this.ddNamespace, predicateNS)) {
            dataElemIdentifier = attributeIdentifier;
        }

        // TODO code below can be refactored
        if (candidateForConceptAttribute
                && !this.conceptsUpdatedForAttributes.get(dataElemIdentifier).contains(this.lastFoundConcept.getId())) {
            this.conceptsUpdatedForAttributes.get(dataElemIdentifier).add(this.lastFoundConcept.getId());
            // update concept value here
            String val = StringUtils.trimToNull(object.stringValue());
            if (StringUtils.equals(attributeIdentifier, NOTATION)) {
                this.lastFoundConcept.setNotation(val);
            } else {
                if (StringUtils.equals(attributeIdentifier, DEFINITION)) {
                    this.lastFoundConcept.setDefinition(val);
                } else if (StringUtils.equals(attributeIdentifier, PREF_LABEL)) {
                    this.lastFoundConcept.setLabel(val);
                }
                String elemLang = StringUtils.substring(((Literal) object).getLanguage(), 0, 2);
                if (StringUtils.isNotBlank(elemLang)) {
                    this.lastCandidateForConceptAttribute
                            .put(this.lastFoundConcept.getId() + dataElemIdentifier, (Literal) object);
                    candidateForConceptAttribute = false;
                }
            }
        } else if (candidateForConceptAttribute
                && this.lastCandidateForConceptAttribute.containsKey(this.lastFoundConcept.getId() + dataElemIdentifier)) {
            // check if more prior value received
            Literal previousCandidate =
                    this.lastCandidateForConceptAttribute.remove(this.lastFoundConcept.getId() + dataElemIdentifier);

            String elemLang = StringUtils.substring(((Literal) object).getLanguage(), 0, 2);
            boolean updateValue = false;
            if (StringUtils.isEmpty(elemLang)) {
                updateValue = true;
            } else if (StringUtils.equals(elemLang, this.workingLanguage)
                    && !StringUtils.equals(StringUtils.substring(previousCandidate.getLanguage(), 0, 2), this.workingLanguage)) {
                updateValue = true;
                candidateForConceptAttribute = false;
                this.lastCandidateForConceptAttribute.put(this.lastFoundConcept.getId() + dataElemIdentifier, (Literal) object);
            } else {
                this.lastCandidateForConceptAttribute.put(this.lastFoundConcept.getId() + dataElemIdentifier, previousCandidate);
                candidateForConceptAttribute = false;
            }

            if (updateValue) {
                String val = StringUtils.trimToNull(object.stringValue());
                if (StringUtils.equals(attributeIdentifier, DEFINITION)) {
                    this.lastFoundConcept.setDefinition(val);
                } else if (StringUtils.equals(attributeIdentifier, PREF_LABEL)) {
                    this.lastFoundConcept.setLabel(val);
                }
            }
        } else {
            candidateForConceptAttribute = false;
        }

        if (!candidateForConceptAttribute) {
            if (!this.boundElementsIds.containsKey(dataElemIdentifier)) {
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
            if (StringUtils.isEmpty(elementValue)) {
                // value is empty, no need to continue
                return;
            }
            String elemLang = null;
            VocabularyConcept foundRelatedConcept = null;
            // if object is a resource (i.e. URI), it can be a related concept
            if (object instanceof URI) {
                foundRelatedConcept = findRelatedConcept(elementValue);
            } else if (object instanceof Literal) {
                // it is literal
                elemLang = StringUtils.substring(((Literal) object).getLanguage(), 0, 2);
            }

            if (!StringUtils.equals(dataElemIdentifier, prevDataElemIdentifier) || !StringUtils.equals(elemLang, prevLang)) {
                elementsOfConceptByLang =
                        getDataElementValuesByNameAndLang(dataElemIdentifier, elemLang, lastFoundConcept.getElementAttributes());
            }
            this.prevLang = elemLang;
            this.prevDataElemIdentifier = dataElemIdentifier;

            // check for pre-existence of the VCE by attribute value or related concept id
            Integer relatedId = null;
            if (foundRelatedConcept != null) {
                relatedId = foundRelatedConcept.getId();
            }
            for (DataElement elemByLang : elementsOfConceptByLang) {
                String elementValueByLang = elemByLang.getAttributeValue();
                if (StringUtils.equals(elementValue, elementValueByLang)) {
                    // vocabulary concept element already in database, no need to continue, return
                    return;
                }
                if (relatedId != null) {
                    Integer relatedConceptId = elemByLang.getRelatedConceptId();
                    if (relatedConceptId != null && relatedConceptId.intValue() == relatedId.intValue()) {
                        // vocabulary concept element already in database, no need to continue, return
                        return;
                    }
                }
            }

            // create VCE
            DataElement elem = new DataElement();
            this.elementsOfConcept.add(elem);
            elem.setAttributeLanguage(elemLang);
            elem.setIdentifier(dataElemIdentifier);
            elem.setId(this.boundElementsIds.get(dataElemIdentifier));
            // check if there is a found related concept
            if (foundRelatedConcept != null) {
                elem.setRelatedConceptIdentifier(foundRelatedConcept.getIdentifier());
                int id = foundRelatedConcept.getId();
                elem.setRelatedConceptId(id);
                elem.setAttributeValue(null);
                if (id < 0) {
                    addToElementsReferringNotCreatedConcepts(id, elem);
                }
            } else {
                elem.setAttributeValue(elementValue);
                elem.setRelatedConceptId(null);
            }

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

        // check for null label containing concepts
        List<String> conceptsWithNullLabels = processNewlyCreatedConceptsForNullCheck();
        // process unseen concepts for related elements
        processUnseenConceptsForRelatedElements();

        // add some logs
        this.logMessages.add("Valid (" + this.numberOfValidTriples + ") / Total (" + this.totalNumberOfTriples + ")");
        // this.logMessages.add("Found related concept cache count: " + this.relatedConceptCache.keySet().size());
        this.logMessages.add("Number of predicates seen: " + this.predicateUpdatesAtConcepts.size());
        this.logMessages.add("Number of duplicate triples: " + this.numberOfDuplicatedTriples);
        this.logMessages.add("Number of concepts seen per predicate: ");
        for (String key : this.predicateUpdatesAtConcepts.keySet()) {
            this.logMessages.add("--> " + key + " (" + this.identifierOfPredicate.get(key) + "): "
                    + this.predicateUpdatesAtConcepts.get(key).size());
        }
        this.logMessages.add("Not imported predicates (" + this.notBoundPredicates.size()
                + ") which are not bound to vocabulary: ");
        for (String predicate : this.notBoundPredicates) {
            this.logMessages.add("--> " + predicate);
        }
        if (conceptsWithNullLabels != null) {
            this.logMessages.addAll(conceptsWithNullLabels);
        }
        this.logMessages.add("Number of updated concepts: " + this.toBeUpdatedConcepts.size());
    } // end of method endRDF

} // end of class VocabularyRDFImportHandler
