package eionet.meta.exports.schema;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;

/**
 * @author jaanus
 *
 *         To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class ElmsContainerSchema extends TblSchema {

    /**
     * List of table element schema objects.
     */
    private List<ElmSchema> elemSchemas = new ArrayList<ElmSchema>();

    /*
     *
     */
    public ElmsContainerSchema(DDSearchEngine searchEngine, PrintWriter writer) {
        super(searchEngine, writer);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.exports.schema.TblSchema#writeContent(eionet.meta.DsTable)
     */
    protected void writeContent(DsTable dsTable) throws Exception {

        int cness = 0;
        String cNamespaceID = dsTable.getNamespace();
        Vector elms = dsTable.getElements();

        // write elements into PrintWriter
        for (int i = 0; elms != null && i < elms.size(); i++) {

            DataElement elm = (DataElement) elms.get(i);
            ElmSchema elmSchema = new ElmSchema(searchEngine, getWriter());

            if (i == 0) {
                if (elms.size() > 1)
                    cness = FIRST_IN_CONTAINER;
                else
                    cness = FIRST_AND_LAST_IN_CONTAINER;
            } else if ((i + 1) == elms.size())
                cness = LAST_IN_CONTAINER;
            else
                cness = IN_CONTAINER;

            elmSchema.setContainerness(cness);
            elmSchema.setContainerNamespaceID(cNamespaceID);
            elmSchema.setAppContext(appContext);
            elmSchema.write(elm.getID());
            elmSchema.addString(lineTerminator);
            elemSchemas.add(elmSchema);
        }
        // collect all namespaces and add it into the first element header
        if (elemSchemas.size() > 1) {
            ElmSchema firstElmSchema = elemSchemas.get(0);
            for (int i = 1; i < elemSchemas.size(); i++) {
                firstElmSchema.addNamespaces(elemSchemas.get(i).getNamespaces());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.exports.schema.SchemaIF#flush()
     */
    public void flush() throws Exception {
        if (elemSchemas != null) {
            for (ElmSchema elmSchema : elemSchemas) {
                elmSchema.flush();
            }
        }
    }
}
