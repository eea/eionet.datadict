package eionet.meta.exports.rdf;

import eionet.util.Props;
import eionet.util.PropsIF;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.web.action.FolderActionBean;

/**
 * test for vocabulary RDF output.
 *
 * @author kaido
 */
public class VocabularyRdfTestIT extends DDDatabaseTestCase   {
    /**
     * Used instead of site prefix.
     */
    private static final String BASE_URL = "http://test.tripledev.ee/datadict";

    /**
     * test if RDF output contains collection resource for a folder.
     * @throws Exception if test fails
     */
    @Test
    public void testFolderRdfContainsCollection() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, FolderActionBean.class);
        trip.addParameter("folder.identifier", "wise");
        trip.execute("rdf");

        String base = Props.getRequiredProperty(PropsIF.DD_URL);

        String dcTypeCollection = "<dctype:Collection rdf:about=\"" + base + "/vocabulary/wise/\">";

        String skosNotation = "<skos:notation>wise</skos:notation>";

        String isPartOf = "<dcterms:isPartOf rdf:resource=\"" + base + "/vocabulary/wise/\"/>";
        String hasPart = "<dcterms:hasPart rdf:resource=\"" + base + "/vocabulary/wise/BWClosed/\"/>";

        String conceptScheme = "<skos:ConceptScheme rdf:about=\"" + BASE_URL +  "/vocabulary/wise/BWClosed/\">";
        String conceptNotation = "<skos:notation>BWClosed</skos:notation>";

        String output = trip.getOutputString();

        Assert.assertTrue(StringUtils.contains(output, dcTypeCollection));
        Assert.assertTrue(StringUtils.contains(output, conceptScheme));
        Assert.assertTrue(StringUtils.contains(output, hasPart));
        Assert.assertTrue(StringUtils.contains(output, isPartOf));

        Assert.assertTrue(StringUtils.contains(output, skosNotation));
        Assert.assertTrue(StringUtils.contains(output, conceptNotation));

        String ddSchema = "xmlns=\"" + base + "/property/\"";
        String skosSchema = "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"";

        String anotherCodeElem = "<AnotherCode>";


        assertTrue(StringUtils.countMatches(output, ddSchema) == 1);
        assertTrue(StringUtils.countMatches(output, skosSchema) == 1);
        assertTrue(StringUtils.countMatches(output, anotherCodeElem) == 4);
    }

    /**
     * tests referential element.
     * @throws Exception if fail
     */
    @Test
    public void testFolderRdfReference() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, FolderActionBean.class);
        trip.addParameter("folder.identifier", "wise");
        trip.execute("rdf");

        String output = trip.getOutputString();
        // Reference as plain string value
        assertTrue(StringUtils.contains(output, "<skos:relatedMatch rdf:resource=\"http://en.wikipedia.org/wiki/Semantic%20Web\"/>"));
        // Reference via RELATED_CONCEPT_ID
        assertTrue(StringUtils.contains(output, "<skos:relatedMatch rdf:resource=\"" + BASE_URL + "/vocabulary/wise/BWClosed/YP\"/>"));
        // Localref via RELATED_CONCEPT_ID
        assertTrue(StringUtils.contains(output, "<skos:related rdf:resource=\"" + BASE_URL + "/vocabulary/wise/BWClosed/YT\"/>"));
    }


    @Override
    protected String getSeedFilename() {
        return "seed-vocabularyrdf.xml";
    }
}
