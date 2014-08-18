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
 *        TripleDev
 */

package eionet.meta.dao.domain;

/**
 * Standard generic status which is compatible with: http://inspire.ec.europa.eu/registry/status/ and based on the principles in ISO
 * 19135. The specification operates with two base states: Accepted and Not accepted, and all selectable statuses are refinements of
 * these:
 *
 * <pre>
 *      notAccepted ----> submitted
 *         +------------> reserved
 *         +------------> invalid
 *      accepted -------> valid -------> stable
 *         |                 +---------> experimental
 *         +------------> deprecated --> superseded
 *                           +---------> retired
 * </pre>
 *
 * @author enver
 */
public enum StandardGenericStatus {

    // @formatter:off
    /**
     * Enum declarations.
     *
     * <pre>
     *      128 | 64 | 32 | 16 | 8 | 4 | 2 | 1 | Dec | Code
     *      -----------------------------------------------
     *        0 |  0 |  0 |  0 | 0 | 0 | 0 | 0 |   0 | Not accepted
     *        0 |  0 |  0 |  1 | 0 | 0 | 0 | 1 |  17 | Submitted
     *        0 |  0 |  1 |  0 | 0 | 0 | 0 | 1 |  33 | Reserved
     *        0 |  0 |  1 |  1 | 0 | 0 | 0 | 1 |  49 | Invalid
     *        1 |  0 |  0 |  0 | 0 | 0 | 0 | 0 | 128 | Accepted
     *        1 |  0 |  0 |  1 | 0 | 0 | 0 | 1 | 145 | Valid
     *        1 |  0 |  0 |  1 | 0 | 0 | 1 | 1 | 147 | Valid - stable
     *        1 |  0 |  0 |  1 | 0 | 1 | 0 | 1 | 149 | Valid - experimental
     *        1 |  0 |  1 |  0 | 0 | 0 | 0 | 1 | 161 | Deprecated
     *        1 |  0 |  1 |  0 | 0 | 0 | 1 | 1 | 163 | Deprecated - retired
     *        1 |  0 |  1 |  0 | 0 | 1 | 0 | 1 | 165 | Deprecated - superseded
     * </pre>
     *
     */
    NOT_ACCEPTED("Not accepted", 0),
    SUBMITTED("Submitted", 17),
    RESERVED("Reserved", 33),
    INVALID("Invalid", 49),
    ACCEPTED("Accepted", 128),
    VALID("Valid", 145),
    VALID_STABLE("Valid - stable", 147),
    VALID_EXPERIMENTAL("Valid - experimental", 149),
    DEPRECATED("Deprecated", 161),
    DEPRECATED_RETIRED("Deprecated - retired", 163),
    DEPRECATED_SUPERSEDED("Deprecated - superseded", 165);
    // @formatter:on

    /**
     * Label for enum.
     */
    private String label;
    /**
     * Value for enum.
     */
    private int value;

    /**
     * Default private constructor.
     *
     * @param label
     *            label of enum
     * @param value
     *            value of enum
     */
    private StandardGenericStatus(String label, int value) {
        this.label = label;
        this.value = value;
    } // end of constructor

    @Override
    public String toString() {
        return this.label;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    /**
     * Returns if status is super status of given status.
     *
     * @param of
     *            given status
     * @return boolean
     */
    public boolean isSuperStatus(StandardGenericStatus of) {
        return this.value == (this.value & of.value);
    }

    /**
     * Returns if status is a sub status of given status.
     *
     * @param of
     *            given status
     * @return boolean
     */
    public boolean isSubStatus(StandardGenericStatus of) {
        return of.value == (this.value & of.value);
    }

    /**
     * Utility method to check a status is valid.
     *
     * @return if status is valid or not
     */
    public boolean isValid(){
        return this.isSubStatus(StandardGenericStatus.VALID);
    } //end of method isValid

    /**
     * Static method to query enum from integer value.
     *
     * @param value
     *            integer value of enum
     * @return found enum or null
     */
    public static StandardGenericStatus fromValue(int value) {
        for (StandardGenericStatus status : StandardGenericStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    } // end of static method StandardGenericStatus

} // end of enum StandardGenericStatus
