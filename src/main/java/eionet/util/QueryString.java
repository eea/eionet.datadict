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
 * Copyright (C) 2003 European Environment Agency. All
 * Rights Reserved.
 *
 * Contributor(s):
 */
package eionet.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import eionet.meta.DDRuntimeException;


/**
 * A Class class.
 * <P>
 * @author Enriko Käsper, Jaanus Heinlaid
 */
public class QueryString{

    /** */
    private String queryString = null;

    /**
     *
     */
    public QueryString() {
        this.queryString = "";
    }

    /**
     *
     * @param queryString
     */
    public QueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return queryString;
    }

    /**
     *
     * @param param
     * @param value
     * @return
     */
    public String changeParam(String param, String value) {

        if (hasParam(param)) {
            change(param, value);
        } else {
            append(param, value);
        }

        return queryString;
    }

    /**
     *
     * @param param
     * @return
     */
    public String removeParam(String param) {
        remove(param);
        return queryString;
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean equals(QueryString s) {
        return equals(s.getValue());
    }

    /**
     *
     * @param param
     * @return
     */
    private boolean hasParam(String param) {

        if (queryString.indexOf(param + "=")>0) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param param
     * @param value
     */

    private void append(String param, String value) {

        String s =queryString.indexOf("?")>0 ? "&" : "?";

        queryString += s + param + "=" + value;
    }

    /**
     *
     * @param param
     */
    private void remove(String param) {

        int i=queryString.indexOf(param);
        if (i<1) {
            return;
        }

        int and=queryString.indexOf("&", i);

        if (and>0) {
            queryString = queryString.substring(0,i-1) + queryString.substring(and);
        } else {
            queryString = queryString.substring(0,i-1);
        }
    }

    /**
     *
     * @param param
     * @param value
     */
    private void change(String param, String value) {

        int i=queryString.indexOf(param);
        if (i<1) {
            return;
        }
        String begin=queryString.substring(0, i);
        String str=queryString.substring(i);
        int j = str.indexOf("&");
        String end = j>0 ? str.substring(j) : "";

        queryString=begin + param + "=" + value + end ;
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean equals(String s) {

        if (queryString.equals(s)) {
            return true;
        }

        int sep = queryString.indexOf("?");
        int sep2 = s.indexOf("?");
        if (sep>0) {
            if (sep!=sep2) {
                return false;
            }
            if (!queryString.substring(0,sep).equalsIgnoreCase(s.substring(0,sep))) {
                return false;
            }

            String query = queryString.substring(sep+1);
            StringTokenizer tokens = new StringTokenizer(query, "&");
            String query2 = s.substring(sep+1);
            StringTokenizer tokens2 = new StringTokenizer(query2, "&");


            if (tokens.countTokens()!=tokens2.countTokens()) {
                return false;
            }
            boolean ok = false;
            while (tokens.hasMoreTokens()) {
                String t=tokens.nextToken();
                while (tokens2.hasMoreTokens()) {
                    if (t.equals(tokens2.nextToken())) {
                        ok = true;
                    }
                }
                if (ok==false) {
                    return false;
                }
                ok=false;
                tokens2 = new StringTokenizer(query2, "&");
            }

        }
        else {
            return queryString.equalsIgnoreCase(s);
        }
        return true;
    }

    /**
     *
     * @param parameterMap
     * @param encoding
     * @return
     */
    public static String toQueryString(Map parameterMap, String encoding){

        if (parameterMap==null || parameterMap.isEmpty()){
            return "";
        }

        StringBuilder result = new StringBuilder();
        Set entrySet = parameterMap.entrySet();

        try {
            for (Iterator entryIter = entrySet.iterator(); entryIter.hasNext();){

                Map.Entry entry = (Map.Entry)entryIter.next();
                String key = entry.getKey().toString();

                Object value = entry.getValue();
                if (value!=null){
                    if (value instanceof String[]){
                        String[] values = (String[]) value;
                        for (int i = 0; i < values.length; i++) {
                            if (result.length()>0){
                                result.append("&");
                            }
                            result.append(URLEncoder.encode(key, encoding)).append("=");
                            result.append(URLEncoder.encode(values[i], encoding));
                        }
                    }
                    else if (value instanceof Iterable){
                        Iterable values = (Iterable) value;
                        for (Iterator iter = values.iterator(); iter.hasNext();){
                            if (result.length()>0){
                                result.append("&");
                            }
                            result.append(URLEncoder.encode(key, encoding)).append("=");
                            result.append(URLEncoder.encode(iter.next().toString(), encoding));
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new DDRuntimeException(e);
        }

        return result.length()==0 ? "" : "?" + result;
    }
}

