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
 * The Original Code is "EINRC-4 / Meta Project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2013 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Jaanus Heinlaid (TietoEnator)
 */

package eionet.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import eionet.meta.DDRuntimeException;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.VocabularyFolder;

//import eionet.meta.Log;

/**
 * This is a class containing several useful utility methods.
 *
 * @author Jaanus Heinlaid
 */
public final class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    /** Size of buffer for the write() method. */
    private static final int BUF_SIZE = 1024;
    /** Cache of reserved characters in XML to be escaped. */
    private static Hashtable xmlEscapes = null;
    private static String[][] allowedFxvDatatypeConversions = {{"boolean", "string"}, {"date", "string"}, {"float", "string"},
            {"double", "string"}, {"integer", "string"}, {"integer", "float"}, {"integer", "double"}, {"integer", "decimal"},
            {"float", "double"}, {"float", "decimal"}, {"double", "decimal"}, {"decimal", "string"}};

    /** */
    private static final SimpleDateFormat hhmmssFormat = new SimpleDateFormat("HH:mm:ss");

    /** */
    private static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** */
    private static String expiresDateString;

    /**
     * To prevent direct initialization.
     */
    private Util() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the given string is null or its length is 0.
     *
     * @param str
     *            The given string.
     * @return true if the given string is null or its length is 0.
     */
    public static boolean isEmpty(String str) {

        return str == null || str.length() == 0;
    }

    /**
     * Checks if given Enum value equals with one of the given values.
     *
     * @param value
     * @param values
     * @return
     */
    public static <T extends Enum<?>> boolean enumEquals(T value, T... values) {
        for (T v : values) {
            if (v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A method for calculating and formatting the current date and time into a String for a log.
     */

    public static String logTime() {

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
     * A method for formatting the given timestamp into a String for history.
     *
     * @param timestamp Milliseconds since 1 January 1970.
     * @return formatted time as string in the form 2015/04/18 12:43.
     */
    public static String historyDate(long timestamp) {

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
     *
     * @param timestamp Milliseconds since 1 January 1970.
     * @return
     */
    public static String releasedDateShort(long timestamp) {
        return releasedDate(timestamp, true);
    }

    /**
     * A method for formatting the given timestamp into a String released_datasets.jsp.
     *
     * @param timestamp Milliseconds since 1 January 1970.
     */
    public static String releasedDate(long timestamp) {
        return releasedDate(timestamp, false);
    }

    /**
     * A method for formatting the given timestamp into a String released_datasets.jsp.
     *
     * @param timestamp Milliseconds since 1 January 1970.
     */
    private static String releasedDate(long timestamp, boolean shortMonth) {

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

        String time = day + " " + (shortMonth ? months.get(month).toString().substring(0, 3) : months.get(month)) + " " + year;
        return time;
    }

    /**
     * Formats a timestamp to the presentation used on web pages.
     *
     * @param timestamp Milliseconds since 1 January 1970.
     * @return
     */
    public static String hoursMinutesSeconds(long timestamp) {

        return hhmmssFormat.format(new Date(timestamp));
    }

    /**
     *
     * @param timestamp Milliseconds since 1 January 1970.
     */
    public static String pdfDate(long timestamp) {

        Date date = new Date(timestamp);

        String year = String.valueOf(1900 + date.getYear());
        String month = String.valueOf(date.getMonth() + 1);
        month = (month.length() < 2) ? ("0" + month) : month;
        String day = String.valueOf(date.getDate());
        day = (day.length() < 2) ? ("0" + day) : day;

        return day + "/" + month + "/" + year;
    }

    /**
     * A method for calculating time difference in MILLISECONDS, between a date-time specified in input parameters and the current
     * date-time. <BR>
     * This should be useful for calculating sleep time for code that has a certain schedule for execution.
     *
     * @param hour
     *            An integer from 0 to 23. If less than 0 or more than 23, then the closest next hour to current hour is taken.
     * @param date
     *            An integer from 1 to 31. If less than 1 or more than 31, then the closest next date to current date is taken.
     * @param month
     *            An integer from Calendar.JANUARY to Calendar.DECEMBER. If out of those bounds, the closest next month to current
     *            month is taken.
     * @param wday
     *            An integer from 1 to 7. If out of those bounds, the closest next weekday to weekday month is taken.
     * @param zone
     *            A String specifying the time-zone in which the calculations should be done. Please see Java documentation an
     *            allowable time-zones and formats.
     * @return Time difference in milliseconds.
     */
    public static long timeDiff(int hour, int date, int month, int wday, String zone) {

        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(zone));
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.setFirstDayOfWeek(Calendar.MONDAY);

        /*
         * here we force the hour to be one of the defualts if (hour < 0) hour = 0; if (hour > 23) hour = 23;
         */
        int cur_hour = cal.get(Calendar.HOUR);

        if (cal.get(Calendar.AM_PM) == Calendar.PM) {
            cur_hour = 12 + cur_hour;
        }

        // here we assume that every full hour is accepted
        /*
         * if (hour < 0 || hour > 23) { hour = cur_hour>=23 ? 0 : cur_hour + 1; }
         */

        if (wday >= 1 && wday <= 7) {

            int cur_wday = cal.get(Calendar.DAY_OF_WEEK);
            if (hour < 0 || hour > 23) {
                if (cur_wday != wday) {
                    hour = 0;
                } else {
                    hour = cur_hour >= 23 ? 0 : cur_hour + 1;
                }
            }

            int amount = wday - cur_wday;
            if (amount < 0) {
                amount = 7 + amount;
            }
            if (amount == 0 && cur_hour >= hour) {
                amount = 7;
            }
            cal.add(Calendar.DAY_OF_WEEK, amount);
        } else if (month >= Calendar.JANUARY && month <= Calendar.DECEMBER) { // do something about when every date is accepted
            if (date < 1) {
                date = 1;
            }
            if (date > 31) {
                date = 31;
            }
            int cur_month = cal.get(Calendar.MONTH);
            int amount = month - cur_month;
            if (amount < 0) {
                amount = 12 + amount;
            }
            if (amount == 0) {
                if (cal.get(Calendar.DATE) > date) {
                    amount = 12;
                } else if (cal.get(Calendar.DATE) == date) {
                    if (cur_hour >= hour) {
                        amount = 12;
                    }
                }
            }
            // cal.set(Calendar.DATE, date);
            cal.add(Calendar.MONTH, amount);
            if (date > cal.getActualMaximum(Calendar.DATE)) {
                date = cal.getActualMaximum(Calendar.DATE);
            }
            cal.set(Calendar.DATE, date);
        } else if (date >= 1 && date <= 31) {
            int cur_date = cal.get(Calendar.DATE);
            if (cur_date > date) {
                cal.add(Calendar.MONTH, 1);
            } else if (cur_date == date) {
                if (cur_hour >= hour) {
                    cal.add(Calendar.MONTH, 1);
                }
            }
            cal.set(Calendar.DATE, date);
        } else {
            if (hour < 0 || hour > 23) {
                hour = cur_hour >= 23 ? 0 : cur_hour + 1;
            }
            if (cur_hour >= hour) {
                cal.add(Calendar.DATE, 1);
            }
        }

        if (hour >= 12) {
            cal.set(Calendar.HOUR, hour - 12);
            cal.set(Calendar.AM_PM, Calendar.PM);
        } else {
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.AM_PM, Calendar.AM);
        }

        Date nextDate = cal.getTime();
        Date currDate = new Date();

        long nextTime = cal.getTime().getTime();
        long currTime = (new Date()).getTime();

        return nextTime - currTime;
    }

    /**
     * A method for counting occurences of a substring in a string.
     * NOTE: Goes into an infinite loop.
     */
    public static int countSubString(String str, String substr) {
        int count = 0;
        while (str.indexOf(substr) != -1) {
            count++;
        }
        return count;
    }

    /**
     * A method for creating a unique digest of a String message.
     *
     * @param src
     *            String to be digested.
     * @param algorithm
     *            Digesting algorithm (please see Java documentation for allowable values).
     * @return A unique String-typed digest of the input message.
     */
    public static String digest(String src, String algorithm) throws GeneralSecurityException {

        byte[] srcBytes = src.getBytes();
        byte[] dstBytes = new byte[16];

        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(srcBytes);
        dstBytes = md.digest();
        md.reset();

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < dstBytes.length; i++) {
            Byte byteWrapper = new Byte(dstBytes[i]);
            buf.append(String.valueOf(byteWrapper.intValue()));
        }

        return buf.toString();
    }

    /**
     * A method for creating a unique Hexa-Decimal digest of a String message.
     *
     * @param src
     *            String to be digested.
     * @param algorithm
     *            Digesting algorithm (please see Java documentation for allowable values).
     * @return A unique String-typed Hexa-Decimal digest of the input message.
     */
    public static String digestHexDec(String src, String algorithm) throws GeneralSecurityException {

        byte[] srcBytes = src.getBytes();
        byte[] dstBytes = new byte[16];

        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(srcBytes);
        dstBytes = md.digest();
        md.reset();

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < dstBytes.length; i++) {
            Byte byteWrapper = new Byte(dstBytes[i]);
            int k = byteWrapper.intValue();
            String s = Integer.toHexString(byteWrapper.intValue());
            if (s.length() == 1) {
                s = "0" + s;
            }
            buf.append(s.substring(s.length() - 2));
        }

        return buf.toString();
    }

    /**
     *
     * @param str The string to create an MD5 hash of
     * @return the MD5 hash as hexadecimal string
     */
    public static String md5(String str) {

        try {
            return digestHexDec(str, "MD5");
        } catch (GeneralSecurityException e) {
            throw new DDRuntimeException("Failed to generate MD5 hash", e);
        }
    }

    /**
     * Returns the result of {@link #processForDisplay(String, boolean, boolean)} with the given input String, setting both booleans
     * to false.
     *
     * @param in
     * @return
     */
    public static String processForDisplay(String in) {
        return processForDisplay(in, false, false);
    }

    /**
     * Returns the result of {@link #processForDisplay(String, boolean, boolean)} with the given input String and first boolean,
     * setting the last boolean to false.
     *
     * @param in
     * @param dontCreateHTMLAnchors
     * @return
     */
    public static String processForDisplay(String in, boolean dontCreateHTMLAnchors) {
        return processForDisplay(in, dontCreateHTMLAnchors, false);
    }

    /**
     * First replaces the given input string with escaped Xml. Then, if the 1st boolean input is
     * false, replaces all occurrences of URLs in the string with HTML links (i.e. anchors) like (&lt;a href"..."&gt;...&lt;/a&gt;). Finally, if
     * the 2nd boolean input is false, replaces all occurrences of Java string line breaks ('\n' and '\r\n') with HTML line breaks
     * like &lt;br/&gt;
     * .
     *
     * @param in
     * @param inTextarea
     * @return
     */
    public static String processForDisplay(String in, boolean dontCreateHTMLAnchors, boolean dontCreateHTMLLineBreaks) {

        if (StringUtils.isBlank(in)) {
            return in;
        }

        // first, escape for XML
        String result = StringEscapeUtils.escapeXml(in);

        // special case: &apos; is commonly used, but not actually legal, so replacing this with &#039;
        result = StringUtils.replace(result, "&apos;", "&#039;");

        // if URLs must be replaced with HTML links (i.e. anchors) then do so
        if (dontCreateHTMLAnchors == false) {
            result = setAnchors(result, true, 50);
        }

        // if requested so, replace all occurrences of '\n' and '\r\n' with HTML line breaks like <br/>
        if (dontCreateHTMLLineBreaks == false) {
            result = StringUtils.replace(result, "\r\n", "<br/>");
            result = StringUtils.replace(result, "\n", "<br/>");
        }

        return result;
    }

    /**
     * Finds all urls in a given string and replaces them with HTML anchors. If boolean newWindow==true then target will be a new
     * window, else no. If boolean cutLink &gt; 0 then cut the displayed link length at cutLink.
     */
    public static String setAnchors(String s, boolean newWindow, int cutLink) {

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
     * Finds all urls in a given string and replaces them with HTML anchors. If boolean newWindow==true then target will be a new
     * window, else no.
     */
    public static String setAnchors(String s, boolean newWindow) {

        return setAnchors(s, newWindow, 9999999);
    }

    /**
     * Finds all urls in a given string and replaces them with HTML anchors with target being a new window.
     */
    public static String setAnchors(String s) {

        return setAnchors(s, true);
    }

    /**
     * Finds all urls in a given string and replaces them with HTML anchors with target being a new window.
     *
     * @param in
     *            - the text to scan in plain text.
     * @param newWindow
     *            - whether to launch links in a new window.
     * @param cutLink
     *            - can shorten the link text in the output HTML.
     * @return The modified text as HTML
     */
    public static String processForLink(String in, boolean newWindow, int cutLink) {

        if (in == null || in.trim().length() == 0) {
            return in;
        }

        HashSet urlSchemes = new HashSet();
        urlSchemes.add("http://");
        urlSchemes.add("https://");
        urlSchemes.add("ftp://");
        urlSchemes.add("mailto://");
        urlSchemes.add("ldap://");
        urlSchemes.add("file://");

        int beginIndex = -1;
        Iterator iter = urlSchemes.iterator();
        while (iter.hasNext() && beginIndex < 0) {
            beginIndex = in.indexOf((String) iter.next());
        }

        if (beginIndex < 0) {
            return in;
        }

        int endIndex = -1;
        String s = null;
        for (endIndex = in.length(); endIndex > beginIndex; endIndex--) {
            s = in.substring(beginIndex, endIndex);
            if (isURI(s)) {
                break;
            }
        }

        if (s == null) {
            return in;
        }

        HashSet endChars = new HashSet();
        endChars.add(new Character('!'));
        endChars.add(new Character('\''));
        endChars.add(new Character('('));
        endChars.add(new Character(')'));
        endChars.add(new Character('.'));
        endChars.add(new Character(':'));
        endChars.add(new Character(';'));

        for (endIndex = endIndex - 1; endIndex > beginIndex; endIndex--) {
            char c = in.charAt(endIndex);
            if (!endChars.contains(new Character(c))) {
                break;
            }
        }

        StringBuffer buf = new StringBuffer(in.substring(0, beginIndex));

        String link = in.substring(beginIndex, endIndex + 1);
        StringBuffer _buf = new StringBuffer("<a ");
        _buf.append("href=\"");
        _buf.append(link);
        _buf.append("\">");

        if (cutLink < link.length()) {
            _buf.append(link.substring(0, cutLink)).append("...");
        } else {
            _buf.append(link);
        }

        _buf.append("</a>");
        buf.append(_buf.toString());

        buf.append(in.substring(endIndex + 1));
        return buf.toString();
    }

    /**
     * Checks if the given string is a well-formed URI.
     */
    public static boolean isURI(String s) {
        try {
            URI uri = new URI(s);
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the given string is a well-formed URL.
     */
    public static boolean isURL(String s) {
        return UrlValidator.getInstance().isValid(s);
    }

    /**
     * Checks if a class implements the interface given as second argument.
     *
     * @param c - class as an object.
     * @param ifName - interface name as string.
     */
    public static boolean implementsIF(Class c, String ifName) {

        boolean f = false;
        Class[] ifs = c.getInterfaces();
        for (int i = 0; ifs != null && i < ifs.length; i++) {
            Class ifClass = ifs[i];
            if (ifClass.getName().endsWith(ifName)) {
                return true;
            }
        }

        return f;
    }

    /*
     * Return's a throwable's stack trace in a string.
     */
    public static String getStack(Throwable t) {
        return ExceptionUtils.getFullStackTrace(t);
    }

    /*
     * Return's indicator-image name according to given status.
     */
    public static String getStatusImage(String status) {

        if (status == null) {
            status = "Incomplete";
        }

        if (status.equals("Incomplete")) {
            return "dd_status_1.gif";
        } else if (status.equals("Candidate")) {
            return "dd_status_2.gif";
        } else if (status.equals("Recorded")) {
            return "dd_status_3.gif";
        } else if (status.equals("Qualified")) {
            return "dd_status_4.gif";
        } else if (status.equals("Released")) {
            return "dd_status_5.gif";
        } else {
            return "dd_status_1.gif";
        }
    }

    /*
     * Return's a sequence of radics illustrating the given status
     */
    public static String getStatusRadics(String status) {

        if (status == null) {
            status = "Incomplete";
        }

        if (status.equals("Incomplete")) {
            return "&radic;";
        } else if (status.equals("Candidate")) {
            return "&radic;&radic;";
        } else if (status.equals("Recorded")) {
            return "&radic;&radic;&radic;";
        } else if (status.equals("Qualified")) {
            return "&radic;&radic;&radic;&radic;";
        } else if (status.equals("Released")) {
            return "&radic;&radic;&radic;&radic;&radic;";
        } else {
            return "&radic;";
        }
    }

    /*
     * Return's a sortable string of the given status, taking into account the business-logical order of statuses
     */
    public static String getStatusSortString(String status) {

        if (status == null) {
            status = "Incomplete";
        }

        if (status.equals("Incomplete")) {
            return "1";
        } else if (status.equals("Candidate")) {
            return "2";
        } else if (status.equals("Recorded")) {
            return "3";
        } else if (status.equals("Qualified")) {
            return "4";
        } else if (status.equals("Released")) {
            return "5";
        } else if (status.equals("Retired")) {
            return "0";
        } else if (status.equals("Superseded")) {
            return "-1";
        }
        else {
            return "1";
        }
    }

    /*
     *
     */
    public static String getIcon(String path) {

        String s = path == null ? null : path.toLowerCase();

        if (s == null) {
            return "file.png";
        } else if (s.endsWith(".pdf")) {
            return "pdf.png";
        } else if (s.endsWith(".doc")) {
            return "doc.png";
        } else if (s.endsWith(".rtf")) {
            return "rtf.png";
        } else if (s.endsWith(".xls")) {
            return "xls.png";
        } else if (s.endsWith(".ppt")) {
            return "ppt.png";
        } else if (s.endsWith(".txt")) {
            return "txt.png";
        } else if (s.endsWith(".zip")) {
            return "zip.png";
        } else if (s.endsWith(".htm")) {
            return "htm.png";
        } else if (s.endsWith(".html")) {
            return "html.png";
        } else if (s.endsWith(".xml")) {
            return "xml.png";
        } else if (s.endsWith(".xsd")) {
            return "xsd.png";
        } else if (s.endsWith(".mdb")) {
            return "mdb.png";
        } else if (s.endsWith(".gif")) {
            return "gif.png";
        } else if (s.endsWith(".jpeg")) {
            return "jpeg.png";
        } else if (s.endsWith(".jpg")) {
            return "jpg.png";
        } else if (s.endsWith(".png")) {
            return "png.png";
        } else if (s.endsWith(".rar")) {
            return "rar.png";
        } else if (s.endsWith(".tar")) {
            return "tar.png";
        } else if (s.endsWith(".tgz")) {
            return "tgz.png";
        } else if (s.endsWith(".xsl")) {
            return "xsl.png";
        } else {
            return "file.png";
        }
    }

    /**
     * Method used in JSP to determine weather the row with a given index is odd or even.
     *
     * @param displayed - row number.
     * @return a String used by JSP to set the style correspondingly and with as little code as possible.
     */
    public static String isOdd(int displayed) {
        String isOdd = (displayed % 2 != 0) ? "odd" : "even";
        return isOdd;
    }

    /**
     * Opens a URL and fetches the content. If there is an error, then the
     * exception message is returned.
     *
     * @param url - the URL
     * @return the content
     */
    public static String getUrlContent(String url) {

        int i;
        byte[] buf = new byte[1024];
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            URL _url = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) _url.openConnection();

            in = _url.openStream();
            while ((i = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, i);
            }
            out.flush();
        } catch (IOException e) {
            return e.toString().trim();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }

        return out.toString().trim();
    }

    /**
     * Converts HTML/XML escape sequences (a la &#147; or &amp;) in the given to UNICODE.
     */
    public static String escapesToUnicode(String literal) {

        return literal;

        /*
         * if (literal == null) return null; UnicodeEscapes unicodeEscapes = null; StringBuffer buf = new StringBuffer(); for (int i
         * = 0; i < literal.length(); i++) { char c = literal.charAt(i); if (c=='&') { int j = literal.indexOf(";", i); if (j > i) {
         * char cc = literal.charAt(i + 1); int decimal = -1; if (cc=='#') { // handle Unicode decimal escape String sDecimal =
         * literal.substring(i + 2, j); try { decimal = Integer.parseInt(sDecimal); } catch (Exception e) {} } else { // handle
         * entity String ent = literal.substring(i + 1, j); if (unicodeEscapes == null) unicodeEscapes = new UnicodeEscapes();
         * decimal = unicodeEscapes.getDecimal(ent); } if (decimal >= 0) { // if decimal was found, use the corresponding char.
         * otherwise stick to c. c = (char) decimal; i = j; } } } buf.append(c); } return buf.toString();
         */
    }

    /**
     * Copy the content from an open input stream to an open output stream and closes both.
     *
     * @param in - the input stream.
     * @param out - the output stream.
     * @throws IOException if something went wrong.
     */
    public static void write(InputStream in, OutputStream out) throws IOException {

        int i = 0;
        byte[] buf = new byte[BUF_SIZE];

        try {
            while ((i = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, i);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            out.close();
        }
    }

    /**
     * Create an HTML attribute of its arguments in the form of color="blue".
     * Value can not contain quote (") or less-than (&lt;) as no XML escaping is done.
     *
     * @param name = the name of the attribute.
     * @param value = the value.
     * @return name="value" as string
     */
    public static String htmlAttr(String name, String value) {
        StringBuffer buf = new StringBuffer();
        if (value != null) {
            buf.append(name).append("=\"").append(value).append("\"");
        }
        return buf.toString();
    }

    /**
     * XML-escape a string. I.e &lt; becomes &amp;lt;
     * 
     * @param text The string
     * @return the escaped string.
     */
    public static String escapeXML(String text) {

        if (text == null) {
            return null;
        }
        if (text.length() == 0) {
            return text;
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            buf.append(escapeXML(i, text));
        }

        return buf.toString();
    }

    /**
     * XML-escape one character in a string. Does not do it if it looks like
     * the string is already escaped. Also doesn't escape if the content is
     * a numeric entity.
     *
     * @param pos the position of the character
     * @param text the string.
     * @return the escaped character.
     */
    public static String escapeXML(int pos, String text) {

        if (xmlEscapes == null) {
            setXmlEscapes();
        }
        Character c = new Character(text.charAt(pos));
        for (Enumeration e = xmlEscapes.elements(); e.hasMoreElements();) {
            String esc = (String) e.nextElement();
            if (pos + esc.length() < text.length()) {
                String sub = text.substring(pos, pos + esc.length());
                if (sub.equals(esc)) {
                    return c.toString();
                }
            }
        }

        if (pos + 1 < text.length() && text.charAt(pos + 1) == '#') {
            int semicolonPos = text.indexOf(';', pos + 1);
            if (semicolonPos != -1) {
                String sub = text.substring(pos + 2, semicolonPos);
                if (sub != null) {
                    try {
                        // if the string between # and ; is a number then return true,
                        // because it is most probably an escape sequence
                        if (Integer.parseInt(sub) >= 0) {
                            return c.toString();
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
        }

        String esc = (String) xmlEscapes.get(c);
        if (esc != null) {
            return esc;
        } else {
            return c.toString();
        }
    }

    private static void setXmlEscapes() {
        xmlEscapes = new Hashtable();
        xmlEscapes.put(new Character('&'), "&amp;");
        xmlEscapes.put(new Character('<'), "&lt;");
        xmlEscapes.put(new Character('>'), "&gt;");
        xmlEscapes.put(new Character('"'), "&quot;");
        xmlEscapes.put(new Character('\''), "&apos;");
    }

    /*
     * Returns true if the given attributes should not be displayed for the elements of the given datatype. Based on XMLSchema
     * specs.
     */
    public static boolean skipAttributeByDatatype(String attrShortName, String datatype) {

        return (attrShortName == null || datatype == null) ? false : IrrelevantAttributes.getInstance().isIrrelevant(datatype,
                 attrShortName);
    }

    /*
     *
     */
    public static String getObligationID(String obligDetailsUrl) {

        if (obligDetailsUrl == null || obligDetailsUrl.length() == 0) {
            return null;
        }

        String obligationID = "";
        String s = new String("id=");
        int j = obligDetailsUrl.indexOf(s);
        if (j < 0) {
            return null;
        }
        int k = obligDetailsUrl.indexOf("&", j);
        if (k < 0) {
            obligationID = obligDetailsUrl.substring(j + s.length());
        } else {
            obligationID = obligDetailsUrl.substring(j + s.length(), k);
        }

        try {
            int oid = Integer.parseInt(obligationID);
        } catch (NumberFormatException nfe) {
            return null;
        }

        return obligationID;
    }

    /*
     *
     */
    public static void forward2errorpage(HttpServletRequest request, HttpServletResponse response, Throwable t, String backURL)
            throws ServletException, IOException {

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
    public static String getServletPathWithQueryString(HttpServletRequest request) {

        StringBuffer result = new StringBuffer();
        String servletPath = request.getServletPath();
        if (servletPath != null && servletPath.length() > 0) {
            if (servletPath.startsWith("/") && servletPath.length() > 1) {
                result.append(servletPath.substring(1));
            }
            String queryString = request.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                result.append("?").append(queryString);
            }
        }

        return result.toString();
    }

    /**
     *
     * @param str
     * @param token
     * @return
     */
    public static HashSet tokens2hash(String str, String delim) {
        HashSet result = new HashSet();
        if (str != null && delim != null) {
            StringTokenizer tokenizer = new StringTokenizer(str, delim);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token != null && token.length() > 0) {
                    result.add(token);
                }
            }
        }

        return result;

    }

    /**
     *
     * @param request
     * @return
     */
    public static String getBaseHref(HttpServletRequest request) {

        String protocol = request.getProtocol().toLowerCase();
        int i = protocol.indexOf('/');
        if (i >= 0) {
            protocol = protocol.substring(0, i);
        }

        StringBuffer buf = new StringBuffer(protocol);
        buf.append("://").append(request.getServerName());
        if (request.getServerPort() > 0) {
            buf.append(":").append(request.getServerPort());
        }
        if (request.getContextPath() != null) {
            buf.append(request.getContextPath());
        }
        if (buf.toString().endsWith("/") == false) {
            buf.append("/");
        }

        return buf.toString();
    }

    /**
     *
     * @param from
     * @param to
     * @return
     */
    public static boolean isAllowedFxvDatatypeConversion(String from, String to) {

        if (allowedFxvDatatypeConversions != null && allowedFxvDatatypeConversions.length > 0) {
            int FROM = 0;
            int TO = 1;
            for (int i = 0; i < allowedFxvDatatypeConversions.length; i++) {
                String[] pair = allowedFxvDatatypeConversions[i];
                if (pair[FROM].equals(from) && pair[TO].equals(to)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @return
     */
    public static synchronized String getExpiresDateString() {

        if (expiresDateString == null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
            dateFormat.setTimeZone(TimeZone.getTimeZone(""));
            expiresDateString = dateFormat.format(new Date(0));
        }

        return expiresDateString;
    }

    /**
     *
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static String getUrlPathAndQuery(String urlString) throws MalformedURLException {

        java.net.URL url = new java.net.URL(urlString);
        StringBuffer buf = new StringBuffer(url.getPath());
        if (url.getQuery() != null) {
            buf.append("?").append(url.getQuery());
        }

        return buf.toString();
    }

    /**
     *
     * @param s
     * @return
     */
    public static boolean isNumericID(String s) {

        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Converts a collection to a row of comma-separated-values. Caution: empty strings are ignored, values are not enclosed in
     * double quotes. Double-quotes in values are not escaped.
     *
     * @param coll
     * @return
     */
    public static String toCSV(Collection coll) {

        StringBuffer buf = new StringBuffer();
        if (coll != null) {
            for (Iterator it = coll.iterator(); it.hasNext();) {

                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(it.next());
            }
        }
        return buf.toString();
    }

    /**
     * Converts an array to a row of comma-separated-values. Caution: empty strings are ignored, values are not enclosed in double
     * quotes. Double-quotes in values are not escaped.
     *
     * @param coll
     * @return
     */
    public static String toCSV(Object[] array) {

        StringBuffer buf = new StringBuffer();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {

                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     *
     * @param collection
     * @return
     */
    public static boolean isEmpty(Collection<?> collection) {

        return collection == null || collection.isEmpty();
    }

    /**
     *
     * @param map
     * @return
     */
    public static boolean isEmpty(Map<?, ?> map) {

        return map == null || map.isEmpty();
    }

    /**
     * Convert a byte to a Boolean. 0 is false. Everything else is true.
     *
     * @param i
     *            - the byte
     * @return the Boolean value
     */
    public static boolean toBoolean(byte i) {
        if (i == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Convert a Boolean value to a byte.
     *
     * @param b
     *            - the value
     * @return 0 or 1.
     */
    public static byte toByte(boolean b) {
        if (b == false) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     *
     * @param schemaSet
     * @return
     */
    public static String generateContinuityId(SchemaSet schemaSet) {

        if (schemaSet == null || isEmpty(schemaSet.getIdentifier())) {
            return null;
        }
        String name = schemaSet.getIdentifier() + Thread.currentThread().getId() + System.currentTimeMillis();
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    /**
     *
     * @param schema
     * @return
     */
    public static String generateContinuityId(Schema schema) {

        if (schema == null || isEmpty(schema.getFileName())) {
            return null;
        }
        String name = schema.getFileName() + Thread.currentThread().getId() + System.currentTimeMillis();
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    /**
     * Return unique continuity ID for vocabulary folder.
     *
     * @param vocabularyFolder
     * @return
     */
    public static String generateContinuityId(VocabularyFolder vocabularyFolder) {

        if (vocabularyFolder == null || isEmpty(vocabularyFolder.getIdentifier())) {
            return null;
        }
        String name = vocabularyFolder.getIdentifier() + Thread.currentThread().getId() + System.currentTimeMillis();
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    /**
     * Returns the URL binding of the given Stripes action bean class. Be aware that a Stripes URL binding may be parameterized
     * (e.g. "/foo/{bar}/{baz}"). If you want to get the URL binding with parameters replaced by real values, use
     * getUrlBinding(Class, String, Pair...).
     *
     * @param actionBeanClass
     * @return
     */
    public static String getUrlBinding(Class<? extends ActionBean> actionBeanClass) {

        if (actionBeanClass == null) {
            return null;
        } else {
            return actionBeanClass.getAnnotation(UrlBinding.class).value();
        }
    }

    /**
     * Returns the URL binding of the given Stripes action bean class. If the URL binding is parameterized, replaces the reserved
     * "$event" parameter with the given event (but only if the latter is supplied), and replaces all other parameters with values
     * found from the given array of key-value pairs.
     *
     * @param actionBeanClass
     * @return
     */
    public static String getUrlBinding(Class<? extends ActionBean> actionBeanClass, String event,
            Pair<String, Object>... parameters) {

        String urlBinding = null;
        if (actionBeanClass != null) {

            urlBinding = actionBeanClass.getAnnotation(UrlBinding.class).value();
            if (StringUtils.isNotBlank(event)) {
                urlBinding = StringUtils.replace(urlBinding, "{$event}", event);
            }

            if (parameters != null && parameters.length > 0) {
                for (int i = 0; i < parameters.length; i++) {
                    String paramPlaceHolder = "{" + parameters[i].getLeft() + "}";
                    String paramValue = parameters[i].getRight().toString();
                    urlBinding = StringUtils.replace(urlBinding, paramPlaceHolder, paramValue);
                }
            }
        }

        return urlBinding;
    }

    /**
     * Returns true if the identifier doesn't contain banned characters.
     *
     * @param identifier
     *            - the string to test.
     * @return - the true/false result.
     */
    public static boolean isValidIdentifier(String identifier) {
        if (StringUtils.isNotBlank(identifier)) {
            String regex = "^[^/\\?\\%\\\\#:]+$";
            return identifier.matches(regex);
        }
        return false;
    }

    /**
     * Format a date object to yyyy-MM-dd HH:mm:ss. Is this format chosen to fit what the database uses?
     *
     * @param date
     *            - the date
     * @return date as string
     */
    public static String formatDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    /**
     * Encodes URL fragment to UTF-8.
     *
     * @param value
     *            value to be encoded
     * @return encoded value
     */
    public static String encodeURLPath(String value) {
        String retValue = value;
        try {
            retValue = UriUtils.encodePath(value, "utf-8");
        } catch (UnsupportedEncodingException ue) {
            return retValue;
        }

        return retValue;
    }

    /**
     * Checks if the given String corresponds to URI syntax. Is different from isURI() that supports all prefixes and requires
     * double slash to be entered after the scheme. The allowed schemes are: http, https, ftp, mailto, tel and urn.
     *
     * @param str
     *            string to be checked
     * @return true if matches URI requirements
     */
    public static boolean isValidUri(String str) {
        // if it is a blank string or does not contain schema seperator, just return false!
        if (StringUtils.isBlank(str) || str.indexOf(':') == -1) {
            return false;
        }

        str = str.toLowerCase().trim();
        // check for schemas
        if (!str.startsWith("http://") && !str.startsWith("https://") && !str.startsWith("ftp://") && !str.startsWith("mailto:")
                && !str.startsWith("tel:") && !str.startsWith("urn:")) {
            return false;
        }

        try {
            URI uri = new URI(str);
            String scheme = uri.getScheme();

            if (StringUtils.isBlank(scheme)) {
                return false;
            }

            if (scheme.equals("http") || scheme.equals("https") || scheme.equals("ftp")) {
                if (StringUtils.isBlank(uri.getHost())) {
                    return false;
                }

                String path = uri.getPath();
                if (StringUtils.isNotBlank(path)) {
                    String pattern = "[?<>:*|\"\\\\]|//|\\.\\.|  ";
                    Pattern regex = Pattern.compile(pattern);
                    Matcher matcher = regex.matcher(path);
                    // check if the regex matches path
                    if (matcher.find()) {
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
