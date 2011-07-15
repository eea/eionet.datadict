package eionet.meta.exports.xls;

public interface XlsIF {
    
    public void create(String dstID) throws Exception;
    public void write() throws Exception;
    public String getName();
}
