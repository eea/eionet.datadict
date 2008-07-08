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

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import eionet.meta.Log;

/**
 * This is a class containing several useful utility methods.
 *
 * @author Jaanus Heinlaid
 */
public class Util {
	
	private static final int BUF_SIZE = 1024;
	private static Hashtable xmlEscapes = null;
	private static String[][] allowedFxvDatatypeConversions = {{"boolean", "string"},
		{"date", "string"},
		{"float", "string"},
		{"double", "string"},
		{"integer", "string"},
		{"integer", "float"},
		{"integer", "double"},
		{"float", "double"}
		};
    
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
        String month = String.valueOf(date.getMonth() + 1);
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
	 * A method for formatting the given timestamp into a String released_datasets.jsp.
	 */
     
	public static String releasedDate(long timestamp){
        
		Date date = new Date(timestamp);
		
		String year = String.valueOf(1900 + date.getYear());
		String month = String.valueOf(date.getMonth());
		String day = String.valueOf(date.getDate());
		day = (day.length() < 2) ? ("0" + day) : day;
		
		Hashtable months = new Hashtable();
		months.put("0", "January");
		months.put("1", "February");
		months.put("2", "March");
		months.put("3", "April");
		months.put("4", "May");
		months.put("5", "June");
		months.put("6", "July");
		months.put("7", "August");
		months.put("8", "September");
		months.put("9", "October");
		months.put("10", "November");
		months.put("11", "December");
		
		String time = day + " " + months.get(month) + " " + year;
		return time;
	}

	/**
	 * 
	 */
     
	public static String pdfDate(long timestamp){
        
		Date date = new Date(timestamp);
		
		String year = String.valueOf(1900 + date.getYear());
		String month = String.valueOf(date.getMonth() + 1);
		month = (month.length() < 2) ? ("0" + month) : month;
		String day = String.valueOf(date.getDate());
		day = (day.length() < 2) ? ("0" + day) : day;
		
		return day + "/" + month + "/" + year;
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
     * Calls replaceTags(in, false, false). See the documentation of that method.
     *  
     * @param in
     * @return
     */
    public static String replaceTags(String in){
        return replaceTags(in, false, false);
    }
    
    /**
     * Calls replaceTags(in, dontCreateHTMLAnchors, false). See the documentation of that method.
     * 
     * @param in
     * @param dontCreateHTMLAnchors
     * @return
     */
    public static String replaceTags(String in, boolean dontCreateHTMLAnchors){
    	return replaceTags(in, dontCreateHTMLAnchors, false);
    }

    /**
     * Replaces the following characters with their XML escape codes: ', ", <, >, \, &.
     * If an ampersand is found and it is the start of an escape sequence, the ampersand is not escaped.
     * 
     * By default, this method creates HTML anchors (<a href"...">...</a>) for URLs it finds in the string. This can be switched
     * off by setting dontCreateHTMLAnchors to true.
     * Also by default, this method converts discovered line breaks into HTML line breaks (<br>). This can be switched off by setting
     * dontCreateHTMLLineBreaks to true.
     * 
     * @param in
     * @param inTextarea
     * @return
     */
    public static String replaceTags(
    		String in, boolean dontCreateHTMLAnchors, boolean dontCreateHTMLLineBreaks){
    	
	    in = (in != null ? in : "");
	    
	    StringBuffer ret = new StringBuffer();
	    for (int i = 0; i < in.length(); i++) {
	      char c = in.charAt(i);
	      if (c == '<')             // less than
	        ret.append("&lt;");
	      else if (c == '>')        // greater than
	        ret.append("&gt;");
	      else if (c == '"')        // quotation
	          ret.append("&quot;");
	      else if (c == '\'')       // apostrophe
	    	  ret.append("&#039;");
	      else if (c == '\\')       // backslash
	          ret.append("&#092;");
	      else if (c == '&'){       // ampersand
	    	  boolean startsEscapeSequence = false;
	    	  int j = in.indexOf(';', i);
	    	  if (j>0){
	    		  String s = in.substring(i,j+1);
	    		  UnicodeEscapes unicodeEscapes = new UnicodeEscapes();
	    		  if (unicodeEscapes.isXHTMLEntity(s) || unicodeEscapes.isNumericHTMLEscapeCode(s))
	    			  startsEscapeSequence = true;
	    	  }
	    	  
	    	  if (startsEscapeSequence)
	    		  ret.append(c);
	    	  else
	    		  ret.append("&amp;");
	      }
	      else
	        ret.append(c);
	    }
	    
	    String retString = ret.toString();
	    if (dontCreateHTMLAnchors==false)
	    	retString=setAnchors(retString, true, 50);
	    
	    ret = new StringBuffer();
	    for (int i = 0; i < retString.length(); i++) {
	    	char c = retString.charAt(i);
	    	if (c == '\n' && dontCreateHTMLLineBreaks==false)
	    		ret.append("<br/>");
	    	else if (c == '\r' && i!=(retString.length()-1) && retString.charAt(i+1)=='\n' && dontCreateHTMLLineBreaks==false){
	    		ret.append("<br/>");
				i = i + 1;
	    	}
	    	else
	    		ret.append(c);
	    }

	    return ret.toString();
	}
	
    /**
     * A method for replacing substrings in string
     */
    public static String Replace(String str, String oldStr, String replace) {
        str = (str != null ? str : "");

        StringBuffer buf = new StringBuffer();
        int found = 0;
        int last=0;

        while ((found = str.indexOf(oldStr, last)) >= 0) {
            buf.append(str.substring(last, found));
            buf.append(replace);
            last = found+oldStr.length();
        }
        buf.append(str.substring(last));
        return buf.toString();
	}

	/**
	* Finds all urls in a given string and replaces them with HTML anchors.
	* If boolean newWindow==true then target will be a new window, else no.
	* If boolean cutLink > 0 then cut the displayed link length at cutLink.
	*/
	public static String setAnchors(String s, boolean newWindow, int cutLink){

		StringBuffer buf = new StringBuffer();
        
		StringTokenizer st = new StringTokenizer(s, " \t\n\r\f", true);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			token = processForLink(token, newWindow, cutLink);
			buf.append(token);
		}
        
		return buf.toString();
	}
      
