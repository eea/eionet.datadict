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

package eionet.web.action;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.meta.service.ServiceException;

/**
 * Error page action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/error.action")
public class ErrorActionBean extends AbstractActionBean {

    /** Error type key identifier. */
    public static final String ERROR_TYPE_KEY = "eionet.web.action.ErrorActionBean.ErrorType";

    /** Possible Error Types. */
    public static enum ErrorType {
        UNKNOWN, NOT_FOUND_404, INVALID_INPUT
    };

    /** Error message. */
    private String message;
    /** Error type. */
    private ErrorType type;

    @DefaultHandler
    public Resolution showError() throws ServiceException {
        switch (this.type) {
            // not found 404 errors are handled in DDExceptionHandler, this line probably wont be reached
            case NOT_FOUND_404:
                return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, this.message);
            case UNKNOWN:
            default:
                return new ForwardResolution("/pages/error.jsp");
        }
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorTypeMsg() {
        switch (this.type) {
            // not found 404 errors are handled in DDExceptionHandler, this line probably wont be reached
            case NOT_FOUND_404:
                return "404 Not Found";
            case INVALID_INPUT:
                return "An error has occurred due to invalid input";
            case UNKNOWN:
            default:
                return "An unexpected system error has occurred:";
        }
    }

    public void setType(ErrorType type) {
        if (type != null) {
            this.type = type;
        } else {
            this.type = ErrorType.UNKNOWN;
        }
    }
}
