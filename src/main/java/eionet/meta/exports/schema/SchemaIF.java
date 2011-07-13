package eionet.meta.exports.schema;

public interface SchemaIF {
    
    public static final String DATASET  = "DST";
    public static final String DSTABLE  = "TBL";
    public static final String DATAELEM = "ELM";
    
	public static final String NSID_DATASETS = "1";
	public static final String NSID_ISOATTRS = "2";
	public static final String NSID_DDATTRS  = "3";

	public static final int NOT_IN_CONTAINER = 0;
	public static final int IN_CONTAINER = 1;
	public static final int FIRST_IN_CONTAINER = 2;
	public static final int LAST_IN_CONTAINER = 3;
	public static final int FIRST_AND_LAST_IN_CONTAINER = 4;
    
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
