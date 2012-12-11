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

import java.util.ArrayList;
import java.util.List;

/**
 * Complex Attribute.
 *
 * @author Enriko Käsper
 */
public class ComplexAttribute {

    /** */
    private int id;

    /** */
    private String name;

    /** */
    private List<ComplexAttributeField> fields;

    /** */
    private String shortName;

    public ComplexAttribute(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public ComplexAttribute() {
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComplexAttribute other = (ComplexAttribute) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName
     *            the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the fields
     */
    public List<ComplexAttributeField> getFields() {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(List<ComplexAttributeField> fields) {
        this.fields = fields;
    }

    /**
     * @param adds field into the list of complex attribute fields
     */
    public void addField(ComplexAttributeField field) {
        if (this.fields == null) {
            this.fields = new ArrayList<ComplexAttributeField>();
        }
        this.fields.add(field);
    }

    /**
     * Sets complex attribute field value
     * @param string
     * @param obligationId
     */

    public void setFieldValue(String name, String value) {
        if (this.fields != null) {
            for (ComplexAttributeField field : this.fields) {
                if (field.getName().equals(name)) {
                    field.setValue(value);
                }
            }
        }
    }

    /**
     * Returns complex attribute field by field name
     * @param name
     * @return
     */
    public ComplexAttributeField getField(String name) {
        if (this.fields != null) {
            for (ComplexAttributeField field : this.fields) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
        }
        return null;
    }
}
