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
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.meta.dao.domain;

import java.util.Date;

/**
 * Domain object for API_KEY table.
 *
 * @author enver
 */
public class DDApiKey {
    /**
     * IDENTIFIER column for API key.
     */
    private String identifier;
    /**
     * SCOPE column for API key.
     */
    private String scope;
    /**
     * KEY_VALUE column for API key.
     */
    private String keyValue;

    /**
     * EXPIRES column for API key.
     */
    private Date expires;
    /**
     * NOTES column for API key.
     */
    private String notes;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String key) {
        this.keyValue = key;
    }


    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}//end of class DDApiKey
