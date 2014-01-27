package eionet.meta.exports.pdf;

import eionet.meta.savers.Parameters;

public interface PdfHandoutIF {

    /* PdfHandout Constants */
    public static final String DATASET = "DST";
    public static final String DSTABLE = "TBL";      
    public static final String GUIDELINE = "GDLN";

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

    /**
     * Sets full path to datadict images directory
     */
    public abstract void setVisualsPath(String visualsPath);

    /**
     * Sets whatever additional parameters that the handouts might need
     */
    public abstract void setParameters(Parameters params);

    /**
     * Gets the name of the generated file
     */
    public abstract String getFileName();

}
