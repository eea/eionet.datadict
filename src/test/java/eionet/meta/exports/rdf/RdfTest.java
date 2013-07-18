package eionet.meta.exports.rdf;

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.junit.Test;

import eionet.meta.DDSearchEngine;
import eionet.meta.DsTable;

/**
 * Purpose to test the RDF output generated for by the {@link Rdf}.
 *
 * @author jaanus
 */
public class RdfTest  {

    /**
     * Purpose is to test the RDF generated about a table (not its elements).
     * @throws Exception Any sort of exception thrown by the code under test.
     */
    @Test
    public void testRdfAboutTable() throws Exception {

        DsTable mockedTable = new DsTable("111", "222", "tableShortName");
        mockedTable.setIdentifier("tableIdentifier");
        mockedTable.setDstIdentifier("datasetIdentifier");

        Rdf rdfGen = new Rdf(mockedTable.getID(), Rdf.TABLE_TYPE, new DDSearchEngineMock(mockedTable, null));
        StringWriter writer = new StringWriter();
        rdfGen.write(writer);
        String stringOutput = writer.toString();

        String expectedString = "<dcterms:isVersionOf rdf:resource=\"@ref_url@\"";
        expectedString = expectedString.replace("@ref_url@", mockedTable.getReferenceURL());
        assertTrue("Expected <dcterms:isVersionOf> pointing to reference URL", stringOutput.contains(expectedString));
    }

    /**
     * A mock of {@link DDSearchEngine} used for mocking RDF generation.
     *
     * @author jaanus
     */
    public static class DDSearchEngineMock extends DDSearchEngine {

        /** Mocked table for which we expect RDF generation. */
        private DsTable mockedTable;

        /**
         *
         * Default constructor.
         * @param mockedTable Mocked table for which we expect RDF generation.
         * @param conn Passed to {@link DDSearchEngine#DDSearchEngine(Connection)}
         */
        public DDSearchEngineMock(DsTable mockedTable, Connection conn) {
            super(conn);
            this.mockedTable = mockedTable;
        }

        /*
         * (non-Javadoc)
         * @see eionet.meta.DDSearchEngine#initSpringContext()
         */
        @Override
        protected void initSpringContext() {
            // We don't need no Spring context for this unit test.
        }

        /*
         * (non-Javadoc)
         * @see eionet.meta.DDSearchEngine#getDatasetTable(java.lang.String)
         */
        @Override
        public DsTable getDatasetTable(String tableID) throws SQLException {
            return mockedTable;
        }

        /*
         * (non-Javadoc)
         * @see eionet.meta.DDSearchEngine#getDataElements(java.util.Vector, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public Vector getDataElements(Vector unUsed1, String unUsed2, String unUsed3, String unUsed4, String tableID)
                throws SQLException {
            return new Vector();
        }
    }
}
