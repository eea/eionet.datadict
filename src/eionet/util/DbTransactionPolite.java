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
 * The Original Code is Data Dictionary.
 * 
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by TietoEnator Estonia are
 * Copyright (C) 2003 European Environment Agency. All
 * Rights Reserved.
 * 
 * Contributor(s): 
 */
package eionet.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class DbTransactionPolite{
	
	/** */
	private boolean isAnotherTransactionRunning = false;
	
	/** */
	private Connection conn = null;

	/**
	 * 
	 * @param conn
	 */
	private DbTransactionPolite(Connection conn){
		this.conn = conn;
	}

	/**
	 * 
	 *
	 */
	private void start(){
		boolean anotherTransactionRunning = false;
		try{
			anotherTransactionRunning = conn.getAutoCommit()==false;
			if (!anotherTransactionRunning)
				conn.setAutoCommit(false);
		}
		catch (SQLException e){
		}
	}
	
	/**
	 * 
	 * @param conn
	 * @return
	 */
	public static DbTransactionPolite start(Connection conn){
		DbTransactionPolite tx = new DbTransactionPolite(conn);
		tx.start();
		return tx;
	}
	
	/**
	 * 
	 *
	 */
	public void commit(){
		try {
			if (!isAnotherTransactionRunning)
				conn.commit();
		}
		catch (SQLException e){
		}
		
	}
	
	/**
	 * 
	 *
	 */
	public void rollback(){
		try {
			if (!isAnotherTransactionRunning)
				conn.rollback();
		}
		catch (SQLException e){
		}
		
	}
	
	/**
	 * 
	 *
	 */
	public void end(){
		try {
			if (!isAnotherTransactionRunning)
				conn.setAutoCommit(true);
		}
		catch (SQLException e){
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean getIsAnotherTransactionRunning() {
		return isAnotherTransactionRunning;
	}
}
