package eionet.meta.exports.xforms;

public interface XFormIF {
	
	public static final String ATTR_ID       = "id";
	public static final String ATTR_TYPE     = "type";
	public static final String ATTR_REF      = "ref";
	public static final String ATTR_BIND     = "bind";
	public static final String ATTR_NODESET  = "nodeset";
	public static final String ATTR_MINSIZE  = "min-size";
	public static final String ATTR_MAXSIZE  = "max-size";
	public static final String ATTR_MINVALUE = "min-value";
	public static final String ATTR_MAXVALUE = "max-value";
	
	public static final String CTRL_LABEL = "ctrl-label";
	public static final String CTRL_HINT  = "ctrl-hint";
	public static final String CTRL_TYPE  = "ctrl-type";
	public static final String CTRL_ALERT = "ctrl-alert";
	public static final String CTRL_FXVS  = "ctrl-fixed-values";
	
	public static final String DEFAULT_DATATYPE = "string";
	public static final String DEFAULT_CTRLTYPE = "input";

	/**
	* Write an XML instance for the given object.
	*/
	public abstract void write(String objID) throws Exception;
    
	/**
	* Flush the written content into the writer.
	*/
	public abstract void flush(String template) throws Exception;

	/**
	* Sets the request URI up to servlet name. Does not have to end with slash.
	*/
	public abstract void setAppContext(String appContext);

}
