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
 * Schema search filter.
 *
 * @author Juhan Voolaid
 */
public class SchemaFilter extends PagedRequest {

    private String fileName;

    private String schemaSetIdentifier;

    public boolean isValued() {
        if (StringUtils.isNotEmpty(fileName)) {
            return true;
        }
        if (StringUtils.isNotEmpty(schemaSetIdentifier)) {
            return true;
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

}
