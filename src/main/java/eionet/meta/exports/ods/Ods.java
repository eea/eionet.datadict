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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */
package eionet.meta.exports.ods;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.exports.ods.tags.NumberStyle;
import eionet.meta.exports.ods.tags.Style;
import eionet.meta.exports.ods.tags.Table;

/**
 *
 * @author jaanus
 */
public abstract class Ods {

    /**
     * ODS extension.
     */
    public static final String ODS_EXTENSION = "ods";
    /**
     * Integer style name.
     */
    public static final String INTEGER_STYLE_NAME = "ce2";
    /**
     * Default style name.
     */
    public static final String DEFAULT_STYLE_NAME = "Default";
    /**
     * ODF file name.
     */
    public static final String ODS_FILE_NAME = "template.ods";
    /**
     * Content file name.
     */
    public static final String CONTENT_FILE_NAME = "content.xml";
    /**
     * Meta file name.
     */
    public static final String META_FILE_NAME = "meta.xml";
    /**
     * Buffer size.
     */
    public static final int BUF_SIZE = 1024;

    /**
     * Data Dictionary search engine.
     */
    protected DDSearchEngine searchEngine = null;
    /**
     * Output file name.
     */
    protected String finalFileName = null;
    /**
     * Schema URL trailer.
     */
    protected String schemaURLTrailer = null;
    /**
     * Number of styles.
     */
    protected Vector numberStyles = null;
    /**
     * Styles vector.
     */
    protected Vector styles = null;
    /**
     * Tables vector.
     */
    protected Vector tables = null;
    /**
     * Working folder path.
     */
    private String workingFolderPath = null;
    /**
     * Schema URL base.
     */
    private String schemaURLBase = null;

    /**
     *
     * @return table count
     */
    protected final int getTableCount() {
        return tables == null ? 0 : tables.size();
    }

    /**
     *
     * @return total column count
     */
    protected final int getTotalColumnCount() {

        int count = 0;
        for (int i = 0; tables != null && i < tables.size(); i++) {
            Table table = (Table) tables.get(i);
            Vector cols = table.getTableColumns();
            if (cols != null)
                count = count + cols.size();
        }

        return count;
    }

    /**
     * Adds a number style.
     *
     * @param numberStyle
     *            number style to be added
     */
    protected void addNumberStyle(NumberStyle numberStyle) {

        if (numberStyles == null)
            numberStyles = new Vector();

        numberStyles.add(numberStyle);
    }

    /**
     * Adds a style.
     *
     * @param style
     *            style to be added
     */
    protected void addStyle(Style style) {

        if (styles == null)
            styles = new Vector();

        styles.add(style);
    }

    /**
     * Adds a table.
     *
     * @param table
     *            table to be added
     */
    protected void addTable(Table table) {

        if (tables == null)
            tables = new Vector();

        tables.add(table);
    }

    /**
     * Prepares for table.
     *
     * @param tbl
     *            table
     * @throws java.lang.Exception
     *             when operation fails
     */
    protected void prepareTbl(DsTable tbl) throws Exception {

        Table tableTag = new Table();
        tableTag.setTableName(tbl.getIdentifier());
        tableTag.setSchemaURLTrailer("TBL" + tbl.getID());

        Vector elms = searchEngine.getDataElements(null, null, null, null, tbl.getID());
        for (int i = 0; elms != null && i < elms.size(); i++) {
            DataElement elm = (DataElement) elms.get(i);
            String defaultCellStyleName = getDefaultCellStyleName(elm);
            tableTag.addTableColumn(defaultCellStyleName);
            tableTag.addColumnHeader(elm.getIdentifier());
        }

        addTable(tableTag);
    }

