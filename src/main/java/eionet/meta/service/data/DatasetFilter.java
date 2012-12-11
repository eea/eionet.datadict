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

package eionet.meta.service.data;

import java.util.List;

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;

/**
 * Dataset search filter.
 *
 * @author Enriko Käsper
 */
public class DatasetFilter implements IObjectWithDynamicAttrs {

    private String shortName;

    private String identifier;

    private List<Attribute> attributes;

    private List<ComplexAttribute> complexAttributes;

    private List<String> regStatuses;

    /** List of ROD obligation IDs. */
    private List<Integer> rodIds;

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
     * @return the complexAttributes
     */
    @Override
    public List<ComplexAttribute> getComplexAttributes() {
        return complexAttributes;
    }

    /**
     * @param complexAttributes the complexAttributes to set
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

    /**
     * @return the rodIds
     */
    public List<Integer> getRodIds() {
        return rodIds;
    }

    /**
     * @param rodIds the rodIds to set
     */
    public void setRodIds(List<Integer> rodIds) {
        this.rodIds = rodIds;
    }

}
