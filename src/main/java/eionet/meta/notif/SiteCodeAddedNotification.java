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

/**
 * Notification object for sending data to UNS.
 *
 * @author Enriko Käsper
 */
public class SiteCodeAddedNotification {

    private String username;
    private String createdTime;
    private int nofAddedCodes;
    private int newCodesStartIdentifier;
    private int newCodesEndIdentifier;
    private int totalNumberOfAvailableCodes;
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
     * @return the nofAddedCodes
     */
    public int getNofAddedCodes() {
        return nofAddedCodes;
    }

    /**
     * @param nofAddedCodes
     *            the nofAddedCodes to set
     */
    public void setNofAddedCodes(int nofAddedCodes) {
        this.nofAddedCodes = nofAddedCodes;
    }

    /**
     * @return the newCodesStartIdentifier
     */
    public int getNewCodesStartIdentifier() {
        return newCodesStartIdentifier;
    }

    /**
     * @param newCodesStartIdentifier
     *            the newCodesStartIdentifier to set
     */
    public void setNewCodesStartIdentifier(int newCodesStartIdentifier) {
        this.newCodesStartIdentifier = newCodesStartIdentifier;
    }

    /**
     * @return the newCodesEndIdentifier
     */
    public int getNewCodesEndIdentifier() {
        return newCodesEndIdentifier;
    }

    /**
     * @param newCodesEndIdentifier
     *            the newCodesEndIdentifier to set
     */
    public void setNewCodesEndIdentifier(int newCodesEndIdentifier) {
        this.newCodesEndIdentifier = newCodesEndIdentifier;
    }

    /**
     * @return the totalNumberOfAvailableCodes
     */
    public int getTotalNumberOfAvailableCodes() {
        return totalNumberOfAvailableCodes;
    }

    /**
     * @param totalNumberOfAvailableCodes
     *            the totalNumberOfAvailableCodes to set
     */
    public void setTotalNumberOfAvailableCodes(int totalNumberOfAvailableCodes) {
        this.totalNumberOfAvailableCodes = totalNumberOfAvailableCodes;
    }

    /**
     * @return the createdTime
     */
    public String getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createdTime
     *            the createdTime to set
     */
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
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
