/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;


/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class ExportStatics {
    
    protected ExportStatics(){}
    
    static enum ObjectType{
        ELM,
        TBL,
        DST;
    }
    
    static final String WRAPPER = "\"";
    static final String WRAPPER_SINGLE_QUOTE = "\'";
    static final String CSV_HEADER_DATASET = "Dataset";
    static final String CSV_HEADER_TABLE = "Table";
    static final String CSV_HEADER_ELEMENT = "Element";
    static final String CSV_HEADER_FIXED = "Fixed";
    static final String CSV_HEADER_CODEVALUE = "\"Code\",\"Label\",\"Definition\"";
    static final String CSV_DELIMITER_COMMA = ",";
    static final String CSV_DELIMITER_SPACE = " ";
    static final String CSV_DELIMITER_LIST = "|";
    static final String CSV_DELIMITER_LABEL = ":";
    static final String CSV_NEW_LINE = "\n";
    
    static final String DD_NAMESPACE = "http://dd.eionet.europa.eu";
    static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    
    static String wrap( String str ){
        return wrap( str, WRAPPER );
    }
    static String wrap( String str, String wrapper ){
        if ( wrapper == null )
            return str;
        StringBuilder bld = new StringBuilder().append(wrapper);
        bld.append(str);
        bld.append(wrapper);
        return bld.toString();
    }
    
}
