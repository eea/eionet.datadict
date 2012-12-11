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
 *        Enriko Käsper
 */

package eionet.meta.dao.domain;

/**
 * Registration status.
 *
 * @author Enriko Käsper
 */
public enum DatasetRegStatus {

    /** */
    INCOMPLETE("Incomplete"), CANDIDATE("Candidate"), RECORDED("Recorded"), QUALIFIED("Qualified"), RELEASED("Released");
    /** */
    String s;

    /**
     *
     * @param s
     */
    DatasetRegStatus(String s) {
        this.s = s;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return s;
    }

    /**
     *
     * @param s
     * @return
     */
    public static DatasetRegStatus fromString(String s) {
        for (DatasetRegStatus regStatus : DatasetRegStatus.values()) {
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
    public static DatasetRegStatus getDefault() {
        return DatasetRegStatus.INCOMPLETE;
    }
}