    /**
    * Finds all urls in a given string and replaces them with HTML anchors.
    * If boolean newWindow==true then target will be a new window, else no.
    */
    public static String setAnchors(String s, boolean newWindow){
        
        return setAnchors(s, newWindow, 9999999);
    }
  
    /**
    * Finds all urls in a given string and replaces them with HTML anchors
    * with target being a new window.
    */
    public static String setAnchors(String s){
        
        return setAnchors(s, true);
    }
    
    /**
     * 
     * @param s
     * @return
     */
    public static String processForLink(String in, boolean newWindow, int cutLink){
    	
    	if (in==null || in.trim().length()==0)
    		return in;
    	
    	HashSet urlSchemes = new HashSet();
    	urlSchemes.add("http://");
    	urlSchemes.add("https://");
    	urlSchemes.add("ftp://");
    	urlSchemes.add("mailto://");
    	urlSchemes.add("ldap://");
    	urlSchemes.add("file://");
    	
    	int beginIndex = -1;
    	Iterator iter = urlSchemes.iterator();
    	while (iter.hasNext() && beginIndex<0)
    		beginIndex = in.indexOf((String)iter.next());
    	
    	if (beginIndex<0)
    		return in;
    	
    	int endIndex = -1;
    	String s = null;
    	for (endIndex=in.length(); endIndex>beginIndex; endIndex--){
    		s = in.substring(beginIndex, endIndex);
    		if (isURI(s))
    			break;
    	}
    	
    	if (s==null)
    		return in;
    	
    	HashSet endChars = new HashSet();
    	endChars.add(new Character('!'));
    	endChars.add(new Character('\''));
    	endChars.add(new Character('('));
    	endChars.add(new Character(')'));
    	endChars.add(new Character('.'));
    	endChars.add(new Character(':'));
    	endChars.add(new Character(';'));
    	
    	for (endIndex=endIndex-1; endIndex > beginIndex; endIndex--){
    		char c = in.charAt(endIndex);
    		if (!endChars.contains(new Character(c)))
    			break;
    	}
    	
    	StringBuffer buf = new StringBuffer(in.substring(0, beginIndex));
    	
    	String link = in.substring(beginIndex, endIndex+1);
    	StringBuffer _buf = new StringBuffer("<a ");
		_buf.append("href=\"");
		_buf.append(link);
		_buf.append("\">");
		
		if (cutLink<link.length())
			_buf.append(link.substring(0, cutLink)).append("...");
		else
			_buf.append(link);
			
		_buf.append("</a>");
		buf.append(_buf.toString());
    	
    	buf.append(in.substring(endIndex+1));
    	return buf.toString();
    }
    
