/*
 * Created on 3.05.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.exports.ods.tags;

import java.util.Hashtable;
import eionet.util.XmlTag;

/**
 * 
 * @author jaanus
 */
public class Style extends XmlTag{
	
	/** */
	private String styleName = null;
	private String dataStyleName = null;

	/*
	 *  (non-Javadoc)
	 * @see eionet.util.XmlTag#getTagName()
	 */
	public String getTagName(){
		return "style:style";
	}

	/**
	 * 
	 * @param styleName
	 */
	public void setStyleName(String styleName){
		this.styleName = styleName;
		setAttribute("style:name", styleName);
	}
	
	/**
	 * 
	 * @param dataStyleName
	 */
	public void setDataStyleName(String dataStyleName){
		this.dataStyleName = dataStyleName;
		setAttribute("style:data-style-name", dataStyleName);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see eionet.util.XmlTag#getDefaultAttributes()
	 */
	public Hashtable getDefaultAttributes(){
		
		Hashtable defaultAttrs = new Hashtable();
		defaultAttrs.put("style:family", "table-cell");
		defaultAttrs.put("style:parent-style-name", "Default");
		return defaultAttrs;
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

	/**
	 * 
	 */
	public static void main(String[] args){
		
		Style s = new Style();
		s.setStyleName("ce3");
		System.out.println(s.toString());
	}
}
