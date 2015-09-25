package eionet.meta.exports.rdf;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.data.SiteCode;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

/**
 * Test VocabularyXmlWriter.
 */
public class VocabularyXmlWriterTest {

	/**
	 * Used instead of site prefix.
	 */
	private static final String BASE_URL = "http://test.tripledev.ee/datadict";

	@Test
	public void writeVocabularyXml() throws Exception {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		VocabularyXmlWriter writer = new VocabularyXmlWriter(outputStream);

		String rootContext = BASE_URL + "/vocabulary/folder/";
		String vocabularyContext = BASE_URL + "/vocabulary/folder/test/";
		String commonElemsUri = BASE_URL + "/property/";
		VocabularyFolder testVocabulary = prepareVocabularyFolder(VocabularyType.COMMON);
		List<VocabularyConcept> concepts = prepareVocabularyConcepts();

		writer.writeRDFXml(commonElemsUri, rootContext, vocabularyContext, testVocabulary, concepts, new ArrayList<RdfNamespace>());
		outputStream.close();
		String output = new String(outputStream.toByteArray(), "UTF-8");
    
		// test output
		Assert.assertTrue(StringUtils.contains(output, "<skos:Concept rdf:about=\"" + BASE_URL + "/vocabulary/folder/test/Id1\">"));
		Assert.assertTrue(StringUtils.contains(output, "<skos:notation>Notation1</skos:notation>"));
		Assert.assertTrue(StringUtils.contains(output, "<skos:prefLabel>Label1</skos:prefLabel>"));
		Assert.assertTrue(StringUtils.contains(output, "<skos:inScheme rdf:resource=\"" + BASE_URL + "/vocabulary/folder/test/\"/>"));

		Assert.assertTrue(StringUtils.contains(output, "<DataElemId1 rdf:resource=\"" + BASE_URL + "/vocabulary/folder/related/RelatedConcept1\"/>"));
		Assert.assertTrue(StringUtils
				.contains(output, "<DataElemId2 xml:lang=\"et\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">AttributeValue2</DataElemId2>"));

		// test if output is valid RDF
		Reader reader = new StringReader(output);
		RDFParser parser = new RDFXMLParser();
		parser.parse(reader, vocabularyContext);
		reader.close();
        }

	@Test
	public void writeSiteCodesXml() throws Exception {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		VocabularyXmlWriter writer = new VocabularyXmlWriter(outputStream);

		String rootContext = BASE_URL + "/vocabulary/folder/";
		String vocabularyContext = BASE_URL + "/vocabulary/folder/sitecodes/";
		String commonElemsUri = BASE_URL + "/datadict/property/";

		VocabularyFolder testVocabulary = prepareVocabularyFolder(VocabularyType.SITE_CODE);
		List<SiteCode> siteCodes = prepareSiteCodes();

		writer.writeRDFXml(commonElemsUri, rootContext, vocabularyContext, testVocabulary, siteCodes, new ArrayList<RdfNamespace>());
		outputStream.close();
		String output = new String(outputStream.toByteArray(), "UTF-8");

		// test output
		Assert.assertTrue(StringUtils.contains(output, "<skos:Concept rdf:about=\"" + BASE_URL + "/vocabulary/folder/sitecodes/1111\">"));
		Assert.assertTrue(StringUtils.contains(output, "<skos:notation>SiteCodeNotation1</skos:notation>"));
		Assert.assertTrue(StringUtils.contains(output, "<skos:prefLabel>SiteCodeLabel1</skos:prefLabel>"));
		Assert.assertTrue(StringUtils.contains(output, "<skos:inScheme rdf:resource=\"" + BASE_URL + "/vocabulary/folder/sitecodes/\"/>"));

		Assert.assertTrue(StringUtils.contains(output, "<dd:siteCode rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1111</dd:siteCode>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:siteName>SiteCodeLabel1</dd:siteName>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:status>ALLOCATED</dd:status>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:status>DELETED</dd:status>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:countryAllocated rdf:resource=\"http://rdfdata.eionet.europa.eu/eea/countries/EE\"/>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:yearCreated rdf:datatype=\"http://www.w3.org/2001/XMLSchema#gYear\">2009</dd:yearCreated>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:status>DELETED</dd:status>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:yearsDeleted>2011</dd:yearsDeleted>"));
		Assert.assertTrue(StringUtils.contains(output, "<dd:yearsDisappeared>2012</dd:yearsDisappeared>"));
		Assert.assertTrue(StringUtils.contains(output, "<rdf:type rdf:resource=\"http://dd.eionet.europa.eu/schema.rdf#SiteCode\"/>"));

		// test if output is valid RDF
		Reader reader = new StringReader(output);
		RDFParser parser = new RDFXMLParser();
		parser.parse(reader, vocabularyContext);
		reader.close();

	}

	private VocabularyFolder prepareVocabularyFolder(VocabularyType vocabularyType) {
		VocabularyFolder vocabulary = new VocabularyFolder();
		vocabulary.setLabel("Test vocabulary");
		vocabulary.setIdentifier("test");
		vocabulary.setType(vocabularyType);

		return vocabulary;
	}

	private List<VocabularyConcept> prepareVocabularyConcepts() {
		List<VocabularyConcept> concepts = new ArrayList<VocabularyConcept>();
		VocabularyConcept concept1 = new VocabularyConcept();
		concept1.setIdentifier("Id1");
		concept1.setNotation("Notation1");
		concept1.setLabel("Label1");
		concept1.setDefinition("Definition1");
		concepts.add(concept1);

		VocabularyConcept concept2 = new VocabularyConcept();
		concept2.setIdentifier("Id2");
		concept2.setNotation("Notation2");
		concept2.setLabel("Label2");
		concept2.setDefinition("Definition2");

		List<List<DataElement>> elements = new ArrayList<List<DataElement>>();
		List<DataElement> elems = new ArrayList<DataElement>();
		DataElement elem1 = new DataElement();
		elem1.setIdentifier("DataElemId1");
		elem1.setRelatedConceptId(1);
		elem1.setRelatedConceptIdentifier("RelatedConcept1");
		elem1.setRelatedConceptBaseURI(BASE_URL + "/vocabulary/folder/related/");
		elems.add(elem1);

		DataElement elem2 = new DataElement();
		elem2.setIdentifier("DataElemId2");
		elem2.setAttributeValue("AttributeValue2");
		elem2.setAttributeLanguage("et");
		Map<String, List<String>> elemAttributeValues = new HashMap<String, List<String>>();
		elemAttributeValues.put("Datatype", Arrays.asList(new String[] {"int"}));
		elem2.setElemAttributeValues(elemAttributeValues);
		elems.add(elem2);
		elements.add(elems);

		concept2.setElementAttributes(elements);
		concepts.add(concept2);

		return concepts;
	}

	private List<SiteCode> prepareSiteCodes() {
		List<SiteCode> siteCodes = new ArrayList<SiteCode>();
		SiteCode siteCode1 = new SiteCode();
		siteCode1.setIdentifier("1111");
		siteCode1.setNotation("SiteCodeNotation1");
		siteCode1.setLabel("SiteCodeLabel1");
		siteCode1.setDefinition("SiteCodeDefinition1");
		siteCode1.setSiteCodeStatus(SiteCodeStatus.ALLOCATED);
		siteCode1.setCountryCode("EE");
		Calendar created = Calendar.getInstance();
		created.set(Calendar.YEAR, 2009);
		siteCode1.setDateCreated(created.getTime());
		siteCodes.add(siteCode1);

		SiteCode siteCode2 = new SiteCode();
		siteCode2.setIdentifier("2222");
		siteCode2.setNotation("SiteCodeNotation2");
		siteCode2.setLabel("SiteCodeLabel2");
		siteCode2.setDefinition("SiteCodeDefinition2");
		siteCode2.setSiteCodeStatus(SiteCodeStatus.DELETED);
		siteCode2.setCountryCode("FR");
		siteCode2.setYearsDeleted("2011");
		siteCode2.setYearsDisappeared("2012");
		siteCodes.add(siteCode2);

		return siteCodes;
	}
}
