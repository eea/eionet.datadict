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

import java.util.Arrays;
import java.util.List;

/**
 * Registration status.
 *
 * @author Juhan Voolaid
 */
public enum RegStatus {

    /** */
    DRAFT("Draft"), PUBLIC_DRAFT("Public draft"), RELEASED("Released");

    /** */
    String s;

    /**
     *
     * @param s
     */
    RegStatus(String s) {
        this.s = s;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return s;
    }

    /**
     * Returns label.
     *
     * @return
     */
    public String getLabel() {
        return s;
    }

    /**
     *
     * @param s
     * @return
     */
    public static RegStatus fromString(String s) {
        for (RegStatus regStatus : RegStatus.values()) {
            if (regStatus.toString().equals(s)) {
                return regStatus;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public static RegStatus getDefault() {
        return RegStatus.DRAFT;
    }

    /**
     * Returns the list of public statuses that can be displayed for anonymous users.
     *
     * @return List of RegStatues objects converted to String.
     */
    public static List<String> getPublicStatuses() {
        return Arrays.asList(RegStatus.RELEASED.toString(), RegStatus.PUBLIC_DRAFT.toString());
    }
}
