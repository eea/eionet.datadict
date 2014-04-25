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

package eionet.meta.dao.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * Vocabulary concept.
 *
 * @author Juhan Voolaid
 */
public class VocabularyConcept {

    /** Properties. */
    private int id;
    private String identifier;
    private String label;
    private String definition;
    private String notation;
    private Date created;
    private Date obsolete;

    /** parent vocabulary identifier. */
    private int vocabularyId;

    /** Attributes. */
    private List<List<DataElement>> elementAttributes;

    /** vocabulary Label. */
    private String vocabularyLabel;

    /** vocabulary Set id. */
    private int vocabularySetId;

    /** vocabulary Set label. */
    private String vocabularySetLabel;
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
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
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
     * @return the notation
     */
    public String getNotation() {
        return notation;
    }

    /**
     * @param notation
     *            the notation to set
     */
    public void setNotation(String notation) {
        this.notation = notation;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created
     *            the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return the obsolete
     */
    public Date getObsolete() {
        return obsolete;
    }

    /**
     * @param obsolete
     *            the obsolete to set
     */
    public void setObsolete(Date obsolete) {
        this.obsolete = obsolete;
    }

    /**
     * @return the elementAttributes
     */
    public List<List<DataElement>> getElementAttributes() {
        return elementAttributes;
    }

    /**
     * @param elementAttributes
     *            the elementAttributes to set
     */
    public void setElementAttributes(List<List<DataElement>> elementAttributes) {
        this.elementAttributes = elementAttributes;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id).append("identifier", identifier)
                .append("label", label).append("definition", definition).append("notation", notation).toString();
    }

    public int getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(int vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public String getVocabularyLabel() {
        return vocabularyLabel;
    }

    public void setVocabularyLabel(String vocabularyLabel) {
        this.vocabularyLabel = vocabularyLabel;
    }

    public String getVocabularySetLabel() {
        return vocabularySetLabel;
    }

    public void setVocabularySetLabel(String vocabularySetLabel) {
        this.vocabularySetLabel = vocabularySetLabel;
    }

    public int getVocabularySetId() {
        return vocabularySetId;
    }

    public void setVocabularySetId(int vocabularySetId) {
        this.vocabularySetId = vocabularySetId;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        VocabularyConcept rhs = (VocabularyConcept) obj;
        return new EqualsBuilder().append(id, rhs.id).append(identifier, rhs.identifier).append(label, rhs.label)
                .append(definition, rhs.definition).append(notation, rhs.notation).isEquals();
    }
}
