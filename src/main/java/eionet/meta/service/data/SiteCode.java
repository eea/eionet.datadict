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
    private int siteCodeId;
    private SiteCodeStatus status = SiteCodeStatus.AVAILABLE;
    private String countryCode;
    private Date dateCreated;
    private String userCreated;
    private Date dateAllocated;
    private String userAllocated;
    private String initialSiteName;
    /**
     * @return the id
     */
    public int getSiteCodeId() {
        return siteCodeId;
    }
    /**
     * @param id the id to set
     */
    public void setSiteCodeId(int id) {
        this.siteCodeId = id;
    }
    /**
     * @return the status
     */
    public SiteCodeStatus getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(SiteCodeStatus status) {
        this.status = status;
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

}
