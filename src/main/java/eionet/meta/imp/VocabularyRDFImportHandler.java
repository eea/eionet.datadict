package eionet.meta.imp;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.VocabularyCSVOutputHelper;
import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of OpenRDF's {@link RDFHandler} that will be used by implementations of
 * {@link eionet.meta.service.IRDFVocabularyImportService}.
 * Contains callback methods for listening to the content coming from {@link eionet.meta.service.IRDFVocabularyImportService} and
 * loading them into vocabulary.
 *
 * @author enver
 */
public class VocabularyRDFImportHandler implements RDFHandler {

    /* static constants */
    /**
     * used with concept attributes.
     */
    private static final String SKOS_CONCEPT_ATTRIBUTE_NS = "skosConceptAttribute";
    /**
     * concept attribute namespaces to update concept fields instead of dataelements.
     */
    private static final List<String> SKOS_CONCEPT_ATTRIBUTES;

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

    static {
        SKOS_CONCEPT_ATTRIBUTES = new ArrayList<String>();
        SKOS_CONCEPT_ATTRIBUTES.add(NOTATION);
        SKOS_CONCEPT_ATTRIBUTES.add(PREF_LABEL);
        SKOS_CONCEPT_ATTRIBUTES.add(DEFINITION);
    }

    /* member fields */
    /**
     * Vocabulary service.
     */
    private IVocabularyService vocabularyService;
    /**
     * value with folderContextRoot + CONCEPT_KEY .
     */
    private String folderContextRootWithConceptKey = null;
    /**
     * log message list.
     */
    private List<String> logs = null;
    /**
     * bounded uri's to vocabulary.
     */
    private Map<String, String> boundedURIs = null;
    /**
     * Concepts of folder.
     */
    private List<VocabularyConcept> concepts = null;
    /**
     * Generated concept beans.
     */
    private List<VocabularyConcept> toBeUpdatedConcepts = null;
    /**
     * Binded elements of vocabulary.
     */
    private Map<String, List<String>> bindedElements = null;
    /**
     * Binded elements ids.
     */
    Map<String, Integer> bindedElementsIds = null;
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
    private String prevAttributeIdentifier = null;
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
     * Temporary map to hold found related concepts for caching.
     */
    private Map<String, VocabularyConcept> relatedConceptCache = null;

    private int numberOfSearches = 0;
    private int numberOfPotentialRelatedConcepts = 0;
    private int numberOfVocabularySearches = 0;

    private long start = 0;
    private long end = 0;
    private int numberOfCacheFound = 0;


    /**
     * Constructor for RDFHandler to import rdf into vocabulary.
     *
     * @param folderContextRoot base uri for vocabulary.
     * @param concepts          concepts of vocabulary
     * @param bindedElements    binded elements to vocabulary.
     * @param bindedElementsIds binded elements ids.
     */
    public VocabularyRDFImportHandler(String folderContextRoot, List<VocabularyConcept> concepts, Map<String,
            List<String>> bindedElements, Map<String, Integer> bindedElementsIds) {
        this.folderContextRootWithConceptKey = folderContextRoot; // + CONCEPT_KEY;
        this.concepts = concepts;
        this.bindedElements = bindedElements;
        this.bindedElementsIds = bindedElementsIds;
        this.toBeUpdatedConcepts = new ArrayList<VocabularyConcept>();
        this.logs = new ArrayList<String>();
        this.boundedURIs = new HashMap<String, String>();
        this.attributePositions = new HashMap<String, Map<String, Integer>>();
        this.relatedConceptCache = new HashMap<String, VocabularyConcept>();
        //@Configure and @Autowired does not work :\
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
        this.vocabularyService = context.getBean(IVocabularyService.class);
    } // end of constructor

