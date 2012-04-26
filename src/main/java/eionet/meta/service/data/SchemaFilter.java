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
 * Schema search filter.
 *
 * @author Juhan Voolaid
 */
public class SchemaFilter extends PagedRequest {

    private String fileName;

    private String schemaSetIdentifier;

    private String regStatus;

    /** Dynamic search attributes. */
    private List<Attribute> attributes;

    public boolean isValued() {
        if (StringUtils.isNotEmpty(fileName)) {
            return true;
        }
        if (StringUtils.isNotEmpty(schemaSetIdentifier)) {
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
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the schemaSetIdentifier
     */
    public String getSchemaSetIdentifier() {
        return schemaSetIdentifier;
    }

    /**
     * @param schemaSetIdentifier
     *            the schemaSetIdentifier to set
     */
    public void setSchemaSetIdentifier(String schemaSetIdentifier) {
        this.schemaSetIdentifier = schemaSetIdentifier;
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
