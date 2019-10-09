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

package eionet.meta.dao.domain;

/**
 * Status codes used for site codes.
 *
 * @author Enriko Käsper
 */
public enum SiteCodeStatus {

    /** Not allocated to any country. */
    AVAILABLE("Available"),
    /** Country has requested site code and it is allocated for this country. */
    ALLOCATED("Allocated"),
    /** Site code has been allocated for particular site name. */
    ASSIGNED("Assigned"),
    /** Site code was not reported in the CDDA dataset. */
    DISAPPEARED("Disappeared"),
    /** Site code has been flagged for deletion by the data reporter. */
    DELETED("Deleted");

    /** Label. */
    private String label;

    /**
     * Class constructor.
     *
     * @param name
     */
    private SiteCodeStatus(String label) {
        this.label = label;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

}
