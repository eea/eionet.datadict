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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.service.data;

import java.util.ArrayList;
import java.util.List;

import eionet.meta.dao.domain.Attribute;

/**
 * Data table search filter.
 *
 * @author Juhan Voolaid
 */
public class TableFilter {

    private String shortName;

    private String identifier;

    private List<Attribute> attributes;



    public TableFilter() {
        attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute(1, "Name"));
        attributes.add(new Attribute(4, "Definition"));
        attributes.add(new Attribute(5, "Keyword"));
        attributes.add(new Attribute(15, "Short description"));
        attributes.add(new Attribute(17, "Methodology"));
        attributes.add(new Attribute(37, "EEA issue"));
        attributes.add(new Attribute(40, "Descriptive image"));
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName
     *            the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the attributes
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

}
