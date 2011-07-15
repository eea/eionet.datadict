/*
 * Created on Feb 13, 2006
 */
package eionet.meta.exports.mdb;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import eionet.meta.DDSearchEngine;
import eionet.meta.DsTable;

/**
 * @author jaanus
 *
 * Takes an input MSAccess file and makes sure it has got the GDEM-related metadata in there.
 */
public class MdbGDEM {

    /** */
public static final String METADATA_TABLENAME = "Metadata_for_GDEM_(NOT_TO_BE_DELETED)";
    //public static final String METADATA_TABLENAME = "Metadata_for_GDEM";
    public static final String[] METADATA_COLS = {"TableID", "SchemaURL", "DatasetSchemaURL"};

    /** */
    private File file = null;
    private DDSearchEngine searchEngine = null;
    private String tblSchemaURL = null;
    private String dstSchemaURL = null;
    private String dstID = null;

    /*
     *
     */
    public MdbGDEM(File file, Connection conn, String dstID) {
        this(file, new DDSearchEngine(conn), dstID);
    }

    /*
     *
     */
    public MdbGDEM(File file, DDSearchEngine searchEngine, String dstID) {
        this.file = file;
        this.searchEngine = searchEngine;
        this.dstID = dstID;
    }

    /*
     *
     */
    public void process() throws Exception {

//      Database db = null;
//      try {
//          db = Database.open(file);
//          if (db==null) throw new NullPointerException();
//
////            Set tableNames = db.getTableNames();
////            if (tableNames!=null && tableNames.contains(METADATA_TABLENAME)) return;
//
//          Object[] cols = new Object[3];
//
//          Column col1 = new Column();
//          col1.setName("a");
//          col1.setSQLType(Types.VARCHAR);
//          cols[0] = col1;
//
//          Column col2 = new Column();
//          col2.setName("aa");
//          col2.setSQLType(Types.VARCHAR);
//          cols[1] = col2;
//
//          Column col3 = new Column();
//          col3.setName("aaa");
//          col3.setSQLType(Types.VARCHAR);
//          cols[2] = col3;
//
////            for (int i=0; i<METADATA_COLS.length; i++) {
////                Column col = new Column();
////                col.setName(METADATA_COLS[i]);
////                col.setSQLType(Mdb.DEFAULT_MDB_TYPE);
////                cols.add(col);
////            }
//
//          db.createTable(METADATA_TABLENAME, Arrays.asList(cols));
////            Table table = db.getTable(METADATA_TABLENAME);
////            if (table==null) throw new NullPointerException();
//
//          //addRows(table);
//      }
//      finally {
//          if (db!=null) db.close();
//      }
    }

    /*
     *
     */
    private void addRows(Table table) throws Exception {

        String dstID = null;
        Vector ddTables = searchEngine.getDatasetTables(dstID, true);
        for (int i=0; ddTables!=null && i<ddTables.size(); i++) {

            DsTable ddTable = (DsTable)ddTables.get(i);

            Object[] row = new Object[METADATA_COLS.length];
            row[0] = ddTable.getIdentifier();
            row[1] = tblSchemaURL + ddTable.getID();
            row[2] = dstSchemaURL + dstID;

            table.addRow(row);
        }
    }

    /*
     *
     */
    private void printRows() throws Exception {

        Database db = Database.open(file);
        if (db==null) throw new NullPointerException();

        Table table = db.getTable("Table1");
        if (table==null) throw new NullPointerException();

        List cols = table.getColumns();
        for (int i=0; i<cols.size(); i++) {
            Column col = (Column)cols.get(i);
            short nr = col.getColumnNumber();
            short len = col.getLength();
            byte prec = col.getPrecision();
            byte scale = col.getScale();
            String name = col.getName();
        }

        db.close();
    }

    /*
     *
     */
    public static void main(String[] args) {

        File file = new File("E:\\test.mdb");
        MdbGDEM mdbGDEM = new MdbGDEM(file, (DDSearchEngine)null, null);
        try {
            mdbGDEM.printRows();
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }

        System.out.println("KONETS");
    }
}
