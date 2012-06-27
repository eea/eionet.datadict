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

/**
 * Result object for schema conversions.
 *
 * @author Juhan Voolaid
 */
public class SchemaConversionsData {

    private int numberOfConversions;

    private int numberOfQAScripts;

    private String xmlConvUrl;

    /**
     * @return the numberOfConversions
     */
    public int getNumberOfConversions() {
        return numberOfConversions;
    }

    /**
     * @param numberOfConversions
     *            the numberOfConversions to set
     */
    public void setNumberOfConversions(int numberOfConversions) {
        this.numberOfConversions = numberOfConversions;
    }

    /**
     * @return the numberOfQAScripts
     */
    public int getNumberOfQAScripts() {
        return numberOfQAScripts;
    }

    /**
     * @param numberOfQAScripts
     *            the numberOfQAScripts to set
     */
    public void setNumberOfQAScripts(int numberOfQAScripts) {
        this.numberOfQAScripts = numberOfQAScripts;
    }

    /**
     * @return the xmlConvUrl
     */
    public String getXmlConvUrl() {
        return xmlConvUrl;
    }

    /**
     * @param xmlConvUrl
     *            the xmlConvUrl to set
     */
    public void setXmlConvUrl(String xmlConvUrl) {
        this.xmlConvUrl = xmlConvUrl;
    }

}
