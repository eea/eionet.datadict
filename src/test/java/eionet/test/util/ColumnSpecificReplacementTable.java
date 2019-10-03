package eionet.test.util;

import java.util.HashSet;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementTable;

/**
 * An extension of {@link ReplacementTable} that allows replacements in one column only.
 *
 * @author Jaanus Heinlaid
 *
 */
public class ColumnSpecificReplacementTable extends ReplacementTable {

    /** */
    private ITable table;

    /** */
    private HashSet<String> columnNames = new HashSet<String>();

    /**
     *
     * @param table
     */
    public ColumnSpecificReplacementTable(ITable table, String... columnNames) {

        super(table);

        if (columnNames == null || columnNames.length == 0) {
            throw new IllegalArgumentException("At least one column name must be given!");
        }

        this.table = table;
        for (int i = 0; i < columnNames.length; i++) {
            this.columnNames.add(columnNames[i]);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.dbunit.dataset.ReplacementTable#addReplacementSubstring(java.lang.String, java.lang.String)
     */
    @Override
    public void addReplacementSubstring(String originalSubstring, String replacementSubstring) {

        throw new UnsupportedOperationException("Method not supported!");
    }


    /*
     * (non-Javadoc)
     * @see org.dbunit.dataset.ReplacementTable#getValue(int, java.lang.String)
     */
    @Override
    public Object getValue(int row, String column) throws DataSetException{

        if (column != null && columnNames.contains(column)) {
            return super.getValue(row, column);
        }
        else{
            return this.table.getValue(row, column);
        }
    }
}
