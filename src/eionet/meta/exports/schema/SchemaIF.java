package eionet.meta.exports.schema;

public interface SchemaIF {
    
    public static final String DATASET  = "DST";
    public static final String DSTABLE  = "TBL";
    public static final String DATAELEM = "ELM";
    
    /**
    * Write a schema for an object given by ID.
    */
    public abstract void write(String objID) throws Exception;
    
    /**
    * Flush the written content into the writer.
    */
    public abstract void flush() throws Exception;
    
    /**
    * Sets the identitation of lines to be written
    */
    public abstract void setIdentitation(String identitation);
    
    /**
    * Sets the request URI up to servlet name. Does not have to end with slash.
    */
    public abstract void setAppContext(String appContext);
}