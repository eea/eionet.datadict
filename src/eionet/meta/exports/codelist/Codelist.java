/*
 * Created on 14.02.2007
 */
package eionet.meta.exports.codelist;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;

/**
 * 
 * @author jaanus
 */
public abstract class Codelist {

	protected static final String ELM = "ELM";
	protected static final String TBL = "TBL";
	protected static final String DST = "DST";
	
	protected DDSearchEngine searchEngine = null;
	protected PrintWriter writer = null;
	
	protected Vector lines = new Vector();
	protected String lineTerminator = "\n";
	
	/**
	 * 
	 * @param objID
	 * @param objType
	 * @throws Exception
	 */
	public abstract void write(String objID, String objType) throws Exception;

	/**
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException{
		for (int i=0; lines!=null && i<lines.size(); i++){
			writer.write(lines.get(i).toString());
			writer.write(lineTerminator);			
		}
	}
}
