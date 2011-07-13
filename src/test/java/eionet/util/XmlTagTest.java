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
 * Copyright (C) 2007 European Environment Agency. All
 * Rights Reserved.
 * 
 * Contributor(s): 
 */
package eionet.util;

import junit.framework.TestCase;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class XmlTagTest extends TestCase {


        public void testSimple(){
                XmlTag tag = new XmlTag();
                tag.setTagName("jaanus");
                tag.setAttribute("vanues", "32");
                tag.setAttribute("pikkus", "186cm");
                tag.setContent("tore mees on &amp;");
                assertEquals(tag.toString(),"<jaanus vanues=\"32\" pikkus=\"186cm\">tore mees on &amp;</jaanus>");
        }

	// Show that &, " and < are NOT automatically escaped
        public void testShowNoEscapes(){
                XmlTag tag = new XmlTag();
                tag.setTagName("produce");
                tag.setAttribute("fruits", "2\"");
                tag.setAttribute("vegetables", "&18'");
                tag.setContent("Fruit & < vegetables");
                assertEquals(tag.toString(),"<produce vegetables=\"&18'\" fruits=\"2\"\">Fruit & < vegetables</produce>");
        }

}