    @Override
    public void startRDF() throws RDFHandlerException {
        start = System.currentTimeMillis();
    } // end of method startRDF

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
    } // end of method handleComment

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        if (this.bindedElements.containsKey(prefix)) {
            this.boundedURIs.put(uri, prefix);
        }
    } // end of method handleNamespace

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        this.totalNumberOfTriples++;
        Resource subject = st.getSubject();
        URI predicate = st.getPredicate();
        Value object = st.getObject();

        if (!(subject instanceof URI)) {
            //this.logs.add(st.toString() + " NOT imported, subject is not a URI");
            return;
        }

        if (!(object instanceof URI) && !(object instanceof Literal)) { // a resource or a literal (value)
            //this.logs.add(st.toString() + " NOT imported, object is not instance of URI or Literal");
            return;
        }

        //remove concept key if there is. TODO am i missing something here? some concepts has this some not!
        String conceptUri = subject.stringValue(); //.replace(CONCEPT_KEY, "");
        if (!StringUtils.startsWith(conceptUri, this.folderContextRootWithConceptKey)) {
            //this.logs.add(st.toString() + " NOT imported, does not have base URI");
            return;
        }

        //if it does not a have conceptIdentifier than it may be an attribute for vocabulary or a wrong record, so just ignore it
        String conceptIdentifier = conceptUri.replace(this.folderContextRootWithConceptKey, "");
        if (StringUtils.isEmpty(conceptIdentifier) || StringUtils.contains(conceptIdentifier, "/")) {
            //this.logs.add(st.toString() + " NOT imported, contains a / in concept identifier or empty");
            return;
        }

        String predicateUri = predicate.stringValue();
        String attributeIdentifier = null;
        String predicateNS = null;
        for (String key : this.boundedURIs.keySet()) {
            if (StringUtils.startsWith(predicateUri, key)) {
                attributeIdentifier = predicateUri.replace(key, "");
                predicateNS = this.boundedURIs.get(key);
                if (!this.bindedElements.get(predicateNS).contains(attributeIdentifier)) {
                    predicateNS = null;
                }
                break;
            }
        }

        boolean conceptAttribute = false;
        if (StringUtils.isEmpty(predicateNS)) {
            if (StringUtils.startsWith(predicateUri, VocabularyXmlWriter.SKOS_NS)) {
                attributeIdentifier = predicateUri.replace(VocabularyXmlWriter.SKOS_NS, "");
                conceptAttribute = SKOS_CONCEPT_ATTRIBUTES.contains(attributeIdentifier);
                if (conceptAttribute) {
                    predicateNS = SKOS_CONCEPT_ATTRIBUTE_NS;
                }
            }
        }

        if (StringUtils.isEmpty(predicateNS)) {
            //this.logs.add(st.toString() + " NOT imported, predicate is not a bound URI nor a concept attribute");
            return;
        }

        if (conceptAttribute && !(object instanceof Literal)) {
            //this.logs.add(st.toString() + " NOT imported, object is not a Literal for concept attribute");
            return;
        }

        //if execution comes here so we have a valid triple to import
        //first find the concept
        if (!StringUtils.equals(conceptIdentifier, this.prevConceptIdentifier)) {
            this.prevAttributeIdentifier = null;
            this.prevLang = null;
            this.lastFoundConcept = null;
        }
        this.prevConceptIdentifier = conceptIdentifier;

        //TODO copied and pasted code from CSV, make it common, and use collection finder
        if (this.lastFoundConcept == null) {
            int j;
            for (j = 0; j < this.concepts.size(); j++) {
                VocabularyConcept vc = this.concepts.get(j);
                if (StringUtils.equals(conceptIdentifier, vc.getIdentifier())) {
                    break;
                }
            }

            //this.lastFoundConcept = null;
            // concept found
            if (j < this.concepts.size()) {
                this.lastFoundConcept = this.concepts.remove(j);
                // vocabulary concept found
                this.toBeUpdatedConcepts.add(lastFoundConcept);
            } else {
                for (j = 0; j < this.toBeUpdatedConcepts.size(); j++) {
                    VocabularyConcept vc = this.toBeUpdatedConcepts.get(j);
                    if (StringUtils.equals(conceptIdentifier, vc.getIdentifier())) {
                        this.lastFoundConcept = vc;
                        break;
                    }
                }
                if (this.lastFoundConcept == null && j == this.toBeUpdatedConcepts.size()) {
                    // if there is already such a concept, ignore that line. if not, add a new concept with params.
                    this.lastFoundConcept = new VocabularyConcept();
                    this.lastFoundConcept.setIdentifier(conceptIdentifier);
                    // TODO set other properties
                    List<List<DataElement>> newConceptElementAttributes = new ArrayList<List<DataElement>>();
                    this.lastFoundConcept.setElementAttributes(newConceptElementAttributes);
                    // vocabulary concept created
                    this.toBeUpdatedConcepts.add(lastFoundConcept);
                }
            }
        }

        // if vocabulary concept couldnt find or created
        if (this.lastFoundConcept == null) {
            //this.logs.add(st.toString() + " NOT imported, cannot find or create.");
            return;
        }

        if (conceptAttribute) {
            //update concept value here
            if (StringUtils.equals(attributeIdentifier, NOTATION)) {
                this.lastFoundConcept.setNotation(object.stringValue());
            } else if (StringUtils.equals(attributeIdentifier, DEFINITION)) {
                this.lastFoundConcept.setDefinition(object.stringValue());
            } else if (StringUtils.equals(attributeIdentifier, PREF_LABEL)) {
                this.lastFoundConcept.setLabel(object.stringValue());
            } else {
                //this.logs.add("this line shouldn't be reached");
                return;
            }
        } else {
            //find the data element
            String dataElemIdentifier = predicateNS + ":" + attributeIdentifier;
            if (!StringUtils.equals(attributeIdentifier, this.prevAttributeIdentifier)) {
                this.elementsOfConcept = VocabularyCSVOutputHelper.getDataElementValuesByName(dataElemIdentifier,
                        this.lastFoundConcept.getElementAttributes());
                if (this.elementsOfConcept == null) {
                    this.elementsOfConcept = new ArrayList<DataElement>();
                    this.lastFoundConcept.getElementAttributes().add(this.elementsOfConcept);
                }
            }

            String elementValue = object.stringValue();
            String elemLang = null;
            int relatedConceptId = -1;
            String relatedConceptIdentifier = null;
            String relatedConceptBaseUri = null;
            String relatedConceptVocabularyIdentifier = null;
            //if object is a resource (i.e. URI), it can be a related concept
            if (object instanceof URI && StringUtils.isNotEmpty(elementValue)) {
                //for better meaning
                String relatedConceptUri = elementValue;
                int lastSlashIndex = relatedConceptUri.lastIndexOf("/") + 1;
                relatedConceptIdentifier = relatedConceptUri.substring(lastSlashIndex);
                relatedConceptBaseUri = relatedConceptUri.substring(0, lastSlashIndex);
                if (StringUtils.isNotEmpty(relatedConceptBaseUri) && StringUtils.isNotEmpty(relatedConceptIdentifier)) {
                    this.numberOfPotentialRelatedConcepts++;

                    //check cache first
                    VocabularyConcept foundRelatedConcept = this.relatedConceptCache.get(relatedConceptUri);
                    //&& !this.notFoundRelatedConceptCache.contains(relatedConceptUri)
                    if (foundRelatedConcept == null) {
                        //not found in cache search in database
                        String temp = relatedConceptBaseUri.substring(0, relatedConceptBaseUri.length() - 1);
                        relatedConceptVocabularyIdentifier = temp.substring(temp.lastIndexOf("/") + 1);
                        if (StringUtils.isNotEmpty(relatedConceptVocabularyIdentifier)) {
                            try {

                                VocabularyFolder foundVocabularyFolder = null;
                                //create vocabulary filter
                                VocabularyFilter vocabularyFilter = new VocabularyFilter();
                                vocabularyFilter.setIdentifier(relatedConceptVocabularyIdentifier);
                                vocabularyFilter.setWorkingCopy(false);
                                this.numberOfVocabularySearches++;
                                //first search for vocabularies, to find correct concept and to make searching faster for concepts
                                VocabularyResult vocabularyResult = this.vocabularyService.searchVocabularies(vocabularyFilter);
                                if (vocabularyResult != null) {
                                    for (VocabularyFolder vocabularyFolder : vocabularyResult.getList()) {
                                        //if it matches with base uri then we found it! this is an costly operation
                                        // but to satisfy consistency we need it.
                                        if (StringUtils.equals(relatedConceptBaseUri,
                                                VocabularyFolder.getBaseUri(vocabularyFolder))) {
                                            foundVocabularyFolder = vocabularyFolder;
                                            break;
                                        }
                                    }
                                }
                                //if a vocabulary not found don't go on!
                                if (foundVocabularyFolder != null) {
                                    VocabularyConceptFilter filter = new VocabularyConceptFilter();
                                    filter.setIdentifier(relatedConceptIdentifier);
                                    filter.setVocabularyFolderId(foundVocabularyFolder.getId());
                                    //search for concepts now
                                    VocabularyConceptResult results = this.vocabularyService.searchVocabularyConcepts(filter);
                                    this.numberOfSearches++;
                                    //if found more than one, how can system detect which one is searched for!
                                    if (results != null && results.getFullListSize() == 1) {
                                        foundRelatedConcept = results.getList().get(0);
                                        this.relatedConceptCache.put(relatedConceptUri, foundRelatedConcept);
                                    }
                                }
                            } catch (ServiceException e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        numberOfCacheFound++;
                    }

                    //either found in cache or in database
                    if (foundRelatedConcept != null) {
                        relatedConceptId = foundRelatedConcept.getId();
                    }
                }
            } else if (object instanceof Literal) {
                // it is literal
                elemLang = ((Literal) object).getLanguage();
            }

            if (!StringUtils.equals(attributeIdentifier, this.prevAttributeIdentifier)
                    || !StringUtils.equals(elemLang, prevLang)) {
                this.elementsOfConceptByLang =
                        VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang(dataElemIdentifier, elemLang,
                                this.lastFoundConcept.getElementAttributes());
            }
            this.prevLang = elemLang;
            this.prevAttributeIdentifier = attributeIdentifier;

            Map<String, Integer> attributePosition = this.attributePositions.get(conceptIdentifier);
            if (attributePosition == null) {
                attributePosition = new HashMap<String, Integer>();
                this.attributePositions.put(conceptIdentifier, attributePosition);
            }

            if (!attributePosition.containsKey(attributeIdentifier)) {
                attributePosition.put(attributeIdentifier, 0);
            }
            int index = attributePosition.get(attributeIdentifier);

            // if object is empty, user wants to delete
            if (StringUtils.isNotEmpty(elementValue)) {
                DataElement elem;
                if (index < this.elementsOfConceptByLang.size()) {
                    elem = this.elementsOfConceptByLang.get(index);
                } else {
                    elem = new DataElement();
                    this.elementsOfConcept.add(elem);
                    elem.setAttributeLanguage(elemLang);
                    elem.setIdentifier(attributeIdentifier);
                    elem.setId(this.bindedElementsIds.get(dataElemIdentifier));
                }

                if (relatedConceptId > 0) {
                    elem.setRelatedConceptIdentifier(relatedConceptIdentifier);
                    elem.setRelatedConceptId(relatedConceptId);
                    elem.setRelatedConceptVocabulary(relatedConceptVocabularyIdentifier);
                    elem.setRelatedConceptBaseURI(relatedConceptBaseUri);
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
            attributePosition.put(attributeIdentifier, ++index);
        }

        this.numberOfValidTriples++;
    } // end of method handleStatement

    @Override
    public void endRDF() throws RDFHandlerException {
        end = System.currentTimeMillis();
        this.logs.add("Valid (" + this.numberOfValidTriples + ") / Total (" + this.totalNumberOfTriples + ")");
        this.logs.add("Number of potential related concept: " + numberOfPotentialRelatedConcepts);
        this.logs.add("Number of vocabulary searches: " + numberOfVocabularySearches);
        this.logs.add("Number of search: " + numberOfSearches);
        this.logs.add("Time of handling (msecs): " + (end - start));
        this.logs.add("Found related concept cache count: " + this.relatedConceptCache.keySet().size());
        this.logs.add("Cache hit: " + numberOfCacheFound);
    } // end of method endRDF

    public List<String> getLogs() {
        return this.logs;
    } //end of method getLogs

} // end of class VocabularyRDFImportHandler
