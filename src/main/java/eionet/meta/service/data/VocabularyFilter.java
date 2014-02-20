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
 *        Kaido Laine
 */

package eionet.meta.service.data;

import eionet.meta.dao.domain.RegStatus;

/**
 *
 * Filter for searching vocabularies.
 *
 * @author Kaido Laine
 */
public class VocabularyFilter extends PagedRequest {


    /** Text search value. */
    private String text;

    /** filter by working copy status. */
    private Boolean workingCopy;

    /** text to be searched in the concepts of the vocabulary. */
    private String conceptText;

    /** vocabulary status. */
    private RegStatus status;

    /** if false perform full text search otherwise exact match .*/
    private boolean exactMatch = false;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean isWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(Boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public String getConceptText() {
        return conceptText;
    }

    public void setConceptText(String conceptText) {
        this.conceptText = conceptText;
    }

    public void setStatus(RegStatus status) {
        this.status = status;
    }

    public RegStatus getStatus() {
        return status;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

}
