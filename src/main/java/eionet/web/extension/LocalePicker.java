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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.web.extension;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.localization.DefaultLocalePicker;

/**
 * Custom locale picker that overrides {@link DefaultLocalePicker}.
 *
 * @author Jaanus Heinlaid
 *
 */
public class LocalePicker extends DefaultLocalePicker {

    /**
     * @see net.sourceforge.stripes.localization.DefaultLocalePicker#pickCharacterEncoding(javax.servlet.http.HttpServletRequest, java.util.Locale)
     */
    @Override
    public String pickCharacterEncoding(HttpServletRequest request, Locale locale) {

        String encoding = super.pickCharacterEncoding(request, locale);
        return encoding == null ? "UTF-8" : null;
    }
}
