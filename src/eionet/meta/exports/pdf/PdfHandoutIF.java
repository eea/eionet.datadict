
package eionet.meta.exports.pdf;

public interface PdfHandoutIF {
    
    public static final String DATASET  = "DST";
    public static final String DSTABLE  = "TBL";
    public static final String DATAELEM = "ELM";
    
    public static final String FACTSHEET  = "FCTS";
    public static final String GUIDELINE  = "GDLN";
    
    /**
    * Write a handout for an object given by ID.
    */
    public abstract void write(String objID) throws Exception;
    
    /**
    * Flush the written content into the document.
    */
    public abstract void flush() throws Exception;
    
    /**
    * Sets the full path to the EEA logo image
    */
    public abstract void setLogo(String logo);
}