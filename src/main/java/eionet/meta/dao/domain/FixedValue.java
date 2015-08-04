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
 *        Raptis Dimos
 */

package eionet.meta.dao.domain;

import org.apache.commons.lang.StringUtils;

/**
 * Fixed value of data element.
 *
 * @author Juhan Voolaid
 */
public class FixedValue {
    
    public static enum OwnerType {
        DATA_ELEMENT("elem"),
        ATTRIBUTE("attr");
        
        private final String value;
        
        private OwnerType(String value) {
            this.value = value;
        }
        
        public boolean isMatch(String value) {
            return this.value.equalsIgnoreCase(value);
        }

        @Override
        public String toString() {
            return this.value;
        }
        
        public static OwnerType parse(String value) {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            
            for (OwnerType type : OwnerType.values()) {
                if (type.isMatch(value)) {
                    return type;
                }
            }
            
            throw new IllegalArgumentException();
        }
    }

    private int id;

    private int ownerId;

    private String ownerType;

    private String value;

    private String shortDescription;

    private String definition;
    
    private boolean defaultValue;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the ownerId
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId
     *            the ownerId to set
     */
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return the ownerType
     */
    public String getOwnerType() {
        return ownerType;
    }

    /**
     * @param ownerType
     *            the ownerType to set
     */
    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * @param shortDescription
     *            the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition
     *            the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    /**
     * Label to display in the UI.
     * @return code with Short definition. if short def is empty then definition is shown.
     */
    public String getLabel() {
        String label = StringUtils.isNotEmpty(shortDescription) ? shortDescription : definition;

        return value + " [" + label + "]";
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
    
}
