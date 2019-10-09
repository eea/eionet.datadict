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

package eionet.web;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;

/**
 * Extension of stripes ActionBeanContext.
 *
 * @author Juhan Voolaid
 */
public class DDActionBeanContext extends ActionBeanContext {

    /** Page resolution. */
    private Resolution sourcePageResolution;

    /**
     * Wrapper method for {@link javax.servlet.ServletRequest.ServletRequest#getParameter(String)}.
     * <p>
     * The wrapper allows to avoid direct usage of {@link javax.servlet.http.HttpServletRequest}.
     *
     * @param parameterName
     *            parameter name.
     * @return corresponding parameter value from {@link javax.servlet.http.HttpServletRequest}.
     */
    public String getRequestParameter(String parameterName) {
        return getRequest().getParameter(parameterName);
    }

    /**
     * Wrapper method for {@link javax.servlet.http.HttpSession#setAttribute(String, eionet.cr.dto.ObjectDTO)}.
     * <p>
     * The wrapper allows to avoid direct usage of {@link javax.servlet.http.HttpSession}.
     *
     * @param name
     *            session attribute name.
     * @param value
     *            session attribute value.
     */
    public void setSessionAttribute(String name, Object value) {
        getRequest().getSession().setAttribute(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resolution getSourcePageResolution() {

        if (this.sourcePageResolution != null) {
            return this.sourcePageResolution;
        } else {
            return super.getSourcePageResolution();
        }
    }

    /**
     *
     * @param resolution
     */
    public void setSourcePageResolution(Resolution resolution) {
        this.sourcePageResolution = resolution;
    }

    /**
     * Gets application init parameter.
     *
     * @param key
     * @return String
     */
    public String getInitParameter(String key) {
        return getServletContext().getInitParameter(key);
    }
}
