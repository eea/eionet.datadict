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

package eionet.meta.dao.domain;

import eionet.util.Util;

import java.util.Calendar;
import java.util.Date;

/**
 * Data set.
 *
 * @author Juhan Voolaid
 */
public class DataSet {

    /** Id. */
    private int id;

    /** Identifier. */
    private String identifier;

    /** Short name. */
    private String shortName;

    /**
     * Date.
     */
    private long date;

    /**
     * Longer name.
     */
    private String name;

    private boolean workingCopy;
    
    private String workingUser;
    
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

    public long getDate() {
        return date;
    }

    /**
     * Calculates and returns adjusted date for dataset.
     *
     * @return adjusted date.
     */
    public Date getAdjustedDate(){
        Calendar adjusted = Calendar.getInstance();
        adjusted.setTimeInMillis(this.date);
        return adjusted.getTime();
    }

    public String getDateString() {
        return Util.releasedDate(date);
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }
    
}
