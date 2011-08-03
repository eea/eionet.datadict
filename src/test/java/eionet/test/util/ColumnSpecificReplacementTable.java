package eionet.test.util;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementTable;

/**
 * An extension of {@link ReplacementTable} that allows replacements in one column only.
 *
 * @author Jaanus Heinlaid
 *
 */
public class ColumnSpecificReplacementTable extends ReplacementTable{

    /** */
    private ITable table;

    /** */
    private String columnName;

    /**
     *
     * @param table
     */
    public ColumnSpecificReplacementTable(ITable table, String columnName) {

        super(table);

        if (columnName==null || columnName.trim().length()==0){
            throw new IllegalArgumentException("Column name must be given!");
        }

        this.table = table;
        this.columnName = columnName;
    }

    /*
     * (non-Javadoc)
     * @see org.dbunit.dataset.ReplacementTable#addReplacementSubstring(java.lang.String, java.lang.String)
     */
    @Override
    public void addReplacementSubstring(String originalSubstring, String replacementSubstring){

        throw new UnsupportedOperationException("Method not supported!");
    }


    /*
     * (non-Javadoc)
     * @see org.dbunit.dataset.ReplacementTable#getValue(int, java.lang.String)
     */
    @Override
    public Object getValue(int row, String column) throws DataSetException{

        if (column!=null && column.equalsIgnoreCase(this.columnName)){
            return super.getValue(row, column);
        }
        else{
            return this.table.getValue(row, column);
        }
    }
}
