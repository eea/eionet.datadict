/*
 * Created on 3.05.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.exports.ods.tags;

import eionet.util.XmlTag;

/**
 * 
 * @author jaanus
 */
public class NumberStyle extends XmlTag {
	
	/** */
	private String styleName = null;
	private String decimalPlaces = null;
	private String minIntegerDigits = null;	

	/*
	 *  (non-Javadoc)
	 * @see eionet.util.XmlTag#getTagName()
	 */
	public String getTagName(){
		return "number:number-style";
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
	 * @param decimalPlaces
	 */
	public void setDecimalPlaces(String decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	/**
	 * 
	 * @param minIntegerDigits
	 */
	public void setMinIntegerDigits(String minIntegerDigits) {
		this.minIntegerDigits = minIntegerDigits;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see eionet.util.XmlTag#getContent()
	 */
	public String getContent(){
		
		XmlTag content = new XmlTag();
		content.setTagName("number:number");
		content.setAttribute("number:decimal-places", decimalPlaces);
		content.setAttribute("number:min-integer-digits", minIntegerDigits);
		return content.toString();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		
		StringBuffer buf = new StringBuffer();
		buf.append("<number:number-style style:name=\"");
		buf.append(styleName);
		buf.append("\"><number:number number:decimal-places=\"");
		buf.append(decimalPlaces);
		buf.append("\" number:min-integer-digits=\"");
		buf.append(minIntegerDigits);
		buf.append("\"/></number:number-style>");
		
		return buf.toString();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see eionet.util.XmlTag#writeInto(java.lang.String)
	 */
	public String writeInto(String intoStr){
		
		if (intoStr==null || intoStr.length()==0)
			return intoStr;
		
		String officeAutomaticStyles = new String("<office:automatic-styles>");
		int i = intoStr.indexOf(officeAutomaticStyles);
		if (i<0)
			return intoStr;
		
		StringBuffer buf = new StringBuffer();
		buf.append(intoStr.substring(0, i + officeAutomaticStyles.length()));
		buf.append(this.toString());
		buf.append(intoStr.substring(i + officeAutomaticStyles.length()));
		
		return buf.toString();
	}
	
	/**
	 * 
	 */
	public static void main(String[] args){
		
		NumberStyle ns = new NumberStyle();
		ns.setStyleName("N1");
		ns.setDecimalPlaces("4");
		ns.setMinIntegerDigits("1");
		System.out.println(ns.toString());
	}
}