    /**
     * Returns default cell style name.
     *
     * @param elm
     *            data element
     * @return cell style name
     */
    protected String getDefaultCellStyleName(DataElement elm) {

        String defaultCellStyleName = DEFAULT_STYLE_NAME;
        String elmDataType = elm.getAttributeValueByShortName("Datatype");
        if (elmDataType != null) {
            if (elmDataType.equals("integer")) {
                defaultCellStyleName = INTEGER_STYLE_NAME;
            } else if (elmDataType.equals("decimal")) {

                String decimalPrecision = elm.getAttributeValueByShortName("DecimalPrecision");
                if (decimalPrecision != null && decimalPrecision.length() > 0) {
                    try {
                        Integer.parseInt(decimalPrecision);

                        int count = numberStyles == null ? 0 : numberStyles.size();
                        String numberStyleName = "N" + String.valueOf(2 + count);
                        NumberStyle numberStyle = new NumberStyle();
                        numberStyle.setStyleName(numberStyleName);
                        numberStyle.setMinIntegerDigits("1");
                        numberStyle.setDecimalPlaces(decimalPrecision);

                        count = styles == null ? 0 : styles.size();
                        defaultCellStyleName = "ce" + String.valueOf(3 + count);
                        Style style = new Style();
                        style.setStyleName(defaultCellStyleName);
                        style.setDataStyleName(numberStyleName);

                        addNumberStyle(numberStyle);
                        addStyle(style);
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
        }

        return defaultCellStyleName;
    }

    /**
     * Writes content into string.
     *
     * @param intoStr
     *            string to be updated
     * @return updated value
     */
    private String writeContentInto(String intoStr) {

        String str = new String(intoStr);

        for (int i = 0; numberStyles != null && i < numberStyles.size(); i++) {
            NumberStyle numberStyle = (NumberStyle) numberStyles.get(i);
            str = numberStyle.writeInto(str);
        }

        for (int i = 0; styles != null && i < styles.size(); i++) {
            Style style = (Style) styles.get(i);
            str = style.writeInto(str);
        }

        for (int i = 0; tables != null && i < tables.size(); i++) {
            Table table = (Table) tables.get(i);
            str = table.writeContentInto(str);
        }

        return str;
    }

    /**
     * Writes meto into string.
     *
     * @param intoStr
     *            string to be updated
     * @return updated value
     */
    private String writeMetaInto(String intoStr) {

        if (intoStr == null || intoStr.length() == 0)
            return intoStr;

        String officeMeta = new String("</office:meta>");
        int index = intoStr.indexOf(officeMeta);
        if (index < 0)
            return intoStr;

        StringBuffer buf = new StringBuffer();
        buf.append(intoStr.substring(0, index));

        // write creation date & DublinCore date
        String date = Ods.getDate(System.currentTimeMillis());
        buf.append("<meta:creation-date>");
        buf.append(date);
        buf.append("</meta:creation-date>");
        buf.append("<dc:date>");
        buf.append(date);
        buf.append("</dc:date>");

        // write schemaURL of this ods
        if (schemaURLTrailer != null && schemaURLBase != null) {
            buf.append("<meta:user-defined meta:name=\"schema-url\">");
            buf.append(schemaURLBase + schemaURLTrailer);
            buf.append("</meta:user-defined>");
        }

        // if this is DstOds, write schemaURL of each table
        int tableCount = getTableCount();
        if (this instanceof DstOds && tableCount > 0) {
            StringBuffer buf1 = new StringBuffer();
            for (int i = 0; i < tables.size(); i++) {
                Table table = (Table) tables.get(i);
                String tableName = table.getTableName();
                String tableSchemaURLTrailer = table.getSchemaURLTrailer();
                if (tableName != null && tableSchemaURLTrailer != null && schemaURLBase != null) {
                    if (i > 0)
                        buf1.append(";");
                    buf1.append("tableName=");
                    buf1.append(tableName);
                    buf1.append(",tableSchemaURL=");
                    buf1.append(schemaURLBase + tableSchemaURLTrailer);
                }
            }

            if (buf1.length() > 0) {
                buf.append("<meta:user-defined meta:name=\"table-schema-urls\">");
                buf.append(buf1.toString());
                buf.append("</meta:user-defined>");
            }
        }

        // write document statistics
        int totalColumnCount = getTotalColumnCount();
        if (tableCount > 0 || totalColumnCount > 0) {

            buf.append("<meta:document-statistic");
            if (tableCount > 0) {
                buf.append(" meta:table-count=\"");
                buf.append(tableCount);
                buf.append("\"");
            }

            if (totalColumnCount > 0) {
                buf.append(" meta:cell-count=\"");
                buf.append(totalColumnCount);
                buf.append("\"");
            }
            buf.append("/>");
        }

        buf.append(intoStr.substring(index));

        return buf.toString();
    }

    /**
     * Sets working folder path.
     *
     * @param folderPath
     *            new folder path
     */
    public void setWorkingFolderPath(String folderPath) {
        this.workingFolderPath = folderPath;
        if (!this.workingFolderPath.endsWith(File.separator))
            this.workingFolderPath = this.workingFolderPath + File.separator;
    }

    /**
     * Reads file content into string.
     *
     * @param file
     *            file to be read
     * @return file content
     * @throws java.lang.Exception
     *             if operation fails
     */
    private String fileToString(File file) throws Exception {

        String result = null;

        int i = 0;
        byte[] buf = new byte[BUF_SIZE];
        FileInputStream in = null;
        ByteArrayOutputStream out = null;

        try {
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            while ((i = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, i);
            }

            out.flush();
            result = out.toString();
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }

        return result;
    }

    /**
     * Returns content file as string.
     *
     * @return file content as string
     * @throws java.lang.Exception
     *             when operation fails
     */
    private String contentFileToString() throws Exception {
        return fileToString(new File(workingFolderPath + CONTENT_FILE_NAME));
    }

    /**
     * Returns meta file as string.
     *
     * @return file content as string
     * @throws java.lang.Exception
     *             when operation fails
     */
    private String metaFileToString() throws Exception {

        return fileToString(new File(workingFolderPath + META_FILE_NAME));
    }

    /**
     * Writes string value into given file.
     *
     * @param str
     *            value to be written
     * @param file
     *            file to be updated
     * @throws Exception
     *             if operation fails
     */
    private void stringToFile(String str, File file) throws Exception {

        if (file.exists())
            file.delete();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(str.getBytes());
            out.flush();
        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * Writes string value into content file.
     *
     * @param str
     *            value to be written
     * @throws Exception
     *             if operation fails
     */
    private void stringToContentFile(String str) throws Exception {

        stringToFile(str, new File(workingFolderPath + CONTENT_FILE_NAME));
    }

    /**
     * Writes string value into meta file.
     *
     * @param str
     *            value to be written
     * @throws Exception
     *             if operation fails
     */
    private void stringToMetaFile(String str) throws Exception {

        stringToFile(str, new File(workingFolderPath + META_FILE_NAME));
    }

    /**
     * Writes content into file.
     *
     * @throws Exception
     *             if operation fails
     */
    private void writeContentIntoFile() throws Exception {
        String str = contentFileToString();
        str = writeContentInto(str);
        stringToContentFile(str);
    }

    /**
     * Writes meta into file.
     *
     * @throws Exception
     *             if operation fails
     */
    private void writeMetaIntoFile() throws Exception {

        String str = metaFileToString();
        str = writeMetaInto(str);
        stringToMetaFile(str);
    }

    /**
     * Zips file.
     *
     * @param fileToZip
     *            file to zip (full path)
     * @param fileName
     *            file name
     * @throws java.lang.Exception
     *             if operation fails.
     */
    private void zip(String fileToZip, String fileName) throws Exception {
        // get source file
        File src = new File(workingFolderPath + ODS_FILE_NAME);
        ZipFile zipFile = new ZipFile(src);
        // create temp file for output
        File tempDst = new File(workingFolderPath + ODS_FILE_NAME + ".zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempDst));
        // iterate on each entry in zip file
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            BufferedInputStream bis;
            if (StringUtils.equals(fileName, zipEntry.getName())) {
                bis = new BufferedInputStream(new FileInputStream(new File(fileToZip)));
            } else {
                bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
            }
            ZipEntry ze = new ZipEntry(zipEntry.getName());
            zos.putNextEntry(ze);

            while (bis.available() > 0) {
                zos.write(bis.read());
            }
            zos.closeEntry();
            bis.close();
        }
        zos.finish();
        zos.close();
        zipFile.close();
        // rename file
        src.delete();
        tempDst.renameTo(src);
    } // end of method zip

    /**
     * Zips content.
     *
     * @throws Exception
     *             if operation fails
     */
    private void zipContent() throws Exception {
        zip(workingFolderPath + CONTENT_FILE_NAME, CONTENT_FILE_NAME);
    }

    /**
     * Zips meta.
     *
     * @throws Exception
     *             if operation fails
     */
    private void zipMeta() throws Exception {
        zip(workingFolderPath + META_FILE_NAME, META_FILE_NAME);
    }

    /**
     * Processes content.
     *
     * @throws Exception
     *             if operation fails
     */
    public void processContent() throws Exception {
        writeContentIntoFile();
        zipContent();
    }

    /**
     * Processes meta.
     *
     * @throws Exception
     *             if operation fails
     */
    public void processMeta() throws Exception {
        writeMetaIntoFile();
        zipMeta();
    }

    public String getFinalFileName() {
        return finalFileName;
    }

    public String getWorkingFolderPath() {
        return workingFolderPath;
    }

    public void setSchemaURLBase(String schemaURLBase) {
        this.schemaURLBase = schemaURLBase;
    }

    /**
     * Returns date.
     *
     * @param timestamp
     *            time stamp
     *
     * @return date as a string
     */
    public static String getDate(long timestamp) {

        Date date = new Date(timestamp);
        String year = String.valueOf(1900 + date.getYear());
        String month = String.valueOf(date.getMonth() + 1);
        month = (month.length() < 2) ? ("0" + month) : month;
        String day = String.valueOf(date.getDate());
        day = (day.length() < 2) ? ("0" + day) : day;
        String hours = String.valueOf(date.getHours());
        hours = (hours.length() < 2) ? ("0" + hours) : hours;
        String minutes = String.valueOf(date.getMinutes());
        minutes = (minutes.length() < 2) ? ("0" + minutes) : minutes;
        String seconds = String.valueOf(date.getSeconds());
        seconds = (seconds.length() < 2) ? ("0" + seconds) : seconds;

        String time = year;
        time = time + "-" + month;
        time = time + "-" + day;
        time = time + "T" + hours;
        time = time + ":" + minutes;
        time = time + ":" + seconds;

        return time;
    }
}
