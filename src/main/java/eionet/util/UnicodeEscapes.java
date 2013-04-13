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
/**
 * Original Code: Jaanus Heinlaid (TietoEnator)
 */

package eionet.util;

import java.util.Hashtable;

/**
 *
 * @author jaanus
 */
public class UnicodeEscapes {

    /** */
    private Hashtable unicodeEscapes = new Hashtable();

    /**
     *
     *
     */
    public UnicodeEscapes() {
        unicodeEscapes.put("nbsp", "160");
        unicodeEscapes.put("iexcl", "161");
        unicodeEscapes.put("cent", "162");
        unicodeEscapes.put("pound", "163");
        unicodeEscapes.put("curren", "164");
        unicodeEscapes.put("yen", "165");
        unicodeEscapes.put("brvbar", "166");
        unicodeEscapes.put("sect", "167");
        unicodeEscapes.put("uml", "168");
        unicodeEscapes.put("copy", "169");
        unicodeEscapes.put("ordf", "170");
        unicodeEscapes.put("laquo", "171");
        unicodeEscapes.put("not", "172");
        unicodeEscapes.put("shy", "173");
        unicodeEscapes.put("reg", "174");
        unicodeEscapes.put("macr", "175");
        unicodeEscapes.put("deg", "176");
        unicodeEscapes.put("plusmn", "177");
        unicodeEscapes.put("sup2", "178");
        unicodeEscapes.put("sup3", "179");
        unicodeEscapes.put("acute", "180");
        unicodeEscapes.put("micro", "181");
        unicodeEscapes.put("para", "182");
        unicodeEscapes.put("middot", "183");
        unicodeEscapes.put("cedil", "184");
        unicodeEscapes.put("sup1", "185");
        unicodeEscapes.put("ordm", "186");
        unicodeEscapes.put("raquo", "187");
        unicodeEscapes.put("frac14", "188");
        unicodeEscapes.put("frac12", "189");
        unicodeEscapes.put("frac34", "190");
        unicodeEscapes.put("iquest", "191");
        unicodeEscapes.put("Agrave", "192");
        unicodeEscapes.put("Aacute", "193");
        unicodeEscapes.put("Acirc", "194");
        unicodeEscapes.put("Atilde", "195");
        unicodeEscapes.put("Auml", "196");
        unicodeEscapes.put("Aring", "197");
        unicodeEscapes.put("AElig", "198");
        unicodeEscapes.put("Ccedil", "199");
        unicodeEscapes.put("Egrave", "200");
        unicodeEscapes.put("Eacute", "201");
        unicodeEscapes.put("Ecirc", "202");
        unicodeEscapes.put("Euml", "203");
        unicodeEscapes.put("Igrave", "204");
        unicodeEscapes.put("Iacute", "205");
        unicodeEscapes.put("Icirc", "206");
        unicodeEscapes.put("Iuml", "207");
        unicodeEscapes.put("ETH", "208");
        unicodeEscapes.put("Ntilde", "209");
        unicodeEscapes.put("Ograve", "210");
        unicodeEscapes.put("Oacute", "211");
        unicodeEscapes.put("Ocirc", "212");
        unicodeEscapes.put("Otilde", "213");
        unicodeEscapes.put("Ouml", "214");
        unicodeEscapes.put("times", "215");
        unicodeEscapes.put("Oslash", "216");
        unicodeEscapes.put("Ugrave", "217");
        unicodeEscapes.put("Uacute", "218");
        unicodeEscapes.put("Ucirc", "219");
        unicodeEscapes.put("Uuml", "220");
        unicodeEscapes.put("Yacute", "221");
        unicodeEscapes.put("THORN", "222");
        unicodeEscapes.put("szlig", "223");
        unicodeEscapes.put("agrave", "224");
        unicodeEscapes.put("aacute", "225");
        unicodeEscapes.put("acirc", "226");
        unicodeEscapes.put("atilde", "227");
        unicodeEscapes.put("auml", "228");
        unicodeEscapes.put("aring", "229");
        unicodeEscapes.put("aelig", "230");
        unicodeEscapes.put("ccedil", "231");
        unicodeEscapes.put("egrave", "232");
        unicodeEscapes.put("eacute", "233");
        unicodeEscapes.put("ecirc", "234");
        unicodeEscapes.put("euml", "235");
        unicodeEscapes.put("igrave", "236");
        unicodeEscapes.put("iacute", "237");
        unicodeEscapes.put("icirc", "238");
        unicodeEscapes.put("iuml", "239");
        unicodeEscapes.put("eth", "240");
        unicodeEscapes.put("ntilde", "241");
        unicodeEscapes.put("ograve", "242");
        unicodeEscapes.put("oacute", "243");
        unicodeEscapes.put("ocirc", "244");
        unicodeEscapes.put("otilde", "245");
        unicodeEscapes.put("ouml", "246");
        unicodeEscapes.put("divide", "247");
        unicodeEscapes.put("oslash", "248");
        unicodeEscapes.put("ugrave", "249");
        unicodeEscapes.put("uacute", "250");
        unicodeEscapes.put("ucirc", "251");
        unicodeEscapes.put("uuml", "252");
        unicodeEscapes.put("yacute", "253");
        unicodeEscapes.put("thorn", "254");
        unicodeEscapes.put("yuml", "255");
        unicodeEscapes.put("fnof", "402");
        unicodeEscapes.put("Alpha", "913");
        unicodeEscapes.put("Beta", "914");
        unicodeEscapes.put("Gamma", "915");
        unicodeEscapes.put("Delta", "916");
        unicodeEscapes.put("Epsilon", "917");
        unicodeEscapes.put("Zeta", "918");
        unicodeEscapes.put("Eta", "919");
        unicodeEscapes.put("Theta", "920");
        unicodeEscapes.put("Iota", "921");
        unicodeEscapes.put("Kappa", "922");
        unicodeEscapes.put("Lambda", "923");
        unicodeEscapes.put("Mu", "924");
        unicodeEscapes.put("Nu", "925");
        unicodeEscapes.put("Xi", "926");
        unicodeEscapes.put("Omicron", "927");
        unicodeEscapes.put("Pi", "928");
        unicodeEscapes.put("Rho", "929");
        unicodeEscapes.put("Sigma", "931");
        unicodeEscapes.put("Tau", "932");
        unicodeEscapes.put("Upsilon", "933");
        unicodeEscapes.put("Phi", "934");
        unicodeEscapes.put("Chi", "935");
        unicodeEscapes.put("Psi", "936");
        unicodeEscapes.put("Omega", "937");
        unicodeEscapes.put("alpha", "945");
        unicodeEscapes.put("beta", "946");
        unicodeEscapes.put("gamma", "947");
        unicodeEscapes.put("delta", "948");
        unicodeEscapes.put("epsilon", "949");
        unicodeEscapes.put("zeta", "950");
        unicodeEscapes.put("eta", "951");
        unicodeEscapes.put("theta", "952");
        unicodeEscapes.put("iota", "953");
        unicodeEscapes.put("kappa", "954");
        unicodeEscapes.put("lambda", "955");
        unicodeEscapes.put("mu", "956");
        unicodeEscapes.put("nu", "957");
        unicodeEscapes.put("xi", "958");
        unicodeEscapes.put("omicron", "959");
        unicodeEscapes.put("pi", "960");
        unicodeEscapes.put("rho", "961");
        unicodeEscapes.put("sigmaf", "962");
        unicodeEscapes.put("sigma", "963");
        unicodeEscapes.put("tau", "964");
        unicodeEscapes.put("upsilon", "965");
        unicodeEscapes.put("phi", "966");
        unicodeEscapes.put("chi", "967");
        unicodeEscapes.put("psi", "968");
        unicodeEscapes.put("omega", "969");
        unicodeEscapes.put("thetasy", "977");
        unicodeEscapes.put("upsih", "978");
        unicodeEscapes.put("piv", "982");
        unicodeEscapes.put("bull", "8226");
        unicodeEscapes.put("hellip", "8230");
        unicodeEscapes.put("prime", "8242");
        unicodeEscapes.put("Prime", "8243");
        unicodeEscapes.put("oline", "8254");
        unicodeEscapes.put("frasl", "8260");
        unicodeEscapes.put("weierp", "8472");
        unicodeEscapes.put("image", "8465");
        unicodeEscapes.put("real", "8476");
        unicodeEscapes.put("trade", "8482");
        unicodeEscapes.put("alefsym", "8501");
        unicodeEscapes.put("larr", "8592");
        unicodeEscapes.put("uarr", "8593");
        unicodeEscapes.put("rarr", "8594");
        unicodeEscapes.put("darr", "8595");
        unicodeEscapes.put("harr", "8596");
        unicodeEscapes.put("crarr", "8629");
        unicodeEscapes.put("lArr", "8656");
        unicodeEscapes.put("uArr", "8657");
        unicodeEscapes.put("rArr", "8658");
        unicodeEscapes.put("dArr", "8659");
        unicodeEscapes.put("hArr", "8660");
        unicodeEscapes.put("forall", "8704");
        unicodeEscapes.put("part", "8706");
        unicodeEscapes.put("exist", "8707");
        unicodeEscapes.put("empty", "8709");
        unicodeEscapes.put("nabla", "8711");
        unicodeEscapes.put("isin", "8712");
        unicodeEscapes.put("notin", "8713");
        unicodeEscapes.put("ni", "8715");
        unicodeEscapes.put("prod", "8719");
        unicodeEscapes.put("sum", "8721");
        unicodeEscapes.put("minus", "8722");
        unicodeEscapes.put("lowast", "8727");
        unicodeEscapes.put("radic", "8730");
        unicodeEscapes.put("prop", "8733");
        unicodeEscapes.put("infin", "8734");
        unicodeEscapes.put("ang", "8736");
        unicodeEscapes.put("and", "8743");
        unicodeEscapes.put("or", "8744");
        unicodeEscapes.put("cap", "8745");
        unicodeEscapes.put("cup", "8746");
        unicodeEscapes.put("int", "8747");
        unicodeEscapes.put("there4", "8756");
        unicodeEscapes.put("sim", "8764");
        unicodeEscapes.put("cong", "8773");
        unicodeEscapes.put("asymp", "8776");
        unicodeEscapes.put("ne", "8800");
        unicodeEscapes.put("equiv", "8801");
        unicodeEscapes.put("le", "8804");
        unicodeEscapes.put("ge", "8805");
        unicodeEscapes.put("sub", "8834");
        unicodeEscapes.put("sup", "8835");
        unicodeEscapes.put("nsub", "8836");
        unicodeEscapes.put("sube", "8838");
        unicodeEscapes.put("supe", "8839");
        unicodeEscapes.put("oplus", "8853");
        unicodeEscapes.put("otimes", "8855");
        unicodeEscapes.put("perp", "8869");
        unicodeEscapes.put("sdot", "8901");
        unicodeEscapes.put("lceil", "8968");
        unicodeEscapes.put("rceil", "8969");
        unicodeEscapes.put("lfloor", "8970");
        unicodeEscapes.put("rfloor", "8971");
        unicodeEscapes.put("lang", "9001");
        unicodeEscapes.put("rang", "9002");
        unicodeEscapes.put("loz", "9674");
        unicodeEscapes.put("spades", "9824");
        unicodeEscapes.put("clubs", "9827");
        unicodeEscapes.put("hearts", "9829");
        unicodeEscapes.put("diams", "9830");
        unicodeEscapes.put("quot", "34");
        unicodeEscapes.put("amp", "38");
        unicodeEscapes.put("lt", "60");
        unicodeEscapes.put("gt", "62");
        unicodeEscapes.put("OElig", "338");
        unicodeEscapes.put("oelig", "339");
        unicodeEscapes.put("Scaron", "352");
        unicodeEscapes.put("scaron", "353");
        unicodeEscapes.put("Yuml", "376");
        unicodeEscapes.put("circ", "710");
        unicodeEscapes.put("tilde", "732");
        unicodeEscapes.put("ensp", "8194");
        unicodeEscapes.put("emsp", "8195");
        unicodeEscapes.put("thinsp", "8201");
        unicodeEscapes.put("zwnj", "8204");
        unicodeEscapes.put("zwj", "8205");
        unicodeEscapes.put("lrm", "8206");
        unicodeEscapes.put("rlm", "8207");
        unicodeEscapes.put("ndash", "8211");
        unicodeEscapes.put("mdash", "8212");
        unicodeEscapes.put("lsquo", "8216");
        unicodeEscapes.put("rsquo", "8217");
        unicodeEscapes.put("sbquo", "8218");
        unicodeEscapes.put("ldquo", "8220");
        unicodeEscapes.put("rdquo", "8221");
        unicodeEscapes.put("bdquo", "8222");
        unicodeEscapes.put("dagger", "8224");
        unicodeEscapes.put("Dagger", "8225");
        unicodeEscapes.put("permil", "8240");
        unicodeEscapes.put("lsaquo", "8249");
        unicodeEscapes.put("rsaquo", "8250");
        unicodeEscapes.put("euro", "8364");
    }

    /**
     *
     * @param ent
     * @return
     */
    public int getDecimal(String ent) {

        String sDecimal = (String) unicodeEscapes.get(ent);
        if (sDecimal == null) return -1;
        return Integer.parseInt(sDecimal);
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean isXHTMLEntity(String s) {

        if (s == null || s.length() == 0) return false;
        if (!(s.startsWith("&") && s.endsWith(";"))) return false;

        if (s.length() == 2) return false;

        String ss = s.substring(1, s.length()-1);
        return unicodeEscapes.containsKey(ss);
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean isNumericHTMLEscapeCode(String s) {

        if (s == null || s.length() == 0) return false;
        if (!(s.startsWith("&") && s.endsWith(";"))) return false;

        char c = s.charAt(1);
        if (c != '#') return false;

        if (s.length() == 3) return false;

        try {
            Integer.parseInt(s.substring(2, s.length()-1));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
