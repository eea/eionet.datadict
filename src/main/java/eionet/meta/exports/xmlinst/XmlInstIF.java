package eionet.meta.exports.xmlinst;

public interface XmlInstIF {

    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /**
    * Write an XML instance for the given object.
    */
    public abstract void write(String objID) throws Exception;

    /**
    * Flush the written content into the writer.
    */
    public abstract void flush() throws Exception;

    /**
    * Sets the request URI up to servlet name. Does not have to end with slash.
    */
    public abstract void setAppContext(String appContext);
}
