package eionet.datadict.commons.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetUtils {

    public static Byte getByte(ResultSet rs, String columnLabel) throws SQLException {
        return coerceNull(rs, rs.getByte(columnLabel));
    }
    
    public static Long getLong(ResultSet rs, String columnLabel) throws SQLException {
        return coerceNull(rs, rs.getLong(columnLabel));
    }
    
    protected static <T> T coerceNull(ResultSet rs, T value) throws SQLException {
        return rs.wasNull() ? null : value;
    }
    
}
