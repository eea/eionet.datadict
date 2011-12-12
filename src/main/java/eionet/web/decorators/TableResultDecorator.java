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

package eionet.web.decorators;

import javax.servlet.http.HttpServletRequest;

import org.displaytag.decorator.TableDecorator;

import eionet.dao.domain.DataSetTable;
import eionet.meta.DDUser;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;

/**
 * Table result table decorator.
 *
 * @author Juhan Voolaid
 */
public class TableResultDecorator extends TableDecorator {

    public String getName() {
        DataSetTable table = (DataSetTable) getCurrentRowObject();
        String status = table.getDataSetStatus();
        boolean clickable = false;

        if (status != null) {
            DDUser user = SecurityUtil.getUser((HttpServletRequest) getPageContext().getRequest());
            if (user != null && user.isAuthentic()) {
                if (status.equals("Incomplete") || status.equals("Candidate") || status.equals("Qualified")) {
                    clickable = true;
                }
            }
        }

        if (clickable) {

            String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
            jspUrlPrefix = jspUrlPrefix==null ? "" : jspUrlPrefix;

            String url = jspUrlPrefix + "/tables/" + table.getId();
            return "<a href=\"" + url + "\">" + table.getName() + "</a>";
        }

        return table.getName();
    }
}
