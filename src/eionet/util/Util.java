/**
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
 * The Original Code is "EINRC-4 / Meta Project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2002 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Jaanus Heinlaid (TietoEnator)
 */
 
package eionet.util;

import java.net.*;
import java.util.*;
import java.security.*;

//import eionet.meta.Log;

/**
 * This is a class containing several useful utility methods.
 *
 * @author Jaanus Heinlaid
 */
public class Util {
    
    /**
     * A method for determining if a String is void.
     * And by void we mean either null or zero-length.
     * Returns true, if the string IS void.
     */
     
    public static boolean voidStr(String s){
        if (s == null)
            return true;
        if (s.length() == 0)
            return true;
        
        return false;
    }
    
    /**
     * A method for calculating and formatting the current
     * date and time into a String for a log.
     */
     
    public static String logTime(){
        
        Date date = new Date();
        String month = String.valueOf(date.getMonth());
        month = (month.length() < 2) ? ("0" + month) : month;
        String day = String.valueOf(date.getDate());
        day = (day.length() < 2) ? ("0" + day) : day;
        String hours = String.valueOf(date.getHours());
        hours = (hours.length() < 2) ? ("0" + hours) : hours;
        String minutes = String.valueOf(date.getMinutes());
        minutes = (minutes.length() < 2) ? ("0" + minutes) : minutes;
        String seconds = String.valueOf(date.getSeconds());
        seconds = (seconds.length() < 2) ? ("0" + seconds) : seconds;
        
        String time = "[" + month;
        time = time + "/" + day;
        time = time + " " + hours;
        time = time + ":" + minutes;
        time = time + ":" + seconds;
        time = time + "] ";
        
        return time;
    }
    
    /**
     * A method for formatting the given timestamp into a String
     * for history.
     */
     
    public static String historyDate(long timestamp){
        
        Date date = new Date(timestamp);
        String year = String.valueOf(1900 + date.getYear());
        String month = String.valueOf(date.getMonth());
        month = (month.length() < 2) ? ("0" + month) : month;
        String day = String.valueOf(date.getDate());
        day = (day.length() < 2) ? ("0" + day) : day;
        String hours = String.valueOf(date.getHours());
        hours = (hours.length() < 2) ? ("0" + hours) : hours;
        String minutes = String.valueOf(date.getMinutes());
        minutes = (minutes.length() < 2) ? ("0" + minutes) : minutes;
        String seconds = String.valueOf(date.getSeconds());
        seconds = (seconds.length() < 2) ? ("0" + seconds) : seconds;
        
        String time = year;
        time = time + "/" + month;
        time = time + "/" + day;
        time = time + " " + hours;
        time = time + ":" + minutes;
        
        return time;
    }

    /**
     * A method for calculating time difference in MILLISECONDS,
     * between a date-time specified in input parameters and the
     * current date-time.<BR><BR>
     * This should be useful for calculating sleep time for code
     * that has a certain schedule for execution.
     *
     * @param   hour    An integer from 0 to 23. If less than 0
     *                  or more than 23, then the closest next
     *                  hour to current hour is taken.
     * @param   date    An integer from 1 to 31. If less than 1
     *                  or more than 31, then the closest next
     *                  date to current date is taken.
     * @param   month   An integer from Calendar.JANUARY to Calendar.DECEMBER.
     *                  If out of those bounds, the closest next
     *                  month to current month is taken.
     * @param   wday    An integer from 1 to 7. If out of those bounds,
     *                  the closest next weekday to weekday month is taken.
     * @param   zone    A String specifying the time-zone in which the
     *                  calculations should be done. Please see Java
     *                  documentation an allowable time-zones and  formats.
     * @return          Time difference in milliseconds.
     */
    public static long timeDiff(int hour, int date, int month, int wday, String zone){
        
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(zone));
        if (cal == null)
            cal = new GregorianCalendar(TimeZone.getDefault());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        
        /* here we force the hour to be one of the defualts
        if (hour < 0) hour = 0;
        if (hour > 23) hour = 23;
        */
        int cur_hour = cal.get(Calendar.HOUR);
        
        if (cal.get(Calendar.AM_PM) == Calendar.PM)
            cur_hour = 12 + cur_hour;
        
        // here we assume that every full hour is accepted
        /*if (hour < 0 || hour > 23){
            
            hour = cur_hour>=23 ? 0 : cur_hour + 1;
        }*/
        
