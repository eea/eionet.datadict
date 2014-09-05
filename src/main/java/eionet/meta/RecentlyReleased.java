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

/**
 * Released item bean.
 *
 * @author enver
 */
public class RecentlyReleased {
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
    } // end of constructor

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
} // end of class RecentlyReleased
