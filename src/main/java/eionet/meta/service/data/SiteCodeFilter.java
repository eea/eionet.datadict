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

import eionet.meta.DDUser;
import eionet.meta.dao.domain.SiteCodeStatus;

/**
 * Site code search filter.
 *
 * @author Juhan Voolaid
 */

/**
 * Site code search filter.
 *
 * @author Juhan Voolaid
 */
public class SiteCodeFilter extends PagedRequest {

    /** site code status representing allocated codes that are or has been in use: assigned, deleted or disappeared */
    public static final String[] ALLOCATED_USED_STATUSES = { SiteCodeStatus.ASSIGNED.name(), SiteCodeStatus.DELETED.name(),
        SiteCodeStatus.DISAPPEARED.name() };

    private String countryCode;
    private SiteCodeStatus status;
    private String siteName;
    private String identifier;
    private Date dateAllocated;
    /** Used for filtering by allocated user name. */
    private String userAllocated;
    /** Used for filtering by user privileges. */
    private DDUser user;

    /** use only allocated codes in use */
    private boolean allocatedUsedStatuses;

    private Integer numberOfElements;

    /**
     * @return the status
     */
    public SiteCodeStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
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
     * @param countryCode
     *            the countryCode to set
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @return the siteName
     */
    public String getSiteName() {
        return siteName;
    }

    /**
     * @param siteName
     *            the siteName to set
     */
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    /**
     * @return the user
     */
    public DDUser getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(DDUser user) {
        this.user = user;
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
     * @return the dateAllocated
     */
    public Date getDateAllocated() {
        return dateAllocated;
    }

    /**
     * @param dateAllocated
     *            the dateAllocated to set
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
     * @param userAllocated
     *            the userAllocated to set
     */
    public void setUserAllocated(String userAllocated) {
        this.userAllocated = userAllocated;
    }

    /**
     * @return the allocatedUsed
     */
    public boolean isAllocatedUsedStatuses() {
        return allocatedUsedStatuses;
    }

    /**
     * @param allocatedUsed the allocatedUsed to set
     */
    public void setAllocatedUsedStatuses(boolean allocatedUsed) {
        this.allocatedUsedStatuses = allocatedUsed;
    }

    /**
     * @return the numberOfElements
     */
    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    /**
     * @param numberOfElements the numberOfElements to set
     */
    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

}
