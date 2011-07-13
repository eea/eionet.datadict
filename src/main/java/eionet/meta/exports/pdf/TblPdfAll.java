
package eionet.meta.exports.pdf;

import java.util.Hashtable;
import java.util.Vector;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.savers.Parameters;
import eionet.util.Util;

public class TblPdfAll {
    
    private DDSearchEngine searchEngine = null;
    //private Section parentSection = null;
    //private Section section = null;
    
    //private Vector docElements = new Vector();
    
	private String vsPath = null;
	
	private Parameters params = null;
	
	private DstPdfAll owner = null;
	
	// methods
	/////////////
    
    public TblPdfAll(DDSearchEngine searchEngine, DstPdfAll owner)
        throws Exception {
            
        //if (parentSection==null) throw new Exception("parentSection cannot be null!");
        
        if (searchEngine==null)
            throw new Exception("searchEngine cannot be null!");
            
        this.searchEngine = searchEngine;
        //this.parentSection = parentSection;
        this.owner = owner;
    }
    
	public void write(String tblID) throws Exception {
		write(tblID, null);
	}
    
    protected void write(String tblID, String dstID) throws Exception {
        
        if (Util.voidStr(tblID))
            throw new Exception("Table ID not specified");
        
        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID, dstID);
        if (dsTable == null)
            throw new Exception("Table not found!");
            
        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T");
        dsTable.setSimpleAttributes(v);
        
        // get data elements (this will set all the simple attributes,
        // but no fixed values required by writer!)
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);
        
        // get the dataset basic info
        /* JH151003 - not needed, cause tbl gdln is always part of a dst gdln
        Dataset ds = null;
        if (!Util.voidStr(dsTable.getDatasetID())){
            ds = searchEngine.getDataset(dsTable.getDatasetID());
        }
        */
        
        write(dsTable);
    }
    
    /**
    * Write a full guideline for a dataset table given by table object.
    */
    private void write(DsTable dsTable) throws Exception {
        
        if (dsTable==null)
            throw new Exception("Table object was null!");

		String s = dsTable.getAttributeValueByShortName("Name");
		String tblName = Util.voidStr(s) ? dsTable.getShortName() : s;
				
		String nr = "";
		if (owner != null)
			nr = owner.getSectioning().level(tblName + " table", 2);
		nr = nr==null ? "" : nr + " ";
		
        Paragraph prg = new Paragraph();
        prg.add(new Chunk(nr + tblName, Fonts.getUnicode(14, Font.BOLD)));
        prg.add(new Chunk(" table", Fonts.getUnicode(14)));
        
        //section = parentSection.addSection(prg, 2);
        
        addElement(prg);
        
        // write simple attributes
        
        Hashtable hash = null;
        Vector v = dsTable.getSimpleAttributes();
        
        // short name
        hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", dsTable.getShortName());
        v.add(0, hash);
        
		// version
		String ver = dsTable.getVersion();
		if (!Util.voidStr(ver)){
			hash = new Hashtable();
			hash.put("name", "Version");
			hash.put("value", ver);
			v.add(0, hash);
		}

        addElement(PdfUtil.simpleAttributesTable(v));
        addElement(new Phrase("\n"));

        // write table elements factlist, but first get fixed values & fk rels        
        v = dsTable.getElements();
        if (v==null || v.size()==0)
            return;
        
        DataElement elem = null;
		String dstID = params==null ? null : params.getParameter("dstID");
        for (int i=0; i<v.size(); i++){
            elem = (DataElement)v.get(i);
            Vector fxValues = searchEngine.getFixedValues(elem.getID(), "elem");
            elem.setFixedValues(fxValues);
			Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
			elem.setFKRelations(fks);
			Vector attrs = searchEngine.getSimpleAttributes(elem.getID(), "E");
			elem.setAttributes(attrs);
        }
        
        if (owner!=null){
        	owner.addTblElms(dsTable.getID(), v);
			owner.addTblNames(dsTable.getID(), tblName);
        }
        
		addElement(new Paragraph("Columns in the table:",
											Fonts.get(Fonts.HEADING_0)));
        addElement(PdfUtil.tableElements(v, null, null));
        
        // write data element full guidelines, each into a separate chapter
		for (int i=0; v!=null && i<v.size(); i++){
            elem = (DataElement)v.get(i);
            addElement(new Paragraph("\n"));
            ElmPdfAll elmAll = new ElmPdfAll(searchEngine, this);
			elmAll.setVsPath(this.vsPath);
			elmAll.write(elem.getID(), dsTable.getID());
        }
    }
    
    protected void addElement(Element elm){
    	
    	if (owner!=null)
    		owner.addElement(elm);
        
        //if (elm != null) section.add(elm);        
        //return docElements.size();
    }

	public void setVsPath(String vsPath){
		this.vsPath = vsPath;
	}

	public void setParameters(Parameters params){
		this.params = params;
	}
	
	protected Sectioning getSectioning(){
		if (owner!=null)
			return owner.getSectioning();
		else
			return null;
	}
}
