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

import java.util.Date;

import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.dao.domain.VocabularyConcept;

/**
 * Site code domain object.
 *
 * @author Juhan Voolaid
 */
public class SiteCode extends VocabularyConcept {

    /** Properties. */
    private SiteCodeStatus siteCodeStatus = SiteCodeStatus.AVAILABLE;
    private String countryCode;
    private Date dateCreated;
    private String userCreated;
    private Date dateAllocated;
    private String userAllocated;
    private String initialSiteName;
    private String yearsDeleted;
    private String yearsDisappeared;

    /**
     * @return the status
     */
    public SiteCodeStatus getSiteCodeStatus() {
        return siteCodeStatus;
    }
    /**
     * @param siteCodeStatus the status to set
     */
    public void setSiteCodeStatus(SiteCodeStatus siteCodeStatus) {
        this.siteCodeStatus = siteCodeStatus;
    }
    /**
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }
    /**
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }
    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    /**
     * @return the userCreated
     */
    public String getUserCreated() {
        return userCreated;
    }
    /**
     * @param userCreated the userCreated to set
     */
    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }
    /**
     * @return the dateAllocated
     */
    public Date getDateAllocated() {
        return dateAllocated;
    }
    /**
     * @param dateAllocated the dateAllocated to set
     */
    public void setDateAllocated(Date dateAllocated) {
        this.dateAllocated = dateAllocated;
    }
    /**
     * @return the userAllocated
     */
    public String getUserAllocated() {
        return userAllocated;
    }
    /**
     * @param userAllocated the userAllocated to set
     */
    public void setUserAllocated(String userAllocated) {
        this.userAllocated = userAllocated;
    }
    /**
     * @return the initialSiteName
     */
    public String getInitialSiteName() {
        return initialSiteName;
    }
    /**
     * @param initialSiteName the initialSiteName to set
     */
    public void setInitialSiteName(String initialSiteName) {
        this.initialSiteName = initialSiteName;
    }
    /**
     * @return the yearsDeleted
     */
    public String getYearsDeleted() {
        return yearsDeleted;
    }
    /**
     * @param yearsDeleted the yearsDeleted to set
     */
    public void setYearsDeleted(String yearsDeleted) {
        this.yearsDeleted = yearsDeleted;
    }
    /**
     * @return the yearsDisappeared
     */
    public String getYearsDisappeared() {
        return yearsDisappeared;
    }
    /**
     * @param yearsDisappeared the yearsDisappeared to set
     */
    public void setYearsDisappeared(String yearsDisappeared) {
        this.yearsDisappeared = yearsDisappeared;
    }

}
