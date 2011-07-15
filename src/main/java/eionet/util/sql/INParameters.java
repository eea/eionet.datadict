package eionet.util.sql;

import java.sql.Types;
import java.util.ArrayList;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class INParameters {
    
    /** */
    ArrayList values = null;
    ArrayList sqlTypes = null;

    /**
     *
     */
    public INParameters(){
        values = new ArrayList();
        sqlTypes = new ArrayList();
    }
    
    /**
     * 
     * @param value
     * @param sqlType
     * @return
     */
    public String add(Object value, int sqlType){
        values.add(value);
        sqlTypes.add(new Integer(sqlType));
        return "?";
    }
    
    /**
     * 
     * @param value
     * @return
     */
    public String add(Object value){
        values.add(value);
        sqlTypes.add(null);
        return "?";
    }
    
    /**
     * 
     * @return
     */
    public int size(){
        return values.size();
    }
    
    /**
     * 
     * @param i
     * @return
     */
    public Object getValue(int i){
        return values.get(i);
    }

    /**
     * 
     * @param i
     * @return
     */
    public Integer getSQLType(int i){
        return (Integer)sqlTypes.get(i);
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        
        StringBuffer buf = new StringBuffer(size()==0 ? "empty" : "");
        for (int i=0; i<values.size(); i++){
            
            buf.append((String)values.get(i));          
            Integer sqlType = getSQLType(i);
            buf.append(", ").append(sqlType==null ? "null" : sqlTypeLabel(sqlType)).
            append("\n");
        }
        return buf.toString();
    }
    
    /**
     * 
     * @return
     */
    private static String sqlTypeLabel(Integer sqlType){
        
        String retString = "???";
        switch (sqlType.intValue()){
            case Types.ARRAY:
                retString = "Types.ARRAY";
                break;
            case Types.BIGINT:
                retString = "Types.BIGINT";
                break;
            case Types.BINARY:
                retString = "Types.BINARY";
                break;
            case Types.BIT:
                retString = "Types.BIT";
                break;
            case Types.BLOB:
                retString = "Types.BLOB";
                break;
            case Types.BOOLEAN:
                retString = "Types.BOOLEAN";
                break;
            case Types.CHAR:
                retString = "Types.CHAR";
                break;
            case Types.CLOB:
                retString = "Types.CLOB";
                break;
            case Types.DATALINK:
                retString = "Types.DATALINK";
                break;
            case Types.DATE:
                retString = "Types.DATE";
                break;
            case Types.DECIMAL:
                retString = "Types.DECIMAL";
                break;
            case Types.DISTINCT:
                retString = "Types.DISTINCT";
                break;
            case Types.DOUBLE:
                retString = "Types.DOUBLE";
                break;
            case Types.FLOAT:
                retString = "Types.FLOAT";
                break;
            case Types.INTEGER:
                retString = "Types.INTEGER";
                break;
            case Types.JAVA_OBJECT:
                retString = "Types.JAVA_OBJECT";
                break;
            case Types.LONGVARBINARY:
                retString = "Types.LONGVARBINARY";
                break;
            case Types.LONGVARCHAR:
                retString = "Types.LONGVARCHAR";
                break;
            case Types.NULL:
                retString = "Types.NULL";
                break;
            case Types.NUMERIC:
                retString = "Types.NUMERIC";
                break;
            case Types.OTHER:
                retString = "Types.OTHER";
                break;
            case Types.REAL:
                retString = "Types.REAL";
                break;
            case Types.REF:
                retString = "Types.REF";
                break;
            case Types.SMALLINT:
                retString = "Types.SMALLINT";
                break;
            case Types.STRUCT:
                retString = "Types.STRUCT";
                break;
            case Types.TIME:
                retString = "Types.TIME";
                break;
            case Types.TIMESTAMP:
                retString = "Types.TIMESTAMP";
                break;
            case Types.TINYINT:
                retString = "Types.TINYINT";
                break;
            case Types.VARBINARY:
                retString = "Types.VARBINARY";
                break;
            case Types.VARCHAR:
                retString = "Types.VARCHAR";
                break;
            default:
                break;
        }
        
        return retString;
    }
}