        if (wday >= 1 && wday <= 7){
            
            int cur_wday = cal.get(Calendar.DAY_OF_WEEK);
            if (hour < 0 || hour > 23){
                if (cur_wday != wday)
                    hour = 0;
                else
                    hour = cur_hour>=23 ? 0 : cur_hour + 1;
            }
            
            int amount = wday-cur_wday;
            if (amount < 0) amount = 7 + amount;
            if (amount == 0 && cur_hour >= hour) amount = 7;
            cal.add(Calendar.DAY_OF_WEEK, amount);
        }
        // do something about when every date is accepted
        else if (month >= Calendar.JANUARY && month <= Calendar.DECEMBER){
            if (date < 1) date = 1;
            if (date > 31) date = 31;
            int cur_month = cal.get(Calendar.MONTH);
            int amount = month-cur_month;
            if (amount < 0) amount = 12 + amount;
            if (amount == 0){
                if (cal.get(Calendar.DATE) > date)
                    amount = 12;
                else if (cal.get(Calendar.DATE) == date){
                    if (cur_hour >= hour)
                        amount = 12;
                }
            }
            //cal.set(Calendar.DATE, date);
            cal.add(Calendar.MONTH, amount);
            if (date > cal.getActualMaximum(Calendar.DATE))
                date = cal.getActualMaximum(Calendar.DATE);
            cal.set(Calendar.DATE, date);
        }
        else if (date >= 1 && date <= 31){
            int cur_date = cal.get(Calendar.DATE);
            if (cur_date > date)
                cal.add(Calendar.MONTH, 1);
            else if (cur_date == date){
                if (cur_hour >= hour)
                    cal.add(Calendar.MONTH, 1);
            }
            cal.set(Calendar.DATE, date);
        }
        else{
            if (hour < 0 || hour > 23){
                hour = cur_hour>=23 ? 0 : cur_hour + 1;
            }
            if (cur_hour >= hour) cal.add(Calendar.DATE, 1);
        }
        
