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
 *        Enriko Käsper
 */

package eionet.meta.dao.domain;

/**
 * Copmlex Attribute Field.
 *
 * @author Enriko Käsper
 */
public class ComplexAttributeField {

    private int id;

    private String name;

    private String value;

    private boolean exactMatchInSearch;

    public ComplexAttributeField(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public ComplexAttributeField() {
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the exactMatchInSearch
     */
    public boolean isExactMatchInSearch() {
        return exactMatchInSearch;
    }

    /**
     * @param exactMatchInSearch the exactMatchInSearch to set
     */
    public void setExactMatchInSearch(boolean exactMatchInSearch) {
        this.exactMatchInSearch = exactMatchInSearch;
    }

}
