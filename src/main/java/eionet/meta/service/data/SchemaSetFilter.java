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

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.Attribute;

/**
 * Schema set search filter.
 *
 * @author Juhan Voolaid
 */
public class SchemaSetFilter extends PagedRequest {

    /** */
    private String identifier;

    /** */
    private String regStatus;

    /** */
    private String searchingUser;

    /** Dynamic search attributes. */
    private List<Attribute> attributes;

    /**
     *
     * @return
     */
    public boolean isValued() {
        if (StringUtils.isNotEmpty(identifier)) {
            return true;
        }
        if (StringUtils.isNotEmpty(regStatus)) {
            return true;
        }
        if (isAttributesValued()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public boolean isAttributesValued() {
        if (attributes != null && attributes.size() > 0) {
            for (Attribute a : attributes) {
                if (StringUtils.isNotEmpty(a.getValue())) {
                    return true;
                }
            }
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

    /**
     * @return the attributes
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the searchingUser
     */
    public String getSearchingUser() {
        return searchingUser;
    }

    /**
     * @param searchingUser the searchingUser to set
     */
    public void setSearchingUser(String searchingUser) {
        this.searchingUser = searchingUser;
    }

}
