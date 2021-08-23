package eionet.meta.exports.rdf;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.data.SiteCode;
import eionet.util.StringEncoder;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test VocabularyXmlWriter.
 */

@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class VocabularyXmlWriterTestIT {

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

		concepts.add(concept2);

		return concepts;
	}
}
