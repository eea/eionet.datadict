package eionet.meta.exports.rdf;

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.web.action.VocabularyFolderActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Created by kaido on 11.03.14.
 */
public class InspireCodelistTestIT extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-inspirecodelistrdf.xml";
    }


    @Test
    public void testInspireCodelist() throws  Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
        trip.addParameter("vocabularyFolder.identifier", "BWClosed");
        trip.addParameter("vocabularyFolder.folderName", "wise");

        trip.execute("codelist");
        String output = trip.getOutputString();

        assertTrue("Output has to contain definition elem <definition xml:lang=\"en\">Bathing water is temporarily closed for a part of one season or the entire season, but not yet permanently closed</definition>",
                StringUtils.contains(output, "<definition xml:lang=\"en\">Bathing water is temporarily closed for a part of one season or the entire season, but not yet permanently closed</definition>"));

        assertTrue("Output has to contain label elem <label xml:lang=\"en\">Bathing Water Closed status</label>",
                StringUtils.contains(output, "<label xml:lang=\"en\">Bathing Water Closed status</label>"));

        assertTrue("Output has to contain 3 concepts",
                StringUtils.countMatches(output, "<value") == 3);

    }
}
