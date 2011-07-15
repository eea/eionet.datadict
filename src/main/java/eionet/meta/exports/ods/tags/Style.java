/*
 * Created on 3.05.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.exports.ods.tags;


/**
 * 
 * @author jaanus
 */
public class Style{
    
    /** */
    private String styleName = null;
    private String dataStyleName = null;

    /**
     * 
     * @param styleName
     */
    public void setStyleName(String styleName){
        this.styleName = styleName;
    }
    
    /**
     * 
     * @param dataStyleName
     */
    public void setDataStyleName(String dataStyleName){
        this.dataStyleName = dataStyleName;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        
        StringBuffer buf = new StringBuffer();
        buf.append("<style:style style:name=\"");
        buf.append(styleName);
        buf.append("\" style:family=\"table-cell\" style:parent-style-name=\"Default\"");
        if (dataStyleName!=null){
            buf.append(" style:data-style-name=\"");
            buf.append(dataStyleName);
            buf.append("\"");
        }
        buf.append("/>");
        
        return buf.toString();
    }
    
    /*
     *  (non-Javadoc)
     * @see eionet.util.XmlTag#writeInto(java.lang.String)
     */
    public String writeInto(String intoStr){
        
        if (intoStr==null || intoStr.length()==0)
            return intoStr;
        
        String officeAutomaticStyles = new String("</office:automatic-styles>");
        int i = intoStr.indexOf(officeAutomaticStyles);
        if (i<0)
            return intoStr;
        
        StringBuffer buf = new StringBuffer();
        buf.append(intoStr.substring(0, i));
        buf.append(this.toString());
        buf.append(intoStr.substring(i));
        
        return buf.toString();
    }
}
