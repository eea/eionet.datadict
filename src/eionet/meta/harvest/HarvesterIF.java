
package eionet.meta.harvest;

import eionet.util.LogServiceIF;

public interface HarvesterIF {	
	
	/**
	* 
	*/
	public abstract void harvest() throws Exception;

	/**
	* 
	*/
	public abstract void cleanup();
	
	/**
	* 
	*/
	public abstract LogServiceIF getLog();
}
