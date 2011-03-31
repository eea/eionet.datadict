/*
 * Created on 9.11.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.harvest;

import java.util.Hashtable;
import java.util.Vector;

import eionet.directory.DirServiceException;
import eionet.directory.DirectoryService;

public class OrgHarvester extends DDHarvester{
	
	public OrgHarvester(){
		super("Organisations harvester");
	}
	
	public void doHarvest() throws Exception{
		
		Vector orgs = null;
		try{
			orgs = DirectoryService.listOrganisations();
		}
		catch (DirServiceException dse){
			dse.printStackTrace();
		}		
		if (orgs==null)
			return;

		for (int i=0; i<orgs.size(); i++){
			
			String orgID = (String)orgs.get(i);
			if (orgID.startsWith("="))
				orgID = orgID.substring(1).trim();
			
			Hashtable h = null;
			try{
				h = DirectoryService.getOrganisation(orgID);
			}
			catch (DirServiceException dse){
				dse.printStackTrace();
			}
			if (h==null)
				continue;
			
			String id = (String)h.get("ID");
			if (id==null)
				continue;
				
			store(h, id);
		}
	}

	public static void main(String[] args) {
		
		HarvesterIF harvester = new OrgHarvester();
		
		try{
			harvester.harvest();
		}
		catch (Exception e){
			e.printStackTrace(System.out);
			harvester.cleanup();
		}
	}
}
