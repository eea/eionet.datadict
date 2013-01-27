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
 *        Enriko Käsper
 */

package eionet.meta.notif;

import java.util.List;

import eionet.meta.service.data.SiteCode;

/**
 * Notification object for sending site code allocation event to UNS.
 *
 * @author Enriko Käsper
 */
public class SiteCodeAllocationNotification {

    private String username;
    private String allocationTime;
    private String country;
    private int nofCodesAllocatedByEvent;
    private int totalNofAllocatedCodes;
    private int nofAvailableCodes;
    private List<SiteCode> siteCodes;
    private boolean adminRole;
    private boolean test;
    private String to;

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the allocationTime
     */
    public String getAllocationTime() {
        return allocationTime;
    }

    /**
     * @param allocationTime
     *            the allocationTime to set
     */
    public void setAllocationTime(String allocationTime) {
        this.allocationTime = allocationTime;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country
     *            the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the nofCodesAllocatedByEvent
     */
    public int getNofCodesAllocatedByEvent() {
        return nofCodesAllocatedByEvent;
    }

    /**
     * @param nofCodesAllocatedByEvent
     *            the nofCodesAllocatedByEvent to set
     */
    public void setNofCodesAllocatedByEvent(int nofCodesAllocatedByEvent) {
        this.nofCodesAllocatedByEvent = nofCodesAllocatedByEvent;
    }

    /**
     * @return the totalNofAllocatedCodes
     */
    public int getTotalNofAllocatedCodes() {
        return totalNofAllocatedCodes;
    }

    /**
     * @param totalNofAllocatedCodes
     *            the totalNofAllocatedCodes to set
     */
    public void setTotalNofAllocatedCodes(int totalNofAllocatedCodes) {
        this.totalNofAllocatedCodes = totalNofAllocatedCodes;
    }

    /**
     * @return the nofAvailableCodes
     */
    public int getNofAvailableCodes() {
        return nofAvailableCodes;
    }

    /**
     * @param nofAvailableCodes
     *            the nofAvailableCodes to set
     */
    public void setNofAvailableCodes(int nofAvailableCodes) {
        this.nofAvailableCodes = nofAvailableCodes;
    }

    /**
     * @return the siteCodes
     */
    public List<SiteCode> getSiteCodes() {
        return siteCodes;
    }

    /**
     * @param siteCodes
     *            the siteCodes to set
     */
    public void setSiteCodes(List<SiteCode> siteCodes) {
        this.siteCodes = siteCodes;
    }

    /**
     * @return the adminRole
     */
    public boolean isAdminRole() {
        return adminRole;
    }

    /**
     * @param adminRole
     *            the adminRole to set
     */
    public void setAdminRole(boolean adminRole) {
        this.adminRole = adminRole;
    }

    /**
     * @return the test
     */
    public boolean isTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(boolean test) {
        this.test = test;
    }

    /**
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(String to) {
        this.to = to;
    }

}
