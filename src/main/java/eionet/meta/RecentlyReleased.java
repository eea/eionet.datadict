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
 * Agency. Portions created by TripleDev or Zero TechnoLOGGERies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */
package eionet.meta;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Released item bean.
 * Implements Comparable interface for sorting.
 *
 * @author enver
 */
public class RecentlyReleased implements Comparable<RecentlyReleased> {
    /**
     * Type enum for current item.
     */
    public static enum Type {
        VOCABULARY, DATASET, SCHEMA
    };

    /**
     * Name of item.
     */
    private String name;
    /**
     * Released date.
     */
    private Date releasedDate;
    /**
     * Type of current item.
     */
    Type type;
    /**
     * Additional parameters for links.
     */
    private Map<String, Object> parameters;

    /**
     * Default constructor without any parameters.
     */
    public RecentlyReleased() {
    } // end of default constructor

    /**
     * Constructor with all parameters.
     *
     * @param name
     *            name
     * @param releasedDate
     *            releasedDate
     * @param type
     *            type
     */
    public RecentlyReleased(String name, Date releasedDate, Type type) {
        this.name = name;
        this.releasedDate = releasedDate;
        this.type = type;
        this.parameters = new HashMap<String, Object>();
    } // end of constructor

    /**
     * Adds additional parameters to map.
     *
     * @param key
     *            key
     * @param value
     *            value
     */
    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    } // end of method addParameter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getReleasedDate() {
        return releasedDate;
    }

    public void setReleasedDate(Date releasedDate) {
        this.releasedDate = releasedDate;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Compare to method to sort collections. Comparison is done based on released date.
     *
     * @param that
     *            object to compare
     * @return result
     */
    @Override
    public int compareTo(RecentlyReleased that) {
        if (this == that) {
            return 0;
        }

        return this.releasedDate.compareTo(that.releasedDate);
    } // end of method compareTo

} // end of class RecentlyReleased
