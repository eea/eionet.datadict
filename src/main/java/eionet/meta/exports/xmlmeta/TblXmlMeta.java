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
 * The Original Code is Web Dashboards Service
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency (EEA).  Portions created by European Dynamics (ED) company are
 * Copyright (C) by European Environment Agency.  All Rights Reserved.
 *
 * Contributors(s):
 *    Original code: Dusko Kolundzija (ED)
 *                   Istvan Alfeldi (ED)
 */

package eionet.meta.exports.xmlmeta;

import java.io.PrintWriter;
import java.util.Hashtable;

import eionet.meta.DDSearchEngine;
import eionet.meta.DsTable;
import eionet.util.Util;


public class TblXmlMeta extends XmlMeta {

    public TblXmlMeta(DDSearchEngine searchEngine, PrintWriter writer) {
        super(searchEngine, writer);
    }

    public void write(String tblID) throws Exception {

        if (Util.isEmpty(tblID))
            throw new Exception("Table ID not specified!");

        // Get the table object.
        DsTable tbl = searchEngine.getDatasetTable(tblID);
        if (tbl == null)
            throw new Exception("Table not found!");

        // get data elements (this will set all the simple attributes of
        // elements)
        tbl.setElements(searchEngine.getDataElements(null, null, null, null,
                tblID));

        write(tbl);
    }

    /**
     * Write a schema for a given object.
     */
    private void write(DsTable tbl) throws Exception {

        writeTable(tbl);
    }

    protected String getSchemaLocation(String nsID, String id) {
        StringBuffer buf = new StringBuffer().append(this.appContext).append(
                "GetSchema?id=TBL").append(id);

        return buf.toString();
    }

    protected void setLeads() {
        leads = new Hashtable();
        leads.put("row", "\t");
        leads.put("elm", "\t\t");
    }

}
