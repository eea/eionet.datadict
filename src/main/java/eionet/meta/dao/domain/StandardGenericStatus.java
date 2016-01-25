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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
     * Every state should be greater than 0 (at least one bit "1") for "and" (&) bit masking operation.
     * Because, "0" will consume everything.
     *
     * <pre>
     *      128 | 64 | 32 | 16 | 8 | 4 | 2 | 1 | Dec | Code
     *      -----------------------------------------------
     *        0 |  1 |  0 |  0 | 0 | 0 | 0 | 0 |  64 | Not accepted
     *        0 |  1 |  0 |  1 | 0 | 0 | 0 | 1 |  81 | Submitted
     *        0 |  1 |  1 |  0 | 0 | 0 | 0 | 1 |  97 | Reserved
     *        0 |  1 |  1 |  1 | 0 | 0 | 0 | 1 | 113 | Invalid
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
    NOT_ACCEPTED("Not accepted", "notAccepted", 64),
    SUBMITTED("Submitted", "submitted", 81),
    RESERVED("Reserved", "reserved", 97),
    INVALID("Invalid", "invalid", 113),
    ACCEPTED("Accepted", "accepted", 128),
    VALID("Valid", "valid", 145),
    VALID_STABLE("Valid - stable", "stable", 147),
    VALID_EXPERIMENTAL("Valid - experimental", "experimental", 149),
    DEPRECATED("Deprecated", "deprecated", 161),
    DEPRECATED_RETIRED("Deprecated - retired", "retired", 163),
    DEPRECATED_SUPERSEDED("Deprecated - superseded", "superseded", 165);
    // @formatter:on

    /** Bit mask for UI status types (LSB is 1). */
    public static final int UI_ELEMENTS_MASK = 1;

    /**
     * Bit mask to determine when a status is changed from accepted to not accepted (or vice versa) or in same set. Value is
     * 11000000. So it consumes all 6 least significant bits and preserves SET BITS (ACCEPTED and NOT_ACCEPTED). After anding with this
     * mask, if result is greater than 0 then in same set.
     */
    public static final int IN_SAME_SET_MASK = 192;

    /** Bit mask to match all status types. */
    public static final int ALL_MASK = 255;

    /** The status's skos:prefLabel. */
    private String label;

    /** The status's skos:notation. */
    private String notation;

    /** The status's numeric value as explained above. */
    private int value;

    /**
     * Default private constructor.
     *
     * @param prefLabel The status's skos:prefLabel.
     * @param notation The status's skos:notation.
     * @param value The status's numeric value as explained above.
     */
    private StandardGenericStatus(String prefLabel, String notation, int value) {

        if (StringUtils.isBlank(prefLabel) || StringUtils.isBlank(notation)) {
            throw new IllegalArgumentException("Preferred label and notation must not be blank!");
        }

        if (value < 0) {
            throw new IllegalArgumentException("Numeric value must be >= 0!");
        }

        this.label = prefLabel;
        this.notation = notation;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return this.label;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the notation.
     *
     * @return the notation
     */
    public String getNotation() {
        return notation;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns if status is super status of given status.
     *
     * @param of given status
     * @return boolean
     */
    public boolean isSuperStatus(StandardGenericStatus of) {
        return this.value == (this.value & of.value);
    }

    /**
     * Returns if status is a sub status of given status.
     *
     * @param of given status
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
    public boolean isValid() {
        return this.isSubStatus(StandardGenericStatus.VALID);
    }

    /**
     * Utility method to check a status is accepted.
     *
     * @return if status is accepted or not
     */
    public boolean isAccepted() {
        return this.isSubStatus(StandardGenericStatus.ACCEPTED);
    }

    /**
     * Returns if a status is opposite of another. This comparison is done in ACCEPTED and NOT_ACCEPTED sets.
     *
     * @param with comparing status
     * @return if they are in same set or not same set
     */
    public boolean isSameSet(StandardGenericStatus with) {
        return (this.value & with.value & IN_SAME_SET_MASK) > 0;
    }

    /**
     * Static method to query enum from integer value.
     *
     * @param value integer value of enum
     * @return found enum or null
     */
    public static StandardGenericStatus fromValue(int value) {
        for (StandardGenericStatus status : StandardGenericStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    /**
     * Apply UI mask and returns statuses.
     *
     * @return list of statuses which user can set from UI.
     */
    public static StandardGenericStatus[] uiValues() {
        StandardGenericStatus[] values = StandardGenericStatus.values();
        List<StandardGenericStatus> uiValues = new ArrayList<StandardGenericStatus>();
        for (StandardGenericStatus status : values) {
            if ((status.getValue() & UI_ELEMENTS_MASK) == UI_ELEMENTS_MASK) {
                uiValues.add(status);
            }
        }

        return uiValues.toArray(new StandardGenericStatus[uiValues.size()]);
    }

    /**
     * A helper method to return enum values as list.
     * @return All enum values as list.
     */
    public static List<StandardGenericStatus> valuesAsList(){
        return Arrays.asList(StandardGenericStatus.values());
    }
}