        if (hour >= 12){
            cal.set(Calendar.HOUR, hour - 12);
            cal.set(Calendar.AM_PM, Calendar.PM);
        }
        else{
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.AM_PM, Calendar.AM);
        }
        
        Date nextDate = cal.getTime();
        Date currDate = new Date();

        System.out.println(nextDate.toString());
        System.out.println(currDate.toString());

        long nextTime = cal.getTime().getTime();
        long currTime = (new Date()).getTime();

        return nextTime-currTime;
    }

    /**
     * A method for counting occurances of a substring in a string.
     */
    public static int countSubString(String str, String substr){
        int count = 0;
        while (str.indexOf(substr) != -1) {count++;}
        return count;
    }
    
    /**
     * A method for creating a unique digest of a String message.
     *
     * @param   src         String to be digested.
     * @param   algosrithm  Digesting algorithm (please see Java
     *                      documentation for allowable values).
     * @return              A unique String-typed digest of the input message.
     */
    public static String digest(String src, String algorithm) throws GeneralSecurityException{
        
        byte[] srcBytes = src.getBytes();
        byte[] dstBytes = new byte[16];
        
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(srcBytes);
        dstBytes = md.digest();
        md.reset();
        
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<dstBytes.length; i++){
            Byte byteWrapper = new Byte(dstBytes[i]);
            buf.append(String.valueOf(byteWrapper.intValue()));
        }
        
        return buf.toString();
    }
    
    /**
     * A method for creating a unique Hexa-Decimal digest of a String message.
     *
     * @param   src         String to be digested.
     * @param   algosrithm  Digesting algorithm (please see Java
     *                      documentation for allowable values).
     * @return              A unique String-typed Hexa-Decimal digest of the input message.
     */
    public static String digestHexDec(String src, String algorithm) throws GeneralSecurityException {
        
        byte[] srcBytes = src.getBytes();
        byte[] dstBytes = new byte[16];
        
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(srcBytes);
        dstBytes = md.digest();
        md.reset();
        
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<dstBytes.length; i++){
            Byte byteWrapper = new Byte(dstBytes[i]);
            int k = byteWrapper.intValue();
            String s = Integer.toHexString(byteWrapper.intValue());
            if (s.length() == 1) s = "0" + s;
            buf.append(s.substring(s.length() - 2));
        }
        
        return buf.toString();
    }
    
    ///
    
    public static String strLiteral(String in) {
    in = (in != null ? in : "");
    StringBuffer ret = new StringBuffer("'");

    for (int i = 0; i < in.length(); i++) {
      char c = in.charAt(i);
      if (c == '\'')
        ret.append("''");
      else
        ret.append(c);
    }
    ret.append('\'');

    return ret.toString();
  }

    /**
     * A method for replacing < > tags in string for web layout
     */
    public static String replaceTags(String in) {
        return replaceTags(in, false);
    }
    public static String replaceTags(String in, boolean inTextarea) {
    in = (in != null ? in : "");


    StringBuffer ret = new StringBuffer();

    for (int i = 0; i < in.length(); i++) {
      char c = in.charAt(i);
      if (c == '<')
        ret.append("&lt;");
      else if (c == '>')
        ret.append("&gt;");
      else if (c == '\n' && inTextarea==false)
        ret.append("<BR>");
      else
        ret.append(c);
    }
    String retString = ret.toString();
    if (inTextarea == false)
        retString=setAnchors(retString);

    return retString;
  }
  
    /**
    * Finds all urls in a given string and replaces them with HTML anchors.
    * If boolean newWindow==true then target will be a new window, else no.
    */
    public static String setAnchors(String s, boolean newWindow){
        
        StringBuffer buf = new StringBuffer();
        
        StringTokenizer st = new StringTokenizer(s, " \t\n\r\f", true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!isURL(token))
                buf.append(token);
            else{
                StringBuffer _buf = new StringBuffer("<a ");
                if (newWindow) _buf.append("target=\"_blank\" ");
                _buf.append("href=\"");
                _buf.append(token);
                _buf.append("\">");
                _buf.append(token);
                _buf.append("</a>");
                buf.append(_buf.toString());
            }
        }
        
        return buf.toString();
    }
  
    /**
    * Finds all urls in a given string and replaces them with HTML anchors
    * with target being a new window.
    */
    public static String setAnchors(String s){
        
        return setAnchors(s, true);
    }
    
    /**
    * Checks if the given string is a well-formed URL
    */
    public static boolean isURL(String s){
        try {
            URL url = new URL(s);
        }
        catch (MalformedURLException e){
            return false;
        }
        
        return true;
    }
    
    /**
    *
    */
    public static boolean implementsIF(Class c, String ifName){
        
        boolean f = false;
        Class[] ifs = c.getInterfaces();
        for (int i=0; ifs!=null && i<ifs.length; i++){
            Class ifClass = ifs[i];
            if (ifClass.getName().endsWith(ifName))
                return true;
        }
        
        return f;
    }
    
    /**
    * Converts HTML/XML escape sequences (a la &#147; or &amp;)
    * in the given to UNICODE.
    */
    public static String escapesToUnicode(String literal) {
        
        return literal;
        
        /*if (literal == null)
            return null;
        
        UnicodeEscapes unicodeEscapes = null;
        
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<literal.length(); i++){
            
            char c = literal.charAt(i);
            
            if (c=='&'){
                int j = literal.indexOf(";", i);
                if (j > i){
                    char cc = literal.charAt(i+1);
                    int decimal = -1;
                    if (cc=='#'){
                        // handle Unicode decimal escape
                        String sDecimal = literal.substring(i+2, j);
                        
                        try{
                            decimal = Integer.parseInt(sDecimal);
                        }
                        catch (Exception e){}
                    }
                    else{
                        // handle entity
                        String ent = literal.substring(i+1, j);
                        if (unicodeEscapes == null)
                            unicodeEscapes = new UnicodeEscapes();
                        decimal = unicodeEscapes.getDecimal(ent);
                    }
                    
                    if (decimal >= 0){
                        // if decimal was found, use the corresponding char. otherwise stick to c.
                        c = (char)decimal;
                        i = j;
                    }
                }
            }
            
            buf.append(c);
        }
        
        return buf.toString();*/
    }
    
    /**
    * main
    */
    public static void main(String[] args){
        
        String s = "kala http://www.neti.ee mees";
        System.out.println(setAnchors(s));
        
        /*try {
            System.out.println(digestHexDec("http://purl.org/dc/elements/1.1/subject", "md5"));
        }
        catch (GeneralSecurityException e){
            System.out.println(e.toString());
        }
        
        byte[] digest = new byte[1];
        String sMessage = "kalu";
        byte[] message = sMessage.getBytes();
        String sDigest = "";
        String sDigest2 = "";
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(message);
            MessageDigest tc1 = (MessageDigest)md.clone();
            digest = tc1.digest();
            sDigest2 = new String(digest, "US-ASCII");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<digest.length; i++){
            Byte byteWrapper = new Byte(digest[i]);
            int j = byteWrapper.intValue();
            buf.append(String.valueOf(j));
        }
        
        System.out.println(buf.toString());
        
        System.out.println(sDigest);
        System.out.println(sDigest2);
        System.out.println("bye");*/
    }
}
