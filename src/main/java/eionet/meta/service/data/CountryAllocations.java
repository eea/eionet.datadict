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

import eionet.meta.dao.domain.FixedValue;

/**
 * Data about site code allocations for country.
 *
 * @author Juhan Voolaid
 */
public class CountryAllocations {

    private FixedValue country;

    /** Number of site codes in status: assigned, deleted, disapared. */
    private int usedCodes;

    /** Number of site codes in status: allocated. */
    private int unusedCodes;

    /**
     * @return the country
     */
    public FixedValue getCountry() {
        return country;
    }

    /**
     * @param country
     *            the country to set
     */
    public void setCountry(FixedValue country) {
        this.country = country;
    }

    /**
     * @return the usedCodes
     */
    public int getUsedCodes() {
        return usedCodes;
    }

    /**
     * @param usedCodes
     *            the usedCodes to set
     */
    public void setUsedCodes(int usedCodes) {
        this.usedCodes = usedCodes;
    }

    /**
     * @return the unusedCodes
     */
    public int getUnusedCodes() {
        return unusedCodes;
    }

    /**
     * @param unusedCodes
     *            the unusedCodes to set
     */
    public void setUnusedCodes(int unusedCodes) {
        this.unusedCodes = unusedCodes;
    }

}
