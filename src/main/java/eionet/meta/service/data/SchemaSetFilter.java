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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;
import eionet.meta.dao.domain.ComplexAttributeField;

/**
 * Schema set search filter.
 *
 * @author Juhan Voolaid
 */
public class SchemaSetFilter extends PagedRequest implements IObjectWithDynamicAttrs {

    /** */
    private String identifier;

    /** */
    private String regStatus;

    /** */
    private List<String> regStatuses;

    /** */
    private String searchingUser;
    
    /** Dynamic search attributes. */
    private List<Attribute> attributes;

    /** Dynamic search complex attributes. */
    private List<ComplexAttribute> complexAttributes;

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
        if (CollectionUtils.isNotEmpty(regStatuses)) {
            return true;
        }
        if (isAttributesValued()) {
            return true;
        }
        if (isComplexAttributesValued()) {
            return true;
        }
        return false;
    }

    /**
     * Check if any field in attributes has value.
     * @return true if attribute field has value.
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
     * Check if any field in complex attributes has value.
     * @return true if complex attribute field has value.
     */
    public boolean isComplexAttributesValued() {

        if (this.complexAttributes != null && this.complexAttributes.size() > 0) {
            for (ComplexAttribute a : this.complexAttributes) {
                if (a.getFields() != null) {
                    for (ComplexAttributeField field : a.getFields()) {
                        if (StringUtils.isNotEmpty(field.getValue())) {
                            return true;
                        }
                    }
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
    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    @Override
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

    /**
     * @return the complexAttributes
     */
    @Override
    public List<ComplexAttribute> getComplexAttributes() {
        return complexAttributes;
    }

    /**
     * @param complexAtributes the complexAttributes to set
     */
    @Override
    public void setComplexAttributes(List<ComplexAttribute> complexAttributes) {
        this.complexAttributes = complexAttributes;
    }

    /**
     * @return the regStatuses
     */
    public List<String> getRegStatuses() {
        return regStatuses;
    }

    /**
     * @param regStatuses the regStatuses to set
     */
    public void setRegStatuses(List<String> regStatuses) {
        this.regStatuses = regStatuses;
    }
}