    /**
    * Checks if the given string is a well-formed URI.
    */
    public static boolean isURI(String s){
        try {
            URI uri = new URI(s);
        }
        catch (URISyntaxException e){
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
    
    /*
     * Return's a throwable's stack trace in a string 
     */
    public static String getStack(Throwable t){
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		t.printStackTrace(new PrintStream(bytesOut));
		return bytesOut.toString();
    }

	/*
	 * Return's indicator-image name according to given status 
	 */
	public static String getStatusImage(String status){
		
		if (status==null) status = "Incomplete";
		
		if (status.equals("Incomplete"))
			return "dd_status_1.gif";
		else if (status.equals("Candidate"))
			return "dd_status_2.gif";
		else if (status.equals("Recorded"))
			return "dd_status_3.gif";
		else if (status.equals("Qualified"))
			return "dd_status_4.gif";
		else if (status.equals("Released"))
			return "dd_status_5.gif";
		else
			return "dd_status_1.gif";
	}

	/*
	 * Return's a sequence of radics illustrating the given status 
	 */
	public static String getStatusRadics(String status){
		
		if (status==null) status = "Incomplete";
		
		if (status.equals("Incomplete"))
			return "&radic;";
		else if (status.equals("Candidate"))
			return "&radic;&radic;";
		else if (status.equals("Recorded"))
			return "&radic;&radic;&radic;";
		else if (status.equals("Qualified"))
			return "&radic;&radic;&radic;&radic;";
		else if (status.equals("Released"))
			return "&radic;&radic;&radic;&radic;&radic;";
		else
			return "&radic;";
	}

	/*
	 * Return's a sortable string of the given status, taking into account
	 * the business-logical order of statuses   
	 */
	public static String getStatusSortString(String status){
		
		if (status==null) status = "Incomplete";
		
		if (status.equals("Incomplete"))
			return "1";
		else if (status.equals("Candidate"))
			return "2";
		else if (status.equals("Recorded"))
			return "3";
		else if (status.equals("Qualified"))
			return "4";
		else if (status.equals("Released"))
			return "5";
		else
			return "1";
	}

	/*
	 * 
	 */
	public static String getIcon(String path){
		
		String s = path==null ? null : path.toLowerCase();
		
		if (s==null)
			return "file.png";
		else if (s.endsWith(".pdf"))
			return "pdf.png";
		else if (s.endsWith(".doc"))
			return "doc.png";
		else if (s.endsWith(".rtf"))
			return "rtf.png";
		else if (s.endsWith(".xls"))
			return "xls.png";
		else if (s.endsWith(".ppt"))
			return "ppt.png";
		else if (s.endsWith(".txt"))
			return "txt.png";
		else if (s.endsWith(".zip"))
			return "zip.png";
		else if (s.endsWith(".htm"))
			return "htm.png";
		else if (s.endsWith(".html"))
			return "html.png";
		else if (s.endsWith(".xml"))
			return "xml.png";
		else if (s.endsWith(".xsd"))
			return "xsd.png";
		else if (s.endsWith(".mdb"))
			return "mdb.png";
		else if (s.endsWith(".gif"))
			return "gif.png";
		else if (s.endsWith(".jpeg"))
			return "jpeg.png";
		else if (s.endsWith(".jpg"))
			return "jpg.png";
		else if (s.endsWith(".png"))
			return "png.png";
		else if (s.endsWith(".rar"))
			return "rar.png";
		else if (s.endsWith(".tar"))
			return "tar.png";
		else if (s.endsWith(".tgz"))
			return "tgz.png";
		else if (s.endsWith(".xsl"))
			return "xsl.png";

		else
			return "file.png";
	}

	/**
	 * Method used in JSP to determine weather the row with a given index is
	 * odd or even. Returns a String used by JSP to set the style correspondingly
	 * and with as little code as possible.
	 */
	public static String isOdd(int displayed){
		String isOdd = (displayed % 2 != 0) ? "odd" : "even";
		return isOdd;
	}

	/**
	 *  
	 */	
	public static String getUrlContent(String url){

		int i;
		byte[] buf = new byte[1024];		
		InputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try{
			URL _url = new URL(url);
			HttpURLConnection httpConn = (HttpURLConnection)_url.openConnection();
					
			in = _url.openStream();
			while ((i=in.read(buf, 0, buf.length)) != -1){
				out.write(buf, 0, i);
			}
			out.flush();
		}
		catch (IOException e){
			return e.toString().trim();
		}
		finally{
			try{
				if (in!=null) in.close();
				if (out!=null) out.close();
			}
			catch (IOException e){}
		}
		
		return out.toString().trim();
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
    
	public static void write(InputStream in, OutputStream out) throws IOException{
		
		int i = 0;
		byte[] buf = new byte[BUF_SIZE];
		
		try{
			while ((i=in.read(buf, 0, buf.length)) != -1){
				out.write(buf, 0, i);
			}
		}
		finally{
			if (in!=null) in.close();
			out.close();
		}
	}
	
	public static String htmlAttr(String name, String value){
		StringBuffer buf = new StringBuffer();
		if (value!=null) buf.append(name).append("=\"").append(value).append("\"");
		return buf.toString();
	}
	
	public static String escapeXML(String text){
		
		if (text==null) return null;
		if (text.length()==0) return text;
		
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<text.length(); i++)
			buf.append(escapeXML(i, text));
		
		return buf.toString();
	}
	
	public static String escapeXML(int pos, String text){
		
		if (xmlEscapes==null) setXmlEscapes();
		Character c = new Character(text.charAt(pos));
		for (Enumeration e=xmlEscapes.elements(); e.hasMoreElements(); ){
			String esc = (String)e.nextElement();
			if (pos+esc.length() < text.length()){
				String sub = text.substring(pos, pos+esc.length());
				if (sub.equals(esc))
					return c.toString();
			}
		}
		
		if (pos+1 < text.length() && text.charAt(pos+1)=='#'){
			int semicolonPos = text.indexOf(';', pos+1);
			if (semicolonPos!=-1){
				String sub = text.substring(pos+2, semicolonPos);
				if (sub!=null){
					try{
						// if the string between # and ; is a number then return true,
						// because it is most probably an escape sequence
						if (Integer.parseInt(sub)>=0)
							return c.toString();
					}
					catch (NumberFormatException nfe){}
				}
			}
		}
		
		String esc = (String)xmlEscapes.get(c);
		if (esc!=null)
			return esc;
		else
			return c.toString();
	}
	
	private static void setXmlEscapes(){
		xmlEscapes = new Hashtable();
		xmlEscapes.put(new Character('&'), "&amp;");
		xmlEscapes.put(new Character('<'), "&lt;");
		xmlEscapes.put(new Character('>'), "&gt;");
		xmlEscapes.put(new Character('"'), "&quot;");
		xmlEscapes.put(new Character('\''), "&apos;");
	}

	/*
	 * Returns true if the given attributes should not be displayed for the elements
	 * of the given datatype. Based on XMLSchema specs.
	 */
	public static boolean skipAttributeByDatatype(String attrShortName, String datatype){
		
		return (attrShortName==null || datatype==null) ? false : IrrelevantAttributes.getInstance().isIrrelevant(datatype, attrShortName);
	}

	/*
	 * 
	 */
	public static String getObligationID(String obligDetailsUrl){
		
		if (obligDetailsUrl==null || obligDetailsUrl.length()==0) return null;
		
		String obligationID = "";
		String s = new String("id=");
		int j = obligDetailsUrl.indexOf(s);
		if (j<0) return null;
		int k = obligDetailsUrl.indexOf("&", j);
		if (k<0)
			obligationID = obligDetailsUrl.substring(j+s.length());
		else
			obligationID = obligDetailsUrl.substring(j+s.length(), k);
		
		try{
			int oid = Integer.parseInt(obligationID);
		}
		catch (NumberFormatException nfe){
			return null;
		}
		
		return obligationID;
	}
	
	/*
	 * 
	 */
	public static void forward2errorpage(
				HttpServletRequest request, HttpServletResponse response, Throwable t, String backURL) throws ServletException, IOException{

		String msg = t.getMessage();
							
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
		t.printStackTrace(new PrintStream(bytesOut));
		
		String trace = bytesOut.toString(response.getCharacterEncoding());
						
		request.setAttribute("DD_ERR_MSG", msg);
		request.setAttribute("DD_ERR_TRC", trace);
		request.setAttribute("DD_ERR_BACK_LINK", backURL);
						
		request.getRequestDispatcher("error.jsp").forward(request, response);
	}
	
	/**
	 * 
	 *
	 */
	public static String getServletPathWithQueryString(HttpServletRequest request){

		StringBuffer result = new StringBuffer();
		String servletPath = request.getServletPath();
		if (servletPath!=null && servletPath.length()>0){
			if (servletPath.startsWith("/") && servletPath.length()>1)
				result.append(servletPath.substring(1));
			String queryString = request.getQueryString();
			if (queryString!=null && queryString.length()>0)
				result.append("?").append(queryString);
		}
		
		return result.toString();
	}
	
	/**
	 * 
	 * @param str
	 * @param token
	 * @return
	 */
	public static HashSet tokens2hash(String str, String delim){
		HashSet result = new HashSet();
		if (str!=null && delim!=null){
			StringTokenizer tokenizer = new StringTokenizer(str, delim);
			while (tokenizer.hasMoreTokens()){
				String token = tokenizer.nextToken();
				if (token!=null && token.length()>0)
					result.add(token);
			}
		}
		
		return result;
		
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getBaseHref(HttpServletRequest request){
		
		String protocol = request.getProtocol().toLowerCase();
		int i = protocol.indexOf('/');
		if (i>=0)
			protocol = protocol.substring(0,i);
			
		StringBuffer buf = new StringBuffer(protocol);
		buf.append("://").append(request.getServerName());
		if (request.getServerPort()>0)
			buf.append(":").append(request.getServerPort());
		if (request.getContextPath()!=null)
			buf.append(request.getContextPath());
		if (buf.toString().endsWith("/")==false)
			buf.append("/");
		
		return buf.toString();
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean isAllowedFxvDatatypeConversion(String from, String to){
		
		if (allowedFxvDatatypeConversions!=null && allowedFxvDatatypeConversions.length>0){
			int FROM = 0;
			int TO = 1;
			for (int i=0; i<allowedFxvDatatypeConversions.length; i++){
				String[] pair = allowedFxvDatatypeConversions[i];
				if (pair[FROM].equals(from) && pair[TO].equals(to))
					return true;
			}
		}
		
		return false;
	}
	
    /**
    * main
    */
    public static void main(String[] args){
    }
}
