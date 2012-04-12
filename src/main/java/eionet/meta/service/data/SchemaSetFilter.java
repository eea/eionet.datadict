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

import org.apache.commons.lang.StringUtils;

/**
 * Schema set search filter.
 *
 * @author Juhan Voolaid
 */
public class SchemaSetFilter extends PagedRequest {

    private String identifier;

    private String regStatus;

    public boolean isValued() {
        if (StringUtils.isNotEmpty(identifier)) {
            return true;
        }
        if (StringUtils.isNotEmpty(regStatus)) {
            return true;
        }
        return false;
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
     * @return the regStatus
     */
    public String getRegStatus() {
        return regStatus;
    }

    /**
     * @param regStatus
     *            the regStatus to set
     */
    public void setRegStatus(String regStatus) {
        this.regStatus = regStatus;
    }

}
