package eionet.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	 * @param inParams
	 * @param conn
	 * @return
	 * @throws SQLException 
	 */
	public static PreparedStatement preparedStatement(String sql, INParameters inParams, Connection conn) throws SQLException{
		
		PreparedStatement stmt = conn.prepareStatement(sql);
		populate(stmt, inParams);
		return stmt;
	}
	
	/**
	 * 
	 * @param stmt
	 * @param inParams
	 * @throws SQLException 
	 */
	public static void populate(PreparedStatement stmt, INParameters inParams) throws SQLException{
		
		for (int i=0; stmt!=null && inParams!=null && i<inParams.size(); i++){
			
			Integer sqlType = inParams.getSQLType(i);
			if (sqlType==null)
				stmt.setObject(i+1, inParams.getValue(i));
			else
				stmt.setObject(i+1, inParams.getValue(i), sqlType.intValue());
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

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String encloseWithApos(String s){
		
		if (s==null)
			return null;
		else{
			StringBuffer buf = new StringBuffer("'");
			return buf.append(s).append("'").toString();
		}
	}
	
	/**
	 * 
	 * @param preparedSQL
	 * @param inParams
	 * @param conn
	 * @throws SQLException 
	 */
	public static void executeUpdate(String preparedSQL, INParameters inParams, Connection conn) throws SQLException{
		
		PreparedStatement stmt = null;
        try{
        	stmt = SQL.preparedStatement(preparedSQL, inParams, conn);
        	stmt.executeUpdate();
        }
        finally{
        	try{
        		if (stmt!=null) stmt.close();
        	}
        	catch (SQLException e){}
        }
	}
	
	/**
	 * 
	 * @param conn
	 */
	public static void close(Connection conn){
		
		if (conn!=null){
			try{
				conn.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @param stmt
	 */
	public static void close(Statement stmt){
		
		if (stmt!=null){
			try{
				stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @param rs
	 */
	public static void close(ResultSet rs){
		
		if (rs!=null){
			try{
				rs.close();
			}
			catch (SQLException e){}
		}
	}
}
