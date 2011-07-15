/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * The Original Code is Data Dictionary.
 * 
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by TietoEnator Estonia are
 * Copyright (C) 2006 European Environment Agency. All
 * Rights Reserved.
 * 
 * Contributor(s): 
 */
/*
 * Created on 3.05.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 * @author jaanus
 */
public class XmlTag {
    
    /** */
    private String tagName = null;
    private String content = null;
    private Hashtable attributes = null;
    
    /**
     * 
     * @param attrName
     * @param attrValue
     */
    public void setAttribute(String attrName, String attrValue){
        if (attributes==null)
            attributes = new Hashtable();
        
        attributes.put(attrName, attrValue);
    }

    /**
     * 
     * @return
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * 
     * @param tagName
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * 
     * @return
     */
    public Hashtable getAttributes() {
        return attributes;
    }

    /**
     * 
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * 
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 
     * @return
     */
    public String getStartUnclosed(){
        
        StringBuffer buf = new StringBuffer("<");
        buf.append(getTagName());
        
        Hashtable attributes = getAttributes();
        if (attributes!=null && attributes.size()>0){           
            Enumeration attrNames = attributes.keys();
            while (attrNames.hasMoreElements()){
                String attrName = (String)attrNames.nextElement();
                String attrValue = (String)attributes.get(attrName);
                buf.append(" ");
                buf.append(attrName);
                buf.append("=\"");
                buf.append(attrValue);
                buf.append("\"");
            }
        }
        
        return buf.toString();
    }

    /**
     * 
     * @return
     */
    public String getStart(){
        
        StringBuffer buf = new StringBuffer(getStartUnclosed());
        buf.append(">");
        return buf.toString();
    }

    /**
     * 
     * @return
     */
    public String getStartEnd(){
        
        StringBuffer buf = new StringBuffer(getStartUnclosed());
        buf.append("/>");
        return buf.toString();
    }
    
    /**
     * 
     * @return
     */
    public String getEnd(){
        
        StringBuffer buf = new StringBuffer("</");
        buf.append(getTagName());
        buf.append(">");
        
        return buf.toString();
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        
        addDefaultAttributes();
        
        String content = getContent();
        if (content==null || content.trim().length()==0)
            return getStartEnd();
        else{
            StringBuffer buf = new StringBuffer(getStart());
            buf.append(content.trim());
            buf.append(getEnd());
            return buf.toString();
        }
    }
    
    /**
     * Callback method for subclasses.
     * @return
     */
    protected Hashtable getDefaultAttributes(){
        return null;
    }
    
    /**
     * 
     *
     */
    private void addDefaultAttributes(){
        
        Hashtable defaultAttributes = getDefaultAttributes();
        if (defaultAttributes!=null && defaultAttributes.size()>0){
            Enumeration defaultAttrNames = defaultAttributes.keys();
            while (defaultAttrNames.hasMoreElements()){
                String defaultAttrName = (String)defaultAttrNames.nextElement();
                String defaultAttrValue = (String)defaultAttributes.get(defaultAttrName);
                setAttribute(defaultAttrName, defaultAttrValue);
            }
        }
    }
    
    /**
     * 
     * @param intoStr
     */
    protected String writeInto(String intoStr){
        return intoStr;
    }
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args){
        
        XmlTag tag = new XmlTag();
        tag.setTagName("jaanus");
        tag.setAttribute("vanues", "32");
        tag.setAttribute("pikkus", "186cm");
        tag.setContent("tore mees on");
        System.out.println(tag);
    }
}
