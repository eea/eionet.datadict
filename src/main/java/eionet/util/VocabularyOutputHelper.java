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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.DataElement;

/**
 * Vocabulary common output helper.
 *
 * @author enver
 */
public final class VocabularyOutputHelper {
    /**
     * BOM byte array length.
     */
    public static final int BOM_BYTE_ARRAY_LENGTH = 3;

    /**
     * Prevent public initialization.
     */
    private VocabularyOutputHelper() {
    }

    /**
     * finds list of data element values by name.
     *
     * @param elemName
     *            element name to be looked for
     * @param elems
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    public static List<DataElement> getDataElementValuesByName(String elemName, List<List<DataElement>> elems) {
        for (List<DataElement> elem : elems) {
            if (elem != null && elem.size() > 0) {
                DataElement elemMeta = elem.get(0);
                if (elemMeta != null && StringUtils.equals(elemMeta.getIdentifier(), elemName)) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * Finds list of data element values by name and language.
     *
     * @param elemName
     *            element name to be looked for
     * @param lang
     *            element lang to be looked for
     * @param elems
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    public static List<DataElement> getDataElementValuesByNameAndLang(String elemName, String lang, List<List<DataElement>> elems) {
        boolean isLangEmpty = StringUtils.isEmpty(lang);
        ArrayList<DataElement> elements = new ArrayList<DataElement>();
        for (List<DataElement> elem : elems) {
            if (elem == null || elem.size() < 1 || !StringUtils.equals(elem.get(0).getIdentifier(), elemName)) { // check first one
                continue;
            }
            for (DataElement elemMeta : elem) {
                String elemLang = elemMeta.getAttributeLanguage();
                if ((isLangEmpty && StringUtils.isEmpty(elemLang)) || StringUtils.equals(lang, elemLang)) {
                    elements.add(elemMeta);
                } else if (elements.size() > 0) {
                    break;
                }
            }
            // return elements;
        }
        return elements;
    } // end of method getDataElementValuesByNameAndLang

    /**
     * Returns bom byte array.
     *
     * @return bom byte array
     */
    public static byte[] getBomByteArray() {
        return new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    } // end of method getBomByteArray
} // end of class VocabularyOutputHelper
