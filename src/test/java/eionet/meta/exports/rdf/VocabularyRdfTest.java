package eionet.meta.exports.rdf;

import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.web.action.FolderActionBean;

/**
 * test for vocabulary RDF output.
 *
 * @author kaido
 */
public class VocabularyRdfTest extends DDDatabaseTestCase   {


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

        String url = Props.getProperty(PropsIF.DD_URL);
        String dcTypeCollection = "<dctype:Collection rdf:about=\"" + url + "/vocabulary/wise/\">";

        String skosNotation = "<skos:notation>wise</skos:notation>";

        String isPartOf = "<dcterms:isPartOf rdf:resource=\"" + url + "/vocabulary/wise/\"/>";
        String hasPart = "<dcterms:hasPart rdf:resource=\"" + url + "/vocabulary/wise/BWClosed/\"/>";

        String conceptScheme = "<skos:ConceptScheme rdf:about=\"" + url +  "/vocabulary/wise/BWClosed/\">";
        String conceptNotation = "<skos:notation>BWClosed</skos:notation>";

        String output = trip.getOutputString();

        Assert.assertTrue(StringUtils.contains(output, dcTypeCollection));
        Assert.assertTrue(StringUtils.contains(output, conceptScheme));
        Assert.assertTrue(StringUtils.contains(output, hasPart));
        Assert.assertTrue(StringUtils.contains(output, isPartOf));

        Assert.assertTrue(StringUtils.contains(output, skosNotation));
        Assert.assertTrue(StringUtils.contains(output, conceptNotation));

        String ddSchema = "xmlns=\"" + url + "/property/\"";
        String skosSchema = "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"";

        String anotherCodeElem = "<AnotherCode>";


        assertTrue(StringUtils.countMatches(output, ddSchema) == 1);
        assertTrue(StringUtils.countMatches(output, skosSchema) == 1);
        assertTrue(StringUtils.countMatches(output, anotherCodeElem) == 2);
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
        assertTrue(StringUtils.contains(output, "<skos:relatedMatch rdf:resource=\"http://en.wikipedia.org/wiki/Semantic_Web\"/>"));
    }


    @Override
    protected String getSeedFilename() {
        return "seed-vocabularyrdf.xml";
    }
}
