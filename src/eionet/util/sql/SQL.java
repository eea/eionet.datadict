package eionet.util.sql;

import java.sql.Types;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class SQL {

	/**
	 * 
	 * @param sql
	 * @param sqlArgs
	 * @param conn
	 * @return
	 * @throws SQLException 
	 */
	public static PreparedStatement preparedStatement(String sql, INParameters sqlArgs, Connection conn) throws SQLException{
		
		PreparedStatement stmt = conn.prepareStatement(sql);
		populate(stmt, sqlArgs);
		return stmt;
	}
	
	/**
	 * 
	 * @param stmt
	 * @param sqlArgs
	 * @throws SQLException 
	 */
	public static void populate(PreparedStatement stmt, INParameters sqlArgs) throws SQLException{
		
		for (int i=0; stmt!=null && sqlArgs!=null && i<sqlArgs.size(); i++){
			
			int sqlType = sqlArgs.getSQLType(i);
			if (sqlType==Types.JAVA_OBJECT)
				stmt.setObject(i+1, sqlArgs.getValue(i));
			else
				stmt.setObject(i+1, sqlArgs.getValue(i), sqlType);
		}
	}
	
	/**
	 * 
	 * @param columns
	 * @return
	 */
	public static String insertStatement(String tableName, LinkedHashMap columns){
		
		if (columns==null || columns.size()==0)
			return null;
		
		StringBuffer buf = new StringBuffer("insert into ");
		buf.append(tableName).append(" (");
		
		// add names
		Iterator iter = columns.keySet().iterator();
		for (int i=0; iter.hasNext(); i++){
			if (i>0)
				buf.append(", ");
			buf.append(iter.next());
		}
		
		buf.append(") values (");
		
		// add values
		iter = columns.values().iterator();
		for (int i=0; iter.hasNext(); i++){
			if (i>0)
				buf.append(", ");
			buf.append(iter.next());
		}
		
		return buf.append(")").toString();
	}

	/**
	 * 
	 * @param columns
	 * @return
	 */
	public static String updateStatement(String tableName, LinkedHashMap columns){
		
		if (columns==null || columns.size()==0)
			return null;
		
		StringBuffer buf = new StringBuffer("update ");
		buf.append(tableName).append(" set ");
		
		Iterator colNames = columns.keySet().iterator();
		for (int i=0; colNames.hasNext(); i++){
			if (i>0)
				buf.append(", ");
			String colName = colNames.next().toString();
			buf.append(colName).append("=").append(columns.get(colName));
		}
		
		return buf.toString();
	}
}
