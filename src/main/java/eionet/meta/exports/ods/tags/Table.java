/*
 * Created on 4.05.2006
 */
package eionet.meta.exports.ods.tags;

import java.util.Vector;

/**
 *
 * @author jaanus
 */
public class Table {

    /** */
    private String tableName = null;
    private String schemaURLTrailer = null;
    private Vector tableColumns = null;
    private Vector columnHeaders = null;

    /**
     *
     * @param tabelName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     *
     * @return
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     *
     * @param defaultCellStyleName
     */
    public void addTableColumn(String defaultCellStyleName) {

        if (tableColumns == null)
            tableColumns = new Vector();

        tableColumns.add(defaultCellStyleName);
    }

    /**
     *
     * @param headerText
     */
    public void addColumnHeader(String headerText) {

        if (columnHeaders == null)
            columnHeaders = new Vector();

        columnHeaders.add(headerText);
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append("<table:table table:name=\"");
        buf.append(tableName);
        buf.append("\" table:style-name=\"ta1\" table:print=\"false\">");

        // add columns
        for (int i = 0; tableColumns != null && i<tableColumns.size(); i++) {
            String defaultCellStyleName = (String) tableColumns.get(i);
            buf.append(
                "<table:table-column table:style-name=\"co1\" table:default-cell-style-name=\"");
            buf.append(defaultCellStyleName);
            buf.append("\"/>");
        }

        buf.append("<table:table-column table:style-name=\"co1\" ");
        buf.append("table:number-columns-repeated=\"253\" ");
        buf.append("table:default-cell-style-name=\"Default\"/>");
        buf.append("<table:table-row table:style-name=\"ro1\">");

        // add column headers
        for (int i = 0; columnHeaders != null && i<columnHeaders.size(); i++) {
            String headerText = (String) columnHeaders.get(i);
            buf.append("<table:table-cell table:style-name=\"ce1\" office:value-type=\"string\">");
            buf.append("<text:p>");
            buf.append(headerText);
            buf.append("</text:p>");
            buf.append("</table:table-cell>");
        }

        buf.append("<table:table-cell table:style-name=\"ce1\" ");
        buf.append("table:number-columns-repeated=\"253\"/>");
        buf.append("</table:table-row>");
        buf.append("<table:table-row table:style-name=\"ro1\" ");
        buf.append("table:number-rows-repeated=\"65534\">");
        buf.append("<table:table-cell table:number-columns-repeated=\"256\"/>");
        buf.append("</table:table-row>");
        buf.append("<table:table-row table:style-name=\"ro1\">");
        buf.append("<table:table-cell table:number-columns-repeated=\"256\"/>");
        buf.append("</table:table-row>");
        buf.append("</table:table>");

        return buf.toString();
    }

    /**
     *
     * @param intoStr
     * @return
     */
    public String writeContentInto(String intoStr) {

        if (intoStr == null || intoStr.length() == 0)
            return intoStr;

        String officeSpreadsheet = new String("</office:spreadsheet>");
        int i = intoStr.indexOf(officeSpreadsheet);
        if (i<0)
            return intoStr;

        StringBuffer buf = new StringBuffer();
        buf.append(intoStr.substring(0, i));
        buf.append(this.toString());
        buf.append(intoStr.substring(i));

        return buf.toString();
    }

    /**
     *
     * @return
     */
    public Vector getTableColumns() {
        return tableColumns;
    }

    /**
     *
     * @return
     */
    public String getSchemaURLTrailer() {
        return schemaURLTrailer;
    }

    /**
     *
     * @param schemaURLTrailer
     */
    public void setSchemaURLTrailer(String schemaURLTrailer) {
        this.schemaURLTrailer = schemaURLTrailer;
    }
}
